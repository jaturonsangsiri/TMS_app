package com.siamatic.tms.services

import kotlinx.coroutines.flow.MutableStateFlow

object HardwareStatusValueState {
  val acPower = MutableStateFlow(false)
  val isConnect = MutableStateFlow(false)
}