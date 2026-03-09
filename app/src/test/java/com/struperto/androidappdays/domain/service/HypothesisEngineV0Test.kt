package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.data.repository.defaultDomainGoals
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Test

class HypothesisEngineV0Test {
    private val engine = LocalHypothesisEngineV0()

    @Test
    fun repeatedSignals_createSoftHypothesis() {
        val observations = listOf(
            observation("sleep-1", LocalDate.of(2026, 3, 6), LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 6.4f),
            observation("notify-1", LocalDate.of(2026, 3, 6), LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 9f),
            observation("sleep-2", LocalDate.of(2026, 3, 7), LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 6.8f),
            observation("notify-2", LocalDate.of(2026, 3, 7), LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 7f),
        )

        val hypotheses = engine.build(
            goals = defaultDomainGoals(),
            observations = observations,
            today = LocalDate.of(2026, 3, 8),
        )

        assertTrue(hypotheses.isNotEmpty())
        assertTrue(hypotheses.first().summary.startsWith("Moegliches Muster"))
    }

    private fun observation(
        id: String,
        logicalDate: LocalDate,
        domain: LifeDomain,
        metric: ObservationMetric,
        value: Float,
    ): DomainObservation {
        return DomainObservation(
            id = id,
            goalId = null,
            domain = domain,
            metric = metric,
            source = ObservationSource.USER_INPUT,
            startedAt = Instant.parse("${logicalDate}T08:00:00Z"),
            value = DomainObservationValue(
                numeric = value,
                unit = "",
            ),
            logicalDate = logicalDate,
            confidence = 0.8f,
        )
    }
}
