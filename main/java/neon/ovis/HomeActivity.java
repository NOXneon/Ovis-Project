package neon.ovis;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout dashfunc;
    private LinearLayout dayTimetable;
    private ListView listView;
    private static String filePath = "/sdcard/Download/plan.csv";
    private TDB db;
    ProgressDialog progressDialog;
    private String progTitle = "Refresh";
    private String progMessage = "We are refreshing your timetable";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setTitle(R.string.title_home);
                    dashfunc.setVisibility(View.GONE);
                    dayTimetable.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    setTitle(R.string.title_dashboard);
                    dashfunc.setVisibility(View.VISIBLE);
                    dayTimetable.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_notifications:
                    setTitle(R.string.title_notifications);
                    dashfunc.setVisibility(View.GONE);
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
        dashfunc = findViewById(R.id.dashfunc);
        dayTimetable = findViewById(R.id.dayTimetable);
        dayTimetable.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.dayTimetablelv);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        CardView myWeek = findViewById(R.id.myweek);
        myWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,WeekActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });
        /*
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        String date = " "+today.monthDay+"/"+today.month+"/"+today.year;
        */

        String date = " 09/04/2018";
        ArrayList<Line> lines = db.getClassesOf(date);
        //ArrayList<Line> test = db.recup_lines();
        DayAdapter adapter = new DayAdapter(this, R.layout.activity_home_timetable_item, lines);
        listView.setAdapter(adapter);

        CardView signout = findViewById(R.id.disconnect);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear preferences
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().apply();
                HomeActivity.this.finish();
                Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                HomeActivity.this.startActivity(intent);
                HomeActivity.this.finish();
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
                    }

                };

                new DatePickerDialog(HomeActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, seekDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();



                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                HomeActivity.this.startActivity(intent);
                HomeActivity.this.finish();
            }
        });

        CardView refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(HomeActivity.this);
                progressDialog.setMessage(progMessage); // Setting Message
                progressDialog.setTitle(progTitle); // Setting Title
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String SUO = settings.getString("UO",""); //Stored UO
                String fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo="+SUO;
                String saveDir = "/sdcard/Download";
                final String[] params = new String[2];
                params[0] = fileURL;
                params[1] = saveDir;

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
