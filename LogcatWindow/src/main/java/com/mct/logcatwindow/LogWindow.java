package com.mct.logcatwindow;

import static com.mct.logcatwindow.utils.ParamsUtils.correctLayoutPosition;
import static com.mct.logcatwindow.utils.ParamsUtils.getWindowParams;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.mct.logcatwindow.utils.ParamsUtils;
import com.mct.logcatwindow.utils.ScreenMetrics;
import com.mct.logcatwindow.utils.Utils;
import com.mct.logcatwindow.view.LogPanelView;
import com.mct.logcatwindow.view.LogPanelView.OnWindowChangeListener;
import com.mct.logcatwindow.view.bubble.BubbleLayout;
import com.mct.logcatwindow.view.bubble.BubbleTrashLayout;
import com.mct.logcatwindow.view.bubble.BubblesManager;

public class LogWindow {

    private static final int MIN_HEIGHT = Utils.dp2px(170);
    private static final int MIN_WIDTH = Utils.dp2px(150);
    private static final int MAX_TOUCH = Utils.dp2px(40);
    private static final int INIT_TOUCH = Utils.dp2px(20);

    /**
     * Name of the preference file.
     */
    private static final String PREFERENCE_NAME = "LogWindow";

    private static final String KEY_LANDSCAPE_PANEL_X = "KEY_LANDSCAPE_PANEL_X";
    private static final String KEY_LANDSCAPE_PANEL_Y = "KEY_LANDSCAPE_PANEL_Y";
    private static final String KEY_LANDSCAPE_HEIGHT = "KEY_HEIGHT_LANDSCAPE";
    private static final String KEY_LANDSCAPE_WIDTH = "KEY_WIDTH_LANDSCAPE";

    private static final String KEY_PORTRAIT_PANEL_X = "KEY_PORTRAIT_PANEL_X";
    private static final String KEY_PORTRAIT_PANEL_Y = "KEY_PORTRAIT_PANEL_Y";
    private static final String KEY_PORTRAIT_HEIGHT = "KEY_HEIGHT_PORTRAIT";
    private static final String KEY_PORTRAIT_WIDTH = "KEY_WIDTH_PORTRAIT";

    private final SharedPreferences pref;
    private final ScreenMetrics screenMetrics;
    private final WindowManager windowManager;
    private BubblesManager bubblesManager;

    private BubbleTrashLayout trashLayout;
    private BubbleLayout bubbleLayout;

    private LogPanelView mLogPanel;
    private LayoutParams mLogPanelParams;


    private static LogWindow instance;

    private LogWindow(@NonNull Context context) {
        this.screenMetrics = new ScreenMetrics(context);
        this.pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initBubble(context);
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
            instance.screenMetrics.dispose();
            instance.bubblesManager.dispose();
            instance = null;
            Log.d(Utils.LOGCAT_WINDOW_TAG, "dispose");
        }
    }

    @SuppressWarnings("unused")
    public LogWindow setLogConfig(LogConfig logConfig) {
        mLogPanel.getLogManager().setLogConfig(logConfig);
        return this;
    }

    public void attachBubbleControl(FragmentActivity activity) {
        Utils.requestOverlayPermission(activity, (allGranted, gl, dl) -> {
            if (allGranted) {
                if (!trashLayout.isAttachedToWindow()) {
                    bubblesManager.setTrash(trashLayout);
                }
                if (!bubbleLayout.isAttachedToWindow()) {
                    bubbleLayout.getViewParams().x = 0;
                    bubbleLayout.getViewParams().y = 200;
                    bubblesManager.addBubble(bubbleLayout);
                }
            }
        });
    }

    private void attachLogPanel() {
        if (!mLogPanel.isAttachedToWindow()) {
            bubbleLayout.setVisibility(View.GONE);
            screenMetrics.computeSelf();
            loadPanelPosition(screenMetrics.getOrientation());
            windowManager.addView(mLogPanel, mLogPanelParams);
            Log.d(Utils.LOGCAT_WINDOW_TAG, "attach LogView");
        }
    }

    private void detachLogPanel() {
        if (mLogPanel.isAttachedToWindow()) {
            bubbleLayout.setVisibility(View.VISIBLE);
            savePanelPosition(screenMetrics.getOrientation());
            windowManager.removeViewImmediate(mLogPanel);
            Log.d(Utils.LOGCAT_WINDOW_TAG, "detach LogView");
        }
    }

    @SuppressLint("InflateParams")
    private void initBubble(Context context) {
        bubblesManager = new BubblesManager(windowManager);

        trashLayout = new BubbleTrashLayout(context);
        LayoutInflater.from(context).inflate(R.layout.lw_layout_bubble_trash, trashLayout, true);
        int x = (Utils.getScreenWidth() - trashLayout.getChildAt(0).getLayoutParams().width) / 2;
        int y = Utils.getScreenHeight() - 150;
        trashLayout.setViewParams(ParamsUtils.getWindowParams(
                x, y,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                false)
        );

        bubbleLayout = (BubbleLayout) LayoutInflater.from(context).inflate(R.layout.lw_layout_bubble, null);
        bubbleLayout.setViewParams(ParamsUtils.getWindowParams(
                0, 0,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                false)
        );

        bubbleLayout.setOnBubbleRemoveListener(bubble -> dispose());

        bubbleLayout.setOnBubbleClickListener(bubble -> attachLogPanel());

        bubbleLayout.setRequestDisplayListener(screenMetrics::getDisplaySize);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPanel(Context context) {
        mLogPanel = new LogPanelView(context);
        mLogPanelParams = getWindowParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
        OnTouchListener onTouchListener = new OnTouchListener() {

            private final Point moveInitPos = new Point();
            private final Point moveInitTouchPos = new Point();

            public boolean onTouch(View v, @NonNull MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLogPanel.requestFocus();
                        moveInitPos.set(mLogPanelParams.x, mLogPanelParams.y);
                        moveInitTouchPos.set((int) event.getRawX(), (int) event.getRawY());
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x = moveInitPos.x + (int) (event.getRawX() - moveInitTouchPos.x);
                        int y = moveInitPos.y + (int) (event.getRawY() - moveInitTouchPos.y);
                        LayoutParams params = correctLayoutPosition(screenMetrics.getDisplaySize(), mLogPanelParams, x, y);
                        windowManager.updateViewLayout(mLogPanel, params);
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
                mLogPanelParams.height = height + MIN_HEIGHT;
                LayoutParams params = correctLayoutPosition(screenMetrics.getDisplaySize(), mLogPanelParams);
                windowManager.updateViewLayout(mLogPanel, params);
            }

            @Override
            public void changeWindowWidth(int width) {
                mLogPanelParams.width = width + MIN_WIDTH;
                LayoutParams params = correctLayoutPosition(screenMetrics.getDisplaySize(), mLogPanelParams);
                windowManager.updateViewLayout(mLogPanel, params);
            }

            @Override
            public void onDropDownChanged(boolean isOpened) {
                if (isOpened) {
                    mLogPanelParams.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    mLogPanelParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                } else {
                    mLogPanelParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    mLogPanelParams.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                    mLogPanel.requestFocus();
                }
                windowManager.updateViewLayout(mLogPanel, mLogPanelParams);
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
        mLogPanel.setOnTouchListener(onTouchListener);
        mLogPanel.setChangeWindowListener(onWindowChangeListener);
        mLogPanel.setOnKeyListener(onKeyListener);
    }

    private void onOrientationChanged() {
        if (trashLayout.isAttachedToWindow()) {
            Point display = screenMetrics.getDisplaySize();
            int x = (display.x - trashLayout.getChildAt(0).getLayoutParams().width) / 2;
            int y = display.y - 150;
            trashLayout.getViewParams().x = x;
            trashLayout.getViewParams().y = y;
            windowManager.updateViewLayout(trashLayout, trashLayout.getViewParams());
        }

        if (bubbleLayout.isAttachedToWindow()) {
            bubbleLayout.getViewParams().x = 0;
            bubbleLayout.getViewParams().y = 200;
            windowManager.updateViewLayout(bubbleLayout, bubbleLayout.getViewParams());
        }

        savePanelPosition(screenMetrics.getOrientation() == Configuration.ORIENTATION_LANDSCAPE
                ? Configuration.ORIENTATION_PORTRAIT
                : Configuration.ORIENTATION_LANDSCAPE);
        loadPanelPosition(screenMetrics.getOrientation());
        if (mLogPanel.isAttachedToWindow()) {
            windowManager.updateViewLayout(mLogPanel, mLogPanelParams);
        }
    }

    /**
     * Load last user menu position for the current orientation, if any.
     *
     * @param orientation the orientation to load the position for.
     */
    private void loadPanelPosition(int orientation) {
        Bundle extraData = new Bundle();
        int x, y, height, width;
        Point display = screenMetrics.getDisplaySize();
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            x = pref.getInt(KEY_LANDSCAPE_PANEL_X, 0);
            y = pref.getInt(KEY_LANDSCAPE_PANEL_Y, 0);

            height = pref.getInt(KEY_LANDSCAPE_HEIGHT, display.y);
            width = pref.getInt(KEY_LANDSCAPE_WIDTH, display.x / 3);
        } else {
            x = pref.getInt(KEY_PORTRAIT_PANEL_X, 0);
            y = pref.getInt(KEY_PORTRAIT_PANEL_Y, 0);

            height = pref.getInt(KEY_PORTRAIT_HEIGHT, display.y / 3);
            width = pref.getInt(KEY_PORTRAIT_WIDTH, display.x);
        }
        extraData.putInt(LogPanelView.KEY_MAX_HEIGHT, display.y - MIN_HEIGHT);
        extraData.putInt(LogPanelView.KEY_MAX_WIDTH, display.x - MIN_WIDTH);
        extraData.putInt(LogPanelView.KEY_MAX_TOUCH, MAX_TOUCH);
        extraData.putInt(LogPanelView.KEY_HEIGHT, height - MIN_HEIGHT);
        extraData.putInt(LogPanelView.KEY_WIDTH, width - MIN_WIDTH);
        extraData.putInt(LogPanelView.KEY_TOUCH, INIT_TOUCH);
        mLogPanel.setData(extraData);
        mLogPanelParams.height = height;
        mLogPanelParams.width = width;
        mLogPanelParams.x = x;
        mLogPanelParams.y = y;
    }

    /**
     * Save the last user menu position for the current orientation.
     *
     * @param orientation the orientation to save the position for.
     */
    private void savePanelPosition(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            pref.edit()
                    .putInt(KEY_LANDSCAPE_PANEL_X, mLogPanelParams.x)
                    .putInt(KEY_LANDSCAPE_PANEL_Y, mLogPanelParams.y)
                    .putInt(KEY_LANDSCAPE_HEIGHT, mLogPanelParams.height)
                    .putInt(KEY_LANDSCAPE_WIDTH, mLogPanelParams.width)
                    .apply();
        } else {
            pref.edit()
                    .putInt(KEY_PORTRAIT_PANEL_X, mLogPanelParams.x)
                    .putInt(KEY_PORTRAIT_PANEL_Y, mLogPanelParams.y)
                    .putInt(KEY_PORTRAIT_HEIGHT, mLogPanelParams.height)
                    .putInt(KEY_PORTRAIT_WIDTH, mLogPanelParams.width)
                    .apply();
        }
    }

}
