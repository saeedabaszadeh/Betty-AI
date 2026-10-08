package com.bettyai.assistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bettyai.assistant.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC, id ASC")
    fun observeForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC, id DESC LIMIT :limit")
    suspend fun latest(limit: Int): List<MessageEntity>

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun countForConversation(conversationId: Long): Int

    @Insert
    suspend fun insert(entity: MessageEntity): Long

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}
