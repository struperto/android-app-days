package com.struperto.androidappdays.testing

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.domain.EvaluationState
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MvpPersonaPlaybackTest {
    @Test
    fun allPersonas_canBeSeededAndProjected() = runBlocking {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp

        val results = app.appContainer.mvpPersonaScenarioRunner.playAll(
            baseDate = LocalDate.of(2026, 3, 8),
        )

        assertEquals(10, results.size)
        results.forEach { result ->
            assertTrue("${result.personaId} should have 24h segments", result.homeState.segments.isNotEmpty())
            assertEquals("${result.personaId} should expose 5 core domains", 5, result.homeState.dailyDomains.size)
            assertTrue(
                "${result.personaId} should not collapse into all unknown",
                result.evaluations.any { it.state != EvaluationState.UNKNOWN },
            )
        }
    }
}
