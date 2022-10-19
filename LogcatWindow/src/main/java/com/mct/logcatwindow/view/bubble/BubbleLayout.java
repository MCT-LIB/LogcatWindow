package com.mct.logcatwindow.view.bubble;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.mct.logcatwindow.R;
import com.mct.logcatwindow.utils.Utils;
import com.mct.touchutils.TouchUtils;

public class BubbleLayout extends BubbleBaseLayout {

    private OnBubbleRemoveListener onBubbleRemoveListener;
    private OnBubbleClickListener onBubbleClickListener;
    private RequestDisplayListener requestDisplayListener = Utils::getDisplay;

    public BubbleLayout(Context context) {
        this(context, null);
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        TouchUtils.setTouchListener(this, new TouchUtils.FlingMoveToWallListener() {

            final FloatPropertyCompat<View> propertyCompatX, propertyCompatY;

            {
                propertyCompatX = WINDOW_X.getPropertyCompat();
                propertyCompatY = WINDOW_Y.getPropertyCompat();
            }

            @NonNull
            @Override
            protected Rect initArea(View view) {
                int offset = 24;
                return new Rect(-offset, -offset,
                        requestDisplayListener.request().x + offset,
                        requestDisplayListener.request().y + offset);
            }

            @Override
            protected FloatPropertyCompat<View> getPropX() {
                return propertyCompatX;
            }

            @Override
            protected FloatPropertyCompat<View> getPropY() {
                return propertyCompatY;
            }

            @Override
            protected float getFrictionY() {
                return 2;
            }

            @Override
            protected boolean onDown(View view, MotionEvent event) {
                playAnimationClickDown();
                return true;
            }

            @Override
            protected boolean onMove(View view, MotionEvent event) {
                if (isTouching()) {
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubblePositionChanged(BubbleLayout.this);
                    }
                }
                return true;
            }

            @Override
            protected boolean onStop(View view, MotionEvent event) {
                if (getLayoutCoordinator() != null) {
                    getLayoutCoordinator().notifyBubbleRelease(BubbleLayout.this);
                    playAnimationClickUp();
                }
                if (!isTouching()) {
                    if (onBubbleClickListener != null) {
                        onBubbleClickListener.onBubbleClick(BubbleLayout.this);
                    }
                }
                return true;
            }

        });
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
