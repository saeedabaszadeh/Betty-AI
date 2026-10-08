package com.bettyai.assistant.domain.model

/**
 * A conversation groups a sequence of [Message]s between the user and Betty.
 */
data class Conversation(
    val id: Long = 0,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messageCount: Int = 0
)

/** Who authored a [Message]. */
enum class MessageRole {
    USER,
    BETTY,
    SYSTEM
}

/**
 * A single message inside a [Conversation].
 */
data class Message(
    val id: Long = 0,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
    val skillId: String? = null
)
