/*
 * Copyright (C) SlateOS
 * Ask for permission before copying, using, or modifying this file.
 */

package com.slate.clipboardmerge;

import android.app.AlertDialog;
import android.content.Context;

/**
 * UI dialog to confirm clipboard merge decision.
 */
public class ClipboardMergePromptDialog {

    public static void show(Context context, Runnable onMerge, Runnable onNew) {
        new AlertDialog.Builder(context)
            .setTitle("Merge Clipboard?")
            .setMessage("Do you want to merge this with the last copied text?")
            .setPositiveButton("Merge", (d, w) -> onMerge.run())
            .setNegativeButton("Start New", (d, w) -> onNew.run())
            .show();
    }
}
