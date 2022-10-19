package com.mct.logcatwindow.view.bubble;

import android.view.View;

import androidx.annotation.NonNull;

final class BubblesLayoutCoordinator {

    private final BubbleTrashLayout trashView;
    private final BubbleReleaseListener listener;

    public BubblesLayoutCoordinator(BubbleTrashLayout trashView, BubbleReleaseListener listener) {
        this.trashView = trashView;
        this.listener = listener;
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
        View trashContentView = trashView.getTrashContent();
        int trashCenterX = (trashView.getViewParams().x + (trashContentView.getMeasuredWidth() / 2));
        int trashCenterY = (trashView.getViewParams().y + (trashContentView.getMeasuredHeight() / 2));
        int x = (trashCenterX - (bubble.getMeasuredWidth() / 2));
        int y = (trashCenterY - (bubble.getMeasuredHeight() / 2));
        bubble.getViewParams().x = x;
        bubble.getViewParams().y = y;
        bubble.updateLayoutParams();
    }

    private boolean checkIfBubbleIsOverTrash(@NonNull BubbleLayout bubble) {
        boolean result = false;
        int trashWidth = trashView.getTrashContent().getMeasuredWidth();
        int trashHeight = trashView.getTrashContent().getMeasuredHeight();
        int trashLeft = (trashView.getViewParams().x - trashWidth);
        int trashRight = (trashView.getViewParams().x + 2 * trashWidth);
        int trashTop = (trashView.getViewParams().y - trashHeight);
        int trashBottom = (trashView.getViewParams().y + 2 * trashHeight);
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

    public interface BubbleReleaseListener {
        void onRelease(BubbleLayout bubble);
    }
}
