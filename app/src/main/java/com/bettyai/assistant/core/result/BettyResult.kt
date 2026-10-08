package com.bettyai.assistant.core.result

/**
 * A lightweight result wrapper used across the domain and data layers so that
 * failures are explicit and never crash the caller.
 */
sealed interface BettyResult<out T> {

    data class Success<T>(val data: T) : BettyResult<T>
    data class Failure(val error: BettyError) : BettyResult<Nothing>

    val isSuccess: Boolean get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): BettyError? = (this as? Failure)?.error
}

/** Map the success value while preserving failures. */
inline fun <T, R> BettyResult<T>.map(transform: (T) -> R): BettyResult<R> = when (this) {
    is BettyResult.Success -> BettyResult.Success(transform(data))
    is BettyResult.Failure -> this
}

/** Run [block] on success. */
inline fun <T> BettyResult<T>.onSuccess(block: (T) -> Unit): BettyResult<T> {
    if (this is BettyResult.Success) block(data)
    return this
}

/** Run [block] on failure. */
inline fun <T> BettyResult<T>.onFailure(block: (BettyError) -> Unit): BettyResult<T> {
    if (this is BettyResult.Failure) block(error)
    return this
}
