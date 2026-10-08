package com.bettyai.assistant.skills

/**
 * Describes one way a user can invoke a skill. The [CommandRouter] scores intents
 * using [keywords] and [regex] to decide whether a skill should handle an utterance.
 *
 * @param keywords all (normalized) keywords that should be present for a strong match
 * @param regex optional regular expressions; any match yields a high confidence
 */
data class SkillIntent(
    val id: String,
    val description: String = "",
    val keywords: List<String> = emptyList(),
    val regex: List<String> = emptyList(),
    val examples: List<String> = emptyList()
)

/** Input passed to a skill when it is executed. */
data class SkillInput(
    val rawText: String,
    val normalizedText: String,
    val matchedIntentId: String,
    val languageTag: String
)

/** What a skill returns to the core engine. */
data class SkillResult(
    val spokenText: String,
    val displayText: String = spokenText,
    val handled: Boolean = true,
    val data: Map<String, String> = emptyMap()
)

/**
 * The contract every Betty capability implements. A skill is self-contained:
 * it declares its own intents and permissions and performs its work in
 * [execute]. The core engine only knows this interface, so new capabilities
 * (calls, WhatsApp, reminders, trading, ...) can be added without touching it.
 */
interface Skill {
    /** Stable unique id, e.g. "time". */
    val id: String

    /** Display name shown in settings. */
    val name: String

    /** Short description shown in settings. */
    val description: String

    /** Android permissions this skill may need (requested lazily). */
    val permissions: List<String>
        get() = emptyList()

    /** Intents this skill can handle. */
    val intents: List<SkillIntent>

    /** Whether this skill ships with the app. */
    val builtIn: Boolean
        get() = true

    /** Execute the skill for the given [input]. Must not throw. */
    suspend fun execute(input: SkillInput, context: SkillContext): SkillResult
}
