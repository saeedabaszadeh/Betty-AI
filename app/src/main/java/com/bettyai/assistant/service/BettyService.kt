package com.bettyai.assistant.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.bettyai.assistant.core.BettyCore
import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.notifications.BettyNotificationManager
import com.bettyai.assistant.permissions.BettyPermission
import com.bettyai.assistant.permissions.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps Betty's core warm and, importantly, *visible*.
 *
 * Android forbids silent, always-on microphone access; this service therefore
 * always shows a transparent, ongoing notification while it runs. Wake-word
 * detection is intentionally left to [com.bettyai.assistant.voice.wakeword.WakeWordEngine],
 * which reports whether the platform supports it.
 */
@AndroidEntryPoint
class BettyService : Service() {

    @Inject lateinit var bettyCore: BettyCore
    @Inject lateinit var notificationManager: BettyNotificationManager
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var logger: BettyLogger

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        notificationManager.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundSafely()
        scope.launch {
            runCatching { bettyCore.initialize() }
                .onFailure { logger.error(TAG, "Service init failed", it) }
        }
        return START_STICKY
    }

    @Suppress("DEPRECATION") // FOREGROUND_SERVICE_TYPE_NONE has no non-deprecated replacement.
    private fun startForegroundSafely() {
        val notification = notificationManager.buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (permissionManager.isGranted(BettyPermission.MICROPHONE)) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            }
            startForeground(BettyNotificationManager.FOREGROUND_NOTIFICATION_ID, notification, type)
        } else {
            startForeground(BettyNotificationManager.FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "BettyService"

        fun start(context: Context) {
            val intent = Intent(context, BettyService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BettyService::class.java))
        }
    }
}
