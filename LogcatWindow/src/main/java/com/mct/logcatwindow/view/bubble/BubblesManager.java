package com.mct.logcatwindow.view.bubble;

import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BubblesManager {
    private final WindowManager windowManager;
    private final List<BubbleLayout> bubbles = new ArrayList<>();
    private BubbleTrashLayout bubblesTrash;
    private BubblesLayoutCoordinator layoutCoordinator;


    public BubblesManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void dispose() {
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            BubbleLayout bubble = bubbles.get(i);
            windowManager.removeView(bubble);
            bubbles.remove(bubble);
            bubble.notifyBubbleRemoved();
        }
        bubbles.clear();
    }

    public void setTrash(BubbleTrashLayout trash) {
        if (bubblesTrash == null) {
            bubblesTrash = trash;
            bubblesTrash.setWindowManager(windowManager);
            bubblesTrash.setVisibility(View.GONE);
            addViewToWindow(bubblesTrash);
            initializeLayoutCoordinator();
        }
    }

    public void addBubble(@NonNull BubbleLayout bubble) {
        bubble.setWindowManager(windowManager);
        bubble.setLayoutCoordinator(layoutCoordinator);
        bubbles.add(bubble);
        addViewToWindow(bubble);
    }

    public void removeBubble(BubbleLayout bubble) {
        windowManager.removeView(bubble);
        bubbles.remove(bubble);
        bubble.notifyBubbleRemoved();
    }

    private void addViewToWindow(final BubbleBaseLayout view) {
        windowManager.addView(view, view.getViewParams());
    }

    private void initializeLayoutCoordinator() {
        layoutCoordinator = new BubblesLayoutCoordinator.Builder(this::removeBubble)
                .setWindowManager(windowManager)
                .setTrashView(bubblesTrash)
                .build();
    }

}
