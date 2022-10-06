package com.mct.logcatwindow.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class LogUtils {

    public static final String LOGCAT_WINDOW_TAG = "Logcat Window";

    private static int[] disPlay;

    public LogUtils() {
    }

    public static int dpToPx(Context context, float dipValue) {
        if (context == null) {
            return 0;
        } else {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dipValue * scale + 0.5F);
        }
    }

    public static int[] getDevDisplay(Context context) {
        if (disPlay == null) {
            disPlay = new int[2];
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            disPlay[0] = dm.widthPixels;
            disPlay[1] = dm.heightPixels;
        }
        return disPlay;
    }
}
