package com.bettyai.assistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bettyai.assistant.data.local.entity.ConversationEntity
import com.bettyai.assistant.data.local.entity.ConversationWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query(
        """
        SELECT conversations.*,
               (SELECT COUNT(*) FROM messages WHERE messages.conversationId = conversations.id) AS messageCount
        FROM conversations
        ORDER BY updatedAt DESC
        LIMIT :limit
        """
    )
    fun observeAll(limit: Int): Flow<List<ConversationWithCount>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC LIMIT 1")
    suspend fun mostRecent(): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConversationEntity): Long

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rename(id: Long, title: String, updatedAt: Long)

    @Query("UPDATE conversations SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touch(id: Long, updatedAt: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
