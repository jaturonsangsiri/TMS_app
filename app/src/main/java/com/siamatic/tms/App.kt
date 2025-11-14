package com.siamatic.tms

import android.app.Application
import androidx.work.Configuration
import android.util.Log

class App : Application(), Configuration.Provider {

  override fun onCreate() {
    super.onCreate()
    Log.d("AppInit", "WorkManager initialized manually.")
  }

  // optional: explicitly provide configuration
  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setMinimumLoggingLevel(Log.DEBUG)
      .build()
}