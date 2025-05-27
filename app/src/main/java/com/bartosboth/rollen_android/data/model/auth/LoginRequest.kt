package com.bartosboth.rollen_android.data.model.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LoginRequest(
    val username: String,
    val password: String
) : Parcelable
