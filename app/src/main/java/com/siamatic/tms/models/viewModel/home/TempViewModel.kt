package com.siamatic.tms.models.viewModel.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.P1_ADJUST_TEMP
import com.siamatic.tms.constants.P2_ADJUST_TEMP
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
  // Flow สำหรับรับค่า temp ล่าสุด
  private val _latestTemp = MutableStateFlow<Pair<Float?, Float?>>(null to null)
  val latestTemp: StateFlow<Pair<Float?, Float?>> = _latestTemp

  private val _interval = MutableStateFlow(5 * 60 * 1000L)
  val interval: StateFlow<Long?> = _interval

  // Get all temperature store in database
  private val _allTemps = MutableStateFlow<List<Temp>>(emptyList())
  val allTemps: StateFlow<List<Temp>> = _allTemps.asStateFlow()

  init {
    startTempTimer()
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

  // Timer to save temperature to database (default 5 minutes)
  private fun startTempTimer() {
    timer.schedule(object: TimerTask() {
      val serialNumber = prePref.getPreference(DEVICE_ID, "String", "").toString()
      val sheetId = prePref.getPreference(SHEET_ID, "String", "").toString()
      val maxTemp1 = prePref.getPreference(TEMP_MAX_P1, "Float", 22f).toString().toFloat()
      val minTemp1 = prePref.getPreference(TEMP_MIN_P1, "Float", 0f).toString().toFloat()
      val maxTemp2 = prePref.getPreference(TEMP_MAX_P2, "Float", 22f).toString().toFloat()
      val minTemp2 = prePref.getPreference(TEMP_MIN_P2, "Float", 0f).toString().toFloat()
      val adjTemp1 = prePref.getPreference(P1_ADJUST_TEMP, "Float", -1.0f).toString().toFloatOrNull() ?: -1.0f
      val adjTemp2 = prePref.getPreference(P2_ADJUST_TEMP, "Float", -1.0f).toString().toFloatOrNull() ?: -1.0f
      val ipAddress = defaultCustomComposable.getDeviceIP().toString()

      override fun run() {
        viewModelScope.launch(Dispatchers.IO) {
          val roundedTemp1 = _latestTemp.value.first?.let { "%.2f".format(it).toFloat() }
          val roundedTemp2 = _latestTemp.value.second?.let { "%.2f".format(it).toFloat() }
          val date = defaultCustomComposable.convertLongToDateOnly(System.currentTimeMillis())
          val time = defaultCustomComposable.convertLongToTime(System.currentTimeMillis())
          val isOnline = checkForInternet(context = application)

          // Get offline temps
          val offlineTemps = getAllOfflineTemps()
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

  override fun onCleared() {
    super.onCleared()
    timer.cancel()
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

  // Reset all records
  fun resetData() {
    viewModelScope.launch {
      tempDao.resetData()
    }
  }
}