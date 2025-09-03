package com.siamatic.tms.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.siamatic.tms.R
import com.siamatic.tms.ui.theme.BabyBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(dateMillis: Long?): String {
  return if (dateMillis != null) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    sdf.format(Date(dateMillis))
  } else {
    "Select Date"
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativeDatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
  val datePickerState = rememberDatePickerState()

  androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFA9A9A9)) {
      Column(modifier = Modifier.padding(16.dp)) {
        DatePicker(state = datePickerState, title = null, modifier = Modifier.fillMaxWidth(), colors = DatePickerDefaults.colors(
          headlineContentColor = Color.White,
          //subheadContentColor = Color.White,
          navigationContentColor = Color.White,
          dayContentColor = Color.White,
          weekdayContentColor = Color.White,
          todayContentColor = Color.Blue.copy(alpha = 0.7f),
          todayDateBorderColor = Color.Blue.copy(alpha = 0.7f),
          selectedDayContentColor = Color.White,
          selectedDayContainerColor = BabyBlue,
          selectedYearContentColor = Color.White,
          selectedYearContainerColor = BabyBlue
        ))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp), horizontalArrangement = Arrangement.End) {
          Button(onClick = {
            onDateSelected(datePickerState.selectedDateMillis)
            onDismiss()
          }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.7f))) {
            Text("Select", color = Color.White)
          }
          Spacer(modifier = Modifier.width(20.dp))
          Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.colorRed))) {
            Text("Cancel", color = Color.White)
          }
        }
      }
    }
  }
}

// check endDate is Before startDate
fun checkDateIsBefore(startDate: Long?, endDate: Long?): Boolean {
  return when {
    startDate == null || endDate == null -> false
    startDate > endDate -> true
    else -> false
  }
}
