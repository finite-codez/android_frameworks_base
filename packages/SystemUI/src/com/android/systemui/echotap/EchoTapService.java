package com.android.systemui.echotap;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;

public class EchoTapService extends Service {
    private static final String TAG = "EchoTapService";
    private MediaRecorder recorder;
    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new MediaRecorder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
        return START_NOT_STICKY;
    }

    private void startRecording() {
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/echotap_recording.mp4");
            recorder.prepare();
            recorder.start();
            isRecording = true;
            Log.d(TAG, "Recording started");
        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
            stopSelf();
        }
    }

    private void stopRecording() {
        try {
            recorder.stop();
            recorder.reset();
            isRecording = false;
            Log.d(TAG, "Recording stopped");
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to stop recording", e);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
