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

  // Disable back button on main screen
  override fun onBackPressed() {
    // Do nothing
  }
}