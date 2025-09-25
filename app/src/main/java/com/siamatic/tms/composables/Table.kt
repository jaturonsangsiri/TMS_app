package com.siamatic.tms.composables

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siamatic.tms.models.viewModel.home.TempViewModel

@Composable
fun MainTables(startDate: Long? = 0L) {
  val context = LocalContext.current
  val tempViewModel: TempViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))
  val tableData by tempViewModel.allTemps.collectAsState()

  LaunchedEffect(startDate) {
    startDate?.let { tempViewModel.loadTempsByDate(it) }
  }

  LazyColumn(Modifier.fillMaxSize()) {
    // Headers
    item {
      Row(Modifier.background(Color.Gray)) {
        TableCell(text = "No.", weight = 0.1f, isTitle = true)
        TableCell(text = "Date", weight = 0.3f, isTitle = true)
        TableCell(text = "Time", weight = 0.2f, isTitle = true)
        TableCell(text = "Temp1", weight = 0.2f, isTitle = true)
        TableCell(text = "Temp2", weight = 0.2f, isTitle = true)
      }
    }
    // Lines in table
    itemsIndexed(tableData) { index, item ->
      Row(modifier = Modifier.fillMaxWidth().background(if (index % 2 == 0) Color.LightGray.copy(alpha = 0.1f) else Color.Transparent)) {
        TableCell(text = "${index + 1}", weight = 0.1f)
        TableCell(text = item.dateStr, weight = 0.3f)
        TableCell(text = item.timeStr, weight = 0.2f)
        TableCell(text = String.format("%.2f", item.temp1), weight = 0.2f)
        TableCell(text = String.format("%.2f", item.temp2), weight = 0.2f)
      }
    }
  }
}

// Build row function
@Composable
private fun RowScope.TableCell(text: String, weight: Float, isTitle: Boolean = false) {
  Text(text = text, Modifier.weight(weight).padding(8.dp), color = if (isTitle) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.7f))
}