package com.siamatic.tms.repository

import com.siamatic.tms.models.dataClass.login.LoginRequest
import com.siamatic.tms.models.dataClass.login.LoginResponse
import com.siamatic.tms.services.api_Service.ApiClient

class AuthRepository {
  private val apiService = ApiClient.apiService

  suspend fun login(username: String, password: String): Result<LoginResponse> {
    return try {
      val loginRequest = LoginRequest(username, password)
      val response = apiService.login(loginRequest)

      if (response.isSuccessful) {
        val loginResponse = response.body()
        if (loginResponse != null) {
          Result.success(loginResponse)
        } else {
          Result.failure(Exception("Response body is null"))
        }
      } else {
        Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}