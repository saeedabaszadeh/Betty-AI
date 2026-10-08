package com.bettyai.assistant.data.repository

import com.bettyai.assistant.data.local.dao.MemoryDao
import com.bettyai.assistant.data.mapper.toDomain
import com.bettyai.assistant.data.mapper.toEntity
import com.bettyai.assistant.domain.model.Memory
import com.bettyai.assistant.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao
) : MemoryRepository {

    override fun observeAll(): Flow<List<Memory>> =
        memoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(memory: Memory) {
        val now = System.currentTimeMillis()
        val existing = memoryDao.recall(memory.key)
        val toStore = memory.copy(
            id = existing?.id ?: memory.id,
            createdAt = existing?.createdAt ?: memory.createdAt.takeIf { it > 0 } ?: now,
            updatedAt = now
        )
        memoryDao.upsert(toStore.toEntity())
    }

    override suspend fun recall(key: String): Memory? =
        memoryDao.recall(key)?.toDomain()

    override suspend fun search(query: String): List<Memory> =
        memoryDao.search(query).map { it.toDomain() }

    override suspend fun forget(key: String) = memoryDao.forget(key)

    override suspend fun clear() = memoryDao.clear()
}
