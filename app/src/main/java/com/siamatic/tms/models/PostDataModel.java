package com.siamatic.tms.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.siamatic.tms.configs.PTMSconfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/***
 * Class to manage save data to google sheet ex
 *
 *
 ***/
public class PostDataModel {
    private int machine_key;
    private String machine_name;
    private String  temp_value;
    private String insert_time;
    private String sType;
    private String ac_status;
    private String machine_ip;
    private int prob_no;
    private int chkOnline;
    private int chkSms;
    private int chkMail;
    private int chkLine;
    private String min_temp;
    private String max_temp;
    private float adj_temp;

    private String hospital_english_name;
    private String hospital_thai_name;

    private boolean isEnable;
    private int interval;

    public String getHospital_english_name() {
        return hospital_english_name;
    }

    public void setHospital_english_name(String hospital_english_name) {
        this.hospital_english_name = hospital_english_name;
    }

    public String getHospital_thai_name() {
        return hospital_thai_name;
    }

    public void setHospital_thai_name(String hospital_thai_name) {
        this.hospital_thai_name = hospital_thai_name;
    }

    private static boolean isChanged;
    private static SharedPreferences postDataShare;

    public int getMachine_key() {
        return machine_key;
    }

    public void setMachine_key(int machine_key) {
        this.machine_key = machine_key;
    }

    public String getMachine_name() {
        return machine_name;
    }

    public void setMachine_name(String machine_name) {
        this.machine_name = machine_name;
    }

    public String getTemp_value() {
        return temp_value;
    }

    public void setTemp_value(String temp_value) {
        this.temp_value = temp_value;
    }

    public String getInsert_time() {
        return insert_time;
    }

    public void setInsert_time(String insert_time) {
        this.insert_time = insert_time;
    }

    public String getsType() {
        return sType;
    }

    public void setsType(String sType) {
        this.sType = sType;
    }

    public String getAc_status() {
        return ac_status;
    }

    public void setAc_status(String ac_status) {
        this.ac_status = ac_status;
    }

    public String getMachine_ip() {
        return machine_ip;
    }

    public void setMachine_ip(String machine_ip) {
        this.machine_ip = machine_ip;
    }

    public int getProb_no() {
        return prob_no;
    }

    public void setProb_no(int prob_no) {
        this.prob_no = prob_no;
    }

    public int getChkOnline() {
        return chkOnline;
    }

    public void setChkOnline(int chkOnline) {
        this.chkOnline = chkOnline;
    }

    public int getChkSms() {
        return chkSms;
    }

    public void setChkSms(int chkSms) {
        this.chkSms = chkSms;
    }

    public int getChkMail() {
        return chkMail;
    }

    public void setChkMail(int chkMail) {
        this.chkMail = chkMail;
    }

    public int getChkLine() {
        return chkLine;
    }

    public void setChkLine(int chkLine) {
        this.chkLine = chkLine;
    }

    public String getMin_temp() {
        return min_temp;
    }

    public void setMin_temp(String min_temp) {
        this.min_temp = min_temp;
    }

    public String getMax_temp() {
        return max_temp;
    }

    public void setMax_temp(String max_temp) {
        this.max_temp = max_temp;
    }

    public float getAdj_temp() {
        return adj_temp;
    }

    public void setAdj_temp(float adj_temp) {
        this.adj_temp = adj_temp;
    }

    public void postData() {
        new SendRequest().execute();
    }

    public void postNameData() {
        new SendHospitalNameRequest().execute();
    }

    public void postRealTimeData(){
        new SendRealTimeTempRequest().execute();
    }

    public boolean isEnable(Context context) {
        postDataShare = context.getSharedPreferences("post_setting", Context.MODE_PRIVATE);
        return isEnable = postDataShare.getBoolean("postData", true);
    }

    public void setEnable(boolean enable, Context context) {
        isEnable = enable;
        postDataShare = context.getSharedPreferences("post_setting", Context.MODE_PRIVATE);
        //postDataShare = PreferenceManager.getDefaultSharedPreferences(context);
        postDataShare.edit().putBoolean("postData", enable).apply();
    }

    public int getInterval(Context context) {
        postDataShare = context.getSharedPreferences("post_setting", Context.MODE_PRIVATE);
        //postDataShare = PreferenceManager.getDefaultSharedPreferences(context);
        return interval = postDataShare.getInt("botInterval", 300000);
    }

    public void setInterval(int interval, Context context) {
        this.interval = interval;
        postDataShare = context.getSharedPreferences("post_setting", Context.MODE_PRIVATE);
        //postDataShare = PreferenceManager.getDefaultSharedPreferences(context);
        postDataShare.edit().putInt("botInterval", interval).apply();
    }

    public static boolean isChanged() {
        return isChanged;
    }

    public static void setChanged(boolean changed) {
        isChanged = changed;
    }

    public class SendRequest extends AsyncTask<String, Void, String> {
        protected void onPostExecute(){}

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(PTMSconfig.getUrlGScript());
                JSONObject postDataParams = new JSONObject();
                String id= PTMSconfig.getSheetId();

                postDataParams.put("input_type","GeneralRecord");
                postDataParams.put("machine_key",machine_key);
                postDataParams.put("machine_name",machine_name);
                postDataParams.put("temp_value",temp_value);
                postDataParams.put("insert_time",insert_time);
                postDataParams.put("sType",sType);
                postDataParams.put("ac_status",ac_status);
                postDataParams.put("machine_ip",machine_ip);
                postDataParams.put("prob_no",prob_no);
                postDataParams.put("chkOnline",chkOnline);
                postDataParams.put("chkSms",chkSms);
                postDataParams.put("chkMail",chkMail);
                postDataParams.put("chkLine",chkLine);
                postDataParams.put("min_temp",min_temp);
                postDataParams.put("max_temp",max_temp);
                postDataParams.put("adj_temp",adj_temp);
                postDataParams.put("id",id);

                //Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //Log.e("params",conn.toString());

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    //Log.e("params",sb.toString());
                    return sb.toString();
                }
                else {
                    return new String("false : "+responseCode);
                }
            }catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }
    }

    public class SendHospitalNameRequest extends AsyncTask<String, Void, String> {
        protected void onPostExecute(){}

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(PTMSconfig.getUrlGScript());
                JSONObject postDataParams = new JSONObject();

                String id= PTMSconfig.getSheetId();

                postDataParams.put("input_type","HospitalName");
                postDataParams.put("hospital_english_name",hospital_english_name);
                postDataParams.put("hospital_thai_name",hospital_thai_name);
                postDataParams.put("id",id);

                //Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //Log.e("params",conn.toString());

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    Log.e("params",sb.toString());
                    return sb.toString();
                }
                else {
                    return new String("false : "+responseCode);
                }
            }catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }
    }

    public class SendRealTimeTempRequest extends AsyncTask<String, Void, String> {
        protected void onPostExecute(){}

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(PTMSconfig.getUrlGScript());
                JSONObject postDataParams = new JSONObject();
                String id= PTMSconfig.getSheetId();

                postDataParams.put("input_type","RealTimeRecord");
                postDataParams.put("machine_key",machine_key);
                postDataParams.put("machine_name",machine_name);
                postDataParams.put("temp_value",temp_value);
                postDataParams.put("insert_time",insert_time);
                postDataParams.put("sType",sType);
                postDataParams.put("ac_status",ac_status);
                postDataParams.put("id",id);

                //Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //Log.e("params",conn.toString());

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    Log.e("params",sb.toString());
                    return sb.toString();
                }
                else {
                    return new String("false : "+responseCode);
                }
            }catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
            //Log.e("params",String.valueOf(result));
        }
        return result.toString();
    }
}
