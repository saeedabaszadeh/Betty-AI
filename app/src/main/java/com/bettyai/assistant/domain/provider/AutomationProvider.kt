package com.bettyai.assistant.domain.provider

import com.bettyai.assistant.core.result.BettyError

/**
 * Result of triggering an automation workflow.
 */
sealed interface AutomationResult {
    data class Success(val body: String) : AutomationResult
    data class Failure(val error: BettyError) : AutomationResult
}

/**
 * Abstraction over external automation platforms such as **n8n**.
 *
 * The core engine depends only on this interface. An n8n implementation can be
 * registered later without any change to Betty's core. No hard dependency is
 * created now.
 */
interface AutomationProvider {
    val id: String
    val displayName: String

    fun isConfigured(baseUrl: String): Boolean

    /** Trigger a webhook identified by [webhookPath] with [payload]. */
    suspend fun trigger(
        baseUrl: String,
        webhookPath: String,
        payload: Map<String, String>
    ): AutomationResult
}
