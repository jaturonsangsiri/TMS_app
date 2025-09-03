package com.siamatic.tms.composables.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TemperatureValue(temperature: Float?, modifier: Modifier = Modifier) {
  val tem: List<String> = if (temperature != null) {
    String.format("%.2f", temperature).split(".")
  } else {
    listOf("--", "--")
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.Center
  ) {
    Text(
      tem[0],
      fontSize = 80.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White.copy(alpha = 0.7f)
    )
    Box(
      modifier = Modifier
        .padding(horizontal = 8.dp)
        .offset(y = (-12).dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.7f))
      )
    }
    Text(
      "${tem[1]}°C", // แสดงทศนิยม 2 ตำแหน่ง หรือ "--"
      fontSize = 80.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White.copy(alpha = 0.7f)
    )
  }
}
