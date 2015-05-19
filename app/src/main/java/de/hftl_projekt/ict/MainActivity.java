package de.hftl_projekt.ict;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.InjectView;
import butterknife.OnClick;
import de.hftl_projekt.ict.base.BaseActivity;
import de.hftl_projekt.ict.utilities.SharedPrefsHandler;

public class MainActivity extends BaseActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    public static final String TAG = "MainActivity";

    /** opencv camera view class */
    @InjectView(R.id.camera_view)
    JavaCameraView mOpenCvCameraView;

    /** quantization mode chosen by user */
    private int quantizationMode = 0;

    /** current Mat shown by screen */
    private Mat mCurrentMat;

    private static final int QUANTIZATION_MODE_NONE = 0;
    private static final int QUANTIZATION_MODE_BRIGHTNESS = 1;
    private static final int QUANTIATION_MODE_COLOR = 2;

    private static final String QUANTIZATION_MODE_NONE_STRING = "Keins";
    private static final String QUANTIZATION_MODE_BRIGHTNESS_STRING = "Helligkeit";
    private static final String QUANTIZATION_MODE_COLOR_STRING = "Farbwerte";

    private static final String KEY_QUANTIZATION_MODE = "quantization_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void onResume() {
        super.onResume();
        //init OpenCV
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        //get the quantization mode
        quantizationMode = Integer.parseInt(SharedPrefsHandler.getInstance(this).
                loadStringSettings(KEY_QUANTIZATION_MODE));
    }

    /**
     * gets called on capture screen button click
     */
    @OnClick(R.id.btn_capture_screen) void captureScreen() {
        Log.w(TAG, "captureScreen!");
        if(saveImage(mCurrentMat)) {
            Log.i(TAG, "image saving successful!");
            Toast.makeText(this, "Bildschirmaufnahme erfolgreich", Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "error while saving image!");
            Toast.makeText(this, "Fehler während Bildschirmaufnahme", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_quantization) void chooseQuantization() {
        Log.w(TAG, "chose quantization!");
        buildQuantizationModeDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.w(TAG, "Camera res: " + width + " * " + height);
        Log.w(TAG, "onCameraViewStarted");
    }

    @Override
    public void onCameraViewStopped() {
        Log.w(TAG, "onCameraViewStopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "camera frame");
        mCurrentMat = getQuantizisedImage(inputFrame.rgba());
        return mCurrentMat;
    }

    /**
     * quantizises the image based on chosen quantization mode
     * @param pInputMat input image from camera
     * @return mat
     */
    public Mat getQuantizisedImage(Mat pInputMat) {
        Log.d(TAG, "inputMatSize: " + pInputMat.size());

        switch (quantizationMode) {
            case QUANTIZATION_MODE_NONE:
                //just return camera picture if no quantization is chosen
                return pInputMat;
            case QUANTIZATION_MODE_BRIGHTNESS:
                //TODO!
                return pInputMat;
            case QUANTIATION_MODE_COLOR:
                //TODO!
                return pInputMat;
        }
        return pInputMat;
    }

    /**
     * builds the dialog to chose QuantizationMode
     */
    private void buildQuantizationModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] choices = {QUANTIZATION_MODE_NONE_STRING, QUANTIZATION_MODE_BRIGHTNESS_STRING,
                QUANTIZATION_MODE_COLOR_STRING};
        builder.setSingleChoiceItems(choices, 0, null);
        builder.setPositiveButton("Select", null);
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * saves an Image to external storage
     * @param pMat Mat to save
     * @return saving succeeded or failed
     */
    @SuppressLint("SimpleDateFormat")
    public boolean saveImage(Mat pMat) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "path: " + path);

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
        return Highgui.imwrite(filename, pMat);
    }

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

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }
}