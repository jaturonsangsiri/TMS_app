package com.siamatic.tms

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
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Lock Task Mode (Kiosk)
    startLockTask()

    // FullScreen and hide bottom & top system bars
    WindowCompat.setDecorFitsSystemWindows(window, true)
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.hide(WindowInsetsCompat.Type.systemBars())
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    // Keep the screen on
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


    // Open Immersive Sticky
//    window.decorView.systemUiVisibility = (
//       android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//       or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
//       or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//       or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//       or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//       or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//    )

    setContent {
      TMSTheme {
        val navController = rememberNavController()
        Routes(navController)
      }
    }
  }

  // Disable back button on main screen
  override fun onBackPressed() {
    // Do nothing
  }

//  override fun onWindowFocusChanged(hasFocus: Boolean) {
//    super.onWindowFocusChanged(hasFocus)
//    if (hasFocus) {
//      // Reset Immersive Sticky
//      window.decorView.systemUiVisibility = (
//              android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                      or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
//                      or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                      or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                      or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                      or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//              )
//    }
//  }
}