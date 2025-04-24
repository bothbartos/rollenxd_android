package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.song.Song
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import javax.inject.Singleton

@Singleton
interface SongAPI {
    @GET("api/song/all")
    suspend fun getSongs(): Response<List<Song>>

    @Streaming
    @GET("api/song/stream/{id}")
    suspend fun streamAudio(@Path("id") id: Long): Response<ResponseBody>

    @POST("api/song/like/id/{id}")
    suspend fun likeSong(@Path("id") id: Long): Response<ResponseBody>

    @DELETE("api/song/unlike/id/{id}")
    suspend fun unlikeSong(@Path("id") id: Long): Response<ResponseBody>

    @GET("api/song/like/all")
    suspend fun getLikedSongs(): Response<List<Song>>
}
