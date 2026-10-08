package com.bettyai.assistant.voice

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.domain.model.TtsResult
import com.bettyai.assistant.domain.model.VoiceInfo
import com.bettyai.assistant.domain.provider.TtsProvider
import com.bettyai.assistant.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Selects the active [TtsProvider] and applies user settings (language, voice,
 * voice on/off). Providers are injected as a set, so adding a new engine is a
 * one-line DI change.
 */
@Singleton
class TtsManager @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards TtsProvider>,
    private val settingsRepository: SettingsRepository,
    private val logger: BettyLogger
) {

    fun activeProvider(): TtsProvider? = providers.minByOrNull { it.id }

    /** Speak [text] honoring current settings. Never throws. */
    suspend fun speak(text: String): TtsResult {
        val settings = settingsRepository.current()
        if (!settings.voiceEnabled) {
            logger.debug(TAG, "Voice disabled; skipping TTS")
            return TtsResult.Spoken
        }
        val provider = activeProvider()
            ?: return TtsResult.Failure(BettyError.TtsUnavailable("no provider"))
        return provider.speak(text, settings.languageTag, settings.ttsVoiceName)
    }

    fun stop() {
        activeProvider()?.stop()
    }

    fun availableVoices(): List<VoiceInfo> = activeProvider()?.availableVoices().orEmpty()

    fun shutdown() {
        providers.forEach { runCatching { it.shutdown() } }
    }

    private companion object {
        const val TAG = "TtsManager"
    }
}
