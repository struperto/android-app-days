package com.struperto.androidappdays.testing

import android.app.Instrumentation
import android.service.notification.NotificationListenerService
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal fun instrumentation(): Instrumentation {
    return InstrumentationRegistry.getInstrumentation()
}

internal fun targetApp(): DaysApp {
    return instrumentation().targetContext.applicationContext as DaysApp
}

internal fun shell(command: String): String {
    val descriptor = instrumentation().uiAutomation.executeShellCommand(command)
    return BufferedReader(InputStreamReader(android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor))).use {
        it.readText()
    }
}

internal fun grantRuntimePermission(
    packageName: String,
    permission: String,
) {
    shell("pm grant $packageName $permission")
}

internal fun revokeRuntimePermission(
    packageName: String,
    permission: String,
) {
    shell("pm revoke $packageName $permission")
}

internal fun ensureNotificationListenerEnabled(componentName: String) {
    val current = shell("settings get secure enabled_notification_listeners").trim()
    if (current.split(':').filter { it.isNotBlank() }.contains(componentName)) return
    val merged = buildString {
        if (current.isNotBlank() && current != "null") {
            append(current)
            append(':')
        }
        append(componentName)
    }
    shell("settings put secure enabled_notification_listeners $merged")
    NotificationListenerService.requestRebind(android.content.ComponentName.unflattenFromString(componentName)!!)
}

internal fun waitUntil(
    timeout: Duration = 10_000.milliseconds,
    pollEvery: Duration = 200.milliseconds,
    predicate: () -> Boolean,
) {
    val deadline = System.nanoTime() + timeout.inWholeNanoseconds
    while (System.nanoTime() < deadline) {
        if (predicate()) return
        Thread.sleep(pollEvery.inWholeMilliseconds)
    }
    check(predicate()) { "Condition was not met within $timeout" }
}
