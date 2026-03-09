package com.struperto.androidappdays.testing

import com.struperto.androidappdays.data.repository.DateContext
import com.struperto.androidappdays.data.repository.GoalRepository
import com.struperto.androidappdays.data.repository.HourSlotEntryRepository
import com.struperto.androidappdays.data.repository.ObservationRepository
import com.struperto.androidappdays.data.repository.RecentBehavior
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.data.repository.UserFingerprintRepository
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainEvaluation
import com.struperto.androidappdays.domain.Hypothesis
import com.struperto.androidappdays.domain.service.EvaluationEngineV0
import com.struperto.androidappdays.domain.service.HomeDomainHintProjector
import com.struperto.androidappdays.domain.service.HypothesisEngineV0
import com.struperto.androidappdays.feature.single.home.DayModelEngine
import com.struperto.androidappdays.feature.single.model.HomeDomainHint
import com.struperto.androidappdays.feature.single.model.HomeDomainStripItem
import com.struperto.androidappdays.feature.single.model.HomeTimelineSegment
import com.struperto.androidappdays.feature.single.model.HomeTrackWindow
import com.struperto.androidappdays.feature.single.model.SingleHomeProjection
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.feature.single.model.projectSingleHomeState
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime

data class PersonaPlaybackResult(
    val personaId: String,
    val personaName: String,
    val homeState: SingleHomeState,
    val evaluations: List<DomainEvaluation>,
    val hypotheses: List<Hypothesis>,
)

class MvpPersonaScenarioRunner(
    private val clock: Clock,
    private val userFingerprintRepository: UserFingerprintRepository,
    private val goalRepository: GoalRepository,
    private val observationRepository: ObservationRepository,
    private val hourSlotEntryRepository: HourSlotEntryRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val dayModelEngine: DayModelEngine,
    private val evaluationEngineV0: EvaluationEngineV0,
    private val hypothesisEngineV0: HypothesisEngineV0,
    private val homeDomainHintProjector: HomeDomainHintProjector,
) {
    suspend fun seedPersona(
        persona: MvpTestPersona,
        baseDate: LocalDate = LocalDate.now(clock),
    ) {
        userFingerprintRepository.save(persona.fingerprintDraft)
        defaultSources().forEach { source ->
            sourceCapabilityRepository.setEnabled(
                source = source,
                enabled = persona.sourcePreset.contains(source),
            )
        }
        persona.buildGoals().forEach { goal ->
            goalRepository.save(goal)
        }
        hourSlotEntryRepository.clearAll()
        observationRepository.clearAll()
        observationRepository.upsertAll(persona.buildObservations(baseDate))
    }

    suspend fun playPersona(
        persona: MvpTestPersona,
        baseDate: LocalDate = LocalDate.now(clock),
    ): PersonaPlaybackResult {
        seedPersona(persona, baseDate)
        val fingerprint = userFingerprintRepository.load()
        val goals = goalRepository.loadActiveGoals()
        val capabilityProfile = sourceCapabilityRepository.loadProfile()
        val observations = observationRepository.loadRange(
            startLogicalDate = baseDate.minusDays(13),
            endLogicalDate = baseDate,
        )
        val todayObservations = observations.filter { it.logicalDate == baseDate }
        val evaluations = evaluationEngineV0.evaluate(
            goals = goals,
            observations = todayObservations,
            capabilityProfile = capabilityProfile,
            logicalDate = baseDate,
        )
        val dayModel = dayModelEngine.project(
            input = com.struperto.androidappdays.data.repository.DayModelInput(
                userFingerprint = fingerprint,
                dateContext = DateContext(
                    date = baseDate,
                    now = LocalTime.of(10, 0),
                    zoneId = clock.zone,
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
        val projectedHints = homeDomainHintProjector.project(
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
        val hypotheses = hypothesisEngineV0.build(
            goals = goals,
            observations = observations,
            today = baseDate,
        )
        return PersonaPlaybackResult(
            personaId = persona.id,
            personaName = persona.name,
            homeState = projectSingleHomeState(
                SingleHomeProjection(
                    today = baseDate,
                    dayModel = dayModel,
                    dailyDomains = projectedHints.dailyStrip.map { summary ->
                        HomeDomainStripItem(
                            domain = summary.domain,
                            state = summary.state,
                            confidence = summary.confidence,
                        )
                    },
                    segmentHints = projectedHints.segmentHints.mapValues { (_, hints) ->
                        hints.map { hint ->
                            HomeDomainHint(
                                domain = hint.domain,
                                state = hint.state,
                            )
                        }
                    },
                ),
            ),
            evaluations = evaluations,
            hypotheses = hypotheses,
        )
    }

    suspend fun playAll(
        baseDate: LocalDate = LocalDate.now(clock),
    ): List<PersonaPlaybackResult> {
        return MvpTestPersonas.all.map { persona ->
            playPersona(
                persona = persona,
                baseDate = baseDate,
            )
        }
    }
}

private fun defaultSources(): List<DataSourceKind> {
    return DataSourceKind.entries.toList()
}
