package com.example.spamfilter

import android.app.Notification
import android.os.Process
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageNotificationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val messagesPackage = "com.google.android.apps.messaging"

    private fun builder() = NotificationCompat.Builder(context, "messages")
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setCategory(Notification.CATEGORY_MESSAGE)

    @Suppress("DEPRECATION")
    private fun preview(notification: Notification, pkg: String = messagesPackage): String? {
        val sbn = StatusBarNotification(pkg, pkg, 1, null, 1000, 0, 0, notification,
            Process.myUserHandle(), 0L)
        return messagePreview(sbn)?.toString()
    }

    @Test fun usesNewestMessageBodyAndSkipsOwnReply() {
        val me = Person.Builder().setName("Me").build()
        val sender = Person.Builder().setName("SALE").build()
        val style = NotificationCompat.MessagingStyle(me)
            .addMessage("flash SALE", 1, sender)
            .addMessage("see you tomorrow", 2, sender)
        assertEquals("see you tomorrow", preview(builder().setStyle(style).build()))

        style.addMessage("I saw the sale", 3, null as Person?)
        assertNull(preview(builder().setStyle(style).build()))
    }

    @Test fun readsExpandedOrPlainMessagePreview() {
        assertEquals("plain", preview(builder().setContentText("plain").build()))
        val expanded = builder().setContentText("short")
            .setStyle(NotificationCompat.BigTextStyle().bigText("long preview")).build()
        assertEquals("long preview", preview(expanded))
    }

    @Test fun ignoresOtherAppsSummariesOngoingAndNonMessageAlerts() {
        val builder = builder().setContentText("flash SALE")
        assertNull(preview(builder.build(), "example.other"))
        assertNull(preview(builder.setGroup("chats").setGroupSummary(true).build()))
        assertNull(preview(builder.setGroupSummary(false).setOngoing(true).build()))
        assertNull(preview(builder.setOngoing(false).setCategory(Notification.CATEGORY_ERROR).build()))
        assertNull(preview(builder().setContentTitle("flash SALE").build()))
    }
}
