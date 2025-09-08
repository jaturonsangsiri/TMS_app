package com.siamatic.tms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController

import com.siamatic.tms.composables.DefaultCustomComposable
import com.siamatic.tms.configs.Routes
import com.siamatic.tms.ui.theme.TMSTheme

val defaultCustomComposable = DefaultCustomComposable()

open class MainActivity : ComponentActivity() {
//  private val readSB = ArrayList<Byte>()
//  private var isReading = false
//  private var handlerThread: Thread? = null
//
//  // Use FT311 UART Communication
//  private var uartInterface: FT311UARTInterface? = null
//
//  /* allocate buffer */
//  private var writeBuffer = ByteArray(64)
//  var readBuffer = ByteArray(4096)
//  /* Check length byte send from hardware */
//  var actualNumBytes = IntArray(1)
//
//  // Probe1 temperature
//  private var fTemp1: Float? = null
//  // Probe2 temperature
//  private var fTemp2: Float? = null
//


//  private var tempMaxRange = 0f
//  private var tempMinRange = 0f
//  // Count error times getting teperature
//  private var countTempError = 0
//
//  private lateinit var handlerAppendData: Handler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

//    uartInterface = FT311UARTInterface().apply {
//      initUartConnection(this@MainActivity)
//      initAccessory()
//      // Set UART Config
//      SetConfig()
//    }
    //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

//    val folder = File(Environment.getExternalStorageDirectory().toString() + File.separator + "TMS")
//    var success = true
//    if (!folder.exists()) {
//      success = folder.mkdirs()
//    }
//    if (success) {
//      Toast.makeText(this, "Directory Created", Toast.LENGTH_SHORT).show()
//    } else {
//      Toast.makeText(this, "Failed - Error", Toast.LENGTH_SHORT).show()
//    }

    // Handler ทำงานบน UI Thread
//    handlerAppendData = object : Handler(Looper.getMainLooper()) {
//      override fun handleMessage(msg: Message) {
//        //Log.i(debugTag, "handleMessage Start, actualNumBytes=${actualNumBytes[0]}")
//
//        if (actualNumBytes[0] > 0) {
//          // For Debug ASCII String
//          //val asciiStr = String(readBuffer, 0, actualNumBytes[0])
//          //Log.d(debugTag, "ASCII=$asciiStr")
//
//          // For Debug Heximal String
//          val hexBuilder = StringBuilder()
//          for (i in 0 until actualNumBytes[0]) {
//            hexBuilder.append(String.format("%02X ", readBuffer[i]))
//          }
//          Log.d(debugTag, "HEX=$hexBuilder")
//
//          appendData(readBuffer)
//        }
//      }
//    }

    // prepare command for MCU
//    val (commandBytes, commandLength) = FT311UARTInterface.prepareCommand("410D")
//    writeBuffer = commandBytes
//
//    // เริ่มอ่านข้อมูลใน background thread
//    isReading = true
//    handlerThread = Thread {
//      while (isReading) {
//        try {
//          uartInterface?.sendData(commandLength, writeBuffer)
//          Thread.sleep(1000) // รอให้ MCU process command
//
//          if (uartInterface != null) {
//            val status = uartInterface!!.ReadData(4096, readBuffer, actualNumBytes)
//            //Log.d(debugTag, "status: $status")
//            if (status && actualNumBytes[0] > 0) {
//              handlerAppendData.sendEmptyMessage(0)
//            }
//          }
//          Thread.sleep(2000)
//        } catch (e: InterruptedException) {
//          break
//        } catch (e: IOException) {
//          Log.e(debugTag, "Read failed: ${e.message}")
//          Thread.sleep(500) // wait before retry
//        }
//      }
//    }
//    handlerThread?.start()

    setContent {
      TMSTheme {
        val navController = rememberNavController()
        Routes(navController)
      }
    }
  }

//  fun appendData(packetData: ByteArray) {
//    try {
//      readSB.addAll(packetData.toList()) // เติม buffer
//
//      while (true) {
//        val packet = extractPacket(readSB) ?: break
//
//        val sensorType = packet[1].toInt().toChar()
//        val (maxTemp, minTemp) = FT311UARTInterface.maxminTemp(sensorType)
//        tempMaxRange = maxTemp
//        tempMinRange = minTemp
//
//        fun parseTemp(high: Byte, low: Byte): Int = ((high.toInt() and 0xFF) shl 8) or (low.toInt() and 0xFF)
//        val iTemp1 = parseTemp(packet[5], packet[6])
//        val iTemp2 = parseTemp(packet[8], packet[9])
//
//        if (iTemp1 != 0xFFFF) fTemp1 = FT311UARTInterface.calculateTemperature(sensorType, iTemp1)
//        if (iTemp2 != 0xFFFF) fTemp2 = FT311UARTInterface.calculateTemperature(sensorType, iTemp2)
//
//        repeat(packet.size) { readSB.removeAt(0) } // ลบ packet ที่ใช้แล้ว
//      }
//
//      // Log summary
//      Log.d(debugTag, "Temp probe1: ${fTemp1 ?: "⚠ Not found"} °C")
//      Log.d(debugTag, "Temp probe2: ${fTemp2 ?: "⚠ Not found"} °C")
//      Log.d(debugTag, "Range: $tempMinRange°C ~ $tempMaxRange°C")
//    } catch (e: Exception) {
//      Log.e(debugTag, "Exception in appendData", e)
//    }
//  }

 /* *//**
   * ดึง packet สมบูรณ์จาก buffer
   *//*
  private fun extractPacket(buffer: MutableList<Byte>): ByteArray? {
    val startIndex = buffer.indexOf(0x41.toByte()) // 'A' = 0x41
    if (startIndex == -1 || buffer.size - startIndex < 12) return null

    val endIndex = startIndex + 11
    if (buffer[endIndex] != 0x0D.toByte()) return null

    return buffer.subList(startIndex, startIndex + 12).toByteArray()
  }*/

//  private fun conditionTemp() {
//    if (fTemp1!! < tempMaxRange && fTemp1!! > tempMinRange) {
//      // reset countTempError
//      countTempError = 0
//      //checkRangeTemp()
//
//      //Clean Read Bytes Field
//      //tmpSB!!.delete(0, tmpSB!!.length)
//    } else if (fTemp1 == tempNotSensor) {
//      tempAdjusted = ""
//      val text = "-- . --"
//    } else {
//      countTempError++
//      //MainFragment.ledBlink()
//
//      if (countTempError % 10 == 0) {
//        // prepare reset command for MCU
//        val (commandBytes, commandLength) = FT311UARTInterface.prepareCommand("520D")
//        uartInterface?.sendData(commandLength, commandBytes)
//      }
//    }
//  }

//  // Destroy service, background work prevent from crash
//  override fun onDestroy() {
//    super.onDestroy()
//    isReading = false
//
//    handlerThread?.interrupt()
//    handlerThread = null
//
//    uartInterface?.cleanUp()
//    uartInterface = null
//    countTempError = 0
//
//    // clear append data Handler
//    handlerAppendData.removeCallbacksAndMessages(null)
//  }
}