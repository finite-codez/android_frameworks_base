package com.android.systemui.echotap;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

public class EchoTapService extends Service {
    private static final String TAG = "EchoTapService";
    private MediaRecorder recorder;

    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new MediaRecorder();
        // Setup MediaRecorder config for mic audio
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    private void startRecording() {
        // Prepare and start MediaRecorder
        Log.d(TAG, "Audio recording started");
    }

    private void stopRecording() {
        // Stop and release MediaRecorder
        Log.d(TAG, "Audio recording stopped");
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding
    }
}
