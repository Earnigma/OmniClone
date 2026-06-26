package com.omniclone.service

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.omniclone.model.FeatureKey

/**
 * Notification listener implementing all Module I features.
 */
class OmniNotificationListener : NotificationListenerService() {

    private val prefs by lazy { getSharedPreferences("omniclone_notifications", Context.MODE_PRIVATE) }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!isEnabled()) return

        val clonePackage = sbn.packageName
        if (!clonePackage.startsWith("com.omniclone.")) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (shouldSilenceAll()) {
            cancelNotification(sbn.key)
            return
        }

        if (shouldFilterKeyword(title, text)) {
            cancelNotification(sbn.key)
            return
        }

        if (isQuietTime()) {
            cancelNotification(sbn.key)
            return
        }

        if (shouldSecretMode()) {
            val secretNotification = Notification.Builder(this, sbn.notification.channelId ?: "default")
                .setSmallIcon(sbn.notification.smallIcon)
                .setContentTitle("••••")
                .setContentText("••••")
                .setGroup(sbn.notification.group)
                .build()
            cancelNotification(sbn.key)
            postNotification(sbn.id, secretNotification)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No-op
    }

    private fun isEnabled(): Boolean {
        return prefs.getBoolean("enabled", true)
    }

    private fun shouldSilenceAll(): Boolean {
        return prefs.getBoolean(FeatureKey.SILENCE_ALL_NOTIFICATIONS.name, false)
    }

    private fun shouldFilterKeyword(title: String, text: String): Boolean {
        val keywords = prefs.getStringSet(FeatureKey.FILTER_NOTIFICATIONS_KEYWORD.name, emptySet()) ?: return false
        return keywords.any { title.contains(it, ignoreCase = true) || text.contains(it, ignoreCase = true) }
    }

    private fun isQuietTime(): Boolean {
        return prefs.getBoolean(FeatureKey.QUIET_TIME_SCHEDULE.name, false)
    }

    private fun shouldSecretMode(): Boolean {
        return prefs.getBoolean(FeatureKey.NOTIFICATION_SECRET_MODE.name, false) ||
                prefs.getBoolean(FeatureKey.NOTIFICATION_SECRET.name, false)
    }

    private fun postNotification(id: Int, notification: Notification) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(id, notification)
    }
}
