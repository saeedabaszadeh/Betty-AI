package com.bettyai.assistant.core.result

/**
 * Central, typed error hierarchy for Betty.
 *
 * Every subsystem (voice, AI, TTS, storage, permissions, network) reports failures
 * using one of these. Each error carries a machine-readable [code] and a
 * human-friendly, localized [userMessage] that the UI can surface directly.
 *
 * Never include secrets (API keys, tokens) in [message] or [userMessage].
 */
sealed class BettyError(
    val code: String,
    override val message: String,
    val userMessage: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /** Microphone is missing, busy, or otherwise unavailable. */
    class MicrophoneUnavailable(cause: Throwable? = null) : BettyError(
        code = "MIC_UNAVAILABLE",
        message = "Microphone is unavailable",
        userMessage = "به میکروفون دسترسی ندارم. لطفاً دسترسی میکروفون را بررسی کنید.",
        cause = cause
    )

    /** Speech recognition failed or returned nothing usable. */
    class SpeechRecognitionFailed(detail: String = "", cause: Throwable? = null) : BettyError(
        code = "STT_FAILED",
        message = "Speech recognition failed: $detail",
        userMessage = "متأسفم، صحبت شما را متوجه نشدم. لطفاً دوباره تلاش کنید.",
        cause = cause
    )

    /** No network connection. */
    class NetworkUnavailable(cause: Throwable? = null) : BettyError(
        code = "NETWORK_UNAVAILABLE",
        message = "No network connection",
        userMessage = "اتصال اینترنت برقرار نیست. برخی قابلیت‌ها در حالت آفلاین در دسترس نیستند.",
        cause = cause
    )

    /** The configured AI provider could not be reached or returned an error. */
    class AiProviderUnavailable(detail: String = "", cause: Throwable? = null) : BettyError(
        code = "AI_UNAVAILABLE",
        message = "AI provider unavailable: $detail",
        userMessage = "در حال حاضر به دستیار هوش مصنوعی دسترسی ندارم. لطفاً بعداً تلاش کنید.",
        cause = cause
    )

    /** The AI provider is not configured yet (missing URL / key / model). */
    class AiNotConfigured : BettyError(
        code = "AI_NOT_CONFIGURED",
        message = "AI provider is not configured",
        userMessage = "هوش مصنوعی هنوز تنظیم نشده است. لطفاً در تنظیمات، آدرس و کلید سرویس را وارد کنید."
    )

    /** Text-to-speech engine is unavailable. */
    class TtsUnavailable(detail: String = "", cause: Throwable? = null) : BettyError(
        code = "TTS_UNAVAILABLE",
        message = "Text-to-speech unavailable: $detail",
        userMessage = "موتور گفتار در دسترس نیست، اما پاسخ متنی را می‌بینید.",
        cause = cause
    )

    /** A required runtime permission was denied. */
    class PermissionDenied(permission: String) : BettyError(
        code = "PERMISSION_DENIED",
        message = "Permission denied: $permission",
        userMessage = "برای این کار به دسترسی لازم نیاز دارم که هنوز داده نشده است."
    )

    /** A persistence / database operation failed. */
    class StorageError(detail: String = "", cause: Throwable? = null) : BettyError(
        code = "STORAGE_ERROR",
        message = "Storage error: $detail",
        userMessage = "در ذخیره‌سازی اطلاعات خطایی رخ داد.",
        cause = cause
    )

    /** Betty is currently disabled by the user. */
    class Disabled : BettyError(
        code = "DISABLED",
        message = "Betty is disabled",
        userMessage = "بتی در حال حاضر غیرفعال است. برای فعال‌سازی به تنظیمات بروید."
    )

    /** Catch-all for anything unexpected. */
    class Unknown(detail: String = "", cause: Throwable? = null) : BettyError(
        code = "UNKNOWN",
        message = "Unexpected error: $detail",
        userMessage = "خطای غیرمنتظره‌ای رخ داد. لطفاً دوباره تلاش کنید.",
        cause = cause
    )
}
