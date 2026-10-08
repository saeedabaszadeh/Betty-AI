package com.bettyai.assistant.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bettyai.assistant.domain.model.AppSettings
import com.bettyai.assistant.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.bettySettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "betty_settings")

/**
 * DataStore-backed implementation of [SettingsRepository]. Only non-secret values
 * live here; API keys are stored separately in
 * [com.bettyai.assistant.data.security.SecureConfigStore].
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val store = context.bettySettingsDataStore

    override val settings: Flow<AppSettings> = store.data.map { it.toAppSettings() }

    override suspend fun current(): AppSettings = settings.first()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { prefs ->
            val next = transform(prefs.toAppSettings())
            prefs[Keys.BETTY_ENABLED] = next.bettyEnabled
            prefs[Keys.VOICE_ENABLED] = next.voiceEnabled
            prefs[Keys.WAKE_WORD_ENABLED] = next.wakeWordEnabled
            prefs[Keys.LANGUAGE_TAG] = next.languageTag
            prefs[Keys.TTS_VOICE] = next.ttsVoiceName ?: ""
            prefs[Keys.AI_PROVIDER] = next.aiProviderId
            prefs[Keys.AI_BASE_URL] = next.aiBaseUrl
            prefs[Keys.AI_MODEL] = next.aiModel
            prefs[Keys.USER_DISPLAY_NAME] = next.userDisplayName
            prefs[Keys.HAS_API_KEY] = next.hasApiKey
        }
    }

    override suspend fun setBettyEnabled(enabled: Boolean) = update { it.copy(bettyEnabled = enabled) }
    override suspend fun setVoiceEnabled(enabled: Boolean) = update { it.copy(voiceEnabled = enabled) }
    override suspend fun setWakeWordEnabled(enabled: Boolean) = update { it.copy(wakeWordEnabled = enabled) }
    override suspend fun setLanguage(tag: String) = update { it.copy(languageTag = tag) }
    override suspend fun setTtsVoice(name: String?) = update { it.copy(ttsVoiceName = name) }
    override suspend fun setAiProvider(id: String) = update { it.copy(aiProviderId = id) }
    override suspend fun setAiBaseUrl(url: String) = update { it.copy(aiBaseUrl = url) }
    override suspend fun setAiModel(model: String) = update { it.copy(aiModel = model) }
    override suspend fun setUserDisplayName(name: String) = update { it.copy(userDisplayName = name) }
    override suspend fun setHasApiKey(hasKey: Boolean) = update { it.copy(hasApiKey = hasKey) }

    private fun Preferences.toAppSettings() = AppSettings(
        bettyEnabled = this[Keys.BETTY_ENABLED] ?: true,
        voiceEnabled = this[Keys.VOICE_ENABLED] ?: true,
        wakeWordEnabled = this[Keys.WAKE_WORD_ENABLED] ?: false,
        languageTag = this[Keys.LANGUAGE_TAG] ?: AppSettings.DEFAULT_LANGUAGE,
        ttsVoiceName = this[Keys.TTS_VOICE]?.takeIf { it.isNotBlank() },
        aiProviderId = this[Keys.AI_PROVIDER] ?: "openai_compatible",
        aiBaseUrl = this[Keys.AI_BASE_URL] ?: "",
        aiModel = this[Keys.AI_MODEL] ?: "gpt-4o-mini",
        userDisplayName = this[Keys.USER_DISPLAY_NAME] ?: "",
        hasApiKey = this[Keys.HAS_API_KEY] ?: false
    )

    private object Keys {
        val BETTY_ENABLED = booleanPreferencesKey("betty_enabled")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val WAKE_WORD_ENABLED = booleanPreferencesKey("wake_word_enabled")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val TTS_VOICE = stringPreferencesKey("tts_voice")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val AI_BASE_URL = stringPreferencesKey("ai_base_url")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val HAS_API_KEY = booleanPreferencesKey("has_api_key")
    }
}
