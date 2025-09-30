package com.siamatic.tms.models.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.models.dataClass.google_sheet.GoogleSheetRequest
import com.siamatic.tms.services.api_Service.ApiClient
import kotlinx.coroutines.launch

class GoogleSheetViewModel : ViewModel() {
  fun addTemperatureToGoogleSheet(sheetId: String, serialNumber: String, probe: String, temp: Float, acStatus: String, machineIP: String, minTemp: Float, maxTemp: Float, adjTemp: Float) {
    viewModelScope.launch {
      try {
        val request = GoogleSheetRequest(sheetId, serialNumber, probe, temp, acStatus, machineIP, minTemp, maxTemp, adjTemp)
        val response = ApiClient.apiService.addGoogleSheetTemperature(request)

        if (response.isSuccessful) {
          Log.d(debugTag, "Added google sheet rows")
        } else {
          Log.e(debugTag, "Error in add google sheet: ${response.code()} - ${response.errorBody()?.string()}")
        }
      } catch (error: Exception) {
        Log.e(debugTag, "Error while adding Temp to google sheet: ${error.message}")
      }
    }
  }
}