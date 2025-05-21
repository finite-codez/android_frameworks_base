/*
 * Copyright (C) SlateOS
 * Ask for permission before copying, using, or modifying this file.
 */

package com.slate.clipboardmerge;

import android.view.KeyEvent;

/**
 * Detects volume button combinations to trigger clipboard merge actions.
 */
public class ClipboardMergeTrigger {

    private boolean volUpPressed = false;
    private boolean volDownPressed = false;

    public boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            volUpPressed = down;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            volDownPressed = down;
        }

        return volUpPressed && volDownPressed;
    }

    public void reset() {
        volUpPressed = false;
        volDownPressed = false;
    }
}
