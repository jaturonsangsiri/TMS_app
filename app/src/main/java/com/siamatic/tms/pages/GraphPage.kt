package com.siamatic.tms.pages

import SmoothLineChart
import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siamatic.tms.R
import com.siamatic.tms.constants.outerBoxPadding
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.DataPoint
import com.siamatic.tms.models.Probe
import com.siamatic.tms.models.viewModel.home.TempViewModel
import com.siamatic.tms.services.AlternativeDatePickerModal
import com.siamatic.tms.services.formatDate

@SuppressLint("DefaultLocale")
@Composable
fun GraphPage(paddingValues: PaddingValues) {
  val probes = listOf(Probe("Probe 1"), Probe("Probe 2"))
  var selectedChart by remember { mutableStateOf("Probe 1") }
  var selectedDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
  var showModal by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val tempViewModel: TempViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))
  val tableData by tempViewModel.allTemps.collectAsState()

  var probe1 = mutableListOf<DataPoint>()
  var probe2 = mutableListOf<DataPoint>()
  for ((index, value) in tableData.withIndex()) {
    value.temp1?.let { probe1.add(DataPoint(x = index.toFloat(), it)) }
    value.temp2?.let { probe2.add(DataPoint(x = index.toFloat(), it)) }
  }

  val lineData = listOf(probe1, probe2)

  LaunchedEffect(selectedDate) {
    selectedDate?.let { tempViewModel.loadTempsByDate(it) }
  }

  Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    Column(modifier = Modifier.padding(outerBoxPadding)) {
      // ปุ่มสำหรับเลือกประเภทกราฟ
      Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Row(modifier = Modifier.align(Alignment.TopStart), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            probes.forEach { probe -> defaultCustomComposable.BuildTextIconButton(
              text = probe.name,
              bgColor = if (selectedChart == probe.name) Color.Blue.copy(alpha = 0.7f) else Color.Gray,
              onClick = { selectedChart = probe.name },
              painter = painterResource(id = R.drawable.thermometer),
              imageVector = null,
              iconSize = 20.dp
            )
            }
          }
          Text(formatDate(selectedDate), modifier = Modifier.align(Alignment.Center), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
          Button(modifier = Modifier.align(Alignment.TopEnd), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), onClick = { showModal = true }) {
            Text("Select date", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Blue.copy(alpha = 0.7f))
          }
        }
      }

      if (showModal) {
        AlternativeDatePickerModal(selectedDate ?: 0L, onDateSelected = { selectedDate = it }, onDismiss = { showModal = false })
      }

      // แสดงกราฟ
      Card(modifier = Modifier.fillMaxHeight(0.8f)) {
        probes.forEachIndexed { index, probe ->
          if (selectedChart == probe.name) {
            SmoothLineChart(data = lineData[index], modifier = Modifier.padding(0.dp), showGrid = true, lineColor = Color.Cyan)
          }
        }
      }

      Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row() {
          Image(painter = painterResource(id = if (selectedChart == "Probe 1") R.drawable.graph1 else R.drawable.graph2), contentDescription = "", modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("temp ${selectedChart.split(" ")[1]}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.W600)
        }
        Card( modifier = Modifier.height(60.dp).width(200.dp), colors = CardDefaults.cardColors(Color.Gray) ) {
          Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            defaultCustomComposable.BuildIconButton(onClick = {}, painter = painterResource(id = R.drawable.zoom_in), imageVector = null, iconSize = 30.dp)
            defaultCustomComposable.BuildIconButton(onClick = {}, painter = painterResource(id = R.drawable.zoom_out), imageVector = null, iconSize = 30.dp)
            defaultCustomComposable.BuildIconButton(onClick = {}, painter = painterResource(id = R.drawable.scale1), imageVector = null, iconSize = 30.dp)
          }
        }
      }
    }
  }
}