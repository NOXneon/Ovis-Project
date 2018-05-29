package neon.ovis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TDB extends SQLiteOpenHelper
{
    private static String DATABASE_NAME = "timetable.db";
    private static final int DATABASE_VERSION = 4;
    private SQLiteDatabase db;
    private static String TABLE_tb = "Timetable";
    private static String CREATE_TABLE_Tb = "CREATE TABLE "
            + TABLE_tb + " ("
            + "id integer PRIMARY KEY AUTOINCREMENT,"
            + "subject VARCHAR(250) NOT NULL,"
            + "startDate VARCHAR(250) NOT NULL,"
            + "startTime VARCHAR(250) NOT NULL,"
            + "endDate VARCHAR(250) NOT NULL,"
            + "endTime VARCHAR(250) NOT NULL,"
            + "description VARCHAR(250) NOT NULL,"
            + "location VARCHAR(250) NOT NULL"
            + ")";

    public TDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Tb);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_tb);
        onCreate(db);
    }

    public long insertLine(Line l)
    {
        ContentValues lineToInsert = new ContentValues();

        lineToInsert.put("subject",l.getSubject());
        lineToInsert.put("startDate",l.getStartDate());
        lineToInsert.put("startTime",l.getStartTime());
        lineToInsert.put("endDate",l.getEndDate());
        lineToInsert.put("endTime",l.getEndTime());
        lineToInsert.put("description",l.getDescription());
        lineToInsert.put("location",l.getLocation());

        return db.insert(TABLE_tb, null, lineToInsert);
    }

    public void clear()
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_tb);
        onCreate(db);
    }

    public void adjust()
    {
        this.getWritableDatabase().delete(TABLE_tb,"subject = '%ubject' ",null );
    }

    public ArrayList<Line> getClassesOf(String date)
    {
        ArrayList<Line> lines = new ArrayList<>();
        String[] string_line = new String[]{"id","subject", "startDate", "startTime", "endDate", "endTime", "description", "location"};

        Cursor cursorResults = db.query(false, TABLE_tb, string_line, "startDate='"+date+"'", null, null, null, "startTime", null, null);

        if( null!= cursorResults) {
            if (cursorResults.moveToFirst()) {
                do {
                    Line l = new Line(
                                        cursorResults.getString(0),
                                        cursorResults.getString(1),
                                        cursorResults.getString(2),
                                        cursorResults.getString(3),
                                        cursorResults.getString(4),
                                        cursorResults.getString(5),
                                        cursorResults.getString(6),
                                        cursorResults.getString(7)
                                     );
                    lines.add(l);
                } while (cursorResults.moveToNext());
            }
        }
        cursorResults.close();
        return lines;
    }

    public ArrayList<Line> recup_lines()
    {
        ArrayList<Line> lines = new ArrayList<>();
        String[] string_line = new String[]{"id","subject", "startDate", "startTime","endDate", "endTime", "description", "location"};

        Cursor cursorResults = db.query(false, TABLE_tb, string_line, null, null, null, null, "startDate, startTime, subject", null, null);

        if( null!= cursorResults) {
            if (cursorResults.moveToFirst()) {
                do {
                    Line l = new Line(
                                        cursorResults.getString(0),
                                        cursorResults.getString(1),
                                        cursorResults.getString(2),
                                        cursorResults.getString(3),
                                        cursorResults.getString(4),
                                        cursorResults.getString(5),
                                        cursorResults.getString(6),
                                        cursorResults.getString(7)
                                     );
                    lines.add(l);
                } while (cursorResults.moveToNext());
            }
        }
        cursorResults.close();
        return lines;
    }
}
