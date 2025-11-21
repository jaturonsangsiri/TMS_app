package com.siamatic.tms.pages

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextClock
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siamatic.tms.R
import com.siamatic.tms.composables.home.ProbeBox
import com.siamatic.tms.constants.DEVICE_NAME1
import com.siamatic.tms.constants.DEVICE_NAME2
import com.siamatic.tms.constants.IS_MUTE
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
import com.siamatic.tms.constants.TEMP_MAX_P1
import com.siamatic.tms.constants.TEMP_MAX_P2
import com.siamatic.tms.constants.TEMP_MIN_P1
import com.siamatic.tms.constants.TEMP_MIN_P2
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import com.siamatic.tms.models.viewModel.ApiServerViewModel
import com.siamatic.tms.models.viewModel.home.TempViewModel
import com.siamatic.tms.services.HardwareStatusValueState
import com.siamatic.tms.services.ReportTimer
import com.siamatic.tms.services.ResetMaxMinTimer
import com.siamatic.tms.ui.theme.BabyBlue
import com.siamatic.tms.util.checkForInternet
import com.siamatic.tms.util.sharedPreferencesClass

@Composable
fun MainPage(paddingValues: PaddingValues, fTemp1: Float?, fTemp2: Float?) {
  var isOutOfRange1 = remember { mutableStateOf(false) } // Probe1
  var isOutOfRange2 = remember { mutableStateOf(false) } // Probe2

  val context = LocalContext.current
  val tempViewModel: TempViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))
  val apiServerViewModel = ApiServerViewModel()

  // Check connection
  val isConnect by HardwareStatusValueState.isConnect.collectAsState()
  // AC Power checking
  val acPower by HardwareStatusValueState.acPower.collectAsState()

  // Painter resource for wifi icon.
  var wifiIcon by remember { mutableIntStateOf(R.drawable.no_wifi) }  // WIFI not connect
  var wifiColor by remember { mutableStateOf(Color.Red) }
  val sharedPref = sharedPreferencesClass(context)
  val tempAdjust1 = sharedPref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
  val tempAdjust2 = sharedPref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f

  // Report timer variables
  val reportTimers = remember { mutableStateListOf<ReportTimer>().apply { repeat(6) { add(
    ReportTimer()
  ) } } }

  // Check for internet show Dialog
  var shownNoWifiToast by remember { mutableStateOf(false) }
  val wifiReceiver = remember {
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        if (checkForInternet(context)) {
          wifiIcon = R.drawable.wifi
          wifiColor = Color.Green
          //Log.i(debugTag, "WIFI is connected")
          if (shownNoWifiToast) {
            Toast.makeText(context, "WIFI is connected", Toast.LENGTH_SHORT).show()
            shownNoWifiToast = false
          }
        } else {
          wifiIcon = R.drawable.no_wifi
          wifiColor = Color.Red
          if (!shownNoWifiToast) {
            Toast.makeText(context, "WIFI is not connect", Toast.LENGTH_SHORT).show()
            shownNoWifiToast = true
          }
        }
      }
    }
  }

  val probes = remember(fTemp1, fTemp2) {
    mutableStateListOf(
      Probe(sharedPref.getPreference(DEVICE_NAME1, "String", "Probe 1").toString(), temperature = fTemp1?.plus(tempAdjust1)),
      Probe(sharedPref.getPreference(DEVICE_NAME2, "String", "Probe 2").toString(), temperature =  fTemp2?.plus(tempAdjust2))
    )
  }

  // Get max, min temperature from SharedPreference and update to ui
  val maxTempToday1 = sharedPref.getPreference("MAX_TEMP_TODAY1", "Float", -100f).toString().toFloat()
  val minTempToday1 = sharedPref.getPreference("MIN_TEMP_TODAY1", "Float", 100f).toString().toFloat()
  val maxTempToday2 = sharedPref.getPreference("MAX_TEMP_TODAY2", "Float", -100f).toString().toFloat()
  val minTempToday2 = sharedPref.getPreference("MIN_TEMP_TODAY2", "Float", 100f).toString().toFloat()

  val maxTemp1 = sharedPref.getPreference(TEMP_MAX_P1, "Float", 22f).toString().toFloat()
  val minTemp1 = sharedPref.getPreference(TEMP_MIN_P1, "Float", 0f).toString().toFloat()
  val maxTemp2 = sharedPref.getPreference(TEMP_MAX_P2, "Float", 22f).toString().toFloat()
  val minTemp2 = sharedPref.getPreference(TEMP_MIN_P2, "Float", 0f).toString().toFloat()

  // Mute or unmute
  val isMute by tempViewModel.isMute.collectAsState()

//  LaunchedEffect(Unit) {
//    withContext(Dispatchers.IO) {
//      val offlineTemps = tempViewModel.getAllOfflineTemps()
//      offlineTemps?.forEach { temp ->
//        Log.i("OfflineData", "Offline Temp ID: ${temp.id}, Temp1: ${temp.temp1}, Temp2: ${temp.temp2}, Date: ${temp.dateStr}, Time: ${temp.timeStr}")
//      }
//    }
//  }

  LaunchedEffect(fTemp1, fTemp2) {
    isOutOfRange1.value = defaultCustomComposable.checkRangeTemperature(fTemp1?.plus(tempAdjust1), minTemp1, maxTemp1)
    isOutOfRange2.value = defaultCustomComposable.checkRangeTemperature(fTemp2?.plus(tempAdjust2), minTemp2, maxTemp2)

    // Update real temperature (temperature + adjust)
    if (fTemp1 != null && fTemp2 != null) {
      tempViewModel.updateTemp(fTemp1!! + tempAdjust1, fTemp2!! + tempAdjust2)
    } else {
      tempViewModel.updateTemp(null, null)
    }
  }

  // Use LaunchedEffect to register and unregister WIFI BroadcastReceiver
  DisposableEffect(Unit) {
    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    context.registerReceiver(wifiReceiver, filter)
    onDispose { 
      context.unregisterReceiver(wifiReceiver) 
    }
  }

  // ตั้งค่าการแจ้งเตือน Report ส่ง notification ขึ้น API
  LaunchedEffect(Unit) {
    ResetMaxMinTimer().startDairy(context)

    for(index in 1..6) {
      val isReport = sharedPref.getPreference("IS_REPORT$index", "Boolean", false) == true
      val reportTime = sharedPref.getPreference("REPORT${index}_TIME", "String", "").toString().split(":")
      if (isReport && reportTime != null) {
        reportTimers[index - 1].startDairy(reportTime[0].toInt(), reportTime[1].toInt(), tempViewModel, apiServerViewModel)
        Log.i(debugTag, "Set notification report$index time: ${reportTime[0]}:${reportTime[1]}")
      }
    }

//    val timer = ReportTimer()
//    timer.startDairy(15,56,tempViewModel,apiServerViewModel)
//    Log.i(debugTag, "Set notification report1 time: 15:56")
  }

  DisposableEffect(Unit) {
    onDispose {
      reportTimers.forEach { it.stop() }
    }
  }

  // If temperature is more than MaxTemp or less than MinTemp play the alarm
  Column(modifier = Modifier.padding(paddingValues)) {
    // Box Status
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Connect hardware status
        Image(modifier = Modifier
          .size(90.dp)
          .padding(start = 10.dp), painter = painterResource(
          if (isConnect) R.drawable.green_led // SerialPort connected
          else R.drawable.red_led // SerialPort not connect
        ), contentDescription = "")
        // AC icon status
        Image(modifier = Modifier.size(90.dp).padding(start = 10.dp), painter = painterResource(
          if (acPower) R.drawable.plug_in
          else R.drawable.unplug
        ), contentDescription = "")
        // WIFI Status
        Icon(modifier = Modifier
          .size(90.dp)
          .padding(start = 10.dp), tint = wifiColor, painter = painterResource(id = wifiIcon), contentDescription = "")
      }

      Card(modifier = Modifier
        .width(220.dp)
        .height(80.dp)
        .padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.6f)), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Row(modifier = Modifier.padding(start = 10.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
          Icon(modifier = Modifier
            .size(35.dp)
            .padding(end = 10.dp), tint = Color.White.copy(alpha = 0.7f), painter = painterResource(id = R.drawable.alarm_clock), contentDescription = "")
          AndroidView(
            factory = { context ->
              TextClock(context).apply {
                format24Hour = "HH:mm:ss"
                format12Hour = null
                textSize = 24f
                setTextColor(context.getColor(R.color.white70))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTypeface(typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
              }
            },
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }

    // Probs , temperature
    Row(modifier = Modifier.fillMaxWidth()) {
      ProbeBox(probes[0].name, probes[0].temperature, maxTempToday1, minTempToday1, isOutOfRange1.value, modifier = Modifier.weight(1f))
      ProbeBox(probes[1].name, probes[1].temperature, maxTempToday2, minTempToday2, isOutOfRange2.value, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(20.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      defaultCustomComposable.BuildTextIconButton(
        text = if (isMute) "Unmute Alarm" else "Mute alarm",
        bgColor = if (isMute) BabyBlue else colorResource(id = R.color.colorRed),
        onClick = {
          if (isMute) {
            tempViewModel.muteUnmute(false)
            Toast.makeText(context, "Unmute alarm Success", Toast.LENGTH_SHORT).show()
          } else {
            tempViewModel.muteUnmute(true)
            Toast.makeText(context, "Mute Alarm Success", Toast.LENGTH_SHORT).show()
          }
        },
        imageVector = null,
        painter = painterResource(id = if (isMute) R.drawable.speaker else R.drawable.mute_icon),
        iconSize = 20.dp
      )
    }
  }
}