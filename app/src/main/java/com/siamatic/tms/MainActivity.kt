package com.siamatic.tms

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.siamatic.tms.composables.DefaultCustomComposable
import com.siamatic.tms.configs.Routes
import com.siamatic.tms.constants.DEVICE_API_TOKEN
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.EMAIL_PASSWORD
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
import com.siamatic.tms.constants.SHEET_ID
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.models.viewModel.PageIndicatorViewModel
import com.siamatic.tms.models.viewModel.home.TempViewModel
import com.siamatic.tms.models.viewModel.home.UartViewModel
import com.siamatic.tms.ui.theme.TMSTheme
import com.siamatic.tms.util.sharedPreferencesClass
import com.siamatic.tms.work_manager.worker.OpenAppWorker
import java.util.concurrent.TimeUnit

val defaultCustomComposable = DefaultCustomComposable()

class MainActivity : ComponentActivity() {
  private lateinit var pageIndicatorViewModel: PageIndicatorViewModel

  // ถ้าผู้ใช้ไม่ Interaction กับหน้าจอเกิน 2 นาทีให้กลับไปที่หน้าหลัก
  private val handlerInteraction = Handler(Looper.getMainLooper())
  private val inactivityRunnable = Runnable {
    Log.d(debugTag, "User inactive for 2 minutes!")
    pageIndicatorViewModel.isHomePage.value = true
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val sharedPref = sharedPreferencesClass(this)
    val config = defaultCustomComposable.loadConfig(this)
    //Log.d(debugTag, "DEVICE_ID: ${config?.get("SN_DEVICE_KEY")}, SHEET_ID: ${config?.get("SHEET_ID")}, EMAIL_PASSWORD: ${config?.get("EMAIL_PASSWORD")}, DEVICE_API_TOKEN: ${config?.get("DEVICE_API_TOKEN")}")
    sharedPref.savePreference(DEVICE_ID, config?.get("SN_DEVICE_KEY"))
    sharedPref.savePreference(SHEET_ID, config?.get("SHEET_ID"))
    sharedPref.savePreference(EMAIL_PASSWORD, config?.get("EMAIL_PASSWORD"))
    sharedPref.savePreference(DEVICE_API_TOKEN, config?.get("DEVICE_API_TOKEN"))

    // FullScreen and hide bottom & top system bars
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.hide(WindowInsetsCompat.Type.systemBars())
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    // Keep the screen on
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    // Set the screen brightness to 40% for prevent energy waste
    window.attributes.screenBrightness = 0.4f

    setContent {
      val uartViewModel: UartViewModel = viewModel()
      val tempViewModel: TempViewModel = viewModel()
      pageIndicatorViewModel = ViewModelProvider(this)[PageIndicatorViewModel::class.java]
      uartViewModel.initUart(this)

      // Reset data for debug (record to many)
      //tempViewModel.resetData()

      val tempAdjust1 = sharedPref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
      val tempAdjust2 = sharedPref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f

      val fTemp1 by uartViewModel.fTemp1.collectAsState()
      val fTemp2 by uartViewModel.fTemp2.collectAsState()

      // Update real temperature (temperature + adjust)
      if (fTemp1 != null && fTemp2 != null) {
        tempViewModel.updateTemp(fTemp1!! + tempAdjust1, fTemp2!! + tempAdjust2)
      } else {
        tempViewModel.updateTemp(null, null)
      }

      TMSTheme {
        val navController = rememberNavController()
        Routes(pageIndicatorViewModel, navController)
      }
    }

    resetInteractionTimer()
  }

  // ถ้าผู้ใช้กด เลื่อน ขยับจอจะ reset timer 2 นาที
  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    resetInteractionTimer()
    return super.dispatchTouchEvent(ev)
  }

  private fun resetInteractionTimer() {
    handlerInteraction.removeCallbacks(inactivityRunnable)
    handlerInteraction.postDelayed(inactivityRunnable, 2 * 60 * 1000L)
  }

  // ปิดปุ่มกดย้อนกลับ
  override fun onBackPressed() {
    // Do nothing
    defaultCustomComposable.showToast(this, "Please don't leave app!")
  }

  // ปิดการกดปุ่มโฮม
  override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    defaultCustomComposable.showToast(this, "Please don't leave app!")

    val workRequest = OneTimeWorkRequestBuilder<OpenAppWorker>()
      .setInitialDelay(1, TimeUnit.SECONDS)
      .build()
    WorkManager.getInstance(this).enqueue(workRequest)
  }

  override fun onDestroy() {
    super.onDestroy()
    handlerInteraction.removeCallbacks(inactivityRunnable)
  }
}