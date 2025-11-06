package com.siamatic.tms.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.siamatic.tms.MainActivity;

public class BootStartService extends IntentService {
  public BootStartService() {
    super("BootStartService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d("RebootApp", "BootStartService triggered - launching MainActivity");

    Intent activityIntent = new Intent(this, MainActivity.class);
    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(activityIntent);

    stopSelf();
  }
}
