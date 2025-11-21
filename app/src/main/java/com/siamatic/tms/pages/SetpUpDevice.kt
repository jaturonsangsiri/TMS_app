package com.siamatic.tms.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siamatic.tms.constants.DEVICE_NAME1
import com.siamatic.tms.constants.DEVICE_NAME2
import com.siamatic.tms.constants.RECORD_INTERVAL
import com.siamatic.tms.constants.TEMP_MAX_P1
import com.siamatic.tms.constants.TEMP_MAX_P2
import com.siamatic.tms.constants.TEMP_MIN_P1
import com.siamatic.tms.constants.TEMP_MIN_P2
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.constants.innerBoxPadding
import com.siamatic.tms.constants.minuteOptions
import com.siamatic.tms.constants.outerBoxPadding
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.ui.theme.BabyBlue
import com.siamatic.tms.util.sharedPreferencesClass

@Composable
fun SetUpDevice(paddingValues: PaddingValues) {
  val context = LocalContext.current
  val sharedPref = sharedPreferencesClass(context)
  var selectedOption by remember { mutableStateOf(sharedPref.getPreference(RECORD_INTERVAL, "String", "5 minute").toString()) }
  var probeName1 by remember { mutableStateOf("") }
  var probeName2 by remember { mutableStateOf("") }
  var tempMaxP1 by remember { mutableFloatStateOf(sharedPref.getPreference(TEMP_MAX_P1, "Float", 22f).toString().toFloat()) }
  var tempMinP1 by remember { mutableFloatStateOf(sharedPref.getPreference(TEMP_MIN_P1, "Float", 0f).toString().toFloat()) }
  var tempMaxP2 by remember { mutableFloatStateOf(sharedPref.getPreference(TEMP_MAX_P2, "Float", 22f).toString().toFloat()) }
  var tempMinP2 by remember { mutableFloatStateOf(sharedPref.getPreference(TEMP_MIN_P2, "Float", 0f).toString().toFloat()) }

  val pN1focus = remember { FocusRequester() }
  val pN2focus = remember { FocusRequester() }
  val tMinP1focus = remember { FocusRequester() }
  val tMaxP1focus = remember { FocusRequester() }
  val tMinP2focus = remember { FocusRequester() }
  val tMaxP2focus = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  // ******  For responsive ui *******
  val isTab3 = defaultCustomComposable.getDeviceHeightPixels(context)


  LaunchedEffect(Unit) {
    probeName1 = sharedPref.getPreference(DEVICE_NAME1, "String", "Probe 1").toString()
    probeName2 = sharedPref.getPreference(DEVICE_NAME2, "String", "Probe 2").toString()
  }

  Column(modifier = Modifier.padding(paddingValues)) {
    Row(modifier = Modifier.padding(outerBoxPadding)) {
      Card(modifier = Modifier.weight(0.3f).height(400.dp).padding(innerBoxPadding)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp).weight(0.3f).padding(innerBoxPadding)) {
          Text("Record Interval", modifier = Modifier.padding(bottom = 10.dp), fontSize = 18.sp, fontWeight = FontWeight.W600, color = Color.Black.copy(alpha = 0.70f))

          // Radio Button with text
          minuteOptions.forEach { text ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start, modifier = Modifier.height(35.dp).fillMaxWidth()
              .selectable(selected = (text == selectedOption), onClick = { selectedOption = text })) {
              RadioButton(selected = (text == selectedOption), onClick = { selectedOption = text }, modifier = Modifier.padding(end = 4.dp), colors = RadioButtonDefaults.colors(selectedColor = BabyBlue, unselectedColor = Color.DarkGray), interactionSource = remember { MutableInteractionSource() })
              Text(text = text, modifier = Modifier.padding(start = 4.dp))
            }
          }
          Spacer(modifier = Modifier.weight(1f))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildButton(text = "Save", isTab3, bgColor = BabyBlue, onClick = {
              Log.i(debugTag, "Record Interval saved: $selectedOption")
              sharedPref.savePreference(RECORD_INTERVAL, selectedOption)
              Toast.makeText(context, "Saved Interval", Toast.LENGTH_SHORT).show()
            })
          }
        }
      }
      Card(modifier = Modifier.weight(0.7f).height(400.dp).padding(innerBoxPadding)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
          Text("Alarm", fontSize = 18.sp, fontWeight = FontWeight.W600, color = Color.Black.copy(alpha = 0.70f))

          TextField("Probe name1: ", probeName1, KeyboardType.Text, pN1focus) { newValue ->
            probeName1 = newValue
          }
          Spacer(modifier = Modifier.height(15.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TempField("Over: ", tempMaxP1, tMaxP1focus) { newValue ->
              tempMaxP1 = newValue ?: 0f
            }

            TempField("Under: ", tempMinP1, tMinP1focus) { newValue ->
              tempMinP1 = newValue ?: 0f
            }
          }
          Spacer(modifier = Modifier.height(15.dp))

          TextField(
            "Probe name2: ", probeName2, KeyboardType.Text, pN2focus
          ) { newValue ->
            probeName2 = newValue
          }
          Spacer(modifier = Modifier.height(15.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TempField("Over: ", tempMaxP2, tMaxP2focus) { newValue ->
              tempMaxP2 = newValue ?: 0f
            }

            TempField("Under: ", tempMinP2, tMinP2focus) { newValue ->
              tempMinP2 =newValue ?: 0f
            }
          }
          Spacer(modifier = Modifier.weight(1f))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildButton(text = "Save", isTab3, bgColor = BabyBlue, onClick = {
              // Clear all focus
              focusManager.clearFocus()

              // Save setup data
              sharedPref.savePreference(DEVICE_NAME1, probeName1)
              sharedPref.savePreference(DEVICE_NAME2, probeName2)
              sharedPref.savePreference(TEMP_MAX_P1, tempMaxP1)
              sharedPref.savePreference(TEMP_MIN_P1, tempMinP1)
              sharedPref.savePreference(TEMP_MAX_P2, tempMaxP2)
              sharedPref.savePreference(TEMP_MIN_P2, tempMinP2)
              Toast.makeText(context, "Saved Alarm", Toast.LENGTH_SHORT).show()
            })
          }
        }
      }
    }
  }
}

@Composable
private fun TextField(title: String, value: String, keyboardType: KeyboardType, focus: FocusRequester, callback: (String) -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(title, fontSize = 18.sp)
    Spacer(modifier = Modifier.width(10.dp))
    OutlinedTextField(
      value = value,
      onValueChange = callback,
      label = { Text("") },
      textStyle = TextStyle(fontSize = 16.sp),
      modifier = Modifier.height(60.dp).focusRequester(focus),
      shape = RoundedCornerShape(6.dp),
      keyboardOptions = KeyboardOptions( keyboardType = keyboardType ),
      singleLine = true
    )
  }
}

@Composable
private fun TempField(title: String, value: Float?, focus: FocusRequester, onValueChange: (Float?) -> Unit) {
  var text by remember { mutableStateOf(value?.toString() ?: "") }

  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(title, fontSize = 18.sp)
    Spacer(modifier = Modifier.width(10.dp))
    OutlinedTextField(
      value = text,
      onValueChange = { newText ->
        text = newText
        onValueChange(newText.toFloatOrNull()) // อัปเดต probe เป็น Float?
      },
      textStyle = TextStyle(fontSize = 16.sp),
      modifier = Modifier.width(90.dp).height(53.dp).focusRequester(focus),
      shape = RoundedCornerShape(6.dp),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      singleLine = true,
    )
  }
}