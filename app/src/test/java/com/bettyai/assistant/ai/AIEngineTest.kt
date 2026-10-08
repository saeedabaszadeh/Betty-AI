package com.bettyai.assistant.ai

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.data.security.SecureConfigStore
import com.bettyai.assistant.domain.model.AiRequest
import com.bettyai.assistant.domain.model.AiResponse
import com.bettyai.assistant.domain.model.AiRuntimeConfig
import com.bettyai.assistant.domain.model.AppSettings
import com.bettyai.assistant.domain.provider.AIProvider
import com.bettyai.assistant.domain.provider.NetworkMonitor
import com.bettyai.assistant.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIEngineTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var secureConfigStore: SecureConfigStore
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var promptBuilder: PromptBuilder
    private lateinit var logger: BettyLogger

    private val defaultSettings = AppSettings(
        aiProviderId = "fake",
        aiBaseUrl = "https://example.test/v1",
        aiModel = "test-model"
    )

    @Before
    fun setUp() {
        settingsRepository = mockk()
        secureConfigStore = mockk()
        networkMonitor = mockk()
        promptBuilder = mockk()
        logger = mockk(relaxed = true)

        coEvery { settingsRepository.current() } returns defaultSettings
        every { secureConfigStore.getApiKey() } returns "sk-test"
        every { networkMonitor.isOnline() } returns true
        coEvery { promptBuilder.buildSystemPrompt(any()) } returns "system"
    }

    private fun engineWith(vararg providers: AIProvider) = AIEngine(
        providers = providers.toSet(),
        settingsRepository = settingsRepository,
        secureConfigStore = secureConfigStore,
        networkMonitor = networkMonitor,
        promptBuilder = promptBuilder,
        logger = logger
    )

    @Test
    fun `offline returns graceful fallback`() = runTest {
        every { networkMonitor.isOnline() } returns false
        val engine = engineWith(fakeProvider(configured = true))
        val response = engine.respond("سلام")
        assertTrue(response.fromFallback)
        assertEquals("fallback", response.providerId)
    }

    @Test
    fun `not configured returns setup message`() = runTest {
        val engine = engineWith(fakeProvider(configured = false))
        val response = engine.respond("سلام")
        assertTrue(response.fromFallback)
    }

    @Test
    fun `configured provider returns its answer`() = runTest {
        val engine = engineWith(fakeProvider(configured = true, answer = "پاسخ تست"))
        val response = engine.respond("سلام")
        assertEquals("پاسخ تست", response.text)
        assertEquals("fake", response.providerId)
    }

    @Test
    fun `provider failure is caught and surfaced gracefully`() = runTest {
        val engine = engineWith(fakeProvider(configured = true, throwError = true))
        val response = engine.respond("سلام")
        assertTrue(response.fromFallback)
        assertTrue(response.text.isNotBlank())
    }

    @Test
    fun `no providers returns not configured`() = runTest {
        val engine = engineWith()
        val response = engine.respond("سلام")
        assertTrue(response.fromFallback)
    }

    private fun fakeProvider(
        configured: Boolean,
        answer: String = "ok",
        throwError: Boolean = false
    ) = object : AIProvider {
        override val id = "fake"
        override val displayName = "Fake"
        override val requiresApiKey = true
        override fun isConfigured(config: AiRuntimeConfig) = configured
        override suspend fun complete(request: AiRequest, config: AiRuntimeConfig): AiResponse {
            if (throwError) throw BettyError.AiProviderUnavailable()
            return AiResponse(text = answer, providerId = id, model = config.model)
        }
    }
}
