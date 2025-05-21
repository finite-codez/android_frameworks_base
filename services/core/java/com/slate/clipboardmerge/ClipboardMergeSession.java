/*
 * Copyright (C) SlateOS
 * Ask for permission before copying, using, or modifying this file.
 */

package com.slate.clipboardmerge;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single clipboard merge session.
 */
public class ClipboardMergeSession {

    private final List<String> entries;
    private long lastUpdatedTime;

    public ClipboardMergeSession() {
        this.entries = new ArrayList<>();
        this.lastUpdatedTime = System.currentTimeMillis();
    }

    public void addEntry(String text) {
        entries.add(text);
        lastUpdatedTime = System.currentTimeMillis();
    }

    public String getMergedText() {
        return String.join("\n", entries);
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
