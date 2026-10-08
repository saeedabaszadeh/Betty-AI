package com.bettyai.assistant.ai

import com.bettyai.assistant.ai.dto.ApiErrorResponse
import com.bettyai.assistant.ai.dto.ChatCompletionRequest
import com.bettyai.assistant.ai.dto.ChatCompletionResponse
import com.bettyai.assistant.ai.dto.ChatMessageDto
import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.data.network.HttpClientProvider
import com.bettyai.assistant.domain.model.AiRequest
import com.bettyai.assistant.domain.model.AiResponse
import com.bettyai.assistant.domain.model.AiRuntimeConfig
import com.bettyai.assistant.domain.provider.AIProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default AI provider that speaks the widely-supported OpenAI "Chat Completions"
 * wire format. Works with OpenAI, Azure OpenAI, OpenRouter, Groq, Together,
 * local llama.cpp / Ollama gateways and any custom backend exposing the same API.
 *
 * The base URL, model and API key are supplied at call time via [AiRuntimeConfig];
 * nothing is hard-coded.
 */
@Singleton
class OpenAiCompatibleProvider @Inject constructor(
    private val http: HttpClientProvider,
    private val logger: BettyLogger
) : AIProvider {

    override val id: String = "openai_compatible"
    override val displayName: String = "OpenAI-compatible API"
    override val requiresApiKey: Boolean = true

    override fun isConfigured(config: AiRuntimeConfig): Boolean =
        config.baseUrl.isNotBlank() && config.model.isNotBlank()

    override suspend fun complete(request: AiRequest, config: AiRuntimeConfig): AiResponse =
        withContext(Dispatchers.IO) {
            if (!isConfigured(config)) throw BettyError.AiNotConfigured()

            val endpoint = endpoint(config.baseUrl)
            val payload = ChatCompletionRequest(
                model = config.model,
                messages = buildList {
                    add(ChatMessageDto("system", request.systemPrompt))
                    request.messages.forEach { add(ChatMessageDto(it.role.wire, it.content)) }
                },
                temperature = request.temperature,
                maxTokens = request.maxTokens
            )

            val body = http.json.encodeToString(payload)
                .toRequestBody(JSON_MEDIA_TYPE)

            val builder = Request.Builder()
                .url(endpoint)
                .post(body)
                .header("Content-Type", "application/json")

            config.apiKey?.takeIf { it.isNotBlank() }?.let {
                builder.header("Authorization", "Bearer $it")
            }

            logger.debug(TAG, "POST $endpoint model=${config.model}")

            try {
                http.client.newCall(builder.build()).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val message = runCatching {
                            http.json.decodeFromString<ApiErrorResponse>(text).error?.message
                        }.getOrNull() ?: "HTTP ${response.code}"
                        logger.warning(TAG, "Provider error: $message")
                        throw BettyError.AiProviderUnavailable(message)
                    }
                    val parsed = http.json.decodeFromString<ChatCompletionResponse>(text)
                    val content = parsed.choices.firstOrNull()?.message?.content?.trim()
                    if (content.isNullOrEmpty()) {
                        throw BettyError.AiProviderUnavailable("empty response")
                    }
                    AiResponse(text = content, providerId = id, model = parsed.model ?: config.model)
                }
            } catch (e: BettyError) {
                throw e
            } catch (t: Throwable) {
                logger.error(TAG, "AI request failed", t)
                throw BettyError.AiProviderUnavailable(t.message ?: "network error", t)
            }
        }

    private fun endpoint(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return if (trimmed.endsWith("/chat/completions")) trimmed else "$trimmed/chat/completions"
    }

    private companion object {
        const val TAG = "OpenAiProvider"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
