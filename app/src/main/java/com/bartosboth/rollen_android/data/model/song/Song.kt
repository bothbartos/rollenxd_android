package com.bartosboth.rollen_android.data.model.song

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val uri: Uri,
    val author: String?,
    val length: Double,
    val title: String?,
    val numberOfLikes: Int,
    val reShares: Int,
    val id: Long
    ): Parcelable
