package com.siamatic.tms.services

import android.content.Context
import com.siamatic.tms.util.sharedPreferencesClass
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class ResetMaxMinTimer {
  private var timer: Timer? = null

  fun startDairy(context: Context) {
    timer?.cancel()
    val prePref = sharedPreferencesClass(context)

    val now = Calendar.getInstance()
    // รีเซ็ตค่า Max Min ของทั้งวันนี้ในเวลา
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)

      // ถ้าเวลานี้ผ่านไปแล้ว ให้ตั้งพรุ่งนี้
      if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
    }
    val delay = target.timeInMillis - now.timeInMillis

    timer = Timer().apply {
      schedule(object : TimerTask() {
        override fun run() {
          prePref.savePreference("MAX_TEMP_TODAY1", -100f)
          prePref.savePreference("MIN_TEMP_TODAY1", 100f)
          prePref.savePreference("MAX_TEMP_TODAY2", -100f)
          prePref.savePreference("MIN_TEMP_TODAY2", 100f)

          startDairy(context)
        }
      }, delay)
    }
  }

  fun stop() {
    timer?.cancel()
    timer = null
  }
}