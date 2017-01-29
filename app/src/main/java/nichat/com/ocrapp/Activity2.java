package nichat.com.ocrapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nichat.com.ocrapp.tools.RequestPermissionsTool;
import nichat.com.ocrapp.tools.RequestPermissionsToolImpl;

//Camera image capture activity
public class Activity2 extends Activity {

    private static final String TAG = "OCRApp";
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private ImageButton capture;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    FloatingActionButton floatingActionButton1;
    String path= Environment.getExternalStorageDirectory().getPath()+"/ScanIt";
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName =JPEG_FILE_PREFIX+timeStamp+"_"+JPEG_FILE_SUFFIX;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    // Permission request codes need to be < 256
    private static final int REQUEST_CAMERA = 0;
    public View mLayout;

    private TessBaseAPI tessBaseApi;
    TextView textView;
    private static final String lang = "eng";
    String result = "empty";
    private RequestPermissionsTool requestTool; //for API >=23 only

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/ScanIt/";
    private static final String TESSDATA = "tessdata";
    Uri outputFileUri;
    File picFile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        mLayout = findViewById(R.id.sample_main_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        TastyToast.makeText(this,"Landscape mode supported only.",TastyToast.LENGTH_LONG,TastyToast.DEFAULT).show();
        myContext = this;
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        /*for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Toast.makeText(this,"Back Camera exists", Toast.LENGTH_SHORT).show();
                break;
            }
        }*/

        initialize();

    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext))
        {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        else
        {

        }
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);
        mPicture =picture;
        mPreview.refreshCamera(mCamera);
    }


    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.setOrientation(LinearLayout.VERTICAL);
        cameraPreview.addView(mPreview);
        capture = (ImageButton) findViewById(R.id.capture);
        capture.setOnClickListener(captureListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {

                picFile=new File(path+"/"+imageFileName);
                FileOutputStream fos=new FileOutputStream(picFile);
                fos.write(data);
                fos.flush();
                fos.close();
                outputFileUri = Uri.fromFile(picFile);
                Intent i=new Intent(Activity2.this,Activity3.class);
                i.putExtra("outputFile",picFile.getAbsolutePath().toString());
                startActivity(i);
                finish();

            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }

            //refresh camera to continue preview
            //mPreview.refreshCamera(mCamera);
            //doOCR();
        }
    };



    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null, null,mPicture);
            //TastyToast.makeText(getBaseContext(),"Please wait...OCR processing ongoing",TastyToast.LENGTH_SHORT,TastyToast.DEFAULT).show();
        }
    };

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void requestPermissions() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestTool = new RequestPermissionsToolImpl();
        requestTool.requestPermissions(this, permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean grantedAllPermissions = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                grantedAllPermissions = false;
            }
        }

        if (grantResults.length != permissions.length || (!grantedAllPermissions)) {

            requestTool.onPermissionDenied();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }




}
