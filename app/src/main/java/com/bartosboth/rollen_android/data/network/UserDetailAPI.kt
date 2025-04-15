package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.data.model.user.UserUpdateDetail
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import javax.inject.Singleton

@Singleton
interface UserDetailAPI {

    @GET("api/user/details")
    suspend fun getUserDetails(): Response<UserDetail>

    @Multipart
    @PATCH("api/user/details/update")
    suspend fun updateUserDetails(
        @Part("bio") bio: RequestBody,
        @Part profilePicture: MultipartBody.Part?
    ): Response<UserUpdateDetail>

}