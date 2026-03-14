package com.struperto.androidappdays.testing

import com.struperto.androidappdays.data.repository.DateContext
import com.struperto.androidappdays.data.repository.DayModelInput
import com.struperto.androidappdays.data.repository.FingerprintDimension
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.RecentBehavior
import com.struperto.androidappdays.data.repository.UserFingerprint
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceCapability
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.GoalPriority
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.service.LocalEvaluationEngineV0
import com.struperto.androidappdays.domain.service.LocalHomeDomainHintProjector
import com.struperto.androidappdays.domain.service.LocalHypothesisEngineV0
import com.struperto.androidappdays.feature.single.home.LocalDayModelEngine
import com.struperto.androidappdays.feature.single.model.HomeTimelineSegment
import com.struperto.androidappdays.feature.single.model.HomeTrackWindow
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonaDashboardInsightTest {
    @Test
    fun generatePersonaDashboardReport() {
        val baseDate = LocalDate.of(2026, 3, 8)
        val evaluationEngine = LocalEvaluationEngineV0()
        val dayModelEngine = LocalDayModelEngine()
        val hypothesisEngine = LocalHypothesisEngineV0()
        val projector = LocalHomeDomainHintProjector()

        val report = buildString {
            appendLine("# Persona Dashboard Insights")
            appendLine()
            MvpTestPersonas.all.forEach { persona ->
                val goals = persona.buildGoals()
                val observations = persona.buildObservations(baseDate)
                val capabilityProfile = buildCapabilityProfile(persona.sourcePreset)
                val evaluations = evaluationEngine.evaluate(
                    goals = goals,
                    observations = observations.filter { it.logicalDate == baseDate },
                    capabilityProfile = capabilityProfile,
                    logicalDate = baseDate,
                )
                val dayModel = dayModelEngine.project(
                    DayModelInput(
                        userFingerprint = persona.fingerprintDraft.toFingerprint(),
                        dateContext = DateContext(
                            date = baseDate,
                            now = LocalTime.of(10, 0),
                            zoneId = ZoneId.systemDefault(),
                        ),
                        plans = emptyList(),
                        signals = emptyList(),
                        recentBehavior = RecentBehavior(
                            recentPlans = emptyList(),
                            learningEvents = emptyList(),
                        ),
                        overrides = emptyList(),
                    ),
                )
                val projected = projector.project(
                    goals = goals,
                    evaluations = evaluations,
                    segments = dayModel.segments.map { segment ->
                        HomeTimelineSegment(
                            id = segment.id,
                            label = segment.label,
                            subtitle = "",
                            targetLoad = segment.targetLoad,
                            actualLoad = segment.actualLoad,
                            drift = segment.drift,
                            primaryFocus = segment.primaryFocus,
                            layers = segment.layers,
                            isCurrent = segment.isCurrent,
                            window = HomeTrackWindow.fromHour(segment.startHour),
                        )
                    },
                )
                val hypotheses = hypothesisEngine.build(
                    goals = goals,
                    observations = observations,
                    today = baseDate,
                )
                val windowPressure = projected.segmentHints
                    .entries
                    .groupBy { entry ->
                        val hour = entry.key.substringAfterLast('_').toIntOrNull() ?: 0
                        HomeTrackWindow.fromHour(hour)
                    }
                    .mapValues { (_, entries) ->
                        entries.sumOf { entry ->
                            entry.value.count { hint ->
                                hint.state == EvaluationState.BELOW_TARGET ||
                                    hint.state == EvaluationState.ABOVE_TARGET ||
                                    hint.state == EvaluationState.OUTSIDE_WINDOW
                            }
                        }
                    }

                appendLine("## ${persona.name} / ${persona.archetype}")
                appendLine("- Summary: ${persona.summary}")
                appendLine("- Quellen: ${persona.sourcePreset.joinToString()}")
                appendLine(
                    "- Evaluations: ${
                        evaluations.joinToString { evaluation ->
                            "${evaluation.domain.name}:${evaluation.state.name.lowercase()}"
                        }
                    }",
                )
                appendLine(
                    "- Daily strip: ${
                        projected.dailyStrip.joinToString { summary ->
                            "${summary.domain.name}:${summary.state.name.lowercase()}"
                        }
                    }",
                )
                appendLine(
                    "- Window pressure: ${
                        HomeTrackWindow.all.joinToString { window ->
                            "${window.label}=${windowPressure[window] ?: 0}"
                        }
                    }",
                )
                appendLine(
                    "- Hypothesen: ${
                        if (hypotheses.isEmpty()) "keine"
                        else hypotheses.joinToString { it.summary }
                    }",
                )
                appendLine(
                    "- Unknown core domains: ${
                        evaluations.count { it.priority == GoalPriority.CORE && it.state == EvaluationState.UNKNOWN }
                    }",
                )
                appendLine()
            }
        }

        val output = File("build/reports/persona-dashboard-insights.txt")
        output.parentFile?.mkdirs()
        output.writeText(report)

        assertTrue(output.exists())
    }
}

private fun buildCapabilityProfile(
    activeSources: Set<DataSourceKind>,
): CapabilityProfile {
    return CapabilityProfile(
        sources = DataSourceKind.entries.map { source ->
            DataSourceCapability(
                source = source,
                label = source.name,
                enabled = activeSources.contains(source),
                available = true,
                granted = activeSources.contains(source),
                detail = "",
            )
        },
    )
}

private fun com.struperto.androidappdays.data.repository.UserFingerprintDraft.toFingerprint(): UserFingerprint {
    return UserFingerprint(
        lifeAreas = listOf(
            LifeArea("focus", "Fokus", "Wichtiges schuetzen", 5, 0, true),
            LifeArea("health", "Gesundheit", "Basis halten", 5, 1, true),
        ),
        roles = rolesText.split(',').map(String::trim).filter(String::isNotBlank),
        responsibilities = responsibilitiesText.split(',').map(String::trim).filter(String::isNotBlank),
        priorityRules = priorityRulesText.split(',').map(String::trim).filter(String::isNotBlank),
        weeklyRhythm = weeklyRhythm,
        recurringCommitments = recurringCommitmentsText.split(',').map(String::trim).filter(String::isNotBlank),
        goodDayPattern = goodDayPattern,
        badDayPattern = badDayPattern,
        dayStartHour = dayStartHour,
        dayEndHour = if (dayEndHour < dayStartHour) dayEndHour + 24 else dayEndHour,
        morningEnergy = morningEnergy,
        afternoonEnergy = afternoonEnergy,
        eveningEnergy = eveningEnergy,
        focusStrength = focusStrength,
        disruptionSensitivity = disruptionSensitivity,
        recoveryNeed = recoveryNeed,
        discoveryDay = 4,
        discoveryCommitted = false,
        dimensions = listOf(
            FingerprintDimension(
                id = "persona",
                label = "Persona",
                summary = weeklyRhythm,
                confidence = 0.8f,
            ),
        ),
    )
}
