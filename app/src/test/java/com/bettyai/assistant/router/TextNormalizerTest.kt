package com.bettyai.assistant.router

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun `normalize lowercases and trims`() {
        assertEquals("سلام بتی", TextNormalizer.normalize("  سلام بتی  "))
    }

    @Test
    fun `normalize converts arabic letters to persian`() {
        // Arabic Yeh (U+064A) and Kaf (U+0643) should become Persian Yeh/Kaf.
        val input = "\u064a\u0627\u062f \u0643\u0631\u062f\u0646"
        val result = TextNormalizer.normalize(input)
        assertTrue("should contain Persian yeh", result.contains('\u06cc'))
        assertTrue("should contain Persian kaf", result.contains('\u06a9'))
    }

    @Test
    fun `normalize strips diacritics`() {
        val input = "\u0633\u064e\u0644\u0627\u0645" // سلام with fatha
        assertEquals("سلام", TextNormalizer.normalize(input))
    }

    @Test
    fun `normalize removes punctuation and collapses whitespace`() {
        val input = "سلام،   بتی!!!"
        assertEquals("سلام بتی", TextNormalizer.normalize(input))
    }

    @Test
    fun `normalize removes zero width characters`() {
        val input = "سلام\u200cبتی" // ZWNJ between words
        assertEquals("سلام بتی", TextNormalizer.normalize(input))
    }

    @Test
    fun `stripWakePhrase removes leading phrase`() {
        val result = TextNormalizer.stripWakePhrase("سلام بتی ساعت چنده")
        assertEquals("ساعت چنده", result)
    }

    @Test
    fun `stripWakePhrase returns full text when no phrase`() {
        val result = TextNormalizer.stripWakePhrase("ساعت چنده")
        assertEquals("ساعت چنده", result)
    }

    @Test
    fun `stripWakePhrase is tolerant to arabic variants and punctuation`() {
        // "سلام" with Arabic yeh + comma before the wake phrase.
        val result = TextNormalizer.stripWakePhrase("سلام، بتي حالا ساعت چنده")
        assertTrue(result.contains("ساعت"))
    }
}
