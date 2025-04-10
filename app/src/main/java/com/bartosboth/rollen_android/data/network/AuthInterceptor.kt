package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.manager.TokenManager
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if(originalRequest.url.encodedPath.contains("login") || originalRequest.url.encodedPath.contains("register")) {
            return chain.proceed(originalRequest)
        }
        val token = tokenManager.getAccessToken()
        val modifiedRequest = if(!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }else{
            originalRequest
        }
        return chain.proceed(modifiedRequest)
    }

}