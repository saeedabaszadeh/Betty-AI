package com.bettyai.assistant.ai.agent

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.data.network.HttpClientProvider
import com.bettyai.assistant.domain.provider.AgentProvider
import com.bettyai.assistant.domain.provider.AgentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Framework implementation for **Flowise** prediction endpoints. Registered but
 * not yet wired to the UI; future skills/agents can call it through the stable
 * [AgentProvider] interface.
 */
@Singleton
class FlowiseAgentProvider @Inject constructor(
    private val http: HttpClientProvider,
    private val logger: BettyLogger
) : AgentProvider {

    override val id: String = "flowise"
    override val displayName: String = "Flowise"

    override suspend fun chat(
        baseUrl: String,
        apiKey: String?,
        message: String,
        sessionId: String?
    ): AgentResult = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext AgentResult.Failure(BettyError.AiNotConfigured())

        val body = http.json.encodeToString(
            FlowiseRequest(question = message, overrideConfig = sessionId?.let { mapOf("sessionId" to it) })
        ).toRequestBody(JSON_MEDIA_TYPE)

        val builder = Request.Builder()
            .url(baseUrl.trim())
            .post(body)
            .header("Content-Type", "application/json")
        apiKey?.takeIf { it.isNotBlank() }?.let { builder.header("Authorization", "Bearer $it") }

        try {
            http.client.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext AgentResult.Failure(
                        BettyError.AiProviderUnavailable("HTTP ${response.code}")
                    )
                }
                val parsed = runCatching { http.json.decodeFromString<FlowiseResponse>(text) }.getOrNull()
                val answer = parsed?.resolved ?: text
                AgentResult.Success(answer)
            }
        } catch (t: Throwable) {
            logger.error(TAG, "Flowise call failed", t)
            AgentResult.Failure(BettyError.NetworkUnavailable(t))
        }
    }

    @Serializable
    private data class FlowiseRequest(
        val question: String,
        @SerialName("overrideConfig") val overrideConfig: Map<String, String>? = null
    )

    @Serializable
    private data class FlowiseResponse(
        val text: String? = null,
        val answer: String? = null
    ) {
        val resolved: String? get() = text ?: answer
    }

    private companion object {
        const val TAG = "FlowiseProvider"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
