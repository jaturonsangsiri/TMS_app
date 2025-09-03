package com.siamatic.tms.pages

import com.siamatic.tms.models.viewModel.home.UartViewModel
import android.annotation.SuppressLint
import android.app.Application
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.siamatic.tms.constants.tabsName
import kotlinx.coroutines.launch

@SuppressLint("ContextCastToActivity")
@Composable
fun HomePage(controlRoute: NavHostController) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabsName.size })
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val uartViewModel: UartViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as Application))

  val fTemp1 by uartViewModel.fTemp1.collectAsState()
  val fTemp2 by uartViewModel.fTemp2.collectAsState()

  // Sync pager state with selected tab
  LaunchedEffect(pagerState.currentPage) {
    selectedTabIndex = pagerState.currentPage

    // Init UART fuction
    uartViewModel.initUart(context)
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
        //0 -> MainPage(paddingValues, fTemp1, fTemp2)
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