package com.mct.logcatwindow.control;

import com.mct.logcatwindow.model.TraceObject;

import java.util.LinkedList;
import java.util.List;

public class TraceBuffer {
    private int bufferSize;
    private final List<TraceObject> traces;

    TraceBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.traces = new LinkedList<>();
    }

    void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        this.removeExceededTracesIfNeeded();
    }

    int add(List<TraceObject> traces) {
        this.traces.addAll(traces);
        return this.removeExceededTracesIfNeeded();
    }

    List<TraceObject> getTraces() {
        return this.traces;
    }

    public int getCurrentNumberOfTraces() {
        return this.traces.size();
    }

    public void clear() {
        this.traces.clear();
    }

    private int removeExceededTracesIfNeeded() {
        int tracesToDiscard = this.getNumberOfTracesToDiscard();
        if (tracesToDiscard > 0) {
            this.discardTraces(tracesToDiscard);
        }

        return tracesToDiscard;
    }

    private int getNumberOfTracesToDiscard() {
        int currentTracesSize = this.traces.size();
        int tracesToDiscard = currentTracesSize - this.bufferSize;
        tracesToDiscard = Math.max(tracesToDiscard, 0);
        return tracesToDiscard;
    }

    private void discardTraces(int tracesToDiscard) {
        if (tracesToDiscard > 0) {
            this.traces.subList(0, tracesToDiscard).clear();
        }
    }
}
