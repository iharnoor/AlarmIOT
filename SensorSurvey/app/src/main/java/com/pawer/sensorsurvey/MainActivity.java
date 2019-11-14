package com.pawer.sensorsurvey;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // System sensor manager instance.
    private SensorManager mSensorManager;

    // Proximity and light sensors, as retrieved from the sensor manager.
    private Sensor mSensorGravity;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorGyroscope;

    // TextViews to display current sensor values.
    private TextView mTextSensorGravity;
    private TextView mTextSensorAccelerometer;
    private TextView mTextSensorGyroscope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all view variables.
        mTextSensorGravity = findViewById(R.id.label_gravity);
        mTextSensorAccelerometer = findViewById(R.id.label_accelerometer);
        mTextSensorGyroscope = findViewById(R.id.label_gyroscope);

        // Get an instance of the sensor manager.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Get light and proximity sensors from the sensor manager.
        // The getDefaultSensor() method returns null if the sensor
        // is not available on the device.
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Get the error message from string resources.
        String sensor_error = getResources().getString(R.string.error_no_sensor);

        // If either mSensorAccelerometer or mSensorGravity are null, those sensors
        // are not available in the device.  Set the text to the error message
        if (mSensorAccelerometer == null) {
            mTextSensorGravity.setText(sensor_error);
        }
        if (mSensorGravity == null) {
            mTextSensorAccelerometer.setText(sensor_error);
        }
        if (mSensorGyroscope == null) {
            mTextSensorGyroscope.setText(sensor_error);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onPause().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL)
        if (mSensorGravity != null) {
            mSensorManager.registerListener(this, mSensorGravity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorGyroscope != null) {
            mSensorManager.registerListener(this, mSensorGyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is paused.
        mSensorManager.unregisterListener(this);
    }

    /**
     * write code to save to csv here
     * find a way to push the old and new value of the sensors to the csv
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // The sensor type (as defined in the Sensor class).
        int sensorType = sensorEvent.sensor.getType();

        // The new data value of the sensor.  Both the light and proximity
        // sensors report one value at a time, which is always the first
        // element in the values array.
        float currentValue = sensorEvent.values[0];

        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_GRAVITY:
                // Set the light sensor text view to the light sensor string
                // from the resources, with the placeholder filled in.
                mTextSensorGravity.setText(getResources().getString(
                        R.string.label_gravity, currentValue));
                break;
            case Sensor.TYPE_ACCELEROMETER:
                // Set the proximity sensor text view to the light sensor
                // string from the resources, with the placeholder filled in.
                mTextSensorAccelerometer.setText(getResources().getString(
                        R.string.label_accelerometer, currentValue));
                break;
            case Sensor.TYPE_GYROSCOPE:
                // Set the proximity sensor text view to the light sensor
                // string from the resources, with the placeholder filled in.
                mTextSensorGyroscope.setText(getResources().getString(
                        R.string.label_gyroscope, currentValue));
                break;
            default:
                // do nothing
        }
    }

    /**
     * Abstract method in SensorEventListener.  It must be implemented, but is
     * unused in this app.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}