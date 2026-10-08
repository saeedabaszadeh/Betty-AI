package com.bettyai.assistant.ai

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.data.security.SecureConfigStore
import com.bettyai.assistant.domain.model.AiMessage
import com.bettyai.assistant.domain.model.AiRequest
import com.bettyai.assistant.domain.model.AiResponse
import com.bettyai.assistant.domain.model.AiRole
import com.bettyai.assistant.domain.model.AiRuntimeConfig
import com.bettyai.assistant.domain.model.Message
import com.bettyai.assistant.domain.model.MessageRole
import com.bettyai.assistant.domain.provider.AIProvider
import com.bettyai.assistant.domain.provider.NetworkMonitor
import com.bettyai.assistant.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The AI layer of the core engine. Selects the active [AIProvider] based on
 * settings, resolves runtime configuration (including the secret API key),
 * assembles the conversation context and returns a graceful fallback when the
 * network or provider is unavailable — so Betty never crashes offline.
 */
@Singleton
class AIEngine @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AIProvider>,
    private val settingsRepository: SettingsRepository,
    private val secureConfigStore: SecureConfigStore,
    private val networkMonitor: NetworkMonitor,
    private val promptBuilder: PromptBuilder,
    private val logger: BettyLogger
) {

    fun availableProviders(): List<AIProvider> = providers.sortedBy { it.id }

    /** Generate a reply for [userText] given prior [history]. Never throws. */
    suspend fun respond(userText: String, history: List<Message> = emptyList()): AiResponse {
        val settings = settingsRepository.current()

        if (!networkMonitor.isOnline()) {
            logger.info(TAG, "Offline: returning offline fallback")
            return AiResponse(
                text = OFFLINE_MESSAGE,
                providerId = FALLBACK_ID,
                fromFallback = true
            )
        }

        val provider = providers.firstOrNull { it.id == settings.aiProviderId }
            ?: providers.firstOrNull()

        if (provider == null) {
            return AiResponse(NOT_CONFIGURED_MESSAGE, FALLBACK_ID, fromFallback = true)
        }

        val config = AiRuntimeConfig(
            baseUrl = settings.aiBaseUrl,
            apiKey = secureConfigStore.getApiKey(),
            model = settings.aiModel
        )

        if (!provider.isConfigured(config)) {
            logger.info(TAG, "Provider ${provider.id} not configured")
            return AiResponse(NOT_CONFIGURED_MESSAGE, provider.id, fromFallback = true)
        }

        val systemPrompt = promptBuilder.buildSystemPrompt(settings.languageTag)
        val messages = buildList {
            history.takeLast(MAX_HISTORY).forEach { msg ->
                when (msg.role) {
                    MessageRole.USER -> add(AiMessage(AiRole.USER, msg.content))
                    MessageRole.BETTY -> add(AiMessage(AiRole.ASSISTANT, msg.content))
                    MessageRole.SYSTEM -> Unit
                }
            }
            add(AiMessage(AiRole.USER, userText))
        }

        val request = AiRequest(systemPrompt = systemPrompt, messages = messages)

        return try {
            provider.complete(request, config)
        } catch (e: BettyError) {
            logger.warning(TAG, "AI provider failed: ${e.code}")
            AiResponse(
                text = e.userMessage,
                providerId = provider.id,
                fromFallback = true
            )
        } catch (t: Throwable) {
            logger.error(TAG, "Unexpected AI failure", t)
            AiResponse(
                text = BettyError.AiProviderUnavailable().userMessage,
                providerId = provider.id,
                fromFallback = true
            )
        }
    }

    private companion object {
        const val TAG = "AIEngine"
        const val FALLBACK_ID = "fallback"
        const val MAX_HISTORY = 12
        const val OFFLINE_MESSAGE =
            "در حال حاضر به اینترنت دسترسی ندارم، اما هسته‌ی بتی فعال است. برای پاسخ‌های هوش مصنوعی به اتصال نیاز دارم."
        const val NOT_CONFIGURED_MESSAGE =
            "هوش مصنوعی هنوز تنظیم نشده است. لطفاً در تنظیمات، آدرس سرویس و مدل را وارد کنید."
    }
}
