package com.siamatic.tms.services.boot_service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.siamatic.tms.MainActivity;

// ตัวช่วยในการเปิดแอปหลังจากแท็บเล็ต Reboot เครื่อง หรือ เครื่องเปิด/ปิด ใหม่
public class BootStartService extends IntentService {
  public BootStartService() {
    super("BootStartService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d("RebootApp", "BootStartService triggered - launching MainActivity");

    // เรียก MainActivity ขึ้นมาใหม่ตอน Boot เสร็จ
    Intent activityIntent = new Intent(this, MainActivity.class);
    // เพิ่มเป็น FLAG ใหม่
    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // เริ่ม Activity
    startActivity(activityIntent);
    
    stopSelf();
  }
}
