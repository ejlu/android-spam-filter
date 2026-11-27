package com.example.spamfilter

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class MessageNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val preview = messagePreview(sbn) ?: return
        if (KeywordStore.load(this).any { it.isNotBlank() && preview.contains(it, ignoreCase = true) }) {
            cancelNotification(sbn.key)
        }
    }
}

internal fun messagePreview(sbn: StatusBarNotification): CharSequence? {
    if (sbn.packageName != "com.google.android.apps.messaging" || !sbn.isClearable) return null

    val notification = sbn.notification
    if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return null

    val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
    if (style != null) {
        // A conversation update can include older texts and the user's own replies.
        val latest = style.messages.lastOrNull() ?: return null
        return latest.text.takeIf { latest.person != null }
    }
    if (notification.category != Notification.CATEGORY_MESSAGE) return null

    val extras = notification.extras
    return extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.takeIf { it.isNotBlank() }
        ?: extras.getCharSequence(Notification.EXTRA_TEXT)
}
