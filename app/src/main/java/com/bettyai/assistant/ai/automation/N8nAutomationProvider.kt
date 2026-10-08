package com.bettyai.assistant.ai.automation

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.data.network.HttpClientProvider
import com.bettyai.assistant.domain.provider.AutomationProvider
import com.bettyai.assistant.domain.provider.AutomationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Framework implementation for **n8n** webhooks. Not wired to the UI yet; it is
 * registered so future skills can trigger n8n workflows through the stable
 * [AutomationProvider] interface without touching the core.
 */
@Singleton
class N8nAutomationProvider @Inject constructor(
    private val http: HttpClientProvider,
    private val logger: BettyLogger
) : AutomationProvider {

    override val id: String = "n8n"
    override val displayName: String = "n8n"

    override fun isConfigured(baseUrl: String): Boolean = baseUrl.isNotBlank()

    override suspend fun trigger(
        baseUrl: String,
        webhookPath: String,
        payload: Map<String, String>
    ): AutomationResult = withContext(Dispatchers.IO) {
        if (!isConfigured(baseUrl)) {
            return@withContext AutomationResult.Failure(BettyError.AiNotConfigured())
        }
        val url = "${baseUrl.trim().trimEnd('/')}/${webhookPath.trim('/')}"
        val body = http.json.encodeToString(payload).toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder().url(url).post(body).build()
        try {
            http.client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    AutomationResult.Success(text)
                } else {
                    AutomationResult.Failure(BettyError.AiProviderUnavailable("HTTP ${response.code}"))
                }
            }
        } catch (t: Throwable) {
            logger.error(TAG, "n8n trigger failed", t)
            AutomationResult.Failure(BettyError.NetworkUnavailable(t))
        }
    }

    private companion object {
        const val TAG = "N8nProvider"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
