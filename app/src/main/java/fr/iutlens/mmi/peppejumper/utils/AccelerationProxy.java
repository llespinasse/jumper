package fr.iutlens.mmi.peppejumper.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by dubois on 23/04/15.
 */
public class AccelerationProxy implements SensorEventListener {

    private final AccelerationListener mListener;

    public interface AccelerationListener {
        void onAcceleration(float accelDelta, double dt);
    }

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];


    private float[] mPreviousAccelerometer = new float[3];
    private float[] gravity = new float[3];

    private float vertical_dist, vertical_speed;


    long lastTimestamp,previousTimestamp;


    public AccelerationProxy(Context context, AccelerationListener listener)
    {
        mListener = listener;

        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



    }

    public void resume() {
        lastTimestamp = -1;
        previousTimestamp = -1;
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);

    }

    public void pause() {
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private double length(float[] array){
        double sum =0;
        for(float f: array){
            sum += f*f;
        }
        return Math.sqrt(sum);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
//            SensorManager.getOrientation(mR, mOrientation);
//            mListener.onOrientationChanged(mOrientation,event.timestamp/1000000l);


            if (event.sensor == mAccelerometer) {

                previousTimestamp = lastTimestamp;
                lastTimestamp = event.timestamp;

                System.arraycopy(mLastAccelerometer, 0, mPreviousAccelerometer, 0, 3);


                gravity[0] = mR[0]*event.values[0] + mR[3]*event.values[1] + mR[6]*event.values[2];
                gravity[1] = mR[1]*event.values[0] + mR[4]*event.values[1] + mR[7]*event.values[2];
                gravity[2] = mR[2]*event.values[0] + mR[5]*event.values[1] + mR[8]*event.values[2];

                mLastAccelerometer[0] = gravity[0] ;
                mLastAccelerometer[1] = gravity[1] ;
                mLastAccelerometer[2] = gravity[2] - SensorManager.GRAVITY_EARTH;

                Log.d("Accel", Arrays.toString(mLastAccelerometer));
                long dt = (lastTimestamp - previousTimestamp) / 1000000L;

                if (previousTimestamp != -1 && dt <= 100) {

                    float da = (float) (mLastAccelerometer[2]);
                    //Log.d("Accel",""+da);
                    mListener.onAcceleration(da, dt);


                } else {
                    vertical_dist = 0;
                    vertical_speed = 0;
                }

            }
        }

    }
}
