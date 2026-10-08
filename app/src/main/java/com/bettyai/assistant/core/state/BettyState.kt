package com.bettyai.assistant.core.state

/**
 * The Betty state machine.
 *
 * IDLE → (wake word / manual tap) → LISTENING → PROCESSING → SPEAKING → IDLE
 * Any stage may transition to ERROR, and the whole engine can be DISABLED.
 */
enum class BettyState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR,
    DISABLED;

    val isBusy: Boolean
        get() = this == LISTENING || this == PROCESSING || this == SPEAKING
}

/** Snapshot of Betty's current status, exposed to the UI. */
data class BettyStatus(
    val state: BettyState = BettyState.IDLE,
    val detail: String? = null,
    val isOnline: Boolean = true,
    val lastError: String? = null
)
