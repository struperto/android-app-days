package com.struperto.androidappdays.feature.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaSourceBinding
import com.struperto.androidappdays.data.repository.AreaSourceBindingRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.data.repository.HealthConnectRepository
import com.struperto.androidappdays.data.repository.NotificationSignal
import com.struperto.androidappdays.data.repository.NotificationSignalRepository
import com.struperto.androidappdays.data.repository.PlanRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceKind
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private object NoopStartCaptureRepository : CaptureRepository {
    override fun observeOpen() = flowOf(emptyList<com.struperto.androidappdays.data.repository.CaptureItem>())

    override fun observeArchived() = flowOf(emptyList<com.struperto.androidappdays.data.repository.CaptureItem>())

    override fun observeWriteCountSince(sinceEpochMillis: Long) = flowOf(0)

    override fun observeTouchedAreaIdsSince(sinceEpochMillis: Long) = flowOf(emptySet<String>())

    override suspend fun createTextCapture(
        text: String,
        areaId: String?,
    ) = throw UnsupportedOperationException("NoopStartCaptureRepository does not persist captures.")

    override suspend fun markConverted(id: String) = Unit

    override suspend fun archive(id: String) = Unit

    override suspend fun updateArea(
        id: String,
        areaId: String?,
    ) = Unit

    override suspend fun loadLatestOpen() = null

    override suspend fun loadById(id: String) = null
}

private object NoopStartAreaWebFeedSourceRepository : AreaWebFeedSourceRepository {
    override fun observeAll() = flowOf(emptyList<AreaWebFeedSource>())

    override fun observeByArea(areaId: String) = flowOf(emptyList<AreaWebFeedSource>())

    override suspend fun loadAll() = emptyList<AreaWebFeedSource>()

    override suspend fun loadByArea(areaId: String) = emptyList<AreaWebFeedSource>()

    override suspend fun save(
        areaId: String,
        url: String,
        sourceKind: AreaWebFeedSourceKind,
        isAutoSyncEnabled: Boolean,
        syncCadence: AreaWebFeedSyncCadence,
    ) = Unit

    override suspend fun remove(
        areaId: String,
        url: String,
    ) = Unit

    override suspend fun clearArea(areaId: String) = Unit

    override suspend fun updateSyncResult(
        areaId: String,
        url: String,
        syncedAt: Long,
        statusLabel: String,
        statusDetail: String,
    ) = Unit

    override suspend fun setAutoSyncEnabled(
        areaId: String,
        url: String,
        enabled: Boolean,
    ) = Unit

    override suspend fun setSyncCadence(
        areaId: String,
        url: String,
        cadence: AreaWebFeedSyncCadence,
    ) = Unit
}

class StartViewModel(
    private val areaKernelRepository: AreaKernelRepository,
    private val areaSourceBindingRepository: AreaSourceBindingRepository,
    private val planRepository: PlanRepository,
    private val captureRepository: CaptureRepository = NoopStartCaptureRepository,
    private val areaWebFeedSourceRepository: AreaWebFeedSourceRepository = NoopStartAreaWebFeedSourceRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val calendarSignalRepository: CalendarSignalRepository,
    private val notificationSignalRepository: NotificationSignalRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val clock: Clock,
) : ViewModel() {
    private val today: LocalDate
        get() = LocalDate.now(clock)

    private val todayIso: String
        get() = today.toString()

    private val zoneId
        get() = clock.zone

    private val activeInstances = areaKernelRepository.observeActiveInstances().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val baseOverviewState = combine(
        activeInstances,
        areaKernelRepository.observeSnapshots(LocalDate.parse(todayIso)),
        planRepository.observeToday(todayIso),
    ) { instances, snapshots, todayPlans ->
        projectStartOverviewState(
            inputs = buildStartOverviewKernelInputsFromKernel(
                instances = instances,
                snapshots = snapshots,
                todayPlans = todayPlans,
                logicalDate = today,
                projectionTime = Instant.now(clock),
            ),
        )
    }

    private val overviewSupportInputs = combine(
        areaSourceBindingRepository.observeAll(),
        captureRepository.observeOpen(),
        areaWebFeedSourceRepository.observeAll(),
        sourceCapabilityRepository.observeProfile(),
    ) { bindings, captures, webFeedSources, capabilityProfile ->
        OverviewSupportInputs(
            bindings = bindings,
            captures = captures,
            webFeedSources = webFeedSources,
            capabilityProfile = capabilityProfile,
        )
    }

    val state = combine(
        baseOverviewState,
        overviewSupportInputs,
        calendarSignalRepository.observeToday(today, zoneId),
        notificationSignalRepository.observeToday(today, zoneId),
    ) { baseState, supportInputs, calendarSignals, notificationSignals ->
        val bindings = supportInputs.bindings
        val captures = supportInputs.captures
        val webFeedSources = supportInputs.webFeedSources
        val capabilityProfile = supportInputs.capabilityProfile
        val healthObservations = if (capabilityProfile.isUsable(DataSourceKind.HEALTH_CONNECT)) {
            healthConnectRepository.readDailyObservations(today)
        } else {
            emptyList()
        }
        val pendingImports = captures
            .asSequence()
            .filter { it.areaId == null }
            .mapNotNull { capture ->
                parseAreaImportCapture(capture)?.let { parsed ->
                    capture.updatedAt to StartPendingImportState(
                        id = parsed.id,
                        kind = parsed.kind,
                        title = parsed.title,
                        detail = parsed.detail,
                        reference = parsed.reference,
                        suggestedInput = buildImportedMaterialPrompt(parsed),
                    )
                }
            }
            .sortedByDescending { it.first }
            .map { it.second }
            .toList()
        overlayOverviewWithContentSources(
            state = overlayOverviewWithSources(
                state = baseState,
                bindings = bindings,
                capabilityProfile = capabilityProfile,
                calendarSignals = calendarSignals,
                notificationSignals = notificationSignals,
                healthObservations = healthObservations,
                zoneId = zoneId,
            ),
            captures = captures,
            webFeedSources = webFeedSources,
        ).copy(
            pendingImports = pendingImports,
            capabilityProfile = capabilityProfile,
            activeAreaCount = baseState.areas.size,
            maxActiveAreaCount = MaxActiveAreas,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StartOverviewState(),
    )

    fun createArea(
        title: String,
        meaning: String,
        templateId: String,
        iconKey: String,
        behaviorClass: com.struperto.androidappdays.domain.area.AreaBehaviorClass,
        sourceKind: DataSourceKind?,
        onCreated: (String) -> Unit,
    ) {
        if (activeInstances.value.size >= MaxActiveAreas) return
        viewModelScope.launch {
            val areaId = areaKernelRepository.createActiveInstance(
                CreateAreaInstanceDraft(
                    title = title,
                    summary = meaning,
                    templateId = templateId,
                    iconKey = iconKey,
                    behaviorClass = behaviorClass,
                ),
            )
            sourceKind?.let { source ->
                areaSourceBindingRepository.bind(
                    areaId = areaId.areaId,
                    source = source,
                )
            }
            onCreated(areaId.areaId)
        }
    }

    fun deleteArea(areaId: String) {
        viewModelScope.launch {
            areaSourceBindingRepository.clearArea(areaId)
            areaKernelRepository.deleteActiveInstance(areaId)
        }
    }

    fun attachPendingImport(
        importId: String,
        areaId: String,
    ) {
        viewModelScope.launch {
            captureRepository.updateArea(
                id = importId,
                areaId = areaId,
            )
        }
    }

    fun dismissPendingImport(importId: String) {
        viewModelScope.launch {
            captureRepository.archive(importId)
        }
    }

    fun updateAreaIdentity(
        areaId: String,
        title: String,
        meaning: String,
        templateId: String,
        iconKey: String,
    ) {
        val current = activeInstances.value.firstOrNull { it.areaId == areaId } ?: return
        viewModelScope.launch {
            areaKernelRepository.updateActiveInstance(
                current.copy(
                    title = title.trim(),
                    summary = meaning.trim(),
                    templateId = templateId,
                    iconKey = iconKey,
                ),
            )
        }
    }

    fun swapAreas(
        firstAreaId: String,
        secondAreaId: String,
    ) {
        viewModelScope.launch {
            areaKernelRepository.swapActiveInstanceOrder(
                firstAreaId = firstAreaId,
                secondAreaId = secondAreaId,
            )
        }
    }

    fun moveAreaEarlier(areaId: String) {
        viewModelScope.launch {
            areaKernelRepository.moveActiveInstanceEarlier(areaId)
        }
    }

    fun moveAreaLater(areaId: String) {
        viewModelScope.launch {
            areaKernelRepository.moveActiveInstanceLater(areaId)
        }
    }

    companion object {
        private const val MaxActiveAreas = 16

        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StartViewModel(
                    areaKernelRepository = appContainer.areaKernelRepository,
                    areaSourceBindingRepository = appContainer.areaSourceBindingRepository,
                    planRepository = appContainer.planRepository,
                    captureRepository = appContainer.captureRepository,
                    areaWebFeedSourceRepository = appContainer.areaWebFeedSourceRepository,
                    sourceCapabilityRepository = appContainer.sourceCapabilityRepository,
                    calendarSignalRepository = appContainer.calendarSignalRepository,
                    notificationSignalRepository = appContainer.notificationSignalRepository,
                    healthConnectRepository = appContainer.healthConnectRepository,
                    clock = appContainer.clock,
                )
            }
        }
    }
}

private data class OverviewSupportInputs(
    val bindings: List<AreaSourceBinding>,
    val captures: List<CaptureItem>,
    val webFeedSources: List<com.struperto.androidappdays.data.repository.AreaWebFeedSource>,
    val capabilityProfile: CapabilityProfile,
)
