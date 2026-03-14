package com.struperto.androidappdays.data.repository

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.R
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.testing.ensureNotificationListenerEnabled
import com.struperto.androidappdays.testing.grantRuntimePermission
import com.struperto.androidappdays.testing.targetApp
import com.struperto.androidappdays.testing.waitUntil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationListenerDeviceTest {
    private val channelId = "days-device-test"
    private val notificationId = 4407

    @Before
    fun setup() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        grantRuntimePermission(targetContext.packageName, Manifest.permission.POST_NOTIFICATIONS)
        ensureNotificationListenerEnabled(
            "com.struperto.androidappdays/com.struperto.androidappdays.notifications.DaysNotificationListenerService",
        )
        targetApp().appContainer.notificationSignalRepository.clearAll()
        createChannel(targetContext)
    }

    @After
    fun tearDown() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        NotificationManagerCompat.from(targetContext).cancel(notificationId)
        targetContext.getSystemService(NotificationManager::class.java)?.deleteNotificationChannel(channelId)
    }

    @Test
    fun emulator_canPostNotifications_andDetectListenerAccess() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationManager = targetContext.getSystemService(NotificationManager::class.java)
        val capabilityProfile = targetApp().appContainer.sourceCapabilityRepository.loadProfile()

        NotificationManagerCompat.from(targetContext).notify(
            notificationId,
            NotificationCompat.Builder(targetContext, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Device Test")
                .setContentText("Listener should persist this notification.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .build(),
        )

        waitUntil {
            notificationManager.activeNotifications.any { record ->
                record.id == notificationId && record.notification.extras.getString(Notification.EXTRA_TITLE) == "Device Test"
            }
        }

        assertTrue(capabilityProfile.find(DataSourceKind.NOTIFICATIONS)?.granted == true)

        NotificationManagerCompat.from(targetContext).cancel(notificationId)

        waitUntil {
            notificationManager.activeNotifications.none { record ->
                record.id == notificationId
            }
        }
        assertEquals(
            0,
            notificationManager.activeNotifications.count { record -> record.id == notificationId },
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
