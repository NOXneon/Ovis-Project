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
    private static String TABLE_ntf = "Notification";
    private static String CREATE_TABLE_Ntf = "CREATE TABLE "
            + TABLE_ntf + " ( "
            + "id integer PRIMARY KEY AUTOINCREMENT,"
            + "title VARCHAR(250) NOT NULL,"
            + "msg VARCHAR(500) NOT NULL,"
            + "date VARCHAR(250) NOT NULL"
            + ")";

    public TDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Tb);
        db.execSQL(CREATE_TABLE_Ntf);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_tb);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ntf);
        onCreate(db);
    }

    public long insertLine(Line l)
    {
        ContentValues lineToInsert = new ContentValues();
        long r = 0;

        db.beginTransaction();
        try
        {
            lineToInsert.put("subject",l.getSubject());
            lineToInsert.put("startDate",l.getStartDate());
            lineToInsert.put("startTime",l.getStartTime());
            lineToInsert.put("endDate",l.getEndDate());
            lineToInsert.put("endTime",l.getEndTime());
            lineToInsert.put("description",l.getDescription());
            lineToInsert.put("location",l.getLocation());
            r = db.insert(TABLE_tb, null, lineToInsert);
            db.setTransactionSuccessful();
        }
        catch (Exception e) {e.printStackTrace();}

        finally
        {
            db.endTransaction();
        }

        return r;
    }

    public long insertNtf(Notification n)
    {
        ContentValues lineToInsert = new ContentValues();
        long r = 0;

        db.beginTransaction();
        try
        {
            lineToInsert.put("title",n.getTitle());
            lineToInsert.put("msg",n.getMsg());
            lineToInsert.put("date",n.getDate());
            r = db.insert(TABLE_ntf, null, lineToInsert);
            db.setTransactionSuccessful();
        }
        catch (Exception e) {e.printStackTrace();}

        finally
        {
            db.endTransaction();
        }

        return r;
    }

    public void clear()
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_tb);
        db.execSQL(CREATE_TABLE_Tb);
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

    public ArrayList<Notification> getNotifications()
    {
        ArrayList<Notification> lines = new ArrayList<>();
        String[] string_line = new String[]{"id","title","msg","date"};

        Cursor cursor = db.query(false, TABLE_ntf, string_line, null, null, null, null, "date DESC",null);

        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                do {
                    Notification n = new Notification(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                    lines.add(n);
                }
                while (cursor.moveToNext());
            }
        }
        cursor.close();
        return lines;
    }
}
