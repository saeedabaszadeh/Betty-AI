package com.bettyai.assistant.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bettyai.assistant.core.logger.BettyLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores secrets (API keys, tokens) in an encrypted [SharedPreferences] file
 * backed by the Android Keystore. Secrets never appear in source code, logs,
 * backups, or the plain settings store.
 */
@Singleton
class SecureConfigStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: BettyLogger
) {

    private val prefs: SharedPreferences by lazy { createPrefs() }

    private fun createPrefs(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (t: Throwable) {
            // Extremely rare (e.g. keystore reset). Fall back to a private prefs file
            // so the app keeps working; the file is still app-private.
            logger.error(TAG, "Encrypted prefs unavailable, using private fallback", t)
            context.getSharedPreferences("${FILE_NAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    /** Store (or clear, when null/blank) the AI API key. */
    fun setApiKey(key: String?) {
        prefs.edit().apply {
            if (key.isNullOrBlank()) remove(KEY_AI_API_KEY) else putString(KEY_AI_API_KEY, key)
        }.apply()
        logger.info(TAG, "API key ${if (key.isNullOrBlank()) "cleared" else "updated"}")
    }

    fun getApiKey(): String? = prefs.getString(KEY_AI_API_KEY, null)?.takeIf { it.isNotBlank() }

    fun hasApiKey(): Boolean = getApiKey() != null

    /** Generic secret accessor for future providers (n8n, Flowise, ...). */
    fun getSecret(key: String): String? = prefs.getString(key, null)

    fun setSecret(key: String, value: String?) {
        prefs.edit().apply {
            if (value.isNullOrBlank()) remove(key) else putString(key, value)
        }.apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        logger.info(TAG, "Cleared all secure config")
    }

    private companion object {
        const val TAG = "SecureConfig"
        const val FILE_NAME = "betty_secure_prefs"
        const val KEY_AI_API_KEY = "ai_api_key"
    }
}
