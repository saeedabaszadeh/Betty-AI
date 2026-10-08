package com.bettyai.assistant.router

/**
 * Normalizes Persian (and mixed) text so that keyword/regex matching is robust
 * against diacritics, Arabic vs. Persian letter variants, zero-width characters
 * and punctuation.
 */
object TextNormalizer {

    const val DEFAULT_WAKE_PHRASE = "سلام بتی"

    private val diacritics = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val zeroWidth = Regex("[\\u200B-\\u200F\\u202A-\\u202E\\u2066-\\u2069]")
    private val punctuation = Regex("\\p{P}")
    private val whitespace = Regex("\\s+")

    fun normalize(input: String): String {
        var s = input.trim().lowercase()
        s = s.replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
            .replace('ۀ', 'ه')
            .replace('أ', 'ا')
            .replace('إ', 'ا')
        s = diacritics.replace(s, "")
        s = zeroWidth.replace(s, " ")
        s = punctuation.replace(s, " ")
        s = whitespace.replace(s, " ")
        return s.trim()
    }

    /** Remove a leading wake phrase (e.g. "سلام بتی") from the utterance. */
    fun stripWakePhrase(input: String, phrase: String = DEFAULT_WAKE_PHRASE): String {
        val normalized = normalize(input)
        val normalizedPhrase = normalize(phrase)
        return if (normalized.startsWith(normalizedPhrase)) {
            normalized.removePrefix(normalizedPhrase).trim()
        } else {
            normalized
        }
    }
}
