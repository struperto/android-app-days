package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.data.repository.lifeDomainLabel
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DomainEvaluation
import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.GoalTarget
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.LocalDate

interface EvaluationEngineV0 {
    fun evaluate(
        goals: List<DomainGoal>,
        observations: List<DomainObservation>,
        capabilityProfile: CapabilityProfile,
        logicalDate: LocalDate,
    ): List<DomainEvaluation>
}

class LocalEvaluationEngineV0 : EvaluationEngineV0 {
    override fun evaluate(
        goals: List<DomainGoal>,
        observations: List<DomainObservation>,
        capabilityProfile: CapabilityProfile,
        logicalDate: LocalDate,
    ): List<DomainEvaluation> {
        return goals
            .filter(DomainGoal::isActive)
            .map { goal ->
                val candidates = observations.filter { observation ->
                    observation.logicalDate == logicalDate &&
                        observation.domain == goal.domain &&
                        observation.metric == metricFor(goal.domain)
                }
                val selected = candidates.maxWithOrNull(
                    compareBy<DomainObservation> { observationSourceRank(it.source) }
                        .thenBy { it.startedAt },
                )
                val state = selected?.value?.numeric?.let { observed ->
                    goal.target.evaluateNumeric(
                        observed = observed,
                        withinWindow = true,
                    )
                } ?: EvaluationState.UNKNOWN
                DomainEvaluation(
                    goalId = goal.id,
                    domain = goal.domain,
                    state = state,
                    expectedSummary = expectedSummary(goal),
                    actualSummary = actualSummary(goal.domain, selected),
                    deviationSummary = deviationSummary(goal.target, selected?.value?.numeric, state),
                    confidence = selected?.confidence ?: defaultUnknownConfidence(capabilityProfile, goal.domain),
                    priority = goal.priority,
                    sourceEvidence = buildSourceEvidence(selected),
                )
            }
    }
}

private fun metricFor(domain: LifeDomain): ObservationMetric {
    return when (domain) {
        LifeDomain.SLEEP -> ObservationMetric.SLEEP_HOURS
        LifeDomain.MOVEMENT -> ObservationMetric.STEPS
        LifeDomain.HYDRATION -> ObservationMetric.HYDRATION_LITERS
        LifeDomain.NUTRITION -> ObservationMetric.PROTEIN_GRAMS
        LifeDomain.FOCUS -> ObservationMetric.FOCUS_MINUTES
        LifeDomain.STRESS -> ObservationMetric.NOTIFICATION_LOAD
        else -> ObservationMetric.FOCUS_MINUTES
    }
}

private fun expectedSummary(goal: DomainGoal): String {
    return when (goal.target.kind) {
        com.struperto.androidappdays.domain.TargetKind.RANGE -> {
            "${displayNumber(goal.target.minimum)}-${displayNumber(goal.target.maximum)} ${goal.target.unit}"
        }
        else -> {
            "${displayNumber(goal.target.minimum ?: goal.target.maximum ?: goal.target.exact)} ${goal.target.unit}"
        }
    }.trim()
}

private fun actualSummary(
    domain: LifeDomain,
    observation: DomainObservation?,
): String {
    if (observation?.value?.numeric == null) {
        return "Noch kein Ist fuer ${lifeDomainLabel(domain)}."
    }
    val number = displayNumber(observation.value.numeric)
    val unit = observation.value.unit.orEmpty()
    return "$number $unit aus ${sourceLabel(observation.source)}".trim()
}

private fun deviationSummary(
    target: GoalTarget,
    observed: Float?,
    state: EvaluationState,
): String {
    if (observed == null) return "Sparse data: noch neutral."
    return when (state) {
        EvaluationState.ON_TRACK -> "Liegt im Soll."
        EvaluationState.BELOW_TARGET -> {
            val delta = (target.minimum ?: target.exact ?: 0f) - observed
            "${displayNumber(delta)} ${target.unit} unter Soll."
        }
        EvaluationState.ABOVE_TARGET -> {
            val reference = target.maximum ?: target.exact ?: observed
            "${displayNumber(observed - reference)} ${target.unit} ueber Soll."
        }
        EvaluationState.OUTSIDE_WINDOW -> "Passiert ausserhalb des Ziel-Fensters."
        EvaluationState.UNKNOWN -> "Noch ohne genug Ist-Daten."
    }
}

private fun buildSourceEvidence(observation: DomainObservation?): List<String> {
    if (observation == null) return emptyList()
    return buildList {
        add(sourceLabel(observation.source))
        observation.contextTags.forEach(::add)
    }
}

private fun defaultUnknownConfidence(
    capabilityProfile: CapabilityProfile,
    domain: LifeDomain,
): Float {
    return when (domain) {
        LifeDomain.SLEEP,
        LifeDomain.MOVEMENT,
        -> if (capabilityProfile.sources.any { it.source.name == "HEALTH_CONNECT" && it.granted }) 0.35f else 0.12f
        else -> 0.18f
    }
}

private fun displayNumber(value: Float?): String {
    if (value == null) return "?"
    val rounded = value.toInt()
    return if (value == rounded.toFloat()) rounded.toString() else String.format("%.1f", value)
}

private fun sourceLabel(source: ObservationSource): String {
    return when (source) {
        ObservationSource.USER_INPUT -> "manuell"
        ObservationSource.WEARABLE -> "Health Connect"
        ObservationSource.PHONE_SENSOR -> "Telefon"
        ObservationSource.CALENDAR -> "Kalender"
        ObservationSource.NOTIFICATION_LISTENER -> "Notifications"
        ObservationSource.IMPORT -> "Import"
        ObservationSource.SYSTEM_INFERENCE -> "System"
    }
}

private fun observationSourceRank(source: ObservationSource): Int {
    return when (source) {
        ObservationSource.USER_INPUT -> 4
        ObservationSource.WEARABLE -> 3
        ObservationSource.CALENDAR,
        ObservationSource.NOTIFICATION_LISTENER,
        ObservationSource.IMPORT,
        ObservationSource.SYSTEM_INFERENCE,
        ObservationSource.PHONE_SENSOR,
        -> 2
    }
}
