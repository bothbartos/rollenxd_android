package com.bartosboth.rollen_android.data.model.comment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: Long,
    val songId: Long,
    val userId: Long,
    val username: String,
    val profilePicture: String,
    val text: String
): Parcelable
