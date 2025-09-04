package com.siamatic.tms.models.viewModel.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.util.FT311UARTInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UartViewModel(application: Application) : AndroidViewModel(application) {
  private val readSB = ArrayList<Byte>()
  private var isReading = false
  private var handlerThread: Thread? = null

  private var writeBuffer = ByteArray(64)
  private var readBuffer = ByteArray(4096)
  private var actualNumBytes = IntArray(1)

  private var uartInterface: FT311UARTInterface? = null

    private val _fTemp1 = MutableStateFlow<Float?>(null)
    val fTemp1 = _fTemp1.asStateFlow()

    private val _fTemp2 = MutableStateFlow<Float?>(null)
    val fTemp2 = _fTemp2.asStateFlow()

  private var previousFTemp1 = MutableStateFlow<Float?>(null) // previous temp1
  private var previousFTemp2 = MutableStateFlow<Float?>(null) // previous temp2

  private val _tempMinRange = MutableStateFlow(0f)
  val tempMinRange = _tempMinRange.asStateFlow()

  private val _tempMaxRange = MutableStateFlow(0f)
  val tempMaxRange = _tempMaxRange.asStateFlow()
  private var isInit = false

  private var currentPageIndex: Int = 0

  fun initUart(context: Context) {
    // Call UART init only one time
    if (isInit) return
    isInit = true

    uartInterface = FT311UARTInterface().apply {
      initUartConnection(context)
      initAccessory()
      SetConfig()
    }

    val (commandBytes, commandLength) = FT311UARTInterface.prepareCommand("410D")
    writeBuffer = commandBytes

    isReading = true
    handlerThread = Thread {
      while (isReading) {
        try {
          uartInterface?.sendData(commandLength, writeBuffer)
          Thread.sleep(1000)

          if (uartInterface != null) {
            val status = uartInterface!!.ReadData(4096, readBuffer, actualNumBytes)
            if (status && actualNumBytes[0] > 0) {
              processData()
            }
          }

          Thread.sleep(2000)
        } catch (e: Exception) {
          Log.e(debugTag, "UART error: ${e.message}")
          Thread.sleep(500)
        }
      }
    }
    handlerThread?.start()
  }

  private fun processData() {
    // ถ้าไม่ใช่หน้า MainPage ก็ยังต้อง parse data (กันค่าไม่อัปเดต)
    // แต่ไม่ต้อง Log
    FT311UARTInterface.appendData(readBuffer, readSB) { temp1, temp2, maxRange, minRange ->
      val prevTemp1 = _fTemp1.value
      val prevTemp2 = _fTemp2.value

      // อัปเดตเฉพาะถ้าค่าเปลี่ยน
      if (temp1 != prevTemp1) _fTemp1.value = temp1
      if (temp2 != prevTemp2) _fTemp2.value = temp2

      _tempMaxRange.value = maxRange
      _tempMinRange.value = minRange

      // ถ้าไม่ใช่หน้า MainPage ไม่ต้อง Log
      if (currentPageIndex != 0) return@appendData

      // Debug log เฉพาะตอนค่าเปลี่ยน
      if (temp1 != prevTemp1) Log.d(debugTag, "Temp probe1: ${String.format("%.2f", temp1)}°C")
      if (temp2 != prevTemp2) Log.d(debugTag, "Temp probe2: ${String.format("%.2f", temp2)}°C")
    }
  }

  // Set Page current
  fun setCurrentPage(index: Int) {
    currentPageIndex = index
  }

  // Destroy service, background work prevent from crash
  override fun onCleared() {
    super.onCleared()
    isReading = false

    handlerThread?.interrupt()
    handlerThread = null

    uartInterface?.cleanUp()
    uartInterface = null
    //countTempError = 0

    // clear append data Handler
    //handlerAppendData.removeCallbacksAndMessages(null)
  }
}