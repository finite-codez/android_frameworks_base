package com.android.systemui.echotap;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.provider.Settings;
import android.database.ContentObserver;
import android.net.Uri;

/**
 * EchoTapController listens for triple taps when the screen is face down
 * and toggles a recording service. Its activation is controlled by
 * Settings.Secure key: "echotap_enabled".
 */
public class EchoTapController implements SensorEventListener {

    private static final String TAG = "EchoTapController";

    private final Context mContext;
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Handler handler = new Handler();

    private boolean isScreenDown = false;
    private int tapCount = 0;
    private boolean isRecording = false;

    private final Runnable tapResetRunnable = () -> tapCount = 0;

    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateEnabledState();
        }
    };

    public EchoTapController(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register observer for settings change
        Uri settingUri = Settings.Secure.getUriFor("echotap_enabled");
        mContext.getContentResolver().registerContentObserver(settingUri, false, mSettingsObserver);

        // Initial check
        updateEnabledState();
    }

    public void start() {
        Log.d(TAG, "EchoTap started");
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        Log.d(TAG, "EchoTap stopped");
        mSensorManager.unregisterListener(this);
    }

    public void destroy() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float z = event.values[2];

        if (z < -9 && !isScreenDown) {
            isScreenDown = true;
            tapCount = 0;
        } else if (z > 0 && isScreenDown) {
            isScreenDown = false;
            tapCount = 0;
        }
    }

    public void onTapDetected() {
        if (!isScreenDown) return;

        tapCount++;
        handler.removeCallbacks(tapResetRunnable);
        handler.postDelayed(tapResetRunnable, 1500);

        if (tapCount == 3) {
            toggleRecording();
            tapCount = 0;
        }
    }

    private void toggleRecording() {
        Intent intent = new Intent(mContext, EchoTapService.class);
        if (isRecording) {
            mContext.stopService(intent);
            isRecording = false;
            Log.d(TAG, "Stopped recording");
        } else {
            mContext.startService(intent);
            isRecording = true;
            Log.d(TAG, "Started recording");
        }
    }

    private void updateEnabledState() {
        boolean enabled = Settings.Secure.getInt(
                mContext.getContentResolver(),
                "echotap_enabled", 0) == 1;

        if (enabled) {
            start();
        } else {
            stop();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
