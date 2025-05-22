package com.android.systemui.display;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.hardware.display.ColorDisplayManager;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GrayscaleController extends ContentObserver {

    private static final String TAG = "GrayscaleController";

    private final Context mContext;
    private final ColorDisplayManager mColorDisplayManager;
    private final Handler mHandler;

    public GrayscaleController(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
        mColorDisplayManager = mContext.getSystemService(ColorDisplayManager.class);

        register();
        updateGrayscaleMode();
    }

    private void register() {
        Uri uri = Settings.Secure.getUriFor("scheduled_grayscale_enabled");
        mContext.getContentResolver().registerContentObserver(uri, false, this);

        mContext.getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor("scheduled_grayscale_start_time"), false, this);

        mContext.getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor("scheduled_grayscale_end_time"), false, this);

        mContext.getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor("grayscale_24hour_mode"), false, this);
    }

    @Override
    public void onChange(boolean selfChange) {
        updateGrayscaleMode();
    }

    public void updateGrayscaleMode() {
        boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
            "scheduled_grayscale_enabled", 0) == 1;

        boolean mode24h = Settings.Secure.getInt(mContext.getContentResolver(),
            "grayscale_24hour_mode", 0) == 1;

        if (!enabled) {
            setGrayscale(false);
            return;
        }

        if (mode24h) {
            setGrayscale(true);
            return;
        }

        // Get start/end time from settings
        String startStr = Settings.Secure.getString(mContext.getContentResolver(),
            "scheduled_grayscale_start_time");
        String endStr = Settings.Secure.getString(mContext.getContentResolver(),
            "scheduled_grayscale_end_time");

        if (startStr == null || endStr == null) {
            setGrayscale(false);
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date now = new Date();
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);

            // Adjust today's date to times for comparison
            Date nowTime = sdf.parse(sdf.format(now));

            boolean inRange;
            if (start.before(end)) {
                // normal range same day
                inRange = nowTime.equals(start) || nowTime.equals(end) ||
                          (nowTime.after(start) && nowTime.before(end));
            } else {
                // overnight range (e.g., 21:00 to 07:00)
                inRange = nowTime.after(start) || nowTime.before(end);
            }

            setGrayscale(inRange);

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse grayscale schedule times", e);
            setGrayscale(false);
        }
    }

    private void setGrayscale(boolean enable) {
        if (enable) {
            mColorDisplayManager.setSaturationLevel(0f); // grayscale ON
        } else {
            mColorDisplayManager.setSaturationLevel(1f); // grayscale OFF
        }
    }
}
