package neon.ovis;

import android.app.Activity;
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
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
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

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

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
    private ListView listView;
    private ListView ntfLV;
    private LinearLayout ntfLayout;
    private static String filePath;
    private TDB db;
    ProgressDialog progressDialog;
    private String progTitle = "Refresh";
    private String progMessage = "We are refreshing your timetable";
    private ArrayList<Line> lines;
    private ArrayList<Line> classes;
    private boolean alarmON;
    private boolean processed = false;
    private InsertTask insertTask;
    private static long delay = TimeUnit.DAYS.toMillis(3);
    private static int alarmDelay = 5;
    private Handler handler;
    private String id;
    private NotificationManager mNotificationManager;
    private TestInternet it;
    private ArrayList<Notification> ntfs;

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
                    ntfLayout.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    setTitle(R.string.title_dashboard);
                    scrv.setVisibility(View.VISIBLE);
                    dayTimetable.setVisibility(View.GONE);
                    ntfLayout.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_notifications:
                    setTitle(R.string.title_notifications);
                    scrv.setVisibility(View.GONE);
                    dayTimetable.setVisibility(View.GONE);
                    ntfLayout.setVisibility(View.VISIBLE);
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
        ntfLayout = findViewById(R.id.myNtf);
        listView = findViewById(R.id.dayTimetablelv);
        ntfLV = findViewById(R.id.ntflv);
        ntfs = db.getNotifications();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        if(externalMemoryAvailable(this))
        {
            filePath = "/sdcard/Download/plan.csv";
        }
        else
        {
            filePath = getDir("plan",MODE_PRIVATE).getPath()+"/plan.csv";
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        id = settings.getString("UO",""); //Stored UO

        Common.currentToken = FirebaseInstanceId.getInstance().getToken();

        /* -------------- TODAY DATE ----------- */


        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        final String[] date = {" "+String.format("%02d",today.monthDay)+"/"+String.format("%02d",today.month+1)+"/"+today.year};


        //final String[] date = {" 09/04/2018"};

        handler = new Handler();
        startRefreshTask(date[0]);
        startNowRefresh();


        insertTask = new InsertTask(this);
        insertTask.execute();

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

        NotificationAdapter ntfAdapter = new NotificationAdapter(this, R.layout.activity_home_notification_item, ntfs);
        ntfLV.setAdapter(ntfAdapter);

        startNtfsref();

        CardView signout = findViewById(R.id.disconnect);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setIcon(R.drawable.ic_signout_black)
                        .setTitle("Sign out")
                        .setMessage("Are you sure you want to sign out ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().apply();
                                        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                                        for (int i=0; i<classes.size(); i++)
                                        {
                                            Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
                                            PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                            alarmMgr.cancel(pendingIntent);
                                        }
                                        HomeActivity.this.finish();
                                        Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                                        HomeActivity.this.startActivity(intent);
                                        HomeActivity.this.finish();
                                    }
                                })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        final CardView settingsCard = findViewById(R.id.settings);
        settingsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final CardView syncCard = findViewById(R.id.sync);
        syncCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Lines", lines);
                intent.putExtras(bundle);
                HomeActivity.this.startActivity(intent);
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
                                    for (int i=0; i<classes.size(); i++)
                                    {
                                        Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                        alarmMgr.cancel(pendingIntent);
                                    }
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

                                    for(int i=0; i<classes.size(); i++)
                                    {
                                        Line l = classes.get(i);
                                        Date lineDate = l.sDate();
                                        Date currentDate = new Date();

                                        if(lineDate.after(currentDate))
                                        {
                                            Calendar cal = Calendar.getInstance();

                                            cal.setTimeInMillis(System.currentTimeMillis());
                                            cal.clear();
                                            cal.set(l.getYear(),l.getMonth()-1,l.dayOfMonth(),l.getHour(),l.getMinute()-alarmDelay,0);

                                            Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
                                            intent.putExtra("Title",l.getSubject());
                                            intent.putExtra("Content", "Starting in " + alarmDelay + " minutes. ("+l.getLocation().trim()+")");
                                            PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                            alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                                        }
                                    }

                                    if(progressDialog.isShowing())
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
                        bundle.putSerializable("Classes", lines);
                        bundle.putSerializable("CurrentDate", date);
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
                new AlertDialog.Builder(HomeActivity.this)
                        .setIcon(R.drawable.ic_refresh_black)
                        .setTitle("Refresh")
                        .setMessage("Are you sure you want to refresh data ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                it = new TestInternet();
                                it.execute();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
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

            int now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int c = lines.get(position).getHour();

            if(c == now)
            {
                convertView.setBackgroundColor(getResources().getColor(R.color.test));
                holder.tvSubject.setTypeface(null, Typeface.BOLD);
                holder.tvTime.setTypeface(null, Typeface.BOLD);
                holder.tvLocation.setTypeface(null, Typeface.BOLD);
            }

            return convertView;
        }

        class ViewHolder {
            private LetterImageView ivLogo;
            private TextView tvSubject;
            private TextView tvTime;
            private TextView tvLocation;
        }
    }

    public class NotificationAdapter extends ArrayAdapter
    {
        private int resource;
        private LayoutInflater layoutInflater;
        private ArrayList<Notification> lines;

        public NotificationAdapter(Context context, int resource, ArrayList<Notification> objects) {
            super(context, resource, objects);
            this.resource = resource;
            this.lines = objects;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            NotificationAdapter.ViewHolder holder;
            if(convertView==null)
            {
                holder = new NotificationAdapter.ViewHolder();
                convertView = layoutInflater.inflate(resource,null);
                holder.ivLogo = convertView.findViewById(R.id.ivClassLetter);
                holder.Title = convertView.findViewById(R.id.Title);
                holder.Date = convertView.findViewById(R.id.Date);
                holder.Message = convertView.findViewById(R.id.Message);
                convertView.setTag(holder);
            }
            else
            {
                holder = (NotificationAdapter.ViewHolder) convertView.getTag();
            }

            String[] titles = new String[lines.size()];
            String[] dates = new String[lines.size()];
            String[] messages = new String[lines.size()];
            for(int i=0; i<lines.size(); i++)
            {
                titles[i] = lines.get(i).getTitle();
                dates[i] = lines.get(i).getDate();
                messages[i] = lines.get(i).getMsg();
            }

            //holder.ivLogo.setOval(true);
            holder.ivLogo.setLetter(titles[position].toUpperCase().charAt(0));
            holder.Title.setText(titles[position]);
            holder.Date.setText(dates[position]);
            holder.Message.setText(messages[position]);

            return convertView;
        }


        class ViewHolder {
            private LetterImageView ivLogo;
            private TextView Title;
            private TextView Date;
            private TextView Message;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        final String[] date = {" "+String.format("%02d",today.monthDay)+"/"+String.format("%02d",today.month+1)+"/"+today.year};
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
        NotificationAdapter ntfAdapter = new NotificationAdapter(this, R.layout.activity_home_notification_item, ntfs);
        ntfLV.setAdapter(ntfAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRefreshTask();
        stopRefNtfs();
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

    ArrayList<Line> copyFileData(String path) throws FileNotFoundException
    {
        ArrayList<Line> res = new ArrayList<>();
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
                        res.add(l);
                    }
                    catch (Exception e)
                    {
                        Log.e("Problem",e.toString());
                    }
                }
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
        return res;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        processed = true;
    }

    public class InsertTask extends AsyncTask<String, Void, Boolean> {

        private Context context;

        public InsertTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toast.makeText(HomeActivity.this, "Refreshing database", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                copyFileData(filePath);
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
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "REFRESHING", Toast.LENGTH_LONG).show();
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String SUO = settings.getString("UO",""); //Stored UO
                    final String fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo="+SUO;
                    //String saveDir = "/sdcard/Download";
                    final String saveDir;
                    if(externalMemoryAvailable(HomeActivity.this))
                    {
                        saveDir = "/sdcard/Download";
                    }
                    else {
                        saveDir = getDir("plan",MODE_PRIVATE).getPath();
                    }
                    final String[] params = new String[2];
                    params[0] = fileURL;
                    params[1] = saveDir;
                    try {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try
                                {
                                    HttpDownloadUtility.downloadFile(fileURL, saveDir);
                                }catch (IOException e)
                                {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(progressDialog != null && progressDialog.isShowing())
                                                progressDialog.dismiss();
                                            Toast.makeText(HomeActivity.this, "INCORRECT UO", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
                        t.start();
                        t.join();
                        copyFileData(filePath);
                        insertFileData(filePath);
                        Toast.makeText(HomeActivity.this, "Database refreshed", Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
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
                //String saveDir = "/sdcard/Download";
                final String saveDir;
                if(externalMemoryAvailable(HomeActivity.this))
                {
                    saveDir = "/sdcard/Download";
                }
                else {
                    saveDir = getDir("plan",MODE_PRIVATE).getPath();
                }

                final String[] params = new String[2];
                params[0] = fileURL;
                params[1] = saveDir;
                ArrayList<Line> res = new ArrayList();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpDownloadUtility.downloadFile(fileURL, saveDir);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(progressDialog != null && progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    Toast.makeText(HomeActivity.this, "INCORRECT UO", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

                try
                {
                    t.start();
                    t.join();
                    try {
                        if(!copyFileData(filePath).isEmpty())
                        {
                            res = copyFileData(filePath);
                            lines = res;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            finally {
                handler.postDelayed(mStatusChecker, delay);
            }
        }
    };

    Runnable refNtfs = new Runnable() {
        @Override
        public void run() {
            ntfs = db.getNotifications();
            NotificationAdapter ntfAdapter = new NotificationAdapter(HomeActivity.this, R.layout.activity_home_notification_item, ntfs);
            ntfLV.setAdapter(ntfAdapter);
            handler.postDelayed(mStatusChecker, TimeUnit.SECONDS.toMillis(30));
        }
    };

    void startNtfsref()
    {
        refNtfs.run();
    }

    void stopRefNtfs()
    {
        handler.removeCallbacks(refNtfs);
    }

    Runnable mNowChecker = new Runnable() {
        @Override
        public void run() {
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            final String[] date = {" "+String.format("%02d",today.monthDay)+"/"+String.format("%02d",today.month+1)+"/"+today.year};
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
            handler.postDelayed(mStatusChecker, TimeUnit.MINUTES.toMillis(30));
        }
    };

    void startNowRefresh()
    {
        mNowChecker.run();
    }

    void stopNowRefresh()
    {
        handler.removeCallbacks(mNowChecker);
    }

    void startRefreshTask(String date)
    {
        mStatusChecker.run();
        Line l;
        for (int i=0; i<lines.size(); i++)
        {
            l = lines.get(i);
            if(l.getStartDate().trim().equals(date))
            {
                classes.add(l);
            }
        }
    }

    void stopRefreshTask()
    {
        handler.removeCallbacks(mStatusChecker);
    }

    public static boolean externalMemoryAvailable(Activity context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        if (storages.length > 1 && storages[0] != null && storages[1] != null)
            return true;
        else
            return false;
    }
}
