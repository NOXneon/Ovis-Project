package neon.ovis;

import android.graphics.Color;

import java.util.Random;
import java.util.regex.Pattern;

public class Line
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
        String date = getStartDate().trim();
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
        String date = getStartDate().trim();
        String[] val = date.split(Pattern.quote("/"));
        int year = Integer.parseInt(val[2]);
        return  year;
    }

    public int getEndHour()
    {
        String date = getStartTime().trim();
        String[] val = date.split(Pattern.quote("."));
        int hour = Integer.parseInt(val[0]);
        return  hour;
    }

    public int getEndMinute()
    {
        String date = getStartTime().trim();
        String[] val = date.split(Pattern.quote("."));
        int minute = Integer.parseInt(val[1]);
        return  minute;
    }
}
