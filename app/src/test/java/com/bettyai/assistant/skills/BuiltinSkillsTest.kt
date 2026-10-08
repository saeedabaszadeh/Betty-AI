package com.bettyai.assistant.skills

import com.bettyai.assistant.memory.MemoryManager
import com.bettyai.assistant.skills.builtin.AboutSkill
import com.bettyai.assistant.skills.builtin.GreetingSkill
import com.bettyai.assistant.skills.builtin.HelpSkill
import com.bettyai.assistant.skills.builtin.RememberSkill
import com.bettyai.assistant.skills.builtin.TimeSkill
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltinSkillsTest {

    private val memoryManager: MemoryManager = mockk(relaxed = true)

    private fun context(name: String? = "علی") = SkillContext(
        userDisplayName = name,
        languageTag = "fa-IR",
        memoryManager = memoryManager
    )

    private fun input(text: String, intent: String) =
        SkillInput(rawText = text, normalizedText = text, matchedIntentId = intent, languageTag = "fa-IR")

    @Test
    fun `time skill answers the time`() = runTest {
        val result = TimeSkill().execute(input("ساعت چنده", "ask_time"), context())
        assertTrue(result.handled)
        assertTrue(result.spokenText.contains("ساعت"))
    }

    @Test
    fun `time skill answers the date`() = runTest {
        val result = TimeSkill().execute(input("امروز چندمه", "ask_date"), context())
        assertTrue(result.handled)
        assertTrue(result.data.containsKey("date"))
    }

    @Test
    fun `greeting skill uses the real user name`() = runTest {
        val result = GreetingSkill().execute(input("سلام", "greet"), context("علی"))
        assertTrue(result.spokenText.contains("علی"))
    }

    @Test
    fun `greeting skill works without a name`() = runTest {
        val result = GreetingSkill().execute(input("سلام", "greet"), context(null))
        assertTrue(result.spokenText.contains("سلام"))
        assertFalse(result.spokenText.contains("null"))
    }

    @Test
    fun `remember skill stores the fact`() = runTest {
        val result = RememberSkill().execute(
            input("به خاطر بسپار که من چای دوست دارم", "remember"),
            context()
        )
        assertTrue(result.handled)
        coVerify { memoryManager.remember(any(), any(), any(), any()) }
    }

    @Test
    fun `remember skill asks what to remember when empty`() = runTest {
        val result = RememberSkill().execute(input("به خاطر بسپار", "remember"), context())
        assertTrue(result.spokenText.isNotBlank())
    }

    @Test
    fun `help and about skills respond`() = runTest {
        assertTrue(HelpSkill().execute(input("کمک", "help"), context()).spokenText.isNotBlank())
        assertTrue(AboutSkill().execute(input("تو کی هستی", "about"), context()).spokenText.isNotBlank())
    }
}
