package com.bartosboth.rollen_android.data.repository

import android.util.Log
import com.bartosboth.rollen_android.data.model.comment.Comment
import com.bartosboth.rollen_android.data.network.CommentAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val commentAPI: CommentAPI
) {
    suspend fun getCommentsBySongId(id: Long): List<Comment> = withContext(Dispatchers.IO) {
        val response = commentAPI.getCommentsBySongId(id)
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
        } else {
            throw HttpException(response)
        }
        response.body()!!
    }

    suspend fun addComment(songId: Long, text: String): Comment = withContext(Dispatchers.IO) {
        Log.d("ADD_COMMENT_REPO", "addComment: $text")
        val songIdPart = songId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val textPart = text.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = commentAPI.addComment(songIdPart, textPart)
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Response body is null")
            Log.d("ADD_COMMENT_REPO_RES", "addComment: ${response.body()!!.text}")
        } else {
            throw HttpException(response)
        }
        response.body()!!
    }


}