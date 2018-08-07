package com.newsapp.util.jobscheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import static com.newsapp.util.Common.startJobScheduler;

public class JobCreator implements com.evernote.android.job.JobCreator {

    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case NotificationJob.TAG:
                return new NotificationJob();
            default:
                return null;
        }
    }

    public static final class AddReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction())) {
                startJobScheduler();
            }
        }
    }
}
