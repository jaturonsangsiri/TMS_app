package com.siamatic.tms.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.util.sharedPreferencesClass
import com.siamatic.tms.work_manager.worker.OpenAppWorker
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@SuppressLint("ContextCastToActivity")
@Composable
fun ExitPage(paddingValues: PaddingValues) {
  val context = LocalContext.current
  val activity = context as? Activity
  val prefev = sharedPreferencesClass(context)
  val serialNumber = prefev.getPreference(DEVICE_ID, "String", "").toString()
  val appVersion = prefev.getPreference("APP_VERSION", "String", "").toString()

  Box(
    modifier = Modifier
      .padding(paddingValues)
      .fillMaxSize()
  ) {

    // กลางจอ
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(
        modifier = Modifier.width(300.dp).height(70.dp),
        onClick = {
          val workRequest = OneTimeWorkRequestBuilder<OpenAppWorker>()
            .setInitialDelay(5, TimeUnit.MINUTES)
            .addTag("reopenApp")
            .build()

          WorkManager.getInstance(context).enqueue(workRequest)

          Handler(Looper.getMainLooper()).postDelayed({
            activity?.finishAffinity()
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
          }, 1000)
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
      ) {
        Text(
          "Click here to Exit App",
          fontSize = 21.sp,
          fontWeight = FontWeight.W600,
          color = Color.Blue.copy(alpha = 0.7f)
        )
      }
    }

    // ล่างจอ
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 13.dp, end = 13.dp, bottom = 13.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("version: $appVersion",
        style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
      )
      Text("Serial Number: $serialNumber",
        style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
      )
    }
  }
}
