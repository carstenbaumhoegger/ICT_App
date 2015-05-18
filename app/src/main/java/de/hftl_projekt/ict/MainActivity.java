package de.hftl_projekt.ict;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import butterknife.InjectView;
import butterknife.OnClick;
import de.hftl_projekt.ict.base.BaseActivity;

public class MainActivity extends BaseActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    public static final String TAG = "MainActivity";

    /** opencv camera view class */
    @InjectView(R.id.camera_view)
    JavaCameraView mOpenCvCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void onResume() {
        super.onResume();
        //init OpenCV
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback);
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
        Log.w(TAG, "onCameraViewStarted");
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG,"camera frame");
        return inputFrame.rgba();
    }

    /**
     * reacts onTouch
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        Log.w(TAG, "onTouch!");
        return false;
    }

    /**
     * saves an Image to external storage
     * @param pMat Mat to save
     * @return saving succeeded or failed
     */
    /*
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