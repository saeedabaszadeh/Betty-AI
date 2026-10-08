package com.bettyai.assistant.domain.model

/**
 * Local user profile. The display name is never hard-coded: it is read from this
 * profile (populated by the user during onboarding / settings) so Betty can greet
 * the user by name.
 */
data class UserProfile(
    val id: Long = SINGLETON_ID,
    val displayName: String,
    val preferredLanguage: String = "fa-IR",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    companion object {
        const val SINGLETON_ID = 1L
    }
}
