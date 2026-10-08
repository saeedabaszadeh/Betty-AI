package com.bettyai.assistant.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central authority for permission checks. The actual *request* is performed by
 * the UI (via ActivityResultContracts) so that permissions are only ever asked
 * for at the moment they are needed.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun isGranted(permission: BettyPermission): Boolean =
        !permission.appliesOnThisDevice() || isGranted(permission.androidPermission)

    fun hasMicrophone(): Boolean = isGranted(BettyPermission.MICROPHONE)

    fun status(permission: BettyPermission): PermissionStatus = when {
        !permission.appliesOnThisDevice() -> PermissionStatus(permission, PermissionState.NOT_REQUIRED)
        isGranted(permission) -> PermissionStatus(permission, PermissionState.GRANTED)
        else -> PermissionStatus(permission, PermissionState.DENIED)
    }

    fun allStatuses(): List<PermissionStatus> =
        BettyPermission.entries.map { status(it) }

    /** Runtime permissions that still need to be granted. */
    fun missingCoreRuntimePermissions(): List<BettyPermission> =
        BettyPermission.coreRuntimePermissions().filterNot { isGranted(it) }
}
