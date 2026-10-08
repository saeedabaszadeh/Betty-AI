package com.bettyai.assistant.domain.model

/** Configuration for a speech-recognition session. */
data class SpeechConfig(
    val languageTag: String = "fa-IR",
    val preferOffline: Boolean = false,
    val partialResults: Boolean = true
)

/** Outcome of a speech-recognition attempt. */
sealed interface SpeechResult {
    data class Success(val text: String) : SpeechResult
    data object NoMatch : SpeechResult
    data class Failure(val error: com.bettyai.assistant.core.result.BettyError) : SpeechResult
    data object Cancelled : SpeechResult
}

/** Outcome of a text-to-speech attempt. */
sealed interface TtsResult {
    data object Spoken : TtsResult
    data class Failure(val error: com.bettyai.assistant.core.result.BettyError) : TtsResult
}

/** Describes an installed TTS voice. */
data class VoiceInfo(
    val name: String,
    val locale: String,
    val isDefault: Boolean
)
