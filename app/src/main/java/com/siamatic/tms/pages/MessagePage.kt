package com.siamatic.tms.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.ui.theme.BabyBlue

@Composable
fun MessagePage(paddingValues: PaddingValues) {
  val isImmediately = remember { mutableStateOf(true) }
  val immmediaMin = remember { mutableIntStateOf(5) }
  val isOnetime = remember { mutableStateOf(false) }
  val repetiMin = remember { mutableIntStateOf(10) }
  val isNormal = remember { mutableStateOf(true) }
  val context = LocalContext.current;

  Column(modifier = Modifier.padding(paddingValues).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.4f)) {
      Card(modifier = Modifier.weight(0.5f).padding(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
          val isLine = remember { mutableStateOf(true) }
          val isSMS = remember { mutableStateOf(false) }

          defaultCustomComposable.BuildCheckBox("LINE", isLine.value) { isLine.value = it }
          defaultCustomComposable.BuildCheckBox("SMS", isSMS.value) { isSMS.value = it }

          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildButton("Phone No.1", BabyBlue, onClick = { Toast.makeText(context, "Phone1", Toast.LENGTH_SHORT).show() })
            Spacer(modifier = Modifier.width(10.dp))
            defaultCustomComposable.BuildButton("Phone No.2", BabyBlue, onClick = { Toast.makeText(context, "Phone2", Toast.LENGTH_SHORT).show() })
          }
        }
      }

      Card(
        modifier = Modifier.weight(0.5f).padding(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
          ContentRadioBox("2.Repetition of message.", isOnetime.value, "One time", "Every", { isOnetime.value = true }, { isOnetime.value = false })
          Row(verticalAlignment = Alignment.CenterVertically) {
            defaultCustomComposable.BuildAddMinusControl(!isOnetime.value, { repetiMin.intValue -= 1 }, !isOnetime.value, { repetiMin.intValue += 1 }, repetiMin.intValue.toString())
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "min. warning", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f)) {
      Card(modifier = Modifier.weight(0.5f).padding(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
          ContentRadioBox("1.Sending message for the first time.", isImmediately.value, "Immediately", "After", { isImmediately.value = true }, { isImmediately.value = false })
          Row(verticalAlignment = Alignment.CenterVertically) {
           defaultCustomComposable.BuildAddMinusControl(!isImmediately.value, { immmediaMin.intValue -= 1 }, !isImmediately.value, { immmediaMin.intValue += 1 }, immmediaMin.intValue.toString())
          }
        }
      }

      Card(modifier = Modifier.weight(0.5f).padding(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
          ContentRadioBox("3.Sending message when the temperature returned to normal level.", isNormal.value, "Send", "Do not send", { isNormal.value = true }, { isNormal.value = false })
        }
      }
    }

      //Spacer(modifier = Modifier.weight(1f))
    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f), horizontalArrangement = Arrangement.Center) {
      Button(onClick = {}, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(contentColor = Color.White.copy(alpha = 0.70f), containerColor = BabyBlue)) {
        Row {
          Icon(modifier = Modifier.size(20.dp), tint = Color.White, imageVector = Icons.Default.Email, contentDescription = "")
          Spacer(Modifier.width(8.dp))
          Text("Save")
        }
      }
    }
  }
}

@Composable
fun ContentRadioBox(text: String, selected: Boolean, title1: String, title2: String, onClick1: (() -> Unit), onClick2: (() -> Unit)) {
  Text(text, modifier = Modifier.padding(bottom = 8.dp))

  defaultCustomComposable.RadioButtonCustom(title1, !selected, onClick1)
  defaultCustomComposable.RadioButtonCustom(title2, selected, onClick2)
}