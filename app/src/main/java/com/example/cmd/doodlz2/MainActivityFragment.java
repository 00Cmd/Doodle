package com.example.cmd.doodlz2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    @Override
    public void onPause() {
        super.onPause();
        disableAccelerationListening();
    }

    private void enableAccelerometerListening() {
        SensorManager sensorManager = (SensorManager) getActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
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
                        fragment.show(getFragmentManager(),"erase dialog");
                }
            };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doodle_fragment_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialog = new ColorDialogFragment();
                colorDialog.show(getFragmentManager(), "color dialog");
                return true; // consume the menu event
            case R.id.line_width:
                LineWidthDialogFragment widthDialog =
                        new LineWidthDialogFragment();
                widthDialog.show(getFragmentManager(), "line width dialog");
                return true; // consume the menu event
            case R.id.delete_drawing:
                confirmErase(); // confirm before erasing image
                return true; // consume the menu event
            case R.id.save:
                saveImage(); // check permission and save current image
                return true; // consume the menu event
            case R.id.print:
                doodleView.printImage(); // print the current images
                return true; // consume the menu event
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SAVE_IMAGE_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mDoodleView.saveImage();
                return;
        }
    }

    private void saveImage() {
        if(getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            if(shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage("We need permission to save the image.");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                SAVE_IMAGE_REQUEST);
                    }
                });
                dialog.show();
            } else {
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        SAVE_IMAGE_REQUEST);
            }
        } else {
            mDoodleView.saveImage();
        }
    }

    private void confirmErase() {

    }

    public DoodleView getDoodleView() {
        return mDoodleView;
    }

    public void setDialogOnScreen(boolean visible) {
        dialogOnScreen = visible;
    }
}
