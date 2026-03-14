package com.struperto.androidappdays.data.repository

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class FingerprintDimension(
    val id: String,
    val label: String,
    val summary: String,
    val confidence: Float,
)

data class UserFingerprint(
    val lifeAreas: List<LifeArea>,
    val roles: List<String>,
    val responsibilities: List<String>,
    val priorityRules: List<String>,
    val weeklyRhythm: String,
    val recurringCommitments: List<String>,
    val goodDayPattern: String,
    val badDayPattern: String,
    val dayStartHour: Int,
    val dayEndHour: Int,
    val morningEnergy: Int,
    val afternoonEnergy: Int,
    val eveningEnergy: Int,
    val focusStrength: Int,
    val disruptionSensitivity: Int,
    val recoveryNeed: Int,
    val discoveryDay: Int,
    val discoveryCommitted: Boolean,
    val dimensions: List<FingerprintDimension>,
)

data class UserFingerprintDraft(
    val rolesText: String,
    val responsibilitiesText: String,
    val priorityRulesText: String,
    val weeklyRhythm: String,
    val recurringCommitmentsText: String,
    val goodDayPattern: String,
    val badDayPattern: String,
    val dayStartHour: Int,
    val dayEndHour: Int,
    val morningEnergy: Int,
    val afternoonEnergy: Int,
    val eveningEnergy: Int,
    val focusStrength: Int,
    val disruptionSensitivity: Int,
    val recoveryNeed: Int,
)

enum class SignalKind {
    CALENDAR,
    NOTIFICATION,
    CAPTURE,
    PLAN,
    LATER,
}

data class SignalEnvelope(
    val id: String,
    val kind: SignalKind,
    val sourceLabel: String,
    val title: String,
    val detail: String,
    val startMillis: Long,
    val endMillis: Long?,
    val intensity: Float,
    val areaId: String?,
)

data class DateContext(
    val date: LocalDate,
    val now: LocalTime,
    val zoneId: ZoneId,
)

data class RecentBehavior(
    val recentPlans: List<PlanItem>,
    val learningEvents: List<LearningEvent>,
)

data class DayModelInput(
    val userFingerprint: UserFingerprint,
    val dateContext: DateContext,
    val plans: List<PlanItem>,
    val signals: List<SignalEnvelope>,
    val recentBehavior: RecentBehavior,
    val overrides: List<LearningEvent>,
)

enum class SollDayLayerType {
    BASELINE,
    COMMITMENT,
    PROTECTED_FOCUS,
    FLEX,
    INCOMING_PRESSURE,
    ACTUAL,
    DRIFT,
}

data class SollDayLayer(
    val type: SollDayLayerType,
    val label: String,
    val intensity: Float,
)

data class CoachSuggestion(
    val title: String,
    val detail: String,
    val segmentId: String?,
    val intensity: Float,
)

data class DayRisk(
    val title: String,
    val detail: String,
    val severity: Float,
    val segmentId: String?,
)

data class SollDaySegment(
    val id: String,
    val startHour: Int,
    val endHour: Int,
    val label: String,
    val targetLoad: Float,
    val actualLoad: Float,
    val drift: Float,
    val primaryFocus: String,
    val layers: List<SollDayLayer>,
    val linkedSignals: List<SignalEnvelope>,
    val linkedPlanItems: List<PlanItem>,
    val reasons: List<String>,
    val learningHint: String,
    val coachSuggestion: CoachSuggestion?,
    val isCurrent: Boolean,
)

data class SollDayModel(
    val date: LocalDate,
    val fingerprint: UserFingerprint,
    val thesis: String,
    val fitScore: Float,
    val fitLabel: String,
    val topPriorities: List<String>,
    val risks: List<DayRisk>,
    val coachSuggestions: List<CoachSuggestion>,
    val segments: List<SollDaySegment>,
)

enum class LearningEventType {
    QUICK_ADD_NOW,
    QUICK_ADD_LATER,
    CAPTURE_SAVED,
    CAPTURE_TO_PLAN,
    CAPTURE_TO_LATER,
    PLAN_SAVED,
    PLAN_TOGGLED,
    PLAN_MOVED,
    PLAN_REMOVED,
    LATER_SAVED,
    LATER_TO_PLAN,
    LATER_DONE,
    FINGERPRINT_SAVED,
    DISCOVERY_COMMITTED,
    HOUR_SLOT_STATUS_SET,
    HOUR_SLOT_NOTE_SAVED,
}

data class LearningEvent(
    val id: String,
    val type: LearningEventType,
    val title: String,
    val detail: String,
    val createdAt: Long,
    val day: String,
)
