package com.bartosboth.rollen_android.data.network

import com.bartosboth.rollen_android.data.model.comment.Comment
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import javax.inject.Singleton

@Singleton
interface CommentAPI {
    @GET("api/comment/id/{id}")
    suspend fun getCommentsBySongId(@Path("id") id: Long): Response<List<Comment>>


    @Multipart
    @POST("api/comment/addComment")
    suspend fun addComment(
        @Part("songId") songId: RequestBody,
        @Part("text") text: RequestBody
    ): Response<Comment>
}