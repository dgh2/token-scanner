package com.tokenscanner;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class ScannerActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {
    private static final String TAG = ScannerActivity.class.getName();
    private static final double THRESHOLD_VALUE = 75;
    private static final double MIN_BOUNDBOX_VOLUME_PERCENT = 0.01;
    private static final double ELLIPSE_MATCH_THRESHOLD = 0.0025;
    private static final Scalar SCALAR_RED = new Scalar(255, 0, 0);
    private static final Scalar SCALAR_GREEN = new Scalar(0, 255, 0);
    private static final Scalar SCALAR_BLUE = new Scalar(0, 0, 255);

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
        CAMERA_VIEW,
        FILTERED,
        CONTOURS,
        ELLIPSE_BOXES,
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
        Mat grayscale = new Mat();
        Mat bilateralFiltered = new Mat();
        Mat contourMap = new Mat();
        Mat result = new Mat();
        LinkedList<MatOfPoint> contours = new LinkedList<>();

        Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(grayscale, bilateralFiltered, 9, 150, 50);
        Imgproc.Canny(bilateralFiltered, contourMap, 50, 150);
        Imgproc.findContours(contourMap, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        filterContours(contours);

        switch (display) {
            case CAMERA_VIEW:
                frame.copyTo(result);
                break;
            case FILTERED:
                bilateralFiltered.copyTo(result);
                break;
            case CONTOURS:
                contourMap.copyTo(result);
                break;
            case RESULT:
                frame.copyTo(result);
                detectEllipticalContours(result, contours);
                break;
            case ELLIPSE_BOXES:
                frame.copyTo(result);
                for (MatOfPoint contour : contours) {
                    drawRotatedRect(result, getEllipseForContour(contour));
                }
                break;
            default:
                Log.w(TAG, "Unrecognized display type detected: " + display.name());
                display = DISPLAY_TYPE.RESULT;
                frame.copyTo(result);
                detectEllipticalContours(result, contours);
                break;
        }
        return result;
    }

    private void filterContours(LinkedList<MatOfPoint> contours) {
        LinkedList<MatOfPoint> ignoredContours = new LinkedList<>();
        for (MatOfPoint contour : contours) {
            if (!contour.isContinuous() || contour.toList().size() < 5) {
                ignoredContours.add(contour);
            }
        }
        contours.removeAll(ignoredContours);
    }

    private RotatedRect getEllipseForContour(final MatOfPoint contour) {
        MatOfPoint2f mat2F = new MatOfPoint2f();
        contour.convertTo(mat2F, CvType.CV_32FC2);
        return Imgproc.fitEllipse(mat2F);
    }

    private boolean contourIsEllipse(MatOfPoint contour) {
        //TODO: match contour to its ellipses' contour
//        Mat ellipseContour = new Mat();
//        Imgproc.drawContours(ellipseContour, getEllipseForContour(contour), 1, SCALAR_RED, 2);
//        return Imgproc.matchShapes(contour, getEllipseForContour(contour), Imgproc.CV_CONTOURS_MATCH_I1, 0.0) <= ELLIPSE_MATCH_THRESHOLD;
        return true;
    }

    private LinkedList<RotatedRect> detectEllipticalContours(final Mat frame, final LinkedList<MatOfPoint> contours) {
        LinkedList<RotatedRect> ellipseRects = new LinkedList<>();
        for (MatOfPoint contour : contours) {
            RotatedRect ellipseRect = getEllipseForContour(contour);
            if (contourIsEllipse(contour)) {
                Imgproc.ellipse(frame, ellipseRect.center, ellipseRect.size,
                        ellipseRect.angle, 0, 360, SCALAR_GREEN, 10);
            } else {
                Imgproc.ellipse(frame, ellipseRect.center, ellipseRect.size,
                        ellipseRect.angle, 0, 360, SCALAR_RED, 10);
            }
        }
        return ellipseRects;
    }

    private void drawRotatedRect(final Mat frame, final RotatedRect rotatedRect) {
        Point[] rect_points = new Point[4];
        rotatedRect.points(rect_points);
        for (int i = 0; i < 4; ++i) {
            Imgproc.line(frame, rect_points[i], rect_points[(i + 1) % 4], SCALAR_BLUE, 3);
        }
    }
}
