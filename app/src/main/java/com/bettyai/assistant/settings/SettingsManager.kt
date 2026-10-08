package com.bettyai.assistant.settings

import com.bettyai.assistant.data.security.SecureConfigStore
import com.bettyai.assistant.domain.model.AppSettings
import com.bettyai.assistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core-facing settings facade. Wraps [SettingsRepository] (non-secret values) and
 * [SecureConfigStore] (secrets) behind a single, UI-independent API.
 */
@Singleton
class SettingsManager @Inject constructor(
    private val repository: SettingsRepository,
    private val secureConfigStore: SecureConfigStore
) {

    val settings: Flow<AppSettings> = repository.settings

    suspend fun current(): AppSettings = repository.current()

    suspend fun setBettyEnabled(enabled: Boolean) = repository.setBettyEnabled(enabled)
    suspend fun setVoiceEnabled(enabled: Boolean) = repository.setVoiceEnabled(enabled)
    suspend fun setWakeWordEnabled(enabled: Boolean) = repository.setWakeWordEnabled(enabled)
    suspend fun setLanguage(tag: String) = repository.setLanguage(tag)
    suspend fun setTtsVoice(name: String?) = repository.setTtsVoice(name)
    suspend fun setAiProvider(id: String) = repository.setAiProvider(id)
    suspend fun setAiBaseUrl(url: String) = repository.setAiBaseUrl(url)
    suspend fun setAiModel(model: String) = repository.setAiModel(model)
    suspend fun setUserDisplayName(name: String) = repository.setUserDisplayName(name)

    /** Persist an API key securely (never stored in plain settings). */
    suspend fun setApiKey(key: String?) {
        secureConfigStore.setApiKey(key)
        repository.setHasApiKey(!key.isNullOrBlank())
    }

    fun getApiKey(): String? = secureConfigStore.getApiKey()
    fun hasApiKey(): Boolean = secureConfigStore.hasApiKey()
}
