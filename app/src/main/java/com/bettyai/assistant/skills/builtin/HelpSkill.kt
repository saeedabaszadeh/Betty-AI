package com.bettyai.assistant.skills.builtin

import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillResult
import javax.inject.Inject
import javax.inject.Singleton

/** Explains what Betty can currently do. */
@Singleton
class HelpSkill @Inject constructor() : Skill {

    override val id: String = "help"
    override val name: String = "راهنما"
    override val description: String = "توضیح قابلیت‌های فعلی بتی"

    override val intents: List<SkillIntent> = listOf(
        SkillIntent(
            id = "help",
            description = "درخواست کمک",
            keywords = listOf("کمک"),
            regex = listOf("چه کار", "چیکار", "چی میتونی", "help", "راهنما"),
            examples = listOf("چه کارهایی می‌تونی انجام بدی")
        )
    )

    override suspend fun execute(input: SkillInput, context: SkillContext): SkillResult {
        val text = buildString {
            append("من هسته‌ی بتی هستم. الان می‌تونم ساعت و تاریخ بگم، ")
            append("به خاطر بسپارم، و به سؤال‌های عمومی با هوش مصنوعی جواب بدم. ")
            append("قابلیت‌های بیشتری مثل تماس، پیام و یادآور به‌زودی اضافه می‌شن.")
        }
        return SkillResult(spokenText = text)
    }
}
