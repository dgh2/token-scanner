package com.tokenscanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

public class ScannerView extends JavaCameraView {
    private boolean lightOn = false;
    public ScannerView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean lightAvailable() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void toggleLight() {
        Camera.Parameters params;
        if (!lightAvailable() || ((params = mCamera.getParameters()) == null)) {
            return;
        }
        if (lightOn) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            lightOn = false;
        } else {
            lightOn = true;
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
        }
    }
}
