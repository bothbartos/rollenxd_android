package com.bartosboth.rollen_android.data.repository

import android.util.Log
import com.bartosboth.rollen_android.data.model.UserDetail
import com.bartosboth.rollen_android.data.network.UserDetailAPI
import retrofit2.HttpException
import javax.inject.Inject

class UserDetailRepository @Inject constructor(
    private val userDetailAPI: UserDetailAPI
) {
    suspend fun getUserDetail(): UserDetail {
        val response = userDetailAPI.getUserDetails()

        if (response.isSuccessful) {
            return response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e("UserDetailRepo", "Error getting user details: ${response.code()} - ${response.message()}")
            throw HttpException(response)
        }
    }
}
