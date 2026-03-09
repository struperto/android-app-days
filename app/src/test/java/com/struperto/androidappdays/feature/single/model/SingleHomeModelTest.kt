package com.struperto.androidappdays.feature.single.model

import com.struperto.androidappdays.data.repository.CoachSuggestion
import com.struperto.androidappdays.data.repository.DayRisk
import com.struperto.androidappdays.data.repository.FingerprintDimension
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.SignalEnvelope
import com.struperto.androidappdays.data.repository.SignalKind
import com.struperto.androidappdays.data.repository.SollDayLayer
import com.struperto.androidappdays.data.repository.SollDayLayerType
import com.struperto.androidappdays.data.repository.SollDayModel
import com.struperto.androidappdays.data.repository.SollDaySegment
import com.struperto.androidappdays.data.repository.UserFingerprint
import com.struperto.androidappdays.domain.HourSlotEntry
import com.struperto.androidappdays.domain.HourSlotStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SingleHomeModelTest {
    @Test
    fun projectState_keepsSegments_priorities_andDetails() {
        val state = projectSingleHomeState(
            SingleHomeProjection(
                today = LocalDate.of(2026, 3, 7),
                dayModel = previewDayModel(),
            ),
        )

        assertEquals("Single", state.modeLabel)
        assertEquals("auf Spur", state.fitLabel)
        assertEquals(listOf("Strategieblock", "Inbox abschirmen"), state.topPriorities)
        assertEquals(2, state.segments.size)
        assertTrue(state.segments.any { it.isCurrent })
        assertTrue(state.segmentDetails.containsKey("2026-03-07_8"))
        assertEquals("Strategieblock", state.segmentDetails.getValue("2026-03-07_8").primaryFocus)
    }

    @Test
    fun projectState_clampsTimelineValues() {
        val dayModel = previewDayModel().copy(
            segments = listOf(
                previewDayModel().segments.first().copy(
                    targetLoad = 1.4f,
                    actualLoad = -0.3f,
                    drift = 1.8f,
                ),
            ),
        )

        val state = projectSingleHomeState(
            SingleHomeProjection(
                today = LocalDate.of(2026, 3, 7),
                dayModel = dayModel,
            ),
        )

        assertEquals(1f, state.segments.first().targetLoad, 0.001f)
        assertEquals(0f, state.segmentDetails.getValue("2026-03-07_8").actualLoad, 0.001f)
        assertEquals(1f, state.segmentDetails.getValue("2026-03-07_8").drift, 0.001f)
    }

    @Test
    fun projectState_formatsOvernightHoursAsEvening() {
        val dayModel = previewDayModel().copy(
            segments = listOf(
                previewDayModel().segments.first().copy(
                    id = "2026-03-07_25",
                    startHour = 25,
                    endHour = 26,
                    label = "01:00",
                ),
            ),
        )

        val state = projectSingleHomeState(
            SingleHomeProjection(
                today = LocalDate.of(2026, 3, 7),
                dayModel = dayModel,
            ),
        )

        assertEquals(HomeTrackWindow.ABEND, state.segments.first().window)
        assertEquals("01:00 - 02:00", state.segmentDetails.getValue("2026-03-07_25").subtitle)
    }

    @Test
    fun projectState_derivesOpenStatusFromAlertHints() {
        val state = projectSingleHomeState(
            SingleHomeProjection(
                today = LocalDate.of(2026, 3, 7),
                dayModel = previewDayModel(),
                segmentHints = mapOf(
                    "2026-03-07_8" to listOf(
                        HomeDomainHint(
                            domain = com.struperto.androidappdays.domain.LifeDomain.FOCUS,
                            state = com.struperto.androidappdays.domain.EvaluationState.BELOW_TARGET,
                        ),
                    ),
                ),
            ),
        )

        assertEquals(HourSlotStatus.OPEN, state.segments.first().slotStatus)
    }

    @Test
    fun projectState_prefersExplicitSlotEntry() {
        val state = projectSingleHomeState(
            SingleHomeProjection(
                today = LocalDate.of(2026, 3, 7),
                dayModel = previewDayModel(),
                segmentHints = mapOf(
                    "2026-03-07_8" to listOf(
                        HomeDomainHint(
                            domain = com.struperto.androidappdays.domain.LifeDomain.FOCUS,
                            state = com.struperto.androidappdays.domain.EvaluationState.BELOW_TARGET,
                        ),
                    ),
                ),
                slotEntries = mapOf(
                    "2026-03-07_8" to HourSlotEntry(
                        id = "slot_8",
                        logicalDate = LocalDate.of(2026, 3, 7),
                        segmentId = "2026-03-07_8",
                        logicalHour = 8,
                        windowId = "vormittag",
                        status = HourSlotStatus.REDUCED,
                        note = "sanfter fahren",
                    ),
                ),
            ),
        )

        assertEquals(HourSlotStatus.REDUCED, state.segments.first().slotStatus)
        assertEquals("sanfter fahren", state.segments.first().slotNote)
    }
}

private fun previewDayModel(): SollDayModel {
    val fingerprint = UserFingerprint(
        lifeAreas = listOf(
            LifeArea("focus", "Fokus", "Wichtiges schuetzen", 5, 0, true),
        ),
        roles = listOf("Produkt"),
        responsibilities = listOf("Klarheit"),
        priorityRules = listOf("Strategieblock", "Inbox abschirmen"),
        weeklyRhythm = "Werktage fokussiert",
        recurringCommitments = listOf("Kalender zuerst lesen"),
        goodDayPattern = "Klarer Start",
        badDayPattern = "Zu viele Eingaenge",
        dayStartHour = 6,
        dayEndHour = 22,
        morningEnergy = 4,
        afternoonEnergy = 3,
        eveningEnergy = 2,
        focusStrength = 4,
        disruptionSensitivity = 3,
        recoveryNeed = 4,
        discoveryDay = 4,
        discoveryCommitted = false,
        dimensions = listOf(
            FingerprintDimension(
                id = "prioritaeten",
                label = "Prioritaeten",
                summary = "Strategieblock",
                confidence = 0.72f,
            ),
        ),
    )
    return SollDayModel(
        date = LocalDate.of(2026, 3, 7),
        fingerprint = fingerprint,
        thesis = "Strategieblock gibt dem Tag Richtung.",
        fitScore = 0.76f,
        fitLabel = "auf Spur",
        topPriorities = listOf("Strategieblock", "Inbox abschirmen"),
        risks = listOf(
            DayRisk(
                title = "Stoerdruck",
                detail = "09:00 steht unter viel eingehendem Druck.",
                severity = 0.62f,
                segmentId = "2026-03-07_9",
            ),
        ),
        coachSuggestions = listOf(
            CoachSuggestion(
                title = "Stoerquellen abfangen",
                detail = "Schirme das Fokusfenster ab.",
                segmentId = "2026-03-07_9",
                intensity = 0.58f,
            ),
        ),
        segments = listOf(
            SollDaySegment(
                id = "2026-03-07_8",
                startHour = 8,
                endHour = 9,
                label = "08:00",
                targetLoad = 0.62f,
                actualLoad = 0.28f,
                drift = -0.14f,
                primaryFocus = "Strategieblock",
                layers = listOf(
                    SollDayLayer(SollDayLayerType.BASELINE, "Basis-Soll", 0.4f),
                    SollDayLayer(SollDayLayerType.PROTECTED_FOCUS, "Schutz", 0.2f),
                ),
                linkedSignals = listOf(
                    SignalEnvelope(
                        id = "capture:c1",
                        kind = SignalKind.CAPTURE,
                        sourceLabel = "Signal",
                        title = "Noch offene Mail",
                        detail = "Antwort schreiben",
                        startMillis = 1L,
                        endMillis = null,
                        intensity = 0.3f,
                        areaId = null,
                    ),
                ),
                linkedPlanItems = emptyList(),
                reasons = listOf("Das Fokusfenster soll geschuetzt bleiben."),
                learningHint = "Jede Justierung schaerft dein Profil.",
                coachSuggestion = null,
                isCurrent = true,
            ),
            SollDaySegment(
                id = "2026-03-07_9",
                startHour = 9,
                endHour = 10,
                label = "09:00",
                targetLoad = 0.56f,
                actualLoad = 0.62f,
                drift = 0.18f,
                primaryFocus = "Inbox",
                layers = listOf(
                    SollDayLayer(SollDayLayerType.INCOMING_PRESSURE, "Druck", 0.42f),
                ),
                linkedSignals = emptyList(),
                linkedPlanItems = emptyList(),
                reasons = listOf("Eingehender Druck ist sichtbar."),
                learningHint = "Druck sauber markieren.",
                coachSuggestion = CoachSuggestion(
                    title = "Stoerquellen abfangen",
                    detail = "Schirme die Stunde ab.",
                    segmentId = "2026-03-07_9",
                    intensity = 0.58f,
                ),
                isCurrent = false,
            ),
        ),
    )
}
