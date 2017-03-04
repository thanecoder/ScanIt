package nichat.com.ocrapp;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

//Splash screen activity
public class Activity0 extends Activity {

    public final int CAMERA_PERMISSION=101;
    public final int STORAGE_PERMISSION=111;
    int permissionCheck1;
    int permissionCheck2;
    int flag1=0;
    int flag2=0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_0);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permissionCheck1 = checkSelfPermission(Manifest.permission.CAMERA);
            permissionCheck2 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if ((permissionCheck1 != PackageManager.PERMISSION_GRANTED) || (permissionCheck2 != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION);
        }
        Thread background = new Thread() {// Create Thread that will sleep for 5 seconds
            public void run() {

                try {
                    // Thread will sleep for 5 seconds
                    sleep(4 * 1000);
                        // After 2 seconds redirect to another intent
                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(i);

                        //Remove activity
                        finish();


                } catch (Exception e) {
                }
            }
        };
        // start thread
        background.start();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_PERMISSION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    flag1=1;
                }
                else
                {
                    flag1=0;
                }
                return;
            }
            case STORAGE_PERMISSION:
                {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        flag2=1;
                    }
                    else
                    {
                        flag2=0;
                    }
                    return;
                }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

}
