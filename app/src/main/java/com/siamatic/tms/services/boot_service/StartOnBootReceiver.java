package com.siamatic.tms.services.boot_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartOnBootReceiver extends BroadcastReceiver {
  // ดักจับ Intent ที่ส่งมาจากระบบ
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("RebootApp", "Received intent: " + intent.getAction());

    // ถ้าเจอ Action การ Boot
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
            || "android.intent.action.QUICKBOOT_COMPLETED".equals(intent.getAction())) {

      Log.d("RebootApp", "Boot completed detected! Starting BootStartService");

      // เรียกคลาสช่วย Boot
      Intent serviceIntent = new Intent(context, BootStartService.class);
      context.startService(serviceIntent);
    }
  }
}

