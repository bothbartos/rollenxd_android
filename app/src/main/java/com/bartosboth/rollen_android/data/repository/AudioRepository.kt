package com.bartosboth.rollen_android.data.repository

import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.network.SongAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val songApi: SongAPI
) {
    suspend fun getAudioData(): List<Song> = withContext(Dispatchers.IO) {
        songApi.getSongs().body()!!
    }

    suspend fun streamAudio(id: Long): ResponseBody = withContext(Dispatchers.IO) {
        songApi.streamAudio(id).body()!!
    }

    suspend fun likeSong(id: Long): Int = withContext(Dispatchers.IO) {
        songApi.likeSong(id).code()!!
    }

    suspend fun unlikeSong(id: Long): Int = withContext(Dispatchers.IO) {
        songApi.unlikeSong(id).code()!!
    }
}
