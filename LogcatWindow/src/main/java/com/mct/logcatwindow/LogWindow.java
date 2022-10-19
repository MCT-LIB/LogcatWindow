package com.mct.logcatwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.mct.logcatwindow.utils.LogWindowPreferences;
import com.mct.logcatwindow.utils.ScreenMetrics;
import com.mct.logcatwindow.utils.Utils;
import com.mct.logcatwindow.view.LogPanelView;
import com.mct.logcatwindow.view.LogPanelView.OnWindowChangeListener;
import com.mct.logcatwindow.view.bubble.BubbleLayout;
import com.mct.logcatwindow.view.bubble.BubbleTrashLayout;
import com.mct.logcatwindow.view.bubble.BubblesManager;

import org.jetbrains.annotations.Contract;

public class LogWindow {

    private static final int MIN_HEIGHT = Utils.dp2px(170);
    private static final int MIN_WIDTH = Utils.dp2px(150);
    private static final int MAX_TOUCH = Utils.dp2px(40);
    private static final int INIT_TOUCH = Utils.dp2px(20);

    private final LogWindowPreferences preferences;
    private final ScreenMetrics screenMetrics;
    private final WindowManager windowManager;

    private BubblesManager bubblesManager;

    private BubbleTrashLayout trashLayout;
    private BubbleLayout bubbleLayout;

    private LogPanelView logPanel;
    private LayoutParams logPanelParams;

    private static LogWindow instance;

    private LogWindow(@NonNull Context context) {
        this.screenMetrics = new ScreenMetrics(context);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.preferences = LogWindowPreferences.getInstance(context);

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
            instance.logPanel.dispose();
            instance.screenMetrics.dispose();
            instance.bubblesManager.dispose();
            instance = null;
            Log.d(Utils.LOGCAT_WINDOW_TAG, "dispose");
        }
    }

    @SuppressWarnings("unused")
    public LogWindow setLogConfig(LogConfig logConfig) {
        logPanel.getLogManager().setLogConfig(logConfig);
        return this;
    }

    public void attachBubbleControl(FragmentActivity activity) {
        Utils.requestOverlayPermission(activity, (allGranted, gl, dl) -> {
            if (allGranted) {
                if (!trashLayout.isAttachedToWindow()) {
                    bubblesManager.setTrash(trashLayout);
                }
                if (!bubbleLayout.isAttachedToWindow()) {
                    bubbleLayout.getViewParams().x = -20;
                    bubbleLayout.getViewParams().y = 200;
                    bubblesManager.addBubble(bubbleLayout);
                }
            }
        });
    }

    private void attachLogPanel() {
        if (!logPanel.isAttachedToWindow()) {
            bubbleLayout.setVisibility(View.GONE);
            screenMetrics.computeSelf();
            loadPanelPosition(screenMetrics.getOrientation());
            windowManager.addView(logPanel, logPanelParams);
            Log.d(Utils.LOGCAT_WINDOW_TAG, "attach LogView");
        }
    }

    private void detachLogPanel() {
        if (logPanel.isAttachedToWindow()) {
            bubbleLayout.setVisibility(View.VISIBLE);
            savePanelPosition(screenMetrics.getOrientation());
            windowManager.removeView(logPanel);
            Log.d(Utils.LOGCAT_WINDOW_TAG, "detach LogView");
        }
    }

    @SuppressLint("InflateParams")
    private void initBubble(Context context) {
        bubblesManager = BubblesManager.getInstance(windowManager);

        trashLayout = new BubbleTrashLayout(context);
        LayoutInflater.from(context).inflate(R.layout.lw_layout_bubble_trash, trashLayout, true);
        int x = (Utils.getScreenWidth() - trashLayout.getTrashContent().getLayoutParams().width) / 2;
        int y = Utils.getScreenHeight() - trashLayout.getTrashContent().getLayoutParams().height - 32;
        trashLayout.setViewParams(getWindowParams(
                x, y,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                false)
        );

        bubbleLayout = (BubbleLayout) LayoutInflater.from(context).inflate(R.layout.lw_layout_bubble, null);
        bubbleLayout.setViewParams(getWindowParams(
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
        logPanel = new LogPanelView(context);
        logPanelParams = getWindowParams(0, 0,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                true);
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
                        LayoutParams params = correctLayoutPosition(screenMetrics.getDisplaySize(), logPanelParams, x, y);
                        windowManager.updateViewLayout(logPanel, params);
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
                LayoutParams params = correctLayoutPosition(screenMetrics.getDisplaySize(), logPanelParams);
                windowManager.updateViewLayout(logPanel, params);
            }

            @Override
            public void changeWindowWidth(int width) {
                logPanelParams.width = width + MIN_WIDTH;
                LayoutParams params = correctLayoutPosition(screenMetrics.getDisplaySize(), logPanelParams);
                windowManager.updateViewLayout(logPanel, params);
            }

            @Override
            public void onDropDownChanged(boolean isOpened) {
                if (isOpened) {
                    logPanelParams.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    logPanelParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                } else {
                    logPanelParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    logPanelParams.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
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
        Toast.makeText(trashLayout.getContext(), "is LANDSCAPE: "
                        + (screenMetrics.getOrientation() == Configuration.ORIENTATION_LANDSCAPE),
                Toast.LENGTH_SHORT).show();
        Point display = screenMetrics.getDisplaySize();
        int x = (display.x - trashLayout.getTrashContent().getLayoutParams().width) / 2;
        int y = display.y - trashLayout.getTrashContent().getLayoutParams().height - 32;

        trashLayout.getViewParams().x = x;
        trashLayout.getViewParams().y = y;
        trashLayout.updateLayoutParams();

        bubbleLayout.getViewParams().x = -20;
        bubbleLayout.getViewParams().y = 200;
        bubbleLayout.updateLayoutParams();

        savePanelPosition(screenMetrics.getOrientation() == Configuration.ORIENTATION_LANDSCAPE
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
        Point display = screenMetrics.getDisplaySize();
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
        extraData.putInt(LogPanelView.KEY_HEIGHT, Math.max(size.y - MIN_HEIGHT, MIN_HEIGHT));
        extraData.putInt(LogPanelView.KEY_WIDTH, Math.max(size.x - MIN_WIDTH, MIN_WIDTH));
        extraData.putInt(LogPanelView.KEY_TOUCH, INIT_TOUCH);
        logPanel.setData(extraData);
        logPanelParams.height = (int) extraData.get(LogPanelView.KEY_HEIGHT) + MIN_HEIGHT;
        logPanelParams.width = (int) extraData.get(LogPanelView.KEY_WIDTH) + MIN_WIDTH;
        logPanelParams.x = position.x;
        logPanelParams.y = position.y;
    }

    /**
     * Save the last user menu position for the current orientation.
     *
     * @param orientation the orientation to save the position for.
     */
    private void savePanelPosition(int orientation) {
        Point position = new Point(logPanelParams.x, logPanelParams.y);
        Point size = new Point(logPanelParams.width, logPanelParams.height);
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;
        preferences.savePanelPosition(position, size, isLandscape);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Internal Utils
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    @Contract("_, _, _, _, _ -> new")
    static WindowManager.LayoutParams getWindowParams(int x, int y, int width, int height, boolean focusable) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width, height,
                getType(), getFlag(focusable),
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;
        return params;
    }

    private static int getType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private static int getFlag(boolean focusable) {
        if (focusable) {
            return WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            // | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        } else {
            return WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }
    }

    @NonNull
    private static WindowManager.LayoutParams correctLayoutPosition(
            Point displaySize, @NonNull WindowManager.LayoutParams params) {
        return correctLayoutPosition(displaySize, params, params.x, params.y);
    }


    @NonNull
    private static WindowManager.LayoutParams correctLayoutPosition(
            Point displaySize, @NonNull WindowManager.LayoutParams params, int x, int y) {
        params.x = x < 0 ? 0 : Math.min(x, displaySize.x - params.width);
        params.y = y < 0 ? 0 : Math.min(y, displaySize.y - params.height);
        return params;
    }

}
