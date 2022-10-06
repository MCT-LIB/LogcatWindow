package com.mct.logcatwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;

import com.mct.logcatwindow.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "ConstantConditions"})
public class LogWindow {
    private LogView logView;
    private LayoutParams params;
    private WindowManager wm;
    private boolean isAdd = false;
    private final Context mContext;
    private final Application mApplication;
    private final WeakReference<LogWindow> owf;
    private OnTouchListener onTouchListener;
    private boolean registerLifeCycleInStop = false;
    private LogConfig logConfig;
    private static final int INITIAL_HEIGHT = 5;
    private static final int MIN_HEIGHT = 170;
    private static final int MAX_HEIGHT_OFFSET = 10;
    private static final int INITIAL_TOUCH = 25;
    private static final int MAX_TOUCH_AREA = 50;
    private static final int MIN_WIDTH = 100;
    private LogView.ChangeWindowListener changeWindowListener;

    public LogWindow setRegisterLifeCycleInStop(boolean registerLifeCycleInStop) {
        this.registerLifeCycleInStop = registerLifeCycleInStop;
        return this;
    }

    public LogWindow(Context context, Application application) {
        this.mContext = context;
        this.mApplication = application;
        this.owf = new WeakReference<>(this);
    }

    public LogWindow setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
        return this;
    }

    public LogWindow setWindowManager(WindowManager wm, LayoutParams params) {
        this.wm = wm;
        this.params = params;
        return this;
    }

    public LogWindow setLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
        return this;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void createLogView() {
        Log.d(LogUtils.LOGCAT_WINDOW_TAG, "create LogView");
        if (!this.isAdd) {
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("width", LogUtils.getDevDisplay(this.mContext)[0]);
            extraData.put("height", LogUtils.getDevDisplay(this.mContext)[1] / INITIAL_HEIGHT);
            extraData.put("touchArea", LogUtils.dpToPx(this.mContext, (float) INITIAL_TOUCH));
            extraData.put("touchAreaMax", LogUtils.dpToPx(this.mContext, (float) MAX_TOUCH_AREA));
            if (this.wm == null) {
                this.wm = (WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE);
                this.params = new LayoutParams();
                this.params.format = 1;
                this.params.type = LayoutParams.TYPE_APPLICATION;
                this.params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
                this.params.width = -1;
                this.params.height = LogUtils.dpToPx(this.mContext, (float) (Integer) extraData.get("height"));
            }

            this.isAdd = true;
            if (this.logView == null) {
                this.logView = new LogView(this.mContext, extraData);
            }

            if (this.onTouchListener == null) {
                this.onTouchListener = new OnTouchListener() {
                    int lastX;
                    int lastY;
                    int paramX;
                    int paramY;
                    int startX;
                    int startY;

                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                logView.hideKeyboardAndFocus();
                                this.lastX = (int) event.getRawX();
                                this.lastY = (int) event.getRawY();
                                this.paramX = params.x;
                                this.paramY = params.y;
                                this.startX = this.lastX;
                                this.startY = this.lastY;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                int dx = (int) event.getRawX() - this.lastX;
                                int dy = (int) event.getRawY() - this.lastY;
                                params.x = this.paramX + dx;
                                params.y = this.paramY + dy;
                                wm.updateViewLayout(logView, params);
                        }
                        return false;
                    }
                };
            }

            if (this.changeWindowListener == null) {
                this.changeWindowListener = new LogView.ChangeWindowListener() {
                    public void changeWindowHeight(int height) {
                        if (height < LogUtils.dpToPx(mContext, (float) MIN_HEIGHT)) {
                            height = LogUtils.dpToPx(mContext, (float) MIN_HEIGHT);
                        }

                        if (height >= LogUtils.getDevDisplay(mContext)[1] - LogUtils.dpToPx(mContext, (float) MAX_HEIGHT_OFFSET)) {
                            height = LogUtils.getDevDisplay(mContext)[1] - LogUtils.dpToPx(mContext, (float) MAX_HEIGHT_OFFSET);
                        }

                        params.height = height;
                        wm.updateViewLayout(logView, params);
                    }

                    public void changeWindowsWidth(int width) {
                        if (width <= LogUtils.dpToPx(mContext, (float) MIN_WIDTH)) {
                            width = LogUtils.dpToPx(mContext, (float) MIN_WIDTH);
                        }

                        params.width = width;
                        wm.updateViewLayout(logView, params);
                    }
                };
            }

            this.logView.setOnTouchListener(this.onTouchListener);
            this.logView.setChangeWindowListener(this.changeWindowListener);
            if (this.logConfig != null && this.logView.getLogManager() != null) {
                this.logView.getLogManager().setLogConfig(this.logConfig);
            }

            this.mApplication.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                }

                public void onActivityStarted(Activity activity) {
                }

                public void onActivityResumed(Activity activity) {
                }

                public void onActivityPaused(Activity activity) {
                }

                public void onActivityStopped(Activity activity) {
                    LogWindow logWindow = owf.get();
                    if (logWindow != null) {
                        if (logWindow.registerLifeCycleInStop) {
                            WindowManager wm = logWindow.wm;
                            LogView logView = logWindow.logView;
                            if (logWindow.mContext == activity && wm != null && logView != null && activity != null && (logView.isActivated() || logView.isEnabled())) {
                                removeLogView(wm, logView);
                            }

                            if (activity != null) {
                                activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                            }
                        }
                    }
                }

                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                public void onActivityDestroyed(Activity activity) {
                    LogWindow logWindow = owf.get();
                    if (logWindow != null) {
                        WindowManager wm = logWindow.wm;
                        LogView logView = logWindow.logView;
                        if (!logWindow.registerLifeCycleInStop) {
                            if (logWindow.mContext == activity && wm != null && logView != null && activity != null && activity.isFinishing() && (logView.isActivated() || logView.isEnabled())) {
                                removeLogView(wm, logView);
                            }

                            if (activity != null) {
                                activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                            }
                        }
                    }
                }
            });

            this.logView.setOnKeyListener((v, keyCode, event) -> {
                if (KeyEvent.KEYCODE_BACK == event.getKeyCode() && event.getAction() == KeyEvent.ACTION_UP) {
                    removeLogView(wm, logView);
                    return true;
                }
                return false;
            });
            wm.addView(this.logView, this.params);
        }
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void removeLogView() {
        Log.d(LogUtils.LOGCAT_WINDOW_TAG, "remove LogView");
        removeLogView(wm, logView);
    }

    private void removeLogView(@NonNull WindowManager wm, View view) {
        if (isAdd) {
            isAdd = false;
            wm.removeViewImmediate(view);
        }
    }
}
