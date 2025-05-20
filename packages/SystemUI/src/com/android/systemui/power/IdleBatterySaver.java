package com.android.systemui.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

/**
 * IdleBatterySaver automatically enables Battery Saver mode
 * after 1 hour of screen-off idle time (if not charging),
 * and disables it when the user turns the screen on or unlocks.
 */
public class IdleBatterySaver {
    private static final String TAG = "IdleBatterySaver";

    private static final long IDLE_TIMEOUT_MS = 60 * 60 * 1000; // 1 hour

    private final Context mContext;
    private final PowerManager mPowerManager;
    private final Handler mHandler;

    private boolean mBatterySaverEnabledByIdle = false;
    private boolean mIsCharging = false;

    private final Runnable mEnableBatterySaverRunnable = () -> {
        if (!mIsCharging && !mPowerManager.isPowerSaveMode()) {
            Log.i(TAG, "Idle timeout reached — enabling Battery Saver");
            setBatterySaver(true);
            mBatterySaverEnabledByIdle = true;
        } else {
            Log.i(TAG, "Idle timeout but charging or Battery Saver already on, skipping");
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case Intent.ACTION_SCREEN_OFF:
                    Log.i(TAG, "Screen OFF detected — starting idle timer");
                    mHandler.postDelayed(mEnableBatterySaverRunnable, IDLE_TIMEOUT_MS);
                    break;

                case Intent.ACTION_SCREEN_ON:
                case Intent.ACTION_USER_PRESENT:
                    Log.i(TAG, "User active — cancelling idle timer");
                    mHandler.removeCallbacks(mEnableBatterySaverRunnable);
                    if (mBatterySaverEnabledByIdle) {
                        Log.i(TAG, "Disabling Battery Saver due to user activity");
                        setBatterySaver(false);
                        mBatterySaverEnabledByIdle = false;
                    }
                    break;

                case Intent.ACTION_BATTERY_CHANGED:
                    int status = intent.getIntExtra("status", -1);
                    mIsCharging = (status == PowerManager.BATTERY_STATUS_CHARGING
                            || status == PowerManager.BATTERY_STATUS_FULL);
                    Log.i(TAG, "Battery status changed, isCharging=" + mIsCharging);

                    if (mIsCharging && mBatterySaverEnabledByIdle) {
                        Log.i(TAG, "Charging started — disabling Battery Saver");
                        setBatterySaver(false);
                        mBatterySaverEnabledByIdle = false;
                        mHandler.removeCallbacks(mEnableBatterySaverRunnable);
                    }
                    break;

                default:
                    Log.w(TAG, "Unexpected intent: " + action);
                    break;
            }
        }
    };

    public IdleBatterySaver(Context context) {
        mContext = context;
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        Log.i(TAG, "IdleBatterySaver started");
    }

    public void stop() {
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver already unregistered");
        }
        mHandler.removeCallbacks(mEnableBatterySaverRunnable);

        if (mBatterySaverEnabledByIdle) {
            setBatterySaver(false);
            mBatterySaverEnabledByIdle = false;
        }
        Log.i(TAG, "IdleBatterySaver stopped");
    }

    private void setBatterySaver(boolean enabled) {
        try {
            mPowerManager.setPowerSaveMode(enabled);
        } catch (Exception e) {
            Log.e(TAG, "Failed to toggle Battery Saver", e);
        }
    }
}
