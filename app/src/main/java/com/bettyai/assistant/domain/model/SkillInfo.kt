package com.bettyai.assistant.domain.model

/**
 * Read-only description of a registered skill, used by the UI (settings screen)
 * to display and toggle skills.
 */
data class SkillInfo(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val permissions: List<String>,
    val builtIn: Boolean
)
