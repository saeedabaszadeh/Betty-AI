package com.bettyai.assistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bettyai.assistant.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {

    @Query("SELECT * FROM memories ORDER BY importance DESC, updatedAt DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE `key` = :key LIMIT 1")
    suspend fun recall(key: String): MemoryEntity?

    @Query(
        """
        SELECT * FROM memories
        WHERE `key` LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%'
        ORDER BY importance DESC, updatedAt DESC
        """
    )
    suspend fun search(query: String): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MemoryEntity)

    @Query("DELETE FROM memories WHERE `key` = :key")
    suspend fun forget(key: String)

    @Query("DELETE FROM memories")
    suspend fun clear()
}
