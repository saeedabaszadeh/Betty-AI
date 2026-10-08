package com.bettyai.assistant.domain.repository

import com.bettyai.assistant.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for non-secret application settings (DataStore-backed). */
interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun current(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)

    suspend fun setBettyEnabled(enabled: Boolean)
    suspend fun setVoiceEnabled(enabled: Boolean)
    suspend fun setWakeWordEnabled(enabled: Boolean)
    suspend fun setLanguage(tag: String)
    suspend fun setTtsVoice(name: String?)
    suspend fun setAiProvider(id: String)
    suspend fun setAiBaseUrl(url: String)
    suspend fun setAiModel(model: String)
    suspend fun setUserDisplayName(name: String)
    suspend fun setHasApiKey(hasKey: Boolean)
}
