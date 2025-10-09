package com.siamatic.tms.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.os.ParcelFileDescriptor
import android.util.Log
import com.siamatic.tms.configs.SerialConfig.BAUD_RATE
import com.siamatic.tms.configs.SerialConfig.DATA_BIT
import com.siamatic.tms.configs.SerialConfig.FLOW_CONTROL
import com.siamatic.tms.configs.SerialConfig.PARITY
import com.siamatic.tms.configs.SerialConfig.STOP_BIT
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.viewModel.home.TempViewModel
import com.siamatic.tms.services.BufferRead

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

class FT311UARTInterface() {
  val ACTION_USB_PERMISSION :String =  "com.UARTLoopback.USB_PERMISSION"

  private var globalContext: WeakReference<Context>? = null
  var usbManager: UsbManager? = null
  var usbAccessory: UsbAccessory? = null
  var mPermissionIntent: PendingIntent? = null
  var fileDescriptor: ParcelFileDescriptor? = null
  var inputStream: FileInputStream? = null
  var outputStream: FileOutputStream? = null
  var mPermissionRequestPending: Boolean = false
  // ReadThread for read data
  var readThread: ReadThread? = null

  var usbData = ByteArray(1024)
  var writeUsbData = ByteArray(256)
  var readCount: Int = 0
  var totalBytes: Int? = 0
  var writeIndex = 0
  var readIndex = 0
  /* Status is 0x00=Connect Hardware success, 0x01=Can't connect hardware */

  var READ_ENABLE: Boolean = false
  // Check if the accessory is attached
  var accessoryAttached: Boolean = false
  var readBuffer = ByteArray(MAX_BYTES)

  private val mUsbReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val action = intent?.action
      when (action) {
        ACTION_USB_PERMISSION -> {
          synchronized(this) {
            mPermissionRequestPending = false
            val accessory: UsbAccessory? = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY)
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
              if (accessory != null) {
                getContext()?.let { context -> defaultCustomComposable.showToast(context, "Allow USB Permission") }
                openAccessory(accessory)
              } else {
                getContext()?.let { context -> defaultCustomComposable.showToast(context, "Accessory is null") }
                Log.d(debugTag, "Accessory is null")
              }
            } else {
              getContext()?.let { context -> defaultCustomComposable.showToast(context, "Deny USB Permission") }
            }
          }
        }

        UsbManager.ACTION_USB_DEVICE_DETACHED -> { destroyAccessory() }

        else -> {
          Log.d("LED", "....")
        }
      }
    }
  }

  companion object {
    /* Prepare command */
    fun prepareCommand(command: String): Pair<ByteArray, Int> {
      var byteArray = ByteArray(64)

      val asciiString = BufferRead.hexToAscii(command)
      for (index in asciiString.indices) {
        byteArray[index] = asciiString[index].code.toByte()
      }
      return Pair(byteArray, asciiString.length)
    }

    /* Temperature Formula check by sensor type */
    fun calculateTemperature(sensorType: Char, iTemp: Int): Float {
      return if (sensorType == 'A') (iTemp - 4000) * 0.01f else -(iTemp * 0.01f) - 48.24f
    }

    /* Get max & min temperature by sensor type */
    fun maxminTemp(sensorType: Char):Pair<Float, Float> {
      var maxTemp: Float
      var minTemp: Float
      if (sensorType == 'A') { // Sensor type A
        maxTemp = 60f
        minTemp = -30f
      } else { // Sensor type E
        maxTemp = 40f
        minTemp = -80f
      }
      return Pair(maxTemp, minTemp)
    }

      fun appendData(packetData: ByteArray, readSB: MutableList<Byte>, onTemperatureCalculated: (Float?, Float?, Float, Float) -> Unit) {
        try {
          readSB.addAll(packetData.toList())

          var fTemp1: Float? = null
          var fTemp2: Float? = null
          var tempMinRange = 0f
          var tempMaxRange = 0f

          while (true) {
            val packet = extractPacket(readSB) ?: break

            val sensorType = packet[1].toInt().toChar()
            val (maxTemp, minTemp) = maxminTemp(sensorType)
            tempMaxRange = maxTemp
            tempMinRange = minTemp

            fun parseTemp(high: Byte, low: Byte): Int = ((high.toInt() and 0xFF) shl 8) or (low.toInt() and 0xFF)

            //Log.d(debugTag, "packet[5]: ${packet[5]}, packet[6]: ${packet[6]}, packet[8]: ${packet[8]}, packet[9]: ${packet[9]}")
            val iTemp1 = parseTemp(packet[5], packet[6])
            val iTemp2 = parseTemp(packet[8], packet[9])
            //Log.d(debugTag, "iTemp1: $iTemp1, iTemp2: $iTemp2")

            if (iTemp1 != 0xFFFF) fTemp1 = calculateTemperature(sensorType, iTemp1)
            if (iTemp2 != 0xFFFF) fTemp2 = calculateTemperature(sensorType, iTemp2)
          }

          Log.d(debugTag, "Temp probe1: ${fTemp1 ?: "⚠ Not found"} °C")
          Log.d(debugTag, "Temp probe2: ${fTemp2 ?: "⚠ Not found"} °C")
          //Log.d(debugTag, "Range: $tempMinRange°C ~ $tempMaxRange°C")

          // ส่งผลกลับผ่าน callback
          onTemperatureCalculated(fTemp1, fTemp2, tempMaxRange, tempMinRange)

        } catch (e: Exception) {
          Log.e(debugTag, "Exception in appendData", e)
        }
      }

    /**
     * ดึง packet สมบูรณ์จาก buffer
     */
    private fun extractPacket(buffer: MutableList<Byte>): ByteArray? {
      val startIndex = buffer.indexOf(0x41.toByte()) // 'A' = 0x41
      if (startIndex == -1 || buffer.size - startIndex < 12) return null

      val endIndex = startIndex + 11
      if (buffer[endIndex] != 0x0D.toByte()) return null

      // ดึง packet
      val packet = buffer.subList(startIndex, startIndex + 12).toByteArray()

      // ลบออกจาก buffer
      repeat(startIndex + 12) { buffer.removeAt(0) }

      return packet
    }

    const val MAX_BYTES = 65536
  }

  fun initUartConnection(context: Context ) {
    globalContext = WeakReference(context)
    // Get USB Lists
    usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
    var accessories: Array<UsbAccessory>? = usbManager?.accessoryList
    // Check usb exist
    if (accessories != null) getContext()?.let { context -> defaultCustomComposable.showToast(context, "Accessory Attached") } else getContext()?.let { context -> defaultCustomComposable.showToast(context, "Accessory Not Attached. Please Check!") }

    // Permission Handling
    mPermissionIntent = PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE)
    val filter = IntentFilter(ACTION_USB_PERMISSION)
    filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
    context.registerReceiver(mUsbReceiver, filter)

    // Reset input - output in buffer
    inputStream = null;
    outputStream = null;
  }

  // Set FT311 send and read config
  fun SetConfig() {
    /* prepare the baud rate buffer */
    writeUsbData[0] = BAUD_RATE.toByte()
    writeUsbData[1] = (BAUD_RATE shr 8).toByte()
    writeUsbData[2] = (BAUD_RATE shr 16).toByte()
    writeUsbData[3] = (BAUD_RATE shr 24).toByte()

    /* data bits */
    writeUsbData[4] = DATA_BIT
    /* stop bits */
    writeUsbData[5] = STOP_BIT
    /* parity */
    writeUsbData[6] = PARITY
    /* flow control */
    writeUsbData[7] = FLOW_CONTROL
    /* send the UART configuration packet */
    sendPacket(8);
  }

  /* Write Data */
  fun sendData(numBytes: Int, buffer: ByteArray):Boolean {
    var numBytes = numBytes
    if (numBytes < 1) {
      return false /* return the status with the error in the command */
    }

    // If num bytes are more than Maximum Byte Limit
    if (numBytes > 256) {
      numBytes = 256
    }

    /* prepare the packet to be sent */

    /*prepare the packet to be sent*/
    for (count in 0 until numBytes) {
      writeUsbData[count] = buffer[count]
    }

    if (numBytes != 64) {
      sendPacket(numBytes)
    } else {
      val temp = writeUsbData[63]
      sendPacket(63)
      writeUsbData[0] = temp
      sendPacket(1)
    }
    return true
  }

  /* Read Data */
  fun ReadData(numBytes: Int, buffer: ByteArray, actualNumBytes: IntArray):Boolean {
    var numBytes = numBytes
    //Log.d(debugTag, "ReadData called with numBytes=$numBytes, totalBytes=$totalBytes")

    /* Should be at least one byte to read */
    if ((numBytes <1) || (totalBytes == 0)) {
      Log.e(debugTag, "No data to read (totalBytes=$totalBytes, numBytes=$numBytes)")
      actualNumBytes[0] = 0
      return false
    }

    /* Check if current bytes is more than total bytes */
    if (numBytes > totalBytes!!) {
      //Log.w(debugTag, "numbytes > totalBytes ($numBytes > $totalBytes), adjusting...")
      numBytes = totalBytes!!
    }
  
    /* Update the number of bytes available */
    totalBytes = totalBytes!! - numBytes
    actualNumBytes[0] = numBytes

    //Log.d(debugTag, "Preparing to copy $numBytes bytes from readBuffer. New totalBytes=$totalBytes")
    /* Copy Buffer */
    for (count in 0 until numBytes) {
      buffer[count] = readBuffer[readIndex]
      readIndex++
      readIndex %= MAX_BYTES
    }

    /***
      For debug
    ***/
//    // แปลงเป็น String (ถ้าเป็น ASCII/UTF-8)
//    val dataStr = String(buffer, 0, numBytes)
//    Log.d(debugTag, "ReadData result (string): $dataStr")
//
//
//    // แปลงเป็น Hex เพื่อ debug ข้อมูล raw
//    val hexBuilder = StringBuilder()
//    for (i in 0 until numBytes) {
//      hexBuilder.append(String.format("%02X ", buffer[i]))
//    }
//    Log.d(debugTag, "ReadData result (hex): $hexBuilder")

    return true
  }

  /* method to send on USB */
  private fun sendPacket(numBytes: Int) {
    try {
      if (outputStream != null) {
        outputStream!!.write(writeUsbData, 0, numBytes)
      }
    } catch (error: IOException) {
      error.printStackTrace()
    }
  }

  // Init accessory to connect to serialport RS232 to get temperature data
  fun initAccessory(): Int {
    if (inputStream != null && outputStream != null) {
      return 1;
    }

    val accessories: Array<UsbAccessory>? = usbManager?.accessoryList
    if (accessories != null) {
      getContext()?.let { context -> defaultCustomComposable.showToast(context, "Accessory Attached") }
      Log.d(debugTag, "Accessory Attached")
    } else {
      accessoryAttached = false
      return 2
    }

    // Get accessory and Manufacturer version
    val accessory: UsbAccessory? = if (accessories != null) accessories[0] else null
    if (accessory != null) {
      if (accessory.toString().indexOf("mManufacturer=FTDI") == -1) {
        manufacNotMatch()
        return 1
      }

      if (accessory.toString().indexOf("mModel=FTDIUARTDemo") == -1 && accessory.toString().indexOf("mModel=Android Accessory FT312D") == -1) {
        manufacNotMatch()
        return 1
      }

      if (accessory.toString().indexOf("mVersion=1.0") == -1) {
        manufacNotMatch()
        return 1
      }

      getContext()?.let { context -> defaultCustomComposable.showToast(context, "Manufacturer, Model & Version are matched!") }
      accessoryAttached = true

      if (usbManager?.hasPermission(accessory) == true) {
        openAccessory(accessory)
      } else {
        synchronized(mUsbReceiver) {
          if (!mPermissionRequestPending) {
            getContext()?.let { context -> defaultCustomComposable.showToast(context, "Request USB Permission") }
            Log.d(debugTag, "Request USB Permission")
            usbManager?.requestPermission(accessory, mPermissionIntent)
            mPermissionRequestPending = true
          }
        }
      }
    }

    return 0
  }

  // Destroy Accessory if ACCESSORY_DETACHED
  fun destroyAccessory() {
    READ_ENABLE = false // Stop reading data

    readThread?.interrupt()
    readThread = null

//    try {
//      writeUsbData[0] = 0 // send dummy data for input stream .read going
//      sendPacket(1)
//    } catch (_: Exception) {}

    //Thread.sleep(10)
    closeAccessory()
  }

  // open Accessory
  fun openAccessory(accessory: UsbAccessory) {
    fileDescriptor = usbManager?.openAccessory(accessory)
    // can open Accessory
    if (fileDescriptor != null) {
      usbAccessory = accessory
      val fd = fileDescriptor!!.fileDescriptor

      inputStream = FileInputStream(fd)
      outputStream = FileOutputStream(fd)

      /* Check if any of them are null */
      if (inputStream == null || outputStream == null) {
        Log.e(debugTag, "Can't open input or output stream!")
      }

      if (!READ_ENABLE) {
        READ_ENABLE = true
        readThread = ReadThread(inputStream)
        readThread!!.start()
      }
    } else {
      Log.e(debugTag, "File descriptor == null");
    }
  }

  // Close the Accessory
  private fun closeAccessory() {
    try { fileDescriptor?.close() } catch (e: IOException) {
      Log.e(debugTag, "Error closing fileDescriptor: ${e.message}")
    } finally { fileDescriptor = null }

    try { inputStream?.close() } catch (e: IOException) {
      Log.e(debugTag, "Error closing inputStream: ${e.message}")
    } finally { inputStream = null }

    try { outputStream?.close() } catch (e: IOException) {
      Log.e(debugTag, "Error closing outputStream: ${e.message}")
    } finally { outputStream = null }
  }

  /* USB input data handler */
  inner class ReadThread(private var instream: FileInputStream?) : Thread() {
    init {
      this.priority = MAX_PRIORITY
    }

    override fun run() {
      while (READ_ENABLE) {
        while (totalBytes!! > (MAX_BYTES - 1024)) {
          try {
            sleep(50)
          } catch (e: InterruptedException) {
            e.printStackTrace()
          }
        }

        if (instream != null) {
          try {
            readCount = instream?.read(usbData, 0, 1024) ?: -1
            if (readCount > 0) {
              for (count in 0 until readCount) {
                readBuffer[writeIndex] = usbData[count]
                writeIndex++
                writeIndex %= MAX_BYTES
              }

              totalBytes = if (writeIndex >= readIndex) writeIndex - readIndex else (MAX_BYTES - readIndex) + writeIndex

              //Log.d(debugTag, "Received data: " + String(usbData, 0, readCount))
            } else if (readCount == 0) {
              sleep(10)
            } else {
              // readcount == -1 means EOF (stream closed)
              Log.e(debugTag, "End of stream reached (read == -1)")
              break // or handle accordingly
            }
          } catch (e: IOException) {
            Log.e(debugTag, "USB disconnected", e)
            destroyAccessory()
            break
          }
        }
      }
    }
  }

  // Helper function to get context safely ** This prevents from memory leaks **
  private fun getContext(): Context? {
    return globalContext?.get()
  }

  // Show manufacturer not matched message
  private fun manufacNotMatch() {
    getContext()?.let { context -> defaultCustomComposable.showToast(context, "Manufacturer is not matched!") }
    Log.d(debugTag, "Manufacturer is not matched!")
  }

  // Clean up for prevent memory leaks
  fun cleanUp() {
    // stop the loop ReadThread
    READ_ENABLE = false
    readThread?.interrupt()
    readThread = null

    // Close USB Accessory
    closeAccessory()

    // Unregister the data receiver prevent memory leak
    getContext()?.let { context ->
      try {
        context.unregisterReceiver(mUsbReceiver)
      } catch (e: IllegalArgumentException) {
        Log.e(debugTag, "Receiver not registered or already unregistered")
      }
    }

    // clear context
    globalContext?.clear()
    globalContext = null
  }
}