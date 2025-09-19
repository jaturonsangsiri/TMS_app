package com.siamatic.tms.pages

import android.app.Application
import android.graphics.Typeface
import android.media.MediaPlayer
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
import com.siamatic.tms.constants.RECORD_INTERVAL
import com.siamatic.tms.constants.TEMP_MAX_P1
import com.siamatic.tms.constants.TEMP_MAX_P2
import com.siamatic.tms.constants.TEMP_MIN_P1
import com.siamatic.tms.constants.TEMP_MIN_P2
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.constants.minOptionsLng
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import com.siamatic.tms.models.viewModel.home.TempViewModel
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
  var isOutOfRange = remember { mutableStateOf(false) }
  val context = LocalContext.current
  // Painter resource for wifi icon.
  var wifiIcon by remember { mutableIntStateOf(R.drawable.no_wifi) }  // WIFI not connect
  val sharedPref = sharedPreferencesClass(context)
  val tempAdjust1 = sharedPref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
  val tempAdjust2 = sharedPref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f

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
  val timer = Timer()

  // Settings alarm sound
  var isStartedAlarm = remember { mutableStateOf(false) }
  var mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.alarm_sound).apply {
    setOnCompletionListener {
      isStartedAlarm.value = false
      Log.i(debugTag, "Alarm sound complete: ${isStartedAlarm.value}")
    }
  }

  // Timer: insert/log by RECORD_INTERVAL in SetUpDevice page
  DisposableEffect(Unit) {
    val tag = sharedPref.getPreference(RECORD_INTERVAL, "String", "5 minute")
    val tempAdjust1 = sharedPref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
    val tempAdjust2 = sharedPref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
    timer.schedule(
      object : TimerTask() {
        override fun run() {
          CoroutineScope(Dispatchers.Main).launch {
            Log.d(debugTag, "tag minute: $tag")
            Log.d(debugTag, "minOptionsLng: ${minOptionsLng[tag]} milli seconds")
            val roundedTemp1 = fTemp1?.let { "%.2f".format(it + tempAdjust1).toFloat() }
            val roundedTemp2 = fTemp2?.let { "%.2f".format(it + tempAdjust2).toFloat() }

            if (roundedTemp1 != null || roundedTemp2 != null) {
              tempViewModel.insertTemp(roundedTemp1 ?: 0f, roundedTemp2 ?: 0f)
              Log.i(
                debugTag,
                "New temp recorded at ${
                  defaultCustomComposable.convertLongToTime(System.currentTimeMillis())
                }: fTemp1=${String.format("%.2f", roundedTemp1)}, fTemp2=${String.format("%.2f", roundedTemp2)}"
              )
              previousTemp1 = roundedTemp1
              previousTemp2 = roundedTemp2
            }
          }
        }
      },
      0L,
      minOptionsLng[tag] ?: (5 * 60 * 1000L) // 5 minute default
    )

    onDispose {
      timer.cancel() // Prevent memory leak
    }
  }

  LaunchedEffect(fTemp1, fTemp2) {
    val roundedTemp1 = fTemp1?.let { "%.2f".format(it).toFloat() }
    val roundedTemp2 = fTemp2?.let { "%.2f".format(it).toFloat() }
    isOutOfRange.value = checkRangeTemperature(fTemp1, fTemp2, minTemp1, maxTemp1, minTemp2, maxTemp2)

    if ((roundedTemp1 != null || roundedTemp2 != null) && (roundedTemp1 != previousTemp1 || roundedTemp2 != previousTemp2)) {
      // อัปเดต previous
      previousTemp1 = roundedTemp1
      previousTemp2 = roundedTemp2

      // Check if user mute alarm
      if (isMute.value != "true") {
        if (isOutOfRange.value) {
          Log.i(debugTag, "Is the alarm start?: ${isStartedAlarm.value}")
          if (!isStartedAlarm.value) {
            isStartedAlarm.value = true
            mediaPlayer?.start()
            Log.i(debugTag, "Started alarm: ${isStartedAlarm.value}")
          }
        }
      }
    }
  }

  // Checking wifi in every 30 second
  // Use LaunchedEffect + Delay instead of Timer (safer to write)
  LaunchedEffect(Unit) {
    while(true) {
      if (checkForInternet(context)) {
        wifiIcon = R.drawable.wifi
        //Log.i(debugTag, "WIFI is connected")
      } else {
        wifiIcon = R.drawable.no_wifi
        //Log.i(debugTag, "WIFI is not connect")
        Toast.makeText(context, "WIFI is not connect", Toast.LENGTH_SHORT).show()
      }
      delay(30000L) // every 30 second
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
          if (checkForInternet(context)) R.drawable.green_led // SerialPort connected
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
      ProbeBox(probes[0].name, probes[0].temperature, maxTemp1, minTemp1, isOutOfRange.value, modifier = Modifier.weight(1f))
      ProbeBox(probes[1].name, probes[1].temperature, maxTemp2, minTemp2, isOutOfRange.value, modifier = Modifier.weight(1f))
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
fun checkRangeTemperature(fTemp1: Float?, fTemp2: Float?, minTemp1: Float?, maxTemp1: Float?, minTemp2: Float?, maxTemp2: Float?): Boolean {
  var isOutOfRange = false

  fTemp1?.let { temp1 ->
    minTemp1?.let { minTemp ->
      maxTemp1?.let { maxTemp ->
        val outOfRange = (temp1 < minTemp || temp1 > maxTemp)
        //Log.i(debugTag, "Probe1 -> $temp1 < $minTemp, $temp1 > $maxTemp")
        if (outOfRange) isOutOfRange = true
      }
    }
  }

  fTemp2?.let { temp2 ->
    minTemp2?.let { minTemp ->
      maxTemp2?.let { maxTemp ->
        val outOfRange = (temp2 < minTemp || temp2 > maxTemp)
        //Log.i(debugTag, "Probe2 -> $temp2 < $minTemp, $temp2 > $maxTemp")
        if (outOfRange) isOutOfRange = true
      }
    }
  }

  //Log.i(debugTag, "Final isOutOfRange = $isOutOfRange")
  return isOutOfRange
}