package com.struperto.androidappdays.domain.area

import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceCapability
import com.struperto.androidappdays.domain.DataSourceKind
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaCalendarSourceSliceTest {
    private val zoneId = ZoneId.of("Europe/Berlin")
    private val generatedAt = Instant.parse("2026-03-11T08:00:00Z")

    @Test
    fun resolveCalendarSlice_marksUnconfiguredWhenIntentExistsWithoutBinding() {
        val slice = resolveCalendarAreaSlice(
            title = "Kalender Heute",
            summary = "Besprechungen und Termine lesen.",
            iconKey = "calendar",
            templateId = "project",
            behaviorClass = AreaBehaviorClass.PROGRESS,
            boundSources = emptySet(),
            capabilityProfile = capabilityProfile(DataSourceKind.CALENDAR),
            calendarSignals = emptyList(),
        )

        assertEquals(AreaSourceSetupStatus.UNCONFIGURED, slice?.status)
    }

    @Test
    fun projectCalendarOutput_blocksWhenPermissionIsMissing() {
        val output = projectCalendarAreaTodayOutput(
            baseOutput = baseOutput(),
            areaTitle = "Kalender Heute",
            slice = AreaCalendarSourceSlice(
                status = AreaSourceSetupStatus.PERMISSION_REQUIRED,
                signals = emptyList(),
            ),
            generatedAt = generatedAt,
            zoneId = zoneId,
        )

        assertTrue(output.isEmptyState)
        assertEquals("Freigabe offen", output.statusLabel)
        assertEquals(AreaStepStatus.BLOCKED, output.nextMeaningfulStep.status)
    }

    @Test
    fun projectCalendarOutput_usesUpcomingSignalAsRealTodayReading() {
        val output = projectCalendarAreaTodayOutput(
            baseOutput = baseOutput(),
            areaTitle = "Kalender Heute",
            slice = AreaCalendarSourceSlice(
                status = AreaSourceSetupStatus.READY,
                signals = listOf(
                    CalendarSignal(
                        id = 7L,
                        title = "Review",
                        startMillis = Instant.parse("2026-03-11T10:00:00Z").toEpochMilli(),
                        endMillis = Instant.parse("2026-03-11T11:00:00Z").toEpochMilli(),
                        isAllDay = false,
                    ),
                ),
            ),
            generatedAt = generatedAt,
            zoneId = zoneId,
        )

        assertEquals("1 Termin heute", output.statusLabel)
        assertEquals("Kalender Heute: 11:00 Review", output.headline)
        assertEquals("11:00 · Review", output.nextMeaningfulStep.label)
        assertEquals(AreaSourceTruth.local_derived, output.sourceTruth)
        assertTrue(output.evidenceSummary.contains("Review"))
        assertNotEquals(output.recommendation, output.nextMeaningfulStep.label)
    }

    @Test
    fun projectCalendarOutput_keepsNoDataHonest() {
        val output = projectCalendarAreaTodayOutput(
            baseOutput = baseOutput(),
            areaTitle = "Kalender Heute",
            slice = AreaCalendarSourceSlice(
                status = AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA,
                signals = emptyList(),
            ),
            generatedAt = generatedAt,
            zoneId = zoneId,
        )

        assertTrue(output.isEmptyState)
        assertEquals("Heute frei", output.statusLabel)
        assertEquals(AreaSourceTruth.local_derived, output.sourceTruth)
        assertEquals("Kalender Heute: Heute frei", output.headline)
        assertNotEquals(output.recommendation, output.nextMeaningfulStep.label)
    }

    private fun baseOutput(): AreaTodayOutput {
        return AreaTodayOutput(
            instanceId = "home",
            date = LocalDate.of(2026, 3, 11),
            generatedAt = generatedAt,
            behaviorClass = AreaBehaviorClass.PROGRESS,
            headline = "Kein heutiger Zug verankert.",
            statusLabel = "Ausstehend",
            recommendation = "Aus Ziel muss ein klarer Zug werden.",
            nextMeaningfulStep = AreaNextMeaningfulStep(
                kind = AreaStepKind.do_step,
                label = "Naechsten Arbeitsschritt festlegen",
                status = AreaStepStatus.EMPTY,
                origin = AreaStepOrigin.projected_empty_state,
                isUserConfirmed = false,
                fallbackLabel = "Naechsten Arbeitsschritt festlegen",
            ),
            evidenceSummary = "Noch keine belastbare Evidenz vorhanden.",
            sourceTruth = AreaSourceTruth.missing,
            confidence = 0.18f,
            freshnessAt = null,
            freshnessBand = AreaFreshnessBand.UNKNOWN,
            severity = AreaSeverity.MEDIUM,
            singleDockKind = AreaTodayDockKind.ACTION,
            isEmptyState = true,
            usabilitySignal = AreaUsabilitySignal.EMPTY,
        )
    }

    private fun capabilityProfile(
        vararg usableSources: DataSourceKind,
    ): CapabilityProfile {
        return CapabilityProfile(
            sources = DataSourceKind.entries.map { source ->
                DataSourceCapability(
                    source = source,
                    label = source.name,
                    enabled = source in usableSources || source == DataSourceKind.MANUAL,
                    available = true,
                    granted = source in usableSources || source == DataSourceKind.MANUAL,
                    detail = "",
                )
            },
        )
    }
}
