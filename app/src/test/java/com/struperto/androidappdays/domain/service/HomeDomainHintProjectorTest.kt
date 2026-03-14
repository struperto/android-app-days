package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.data.repository.defaultDomainGoals
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.GoalPriority
import com.struperto.androidappdays.feature.single.model.HomeTimelineSegment
import com.struperto.androidappdays.feature.single.model.HomeTrackWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDomainHintProjectorTest {
    @Test
    fun projector_keepsDailyStripCompact() {
        val projector = LocalHomeDomainHintProjector()
        val goals = defaultDomainGoals()
        val evaluations = goals.map { goal ->
            com.struperto.androidappdays.domain.DomainEvaluation(
                goalId = goal.id,
                domain = goal.domain,
                state = EvaluationState.ON_TRACK,
                expectedSummary = "",
                actualSummary = "",
                deviationSummary = "",
                confidence = 0.8f,
                priority = goal.priority,
            )
        }
        val segments = listOf(
            HomeTimelineSegment(
                id = "2026-03-08_8",
                label = "08:00",
                subtitle = "",
                targetLoad = 0.5f,
                actualLoad = 0.4f,
                drift = 0f,
                primaryFocus = "Fokus",
                layers = emptyList(),
                isCurrent = true,
                window = HomeTrackWindow.VORMITTAG,
            ),
        )

        val projection = projector.project(
            goals = goals,
            evaluations = evaluations,
            segments = segments,
        )

        assertEquals(6, projection.dailyStrip.size)
        assertTrue(projection.segmentHints.getValue("2026-03-08_8").size <= 3)
        assertEquals(GoalPriority.CORE, projection.dailyStrip.first().priority)
    }
}
