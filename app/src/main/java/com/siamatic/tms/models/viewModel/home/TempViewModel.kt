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
import com.siamatic.tms.database.Temp
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import com.siamatic.tms.models.viewModel.GoogleSheetViewModel
import com.siamatic.tms.util.sharedPreferencesClass
import kotlinx.coroutines.Dispatchers
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

  private val tempDao = DatabaseProvider.getDatabase(application).tempDao()
  private val timer = Timer()
  // Flow สำหรับรับค่า temp ล่าสุด
  private val _latestTemp = MutableStateFlow<Pair<Float?, Float?>>(null to null)
  val latestTemp: StateFlow<Pair<Float?, Float?>> = _latestTemp

  private val _interval = MutableStateFlow(5 * 60 * 1000L)
  val interval: StateFlow<Long?> = _interval

  // Get all temperature store in database
  private val _allTemps = MutableStateFlow<List<Temp>>(emptyList())
  val allTemps: StateFlow<List<Temp>> = _allTemps.asStateFlow()

  val _allProbes = MutableStateFlow(List(2) { index -> Probe(name = "Probe ${index + 1}") })
  val allProbes: StateFlow<List<Probe>> = _allProbes.asStateFlow()

  init {
    startTempTimer()
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
          val (fTemp1, fTemp2) = _latestTemp.value
          //Log.d(debugTag, "minOptionsLng: ${minOptionsLng[tag]} milli seconds")
          val roundedTemp1 = fTemp1?.let { "%.2f".format(it).toFloat() }
          val roundedTemp2 = fTemp2?.let { "%.2f".format(it).toFloat() }

          if (roundedTemp1 != null || roundedTemp2 != null) {
            insertTemp(roundedTemp1 ?: 0f, roundedTemp2 ?: 0f)
            googleViewModel.addTemperatureToGoogleSheet(
              sheetId = sheetId,
              serialNumber = serialNumber,
              probe = "Probe 1",
              temp = roundedTemp1 ?: 0f,
              acStatus = defaultCustomComposable.checkTempOutOfRange(roundedTemp1, minTemp1, maxTemp1),
              machineIP = ipAddress,
              minTemp = minTemp1,
              maxTemp = maxTemp1,
              adjTemp = adjTemp1
            )
            googleViewModel.addTemperatureToGoogleSheet(
              sheetId = sheetId,
              serialNumber = serialNumber,
              probe = "Probe 2",
              temp = roundedTemp2 ?: 0f,
              acStatus = defaultCustomComposable.checkTempOutOfRange(roundedTemp2, minTemp2, maxTemp2),
              machineIP = ipAddress,
              minTemp = minTemp2,
              maxTemp = maxTemp2,
              adjTemp = adjTemp2
            )
            Log.i(debugTag, "New temp recorded at ${defaultCustomComposable.convertLongToTime(System.currentTimeMillis())}: fTemp1=$roundedTemp1, fTemp2=$roundedTemp2")
          }
        } 
      }
    }, 0, _interval.value
    )
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

  fun updateProbeAdjustTemp(index: Int, adjustTemp: Float?) {
    if (adjustTemp != null) {
      val current = _allProbes.value.toMutableList()
      current[index] = current[index].copy(adjustTemp = adjustTemp)
      _allProbes.value = current
    }
  }

  // Reset all records
  fun resetData() {
    viewModelScope.launch {
      tempDao.resetData()
    }
  }
}