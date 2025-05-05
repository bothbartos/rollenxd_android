package com.bartosboth.rollen_android.data.model.playlist

import android.os.Parcelable
import com.bartosboth.rollen_android.data.model.song.Song
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long,
    val title: String,
    val author: String,
    val coverBase64: String,
    val songs: List<Song>
): Parcelable
