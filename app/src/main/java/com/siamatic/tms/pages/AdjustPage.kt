package com.siamatic.tms.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.viewModel.home.UartViewModel
import com.siamatic.tms.util.sharedPreferencesClass

@Composable
fun AdjustPage(paddingValues: PaddingValues) {
  val context = LocalContext.current
  val sharedPreferences = sharedPreferencesClass(context)
  val uartViewModel: UartViewModel = viewModel()

  val realTemp1 by uartViewModel.fTemp1.collectAsState()
  val realTemp2 by uartViewModel.fTemp2.collectAsState()
  var tempAdjust1 by remember { mutableFloatStateOf(sharedPreferences.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f) }
  var tempAdjust2 by remember { mutableFloatStateOf(sharedPreferences.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f) }
  val probes = listOf(ProbeData("Probe 1", realTemp1, tempAdjust1) { tempAdjust1 = it }, ProbeData("Probe 2", realTemp2, tempAdjust2) { tempAdjust2 = it })

  // ******  For responsive ui *******
  val isTab3 = defaultCustomComposable.getDeviceHeightPixels(context)

  Column(modifier = Modifier.padding(paddingValues).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Row {
      probes.forEachIndexed { index, probe ->
        ProbeCard(
          probe = probe,
          onSave = {
            if (index == 0) sharedPreferences.savePreference(P1_ADJUST_TEMP, probe.adjustTemp)
            else sharedPreferences.savePreference(P2_ADJUST_TEMP, probe.adjustTemp)
            Toast.makeText(context, "Saved Data", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.weight(0.5f).fillMaxHeight(if (isTab3) 0.8f else 0.65f).padding(30.dp),
          isTab3 = isTab3
        )
      }
    }
  }
}

@Composable
fun ProbeCard(probe: ProbeData, onSave: () -> Unit, modifier: Modifier = Modifier, isTab3: Boolean) {
  Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
    Column(modifier = Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.Top) {
      Text(text = "Adjust ${probe.name}", fontSize = if (isTab3) 18.sp else 20.sp, fontWeight = FontWeight.W600, color = Color.Black.copy(alpha = 0.70f), modifier = Modifier.padding(bottom = 8.dp))
      Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Real Temp: ", fontSize = if (isTab3) 15.sp else 17.sp)
        if (probe.realTemp != null) temperatureBox(probe.realTemp) else temperatureBox(0f)
      }

      Row(modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Adjust Temp: ", fontSize = if (isTab3) 15.sp else 17.sp)
        val adjusted = (probe.realTemp ?: 0f) + probe.adjustTemp
        temperatureBox(adjusted)
      }

      Row(modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp), horizontalArrangement = Arrangement.Center) {
        defaultCustomComposable.BuildAddMinusControl(
          true,
          { probe.onAdjustChange(probe.adjustTemp - 1) },
          true,
          { probe.onAdjustChange(probe.adjustTemp + 1) },
          probe.adjustTemp.toString()
        )
      }

      Spacer(modifier = Modifier.weight(1f))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        defaultCustomComposable.BuildButton("Save", isTab3, Color(0xFF3CB371)) { onSave() }
      }
    }
  }
}

@Composable
fun temperatureBox(temp: Float) {
  return Box(modifier = Modifier.width(100.dp).height(50.dp).background(Color.LightGray, RoundedCornerShape(16.dp)).border(2.dp, Color.DarkGray, RoundedCornerShape(16.dp)).padding(8.dp).clip(RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
    Text(String.format("%.2f°C", temp))
  }
}

data class ProbeData(val name: String, val realTemp: Float?, val adjustTemp: Float, val onAdjustChange: (Float) -> Unit)