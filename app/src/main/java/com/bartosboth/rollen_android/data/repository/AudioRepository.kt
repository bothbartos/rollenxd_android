package com.bartosboth.rollen_android.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.network.SongAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val songApi: SongAPI,
    @ApplicationContext private val context: Context
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

    suspend fun uploadSong(title: String, audioFile: Uri?, coverImage: Uri?): Int = withContext(Dispatchers.IO) {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())

        val audioFilePart = createMultipartFromUri(audioFile!!, "file", "audio")
        val coverImagePart = createMultipartFromUri(coverImage!!, "cover", "image")

        val response = songApi.uploadSong(titlePart, audioFilePart, coverImagePart)
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        }else{
            Log.e("AudioRepository", "Error uploading song: ${response.code()} - ${response.message()}")
            throw HttpException(response)
        }
        response.code()
    }

    private fun createMultipartFromUri(uri: Uri, paramName: String, fileType: String): MultipartBody.Part {
        val fileName = getFileNameFromUri(uri) ?: "${fileType}_${System.currentTimeMillis()}"

        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        val mimeType = context.contentResolver.getType(uri) ?: when (fileType) {
            "audio" -> "audio/*"
            "image" -> "image/*"
            else -> "application/octet-stream"
        }

        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(paramName, fileName, requestFile)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result.substring(cut + 1)
            }
        }

        return result
    }

}
