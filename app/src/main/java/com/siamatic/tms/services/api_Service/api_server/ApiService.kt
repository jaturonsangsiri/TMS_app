package com.siamatic.tms.services.api_Service.api_server

import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequest
import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequestNoti
import com.siamatic.tms.models.dataClass.apiServer.ApiServerResponse
import com.siamatic.tms.models.dataClass.apiServer.ApiServerResponseNoti
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServerService {
  @POST("legacy/templog")
  suspend fun addTempToServer(@Body addTempRequest: ApiServerRequest): Response<ApiServerResponse>

  @POST("legacy/templog/alert/notification")
  suspend fun notifyNotNormalTemp(@Body addNotiRequest: ApiServerRequestNoti): Response<ApiServerResponseNoti>
}