package neon.ovis;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity
{
    private LinearLayout dayTimetable;
    private ScrollView scrv;
    private String TAG = "";
    private ListView listView;
    private static String filePath = "/sdcard/Download/plan.csv";
    private TDB db;
    ProgressDialog progressDialog;
    private String progTitle = "Refresh";
    private String progMessage = "We are refreshing your timetable";
    private ArrayList<Line> lines;
    private ArrayList<Line> classes;
    private boolean alarmON;
    private boolean processed = false;
    private InsertTask insertTask;
    private long delay = TimeUnit.DAYS.toMillis(3);
    //private long delay = TimeUnit.MINUTES.toMillis(3);
    private int cpt = 0;
    private Handler handler;
    private String id;
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            ScrollView scrv = findViewById(R.id.scrv);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setTitle(R.string.title_home);
                    scrv.setVisibility(View.GONE);
                    dayTimetable.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    setTitle(R.string.title_dashboard);
                    scrv.setVisibility(View.VISIBLE);
                    dayTimetable.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_notifications:
                    setTitle(R.string.title_notifications);
                    scrv.setVisibility(View.GONE);
                    dayTimetable.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = new TDB(this);
        lines = getIntent().getParcelableArrayListExtra("Lines");
        dayTimetable = findViewById(R.id.dayTimetable);
        dayTimetable.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.dayTimetablelv);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        id = settings.getString("UO",""); //Stored UO

        handler = new Handler();
        startRefreshTask();

        /*
        insertTask = new InsertTask(this);
        insertTask.execute();
        */

        CardView myWeek = findViewById(R.id.myweek);
        myWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,BasicActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Lines",lines);
                intent.putExtras(bundle);
                HomeActivity.this.startActivity(intent);
            }
        });


        /* -------------- TODAY DATE ----------- */

        /*
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        final String[] date = {" "+today.monthDay+"/"+(today.month+1)+"/"+today.year};
        */

        final String[] date = {" 09/04/2018"};

        ImageButton prev = findViewById(R.id.prevButton);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] val = date[0].trim().split(Pattern.quote("/"));
                int d = Integer.valueOf(val[0]);
                int m = Integer.valueOf(val[1]);
                int y = Integer.valueOf(val[2]);
                Calendar fd = Calendar.getInstance();
                fd.set(Calendar.DAY_OF_MONTH, d);
                fd.set(Calendar.MONTH, m-1);
                fd.set(Calendar.YEAR, y);
                fd.add(Calendar.DATE, -1);
                d = fd.get(Calendar.DAY_OF_MONTH);
                m = fd.get(Calendar.MONTH)+1;
                y = fd.get(Calendar.YEAR);
                String sd = " " + String.format("%02d",d) + "/" + String.format("%02d",m) + "/" + y;

                TextView title = findViewById(R.id.titleHome);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY");
                String currentDate = format.format(Calendar.getInstance().getTime());

                date[0] = sd;

                if(sd.trim().equals(currentDate.trim()))
                {
                    sd = "TODAY";
                }
                title.setText(sd.trim());

                classes = new ArrayList<>();
                Line l;
                for (int i=0; i<lines.size(); i++)
                {
                    l = lines.get(i);
                    if(l.getStartDate().trim().equals(date[0].trim()))
                    {
                        classes.add(l);
                    }
                }

                Collections.sort(classes, Line.sort);

                DayAdapter adapter = new DayAdapter(HomeActivity.this, R.layout.activity_home_timetable_item, classes);
                listView.setAdapter(adapter);
            }
        });

        final ImageButton next = findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"cpt = "+cpt,Toast.LENGTH_SHORT).show();
                String[] val = date[0].trim().split(Pattern.quote("/"));
                int d = Integer.valueOf(val[0]);
                int m = Integer.valueOf(val[1]);
                int y = Integer.valueOf(val[2]);
                Calendar fd = Calendar.getInstance();
                fd.set(Calendar.DAY_OF_MONTH, d);
                fd.set(Calendar.MONTH, m-1);
                fd.set(Calendar.YEAR, y);
                fd.add(Calendar.DATE, 1);
                d = fd.get(Calendar.DAY_OF_MONTH);
                m = fd.get(Calendar.MONTH)+1;
                y = fd.get(Calendar.YEAR);
                String sd = " " + String.format("%02d",d) + "/" + String.format("%02d",m) + "/" + y;

                TextView title = findViewById(R.id.titleHome);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY");
                String currentDate = format.format(Calendar.getInstance().getTime());

                date[0] = sd;

                if(sd.trim().equals(currentDate.trim()))
                {
                    sd = "TODAY";
                }
                title.setText(sd.trim());

                classes = new ArrayList<>();
                Line l;
                for (int i=0; i<lines.size(); i++)
                {
                    l = lines.get(i);
                    if(l.getStartDate().trim().equals(date[0].trim()))
                    {
                        classes.add(l);
                    }
                }

                Collections.sort(classes, Line.sort);

                DayAdapter adapter = new DayAdapter(HomeActivity.this, R.layout.activity_home_timetable_item, classes);
                listView.setAdapter(adapter);

            }
        });


        classes = new ArrayList<>();
        Line l;
        for (int i=0; i<lines.size(); i++)
        {
            l = lines.get(i);
            if(l.getStartDate().trim().equals(date[0].trim()))
            {
                classes.add(l);
            }
        }
        Collections.sort(classes, Line.sort);

        DayAdapter adapter = new DayAdapter(this, R.layout.activity_home_timetable_item, classes);
        listView.setAdapter(adapter);

        CardView signout = findViewById(R.id.disconnect);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().apply();
                HomeActivity.this.finish();
                Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                HomeActivity.this.startActivity(intent);
                HomeActivity.this.finish();
            }
        });

        final CardView alarm = findViewById(R.id.alarm);
        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ImageView[] child = new ImageView[1];
                final View[] line = new View[1];

                if(alarmON)
                {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setIcon(android.R.drawable.ic_lock_idle_alarm)
                            .setTitle("Alarms OFF ")
                            .setMessage("Are you sure you want to set off the alarms ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alarmON = !alarmON;

                                    child[0] = (ImageView) findViewById(R.id.alarmIcon);
                                    child[0].setBackgroundResource(R.drawable.cerclegris);
                                    child[0].setImageResource(R.drawable.ic_alarm_off);
                                    line[0] = findViewById(R.id.alarmLine);
                                    String[] colors = getResources().getStringArray(R.array.colors);
                                    int color = Color.parseColor(colors[16]);
                                    line[0].setBackgroundColor(color);

                                    AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                                    Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                    alarmMgr.cancel(pendingIntent);
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setIcon(android.R.drawable.ic_lock_idle_alarm)
                            .setTitle("Alarms ON ")
                            .setMessage("Are you sure you want to set on the alarms ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alarmON = !alarmON;

                                    child[0] = findViewById(R.id.alarmIcon);
                                    child[0].setBackgroundResource(R.drawable.cerclebleu);
                                    child[0].setImageResource(R.drawable.ic_alarm);
                                    line[0] = findViewById(R.id.alarmLine);
                                    String[] colors = getResources().getStringArray(R.array.colors);
                                    int color = Color.parseColor(colors[1]);
                                    line[0].setBackgroundColor(color);

                                    progressDialog = new ProgressDialog(HomeActivity.this);
                                    progressDialog.setMessage(progMessage); // Setting Message
                                    progressDialog.setTitle(progTitle); // Setting Title
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();

                                    AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                                    Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
                                    intent.putExtra("Title","Est.S.1");
                                    intent.putExtra("Content", "Class starts in 5 minutes");
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


                                    Date lineDate = new Date(2018,5,28,4,59);
                                    Date currentDate = new Date();
                                    if(lineDate.after(currentDate))
                                    {
                                        Calendar cal = Calendar.getInstance();

                                        cal.setTimeInMillis(System.currentTimeMillis());
                                        cal.clear();
                                        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                                        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
                                        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
                                        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
                                        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                                        cal.add(Calendar.MINUTE, 1);
                                        //cal.set(2018,4,28,4,59);
                                        alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                                    }

                                    /*
                                    for(Line l : classes)
                                    {
                                        Date lineDate = l.sDate();
                                        Date currentDate = new Date();

                                        if(lineDate.after(currentDate))
                                        {
                                            Calendar cal = Calendar.getInstance();

                                            cal.setTimeInMillis(System.currentTimeMillis());
                                            cal.clear();
                                            cal.set(l.getYear(),l.getMonth()-1,l.dayOfMonth(),l.getHour(),l.getMinute());
                                            alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                                        }
                                    }
                                    */
                                    progressDialog.dismiss();
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();

                }
            }
        });

        CardView searchDate = findViewById(R.id.seekDate);
        searchDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar myCalendar = Calendar.getInstance();

                DatePickerDialog.OnDateSetListener seekDate = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String date = " "+String.format("%02d",dayOfMonth)+"/"+String.format("%02d",monthOfYear+1)+"/"+year;

                        ArrayList<Line> classes = new ArrayList<>();
                        Line l;
                        for (int i=0; i<lines.size(); i++)
                        {
                            l = lines.get(i);
                            if(l.getStartDate().trim().equals(date.trim()))
                            {
                                classes.add(l);
                            }
                        }

                        Collections.sort(classes, Line.sort);

                        Intent intent = new Intent(HomeActivity.this,DayActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Lines", classes);
                        intent.putExtras(bundle);
                        HomeActivity.this.startActivity(intent);
                    }
                };

                new DatePickerDialog(HomeActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, seekDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        CardView refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestInternet it = new TestInternet();
                it.execute();
            }
        });
    }

    public class DayAdapter extends ArrayAdapter {

        private int resource;
        private LayoutInflater layoutInflater;
        private ArrayList<Line> lines;

        public DayAdapter(Context context, int resource, ArrayList<Line> objects) {
            super(context, resource, objects);
            this.resource = resource;
            this.lines = objects;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null)
            {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(resource,null);
                holder.ivLogo = convertView.findViewById(R.id.ivClassLetter);
                holder.tvSubject = convertView.findViewById(R.id.tvSubject);
                holder.tvTime = convertView.findViewById(R.id.tvTime);
                holder.tvLocation = convertView.findViewById(R.id.tvLocation);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            String[] classes = new String[lines.size()];
            String[] times = new String[lines.size()];
            String[] locations = new String[lines.size()];
            for(int i=0; i<lines.size(); i++)
            {
                classes[i] = lines.get(i).getSubject();
                times[i] = lines.get(i).getStartDate() + " - " + lines.get(i).getStartTime() + "\n" + " " +
                          lines.get(i).getEndDate() + " - " + lines.get(i).getEndTime();
                locations[i] = " " + lines.get(i).getLocation();
            }

            //holder.ivLogo.setOval(true);
            holder.ivLogo.setLetter(classes[position].charAt(0));
            holder.tvSubject.setText(classes[position]);
            holder.tvTime.setText(times[position]);
            holder.tvLocation.setText(locations[position]);

            return convertView;
        }

        class ViewHolder {
            private LetterImageView ivLogo;
            private TextView tvSubject;
            private TextView tvTime;
            private TextView tvLocation;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRefreshTask();
    }

    void insertFileData(String path) throws FileNotFoundException
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        processed = true;
    }

    /*
    private Boolean isOnline()	{
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null && ni.isConnected() && (ni.getType() == ConnectivityManager.TYPE_MOBILE || ni.getType() == ConnectivityManager.TYPE_WIFI))
            return true;

        return false;
    }
    */

    public Boolean isOnline() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
            if (urlc.getResponseCode() == 200) {
                return true;
            }
            else
            {
                return false;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public class InsertTask extends AsyncTask<String, Void, Boolean> {

        private Context context;

        public InsertTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(HomeActivity.this, "Refreshing database", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                insertFileData(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Toast.makeText(HomeActivity.this, "Database refreshed", Toast.LENGTH_LONG).show();
        }
    }

    class TestInternet extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(HomeActivity.this);
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
                progressDialog.dismiss();
                Toast.makeText(HomeActivity.this, "NETWORK UNAVAILABLE", Toast.LENGTH_LONG).show();
            } else { // code if connected
                if (!processed) {
                    Toast.makeText(HomeActivity.this, "REFRESHING", Toast.LENGTH_LONG).show();
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String SUO = settings.getString("UO",""); //Stored UO
                    String fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo="+SUO;
                    String saveDir = "/sdcard/Download";
                    final String[] params = new String[2];
                    params[0] = fileURL;
                    params[1] = saveDir;
                    try {
                        HttpDownloader.dl(params);
                        copyFileData(filePath);
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "REFESHED", Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try
            {
                final String fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo=" + id;
                String saveDir = "/sdcard/Download";
                final String[] params = new String[2];
                params[0] = fileURL;
                params[1] = saveDir;

                try
                {
                    HttpDownloader.dl(params);
                    copyFileData(filePath);
                    cpt++;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            finally {
                handler.postDelayed(mStatusChecker, delay);
            }
        }
    };

    void startRefreshTask()
    {
        mStatusChecker.run();
    }

    void stopRefreshTask()
    {
        handler.removeCallbacks(mStatusChecker);
    }
}
