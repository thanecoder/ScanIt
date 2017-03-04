package nichat.com.ocrapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;

import nichat.com.ocrapp.tools.RequestPermissionsTool;
import nichat.com.ocrapp.tools.RequestPermissionsToolImpl;

//Activity which is accessed when we click on a file name in the landing page list.This activity only shows the generated pdf
public class Activity4 extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    TextView textView;
    String path1= Environment.getExternalStorageDirectory().getPath()+"/ScanIt/PDF/Processed";
    String path2= Environment.getExternalStorageDirectory().getPath()+"/ScanIt/PDF/Original";
    String filename = "empty";
    int option=1;
    private RequestPermissionsTool requestTool; //for API >=23 only
    PDFView pdfView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);
        pdfView=(PDFView)findViewById(R.id.pdfView);
        Intent i = getIntent();
        option=i.getIntExtra("option",0);
        filename = i.getStringExtra("filename");
        if(option==1)
        {
            File dir2=new File(path2);
            dir2.mkdirs();
            File pdf=new File(path2,filename);
            pdfView.fromFile(pdf).enableDoubletap(true).load();
        }
        else
        {
            File dir1=new File(path1);
            dir1.mkdirs();
            File pdf=new File(path1,filename);
            pdfView.fromFile(pdf).enableDoubletap(true).load();
        }

    }
}
