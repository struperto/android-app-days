package com.struperto.androidappdays.feature.single.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SingleHomeModelTest {
    @Test
    fun previewState_containsSingleCoreActions() {
        val state = previewSingleHomeState()

        assertEquals("Heute", state.stageLabel)
        assertEquals("Single", state.modeLabel)
        assertEquals("Tagesabgleich", state.mirrorTitle)
        assertEquals(6, state.actions.size)
        assertEquals(
            listOf(
                "single_life_wheel",
                "single_working_set",
                "single_day_schedule",
                "single_plan",
                "single_capture",
                "single_create",
            ),
            state.actions.map { it.route },
        )
        assertTrue(state.metrics.isNotEmpty())
        assertEquals(6, state.mirrorLanes.size)
    }
}
