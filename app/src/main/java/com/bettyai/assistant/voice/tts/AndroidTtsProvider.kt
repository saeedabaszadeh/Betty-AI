package com.bettyai.assistant.voice.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.domain.model.TtsResult
import com.bettyai.assistant.domain.model.VoiceInfo
import com.bettyai.assistant.domain.provider.TtsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Text-to-speech provider built on Android's [TextToSpeech]. Speaks Persian by
 * default (configurable via settings). Swappable for any other engine through the
 * [TtsProvider] interface.
 */
@Singleton
class AndroidTtsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: BettyLogger
) : TtsProvider {

    override val id: String = "android_tts"
    override val displayName: String = "Android TextToSpeech"

    private var tts: TextToSpeech? = null

    @Volatile
    private var ready = false

    private val initMutex = Mutex()

    override fun isAvailable(): Boolean = true

    private suspend fun ensureInitialized(): Boolean = initMutex.withLock {
        if (ready && tts != null) return true

        val success = suspendCancellableCoroutine { cont ->
            val engine = TextToSpeech(context) { status ->
                if (cont.isActive) cont.resume(status == TextToSpeech.SUCCESS)
            }
            tts = engine
        }

        ready = success
        if (!success) {
            runCatching { tts?.shutdown() }
            tts = null
            logger.warning(TAG, "TextToSpeech initialization failed")
        }
        success
    }

    override suspend fun speak(text: String, languageTag: String, voiceName: String?): TtsResult {
        if (text.isBlank()) return TtsResult.Spoken
        if (!ensureInitialized()) return TtsResult.Failure(BettyError.TtsUnavailable("init failed"))
        val engine = tts ?: return TtsResult.Failure(BettyError.TtsUnavailable("engine null"))

        return withContext(Dispatchers.Main) {
            val locale = Locale.forLanguageTag(languageTag)
            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                logger.warning(TAG, "Language '$languageTag' unavailable, using device default")
                engine.setLanguage(Locale.getDefault())
            }

            voiceName?.let { name ->
                runCatching {
                    engine.voices?.firstOrNull { it.name == name }?.let { engine.voice = it }
                }
            }

            suspendCancellableCoroutine { cont ->
                val utteranceId = UUID.randomUUID().toString()
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}

                    override fun onDone(id: String?) {
                        if (cont.isActive) cont.resume(TtsResult.Spoken)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(id: String?) {
                        if (cont.isActive) {
                            cont.resume(TtsResult.Failure(BettyError.TtsUnavailable("utterance error")))
                        }
                    }

                    override fun onError(id: String?, errorCode: Int) {
                        if (cont.isActive) {
                            cont.resume(TtsResult.Failure(BettyError.TtsUnavailable("utterance error $errorCode")))
                        }
                    }
                })

                val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
                if (result == TextToSpeech.ERROR && cont.isActive) {
                    cont.resume(TtsResult.Failure(BettyError.TtsUnavailable("speak failed")))
                }

                cont.invokeOnCancellation { runCatching { engine.stop() } }
            }
        }
    }

    override fun availableVoices(): List<VoiceInfo> =
        runCatching {
            val engine = tts ?: return emptyList()
            val defaultVoiceName = runCatching { engine.defaultVoice?.name }.getOrNull()
            engine.voices
                ?.map {
                    VoiceInfo(
                        name = it.name,
                        locale = it.locale.toLanguageTag(),
                        isDefault = it.name == defaultVoiceName
                    )
                }
                ?.sortedBy { it.name }
        }.getOrNull().orEmpty()

    override fun stop() {
        runCatching { tts?.stop() }
    }

    override fun shutdown() {
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        ready = false
    }

    private companion object {
        const val TAG = "AndroidTts"
    }
}
