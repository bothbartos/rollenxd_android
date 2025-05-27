package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.song.Song
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import javax.inject.Singleton

@Singleton
interface SongAPI {
    @GET("api/song/all")
    suspend fun getSongs(): Response<List<Song>>

    @POST("api/song/like/id/{id}")
    suspend fun likeSong(@Path("id") id: Long): Response<ResponseBody>

    @DELETE("api/song/unlike/id/{id}")
    suspend fun unlikeSong(@Path("id") id: Long): Response<ResponseBody>

    @GET("api/song/like/all")
    suspend fun getLikedSongs(): Response<List<Song>>

    @Multipart
    @POST("api/song/upload")
    suspend fun uploadSong(
        @Part("title") title: RequestBody,
        @Part file: MultipartBody.Part,
        @Part cover: MultipartBody.Part
    ): Response<Song>
}
