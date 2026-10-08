package com.bettyai.assistant.core.di

import com.bettyai.assistant.core.logger.AndroidBettyLogger
import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.data.network.NetworkMonitorImpl
import com.bettyai.assistant.data.repository.ConversationRepositoryImpl
import com.bettyai.assistant.data.repository.MemoryRepositoryImpl
import com.bettyai.assistant.data.repository.UserRepositoryImpl
import com.bettyai.assistant.data.settings.SettingsRepositoryImpl
import com.bettyai.assistant.domain.provider.NetworkMonitor
import com.bettyai.assistant.domain.repository.ConversationRepository
import com.bettyai.assistant.domain.repository.MemoryRepository
import com.bettyai.assistant.domain.repository.SettingsRepository
import com.bettyai.assistant.domain.repository.UserRepository
import com.bettyai.assistant.voice.wakeword.DefaultWakeWordEngine
import com.bettyai.assistant.voice.wakeword.WakeWordEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds repository, logger, network and wake-word implementations to their interfaces. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindLogger(impl: AndroidBettyLogger): BettyLogger

    @Binds
    abstract fun bindNetworkMonitor(impl: NetworkMonitorImpl): NetworkMonitor

    @Binds
    abstract fun bindWakeWordEngine(impl: DefaultWakeWordEngine): WakeWordEngine
}
