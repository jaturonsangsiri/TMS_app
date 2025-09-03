package com.siamatic.tms.models.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Model to menage temperature 
class TempViewModel : ViewModel() {
  private val _tempList = MutableStateFlow<List<Temperature>>(emptyList())
  val tempList: StateFlow<List<Temperature>> = _tempList

  // Add temperature to state
  fun addTemperature(temp: Temperature) {
    val currentList = _tempList.value.toMutableList()
    currentList.add(temp)
    _tempList.value = currentList
  }

  // get temperature to state
  fun getTemperature() {
    // Value that is get from API Retrofit
    val getData = listOf(
      Temperature("1as5d5aw", 1690026390000, 27.5f, 0f),
      Temperature("2as5d5aw", 1690026390000, 22.4f, 0f),
      Temperature("3as5d5aw", 1690026390000, 18.5f, 0f)
    )

    viewModelScope.launch {
      _tempList.value = getData
    }
  }

  // Delete list from State
  fun removeTemperature(id: String) {
    _tempList.value = _tempList.value.filter { it.id != id }
  }
}

data class Temperature(
  val id: String,
  val createdAt: Long,
  val temp1: Float,
  val temp2: Float
)