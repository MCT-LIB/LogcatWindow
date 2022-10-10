package com.mct.logcatwindow.view.bubble;

import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

final class BubblesLayoutCoordinator {
    private static BubblesLayoutCoordinator INSTANCE;
    private BubbleTrashLayout trashView;
    private WindowManager windowManager;
    private BubbleReleaseListener listener;

    private static BubblesLayoutCoordinator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BubblesLayoutCoordinator();
        }
        return INSTANCE;
    }

    private BubblesLayoutCoordinator() {
    }

    public void notifyBubblePositionChanged(BubbleLayout bubble) {
        if (trashView != null) {
            trashView.setVisibility(View.VISIBLE);
            if (checkIfBubbleIsOverTrash(bubble)) {
                trashView.applyMagnetism();
                trashView.vibrate();
                applyTrashMagnetismToBubble(bubble);
            } else {
                trashView.releaseMagnetism();
            }
        }
    }

    private void applyTrashMagnetismToBubble(@NonNull BubbleLayout bubble) {
        View trashContentView = getTrashContent();
        int trashCenterX = (trashView.getViewParams().x + (trashContentView.getMeasuredWidth() / 2));
        int trashCenterY = (trashView.getViewParams().y + (trashContentView.getMeasuredHeight() / 2));
        int x = (trashCenterX - (bubble.getMeasuredWidth() / 2));
        int y = (trashCenterY - (bubble.getMeasuredHeight() / 2));
        bubble.getViewParams().x = x;
        bubble.getViewParams().y = y;
        windowManager.updateViewLayout(bubble, bubble.getViewParams());
    }

    private boolean checkIfBubbleIsOverTrash(BubbleLayout bubble) {
        boolean result = false;
        if (trashView.getVisibility() == View.VISIBLE) {
            View trashContentView = getTrashContent();
            int trashWidth = trashContentView.getMeasuredWidth();
            int trashHeight = trashContentView.getMeasuredHeight();
            int trashLeft = (trashView.getViewParams().x - (trashWidth / 2));
            int trashRight = (trashView.getViewParams().x + trashWidth + (trashWidth / 2));
            int trashTop = (trashView.getViewParams().y - (trashHeight / 2));
            int trashBottom = (trashView.getViewParams().y + trashHeight + (trashHeight / 2));
            int bubbleWidth = bubble.getMeasuredWidth();
            int bubbleHeight = bubble.getMeasuredHeight();
            int bubbleLeft = bubble.getViewParams().x;
            int bubbleRight = bubbleLeft + bubbleWidth;
            int bubbleTop = bubble.getViewParams().y;
            int bubbleBottom = bubbleTop + bubbleHeight;
            if (bubbleLeft >= trashLeft && bubbleRight <= trashRight) {
                if (bubbleTop >= trashTop && bubbleBottom <= trashBottom) {
                    result = true;
                }
            }
        }
        return result;
    }

    public void notifyBubbleRelease(BubbleLayout bubble) {
        if (trashView != null) {
            if (checkIfBubbleIsOverTrash(bubble)) {
                listener.onRelease(bubble);
            }
            trashView.setVisibility(View.GONE);
        }
    }

    public static class Builder {
        private final BubblesLayoutCoordinator layoutCoordinator;

        public Builder(BubbleReleaseListener listener) {
            layoutCoordinator = getInstance();
            layoutCoordinator.listener = listener;
        }

        public Builder setTrashView(BubbleTrashLayout trashView) {
            layoutCoordinator.trashView = trashView;
            return this;
        }

        public Builder setWindowManager(WindowManager windowManager) {
            layoutCoordinator.windowManager = windowManager;
            return this;
        }

        public BubblesLayoutCoordinator build() {
            return layoutCoordinator;
        }
    }

    private View getTrashContent() {
        return trashView.getChildAt(0);
    }

    public interface BubbleReleaseListener {
        void onRelease(BubbleLayout bubble);
    }
}
