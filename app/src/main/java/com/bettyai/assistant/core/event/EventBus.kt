package com.bettyai.assistant.core.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A process-wide, in-memory event bus. Uses a [MutableSharedFlow] with a small
 * buffer so publishers never block; slow consumers simply miss stale events.
 */
@Singleton
class EventBus @Inject constructor() {

    private val _events = MutableSharedFlow<BettyEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    val events: SharedFlow<BettyEvent> = _events.asSharedFlow()

    fun publish(event: BettyEvent) {
        _events.tryEmit(event)
    }
}
