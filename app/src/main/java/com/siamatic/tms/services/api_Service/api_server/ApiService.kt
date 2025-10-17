package com.siamatic.tms.services.api_Service.api_server

import com.siamatic.tms.models.dataClass.login.LoginRequest
import com.siamatic.tms.models.dataClass.login.LoginResponse
import com.siamatic.tms.models.dataClass.apiServer.ApiServerRequest
import com.siamatic.tms.models.dataClass.apiServer.ApiServerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiServerService {
  @POST("auth/login")
  suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

  @Headers(
    "Content-Type: application/json",
    "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzbiI6IlBNUy0xUC1MMDI2Mi0wODYyLTAyOSIsIndhcmQiOiJhNTE2MmRmZC04MTlmLTQ4YzQtODIyZi02YWQyMDg5N2M0YTEiLCJob3NwaXRhbCI6IjZjNGY0NGQzLTczMjItNGFmMS1hZDgwLWMyMzk0YjQ3MTMxOSIsImlhdCI6MTc1NTU5NDY1MX0.2dtBfgYBCEXECnpo_IYdTrf2MMeQeu5cmbp_tk-22zc"
  )
  @POST("legacy/templog")
  suspend fun addTempToServer(@Body addTempRequest: ApiServerRequest): Response<ApiServerResponse>
}