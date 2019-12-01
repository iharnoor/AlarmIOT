package com.pawer.alarmiot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;

    // Proximity and light sensors, as retrieved from the sensor manager.
    private Sensor mSensorGravity;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorGyroscope;

    // TextViews to display current sensor values.
    private TextView mTextSensorGravity;
    private TextView mTextSensorAccelerometer;
    private TextView mTextSensorGyroscope;
    private TextView resultTextView;

    Handler handler;
    Runnable runnable;

    float[] accData;
    float[] gyroData;
    private CircularFifoQueue<String> sensor_data = new CircularFifoQueue(500);
    private DetectionClient client;
    private IndicatorView indicatorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        indicatorView = findViewById(R.id.myIndicator);

        // Change Color Here
        indicatorView.setmColor(ContextCompat.getColor(this, R.color.colorPrimary));


        Button btnCall = findViewById(R.id.btnCall);
        Button btnAlarm = findViewById(R.id.btnAlarm);

        btnCall.setVisibility(View.GONE);
        btnAlarm.setVisibility(View.GONE);

        btnCall.setOnClickListener(
                v -> {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:1231231231"));
                    startActivity(callIntent);
                }
        );


        btnAlarm.setOnClickListener(
                v -> {
                    final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.xyz);
                    mediaPlayer.start();
                }
        );

        client = new DetectionClient(getApplicationContext());
        handler = new Handler();
        resultTextView = findViewById(R.id.result_text_view);

        Button classifyButton = findViewById(R.id.classifyButton);
        classifyButton.setEnabled(false);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        classifyButton.setEnabled(true);
                        classifyButton.setText("Start");
                    }
                });
            }
        }).start();

        classifyButton.setOnClickListener(
                v -> classify()
        );

        // sensor data
        accData = new float[3];
        gyroData = new float[3];


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) //this might be wrong needed context
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            return;
        }

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

        //Handler for graph plotting on background thread

        handler = new Handler();

        //Runnable for background plotting
        runnable = new Runnable() {
            @Override
            public void run() {
                // specify time here
                handler.postDelayed(this, 10);
                writeToCSV();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onPause().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL)
        if (mSensorGravity != null) {
            mSensorManager.registerListener(this, mSensorGravity,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mSensorGyroscope != null) {
            mSensorManager.registerListener(this, mSensorGyroscope,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is paused.
        mSensorManager.unregisterListener(this);
    }

    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Perform long-running task here
                // (like audio buffering).
                // you may want to update some progress
                // bar every second, so use handler:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // make operation on UI - on example
                        // on progress bar.
                        client.load();
                    }
                });
            }
        }).start();
    }

    private void classify() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(100);
                    handler.post(() -> {
                        float[][] results = client.classify(sensor_data);
                        resultTextView.setText(String.valueOf(results[0][0]));
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
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
        float x_Value = sensorEvent.values[0];

        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_ACCELEROMETER:
                // Set the proximity sensor text view to the light sensor
                // string from the resources, with the placeholder filled in.
                accData = sensorEvent.values;
                mTextSensorAccelerometer.setText(getResources().getString(
                        R.string.label_accelerometer, Arrays.toString(accData)));
                break;
            case Sensor.TYPE_GYROSCOPE:
                // Set the proximity sensor text view to the light sensor
                // string from the resources, with the placeholder filled in.
                gyroData = sensorEvent.values;
                mTextSensorGyroscope.setText(getResources().getString(
                        R.string.label_gyroscope, Arrays.toString(gyroData)));
                break;
            default:
        }
    }

    public void writeToCSV() {
        final String content = accData[0] + "," + accData[1] + "," + accData[2] + "," + gyroData[0] + "," + gyroData[1] + "," + gyroData[2];
        sensor_data.offer(content);
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS) + "/csv/";
        File newdir = new File(dir);
        if (!newdir.exists()) {
            newdir.mkdirs();
        }
        try {
            File file = new File(dir + "a" + ".csv");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter printWriter = new PrintWriter(bw);
            printWriter.println(content);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
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

