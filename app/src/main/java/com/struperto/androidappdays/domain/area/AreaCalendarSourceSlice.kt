package com.struperto.androidappdays.domain.area

import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceKind
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AreaSourceSetupStatus {
    UNCONFIGURED,
    PERMISSION_REQUIRED,
    READY,
    NO_RECENT_OR_TODAY_DATA,
}

data class AreaCalendarSourceSlice(
    val status: AreaSourceSetupStatus,
    val signals: List<CalendarSignal>,
)

fun resolvePreferredAreaSourceKind(
    title: String,
    summary: String,
    iconKey: String,
    templateId: String?,
    behaviorClass: AreaBehaviorClass,
    boundSources: Set<DataSourceKind>,
): DataSourceKind? {
    listOf(
        DataSourceKind.CALENDAR,
        DataSourceKind.NOTIFICATIONS,
        DataSourceKind.HEALTH_CONNECT,
    ).firstOrNull(boundSources::contains)?.let { return it }

    val lower = buildString {
        append(title)
        append(' ')
        append(summary)
        append(' ')
        append(iconKey)
        append(' ')
        append(templateId.orEmpty())
        append(' ')
        append(behaviorClass.persistedValue)
    }.lowercase()

    return when {
        containsAny(lower, "kalender", "termin", "meeting", "besprech") -> DataSourceKind.CALENDAR
        containsAny(lower, "benachr", "nachricht", "notification", "kontakt", "absender") -> DataSourceKind.NOTIFICATIONS
        containsAny(lower, "health", "gesund", "schlaf", "sleep", "steps", "beweg", "koerper", "energie") -> {
            DataSourceKind.HEALTH_CONNECT
        }
        templateId == "person" -> DataSourceKind.NOTIFICATIONS
        templateId == "project" && behaviorClass == AreaBehaviorClass.PROGRESS && containsAny(lower, "heute", "tag", "plan") -> {
            DataSourceKind.CALENDAR
        }
        else -> null
    }
}

fun resolveCalendarAreaSlice(
    title: String,
    summary: String,
    iconKey: String,
    templateId: String?,
    behaviorClass: AreaBehaviorClass,
    boundSources: Set<DataSourceKind>,
    capabilityProfile: CapabilityProfile,
    calendarSignals: List<CalendarSignal>,
): AreaCalendarSourceSlice? {
    val preferredSourceKind = resolvePreferredAreaSourceKind(
        title = title,
        summary = summary,
        iconKey = iconKey,
        templateId = templateId,
        behaviorClass = behaviorClass,
        boundSources = boundSources,
    )
    if (preferredSourceKind != DataSourceKind.CALENDAR) return null

    return when {
        DataSourceKind.CALENDAR !in boundSources -> AreaCalendarSourceSlice(
            status = AreaSourceSetupStatus.UNCONFIGURED,
            signals = emptyList(),
        )

        !capabilityProfile.isUsable(DataSourceKind.CALENDAR) -> AreaCalendarSourceSlice(
            status = AreaSourceSetupStatus.PERMISSION_REQUIRED,
            signals = emptyList(),
        )

        calendarSignals.isEmpty() -> AreaCalendarSourceSlice(
            status = AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA,
            signals = emptyList(),
        )

        else -> AreaCalendarSourceSlice(
            status = AreaSourceSetupStatus.READY,
            signals = calendarSignals.sortedBy(CalendarSignal::startMillis),
        )
    }
}

fun projectCalendarAreaTodayOutput(
    baseOutput: AreaTodayOutput,
    areaTitle: String,
    slice: AreaCalendarSourceSlice,
    generatedAt: Instant,
    zoneId: ZoneId,
): AreaTodayOutput {
    return when (slice.status) {
        AreaSourceSetupStatus.UNCONFIGURED -> buildCalendarSetupOutput(
            baseOutput = baseOutput,
            headline = "$areaTitle: Kalender verbinden",
            statusLabel = "Einrichtung offen",
            recommendation = "Erst nach einer lokalen Kalenderbindung kann dieser Bereich echte Termine lesen.",
            stepLabel = "Kalender fuer diesen Bereich verbinden",
            evidenceSummary = "Noch keine Kalenderbindung fuer diesen Bereich gespeichert.",
            generatedAt = generatedAt,
            confidence = 0.14f,
            severity = setupSeverity(baseOutput.behaviorClass),
            usabilitySignal = AreaUsabilitySignal.EMPTY,
            stepStatus = AreaStepStatus.READY,
        )

        AreaSourceSetupStatus.PERMISSION_REQUIRED -> buildCalendarSetupOutput(
            baseOutput = baseOutput,
            headline = "$areaTitle: Kalenderfreigabe fehlt",
            statusLabel = "Freigabe offen",
            recommendation = "Die Kalenderbindung ist gespeichert, aber der Kalender ist lokal noch nicht freigegeben.",
            stepLabel = "Kalenderfreigabe in Einstellungen erteilen",
            evidenceSummary = "Kalenderbindung vorhanden, aber READ_CALENDAR fehlt oder ist global deaktiviert.",
            generatedAt = generatedAt,
            confidence = 0.18f,
            severity = AreaSeverity.MEDIUM,
            usabilitySignal = AreaUsabilitySignal.WEAK,
            stepStatus = AreaStepStatus.BLOCKED,
        )

        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> {
            if (baseOutput.sourceTruth == AreaSourceTruth.manual || baseOutput.sourceTruth == AreaSourceTruth.manual_plus_local) {
                baseOutput
            } else {
                val contract = defaultEmptyStateContract(baseOutput.behaviorClass)
                baseOutput.copy(
                    headline = "$areaTitle: Heute frei",
                    statusLabel = "Heute frei",
                    recommendation = "Heute liegt kein relevanter Termin im lokalen Kalender.",
                    nextMeaningfulStep = AreaNextMeaningfulStep(
                        kind = contract.fallbackStepKind,
                        label = calendarNoDataStepLabel(baseOutput.behaviorClass),
                        status = AreaStepStatus.EMPTY,
                        origin = AreaStepOrigin.projected_empty_state,
                        isUserConfirmed = false,
                        fallbackLabel = contract.fallbackStepLabel,
                    ),
                    evidenceSummary = "Kalender verbunden. Fuer heute wurde kein Termin gefunden.",
                    sourceTruth = AreaSourceTruth.local_derived,
                    confidence = 0.58f,
                    freshnessAt = generatedAt,
                    freshnessBand = AreaFreshnessBand.FRESH,
                    severity = AreaSeverity.LOW,
                    singleDockKind = contract.fallbackDockKind,
                    isEmptyState = true,
                    usabilitySignal = AreaUsabilitySignal.WEAK,
                )
            }
        }

        AreaSourceSetupStatus.READY -> {
            if (baseOutput.sourceTruth == AreaSourceTruth.manual || baseOutput.sourceTruth == AreaSourceTruth.manual_plus_local) {
                baseOutput
            } else {
                buildCalendarReadyOutput(
                    baseOutput = baseOutput,
                    areaTitle = areaTitle,
                    signals = slice.signals,
                    generatedAt = generatedAt,
                    zoneId = zoneId,
                )
            }
        }
    }
}

private fun buildCalendarSetupOutput(
    baseOutput: AreaTodayOutput,
    headline: String,
    statusLabel: String,
    recommendation: String,
    stepLabel: String,
    evidenceSummary: String,
    generatedAt: Instant,
    confidence: Float,
    severity: AreaSeverity,
    usabilitySignal: AreaUsabilitySignal,
    stepStatus: AreaStepStatus,
): AreaTodayOutput {
    val contract = defaultEmptyStateContract(baseOutput.behaviorClass)
    return baseOutput.copy(
        headline = headline,
        statusLabel = statusLabel,
        recommendation = recommendation,
        nextMeaningfulStep = AreaNextMeaningfulStep(
            kind = contract.fallbackStepKind,
            label = stepLabel,
            status = stepStatus,
            origin = AreaStepOrigin.projected_empty_state,
            isUserConfirmed = false,
            fallbackLabel = contract.fallbackStepLabel,
        ),
        evidenceSummary = evidenceSummary,
        sourceTruth = AreaSourceTruth.missing,
        confidence = confidence,
        freshnessAt = null,
        freshnessBand = AreaFreshnessBand.UNKNOWN,
        severity = severity,
        singleDockKind = contract.fallbackDockKind,
        isEmptyState = true,
        usabilitySignal = usabilitySignal,
        generatedAt = generatedAt,
        instanceId = baseOutput.instanceId,
        date = baseOutput.date,
        behaviorClass = baseOutput.behaviorClass,
    )
}

private fun buildCalendarReadyOutput(
    baseOutput: AreaTodayOutput,
    areaTitle: String,
    signals: List<CalendarSignal>,
    generatedAt: Instant,
    zoneId: ZoneId,
): AreaTodayOutput {
    val relevantSignal = resolveRelevantCalendarSignal(
        signals = signals,
        nowMillis = generatedAt.toEpochMilli(),
    ) ?: return baseOutput
    val statusLabel = calendarStatusLabel(signals)
    val timeLabel = calendarSignalTimeLabel(
        signal = relevantSignal.signal,
        zoneId = zoneId,
    )
    val headline = buildCalendarHeadline(
        areaTitle = areaTitle,
        relevantSignal = relevantSignal,
        timeLabel = timeLabel,
    )
    val recommendation = buildCalendarRecommendation(relevantSignal)
    val stepLabel = buildCalendarStepLabel(
        relevantSignal = relevantSignal,
        timeLabel = timeLabel,
    )
    val freshnessAt = Instant.ofEpochMilli(
        when (relevantSignal.phase) {
            CalendarSignalPhase.PAST -> relevantSignal.signal.endMillis
            CalendarSignalPhase.ONGOING,
            CalendarSignalPhase.UPCOMING,
            -> relevantSignal.signal.startMillis
        },
    )
    return baseOutput.copy(
        headline = headline,
        statusLabel = statusLabel,
        recommendation = recommendation,
        nextMeaningfulStep = AreaNextMeaningfulStep(
            kind = defaultEmptyStateContract(baseOutput.behaviorClass).fallbackStepKind,
            label = stepLabel,
            status = when (relevantSignal.phase) {
                CalendarSignalPhase.PAST -> AreaStepStatus.STALE
                CalendarSignalPhase.ONGOING,
                CalendarSignalPhase.UPCOMING,
                -> AreaStepStatus.READY
            },
            origin = AreaStepOrigin.projected_from_signal,
            isUserConfirmed = false,
            fallbackLabel = defaultEmptyStateContract(baseOutput.behaviorClass).fallbackStepLabel,
            dueHint = timeLabel.takeIf { !relevantSignal.signal.isAllDay && relevantSignal.phase != CalendarSignalPhase.PAST },
            linkedSourceId = "calendar:${relevantSignal.signal.id}",
        ),
        evidenceSummary = buildCalendarEvidenceSummary(
            signals = signals,
            relevantSignal = relevantSignal,
            timeLabel = timeLabel,
        ),
        sourceTruth = AreaSourceTruth.local_derived,
        confidence = when (relevantSignal.phase) {
            CalendarSignalPhase.ONGOING,
            CalendarSignalPhase.UPCOMING,
            -> 0.86f
            CalendarSignalPhase.PAST -> 0.74f
        },
        freshnessAt = freshnessAt,
        freshnessBand = freshnessBandFor(generatedAt, freshnessAt),
        severity = calendarSeverity(
            behaviorClass = baseOutput.behaviorClass,
            relevantSignal = relevantSignal,
            generatedAt = generatedAt,
        ),
        singleDockKind = calendarDockKind(baseOutput.behaviorClass),
        isEmptyState = false,
        usabilitySignal = when (relevantSignal.phase) {
            CalendarSignalPhase.ONGOING -> AreaUsabilitySignal.STRONG
            CalendarSignalPhase.UPCOMING -> AreaUsabilitySignal.USEFUL
            CalendarSignalPhase.PAST -> AreaUsabilitySignal.WEAK
        },
    )
}

private enum class CalendarSignalPhase {
    ONGOING,
    UPCOMING,
    PAST,
}

private data class RelevantCalendarSignal(
    val signal: CalendarSignal,
    val phase: CalendarSignalPhase,
)

private fun resolveRelevantCalendarSignal(
    signals: List<CalendarSignal>,
    nowMillis: Long,
): RelevantCalendarSignal? {
    val sortedSignals = signals.sortedBy(CalendarSignal::startMillis)
    sortedSignals.firstOrNull { signal ->
        signal.startMillis <= nowMillis && signal.endMillis > nowMillis
    }?.let { signal ->
        return RelevantCalendarSignal(signal = signal, phase = CalendarSignalPhase.ONGOING)
    }
    sortedSignals.firstOrNull { signal ->
        signal.startMillis >= nowMillis
    }?.let { signal ->
        return RelevantCalendarSignal(signal = signal, phase = CalendarSignalPhase.UPCOMING)
    }
    return sortedSignals.maxByOrNull(CalendarSignal::endMillis)?.let { signal ->
        RelevantCalendarSignal(signal = signal, phase = CalendarSignalPhase.PAST)
    }
}

private fun buildCalendarHeadline(
    areaTitle: String,
    relevantSignal: RelevantCalendarSignal,
    timeLabel: String,
): String {
    val title = relevantSignal.signal.title.take(44)
    return when (relevantSignal.phase) {
        CalendarSignalPhase.ONGOING -> {
            if (relevantSignal.signal.isAllDay) "$areaTitle: Heute $title" else "$areaTitle: Jetzt $title"
        }
        CalendarSignalPhase.UPCOMING -> {
            if (relevantSignal.signal.isAllDay) "$areaTitle: Heute $title" else "$areaTitle: $timeLabel $title"
        }
        CalendarSignalPhase.PAST -> "$areaTitle: Heute $title"
    }
}

private fun buildCalendarRecommendation(
    relevantSignal: RelevantCalendarSignal,
): String {
    val title = relevantSignal.signal.title.take(48)
    return when (relevantSignal.phase) {
        CalendarSignalPhase.ONGOING -> "Der aktuelle Kalendereintrag $title gibt diesem Bereich heute den realen Takt."
        CalendarSignalPhase.UPCOMING -> "Der naechste reale Takt fuer diesen Bereich kommt heute aus $title."
        CalendarSignalPhase.PAST -> "Die letzte reale Spur heute kam aus $title."
    }
}

private fun buildCalendarStepLabel(
    relevantSignal: RelevantCalendarSignal,
    timeLabel: String,
): String {
    val title = relevantSignal.signal.title.take(40)
    return when (relevantSignal.phase) {
        CalendarSignalPhase.ONGOING -> {
            if (relevantSignal.signal.isAllDay) "Heute: $title" else "Jetzt: $title"
        }
        CalendarSignalPhase.UPCOMING -> {
            if (relevantSignal.signal.isAllDay) "Ganztag: $title" else "$timeLabel · $title"
        }
        CalendarSignalPhase.PAST -> "Nachklang von $title kurz einordnen"
    }
}

private fun buildCalendarEvidenceSummary(
    signals: List<CalendarSignal>,
    relevantSignal: RelevantCalendarSignal,
    timeLabel: String,
): String {
    val countLabel = calendarStatusLabel(signals)
    val lead = when (relevantSignal.phase) {
        CalendarSignalPhase.ONGOING -> "Laufender Termin"
        CalendarSignalPhase.UPCOMING -> "Naechster Termin"
        CalendarSignalPhase.PAST -> "Letzte Spur heute"
    }
    val timing = if (relevantSignal.signal.isAllDay) "Ganztag" else timeLabel
    return "Kalender verbunden. $countLabel. $lead: $timing · ${relevantSignal.signal.title.take(48)}."
}

private fun calendarSignalTimeLabel(
    signal: CalendarSignal,
    zoneId: ZoneId,
): String {
    if (signal.isAllDay) return "Ganztag"
    return Instant.ofEpochMilli(signal.startMillis)
        .atZone(zoneId)
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
}

private fun calendarSeverity(
    behaviorClass: AreaBehaviorClass,
    relevantSignal: RelevantCalendarSignal,
    generatedAt: Instant,
): AreaSeverity {
    if (relevantSignal.phase == CalendarSignalPhase.PAST) return AreaSeverity.LOW
    if (relevantSignal.phase == CalendarSignalPhase.ONGOING) {
        return when (behaviorClass) {
            AreaBehaviorClass.PROGRESS,
            AreaBehaviorClass.PROTECTION,
            -> AreaSeverity.HIGH
            AreaBehaviorClass.MAINTENANCE,
            AreaBehaviorClass.TRACKING,
            -> AreaSeverity.MEDIUM
            AreaBehaviorClass.RELATIONSHIP,
            AreaBehaviorClass.REFLECTION,
            -> AreaSeverity.LOW
        }
    }

    val minutesUntilStart = (relevantSignal.signal.startMillis - generatedAt.toEpochMilli()) / 60_000L
    return when {
        minutesUntilStart <= 90L -> when (behaviorClass) {
            AreaBehaviorClass.PROGRESS,
            AreaBehaviorClass.PROTECTION,
            -> AreaSeverity.HIGH
            else -> AreaSeverity.MEDIUM
        }
        minutesUntilStart <= 240L -> AreaSeverity.MEDIUM
        else -> AreaSeverity.LOW
    }
}

private fun calendarNoDataStepLabel(
    behaviorClass: AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> "Kalender spaeter erneut lesen"
        AreaBehaviorClass.PROGRESS -> "Falls noetig einen Tageszug manuell setzen"
        AreaBehaviorClass.RELATIONSHIP -> "Falls noetig einen Kontaktimpuls manuell setzen"
        AreaBehaviorClass.MAINTENANCE -> "Falls noetig einen Pflegeanker manuell setzen"
        AreaBehaviorClass.PROTECTION -> "Falls noetig einen Schutzanker manuell setzen"
        AreaBehaviorClass.REFLECTION -> "Falls noetig einen Lesepunkt manuell setzen"
    }
}

private fun calendarDockKind(
    behaviorClass: AreaBehaviorClass,
): AreaTodayDockKind {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> AreaTodayDockKind.STATUS
        AreaBehaviorClass.PROGRESS -> AreaTodayDockKind.ACTION
        AreaBehaviorClass.RELATIONSHIP -> AreaTodayDockKind.CARE
        AreaBehaviorClass.MAINTENANCE -> AreaTodayDockKind.ACTION
        AreaBehaviorClass.PROTECTION -> AreaTodayDockKind.SHIELD
        AreaBehaviorClass.REFLECTION -> AreaTodayDockKind.REFLECTION
    }
}

private fun setupSeverity(
    behaviorClass: AreaBehaviorClass,
): AreaSeverity {
    return when (behaviorClass) {
        AreaBehaviorClass.PROGRESS,
        AreaBehaviorClass.PROTECTION,
        -> AreaSeverity.MEDIUM
        else -> AreaSeverity.LOW
    }
}

private fun calendarStatusLabel(
    calendarSignals: List<CalendarSignal>,
): String {
    return when (calendarSignals.size) {
        0 -> "Heute frei"
        1 -> "1 Termin heute"
        else -> "${calendarSignals.size} Termine heute"
    }
}

private fun containsAny(
    value: String,
    vararg keywords: String,
): Boolean {
    return keywords.any(value::contains)
}
