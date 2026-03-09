package com.struperto.androidappdays.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.struperto.androidappdays.DaysApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DaysNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val title = sbn.notification.extras?.getCharSequence("android.title")?.toString().orEmpty()
        val text = sbn.notification.extras?.getCharSequence("android.text")?.toString().orEmpty()
        val body = when {
            title.isBlank() && text.isBlank() -> sbn.packageName
            title.isBlank() -> text
            text.isBlank() -> title
            else -> "$title  $text"
        }
        serviceScope.launch {
            (application as? DaysApp)?.appContainer?.notificationSignalRepository?.upsert(
                id = sbn.key,
                packageName = sbn.packageName,
                title = title.ifBlank { sbn.packageName },
                text = body,
                postedAt = sbn.postTime,
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        serviceScope.launch {
            (application as? DaysApp)?.appContainer?.notificationSignalRepository?.markRemoved(
                id = sbn.key,
                removedAt = System.currentTimeMillis(),
            )
        }
    }
}
