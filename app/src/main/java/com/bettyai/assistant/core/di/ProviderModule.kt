package com.bettyai.assistant.core.di

import com.bettyai.assistant.ai.OpenAiCompatibleProvider
import com.bettyai.assistant.ai.agent.FlowiseAgentProvider
import com.bettyai.assistant.ai.automation.N8nAutomationProvider
import com.bettyai.assistant.domain.provider.AIProvider
import com.bettyai.assistant.domain.provider.AgentProvider
import com.bettyai.assistant.domain.provider.AutomationProvider
import com.bettyai.assistant.domain.provider.SpeechProvider
import com.bettyai.assistant.domain.provider.TtsProvider
import com.bettyai.assistant.voice.speech.AndroidSpeechRecognizerProvider
import com.bettyai.assistant.voice.tts.AndroidTtsProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Registers every provider implementation into its respective set. Adding a new
 * AI / speech / TTS / automation / agent provider is a single @Binds line here —
 * the engines that consume the sets never change.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @IntoSet
    abstract fun bindAiProvider(impl: OpenAiCompatibleProvider): AIProvider

    @Binds
    @IntoSet
    abstract fun bindSpeechProvider(impl: AndroidSpeechRecognizerProvider): SpeechProvider

    @Binds
    @IntoSet
    abstract fun bindTtsProvider(impl: AndroidTtsProvider): TtsProvider

    @Binds
    @IntoSet
    abstract fun bindAutomationProvider(impl: N8nAutomationProvider): AutomationProvider

    @Binds
    @IntoSet
    abstract fun bindAgentProvider(impl: FlowiseAgentProvider): AgentProvider
}
