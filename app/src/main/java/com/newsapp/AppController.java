package com.newsapp;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.evernote.android.job.JobManager;
import com.newsapp.util.jobscheduler.JobCreator;
import com.newsapp.util.logger.Log;
import com.newsapp.util.logger.LogWrapper;

public class AppController extends MultiDexApplication {

    private static AppController mControllerInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mControllerInstance = this;
        init();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void init() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);


        JobManager.create(this).addJobCreator(new JobCreator());
    }

    public static AppController getAppController() {
        return mControllerInstance;
    }

    public static Context getContext() {
        return getAppController().getApplicationContext();
    }
}
