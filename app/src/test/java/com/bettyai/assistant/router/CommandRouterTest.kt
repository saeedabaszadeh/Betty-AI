package com.bettyai.assistant.router

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillManager
import com.bettyai.assistant.skills.SkillResult
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CommandRouterTest {

    private lateinit var skillManager: SkillManager
    private lateinit var logger: BettyLogger
    private lateinit var router: CommandRouter

    private val timeSkill = fakeSkill(
        id = "time",
        intents = listOf(
            SkillIntent(id = "ask_time", keywords = listOf("ساعت")),
            SkillIntent(id = "ask_date", regex = listOf("تاریخ", "چندم"))
        )
    )

    private val greetingSkill = fakeSkill(
        id = "greeting",
        intents = listOf(SkillIntent(id = "greet", keywords = listOf("سلام")))
    )

    @Before
    fun setUp() {
        skillManager = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        every { skillManager.enabledSkills() } returns listOf(timeSkill, greetingSkill)
        router = CommandRouter(skillManager, logger)
    }

    @Test
    fun `routes time question to time skill`() {
        val decision = router.route("ساعت چنده؟")
        assertTrue(decision is RouteDecision.SkillMatch)
        assertEquals("time", (decision as RouteDecision.SkillMatch).skillId)
        assertEquals("ask_time", decision.intentId)
    }

    @Test
    fun `routes date question via regex`() {
        val decision = router.route("امروز چندمه")
        assertTrue(decision is RouteDecision.SkillMatch)
        assertEquals("time", (decision as RouteDecision.SkillMatch).skillId)
        assertEquals("ask_date", decision.intentId)
    }

    @Test
    fun `routes greeting`() {
        val decision = router.route("سلام بتی")
        assertTrue(decision is RouteDecision.SkillMatch)
        assertEquals("greeting", (decision as RouteDecision.SkillMatch).skillId)
    }

    @Test
    fun `unknown utterance falls back to AI`() {
        val decision = router.route("یک شعر درباره دریا بگو")
        assertEquals(RouteDecision.AiFallback, decision)
    }

    @Test
    fun `blank utterance falls back to AI`() {
        assertEquals(RouteDecision.AiFallback, router.route("   "))
    }

    @Test
    fun `wake phrase is stripped before matching`() {
        val decision = router.route("سلام بتی ساعت چنده")
        assertTrue(decision is RouteDecision.SkillMatch)
        assertEquals("time", (decision as RouteDecision.SkillMatch).skillId)
    }

    private fun fakeSkill(id: String, intents: List<SkillIntent>) = object : Skill {
        override val id = id
        override val name = id
        override val description = id
        override val intents = intents
        override suspend fun execute(input: SkillInput, context: SkillContext) =
            SkillResult(spokenText = id)
    }
}
