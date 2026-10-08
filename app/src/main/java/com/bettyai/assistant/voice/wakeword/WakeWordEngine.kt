package com.bettyai.assistant.voice.wakeword

/**
 * Abstraction for wake-word detection ("سلام بتی").
 *
 * Android does not allow continuous background microphone access without a
 * visible foreground service and explicit user consent, so the real engine is
 * intentionally left as a pluggable interface. A future implementation
 * (Porcupine, Vosk, an on-device keyword spotter, ...) implements this interface
 * and is registered with Hilt — the core engine does not change.
 *
 * No hidden/unauthorized always-on microphone tricks are used.
 */
interface WakeWordEngine {
    val id: String

    /** The wake phrase this engine listens for. */
    val phrase: String

    /** Whether this engine can run on the current device/configuration. */
    val isSupported: Boolean

    val isRunning: Boolean

    /** Start listening. [onDetected] is invoked with the matched phrase. */
    fun start(onDetected: (String) -> Unit)

    /** Stop listening and release resources. */
    fun stop()
}
