package com.pawer.alarmiot;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.commons.collections4.queue.CircularFifoQueue;


public class DetectionClient {
    private static final String TAG = "ActionClassification";
    private static final String MODEL_PATH = "conv.tflite";
    private static final String TEST_PATH = "test.csv";
    private final Context context;
    private Interpreter tflite;
    private float [][][] test_data = new float[1][300][6];

    public void load() {
        loadModel();
    }

    public DetectionClient(Context context) {
        this.context = context;
    }

    private synchronized void loadModel() {
        try {
            ByteBuffer buffer = loadModelFile(this.context.getAssets());
            tflite = new Interpreter(buffer);
            Log.v(TAG, "TFLite model loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    // public synchronized List<Result> classify(String text) {
    public synchronized float[][] classify (CircularFifoQueue<String> sensor_data) {
        /* Pre-prosessing.
        try{
            loadSensorFile(this.context.getAssets());
        }catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }

        Log.v(TAG, "Classifying text with TF Lite...");
         */
        float[][][] input = new float[1][300][6];
        for (int i=0; i<1; i++ ){
            for (int j=0; j<300; j++) {
                String[] s = sensor_data.get(50*i+j).split(",");
                for (int k = 0; k<6; k++){
                    input[i][j][k] = Float.valueOf(s[k]);
                }
            }
        }
        //float[][] output = new float[3][1];
        float[][] output = new float[1][1];
        tflite.run(input, output);
        return output;
    }

    private static MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        try (AssetFileDescriptor fileDescriptor = assetManager.openFd(MODEL_PATH);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declaredLength = fileDescriptor.getDeclaredLength();
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private void loadSensorFile(AssetManager assetManager) throws IOException {
        try (InputStream ins = assetManager.open(TEST_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            // Each line in the label file is a label.
            int lines = 0;
            while (reader.ready() && lines < 300) {
                String[] s = reader.readLine().split(",");
                Log.v(TAG, s[0]);
                for (int i = 0; i<6; i++)
                    test_data[0][lines][i] = Float.valueOf(s[i]);
                lines += 1;
            }
        }
    }

    Interpreter getTflite() {
        return this.tflite;
    }
}
