package com.siamatic.tms.pages

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siamatic.tms.defaultCustomComposable

@Composable
fun ManageSim(paddingValues: PaddingValues) {
  val networkOptions = listOf("AIS","DTAC","TRUE")
  val (selectedOption, onOptionSelected) = remember { mutableStateOf(networkOptions[2]) }
  var number by remember { mutableStateOf("") }
  val context = LocalContext.current;

  Column(modifier = Modifier.padding(paddingValues).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Row {
      Card(modifier = Modifier.weight(0.3f).height(270.dp).padding(10.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(text = "Mobile networks", fontSize = 18.sp, fontWeight = FontWeight.W500)

          // Radio Button with text
          networkOptions.forEach { text ->
            defaultCustomComposable.RadioButtonCustom(text, text != selectedOption) { onOptionSelected(text) }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildButton(text = "Check Balance", Color.Blue.copy(alpha = 0.7f), onClick = { Toast.makeText(context, "Check Balance", Toast.LENGTH_SHORT).show() })
          }
        }
      }
      Card(modifier = Modifier.weight(0.7f).height(270.dp).padding(10.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(text = "Top-up", fontSize = 18.sp, fontWeight = FontWeight.W500)
          OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Serial") },
            placeholder = { Text("Enter Serial") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = com.siamatic.tms.ui.theme.BabyBlue, focusedLabelColor = com.siamatic.tms.ui.theme.BabyBlue, cursorColor = com.siamatic.tms.ui.theme.BabyBlue),
            keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Text ),
            singleLine = true
          )
          Text(text = "Press 14 or 16 digits pin (Do not add*)", fontSize = 15.sp)

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildButton("OK", Color.Blue.copy(alpha = 0.7f), onClick = { Toast.makeText(context, "OK Clicked", Toast.LENGTH_SHORT).show() })
          }
        }
      }
    }
  }
}