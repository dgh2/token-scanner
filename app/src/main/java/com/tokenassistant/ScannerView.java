package com.tokenassistant;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ScannerView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera;

    public ScannerView(Context context) {
        super(context);
    }

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        getHolder().addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        openCamera();
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    public void openCamera() {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCameraPreview() {
        if (mCamera == null) {
            return;
        }

        try {
            mCamera.setPreviewDisplay(getHolder());
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshCameraPreview() {
        if (mCamera == null || getHolder().getSurface() == null) {
            return;
        }

        // stop preview before making changes
        stopCameraPreview();

        // set preview size and make any resize, rotate or reformatting changes here
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(getHolder());
        } catch (Exception e) {
            // ignore
            e.printStackTrace();
        }

        // start preview with new settings
        startCameraPreview();
    }

    public void stopCameraPreview() {
        if (mCamera == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        if (mCamera == null) {
            return;
        }

        stopCameraPreview();
        try {
            mCamera.release();
        } catch (Exception e){
            // ignore
            e.printStackTrace();
        }
        mCamera = null;
    }
}
