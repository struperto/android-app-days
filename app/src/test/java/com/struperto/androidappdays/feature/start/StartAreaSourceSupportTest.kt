package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureItemStatus
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaFreshnessBand
import com.struperto.androidappdays.domain.area.AreaNextMeaningfulStep
import com.struperto.androidappdays.domain.area.AreaSeverity
import com.struperto.androidappdays.domain.area.AreaSourceTruth
import com.struperto.androidappdays.domain.area.AreaStepKind
import com.struperto.androidappdays.domain.area.AreaStepOrigin
import com.struperto.androidappdays.domain.area.AreaStepStatus
import com.struperto.androidappdays.domain.area.AreaTodayDockKind
import com.struperto.androidappdays.domain.area.AreaTodayOutput
import com.struperto.androidappdays.domain.area.AreaUsabilitySignal
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StartAreaSourceSupportTest {
    @Test
    fun overlayOverviewWithContentSources_summarizesRadarSourcesForRootTile() {
        val baseState = StartOverviewState(
            areas = listOf(
                StartAreaOverviewTile(
                    areaId = "news-area",
                    label = "News",
                    summary = "Dieser Bereich sammelt News-Quellen fuer einen spaeteren Feed.",
                    family = StartAreaFamily.Radar,
                    todayLabel = "Offen",
                    todayStepLabel = "Quelle waehlen",
                    templateId = "medium",
                    iconKey = "book",
                    statusKind = StartAreaStatusKind.Waiting,
                    statusLabel = "Quelle fehlt",
                    primaryHint = StartAreaHintState(
                        id = "source-missing",
                        title = "Lokale Quelle fehlt",
                        detail = "Noch keine Quelle verbunden.",
                        compactLabel = "Quelle fehlt",
                        tone = StartAreaHintTone.Notice,
                    ),
                    focusLabel = "Quellen",
                    profileLabel = "Ruhig",
                    progress = 0.2f,
                    canMoveEarlier = false,
                    canMoveLater = false,
                    todayOutput = sampleTodayOutput(),
                ),
            ),
        )

        val state = overlayOverviewWithContentSources(
            state = baseState,
            captures = listOf(
                capture(
                    areaId = "news-area",
                    text = buildAreaImportCaptureText(
                        kind = AreaImportKind.Link,
                        title = "Instagram Post",
                        detail = "Link fuer diesen Bereich gespeichert.",
                        reference = "https://www.instagram.com/p/demo/",
                    ),
                ),
                capture(
                    areaId = "news-area",
                    text = buildAreaImportCaptureText(
                        kind = AreaImportKind.Image,
                        title = "Screenshot",
                        detail = "Bild fuer diesen Bereich gespeichert.",
                        reference = "content://screenshot/demo",
                    ),
                ),
            ),
            webFeedSources = listOf(
                feedSource(areaId = "news-area", url = "https://www.faz.net/rss/aktuell/"),
                feedSource(areaId = "news-area", url = "https://www.stol.it/"),
            ),
        )

        val tile = state.areas.single()
        assertEquals("4 Quellen", tile.primaryHint.compactLabel)
        assertEquals("4 Quellen", tile.todayLabel)
        assertEquals("4 Quellen · Instagram · FAZ · stol.it · Screenshots", tile.statusLabel)
        assertEquals(StartAreaStatusKind.Live, tile.statusKind)
    }
}

private fun sampleTodayOutput() = AreaTodayOutput(
    instanceId = "news-area",
    date = LocalDate.of(2026, 3, 14),
    generatedAt = Instant.parse("2026-03-14T10:00:00Z"),
    behaviorClass = AreaBehaviorClass.TRACKING,
    headline = "News ist offen",
    statusLabel = "Offen",
    recommendation = "Quelle waehlen",
    nextMeaningfulStep = AreaNextMeaningfulStep(
        kind = AreaStepKind.observe,
        label = "Quelle waehlen",
        status = AreaStepStatus.READY,
        origin = AreaStepOrigin.projected_empty_state,
        isUserConfirmed = false,
        fallbackLabel = "Quelle waehlen",
    ),
    evidenceSummary = "Noch kein Signal.",
    sourceTruth = AreaSourceTruth.missing,
    confidence = 0.3f,
    freshnessAt = null,
    freshnessBand = AreaFreshnessBand.UNKNOWN,
    severity = AreaSeverity.NEUTRAL,
    singleDockKind = AreaTodayDockKind.NONE,
    isEmptyState = true,
    usabilitySignal = AreaUsabilitySignal.EMPTY,
)

private fun capture(
    areaId: String,
    text: String,
) = CaptureItem(
    id = "capture-$areaId-${text.hashCode()}",
    text = text,
    areaId = areaId,
    createdAt = 1L,
    updatedAt = 1L,
    status = CaptureItemStatus.OPEN,
)

private fun feedSource(
    areaId: String,
    url: String,
) = AreaWebFeedSource(
    areaId = areaId,
    url = url,
    sourceKind = AreaWebFeedSourceKind.Feed,
    isAutoSyncEnabled = true,
    syncCadence = AreaWebFeedSyncCadence.SixHours,
    lastSyncedAt = 1L,
    lastStatusLabel = "Ok",
    lastStatusDetail = "Aktiv",
)
