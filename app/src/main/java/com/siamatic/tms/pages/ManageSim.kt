package com.siamatic.tms.pages

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.siamatic.tms.constants.NETWORK
import com.siamatic.tms.constants.SERIAL_NO
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.util.SimManage
import com.siamatic.tms.util.sharedPreferencesClass

@Composable
fun ManageSim(paddingValues: PaddingValues) {
  val context = LocalContext.current
  val sharedPref = sharedPreferencesClass(context)
  val simManage = SimManage()

  val networkOptions = listOf("AIS","DTAC","TRUE")
  val networkName = sharedPref.getPreference(NETWORK, "String", "AIS").toString()
  val (selectedOption, onOptionSelected) = remember { mutableStateOf(if (networkName != "") networkName else networkOptions[2] ) }
  var number by remember { mutableStateOf("") }
  val numberFocus = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  // ******  For responsive ui *******
  val isTab3 = defaultCustomComposable.getDeviceHeightPixels(context)

  Column(modifier = Modifier
    .padding(paddingValues)
    .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Row {
      Card(modifier = Modifier
        .weight(0.3f)
        .height(if (isTab3) 280.dp else 260.dp)
        .padding(10.dp)) {
        Column(modifier = Modifier
          .fillMaxSize()
          .padding(10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(text = "Mobile networks", fontSize = if (isTab3) 18.sp else 20.sp, fontWeight = FontWeight.W600, color = Color.Black.copy(alpha = 0.70f))

          // Radio Button with text
          networkOptions.forEach { text ->
            defaultCustomComposable.RadioButtonCustom(text, text != selectedOption) { onOptionSelected(text) }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildButton(text = "Check Balance", isTab3, Color.Blue.copy(alpha = 0.7f), onClick = {
              Log.d(debugTag, "Network: $selectedOption")
              focusManager.clearFocus()  // Clear focus
              sharedPref.savePreference(NETWORK, selectedOption)  // Save setup data
              Toast.makeText(context, "Checking balance. Please wait", Toast.LENGTH_SHORT).show()

              makePhoneCall(context, simManage.getTelDetail(selectedOption))
            })
          }
        }
      }
      Card(modifier = Modifier
        .weight(0.7f)
        .height(if (isTab3) 280.dp else 260.dp)
        .padding(10.dp)) {
        Column(modifier = Modifier
          .fillMaxSize()
          .padding(10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(text = "Top-up", fontSize = if (isTab3) 18.sp else 20.sp, fontWeight = FontWeight.W600, color = Color.Black.copy(alpha = 0.70f))
          OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Serial") },
            placeholder = { Text("Enter number") },
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(numberFocus),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = com.siamatic.tms.ui.theme.BabyBlue, focusedLabelColor = com.siamatic.tms.ui.theme.BabyBlue, cursorColor = com.siamatic.tms.ui.theme.BabyBlue),
            keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Text ),
            singleLine = true
          )
          Text(text = "Press 14 or 16 digits pin (Do not add*)", fontSize = 15.sp)

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildButton("OK", isTab3, Color.Blue.copy(alpha = 0.7f), onClick = {
              focusManager.clearFocus()  // Clear focus
              sharedPref.savePreference(SERIAL_NO, selectedOption)  // Save setup data

              makePhoneCall(context, simManage.refillTel(selectedOption,number))
              Toast.makeText(context, "Saved number", Toast.LENGTH_SHORT).show()
            })
          }
        }
      }
    }
  }
}

fun makePhoneCall(context: Context, tel: String) {
  Log.d(debugTag, tel)
  val activity = context as? Activity ?: return

  if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE)
    != PackageManager.PERMISSION_GRANTED
  ) {
    ActivityCompat.requestPermissions(
      activity,
      arrayOf(Manifest.permission.CALL_PHONE),
      1001
    )
    return
  }

  val intent = Intent(Intent.ACTION_CALL)
  intent.data = Uri.parse("tel:${tel.replace("#", Uri.encode("#"))}")
  activity.startActivity(intent)
}