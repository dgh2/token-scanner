package com.tokenscanner;

import android.os.AsyncTask;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class ScannerAsyncManager {
//    private static long MIN_TIME_BETWEEN_THREADS = 50;
    private long lastUpdateTime = 0;
//    private long lastThreadStartTime = 0;
    private LinkedList<MatOfPoint> currentContours = new LinkedList<>();
    private Mat currentMat = null;

    private class ScannerAsync extends AsyncTask<Void, Void, LinkedList<MatOfPoint>> {
        private long startTime;
        private ScannerAsyncManager manager;
        private Mat input;

        public ScannerAsync(final ScannerAsyncManager manager, final Mat input) {
            this.manager = manager;
            this.input = input;
            startTime = System.currentTimeMillis();
        }

        @Override
        protected LinkedList<MatOfPoint> doInBackground(Void... params) {
            Mat temp = new Mat();
            LinkedList<MatOfPoint> contours = new LinkedList<>();
            Imgproc.cvtColor(input, temp, Imgproc.COLOR_RGB2GRAY);
            Imgproc.resize(temp, temp,
                    new Size(input.width() * 0.25, input.height() * 0.25),
                    0, 0, Imgproc.INTER_AREA);
            Imgproc.GaussianBlur(temp, temp, new Size(5, 5), 5, 5);
            Imgproc.Canny(temp, temp, 50, 150);
            Imgproc.resize(temp, temp,
                    new Size(input.width(), input.height()),
                    0, 0, Imgproc.INTER_AREA);
            Imgproc.findContours(temp, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
            currentMat = temp;
//            Imgproc.GaussianBlur(input, temp, new Size(5, 5), 5, 5);
//            Imgproc.resize(temp, temp,
//                    new Size(input.size().width * 0.25, input.size().height * 0.25),
//                    0, 0, Imgproc.INTER_AREA);
//            Imgproc.cvtColor(temp, temp, Imgproc.COLOR_RGB2GRAY);
//            Imgproc.Canny(input, temp, 50, 150);
//            Imgproc.resize(temp, temp, input.size(), 0, 0, Imgproc.INTER_AREA);
//            Imgproc.findContours(temp, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
            return contours;
        }

        @Override
        protected void onPostExecute(LinkedList<MatOfPoint> param) {
            manager.postExecute(startTime, param);
        }
    }

    public void execute(final Mat frame) {
//        if (System.currentTimeMillis() >= lastThreadStartTime + MIN_TIME_BETWEEN_THREADS) {
//            lastThreadStartTime = System.currentTimeMillis();
            new ScannerAsync(this, frame).execute();
//        }
    }

    private void postExecute(long time, LinkedList<MatOfPoint> contours) {
        if (time > lastUpdateTime) {
            lastUpdateTime = time;
            currentContours = contours;
        }
    }

    public LinkedList<MatOfPoint> getCurrentContours() {
        return currentContours;
    }

    public Mat getCurrentMat() {
        return currentMat;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
