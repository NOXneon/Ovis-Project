package neon.ovis;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "";
    private TDB db;
    private static String filePath;
    ProgressDialog progressDialog;
    private String progTitle = "Refresh";
    private String progMessage = "We are refreshing your timetable";
    private ArrayList<Line> lines;
    private boolean processed = false;
    private CheckBox remcb;
    private EditText ETUO;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        /*
        String AndroidVersion = android.os.Build.VERSION.RELEASE;
        Toast.makeText(LoginActivity.this, "Version : "+AndroidVersion, Toast.LENGTH_LONG).show();
        */

        lines = new ArrayList<>();
        db = new TDB(this);

        if(externalMemoryAvailable(this))
        {
            filePath = "/sdcard/Download/plan.csv";
        }
        else
        {
            filePath = getDir("plan",MODE_PRIVATE).getPath()+"/plan.csv";
        }

        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String SUO = settings.getString("UO",""); //Stored UO

        final CheckBox checkbox = findViewById(R.id.remcb);
        boolean bool = settings.getBoolean("remcb", true);

        if(!SUO.isEmpty())
        {
            TestInternet it = new TestInternet(SUO);
            it.execute();
        }

        ETUO = findViewById(R.id.UO); // EditTextUO
        Button logbutton = findViewById(R.id.logbutton); // Log in button
        remcb = findViewById(R.id.remcb); // Remember me cb

        ETUO.setText(SUO);

        checkbox.setChecked(bool);

        logbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uo = ETUO.getText().toString();
                if(!uo.isEmpty())
                { //correct
                    if(isStoragePermissionGranted())
                    {
                        TestInternet it = new TestInternet(uo.toUpperCase());
                        it.execute();
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

    @Override
    protected void onPause() {
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onDestroy();
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

    void copyFileData(String path) throws FileNotFoundException
    {
        Line l;
        String[] data;
        File file = new File(path);

        if (file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try
            {
                String csvLine;
                while ((csvLine = br.readLine()) != null)
                {
                    if(csvLine.isEmpty())
                        continue;

                    data=csvLine.split(",");
                    try
                    {
                        if(data[0].trim().equals("Subject") || data[0] == null || data[0].trim() == "")
                            continue;

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

                        data[2] = data[2].trim();
                        data[4] = data[4].trim();

                        l = new Line(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                        lines.add(l);
                    }
                    catch (Exception e)
                    {
                        Log.e("Problem : ",e.toString());
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        processed = true;
        super.onConfigurationChanged(newConfig);
    }

    class TestInternet extends AsyncTask<Void, Void, Boolean> {
        private String SUO;

        TestInternet(String s)
        {
            SUO = s.toUpperCase();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(progMessage); // Setting Message
            progressDialog.setTitle(progTitle); // Setting Title
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL("http://www.google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                }
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) { // code if not connected
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "NETWORK UNAVAILABLE", Toast.LENGTH_LONG).show();
            } else { // code if connected
                if (!processed) {
                    final String fileURL;
                    final String saveDir;
                    final String[] params = new String[2];
                    
                    if(externalMemoryAvailable(LoginActivity.this))
                    {
                        fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo=" + SUO.toUpperCase();
                        saveDir = "/sdcard/Download";
                        params[0] = fileURL;
                        params[1] = saveDir;
                    }
                    else
                    {
                        fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo=" + SUO.toUpperCase();
                        saveDir = getDir("plan",MODE_PRIVATE).getPath();
                        params[0] = fileURL;
                        params[1] = saveDir;
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                HttpDownloadUtility.downloadFile(fileURL, saveDir);
                                try {
                                    copyFileData(filePath);
                                    if(progressDialog != null && progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    settings.edit().putString("UO",SUO).apply();
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("Lines", lines);
                                    intent.putExtras(bundle);
                                    LoginActivity.this.startActivity(intent);
                                    LoginActivity.this.finish();
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            catch(IOException e)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(progressDialog != null && progressDialog.isShowing())
                                            progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "INCORRECT UO", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        }
    }

    public static boolean externalMemoryAvailable(Activity context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        if (storages.length > 1 && storages[0] != null && storages[1] != null)
            return true;
        else
            return false;
    }
}

