package com.tokenscanner;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class ScannerActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {
    private static final String TAG = ScannerActivity.class.getName();
    private static final Scalar SCALAR_RED = new Scalar(255, 255, 255);
    private static final Scalar SCALAR_GREEN = new Scalar(0, 255, 0);
//    private static final Scalar SCALAR_BLUE = new Scalar(0, 0, 255);
    private static final double SCALED_DOWN_MAX_IMAGE_WIDTH = 320;
    private static final double SCALED_DOWN_MAX_IMAGE_HEIGHT = 180;

    private ScannerView scannerView;
//    private ScannerAsyncManager scannerAsyncManager = new ScannerAsyncManager();
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

//    public Size getScaledDownSize(Size size) {
//        return getScaledDownSize(size.width, size.height);
//    }
//
//    public Size getScaledDownSize(double width, double height) {
//        //TODO: calculate size dynamically for other camera resolutions
//        double scale = 0.25;
//        double scaledWidth = width * scale;   // 1280 / 4 = 320
//        double scaledHeight = height * scale; // 720  / 4 = 180
//        return new Size(scaledWidth, scaledHeight);
//    }

//    private enum DISPLAY_TYPE {
//        CAMERA_VIEW,
//        GRAYSCALE,
////        INFLATE_ERODE,
////        INFLATE_ERODE_THRESHOLD,
////        INFLATE_ERODE2,
////        INFLATE_ERODE2_THRESHOLD,
////        ERODE_INFLATE,
////        ERODE_INFLATE_THRESHOLD,
////        ERODE_INFLATE2,
////        ERODE_INFLATE2_THRESHOLD,
//        PRE_BLURRED,
//        PRE_BLURRED_THRESHOLD,
//        POST_BLURRED,
//        POST_BLURRED_THRESHOLD,
//        CANNY,
//        CONTOURS,
//        FILTERED_CONTOURS,
//        ELLIPSE_BOXES,
//        RESULT;
//        public DISPLAY_TYPE next() {
//            return values()[((ordinal() + 1) % values().length)];
//        }
//    }
//
//    private DISPLAY_TYPE display = DISPLAY_TYPE.RESULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scannerView = (ScannerView) findViewById(R.id.scanner_view);
        scannerView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        scannerView.enableFpsMeter();
        scannerView.setCvCameraViewListener(this);
        if (scannerView.lightAvailable()) {
            scannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scannerView.toggleLight();
                }
            });
        }
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

    private Mat processFrame(final Mat input) {
        LinkedList<MatOfPoint> contours = new LinkedList<>();
        Mat frame = new Mat();
        input.copyTo(frame);
        Imgproc.resize(frame, frame, getScaledDownSize(frame), 0, 0, Imgproc.INTER_AREA);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(frame, frame, new Size(5, 5), 5, 5);
        Imgproc.Canny(frame, frame, 50, 150);
        Imgproc.resize(frame, frame, input.size(), 0, 0, Imgproc.INTER_AREA);
        Imgproc.findContours(frame, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        filterContours(contours);
        input.copyTo(frame);
        detectEllipticalContours(frame, contours);
        return frame;
    }

    private void filterContours(LinkedList<MatOfPoint> contours) {
        RotatedRect minAreaRect;
        LinkedList<MatOfPoint> ignoredContours = new LinkedList<>();
        for (MatOfPoint contour : contours) {
            minAreaRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            if (!contour.isContinuous() || contour.toList().size() < 5 ||
                    minAreaRect.size.width < .05 * scannerView.getWidth() ||
                    minAreaRect.size.height < .05 * scannerView.getHeight()) {
                ignoredContours.add(contour);
            }
        }
        contours.removeAll(ignoredContours);
    }

    private LinkedList<RotatedRect> detectEllipticalContours(final Mat frame, final LinkedList<MatOfPoint> contours) {
        LinkedList<RotatedRect> ellipseRects = new LinkedList<>();
        for (MatOfPoint contour : contours) {
            RotatedRect ellipseRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            if (contourIsEllipse(contour)) {
                Imgproc.ellipse(frame, ellipseRect.center, ellipseRect.size,
                        ellipseRect.angle, 0, 360, new Scalar(0, 255, 0), 3);
            }/* else {
                Imgproc.ellipse(frame, ellipseRect.center, ellipseRect.size,
                        ellipseRect.angle, 0, 360, new Scalar(255, 0, 0), 1);
            }*/
//            Imgproc.drawContours(frame, contours, contours.indexOf(contour), SCALAR_BLUE, 1);
        }
        return ellipseRects;
    }

    private boolean contourIsEllipse(MatOfPoint contour) {
        MatOfPoint2f contour2F = new MatOfPoint2f(contour.toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour2F, approxCurve, 0.01 * Imgproc.arcLength(contour2F, true), true);
//        double contourArea = Imgproc.minAreaRect(contour2F).size.area();
        double matchValue = Imgproc.matchShapes(contour, approxCurve, Imgproc.CV_CONTOURS_MATCH_I1, 0.0);
        return matchValue > 0 && matchValue < 0.03;/* &&
                (Math.abs(contourArea - Imgproc.minAreaRect(approxCurve).size.area()) < (0.075 * contourArea));*/
    }

    private Size getScaledDownSize(final Mat frame) {
        double wRatio = SCALED_DOWN_MAX_IMAGE_WIDTH / frame.width();
        double hRatio = SCALED_DOWN_MAX_IMAGE_HEIGHT / frame.height();

        if (wRatio >= hRatio) {
            return new Size(frame.width() * wRatio, frame.height() * wRatio);
        } else {
            return new Size(frame.width() * hRatio, frame.height() * hRatio);
        }
    }
}
