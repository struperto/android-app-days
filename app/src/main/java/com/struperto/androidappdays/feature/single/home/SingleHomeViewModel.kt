package com.struperto.androidappdays.feature.single.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.feature.content.AreaContentFeedState
import com.struperto.androidappdays.feature.content.AreaContentItem
import com.struperto.androidappdays.feature.content.AreaContentRuntimeRepository
import com.struperto.androidappdays.feature.content.AreaContentState
import com.struperto.androidappdays.feature.content.AreaContentKind
import com.struperto.androidappdays.feature.content.AreaContentPlatform
import com.struperto.androidappdays.data.repository.DateContext
import com.struperto.androidappdays.data.repository.GoalRepository
import com.struperto.androidappdays.data.repository.HourSlotEntryRepository
import com.struperto.androidappdays.data.repository.AreaSourceBinding
import com.struperto.androidappdays.data.repository.AreaSourceBindingRepository
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
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
import com.struperto.androidappdays.domain.area.projectCalendarAreaTodayOutput
import com.struperto.androidappdays.domain.area.projectAreaTodayOutput
import com.struperto.androidappdays.domain.area.resolveCalendarAreaSlice
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
    private val areaSourceBindingRepository: AreaSourceBindingRepository,
    private val calendarSignalRepository: CalendarSignalRepository,
    private val learningEventRepository: LearningEventRepository,
    private val planRepository: PlanRepository,
    private val goalRepository: GoalRepository,
    private val observationRepository: ObservationRepository,
    private val hourSlotEntryRepository: HourSlotEntryRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val observationSyncService: ObservationSyncService,
    private val areaContentRuntimeRepository: AreaContentRuntimeRepository,
    private val newsRuntimeRepository: NewsRuntimeRepository,
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
    private val areaSourceBindings = areaSourceBindingRepository.observeAll()
    private val areaCalendarSignals = calendarSignalRepository.observeToday(today, clock.zone)
    private val areaKernelInputs = combine(
        activeAreaInstances,
        activeAreaSnapshots,
    ) { areaInstances, areaSnapshots ->
        areaInstances to areaSnapshots
    }
    private val areaSourceInputs = combine(
        areaKernelInputs,
        areaSourceBindings,
        areaCalendarSignals,
    ) { kernelInputs, bindings, calendarSignals ->
        Triple(kernelInputs, bindings, calendarSignals)
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
        areaSourceInputs,
    ) { fingerprint: UserFingerprint,
        signals: List<SignalEnvelope>,
        behavior: BehaviorInput,
        lifeAreaInputs: Pair<List<com.struperto.androidappdays.data.repository.LifeAreaDailyCheck>, List<LifeArea>>,
        areaSourceInputs: Triple<
            Pair<List<AreaInstance>, List<AreaSnapshot>>,
            List<AreaSourceBinding>,
            List<com.struperto.androidappdays.data.repository.CalendarSignal>,
            >,
    ->
        val (dailyChecks, activeAreas) = lifeAreaInputs
        val (areaKernelInputs, areaBindings, calendarSignals) = areaSourceInputs
        val (areaInstances, areaSnapshots) = areaKernelInputs
        val projectionTime = java.time.Instant.now(clock)
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
            areaInstances = areaInstances,
            areaTodayOutputs = areaInstances.map { instance ->
                val areaPlans = behavior.todayPlans.filter { it.areaId == instance.areaId && !it.isDone }
                val baseOutput = projectAreaTodayOutput(
                    AreaTodayOutputInput(
                        definition = com.struperto.androidappdays.domain.area.startAreaKernelDefinition(instance.definitionId),
                        blueprint = com.struperto.androidappdays.domain.area.startAreaKernelBlueprint(instance.definitionId),
                        instance = instance,
                        snapshot = areaSnapshots.firstOrNull { it.areaId == instance.areaId },
                        generatedAt = projectionTime,
                        openPlanTitles = areaPlans.map { it.title },
                        dueCount = areaPlans.size,
                    ),
                )
                val calendarSlice = resolveCalendarAreaSlice(
                    title = instance.title,
                    summary = instance.summary,
                    iconKey = instance.iconKey,
                    templateId = instance.templateId,
                    behaviorClass = baseOutput.behaviorClass,
                    boundSources = areaBindings
                        .filter { binding -> binding.areaId == instance.areaId }
                        .mapTo(linkedSetOf()) { binding -> binding.source },
                    capabilityProfile = behavior.domainInput.capabilityProfile,
                    calendarSignals = calendarSignals,
                )
                if (calendarSlice != null) {
                    projectCalendarAreaTodayOutput(
                        baseOutput = baseOutput,
                        areaTitle = instance.title,
                        slice = calendarSlice,
                        generatedAt = projectionTime,
                        zoneId = clock.zone,
                    )
                } else {
                    baseOutput
                }
            },
        )
    }

    val state = combine(
        homeInputs,
        feedbackMessage,
        areaContentRuntimeRepository.state,
        newsRuntimeRepository.state,
    ) { input, feedback, contentState, newsState ->
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
        ).let { baseState ->
            val mergedFeeds = mergeAreaFeeds(
                primaryFeeds = contentState.feeds,
                newsFeed = newsState.toAreaFeedState(),
            )
            val selectedFeed = selectSingleAreaFeed(
                activeAreas = input.areaInstances,
                areaDockInstanceId = baseState.areaDock?.instanceId,
                feeds = mergedFeeds,
            )
            baseState.copy(
                areaFeedAreaId = selectedFeed?.areaId,
                areaFeedTitle = selectedFeed?.areaTitle ?: "Bereichsfeed",
                areaFeedStatusLabel = selectedFeed?.statusLabel.orEmpty(),
                areaFeedStatusDetail = selectedFeed?.statusDetail.orEmpty(),
                areaFeedItems = selectedFeed?.items.orEmpty(),
            )
        }
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
            areaContentRuntimeRepository.refreshNow()
            newsRuntimeRepository.refreshNow()
        }
    }

    fun markAreaContentRead(itemId: String) {
        viewModelScope.launch {
            when {
                itemId.startsWith("news:") -> newsRuntimeRepository.markArticleRead(itemId.removePrefix("news:"))
                areaContentRuntimeRepository.itemById(itemId) != null -> areaContentRuntimeRepository.markItemRead(itemId)
                else -> newsRuntimeRepository.markArticleRead(itemId)
            }
        }
    }

    fun ensureAreaContent(itemId: String) {
        viewModelScope.launch {
            when {
                itemId.startsWith("news:") -> newsRuntimeRepository.ensureArticleContent(itemId.removePrefix("news:"))
                areaContentRuntimeRepository.itemById(itemId) != null -> areaContentRuntimeRepository.ensureItemContent(itemId)
                else -> newsRuntimeRepository.ensureArticleContent(itemId)
            }
        }
    }

    fun findAreaContentItem(itemId: String): AreaContentItem? {
        return areaContentRuntimeRepository.itemById(itemId)
            ?: newsRuntimeRepository.articleById(itemId.removePrefix("news:"))?.toAreaContentItem(
                areaId = state.value.areaFeedAreaId,
                areaTitle = state.value.areaFeedTitle,
            )
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
                    areaSourceBindingRepository = appContainer.areaSourceBindingRepository,
                    calendarSignalRepository = appContainer.calendarSignalRepository,
                    learningEventRepository = appContainer.learningEventRepository,
                    planRepository = appContainer.planRepository,
                    goalRepository = appContainer.goalRepository,
                    observationRepository = appContainer.observationRepository,
                    hourSlotEntryRepository = appContainer.hourSlotEntryRepository,
                    sourceCapabilityRepository = appContainer.sourceCapabilityRepository,
                    observationSyncService = appContainer.observationSyncService,
                    areaContentRuntimeRepository = appContainer.areaContentRuntimeRepository,
                    newsRuntimeRepository = appContainer.newsRuntimeRepository,
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

private fun selectSingleAreaFeed(
    activeAreas: List<AreaInstance>,
    areaDockInstanceId: String?,
    feeds: Map<String, AreaContentFeedState>,
): AreaContentFeedState? {
    areaDockInstanceId
        ?.let(feeds::get)
        ?.takeIf { it.items.isNotEmpty() }
        ?.let { return it }
    activeAreas
        .sortedBy(AreaInstance::sortOrder)
        .mapNotNull { area -> feeds[area.areaId] }
        .firstOrNull { it.items.isNotEmpty() }
        ?.let { return it }
    return feeds.values
        .filter { it.items.isNotEmpty() }
        .maxByOrNull { feed -> feed.items.maxOfOrNull { it.publishedAtMillis ?: Long.MIN_VALUE } ?: Long.MIN_VALUE }
}

private fun mergeAreaFeeds(
    primaryFeeds: Map<String, AreaContentFeedState>,
    newsFeed: AreaContentFeedState?,
): Map<String, AreaContentFeedState> {
    if (newsFeed == null) return primaryFeeds
    val merged = linkedMapOf<String, AreaContentFeedState>()
    merged.putAll(primaryFeeds)
    val existing = merged[newsFeed.areaId]
    merged[newsFeed.areaId] = if (existing == null) {
        newsFeed
    } else {
        val items = (existing.items + newsFeed.items)
            .distinctBy(AreaContentItem::contentUrl)
            .sortedWith(
                compareByDescending<AreaContentItem> { it.publishedAtMillis ?: Long.MIN_VALUE }
                    .thenBy { it.sourceLabel.lowercase() }
                    .thenBy { it.title.lowercase() },
            )
        existing.copy(
            statusLabel = "${items.size} Inhalt${if (items.size == 1) "" else "e"}",
            statusDetail = "News-Quellen, Posts und Bilder laufen hier als gemeinsamer Bereichsfeed zusammen.",
            items = items,
        )
    }
    return merged
}

private fun NewsRuntimeState.toAreaFeedState(): AreaContentFeedState? {
    val resolvedAreaId = areaId ?: return null
    val mappedItems = articles.map { article ->
        article.toAreaContentItem(
            areaId = resolvedAreaId,
            areaTitle = areaTitle,
        )
    }
    return AreaContentFeedState(
        areaId = resolvedAreaId,
        areaTitle = areaTitle,
        statusLabel = statusLabel,
        statusDetail = statusDetail,
        items = mappedItems,
    )
}

private fun NewsArticle.toAreaContentItem(
    areaId: String?,
    areaTitle: String,
): AreaContentItem {
    return AreaContentItem(
        id = "news:$id",
        areaId = areaId.orEmpty(),
        title = title,
        sourceLabel = sourceLabel,
        sourceUrl = sourceUrl,
        contentUrl = articleUrl,
        summary = summary,
        body = body,
        publishedLabel = publishedLabel,
        publishedAtMillis = publishedAtMillis,
        kind = AreaContentKind.Article,
        platform = AreaContentPlatform.Web,
        creatorLabel = "",
        contentState = when (contentState) {
            NewsArticleContentState.Pending -> AreaContentState.Pending
            NewsArticleContentState.Loading -> AreaContentState.Loading
            NewsArticleContentState.Ready -> AreaContentState.Ready
            NewsArticleContentState.AnalysisNeeded -> AreaContentState.AnalysisNeeded
        },
        contentDetail = contentDetail.ifBlank { "$areaTitle liefert einen lesbaren Artikel." },
    )
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
    val areaInstances: List<AreaInstance>,
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
        areaFeedAreaId = null,
        areaFeedTitle = "Bereichsfeed",
        areaFeedStatusLabel = "Feed wird vorbereitet",
        areaFeedStatusDetail = "Sobald ein Bereich verwertbare Signale hat, erscheinen sie hier als priorisierte Kacheln.",
        areaFeedItems = emptyList(),
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
