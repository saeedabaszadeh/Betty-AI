package com.bettyai.assistant.voice.wakeword

import com.bettyai.assistant.core.logger.BettyLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default, honest placeholder implementation.
 *
 * It reports [isSupported] = false so the UI can clearly communicate that
 * always-on wake word is not enabled yet, instead of faking it. Replacing this
 * with a real engine (e.g. Porcupine) is a drop-in change: implement
 * [WakeWordEngine] and bind it in the DI module.
 */
@Singleton
class DefaultWakeWordEngine @Inject constructor(
    private val logger: BettyLogger
) : WakeWordEngine {

    override val id: String = "default_unavailable"
    override val phrase: String = "سلام بتی"
    override val isSupported: Boolean = false
    override val isRunning: Boolean = false

    override fun start(onDetected: (String) -> Unit) {
        logger.info(TAG, "Wake word engine not available on this build; manual activation is used.")
    }

    override fun stop() {
        // no-op
    }

    private companion object {
        const val TAG = "WakeWord"
    }
}
