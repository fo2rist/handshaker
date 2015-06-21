package com.weezlabs.research.handshaker;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.IBinder;

public class SensorBackgroundService extends Service implements AccelerometerHelper.OnAccelerometerListener {

    private static final String TAG = "LOL";

    @Override
    public void onCreate() {
        super.onCreate();

        AccelerometerHelper.getInstance().init(getApplicationContext());
        AccelerometerHelper.getInstance().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        AccelerometerHelper.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onAccelerometerValueChanged(SensorEvent event) {
    }

    @Override
    public void onShakeDetected() {

        if(!MainActivity.isForeground(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("NO_SPLASH", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        sendToDevice();

//        // wake screen here
//        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(getApplicationContext().POWER_SERVICE);
//        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), SensorBackgroundService.class.getSimpleName());
//        wakeLock.acquire();
//
//        //and release again
//        wakeLock.release();

        // optional to release screen lock
        //KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(getApplicationContext().KEYGUARD_SERVICE);
        //KeyguardManager.KeyguardLock keyguardLock =  keyguardManager.newKeyguardLock(TAG);
        //keyguardLock.disableKeyguard();
    }

    private void sendToDevice() {
    }

}
