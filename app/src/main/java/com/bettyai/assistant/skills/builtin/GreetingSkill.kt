package com.bettyai.assistant.skills.builtin

import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Greets the user by name. The name comes from the real user profile
 * ([SkillContext.userDisplayName]) and is never hard-coded.
 */
@Singleton
class GreetingSkill @Inject constructor() : Skill {

    override val id: String = "greeting"
    override val name: String = "احوال‌پرسی"
    override val description: String = "پاسخ به سلام و احوال‌پرسی"

    override val intents: List<SkillIntent> = listOf(
        SkillIntent(
            id = "greet",
            description = "سلام کردن",
            keywords = listOf("سلام"),
            regex = listOf("\\b(سلام|درود|های|hi|hello)\\b"),
            examples = listOf("سلام بتی", "درود")
        )
    )

    override suspend fun execute(input: SkillInput, context: SkillContext): SkillResult {
        val name = context.userDisplayName
        val text = if (!name.isNullOrBlank()) {
            "سلام $name، چطور می‌تونم کمکت کنم؟"
        } else {
            "سلام! چطور می‌تونم کمکت کنم؟"
        }
        return SkillResult(spokenText = text)
    }
}
