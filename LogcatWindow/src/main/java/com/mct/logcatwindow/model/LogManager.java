package com.mct.logcatwindow.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mct.logcatwindow.LogConfig;
import com.mct.logcatwindow.utils.Utils;

import java.lang.Thread.State;
import java.util.LinkedList;
import java.util.List;

public class LogManager {
    private LogCat logCat;
    private final MainThread mainThread;
    private List<TraceObject> tracesBuffer;
    private Listener listener;
    private LogConfig logConfig;
    private long lastNotification;

    public LogManager(LogCat logCat, MainThread mainThread) {
        this.logCat = logCat;
        this.mainThread = mainThread;
        this.logConfig = new LogConfig();
        this.tracesBuffer = new LinkedList<>();
    }

    public void startReading() {
        this.logCat.setListener(traceMsg -> {
            LogManager.this.addTraceToBuffer(traceMsg);
            LogManager.this.notifyNewTraces();
        });
        if (this.logCat.getState().equals(State.NEW)) {
            Log.d(Utils.LOGCAT_WINDOW_TAG, "logCatThread start ---");
            this.logCat.start();
        }
    }

    public void reStart() {
        LogCat.LogCatListener logCatListener = this.logCat.getCurrentListener();
        if (logCatListener == null) {
            logCatListener = LogManager.this::addTraceToBuffer;
        }

        this.lastNotification = 0L;
        if (this.tracesBuffer != null) {
            this.tracesBuffer.clear();
        } else {
            this.tracesBuffer = new LinkedList<>();
        }

        if (this.logCat != null && this.logCat.isAlive()) {
            this.logCat.stopReading();
            this.logCat.interrupt();
            this.logCat = null;
        }

        this.logCat = new LogCat();
        this.logCat.setListener(logCatListener);
        this.logCat.start();
    }

    public void stopReading() {
        this.logCat.stopReading();
        this.logCat.interrupt();
    }

    public void clearLogcat() {
        this.logCat.clear();
    }

    private synchronized void addTraceToBuffer(String trace) {
        if (this.shouldAddTrace(trace)) {
            TraceObject traceObject = null;

            try {
                traceObject = TraceObject.fromString(trace);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (traceObject != null) {
                if (!this.tracesBuffer.contains(traceObject)) {
                    this.tracesBuffer.add(traceObject);
                }
            }
        }
    }

    private boolean shouldAddTrace(String trace) {
        return !this.logConfig.hasFilter() || this.traceMatchesFilter(trace);
    }

    private boolean traceMatchesFilter(String logcatTrace) {
        try {
            if (containsTraceLevel(logcatTrace, this.logConfig.getFilterTraceLevel())) {
                String logcatTraceLowercase = logcatTrace.toLowerCase();
                for (String filter : this.logConfig.getSubFilter()) {
                    if (logcatTraceLowercase.contains(filter)) {
                        return true;
                    }
                }
                return logcatTraceLowercase.contains(this.logConfig.getFilter().toLowerCase());
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private boolean containsTraceLevel(String logcatTrace, @NonNull TraceLevel levelFilter) {
        return levelFilter.equals(TraceLevel.VERBOSE) || this.hasTraceLevelEqualOrHigher(logcatTrace, levelFilter);
    }

    private boolean hasTraceLevelEqualOrHigher(@NonNull String logcatTrace, @NonNull TraceLevel levelFilter) {
        TraceLevel level = TraceLevel.getTraceLevel(logcatTrace.charAt(19));
        return level.ordinal() >= levelFilter.ordinal();
    }

    private boolean shouldNotifyListeners() {
        long now = System.currentTimeMillis();
        long timeFromLastNotification = now - this.lastNotification;
        boolean hasTracesToNotify = this.tracesBuffer.size() > 0;
        return timeFromLastNotification > (long) this.logConfig.getSamplingRate() && hasTracesToNotify;
    }

    private void notifyNewTraces() {
        if (this.shouldNotifyListeners()) {
            List<TraceObject> traces = new LinkedList<>(this.tracesBuffer);
            this.tracesBuffer.clear();
            this.finalNotification(traces);
        }
    }

    private synchronized void finalNotification(final List<TraceObject> tracesBuffer) {
        this.mainThread.post(() -> {
            if (LogManager.this.listener != null) {
                LogManager.this.listener.onNewTraces(tracesBuffer);
                LogManager.this.lastNotification = System.currentTimeMillis();
            }
        });
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    public void unregisterListener() {
        this.listener = null;
    }

    public LogConfig getLogConfig() {
        return this.logConfig;
    }

    public void setLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
    }

    public interface Listener {
        void onNewTraces(List<TraceObject> traces);
    }
}
