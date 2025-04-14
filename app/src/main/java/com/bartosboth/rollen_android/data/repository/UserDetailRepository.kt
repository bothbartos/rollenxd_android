package com.bartosboth.rollen_android.data.repository

import com.bartosboth.rollen_android.data.model.UserDetail
import com.bartosboth.rollen_android.data.network.UserDetailAPI
import javax.inject.Inject

class UserDetailRepository @Inject constructor(
    private val userDetailAPI: UserDetailAPI
) {
    suspend fun getUserDetail(): UserDetail {
        return userDetailAPI.getUserDetails().body()!!
    }
}