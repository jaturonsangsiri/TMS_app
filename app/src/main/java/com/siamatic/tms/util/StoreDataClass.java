package com.siamatic.tms.util;

import static com.siamatic.tms.constants.ConstantsKt.debugTag;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by YinG on 16-Mar-17.
 */
public class StoreDataClass {
    private static char mcu_id;                         //ID, bi_status, iTemp ถ้าไม่ใช้เป็น static ตอนแอพปิดและเปิดใหม่ ค่าเหล่านี้จะไม่ถูกบันทึก
    private static String bi_status;
    private static int iTemp;
    private static float fTemp;
    public int plug_status;

    private char[] ch;
    private StringBuilder SB;

    public float storeParameters(StringBuffer readData) {
        String temp;
        ch = readData.toString().toCharArray();
        SB = new StringBuilder();

        mcu_id = ch[1];
        char ch_status = ch[3];
        plug_status = (int) ch_status;
        bi_status = String.format("%8s", Integer.toBinaryString(ch_status & 0xFF)).replace(' ', '0');       //bi_status=00000000

        Log.d(debugTag, "Plug in status: " + plug_status);
        //MainFragment.plugInStatus(plug_status);
        //MainTabActivity.getACstate(plug_status);

        //Converts temperature at position 5+6
        for (int i = 5; i < ch.length - 2; i++) {
            temp = String.format("%02x", (int) ch[i]);
            Log.d(debugTag, "temp: " + temp);
            if (temp.length() == 4) {
                SB.append(temp.substring(2, 4));
            } else {
                SB.append(temp);
            }
        }

        Log.d(debugTag, "Hex String to parse: " + SB.toString());
        long iTemp = Long.parseLong(SB.toString(), 16);
        Log.d(debugTag, "iTemp: " + iTemp);

        if (readData.charAt(1) == 'A') {
            fTemp = (float) ((iTemp - 4000) * 0.01);
        } else if (readData.charAt(1) == 'E') {
            fTemp = (float) (-(iTemp * 0.01) - 48.24);
        }
        Log.d(debugTag, "fTemp: " + fTemp);

        return fTemp;
    }

    public void recordToDB (String RecordAdj, Context context) {
        DbClass dbHelper = new DbClass(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DbClass.MCU_ID, String.valueOf(mcu_id));
        cv.put(DbClass.STATUS, bi_status);
        if (RecordAdj != "") {
            cv.put(DbClass.TEMP_VALUE, RecordAdj);
        }
        //cv.put(myDBClass.TEMP_VALUE, RecordAdj);
        cv.put(DbClass.REAL_VALUE, iTemp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cv.put(DbClass.DATE, dateFormat.format(new Date()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        cv.put(DbClass.TIME, timeFormat.format(new Date()));
        db.insert(DbClass.TABLE_NAME, null, cv);
    }
}
