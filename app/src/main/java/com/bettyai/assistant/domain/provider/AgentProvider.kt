package com.bettyai.assistant.domain.provider

import com.bettyai.assistant.core.result.BettyError

/**
 * Result returned by an agent platform.
 */
sealed interface AgentResult {
    data class Success(val text: String) : AgentResult
    data class Failure(val error: BettyError) : AgentResult
}

/**
 * Abstraction over external agent platforms such as **Flowise** (or any custom
 * AI agent backend). Kept intentionally minimal and dependency-free so it can be
 * implemented later without touching the core.
 */
interface AgentProvider {
    val id: String
    val displayName: String

    suspend fun chat(
        baseUrl: String,
        apiKey: String?,
        message: String,
        sessionId: String? = null
    ): AgentResult
}
