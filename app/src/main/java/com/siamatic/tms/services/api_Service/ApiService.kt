package com.siamatic.tms.services.api_Service

import com.siamatic.tms.models.LoginRequest
import com.siamatic.tms.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}