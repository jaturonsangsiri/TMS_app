package com.siamatic.tms.services.api_Service.google_sheet

import com.siamatic.tms.models.dataClass.google_sheet.GoogleSheetRequest
import com.siamatic.tms.models.dataClass.google_sheet.GoogleSheetResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GoogleSheetApiService {
  @POST("exec")
  suspend fun addGoogleSheetTemperature(@Body googleSheetRequest: GoogleSheetRequest): Response<GoogleSheetResponse>
}