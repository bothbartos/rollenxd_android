package com.bartosboth.rollen_android.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserUpdateDetail(
    val bio: String,
    val profilePictureBase64: String
): Parcelable