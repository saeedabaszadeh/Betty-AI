package com.bettyai.assistant.domain.repository

import com.bettyai.assistant.domain.model.Conversation
import com.bettyai.assistant.domain.model.Message
import com.bettyai.assistant.domain.model.MessageRole
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for conversations and their messages. */
interface ConversationRepository {
    fun observeConversations(limit: Int = 50): Flow<List<Conversation>>
    fun observeMessages(conversationId: Long): Flow<List<Message>>

    suspend fun getActiveConversation(): Conversation
    suspend fun createConversation(title: String): Conversation
    suspend fun renameConversation(id: Long, title: String)

    suspend fun addMessage(
        conversationId: Long,
        role: MessageRole,
        content: String,
        skillId: String? = null
    ): Message

    suspend fun latestMessages(limit: Int): List<Message>
    suspend fun deleteConversation(id: Long)
    suspend fun clearAll()
}
