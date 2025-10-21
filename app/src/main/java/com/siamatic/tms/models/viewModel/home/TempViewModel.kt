package com.siamatic.tms.models.viewModel.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.IS_MESSAGE_REPEAT
import com.siamatic.tms.constants.IS_SEND_MESSAGE
import com.siamatic.tms.constants.MESSAGE_REPEAT
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
import com.siamatic.tms.constants.RETURN_TO_NORMAL
import com.siamatic.tms.constants.SEND_MESSAGE
import com.siamatic.tms.constants.SHEET_ID
import com.siamatic.tms.constants.TEMP_MAX_P1
import com.siamatic.tms.constants.TEMP_MAX_P2
import com.siamatic.tms.constants.TEMP_MIN_P1
import com.siamatic.tms.constants.TEMP_MIN_P2
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.database.DatabaseProvider
import com.siamatic.tms.database.OfflineTemp
import com.siamatic.tms.database.Temp
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import com.siamatic.tms.models.viewModel.ApiServerViewModel
import com.siamatic.tms.models.viewModel.GoogleSheetViewModel
import com.siamatic.tms.util.checkForInternet
import com.siamatic.tms.util.sharedPreferencesClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

// Model to menage temperature 
class TempViewModel(application: Application): AndroidViewModel(application) {
  private val prePref = sharedPreferencesClass(application)
  private val googleViewModel = GoogleSheetViewModel()
  private val apiServerViewModel = ApiServerViewModel()

  private val tempDao = DatabaseProvider.getDatabase(application).tempDao()
  private val offlineTempDao = DatabaseProvider.getDatabase(application).offlineTempDao()

  private val timer = Timer()
  private var checkTimer = Timer()
  private var intervalTimer1: Timer? = null
  private var intervalTimer2: Timer? = null
  // Flow สำหรับรับค่า temp ล่าสุด
  private val _latestTemp = MutableStateFlow<Pair<Float?, Float?>>(null to null)
  val latestTemp: StateFlow<Pair<Float?, Float?>> = _latestTemp

  private val _interval = MutableStateFlow(5 * 60 * 1000L)
  val interval: StateFlow<Long?> = _interval

  // Get all temperature store in database
  private val _allTemps = MutableStateFlow<List<Temp>>(emptyList())
  val allTemps: StateFlow<List<Temp>> = _allTemps.asStateFlow()

  val serialNumber = prePref.getPreference(DEVICE_ID, "String", "").toString()
  val sheetId = prePref.getPreference(SHEET_ID, "String", "").toString()
  var maxTemp1 = prePref.getPreference(TEMP_MAX_P1, "Float", 22f).toString().toFloat()
  var minTemp1 = prePref.getPreference(TEMP_MIN_P1, "Float", 0f).toString().toFloat()
  var maxTemp2 = prePref.getPreference(TEMP_MAX_P2, "Float", 22f).toString().toFloat()
  var minTemp2 = prePref.getPreference(TEMP_MIN_P2, "Float", 0f).toString().toFloat()
  var adjTemp1 = prePref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
  var adjTemp2 = prePref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
  val ipAddress = defaultCustomComposable.getDeviceIP().toString()

  var isImmediately = prePref.getPreference(IS_SEND_MESSAGE, "Boolean", true) == true
  var immmediaMin = prePref.getPreference(SEND_MESSAGE, "Int", 0).toString().toInt()
  var isOnetime = prePref.getPreference(IS_MESSAGE_REPEAT, "Boolean", false) == true
  var repetiMin = prePref.getPreference(MESSAGE_REPEAT, "Int", 0).toString().toInt()
  var isNormal = prePref.getPreference(RETURN_TO_NORMAL, "Boolean", true) == true
  var delayFirst = if (isImmediately) 0L else immmediaMin * 60 * 1000L
  var repeatInterval = repetiMin * 60 * 1000L

  init {
    startTempTimer()
    startCheckTemp()
  }

  // Get offline temps
  fun getAllOfflineTemps(): List<OfflineTemp>? {
    return offlineTempDao.getAll()
  }

  // Get temperature by date
  fun loadTempsByDate(date: Long) {
    viewModelScope.launch {
      tempDao.getAll(defaultCustomComposable.convertLongToDateOnly(date))?.collect {
        _allTemps.value = it
      }
    }
  }

  // Updated lasted temp
  fun updateTemp(fTemp1: Float?, fTemp2: Float?, interval: Long) {
    _latestTemp.value = fTemp1 to fTemp2
    _interval.value = interval
  }

  private fun startCheckTemp() {
    var isDelayFirst1 = true
    var isDelayFirst2 = true
    var isSendOneTime1 = true
    var isSendOneTime2 = true
    var wasOutOfRange1 = false
    var wasOutOfRange2 = false

    checkTimer.schedule(object: TimerTask() {
      val date = defaultCustomComposable.convertLongToDateOnly(System.currentTimeMillis())
      val time = defaultCustomComposable.convertLongToTime(System.currentTimeMillis())
      val isOnline = checkForInternet(context = application)

      override fun run() {
        getSettings()
        viewModelScope.launch(Dispatchers.IO) {
          val roundedTemp1 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
          val roundedTemp2 = _latestTemp.value.second?.let { "%.2f".format(it).toFloat() }
          val isOutOfRange1 = defaultCustomComposable.checkRangeTemperature(roundedTemp1, minTemp1, maxTemp1)
          val isOutOfRange2 = defaultCustomComposable.checkRangeTemperature(roundedTemp2, minTemp2, maxTemp2)

          //Log.i(debugTag, "$roundedTemp1 < $minTemp1 = ${roundedTemp1 ?: 0f < minTemp1}, $roundedTemp1 > $maxTemp1 = ${roundedTemp1 ?: 0f > maxTemp1}")
          //Log.i(debugTag, "$roundedTemp2 < $minTemp2 = ${roundedTemp2 ?: 0f < minTemp2}, $roundedTemp2 > $maxTemp2 = ${roundedTemp2 ?: 0f > maxTemp2}")

          //Log.i(debugTag, "isOutOfRange1: $isOutOfRange1, wasOutOfRange1: $wasOutOfRange1")

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
              apiServerViewModel.notifyNotNormalTemp(serialNumber, "N/A", roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, "Probe 1 is out of range",  date, time)
            }
            if (isOnetime && !isDelayFirst2 && isOutOfRange2 && isSendOneTime2) {
              isSendOneTime2 = false
              Log.i(debugTag, "อุณหภูมิ probe 2 อุณหภูมิเกิน")
              apiServerViewModel.notifyNotNormalTemp(serialNumber, "N/A", roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, "Probe 2 is out of range",  date, time)
            }

            // 3) ถ้าค่า isOnetime = false จะแจ้งเตือนซ้ำเรื่อยๆ
            if (!isOnetime && !isDelayFirst1 && intervalTimer1 == null) {
              intervalTimer1 = Timer()
              intervalTimer1?.schedule(object: TimerTask() {
                override fun run() {
                  viewModelScope.launch(Dispatchers.IO) {
                    val roundedTemp1 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
                    val isOutOfRange1 = defaultCustomComposable.checkRangeTemperature(roundedTemp1, minTemp1, maxTemp1)

                    if (isOutOfRange1) {
                      Log.i(debugTag, "อุณหภูมิ probe 1 อุณหภูมิเกิน")
                      apiServerViewModel.notifyNotNormalTemp(serialNumber, "N/A", roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, "Probe 1 is out of range",  date, time)
                    } else {
                      intervalTimer1?.cancel()
                      intervalTimer1 = null
                      Log.i(debugTag, "ออกจาก loop แจ้งเตือน probe 1")
                    }
                  }
                }
              }, 0, repeatInterval)
            }
            if (!isOnetime && !isDelayFirst2 && intervalTimer2 == null) {
              intervalTimer2 = Timer()
              intervalTimer2?.schedule(object: TimerTask() {
                override fun run() {
                  viewModelScope.launch(Dispatchers.IO) {
                    val roundedTemp2 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
                    val isOutOfRange2 = defaultCustomComposable.checkRangeTemperature(roundedTemp2, minTemp2, maxTemp2)

                    if (isOutOfRange2) {
                      Log.i(debugTag, "อุณหภูมิ probe 2 อุณหภูมิเกิน")
                      apiServerViewModel.notifyNotNormalTemp(serialNumber, "N/A", roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, "Probe 2 is out of range",  date, time)
                    } else {
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
                apiServerViewModel.notifyNotNormalTemp(serialNumber, "N/A", roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, "Probe 1 is returned to normal",  date, time)
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
                apiServerViewModel.notifyNotNormalTemp(serialNumber, "N/A", roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, "Probe 2 is returned to normal",  date, time)
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
          val isOnline = checkForInternet(context = application)

          // Get offline temps
          val offlineTemps = getAllOfflineTemps()
          // Add offline temp to google sheet
          if (!offlineTemps.isNullOrEmpty() && isOnline) {
            for (offlineTemp in offlineTemps) {
              if (offlineTemp.temp1 != null && offlineTemp.temp2 != null) {
                val addedP1 = addData(sheetId, serialNumber, "Probe 1", offlineTemp.temp1, offlineTemp.temp1 - adjTemp1, minTemp1, maxTemp1, adjTemp1, ipAddress, date, time)
                val addedP2 = addData(sheetId, serialNumber, "Probe 2", offlineTemp.temp2, offlineTemp.temp2 - adjTemp2, minTemp2, maxTemp2, adjTemp2, ipAddress, date, time)

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
            addData(sheetId, serialNumber, "Probe 1", roundedTemp1 ?: 0f, (roundedTemp1 ?: 0f) - adjTemp1, minTemp1, maxTemp1, adjTemp1, ipAddress, date, time)
            addData(sheetId, serialNumber, "Probe 2", roundedTemp2 ?: 0f, (roundedTemp2 ?: 0f) - adjTemp2, minTemp2, maxTemp2, adjTemp2, ipAddress, date, time)
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
    }, 0, _interval.value
    )
  }

  private fun addData(sheetId: String, serialNumber: String, probeName: String, temp: Float, realTemp: Float, minTemp: Float, maxTemp: Float, adjTemp: Float, ipAddress: String, date: String, time: String): Boolean {
    var success = false
    viewModelScope.launch(Dispatchers.IO) {
      // status = สภานะตู้ ซึ่งใน TMS ยังไม่มีเซ็นเซอร์ประตู
      val addedAPI = async { apiServerViewModel.addTemp(mcuId = serialNumber, status = "N/A", tempValue = temp, realValue = realTemp, date = date, time = time) }
      val addedGS = async { googleViewModel.addTemperatureToGoogleSheet(sheetId = sheetId, serialNumber = serialNumber, probe = probeName, temp = temp, acStatus = defaultCustomComposable.checkTempOutOfRange(temp, minTemp, maxTemp), machineIP = ipAddress, minTemp = minTemp, maxTemp = maxTemp, adjTemp = adjTemp, dateTime = "$date $time") }
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
    maxTemp1 = prePref.getPreference(TEMP_MAX_P1, "Float", 22f).toString().toFloat()
    minTemp1 = prePref.getPreference(TEMP_MIN_P1, "Float", 0f).toString().toFloat()
    maxTemp2 = prePref.getPreference(TEMP_MAX_P2, "Float", 22f).toString().toFloat()
    minTemp2 = prePref.getPreference(TEMP_MIN_P2, "Float", 0f).toString().toFloat()
    adjTemp1 = prePref.getPreference(P1_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
    adjTemp2 = prePref.getPreference(P2_ADJUST_TEMP, "Float", 0f).toString().toFloatOrNull() ?: 0f
    isImmediately = prePref.getPreference(IS_SEND_MESSAGE, "Boolean", true) == true
    immmediaMin = prePref.getPreference(SEND_MESSAGE, "Int", 0).toString().toInt()
    isOnetime = prePref.getPreference(IS_MESSAGE_REPEAT, "Boolean", false) == true
    repetiMin = prePref.getPreference(MESSAGE_REPEAT, "Int", 0).toString().toInt()
    isNormal = prePref.getPreference(RETURN_TO_NORMAL, "Boolean", true) == true
    delayFirst = if (isImmediately) 0L else immmediaMin * 60 * 1000L
    repeatInterval = repetiMin * 60 * 1000L
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