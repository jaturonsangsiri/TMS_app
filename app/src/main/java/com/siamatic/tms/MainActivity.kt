package com.siamatic.tms

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController

import com.siamatic.tms.composables.DefaultCustomComposable
import com.siamatic.tms.configs.Routes
import com.siamatic.tms.ui.theme.TMSTheme

val defaultCustomComposable = DefaultCustomComposable()

open class MainActivity : ComponentActivity() {
  private var isRestartingApp = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // FullScreen and hide bottom & top system bars
    WindowCompat.setDecorFitsSystemWindows(window, true)
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.hide(WindowInsetsCompat.Type.systemBars())
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    // Keep the screen on
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    // Set the screen brightness to 40% for prevent energy waste
    window.attributes.screenBrightness = 0.4f

    setContent {
      TMSTheme {
        val navController = rememberNavController()
        Routes(navController)
      }
    }
  }

  // สร้าง Application ใหม่เมื่อถูกทำลาย
  private fun onCreateNewApplication() {
    if (isRestartingApp) return
    isRestartingApp = true

    val context = applicationContext
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)

    Runtime.getRuntime().exit(0) // ออกจาก process เดิม
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

    // กลับมาใน app
    onCreateNewApplication()
  }

  // ปิดปุ่ม Recent Apps
  override fun onPause() {
    super.onPause()
//    if (!isFinishing && !isRestartingApp) {
//      defaultCustomComposable.showToast(this, "Please don't leave app!")
//      // กลับมาใน app
//      onCreateNewApplication()
//    }
  }

  // เมื่อ activity ถูกทำลาย ให้เรียก Application ขึ้นมาใหม่
  override fun onDestroy() {
    super.onDestroy()
    if (!isRestartingApp) {
      onCreateNewApplication()
    }
  }
}