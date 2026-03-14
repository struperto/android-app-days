package com.struperto.androidappdays.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

enum class LifeDomain {
    SLEEP,
    MOVEMENT,
    HYDRATION,
    NUTRITION,
    FOCUS,
    RECOVERY,
    STRESS,
    CAFFEINE,
    MEDICATION,
    SOCIAL,
    HOUSEHOLD,
    SCREEN_TIME,
    ADMIN,
    HEALTH,
    EMOTIONAL_STATE,
}

enum class GoalPriority {
    CORE,
    SUPPORT,
    PLACEHOLDER,
}

enum class GoalCadence {
    HOURLY,
    DAILY,
    WEEKLY,
}

enum class TargetKind {
    MINIMUM,
    MAXIMUM,
    RANGE,
    EXACT,
    WINDOW,
    BOOLEAN,
    QUALITY,
}

enum class AdaptationMode {
    FIXED,
    CONTEXTUAL,
    LEARNING_ASSISTED,
}

enum class ObservationSource {
    USER_INPUT,
    WEARABLE,
    PHONE_SENSOR,
    CALENDAR,
    NOTIFICATION_LISTENER,
    IMPORT,
    SYSTEM_INFERENCE,
}

enum class DataSourceKind {
    HEALTH_CONNECT,
    CALENDAR,
    NOTIFICATIONS,
    MANUAL,
}

enum class ObservationMetric {
    SLEEP_HOURS,
    STEPS,
    EXERCISE_MINUTES,
    HYDRATION_LITERS,
    PROTEIN_GRAMS,
    FOCUS_MINUTES,
    CALENDAR_LOAD,
    NOTIFICATION_LOAD,
}

enum class EvaluationState {
    ON_TRACK,
    BELOW_TARGET,
    ABOVE_TARGET,
    OUTSIDE_WINDOW,
    UNKNOWN,
}

enum class LearningSignalType {
    GOAL_SET,
    GOAL_ADJUSTED,
    OBSERVATION_CAPTURED,
    SLOT_EDITED,
    TARGET_MET,
    TARGET_MISSED,
    ROUTINE_DETECTED,
    OVERRIDE_APPLIED,
}

enum class SensorCapability {
    SLEEP_TRACKING,
    HEART_RATE,
    HEART_RATE_VARIABILITY,
    STEP_COUNT,
    WORKOUTS,
    CALENDAR,
    NOTIFICATIONS,
    SCREEN_TIME,
    LOCATION,
    MANUAL_NUTRITION,
}

enum class HypothesisWordingStyle {
    SOFT,
}

data class LogicalDay(
    val anchorDate: LocalDate,
    val startHour: Int = 6,
) {
    init {
        require(startHour in 0..23) { "startHour must be between 0 and 23." }
    }

    val endHourExclusive: Int
        get() = startHour + 24

    fun containsLogicalHour(hour: Int): Boolean {
        return hour in startHour until endHourExclusive
    }

    fun toLogicalHour(dateTime: LocalDateTime): Int {
        val baseHour = dateTime.hour
        return if (baseHour < startHour) baseHour + 24 else baseHour
    }

    fun displayHour(hour: Int): String {
        return "%02d:00".format(normalizeHour(hour))
    }

    private fun normalizeHour(hour: Int): Int {
        return hour.mod(24)
    }
}

data class GoalWindow(
    val startLogicalHour: Int,
    val endLogicalHourExclusive: Int,
) {
    init {
        require(endLogicalHourExclusive > startLogicalHour) {
            "endLogicalHourExclusive must be greater than startLogicalHour."
        }
    }

    fun contains(hour: Int): Boolean {
        return hour in startLogicalHour until endLogicalHourExclusive
    }
}

data class GoalTarget(
    val kind: TargetKind,
    val unit: String,
    val minimum: Float? = null,
    val maximum: Float? = null,
    val exact: Float? = null,
    val note: String = "",
) {
    fun evaluateNumeric(
        observed: Float,
        withinWindow: Boolean = true,
    ): EvaluationState {
        if (kind == TargetKind.WINDOW && !withinWindow) {
            return EvaluationState.OUTSIDE_WINDOW
        }
        return when (kind) {
            TargetKind.MINIMUM -> when {
                minimum == null -> EvaluationState.UNKNOWN
                observed >= minimum -> EvaluationState.ON_TRACK
                else -> EvaluationState.BELOW_TARGET
            }
            TargetKind.MAXIMUM -> when {
                maximum == null -> EvaluationState.UNKNOWN
                observed <= maximum -> EvaluationState.ON_TRACK
                else -> EvaluationState.ABOVE_TARGET
            }
            TargetKind.RANGE,
            TargetKind.QUALITY,
            -> when {
                minimum != null && observed < minimum -> EvaluationState.BELOW_TARGET
                maximum != null && observed > maximum -> EvaluationState.ABOVE_TARGET
                minimum == null && maximum == null -> EvaluationState.UNKNOWN
                else -> EvaluationState.ON_TRACK
            }
            TargetKind.EXACT -> when {
                exact == null -> EvaluationState.UNKNOWN
                observed == exact -> EvaluationState.ON_TRACK
                observed < exact -> EvaluationState.BELOW_TARGET
                else -> EvaluationState.ABOVE_TARGET
            }
            TargetKind.WINDOW -> if (withinWindow) EvaluationState.ON_TRACK else EvaluationState.OUTSIDE_WINDOW
            TargetKind.BOOLEAN -> EvaluationState.UNKNOWN
        }
    }
}

data class DomainGoal(
    val id: String,
    val domain: LifeDomain,
    val title: String,
    val cadence: GoalCadence,
    val target: GoalTarget,
    val adaptationMode: AdaptationMode,
    val preferredWindow: GoalWindow? = null,
    val priority: GoalPriority = GoalPriority.SUPPORT,
    val isActive: Boolean = true,
    val rationale: String = "",
)

data class DomainCatalogEntry(
    val domain: LifeDomain,
    val title: String,
    val summary: String,
    val priority: GoalPriority,
    val isActive: Boolean,
    val isImplemented: Boolean,
)

data class PersonFoundation(
    val logicalDayStartHour: Int = 6,
    val activeSensors: Set<SensorCapability> = emptySet(),
    val anchoredDomains: Set<LifeDomain> = emptySet(),
)

data class DomainObservationValue(
    val numeric: Float? = null,
    val boolean: Boolean? = null,
    val text: String? = null,
    val unit: String? = null,
)

data class DomainObservation(
    val id: String,
    val goalId: String?,
    val domain: LifeDomain,
    val metric: ObservationMetric,
    val source: ObservationSource,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val value: DomainObservationValue,
    val logicalDate: LocalDate? = null,
    val sourceRecordId: String? = null,
    val confidence: Float = 1f,
    val contextTags: Set<String> = emptySet(),
)

data class DomainEvaluation(
    val goalId: String,
    val domain: LifeDomain,
    val state: EvaluationState,
    val expectedSummary: String,
    val actualSummary: String,
    val deviationSummary: String,
    val confidence: Float,
    val priority: GoalPriority,
    val sourceEvidence: List<String> = emptyList(),
)

data class DataSourceCapability(
    val source: DataSourceKind,
    val label: String,
    val enabled: Boolean,
    val available: Boolean,
    val granted: Boolean,
    val detail: String,
)

data class CapabilityProfile(
    val sources: List<DataSourceCapability>,
) {
    fun find(source: DataSourceKind): DataSourceCapability? {
        return sources.firstOrNull { it.source == source }
    }

    fun isUsable(source: DataSourceKind): Boolean {
        val capability = find(source) ?: return false
        return capability.enabled && capability.available && capability.granted
    }
}

data class Hypothesis(
    val id: String,
    val domain: LifeDomain,
    val summary: String,
    val confidence: Float,
    val sourceEvidence: List<String>,
    val wordingStyle: HypothesisWordingStyle = HypothesisWordingStyle.SOFT,
)

enum class HourSlotStatus {
    ON_TRACK,
    REDUCED,
    OPEN,
    UNKNOWN,
}

data class HourSlotEntry(
    val id: String,
    val logicalDate: LocalDate,
    val segmentId: String,
    val logicalHour: Int,
    val windowId: String,
    val status: HourSlotStatus,
    val note: String = "",
)

data class HourSlotProjection(
    val logicalHour: Int,
    val domains: Set<LifeDomain>,
    val targetLoad: Float,
    val actualLoad: Float,
    val observationIds: List<String> = emptyList(),
    val note: String = "",
)

data class LearningSignal(
    val id: String,
    val type: LearningSignalType,
    val goalId: String?,
    val domain: LifeDomain,
    val logicalHour: Int?,
    val createdAt: Instant,
    val summary: String,
)
