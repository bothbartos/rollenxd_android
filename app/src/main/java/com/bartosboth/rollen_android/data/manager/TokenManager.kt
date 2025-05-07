package com.bartosboth.rollen_android.data.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    private val _isLoggedIn = MutableStateFlow<Boolean>(isLoggedIn())

    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

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
        sharedPreferences.edit() { putString(KEY_ACCESS_TOKEN, token) }
        _isLoggedIn.value = true
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    private fun clearAccessToken() {
        sharedPreferences.edit() { remove(KEY_ACCESS_TOKEN) }
        _isLoggedIn.value = false
    }
    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }

    fun logout() {
        clearAccessToken()
    }
}