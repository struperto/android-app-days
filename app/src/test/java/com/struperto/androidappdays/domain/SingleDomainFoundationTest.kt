package com.struperto.androidappdays.domain

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SingleDomainFoundationTest {
    @Test
    fun logicalDay_wrapsNightHoursIntoSameDayArc() {
        val logicalDay = LogicalDay(
            anchorDate = LocalDate.of(2026, 3, 8),
            startHour = 6,
        )

        assertTrue(logicalDay.containsLogicalHour(6))
        assertTrue(logicalDay.containsLogicalHour(29))
        assertEquals(25, logicalDay.toLogicalHour(LocalDateTime.of(2026, 3, 9, 1, 15)))
        assertEquals("01:00", logicalDay.displayHour(25))
    }

    @Test
    fun goalTarget_evaluatesSimpleNumericTargets() {
        val stepGoal = GoalTarget(
            kind = TargetKind.MINIMUM,
            unit = "steps",
            minimum = 10_000f,
        )
        val proteinGoal = GoalTarget(
            kind = TargetKind.RANGE,
            unit = "g",
            minimum = 120f,
            maximum = 160f,
        )
        val caffeineGoal = GoalTarget(
            kind = TargetKind.MAXIMUM,
            unit = "cups",
            maximum = 3f,
        )

        assertEquals(EvaluationState.ON_TRACK, stepGoal.evaluateNumeric(10_240f))
        assertEquals(EvaluationState.BELOW_TARGET, stepGoal.evaluateNumeric(8_100f))
        assertEquals(EvaluationState.ON_TRACK, proteinGoal.evaluateNumeric(140f))
        assertEquals(EvaluationState.ABOVE_TARGET, proteinGoal.evaluateNumeric(180f))
        assertEquals(EvaluationState.ON_TRACK, caffeineGoal.evaluateNumeric(2f))
        assertEquals(EvaluationState.ABOVE_TARGET, caffeineGoal.evaluateNumeric(4f))
    }
}
