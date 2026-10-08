package com.bettyai.assistant.ai

import com.bettyai.assistant.domain.repository.MemoryRepository
import com.bettyai.assistant.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the system prompt for the language model. The user's name is read from
 * the real [UserRepository] profile (never hard-coded), and relevant memories are
 * injected as context.
 */
@Singleton
class PromptBuilder @Inject constructor(
    private val userRepository: UserRepository,
    private val memoryRepository: MemoryRepository
) {

    suspend fun buildSystemPrompt(languageTag: String): String {
        val name = userRepository.displayName()
        val memories = memoryRepository.search("").take(MAX_MEMORIES)

        return buildString {
            appendLine(BASE_PROMPT)
            appendLine()
            appendLine("زبان پاسخ: $languageTag")
            if (!name.isNullOrBlank()) {
                appendLine("نام کاربر: $name (کاربر را با نامش خطاب کن).")
            } else {
                appendLine("نام کاربر مشخص نیست؛ از نام‌بردن پرهیز کن.")
            }
            if (memories.isNotEmpty()) {
                appendLine()
                appendLine("اطلاعات به‌خاطر سپرده‌شده:")
                memories.forEach { appendLine("- ${it.key}: ${it.value}") }
            }
            appendLine()
            appendLine("پاسخ‌ها را کوتاه، دوستانه و مناسب پخش صوتی بنویس.")
        }
    }

    private companion object {
        const val MAX_MEMORIES = 20
        val BASE_PROMPT = """
            تو «بتی» هستی، یک دستیار صوتی هوشمند فارسی‌زبان روی اندروید.
            وظیفه‌ی تو کمک به کاربر برای انجام کارها با استفاده از قابلیت‌ها (Skillها) است.
            تو صادق، دقیق و مختصر هستی و اگر چیزی را نمی‌دانی صادقانه می‌گویی.
        """.trimIndent()
    }
}
