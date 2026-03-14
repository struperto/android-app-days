package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaSourceBinding
import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.NotificationSignal
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.area.AreaSourceSetupStatus
import com.struperto.androidappdays.domain.area.projectCalendarAreaTodayOutput
import com.struperto.androidappdays.domain.area.resolveCalendarAreaSlice
import com.struperto.androidappdays.domain.area.resolvePreferredAreaSourceKind
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AreaSourceSetupState(
    val sourceKind: DataSourceKind,
    val status: AreaSourceSetupStatus,
    val headline: String,
    val detail: String,
    val primaryActionLabel: String? = null,
    val secondaryActionLabel: String? = null,
    val isWarning: Boolean = false,
    val canConnectSource: Boolean = false,
    val canDisconnectSource: Boolean = false,
)

fun overlayOverviewWithSources(
    state: StartOverviewState,
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    calendarSignals: List<CalendarSignal>,
    notificationSignals: List<NotificationSignal>,
    healthObservations: List<DomainObservation>,
    zoneId: ZoneId,
): StartOverviewState {
    val boundSourcesByAreaId = bindings.groupBy(AreaSourceBinding::areaId)
    return state.copy(
        areas = state.areas.map { tile ->
            when (val sourceKind = resolvePreferredSourceKind(tile, boundSourcesByAreaId[tile.areaId].orEmpty())) {
                DataSourceKind.CALENDAR -> tile.applyCalendarSignals(
                    bindings = boundSourcesByAreaId[tile.areaId].orEmpty(),
                    calendarSignals = calendarSignals,
                    capabilityProfile = capabilityProfile,
                    zoneId = zoneId,
                )

                DataSourceKind.NOTIFICATIONS -> tile.applyNotificationSignals(
                    bindings = boundSourcesByAreaId[tile.areaId].orEmpty(),
                    notificationSignals = notificationSignals,
                    notificationsUsable = capabilityProfile.isUsable(DataSourceKind.NOTIFICATIONS),
                    zoneId = zoneId,
                )

                DataSourceKind.HEALTH_CONNECT -> tile.applyHealthSignals(
                    bindings = boundSourcesByAreaId[tile.areaId].orEmpty(),
                    capabilityProfile = capabilityProfile,
                    healthObservations = healthObservations,
                )

                else -> tile
            }
        },
    )
}

fun overlayOverviewWithContentSources(
    state: StartOverviewState,
    captures: List<CaptureItem>,
    webFeedSources: List<AreaWebFeedSource>,
): StartOverviewState {
    val importsByAreaId = captures
        .mapNotNull { capture ->
            parseAreaImportCapture(capture)?.let { parsed ->
                capture.areaId?.let { areaId -> areaId to parsed }
            }
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second },
        )
    val feedsByAreaId = webFeedSources.groupBy(AreaWebFeedSource::areaId)
    return state.copy(
        areas = state.areas.map { tile ->
            val shouldTreatAsRadar = tile.family == StartAreaFamily.Radar || tile.templateId == "medium" || tile.templateId == "theme"
            if (!shouldTreatAsRadar) return@map tile
            val importedMaterials = importsByAreaId[tile.areaId].orEmpty()
            val feeds = feedsByAreaId[tile.areaId].orEmpty()
            tile.applyRadarContentSources(
                importedMaterials = importedMaterials,
                feeds = feeds,
            )
        },
    )
}

fun overlayDetailWithSources(
    detail: StartAreaDetailState,
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    calendarSignals: List<CalendarSignal>,
    notificationSignals: List<NotificationSignal>,
    healthObservations: List<DomainObservation>,
    zoneId: ZoneId,
): StartAreaDetailState {
    return when (resolvePreferredSourceKind(detail, bindings)) {
        DataSourceKind.CALENDAR -> detail.applyCalendarSignals(
            bindings = bindings,
            capabilityProfile = capabilityProfile,
            calendarSignals = calendarSignals,
            zoneId = zoneId,
        )

        DataSourceKind.NOTIFICATIONS -> detail.applyNotificationSignals(
            bindings = bindings,
            capabilityProfile = capabilityProfile,
            notificationSignals = notificationSignals,
            zoneId = zoneId,
        )

        DataSourceKind.HEALTH_CONNECT -> detail.applyHealthSignals(
            bindings = bindings,
            capabilityProfile = capabilityProfile,
            healthObservations = healthObservations,
        )

        else -> detail
    }
}

fun buildAreaSourceSetupState(
    detail: StartAreaDetailState,
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    calendarSignals: List<CalendarSignal>,
    notificationSignals: List<NotificationSignal>,
    healthObservations: List<DomainObservation>,
    zoneId: ZoneId,
): AreaSourceSetupState? {
    return when (resolvePreferredSourceKind(detail, bindings)) {
        DataSourceKind.CALENDAR -> buildCalendarSourceSetupState(
            detail = detail,
            bindings = bindings,
            capabilityProfile = capabilityProfile,
            calendarSignals = calendarSignals,
            zoneId = zoneId,
        )

        DataSourceKind.NOTIFICATIONS -> buildNotificationSourceSetupState(
            bindings = bindings,
            capabilityProfile = capabilityProfile,
            notificationSignals = notificationSignals,
            zoneId = zoneId,
        )

        DataSourceKind.HEALTH_CONNECT -> buildHealthSourceSetupState(
            bindings = bindings,
            capabilityProfile = capabilityProfile,
            healthObservations = healthObservations,
        )

        else -> null
    }
}

private fun StartAreaOverviewTile.applyCalendarSignals(
    bindings: List<AreaSourceBinding>,
    calendarSignals: List<CalendarSignal>,
    capabilityProfile: CapabilityProfile,
    zoneId: ZoneId,
): StartAreaOverviewTile {
    val slice = resolveCalendarAreaSlice(
        title = label,
        summary = summary,
        iconKey = iconKey,
        templateId = templateId,
        behaviorClass = todayOutput.behaviorClass,
        boundSources = bindings.toSourceKindSet(),
        capabilityProfile = capabilityProfile,
        calendarSignals = calendarSignals,
    ) ?: return this
    val calendarOutput = projectCalendarAreaTodayOutput(
        baseOutput = todayOutput,
        areaTitle = label,
        slice = slice,
        generatedAt = todayOutput.generatedAt,
        zoneId = zoneId,
    )
    return copy(
        primaryHint = calendarOverviewHint(slice),
        todayLabel = calendarOutput.statusLabel,
        todayStepLabel = calendarOutput.nextMeaningfulStep.label,
        statusKind = calendarStatusKind(slice.status),
        statusLabel = calendarTileStatusLabel(slice.status),
        todayOutput = calendarOutput,
    )
}

private fun StartAreaOverviewTile.applyNotificationSignals(
    bindings: List<AreaSourceBinding>,
    notificationSignals: List<NotificationSignal>,
    notificationsUsable: Boolean,
    zoneId: ZoneId,
): StartAreaOverviewTile {
    val notificationsConnected = bindings.any { it.source == DataSourceKind.NOTIFICATIONS }
    if (!notificationsConnected) {
        return copy(
            primaryHint = StartAreaHintState(
                id = "notifications-not-connected",
                title = "Benachrichtigungen noch nicht verbunden",
                detail = "Verbinde den Listener, damit dieser Bereich wichtige Hinweise lesen kann.",
                compactLabel = "Einrichtung offen",
                tone = StartAreaHintTone.Notice,
            ),
            todayLabel = "Nicht verbunden",
            todayStepLabel = "Listener verbinden",
            statusKind = StartAreaStatusKind.Waiting,
            statusLabel = "Einrichtung offen",
        )
    }
    if (!notificationsUsable) {
        return copy(
            primaryHint = StartAreaHintState(
                id = "notifications-permission",
                title = "Benachrichtigungen brauchen Freigabe",
                detail = "Der Bereich ist verbunden, aber der Listener ist global noch nicht aktiv.",
                compactLabel = "Listener freigeben",
                tone = StartAreaHintTone.Warning,
            ),
            todayLabel = "Listener braucht Freigabe",
            todayStepLabel = "In Einstellungen freigeben",
            statusKind = StartAreaStatusKind.Pull,
            statusLabel = "Freigabe offen",
        )
    }
    return copy(
        primaryHint = StartAreaHintState(
            id = "notifications-connected",
            title = "Benachrichtigungen aktiv",
            detail = "Dieser Bereich liest lokale Benachrichtigungen fuer heute.",
            compactLabel = notificationCompactLabel(notificationSignals),
            tone = StartAreaHintTone.Quiet,
        ),
        todayLabel = notificationStatusLabel(notificationSignals),
        todayStepLabel = notificationNextStepLabel(notificationSignals, zoneId),
        statusKind = if (notificationSignals.isEmpty()) StartAreaStatusKind.Stable else StartAreaStatusKind.Live,
        statusLabel = if (notificationSignals.isEmpty()) "Heute ruhig" else "Benachrichtigungen aktiv",
    )
}

private fun StartAreaOverviewTile.applyRadarContentSources(
    importedMaterials: List<AreaImportedMaterialState>,
    feeds: List<AreaWebFeedSource>,
): StartAreaOverviewTile {
    if (importedMaterials.isEmpty() && feeds.isEmpty()) return this
    val sourceLabels = buildRadarSourceLabels(
        importedMaterials = importedMaterials,
        feeds = feeds,
    )
    if (sourceLabels.isEmpty()) return this
    val sourceCount = sourceLabels.size
    val statusSummary = buildString {
        append(sourceCount)
        append(" Quelle")
        if (sourceCount != 1) append('n')
        append(" · ")
        append(sourceLabels.joinToString(separator = " · "))
    }
    return copy(
        primaryHint = StartAreaHintState(
            id = "radar-sources-connected",
            title = "Quellen verbunden",
            detail = "${sourceLabels.joinToString(separator = ", ")} laufen bereits in diesen Bereich.",
            compactLabel = if (sourceCount == 1) "1 Quelle" else "$sourceCount Quellen",
            tone = StartAreaHintTone.Quiet,
        ),
        todayLabel = if (sourceCount == 1) "1 Quelle" else "$sourceCount Quellen",
        todayStepLabel = if (feeds.any(AreaWebFeedSource::isAutoSyncEnabled)) {
            "Bereich lesen"
        } else {
            "Quellen pruefen"
        },
        statusKind = if (feeds.any(AreaWebFeedSource::isAutoSyncEnabled)) {
            StartAreaStatusKind.Live
        } else {
            StartAreaStatusKind.Stable
        },
        statusLabel = statusSummary,
    )
}

private fun buildRadarSourceLabels(
    importedMaterials: List<AreaImportedMaterialState>,
    feeds: List<AreaWebFeedSource>,
): List<String> {
    val labels = linkedSetOf<String>()
    val references = importedMaterials.map { it.reference.lowercase() }
    val feedUrls = feeds.map { it.url.lowercase() }

    if (references.any { "x.com" in it || "twitter.com" in it }) labels += "X"
    if (references.any { "instagram.com" in it }) labels += "Instagram"
    if (references.any { "faz.net" in it } || feedUrls.any { "faz.net" in it }) labels += "FAZ"
    if (references.any { "stol.it" in it } || feedUrls.any { "stol.it" in it }) labels += "stol.it"
    if (importedMaterials.any { it.kind == AreaImportKind.Image }) labels += "Screenshots"

    if (feeds.any { it.sourceKind == AreaWebFeedSourceKind.Feed } && labels.none { it == "FAZ" || it == "stol.it" }) {
        labels += "Feeds"
    }
    if (feeds.any { it.sourceKind == AreaWebFeedSourceKind.Website } && labels.none { it == "FAZ" || it == "stol.it" }) {
        labels += "Web"
    }
    if (importedMaterials.any { item ->
            item.kind == AreaImportKind.Link &&
                listOf("x.com", "twitter.com", "instagram.com", "faz.net", "stol.it").none(item.reference.lowercase()::contains)
        }) {
        labels += "Links"
    }
    if (importedMaterials.any { it.kind == AreaImportKind.File }) labels += "Dateien"
    return labels.toList()
}

private fun StartAreaOverviewTile.applyHealthSignals(
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    healthObservations: List<DomainObservation>,
): StartAreaOverviewTile {
    val healthConnected = bindings.any { it.source == DataSourceKind.HEALTH_CONNECT }
    if (!healthConnected) {
        return copy(
            primaryHint = StartAreaHintState(
                id = "health-not-connected",
                title = "Health Connect noch nicht verbunden",
                detail = "Verbinde Health Connect, damit dieser Bereich lokale Schlaf- und Bewegungsdaten lesen kann.",
                compactLabel = "Einrichtung offen",
                tone = StartAreaHintTone.Notice,
            ),
            todayLabel = "Nicht verbunden",
            todayStepLabel = "Health Connect verbinden",
            statusKind = StartAreaStatusKind.Waiting,
            statusLabel = "Einrichtung offen",
        )
    }
    if (!capabilityProfile.isUsable(DataSourceKind.HEALTH_CONNECT)) {
        return copy(
            primaryHint = StartAreaHintState(
                id = "health-permission",
                title = "Health Connect braucht Freigabe",
                detail = "Der Bereich ist verbunden, aber Health Connect ist global noch nicht nutzbar.",
                compactLabel = "Health Connect freigeben",
                tone = StartAreaHintTone.Warning,
            ),
            todayLabel = "Health Connect braucht Freigabe",
            todayStepLabel = "In Einstellungen freigeben",
            statusKind = StartAreaStatusKind.Pull,
            statusLabel = "Freigabe offen",
        )
    }
    return copy(
        primaryHint = StartAreaHintState(
            id = "health-connected",
            title = "Health Connect aktiv",
            detail = "Dieser Bereich liest lokale Schlaf- und Bewegungsdaten fuer heute.",
            compactLabel = healthCompactLabel(healthObservations),
            tone = StartAreaHintTone.Quiet,
        ),
        todayLabel = healthStatusLabel(healthObservations),
        todayStepLabel = healthNextStepLabel(healthObservations),
        statusKind = if (healthObservations.isEmpty()) StartAreaStatusKind.Stable else StartAreaStatusKind.Live,
        statusLabel = if (healthObservations.isEmpty()) "Heute ruhig" else "Health Connect aktiv",
    )
}

private fun StartAreaDetailState.applyCalendarSignals(
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    calendarSignals: List<CalendarSignal>,
    zoneId: ZoneId,
): StartAreaDetailState {
    val slice = resolveCalendarAreaSlice(
        title = title,
        summary = summary,
        iconKey = iconKey,
        templateId = templateId,
        behaviorClass = todayOutput.behaviorClass,
        boundSources = bindings.toSourceKindSet(),
        capabilityProfile = capabilityProfile,
        calendarSignals = calendarSignals,
    ) ?: return this
    val calendarOutput = projectCalendarAreaTodayOutput(
        baseOutput = todayOutput,
        areaTitle = title,
        slice = slice,
        generatedAt = todayOutput.generatedAt,
        zoneId = zoneId,
    )
    return copy(
        hints = mergeHints(hints, calendarDetailHint(slice)),
        statusKind = calendarStatusKind(slice.status),
        statusLabel = calendarDetailStatusLabel(slice.status),
        todayLabel = calendarOutput.headline,
        todayRecommendation = calendarOutput.recommendation,
        todayStepLabel = calendarOutput.nextMeaningfulStep.label,
        todayOutput = calendarOutput,
    )
}

private fun StartAreaDetailState.applyNotificationSignals(
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    notificationSignals: List<NotificationSignal>,
    zoneId: ZoneId,
): StartAreaDetailState {
    val notificationsConnected = bindings.any { it.source == DataSourceKind.NOTIFICATIONS }
    if (!notificationsConnected) {
        return copy(
            hints = mergeHints(
                hints,
                StartAreaHintState(
                    id = "notifications-not-connected",
                    title = "Benachrichtigungen noch nicht verbunden",
                    detail = "Dieser Bereich kann wichtige Hinweise lesen, sobald du den Notification Listener verbindest.",
                    compactLabel = "Listener verbinden",
                    tone = StartAreaHintTone.Notice,
                ),
            ),
            statusKind = StartAreaStatusKind.Waiting,
            statusLabel = "Einrichtung offen",
            todayLabel = "Benachrichtigungen sind noch nicht verbunden",
            todayStepLabel = "Listener verbinden",
        )
    }
    if (!capabilityProfile.isUsable(DataSourceKind.NOTIFICATIONS)) {
        return copy(
            hints = mergeHints(
                hints,
                StartAreaHintState(
                    id = "notifications-permission",
                    title = "Benachrichtigungen brauchen Freigabe",
                    detail = "Der Bereich ist verbunden, aber der Notification Listener ist global noch nicht aktiv.",
                    compactLabel = "Listener freigeben",
                    tone = StartAreaHintTone.Warning,
                ),
            ),
            statusKind = StartAreaStatusKind.Pull,
            statusLabel = "Freigabe offen",
            todayLabel = "Listener braucht Freigabe",
            todayStepLabel = "In Einstellungen freigeben",
        )
    }
    val statusLabel = notificationStatusLabel(notificationSignals)
    return copy(
        hints = mergeHints(
            hints,
            StartAreaHintState(
                id = "notifications-connected",
                title = "Benachrichtigungen aktiv",
                detail = "Dieser Bereich liest lokale Benachrichtigungen und zeigt nur den kompakten Hinweisstand.",
                compactLabel = notificationCompactLabel(notificationSignals),
                tone = StartAreaHintTone.Quiet,
            ),
        ),
        statusKind = if (notificationSignals.isEmpty()) StartAreaStatusKind.Stable else StartAreaStatusKind.Live,
        statusLabel = if (notificationSignals.isEmpty()) "Heute ruhig" else "Benachrichtigungen aktiv",
        todayLabel = statusLabel,
        todayRecommendation = if (notificationSignals.isEmpty()) {
            "Heute liegt noch kein aktiver Hinweis im Listener."
        } else {
            "Der Bereich liest aktuell ${notificationSignals.size} lokale Hinweise."
        },
        todayStepLabel = notificationNextStepLabel(notificationSignals, zoneId),
    )
}

private fun StartAreaDetailState.applyHealthSignals(
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    healthObservations: List<DomainObservation>,
): StartAreaDetailState {
    val healthConnected = bindings.any { it.source == DataSourceKind.HEALTH_CONNECT }
    if (!healthConnected) {
        return copy(
            hints = mergeHints(
                hints,
                StartAreaHintState(
                    id = "health-not-connected",
                    title = "Health Connect noch nicht verbunden",
                    detail = "Dieser Bereich kann Schlaf- und Bewegungsdaten lesen, sobald du Health Connect verbindest.",
                    compactLabel = "Health Connect verbinden",
                    tone = StartAreaHintTone.Notice,
                ),
            ),
            statusKind = StartAreaStatusKind.Waiting,
            statusLabel = "Einrichtung offen",
            todayLabel = "Health Connect ist noch nicht verbunden",
            todayStepLabel = "Health Connect verbinden",
        )
    }
    if (!capabilityProfile.isUsable(DataSourceKind.HEALTH_CONNECT)) {
        return copy(
            hints = mergeHints(
                hints,
                StartAreaHintState(
                    id = "health-permission",
                    title = "Health Connect braucht Freigabe",
                    detail = "Der Bereich ist verbunden, aber Health Connect ist global noch nicht aktiv oder nicht freigegeben.",
                    compactLabel = "Health Connect freigeben",
                    tone = StartAreaHintTone.Warning,
                ),
            ),
            statusKind = StartAreaStatusKind.Pull,
            statusLabel = "Freigabe offen",
            todayLabel = "Health Connect braucht Freigabe",
            todayStepLabel = "In Einstellungen freigeben",
        )
    }
    return copy(
        hints = mergeHints(
            hints,
            StartAreaHintState(
                id = "health-connected",
                title = "Health Connect aktiv",
                detail = "Dieser Bereich liest lokale Schlaf- und Bewegungsdaten und zeigt nur den kompakten Tagesstand.",
                compactLabel = healthCompactLabel(healthObservations),
                tone = StartAreaHintTone.Quiet,
            ),
        ),
        statusKind = if (healthObservations.isEmpty()) StartAreaStatusKind.Stable else StartAreaStatusKind.Live,
        statusLabel = if (healthObservations.isEmpty()) "Heute ruhig" else "Health Connect aktiv",
        todayLabel = healthStatusLabel(healthObservations),
        todayRecommendation = if (healthObservations.isEmpty()) {
            "Heute liegen noch keine Health-Connect-Daten vor."
        } else {
            "Der Bereich liest lokale Gesundheitsdaten fuer Schlaf und Bewegung."
        },
        todayStepLabel = healthNextStepLabel(healthObservations),
    )
}

private fun buildCalendarSourceSetupState(
    detail: StartAreaDetailState,
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    calendarSignals: List<CalendarSignal>,
    zoneId: ZoneId,
): AreaSourceSetupState {
    val slice = resolveCalendarAreaSlice(
        title = detail.title,
        summary = detail.summary,
        iconKey = detail.iconKey,
        templateId = detail.templateId,
        behaviorClass = detail.todayOutput.behaviorClass,
        boundSources = bindings.toSourceKindSet(),
        capabilityProfile = capabilityProfile,
        calendarSignals = calendarSignals,
    ) ?: return AreaSourceSetupState(
        sourceKind = DataSourceKind.CALENDAR,
        status = AreaSourceSetupStatus.UNCONFIGURED,
        headline = "Kalender noch nicht verbunden",
        detail = "Verbinde den lokalen Kalender, damit dieser Bereich echte Termine lesen kann.",
        primaryActionLabel = "Kalender verbinden",
        canConnectSource = true,
    )
    val calendarOutput = projectCalendarAreaTodayOutput(
        baseOutput = detail.todayOutput,
        areaTitle = detail.title,
        slice = slice,
        generatedAt = detail.todayOutput.generatedAt,
        zoneId = zoneId,
    )
    return when (slice.status) {
        AreaSourceSetupStatus.UNCONFIGURED -> AreaSourceSetupState(
            sourceKind = DataSourceKind.CALENDAR,
            status = slice.status,
            headline = "Kalender noch nicht verbunden",
            detail = "Verbinde den lokalen Kalender, damit dieser Bereich echte Termine lesen kann.",
            primaryActionLabel = "Kalender verbinden",
            canConnectSource = true,
        )

        AreaSourceSetupStatus.PERMISSION_REQUIRED -> AreaSourceSetupState(
            sourceKind = DataSourceKind.CALENDAR,
            status = slice.status,
            headline = "Kalender braucht Freigabe",
            detail = "Die Verbindung ist gespeichert, aber Kalender ist global noch nicht nutzbar.",
            primaryActionLabel = "Einstellungen",
            secondaryActionLabel = "Trennen",
            isWarning = true,
            canDisconnectSource = true,
        )

        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> AreaSourceSetupState(
            sourceKind = DataSourceKind.CALENDAR,
            status = slice.status,
            headline = "Heute frei",
            detail = calendarOutput.recommendation,
            secondaryActionLabel = "Trennen",
            canDisconnectSource = true,
        )

        AreaSourceSetupStatus.READY -> AreaSourceSetupState(
            sourceKind = DataSourceKind.CALENDAR,
            status = slice.status,
            headline = calendarOutput.statusLabel,
            detail = calendarOutput.nextMeaningfulStep.label,
            secondaryActionLabel = "Trennen",
            canDisconnectSource = true,
        )
    }
}

private fun buildNotificationSourceSetupState(
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    notificationSignals: List<NotificationSignal>,
    zoneId: ZoneId,
): AreaSourceSetupState {
    val notificationsConnected = bindings.any { it.source == DataSourceKind.NOTIFICATIONS }
    val notificationsUsable = capabilityProfile.isUsable(DataSourceKind.NOTIFICATIONS)
    return when {
        !notificationsConnected -> AreaSourceSetupState(
            sourceKind = DataSourceKind.NOTIFICATIONS,
            status = AreaSourceSetupStatus.UNCONFIGURED,
            headline = "Benachrichtigungen noch nicht verbunden",
            detail = "Verbinde den lokalen Listener, damit dieser Bereich wichtige Hinweise lesen kann.",
            primaryActionLabel = "Listener verbinden",
            canConnectSource = true,
        )

        !notificationsUsable -> AreaSourceSetupState(
            sourceKind = DataSourceKind.NOTIFICATIONS,
            status = AreaSourceSetupStatus.PERMISSION_REQUIRED,
            headline = "Benachrichtigungen brauchen Freigabe",
            detail = "Die Verbindung ist gespeichert, aber der Notification Listener ist global noch nicht aktiv.",
            primaryActionLabel = "Einstellungen",
            secondaryActionLabel = "Trennen",
            isWarning = true,
            canDisconnectSource = true,
        )

        else -> AreaSourceSetupState(
            sourceKind = DataSourceKind.NOTIFICATIONS,
            status = if (notificationSignals.isEmpty()) {
                AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA
            } else {
                AreaSourceSetupStatus.READY
            },
            headline = notificationStatusLabel(notificationSignals),
            detail = notificationNextStepLabel(notificationSignals, zoneId),
            secondaryActionLabel = "Trennen",
            canDisconnectSource = true,
        )
    }
}

private fun buildHealthSourceSetupState(
    bindings: List<AreaSourceBinding>,
    capabilityProfile: CapabilityProfile,
    healthObservations: List<DomainObservation>,
): AreaSourceSetupState {
    val healthConnected = bindings.any { it.source == DataSourceKind.HEALTH_CONNECT }
    val healthUsable = capabilityProfile.isUsable(DataSourceKind.HEALTH_CONNECT)
    return when {
        !healthConnected -> AreaSourceSetupState(
            sourceKind = DataSourceKind.HEALTH_CONNECT,
            status = AreaSourceSetupStatus.UNCONFIGURED,
            headline = "Health Connect noch nicht verbunden",
            detail = "Verbinde Health Connect, damit dieser Bereich lokale Schlaf- und Bewegungsdaten lesen kann.",
            primaryActionLabel = "Health Connect verbinden",
            canConnectSource = true,
        )

        !healthUsable -> AreaSourceSetupState(
            sourceKind = DataSourceKind.HEALTH_CONNECT,
            status = AreaSourceSetupStatus.PERMISSION_REQUIRED,
            headline = "Health Connect braucht Freigabe",
            detail = "Die Verbindung ist gespeichert, aber Health Connect ist global noch nicht nutzbar.",
            primaryActionLabel = "Einstellungen",
            secondaryActionLabel = "Trennen",
            isWarning = true,
            canDisconnectSource = true,
        )

        else -> AreaSourceSetupState(
            sourceKind = DataSourceKind.HEALTH_CONNECT,
            status = if (healthObservations.isEmpty()) {
                AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA
            } else {
                AreaSourceSetupStatus.READY
            },
            headline = healthStatusLabel(healthObservations),
            detail = healthNextStepLabel(healthObservations),
            secondaryActionLabel = "Trennen",
            canDisconnectSource = true,
        )
    }
}

private fun resolvePreferredSourceKind(
    tile: StartAreaOverviewTile,
    bindings: List<AreaSourceBinding>,
): DataSourceKind? {
    return resolvePreferredSourceKind(
        family = tile.family,
        title = tile.label,
        summary = tile.summary,
        iconKey = tile.iconKey,
        bindings = bindings,
    )
}

private fun resolvePreferredSourceKind(
    detail: StartAreaDetailState,
    bindings: List<AreaSourceBinding>,
): DataSourceKind? {
    return resolvePreferredSourceKind(
        family = detail.family,
        title = detail.title,
        summary = detail.summary,
        iconKey = detail.iconKey,
        bindings = bindings,
    )
}

private fun resolvePreferredSourceKind(
    family: StartAreaFamily,
    title: String,
    summary: String,
    iconKey: String,
    bindings: List<AreaSourceBinding>,
): DataSourceKind? {
    return resolvePreferredAreaSourceKind(
        title = title,
        summary = summary,
        iconKey = iconKey,
        templateId = when (family) {
            StartAreaFamily.Kontakt -> "person"
            StartAreaFamily.Ort -> "place"
            StartAreaFamily.Routine -> "ritual"
            StartAreaFamily.Pflicht -> "project"
            StartAreaFamily.Gesundheit -> "ritual"
            StartAreaFamily.Radar,
            StartAreaFamily.Sammlung,
            -> "free"
        },
        behaviorClass = when (family) {
            StartAreaFamily.Kontakt -> com.struperto.androidappdays.domain.area.AreaBehaviorClass.RELATIONSHIP
            StartAreaFamily.Ort,
            StartAreaFamily.Routine,
            -> com.struperto.androidappdays.domain.area.AreaBehaviorClass.MAINTENANCE
            StartAreaFamily.Pflicht -> com.struperto.androidappdays.domain.area.AreaBehaviorClass.PROGRESS
            StartAreaFamily.Gesundheit -> com.struperto.androidappdays.domain.area.AreaBehaviorClass.TRACKING
            StartAreaFamily.Radar,
            StartAreaFamily.Sammlung,
            -> com.struperto.androidappdays.domain.area.AreaBehaviorClass.REFLECTION
        },
        boundSources = bindings.toSourceKindSet(),
    )
}

private fun List<AreaSourceBinding>.toSourceKindSet(): Set<DataSourceKind> {
    return mapTo(linkedSetOf(), AreaSourceBinding::source)
}

private fun mergeHints(
    hints: List<StartAreaHintState>,
    newHint: StartAreaHintState,
): List<StartAreaHintState> {
    val sourceHintIds = setOf(
        "source-missing",
        "calendar-not-connected",
        "calendar-permission",
        "calendar-connected",
        "notifications-not-connected",
        "notifications-permission",
        "notifications-connected",
        "health-not-connected",
        "health-permission",
        "health-connected",
    )
    return (hints.filterNot { it.id in sourceHintIds } + newHint)
        .sortedByDescending(StartAreaHintState::tonePriority)
}

private fun calendarOverviewHint(
    slice: com.struperto.androidappdays.domain.area.AreaCalendarSourceSlice,
): StartAreaHintState {
    return when (slice.status) {
        AreaSourceSetupStatus.UNCONFIGURED -> StartAreaHintState(
            id = "calendar-not-connected",
            title = "Kalender noch nicht verbunden",
            detail = "Verbinde den Kalender, damit dieser Bereich echte Tagestermine lesen kann.",
            compactLabel = "Einrichtung offen",
            tone = StartAreaHintTone.Notice,
        )

        AreaSourceSetupStatus.PERMISSION_REQUIRED -> StartAreaHintState(
            id = "calendar-permission",
            title = "Kalender braucht Freigabe",
            detail = "Die Verbindung ist gespeichert, aber der Kalender ist global noch nicht nutzbar.",
            compactLabel = "Kalender freigeben",
            tone = StartAreaHintTone.Warning,
        )

        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> StartAreaHintState(
            id = "calendar-connected",
            title = "Kalender aktiv",
            detail = "Die Quelle ist verbunden, heute liegt aber kein relevanter Termin vor.",
            compactLabel = "Heute frei",
            tone = StartAreaHintTone.Quiet,
        )

        AreaSourceSetupStatus.READY -> StartAreaHintState(
            id = "calendar-connected",
            title = "Kalender aktiv",
            detail = "Dieser Bereich liest heute reale lokale Kalendertermine.",
            compactLabel = when (slice.signals.size) {
                1 -> "1 Termin"
                else -> "${slice.signals.size} Termine"
            },
            tone = StartAreaHintTone.Quiet,
        )
    }
}

private fun calendarDetailHint(
    slice: com.struperto.androidappdays.domain.area.AreaCalendarSourceSlice,
): StartAreaHintState {
    return when (slice.status) {
        AreaSourceSetupStatus.UNCONFIGURED -> StartAreaHintState(
            id = "calendar-not-connected",
            title = "Kalender noch nicht verbunden",
            detail = "Dieser Bereich kann echte Tagesdaten lesen, sobald du den lokalen Kalender verbindest.",
            compactLabel = "Kalender verbinden",
            tone = StartAreaHintTone.Notice,
        )

        AreaSourceSetupStatus.PERMISSION_REQUIRED -> StartAreaHintState(
            id = "calendar-permission",
            title = "Kalender braucht Freigabe",
            detail = "Die Verbindung ist gespeichert, aber die globale Kalenderquelle ist noch nicht verfuegbar oder nicht freigegeben.",
            compactLabel = "Kalender freigeben",
            tone = StartAreaHintTone.Warning,
        )

        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> StartAreaHintState(
            id = "calendar-connected",
            title = "Kalender aktiv",
            detail = "Die Quelle ist verbunden, heute liegt aber kein relevanter Termin vor.",
            compactLabel = "Heute frei",
            tone = StartAreaHintTone.Quiet,
        )

        AreaSourceSetupStatus.READY -> StartAreaHintState(
            id = "calendar-connected",
            title = "Kalender aktiv",
            detail = "Dieser Bereich liest heute reale lokale Kalendertermine und zeigt daraus den kompakten Tagesstand.",
            compactLabel = when (slice.signals.size) {
                1 -> "1 Termin"
                else -> "${slice.signals.size} Termine"
            },
            tone = StartAreaHintTone.Quiet,
        )
    }
}

private fun calendarStatusKind(
    status: AreaSourceSetupStatus,
): StartAreaStatusKind {
    return when (status) {
        AreaSourceSetupStatus.UNCONFIGURED -> StartAreaStatusKind.Waiting
        AreaSourceSetupStatus.PERMISSION_REQUIRED -> StartAreaStatusKind.Pull
        AreaSourceSetupStatus.READY -> StartAreaStatusKind.Live
        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> StartAreaStatusKind.Stable
    }
}

private fun calendarTileStatusLabel(
    status: AreaSourceSetupStatus,
): String {
    return when (status) {
        AreaSourceSetupStatus.UNCONFIGURED -> "Einrichtung offen"
        AreaSourceSetupStatus.PERMISSION_REQUIRED -> "Freigabe offen"
        AreaSourceSetupStatus.READY -> "Kalender aktiv"
        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> "Heute frei"
    }
}

private fun calendarDetailStatusLabel(
    status: AreaSourceSetupStatus,
): String {
    return when (status) {
        AreaSourceSetupStatus.UNCONFIGURED -> "Einrichtung offen"
        AreaSourceSetupStatus.PERMISSION_REQUIRED -> "Freigabe offen"
        AreaSourceSetupStatus.READY -> "Kalender aktiv"
        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> "Heute frei"
    }
}

private fun notificationStatusLabel(
    notificationSignals: List<NotificationSignal>,
): String {
    return when (notificationSignals.size) {
        0 -> "Heute ruhig"
        1 -> "1 Hinweis aktiv"
        else -> "${notificationSignals.size} Hinweise aktiv"
    }
}

private fun notificationCompactLabel(
    notificationSignals: List<NotificationSignal>,
): String {
    return when (notificationSignals.size) {
        0 -> "Heute ruhig"
        1 -> "1 Hinweis"
        else -> "${notificationSignals.size} Hinweise"
    }
}

private fun notificationNextStepLabel(
    notificationSignals: List<NotificationSignal>,
    zoneId: ZoneId,
): String {
    val latestSignal = notificationSignals.maxByOrNull(NotificationSignal::postedAt)
        ?: return "Kein Hinweis im Listener"
    val timeLabel = Instant.ofEpochMilli(latestSignal.postedAt)
        .atZone(zoneId)
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    val title = latestSignal.title.ifBlank { latestSignal.packageName }
    return "$timeLabel · ${title.take(32)}"
}

private fun healthStatusLabel(
    healthObservations: List<DomainObservation>,
): String {
    val sleep = healthObservations.metricValue(ObservationMetric.SLEEP_HOURS)
    val steps = healthObservations.metricValue(ObservationMetric.STEPS)
    return when {
        sleep != null -> "Schlaf ${sleep.formatOneDecimal()} h"
        steps != null -> "${steps.toInt()} Schritte"
        healthObservations.isEmpty() -> "Heute ruhig"
        else -> "${healthObservations.size} Health-Signale"
    }
}

private fun healthCompactLabel(
    healthObservations: List<DomainObservation>,
): String {
    val sleep = healthObservations.metricValue(ObservationMetric.SLEEP_HOURS)
    val steps = healthObservations.metricValue(ObservationMetric.STEPS)
    return when {
        sleep != null -> "${sleep.formatOneDecimal()} h Schlaf"
        steps != null -> "${steps.toInt()} Schritte"
        healthObservations.isEmpty() -> "Heute ruhig"
        else -> "${healthObservations.size} Health-Signale"
    }
}

private fun healthNextStepLabel(
    healthObservations: List<DomainObservation>,
): String {
    val sleep = healthObservations.metricValue(ObservationMetric.SLEEP_HOURS)
    val steps = healthObservations.metricValue(ObservationMetric.STEPS)
    val exercise = healthObservations.metricValue(ObservationMetric.EXERCISE_MINUTES)
    return when {
        sleep != null && steps != null -> "${sleep.formatOneDecimal()} h · ${steps.toInt()} Schritte"
        sleep != null -> "${sleep.formatOneDecimal()} h Schlaf gelesen"
        exercise != null -> "${exercise.toInt()} min Bewegung gelesen"
        steps != null -> "${steps.toInt()} Schritte gelesen"
        else -> "Keine Health-Daten fuer heute"
    }
}

private fun List<DomainObservation>.metricValue(
    metric: ObservationMetric,
): Float? {
    return firstOrNull { it.metric == metric }?.value?.numeric
}

private fun Float.formatOneDecimal(): String {
    return String.format(java.util.Locale.US, "%.1f", this)
}

private fun containsAny(
    text: String,
    vararg needles: String,
): Boolean {
    return needles.any(text::contains)
}
