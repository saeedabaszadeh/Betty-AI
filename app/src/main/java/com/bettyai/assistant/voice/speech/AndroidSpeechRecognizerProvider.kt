package com.bettyai.assistant.voice.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.domain.model.SpeechConfig
import com.bettyai.assistant.domain.model.SpeechResult
import com.bettyai.assistant.domain.provider.SpeechProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Speech-to-text provider built on Android's on-device [SpeechRecognizer].
 *
 * This is the default provider. Because it implements the stable [SpeechProvider]
 * interface, additional engines (Cloud Speech, Whisper, Vosk, ...) can be added
 * later without any change to the core.
 */
@Singleton
class AndroidSpeechRecognizerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: BettyLogger
) : SpeechProvider {

    override val id: String = "android_speech"
    override val displayName: String = "Android SpeechRecognizer"

    @Volatile
    private var activeRecognizer: SpeechRecognizer? = null

    override fun isAvailable(): Boolean =
        runCatching { SpeechRecognizer.isRecognitionAvailable(context) }.getOrDefault(false)

    override suspend fun recognize(config: SpeechConfig): SpeechResult = withContext(Dispatchers.Main) {
        if (!isAvailable()) {
            return@withContext SpeechResult.Failure(BettyError.MicrophoneUnavailable())
        }

        suspendCancellableCoroutine<SpeechResult> { cont ->
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            activeRecognizer = recognizer

            fun finish(result: SpeechResult) {
                runCatching { recognizer.destroy() }
                if (activeRecognizer === recognizer) activeRecognizer = null
                if (cont.isActive) cont.resume(result)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    logger.debug(TAG, "Ready for speech")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                    if (text.isNullOrEmpty()) {
                        finish(SpeechResult.NoMatch)
                    } else {
                        finish(SpeechResult.Success(text))
                    }
                }

                override fun onError(error: Int) {
                    logger.debug(TAG, "Recognition error code=$error")
                    finish(mapError(error))
                }
            })

            cont.invokeOnCancellation {
                runCatching { recognizer.cancel() }
                runCatching { recognizer.destroy() }
                if (activeRecognizer === recognizer) activeRecognizer = null
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.languageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, config.partialResults)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                if (config.preferOffline) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
            }

            runCatching { recognizer.startListening(intent) }
                .onFailure { finish(SpeechResult.Failure(BettyError.SpeechRecognitionFailed("start failed", it))) }
        }
    }

    override fun cancel() {
        activeRecognizer?.let { runCatching { it.cancel() } }
    }

    private fun mapError(error: Int): SpeechResult = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechResult.NoMatch

        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            SpeechResult.Failure(BettyError.PermissionDenied(Manifest.permission.RECORD_AUDIO))

        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            SpeechResult.Failure(BettyError.NetworkUnavailable())

        SpeechRecognizer.ERROR_AUDIO,
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            SpeechResult.Failure(BettyError.MicrophoneUnavailable())

        else -> SpeechResult.Failure(BettyError.SpeechRecognitionFailed("code $error"))
    }

    private companion object {
        const val TAG = "AndroidSpeech"
    }
}
