package com.bartosboth.rollen_android.data.model.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LoginResponse(
    val jwtSecret: String,
    val username: String,
    val roles: List<String>
) : Parcelable
