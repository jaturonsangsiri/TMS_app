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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.siamatic.tms.models.Probe

@Composable
fun AdjustPage(paddingValues: PaddingValues) {
  val probes = remember { mutableStateOf(listOf(Probe("Fester1", 20.0f, 45.0f, 2.0f), Probe("Fester2", 20.0f, 45.0f, 2.0f))) }
  var temp1 by remember { mutableDoubleStateOf(-1.0) }
  var temp2 by remember { mutableDoubleStateOf(-1.5) }
  val context = LocalContext.current;

  Column(modifier = Modifier
    .padding(paddingValues)
    .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Row {
      probes.value.forEachIndexed { index, probe ->
        Card(modifier = Modifier
          .weight(0.5f)
          .fillMaxHeight(0.9f)
          .padding(50.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
          Column(modifier = Modifier
            .fillMaxSize()
            .padding(15.dp), verticalArrangement = Arrangement.Top) {
            Text(text = "Adjust Probe ${index + 1}", fontSize = 18.sp, fontWeight = FontWeight.W500)
            Spacer(modifier = Modifier.height(15.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "Real Temp${index + 1}: ", fontSize = 15.sp)
              TextField(probe.temperature.toString()) { newValue ->
                val updatedProbes = probes.value.toMutableList()
                updatedProbes[index] = probe.copy(temperature = newValue.toFloatOrNull() ?: 0f)
                probes.value = updatedProbes
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "Adjust Temp${index + 1}: ", fontSize = 15.sp)
              TextField(probe.temperature.toString()) { newValue ->
                val updatedProbes = probes.value.toMutableList()
                updatedProbes[index] = probe.copy(temperature = newValue.toFloatOrNull() ?: 0f)
                probes.value = updatedProbes
              }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
              defaultCustomComposable.BuildAddMinusControl(true, { if (probe.name == "Fester1") temp1 -= 1 else temp2 -= 1 }, true, { if (probe.name == "Fester1") temp1 += 1 else temp2 += 1 }, if (probe.name == "Fester1") temp1.toString() else temp2.toString())
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
              defaultCustomComposable.BuildButton("Save", Color(0xFF3CB371), onClick = { Toast.makeText(context, "Saved Data ${index + 1}", Toast.LENGTH_SHORT).show() })
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TextField(value: String, callback: (String) -> Unit) {
  OutlinedTextField(
    value = value,
    onValueChange = callback,
    label = { Text("") },
    modifier = Modifier.width(100.dp),
    shape = RoundedCornerShape(6.dp),
    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = com.siamatic.tms.ui.theme.BabyBlue, focusedLabelColor = com.siamatic.tms.ui.theme.BabyBlue, cursorColor = com.siamatic.tms.ui.theme.BabyBlue),
    keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Number ),
    singleLine = true,
  )
}