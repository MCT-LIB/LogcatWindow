package com.mct.logcatwindow.control;

import androidx.annotation.NonNull;

import com.mct.logcatwindow.LogConfig;
import com.mct.logcatwindow.model.LogManager;
import com.mct.logcatwindow.model.TraceLevel;
import com.mct.logcatwindow.model.TraceObject;

import java.util.List;

public class LogCtrl implements LogManager.Listener {
    private static final int MIN_VISIBLE_POSITION_TO_ENABLE_AUTO_SCROLL = 3;
    private final LogManager logManager;
    private final LogInteract logInteract;
    private final TraceBuffer traceBuffer;
    private boolean isInitialized;

    public LogCtrl(LogManager logManager, LogInteract logInteract, LogConfig logConfig) {
        this.logManager = logManager;
        this.logInteract = logInteract;
        if (logConfig == null) {
            throw new IllegalArgumentException("logConfig must not be null");
        } else {
            this.traceBuffer = new TraceBuffer(logConfig.getMaxNumberOfTracesToShow());
            this.setLogConfig(logConfig);
        }
    }

    public void resume() {
        if (!this.isInitialized) {
            this.isInitialized = true;
            this.logManager.registerListener(this);
            this.logManager.startReading();
        }
    }

    public void pause() {
        if (this.isInitialized) {
            this.isInitialized = false;
            this.logManager.unregisterListener();
            this.logManager.stopReading();
        }
    }

    public void clearLogcat() {
        this.logManager.clearLogcat();
    }

    public void setLogConfig(LogConfig config) {
        this.updateBufferConfig(config);
        this.updateLogConfig(config);
    }

    private void updateLogConfig(LogConfig logConfig) {
        if (this.logManager != null) {
            this.logManager.setLogConfig(logConfig);
        }
    }

    private void updateBufferConfig(@NonNull LogConfig logConfig) {
        this.traceBuffer.setBufferSize(logConfig.getMaxNumberOfTracesToShow());
        this.refreshTraces();
    }

    private void refreshTraces() {
        this.onNewTraces(this.traceBuffer.getTraces());
    }

    public void onNewTraces(List<TraceObject> traces) {
        int tracesRemoved = this.updateTraceBuffer(traces);
        List<TraceObject> tracesToNotify = this.getCurrentTraces();
        this.logInteract.showTraces(tracesToNotify, tracesRemoved);
    }

    private int updateTraceBuffer(List<TraceObject> traces) {
        return this.traceBuffer.add(traces);
    }

    public void updateFilter(String filter) {
        if (this.isInitialized && !this.logManager.getLogConfig().getFilter().equals(filter)) {
            this.logManager.getLogConfig().setFilter(filter);
            this.clearView();
            this.restartLog();
        }
    }

    private void clearView() {
        this.traceBuffer.clear();
        this.logInteract.clear();
    }

    public List<TraceObject> getCurrentTraces() {
        return this.traceBuffer.getTraces();
    }

    public void onScrollToPosition(int lastVisiblePositionInTheList) {
        if (this.shouldDisableAutoScroll(lastVisiblePositionInTheList)) {
            this.logInteract.disableAutoScroll();
        } else {
            this.logInteract.enableAutoScroll();
        }
    }

    private boolean shouldDisableAutoScroll(int lastVisiblePosition) {
        int positionOffset = this.traceBuffer.getCurrentNumberOfTraces() - lastVisiblePosition;
        return positionOffset >= MIN_VISIBLE_POSITION_TO_ENABLE_AUTO_SCROLL;
    }

    public void updateFilterTraceLevel(TraceLevel level) {
        if (this.isInitialized) {
            this.logManager.getLogConfig().setFilterTraceLevel(level);
            this.clearView();
            this.restartLog();
        }
    }

    private void restartLog() {
        this.logManager.reStart();
    }

    public interface LogInteract {
        void showTraces(List<TraceObject> traces, int removedTraces);

        void clear();

        void disableAutoScroll();

        void enableAutoScroll();

    }
}
