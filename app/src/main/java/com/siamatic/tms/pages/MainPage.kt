package com.siamatic.tms.pages

import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.siamatic.tms.R
import com.siamatic.tms.composables.home.ProbeBox
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import com.siamatic.tms.ui.theme.BabyBlue
import kotlinx.coroutines.delay

@Composable
fun MainPage(paddingValues: PaddingValues, fTemp1: Float?, fTemp2: Float?) {
  val isMute = remember { mutableStateOf(false) }
  val context = LocalContext.current
  val probes = listOf(Probe("Probe 1", fTemp1, maxTemp = 28.5f, minTemp = 7f), Probe("Probe 2", fTemp2, maxTemp = 31.5f, minTemp = -1f))
  // Painter resource for wifi icon.
  var wifiIcon by remember { mutableIntStateOf(R.drawable.no_wifi) }  // WIFI not connect

  // Checking wifi in every 30 second
  // Use LaunchedEffect + Delay instead of Timer (safer to write)
  LaunchedEffect(Unit) {
    while(true) {
      if (checkForInternet(context)) {
        wifiIcon = R.drawable.wifi
        Log.i(debugTag, "WIFI is connected")
      } else {
        wifiIcon = R.drawable.no_wifi
        Log.i(debugTag, "WIFI is not connect")
        Toast.makeText(context, "WIFI is not connect", Toast.LENGTH_SHORT).show()
      }
      delay(30000L) // every 30 second
    }
  }

  Column(modifier = Modifier.padding(paddingValues)) {
    // Box Status
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Connect hardware status
        Image(modifier = Modifier.size(90.dp).padding(start = 10.dp), painter = painterResource(
          if (checkForInternet(context)) R.drawable.green_led // SerialPort connected
          else R.drawable.red_led // SerialPort not connect
        ), contentDescription = "")
        // WIFI Status
        Icon(modifier = Modifier.size(90.dp).padding(start = 10.dp), tint = Color.White.copy(alpha = 0.7f), painter = painterResource(id = wifiIcon), contentDescription = "")
      }

      Card(modifier = Modifier.width(220.dp).height(80.dp).padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.6f)), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Row(modifier = Modifier.padding(start = 10.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
          Icon(modifier = Modifier.size(35.dp).padding(end = 10.dp), tint = Color.White.copy(alpha = 0.7f), painter = painterResource(id = R.drawable.alarm_clock), contentDescription = "")
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
      probes.forEach { probe ->
        //val temp = Random.nextFloat() * (30.0f - 3.0f) + 3.0f // debuging random temp
        ProbeBox(probe.name, probe.temperature, probe.maxTemp, probe.minTemp, modifier = Modifier.weight(1f))
      }
    }
    Spacer(modifier = Modifier.height(20.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      defaultCustomComposable.BuildTextIconButton(
        text = if (isMute.value) "Unmute Alarm" else "Mute alarm",
        bgColor = if (isMute.value) BabyBlue else colorResource(id = R.color.colorRed),
        onClick = {
          isMute.value = !isMute.value
          Toast.makeText(context, if (isMute.value) "Mute Alarm Success" else "Unmute alarm Success", Toast.LENGTH_SHORT).show()
        },
        imageVector = null,
        painter = painterResource(id = if (isMute.value) R.drawable.speaker else R.drawable.mute_icon),
        iconSize = 20.dp
      )
    }
  }
}

fun checkForInternet(context: Context): Boolean {
  // register activity with the connectivity manager service
  val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  // if the android version is equal to M
  // or greater we need to use the
  // NetworkCapabilities to check what type of
  // network has the internet connection
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

    // Returns a Network object corresponding to
    // the currently active default data network.
    val network = connectivityManager.activeNetwork ?: return false

    // Representation of the capabilities of an active network.
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

    return when {
      // Indicates this network uses a Wi-Fi transport,
      // or WiFi has network connectivity
      activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

      // Indicates this network uses a Cellular transport. or
      // Cellular has network connectivity
      activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

      // else return false
      else -> false
    }
  } else {
    // if the android version is below M
    @Suppress("DEPRECATION") val networkInfo =
      connectivityManager.activeNetworkInfo ?: return false
    @Suppress("DEPRECATION")
    return networkInfo.isConnected
  }
}
