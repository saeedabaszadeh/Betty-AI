package com.bettyai.assistant.ai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList()
) {
    @Serializable
    data class Choice(
        val index: Int = 0,
        val message: ChatMessageDto? = null,
        @SerialName("finish_reason") val finishReason: String? = null
    )
}

@Serializable
data class ApiErrorResponse(
    val error: ApiError? = null
) {
    @Serializable
    data class ApiError(val message: String? = null, val type: String? = null, val code: String? = null)
}
