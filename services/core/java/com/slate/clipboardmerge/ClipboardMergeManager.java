/*
 * Copyright (C) 2025 SlateOS
 *
 * This file is part of the SlateOS project. All rights reserved.
 * Redistribution or use in other projects requires prior permission.
 */

package com.slate.clipboardmerge;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ClipboardMergeManager {
    private final Context context;
    private final ClipboardManager clipboardManager;
    private final ClipboardMergeSession mergeSession = new ClipboardMergeSession();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ClipboardMergeManager(Context context) {
        this.context = context;
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void onCopy(String copiedText) {
        mergeSession.addSnippet(copiedText);
    }

    public void triggerMerge() {
        if (!mergeSession.isEmpty()) {
            clipboardManager.setPrimaryClip(android.content.ClipData.newPlainText("Merged Clipboard", mergeSession.getMergedContent()));
            Toast.makeText(context, "Clipboard merged", Toast.LENGTH_SHORT).show();
        }
    }

    public long getLastCopyTime() {
        return mergeSession.getLastCopyTimestamp();
    }

    public void resetMergeSession() {
        mergeSession.reset();
    }
}
