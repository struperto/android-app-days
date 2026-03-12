package com.struperto.androidappdays.feature.single.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.DateContext
import com.struperto.androidappdays.data.repository.GoalRepository
import com.struperto.androidappdays.data.repository.HourSlotEntryRepository
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.LearningEvent
import com.struperto.androidappdays.data.repository.LearningEventRepository
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeWheelRepository
import com.struperto.androidappdays.data.repository.ObservationRepository
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.PlanRepository
import com.struperto.androidappdays.data.repository.RecentBehavior
import com.struperto.androidappdays.data.repository.SignalEnvelope
import com.struperto.androidappdays.data.repository.SignalRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.data.repository.UserFingerprint
import com.struperto.androidappdays.data.repository.UserFingerprintRepository
import com.struperto.androidappdays.data.repository.defaultLifeAreas
import com.struperto.androidappdays.feature.single.model.HomeCoachCard
import com.struperto.androidappdays.feature.single.model.HomeDomainHint
import com.struperto.androidappdays.feature.single.model.HomeDomainStripItem
import com.struperto.androidappdays.feature.single.model.HomeRiskCard
import com.struperto.androidappdays.feature.single.model.HomeTimelineSegment
import com.struperto.androidappdays.feature.single.model.HomeTrackWindow
import com.struperto.androidappdays.feature.single.model.SingleHomeProjection
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.feature.single.model.projectSingleHomeState
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.HourSlotEntry
import com.struperto.androidappdays.domain.HourSlotStatus
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.AreaTodayOutput
import com.struperto.androidappdays.domain.area.AreaTodayOutputInput
import com.struperto.androidappdays.domain.area.projectAreaTodayOutput
import com.struperto.androidappdays.domain.service.EvaluationEngineV0
import com.struperto.androidappdays.domain.service.HomeDomainHintProjector
import com.struperto.androidappdays.domain.service.ObservationSyncService
import com.struperto.androidappdays.domain.service.ProjectedHomeDomainHint
import com.struperto.androidappdays.domain.service.ProjectedHomeDomainHints
import com.struperto.androidappdays.domain.service.ProjectedHomeDomainSummary
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SingleHomeViewModel(
    private val signalRepository: SignalRepository,
    private val userFingerprintRepository: UserFingerprintRepository,
    private val lifeWheelRepository: LifeWheelRepository,
    private val areaKernelRepository: AreaKernelRepository,
    private val learningEventRepository: LearningEventRepository,
    private val planRepository: PlanRepository,
    private val goalRepository: GoalRepository,
    private val observationRepository: ObservationRepository,
    private val hourSlotEntryRepository: HourSlotEntryRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val observationSyncService: ObservationSyncService,
    private val clock: Clock,
    private val dayModelEngine: DayModelEngine,
    private val evaluationEngineV0: EvaluationEngineV0,
    private val homeDomainHintProjector: HomeDomainHintProjector,
) : ViewModel() {
    private val feedbackMessage = MutableStateFlow<String?>(null)
    private val passiveRefresh = MutableStateFlow(0)

    private val today: LocalDate
        get() = LocalDate.now(clock)

    private val todayIso: String
        get() = today.toString()

    private val recentStartIso: String
        get() = today.minusDays(13).toString()

    private val daySignals = passiveRefresh.flatMapLatest {
        signalRepository.observeDay(
            date = today,
            zoneId = clock.zone,
        )
    }

    init {
        refreshPassiveSignals()
    }

    private val domainInputs = combine(
        goalRepository.observeActiveGoals(),
        observationRepository.observeDay(today),
        hourSlotEntryRepository.observeDay(today),
        sourceCapabilityRepository.observeProfile(),
    ) { goals, observations, hourSlotEntries, capabilityProfile ->
        DomainInput(
            goals = goals,
            observations = observations,
            hourSlotEntries = hourSlotEntries,
            capabilityProfile = capabilityProfile,
        )
    }

    private val behaviorInputs = combine(
        planRepository.observeToday(todayIso),
        planRepository.observeRange(recentStartIso, todayIso),
        learningEventRepository.observeRecent(),
        domainInputs,
    ) { todayPlans: List<PlanItem>,
        recentPlans: List<PlanItem>,
        learningEvents: List<LearningEvent>,
        domainInput: DomainInput,
    ->
        BehaviorInput(
            todayPlans = todayPlans,
            recentPlans = recentPlans,
            learningEvents = learningEvents,
            domainInput = domainInput,
        )
    }

    private val activeAreaInstances = areaKernelRepository.observeActiveInstances()
    private val activeAreaSnapshots = areaKernelRepository.observeSnapshots(today)
    private val areaKernelInputs = combine(
        activeAreaInstances,
        activeAreaSnapshots,
    ) { areaInstances, areaSnapshots ->
        areaInstances to areaSnapshots
    }
    private val lifeAreaInputs = combine(
        lifeWheelRepository.observeDailyChecks(todayIso),
        lifeWheelRepository.observeActiveAreas(),
    ) { dailyChecks, activeAreas ->
        dailyChecks to activeAreas
    }

    private val homeInputs = combine(
        userFingerprintRepository.observe(),
        daySignals,
        behaviorInputs,
        lifeAreaInputs,
        areaKernelInputs,
    ) { fingerprint: UserFingerprint,
        signals: List<SignalEnvelope>,
        behavior: BehaviorInput,
        lifeAreaInputs: Pair<List<com.struperto.androidappdays.data.repository.LifeAreaDailyCheck>, List<LifeArea>>,
        areaKernelInputs: Pair<List<AreaInstance>, List<AreaSnapshot>>,
    ->
        val (dailyChecks, activeAreas) = lifeAreaInputs
        val (areaInstances, areaSnapshots) = areaKernelInputs
        HomeInput(
            fingerprint = fingerprint,
            signals = signals,
            todayPlans = behavior.todayPlans,
            recentPlans = behavior.recentPlans,
            learningEvents = behavior.learningEvents,
            goals = behavior.domainInput.goals,
            observations = behavior.domainInput.observations,
            hourSlotEntries = behavior.domainInput.hourSlotEntries,
            capabilityProfile = behavior.domainInput.capabilityProfile,
            dailyChecks = dailyChecks,
            lifeAreas = activeAreas,
            areaTodayOutputs = areaInstances.map { instance ->
                projectAreaTodayOutput(
                    AreaTodayOutputInput(
                        definition = com.struperto.androidappdays.domain.area.startAreaKernelDefinition(instance.definitionId),
                        blueprint = com.struperto.androidappdays.domain.area.startAreaKernelBlueprint(instance.definitionId),
                        instance = instance,
                        snapshot = areaSnapshots.firstOrNull { it.areaId == instance.areaId },
                        generatedAt = java.time.Instant.now(clock),
                        openPlanTitles = behavior.todayPlans
                            .filter { it.areaId == instance.areaId && !it.isDone }
                            .map { it.title },
                        dueCount = behavior.todayPlans.count { it.areaId == instance.areaId && !it.isDone },
                    ),
                )
            },
        )
    }

    val state = combine(
        homeInputs,
        feedbackMessage,
    ) { input, feedback ->
        val dayModel = dayModelEngine.project(
            com.struperto.androidappdays.data.repository.DayModelInput(
                userFingerprint = input.fingerprint,
                dateContext = DateContext(
                    date = today,
                    now = LocalTime.now(clock),
                    zoneId = clock.zone,
                ),
                plans = input.todayPlans,
                signals = input.signals,
                recentBehavior = RecentBehavior(
                    recentPlans = input.recentPlans,
                    learningEvents = input.learningEvents,
                ),
                overrides = input.learningEvents,
            ),
        )
        val evaluations = evaluationEngineV0.evaluate(
            goals = input.goals,
            observations = input.observations,
            capabilityProfile = input.capabilityProfile,
            logicalDate = today,
        )
        val projectedHints = homeDomainHintProjector.project(
            goals = input.goals,
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
        projectSingleHomeState(
            SingleHomeProjection(
                today = today,
                dayModel = dayModel,
                dailyDomains = projectedHints.dailyStrip.map(::toHomeDomainStripItem),
                segmentHints = projectedHints.segmentHints.mapValues { (_, hints) ->
                    hints.map(::toHomeDomainHint)
                },
                slotEntries = input.hourSlotEntries.associateBy(HourSlotEntry::segmentId),
                feedbackMessage = feedback,
                lifeAreas = input.lifeAreas
                    .ifEmpty {
                        input.fingerprint.lifeAreas.ifEmpty(::defaultLifeAreas)
                    }
                    .ifEmpty(::defaultLifeAreas)
                    .sortedBy(LifeArea::sortOrder),
                dailyChecks = input.dailyChecks,
                areaTodayOutputs = input.areaTodayOutputs,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyState(today),
    )

    fun clearFeedback() {
        feedbackMessage.value = null
    }

    fun refreshPassiveSignals() {
        passiveRefresh.value = passiveRefresh.value + 1
        viewModelScope.launch {
            observationSyncService.syncDay(today)
        }
    }

    fun setHourSlotStatus(
        segmentId: String,
        logicalHour: Int,
        windowId: String,
        status: HourSlotStatus,
    ) {
        viewModelScope.launch {
            hourSlotEntryRepository.saveStatus(
                logicalDate = today,
                segmentId = segmentId,
                logicalHour = logicalHour,
                windowId = windowId,
                status = status,
            )
            learningEventRepository.record(
                type = LearningEventType.HOUR_SLOT_STATUS_SET,
                title = "Stundenslot markiert",
                detail = "$segmentId -> ${status.name}",
            )
            feedbackMessage.value = hourSlotFeedback(status)
        }
    }

    fun saveHourSlotNote(
        segmentId: String,
        logicalHour: Int,
        windowId: String,
        note: String,
    ) {
        viewModelScope.launch {
            hourSlotEntryRepository.saveNote(
                logicalDate = today,
                segmentId = segmentId,
                logicalHour = logicalHour,
                windowId = windowId,
                note = note,
            )
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SingleHomeViewModel(
                    signalRepository = appContainer.signalRepository,
                    userFingerprintRepository = appContainer.userFingerprintRepository,
                    lifeWheelRepository = appContainer.lifeWheelRepository,
                    areaKernelRepository = appContainer.areaKernelRepository,
                    learningEventRepository = appContainer.learningEventRepository,
                    planRepository = appContainer.planRepository,
                    goalRepository = appContainer.goalRepository,
                    observationRepository = appContainer.observationRepository,
                    hourSlotEntryRepository = appContainer.hourSlotEntryRepository,
                    sourceCapabilityRepository = appContainer.sourceCapabilityRepository,
                    observationSyncService = appContainer.observationSyncService,
                    clock = appContainer.clock,
                    dayModelEngine = appContainer.dayModelEngine,
                    evaluationEngineV0 = appContainer.evaluationEngineV0,
                    homeDomainHintProjector = appContainer.homeDomainHintProjector,
                )
            }
        }
    }
}

private fun hourSlotFeedback(status: HourSlotStatus): String {
    return when (status) {
        HourSlotStatus.ON_TRACK -> "Stunde auf Spur markiert."
        HourSlotStatus.REDUCED -> "Stunde als reduziert markiert."
        HourSlotStatus.OPEN -> "Stunde als offen markiert."
        HourSlotStatus.UNKNOWN -> "Stundenstatus zurueckgesetzt."
    }
}

private data class HomeInput(
    val fingerprint: UserFingerprint,
    val signals: List<SignalEnvelope>,
    val todayPlans: List<PlanItem>,
    val recentPlans: List<PlanItem>,
    val learningEvents: List<LearningEvent>,
    val goals: List<DomainGoal>,
    val observations: List<DomainObservation>,
    val hourSlotEntries: List<HourSlotEntry>,
    val capabilityProfile: CapabilityProfile,
    val dailyChecks: List<com.struperto.androidappdays.data.repository.LifeAreaDailyCheck>,
    val lifeAreas: List<LifeArea>,
    val areaTodayOutputs: List<AreaTodayOutput>,
)

private data class DomainInput(
    val goals: List<DomainGoal>,
    val observations: List<DomainObservation>,
    val hourSlotEntries: List<HourSlotEntry>,
    val capabilityProfile: CapabilityProfile,
)

private data class BehaviorInput(
    val todayPlans: List<PlanItem>,
    val recentPlans: List<PlanItem>,
    val learningEvents: List<LearningEvent>,
    val domainInput: DomainInput,
)

private fun emptyState(today: LocalDate): SingleHomeState {
    return SingleHomeState(
        today = today,
        modeLabel = "Day OS",
        title = "Heute · $today",
        thesis = "Der Tag baut gerade sein Soll auf.",
        fitLabel = "anfällig",
        fitScore = 0.42f,
        topPriorities = emptyList(),
        risks = emptyList<HomeRiskCard>(),
        coachSuggestions = emptyList<HomeCoachCard>(),
        dailyDomains = emptyList(),
        segments = emptyList<HomeTimelineSegment>(),
        segmentDetails = emptyMap(),
        segmentHints = emptyMap(),
        feedbackMessage = null,
        lifeAreas = defaultLifeAreas(),
        dailyChecks = emptyMap(),
        areaDock = null,
    )
}

private fun toHomeDomainStripItem(summary: ProjectedHomeDomainSummary): HomeDomainStripItem {
    return HomeDomainStripItem(
        domain = summary.domain,
        state = summary.state,
        confidence = summary.confidence,
    )
}

private fun toHomeDomainHint(hint: ProjectedHomeDomainHint): HomeDomainHint {
    return HomeDomainHint(
        domain = hint.domain,
        state = hint.state,
    )
}
