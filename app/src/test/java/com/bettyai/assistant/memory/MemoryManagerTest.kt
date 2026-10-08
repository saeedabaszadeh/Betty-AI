package com.bettyai.assistant.memory

import com.bettyai.assistant.domain.model.Memory
import com.bettyai.assistant.domain.model.MemoryType
import com.bettyai.assistant.domain.repository.MemoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MemoryManagerTest {

    private lateinit var repository: MemoryRepository
    private lateinit var manager: MemoryManager

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        manager = MemoryManager(repository)
    }

    @Test
    fun `remember trims and delegates to repository`() = runTest {
        manager.remember("  name ", "  علی ", MemoryType.FACT, 2)
        coVerify {
            repository.upsert(
                match {
                    it.key == "name" && it.value == "علی" && it.type == MemoryType.FACT && it.importance == 2
                }
            )
        }
    }

    @Test
    fun `recall returns the value`() = runTest {
        coEvery { repository.recall("name") } returns Memory(key = "name", value = "علی", type = MemoryType.FACT)
        assertEquals("علی", manager.recall("name"))
    }

    @Test
    fun `recall returns null when missing`() = runTest {
        coEvery { repository.recall("x") } returns null
        assertNull(manager.recall("x"))
    }

    @Test
    fun `search with blank query returns everything`() = runTest {
        coEvery { repository.observeAll() } returns flowOf(
            listOf(Memory(key = "a", value = "1", type = MemoryType.FACT))
        )
        assertEquals(1, manager.search("").size)
    }

    @Test
    fun `search with query delegates to repository`() = runTest {
        coEvery { repository.search("چای") } returns listOf(
            Memory(key = "drink", value = "چای", type = MemoryType.PREFERENCE)
        )
        assertEquals(1, manager.search("چای").size)
    }

    @Test
    fun `forget delegates to repository`() = runTest {
        manager.forget("name")
        coVerify { repository.forget("name") }
    }
}
