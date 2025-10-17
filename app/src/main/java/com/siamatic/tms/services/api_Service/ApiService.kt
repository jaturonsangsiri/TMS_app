package com.siamatic.tms.services.api_Service

import com.siamatic.tms.models.dataClass.google_sheet.GoogleSheetRequest
import com.siamatic.tms.models.dataClass.google_sheet.GoogleSheetResponse
import com.siamatic.tms.models.dataClass.login.LoginRequest
import com.siamatic.tms.models.dataClass.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

  @POST("exec")
  suspend fun addGoogleSheetTemperature(@Body googleSheetRequest: GoogleSheetRequest): Response<GoogleSheetResponse>
}