package com.siamatic.tms.work_manager.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.siamatic.tms.MainActivity
import com.siamatic.tms.constants.debugTag

class OpenAppWorker(
  context: Context,
  parameters: WorkerParameters
) : Worker(context, parameters) {
  private val isReturnToApp: Boolean = false

  override fun doWork(): Result {
    return try {
      reOpenAppActivity()
      Result.success()
    } catch (e: Exception) {
      Log.e(debugTag, "Error reopening app", e)
      Result.failure()
    }
  }

  private fun reOpenAppActivity() {
    // ใช้ applicationContext เพื่อความปลอดภัย (Worker ไม่มี Activity context)
    val context = applicationContext

    // สร้าง Intent ไปที่ MainActivity
    val intent = Intent(context, MainActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // จำเป็น!
      addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    // เปิด Activity ขึ้นมาใหม่
    context.startActivity(intent)
  }
}