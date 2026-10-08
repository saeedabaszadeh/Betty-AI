package com.bettyai.assistant.skills.builtin

import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillResult
import javax.inject.Inject
import javax.inject.Singleton

/** Introduces Betty. */
@Singleton
class AboutSkill @Inject constructor() : Skill {

    override val id: String = "about"
    override val name: String = "درباره بتی"
    override val description: String = "معرفی خود بتی"

    override val intents: List<SkillIntent> = listOf(
        SkillIntent(
            id = "about",
            description = "پرسیدن درباره بتی",
            regex = listOf("تو کی هستی", "بتی کی", "درباره تو", "خودت رو معرفی", "اسم تو")
        )
    )

    override suspend fun execute(input: SkillInput, context: SkillContext): SkillResult {
        return SkillResult(
            spokenText = "من بتی هستم، دستیار صوتی هوشمند تو روی اندروید. " +
                "برای انجام کارها از قابلیت‌هایی به نام Skill استفاده می‌کنم و می‌تونم با هوش مصنوعی هم صحبت کنم."
        )
    }
}
