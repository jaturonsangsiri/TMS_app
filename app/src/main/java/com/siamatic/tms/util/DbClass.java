package com.siamatic.tms.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Piyawalee on 25-Feb-16.
 */
public class DbClass extends SQLiteOpenHelper {

    private static final String DB_NAME = "DB_Temp_log";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "temp_log";
    public static final String ID = "_id";
    public static final String MCU_ID = "mcu_id";
    public static final String STATUS = "status";
    public static final String TEMP_VALUE = "temp_value";
    public static final String REAL_VALUE = "real_value";
    public static final String DATE = "date_time";
    public static final String TIME = "time";

    private static final String STRING_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MCU_ID + " TEXT, " + STATUS + " TEXT, " + TEMP_VALUE + " REAL, "
                    + REAL_VALUE + " INTEGER, " + DATE + " DATETIME, " + TIME + " INTEGER);";

    public DbClass(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(STRING_CREATE);
        ContentValues cv = new ContentValues();
        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy     HH:mm:ss");
        //SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        //cv.put(DATE, dateFormat.format(new Date()));
        //cv.put(TIME, timeFormat.format(new Date()));
        db.insert(TABLE_NAME, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);
    }

}
