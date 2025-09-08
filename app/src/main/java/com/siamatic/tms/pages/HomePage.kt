package com.siamatic.tms.pages

import com.siamatic.tms.models.viewModel.home.UartViewModel
import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.constants.tabsName
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.viewModel.home.TempViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@SuppressLint("ContextCastToActivity")
@Composable
fun HomePage(controlRoute: NavHostController) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabsName.size })
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val uartViewModel: UartViewModel = viewModel()
  val tempViewModel: TempViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))

  val fTemp1 by uartViewModel.fTemp1.collectAsState()
  var previousTemp1 by remember { mutableStateOf<Float?>(null) } // previous temp1
  val fTemp2 by uartViewModel.fTemp2.collectAsState()
  var previousTemp2 by remember { mutableStateOf<Float?>(null) } // previous temp2
  val timer = Timer()

  // Call UART init only one time. Prevent call every time slide page
  LaunchedEffect(Unit) {
    // Reset data for debug (record to many)
    //tempViewModel.resetData()

    uartViewModel.initUart(context)
  }

  // Timer: insert/log ทุก 5 นาที
  DisposableEffect(Unit) {
    timer.schedule(object : TimerTask() {
      override fun run() {
        CoroutineScope(Dispatchers.Main).launch {
          val roundedTemp1 = fTemp1?.let { "%.2f".format(it).toFloat() }
          val roundedTemp2 = fTemp2?.let { "%.2f".format(it).toFloat() }

          if (roundedTemp1 != null || roundedTemp2 != null) {
            // Insert temp
            tempViewModel.insertTemp(roundedTemp1 ?: 0f, roundedTemp2 ?: 0f)
            // Log ข้อมูล
            Log.i(debugTag, "New temp recorded at ${defaultCustomComposable.convertLongToTime(System.currentTimeMillis())}: " + "fTemp1=${String.format("%.2f", roundedTemp1)}, " + "fTemp2=${String.format("%.2f", roundedTemp2)}")

            // อัปเดต previous (ถ้าต้องการเก็บ state ล่าสุด)
            previousTemp1 = roundedTemp1
            previousTemp2 = roundedTemp2
          }
        }
      }
    }, 0, 300_000L) // หน่วง 0 ms, รันทุก 5 นาที

    onDispose {
      timer.cancel() // ป้องกัน memory leak
    }
  }

  LaunchedEffect(fTemp1, fTemp2) {
    val roundedTemp1 = fTemp1?.let { "%.2f".format(it).toFloat() }
    val roundedTemp2 = fTemp2?.let { "%.2f".format(it).toFloat() }

    if ((roundedTemp1 != null || roundedTemp2 != null) && (roundedTemp1 != previousTemp1 || roundedTemp2 != previousTemp2)) {
      // อัปเดต previous
      previousTemp1 = roundedTemp1
      previousTemp2 = roundedTemp2
    }
  }

  // Sync pager state with selected tab
  LaunchedEffect(pagerState.currentPage) {
    selectedTabIndex = pagerState.currentPage
    uartViewModel.setCurrentPage(selectedTabIndex)
  }

  Scaffold(
    containerColor = Color(0xFF29292B),
    topBar = {
      TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color(0xFF343434),
        contentColor = Color.White,
        indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = Color.White, height = 3.dp) }
      ) {
        tabsName.forEachIndexed { index, title ->
          Tab(
            selected = selectedTabIndex == index,
            onClick = {
              selectedTabIndex = index
              coroutineScope.launch {
                pagerState.animateScrollToPage(index)
              }
            },
            text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
          )
        }
      }
    }
  ) { paddingValues ->
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
      when(page) {
        0 -> MainPage(paddingValues, fTemp1, fTemp2)
        1 -> GraphPage(paddingValues)
        2 -> DataTable(paddingValues)
        3 -> SetUpDevice(paddingValues)
        4 -> MessagePage(paddingValues)
        5 -> ManageSim(paddingValues)
        6 -> AdjustPage(paddingValues)
        7 -> ExitPage(paddingValues)
      }
    }
  }
}