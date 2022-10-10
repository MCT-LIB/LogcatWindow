package com.mct.logcatwindow.model;

public enum TraceLevel {
    VERBOSE("V"),
    DEBUG("D"),
    INFO("I"),
    WARNING("W"),
    ERROR("E"),
    ASSERT("A"),
    WTF("F");

    private final String value;

    TraceLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static TraceLevel getTraceLevel(char traceString) {
        TraceLevel traceLevel;
        switch (traceString) {
            case 'A':
                traceLevel = ASSERT;
                break;
            case 'B':
            case 'C':
            case 'D':
            case 'G':
            case 'H':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            default:
                traceLevel = DEBUG;
                break;
            case 'E':
                traceLevel = ERROR;
                break;
            case 'F':
                traceLevel = WTF;
                break;
            case 'I':
                traceLevel = INFO;
                break;
            case 'V':
                traceLevel = VERBOSE;
                break;
            case 'W':
                traceLevel = WARNING;
        }
        return traceLevel;
    }

}
