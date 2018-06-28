package neon.ovis;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.regex.Pattern;

public class DayActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Line> classes;
    private ArrayList<Line> lines;
    private String mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        listView = findViewById(R.id.dayTimetablelv);
        lines = new ArrayList<Line>();
        lines = getIntent().getParcelableArrayListExtra("Lines");
        classes = new ArrayList<Line>();
        classes = getIntent().getParcelableArrayListExtra("Classes");
        mDate = getIntent().getStringExtra("CurrentDate");
        TextView title = findViewById(R.id.titleHome);
        title.setText(lines.get(0).getStartDate());

        final ImageButton prev = findViewById(R.id.prevButton);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //String date = lines.get(0).getStartDate();
                    String date = mDate;
                    String[] val = date.trim().split(Pattern.quote("/"));
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

                    date = sd;
                    mDate = sd;

                    if(sd.trim().equals(currentDate.trim()))
                    {
                        sd = "TODAY";
                    }
                    title.setText(sd.trim());

                    lines.clear();

                    Line l;
                    for (int i = 0; i< classes.size(); i++)
                    {
                        l = classes.get(i);
                        if(l.getStartDate().trim().equals(date.trim()))
                        {
                            lines.add(l);
                        }
                    }


                    Collections.sort(lines, Line.sort);

                    DayActivity.DayAdapter adapter = new DayActivity.DayAdapter(DayActivity.this, R.layout.activity_day_timetable_item, lines);
                    listView.setAdapter(adapter);
            }
        });

        final ImageButton next = findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //String date = lines.get(0).getStartDate();
                    String date = mDate;
                    String[] val = date.trim().split(Pattern.quote("/"));
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

                    date = sd;
                    mDate = sd;

                    if(sd.trim().equals(currentDate.trim()))
                    {
                        sd = "TODAY";
                    }
                    title.setText(sd.trim());

                    lines.clear();
                    Line l;
                    for (int i = 0; i< classes.size(); i++)
                    {
                        l = classes.get(i);
                        if(l.getStartDate().trim().equals(date.trim()))
                        {
                            lines.add(l);
                        }
                    }

                    Collections.sort(lines, Line.sort);

                    DayActivity.DayAdapter adapter = new DayActivity.DayAdapter(DayActivity.this, R.layout.activity_day_timetable_item, lines);
                    listView.setAdapter(adapter);
            }
        });

        DayActivity.DayAdapter adapter = new DayActivity.DayAdapter(this, R.layout.activity_day_timetable_item, lines);
        listView.setAdapter(adapter);
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
            DayActivity.DayAdapter.ViewHolder holder;
            if(convertView==null)
            {
                holder = new DayActivity.DayAdapter.ViewHolder();
                convertView = layoutInflater.inflate(resource,null);
                holder.ivLogo = convertView.findViewById(R.id.ivClassLetter);
                holder.tvSubject = convertView.findViewById(R.id.tvSubject);
                holder.tvTime = convertView.findViewById(R.id.tvTime);
                holder.tvLocation = convertView.findViewById(R.id.tvLocation);
                convertView.setTag(holder);
            }
            else
            {
                holder = (DayActivity.DayAdapter.ViewHolder) convertView.getTag();
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

        class ViewHolder
        {
            private LetterImageView ivLogo;
            private TextView tvSubject;
            private TextView tvTime;
            private TextView tvLocation;
        }
    }
}
