package com.bettyai.assistant.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bettyai.assistant.MainActivity
import com.bettyai.assistant.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns notification channels and builds the transparent foreground-service
 * notification. Betty never runs silently in the background: if the service is
 * active, a visible notification is always shown.
 */
@Singleton
class BettyNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            context.getString(R.string.service_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.service_channel_description)
            setShowBadge(false)
        }

        val messagesChannel = NotificationChannel(
            BETTY_CHANNEL_ID,
            context.getString(R.string.betty_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.betty_channel_description)
        }

        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(messagesChannel)
    }

    /** Notification used while the foreground service is running. */
    fun buildForegroundNotification(text: String = context.getString(R.string.service_notification_text)): Notification {
        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.service_notification_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_betty)
            .setContentIntent(openAppIntent())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /** Post a regular notification (used by future skills). */
    fun notify(id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, BETTY_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_betty)
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    companion object {
        const val SERVICE_CHANNEL_ID = "betty_service"
        const val BETTY_CHANNEL_ID = "betty_messages"
        const val FOREGROUND_NOTIFICATION_ID = 1001
    }
}
