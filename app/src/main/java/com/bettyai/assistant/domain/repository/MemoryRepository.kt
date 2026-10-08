package com.bettyai.assistant.domain.repository

import com.bettyai.assistant.domain.model.Memory
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for Betty's long-term memory. */
interface MemoryRepository {
    fun observeAll(): Flow<List<Memory>>
    suspend fun upsert(memory: Memory)
    suspend fun recall(key: String): Memory?
    suspend fun search(query: String): List<Memory>
    suspend fun forget(key: String)
    suspend fun clear()
}
