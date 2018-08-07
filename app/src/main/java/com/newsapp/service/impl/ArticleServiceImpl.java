package com.newsapp.service.impl;


import com.newsapp.model.ArticleResponse;
import com.newsapp.service.interfaces.ArticleService;

import io.reactivex.Single;
import retrofit2.http.Query;

/**
 * Created by AQB Solutions PVT. LTD. on 8/6/18.
 */
public class ArticleServiceImpl extends RxServiceSessionMain {
    private ArticleService mArticleService;

    public ArticleServiceImpl() {
        mArticleService = getRetrofit().create(ArticleService.class);
    }


    /**
     *
     * @param country
     * @param pageSize
     * @param page
     * @return
     */
    public Single<ArticleResponse> getNews(@Query("country") String country,
                                           @Query("pageSize") int pageSize, @Query("page") int page, @Query("sortBy") String sortBy) {
        return mArticleService.getNews(country, pageSize, page,sortBy);
    }


    public Single<ArticleResponse> getNewArticles(@Query("country") String country,
                                           @Query("from") String from, @Query("to") String to, @Query("sortBy") String sortBy) {
        return mArticleService.getNewArticles(country, from, to ,sortBy);
    }


}
