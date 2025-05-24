package com.android.systemui.echotap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.android.systemui.SystemUI;

public class EchoTapController extends SystemUI implements SensorEventListener {
    private static final String TAG = "EchoTapController";
    private static final int TAP_THRESHOLD = 3;
    private static final long TAP_WINDOW_MS = 1500;

    private final SensorManager sensorManager;
    private final PowerManager powerManager;
    private final Sensor accelerometer;
    private final Sensor proximitySensor;

    private int tapCount = 0;
    private long firstTapTime = 0;
    private boolean isFaceDown = false;
    private final Handler handler;

    public EchoTapController(Context context) {
        super(context);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            isFaceDown = event.values[0] < proximitySensor.getMaximumRange();
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isFaceDown && !powerManager.isInteractive()) {
            float acceleration = (float) Math.sqrt(
                event.values[0] * event.values[0] +
                event.values[1] * event.values[1] +
                event.values[2] * event.values[2]);

            if (acceleration > 15) { // rough tap threshold
                long now = SystemClock.elapsedRealtime();
                if (firstTapTime == 0 || (now - firstTapTime > TAP_WINDOW_MS)) {
                    tapCount = 1;
                    firstTapTime = now;
                } else {
                    tapCount++;
                    if (tapCount == TAP_THRESHOLD) {
                        Log.d(TAG, "Triple tap detected while face-down and screen off");
                        triggerRecording();
                        tapCount = 0;
                        firstTapTime = 0;
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void triggerRecording() {
        EchoTapService.startRecording(mContext);
    }
}
