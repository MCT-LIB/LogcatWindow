package com.mct.logcatwindow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import androidx.annotation.NonNull;

public class LogWindowPreferences {

    private static final String PREFERENCE_NAME = "log_window_setting";

    private static final String KEY_LANDSCAPE_PANEL_X = "KEY_LANDSCAPE_PANEL_X";
    private static final String KEY_LANDSCAPE_PANEL_Y = "KEY_LANDSCAPE_PANEL_Y";
    private static final String KEY_LANDSCAPE_PANEL_HEIGHT = "KEY_LANDSCAPE_PANEL_HEIGHT";
    private static final String KEY_LANDSCAPE_PANEL_WIDTH = "KEY_LANDSCAPE_PANEL_WIDTH";

    private static final String KEY_PORTRAIT_PANEL_X = "KEY_PORTRAIT_PANEL_X";
    private static final String KEY_PORTRAIT_PANEL_Y = "KEY_PORTRAIT_PANEL_Y";
    private static final String KEY_PORTRAIT_PANEL_HEIGHT = "KEY_PORTRAIT_PANEL_HEIGHT";
    private static final String KEY_PORTRAIT_PANEL_WIDTH = "KEY_PORTRAIT_PANEL_WIDTH";

    private static LogWindowPreferences instance;

    public static LogWindowPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new LogWindowPreferences(context);
        }
        return instance;
    }

    private final SharedPreferences pref;

    private LogWindowPreferences(@NonNull Context context) {
        this.pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor editor() {
        return pref.edit();
    }

    public void savePanelPosition(Point position, Point size, boolean isLandscape) {
        if (isLandscape) {
            editor().putInt(KEY_LANDSCAPE_PANEL_X, position.x)
                    .putInt(KEY_LANDSCAPE_PANEL_Y, position.y)
                    .putInt(KEY_LANDSCAPE_PANEL_WIDTH, size.x)
                    .putInt(KEY_LANDSCAPE_PANEL_HEIGHT, size.y)
                    .apply();
        } else {
            editor().putInt(KEY_PORTRAIT_PANEL_X, position.x)
                    .putInt(KEY_PORTRAIT_PANEL_Y, position.y)
                    .putInt(KEY_PORTRAIT_PANEL_WIDTH, size.x)
                    .putInt(KEY_PORTRAIT_PANEL_HEIGHT, size.y)
                    .apply();
        }
    }

    public Point getPanelPosition(boolean isLandscape) {
        int x, y;
        if (isLandscape) {
            x = pref.getInt(KEY_LANDSCAPE_PANEL_X, 0);
            y = pref.getInt(KEY_LANDSCAPE_PANEL_Y, 0);
        } else {
            x = pref.getInt(KEY_PORTRAIT_PANEL_X, 0);
            y = pref.getInt(KEY_PORTRAIT_PANEL_Y, 0);
        }
        return new Point(x, y);
    }

    public Point getPanelSize(Point def, boolean isLandscape) {
        int width, height;
        if (isLandscape) {
            width = pref.getInt(KEY_LANDSCAPE_PANEL_WIDTH, def.x);
            height = pref.getInt(KEY_LANDSCAPE_PANEL_HEIGHT, def.y);
        } else {
            width = pref.getInt(KEY_PORTRAIT_PANEL_WIDTH, def.x);
            height = pref.getInt(KEY_PORTRAIT_PANEL_HEIGHT, def.y);
        }
        return new Point(width, height);
    }

}
