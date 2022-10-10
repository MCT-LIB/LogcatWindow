package com.mct.logcatwindow.utils;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class ParamsUtils {

    @NonNull
    @Contract("_, _, _ -> new")
    public static WindowManager.LayoutParams getWindowParams(int width, int height, boolean focusable) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                getType(),
                getFlag(focusable),
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        return params;
    }

    @NonNull
    @Contract("_, _, _, _, _ -> new")
    public static WindowManager.LayoutParams getWindowParams(int x, int y, int width, int height, boolean focusable) {
        WindowManager.LayoutParams params = getWindowParams(width, height, focusable);
        params.x = x;
        params.y = y;
        return params;
    }

    public static int getType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    public static int getFlag(boolean focusable) {
        if (focusable) {
            return WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        } else {
            return WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }
    }

    @NonNull
    public static WindowManager.LayoutParams correctLayoutPosition(
            Point displaySize, @NonNull WindowManager.LayoutParams params) {
        return correctLayoutPosition(displaySize, params, params.x, params.y);
    }

    @NonNull
    public static WindowManager.LayoutParams correctLayoutPosition(
            Point displaySize, @NonNull WindowManager.LayoutParams params, int x, int y) {
        params.x = x < 0 ? 0 : Math.min(x, displaySize.x - params.width);
        params.y = y < 0 ? 0 : Math.min(y, displaySize.y - params.height);
        return params;
    }

}
