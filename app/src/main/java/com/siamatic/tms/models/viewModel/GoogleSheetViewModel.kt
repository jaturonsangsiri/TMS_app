package com.siamatic.tms.models.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.dataClass.google_sheet.GoogleSheetRequest
import com.siamatic.tms.services.api_Service.google_sheet.GoogleSheetApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleSheetViewModel : ViewModel() {
  suspend fun addTemperatureToGoogleSheet(title: String, sheetId: String, serialNumber: String, probe: String, temp: Float, acStatus: String, machineIP: String, minTemp: Float, maxTemp: Float, adjTemp: Float, dateTime: String): Boolean? {
    return withContext(Dispatchers.IO) {
      try {
        //Log.i(debugTag, "sheetId: $sheetId, serialNumber: $serialNumber, probe: $probe, temp: $temp, acStatus: $acStatus, machineIP: $machineIP, minTemp: $minTemp, maxTemp: $maxTemp, adjTemp: $adjTemp, dateTime: $dateTime")
        val request = GoogleSheetRequest(sheetId, serialNumber, probe, temp, acStatus, machineIP, minTemp, maxTemp, adjTemp, dateTime)
        val response = GoogleSheetApiClient.googleSheetApiService.addGoogleSheetTemperature(request)
        Log.i(debugTag, defaultCustomComposable.responseLog(title, response.code(), "${response.body()?.message}"))

        if (response.body() != null) {
          response.isSuccessful
        } else {
          false
        }
      } catch (error: Exception) {
        Log.e(debugTag, "Error while adding Temp to google sheet: ${error.message}")
        null
      }
    }
  }
}