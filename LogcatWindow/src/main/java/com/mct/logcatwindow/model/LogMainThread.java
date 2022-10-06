package com.mct.logcatwindow.model;

import android.os.Handler;
import android.os.Looper;

public class LogMainThread implements MainThread {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public LogMainThread() {
    }

    public void post(Runnable runnable) {
        this.handler.post(runnable);
    }
}
