package com.bettyai.assistant.data.repository

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.data.local.dao.ConversationDao
import com.bettyai.assistant.data.local.dao.MessageDao
import com.bettyai.assistant.data.local.entity.ConversationEntity
import com.bettyai.assistant.data.mapper.toDomain
import com.bettyai.assistant.data.mapper.toEntity
import com.bettyai.assistant.domain.model.Conversation
import com.bettyai.assistant.domain.model.Message
import com.bettyai.assistant.domain.model.MessageRole
import com.bettyai.assistant.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val logger: BettyLogger
) : ConversationRepository {

    override fun observeConversations(limit: Int): Flow<List<Conversation>> =
        conversationDao.observeAll(limit).map { list -> list.map { it.toDomain() } }

    override fun observeMessages(conversationId: Long): Flow<List<Message>> =
        messageDao.observeForConversation(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveConversation(): Conversation {
        val existing = conversationDao.mostRecent()
        return if (existing != null) existing.toDomain() else createConversation(DEFAULT_TITLE)
    }

    override suspend fun createConversation(title: String): Conversation {
        val now = System.currentTimeMillis()
        val id = conversationDao.upsert(
            ConversationEntity(title = title, createdAt = now, updatedAt = now)
        )
        logger.debug(TAG, "Created conversation id=$id")
        return Conversation(id = id, title = title, createdAt = now, updatedAt = now)
    }

    override suspend fun renameConversation(id: Long, title: String) {
        conversationDao.rename(id, title, System.currentTimeMillis())
    }

    override suspend fun addMessage(
        conversationId: Long,
        role: MessageRole,
        content: String,
        skillId: String?
    ): Message {
        val now = System.currentTimeMillis()
        val id = messageDao.insert(
            com.bettyai.assistant.data.local.entity.MessageEntity(
                conversationId = conversationId,
                role = role.name,
                content = content,
                timestamp = now,
                skillId = skillId
            )
        )
        conversationDao.touch(conversationId, now)

        // Auto-title the conversation from the first user message.
        if (role == MessageRole.USER) {
            val count = messageDao.countForConversation(conversationId)
            if (count == 1) {
                val current = conversationDao.get(conversationId)
                if (current != null && current.title == DEFAULT_TITLE) {
                    conversationDao.rename(conversationId, content.take(40), now)
                }
            }
        }

        return Message(
            id = id,
            conversationId = conversationId,
            role = role,
            content = content,
            timestamp = now,
            skillId = skillId
        )
    }

    override suspend fun latestMessages(limit: Int): List<Message> =
        messageDao.latest(limit).map { it.toDomain() }.reversed()

    override suspend fun deleteConversation(id: Long) {
        messageDao.deleteForConversation(id)
        conversationDao.delete(id)
    }

    override suspend fun clearAll() {
        messageDao.deleteAll()
        conversationDao.deleteAll()
        logger.info(TAG, "Cleared all conversations")
    }

    private companion object {
        const val TAG = "ConversationRepo"
        const val DEFAULT_TITLE = "گفتگوی جدید"
    }
}
