package com.mct.logcatwindow.view.bubble;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.FrameLayout;

class BubbleBaseLayout extends FrameLayout {
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private BubblesLayoutCoordinator layoutCoordinator;

    void setLayoutCoordinator(BubblesLayoutCoordinator layoutCoordinator) {
        this.layoutCoordinator = layoutCoordinator;
    }

    BubblesLayoutCoordinator getLayoutCoordinator() {
        return layoutCoordinator;
    }

    void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    WindowManager getWindowManager() {
        return this.windowManager;
    }

    public void setViewParams(WindowManager.LayoutParams params) {
        this.params = params;
    }

    public WindowManager.LayoutParams getViewParams() {
        return this.params;
    }

    public BubbleBaseLayout(Context context) {
        super(context);
    }

    public BubbleBaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleBaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
