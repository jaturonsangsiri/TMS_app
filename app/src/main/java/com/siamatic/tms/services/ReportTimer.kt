package com.siamatic.tms.services

import android.util.Log
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.viewModel.ApiServerViewModel
import com.siamatic.tms.models.viewModel.home.TempViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class ReportTimer {
  private var timer: Timer? = null

  fun startDairy(hourDairy: Int, minuteDairy: Int, tempViewModel: TempViewModel, apiServerViewModel: ApiServerViewModel) {
    timer?.cancel()

    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hourDairy)
      set(Calendar.MINUTE, minuteDairy)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)

      // ถ้าเวลานี้ผ่านไปแล้ว ให้ตั้งพรุ่งนี้
      if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
    }
    val delay = target.timeInMillis - now.timeInMillis

    timer = Timer().apply {
      schedule(object : TimerTask() {
        override fun run() {
          sendReport(tempViewModel, apiServerViewModel)

          startDairy(hourDairy, minuteDairy, tempViewModel, apiServerViewModel)
        }
      }, delay)
    }
  }

  fun sendReport(tempViewModel: TempViewModel, apiServerViewModel: ApiServerViewModel) {
    val date = defaultCustomComposable.convertLongToDateOnly(System.currentTimeMillis())
    val time = defaultCustomComposable.convertLongToTime(System.currentTimeMillis())
    val roundedTemp1 = tempViewModel._latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
    val roundedTemp2 = tempViewModel._latestTemp.value.second?.let { "%.2f".format(it).toFloat() }

    if (roundedTemp1 != null && roundedTemp2 != null) {
      Log.d(debugTag, "Temperature report probe 1 $date $time, temp is $roundedTemp1 °C")
      Log.d(debugTag, "Temperature report probe 2 $date $time, temp is $roundedTemp2 °C")

      CoroutineScope(Dispatchers.IO).launch {
        apiServerViewModel.notifyNotNormalTemp("Report temp to server probe 1", tempViewModel.serialNumber, "N/A", roundedTemp1, roundedTemp1 - tempViewModel.adjTemp1, "Temperature report probe 1 $date $time, temp is $roundedTemp1 °C",  date, time)
        apiServerViewModel.notifyNotNormalTemp("Report temp to server probe 2", tempViewModel.serialNumber, "N/A", roundedTemp2, roundedTemp2 - tempViewModel.adjTemp2, "Temperature report probe 2 $date $time, temp is $roundedTemp2 °C",  date, time)
      }
    } else {
      Log.e(debugTag, "No temp to send Report!")
    }
  }

  fun stop() {
    timer?.cancel()
    timer = null
  }
}