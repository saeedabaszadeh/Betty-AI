package com.bettyai.assistant.core.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BettyResultTest {

    @Test
    fun `success exposes data`() {
        val result: BettyResult<Int> = BettyResult.Success(5)
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull())
        assertNull(result.errorOrNull())
    }

    @Test
    fun `failure exposes error`() {
        val error = BettyError.NetworkUnavailable()
        val result: BettyResult<Int> = BettyResult.Failure(error)
        assertTrue(!result.isSuccess)
        assertNull(result.getOrNull())
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun `map transforms success`() {
        val mapped = BettyResult.Success(2).map { it * 3 }
        assertEquals(6, mapped.getOrNull())
    }

    @Test
    fun `map preserves failure`() {
        val error = BettyError.StorageError()
        val mapped = BettyResult.Failure(error).map { it }
        assertEquals(error, mapped.errorOrNull())
    }

    @Test
    fun `onSuccess runs only for success`() {
        var ran = false
        BettyResult.Success(1).onSuccess { ran = true }
        assertTrue(ran)

        ran = false
        BettyResult.Failure(BettyError.Unknown()).onSuccess { ran = true }
        assertTrue(!ran)
    }

    @Test
    fun `onFailure runs only for failure`() {
        var captured: BettyError? = null
        BettyResult.Failure(BettyError.Disabled()).onFailure { captured = it }
        assertTrue(captured is BettyError.Disabled)
    }

    @Test
    fun `every error has a persian user message`() {
        val errors = listOf(
            BettyError.MicrophoneUnavailable(),
            BettyError.SpeechRecognitionFailed(),
            BettyError.NetworkUnavailable(),
            BettyError.AiProviderUnavailable(),
            BettyError.AiNotConfigured(),
            BettyError.TtsUnavailable(),
            BettyError.PermissionDenied("android.permission.RECORD_AUDIO"),
            BettyError.StorageError(),
            BettyError.Disabled(),
            BettyError.Unknown()
        )
        errors.forEach { error ->
            assertTrue("error ${error.code} should have a user message", error.userMessage.isNotBlank())
        }
    }
}
