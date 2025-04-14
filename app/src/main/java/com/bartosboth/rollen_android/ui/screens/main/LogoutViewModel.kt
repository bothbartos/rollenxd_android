package com.bartosboth.rollen_android.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val authRepository: AuthRepository,
): ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun logout(){
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.LoggedOut
        }
    }

    fun isLoggedIn(): Boolean{
        return authRepository.isLoggedIn()
    }
}

sealed class AuthState{
    object Idle: AuthState()
    object LoggedOut: AuthState()
}