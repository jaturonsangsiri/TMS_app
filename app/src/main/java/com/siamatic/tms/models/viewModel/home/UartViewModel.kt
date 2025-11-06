package com.siamatic.tms.models.viewModel.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.util.FT311UARTInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class UartViewModel(application: Application) : AndroidViewModel(application) {
  private val readSB = ArrayList<Byte>()
  private var isReading = false
  private var handlerThread: Thread? = null

  private var writeBuffer = ByteArray(64)
  private var resetBuffer = ByteArray(64)
  private var readBuffer = ByteArray(4096)
  private var actualNumBytes = IntArray(1)

  private var uartInterface: FT311UARTInterface? = null

  private val _fTemp1 = MutableStateFlow<Float?>(null)
  val fTemp1 = _fTemp1.asStateFlow()

  private val _acPower = MutableStateFlow<Boolean?>(null)
  val acPower = _acPower.asStateFlow()

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
  private var countTempError: Int = 0
  private val _isConnect = MutableStateFlow<Boolean?>(null)
  val isConnect = _isConnect.asStateFlow()

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
    val (resetCommandBytes, resetCommandLength) = FT311UARTInterface.prepareCommand("520D")
    writeBuffer = commandBytes
    resetBuffer = resetCommandBytes

    isReading = true
    handlerThread = Thread {
      while (isReading) {
        try {
          if (uartInterface != null) {
            uartInterface?.sendData(commandLength, writeBuffer)
            Thread.sleep(3000)

            val status = uartInterface!!.ReadData(4096, readBuffer, actualNumBytes)
            if (status && actualNumBytes[0] > 0) {
              _isConnect.value = true
              processData()
            } else {
              countTempError++
            }

            // If can't get temperature 3 times
            if (countTempError >= 3) {
              countTempError = 0
              _fTemp1.value = null
              _fTemp2.value = null
              // Update the connect icon in MainPage
              _isConnect.value = false

              resetHardware(context)
            }

            Thread.sleep(2000)
          }
        } catch (e: Exception) {
          Log.e(debugTag, "UART error: ${e.message}")
          Thread.sleep(500)
        }
      }
    }
    handlerThread?.start()
  }

  // Function to reset hardware & reset variables in App
  private fun resetHardware(context: Context) {
    try {
      uartInterface?.destroyAccessory() // Close the input / output stream and stop ReadThread
      Thread.sleep(3000)
      uartInterface?.initAccessory() // Re-init the Accessory
      uartInterface?.SetConfig()     // Re config UART
      countTempError = 0
    } catch (error: IOException) {
      Log.e(debugTag, "Failed to reset hardware: ${error.message}")
    }
  }

  private fun processData() {
    // ถ้าไม่ใช่หน้า MainPage ก็ยังต้อง parse data (กันค่าไม่อัปเดต)
    // แต่ไม่ต้อง Log
    FT311UARTInterface.appendData(readBuffer, readSB) { temp1, temp2, maxRange, minRange, acPower ->
      val prevTemp1 = _fTemp1.value
      val prevTemp2 = _fTemp2.value

      // อัปเดตเฉพาะถ้าค่าเปลี่ยน
      if (temp1 != prevTemp1) _fTemp1.value = temp1
      if (temp2 != prevTemp2) _fTemp2.value = temp2

      _tempMaxRange.value = maxRange
      _tempMinRange.value = minRange
      _acPower.value = acPower

      // ถ้าไม่ใช่หน้า MainPage ไม่ต้อง Log
      //if (currentPageIndex != 0) return@appendData

      // Debug log เฉพาะตอนค่าเปลี่ยน
      //if (temp1 != prevTemp1) Log.d(debugTag, "Temp probe1: ${String.format("%.2f", temp1)}°C")
      //if (temp2 != prevTemp2) Log.d(debugTag, "Temp probe2: ${String.format("%.2f", temp2)}°C")
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
    countTempError = 0

    // clear append data Handler
    //handlerAppendData.removeCallbacksAndMessages(null)
  }
}