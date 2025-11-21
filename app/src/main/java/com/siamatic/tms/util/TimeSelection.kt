package com.siamatic.tms.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siamatic.tms.R
import com.siamatic.tms.defaultCustomComposable

@Composable
fun TimeSelectDialog(
  initialHour: Int,
  initialMinute: Int,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var hour by remember { mutableIntStateOf(initialHour) }
  var minute by remember { mutableIntStateOf(initialMinute) }

  Dialog(onDismissRequest = { onDismiss() }) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      tonalElevation = 6.dp,
      color = Color.White
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {

        Text("Select Time", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {

          // ------------------- HOUR ----------------------
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hour", fontSize = 16.sp, fontWeight = FontWeight.W500)

            Spacer(Modifier.height(8.dp))

            Row {
              defaultCustomComposable.BuildAddMinusControl(
                true,
                { hour = if (hour > 0) hour - 1 else 23 },
                true,
                { hour = if (hour < 23) hour + 1 else 0 },
                hour.toString().padStart(2, '0')
              )
            }
          }

          // ------------------- MINUTE ----------------------
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Minute", fontSize = 16.sp, fontWeight = FontWeight.W500)

            Spacer(Modifier.height(8.dp))

            Row {
              defaultCustomComposable.BuildAddMinusControl(
                true,
                { minute = if (minute > 0) minute - 5 else 55 },
                true,
                { minute = if (minute < 54) minute + 5 else 0 },
                minute.toString().padStart(2, '0')
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Button(
            onClick = {
              val time = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
              onConfirm(time)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.7f))
          ) {
            Text("Confirm")
          }

          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.colorRed))
          ) {
            Text("Cancel")
          }
        }
      }
    }
  }
}
