package com.bartosboth.rollen_android.ui.screens.login

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.model.auth.LoginRequest
import com.bartosboth.rollen_android.data.network.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                val response = authService.login(LoginRequest(username, password))

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        // Save tokens securely
                        tokenManager.saveAccessToken(
                            loginResponse.jwtSecret,
                        )
                        _loginState.value = LoginState.Success
                    } ?: run {
                        _loginState.value = LoginState.Error("Empty response body")
                    }
                } else {
                    _loginState.value = LoginState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Network error: ${e.message}")
            }
        }
    }
}

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
