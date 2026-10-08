package com.bettyai.assistant.skills.builtin

import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillResult
import com.bettyai.assistant.util.PersianText
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** Answers "ساعت چنده؟" and "امروز چندمه؟". */
@Singleton
class TimeSkill @Inject constructor() : Skill {

    override val id: String = "time"
    override val name: String = "زمان و تاریخ"
    override val description: String = "گفتن ساعت و تاریخ شمسی"

    override val intents: List<SkillIntent> = listOf(
        SkillIntent(
            id = "ask_time",
            description = "پرسیدن ساعت",
            keywords = listOf("ساعت"),
            examples = listOf("ساعت چنده", "الان ساعت چند است")
        ),
        SkillIntent(
            id = "ask_date",
            description = "پرسیدن تاریخ",
            regex = listOf("تاریخ", "چندم", "چندمه", "چه روزی"),
            examples = listOf("امروز چندمه", "تاریخ امروز")
        )
    )

    override suspend fun execute(input: SkillInput, context: SkillContext): SkillResult {
        val now = LocalDateTime.now()
        return when (input.matchedIntentId) {
            "ask_time" -> {
                val time = PersianText.toPersianDigits("%02d:%02d".format(now.hour, now.minute))
                SkillResult(spokenText = "الان ساعت $time است.", data = mapOf("time" to time))
            }

            "ask_date" -> {
                val date = now.toLocalDate()
                val weekday = PersianText.weekdayName(date)
                val jalali = PersianText.formatJalali(date)
                SkillResult(
                    spokenText = "امروز $weekday، $jalali است.",
                    data = mapOf("date" to jalali, "weekday" to weekday)
                )
            }

            else -> SkillResult(spokenText = "متوجه نشدم.", handled = false)
        }
    }
}
