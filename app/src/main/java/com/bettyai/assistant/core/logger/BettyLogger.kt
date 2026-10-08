package com.bettyai.assistant.core.logger

/** Severity levels for Betty's systematic logging. */
enum class LogLevel { DEBUG, INFO, WARNING, ERROR }

/**
 * Abstraction over logging so the rest of the codebase never depends on a
 * concrete logging framework. Implementations must never log secrets.
 */
interface BettyLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
