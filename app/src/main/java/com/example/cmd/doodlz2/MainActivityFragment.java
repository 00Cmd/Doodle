package com.example.cmd.doodlz2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG = "MainActivityFragment";
    private DoodleView mDoodleView;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private boolean dialogOnScreen = false;


    private static final int ACCELERATION_TRESHOLD = 100000;
    private static final int SAVE_IMAGE_REQUEST = 1;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mDoodleView = (DoodleView)v.findViewById(R.id.doodleView);
        acceleration = 0.0f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening();
    }

    private void enableAccelerometerListening() {
        SensorManager sensorManager = (SensorManager) getActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        disableAccelerationListening();
    }

    private void disableAccelerationListening() {
        SensorManager sensorManager = (SensorManager)getActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    private final SensorEventListener sensorEventListener =
            new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if(!dialogOnScreen) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        lastAcceleration = currentAcceleration;

                        currentAcceleration = x*x+y*y+z*z;

                        acceleration = currentAcceleration * (currentAcceleration - lastAcceleration);
                        if(acceleration > ACCELERATION_TRESHOLD) {
                            confirmErase();
                        }

                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
                    private void confirmErase() {
                        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
                        fragment.show(getFragmentManager(),TAG);
                        //TODO: Error maybe because the fragment has only a empty constructor
                }
            };
}
