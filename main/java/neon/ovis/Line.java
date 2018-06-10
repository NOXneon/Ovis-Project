package neon.ovis;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

public class Line implements Parcelable
{
    private String id;
    private String subject;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String description;
    private String location;

    public Line(String subject, String startDate, String startTime, String endDate, String endTime, String description, String location)
    {
        super();
        this.id = null;
        this.subject = subject;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.description = description;
        this.location = location;
    }

    public Line(String id, String subject, String startDate, String startTime, String endDate, String endTime, String description, String location)
    {
        super();
        this.id = id;
        this.subject = subject;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.description = description;
        this.location = location;
    }

    protected Line(Parcel in) {
        id = in.readString();
        subject = in.readString();
        startDate = in.readString();
        startTime = in.readString();
        endDate = in.readString();
        endTime = in.readString();
        description = in.readString();
        location = in.readString();
    }

    public static final Creator<Line> CREATOR = new Creator<Line>() {
        @Override
        public Line createFromParcel(Parcel in) {
            return new Line(in);
        }

        @Override
        public Line[] newArray(int size) {
            return new Line[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int dayOfMonth()
    {
        String date = getStartDate().trim();
        String[] val = date.split(Pattern.quote("/"));
        int day = Integer.parseInt(val[0]);
        return  day;
    }

    public int getMonth()
    {
        String date = getStartDate().trim();
        String[] val = date.split(Pattern.quote("/"));
        int month = Integer.parseInt(val[1]);
        return  month;
    }

    public int getYear()
    {
        String date = getStartDate().trim();
        String[] val = date.split(Pattern.quote("/"));
        int year = Integer.parseInt(val[2]);
        return  year;
    }

    public int getHour()
    {
        String date = getStartTime().trim();
        String[] val = date.split(Pattern.quote("."));
        int hour = Integer.parseInt(val[0]);
        return  hour;
    }

    public int getMinute()
    {
        String date = getStartTime().trim();
        String[] val = date.split(Pattern.quote("."));
        int minute = Integer.parseInt(val[1]);
        return  minute;
    }

    public int EndDayOfMonth()
    {
        String date = getEndDate().trim();
        String[] val = date.split(Pattern.quote("/"));
        int day = Integer.parseInt(val[0]);
        return  day;
    }

    public int getEndMonth()
    {
        String date = getStartDate().trim();
        String[] val = date.split(Pattern.quote("/"));
        int month = Integer.parseInt(val[1]);
        return  month;
    }

    public int getEndYear()
    {
        String date = getEndDate().trim();
        String[] val = date.split(Pattern.quote("/"));
        int year = Integer.parseInt(val[2]);
        return  year;
    }

    public Calendar getSTime()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, getHour());
        cal.set(Calendar.MINUTE, getMinute());
        return cal;
    }

    public int getEndHour()
    {
        String date = getEndTime().trim();
        String[] val = date.split(Pattern.quote("."));
        int hour = Integer.parseInt(val[0]);
        return  hour;
    }

    public int getEndMinute()
    {
        String date = getEndTime().trim();
        String[] val = date.split(Pattern.quote("."));
        int minute = Integer.parseInt(val[1]);
        return  minute;
    }

    public Date sDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(getYear(), getMonth()-1, dayOfMonth(), getHour(), getMinute(), 0);
        Date date = cal.getTime(); // get back a Date object
        return date;
    }

    public Date eDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(getEndYear(), getEndMonth()-1, EndDayOfMonth(), getEndHour(), getEndMinute(), 0);
        Date date = cal.getTime(); // get back a Date object
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(subject);
        dest.writeString(startDate);
        dest.writeString(startTime);
        dest.writeString(endDate);
        dest.writeString(endTime);
        dest.writeString(description);
        dest.writeString(location);
    }

    public static Comparator<Line> sort = new Comparator<Line>() {
        @Override
        public int compare(Line o1, Line o2) {
            int r = 0;
            Date d1 = o1.sDate();
            Date d2 = o2.sDate();

            if(d1.before(d2))
                r = -1;
            if(d1.after(d2))
                r = 1;

            return r;
        }
    };
}
