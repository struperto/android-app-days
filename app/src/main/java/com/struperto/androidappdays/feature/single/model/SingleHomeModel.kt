package com.struperto.androidappdays.feature.single.model

import com.struperto.androidappdays.data.repository.CoachSuggestion
import com.struperto.androidappdays.data.repository.DayRisk
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaDailyCheck
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.SignalEnvelope
import com.struperto.androidappdays.data.repository.SignalKind
import com.struperto.androidappdays.data.repository.SollDayLayer
import com.struperto.androidappdays.data.repository.SollDayModel
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.domain.area.AreaTodayOutput
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.HourSlotEntry
import com.struperto.androidappdays.domain.HourSlotStatus
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.feature.content.AreaContentItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HomeTrackWindow(
    val id: String,
    val label: String,
    val feedbackLabel: String,
) {
    VORMITTAG("vormittag", "Vormittag", "Vormittag"),
    NACHMITTAG("mittag", "Mittag", "Mittagsblock"),
    ABEND("abend", "Abend", "Abend"),
    ;

    val order: Int
        get() = ordinal

    companion object {
        val all = entries.toList()

        fun fromLocalTime(time: LocalTime): HomeTrackWindow {
            return fromHour(time.hour)
        }

        fun fromHour(hour: Int): HomeTrackWindow {
            val normalizedHour = normalizeHour(hour)
            return when {
                normalizedHour in 6..12 -> VORMITTAG
                normalizedHour in 13..17 -> NACHMITTAG
                else -> ABEND
            }
        }

        fun fromTimeBlock(timeBlock: TimeBlock): HomeTrackWindow {
            return when (timeBlock) {
                TimeBlock.MORGEN,
                TimeBlock.MITTAG -> VORMITTAG
                TimeBlock.NACHMITTAG -> NACHMITTAG
                TimeBlock.ABEND -> ABEND
            }
        }
    }
}

data class HomeTimelineSegment(
    val id: String,
    val label: String,
    val subtitle: String,
    val targetLoad: Float,
    val actualLoad: Float,
    val drift: Float,
    val primaryFocus: String,
    val layers: List<SollDayLayer>,
    val isCurrent: Boolean,
    val window: HomeTrackWindow,
    val slotStatus: HourSlotStatus = HourSlotStatus.UNKNOWN,
    val slotNote: String = "",
)

data class HomeSignalCard(
    val id: String,
    val entityId: String?,
    val kind: SignalKind,
    val sourceLabel: String,
    val title: String,
    val detail: String,
    val intensity: Float,
)

data class HomeSegmentDetail(
    val segmentId: String,
    val label: String,
    val subtitle: String,
    val window: HomeTrackWindow,
    val timeBlock: TimeBlock,
    val primaryFocus: String,
    val targetLoad: Float,
    val actualLoad: Float,
    val drift: Float,
    val layers: List<SollDayLayer>,
    val reasons: List<String>,
    val learningHint: String,
    val coachSuggestion: CoachSuggestion?,
    val planItems: List<PlanItem>,
    val signalCards: List<HomeSignalCard>,
)

data class HomeRiskCard(
    val title: String,
    val detail: String,
    val severity: Float,
    val segmentId: String?,
)

data class HomeCoachCard(
    val title: String,
    val detail: String,
    val segmentId: String?,
    val intensity: Float,
)

data class HomeDomainStripItem(
    val domain: LifeDomain,
    val state: EvaluationState,
    val confidence: Float,
)

data class HomeDomainHint(
    val domain: LifeDomain,
    val state: EvaluationState,
)

data class SingleHomeState(
    val today: LocalDate,
    val modeLabel: String,
    val title: String,
    val thesis: String,
    val fitLabel: String,
    val fitScore: Float,
    val topPriorities: List<String>,
    val risks: List<HomeRiskCard>,
    val coachSuggestions: List<HomeCoachCard>,
    val dailyDomains: List<HomeDomainStripItem>,
    val segments: List<HomeTimelineSegment>,
    val segmentDetails: Map<String, HomeSegmentDetail>,
    val segmentHints: Map<String, List<HomeDomainHint>>,
    val feedbackMessage: String? = null,
    val lifeAreas: List<LifeArea> = emptyList(),
    val dailyChecks: Map<String, Int> = emptyMap(),
    val areaDock: AreaTodayOutput? = null,
    val areaFeedAreaId: String? = null,
    val areaFeedTitle: String = "Bereichsfeed",
    val areaFeedStatusLabel: String = "",
    val areaFeedStatusDetail: String = "",
    val areaFeedItems: List<AreaContentItem> = emptyList(),
)

data class SingleHomeProjection(
    val today: LocalDate,
    val dayModel: SollDayModel,
    val dailyDomains: List<HomeDomainStripItem> = emptyList(),
    val segmentHints: Map<String, List<HomeDomainHint>> = emptyMap(),
    val slotEntries: Map<String, HourSlotEntry> = emptyMap(),
    val feedbackMessage: String? = null,
    val lifeAreas: List<LifeArea> = emptyList(),
    val dailyChecks: List<LifeAreaDailyCheck> = emptyList(),
    val areaTodayOutputs: List<AreaTodayOutput> = emptyList(),
)

fun projectSingleHomeState(projection: SingleHomeProjection): SingleHomeState {
    val slotEntries = projection.slotEntries
    val details = projection.dayModel.segments.associate { segment ->
        segment.id to HomeSegmentDetail(
            segmentId = segment.id,
            label = segment.label,
            subtitle = "${displayHourLabel(segment.startHour)} - ${displayHourLabel(segment.endHour)}",
            window = HomeTrackWindow.fromHour(segment.startHour),
            timeBlock = timeBlockForHour(segment.startHour),
            primaryFocus = segment.primaryFocus,
            targetLoad = segment.targetLoad.coerceIn(0f, 1f),
            actualLoad = segment.actualLoad.coerceIn(0f, 1f),
            drift = segment.drift.coerceIn(-1f, 1f),
            layers = segment.layers,
            reasons = segment.reasons,
            learningHint = segment.learningHint,
            coachSuggestion = segment.coachSuggestion,
            planItems = segment.linkedPlanItems,
            signalCards = segment.linkedSignals.map(::toSignalCard),
        )
    }
    return SingleHomeState(
        today = projection.today,
        modeLabel = "Single",
        title = buildMirrorTitle(projection.today),
        thesis = projection.dayModel.thesis,
        fitLabel = projection.dayModel.fitLabel,
        fitScore = projection.dayModel.fitScore.coerceIn(0f, 1f),
        topPriorities = projection.dayModel.topPriorities,
        risks = projection.dayModel.risks.map(::toRiskCard),
        coachSuggestions = projection.dayModel.coachSuggestions.map(::toCoachCard),
        dailyDomains = projection.dailyDomains,
        segments = projection.dayModel.segments.map { segment ->
            val slotEntry = slotEntries[segment.id]
            HomeTimelineSegment(
                id = segment.id,
                label = segment.label,
                subtitle = "${displayHourLabel(segment.startHour)} - ${displayHourLabel(segment.endHour)}",
                targetLoad = segment.targetLoad.coerceIn(0f, 1f),
                actualLoad = segment.actualLoad.coerceIn(0f, 1f),
                drift = segment.drift.coerceIn(-1f, 1f),
                primaryFocus = segment.primaryFocus,
                layers = segment.layers,
                isCurrent = segment.isCurrent,
                window = HomeTrackWindow.fromHour(segment.startHour),
                slotStatus = slotEntry?.status ?: deriveSlotStatus(
                    segmentId = segment.id,
                    segmentHints = projection.segmentHints,
                ),
                slotNote = slotEntry?.note.orEmpty(),
            )
        },
        segmentDetails = details,
        segmentHints = projection.segmentHints,
        feedbackMessage = projection.feedbackMessage,
        lifeAreas = projection.lifeAreas,
        dailyChecks = projection.dailyChecks.associate { check ->
            check.areaId to check.manualScore
        },
        areaDock = projection.areaTodayOutputs
            .sortedWith(
                compareBy<AreaTodayOutput> { it.isEmptyState }
                    .thenByDescending { it.usabilitySignal.ordinal }
                    .thenByDescending { it.severity.ordinal }
                    .thenByDescending { it.confidence }
                    .thenBy { it.headline },
            )
            .firstOrNull(),
    )
}

private fun deriveSlotStatus(
    segmentId: String,
    segmentHints: Map<String, List<HomeDomainHint>>,
): HourSlotStatus {
    val hints = segmentHints[segmentId].orEmpty()
    return when {
        hints.any { hint ->
            hint.state == EvaluationState.BELOW_TARGET ||
                hint.state == EvaluationState.ABOVE_TARGET ||
                hint.state == EvaluationState.OUTSIDE_WINDOW
        } -> HourSlotStatus.OPEN
        hints.any { it.state == EvaluationState.ON_TRACK } -> HourSlotStatus.ON_TRACK
        else -> HourSlotStatus.UNKNOWN
    }
}

private fun toSignalCard(signal: SignalEnvelope): HomeSignalCard {
    return HomeSignalCard(
        id = signal.id,
        entityId = signal.id.substringAfter(':', ""),
        kind = signal.kind,
        sourceLabel = signal.sourceLabel,
        title = signal.title,
        detail = signal.detail,
        intensity = signal.intensity.coerceIn(0f, 1f),
    )
}

private fun toRiskCard(risk: DayRisk): HomeRiskCard {
    return HomeRiskCard(
        title = risk.title,
        detail = risk.detail,
        severity = risk.severity.coerceIn(0f, 1f),
        segmentId = risk.segmentId,
    )
}

private fun toCoachCard(suggestion: CoachSuggestion): HomeCoachCard {
    return HomeCoachCard(
        title = suggestion.title,
        detail = suggestion.detail,
        segmentId = suggestion.segmentId,
        intensity = suggestion.intensity.coerceIn(0f, 1f),
    )
}

private fun timeBlockForHour(hour: Int): TimeBlock {
    val normalizedHour = normalizeHour(hour)
    return when {
        normalizedHour in 6..9 -> TimeBlock.MORGEN
        normalizedHour in 10..12 -> TimeBlock.MITTAG
        normalizedHour in 13..17 -> TimeBlock.NACHMITTAG
        else -> TimeBlock.ABEND
    }
}

private fun buildMirrorTitle(today: LocalDate): String {
    return "Heute · ${today.format(DateTimeFormatter.ofPattern("d. MMM", Locale.GERMAN))}"
}

private fun displayHourLabel(hour: Int): String {
    return "%02d:00".format(normalizeHour(hour))
}

private fun normalizeHour(hour: Int): Int {
    return hour.mod(24)
}
