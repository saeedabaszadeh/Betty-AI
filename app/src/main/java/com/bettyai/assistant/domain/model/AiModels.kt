package com.bettyai.assistant.domain.model

/** Role of a message sent to a language model. */
enum class AiRole(val wire: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant")
}

/** A single message in an AI conversation. */
data class AiMessage(
    val role: AiRole,
    val content: String
)

/** A request to an [com.bettyai.assistant.domain.provider.AIProvider]. */
data class AiRequest(
    val systemPrompt: String,
    val messages: List<AiMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 1024
)

/** The result returned by an [com.bettyai.assistant.domain.provider.AIProvider]. */
data class AiResponse(
    val text: String,
    val providerId: String,
    val model: String = "",
    val fromFallback: Boolean = false
)

/**
 * Runtime configuration resolved from settings + secure storage and passed to a
 * provider on each call. Keeps providers stateless and free of UI dependencies.
 */
data class AiRuntimeConfig(
    val baseUrl: String,
    val apiKey: String?,
    val model: String,
    val timeoutSeconds: Long = 30
)
