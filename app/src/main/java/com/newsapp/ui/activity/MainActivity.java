package com.newsapp.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.newsapp.R;
import com.newsapp.adapter.ArticleAdapter;
import com.newsapp.model.Article;
import com.newsapp.model.ArticleResponse;
import com.newsapp.service.impl.ArticleServiceImpl;
import com.newsapp.util.logger.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import ru.alexbykov.nopaginate.paginate.NoPaginate;

import static com.newsapp.util.Common.startJobScheduler;
import static com.newsapp.util.Constants.ITEMS_PER_PAGE;


public class MainActivity extends AppCompatActivity implements SearchView.OnFocusChangeListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_articles)
    RecyclerView rvArticles;
    @BindView(R.id.srl)
    SwipeRefreshLayout srl;

    private CompositeDisposable disposable = new CompositeDisposable();
    private Unbinder unbinder;
    /**
     * these variables are used to calculate pagination
     */
    private static int currentPage = 1;
    private static int totalItems = 0;
    private static int totalPages = 0;
    /**
     * used for stopping any unwanted webservice call while
     * we engaged with another
     */
    private boolean isLoading = false;
    private NoPaginate noPaginate;
    private ArticleAdapter mAdapter;
    private List<Article> mArticles = new ArrayList<>();
    private ArticleServiceImpl mArticleService;
    private boolean filterDesc = false;
    private OnFilterListener onFilterListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {

        startJobScheduler();

        setSupportActionBar(toolbar);
        // configure recycler view
        setRecyclerView();

        // Make sure pagination options are set before making web service call
        getArticles();

        disposable.add(RxSwipeRefreshLayout.refreshes(srl)
                .subscribe(o -> {
                    resetConfigurations();
                    getArticles();
                    srl.setRefreshing(false);
                }));

    }


    /**
     * Reset everything
     */
    private void resetConfigurations() {
        // we need to clear everything when user wants to reload the page
        isLoading = false;
        totalPages = 0;
        currentPage = 1;
        totalItems = 0;
        mAdapter.clear();
        setRecyclerView();
    }


    private void setRecyclerView() {
        // make sure to detach the current adapter before adding a new one
        rvArticles.setAdapter(null);
        mAdapter = new ArticleAdapter(this, Glide.with(this), mArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));
        rvArticles.setAdapter(mAdapter);

        setPaginate();

    }

    /**
     * Configure pagination options
     */
    private void setPaginate() {
        noPaginate = NoPaginate.with(rvArticles)
                .setLoadingTriggerThreshold(5)
                .setOnLoadMoreListener(() -> {
                    // making sure that we do not call a web service when one request
                    // is being executed
                    if (!isLoading) {
                        // making sure we count pages before calling web service
                        if (totalPages >= currentPage) {
                            currentPage++;
                            getArticles();
                        } else {
                            // stop loading when we reach max available page
                            noPaginate.showLoading(false);
                        }
                    }
                })
                .build();

    }

    @SuppressLint("CheckResult")
    private void getArticles() {
        // make sure we make isLoading to true while starting a fresh api call
        isLoading = true;

        if (mArticleService == null) {
            mArticleService = new ArticleServiceImpl();
        }

        mArticleService.getNews("us", ITEMS_PER_PAGE, currentPage, "publishedAt")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArticleResponse>() {
                    @Override
                    public void onSuccess(ArticleResponse articleResponse) {
                        if (noPaginate != null) {
                            noPaginate.showLoading(false);
                        }
                        // calculated total pages from total items
                        // no need to do it for every api call
                        if (currentPage == 1) {
                            totalItems = articleResponse.getTotalResults();
                            totalPages = totalItems / ITEMS_PER_PAGE;
                        }
                        if (articleResponse.getStatus().equalsIgnoreCase("ok")) {
                            if (!articleResponse.getArticles().isEmpty()) {
                                mAdapter.addAll(articleResponse.getArticles());
                            }
                        }
                        // make sure we assign false to isLoading after every api call
                        isLoading = false;

                        if (totalPages == currentPage)
                            noPaginate.setNoMoreItems(true);


                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (noPaginate != null) {
                            noPaginate.showLoading(false);
                            noPaginate.showError(true);
                        }
                        // make sure we assign false to isLoading after every api call
                        isLoading = false;

                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextFocusChangeListener(this);
        disposable.add(
                RxSearchView.queryTextChangeEvents(searchView)
                        .doOnNext((searchViewQueryTextEvent) -> {
                            if (searchViewQueryTextEvent.isSubmitted()) {
                                myActionMenuItem.collapseActionView();
                            }
                        })
                        .skip(1)
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(searchViewQueryTextEvent -> {
                            Log.d("test", "QUERY: " + searchViewQueryTextEvent.queryText());

                            if (mAdapter != null && mAdapter.getFilter() != null)
                                mAdapter.getFilter().filter(searchViewQueryTextEvent.queryText());
                        })

        );


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            noPaginate.setNoMoreItems(false);
            return true;
        } else if (id == R.id.action_filter) {
            orderByDate(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void orderByDate(MenuItem item) {
        if (!filterDesc) {
            filterDesc = true;
            item.setIcon(R.drawable.ic_clear_filter);
        } else {
            filterDesc = false;
            item.setIcon(R.drawable.ic_filter);
        }

        if (onFilterListener != null)// sends notification when user selects filter option
            onFilterListener.onFilterToggle(filterDesc);

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        // when using search view, there is chance that recycler view's size changes, so we use
        // isLoading variable to decide whether to call a web service or not
        isLoading = b;
        noPaginate.setNoMoreItems(b);
    }

    ////////////////////////////////////  Interface ///////////////////////////////////
    public void setOnFilterListener(OnFilterListener onFilterListener) {
        this.onFilterListener = onFilterListener;
    }

    public interface OnFilterListener {
        void onFilterToggle(boolean desc);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind all views
        if (unbinder != null)
            unbinder.unbind();
        // to unsubscribe observer we must unbind
        if (noPaginate != null)
            noPaginate.unbind();

        if (!disposable.isDisposed())
            disposable.dispose();
    }
}
