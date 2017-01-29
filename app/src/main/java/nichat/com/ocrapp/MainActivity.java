package nichat.com.ocrapp;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;
import nichat.com.ocrapp.tools.RequestPermissionsTool;
import nichat.com.ocrapp.tools.RequestPermissionsToolImpl;

import static android.R.attr.path;

//Landing Page activity
public class MainActivity extends AppCompatActivity {

    FancyButton scan;
    ListView prev_docs;
    String path1= Environment.getExternalStorageDirectory().getPath()+"/ScanIt";
    String path2= Environment.getExternalStorageDirectory().getPath()+"/ScanIt/PDF/Original";
    File dir1,dir2;
    File[] files;
    String listItemName;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scan=(FancyButton)findViewById(R.id.scan);
        prev_docs=(ListView)findViewById(R.id.prev_docs);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,Activity2.class);
                startActivity(i);
            }
        });
        dir1=new File(path1);
        try
        {
            dir1.mkdirs();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        dir2=new File(path2);
        try
        {
            dir2.mkdirs();
        /*    File file[]=dir2.listFiles();
            for(int i=0;i<file.length;i++)
            {
                filename[i]=file[i].getName().toString();
            }
        */
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        ArrayList<String> FilesInFolder = GetFiles(path2);
        prev_docs.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, FilesInFolder));
        registerForContextMenu(prev_docs);
        prev_docs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(files.length!=0)
                {
                    String filename=files[position].getName();
                    Toast.makeText(getBaseContext(),filename,Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(MainActivity.this,Activity4.class);
                    i.putExtra("filename",filename);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(getBaseContext(),"No previously generated documents",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Method which gets the list of all files in the proper folder
    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);
        f.mkdirs();
        files = f.listFiles();
        if (files.length == 0)
            MyFiles.add("No previous generated documents");
        else {
             for (int i=files.length - 1; i >= 0; i--)
                MyFiles.add(files[i].getName());
        }
        return MyFiles;
    }

    protected void onResume()
    {
        super.onResume();
        ArrayList<String> FilesInFolder = GetFiles(path2);
        prev_docs.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, FilesInFolder));

    }


    //For context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.prev_docs) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(files[info.position].getName());
            CreateMenu(menu);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        listItemName = files[(files.length-1)-info.position].getName();
        return MenuChoice(item);
    }

    private void CreateMenu(Menu menu)
    {
        //To enable shortcut keys, need to call setQwertyMode() method
        //menu.setQwertyMode(true);
        MenuItem mnu1 = menu.add(0, 0, 0, "Open");
        //mnu1.setAlphabeticShortcut('a');
        mnu1.setIcon(R.drawable.ic_pdf_icon);
        MenuItem mnu2 = menu.add(0, 1, 1, "Open Processed");
        MenuItem mnu3 = menu.add(0, 2, 2, "Delete");
    }

    //Actions which are performed whien the appropriate button is pressed in the context menu
    private boolean MenuChoice(MenuItem item)
    {
        switch (item.getItemId()) {
            case 0:
                Toast.makeText(getBaseContext(),listItemName,Toast.LENGTH_SHORT).show();
                Intent i=new Intent(MainActivity.this,Activity4.class);
                i.putExtra("option",1);
                i.putExtra("filename",listItemName);
                startActivity(i);
                return true;
            case 1:
                Toast.makeText(getBaseContext(),listItemName,Toast.LENGTH_SHORT).show();
                Intent ii=new Intent(MainActivity.this,Activity4.class);
                ii.putExtra("option",2);
                ii.putExtra("filename",listItemName);
                startActivity(ii);
                return true;
            case 2:
                File pdf=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ScanIt/PDF/Original",listItemName);
                pdf.delete();
                pdf=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ScanIt/PDF/Processed",listItemName);
                pdf.delete();
                ArrayList<String> FilesInFolder = GetFiles(path2);
                prev_docs.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, FilesInFolder));
                return true;
        }
        return false;
    }
}





