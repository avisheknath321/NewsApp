package com.newsapp.util;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.newsapp.util.jobscheduler.NotificationJob;

public class Common {

    public static void startJobScheduler() {
        JobManager.instance().cancelAllForTag(NotificationJob.TAG);

        new JobRequest.Builder(NotificationJob.TAG)
                .setPeriodic(JobRequest.MIN_INTERVAL, JobRequest.MIN_FLEX)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .build()
                .schedule();


        for (JobRequest jr: JobManager.instance().getAllJobRequestsForTag(NotificationJob.TAG)) {
            System.out.println("NOOOTI===>id="+jr.getJobId()+"===periodic="+jr.isPeriodic());

        }
    }
}
