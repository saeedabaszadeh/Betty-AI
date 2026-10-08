package com.bettyai.assistant.data.mapper

import com.bettyai.assistant.data.local.entity.ConversationEntity
import com.bettyai.assistant.data.local.entity.ConversationWithCount
import com.bettyai.assistant.data.local.entity.MemoryEntity
import com.bettyai.assistant.data.local.entity.MessageEntity
import com.bettyai.assistant.data.local.entity.SkillEntity
import com.bettyai.assistant.data.local.entity.UserProfileEntity
import com.bettyai.assistant.domain.model.Conversation
import com.bettyai.assistant.domain.model.Memory
import com.bettyai.assistant.domain.model.MemoryType
import com.bettyai.assistant.domain.model.Message
import com.bettyai.assistant.domain.model.MessageRole
import com.bettyai.assistant.domain.model.SkillInfo
import com.bettyai.assistant.domain.model.UserProfile

// ---- User ----
fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    displayName = displayName,
    preferredLanguage = preferredLanguage,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id,
    displayName = displayName,
    preferredLanguage = preferredLanguage,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ---- Conversation ----
fun ConversationEntity.toDomain(messageCount: Int = 0) = Conversation(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    messageCount = messageCount
)

fun ConversationWithCount.toDomain() = conversation.toDomain(messageCount)

// ---- Message ----
fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    role = role.toMessageRole(),
    content = content,
    timestamp = timestamp,
    skillId = skillId
)

fun Message.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    role = role.name,
    content = content,
    timestamp = timestamp,
    skillId = skillId
)

fun String.toMessageRole(): MessageRole =
    runCatching { MessageRole.valueOf(this) }.getOrDefault(MessageRole.SYSTEM)

// ---- Memory ----
fun MemoryEntity.toDomain() = Memory(
    id = id,
    key = key,
    value = value,
    type = type.toMemoryType(),
    importance = importance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Memory.toEntity() = MemoryEntity(
    id = id,
    key = key,
    value = value,
    type = type.name,
    importance = importance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun String.toMemoryType(): MemoryType =
    runCatching { MemoryType.valueOf(this) }.getOrDefault(MemoryType.FACT)

// ---- Skill ----
fun SkillEntity.toDomain() = SkillInfo(
    id = id,
    name = name,
    description = description,
    enabled = enabled,
    permissions = permissions.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    builtIn = builtIn
)

fun SkillInfo.toEntity(updatedAt: Long) = SkillEntity(
    id = id,
    name = name,
    description = description,
    enabled = enabled,
    builtIn = builtIn,
    permissions = permissions.joinToString(","),
    updatedAt = updatedAt
)
