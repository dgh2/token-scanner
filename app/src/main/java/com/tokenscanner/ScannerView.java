package com.tokenscanner;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ScannerView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = ScannerView.class.getName();

    private Camera mCamera;

    public ScannerView(Context context) {
        super(context);
        init();
    }

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCamera = getCameraInstance();

        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        getHolder().addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        refreshCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    public boolean hasCamera() {
        return mCamera != null;
    }

    public void startCameraPreview() {
        if (mCamera == null) {
            mCamera = getCameraInstance();
            if (mCamera == null) {
                return;
            }
        }

        try {
            mCamera.setPreviewDisplay(getHolder());
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start camera preview!", e);
        }
    }

//    public void refreshCameraPreview() {
//        if (mCamera == null || getHolder().getSurface() == null) {
//            return;
//        }
//
//        // stop preview before making changes
//        stopCameraPreview();
//
//        // set preview size and make any resize, rotate or reformatting changes here
//        updatePreview();
//
//        try {
//            mCamera.setPreviewDisplay(getHolder());
//        } catch (Exception e) {
//            Log.d(TAG, "Failed to set the preview display while refreshing!", e);
//        }
//
//        // start preview with new settings
//        startCameraPreview();
//    }

    public void stopCameraPreview() {
        if (mCamera == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "Tried to stop a non-existent camera preview!", e);
        }
    }

    public void releaseCamera() {
        if (mCamera == null) {
            return;
        }

        stopCameraPreview();
        try {
            mCamera.release();
        } catch (Exception e) {
            Log.d(TAG, "Failed to release camera!", e);
        }
        mCamera = null;
    }

//    private void updatePreview() {
//        mCamera.setDisplayOrientation(90);
//    }

    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance() {
        try {
            return Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Camera was not available!", e);
        }
        return null; // camera is unavailable
    }
}
