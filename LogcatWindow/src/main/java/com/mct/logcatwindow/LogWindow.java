package com.mct.logcatwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.mct.bubblechat.BubblesManager;
import com.mct.logcatwindow.utils.LogWindowPreferences;
import com.mct.logcatwindow.utils.ScreenMetrics;
import com.mct.logcatwindow.utils.Utils;
import com.mct.logcatwindow.view.LogPanelView;
import com.mct.logcatwindow.view.LogPanelView.OnWindowChangeListener;

public class LogWindow {

    private static final int MIN_HEIGHT = Utils.dp2px(170);
    private static final int MIN_WIDTH = Utils.dp2px(150);
    private static final int MAX_TOUCH = Utils.dp2px(40);
    private static final int INIT_TOUCH = Utils.dp2px(20);
    private static final int BUBBLE_OVER_MARGIN = Utils.dp2px(8);

    private final LogWindowPreferences preferences;
    private final ScreenMetrics screenMetrics;
    private final WindowManager windowManager;
    private final BubblesManager bubblesManager;

    private LogPanelView logPanel;
    private LayoutParams logPanelParams;
    private boolean isLoadedPanel;

    private OnDisposeListener mOnDisposeListener;
    private final Object lock = new Object();
    boolean isAttach;

    private static LogWindow instance;

    private LogWindow(@NonNull Context context) {
        this.screenMetrics = new ScreenMetrics(context);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.preferences = LogWindowPreferences.getInstance(context);
        this.bubblesManager = new BubblesManager(context);
        initPanel(context);
    }

    public static LogWindow instance() {
        if (instance == null) {
            throw new IllegalArgumentException("You need call INIT before getInstance!");
        }
        return instance;
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new LogWindow(context);
            instance.screenMetrics.registerOrientationListener(instance::onOrientationChanged);
            Log.d(Utils.LOGCAT_WINDOW_TAG, "init");
        }
    }

    @SuppressWarnings("unused")
    public static void dispose() {
        if (instance != null) {
            instance.detachLogPanel();
            instance.logPanel.dispose();
            instance.screenMetrics.dispose();
            instance.bubblesManager.dispose();
            if (instance.mOnDisposeListener != null) {
                instance.mOnDisposeListener.onDispose();
            }
            instance = null;
            Log.d(Utils.LOGCAT_WINDOW_TAG, "dispose");
        }
    }

    @SuppressWarnings("unused")
    public LogWindow setLogConfig(LogConfig logConfig) {
        logPanel.getLogManager().setLogConfig(logConfig);
        return this;
    }

    public void setSafeInsetRect(Rect safeInsetRect) {
        bubblesManager.setSafeInsetRect(safeInsetRect);
    }

    public void setOnDisposeListener(OnDisposeListener listener) {
        mOnDisposeListener = listener;
    }

    public void attachBubbleControl(Context context) {

        if (!bubblesManager.isEmpty()) {
            return;
        }

        BubblesManager.Options options = new BubblesManager.Options();
        options.overMargin = BUBBLE_OVER_MARGIN;
        options.initX = -BUBBLE_OVER_MARGIN;
        options.initY = 150;
        options.floatingViewWidth = Utils.dp2px(64);
        options.floatingViewHeight = Utils.dp2px(64);
        options.onClickListener = v -> attachLogPanel();
        options.bubbleRemoveListener = LogWindow::dispose;

        bubblesManager.addBubble(getBubbleView(context), options);
    }

    private void attachLogPanel() {
        synchronized (lock) {
            if (!isAttach) {
                isAttach = true;
                bubblesManager.setBubbleVisibility(View.GONE);
                screenMetrics.computeSelf();
                loadPanelPosition(screenMetrics.getOrientation());
                windowManager.addView(logPanel, logPanelParams);
                Log.d(Utils.LOGCAT_WINDOW_TAG, "attach LogView");
            }
        }
    }

    private void detachLogPanel() {
        synchronized (lock) {
            if (isAttach) {
                isAttach = false;
                bubblesManager.setBubbleVisibility(View.VISIBLE);
                savePanelPosition(screenMetrics.getOrientation());
                windowManager.removeView(logPanel);
                Log.d(Utils.LOGCAT_WINDOW_TAG, "detach LogView");
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPanel(Context context) {
        logPanel = new LogPanelView(context);
        logPanelParams = getWindowParams();
        OnTouchListener onTouchListener = new OnTouchListener() {

            private final Point moveInitPos = new Point();
            private final Point moveInitTouchPos = new Point();

            public boolean onTouch(View v, @NonNull MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        logPanel.requestFocus();
                        moveInitPos.set(logPanelParams.x, logPanelParams.y);
                        moveInitTouchPos.set((int) event.getRawX(), (int) event.getRawY());
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x = moveInitPos.x + (int) (event.getRawX() - moveInitTouchPos.x);
                        int y = moveInitPos.y + (int) (event.getRawY() - moveInitTouchPos.y);
                        correctLayoutPosition(screenMetrics.getRealDisplay(), logPanelParams, x, y);
                        windowManager.updateViewLayout(logPanel, logPanelParams);
                        return true;
                    case MotionEvent.ACTION_OUTSIDE:
                        detachLogPanel();
                        return true;
                }
                return false;
            }

        };
        OnWindowChangeListener onWindowChangeListener = new OnWindowChangeListener() {
            @Override
            public void changeWindowHeight(int height) {
                logPanelParams.height = height + MIN_HEIGHT;
                correctLayoutPosition(screenMetrics.getRealDisplay(), logPanelParams);
                windowManager.updateViewLayout(logPanel, logPanelParams);
            }

            @Override
            public void changeWindowWidth(int width) {
                logPanelParams.width = width + MIN_WIDTH;
                correctLayoutPosition(screenMetrics.getRealDisplay(), logPanelParams);
                windowManager.updateViewLayout(logPanel, logPanelParams);
            }

            @Override
            public void onDropDownChanged(boolean isOpened) {
                if (isOpened) {
                    logPanelParams.flags &= ~LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    logPanelParams.flags |= LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                } else {
                    logPanelParams.flags |= LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    logPanelParams.flags &= ~LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                    logPanel.requestFocus();
                }
                windowManager.updateViewLayout(logPanel, logPanelParams);
            }

            @Override
            public void requestDetachWindow() {
                detachLogPanel();
            }

        };
        OnKeyListener onKeyListener = (v, keyCode, e) -> {
            if (KeyEvent.KEYCODE_BACK == keyCode && e.getAction() == KeyEvent.ACTION_UP) {
                detachLogPanel();
                return true;
            }
            return false;
        };
        logPanel.setOnTouchListener(onTouchListener);
        logPanel.setChangeWindowListener(onWindowChangeListener);
        logPanel.setOnKeyListener(onKeyListener);
    }

    private void onOrientationChanged() {
        boolean isLandscape = screenMetrics.isLandscape();

        savePanelPosition(isLandscape
                ? Configuration.ORIENTATION_PORTRAIT
                : Configuration.ORIENTATION_LANDSCAPE);
        loadPanelPosition(screenMetrics.getOrientation());
        if (logPanel.isAttachedToWindow()) {
            windowManager.updateViewLayout(logPanel, logPanelParams);
        }
    }

    /**
     * Load last user menu position for the current orientation, if any.
     *
     * @param orientation the orientation to load the position for.
     */
    private void loadPanelPosition(int orientation) {
        Point display = screenMetrics.getRealDisplay();
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;
        Point defaultSize = new Point();
        if (isLandscape) {
            defaultSize.set(display.x / 3, display.y);
        } else {
            defaultSize.set(display.x, display.y / 3);
        }
        Point position = preferences.getPanelPosition(isLandscape);
        Point size = preferences.getPanelSize(defaultSize, isLandscape);

        Bundle extraData = new Bundle();
        extraData.putInt(LogPanelView.KEY_MAX_HEIGHT, display.y - MIN_HEIGHT);
        extraData.putInt(LogPanelView.KEY_MAX_WIDTH, display.x - MIN_WIDTH);
        extraData.putInt(LogPanelView.KEY_MAX_TOUCH, MAX_TOUCH);
        extraData.putInt(LogPanelView.KEY_HEIGHT, size.y - MIN_HEIGHT);
        extraData.putInt(LogPanelView.KEY_WIDTH, size.x - MIN_WIDTH);
        extraData.putInt(LogPanelView.KEY_TOUCH, INIT_TOUCH);
        logPanel.setData(extraData);
        logPanelParams.height = (int) extraData.get(LogPanelView.KEY_HEIGHT) + MIN_HEIGHT;
        logPanelParams.width = (int) extraData.get(LogPanelView.KEY_WIDTH) + MIN_WIDTH;
        logPanelParams.x = position.x;
        logPanelParams.y = position.y;
        isLoadedPanel = true;
    }

    /**
     * Save the last user menu position for the current orientation.
     *
     * @param orientation the orientation to save the position for.
     */
    private void savePanelPosition(int orientation) {
        if (!isLoadedPanel) {
            return;
        }
        Point position = new Point(logPanelParams.x, logPanelParams.y);
        Point size = new Point(logPanelParams.width, logPanelParams.height);
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;
        Log.e("ddd", "savePanelPosition: " + position + " " + size + " isLandscape: " + isLandscape);
        preferences.savePanelPosition(position, size, isLandscape);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Internal Utils
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    private static View getBubbleView(Context context) {
        ImageView view = new ImageView(context);
        view.setImageResource(R.drawable.lw_bubble_icon);
        return view;
    }

    @NonNull
    private static LayoutParams getWindowParams() {
        LayoutParams mParams = new LayoutParams();
        mParams.width = LayoutParams.MATCH_PARENT;
        mParams.height = LayoutParams.MATCH_PARENT;
        mParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        mParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.gravity = Gravity.START | Gravity.TOP;
        return mParams;
    }

    private static void correctLayoutPosition(Point displaySize, @NonNull LayoutParams params) {
        correctLayoutPosition(displaySize, params, params.x, params.y);
    }

    private static void correctLayoutPosition(Point displaySize, @NonNull LayoutParams params, int x, int y) {
        params.x = x < 0 ? 0 : Math.min(x, displaySize.x - params.width);
        params.y = y < 0 ? 0 : Math.min(y, displaySize.y - params.height);
    }

    public interface OnDisposeListener {
        void onDispose();
    }
}
