package com.siamatic.tms.models.viewModel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.database.DatabaseProvider
import com.siamatic.tms.database.Temp
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

  init {
    viewModelScope.launch {
      tempDao.getAll().collect {
        _allTemps.value = it
      }
    }
  }

  // Add temperature to state
  fun insertTemp(fTemp1: Float?, fTemp2: Float?) {
    viewModelScope.launch {
      val record = Temp(temp1 = fTemp1, temp2 = fTemp2, createdAt = System.currentTimeMillis())
      tempDao.insertAll(record)
    }
  }

  // Reset all records
  fun resetData() {
    viewModelScope.launch {
      tempDao.resetData()
    }
  }
}