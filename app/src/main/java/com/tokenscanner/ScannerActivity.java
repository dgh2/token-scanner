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
    private static final Scalar SCALAR_RED = new Scalar(255, 0, 0);
    private static final Scalar SCALAR_GREEN = new Scalar(0, 255, 0);
    private static final Scalar SCALAR_BLUE = new Scalar(0, 0, 255);

    private ScannerView scannerView;
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
//                    display = display.next();
//                    Toast.makeText(getApplicationContext(), display.name() + " view", Toast.LENGTH_SHORT).show();
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
        Log.w(TAG, "Starting test!");
        int test = 0;
        try {
            Mat frame = new Mat();
            Mat canny = new Mat();
            Mat output = new Mat();
            Log.w(TAG, "Test: " + ++test);
            Imgproc.resize(input, frame, new Size(), 0.25, 0.25, Imgproc.INTER_AREA);
            Log.w(TAG, "Test: " + ++test);
            Imgproc.Canny(frame, canny, 50, 150);
            Log.w(TAG, "Test: " + ++test);
            Imgproc.resize(canny, output, new Size(scannerView.getWidth(), scannerView.getHeight()),
                    0, 0, Imgproc.INTER_AREA);
            Log.w(TAG, "Test Complete: " + ++test);
            return output;
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION CAUGHT!", e);
        } finally {
            Log.w(TAG, "Finally after test!");
        }
        return input;
    }

//    private Mat processFrame(final Mat input) {
//        Mat frame = new Mat();
//        input.copyTo(frame);
//        Mat grayscale = new Mat();
//        Mat blurred = new Mat();
//        Mat weathered = new Mat();
//        Mat resized = new Mat();
//        Mat threshold = new Mat();
//        Mat cannyMap = new Mat();
//        Mat contourMap = new Mat();
//        Mat result = new Mat();
//        LinkedList<MatOfPoint> contours = new LinkedList<>();
////        Point structuringAnchor = new Point(-1, -1);
////        Mat structuringElement = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(6, 6));
//
//        switch (display) {
//            case CAMERA_VIEW:
//                return frame;
//            case GRAYSCALE:
//                Imgproc.resize(input, frame,
//                        getScaledDownSize(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.resize(grayscale, resized,
//                        new Size(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                return resized;
////            case INFLATE_ERODE:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.dilate(grayscale, weathered, structuringElement, structuringAnchor, 1);
////                Imgproc.erode(weathered, weathered, structuringElement, structuringAnchor, 1);
////                return weathered;
////            case INFLATE_ERODE_THRESHOLD:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.dilate(grayscale, weathered, structuringElement, structuringAnchor, 1);
////                Imgproc.erode(weathered, weathered, structuringElement, structuringAnchor, 1);
////                Imgproc.threshold(weathered, threshold, 127, 255, Imgproc.THRESH_BINARY);
////                return threshold;
////            case INFLATE_ERODE2:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.dilate(grayscale, weathered, structuringElement, structuringAnchor, 2);
////                Imgproc.erode(weathered, weathered, structuringElement, structuringAnchor, 2);
////                return weathered;
////            case INFLATE_ERODE2_THRESHOLD:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.dilate(grayscale, weathered, structuringElement, structuringAnchor, 2);
////                Imgproc.erode(weathered, weathered, structuringElement, structuringAnchor, 2);
////                Imgproc.threshold(weathered, threshold, 127, 255, Imgproc.THRESH_BINARY);
////                return threshold;
////            case ERODE_INFLATE:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.erode(grayscale, weathered, structuringElement, structuringAnchor, 1);
////                Imgproc.dilate(weathered, weathered, structuringElement, structuringAnchor, 1);
////                return weathered;
////            case ERODE_INFLATE_THRESHOLD:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.erode(grayscale, weathered, structuringElement, structuringAnchor, 1);
////                Imgproc.dilate(weathered, weathered, structuringElement, structuringAnchor, 1);
////                Imgproc.threshold(weathered, threshold, 127, 255, Imgproc.THRESH_BINARY);
////                return threshold;
////            case ERODE_INFLATE2:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.erode(grayscale, weathered, structuringElement, structuringAnchor, 2);
////                Imgproc.dilate(weathered, weathered, structuringElement, structuringAnchor, 2);
////                return weathered;
////            case ERODE_INFLATE2_THRESHOLD:
////                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.erode(grayscale, weathered, structuringElement, structuringAnchor, 2);
////                Imgproc.dilate(weathered, weathered, structuringElement, structuringAnchor, 2);
////                Imgproc.threshold(weathered, threshold, 127, 255, Imgproc.THRESH_BINARY);
////                return threshold;
//            case PRE_BLURRED:
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
//                Imgproc.resize(blurred, frame,
//                        getScaledDownSize(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                Imgproc.resize(frame, resized,
//                        new Size(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                return resized;
//            case PRE_BLURRED_THRESHOLD:
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
//                Imgproc.resize(blurred, resized,
//                        getScaledDownSize(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                Imgproc.threshold(resized, threshold, 100, 255, Imgproc.THRESH_BINARY);
//                Imgproc.resize(threshold, resized,
//                        new Size(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                return resized;
//            case POST_BLURRED:
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.resize(grayscale, resized,
//                        getScaledDownSize(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                Imgproc.GaussianBlur(resized, blurred, new Size(5, 5), 5, 5);
//                Imgproc.resize(blurred, resized,
//                        new Size(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                return resized;
//            case POST_BLURRED_THRESHOLD:
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.resize(grayscale, resized,
//                        getScaledDownSize(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                Imgproc.GaussianBlur(resized, blurred, new Size(5, 5), 5, 5);
//                Imgproc.threshold(blurred, threshold, 100, 255, Imgproc.THRESH_BINARY);
//                Imgproc.resize(threshold, resized,
//                        new Size(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                return resized;
//            case CANNY:
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.bilateralFilter(grayscale, blurred, 9, 150, 50);
//                Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
//                Imgproc.resize(blurred, resized,
//                        getScaledDownSize(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                Imgproc.Canny(resized, cannyMap, 50, 150);
//                Imgproc.resize(cannyMap, resized,
//                        new Size(scannerView.getWidth(), scannerView.getHeight()),
//                        0, 0, Imgproc.INTER_AREA);
//                return resized;
//            case CONTOURS:
//                frame.copyTo(contourMap);
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.bilateralFilter(grayscale, blurred, 9, 150, 50);
//                Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
//                Imgproc.Canny(blurred, cannyMap, 50, 150);
//                Imgproc.findContours(cannyMap, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
//                Imgproc.drawContours(contourMap, contours, -1, SCALAR_RED);
//                return contourMap;
//            case FILTERED_CONTOURS:
//                frame.copyTo(contourMap);
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.bilateralFilter(grayscale, blurred, 9, 150, 50);
//                Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
//                Imgproc.Canny(blurred, cannyMap, 50, 150);
//                Imgproc.findContours(cannyMap, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
//                filterContours(contours);
//                Imgproc.drawContours(contourMap, contours, -1, SCALAR_RED);
//                return contourMap;
//            case ELLIPSE_BOXES:
//                frame.copyTo(result);
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.bilateralFilter(grayscale, blurred, 9, 150, 50);
//                Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
//                Imgproc.Canny(blurred, cannyMap, 50, 150);
//                Imgproc.findContours(cannyMap, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
//                filterContours(contours);
//                for (MatOfPoint contour : contours) {
//                    drawRotatedRect(result, getEllipseForContour(contour));
//                }
//                return result;
//            case RESULT:
//                frame.copyTo(result);
//                Imgproc.cvtColor(frame, grayscale, Imgproc.COLOR_RGB2GRAY);
////                Imgproc.bilateralFilter(grayscale, blurred, 9, 150, 50);
//                Imgproc.GaussianBlur(grayscale, blurred, new Size(5, 5), 5, 5);
//                Imgproc.Canny(blurred, cannyMap, 50, 150);
//                Imgproc.findContours(cannyMap, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
//                filterContours(contours);
//                detectEllipticalContours(result, contours);
//                return result;
//            default:
//                Log.w(TAG, "Unrecognized display type detected: " + display.name());
//                display = display.next();
//                return frame;
//        }
//    }

    private void filterContours(LinkedList<MatOfPoint> contours) {
        LinkedList<MatOfPoint> ignoredContours = new LinkedList<>();
        for (MatOfPoint contour : contours) {
            if (!contour.isContinuous() || contour.toList().size() < 5) {
                // || !Imgproc.isContourConvex(contour)
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
