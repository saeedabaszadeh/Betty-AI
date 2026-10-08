package com.bettyai.assistant.voice

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.domain.model.SpeechConfig
import com.bettyai.assistant.domain.model.SpeechResult
import com.bettyai.assistant.domain.provider.SpeechProvider
import com.bettyai.assistant.domain.repository.SettingsRepository
import com.bettyai.assistant.permissions.PermissionManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The voice input layer of the core engine. Chooses an active [SpeechProvider],
 * verifies the microphone permission and returns a typed [SpeechResult]. It never
 * throws; every failure is expressed as data so the caller can react gracefully.
 */
@Singleton
class VoiceEngine @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards SpeechProvider>,
    private val settingsRepository: SettingsRepository,
    private val permissionManager: PermissionManager,
    private val logger: BettyLogger
) {

    fun activeProvider(): SpeechProvider? = providers.minByOrNull { it.id }

    fun isAvailable(): Boolean = activeProvider()?.isAvailable() == true

    /** Recognize a single utterance using the active provider. */
    suspend fun listen(): SpeechResult {
        if (!permissionManager.hasMicrophone()) {
            logger.info(TAG, "Microphone permission missing")
            return SpeechResult.Failure(BettyError.PermissionDenied(android.Manifest.permission.RECORD_AUDIO))
        }

        val provider = activeProvider()
        if (provider == null || !provider.isAvailable()) {
            logger.warning(TAG, "No speech provider available")
            return SpeechResult.Failure(BettyError.MicrophoneUnavailable())
        }

        val settings = settingsRepository.current()
        val config = SpeechConfig(languageTag = settings.languageTag)

        return try {
            provider.recognize(config)
        } catch (t: Throwable) {
            logger.error(TAG, "Recognition threw", t)
            SpeechResult.Failure(BettyError.SpeechRecognitionFailed("unexpected", t))
        }
    }

    fun cancel() {
        activeProvider()?.cancel()
    }

    private companion object {
        const val TAG = "VoiceEngine"
    }
}
