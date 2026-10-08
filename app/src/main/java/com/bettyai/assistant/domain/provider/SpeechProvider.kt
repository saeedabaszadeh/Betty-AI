package com.bettyai.assistant.domain.provider

import com.bettyai.assistant.domain.model.SpeechConfig
import com.bettyai.assistant.domain.model.SpeechResult

/**
 * A pluggable speech-to-text engine.
 *
 * The default implementation wraps Android's [android.speech.SpeechRecognizer].
 * Future providers (Cloud Speech, Whisper, Vosk, ...) simply implement this
 * interface; the [com.bettyai.assistant.voice.VoiceEngine] does not change.
 */
interface SpeechProvider {
    val id: String
    val displayName: String

    /** Whether the underlying engine is present and usable on this device. */
    fun isAvailable(): Boolean

    /** Recognize a single utterance. */
    suspend fun recognize(config: SpeechConfig): SpeechResult

    /** Abort an in-flight recognition session. */
    fun cancel()
}
