package com.siamatic.tms.models.viewModel.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.database.DatabaseProvider
import com.siamatic.tms.database.Temp
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

// Model to menage temperature 
class TempViewModel(application: Application): AndroidViewModel(application) {
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
      override fun run() {
        viewModelScope.launch(Dispatchers.IO) {
          val (fTemp1, fTemp2) = _latestTemp.value
          //Log.d(debugTag, "minOptionsLng: ${minOptionsLng[tag]} milli seconds")
          val roundedTemp1 = fTemp1?.let { "%.2f".format(it).toFloat() }
          val roundedTemp2 = fTemp2?.let { "%.2f".format(it).toFloat() }

          if (roundedTemp1 != null || roundedTemp2 != null) {
            insertTemp(roundedTemp1 ?: 0f, roundedTemp2 ?: 0f)
            Log.i(debugTag, "New temp recorded at ${defaultCustomComposable.convertLongToTime(System.currentTimeMillis())}: fTemp1=${String.format("%.2f", roundedTemp1)}, fTemp2=${String.format("%.2f", roundedTemp2)}")
          }
        } 
      }
    }, 0, _interval.value)
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