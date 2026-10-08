package com.bettyai.assistant.domain.model

/**
 * User-configurable application settings. Persisted via DataStore (non-secret
 * values). Secrets such as API keys live in [com.bettyai.assistant.data.security.SecureConfigStore].
 */
data class AppSettings(
    val bettyEnabled: Boolean = true,
    val voiceEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val languageTag: String = "fa-IR",
    val ttsVoiceName: String? = null,
    val aiProviderId: String = "openai_compatible",
    val aiBaseUrl: String = "",
    val aiModel: String = "gpt-4o-mini",
    val userDisplayName: String = "",
    val hasApiKey: Boolean = false
) {
    companion object {
        const val DEFAULT_LANGUAGE = "fa-IR"
    }
}
