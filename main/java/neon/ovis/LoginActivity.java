package neon.ovis;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "";
    private TDB db;
    private static String filePath = "/sdcard/Download/plan.csv";
    ProgressDialog progressDialog;
    private String progTitle = "Refresh";
    private String progMessage = "We are refreshing your timetable";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        db = new TDB(this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String SUO = settings.getString("UO",""); //Stored UO

        final CheckBox checkbox = findViewById(R.id.remcb);
        boolean bool = settings.getBoolean("remcb", true);

        if(!SUO.isEmpty())
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(progMessage); // Setting Message
            progressDialog.setTitle(progTitle); // Setting Title
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            progressDialog.show(); // Display Progress Dialog
            progressDialog.setCancelable(false);

            String fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo="+SUO;
            String saveDir = "/sdcard/Download";
            final String[] params = new String[2];
            params[0] = fileURL;
            params[1] = saveDir;

            if(isNetworkAvailable())
            {
                /*
                try
                {
                    HttpDownloader.dl(params);
                    readFileData(filePath);
                    progressDialog.dismiss();
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                */

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            HttpDownloader.dl(params);
                            readFileData(filePath);
                            progressDialog.dismiss();

                            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                            LoginActivity.this.startActivity(intent);
                            LoginActivity.this.finish();
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        final EditText ETUO = findViewById(R.id.UO); // EditTextUO
        Button logbutton = findViewById(R.id.logbutton); // Log in button
        final CheckBox remcb = findViewById(R.id.remcb); // Remember me cb

        ETUO.setText(SUO);

        checkbox.setChecked(bool);

        logbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uo = ETUO.getText().toString();
                if(!uo.isEmpty())
                { //correct

                    if(remcb.isChecked())
                    {
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("UO",uo);
                        editor.putBoolean("remcb", true);
                        editor.apply();
                    }

                    if(isStoragePermissionGranted())
                    {
                        // EX : UO257514
                        String fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo="+uo;
                        String saveDir = "/sdcard/Download";
                        final String[] params = new String[2];
                        params[0] = fileURL;
                        params[1] = saveDir;

                        if(isNetworkAvailable())
                        {
                            progressDialog = new ProgressDialog(LoginActivity.this);
                            progressDialog.setMessage(progMessage); // Setting Message
                            progressDialog.setTitle(progTitle); // Setting Title
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                            progressDialog.show(); // Display Progress Dialog
                            progressDialog.setCancelable(false);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try
                                    {
                                        HttpDownloader.dl(params);
                                        readFileData(filePath);
                                        progressDialog.dismiss();
                                    }
                                    catch (FileNotFoundException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                            /*
                            try
                            {
                                HttpDownloader.dl(params);
                                readFileData(filePath);
                                progressDialog.dismiss();
                            }
                            catch (FileNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                            progressDialog.dismiss();
                            */

                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "NETWORK UNAVAILABLE", Toast.LENGTH_LONG).show();
                        }

                        Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                        LoginActivity.this.startActivity(intent);
                        LoginActivity.this.finish();
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }
                else
                { //wrong
                    Toast.makeText(LoginActivity.this, "INCORRECT UO", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public  boolean isStoragePermissionGranted()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    void readFileData(String path) throws FileNotFoundException
    {
        Line l;
        String[] data;
        File file = new File(path);

        if (file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try
            {
                db.clear();
                String csvLine;
                while ((csvLine = br.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try
                    {
                        if(!data[0].equals("Subject"))
                        {
                            String[] st = data[2].split(Pattern.quote("."));
                            String sh = st[0];
                            sh = sh.trim();
                            if(Integer.parseInt(sh)<=9)
                            {
                                data[2] = "0"+data[2].trim();
                            }
                            String[] et = data[4].split(Pattern.quote("."));
                            String eh = et[0];
                            eh = eh.trim();
                            if(Integer.parseInt(eh)<=9)
                            {
                                data[4] = "0"+data[4].trim();
                            }
                        }
                        data[2] = data[2].trim();
                        data[4] = data[4].trim();

                        l = new Line(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                        db.insertLine(l);
                    }
                    catch (Exception e)
                    {
                        Log.e("Problem",e.toString());
                    }
                }
                db.adjust();
            }
            catch (IOException ex)
            {
                throw new RuntimeException("CORRUPTED TIMETABLE"+ex);
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"TIMETABLE NOT FOUND",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}