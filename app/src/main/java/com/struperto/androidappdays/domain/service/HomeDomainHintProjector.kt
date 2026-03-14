package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.domain.DomainEvaluation
import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.GoalPriority
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.feature.single.model.HomeTimelineSegment

data class ProjectedHomeDomainSummary(
    val domain: LifeDomain,
    val state: EvaluationState,
    val confidence: Float,
    val priority: GoalPriority,
)

data class ProjectedHomeDomainHint(
    val domain: LifeDomain,
    val state: EvaluationState,
)

data class ProjectedHomeDomainHints(
    val dailyStrip: List<ProjectedHomeDomainSummary>,
    val segmentHints: Map<String, List<ProjectedHomeDomainHint>>,
)

interface HomeDomainHintProjector {
    fun project(
        goals: List<DomainGoal>,
        evaluations: List<DomainEvaluation>,
        segments: List<HomeTimelineSegment>,
    ): ProjectedHomeDomainHints
}

class LocalHomeDomainHintProjector : HomeDomainHintProjector {
    override fun project(
        goals: List<DomainGoal>,
        evaluations: List<DomainEvaluation>,
        segments: List<HomeTimelineSegment>,
    ): ProjectedHomeDomainHints {
        val activeGoals = goals.filter(DomainGoal::isActive)
        val summaryItems = activeGoals.mapNotNull { goal ->
            evaluations.firstOrNull { it.goalId == goal.id }?.let { evaluation ->
                ProjectedHomeDomainSummary(
                    domain = goal.domain,
                    state = evaluation.state,
                    confidence = evaluation.confidence,
                    priority = goal.priority,
                )
            }
        }.sortedBy { it.priority.ordinal }

        val segmentHints = segments.associate { segment ->
            val segmentHour = segment.id.substringAfterLast('_').toIntOrNull()
                ?: segment.label.substringBefore(':').toIntOrNull()
                ?: -1
            val hints = activeGoals.mapNotNull { goal ->
                if (isRelevantForHour(goal, segmentHour)) {
                    evaluations.firstOrNull { it.goalId == goal.id }?.let { evaluation ->
                        ProjectedHomeDomainHint(
                            domain = goal.domain,
                            state = evaluation.state,
                        )
                    }
                } else {
                    null
                }
            }.sortedBy(::hintOrder).take(3)
            segment.id to hints
        }
        return ProjectedHomeDomainHints(
            dailyStrip = summaryItems,
            segmentHints = segmentHints,
        )
    }
}

private fun isRelevantForHour(
    goal: DomainGoal,
    logicalHour: Int,
): Boolean {
    goal.preferredWindow?.let { preferredWindow ->
        return preferredWindow.contains(logicalHour)
    }
    return when (goal.domain) {
        LifeDomain.MOVEMENT -> logicalHour in listOf(9, 12, 15, 18, 21)
        LifeDomain.HYDRATION -> logicalHour in listOf(7, 10, 13, 16, 19, 22)
        LifeDomain.NUTRITION -> logicalHour in listOf(8, 13, 19)
        else -> false
    }
}

private fun hintOrder(hint: ProjectedHomeDomainHint): Int {
    val stateOrder = when (hint.state) {
        EvaluationState.BELOW_TARGET -> 0
        EvaluationState.UNKNOWN -> 1
        EvaluationState.ON_TRACK -> 2
        EvaluationState.ABOVE_TARGET -> 3
        EvaluationState.OUTSIDE_WINDOW -> 4
    }
    return stateOrder * 10 + hint.domain.ordinal
}
