package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.data.repository.defaultDomainGoals
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceCapability
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluationEngineV0Test {
    private val engine = LocalEvaluationEngineV0()
    private val today = LocalDate.of(2026, 3, 8)
    private val capabilityProfile = CapabilityProfile(
        sources = listOf(
            DataSourceCapability(
                source = DataSourceKind.MANUAL,
                label = "Manuell",
                enabled = true,
                available = true,
                granted = true,
                detail = "",
            ),
        ),
    )

    @Test
    fun missingObservation_staysUnknown() {
        val evaluations = engine.evaluate(
            goals = defaultDomainGoals(),
            observations = emptyList(),
            capabilityProfile = capabilityProfile,
            logicalDate = today,
        )

        assertTrue(evaluations.all { it.state != EvaluationState.BELOW_TARGET })
        assertEquals(
            EvaluationState.UNKNOWN,
            evaluations.first { it.domain == LifeDomain.SLEEP }.state,
        )
    }

    @Test
    fun observedSteps_areEvaluatedAgainstGoal() {
        val evaluations = engine.evaluate(
            goals = defaultDomainGoals(),
            observations = listOf(
                DomainObservation(
                    id = "steps",
                    goalId = "goal_movement",
                    domain = LifeDomain.MOVEMENT,
                    metric = ObservationMetric.STEPS,
                    source = ObservationSource.USER_INPUT,
                    startedAt = Instant.parse("2026-03-08T10:00:00Z"),
                    value = DomainObservationValue(
                        numeric = 12_200f,
                        unit = "steps",
                    ),
                    logicalDate = today,
                    confidence = 1f,
                ),
            ),
            capabilityProfile = capabilityProfile,
            logicalDate = today,
        )

        assertEquals(
            EvaluationState.ON_TRACK,
            evaluations.first { it.domain == LifeDomain.MOVEMENT }.state,
        )
    }

    @Test
    fun observedHydration_isEvaluatedAsOnTrack() {
        val evaluations = engine.evaluate(
            goals = defaultDomainGoals(),
            observations = listOf(
                DomainObservation(
                    id = "hydration",
                    goalId = "goal_hydration",
                    domain = LifeDomain.HYDRATION,
                    metric = ObservationMetric.HYDRATION_LITERS,
                    source = ObservationSource.USER_INPUT,
                    startedAt = Instant.parse("2026-03-08T10:00:00Z"),
                    value = DomainObservationValue(
                        numeric = 2.4f,
                        unit = "L",
                    ),
                    logicalDate = today,
                    confidence = 1f,
                ),
            ),
            capabilityProfile = capabilityProfile,
            logicalDate = today,
        )

        assertEquals(
            EvaluationState.ON_TRACK,
            evaluations.first { it.domain == LifeDomain.HYDRATION }.state,
        )
    }
}
