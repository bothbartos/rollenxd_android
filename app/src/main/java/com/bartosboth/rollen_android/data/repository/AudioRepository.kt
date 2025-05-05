package com.bartosboth.rollen_android.data.repository

import android.util.Log
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.network.SongAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.HttpException
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val songApi: SongAPI
) {
    suspend fun getAudioData(): List<Song> = withContext(Dispatchers.IO) {
        val response = songApi.getSongs()

        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e("AudioRepository", "Error getting all songs: ${response.code()} - ${response.message()}")
            throw HttpException(response)
        }
        response.body()!!
    }

    suspend fun likeSong(id: Long): Int = withContext(Dispatchers.IO) {

        val response = songApi.likeSong(id)

        if (response.isSuccessful) {
             response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e("AudioRepository", "Error liking song: ${response.code()} - ${response.message()}")
            throw HttpException(response)
        }
        response.code()
    }

    suspend fun unlikeSong(id: Long): Int = withContext(Dispatchers.IO) {
        val response = songApi.unlikeSong(id)

        if (response.isSuccessful) {
             response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e("AudioRepository", "Error disliking song: ${response.code()} - ${response.message()}")
            throw HttpException(response)
        }
        response.code()
    }

    suspend fun getLikedSongs(): List<Song> = withContext(Dispatchers.IO) {
        val response = songApi.getLikedSongs()
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            Log.e("AudioRepository", "Error getting liked songs: ${response.code()} - ${response.message()}")
            throw HttpException(response)
        }
        response.body()!!
    }
}
