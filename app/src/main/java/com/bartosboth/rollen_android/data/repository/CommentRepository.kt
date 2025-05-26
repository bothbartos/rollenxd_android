package com.bartosboth.rollen_android.data.repository

import com.bartosboth.rollen_android.data.model.comment.Comment
import com.bartosboth.rollen_android.data.network.CommentAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

}