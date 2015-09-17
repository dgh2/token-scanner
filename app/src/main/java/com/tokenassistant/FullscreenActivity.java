package com.tokenassistant;

import android.app.Activity;
import android.os.Bundle;

public class FullscreenActivity extends Activity {
    ScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        scannerView = (ScannerView) findViewById(R.id.content);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
}
