/*
 * Copyright (C) 2025 SlateOS
 *
 * This file is part of the SlateOS project. All rights reserved.
 * Redistribution or use in other projects requires prior permission.
 */

package com.slate.clipboardmerge;

import java.util.ArrayList;
import java.util.List;

public class ClipboardMergeSession {
    private final List<String> clipboardSnippets = new ArrayList<>();
    private long lastCopyTimestamp = 0L;

    public void addSnippet(String text) {
        if (text != null && !text.isEmpty()) {
            clipboardSnippets.add(text);
            lastCopyTimestamp = System.currentTimeMillis();
        }
    }

    public String getMergedContent() {
        return String.join("\n", clipboardSnippets);
    }

    public void reset() {
        clipboardSnippets.clear();
        lastCopyTimestamp = 0L;
    }

    public long getLastCopyTimestamp() {
        return lastCopyTimestamp;
    }

    public boolean isEmpty() {
        return clipboardSnippets.isEmpty();
    }
}
