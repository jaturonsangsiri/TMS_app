package com.siamatic.tms.pages

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.DEVICE_NAME1
import com.siamatic.tms.constants.DEVICE_NAME2
import com.siamatic.tms.constants.EMAIL_PASSWORD
import com.siamatic.tms.constants.IS_MUTE
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
import com.siamatic.tms.constants.RECORD_INTERVAL
import com.siamatic.tms.constants.SHEET_ID
import com.siamatic.tms.constants.TEMP_MAX_P1
import com.siamatic.tms.constants.TEMP_MAX_P2
import com.siamatic.tms.constants.TEMP_MIN_P1
import com.siamatic.tms.constants.TEMP_MIN_P2
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.constants.minOptionsLng
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import com.siamatic.tms.models.viewModel.home.TempViewModel
import com.siamatic.tms.models.viewModel.home.UartViewModel
import com.siamatic.tms.ui.theme.BabyBlue
import com.siamatic.tms.util.checkForInternet
import com.siamatic.tms.util.sharedPreferencesClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@Composable
fun MainPage(paddingValues: PaddingValues, fTemp1: Float?, fTemp2: Float?) {
  var isOutOfRange1 = remember { mutableStateOf(false) } // Probe1
  var isOutOfRange2 = remember { mutableStateOf(false) } // Probe2

  val context = LocalContext.current
  val uartViewModel: UartViewModel = viewModel()
  // Check connection
  val isConnect by uartViewModel.isConnect.collectAsState()

  // Painter resource for wifi icon.
  var wifiIcon by remember { mutableIntStateOf(R.drawable.no_wifi) }  // WIFI not connect
  val sharedPref = sharedPreferencesClass(context)
  val tempAdjust1 = sharedPref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
  val tempAdjust2 = sharedPref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f

  // Check for internet show Dialog
  var shownNoWifiToast by remember { mutableStateOf(false) }
  val wifiReceiver = remember {
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (networkInfo?.isConnected == true) {
          wifiIcon = R.drawable.wifi
          //Log.i(debugTag, "WIFI is connected")
          if (shownNoWifiToast) {
            Toast.makeText(context, "WIFI is connected", Toast.LENGTH_SHORT).show()
            shownNoWifiToast = false
          }
        } else {
          wifiIcon = R.drawable.no_wifi
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
  val maxTemp1 = sharedPref.getPreference(TEMP_MAX_P1, "Float", 22f).toString().toFloat()
  val minTemp1 = sharedPref.getPreference(TEMP_MIN_P1, "Float", 0f).toString().toFloat()
  val maxTemp2 = sharedPref.getPreference(TEMP_MAX_P2, "Float", 22f).toString().toFloat()
  val minTemp2 = sharedPref.getPreference(TEMP_MIN_P2, "Float", 0f).toString().toFloat()

  // Mute or unmute
  val isMute = remember { mutableStateOf(sharedPref.getPreference(IS_MUTE, "String", "false")) }
  val tempViewModel: TempViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))
  var previousTemp1 by remember { mutableStateOf<Float?>(null) } // previous temp1
  var previousTemp2 by remember { mutableStateOf<Float?>(null) } // previous temp2

  // Settings alarm sound
  var isStartedAlarm = remember { mutableStateOf(false) }
  var mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.alarm_sound).apply {
    setOnCompletionListener {
      isStartedAlarm.value = false
    }
  }

  LaunchedEffect(fTemp1, fTemp2) {
    val roundedTemp1 = fTemp1?.let { "%.2f".format(it).toFloat() }
    val roundedTemp2 = fTemp2?.let { "%.2f".format(it).toFloat() }
    isOutOfRange1.value = checkRangeTemperature(fTemp1, minTemp1, maxTemp1)
    isOutOfRange2.value = checkRangeTemperature(fTemp2, minTemp2, maxTemp2)

    if ((roundedTemp1 != null || roundedTemp2 != null) && (roundedTemp1 != previousTemp1 || roundedTemp2 != previousTemp2)) {
      // อัปเดต previous
      previousTemp1 = roundedTemp1
      previousTemp2 = roundedTemp2

      // Check if user mute alarm
      if (isMute.value != "true") {
        if (isOutOfRange1.value || isOutOfRange2.value) {
          if (!isStartedAlarm.value) {
            isStartedAlarm.value = true
            mediaPlayer?.start()
          }
        }
      }
    }
  }

  // Checking wifi in every 30 second
  // Use LaunchedEffect + Delay instead of Timer (safer to write)
  DisposableEffect(Unit) {
    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    context.registerReceiver(wifiReceiver, filter)
    onDispose { 
      context.unregisterReceiver(wifiReceiver) 
    }
  }

  // LaunchedEffect(Unit) {
  //   while(true) {
  //     if (checkForInternet(context)) {
  //       wifiIcon = R.drawable.wifi
  //       shownNoWifiToast = false
  //       //Log.i(debugTag, "WIFI is connected")
  //     } else {
  //       wifiIcon = R.drawable.no_wifi
  //       //Log.i(debugTag, "WIFI is not connect")
        
  //       if (!shownNoWifiToast) {
  //         Toast.makeText(context, "WIFI is not connected", Toast.LENGTH_SHORT).show()
  //         shownNoWifiToast = true
  //       }
  //     }
  //     delay(30000L) // every 30 second
  //   }
  // }

  // If temperature is more than MaxTemp or less than MinTemp play the alarm

  Column(modifier = Modifier.padding(paddingValues)) {
    // Box Status
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Connect hardware status
        Image(modifier = Modifier
          .size(90.dp)
          .padding(start = 10.dp), painter = painterResource(
          if (isConnect == true) R.drawable.green_led // SerialPort connected
          else R.drawable.red_led // SerialPort not connect
        ), contentDescription = "")
        // WIFI Status
        Icon(modifier = Modifier
          .size(90.dp)
          .padding(start = 10.dp), tint = Color.White.copy(alpha = 0.7f), painter = painterResource(id = wifiIcon), contentDescription = "")
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
      ProbeBox(probes[0].name, probes[0].temperature, maxTemp1, minTemp1, isOutOfRange1.value, modifier = Modifier.weight(1f))
      ProbeBox(probes[1].name, probes[1].temperature, maxTemp2, minTemp2, isOutOfRange2.value, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(20.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      defaultCustomComposable.BuildTextIconButton(
        text = if (isMute.value == "true") "Unmute Alarm" else "Mute alarm",
        bgColor = if (isMute.value == "true") BabyBlue else colorResource(id = R.color.colorRed),
        onClick = {
          if (isMute.value == "true") {
            isMute.value = "false"
            sharedPref.savePreference(IS_MUTE, "false")
            Toast.makeText(context, "Unmute alarm Success", Toast.LENGTH_SHORT).show()
          } else {
            isMute.value = "true"
            sharedPref.savePreference(IS_MUTE, "true")
            Toast.makeText(context, "Mute Alarm Success", Toast.LENGTH_SHORT).show()

            mediaPlayer?.stop()
            isStartedAlarm.value = false
          }
        },
        imageVector = null,
        painter = painterResource(id = if (isMute.value == "true") R.drawable.speaker else R.drawable.mute_icon),
        iconSize = 20.dp
      )
    }
  }
}

// Check Range If temperature is more than MaxTemp or less than MinTemp play the alarm
fun checkRangeTemperature(fTemp: Float?, minTemp: Float?, maxTemp: Float?): Boolean {
  return if (fTemp != null && minTemp != null && maxTemp != null) fTemp!! < minTemp!! || fTemp > maxTemp!! else false
}