package com.bartosboth.rollen_android.data.model.playlist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistData(
    val id: Long,
    val title: String,
    val author: String,
    val coverBase64: String,
): Parcelable
