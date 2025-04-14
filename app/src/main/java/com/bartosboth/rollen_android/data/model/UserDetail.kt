package com.bartosboth.rollen_android.data.model

import android.os.Parcelable
import com.bartosboth.rollen_android.data.model.song.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class UserDetail(
    val id: Long,
    val name: String,
    val email: String,
    val bio: String,
    val profileImageBase64: String,
    val songs: List<Song>,
): Parcelable
