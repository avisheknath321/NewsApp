package com.newsapp.util.jobscheduler;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.evernote.android.job.Job;
import com.newsapp.AppController;
import com.newsapp.R;
import com.newsapp.model.ArticleResponse;
import com.newsapp.service.impl.ArticleServiceImpl;
import com.newsapp.ui.activity.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NotificationJob extends Job {

    public static final String TAG = "news_app_tag";
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    @SuppressLint("CheckResult")
    @Override
    @NonNull
    protected Result onRunJob(@NonNull final Params params) {


        ArticleServiceImpl articleService = new ArticleServiceImpl();

        //create Calendar instance
        Calendar date = Calendar.getInstance();
        String to=format.format(date.getTime());

        date.add(Calendar.MINUTE, -30);
        String from=format.format(date.getTime());

        articleService.getNewArticles("us", from,to,"publishAt")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArticleResponse>() {
                    @Override
                    public void onSuccess(ArticleResponse articleResponse) {
                        if (articleResponse.getStatus().equalsIgnoreCase("ok") && articleResponse.getArticles() != null && !articleResponse.getArticles().isEmpty()) {
                            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class), 0);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel(TAG, "News App", NotificationManager.IMPORTANCE_LOW);
                                channel.setDescription("Read news articles");
                                getContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);
                            }

                            Notification notification = new NotificationCompat.Builder(getContext(), TAG)
                                    .setContentTitle(AppController.getContext().getString(R.string.app_name))
                                    .setContentText(String.format(Locale.getDefault(), "%d new articles fetched", articleResponse.getArticles().size()))
                                    .setAutoCancel(true)
                                    .setChannelId(TAG)
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .setContentIntent(pendingIntent)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setShowWhen(true)
                                    .setColor(Color.GREEN)
                                    .setLocalOnly(true)
                                    .build();

                            NotificationManagerCompat.from(getContext()).notify(new Random().nextInt(), notification);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });


        return Result.SUCCESS;
    }
}
