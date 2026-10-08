package com.bettyai.assistant.domain.provider

import com.bettyai.assistant.domain.model.AiRequest
import com.bettyai.assistant.domain.model.AiResponse
import com.bettyai.assistant.domain.model.AiRuntimeConfig

/**
 * A pluggable source of language-model intelligence.
 *
 * Implementations must be free of any UI dependency and must not read settings
 * directly — the runtime configuration is supplied on every call. Adding a new
 * provider (OpenAI, Gemini, Ollama, a self-hosted backend, ...) is a matter of
 * implementing this interface and registering it with Hilt.
 */
interface AIProvider {
    /** Stable identifier used in settings, e.g. "openai_compatible". */
    val id: String

    /** Human readable name shown in the settings screen. */
    val displayName: String

    /** Whether an API key is required for this provider to work. */
    val requiresApiKey: Boolean

    /** Returns true when [config] contains everything this provider needs. */
    fun isConfigured(config: AiRuntimeConfig): Boolean

    /** Produce a completion for [request]. */
    suspend fun complete(request: AiRequest, config: AiRuntimeConfig): AiResponse
}
