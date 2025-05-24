package com.android.systemui.echotap;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.CoreStartable;

public class EchoTapStartable extends CoreStartable {
    private static final String TAG = "EchoTapStartable";
    private EchoTapController mController;

    public EchoTapStartable(Context context) {
        super(context);
    }

    @Override
    public void start() {
        boolean enabled = Settings.Secure.getInt(
                mContext.getContentResolver(),
                "echotap_enabled", 0) == 1;

        if (enabled) {
            mController = new EchoTapController(mContext);
            mController.start();
            Log.d(TAG, "EchoTap enabled and started");
        }

        // Observe setting toggle
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor("echotap_enabled"),
                false,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        boolean enabled = Settings.Secure.getInt(
                                mContext.getContentResolver(),
                                "echotap_enabled", 0) == 1;

                        if (enabled) {
                            if (mController == null) {
                                mController = new EchoTapController(mContext);
                                mController.start();
                            }
                        } else {
                            if (mController != null) {
                                mController.stop();
                                mController = null;
                            }
                        }
                        Log.d(TAG, "EchoTap setting changed: " + enabled);
                    }
                }
        );
    }
}
