package com.bettyai.assistant.skills

import com.bettyai.assistant.domain.model.Memory
import com.bettyai.assistant.domain.model.MemoryType
import com.bettyai.assistant.memory.MemoryManager

/**
 * Everything a skill is allowed to know about the current turn and the user.
 * This is the single seam through which skills touch the rest of the system,
 * keeping them decoupled from Android and from each other.
 */
class SkillContext(
    val userDisplayName: String?,
    val languageTag: String,
    private val memoryManager: MemoryManager,
    private val clock: () -> Long = System::currentTimeMillis
) {
    fun now(): Long = clock()

    suspend fun remember(
        key: String,
        value: String,
        type: MemoryType = MemoryType.FACT,
        importance: Int = 0
    ) = memoryManager.remember(key, value, type, importance)

    suspend fun recall(key: String): String? = memoryManager.recall(key)

    suspend fun searchMemory(query: String): List<Memory> = memoryManager.search(query)
}
