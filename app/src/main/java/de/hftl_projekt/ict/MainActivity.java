package de.hftl_projekt.ict;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
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
    private static final int QUANTIZATION_MODE_COLOR = 2;

    private static final String QUANTIZATION_MODE_NONE_STRING = "Keins";
    private static final String QUANTIZATION_MODE_BRIGHTNESS_STRING = "Helligkeit";
    private static final String QUANTIZATION_MODE_COLOR_STRING = "Farbwerte";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void onResume() {
        super.onResume();
        //init OpenCV
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
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

    /**
     * gets called on chose quantization button klick
     */
    @OnClick(R.id.btn_quantization) void chooseQuantization() {
        Log.w(TAG, "chose quantization!");
        buildQuantizationModeDialog();
    }

    /**
     * gets called on show app info button klick
     */
    @OnClick(R.id.btn_show_app_info) void showAppInfo() {
        //use AboutLibraries to display used libraries
        new LibsBuilder()
                //Pass the fields of your application to the lib so it can find all external lib information
                .withFields(R.string.class.getFields())
                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                .withActivityStyle(Libs.ActivityStyle.LIGHT)
                //show the app license
                .withLicenseShown(true)
                .start(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
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
        mCurrentMat = getQuantizisedImage(inputFrame);
        return mCurrentMat;
    }

    /**
     * quantizises the image based on chosen quantization mode
     * @param pInputFrame input image from camera
     * @return mat
     */
    public Mat getQuantizisedImage(CameraBridgeViewBase.CvCameraViewFrame pInputFrame) {
        switch (quantizationMode) {
            case QUANTIZATION_MODE_NONE:
                //just return camera picture if no quantization is chosen
                return pInputFrame.rgba();
            case QUANTIZATION_MODE_BRIGHTNESS:
                return changeBrightness(pInputFrame.rgba(), 2.2, 30);
            case QUANTIZATION_MODE_COLOR:
                // binary color quantization
                return reduceColors(pInputFrame.rgba());
        }
        return pInputFrame.rgba();
    }

    /**
     * changes the brightness of the input matrix (image) and uses the given alpha/beta values
     * @param image input matrix
     * @param alpha value
     * @param beta value
     * @return modified input matrix
     */
    public Mat changeBrightness(Mat image, double alpha, double beta) {
        // use native OpenCV function to convert the image with the specified alpha and beta values
        // rType (secondParameter) defines the type of the output matrix,
        // if the number is blow 0, the output matrix will be the same type as the input matrix
        image.convertTo(image, -1, alpha, beta);
        return image;
    }

    /**
     * method to reduce the color (quantize) the given matrix (image)
     * @param image input matrix
     * @return modified input matrix
     */
    public Mat reduceColors(Mat image) {

        List<Mat> channels = new ArrayList<>(); // create new list of matrixes (for the different channel (R, G, B)

        for(int i = 0; i < image.channels(); i++) {
            Mat channel = new Mat(); // fill array with a matrix for each channel
            channels.add(channel);
        }
        Core.split(image, channels); // split the image into the different channels

        // process each channel individually
        for(Mat c : channels) {
            // binary quantization (set threshold so each color (R, G, B) can have the value (0 or 255) )
            // and using the Otsu algorithm to optimize the quantization
            Imgproc.threshold(c, c, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        }
        Core.merge(channels, image); // put the channel back together
        return image;
    }

    /**
     * builds the dialog to chose QuantizationMode
     */
    private void buildQuantizationModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] choices = {QUANTIZATION_MODE_NONE_STRING, QUANTIZATION_MODE_BRIGHTNESS_STRING,
                QUANTIZATION_MODE_COLOR_STRING};
        builder.setTitle(getString(R.string.quantization_list_title));
        builder.setSingleChoiceItems(choices, quantizationMode, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                quantizationMode = item;
            }
        });
        builder.setPositiveButton("Auswählen", null);
        builder.setNegativeButton("Abbrechen", null);
        builder.show();
    }

    /**
     * saves an image to external storage
     * @param pMat Mat to save
     * @return saving succeeded or failed
     */
    @SuppressLint("SimpleDateFormat")
    public boolean saveImage(Mat pMat) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "path: " + path);
        // convert the colorspace (opencv doesn't handle rgba correct)
        Imgproc.cvtColor(pMat, pMat, Imgproc.COLOR_RGBA2BGR);

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
}