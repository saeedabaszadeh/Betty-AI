package com.bettyai.assistant.permissions

import android.Manifest
import android.os.Build

/**
 * The set of permissions Betty knows about. Only the minimum required for the
 * core engine is requested at runtime; everything else is declared for future
 * skills and requested lazily when a skill actually needs it.
 */
enum class BettyPermission(
    val androidPermission: String,
    val titleFa: String,
    val descriptionFa: String,
    val runtime: Boolean,
    val minSdk: Int = 0
) {
    MICROPHONE(
        androidPermission = Manifest.permission.RECORD_AUDIO,
        titleFa = "میکروفون",
        descriptionFa = "برای شنیدن صدای شما و تبدیل گفتار به متن",
        runtime = true
    ),
    NOTIFICATIONS(
        androidPermission = Manifest.permission.POST_NOTIFICATIONS,
        titleFa = "اعلان‌ها",
        descriptionFa = "برای نمایش وضعیت بتی و پیام‌های مهم",
        runtime = true,
        minSdk = Build.VERSION_CODES.TIRAMISU
    ),
    INTERNET(
        androidPermission = Manifest.permission.INTERNET,
        titleFa = "اینترنت",
        descriptionFa = "برای اتصال به سرویس هوش مصنوعی",
        runtime = false
    ),
    NETWORK_STATE(
        androidPermission = Manifest.permission.ACCESS_NETWORK_STATE,
        titleFa = "وضعیت شبکه",
        descriptionFa = "برای تشخیص حالت آنلاین/آفلاین",
        runtime = false
    );

    /** True when this permission is relevant on the current Android version. */
    fun appliesOnThisDevice(): Boolean = Build.VERSION.SDK_INT >= minSdk

    companion object {
        /** Permissions the core engine needs (runtime ones must be requested). */
        fun coreRuntimePermissions(): List<BettyPermission> =
            entries.filter { it.runtime && it.appliesOnThisDevice() }
    }
}

/** Result of checking a single permission. */
enum class PermissionState { GRANTED, DENIED, NOT_REQUIRED }

data class PermissionStatus(
    val permission: BettyPermission,
    val state: PermissionState
)
