package com.bartosboth.rollen_android.ui.screens.register

import android.util.Log
import androidx.lifecycle.ViewModel
import com.bartosboth.rollen_android.data.model.auth.RegisterRequest
import com.bartosboth.rollen_android.data.network.AuthAPI
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _registerState.value = RegisterState.Loading
                val response = authRepository.register(username, email, password)

                if (response.isSuccessful) {
                    _registerState.value = RegisterState.Success
                } else {
                    val errorMessage = when (response.code()) {
                        409 -> "Username or email already exists"
                        400 -> "Invalid input data"
                        else -> "Registration failed: ${response.message()}"
                    }
                    _registerState.value = RegisterState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Network error: ${e.message}")
            }
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()

}