package com.bettyai.assistant.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Room entity for the single local user profile row. */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long,
    val displayName: String,
    val preferredLanguage: String,
    val createdAt: Long,
    val updatedAt: Long
)

/** Room entity for a conversation. */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

/** Room entity for a message belonging to a conversation. */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String,
    val content: String,
    val timestamp: Long,
    val skillId: String? = null
)

/** Room entity for a long-term memory record. */
@Entity(
    tableName = "memories",
    indices = [Index(value = ["key"], unique = true)]
)
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val type: String,
    val importance: Int,
    val createdAt: Long,
    val updatedAt: Long
)

/** Room entity describing a registered skill and whether the user enabled it. */
@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val builtIn: Boolean,
    val permissions: String,
    val updatedAt: Long
)

/**
 * Key/value settings table. The reactive source of truth for settings is DataStore;
 * this table is provided so future modules/skills that operate at the database level
 * can read/write settings snapshots (e.g. for export or cross-querying).
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)

/** Projection that pairs a conversation with its message count. */
data class ConversationWithCount(
    @Embedded val conversation: ConversationEntity,
    val messageCount: Int
)
