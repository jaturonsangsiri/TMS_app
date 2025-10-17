package com.siamatic.tms.composables.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siamatic.tms.models.dataClass.login.LoginResponse
import com.siamatic.tms.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
  private val authRepository = AuthRepository()

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  fun performLogin(username: String, password: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

      val result = authRepository.login(username, password)

      _uiState.value = _uiState.value.copy(
        isLoading = false,
        loginResult = result,
        errorMessage = if (result.isFailure) result.exceptionOrNull()?.message else null
      )
    }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }
}

data class LoginUiState(
  val isLoading: Boolean = false,
  val loginResult: Result<LoginResponse>? = null,
  val errorMessage: String? = null
)