package com.bartosboth.rollen_android.data.repository

import android.util.Log
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.network.PlaylistAPI
import com.bartosboth.rollen_android.data.network.SongAPI
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class PlaylistRepository @Inject constructor(
    private val playlistAPI: PlaylistAPI,
    private val songApi: SongAPI
) {
    suspend fun getPlaylists(): List<PlaylistData> = withContext(Dispatchers.IO) {
        val response = playlistAPI.getPlaylists()

        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e(
                "AudioRepository",
                "Error getting all songs: ${response.code()} - ${response.message()}"
            )
            throw HttpException(response)
        }
        response.body()!!
    }

    suspend fun getPlaylistById(id: Long): Playlist = withContext(Dispatchers.IO) {
        val response = playlistAPI.getPlaylistById(id)

        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e(
                "AudioRepository",
                "Error getting playlist by id: ${response.code()} - ${response.message()}"
            )
            throw HttpException(response)
        }
    }

    suspend fun getLikedSongs(): List<Song> = withContext(Dispatchers.IO) {
        val response = songApi.getLikedSongs()
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e(
                "AudioRepository",
                "Error getting liked songs: ${response.code()} - ${response.message()}"
            )
            throw HttpException(response)
        }
        response.body()!!
    }
}