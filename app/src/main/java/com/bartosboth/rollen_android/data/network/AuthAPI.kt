package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.auth.RegisterRequest
import com.bartosboth.rollen_android.data.model.auth.LoginRequest
import com.bartosboth.rollen_android.data.model.auth.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthAPI {
    @POST("/api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/signup")
    suspend fun register(@Body newUserDTO: RegisterRequest): Response<Void>
}