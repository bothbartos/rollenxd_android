package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.UserDetail
import retrofit2.Response
import retrofit2.http.GET
import javax.inject.Singleton

@Singleton
interface UserDetailAPI {

    @GET("api/user/details")
    suspend fun getUserDetails(): Response<UserDetail>
}