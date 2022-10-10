package com.mct.logcatwindow.view.bubble;

import static com.mct.logcatwindow.utils.ParamsUtils.correctLayoutPosition;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.mct.logcatwindow.R;
import com.mct.logcatwindow.utils.Utils;

public class BubbleLayout extends BubbleBaseLayout {

    private OnBubbleRemoveListener onBubbleRemoveListener;
    private OnBubbleClickListener onBubbleClickListener;
    private RequestDisplayListener requestDisplayListener = Utils::getDisplay;
    private static final int TOUCH_TIME_THRESHOLD = 175;
    private final MoveAnimator animator;
    private boolean shouldStickToWall = true;

    public BubbleLayout(Context context) {
        this(context, null);
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        animator = new MoveAnimator();
        initializeView();
    }

    public void setOnBubbleRemoveListener(OnBubbleRemoveListener listener) {
        onBubbleRemoveListener = listener;
    }

    public void setOnBubbleClickListener(OnBubbleClickListener listener) {
        onBubbleClickListener = listener;
    }

    public void setRequestDisplayListener(RequestDisplayListener requestDisplayListener) {
        this.requestDisplayListener = requestDisplayListener;
    }

    public void setShouldStickToWall(boolean shouldStick) {
        this.shouldStickToWall = shouldStick;
    }

    void notifyBubbleRemoved() {
        if (onBubbleRemoveListener != null) {
            onBubbleRemoveListener.onBubbleRemoved(this);
        }
    }

    private void initializeView() {
        setClickable(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(() -> {
            getViewParams().width = getMeasuredWidth();
            getViewParams().height = getMeasuredHeight();
        });
        playAnimation();
    }

    private final Point moveInitPos = new Point();
    private final Point moveInitTouchPos = new Point();
    private long lastTouchDown;
    private boolean isShowTrash;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isShowTrash = false;
                    lastTouchDown = System.currentTimeMillis();
                    moveInitPos.set(getViewParams().x, getViewParams().y);
                    moveInitTouchPos.set((int) event.getRawX(), (int) event.getRawY());
                    playAnimationClickDown();
                    animator.stop();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = moveInitPos.x + (int) (event.getRawX() - moveInitTouchPos.x);
                    int y = moveInitPos.y + (int) (event.getRawY() - moveInitTouchPos.y);
                    WindowManager.LayoutParams params = correctLayoutPosition(
                            requestDisplayListener.request(),
                            getViewParams(),
                            x, y
                    );
                    getWindowManager().updateViewLayout(this, params);
                    if (isShowTrash || System.currentTimeMillis() - lastTouchDown > TOUCH_TIME_THRESHOLD) {
                        isShowTrash = true;
                        if (getLayoutCoordinator() != null) {
                            getLayoutCoordinator().notifyBubblePositionChanged(this);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    goToWall();
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubbleRelease(this);
                        playAnimationClickUp();
                    }
                    if (System.currentTimeMillis() - lastTouchDown < TOUCH_TIME_THRESHOLD) {
                        if (onBubbleClickListener != null) {
                            onBubbleClickListener.onBubbleClick(this);
                        }
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void playAnimation() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.lw_bubble_shown_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    private void playAnimationClickDown() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.lw_bubble_down_click_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    private void playAnimationClickUp() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.lw_bubble_up_click_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    public void goToWall() {
        if (shouldStickToWall) {
            int width = requestDisplayListener.request().x;
            int middle = width / 2;
            float nearestXWall = getViewParams().x >= middle ? width - getMeasuredWidth() : 0;
            animator.start(nearestXWall, getViewParams().y);
        }
    }

    private void move(float deltaX, float deltaY) {
        getViewParams().x += deltaX;
        getViewParams().y += deltaY;
        getWindowManager().updateViewLayout(this, getViewParams());
    }

    private class MoveAnimator implements Runnable {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        private void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() != null && getRootView().getParent() != null) {
                float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
                float deltaX = (destinationX - getViewParams().x) * progress;
                float deltaY = (destinationY - getViewParams().y) * progress;
                move(deltaX, deltaY);
                if (progress < 1) {
                    handler.post(this);
                }
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }

    public interface OnBubbleRemoveListener {
        void onBubbleRemoved(BubbleLayout bubble);
    }

    public interface OnBubbleClickListener {
        void onBubbleClick(BubbleLayout bubble);
    }

    public interface RequestDisplayListener {
        Point request();
    }
}
