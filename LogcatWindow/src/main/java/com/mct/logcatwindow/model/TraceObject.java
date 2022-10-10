package com.mct.logcatwindow.model;

import androidx.annotation.NonNull;

public class TraceObject {
    private static final char TRACE_LEVEL_SEPARATOR = '/';
    private static final int END_OF_DATE_INDEX = 18;
    private static final int START_OF_MESSAGE_INDEX = 21;
    public static final int MIN_TRACE_SIZE = 21;
    public static final int TRACE_LEVEL_INDEX = 19;
    private final TraceLevel traceLevel;
    private final String message;
    private String date;

    public TraceObject(TraceLevel traceLevel, String message, String date) {
        this.traceLevel = traceLevel;
        this.message = message;
        this.date = date;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static TraceObject fromString(String logcatTrace) {
        if (logcatTrace != null && logcatTrace.length() >= MIN_TRACE_SIZE && logcatTrace.charAt(20) == TRACE_LEVEL_SEPARATOR) {
            TraceLevel level = TraceLevel.getTraceLevel(logcatTrace.charAt(TRACE_LEVEL_INDEX));
            String date = logcatTrace.substring(0, END_OF_DATE_INDEX);
            String message = logcatTrace.substring(START_OF_MESSAGE_INDEX);
            return new TraceObject(level, message, date);
        } else {
            return null;
        }
    }

    public TraceLevel getTraceLevel() {
        return this.traceLevel;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TraceObject)) {
            return false;
        } else {
            TraceObject traceObject = (TraceObject) o;
            return traceObject.getMessage().equals(this.message) && traceObject.getTraceLevel() == this.traceLevel || traceObject.getDate().equals(this.getDate()) || this.message.contains(((TraceObject) o).getMessage()) || this.extraCompare(this.message);
        }
    }

    protected boolean extraCompare(@NonNull String trace) {
        String[] tests = trace.split("Trace\\{");
        return tests.length > 1;
    }

    public int hashCode() {
        int result = this.traceLevel.hashCode();
        result = 31 * result + this.message.hashCode() + this.date.hashCode();
        return result;
    }

    @NonNull
    public String toString() {
        return "Trace{'level=" + this.traceLevel + ", message='" + this.message + '\'' + '}';
    }
}
