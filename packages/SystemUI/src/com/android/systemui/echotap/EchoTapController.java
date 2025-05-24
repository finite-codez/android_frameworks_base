package com.android.systemui.echotap;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class EchoTapController implements SensorEventListener {
    private static final String TAG = "EchoTapController";
    private final Context mContext;
    private final SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean isScreenDown = false;
    private int tapCount = 0;
    private Handler handler = new Handler();
    private Runnable tapResetRunnable;

    private boolean isRecording = false;

    public EchoTapController(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        tapResetRunnable = () -> tapCount = 0;
    }

    public void start() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float z = event.values[2];
        // Detect if phone is face down (z < -9) approx
        if (z < -9 && !isScreenDown) {
            isScreenDown = true;
            tapCount = 0; // reset taps on flip
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
