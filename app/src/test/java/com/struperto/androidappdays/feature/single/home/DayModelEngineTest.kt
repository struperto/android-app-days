package com.struperto.androidappdays.feature.single.home

import com.struperto.androidappdays.data.repository.DateContext
import com.struperto.androidappdays.data.repository.DayModelInput
import com.struperto.androidappdays.data.repository.FingerprintDimension
import com.struperto.androidappdays.data.repository.LearningEvent
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.NotificationSignal
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.PlanItemStatus
import com.struperto.androidappdays.data.repository.RecentBehavior
import com.struperto.androidappdays.data.repository.SignalEnvelope
import com.struperto.androidappdays.data.repository.SignalKind
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.data.repository.UserFingerprint
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DayModelEngineTest {
    private val engine: DayModelEngine = LocalDayModelEngine()
    private val zoneId = ZoneId.of("Europe/Berlin")

    @Test
    fun project_createsHourlyTimelineFromFingerprint() {
        val result = engine.project(
            DayModelInput(
                userFingerprint = fingerprint(),
                dateContext = DateContext(
                    date = LocalDate.of(2026, 3, 14),
                    now = LocalTime.of(8, 30),
                    zoneId = zoneId,
                ),
                plans = listOf(
                    PlanItem(
                        id = "p1",
                        vorhabenId = null,
                        title = "Strategieblock",
                        note = "",
                        areaId = "focus",
                        timeBlock = TimeBlock.MORGEN,
                        plannedDate = "2026-03-14",
                        status = PlanItemStatus.OPEN,
                        createdAt = 1L,
                        updatedAt = 1L,
                    ),
                ),
                signals = emptyList(),
                recentBehavior = RecentBehavior(
                    recentPlans = emptyList(),
                    learningEvents = emptyList(),
                ),
                overrides = emptyList(),
            ),
        )

        assertEquals(24, result.segments.size)
        assertEquals("08:00", result.segments.first { it.startHour == 8 }.label)
        assertEquals("05:00", result.segments.first { it.startHour == 29 }.label)
        assertTrue(result.topPriorities.contains("Strategieblock"))
        assertTrue(result.segments.any { it.primaryFocus.contains("Strategieblock") })
    }

    @Test
    fun project_turnsNotificationPressureIntoRiskAndCoach() {
        val result = engine.project(
            DayModelInput(
                userFingerprint = fingerprint(),
                dateContext = DateContext(
                    date = LocalDate.of(2026, 3, 14),
                    now = LocalTime.of(14, 20),
                    zoneId = zoneId,
                ),
                plans = emptyList(),
                signals = listOf(
                    SignalEnvelope(
                        id = "notification:n1",
                        kind = SignalKind.NOTIFICATION,
                        sourceLabel = "Benachrichtigung",
                        title = "Neue Nachricht",
                        detail = "Bitte heute rueckmelden",
                        startMillis = LocalDate.of(2026, 3, 14).atTime(14, 5).atZone(zoneId).toInstant().toEpochMilli(),
                        endMillis = null,
                        intensity = 0.4f,
                        areaId = null,
                    ),
                    SignalEnvelope(
                        id = "notification:n2",
                        kind = SignalKind.NOTIFICATION,
                        sourceLabel = "Benachrichtigung",
                        title = "Ping",
                        detail = "Kannst du kurz schauen?",
                        startMillis = LocalDate.of(2026, 3, 14).atTime(14, 10).atZone(zoneId).toInstant().toEpochMilli(),
                        endMillis = null,
                        intensity = 0.4f,
                        areaId = null,
                    ),
                ),
                recentBehavior = RecentBehavior(
                    recentPlans = emptyList(),
                    learningEvents = listOf(
                        LearningEvent(
                            id = "l1",
                            type = LearningEventType.PLAN_MOVED,
                            title = "Plan verschoben",
                            detail = "Fokusblock spaeter",
                            createdAt = 1L,
                            day = "2026-03-13",
                        ),
                    ),
                ),
                overrides = emptyList(),
            ),
        )

        val afternoon = result.segments.first { it.startHour == 14 }
        assertTrue(afternoon.layers.any { it.label == "Druck" })
        assertTrue(result.risks.any { it.title.contains("Stoerdruck") })
        assertTrue(result.coachSuggestions.any { it.title.contains("Stoerquellen") })
    }

    private fun fingerprint(): UserFingerprint {
        return UserFingerprint(
            lifeAreas = listOf(
                LifeArea("focus", "Fokus", "Wichtiges schuetzen", 5, 0, true),
                LifeArea("work", "Arbeit", "Bewegen", 4, 1, true),
            ),
            roles = listOf("Produkt"),
            responsibilities = listOf("Richtung halten"),
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
            disruptionSensitivity = 4,
            recoveryNeed = 4,
            discoveryDay = 4,
            discoveryCommitted = false,
            dimensions = listOf(
                FingerprintDimension(
                    id = "prioritaeten",
                    label = "Prioritaeten",
                    summary = "Strategieblock",
                    confidence = 0.7f,
                ),
            ),
        )
    }
}
