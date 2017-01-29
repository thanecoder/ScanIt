package nichat.com.ocrapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.sdsmdg.tastytoast.TastyToast;
import com.shockwave.pdfium.PdfDocument;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import nichat.com.ocrapp.tools.RequestPermissionsTool;
import nichat.com.ocrapp.tools.RequestPermissionsToolImpl;

//Activity which does the OCR processing and generates the pdf
public class Activity3 extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "ScanIt";
    private TessBaseAPI tessBaseApi;
    TextView textView;
    private static final String lang = "eng";
    String result = "empty";
    private RequestPermissionsTool requestTool; //for API >=23 only

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/ScanIt/";
    private static final String TESSDATA = "tessdata";
    Uri outputFileUri;
    File picFile;
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String docFileName =PDF_FILE_PREFIX+timeStamp+PDF_FILE_SUFFIX;
    private static final String PDF_FILE_PREFIX = "DOC_";
    private static final String PDF_FILE_SUFFIX = ".pdf";
    PDFView pdfView;
    File file;
    String list = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        Intent i=getIntent();
        result=i.getStringExtra("outputFile");
        pdfView=(PDFView)findViewById(R.id.pdfView);
        //doOCR();
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();

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
//OCR Processing methods
    private void doOCR() {
        prepareTesseract();
        //Toast.makeText(this,""+outputFileUri.toString(),Toast.LENGTH_LONG).show();
        startOCR(result);
    }

    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                SuperToast.create(this,"Directory not created ", Style.DURATION_SHORT);
            }
        } else {
            SuperToast.create(this,"Created directory"+path,Style.DURATION_SHORT);
        }
    }

    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }

    private void startOCR(String filepath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(filepath,options);
            //Here the text extracted from image is stored in a String variable called result
            result = extractText(bitmap);

            //Additional processing done on OCR generated text
            String [] fields = result.split(":|\\n");
            for( String text : fields){
                Log.e("text",text);
            }

            int index = 0;
            for( int i = 0; i < fields.length; ++i, ++index){
                if(fields[i] == ""){
                    index -= 1;
                }
                else {
                    if (index % 2 == 0) {
                        list += "Your " + fields[i] + " is: ";
                    } else {
                        list += fields[i] + "\n";
                    }
                }
            }
            Log.e("textMeraText", list);


            //Original directory created
            File sdcard= Environment.getExternalStorageDirectory();
            File dir=new File(sdcard.getAbsoluteFile()+"/ScanIt/PDF/Original");
            dir.mkdirs();
            file=new File(dir,docFileName);
            try {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                Paragraph prProfile = new Paragraph();
                prProfile.add(result);
                document.add(prProfile);
                document.close();
                File imageFile=new File(filepath);
                imageFile.delete();
                pdfView.fromFile(file).enableDoubletap(true).load();
            } catch (Exception e) {
                e.printStackTrace();
            }

        //Processed directory created
        dir=new File(sdcard.getAbsoluteFile()+"/ScanIt/PDF/Processed");
        dir.mkdirs();
        file=new File(dir,docFileName);
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            Paragraph prProfile = new Paragraph();
            prProfile.add(list);
            document.add(prProfile);
            document.close();
            File imageFile=new File(filepath);
            imageFile.delete();
            pdfView.fromFile(file).enableDoubletap(true).load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    catch (Exception e) {
        e.printStackTrace();
    }

    }


    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(DATA_PATH, lang);

//       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//
//        //blackList Example
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Processing..."); // Calls onProgressUpdate()
            doOCR();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation

        }


        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }


        @Override
        protected void onProgressUpdate(String... text) {
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
            SuperToast.create(getBaseContext(),"Processing ongoing...Please wait",Style.DURATION_VERY_LONG).setAnimations(Style.ANIMATIONS_FLY).show();
        }
    }

}
