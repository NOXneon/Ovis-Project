package neon.ovis;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class BasicActivity extends BaseActivity
{
    private ArrayList<Line> classes;
    private Dialog dialog;
    ImageView close;
    TextView title;
    TextView desc;
    Button ok;
    LetterImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        classes = getIntent().getParcelableArrayListExtra("Lines");
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_event);
        close = dialog.findViewById(R.id.closePopup);
        title = dialog.findViewById(R.id.titlePopup);
        icon = dialog.findViewById(R.id.iconPopup);
        desc = dialog.findViewById(R.id.descPopup);
        ok = dialog.findViewById(R.id.okPopup);
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {

    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with some events.
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        if(today.get(Calendar.MONTH) != newMonth)
            return new ArrayList<>();

        Calendar startTime;
        Calendar endTime;
        Line l;

        for(int i=0; i<classes.size(); i++)
        {
            startTime = Calendar.getInstance();
            l = classes.get(i);
            startTime.set(Calendar.HOUR_OF_DAY, l.getHour());
            startTime.set(Calendar.MINUTE, l.getMinute());
            startTime.set(Calendar.DAY_OF_MONTH, l.dayOfMonth());
            startTime.set(Calendar.MONTH, l.getMonth()-1);
            startTime.set(Calendar.YEAR, l.getYear());
            endTime = (Calendar) startTime.clone();
            endTime.set(Calendar.DAY_OF_MONTH, l.EndDayOfMonth());
            endTime.set(Calendar.MONTH, l.getEndMonth()-1);
            endTime.set(Calendar.YEAR, l.getEndYear());
            endTime.set(Calendar.HOUR_OF_DAY, l.getEndHour());
            endTime.set(Calendar.MINUTE, l.getEndMinute());
            WeekViewEvent event = new WeekViewEvent
                                    (
                                            (i+1),
                                            l.getSubject(),
                                            l.getYear(),
                                            l.getMonth(),
                                            l.dayOfMonth(),
                                            l.getHour(),
                                            l.getMinute(),
                                            l.getEndYear(),
                                            l.getEndMonth(),
                                            l.EndDayOfMonth(),
                                            l.getEndHour(),
                                            l.getEndMinute()
                                    );
            event.setColor(randomColor());
            event.setLocation(l.getLocation());
            events.add(event);
        }

        return events;
    }

    private int randomColor()
    {
        Random random = new Random();
        String[] colorsArr = getResources().getStringArray(R.array.colors);
        return Color.parseColor(colorsArr[random.nextInt(colorsArr.length)]);
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        title.setText(event.getName());
        icon.setLetter(event.getName().charAt(0));
        icon.getmBackgroundPaint().setColor(event.getColor());
        Calendar startTime = event.getStartTime();
        Calendar endTime = event.getEndTime();
        String sdm = String.valueOf(startTime.get(Calendar.DAY_OF_MONTH));
            if(startTime.get(Calendar.DAY_OF_MONTH)<=9)
                sdm = "0"+sdm;
        String sm = String.valueOf(startTime.get(Calendar.MONTH)+1);
            if(startTime.get(Calendar.MONTH)+1<=9)
                sm = "0"+sm;
        String sy = String.valueOf(startTime.get(Calendar.YEAR));
        if(startTime.get(Calendar.YEAR)<=9)
            sy = "0"+sy;
        String sh = String.valueOf(startTime.get(Calendar.HOUR_OF_DAY));
        if(startTime.get(Calendar.HOUR_OF_DAY)<=9)
            sh = "0"+sh;
        String smin = String.valueOf(startTime.get(Calendar.MINUTE));
        if(startTime.get(Calendar.MINUTE)<=9)
            smin = "0"+smin;
        String edm = String.valueOf(endTime.get(Calendar.DAY_OF_MONTH));
        if(endTime.get(Calendar.DAY_OF_MONTH)<=9)
            edm = "0"+edm;
        String em = String.valueOf(endTime.get(Calendar.MONTH)+1);
        if(endTime.get(Calendar.MONTH)+1<=9)
            em = "0"+em;
        String ey = String.valueOf(endTime.get(Calendar.YEAR));
        if(endTime.get(Calendar.YEAR)<=9)
            ey = "0"+ey;
        String eh = String.valueOf(endTime.get(Calendar.HOUR_OF_DAY));
        if(endTime.get(Calendar.HOUR_OF_DAY)<=9)
            eh = "0"+eh;
        String emin = String.valueOf(endTime.get(Calendar.MINUTE));
        if(endTime.get(Calendar.MINUTE)<=9)
            emin = "0"+emin;

        String s = "" + sdm + "/" + sm + "/" + sy + " - " + sh + ":" + smin;
        String e = "" + edm + "/" + em + "/" + ey + " - " + eh + ":" + emin;
        desc.setText(s + "\n" + e + "\n" + event.getLocation().trim());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month-1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }
}
