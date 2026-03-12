package com.struperto.androidappdays.domain.area

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

enum class AreaBehaviorClass(
    val persistedValue: String,
) {
    TRACKING("tracking"),
    PROGRESS("progress"),
    RELATIONSHIP("relationship"),
    MAINTENANCE("maintenance"),
    PROTECTION("protection"),
    REFLECTION("reflection"),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaBehaviorClass {
            return entries.firstOrNull { it.persistedValue == value } ?: REFLECTION
        }
    }
}

enum class AreaSourceTruth {
    manual,
    manual_plus_local,
    local_derived,
    missing,
}

enum class AreaFreshnessBand {
    FRESH,
    AGING,
    STALE,
    UNKNOWN,
}

enum class AreaSeverity {
    NEUTRAL,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

enum class AreaUsabilitySignal {
    EMPTY,
    WEAK,
    USEFUL,
    STRONG,
}

enum class AreaTodayDockKind {
    NONE,
    STATUS,
    ACTION,
    CARE,
    SHIELD,
    REFLECTION,
}

enum class AreaStepKind {
    observe,
    do_step,
    contact,
    maintain,
    protect,
    review,
    reflect,
}

enum class AreaStepStatus {
    READY,
    BLOCKED,
    DONE,
    STALE,
    EMPTY,
}

enum class AreaStepOrigin {
    manual,
    projected_from_goal,
    projected_from_plan,
    projected_from_signal,
    projected_empty_state,
}

data class AreaNextMeaningfulStep(
    val kind: AreaStepKind,
    val label: String,
    val status: AreaStepStatus,
    val origin: AreaStepOrigin,
    val isUserConfirmed: Boolean,
    val fallbackLabel: String,
    val dueHint: String? = null,
    val linkedPlanItemId: String? = null,
    val linkedSourceId: String? = null,
) {
    init {
        require(label.isNotBlank()) { "label must not be blank." }
        require(fallbackLabel.isNotBlank()) { "fallbackLabel must not be blank." }
    }
}

enum class AreaReviewCadence(
    val persistedValue: String,
) {
    daily("daily"),
    weekly("weekly"),
    adaptive("adaptive"),
    manual_only("manual_only"),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaReviewCadence {
            return entries.firstOrNull { it.persistedValue == value } ?: adaptive
        }
    }
}

enum class AreaReviewStatus {
    OFF,
    NOT_DUE,
    DUE,
    OVERDUE,
    MISSING_BASELINE,
}

data class AreaReviewState(
    val cadence: AreaReviewCadence,
    val reviewEnabled: Boolean,
    val lastReviewedAt: Instant?,
    val nextReviewDueAt: Instant?,
    val status: AreaReviewStatus,
)

enum class AreaEvidenceKind {
    MANUAL_SCORE,
    MANUAL_STATE,
    MANUAL_NOTE,
    GOAL,
    PLAN,
    SIGNAL,
    TRACK_SELECTION,
    RHYTHM,
}

data class AreaEvidenceProfile(
    val evidenceCount: Int,
    val primaryEvidenceKind: AreaEvidenceKind?,
    val hasFreshEvidence: Boolean,
    val hasManualAnchor: Boolean,
    val latestEvidenceAt: Instant?,
    val supportingEvidenceKinds: Set<AreaEvidenceKind> = emptySet(),
    val stalenessReason: String? = null,
) {
    init {
        require(evidenceCount >= 0) { "evidenceCount must be positive." }
        require(stalenessReason == null || stalenessReason.isNotBlank()) {
            "stalenessReason must not be blank when provided."
        }
    }
}

data class AreaEmptyStateContract(
    val headline: String,
    val statusLabel: String,
    val recommendation: String,
    val fallbackStepKind: AreaStepKind,
    val fallbackStepLabel: String,
    val fallbackSeverity: AreaSeverity,
    val fallbackDockKind: AreaTodayDockKind,
) {
    init {
        require(headline.isNotBlank()) { "headline must not be blank." }
        require(statusLabel.isNotBlank()) { "statusLabel must not be blank." }
        require(recommendation.isNotBlank()) { "recommendation must not be blank." }
        require(fallbackStepLabel.isNotBlank()) { "fallbackStepLabel must not be blank." }
    }
}

data class AreaTodayOutput(
    val instanceId: String,
    val date: LocalDate,
    val generatedAt: Instant,
    val behaviorClass: AreaBehaviorClass,
    val headline: String,
    val statusLabel: String,
    val recommendation: String,
    val nextMeaningfulStep: AreaNextMeaningfulStep,
    val evidenceSummary: String,
    val sourceTruth: AreaSourceTruth,
    val confidence: Float,
    val freshnessAt: Instant?,
    val freshnessBand: AreaFreshnessBand,
    val severity: AreaSeverity,
    val singleDockKind: AreaTodayDockKind,
    val isEmptyState: Boolean,
    val usabilitySignal: AreaUsabilitySignal,
) {
    init {
        require(instanceId.isNotBlank()) { "instanceId must not be blank." }
        require(headline.isNotBlank()) { "headline must not be blank." }
        require(statusLabel.isNotBlank()) { "statusLabel must not be blank." }
        require(recommendation.isNotBlank()) { "recommendation must not be blank." }
        require(evidenceSummary.isNotBlank()) { "evidenceSummary must not be blank." }
        require(confidence in 0f..1f) { "confidence must be between 0 and 1." }
    }
}

fun freshnessBandFor(
    referenceTime: Instant,
    freshnessAt: Instant?,
): AreaFreshnessBand {
    freshnessAt ?: return AreaFreshnessBand.UNKNOWN
    val age = Duration.between(freshnessAt, referenceTime)
    return when {
        age <= Duration.ofHours(18) -> AreaFreshnessBand.FRESH
        age <= Duration.ofHours(72) -> AreaFreshnessBand.AGING
        else -> AreaFreshnessBand.STALE
    }
}

fun defaultBehaviorClassForTemplate(templateId: String?): AreaBehaviorClass {
    return when (templateId) {
        "project" -> AreaBehaviorClass.PROGRESS
        "ritual", "feeling" -> AreaBehaviorClass.REFLECTION
        "place" -> AreaBehaviorClass.MAINTENANCE
        "person" -> AreaBehaviorClass.RELATIONSHIP
        "theme", "medium", "free", null -> AreaBehaviorClass.REFLECTION
        else -> AreaBehaviorClass.REFLECTION
    }
}

fun defaultEmptyStateContract(
    behaviorClass: AreaBehaviorClass,
): AreaEmptyStateContract {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> AreaEmptyStateContract(
            headline = "Noch kein heutiger Zustand.",
            statusLabel = "Offen",
            recommendation = "Erst lesen, dann einordnen.",
            fallbackStepKind = AreaStepKind.observe,
            fallbackStepLabel = "Heutigen Zustand erfassen",
            fallbackSeverity = AreaSeverity.LOW,
            fallbackDockKind = AreaTodayDockKind.STATUS,
        )
        AreaBehaviorClass.PROGRESS -> AreaEmptyStateContract(
            headline = "Kein heutiger Zug verankert.",
            statusLabel = "Ausstehend",
            recommendation = "Aus Ziel muss ein klarer Zug werden.",
            fallbackStepKind = AreaStepKind.do_step,
            fallbackStepLabel = "Naechsten Arbeitsschritt festlegen",
            fallbackSeverity = AreaSeverity.MEDIUM,
            fallbackDockKind = AreaTodayDockKind.ACTION,
        )
        AreaBehaviorClass.RELATIONSHIP -> AreaEmptyStateContract(
            headline = "Kein aktueller Kontaktimpuls.",
            statusLabel = "Leise",
            recommendation = "Die Beziehung braucht erst einen kleinen Lesepunkt.",
            fallbackStepKind = AreaStepKind.contact,
            fallbackStepLabel = "Kleinen Kontaktimpuls waehlen",
            fallbackSeverity = AreaSeverity.LOW,
            fallbackDockKind = AreaTodayDockKind.CARE,
        )
        AreaBehaviorClass.MAINTENANCE -> AreaEmptyStateContract(
            headline = "Noch kein Pflegeanker gesetzt.",
            statusLabel = "Offen",
            recommendation = "Ohne offenen oder faelligen Anker bleibt der Bereich unklar.",
            fallbackStepKind = AreaStepKind.maintain,
            fallbackStepLabel = "Einen Pflegepunkt verankern",
            fallbackSeverity = AreaSeverity.LOW,
            fallbackDockKind = AreaTodayDockKind.ACTION,
        )
        AreaBehaviorClass.PROTECTION -> AreaEmptyStateContract(
            headline = "Schutz noch nicht konkret.",
            statusLabel = "Fragil",
            recommendation = "Ohne Trigger oder Rueckweg bleibt Schutz nur eine Absicht.",
            fallbackStepKind = AreaStepKind.protect,
            fallbackStepLabel = "Einen Schutzanker festlegen",
            fallbackSeverity = AreaSeverity.MEDIUM,
            fallbackDockKind = AreaTodayDockKind.SHIELD,
        )
        AreaBehaviorClass.REFLECTION -> AreaEmptyStateContract(
            headline = "Noch kein heutiger Lesepunkt.",
            statusLabel = "Offen",
            recommendation = "Ein kurzer Reflexionsanker reicht als Start.",
            fallbackStepKind = AreaStepKind.reflect,
            fallbackStepLabel = "Kurzen Lesepunkt setzen",
            fallbackSeverity = AreaSeverity.NEUTRAL,
            fallbackDockKind = AreaTodayDockKind.REFLECTION,
        )
    }
}

fun resolveAreaReviewState(
    cadenceKey: String,
    reviewEnabled: Boolean,
    lastReviewedAt: Instant?,
    referenceTime: Instant,
): AreaReviewState {
    val cadence = AreaReviewCadence.fromPersistedValue(cadenceKey)
    if (!reviewEnabled) {
        return AreaReviewState(
            cadence = cadence,
            reviewEnabled = false,
            lastReviewedAt = lastReviewedAt,
            nextReviewDueAt = null,
            status = AreaReviewStatus.OFF,
        )
    }
    if (lastReviewedAt == null) {
        return AreaReviewState(
            cadence = cadence,
            reviewEnabled = true,
            lastReviewedAt = null,
            nextReviewDueAt = null,
            status = AreaReviewStatus.MISSING_BASELINE,
        )
    }
    val reviewWindow = when (cadence) {
        AreaReviewCadence.daily -> Duration.ofDays(1)
        AreaReviewCadence.weekly -> Duration.ofDays(7)
        AreaReviewCadence.adaptive -> Duration.ofDays(3)
        AreaReviewCadence.manual_only -> Duration.ofDays(Long.MAX_VALUE / 4)
    }
    val nextDue = lastReviewedAt.plus(reviewWindow)
    val status = when {
        cadence == AreaReviewCadence.manual_only -> AreaReviewStatus.NOT_DUE
        referenceTime.isAfter(nextDue.plus(reviewWindow.dividedBy(2))) -> AreaReviewStatus.OVERDUE
        referenceTime.isAfter(nextDue) || referenceTime == nextDue -> AreaReviewStatus.DUE
        else -> AreaReviewStatus.NOT_DUE
    }
    return AreaReviewState(
        cadence = cadence,
        reviewEnabled = true,
        lastReviewedAt = lastReviewedAt,
        nextReviewDueAt = nextDue,
        status = status,
    )
}
