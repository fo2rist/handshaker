package com.weezlabs.research.handshaker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;

import com.google.common.collect.MapMaker;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class AccelerometerHelper implements SensorEventListener {

    protected interface OnAccelerometerListener {
        public void onAccelerometerValueChanged(SensorEvent event);

        public void onShakeDetected();
    }

    protected static synchronized AccelerometerHelper getInstance() {
        if (mInstance == null) {
            mInstance = new AccelerometerHelper();
        }
        return mInstance;
    }

    private static final float SHAKE_THRESHOLD = 1.1f;
    private static final int SHAKE_WAIT_TIME_MS = 180; //250

    private static AccelerometerHelper mInstance;

    private final Set<OnAccelerometerListener> mOnAccelerometerListeners;
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private long mShakeTimeLast = 0;
    private long mShakeTime = 0;
    private int shakeTimes = 0;

    private AccelerometerHelper() {
        // TODO Get gid of google ConcurrentMap. Investigate event based libraries.
        ConcurrentMap<OnAccelerometerListener, Boolean> concurrentMap = new MapMaker().softKeys().weakValues().makeMap();
        mOnAccelerometerListeners = Collections.newSetFromMap(concurrentMap);
    }

    protected void init(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // region [Register/Unregister for accelerometer events]
    protected synchronized void register(OnAccelerometerListener listener) {
        if (mOnAccelerometerListeners.isEmpty()) {
            // if was empty – register for system events before adding the listener
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        mOnAccelerometerListeners.add(listener);
    }

    protected synchronized void unregister(OnAccelerometerListener listener) {
        mOnAccelerometerListeners.remove(listener);
        if (mOnAccelerometerListeners.isEmpty()) {
            // if became empty – unregister from system events
            mSensorManager.unregisterListener(this);
        }
    }
    // endregion

    // region [overrides android.hardware.SensorEventListener.SensorEventListener]
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 0. Do not notify if event is unreliable
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        boolean shaken = checkIfShake(event);

        // 1. Notify otherwise
        // 2. Even more, if wearable device was shaken – notify about the shake as well
        for (OnAccelerometerListener listener : mOnAccelerometerListeners) {
            listener.onAccelerometerValueChanged(event);
            if (shaken) {
                listener.onShakeDetected();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    //endregion

    // References:
    //  - http://jasonmcreynolds.com/?p=388
    //  - http://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
    private boolean checkIfShake(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;
            // gForce will be close to 1 when there is no movement
            float gForce = FloatMath.sqrt(gX * gX + gY * gY + gZ * gZ);
            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            boolean wasShaken = gForce > SHAKE_THRESHOLD;
            if (!wasShaken) {
                return false;
            }

            /////
            boolean proceedFurther = true;
            if (!MainActivity.isForeground(mContext)) {
                if (now - mShakeTimeLast > 1000 * 3) {
                    mShakeTimeLast = now;
                    shakeTimes = 0;
                    return false;
                }
                if (shakeTimes > 4) {
                    shakeTimes = 0;
                    proceedFurther = true;
                } else {
                    shakeTimes++;
                    proceedFurther = false;
                }
            }
            if (!proceedFurther) {
                return false;
            }

            //////
            shakeTimes = 0;
            return true;
        }

        return false;
    }

}
