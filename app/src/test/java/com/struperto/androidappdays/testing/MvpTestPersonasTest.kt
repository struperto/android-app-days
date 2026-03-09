package com.struperto.androidappdays.testing

import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MvpTestPersonasTest {
    @Test
    fun personas_coverTenDistinctArchetypes() {
        val personas = MvpTestPersonas.all

        assertEquals(10, personas.size)
        assertEquals(10, personas.map { it.id }.toSet().size)
        assertTrue(personas.all { it.testFocus.isNotEmpty() })
    }

    @Test
    fun personas_buildObservationFixtures() {
        val baseDate = LocalDate.of(2026, 3, 8)
        val overloadedLead = MvpTestPersonas.all.first { it.id == "overloaded-lead" }
        val observations = overloadedLead.buildObservations(baseDate)

        assertTrue(observations.any { it.domain == LifeDomain.SLEEP && it.metric == ObservationMetric.SLEEP_HOURS })
        assertTrue(observations.any { it.domain == LifeDomain.FOCUS && it.metric == ObservationMetric.CALENDAR_LOAD })
        assertTrue(observations.all { it.logicalDate != null })
    }
}
