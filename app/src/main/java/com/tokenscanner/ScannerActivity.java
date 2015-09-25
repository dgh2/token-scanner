package com.tokenscanner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class ScannerActivity extends Activity {
    private static final String TAG = ScannerActivity.class.getName();

    private ScannerView scannerView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    scannerView.startCameraPreview();
                    break;
                default:
                    handleFatalError("OpenCV library failed to load!");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scannerView = (ScannerView) findViewById(R.id.scanner_view);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!scannerView.hasCamera()) {
            handleFatalError("Camera access failed!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCameraPreview();
    }

    private void handleFatalError(String errorMessage) {
        handleFatalError(this, getApplicationContext(), errorMessage);
    }

    private static void handleFatalError(Activity activity, Context applicationContext, String errorMessage) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Application closed due to fatal error: " + errorMessage);
        activity.finish();
    }
}
