package com.siamatic.tms.pages

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.siamatic.tms.work_manager.worker.OpenAppWorker
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@SuppressLint("ContextCastToActivity")
@Composable
fun ExitPage(paddingValues: PaddingValues) {
  Column(modifier = Modifier.padding(paddingValues).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    val context = LocalContext.current
    val activity = context as? Activity

    Button(modifier = Modifier.width(300.dp).height(70.dp), onClick = {
        activity?.stopLockTask() // Exit Lock Task Mode
        activity?.finishAffinity() // Close the Activity

        val workRequest = OneTimeWorkRequestBuilder<OpenAppWorker>()
          .setInitialDelay(2, TimeUnit.MINUTES) // หน่วงเวลา 2 นาที
          .build()

        WorkManager.getInstance(context.applicationContext)
          .enqueue(workRequest)
      }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
      Text("Click here to Exit App", fontSize = 21.sp, fontWeight = FontWeight.W600, color = Color.Blue.copy(alpha = 0.7f))
    }
  }
}