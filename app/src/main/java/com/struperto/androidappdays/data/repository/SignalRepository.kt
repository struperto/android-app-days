package com.struperto.androidappdays.data.repository

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface SignalRepository {
    fun observeDay(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<SignalEnvelope>>
}

class LocalSignalRepository(
    private val calendarSignalRepository: CalendarSignalRepository,
    private val notificationSignalRepository: NotificationSignalRepository,
    private val captureRepository: CaptureRepository,
    private val vorhabenRepository: VorhabenRepository,
    private val planRepository: PlanRepository,
) : SignalRepository {
    override fun observeDay(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<SignalEnvelope>> {
        return combine(
            calendarSignalRepository.observeToday(date, zoneId),
            notificationSignalRepository.observeToday(date, zoneId),
            captureRepository.observeOpen(),
            vorhabenRepository.observeActive(),
            planRepository.observeToday(date.toString()),
        ) { calendarSignals, notificationSignals, captures, laterItems, plans ->
            buildList {
                addAll(
                    calendarSignals.map { signal ->
                        SignalEnvelope(
                            id = "calendar:${signal.id}",
                            kind = SignalKind.CALENDAR,
                            sourceLabel = "Kalender",
                            title = signal.title,
                            detail = if (signal.isAllDay) "Ganztagiger Termin" else "Fester Termin",
                            startMillis = signal.startMillis,
                            endMillis = signal.endMillis,
                            intensity = if (signal.isAllDay) 0.74f else 0.62f,
                            areaId = null,
                        )
                    },
                )
                addAll(
                    notificationSignals.map { signal ->
                        SignalEnvelope(
                            id = "notification:${signal.id}",
                            kind = SignalKind.NOTIFICATION,
                            sourceLabel = "Benachrichtigung",
                            title = signal.title.ifBlank { signal.packageName },
                            detail = signal.text,
                            startMillis = signal.postedAt,
                            endMillis = signal.removedAt,
                            intensity = 0.34f,
                            areaId = null,
                        )
                    },
                )
                addAll(
                    captures.map { capture ->
                        SignalEnvelope(
                            id = "capture:${capture.id}",
                            kind = SignalKind.CAPTURE,
                            sourceLabel = "Signal",
                            title = capture.text.lineSequence().firstOrNull().orEmpty().ifBlank { "Offenes Signal" },
                            detail = capture.text,
                            startMillis = capture.createdAt,
                            endMillis = null,
                            intensity = 0.38f,
                            areaId = capture.areaId,
                        )
                    },
                )
                addAll(
                    laterItems.mapIndexed { index, item ->
                        SignalEnvelope(
                            id = "later:${item.id}",
                            kind = SignalKind.LATER,
                            sourceLabel = "Spaeter",
                            title = item.title,
                            detail = item.note,
                            startMillis = date.atTime(18, 0).atZone(zoneId).toInstant().toEpochMilli() + (index * 900_000L),
                            endMillis = null,
                            intensity = 0.28f,
                            areaId = item.areaId,
                        )
                    },
                )
                addAll(
                    plans.mapIndexed { index, item ->
                        val start = derivePlanTime(date, zoneId, item.timeBlock, index)
                        SignalEnvelope(
                            id = "plan:${item.id}",
                            kind = SignalKind.PLAN,
                            sourceLabel = "Plan",
                            title = item.title,
                            detail = item.note,
                            startMillis = start,
                            endMillis = start + 3_600_000L,
                            intensity = if (item.isDone) 0.66f else 0.58f,
                            areaId = item.areaId,
                        )
                    },
                )
            }.sortedBy(SignalEnvelope::startMillis)
        }
    }
}

private fun derivePlanTime(
    date: LocalDate,
    zoneId: ZoneId,
    timeBlock: TimeBlock,
    index: Int,
): Long {
    val baseHour = when (timeBlock) {
        TimeBlock.MORGEN -> 7
        TimeBlock.MITTAG -> 11
        TimeBlock.NACHMITTAG -> 14
        TimeBlock.ABEND -> 19
    }
    val minuteOffset = (index % 3) * 20L
    return date.atTime(LocalTime.of(baseHour, minuteOffset.toInt())).atZone(zoneId).toInstant().toEpochMilli()
}
