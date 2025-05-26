package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.comment.Comment
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Singleton

@Singleton
interface CommentAPI {
    @GET("api/comment/id/{id}")
    suspend fun getCommentsBySongId(@Path("id") id: Long): Response<List<Comment>>
}