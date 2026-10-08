package com.bettyai.assistant.domain.provider

import com.bettyai.assistant.domain.model.TtsResult
import com.bettyai.assistant.domain.model.VoiceInfo

/**
 * A pluggable text-to-speech engine. The default implementation wraps Android's
 * [android.speech.tts.TextToSpeech]. Providers are swappable without touching the
 * core engine.
 */
interface TtsProvider {
    val id: String
    val displayName: String

    fun isAvailable(): Boolean

    /** Speak [text] using the given BCP-47 [languageTag] and optional voice. */
    suspend fun speak(text: String, languageTag: String, voiceName: String? = null): TtsResult

    /** Voices installed on the device. */
    fun availableVoices(): List<VoiceInfo>

    /** Stop any ongoing speech immediately. */
    fun stop()

    /** Release native resources. */
    fun shutdown()
}
