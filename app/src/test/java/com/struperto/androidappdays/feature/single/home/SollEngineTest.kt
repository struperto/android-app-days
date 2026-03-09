package com.struperto.androidappdays.feature.single.home

import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.NotificationSignal
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.PlanItemStatus
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.data.repository.Vorhaben
import com.struperto.androidappdays.feature.single.model.HomeTrackWindow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SollEngineTest {
    private val engine: SollEngine = LocalSollEngine()

    @Test
    fun project_returnsThreeBlocks_evenWithoutHistory() {
        val result = engine.project(
            SollEngineInput(
                today = LocalDate.of(2026, 3, 9),
                now = LocalTime.of(10, 0),
                zoneId = ZoneId.of("Europe/Berlin"),
                activeAreas = listOf(
                    LifeArea("focus", "Fokus", "Tief arbeiten", 5, 0, true),
                ),
                calendarSignals = emptyList(),
                notificationSignals = emptyList(),
                openCaptures = emptyList(),
                activeVorhaben = emptyList(),
                todayPlans = emptyList(),
                recentPlans = emptyList(),
            ),
        )

        assertEquals(3, result.blocks.size)
        assertEquals(HomeTrackWindow.VORMITTAG, result.blocks.first().window)
        assertTrue(result.blocks.all { it.targetFill > 0f })
    }

    @Test
    fun project_derivesFocusHeavyProfile_fromMorningHistory() {
        val result = engine.project(
            SollEngineInput(
                today = LocalDate.of(2026, 3, 10),
                now = LocalTime.of(9, 30),
                zoneId = ZoneId.of("Europe/Berlin"),
                activeAreas = listOf(
                    LifeArea("focus", "Fokus", "Deep work", 5, 0, true),
                ),
                calendarSignals = emptyList(),
                notificationSignals = emptyList(),
                openCaptures = emptyList(),
                activeVorhaben = emptyList(),
                todayPlans = listOf(
                    PlanItem(
                        id = "today-1",
                        vorhabenId = null,
                        title = "Konzept fertigziehen",
                        note = "",
                        areaId = "focus",
                        timeBlock = TimeBlock.MORGEN,
                        plannedDate = "2026-03-10",
                        status = PlanItemStatus.OPEN,
                        createdAt = 1_773_000_000_000L,
                        updatedAt = 1_773_000_000_000L,
                    ),
                ),
                recentPlans = listOf(
                    PlanItem("h1", null, "Deep work", "", "focus", TimeBlock.MORGEN, "2026-03-09", PlanItemStatus.DONE, 1L, 1L),
                    PlanItem("h2", null, "Deep work", "", "focus", TimeBlock.MORGEN, "2026-03-08", PlanItemStatus.DONE, 1L, 1L),
                    PlanItem("h3", null, "Deep work", "", "focus", TimeBlock.MORGEN, "2026-03-07", PlanItemStatus.DONE, 1L, 1L),
                    PlanItem("h4", null, "Review", "", "focus", TimeBlock.MITTAG, "2026-03-06", PlanItemStatus.DONE, 1L, 1L),
                ),
            ),
        )

        val morning = result.blocks.first { it.window == HomeTrackWindow.VORMITTAG }
        val evening = result.blocks.first { it.window == HomeTrackWindow.ABEND }
        assertEquals("Fokus", result.profile.name)
        assertTrue(result.profile.axes.focusPressure > result.profile.axes.disruptionSensitivity)
        assertTrue(morning.targetFill > evening.targetFill)
        assertTrue(morning.reasons.any { it.contains("geschuetzt") || it.contains("Fokus") })
    }

    @Test
    fun project_letsPlansAndDoneInfluenceTargetAndActual() {
        val result = engine.project(
            SollEngineInput(
                today = LocalDate.of(2026, 3, 9),
                now = LocalTime.of(15, 0),
                zoneId = ZoneId.of("Europe/Berlin"),
                activeAreas = listOf(
                    LifeArea("focus", "Fokus", "Tief arbeiten", 5, 0, true),
                    LifeArea("work", "Arbeit", "Bewegen", 4, 1, true),
                ),
                calendarSignals = emptyList(),
                notificationSignals = emptyList(),
                openCaptures = listOf(
                    CaptureItem(
                        id = "c1",
                        text = "Rueckruf fuer spaeter merken",
                        areaId = "work",
                        createdAt = 1_772_846_400_000L,
                        updatedAt = 1_772_846_400_000L,
                        status = "open",
                    ),
                ),
                activeVorhaben = listOf(
                    Vorhaben(
                        id = "v1",
                        title = "Angebot sauber vorbereiten",
                        note = "",
                        areaId = "work",
                        sourceCaptureId = null,
                        status = "active",
                        createdAt = 1_772_846_000_000L,
                        updatedAt = 1_772_846_000_000L,
                    ),
                ),
                todayPlans = listOf(
                    PlanItem(
                        id = "p1",
                        vorhabenId = "v1",
                        title = "Angebot sauber vorbereiten",
                        note = "",
                        areaId = "work",
                        timeBlock = TimeBlock.NACHMITTAG,
                        plannedDate = "2026-03-09",
                        status = PlanItemStatus.DONE,
                        createdAt = 1_772_846_000_000L,
                        updatedAt = 1_772_846_000_000L,
                    ),
                ),
                recentPlans = listOf(
                    PlanItem(
                        id = "hist1",
                        vorhabenId = null,
                        title = "Deep Work",
                        note = "",
                        areaId = "focus",
                        timeBlock = TimeBlock.NACHMITTAG,
                        plannedDate = "2026-03-07",
                        status = PlanItemStatus.DONE,
                        createdAt = 1_772_760_000_000L,
                        updatedAt = 1_772_760_000_000L,
                    ),
                ),
            ),
        )

        val afternoon = result.blocks.first { it.window == HomeTrackWindow.NACHMITTAG }
        assertTrue(afternoon.targetFill > 0.5f)
        assertTrue(afternoon.actualFill > 0.2f)
        assertEquals("Angebot sauber vorbereiten", afternoon.focusHint)
    }

    @Test
    fun project_increasesDisruptionSensitivity_whenOpenLoopsPileUp() {
        val result = engine.project(
            SollEngineInput(
                today = LocalDate.of(2026, 3, 11),
                now = LocalTime.of(10, 15),
                zoneId = ZoneId.of("Europe/Berlin"),
                activeAreas = listOf(
                    LifeArea("work", "Arbeit", "Sortieren", 3, 0, true),
                ),
                calendarSignals = emptyList(),
                notificationSignals = emptyList(),
                openCaptures = listOf(
                    CaptureItem("c1", "Rueckruf 1", "work", 1_772_846_400_000L, 1_772_846_400_000L, "open"),
                    CaptureItem("c2", "Rueckruf 2", "work", 1_772_846_500_000L, 1_772_846_500_000L, "open"),
                    CaptureItem("c3", "Rueckruf 3", "work", 1_772_846_600_000L, 1_772_846_600_000L, "open"),
                    CaptureItem("c4", "Rueckruf 4", "work", 1_772_846_700_000L, 1_772_846_700_000L, "open"),
                ),
                activeVorhaben = listOf(
                    Vorhaben("v1", "Anfrage 1", "", "work", null, "active", 1L, 1L),
                    Vorhaben("v2", "Anfrage 2", "", "work", null, "active", 1L, 1L),
                ),
                todayPlans = emptyList(),
                recentPlans = emptyList(),
            ),
        )

        val morning = result.blocks.first { it.window == HomeTrackWindow.VORMITTAG }
        assertTrue(result.profile.axes.disruptionSensitivity > 0.55f)
        assertTrue(morning.reasons.any { 
            it.contains("Luft") || it.contains("konkurrieren") || it.contains("Impulse") || it.contains("ziehen")
        })
        assertEquals("Ordnen", morning.focusHint)
    }

    @Test
    fun project_turnsYesterdayOpenPlans_intoCarryoverPressure() {
        val result = engine.project(
            SollEngineInput(
                today = LocalDate.of(2026, 3, 12),
                now = LocalTime.of(8, 45),
                zoneId = ZoneId.of("Europe/Berlin"),
                activeAreas = listOf(
                    LifeArea("focus", "Fokus", "Sauber starten", 4, 0, true),
                ),
                calendarSignals = emptyList(),
                notificationSignals = emptyList(),
                openCaptures = emptyList(),
                activeVorhaben = emptyList(),
                todayPlans = emptyList(),
                recentPlans = listOf(
                    PlanItem("y1", null, "Briefing fertigstellen", "", "focus", TimeBlock.MORGEN, "2026-03-11", PlanItemStatus.OPEN, 1L, 1L),
                    PlanItem("y2", null, "Antwort offen", "", "focus", TimeBlock.ABEND, "2026-03-11", PlanItemStatus.OPEN, 1L, 1L),
                    PlanItem("h1", null, "Deep work", "", "focus", TimeBlock.MORGEN, "2026-03-10", PlanItemStatus.DONE, 1L, 1L),
                ),
            ),
        )

        val morning = result.blocks.first { it.window == HomeTrackWindow.VORMITTAG }
        assertTrue(result.profile.axes.disruptionSensitivity > 0.4f)
        assertTrue(result.profile.axes.energyNeed > 0.25f)
        assertTrue(morning.reasons.any { it.contains("gestern") || it.contains("Einstieg") })
        assertTrue(morning.targetFill > 0.34f)
        assertTrue(morning.drift > -0.8f)
    }

    @Test
    fun project_usesCalendarSignals_asPassiveContext() {
        val result = engine.project(
            SollEngineInput(
                today = LocalDate.of(2026, 3, 13),
                now = LocalTime.of(13, 15),
                zoneId = ZoneId.of("Europe/Berlin"),
                activeAreas = listOf(
                    LifeArea("work", "Arbeit", "Abstimmen", 4, 0, true),
                ),
                calendarSignals = listOf(
                    CalendarSignal(
                        id = 77L,
                        title = "Produkt Sync",
                        startMillis = LocalDate.of(2026, 3, 13)
                            .atTime(13, 0)
                            .atZone(ZoneId.of("Europe/Berlin"))
                            .toInstant()
                            .toEpochMilli(),
                        endMillis = LocalDate.of(2026, 3, 13)
                            .atTime(14, 30)
                            .atZone(ZoneId.of("Europe/Berlin"))
                            .toInstant()
                            .toEpochMilli(),
                        isAllDay = false,
                    ),
                ),
                notificationSignals = emptyList(),
                openCaptures = emptyList(),
                activeVorhaben = emptyList(),
                todayPlans = emptyList(),
                recentPlans = emptyList(),
            ),
        )

        val afternoon = result.blocks.first { it.window == HomeTrackWindow.NACHMITTAG }
        assertTrue(afternoon.targetFill > 0.45f)
        assertTrue(afternoon.reasons.any { it.contains("Kalender") || it.contains("Termin") })
        assertEquals("Produkt Sync", afternoon.focusHint)
        assertEquals(1, afternoon.calendarSignals.size)
    }

    @Test
    fun project_usesNotifications_asPassivePressureInCurrentBlock() {
        val zoneId = ZoneId.of("Europe/Berlin")
        val result = engine.project(
            SollEngineInput(
                today = LocalDate.of(2026, 3, 12),
                now = LocalTime.of(14, 10),
                zoneId = zoneId,
                activeAreas = listOf(
                    LifeArea("work", "Arbeit", "Abstimmen", 4, 0, true),
                ),
                calendarSignals = emptyList(),
                notificationSignals = listOf(
                    NotificationSignal(
                        id = "n1",
                        packageName = "com.mail.app",
                        title = "Neue Nachricht",
                        text = "Bitte heute rueckmelden",
                        postedAt = LocalDate.of(2026, 3, 12)
                            .atTime(13, 40)
                            .atZone(zoneId)
                            .toInstant()
                            .toEpochMilli(),
                        removedAt = null,
                    ),
                    NotificationSignal(
                        id = "n2",
                        packageName = "com.chat.app",
                        title = "Rueckfrage",
                        text = "Kannst du kurz schauen?",
                        postedAt = LocalDate.of(2026, 3, 12)
                            .atTime(14, 5)
                            .atZone(zoneId)
                            .toInstant()
                            .toEpochMilli(),
                        removedAt = null,
                    ),
                ),
                openCaptures = emptyList(),
                activeVorhaben = emptyList(),
                todayPlans = emptyList(),
                recentPlans = emptyList(),
            ),
        )

        val afternoon = result.blocks.first { it.window == HomeTrackWindow.NACHMITTAG }
        assertEquals(2, afternoon.notificationSignals.size)
        assertTrue(afternoon.reasons.any { it.contains("Benachrichtigungen") || it.contains("Impulse") })
        assertTrue(afternoon.actualFill > 0.05f)
    }
}
