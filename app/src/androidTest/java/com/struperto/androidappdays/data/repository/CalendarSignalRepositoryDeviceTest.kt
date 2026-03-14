package com.struperto.androidappdays.data.repository

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.testing.grantRuntimePermission
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarSignalRepositoryDeviceTest {
    private val zoneId = ZoneId.systemDefault()
    private val today = LocalDate.now(zoneId)
    private val insertedEventIds = mutableListOf<Long>()
    private var insertedCalendarId: Long? = null

    @Before
    fun setup() {
        val targetPackage = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        grantRuntimePermission(targetPackage, Manifest.permission.READ_CALENDAR)
        grantRuntimePermission(targetPackage, Manifest.permission.WRITE_CALENDAR)
    }

    @After
    fun tearDown() {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        insertedEventIds.forEach { eventId ->
            resolver.delete(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
                null,
                null,
            )
        }
        insertedCalendarId?.let { calendarId ->
            resolver.delete(calendarUri(calendarId), null, null)
        }
    }

    @Test
    fun observeToday_readsEventsInsertedIntoCalendarProvider() = runBlocking {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val calendarId = insertLocalCalendar(resolver)
        insertedCalendarId = calendarId
        val eventId = insertEvent(
            resolver = resolver,
            calendarId = calendarId,
            title = "Architektur Review",
        )
        insertedEventIds += eventId

        val repository = DeviceCalendarSignalRepository(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
        )

        val signals = repository.observeToday(today, zoneId).first()

        assertEquals(1, signals.size)
        assertEquals("Architektur Review", signals.single().title)
        assertTrue(signals.single().startMillis < signals.single().endMillis)
    }

    private fun insertLocalCalendar(resolver: android.content.ContentResolver): Long {
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, "days-local")
            put(CalendarContract.Calendars.ACCOUNT_TYPE, "LOCAL")
            put(CalendarContract.Calendars.NAME, "days-local")
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Days Local")
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF336699.toInt())
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, "days-local")
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        }
        val uri = resolver.insert(calendarUri(), values)
        return checkNotNull(uri) { "Calendar insert failed" }.lastPathSegment!!.toLong()
    }

    private fun insertEvent(
        resolver: android.content.ContentResolver,
        calendarId: Long,
        title: String,
    ): Long {
        val start = today.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()
        val end = today.atTime(11, 0).atZone(zoneId).toInstant().toEpochMilli()
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, start)
            put(CalendarContract.Events.DTEND, end)
            put(CalendarContract.Events.EVENT_TIMEZONE, zoneId.id)
        }
        val uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return checkNotNull(uri) { "Event insert failed" }.lastPathSegment!!.toLong()
    }

    private fun calendarUri(calendarId: Long? = null): android.net.Uri {
        val builder = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "days-local")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "LOCAL")
        return if (calendarId == null) {
            builder.build()
        } else {
            ContentUris.withAppendedId(builder.build(), calendarId)
        }
    }
}
