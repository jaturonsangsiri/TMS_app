package com.siamatic.tms.repository

import com.siamatic.tms.models.LoginRequest
import com.siamatic.tms.services.api_Service.ApiClient
import com.siamatic.tms.models.LoginResponse

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
        val errorBody = response.errorBody()?.string()
        Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}