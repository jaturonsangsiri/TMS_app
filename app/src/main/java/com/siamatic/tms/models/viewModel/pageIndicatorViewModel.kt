package com.siamatic.tms.models.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class PageIndicatorViewModel : ViewModel() {
  var isHomePage = mutableStateOf( false)
}