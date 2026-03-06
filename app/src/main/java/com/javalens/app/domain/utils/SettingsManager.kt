package com.javalens.app.domain.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val secureStorage = EncryptedSharedPreferences.create(
        context,
        "javalens_secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(apiKey: String) {
        secureStorage.edit().putString("gemini_api_key", apiKey).apply()
    }

    fun getApiKey(): String? {
        return secureStorage.getString("gemini_api_key", null)
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }
}
