package com.mct.logcatwindow;

import androidx.annotation.NonNull;

import com.mct.logcatwindow.model.TraceLevel;
import com.mct.logcatwindow.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogConfig implements Serializable, Cloneable {

    private static final int MAX_TRACES_TO_SHOW = 500;

    private int maxNumberOfTracesToShow;
    private String filter;
    private final List<String> subFilter;
    private TraceLevel filterTraceLevel;
    private int samplingRate = 150;

    public LogConfig() {
        this.maxNumberOfTracesToShow = MAX_TRACES_TO_SHOW;
        this.filter = Utils.LOGCAT_WINDOW_TAG;
        this.subFilter = new ArrayList<>();
        this.filterTraceLevel = TraceLevel.VERBOSE;
    }

    public LogConfig setMaxNumberOfTracesToShow(int maxNumberOfTracesToShow) {
        if (maxNumberOfTracesToShow <= 0) {
            throw new IllegalArgumentException("You can't use a max number of traces equals or lower than zero.");
        } else {
            this.maxNumberOfTracesToShow = maxNumberOfTracesToShow;
            return this;
        }
    }

    public LogConfig setFilter(String filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter can't be null");
        } else {
            this.filter = filter;
            this.subFilter.clear();
            for (String s : filter.split("\\|")) {
                s = s.trim();
                if (!s.isEmpty()) {
                    this.subFilter.add(s.toLowerCase());
                }
            }
            return this;
        }
    }

    public LogConfig setFilterTraceLevel(TraceLevel filterTraceLevel) {
        if (filterTraceLevel == null) {
            throw new IllegalArgumentException("filterTraceLevel can't be null");
        } else {
            this.filterTraceLevel = filterTraceLevel;
            return this;
        }
    }

    public LogConfig setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    public int getMaxNumberOfTracesToShow() {
        return this.maxNumberOfTracesToShow;
    }

    public String getFilter() {
        return this.filter;
    }

    public List<String> getSubFilter() {
        return subFilter;
    }

    public TraceLevel getFilterTraceLevel() {
        return this.filterTraceLevel;
    }

    public boolean hasFilter() {
        return !"".equals(this.filter) || !TraceLevel.VERBOSE.equals(this.filterTraceLevel);
    }

    public int getSamplingRate() {
        return this.samplingRate;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LogConfig)) {
            return false;
        } else {
            LogConfig that = (LogConfig) o;
            if (this.maxNumberOfTracesToShow != that.maxNumberOfTracesToShow) {
                return false;
            } else if (this.samplingRate != that.samplingRate) {
                return false;
            } else {
                mLabel:
                {
                    if (this.filter != null) {
                        if (this.filter.equals(that.filter)) {
                            break mLabel;
                        }
                    } else if (that.filter == null) {
                        break mLabel;
                    }
                    return false;
                }

                return this.filterTraceLevel == that.filterTraceLevel;
            }
        }
    }

    public int hashCode() {
        int result = this.maxNumberOfTracesToShow;
        result = 31 * result + (this.filter != null ? this.filter.hashCode() : 0);
        result = 31 * result + this.samplingRate;
        return result;
    }

    @NonNull
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Object clone() {
        return (new LogConfig()).setMaxNumberOfTracesToShow(this.getMaxNumberOfTracesToShow()).setFilter(this.filter).setFilterTraceLevel(this.filterTraceLevel).setSamplingRate(this.getSamplingRate());
    }

    @NonNull
    public String toString() {
        return "LogConfig{maxNumberOfTracesToShow=" + this.maxNumberOfTracesToShow + ", filter='" + this.filter + ", samplingRate=" + this.samplingRate + '}';
    }
}
