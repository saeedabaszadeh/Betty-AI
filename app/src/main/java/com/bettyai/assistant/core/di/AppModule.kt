package com.bettyai.assistant.core.di

import android.content.Context
import androidx.room.Room
import com.bettyai.assistant.data.local.BettyDatabase
import com.bettyai.assistant.data.local.dao.AppSettingsDao
import com.bettyai.assistant.data.local.dao.ConversationDao
import com.bettyai.assistant.data.local.dao.MemoryDao
import com.bettyai.assistant.data.local.dao.MessageDao
import com.bettyai.assistant.data.local.dao.SkillDao
import com.bettyai.assistant.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the Room database and its DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BettyDatabase =
        Room.databaseBuilder(context, BettyDatabase::class.java, BettyDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(db: BettyDatabase): UserDao = db.userDao()

    @Provides
    fun provideConversationDao(db: BettyDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: BettyDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideMemoryDao(db: BettyDatabase): MemoryDao = db.memoryDao()

    @Provides
    fun provideSkillDao(db: BettyDatabase): SkillDao = db.skillDao()

    @Provides
    fun provideAppSettingsDao(db: BettyDatabase): AppSettingsDao = db.appSettingsDao()
}
