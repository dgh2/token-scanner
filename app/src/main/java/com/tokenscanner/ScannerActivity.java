package com.tokenscanner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class ScannerActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {
    private static final String TAG = ScannerActivity.class.getName();

    private JavaCameraView scannerView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    scannerView.enableView();
                    break;
                default:
                    handleFatalError("OpenCV library failed to load!");
                    break;
            }
        }
    };

    private enum DISPLAY_TYPE {
        INPUT,
        GRAYSCALE,
        GAUSSIAN,
        CONTOURS,
        CONTOURS_WITH_BOUNDS,
        RESULT;
        public DISPLAY_TYPE next() {
            return values()[((ordinal() + 1) % values().length)];
        }
    }

    private DISPLAY_TYPE display = DISPLAY_TYPE.RESULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scannerView = (JavaCameraView) findViewById(R.id.scanner_view);
        scannerView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        scannerView.enableFpsMeter();
        scannerView.setCvCameraViewListener(this);
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display = display.next();
                Toast.makeText(getApplicationContext(), display.name() + " view", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scannerView != null)
            scannerView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerView != null)
            scannerView.disableView();
    }

    private void handleFatalError(String errorMessage) {
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Application closed due to fatal error: " + errorMessage);
        finish();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(final Mat frame) {
        long startMillis = System.currentTimeMillis();
        try {
            return processFrame(frame);
        } finally {
            double time = (System.currentTimeMillis() - startMillis) / 1000.0;
            Log.d(TAG, "Image processing took " + time + " seconds!");
        }
    }

    private Mat processFrame(final Mat frame) {
//        if (outputFrame.size().width > 720) {
//            Imgproc.resize(outputFrame, outputFrame,
//                    new Size(720 * outputFrame.size().width / inputFrame.size().height, 720));
//        }
        Mat grayscale = new Mat();
        Mat blurred = new Mat();
        Mat contourMap = new Mat();
        Mat result = new Mat();
        LinkedList<MatOfPoint> contours = new LinkedList<>();

        Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
        Imgproc.Canny(blurred, contourMap, 50, 150);
        Imgproc.findContours(contourMap, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        switch (display) {
            case INPUT:
                frame.copyTo(result);
                break;
            case GRAYSCALE:
                grayscale.copyTo(result);
                break;
            case GAUSSIAN:
                blurred.copyTo(result);
                break;
            case CONTOURS_WITH_BOUNDS:
                contourMap.copyTo(result);
                fitEllipsisToContours(result, new Scalar(255, 0, 0), contours);
                break;
            case CONTOURS:
                contourMap.copyTo(result);
                break;
            case RESULT:
                frame.copyTo(result);
                fitEllipsisToContours(result, new Scalar(0, 255, 0), contours);
                break;
            default:
                Log.w(TAG, "Unrecognized display type detected: " + display.name());
                break;
        }
        return result;
    }

    private void fitEllipsisToContours(final Mat frame, Scalar color, final LinkedList<MatOfPoint> contours) {
        MatOfPoint2f temp;
        for (int i = 0; i < contours.size(); i++) {
            temp = new MatOfPoint2f();
            temp.fromList(contours.get(i).toList());
            Rect bounds = Imgproc.boundingRect(contours.get(i));
            Imgproc.rectangle(frame, bounds.tl(), bounds.br(), color, 4);
        }
    }
}
