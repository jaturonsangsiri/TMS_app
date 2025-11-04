package com.siamatic.tms.models.viewModel

import android.content.Context
import android.util.Log
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequest
import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequestNoti
import com.siamatic.tms.services.api_Service.api_server.ApiServerClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiServerViewModel {
  private val apiService = ApiServerClient.apiServerService

  fun initApiServer(context: Context) {
    ApiServerClient.setToken(context)
  }

  suspend fun addTemp(title: String, mcuId: String, status: String, tempValue: Float, realValue: Float, date: String, time: String): Boolean? = withContext(Dispatchers.IO) {
    try {
      //Log.i(debugTag, "Add temp to server: mcuId=$mcuId, status=$status, tempValue=$tempValue, realValue=$realValue, date=$date, time=$time")
      val apiServerRequest = ApiServerRequest(mcuId, status, tempValue, realValue, date, time)
      val response = apiService.addTempToServer(apiServerRequest)
      Log.i(debugTag, defaultCustomComposable.responseLog(title, response.code(), "${response.body()?.message}\n${if (response.isSuccessful) "date: $date, time: $time, tempValue: ${response.body()?.data?.tempValue}°C, realValue: ${response.body()?.data?.realValue}°C" else ""}"))

      if (response.body() != null) {
        response.isSuccessful && response.body()?.success == true
      } else {
        false
      }
    } catch (e: Exception) {
      Log.e(debugTag, "Add temp failed: $e")
      null
    }
  }

  suspend fun notifyNotNormalTemp(title: String, mcuId: String, status: String, tempValue: Float, realValue: Float, notiMessage: String, date: String, time: String): Boolean? = withContext(Dispatchers.IO) {
    try {
      //Log.i(debugTag, "Notify not normal temp: mcuId=$mcuId, status=$status, tempValue=$tempValue, realValue=$realValue, notiMessage=$notiMessage, date=$date, time=$time")
      val apiServerRequestNoti = ApiServerRequestNoti(mcuId, status, tempValue, realValue, notiMessage, date, time)
      val response = apiService.notifyNotNormalTemp(apiServerRequestNoti)
      Log.i(debugTag, defaultCustomComposable.responseLog(title, response.code(), "${response.body()?.message}\n${if (response.isSuccessful) "date: $date, time: $time, tempValue: ${response.body()?.data?.tempValue}°C, realValue: ${response.body()?.data?.realValue}°C" else ""}"))

      if (response.body() != null) {
        response.isSuccessful && response.body()?.success == true
      } else {
        false
      }
    } catch (e: Exception) {
      Log.e(debugTag, "Notification failed: $e")
      null
    }
  }
}