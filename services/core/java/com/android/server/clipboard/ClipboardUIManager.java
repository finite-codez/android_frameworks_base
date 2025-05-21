/*
 * SlateOS Clipboard Merge
 * Copyright (c) 2025 SlateOS
 * Ask for permission before copying.
 */

package com.android.server.clipboard;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.widget.Toast;

public class ClipboardUIManager {
    private static ClipboardUIManager sInstance;
    private final Context mContext;
    private final Handler mHandler;

    private ClipboardUIManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new ClipboardUIManager(context);
        }
    }

    public static ClipboardUIManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("ClipboardUIManager is not initialized");
        }
        return sInstance;
    }

    public void showMergedToast(int userId, String mergedText) {
        if (mergedText == null || mergedText.isEmpty()) return;

        final String toastMsg = "📋 Merged clipboard ready";
        mHandler.post(() -> {
            Context userContext = mContext.createContextAsUser(UserHandle.of(userId), 0);
            Toast.makeText(userContext, toastMsg, Toast.LENGTH_SHORT).show();
        });
    }
}
