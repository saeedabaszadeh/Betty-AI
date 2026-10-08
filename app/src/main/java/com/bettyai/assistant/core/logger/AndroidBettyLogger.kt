package com.bettyai.assistant.core.logger

import android.util.Log
import com.bettyai.assistant.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [BettyLogger] backed by [android.util.Log].
 *
 * In release builds DEBUG logs are suppressed entirely, and callers are expected
 * to never pass secrets. A defensive scrub is applied to mask anything that looks
 * like a bearer token or API key before it reaches Logcat.
 */
@Singleton
class AndroidBettyLogger @Inject constructor() : BettyLogger {

    private val prefix = "Betty"

    override fun debug(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$prefix/$tag", message)
        }
    }

    override fun info(tag: String, message: String) {
        Log.i("$prefix/$tag", scrub(message))
    }

    override fun warning(tag: String, message: String, throwable: Throwable?) {
        Log.w("$prefix/$tag", scrub(message), throwable)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e("$prefix/$tag", scrub(message), throwable)
    }

    /** Removes anything that looks like a secret from a log line. */
    private fun scrub(message: String): String {
        if (!BuildConfig.DEBUG) {
            return SECRET_PATTERN.replace(message) { m -> "${m.groupValues[1]}=***" }
        }
        return message
    }

    private companion object {
        val SECRET_PATTERN = Regex("(?i)(api[_-]?key|authorization|bearer|token|password)\\s*[=:]\\s*\\S+")
    }
}
