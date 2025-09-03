package com.siamatic.tms.util;

import static com.siamatic.tms.constants.ConstantsKt.debugTag;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by YinG on 15-Mar-17.
 */
public class SendMessageClass {
    private static float max;
    private static float min;
    private static String nameRefrig;
    private static boolean isCheckedLine;
    private static boolean isCheckedSMS;
    private static String pNumber1;
    private static String pNumber2;

    private static char mcu_id;                         //ID, bi_status, iTemp ถ้าไม่ใช้เป็น static ตอนแอพปิดและเปิดใหม่ ค่าเหล่านี้จะไม่ถูกบันทึก
    private static String bi_status;
    private static int iTemp;
    private static float fTemp;
    private int plug_status;

    private char[] ch;
    private StringBuilder SB;

    public float storeParameters(StringBuffer readData, Context context) {
        String temp;
        ch = readData.toString().toCharArray();
        SB = new StringBuilder();

        mcu_id = ch[1];
        char ch_status = ch[3];
        plug_status = (int) ch_status;
        //Toast.makeText(context, "bi = " + Integer.toBinaryString(3), Toast.LENGTH_SHORT).show();
        bi_status = String.format("%8s", Integer.toBinaryString(ch_status & 0xFF)).replace(' ', '0');       //bi_status=00000000
        Log.d(debugTag, "plug status: " + plug_status);
        // Main Page UI
        // MainFragment.plugInStatus(plug_status);

        //Converts temperature at position 5+6
        for (int i = 5; i < ch.length - 2; i++) {
            temp = String.format("%02x", (int) ch[i]);
            //temp = "0018";
            if (temp.length() == 4) {
                SB.append(temp.substring(2, 4));
            } else {
                SB.append(temp);
            }
        }

        //Toast.makeText(context,SB,Toast.LENGTH_LONG).show();

        iTemp = Integer.parseInt(String.valueOf(SB), 16);
        if (readData.charAt(1) == 'A') {
            fTemp = (float) ((iTemp - 4000) * 0.01);
        }else if (readData.charAt(1) == 'E') {
            fTemp = (float) (-(iTemp * 0.01) - 48.24);
        }

        return fTemp;
    }

    public void recordToDB(String RecordAdj, Context context) {
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd         HH:mm:ss");
        cv.put(DbClass.DATE, dateFormat.format(new Date()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        cv.put(DbClass.TIME, timeFormat.format(new Date()));
        db.insert(DbClass.TABLE_NAME, null, cv);
    }

    public static void settingFromFragment(float _max, float _min, String _nameRefrig) {
        max = _max;
        min = _min;
        nameRefrig = _nameRefrig;
    }

    public static void statusCheckBox(boolean cbStatusLine, boolean cbStatusSMS, String phoneNumber1, String phoneNumber2) {
        isCheckedLine = cbStatusLine;
        isCheckedSMS = cbStatusSMS;
        pNumber1 = phoneNumber1;
        pNumber2 = phoneNumber2;
    }
}
