package com.siamatic.tms.models.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.models.dataClass.google_sheet.GoogleSheetRequest
import com.siamatic.tms.services.api_Service.google_sheet.GoogleSheetApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleSheetViewModel : ViewModel() {
  suspend fun addTemperatureToGoogleSheet(sheetId: String, serialNumber: String, probe: String, temp: Float, acStatus: String, machineIP: String, minTemp: Float, maxTemp: Float, adjTemp: Float, dateTime: String): Boolean? {
    return withContext(Dispatchers.IO) {
      try {
        val request = GoogleSheetRequest(sheetId, serialNumber, probe, temp, acStatus, machineIP, minTemp, maxTemp, adjTemp, dateTime)
        val response = GoogleSheetApiClient.googleSheetApiService.addGoogleSheetTemperature(request)

        if (response.body() != null) {
          if (response.isSuccessful && response.body()?.code == 200) {
            Log.i(debugTag, "Response google sheet: ${response.body()!!.message}")
            true
          } else {
            Log.e(debugTag, "Error in add google sheet: ${response.code()} - ${response.errorBody()?.string()}")
            false
          }
        } else {
          Log.e(debugTag, "Response body is null!, ${response.body()}")
          false
        }
      } catch (error: Exception) {
        Log.e(debugTag, "Error while adding Temp to google sheet: ${error.message}")
        null
      }
    }
  }
}