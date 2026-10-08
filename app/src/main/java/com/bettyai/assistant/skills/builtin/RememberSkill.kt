package com.bettyai.assistant.skills.builtin

import com.bettyai.assistant.domain.model.MemoryType
import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores a fact in long-term memory: "به خاطر بسپار که ...". Demonstrates how a
 * skill uses [SkillContext] to interact with the memory subsystem.
 */
@Singleton
class RememberSkill @Inject constructor() : Skill {

    override val id: String = "remember"
    override val name: String = "به خاطر سپردن"
    override val description: String = "ذخیره‌ی اطلاعات در حافظه‌ی بتی"

    override val intents: List<SkillIntent> = listOf(
        SkillIntent(
            id = "remember",
            description = "ذخیره‌ی یک نکته",
            keywords = listOf("به خاطر بسپار"),
            regex = listOf("یادت باشه", "به خاطر بسپار"),
            examples = listOf("به خاطر بسپار که من چای دوست دارم")
        )
    )

    override suspend fun execute(input: SkillInput, context: SkillContext): SkillResult {
        val fact = input.rawText
            .substringAfter("به خاطر بسپار", input.rawText)
            .substringAfter("یادت باشه", input.rawText)
            .trim()
            .removePrefix("که")
            .trim()

        if (fact.isBlank()) {
            return SkillResult(spokenText = "چه چیزی رو به خاطر بسپارم؟")
        }

        val key = "note:${fact.take(40)}"
        context.remember(key = key, value = fact, type = MemoryType.FACT, importance = 1)
        return SkillResult(spokenText = "باشه، به خاطر سپردم: $fact")
    }
}
