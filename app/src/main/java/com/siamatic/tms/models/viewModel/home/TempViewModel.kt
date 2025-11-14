package com.siamatic.tms.models.viewModel.home

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.R
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.DEVICE_NAME1
import com.siamatic.tms.constants.DEVICE_NAME2
import com.siamatic.tms.constants.IS_MESSAGE_REPEAT
import com.siamatic.tms.constants.IS_MUTE
import com.siamatic.tms.constants.IS_SEND_MESSAGE
import com.siamatic.tms.constants.MESSAGE_REPEAT
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
import com.siamatic.tms.constants.RECORD_INTERVAL
import com.siamatic.tms.constants.RETURN_TO_NORMAL
import com.siamatic.tms.constants.SEND_MESSAGE
import com.siamatic.tms.constants.SHEET_ID
import com.siamatic.tms.constants.TEMP_MAX_P1
import com.siamatic.tms.constants.TEMP_MAX_P2
import com.siamatic.tms.constants.TEMP_MIN_P1
import com.siamatic.tms.constants.TEMP_MIN_P2
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.constants.minOptionsLng
import com.siamatic.tms.constants.plugInDoorClose
import com.siamatic.tms.constants.plugOutDoorClose
import com.siamatic.tms.database.DatabaseProvider
import com.siamatic.tms.database.OfflineTemp
import com.siamatic.tms.database.Temp
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.viewModel.ApiServerViewModel
import com.siamatic.tms.models.viewModel.GoogleSheetViewModel
import com.siamatic.tms.services.MqttHandler
import com.siamatic.tms.services.api_Service.api_server.MessagePattern
import com.siamatic.tms.util.checkForInternet
import com.siamatic.tms.util.sharedPreferencesClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

// Model to menage temperature 
class TempViewModel(application: Application): AndroidViewModel(application) {
  private val prePref = sharedPreferencesClass(application)
  private val googleViewModel = GoogleSheetViewModel()
  private val apiServerViewModel = ApiServerViewModel()
  private val uartViewModel = UartViewModel(application)
  private val mesaagePettern = MessagePattern()

  // AC Power checking
  val acPower = uartViewModel.acPower

  private val tempDao = DatabaseProvider.getDatabase(application).tempDao()
  private val offlineTempDao = DatabaseProvider.getDatabase(application).offlineTempDao()

  private val timer = Timer()
  private var checkTimer = Timer()
  private var intervalTimer1: Timer? = null
  private var intervalTimer2: Timer? = null
  // Flow สำหรับรับค่า temp ล่าสุด
  val _latestTemp = MutableStateFlow<Pair<Float?, Float?>>(null to null)
  val latestTemp: StateFlow<Pair<Float?, Float?>> = _latestTemp

  // Get all temperature store in database
  private val _allTemps = MutableStateFlow<List<Temp>>(emptyList())
  val allTemps: StateFlow<List<Temp>> = _allTemps.asStateFlow()

  var serialNumber = ""; var sheetId = ""; var probeName1 = ""; var probeName2 = ""
  var maxTemp1 = 0f; var minTemp1 = 0f; var maxTemp2 = 22f; var minTemp2 = 0f; var adjTemp1 = 0f; var adjTemp2 = 0f

  var mqttHandler: MqttHandler? = null
  private val config = defaultCustomComposable.loadConfig(application)
  private val mqttHOST = config.get("MQTT_HOST").toString()
  private val mqttUsername = config.get("MQTT_USERNAME").toString()
  private val mqttPassword = config.get("MQTT_PASSWORD").toString()
  val mqttCommandTopic = config.get("MQTT_COMMAND_TOPIC").toString()
  val mqttSendTempTopic = config.get("MQTT_SEND_TEMP_TOPIC").toString()
  val ipAddress = defaultCustomComposable.getDeviceIP().toString()

  var isImmediately = prePref.getPreference(IS_SEND_MESSAGE, "Boolean", true) == true
  var immmediaMin = prePref.getPreference(SEND_MESSAGE, "Int", 5).toString().toInt()
  var isOnetime = prePref.getPreference(IS_MESSAGE_REPEAT, "Boolean", false) == true
  var repetiMin = prePref.getPreference(MESSAGE_REPEAT, "Int", 5).toString().toInt()
  var isNormal = prePref.getPreference(RETURN_TO_NORMAL, "Boolean", true) == true
  var delayFirst = if (isImmediately) 0L else immmediaMin * 60 * 1000L
  var repeatInterval = repetiMin * 60 * 1000L
  var interval = minOptionsLng[prePref.getPreference(RECORD_INTERVAL, "String", "5 minute")] ?: 300000L

  // Ismute button state
  var isMute = prePref.getPreference(IS_MUTE, "String", "false")
  // Alarm mute
  var isStartedAlarm = mutableStateOf(prePref.getPreference(IS_MUTE, "String", "false") == "true")
  // Alarm setting sound
  var mediaPlayer: MediaPlayer? = MediaPlayer.create(application, R.raw.alarm_sound).apply {
    setOnCompletionListener {
      isStartedAlarm.value = false
    }
  }

  init {
    startTempTimer()
    startCheckTemp()
    startMQTT()
  }

  // Get offline temps
  fun getAllOfflineTemps(): List<OfflineTemp>? {
    return offlineTempDao.getAll()
  }

  // Get temperature by date
  fun loadTempsByDate(date: Long) {
    viewModelScope.launch {
      tempDao.getAll(defaultCustomComposable.convertLongToDateOnly(date))?.collect { data ->
        _allTemps.value = data
      }
    }
  }

  // Updated lasted temp
  fun updateTemp(fTemp1: Float?, fTemp2: Float?) {
    _latestTemp.value = fTemp1 to fTemp2
  }

  private fun startMQTT() {
    Log.i(debugTag, "start mqttHandler $mqttHOST, client id $serialNumber, username $mqttUsername, password $mqttPassword")
    mqttHandler = MqttHandler(mqttHOST, serialNumber, mqttUsername, mqttPassword)

    // ถ้าไม่สามารถเชื่อมต่อได้ให้หยุด
    if (!mqttHandler!!.connect()) {
      Log.e(debugTag, "Cannot connect to MQTT broker. Check IP / credentials.")
    }

    // เชื่อมต่อ subscribe Topic
    mqttHandler!!.subscribe(mqttCommandTopic)
  }

  private fun startCheckTemp() {
    var isDelayFirst1 = true
    var isDelayFirst2 = true
    var isSendOneTime1 = true
    var isSendOneTime2 = true
    var wasOutOfRange1 = false
    var wasOutOfRange2 = false

    Log.i(debugTag, "isImmediately: $isImmediately," +
            " immmediaMin: $immmediaMin," +
            " isOnetime: $isOnetime," +
            " repetiMin: $repetiMin," +
            " isNormal: $isNormal," +
            " delayFirst: $delayFirst," +
            " repeatInterval: $repeatInterval")

    checkTimer.schedule(object: TimerTask() {
      val date = defaultCustomComposable.convertLongToDateOnly(System.currentTimeMillis())
      val time = defaultCustomComposable.convertLongToTime(System.currentTimeMillis())
      val isOnline = checkForInternet(application)

      override fun run() {
        getSettings()
        viewModelScope.launch(Dispatchers.IO) {
          val roundedTemp1 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
          val roundedTemp2 = _latestTemp.value.second?.let { "%.2f".format(it).toFloat() }
          val isOutOfRange1 = defaultCustomComposable.checkRangeTemperature(roundedTemp1, minTemp1, maxTemp1)
          val isOutOfRange2 = defaultCustomComposable.checkRangeTemperature(roundedTemp2, minTemp2, maxTemp2)
          isMute = prePref.getPreference(IS_MUTE, "String", "false")
          val statusMessage = if (acPower.value == true) plugInDoorClose else plugOutDoorClose

          //Log.i(debugTag, "$roundedTemp1 < $minTemp1 = ${roundedTemp1 ?: 0f < minTemp1}, $roundedTemp1 > $maxTemp1 = ${roundedTemp1 ?: 0f > maxTemp1}")
          //Log.i(debugTag, "$roundedTemp2 < $minTemp2 = ${roundedTemp2 ?: 0f < minTemp2}, $roundedTemp2 > $maxTemp2 = ${roundedTemp2 ?: 0f > maxTemp2}")

          //Log.i(debugTag, "isOutOfRange1: $isOutOfRange1, wasOutOfRange1: $wasOutOfRange1")

          // Alarm sound check & play
          if (isMute == "false") {
            if ((roundedTemp1 != null || roundedTemp2 != null) && (roundedTemp1 != _latestTemp.value.first || roundedTemp2 != _latestTemp.value.second)) {
              if (isOutOfRange1 || isOutOfRange2) {
                if (!isStartedAlarm.value) {
                  isStartedAlarm.value = true
                  mediaPlayer?.start()
                }
              }
            }
          } else {
            if ((roundedTemp1 != null || roundedTemp2 != null) && (roundedTemp1 != _latestTemp.value.first || roundedTemp2 != _latestTemp.value.second)) {
              if (!isOutOfRange1 || !isOutOfRange2) {
                if (isStartedAlarm.value) {
                  isStartedAlarm.value = false
                  mediaPlayer?.stop()
                }
              }
            }
          }

          // MQTT sending and received command!
          if (mqttHandler != null) {
            val sendData = JSONObject()
            sendData.put("probe1", roundedTemp1)
            sendData.put("probe2", roundedTemp2)

            mqttHandler!!.publish(mqttSendTempTopic, sendData.toString())
          } else {
            Log.e(debugTag, "MQTT can't create!, mqttHandler = null")
          }

          if (isOnline) {
            if (isOutOfRange1 && !wasOutOfRange1 && isDelayFirst1) {
              wasOutOfRange1 = true
              if (delayFirst > 0) {
                delay(delayFirst)
                isDelayFirst1 = false
              }
              if (isImmediately) {
                isDelayFirst1 = false
              }
            }
            if (isOutOfRange2 && !wasOutOfRange2 && isDelayFirst2) {
              wasOutOfRange2 = true
              if (delayFirst > 0) {
                delay(delayFirst)
                isDelayFirst2 = false
              }
              if (isImmediately) {
                isDelayFirst2 = false
              }
            }

            if (isOnetime && !isDelayFirst1 && isOutOfRange1 && isSendOneTime1) {
              isSendOneTime1 = false
              Log.i(debugTag, "อุณหภูมิ probe 1 อุณหภูมิเกิน")
              apiServerViewModel.notifyNotNormalTemp("Probe 1 temp is out of range", "$serialNumber(1)", statusMessage, roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, mesaagePettern.messagePatternToServer("warning", probeName1, minTemp1, maxTemp1, roundedTemp1 ?: 0f, date, time),  date, time)
            }
            if (isOnetime && !isDelayFirst2 && isOutOfRange2 && isSendOneTime2) {
              isSendOneTime2 = false
              Log.i(debugTag, "อุณหภูมิ probe 2 อุณหภูมิเกิน")
              apiServerViewModel.notifyNotNormalTemp("Probe 2 temp is out of range", "$serialNumber(2)", statusMessage, roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, mesaagePettern.messagePatternToServer("warning", probeName2, minTemp2, maxTemp2, roundedTemp2 ?: 0f, date, time),  date, time)
            }

            // 3) ถ้าค่า isOnetime = false จะแจ้งเตือนซ้ำเรื่อยๆ
            if (!isOnetime && !isDelayFirst1 && intervalTimer1 == null) {
              Log.d(debugTag, "repeatInterval: $repeatInterval")
              intervalTimer1?.cancel()
              intervalTimer1 = Timer()
              intervalTimer1?.schedule(object: TimerTask() {
                override fun run() {
                  viewModelScope.launch(Dispatchers.IO) {
                    val roundedTemp1 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
                    val isOutOfRange1 = defaultCustomComposable.checkRangeTemperature(roundedTemp1, minTemp1, maxTemp1)
                    Log.d(debugTag, "roundedTemp1: $roundedTemp1, isOutOfRange1: $isOutOfRange1, minTemp1: $minTemp1, maxTemp1: $maxTemp1")

                    if (isOutOfRange1) {
                      Log.i(debugTag, "อุณหภูมิ probe 1 อุณหภูมิเกิน")
                      apiServerViewModel.notifyNotNormalTemp("Probe 1 temp is out of range", "$serialNumber(1)", statusMessage, roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, mesaagePettern.messagePatternToServer("warning", probeName1, minTemp1, maxTemp1, roundedTemp1 ?: 0f, date, time),  date, time)
                    } 
                    if (!isOutOfRange1) {
                      intervalTimer1?.cancel()
                      intervalTimer1 = null
                      Log.i(debugTag, "ออกจาก loop แจ้งเตือน probe 1")
                    }
                  }
                }
              }, 0, repeatInterval)
            }
            if (!isOnetime && !isDelayFirst2 && intervalTimer2 == null) {
              intervalTimer2?.cancel()
              intervalTimer2 = Timer()
              intervalTimer2?.schedule(object: TimerTask() {
                override fun run() {
                  viewModelScope.launch(Dispatchers.IO) {
                    val roundedTemp2 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
                    val isOutOfRange2 = defaultCustomComposable.checkRangeTemperature(roundedTemp2, minTemp2, maxTemp2)
                    Log.d(debugTag, "roundedTemp2: $roundedTemp2, isOutOfRange2: $isOutOfRange2, minTemp2: $minTemp2, maxTemp2: $maxTemp2")

                    if (isOutOfRange2) {
                      Log.i(debugTag, "อุณหภูมิ probe 2 อุณหภูมิเกิน")
                      apiServerViewModel.notifyNotNormalTemp("Probe 2 temp is out of range", "$serialNumber(2)", statusMessage, roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, mesaagePettern.messagePatternToServer("warning", probeName2, minTemp2, maxTemp2, roundedTemp2 ?: 0f, date, time),  date, time)
                    } 
                    if (!isOutOfRange2) {
                      intervalTimer2?.cancel()
                      intervalTimer2 = null
                      Log.i(debugTag, "ออกจาก loop แจ้งเตือน probe 2")
                    }
                  }
                }
              }, 0, repeatInterval)
            }

            // 4) ถ้าอุณหภูมิกลับเข้าช่วงปกติ
            if (!isOutOfRange1 && wasOutOfRange1) {
              isDelayFirst1 = true
              wasOutOfRange1 = false
              isSendOneTime1 = true
              intervalTimer1?.cancel()
              intervalTimer1 = null

              // ส่งการแจ้งเตือนหากมีการเซ็ตส่งการแจ้งเตือนเมื่ออุณหภูมิกลับสู่ช่วงปกติ isNormal
              if (isNormal) {
                Log.i(debugTag, "ส่งการแจ้งเตือนอุณหภูมิ probe 1 กลับเข้าสู่ช่วงปกติ")
                apiServerViewModel.notifyNotNormalTemp("Probe 1 is returned to normal", "$serialNumber(1)", statusMessage, roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, mesaagePettern.messagePatternToServer("normal", probeName1, minTemp1, maxTemp1, roundedTemp1 ?: 0f, date, time),  date, time)
              }
            }
            if (!isOutOfRange2 && wasOutOfRange2) {
              isDelayFirst2 = true
              wasOutOfRange2 = false
              isSendOneTime2 = true
              intervalTimer2?.cancel()
              intervalTimer2 = null

              // ส่งการแจ้งเตือนหากมีการเซ็ตส่งการแจ้งเตือนเมื่ออุณหภูมิกลับสู่ช่วงปกติ isNormal
              if (isNormal) {
                Log.i(debugTag, "ส่งการแจ้งเตือนอุณหภูมิ probe 2 กลับเข้าสู่ช่วงปกติ")
                apiServerViewModel.notifyNotNormalTemp("Probe 2 is returned to normal", "$serialNumber(2)", statusMessage, roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, mesaagePettern.messagePatternToServer("normal", probeName2, minTemp2, maxTemp2, roundedTemp2 ?: 0f, date, time),  date, time)
              }
            }
          }
        }
      }
      }, 0, 5000
    )
  }

  // Timer to save temperature to database (default 5 minutes)
  private fun startTempTimer() {
    apiServerViewModel.initApiServer(application)
    timer.schedule(object: TimerTask() {
      override fun run() {
        getSettings()
        viewModelScope.launch(Dispatchers.IO) {
          val roundedTemp1 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
          val roundedTemp2 = _latestTemp.value.second?.let { "%.2f".format(it).toFloat() }
          val date = defaultCustomComposable.convertLongToDateOnly(System.currentTimeMillis())
          val time = defaultCustomComposable.convertLongToTime(System.currentTimeMillis())
          val isOnline = checkForInternet(application)

          // Get offline temps
          val offlineTemps = getAllOfflineTemps()
          // Add offline temp to google sheet
          if (!offlineTemps.isNullOrEmpty() && isOnline) {
            for (offlineTemp in offlineTemps) {
              if (offlineTemp.temp1 != null && offlineTemp.temp2 != null) {
                val addedP1 = addData(sheetId, serialNumber, probeName1, offlineTemp.temp1, offlineTemp.temp1 - adjTemp1, minTemp1, maxTemp1, adjTemp1, ipAddress, date, time, "$serialNumber(1)")
                val addedP2 = addData(sheetId, serialNumber, probeName2, offlineTemp.temp2, offlineTemp.temp2 - adjTemp2, minTemp2, maxTemp2, adjTemp2, ipAddress, date, time, "$serialNumber(2)")

                // If successfully uploaded, log it
                if (addedP1 && addedP2) {
                  offlineTempDao.deleteById(offlineTemp.id)
                } else {
                  Log.e(debugTag, "Failed to upload offline temp!")
                  continue
                }
              }
            }

            Log.i(debugTag, "Offline temp uploaded")
          }

          // if can connect internet
          if ((roundedTemp1 != null || roundedTemp2 != null) && isOnline) {
            insertTemp(roundedTemp1, roundedTemp2)
            addData(sheetId, serialNumber, probeName1, roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, minTemp1, maxTemp1, adjTemp1, ipAddress, date, time, "$serialNumber(1)")
            addData(sheetId, serialNumber, probeName2, roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, minTemp2, maxTemp2, adjTemp2, ipAddress, date, time, "$serialNumber(2)")
            Log.i(debugTag, "New temp recorded at ${defaultCustomComposable.convertLongToTime(System.currentTimeMillis())}: fTemp1=$roundedTemp1, fTemp2=$roundedTemp2")
          } else if ((roundedTemp1 != null || roundedTemp2 != null) && !isOnline) {
            insertTemp(roundedTemp1 ?: 0f, roundedTemp2 ?: 0f)
            insertOfflineTemp(roundedTemp1 ?: 0f, roundedTemp2 ?: 0f)
            Log.i(debugTag, "No internet connection. Temp saved to offline database at ${defaultCustomComposable.convertLongToTime(System.currentTimeMillis())}: fTemp1=$roundedTemp1, fTemp2=$roundedTemp2")
          } else {
            Log.w(debugTag, "No temperature data to record.")
          }
        } 
      }
    }, 0, interval
    )
  }

  private fun addData(sheetId: String, serialNumber: String, probeName: String, temp: Float, realTemp: Float, minTemp: Float, maxTemp: Float, adjTemp: Float, ipAddress: String, date: String, time: String, type: String): Boolean {
    var success = false
    val statusMessage = if (acPower.value == true) plugInDoorClose else plugOutDoorClose
    viewModelScope.launch(Dispatchers.IO) {
      // status = สภานะตู้ ซึ่งใน TMS ยังไม่มีเซ็นเซอร์ประตู
      val addedAPI = async { apiServerViewModel.addTemp(title = "Add temp to server", mcuId = "$serialNumber$type", status = statusMessage, tempValue = temp, realValue = realTemp, date = date, time = time) }
      val addedGS = async { googleViewModel.addTemperatureToGoogleSheet("add temp to google sheet", sheetId = sheetId, serialNumber = serialNumber, probe = probeName, temp = temp, acStatus = if (defaultCustomComposable.checkRangeTemperature(temp, minTemp, maxTemp)) "Warring Temp is out of range" else "Normal Temp", machineIP = ipAddress, minTemp = minTemp, maxTemp = maxTemp, adjTemp = adjTemp, dateTime = "$date $time") }
      if (addedAPI.await() == true && addedGS.await() == true) {
        success = true
      }
    }
    return success
  }

  // Add temperature to state
  fun insertTemp(fTemp1: Float?, fTemp2: Float?) {
    viewModelScope.launch {
      val date = System.currentTimeMillis()
      val record = Temp(temp1 = fTemp1, temp2 = fTemp2, timeStr = defaultCustomComposable.convertLongToTime(date), dateStr = defaultCustomComposable.convertLongToDateOnly(date))
      tempDao.insertAll(record)
    }
  }

  // Add offline temperature
  fun insertOfflineTemp(fTemp1: Float?, fTemp2: Float?) {
    viewModelScope.launch {
      val date = System.currentTimeMillis()
      val record = OfflineTemp(temp1 = fTemp1, temp2 = fTemp2, timeStr = defaultCustomComposable.convertLongToTime(date), dateStr = defaultCustomComposable.convertLongToDateOnly(date))
      offlineTempDao.insertAll(record)
    }
  }

  // Get settings
  fun getSettings() {
    serialNumber = prePref.getPreference(DEVICE_ID, "String", "").toString()
    sheetId = prePref.getPreference(SHEET_ID, "String", "").toString()
    maxTemp1 = prePref.getPreference(TEMP_MAX_P1, "Float", 22f).toString().toFloat()
    minTemp1 = prePref.getPreference(TEMP_MIN_P1, "Float", 0f).toString().toFloat()
    maxTemp2 = prePref.getPreference(TEMP_MAX_P2, "Float", 22f).toString().toFloat()
    minTemp2 = prePref.getPreference(TEMP_MIN_P2, "Float", 0f).toString().toFloat()
    adjTemp1 = prePref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
    adjTemp2 = prePref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
    isImmediately = prePref.getPreference(IS_SEND_MESSAGE, "Boolean", true) == true
    immmediaMin = prePref.getPreference(SEND_MESSAGE, "Int", 5).toString().toInt()
    isOnetime = prePref.getPreference(IS_MESSAGE_REPEAT, "Boolean", false) == true
    repetiMin = prePref.getPreference(MESSAGE_REPEAT, "Int", 5).toString().toInt()
    isNormal = prePref.getPreference(RETURN_TO_NORMAL, "Boolean", true) == true
    delayFirst = if (isImmediately) 0L else immmediaMin * 60 * 1000L
    repeatInterval = repetiMin * 60 * 1000L
    interval = minOptionsLng[prePref.getPreference(RECORD_INTERVAL, "String", "5 minute")] ?: 300000L
    probeName1 = prePref.getPreference(DEVICE_NAME1, "String", "Probe 1").toString()
    probeName1 = prePref.getPreference(DEVICE_NAME2, "String", "Probe 2").toString()
  }

  // Reset all records
  fun resetData() {
    viewModelScope.launch {
      tempDao.resetData()
    }
  }

  override fun onCleared() {
    super.onCleared()
    timer.cancel()
    checkTimer.cancel()
  }
}