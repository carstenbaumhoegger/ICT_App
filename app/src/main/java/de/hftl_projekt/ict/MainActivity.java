package de.hftl_projekt.ict;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;
import de.hftl_projekt.ict.base.BaseActivity;

public class MainActivity extends BaseActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    public static final String TAG = "MainActivity";

    /** opencv camera view class */
    @InjectView(R.id.camera_view)
    JavaCameraView mOpenCvCameraView;

    private boolean bShootNow = false, bDisplayTitle = true;

    private byte[] byteColourTrackCentreHue;

    private double dTextScaleFactor;

    private List<Integer> iHueMap, channels;
    private List<Float> ranges;

    //used to calculate the fps
    private long lFrameCount = 0, lMilliStart = 0, lMilliNow = 0, lMilliShotTime = 0;

    private Mat mRgba, mGray, mIntermediateMat, mMatRed, mMatGreen, mMatBlue, mROIMat,
            mMatRedInv, mMatGreenInv, mMatBlueInv, mHSVMat, mErodeKernel, mContours, lines;

    private MatOfRect faces;
    private MatOfPoint2f mMOP2f1, mMOP2f2;
    private MatOfPoint2f mApproxContour;
    private MatOfPoint MOPcorners;

    private Scalar colorRed, colorGreen;
    private Size sSize3, sMatSize;
    private String string, sShotText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void onResume() {
        super.onResume();
        //init OpenCV
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }

    /**
     * get's called on capture screen button click
     */
    @OnClick(R.id.btn_capture_screen) void captureScreen() {
        Log.w(TAG, "captureScreen!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            //start SettingsActivity
            Intent i_settings = new Intent(this, SettingsActivity.class);
            startActivity(i_settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.w(TAG, "Camera res: " + width + " * " + height);
        byteColourTrackCentreHue = new byte[3];
        // green = 60 // mid yellow  27
        byteColourTrackCentreHue[0] = 27;
        byteColourTrackCentreHue[1] = 100;
        byteColourTrackCentreHue[2] = (byte)255;

        channels = new ArrayList<>();
        channels.add(0);
        colorRed = new Scalar(255, 0, 0, 255);
        colorGreen = new Scalar(0, 255, 0, 255);

        faces = new MatOfRect();

        iHueMap = new ArrayList<>();
        iHueMap.add(0);
        iHueMap.add(0);
        lines = new Mat();

        mApproxContour = new MatOfPoint2f();
        mContours = new Mat();
        mGray = new Mat();
        mHSVMat = new Mat();
        mIntermediateMat = new Mat();
        mMatRed = new Mat();
        mMatGreen = new Mat();
        mMatBlue = new Mat();
        mMatRedInv = new Mat();
        mMatGreenInv = new Mat();
        mMatBlueInv = new Mat();
        mMOP2f1 = new MatOfPoint2f();
        mMOP2f2 = new MatOfPoint2f();
        MOPcorners = new MatOfPoint();
        mRgba = new Mat();
        mROIMat = new Mat();

        ranges = new ArrayList<>();
        ranges.add(50.0f);
        ranges.add(256.0f);

        sMatSize = new Size();
        sSize3 = new Size(3, 3);

        string = "";

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        dTextScaleFactor = ((double)densityDpi / 240.0) * 0.9;

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);

        Log.w(TAG, "onCameraViewStarted");
    }

    @Override
    public void onCameraViewStopped() {
        releaseMats();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.w(TAG, "onCameraFrame!");
        return null;
    }

    public void releaseMats () {
        mRgba.release();
        mIntermediateMat.release();
        mGray.release();
        mMatRed.release();
        mMatGreen.release();
        mMatBlue.release();
        mROIMat.release();
        mMatRedInv.release();
        mMatGreenInv.release();
        mMatBlueInv.release();
        mHSVMat.release();
        mErodeKernel.release();
        mContours.release();
        lines.release();
        faces.release();
        MOPcorners.release();
        mMOP2f1.release();
        mMOP2f2.release();
        mApproxContour.release();
    }

    //@Override
    public Mat onCameraFrame(Mat inputFrame) {
        mErodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, sSize3);

        // start the timing counter to put the framerate on screen
        // and make sure the start time is up to date, do
        // a reset every 10 seconds
        if (lMilliStart == 0)
            lMilliStart = System.currentTimeMillis();

        if ((lMilliNow - lMilliStart) > 10000) {
            lMilliStart = System.currentTimeMillis();
            lFrameCount = 0;
        }

        inputFrame.copyTo(mRgba);
        sMatSize.width = mRgba.width();
        sMatSize.height = mRgba.height();

        /*
        if (bDisplayTitle) {
            showTitle("RGBA Preview", 1, colorGreen);
        }
        */

        // get the time now in every frame
        lMilliNow = System.currentTimeMillis();

        // increment the frame counter
        lFrameCount++;

        /*
        if (bDisplayTitle) {
            //calculate the fps
            string = String.format("FPS: %2.1f", (float)(lFrameCount * 1000) / (float)(lMilliNow - lMilliStart));

            //showTitle(string, 2, colorGreen);
        }
        */

        if (bShootNow) {
            // get the time of the attempt to save a screenshot
            lMilliShotTime = System.currentTimeMillis();
            bShootNow = false;

            // try it, and set the screen text accordingly.
            // this text is shown at the end of each frame until
            // 1.5 seconds has elapsed
            if (saveImage(mRgba)) {
                sShotText = "SCREENSHOT SAVED";
            }
            else {
                sShotText = "SCREENSHOT FAILED";
            }
        }

        /*
        if (System.currentTimeMillis() - lMilliShotTime < 1500) {
            showTitle(sShotText, 3, colorRed);
        }
        */

        return mRgba;
    }

    /**
     * captures a screenshot onTouch
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        Log.w(TAG, "onTouch!");
        bShootNow = true;
        return false;
    }

    /**
     * saves an Image to external storage
     * @param pMat Mat to save
     * @return saving succeeded or failed
     */
    @SuppressLint("SimpleDateFormat")
    public boolean saveImage(Mat pMat) {
        Imgproc.cvtColor(pMat, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //build the filename
        String filename = getString(R.string.app_name) + "_";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String dateString = fmt.format(date);
        filename += dateString;
        filename += ".png";

        File file = new File(path, filename);
        filename = file.toString();
        //try to write the image to storage
        return Highgui.imwrite(filename, mIntermediateMat);
    }
/*
    private void showTitle(String s, int iLineNum, Scalar color) {
        Core.putText(mRgba, s, new Point(10, (int)(dTextScaleFactor * 60 * iLineNum)),
                Core.FONT_HERSHEY_SIMPLEX, dTextScaleFactor, color, 2);
    }
    */

    /**
     * callback that checks if OpenCV is available
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}