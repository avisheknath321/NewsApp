package com.newsapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import com.newsapp.AppController;
import com.newsapp.R;
import com.newsapp.filter.SearchFilter;
import com.newsapp.model.Article;
import com.newsapp.ui.activity.MainActivity;
import com.newsapp.util.TimeAgo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ArticleAdapter extends Adapter<ArticleAdapter.ItemVH> implements Filterable, MainActivity.OnFilterListener {

    private final Context mContext;
    private RequestManager mRequestManager;
    private List<Article> mData;
    private List<Article> filterList;
    private SearchFilter mFilter;
    private boolean desc;

    public ArticleAdapter(Context mContext, RequestManager mRequestManager, List<Article> mData) {
        this.mContext = mContext;
        this.mRequestManager = mRequestManager;
        this.mData = mData;
        this.filterList = mData;

        ((MainActivity) mContext).setOnFilterListener(this);
    }

    @NonNull
    @Override
    public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemVH(LayoutInflater.from(mContext).inflate(R.layout.item_article, parent, false));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull ItemVH holder, int position) {
        Article article = mData.get(holder.getAdapterPosition());
        holder.titleText.setText(article.getTitle());
        holder.tvTime.setText(new TimeAgo(mContext).timeAgo(article.getPublishedAt()));
        holder.cardView.setTag(article);
        holder.tvDescription.setText(article.getDescription());
        mRequestManager.load(article.getUrlToImage())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(80, 80).centerCrop())
                .into(holder.mediaImage);

        RxView.clicks(holder.cardView)
                .subscribe(o -> {
                    Article anArticle = (Article) holder.cardView.getTag();
                    anArticle.setVisited(true);
                    String url = article.getUrl();
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    AppController.getContext().startActivity(browserIntent);
                    holder.cardView.setAlpha(.5f);
                });

        if (article.isVisited())
            holder.cardView.setAlpha(.5f);
        else
            holder.cardView.setAlpha(1f);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Add item and then notify
     *
     * @param article
     */
    public void addItem(Article article) {
        mData.add(article);
        notifyDataSetChanged();
    }

    /**
     * Add multiple items at once
     *
     * @param articles
     */
    @SuppressLint("CheckResult")
    public void addAll(List<Article> articles) {
        mData.addAll(articles);
        notifyDataSetChanged();

    }

    @SuppressLint("CheckResult")
    public void setFilteredItem(List<Article> articles) {
        //mData.clear();
        mData = articles;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SearchFilter(filterList, this);
        }
        return mFilter;
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("CheckResult")
    @Override
    public void onFilterToggle(boolean desc) {
        this.desc = desc;
        sort().observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
            clear();
            addAll(o);
            notifyDataSetChanged();
        });
    }

    /**
     * Sort list according to the user s
     * @return
     */
    private Single<List<Article>> sort() {
        return Observable.fromIterable(mData)
                .toSortedList((a1, a2) -> {
                    if (!desc)
                        return (a1.getPublishedAt().getTime() > a2.getPublishedAt().getTime() ? -1 : 1);     //descending
                    else
                        return (a1.getPublishedAt().getTime() > a2.getPublishedAt().getTime() ? 1 : -1);     //ascending
                })
                .subscribeOn(Schedulers.computation());
    }

    class ItemVH extends RecyclerView.ViewHolder {
        @BindView(R.id.card_view)
        CardView cardView;
        @BindView(R.id.title_text)
        TextView titleText;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.media_image)
        ImageView mediaImage;
        @BindView(R.id.tv_description)
        TextView tvDescription;

        ItemVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
