package com.siamatic.tms.pages

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siamatic.tms.constants.IS_MESSAGE_REPEAT
import com.siamatic.tms.constants.IS_SEND_MESSAGE
import com.siamatic.tms.constants.MESSAGE_REPEAT
import com.siamatic.tms.constants.RETURN_TO_NORMAL
import com.siamatic.tms.constants.SEND_MESSAGE
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.ui.theme.BabyBlue
import com.siamatic.tms.util.sharedPreferencesClass

@Composable
fun MessagePage(paddingValues: PaddingValues) {
  val context = LocalContext.current
  val sharedPref = sharedPreferencesClass(context)
  val isImmediately = remember { mutableStateOf(sharedPref.getPreference(IS_SEND_MESSAGE, "Boolean", true) == true) }
  val immmediaMin = remember { mutableIntStateOf(sharedPref.getPreference(SEND_MESSAGE, "Int", 5).toString().toInt()) }
  val isOnetime = remember { mutableStateOf(sharedPref.getPreference(IS_MESSAGE_REPEAT, "Boolean", false) == true) }
  val repetiMin = remember { mutableIntStateOf(sharedPref.getPreference(MESSAGE_REPEAT, "Int", 5).toString().toInt()) }
  val isNormal = remember { mutableStateOf(sharedPref.getPreference(RETURN_TO_NORMAL, "Boolean", true) == true) }

  // ******  For responsive ui *******
  val isTab3 = defaultCustomComposable.getDeviceHeightPixels(context)

  // Log.d(debugTag, "Is send message: ${isImmediately.value}")
  // Log.d(debugTag, "send message minute: ${immmediaMin.intValue}")
  // Log.d(debugTag, "Is message repeat: ${isOnetime.value}")
  // Log.d(debugTag, "message repeat minute: ${repetiMin.intValue}")
  // Log.d(debugTag, "Is return to normal: ${isNormal.value}")

  Column(modifier = Modifier.padding(paddingValues).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Card(modifier = Modifier.fillMaxWidth().height(if (isTab3) 200.dp else 190.dp).padding(end = 14.dp, start = 14.dp, top = 9.5.dp, bottom = 7.dp)) {
      Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        ContentRadioBox("1.Sending message for the first time.", isTab3, isImmediately.value, "Immediately", "After", { isImmediately.value = true }, { isImmediately.value = false })
        Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
          defaultCustomComposable.BuildAddMinusControl(!isImmediately.value, { if (immmediaMin.intValue != 5) { immmediaMin.intValue -= 1 } }, !isImmediately.value, { immmediaMin.intValue += 1 }, immmediaMin.intValue.toString())
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth().height(if (isTab3) 200.dp else 190.dp)) {
      Card(modifier = Modifier.weight(1f).padding(end = 14.dp, start = 14.dp, top = 2.5.dp, bottom = 5.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
          ContentRadioBox("2.Repetition of message.", isTab3, isOnetime.value, "One time", "Every", { isOnetime.value = true }, { isOnetime.value = false })
          Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            defaultCustomComposable.BuildAddMinusControl(!isOnetime.value, { if (repetiMin.intValue != 5) { repetiMin.intValue -= 1 } }, !isOnetime.value, { repetiMin.intValue += 1 }, repetiMin.intValue.toString())
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "min. warning", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }

      Card(modifier = Modifier.weight(1f).padding(end = 14.dp, start = 14.dp, top = 2.5.dp, bottom = 5.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
          ContentRadioBox("3.Sending message when the temperature returned to normal level.", isTab3, isNormal.value, "Send", "Do not send", { isNormal.value = true }, { isNormal.value = false })
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      Button(onClick = {
        if (immmediaMin.intValue >= 5 && repetiMin.intValue >= 5) {
          // Save message sending settings data
//          Log.d(debugTag, "Is send message: ${isImmediately.value}")
//          Log.d(debugTag, "send message minute: ${immmediaMin.intValue}")
//          Log.d(debugTag, "Is message repeat: ${isOnetime.value}")
//          Log.d(debugTag, "message repeat minute: ${repetiMin.intValue}")
//          Log.d(debugTag, "Is return to normal: ${isNormal.value}")
          sharedPref.savePreference(IS_SEND_MESSAGE, isImmediately.value)
          sharedPref.savePreference(IS_MESSAGE_REPEAT, isOnetime.value)
          sharedPref.savePreference(SEND_MESSAGE, immmediaMin.intValue)
          sharedPref.savePreference(MESSAGE_REPEAT, repetiMin.intValue)
          sharedPref.savePreference(RETURN_TO_NORMAL, isNormal.value)

          Toast.makeText(context, "Saved message settings", Toast.LENGTH_SHORT).show()
        } else if (immmediaMin.intValue < 5) {
          Toast.makeText(context, "Immediately minute is less or equal 5!", Toast.LENGTH_SHORT).show()
        } else if (repetiMin.intValue < 5) {
          Toast.makeText(context, "Repeat minute is less or equal 5!", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
      }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(contentColor = Color.White.copy(alpha = 0.70f), containerColor = BabyBlue)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
          Icon(modifier = Modifier.size(if (isTab3) 25.dp else 30.dp), tint = Color.White, imageVector = Icons.Default.Email, contentDescription = "")
          Spacer(Modifier.width(8.dp))
          Text("Save", fontSize = if (isTab3) 18.sp else 20.sp)
        }
      }
    }
  }
}

@Composable
fun ContentRadioBox(text: String, isTab3: Boolean, selected: Boolean, title1: String, title2: String, onClick1: (() -> Unit), onClick2: (() -> Unit)) {
  Text(text, modifier = Modifier.padding(bottom = 12.dp), fontSize = 18.sp, fontWeight = FontWeight.W600, color = Color.Black.copy(alpha = 0.70f))

  defaultCustomComposable.RadioButtonCustom(title1, !selected, onClick1)
  if (!isTab3) Spacer(modifier = Modifier.height(10.dp))
  defaultCustomComposable.RadioButtonCustom(title2, selected, onClick2)
  if (!isTab3) Spacer(modifier = Modifier.height(10.dp))
}