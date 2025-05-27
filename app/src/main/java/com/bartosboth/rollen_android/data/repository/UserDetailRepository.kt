package com.bartosboth.rollen_android.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.data.model.user.UserUpdateDetail
import com.bartosboth.rollen_android.data.network.UserDetailAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class UserDetailRepository @Inject constructor(
    private val userDetailAPI: UserDetailAPI,
    @ApplicationContext private val context: Context
) {
    suspend fun getUserDetail(): UserDetail {
        val response = userDetailAPI.getUserDetails()

        if (response.isSuccessful) {
            return response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            throw HttpException(response)
        }
    }

    suspend fun updateUserDetail(bio: String, profilePictureUri: Uri?): UserUpdateDetail {
        val bioPart = bio.toRequestBody("text/plain".toMediaTypeOrNull())

        val profilePicturePart = if (profilePictureUri != null) {
            createMultipartFromUri(profilePictureUri)
        } else {
            null
        }

        val response = userDetailAPI.updateUserDetails(bioPart, profilePicturePart)

        if (response.isSuccessful) {
            return response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            throw HttpException(response)
        }
    }

    private fun createMultipartFromUri(uri: Uri): MultipartBody.Part {
        val fileName = getFileNameFromUri(uri) ?: "profile_image_${System.currentTimeMillis()}.jpg"

        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("profilePicture", fileName, requestFile)
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
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }
}
