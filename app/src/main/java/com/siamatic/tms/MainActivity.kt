package com.siamatic.tms

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.siamatic.tms.composables.DefaultCustomComposable
import com.siamatic.tms.configs.Routes
import com.siamatic.tms.constants.DEVICE_API_TOKEN
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.EMAIL_PASSWORD
import com.siamatic.tms.constants.SHEET_ID
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.models.viewModel.PageIndicatorViewModel
import com.siamatic.tms.ui.theme.TMSTheme
import com.siamatic.tms.util.sharedPreferencesClass
import com.siamatic.tms.work_manager.worker.OpenAppWorker
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

val defaultCustomComposable = DefaultCustomComposable()

class MainActivity : ComponentActivity() {
  private lateinit var pageIndicatorViewModel: PageIndicatorViewModel

  // ถ้าผู้ใช้ไม่ Interaction กับหน้าจอเกิน 2 นาทีให้กลับไปที่หน้าหลัก
  private val handlerInteraction = Handler(Looper.getMainLooper())
  private val inactivityRunnable = Runnable {
    Log.d(debugTag, "User don't interactive for 5 minute!")
    pageIndicatorViewModel.isHomePage.value = true
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ลบ Repoen App Worker เพราะเราเปปิดใหม่มาแล้ว
    //WorkManager.getInstance(this).cancelAllWorkByTag("reopenApp")

    val sharedPref = sharedPreferencesClass(this)
    val config = defaultCustomComposable.loadConfig(this)
    //Log.d(debugTag, "DEVICE_ID: ${config?.get("SN_DEVICE_KEY")}, SHEET_ID: ${config?.get("SHEET_ID")}, EMAIL_PASSWORD: ${config?.get("EMAIL_PASSWORD")}, DEVICE_API_TOKEN: ${config?.get("DEVICE_API_TOKEN")}")
    sharedPref.savePreference(DEVICE_ID, config?.get("SN_DEVICE_KEY"))
    sharedPref.savePreference(SHEET_ID, config?.get("SHEET_ID"))
    sharedPref.savePreference(EMAIL_PASSWORD, config?.get("EMAIL_PASSWORD"))
    sharedPref.savePreference(DEVICE_API_TOKEN, config?.get("DEVICE_API_TOKEN"))

    // ไม่ใช้เต็มจอแล้ว เพราะทำให้ UI โดยทับ
    // FullScreen and hide bottom & top system bars
    //WindowCompat.setDecorFitsSystemWindows(window, false)
    //val controller = WindowInsetsControllerCompat(window, window.decorView)
    //controller.hide(WindowInsetsCompat.Type.systemBars())
    //controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    // Keep the screen on
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    // Set the screen brightness to 40% for prevent energy waste
    window.attributes.screenBrightness = 0.4f

    setContent {
      pageIndicatorViewModel = ViewModelProvider(this)[PageIndicatorViewModel::class.java]

      // Reset data for debug (record to many)
      //tempViewModel.resetData()
      TMSTheme {
        val navController = rememberNavController()
        Routes(pageIndicatorViewModel, navController)
      }
    }

    resetInteractionTimer()
  }

  // ถ้าผู้ใช้กด เลื่อน ขยับจอจะ reset timer 5 นาที
  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    resetInteractionTimer()
    return super.dispatchTouchEvent(ev)
  }

  private fun resetInteractionTimer() {
    handlerInteraction.removeCallbacks(inactivityRunnable)
    handlerInteraction.postDelayed(inactivityRunnable, 5 * 60 * 1000L)
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

    // ตั้ง Worker ให้เริ่มเร็ว (หน่วง 2 วินาทีพอ)
    val workRequest = OneTimeWorkRequestBuilder<OpenAppWorker>()
      .setInitialDelay(2, TimeUnit.SECONDS)
      .addTag("reopenApp")
      .build()

    WorkManager.getInstance(applicationContext).enqueue(workRequest)

    // รอเล็กน้อยให้ WorkManager enqueue เสร็จ
    Handler(Looper.getMainLooper()).postDelayed({
      finishAffinity()
      android.os.Process.killProcess(android.os.Process.myPid())
      exitProcess(0)
    }, 1000) // 1 วินาทีหลังจาก enqueue
  }

  override fun onDestroy() {
    super.onDestroy()
    handlerInteraction.removeCallbacks(inactivityRunnable)
  }
}