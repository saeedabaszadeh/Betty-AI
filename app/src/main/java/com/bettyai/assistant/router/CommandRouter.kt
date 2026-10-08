package com.bettyai.assistant.router

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillManager
import javax.inject.Inject
import javax.inject.Singleton

/** The router's decision for a single utterance. */
sealed interface RouteDecision {
    data class SkillMatch(
        val skillId: String,
        val intentId: String,
        val confidence: Double
    ) : RouteDecision

    data object AiFallback : RouteDecision
}

/**
 * Decides whether an utterance belongs to a skill or should be answered by the AI.
 *
 * Matching is keyword/regex based and intentionally simple for the core version.
 * It is fully decoupled from skills (which only declare their intents) and from
 * the AI engine.
 */
@Singleton
class CommandRouter @Inject constructor(
    private val skillManager: SkillManager,
    private val logger: BettyLogger
) {

    fun route(rawText: String): RouteDecision {
        val stripped = TextNormalizer.stripWakePhrase(rawText)
        // If the user only said the wake phrase, fall back to the full text so a
        // greeting skill can still respond.
        val text = stripped.ifBlank { TextNormalizer.normalize(rawText) }
        if (text.isBlank()) return RouteDecision.AiFallback

        var bestScore = 0.0
        var best: RouteDecision.SkillMatch? = null

        skillManager.enabledSkills().forEach { skill ->
            skill.intents.forEach { intent ->
                val score = score(text, intent)
                if (score > bestScore) {
                    bestScore = score
                    best = RouteDecision.SkillMatch(skill.id, intent.id, score)
                }
            }
        }

        val decision = best
        if (decision != null) {
            logger.debug(TAG, "Routed to skill=${decision.skillId} intent=${decision.intentId} score=${decision.confidence}")
        }
        return decision ?: RouteDecision.AiFallback
    }

    private fun score(text: String, intent: SkillIntent): Double {
        if (intent.regex.isNotEmpty()) {
            val hit = intent.regex.any { pattern ->
                runCatching { Regex(pattern).containsMatchIn(text) }.getOrDefault(false)
            }
            if (hit) return 1.0
        }

        if (intent.keywords.isEmpty()) return 0.0

        val keywords = intent.keywords.map { TextNormalizer.normalize(it) }.filter { it.isNotEmpty() }
        if (keywords.isEmpty()) return 0.0

        val matched = keywords.count { text.contains(it) }
        if (matched == 0) return 0.0

        val ratio = matched.toDouble() / keywords.size
        return if (ratio >= 0.6) 0.6 + 0.4 * ratio else 0.0
    }

    private companion object {
        const val TAG = "CommandRouter"
    }
}
