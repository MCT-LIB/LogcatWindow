package com.mct.logcatwindow;

import androidx.annotation.NonNull;

import com.mct.logcatwindow.model.TraceLevel;
import com.mct.logcatwindow.utils.LogUtils;

import java.io.Serializable;

public class LogConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = 293939299388293L;
    private static final float DEFAULT_TEXT_SIZE_IN_PX = 36.0F;
    private int maxNumberOfTracesToShow = 800;
    private String filter = LogUtils.LOGCAT_WINDOW_TAG;
    private TraceLevel filterTraceLevel;
    private Float textSizeInPx;
    private int samplingRate = 150;

    public LogConfig() {
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

    public LogConfig setTextSizeInPx(float textSizeInPx) {
        this.textSizeInPx = textSizeInPx;
        return this;
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

    public TraceLevel getFilterTraceLevel() {
        return this.filterTraceLevel;
    }

    public boolean hasFilter() {
        return !"".equals(this.filter) || !TraceLevel.VERBOSE.equals(this.filterTraceLevel);
    }

    public float getTextSizeInPx() {
        return this.textSizeInPx == null ? DEFAULT_TEXT_SIZE_IN_PX : this.textSizeInPx;
    }

    public boolean hasTextSizeInPx() {
        return this.textSizeInPx != null;
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
            LogConfig that = (LogConfig)o;
            if (this.maxNumberOfTracesToShow != that.maxNumberOfTracesToShow) {
                return false;
            } else if (this.samplingRate != that.samplingRate) {
                return false;
            } else {
                label41: {
                    if (this.filter != null) {
                        if (this.filter.equals(that.filter)) {
                            break label41;
                        }
                    } else if (that.filter == null) {
                        break label41;
                    }
                    return false;
                }

                if (this.textSizeInPx != null) {
                    if (!this.textSizeInPx.equals(that.textSizeInPx)) {
                        return false;
                    }
                } else if (that.textSizeInPx != null) {
                    return false;
                }

                return this.filterTraceLevel == that.filterTraceLevel;
            }
        }
    }

    public int hashCode() {
        int result = this.maxNumberOfTracesToShow;
        result = 31 * result + (this.filter != null ? this.filter.hashCode() : 0);
        result = 31 * result + (this.textSizeInPx != null ? this.textSizeInPx.hashCode() : 0);
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
        return "LynxConfig{maxNumberOfTracesToShow=" + this.maxNumberOfTracesToShow + ", filter='" + this.filter + '\'' + ", textSizeInPx=" + this.textSizeInPx + ", samplingRate=" + this.samplingRate + '}';
    }
}
