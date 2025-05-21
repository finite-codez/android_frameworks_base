/*
 * SlateOS Clipboard Merge
 * Copyright (c) 2025 SlateOS
 * Ask for permission before copying.
 */

package com.android.server.clipboard;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClipboardMergeManager {

    private static final String TAG = "SlateOSClipboardMerge";
    private static final long SESSION_TIMEOUT_MS = 10 * 1000; // 10 seconds for now

    private static ClipboardMergeManager sInstance;

    private final List<ClipboardSnippet> mSessionSnippets = new ArrayList<>();
    private long mLastCopyTime = 0;

    private ClipboardMergeManager() {}

    public static ClipboardMergeManager getInstance() {
        if (sInstance == null) {
            sInstance = new ClipboardMergeManager();
        }
        return sInstance;
    }

    public void onCopy(String text) {
        long now = SystemClock.elapsedRealtime();

        if (now - mLastCopyTime <= SESSION_TIMEOUT_MS) {
            mSessionSnippets.add(new ClipboardSnippet(text, now));
        } else {
            mSessionSnippets.clear();
            mSessionSnippets.add(new ClipboardSnippet(text, now));
        }

        mLastCopyTime = now;
        Log.d(TAG, "Copied text added to clipboard session: " + text);
    }

    public String getMergedText() {
        cleanExpiredSnippets();
        StringBuilder merged = new StringBuilder();
        for (ClipboardSnippet snippet : mSessionSnippets) {
            merged.append(snippet.text).append(" ");
        }
        return merged.toString().trim();
    }

    public void clearSession() {
        mSessionSnippets.clear();
        mLastCopyTime = 0;
    }

    private void cleanExpiredSnippets() {
        long now = SystemClock.elapsedRealtime();
        Iterator<ClipboardSnippet> iterator = mSessionSnippets.iterator();
        while (iterator.hasNext()) {
            ClipboardSnippet s = iterator.next();
            if (now - s.timestamp > SESSION_TIMEOUT_MS) {
                iterator.remove();
            }
        }
    }

    // Todo: saveToStorage(), loadFromStorage(), promptConsentIfNeeded() here later

    private static class ClipboardSnippet {
        final String text;
        final long timestamp;

        ClipboardSnippet(String text, long timestamp) {
            this.text = text;
            this.timestamp = timestamp;
        }
    }
}
