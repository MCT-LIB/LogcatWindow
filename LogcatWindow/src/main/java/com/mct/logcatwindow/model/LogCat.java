package com.mct.logcatwindow.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogCat extends Thread {
    private Process process;
    private BufferedReader bufferedReader;
    private LogCatListener logCatListener;
    private boolean continueReading = true;

    public LogCat() {
    }

    public void stopReading() {
        this.continueReading = false;
    }

    public void setListener(LogCatListener listener) {
        this.logCatListener = listener;
    }

    public LogCatListener getCurrentListener() {
        return this.logCatListener;
    }

    private void readLogCat() {
        BufferedReader bufferedReader = this.getBufferedReader();
        String trace;
        try {
            while ((trace = bufferedReader.readLine()) != null && this.continueReading) {
                this.notifyListener(trace);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedReader getBufferedReader() {
        if (this.bufferedReader == null && this.process != null) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
        }
        return this.bufferedReader;
    }

    private void notifyListener(String trace) {
        if (this.logCatListener != null) {
            this.logCatListener.onTraceRead(trace);
        }
    }

    public void run() {
        super.run();
        try {
            this.process = Runtime.getRuntime().exec("logcat -v time -T 500");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.readLogCat();
    }

    public void clear() {
        try {
            Runtime.getRuntime().exec("logcat -b all -c");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface LogCatListener {
        void onTraceRead(String trace);
    }
}
