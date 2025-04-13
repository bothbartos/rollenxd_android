package com.bartosboth.rollen_android.data.model.song

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val title: String,
    val author: String,
    val coverBase64: String,
    val length: Double,
    var isLiked: Boolean,
    val reShares: Int,
    val id: Long
    ): Parcelable
