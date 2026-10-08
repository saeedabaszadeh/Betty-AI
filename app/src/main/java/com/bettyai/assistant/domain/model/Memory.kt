package com.bettyai.assistant.domain.model

/** Category of a stored memory item. */
enum class MemoryType {
    PREFERENCE,
    FACT,
    CONTEXT,
    CUSTOM_COMMAND
}

/**
 * A single memory record Betty can recall later (user preferences, facts,
 * conversation context, custom commands, ...).
 */
data class Memory(
    val id: Long = 0,
    val key: String,
    val value: String,
    val type: MemoryType,
    val importance: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
