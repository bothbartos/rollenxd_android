package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.song.Song
import jakarta.inject.Singleton
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


@Singleton
interface SearchAPI {

    @GET("api/song/search")
    suspend fun searchSongs(@Query("search") search: String): Response<List<Song>>
}