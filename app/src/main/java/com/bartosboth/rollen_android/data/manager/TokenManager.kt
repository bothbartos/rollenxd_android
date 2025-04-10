package com.bartosboth.rollen_android.data.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILENAME = "secure_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearAccessToken() {
        sharedPreferences.edit().remove(KEY_ACCESS_TOKEN).apply()
    }
    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }
}