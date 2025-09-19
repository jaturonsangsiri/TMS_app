package com.siamatic.tms.models.viewModel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.database.DatabaseProvider
import com.siamatic.tms.database.Temp
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.Probe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Model to menage temperature 
class TempViewModel(application: Application): AndroidViewModel(application) {
  private val tempDao = DatabaseProvider.getDatabase(application).tempDao()

  // Get all temperature store in database
  private val _allTemps = MutableStateFlow<List<Temp>>(emptyList())
  val allTemps: StateFlow<List<Temp>> = _allTemps.asStateFlow()

  val _allProbes = MutableStateFlow(List(2) { index -> Probe(name = "Probe ${index + 1}") })
  val allProbes: StateFlow<List<Probe>> = _allProbes.asStateFlow()

  init {
    val currentDate = defaultCustomComposable.convertLongToDateOnly(System.currentTimeMillis())
    viewModelScope.launch {
      tempDao.getAll(currentDate)?.collect {
        _allTemps.value = it
      }
    }
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