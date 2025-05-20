package com.android.systemui.power;

import android.content.Context;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * IdleBatterySaver automatically enables Battery Saver mode
 * after 1 hour of screen-off idle time (if not charging),
 * and disables it when the user turns the screen on or unlocks.
 *
 * Doesn't do so if the user is in a call or has media playing.
 *
 * Notifies user by toast after the device is picked up.
 */

public class IdleBatterySaver {
    private static final String TAG = "IdleBatterySaver";
    private static final int DEFAULT_IDLE_TIMEOUT_MS = 60 * 60 * 1000; // 1 hour

    private final Context mContext;
    private final PowerManager mPowerManager;
    private final BatteryManager mBatteryManager;
    private final Handler mHandler;
    private final AudioManager mAudioManager;
    private final TelephonyManager mTelephonyManager;

    private Runnable mIdleRunnable;
    private boolean mBatterySaverAutoEnabled = false;

    public IdleBatterySaver(Context context) {
        mContext = context;
        mPowerManager = context.getSystemService(PowerManager.class);
        mBatteryManager = context.getSystemService(BatteryManager.class);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mHandler = new Handler(context.getMainLooper());

        mIdleRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mPowerManager.isPowerSaveMode() && !isCharging() && !isMediaPlaying() && !isInCall()) {
                    mPowerManager.setPowerSaveMode(true);
                    mBatterySaverAutoEnabled = true;
                    showToast("Battery saver enabled after 1 hour idle.");
                    Log.d(TAG, "Battery saver auto-enabled after idle.");
                }
            }
        };
    }

    public void onUserActivity() {
        if (mBatterySaverAutoEnabled) {
            mPowerManager.setPowerSaveMode(false);
            mBatterySaverAutoEnabled = false;
            int batteryPercent = getBatteryLevel();
            showToast("Turning off battery saver after idle. Drained " + batteryPercent + "%");
            Log.d(TAG, "Battery saver auto-disabled on user activity.");
        }
        resetIdleTimer();
    }

    public void start() {
        resetIdleTimer();
    }

    public void stop() {
        mHandler.removeCallbacks(mIdleRunnable);
    }

    private void resetIdleTimer() {
        mHandler.removeCallbacks(mIdleRunnable);
        int idleTimeout = getIdleTimeout();
        mHandler.postDelayed(mIdleRunnable, idleTimeout);
        Log.d(TAG, "Idle timer reset to " + idleTimeout + " ms");
    }

    private int getIdleTimeout() {
        int timeout = Settings.Global.getInt(mContext.getContentResolver(),
                "idle_battery_saver_timeout_ms", DEFAULT_IDLE_TIMEOUT_MS);
        return timeout > 0 ? timeout : DEFAULT_IDLE_TIMEOUT_MS;
    }

    private boolean isCharging() {
        return mBatteryManager.isCharging();
    }

    private int getBatteryLevel() {
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    private boolean isMediaPlaying() {
        // Returns true if any audio stream is active
        return mAudioManager.isMusicActive();
    }

    private boolean isInCall() {
        // Returns true if phone call is active (off-hook or ringing)
        int callState = mTelephonyManager.getCallState();
        return callState == TelephonyManager.CALL_STATE_OFFHOOK || callState == TelephonyManager.CALL_STATE_RINGING;
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
