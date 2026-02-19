package com.adguard.wireguardhotspotbridge.data.secrets

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecretsStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "wg_secrets",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun put(ref: String, value: String) {
        prefs.edit().putString(ref, value).apply()
    }

    fun get(ref: String): String? = prefs.getString(ref, null)

    fun remove(ref: String) {
        prefs.edit().remove(ref).apply()
    }
}

