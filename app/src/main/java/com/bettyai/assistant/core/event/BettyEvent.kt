package com.bettyai.assistant.core.event

import com.bettyai.assistant.core.state.BettyState

/**
 * Domain events broadcast on the [EventBus]. New modules (skills, providers)
 * can publish and observe events without depending on each other.
 */
sealed interface BettyEvent {
    data class StateChanged(val state: BettyState) : BettyEvent
    data class UserSpoke(val text: String) : BettyEvent
    data class BettyReplied(val text: String, val skillId: String? = null) : BettyEvent
    data class SkillExecuted(val skillId: String, val success: Boolean) : BettyEvent
    data class WakeWordDetected(val phrase: String) : BettyEvent
    data class ErrorOccurred(val code: String, val userMessage: String) : BettyEvent
    data class NetworkChanged(val online: Boolean) : BettyEvent
}
