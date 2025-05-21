package com.bartosboth.rollen_android.data.repository

import android.util.Log
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.network.SearchAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchAPI: SearchAPI
) {
    suspend fun searchSongs(search: String) : List<Song> = withContext(Dispatchers.IO) {
        val response = searchAPI.searchSongs(search)
        Log.d("SEARCH_REPO", "searchSongs: $search")
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            throw HttpException(response)
        }
    }


}