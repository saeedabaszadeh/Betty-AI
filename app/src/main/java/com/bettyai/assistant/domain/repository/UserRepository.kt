package com.bettyai.assistant.domain.repository

import com.bettyai.assistant.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for the local user profile. */
interface UserRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    suspend fun setDisplayName(name: String)

    /** Returns the configured display name, or null when not set. */
    suspend fun displayName(): String?
}
