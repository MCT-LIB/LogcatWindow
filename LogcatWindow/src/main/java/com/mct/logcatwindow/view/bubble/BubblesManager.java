package com.mct.logcatwindow.view.bubble;

import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BubblesManager {
    private final WindowManager windowManager;
    private final List<BubbleLayout> bubbles;
    private BubbleTrashLayout bubblesTrash;
    private BubblesLayoutCoordinator layoutCoordinator;

    private static BubblesManager instance;

    public static BubblesManager getInstance(WindowManager windowManager) {
        if (instance == null) {
            instance = new BubblesManager(windowManager);
        }
        return instance;
    }

    private BubblesManager(WindowManager windowManager) {
        this.windowManager = windowManager;
        this.bubbles = new ArrayList<>();
    }

    public void dispose() {
        if (bubblesTrash != null) {
            bubblesTrash.detachFromWindow();
        }
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            BubbleLayout bubble = bubbles.get(i);
            bubbles.remove(bubble);
            bubble.detachFromWindow();
            bubble.notifyBubbleRemoved();
        }
        bubbles.clear();
    }

    public void setTrash(BubbleTrashLayout trash) {
        if (bubblesTrash == null) {
            bubblesTrash = trash;
            bubblesTrash.setWindowManager(windowManager);
            bubblesTrash.setVisibility(View.GONE);
            if (!bubbles.isEmpty()) {
                bubblesTrash.attachToWindow();
            }
            layoutCoordinator = new BubblesLayoutCoordinator(bubblesTrash, this::removeBubble);
        }
    }

    public void addBubble(@NonNull BubbleLayout bubble) {
        bubble.setWindowManager(windowManager);
        bubble.setLayoutCoordinator(layoutCoordinator);
        bubble.attachToWindow();
        if (!bubbles.contains(bubble)) {
            bubbles.add(bubble);
        }
        if (bubblesTrash != null) {
            bubblesTrash.detachFromWindow();
            bubblesTrash.attachToWindow();
        }
    }

    public void removeBubble(BubbleLayout bubble) {
        windowManager.removeView(bubble);
        bubbles.remove(bubble);
        bubble.notifyBubbleRemoved();
    }

}
