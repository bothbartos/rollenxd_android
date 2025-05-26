package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.playlist.NewPlaylist
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Singleton


@Singleton
interface PlaylistAPI {
    @GET("api/playlist/all")
    suspend fun getPlaylists(): Response<List<PlaylistData>>

    @GET("api/playlist/id/{id}")
    suspend fun getPlaylistById(@Path("id") id: Long): Response<Playlist>

    @POST("api/playlist/upload")
    suspend fun addPlaylist(@Body playlist: NewPlaylist): Response<PlaylistData>
}