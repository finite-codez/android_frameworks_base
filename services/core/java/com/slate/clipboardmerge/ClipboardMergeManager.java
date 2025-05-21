/*
 * Copyright (C) SlateOS
 * Ask for permission before copying, using, or modifying this file.
 */

package com.slate.clipboardmerge;

import android.content.Context;

/**
 * Manages clipboard merge sessions and timing logic.
 */
public class ClipboardMergeManager {

    private ClipboardMergeSession session;
    private final ClipboardMergeConfig config;
    private final Context context;

    public ClipboardMergeManager(Context context) {
        this.context = context;
        this.session = new ClipboardMergeSession();
        this.config = new ClipboardMergeConfig();
    }

    public void startNewSession(String initialText) {
        session = new ClipboardMergeSession();
        session.addEntry(initialText);
    }

    public boolean shouldMerge() {
        long now = System.currentTimeMillis();
        return (now - session.getLastUpdatedTime()) <= config.MERGE_TIMEOUT_MS;
    }

    public void mergeClip(String newText) {
        session.addEntry(newText);
    }

    public String getMergedText() {
        return session.getMergedText();
    }

    public boolean isSessionActive() {
        return session != null && !session.isEmpty();
    }
}
