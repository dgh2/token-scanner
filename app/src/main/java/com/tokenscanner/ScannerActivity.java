package com.tokenscanner;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ScannerActivity extends Activity {
    private static final String TAG = ScannerActivity.class.getName();

    private ScannerView scannerView;

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
        scannerView.startCameraPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCameraPreview();
    }

    private void handleFatalError(String errorMessage) {
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Application closed due to fatal error: " + errorMessage);
        finish();
    }
}
