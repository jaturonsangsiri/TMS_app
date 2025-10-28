package com.siamatic.tms.models.viewModel

import android.content.Context
import android.util.Log
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequest
import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequestNoti
import com.siamatic.tms.services.api_Service.api_server.ApiServerClient

class ApiServerViewModel {
  private val apiService = ApiServerClient.apiServerService

  fun initApiServer(context: Context) {
    ApiServerClient.setContext(context)
  }

  suspend fun addTemp(mcuId: String, status: String, tempValue: Float, realValue: Float, date: String, time: String): Boolean? {
    return try {
      val apiServerRequest = ApiServerRequest(mcuId, status, tempValue, realValue, date, time)
      val response = apiService.addTempToServer(apiServerRequest)

      if (response.body() != null) {
        if (response.isSuccessful && response.body()?.success == true) {
          Log.i(debugTag, "Response: ${response.body()!!.data}")
          true
        } else {
          Log.e(debugTag, "Add temp failed: ${response.code()} ${response.message()}")
          false
        }
      } else {
        Log.e(debugTag, "Response body is null!, ${response.body()}")
        false
      }
    } catch (e: Exception) {
      Log.e(debugTag, "Add temp failed: $e")
      null
    }
  }

  suspend fun notifyNotNormalTemp(mcuId: String, status: String, tempValue: Float, realValue: Float, notiMessage: String, date: String, time: String): Boolean? {
    return try {
      val apiServerRequestNoti = ApiServerRequestNoti(mcuId, status, tempValue, realValue, notiMessage, date, time)
      val response = apiService.notifyNotNormalTemp(apiServerRequestNoti)

      if (response.body() != null) {
        if (response.isSuccessful && response.body()?.success == true) {
          Log.i(debugTag, "Response: ${response.body()!!.data}")
          true
        } else {
          Log.e(debugTag, "Notification failed: ${response.code()} ${response.message()}")
          false
        }
      } else {
        Log.e(debugTag, "Response body is null!, ${response.body()}")
        false
      }
    } catch (e: Exception) {
      Log.e(debugTag, "Notification failed: $e")
      null
    }
  }
}