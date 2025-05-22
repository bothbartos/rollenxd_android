package com.bartosboth.rollen_android.data.manager

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILENAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    }

    private val _isLoggedIn = MutableStateFlow<Boolean>(isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun saveAccessToken(token: String) {
        val encryptedToken = encryptData(token)
        sharedPreferences.edit() { putString(KEY_ACCESS_TOKEN, encryptedToken) }
        _isLoggedIn.value = true
    }

    fun getAccessToken(): String? {
        val encryptedToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        return encryptedToken?.let { decryptData(it) }
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

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalEncodingApi::class)
    private fun encryptData(data: String): String {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias("auth_key")) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    "auth_key",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }

            val key = keyStore.getKey("auth_key", null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charset.forName("UTF-8")))

            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            return Base64.encode(combined)
        } catch (e: Exception) {
            Log.e("TokenManager", "Encryption failed", e)
            return data
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @androidx.annotation.OptIn(UnstableApi::class)
    private fun decryptData(encryptedData: String): String {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val key = keyStore.getKey("auth_key", null) as SecretKey

            val combined = Base64.decode(encryptedData)

            val iv = ByteArray(12)
            val encryptedBytes = ByteArray(combined.size - iv.size)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            Log.e("TokenManager", "Decryption failed", e)
            return ""
        }
    }
}
