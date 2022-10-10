package com.mct.logcatwindow.view.bubble;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;

import com.mct.logcatwindow.R;

public class BubbleTrashLayout extends BubbleBaseLayout {
    public static final int VIBRATION_DURATION_IN_MS = 70;
    private boolean magnetismApplied = false;
    private boolean isVibrateInThisSession = false;

    public BubbleTrashLayout(Context context) {
        super(context);
    }

    public BubbleTrashLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleTrashLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        if (isAttachedToWindow()) {
            if (visibility != getVisibility()) {
                if (visibility == VISIBLE) {
                    playAnimation(R.animator.lw_bubble_trash_shown_animator);
                } else {
                    playAnimation(R.animator.lw_bubble_trash_hide_animator);
                }
            }
        }
        super.setVisibility(visibility);
    }

    void applyMagnetism() {
        if (!magnetismApplied) {
            magnetismApplied = true;
            setSelected(true);
            playAnimation(R.animator.lw_bubble_trash_shown_magnetism_animator);
        }
    }

    void vibrate() {
        if (!isVibrateInThisSession) {
            final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATION_DURATION_IN_MS);
            isVibrateInThisSession = true;
        }
    }

    void releaseMagnetism() {
        if (magnetismApplied) {
            magnetismApplied = false;
            setSelected(false);
            playAnimation(R.animator.lw_bubble_trash_hide_magnetism_animator);
        }
        isVibrateInThisSession = false;
    }

    private void playAnimation(int animationResourceId) {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), animationResourceId);
            animator.setTarget(getChildAt(0));
            animator.start();
        }
    }
}
