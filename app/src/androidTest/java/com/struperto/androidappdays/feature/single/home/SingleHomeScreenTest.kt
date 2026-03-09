package com.struperto.androidappdays.feature.single.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import com.struperto.androidappdays.feature.single.model.SingleHomeProjection
import com.struperto.androidappdays.feature.single.model.HomeDomainHint
import com.struperto.androidappdays.feature.single.model.HomeDomainStripItem
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.feature.single.model.projectSingleHomeState
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.HourSlotStatus
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.ui.theme.DaysTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SingleHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboard_showsThreeTimeBlocks() {
        composeRule.setContent {
            DaysTheme {
                SingleHomeScreen(
                    state = previewState(),
                    onOpenStart = {},
                    onOpenMulti = {},
                    onOpenSettings = {},
                    onRefreshPassiveSignals = {},
                    onSetHourSlotStatus = { _, _, _, _ -> },
                    onSaveHourSlotNote = { _, _, _, _ -> },
                )
            }
        }

        composeRule.onNodeWithTag("home-dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("home-domain-strip").assertIsDisplayed()
        composeRule.onNodeWithTag("home-window-vormittag").assertIsDisplayed()
        composeRule.onNodeWithTag("home-window-mittag").assertIsDisplayed()
        composeRule.onNodeWithTag("home-window-abend").assertIsDisplayed()
        composeRule.onNodeWithTag("home-domain-sleep").assertIsDisplayed()
        composeRule.onNodeWithTag("home-hour-08:00").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-hour-13:00").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-hour-18:00").assertCountEquals(0)
    }

    @Test
    fun dashboard_switchesBetweenTimeBlocks() {
        composeRule.setContent {
            DaysTheme {
                SingleHomeScreen(
                    state = previewState(),
                    onOpenStart = {},
                    onOpenMulti = {},
                    onOpenSettings = {},
                    onRefreshPassiveSignals = {},
                    onSetHourSlotStatus = { _, _, _, _ -> },
                    onSaveHourSlotNote = { _, _, _, _ -> },
                )
            }
        }

        composeRule.onNodeWithTag("home-window-mittag").performClick()
        composeRule.onNodeWithTag("home-hour-13:00").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-hour-08:00").assertCountEquals(0)

        composeRule.onNodeWithTag("home-window-abend").performClick()
        composeRule.onNodeWithTag("home-hour-18:00").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-hour-13:00").assertCountEquals(0)
    }

    @Test
    fun quickAddButton_isGone() {
        composeRule.setContent {
            DaysTheme {
                SingleHomeScreen(
                    state = previewState(),
                    onOpenStart = {},
                    onOpenMulti = {},
                    onOpenSettings = {},
                    onRefreshPassiveSignals = {},
                    onSetHourSlotStatus = { _, _, _, _ -> },
                    onSaveHourSlotNote = { _, _, _, _ -> },
                )
            }
        }

        composeRule.onAllNodesWithContentDescription("Quick Add öffnen").assertCountEquals(0)
    }

    @Test
    fun dashboard_togglesHourStatusAndKeepsItAcrossWindows() {
        composeRule.setContent {
            var state: SingleHomeState by mutableStateOf(previewState())
            DaysTheme {
                SingleHomeScreen(
                    state = state,
                    onOpenStart = {},
                    onOpenMulti = {},
                    onOpenSettings = {},
                    onRefreshPassiveSignals = {},
                    onSetHourSlotStatus = { segmentId, _, _, status ->
                        state = state.withSegmentStatus(segmentId, status)
                    },
                    onSaveHourSlotNote = { segmentId, _, _, note ->
                        state = state.withSegmentNote(segmentId, note)
                    },
                )
            }
        }

        composeRule.onNodeWithTag("home-status-badge-08:00-open").assertIsDisplayed()
        composeRule.onNodeWithTag("home-status-chip-08:00-on_track").performClick()
        composeRule.onNodeWithTag("home-status-badge-08:00-on_track").assertIsDisplayed()

        composeRule.onNodeWithTag("home-window-mittag").performClick()
        composeRule.onNodeWithTag("home-hour-13:00").assertIsDisplayed()
        composeRule.onNodeWithTag("home-window-vormittag").performClick()
        composeRule.onNodeWithTag("home-status-badge-08:00-on_track").assertIsDisplayed()
    }

    @Test
    fun dashboard_canResetStatusBackToUnknown() {
        composeRule.setContent {
            var state: SingleHomeState by mutableStateOf(
                previewState().withSegmentStatus("2026-03-07_8", HourSlotStatus.ON_TRACK),
            )
            DaysTheme {
                SingleHomeScreen(
                    state = state,
                    onOpenStart = {},
                    onOpenMulti = {},
                    onOpenSettings = {},
                    onRefreshPassiveSignals = {},
                    onSetHourSlotStatus = { segmentId, _, _, status ->
                        state = state.withSegmentStatus(segmentId, status)
                    },
                    onSaveHourSlotNote = { segmentId, _, _, note ->
                        state = state.withSegmentNote(segmentId, note)
                    },
                )
            }
        }

        composeRule.onNodeWithTag("home-status-chip-08:00-on_track").performClick()
        composeRule.onNodeWithTag("home-status-badge-08:00-unknown").assertIsDisplayed()
    }
}

private fun previewState() = projectSingleHomeState(
    SingleHomeProjection(
        today = LocalDate.of(2026, 3, 7),
        dayModel = previewDayModel(),
        dailyDomains = listOf(
            HomeDomainStripItem(LifeDomain.SLEEP, EvaluationState.UNKNOWN, 0.2f),
            HomeDomainStripItem(LifeDomain.MOVEMENT, EvaluationState.ON_TRACK, 0.8f),
            HomeDomainStripItem(LifeDomain.HYDRATION, EvaluationState.BELOW_TARGET, 0.7f),
            HomeDomainStripItem(LifeDomain.NUTRITION, EvaluationState.ON_TRACK, 0.8f),
            HomeDomainStripItem(LifeDomain.FOCUS, EvaluationState.BELOW_TARGET, 0.7f),
        ),
        segmentHints = mapOf(
            "2026-03-07_8" to listOf(
                HomeDomainHint(LifeDomain.FOCUS, EvaluationState.BELOW_TARGET),
                HomeDomainHint(LifeDomain.HYDRATION, EvaluationState.UNKNOWN),
            ),
        ),
    ),
)

private fun previewDayModel(): SollDayModel {
    return SollDayModel(
        date = LocalDate.of(2026, 3, 7),
        fingerprint = UserFingerprint(
            lifeAreas = listOf(
                LifeArea("focus", "Fokus", "Wichtiges schuetzen", 5, 0, true),
            ),
            roles = listOf("Produkt"),
            responsibilities = listOf("Klarheit"),
            priorityRules = listOf("Strategieblock"),
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
        ),
        thesis = "Strategieblock gibt dem Tag Richtung.",
        fitScore = 0.76f,
        fitLabel = "auf Spur",
        topPriorities = listOf("Strategieblock"),
        risks = listOf(
            DayRisk(
                title = "Stoerdruck",
                detail = "09:00 steht unter Druck.",
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
                coachSuggestion = null,
                isCurrent = false,
            ),
            SollDaySegment(
                id = "2026-03-07_13",
                startHour = 13,
                endHour = 14,
                label = "13:00",
                targetLoad = 0.48f,
                actualLoad = 0.41f,
                drift = -0.07f,
                primaryFocus = "Abstimmung",
                layers = listOf(
                    SollDayLayer(SollDayLayerType.BASELINE, "Basis-Soll", 0.3f),
                ),
                linkedSignals = emptyList(),
                linkedPlanItems = emptyList(),
                reasons = listOf("Mittags sauber halten."),
                learningHint = "Mittag ruhig lesen.",
                coachSuggestion = null,
                isCurrent = false,
            ),
            SollDaySegment(
                id = "2026-03-07_18",
                startHour = 18,
                endHour = 19,
                label = "18:00",
                targetLoad = 0.34f,
                actualLoad = 0.22f,
                drift = -0.12f,
                primaryFocus = "Abschluss",
                layers = listOf(
                    SollDayLayer(SollDayLayerType.BASELINE, "Basis-Soll", 0.22f),
                ),
                linkedSignals = emptyList(),
                linkedPlanItems = emptyList(),
                reasons = listOf("Abends leichter fahren."),
                learningHint = "Abschluss bleibt klein.",
                coachSuggestion = null,
                isCurrent = false,
            ),
        ),
    )
}

private fun SingleHomeState.withSegmentStatus(
    segmentId: String,
    status: HourSlotStatus,
): SingleHomeState {
    return copy(
        segments = segments.map { segment ->
            if (segment.id == segmentId) segment.copy(slotStatus = status) else segment
        },
    )
}

private fun SingleHomeState.withSegmentNote(
    segmentId: String,
    note: String,
): SingleHomeState {
    return copy(
        segments = segments.map { segment ->
            if (segment.id == segmentId) segment.copy(slotNote = note) else segment
        },
    )
}
