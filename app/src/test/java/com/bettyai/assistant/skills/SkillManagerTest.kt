package com.bettyai.assistant.skills

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.data.local.dao.SkillDao
import com.bettyai.assistant.data.local.entity.SkillEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SkillManagerTest {

    private lateinit var skillDao: SkillDao
    private lateinit var logger: BettyLogger
    private lateinit var manager: SkillManager

    private val skillA = fakeSkill("alpha")
    private val skillB = fakeSkill("beta")

    @Before
    fun setUp() {
        skillDao = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        every { skillDao.observeAll() } returns flowOf(emptyList())
        coEvery { skillDao.getAll() } returns emptyList()
        manager = SkillManager(skillDao, logger, setOf(skillA, skillB))
    }

    @Test
    fun `registers all injected skills`() {
        assertEquals(2, manager.all().size)
        assertTrue(manager.all().map { it.id }.containsAll(listOf("alpha", "beta")))
    }

    @Test
    fun `skills are enabled by default`() {
        assertTrue(manager.isEnabled("alpha"))
        assertTrue(manager.isEnabled("beta"))
    }

    @Test
    fun `ensureInitialized persists metadata`() = runTest {
        manager.ensureInitialized()
        io.mockk.coVerify { skillDao.upsert(any()) }
    }

    @Test
    fun `ensureInitialized loads stored enabled state`() = runTest {
        val stored = listOf(
            SkillEntity(
                id = "alpha", name = "alpha", description = "", enabled = false,
                builtIn = true, permissions = "", updatedAt = 0
            )
        )
        coEvery { skillDao.getAll() } returns stored
        val localManager = SkillManager(skillDao, logger, setOf(skillA, skillB))
        localManager.ensureInitialized()
        assertFalse(localManager.isEnabled("alpha"))
        assertTrue(localManager.isEnabled("beta"))
    }

    @Test
    fun `setEnabled toggles and persists`() = runTest {
        manager.setEnabled("alpha", false)
        assertFalse(manager.isEnabled("alpha"))
        io.mockk.coVerify { skillDao.setEnabled("alpha", false, any()) }
    }

    @Test
    fun `execute runs the skill`() = runTest {
        val result = manager.execute(
            "alpha",
            SkillInput("hi", "hi", "test", "fa-IR"),
            context()
        )
        assertEquals("alpha", result.spokenText)
    }

    @Test
    fun `execute unknown skill returns graceful message`() = runTest {
        val result = manager.execute("missing", SkillInput("x", "x", "i", "fa-IR"), context())
        assertFalse(result.handled)
    }

    @Test
    fun `execute catches skill exceptions`() = runTest {
        val throwing = object : Skill {
            override val id = "boom"
            override val name = "boom"
            override val description = ""
            override val intents = emptyList<SkillIntent>()
            override suspend fun execute(input: SkillInput, context: SkillContext): SkillResult =
                throw IllegalStateException("kaboom")
        }
        val localManager = SkillManager(skillDao, logger, setOf(throwing))
        val result = localManager.execute("boom", SkillInput("x", "x", "i", "fa-IR"), context())
        assertFalse(result.handled)
    }

    private fun context() = SkillContext(
        userDisplayName = "کاربر",
        languageTag = "fa-IR",
        memoryManager = mockk(relaxed = true)
    )

    private fun fakeSkill(id: String) = object : Skill {
        override val id = id
        override val name = id
        override val description = id
        override val intents = emptyList<SkillIntent>()
        override suspend fun execute(input: SkillInput, context: SkillContext) =
            SkillResult(spokenText = id)
    }
}
