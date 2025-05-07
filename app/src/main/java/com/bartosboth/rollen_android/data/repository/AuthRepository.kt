package com.bartosboth.rollen_android.data.repository

import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.model.auth.LoginRequest
import com.bartosboth.rollen_android.data.model.auth.LoginResponse
import com.bartosboth.rollen_android.data.model.auth.RegisterRequest
import com.bartosboth.rollen_android.data.network.AuthAPI
import retrofit2.Response
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val authService: AuthAPI,
    private val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): LoginResponse {
        val loginRequest = LoginRequest(username, password)
        val response = authService.login(loginRequest)
        if (response.isSuccessful) {
            response.body()?.let { loginResponse ->
                tokenManager.saveAccessToken(
                    loginResponse.jwtSecret,
                )
            }
        } else {
            throw Exception("Login failed: ${response.message()}")
        }
        return response.body()!!
    }


    suspend fun register(username: String, email: String, password: String): Response<Void> {
        val registerRequest = RegisterRequest(username, email, password)
        val response = authService.register(registerRequest)
        if(!response.isSuccessful){
            throw Exception(response.message())
        }
        return response
    }

    fun logout() {
        tokenManager.logout()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}