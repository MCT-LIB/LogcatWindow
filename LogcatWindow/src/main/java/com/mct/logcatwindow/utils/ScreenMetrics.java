package com.mct.logcatwindow.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;

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
     * The real size of device.
     */
    private Point screenSize;

    /**
     * The Insets of device.
     */
    private Rect screenInsets;

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
        screenSize = computeScreenSize();
    }

    public boolean isLandscape() {
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public int getOrientation() {
        return orientation;
    }

    public Point getScreenSize() {
        return screenSize;
    }

    public Rect getScreenInsets() {
        return screenInsets;
    }

    /**
     * Without system bar, cutout layout
     *
     * @return Point
     */
    public Point getRealDisplay() {
        return new Point(
                screenSize.x - screenInsets.right - screenInsets.left,
                screenSize.y - screenInsets.bottom - screenInsets.top
        );
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
        Log.e(TAG, "updateScreenMetrics: ");
        computeSelf();
        if (orientationListener != null) {
            Log.e(TAG, "updateScreenMetrics: " + screenSize.x + "|" + screenSize.y);
            orientationListener.onOrientationChanged();
        }
    }

    /**
     * @return the orientation of the screen.
     */
    private int computeOrientation() {
        int rotation = display.getRotation();
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

    @NonNull
    private Point computeScreenSize() {
        Point screenSize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowMetrics metrics = context.getSystemService(WindowManager.class).getCurrentWindowMetrics();
            // Gets all excluding insets
            int type = WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout();
            Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(type);
            screenInsets = new Rect(insets.left, insets.top, insets.right, insets.bottom);
            // size that Display#getSize reports
            final Rect bounds = metrics.getBounds();
            screenSize.set(bounds.width(), bounds.height());
        } else {
            screenInsets = computeScreenInsets();
            display.getRealSize(screenSize);
        }
        Log.e(TAG, "computeScreenSize: inset " + screenInsets);
        return screenSize;
    }

    /**
     * for SDK < 30
     *
     * @return rect of inset
     */
    @NonNull
    private Rect computeScreenInsets() {
        int rotation = display.getRotation();
        Rect insets = new Rect();
        switch (rotation) {
            default:
            case Surface.ROTATION_0:
                insets.top = getStatusBarHeight();
                insets.bottom = getNavigationBarHeight();
                break;
            case Surface.ROTATION_180:
                insets.top = getNavigationBarHeight();
                insets.bottom = getStatusBarHeight();
                break;
            case Surface.ROTATION_90:
                insets.top = getStatusBarHeight();
                insets.right = getNavigationBarHeight();
                break;
            case Surface.ROTATION_270:
                insets.top = getStatusBarHeight();
                insets.left = getNavigationBarHeight();
                break;
        }
        return insets;
    }

    public static int getStatusBarHeight() {
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static int getNavigationBarHeight() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        if (hasBackKey && hasHomeKey) {
            // no navigation bar, unless it is enabled in the settings
            return 0;
        } else {
            // 99% sure there's a navigation bar
            Resources resources = Resources.getSystem();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            return resourceId != 0 ? resources.getDimensionPixelSize(resourceId) : 0;
        }
    }
}
