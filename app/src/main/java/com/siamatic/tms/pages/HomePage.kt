package com.siamatic.tms.pages

import com.siamatic.tms.models.viewModel.home.UartViewModel
import android.annotation.SuppressLint
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.siamatic.tms.constants.DEVICE_API_TOKEN
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.EMAIL_PASSWORD
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
import com.siamatic.tms.constants.RECORD_INTERVAL
import com.siamatic.tms.constants.SHEET_ID
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.constants.minOptionsLng
import com.siamatic.tms.constants.tabsName
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.viewModel.home.TempViewModel
import com.siamatic.tms.util.sharedPreferencesClass
import kotlinx.coroutines.launch

@SuppressLint("ContextCastToActivity")
@Composable
fun HomePage(controlRoute: NavHostController) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabsName.size })
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val sharedPref = sharedPreferencesClass(context)

  val uartViewModel: UartViewModel = viewModel()
  val tempViewModel: TempViewModel = viewModel()

  val fTemp1 by uartViewModel.fTemp1.collectAsState()
  val fTemp2 by uartViewModel.fTemp2.collectAsState()

  val tag = sharedPref.getPreference(RECORD_INTERVAL, "String", "5 minute")

  // Call UART init only one time. Prevent call every time slide page
  LaunchedEffect(Unit) {
    // Reset data for debug (record to many)
    //tempViewModel.resetData()

    val config = defaultCustomComposable.loadConfig(context)
    //Log.d(debugTag, "DEVICE_ID: ${config?.get("SN_DEVICE_KEY")}, SHEET_ID: ${config?.get("SHEET_ID")}, EMAIL_PASSWORD: ${config?.get("EMAIL_PASSWORD")}, DEVICE_API_TOKEN: ${config?.get("DEVICE_API_TOKEN")}")
    sharedPref.savePreference(DEVICE_ID, config?.get("SN_DEVICE_KEY"))
    sharedPref.savePreference(SHEET_ID, config?.get("SHEET_ID"))
    sharedPref.savePreference(EMAIL_PASSWORD, config?.get("EMAIL_PASSWORD"))
    sharedPref.savePreference(DEVICE_API_TOKEN, config?.get("DEVICE_API_TOKEN"))

    uartViewModel.initUart(context)
  }

  // Sync pager state with selected tab
  LaunchedEffect(pagerState.currentPage) {
    selectedTabIndex = pagerState.currentPage
    uartViewModel.setCurrentPage(selectedTabIndex)
  }

  LaunchedEffect(fTemp1, fTemp2) {
    val tempAdjust1 = sharedPref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
    val tempAdjust2 = sharedPref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f

    // Update real temperature (temperature + adjust)
    if (fTemp1 != null && fTemp2 != null) {
      minOptionsLng[tag]?.let {
        tempViewModel.updateTemp(fTemp1!! + tempAdjust1, fTemp2!! + tempAdjust2, it)
      }
    } else {
      tempViewModel.updateTemp(null, null, 300000)
    }
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
        6 -> AdjustPage(paddingValues, fTemp1, fTemp2)
        7 -> ExitPage(paddingValues)
      }
    }
  }
}