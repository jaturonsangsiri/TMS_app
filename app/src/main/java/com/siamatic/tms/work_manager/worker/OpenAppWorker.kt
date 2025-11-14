package com.siamatic.tms.work_manager.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class OpenAppWorker(appContext: Context, workerParams: WorkerParameters)
  : Worker(appContext, workerParams) {

  override fun doWork(): Result {
    try {
      val launchIntent = applicationContext.packageManager
        .getLaunchIntentForPackage(applicationContext.packageName)
      launchIntent?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
      }
      applicationContext.startActivity(launchIntent)
      Log.d("Worker", "Reopened the app successfully.")
    } catch (e: Exception) {
      Log.e("Worker", "Failed to open app!: ${e.message}")
      return Result.failure()
    }
    return Result.success()
  }
}