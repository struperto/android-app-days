package com.struperto.androidappdays.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface CalendarSignalRepository {
    fun observeToday(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<CalendarSignal>>
}

class DeviceCalendarSignalRepository(
    private val context: Context,
) : CalendarSignalRepository {
    override fun observeToday(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<CalendarSignal>> = flow {
        emit(
            querySignalsForDate(
                date = date,
                zoneId = zoneId,
            ),
        )
    }

    private fun querySignalsForDate(
        date: LocalDate,
        zoneId: ZoneId,
    ): List<CalendarSignal> {
        if (!hasCalendarPermission(context)) return emptyList()

        val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(startMillis.toString())
            .appendPath(endMillis.toString())
            .build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
        )

        return runCatching {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val allDayIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            CalendarSignal(
                                id = cursor.getLong(idIndex),
                                title = cursor.getString(titleIndex).orEmpty().ifBlank { "Kalender" },
                                startMillis = cursor.getLong(beginIndex),
                                endMillis = cursor.getLong(endIndex),
                                isAllDay = cursor.getInt(allDayIndex) == 1,
                            ),
                        )
                    }
                }
            }.orEmpty()
        }.getOrElse {
            emptyList()
        }
    }
}

private fun hasCalendarPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CALENDAR,
    ) == PackageManager.PERMISSION_GRANTED
}
