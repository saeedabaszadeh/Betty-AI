package com.bettyai.assistant.domain.provider

import kotlinx.coroutines.flow.Flow

/**
 * Observes connectivity so Betty can degrade gracefully offline instead of
 * crashing. Implemented in the data layer using Android's ConnectivityManager.
 */
interface NetworkMonitor {
    /** Emits the current connectivity state and subsequent changes. */
    val online: Flow<Boolean>

    /** Synchronous best-effort check. */
    fun isOnline(): Boolean
}
