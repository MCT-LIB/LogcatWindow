package com.mct.logcatwindow.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;

import java.util.Objects;

public class ScreenMetrics {

    private static final String TAG = "ScreenMetrics";
    private Context context;

    /**
     * The display to get the value from. It will always be the first one available.
     */
    private final Display display;

    /**
     * The listener upon orientation changes.
     */
    private OrientationListener orientationListener;

    public interface OrientationListener {
        void onOrientationChanged();
    }

    /**
     * The orientation of the display.
     */
    private int orientation;

    /**
     * The display size.
     */
    private Point displaySize;

    /**
     * The real size of device.
     */
    private Point realSize;

    /**
     * Listen to the configuration changes and calls [orientationListener] when needed.
     */
    private final BroadcastReceiver configChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateScreenMetrics();
        }
    };

    public ScreenMetrics(@NonNull Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        computeSelf();
    }

    public void computeSelf() {
        orientation = computeOrientation();
        computeScreenSize();
    }

    public int getOrientation() {
        return orientation;
    }

    public Point getDisplaySize() {
        return displaySize;
    }

    public Point getRealSize() {
        return realSize;
    }

    /**
     * Register a new orientation listener.
     * If a previous listener was registered, the new one will replace it.
     *
     * @param listener the listener to be registered.
     */
    public void registerOrientationListener(OrientationListener listener) {
        if (listener == orientationListener) {
            return;
        }
        unregisterOrientationListener();
        orientationListener = listener;
        context.registerReceiver(configChangedReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }

    /**
     * Unregister a previously registered listener.
     */
    public void unregisterOrientationListener() {
        if (orientationListener != null) {
            context.unregisterReceiver(configChangedReceiver);
            orientationListener = null;
        }
    }

    public void dispose() {
        unregisterOrientationListener();
        context = null;
    }

    /**
     * Update orientation and screen size, if needed. Should be called after a configuration change.
     */
    private void updateScreenMetrics() {
        int newOrientation = computeOrientation();
        if (orientation != newOrientation) {
            orientation = newOrientation;
            computeScreenSize();
            if (orientationListener != null) {
                Log.e(TAG, "updateScreenMetrics: " + displaySize.x + "|" + displaySize.y);
                Log.e(TAG, "updateScreenMetrics: " + realSize.x + "|" + realSize.y);
                orientationListener.onOrientationChanged();
            }
        }
    }

    private void computeScreenSize() {
        Point displaySize, realSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowMetrics metrics = context.getSystemService(WindowManager.class).getCurrentWindowMetrics();
            // Gets all excluding insets
            final WindowInsets windowInsets = metrics.getWindowInsets();

            Insets insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.statusBars() |
                            WindowInsets.Type.navigationBars() |
                            WindowInsets.Type.displayCutout());
            Log.e(TAG, "computeScreenSize: inset " + insets);
            // let remove bottom nav if device has
            int insetsWidth = insets.right + insets.left;
            int insetsHeight = insets.top + insets.bottom;

            // size that Display#getSize reports
            final Rect bounds = metrics.getBounds();
            realSize = new Point(bounds.width(), bounds.height());
            displaySize = new Point(bounds.width() - insetsWidth, bounds.height() - insetsHeight);
        } else {
            displaySize = new Point();
            realSize = new Point();
            display.getSize(displaySize);
            display.getRealSize(realSize);
        }

        // Some phone can be messy with the size change with the orientation. Correct it here.
        if (orientation == Configuration.ORIENTATION_PORTRAIT && displaySize.x > displaySize.y ||
                orientation == Configuration.ORIENTATION_LANDSCAPE && displaySize.x < displaySize.y) {
            int nx = displaySize.y;
            int ny = displaySize.x;
            displaySize = new Point(nx, ny);
            nx = realSize.y;
            ny = realSize.x;
            realSize = new Point(nx, ny);
        }
        this.displaySize = displaySize;
        this.realSize = realSize;
    }

    /**
     * @return the orientation of the screen.
     */
    public int computeOrientation() {
        int rotation = Objects.requireNonNull(display).getRotation();
        Log.e(TAG, "computeOrientation: " + rotation);
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return Configuration.ORIENTATION_PORTRAIT;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                return Configuration.ORIENTATION_LANDSCAPE;
            default:
                return Configuration.ORIENTATION_UNDEFINED;
        }
    }

}
