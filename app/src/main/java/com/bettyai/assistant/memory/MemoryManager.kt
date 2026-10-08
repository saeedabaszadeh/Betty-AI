package com.bettyai.assistant.memory

import com.bettyai.assistant.domain.model.Memory
import com.bettyai.assistant.domain.model.MemoryType
import com.bettyai.assistant.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level facade over long-term memory. Skills and the AI prompt builder use
 * this to store and recall user preferences, facts, context and custom commands.
 */
@Singleton
class MemoryManager @Inject constructor(
    private val repository: MemoryRepository
) {

    fun observeAll(): Flow<List<Memory>> = repository.observeAll()

    suspend fun remember(
        key: String,
        value: String,
        type: MemoryType = MemoryType.FACT,
        importance: Int = 0
    ) {
        repository.upsert(
            Memory(
                key = key.trim(),
                value = value.trim(),
                type = type,
                importance = importance
            )
        )
    }

    suspend fun recall(key: String): String? = repository.recall(key)?.value

    suspend fun recallMemory(key: String): Memory? = repository.recall(key)

    /** Search memories; a blank query returns everything. */
    suspend fun search(query: String): List<Memory> =
        if (query.isBlank()) repository.observeAll().first() else repository.search(query)

    suspend fun forget(key: String) = repository.forget(key)

    suspend fun clear() = repository.clear()
}
