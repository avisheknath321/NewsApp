package com.newsapp.service.impl;

import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.newsapp.AppController;
import com.newsapp.R;
import com.newsapp.util.Constants;
import com.newsapp.util.DateDeserializer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RxServiceSessionMain {

    private static String TAG = RxServiceSessionMain.class.getSimpleName();
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient;


    RxServiceSessionMain() {
        if (okHttpClient == null)
            initOkHttp();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(
                            new GsonBuilder()
                                    .registerTypeAdapter(Date.class, new DateDeserializer())
                                    .serializeNulls().create()))
                    .build();
        }
    }

    private static void initOkHttp() {
        int REQUEST_TIMEOUT = 60;
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.MINUTES)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.MINUTES)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.MINUTES);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient.addInterceptor(interceptor);

        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .addHeader("Authorization", String.format("Bearer %s", AppController.getContext().getString(R.string.api_key)))
                    .addHeader("Accept", "application/json")
                    .addHeader("Request-Type", "Android")
                    .addHeader("Content-Type", "application/json");

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        okHttpClient = httpClient.build();
    }


    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static void resetApiClient() {
        retrofit = null;
        okHttpClient = null;
    }

}
