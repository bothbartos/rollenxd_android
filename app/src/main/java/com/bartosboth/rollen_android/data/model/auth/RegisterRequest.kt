package com.bartosboth.rollen_android.data.model.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
) : Parcelable
