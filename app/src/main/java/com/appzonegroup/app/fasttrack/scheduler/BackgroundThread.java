package com.appzonegroup.app.fasttrack.scheduler;

import android.os.HandlerThread;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by Joseph on 1/28/2017.
 */
public class BackgroundThread extends HandlerThread {
    public BackgroundThread() {
        super("Scheduler-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
    }
}
