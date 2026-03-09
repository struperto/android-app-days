package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.Hypothesis
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import java.time.LocalDate

interface HypothesisEngineV0 {
    fun build(
        goals: List<DomainGoal>,
        observations: List<DomainObservation>,
        today: LocalDate,
    ): List<Hypothesis>
}

class LocalHypothesisEngineV0 : HypothesisEngineV0 {
    override fun build(
        goals: List<DomainGoal>,
        observations: List<DomainObservation>,
        today: LocalDate,
    ): List<Hypothesis> {
        val goalMap = goals.associateBy(DomainGoal::domain)
        val byDay = observations.groupBy { it.logicalDate ?: today }
        val hypotheses = buildList {
            val sleepGoal = goalMap[LifeDomain.SLEEP]
            if (sleepGoal != null) {
                val matches = byDay.count { (_, dayObservations) ->
                    metricValue(dayObservations, ObservationMetric.SLEEP_HOURS)?.let { sleepHours ->
                        val notificationLoad = metricValue(dayObservations, ObservationMetric.NOTIFICATION_LOAD) ?: 0f
                        sleepHours < (sleepGoal.target.minimum ?: 0f) && notificationLoad >= 6f
                    } == true
                }
                if (matches >= 2) {
                    add(
                        Hypothesis(
                            id = "hyp_sleep_notifications",
                            domain = LifeDomain.SLEEP,
                            summary = "Moegliches Muster: an Tagen mit hoeherem Stoerdruck faellt dein Schlaf haeufiger unter das Soll.",
                            confidence = (0.42f + (matches * 0.08f)).coerceAtMost(0.86f),
                            sourceEvidence = listOf("$matches Tage mit wenig Schlaf und hohem Notification-Druck"),
                        ),
                    )
                }
            }

            val focusGoal = goalMap[LifeDomain.FOCUS]
            if (focusGoal != null) {
                val matches = byDay.count { (_, dayObservations) ->
                    metricValue(dayObservations, ObservationMetric.FOCUS_MINUTES)?.let { focusMinutes ->
                        val calendarLoad = metricValue(dayObservations, ObservationMetric.CALENDAR_LOAD) ?: 0f
                        focusMinutes < (focusGoal.target.minimum ?: 0f) && calendarLoad >= 4f
                    } == true
                }
                if (matches >= 2) {
                    add(
                        Hypothesis(
                            id = "hyp_focus_calendar",
                            domain = LifeDomain.FOCUS,
                            summary = "Moegliches Muster: voller Kalenderdruck geht bei dir oft mit weniger Fokuszeit zusammen.",
                            confidence = (0.4f + (matches * 0.08f)).coerceAtMost(0.84f),
                            sourceEvidence = listOf("$matches Tage mit niedrigem Fokus und dichterem Kalender"),
                        ),
                    )
                }
            }

            val movementGoal = goalMap[LifeDomain.MOVEMENT]
            if (movementGoal != null) {
                val matches = byDay.count { (_, dayObservations) ->
                    val steps = metricValue(dayObservations, ObservationMetric.STEPS)
                    val sleep = metricValue(dayObservations, ObservationMetric.SLEEP_HOURS)
                    steps != null && sleep != null &&
                        steps < (movementGoal.target.minimum ?: 0f) &&
                        sleep < 7f
                }
                if (matches >= 2) {
                    add(
                        Hypothesis(
                            id = "hyp_movement_sleep",
                            domain = LifeDomain.MOVEMENT,
                            summary = "Moegliches Muster: kuerzerer Schlaf und weniger Bewegung fallen bei dir haeufig zusammen.",
                            confidence = (0.38f + (matches * 0.08f)).coerceAtMost(0.8f),
                            sourceEvidence = listOf("$matches Tage mit wenig Schlaf und niedriger Bewegung"),
                        ),
                    )
                }
            }
        }
        return hypotheses.sortedByDescending(Hypothesis::confidence).take(3)
    }
}

private fun metricValue(
    observations: List<DomainObservation>,
    metric: ObservationMetric,
): Float? {
    return observations
        .filter { it.metric == metric }
        .maxByOrNull { it.startedAt }
        ?.value
        ?.numeric
}
