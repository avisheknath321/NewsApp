package com.newsapp.service.interfaces;

import com.newsapp.model.ArticleResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ArticleService {

    @Headers("Content-Type:application/json")
    @GET("top-headlines")
    Single<ArticleResponse> getNews(@Query("country") String country,
                                    @Query("pageSize") int pageSize, @Query("page") int page, @Query("sortBy") String sortBy);
    @Headers("Content-Type:application/json")
    @GET("top-headlines")
    Single<ArticleResponse> getNewArticles(@Query("country") String country,
                                           @Query("from") String from, @Query("to") String to, @Query("sortBy") String sortBy);
}
