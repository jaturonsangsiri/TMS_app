package com.siamatic.tms.pages

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.siamatic.tms.constants.tabsName
import kotlinx.coroutines.launch

@Composable
fun HomePage(controlRoute: NavHostController) {
  val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabsName.size }, initialPageOffsetFraction = 0f)
  val coroutineScope = rememberCoroutineScope()
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  LaunchedEffect(pagerState.settledPage) {
    selectedTabIndex = pagerState.settledPage
  }

  Scaffold(
    containerColor = Color(0xFF29292B),
    topBar = {
      TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color(0xFF343434),
        contentColor = Color.White,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
            color = Color.White,
            height = 3.dp
          )
        }
      ) {
        tabsName.forEachIndexed { index, title ->
          Tab(
            selected = selectedTabIndex == index,
            onClick = {
              coroutineScope.launch {
                pagerState.scrollToPage(index)
                selectedTabIndex = index
              }
            },
            text = {
              Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }
    }
  ) { paddingValues ->
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize(),
      key = { page -> page }
    ) { page ->
      when(page) {
        0 -> MainPage(paddingValues)
        1 -> GraphPage(paddingValues)
        2 -> DataTable(paddingValues)
        3 -> SetUpDevice(paddingValues)
        4 -> MessagePage(paddingValues)
        5 -> ManageSim(paddingValues)
        6 -> AdjustPage(paddingValues)
        7 -> ReportPage(paddingValues)
        8 -> ExitPage(paddingValues)
      }
    }
  }
}
