package com.siamatic.tms.models.viewModel

import android.util.Log
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequest
import com.siamatic.tms.services.api_Service.api_server.ApiServerClient

class ApiServerViewModel {
  private val apiService = ApiServerClient.apiServerService

  suspend fun addTemp(mcuId: String, status: String, tempValue: Float, realValue: Float, date: String, time: String): Boolean? {
    return try {
      val apiServerRequest = ApiServerRequest(mcuId, status, tempValue, realValue, date, time)
      val response = apiService.addTempToServer(apiServerRequest)

      if (response.isSuccessful) {
        val apiServerResponse = response.body()
        if (apiServerResponse != null) {
          Log.i(debugTag, "Added temp to api successful!")
          true
        } else {
          Log.e(debugTag, "Response body is null!")
          false
        }
      } else {
        Log.e(debugTag, "Add temp failed: ${response.code()} ${response.message()}")
        false
      }
    } catch (e: Exception) {
      Log.e(debugTag, "Add temp failed: $e")
      null
    }
  }
}