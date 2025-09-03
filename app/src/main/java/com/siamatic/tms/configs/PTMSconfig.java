package com.siamatic.tms.configs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class PTMSconfig {

//D:\12-Android\2.iTeMS2-V0.17.0.5 AoFzuza\app\src\main\java\com\siamatic\tms\ITMConfig\ITMconfig.java -
//    package com.siamatic.tms.ITMConfig;
//    public class ITMconfig {
//        public static final String PRODUCT = "ITM-1P-L0164-0364-08";
//    }


    //update 15-10-67
    // ตรวจสอบสถานะ internet ใข้ function นี้ตรวจสอบได้เเนื่องจากเป็นการส่งข้อมูลเข้า google sheet
    //  public void sendDataToSheet(Context context, boolean isOnline, String probeType, int probeNumber) {

    // ตรวจอสอบ ว่าการเรียกข้อมูลจากอุปกรณ์ทำงานไม่สำเร็จ ให้ restart app และตั้งเวลา time out
    //  private void onDataReceived(final byte[] buffer, final char[] data, final int size) {
    // update 06-11-67
    // check internet
    // add sendDataToServerHandler ส่งข้อมูลขึ้น host ทุก 5 นาท่
    // update 08-11-67
    // SendDataNotify Token To API
    // ตัดคำสั่ง GoogleSheet, Linnotify

    //11-11-67
    // sendAutoWebNotify


    //  0.17.0.5
    // 19-02-68
    // หยุดการทำงานของ sendDataRunnable เมื่อ Activity ถูกปิด
    // sendDataToServerHandler.removeCallbacks(sendDataRunnable);
    // isRunning = false; // ปิดการทำงานของ isRunning เพื่อไม่ให้ส่งข้อมูลอีก
    // new Resum
    // เพิ่มเก็บ error ที่ไฟล์ log

    //20-02-68
    // เปลี่ยนวิธี request เนื่องจาก
    // อาจะป็นที่ request ทำให้จอขาว เปลี่ยนไปใช้
    // timerRequest

    //21-02-68
    // แก้ไขชื่อไฟล์ build.apk เป็น dd-MM-yyyy HHmm
    // โหลดค่าจากไฟล์ config.xml


    //06-05-2568
    //Add 2 Probe


    private static String MACHINE_NAME_INIT;

    private static String PRODUCT;

    private static String URL_G_SCRIPT;
    private static String SHEET_ID;


    private static String NOTIFY_TOKEN;
    private static int MACHINE_KEY;

    private static String NOTIFY_TOKEN_API;
    private static String URL_API;


    private static String EMAIL_USERNAME;
    private static String EMAIL_PASSWORD;



    private static final String CONFIG_DIR = "config/"; // โฟลเดอร์สำหรับเก็บไฟล์ config.xml
    private static final String CONFIG_FILE_NAME = "config.xml"; // ชื่อไฟล์ config.xml

    public static void loadConfig(Context context) throws Exception {
        InputStream inputStream = null;
        try {
            // สร้างโฟลเดอร์ใน external storage หากยังไม่มี
            File configDir = new File(context.getExternalFilesDir(null), CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();  // สร้างโฟลเดอร์
            }

            // กำหนดตำแหน่งของไฟล์ config.xml
            File configFile = new File(configDir, CONFIG_FILE_NAME);

            if (configFile.exists()) {
                // ถ้าไฟล์ config.xml มีอยู่ใน external storage
                inputStream = new FileInputStream(configFile);
            } else {
                // ถ้าไม่พบไฟล์ config.xml ใน external storage ให้เปิดจาก assets
                inputStream = context.getAssets().open(CONFIG_FILE_NAME);
                if (inputStream == null) {
                    throw new FileNotFoundException("ไม่พบไฟล์ config.xml ทั้งใน assets และ external storage");
                }

                // หากต้องการเขียนค่าเริ่มต้นไปในไฟล์
                // writeDefaultConfigFile(configFile);
            }

            // แปลง XML เป็น Document object
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            // อ่านค่าจาก XML โดยใช้ tag name
            MACHINE_NAME_INIT = getValueByTagName(doc, "machine", "name");
            PRODUCT = getValueByTagName(doc, "machine", "product");
            MACHINE_KEY = Integer.parseInt(getValueByTagName(doc, "machine", "key"));

            URL_G_SCRIPT = getValueByTagName(doc, "google", "script-url");
            SHEET_ID = getValueByTagName(doc, "google", "sheet-id");

            NOTIFY_TOKEN = getValueByTagName(doc, "notification", "token");
            NOTIFY_TOKEN_API = getValueByTagName(doc, "notification", "api-token");

            URL_API = getValueByTagName(doc, "api", "url");

            EMAIL_USERNAME = getValueByTagName(doc, "email", "username");
            EMAIL_PASSWORD = getValueByTagName(doc, "email", "password");



        } catch (Exception e) {
            // แสดง dialog เมื่อเกิดข้อผิดพลาด
            showErrorDialog(context, "เกิดข้อผิดพลาดขณะโหลด config.xml: " + e.getMessage());
            // ขว้างข้อผิดพลาดให้กับ caller
            throw new Exception("เกิดข้อผิดพลาดขณะโหลด config.xml: " + e.getMessage(), e);
        } finally {
            // ปิด inputStream เพื่อป้องกันการรั่วไหลของทรัพยากร
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void showErrorDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("เกิดข้อผิดพลาด")
                .setMessage(message)
                .setCancelable(false) // ไม่ให้ผู้ใช้ปิดด้วยการกดข้างนอก
                .setPositiveButton("ปิดแอป", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ปิดแอปเมื่อผู้ใช้กดปุ่ม "ปิดแอป"
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .show();
    }
    // เมธอดช่วยสำหรับอ่านค่าจาก XML โดยใช้ tag name
    private static String getValueByTagName(Document doc, String parentTag, String childTag) {
        NodeList nodeList = doc.getElementsByTagName(parentTag);
        if (nodeList.getLength() > 0) {
            Element element = (Element) nodeList.item(0);
            return element.getElementsByTagName(childTag).item(0).getTextContent();
        }
        throw new RuntimeException("ไม่พบแท็ก " + parentTag + "/" + childTag + " ในไฟล์ config.xml");
    }

    // เมธอด getter สำหรับตัวแปร static
    public static String getMachineName() {
        return MACHINE_NAME_INIT;
    }

    public static String getPRODUCT() {
        return PRODUCT;
    }

    public static String getUrlGScript() {
        return URL_G_SCRIPT;
    }

    public static String getSheetId() {
        return SHEET_ID;
    }

    public static String getNotifyToken() {
        return NOTIFY_TOKEN;
    }

    public static int getMachineKey() { return MACHINE_KEY;  }
//    public static int getMachineKey2() {
//        return MACHINE_KEY_2;
//    }

    public static String getNotifyTokenApi() {
        return NOTIFY_TOKEN_API;
    }

    public static String getURL_API() {
        return URL_API;
    }


    public static String getEmailUsername() {
        return EMAIL_USERNAME;
    }

    public static String getEmailPassword() {
        return EMAIL_PASSWORD;
    }



}

