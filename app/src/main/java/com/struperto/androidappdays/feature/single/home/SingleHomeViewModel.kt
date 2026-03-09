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
import com.struperto.androidappdays.data.repository.LearningEvent
import com.struperto.androidappdays.data.repository.LearningEventRepository
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.data.repository.LifeArea
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

enum class HomeQuickAddTarget(
    val label: String,
) {
    JETZT("Jetzt"),
    SPAETER("Später"),
}

@OptIn(ExperimentalCoroutinesApi::class)
class SingleHomeViewModel(
    private val signalRepository: SignalRepository,
    private val userFingerprintRepository: UserFingerprintRepository,
    private val learningEventRepository: LearningEventRepository,
    private val captureRepository: com.struperto.androidappdays.data.repository.CaptureRepository,
    private val planRepository: PlanRepository,
    private val vorhabenRepository: com.struperto.androidappdays.data.repository.VorhabenRepository,
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

    private val homeInputs = combine(
        userFingerprintRepository.observe(),
        daySignals,
        behaviorInputs,
    ) { fingerprint: UserFingerprint,
        signals: List<SignalEnvelope>,
        behavior: BehaviorInput,
    ->
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
                lifeAreas = input.fingerprint.lifeAreas
                    .ifEmpty(::defaultLifeAreas)
                    .sortedBy(LifeArea::sortOrder),
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

    fun submitQuickAdd(
        text: String,
        target: HomeQuickAddTarget,
    ) {
        val draft = text.trim()
        if (draft.isBlank()) return
        viewModelScope.launch {
            val areaId = resolveAreaId()
            when (target) {
                HomeQuickAddTarget.JETZT -> {
                    val window = HomeTrackWindow.fromLocalTime(LocalTime.now(clock))
                    planRepository.addManual(
                        title = titleFromText(draft),
                        note = noteFromText(draft),
                        areaId = areaId,
                        timeBlock = preferredTimeBlockForWindow(window, LocalTime.now(clock)),
                    )
                    learningEventRepository.record(
                        type = LearningEventType.QUICK_ADD_NOW,
                        title = "Quick Add in den Tag",
                        detail = draft,
                    )
                    feedbackMessage.value = "Liegt jetzt im ${window.feedbackLabel}."
                }
                HomeQuickAddTarget.SPAETER -> {
                    vorhabenRepository.create(
                        title = titleFromText(draft),
                        note = noteFromText(draft),
                        areaId = areaId,
                    )
                    learningEventRepository.record(
                        type = LearningEventType.QUICK_ADD_LATER,
                        title = "Quick Add fuer spaeter",
                        detail = draft,
                    )
                    feedbackMessage.value = "Für später vorgemerkt."
                }
            }
        }
    }

    fun captureToWindow(
        captureId: String,
        window: HomeTrackWindow,
    ) {
        viewModelScope.launch {
            val capture = captureRepository.loadById(captureId) ?: return@launch
            planRepository.addManual(
                title = titleFromText(capture.text),
                note = noteFromText(capture.text),
                areaId = capture.areaId ?: resolveAreaId(),
                timeBlock = preferredTimeBlockForWindow(window, LocalTime.now(clock)),
            )
            captureRepository.markConverted(captureId)
            learningEventRepository.record(
                type = LearningEventType.CAPTURE_TO_PLAN,
                title = "Signal in den Soll-Tag gezogen",
                detail = capture.text,
            )
            feedbackMessage.value = "Im ${window.feedbackLabel} eingeplant."
        }
    }

    fun captureToLater(captureId: String) {
        viewModelScope.launch {
            val capture = captureRepository.loadById(captureId) ?: return@launch
            vorhabenRepository.createFromCapture(
                captureId = captureId,
                title = titleFromText(capture.text),
                note = noteFromText(capture.text),
                areaId = capture.areaId ?: resolveAreaId(),
            )
            captureRepository.markConverted(captureId)
            learningEventRepository.record(
                type = LearningEventType.CAPTURE_TO_LATER,
                title = "Signal in Spaeter verschoben",
                detail = capture.text,
            )
            feedbackMessage.value = "Als späteres Thema abgelegt."
        }
    }

    fun completeCapture(captureId: String) {
        viewModelScope.launch {
            captureRepository.archive(captureId)
            feedbackMessage.value = "Signal abgeschlossen."
        }
    }

    fun vorhabenToWindow(
        vorhabenId: String,
        window: HomeTrackWindow,
    ) {
        viewModelScope.launch {
            val vorhaben = vorhabenRepository.loadById(vorhabenId) ?: return@launch
            planRepository.addFromVorhaben(
                vorhabenId = vorhabenId,
                timeBlock = preferredTimeBlockForWindow(window, LocalTime.now(clock)),
            )
            learningEventRepository.record(
                type = LearningEventType.LATER_TO_PLAN,
                title = "Spaeter-Thema aktiviert",
                detail = vorhaben.title,
            )
            feedbackMessage.value = "In ${window.feedbackLabel} gezogen."
        }
    }

    fun completeVorhaben(vorhabenId: String) {
        viewModelScope.launch {
            val vorhaben = vorhabenRepository.loadById(vorhabenId) ?: return@launch
            vorhabenRepository.archive(vorhabenId)
            learningEventRepository.record(
                type = LearningEventType.LATER_DONE,
                title = "Spaeter-Thema abgeschlossen",
                detail = vorhaben.title,
            )
            feedbackMessage.value = "Später-Eintrag abgeschlossen."
        }
    }

    fun togglePlanDone(planId: String) {
        viewModelScope.launch {
            val plan = planRepository.loadById(planId)
            planRepository.toggleDone(planId)
            learningEventRepository.record(
                type = LearningEventType.PLAN_TOGGLED,
                title = "Planstatus angepasst",
                detail = plan?.title.orEmpty(),
            )
            feedbackMessage.value = "Segment aktualisiert."
        }
    }

    fun movePlanToWindow(
        planId: String,
        window: HomeTrackWindow,
    ) {
        viewModelScope.launch {
            val plan = planRepository.loadById(planId)
            planRepository.moveToTimeBlock(
                id = planId,
                timeBlock = preferredTimeBlockForWindow(window, LocalTime.now(clock)),
            )
            learningEventRepository.record(
                type = LearningEventType.PLAN_MOVED,
                title = "Plan in neuen Block verschoben",
                detail = plan?.title.orEmpty(),
            )
            feedbackMessage.value = "In ${window.feedbackLabel} verschoben."
        }
    }

    private suspend fun resolveAreaId(): String {
        return userFingerprintRepository.load()
            .lifeAreas
            .ifEmpty(::defaultLifeAreas)
            .first()
            .id
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SingleHomeViewModel(
                    signalRepository = appContainer.signalRepository,
                    userFingerprintRepository = appContainer.userFingerprintRepository,
                    learningEventRepository = appContainer.learningEventRepository,
                    captureRepository = appContainer.captureRepository,
                    planRepository = appContainer.planRepository,
                    vorhabenRepository = appContainer.vorhabenRepository,
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

private fun titleFromText(text: String): String {
    return text.lineSequence()
        .firstOrNull()
        .orEmpty()
        .trim()
        .take(60)
}

private fun noteFromText(text: String): String {
    val trimmed = text.trim()
    val title = titleFromText(trimmed)
    return if (trimmed == title) "" else trimmed
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
