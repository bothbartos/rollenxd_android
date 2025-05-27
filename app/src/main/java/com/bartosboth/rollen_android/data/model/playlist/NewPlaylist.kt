package com.bartosboth.rollen_android.data.model.playlist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewPlaylist(
    val title: String,
    val songId: List<Long>
) : Parcelable
