package com.bettyai.assistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bettyai.assistant.data.local.dao.AppSettingsDao
import com.bettyai.assistant.data.local.dao.ConversationDao
import com.bettyai.assistant.data.local.dao.MemoryDao
import com.bettyai.assistant.data.local.dao.MessageDao
import com.bettyai.assistant.data.local.dao.SkillDao
import com.bettyai.assistant.data.local.dao.UserDao
import com.bettyai.assistant.data.local.entity.AppSettingsEntity
import com.bettyai.assistant.data.local.entity.ConversationEntity
import com.bettyai.assistant.data.local.entity.MemoryEntity
import com.bettyai.assistant.data.local.entity.MessageEntity
import com.bettyai.assistant.data.local.entity.SkillEntity
import com.bettyai.assistant.data.local.entity.UserProfileEntity

/**
 * Betty's local SQLite database (Room). The schema is versioned and exported so
 * migrations can be added safely in future releases.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        SkillEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BettyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun skillDao(): SkillDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val NAME = "betty.db"
    }
}
