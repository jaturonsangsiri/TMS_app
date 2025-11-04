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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siamatic.tms.R
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.ui.theme.BabyBlue

@Composable
fun TemperatureValue(temperature: Float?, isOver: Boolean, modifier: Modifier = Modifier) {
  val tem: List<String> = if (temperature != null) {
    defaultCustomComposable.formatTemp(temperature).split(".")
  } else {
    listOf("--", "--")
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.Center
  ) {    Text(
      tem[0],
      fontSize = 80.sp,
      fontWeight = FontWeight.Bold,
      color = if (isOver) colorResource(id = R.color.colorRedLight) else Color.White.copy(alpha = 0.7f)
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
          .background(if (isOver) colorResource(id = R.color.colorRedLight) else Color.White.copy(alpha = 0.7f))
      )
    }
    Text(
      "${tem[1]}°C", // แสดงทศนิยม 2 ตำแหน่ง หรือ "--"
      fontSize = 80.sp,
      fontWeight = FontWeight.Bold,
      color = if (isOver) colorResource(id = R.color.colorRedLight) else Color.White.copy(alpha = 0.7f)
    )
  }
}
