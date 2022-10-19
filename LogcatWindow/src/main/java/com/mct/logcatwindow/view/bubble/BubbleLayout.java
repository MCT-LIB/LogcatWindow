package com.mct.logcatwindow.view.bubble;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.mct.logcatwindow.R;
import com.mct.logcatwindow.utils.SpringInterpolator;
import com.mct.logcatwindow.utils.Utils;

public class BubbleLayout extends BubbleBaseLayout {

    private OnBubbleRemoveListener onBubbleRemoveListener;
    private OnBubbleClickListener onBubbleClickListener;
    private RequestDisplayListener requestDisplayListener = Utils::getDisplay;
    private final ObjectAnimator xAnimator;

    public BubbleLayout(Context context) {
        this(context, null);
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        xAnimator = ObjectAnimator.ofFloat(this, WINDOW_X, 0);
        xAnimator.setInterpolator(SpringInterpolator.softSpring());
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

    private static final int STATE_DOWN = 0;
    private static final int STATE_MOVE = 1;

    private int state;
    private final Point moveInitPos = new Point();
    private final Point moveInitTouchPos = new Point();

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            event.offsetLocation(getViewParams().x, getViewParams().y);
            WindowManager.LayoutParams params = getViewParams();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    state = STATE_DOWN;
                    xAnimator.cancel();
                    moveInitPos.set(params.x, params.y);
                    moveInitTouchPos.set((int) event.getRawX(), (int) event.getRawY());
                    playAnimationClickDown();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = moveInitPos.x + (int) (event.getRawX() - moveInitTouchPos.x);
                    int y = moveInitPos.y + (int) (event.getRawY() - moveInitTouchPos.y);
                    params.x = x;
                    params.y = y < 0 ? 0 : Math.min(y, requestDisplayListener.request().y - params.height);
                    getWindowManager().updateViewLayout(this, params);
                    if (state != STATE_MOVE) {
                        int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                        if (Math.abs(event.getRawX() - moveInitTouchPos.x) >= touchSlop
                                || Math.abs(event.getRawY() - moveInitTouchPos.y) >= touchSlop) {
                            state = STATE_MOVE;
                        }
                    } else {
                        if (getLayoutCoordinator() != null) {
                            getLayoutCoordinator().notifyBubblePositionChanged(this);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("ddd", "ACTION_UP: ");
                    goToWall();
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubbleRelease(this);
                        playAnimationClickUp();
                    }
                    if (state == STATE_DOWN) {
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
        int width = requestDisplayListener.request().x;
        int middle = width / 2;
        float nearestXWall = getViewParams().x >= middle ? width - getMeasuredWidth() + 24 : -24;
        xAnimator.setFloatValues(nearestXWall);
        xAnimator.setDuration(1000);
        xAnimator.start();
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
