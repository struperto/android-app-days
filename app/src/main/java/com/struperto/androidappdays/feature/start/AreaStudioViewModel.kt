package com.struperto.androidappdays.feature.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaSourceBindingRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.data.repository.HealthConnectRepository
import com.struperto.androidappdays.data.repository.NotificationSignalRepository
import com.struperto.androidappdays.data.repository.PlanRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaDirectionMode
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.AreaSourcesMode
import com.struperto.androidappdays.domain.area.AreaVisibilityLevel
import com.struperto.androidappdays.domain.area.withUpdatedIdentity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class AreaStudioAreaState(
    val detail: StartAreaDetailState,
    val authoring: AreaAuthoringStudioState,
    val sourceSetup: AreaSourceSetupState? = null,
    val importedMaterials: List<AreaImportedMaterialState> = emptyList(),
    val webFeedSync: AreaWebFeedSyncState = AreaWebFeedSyncState(),
    val analysis: AreaMachineAnalysisState = AreaMachineAnalysisState(),
)

data class AreaStudioUiState(
    val areas: Map<String, AreaStudioAreaState> = emptyMap(),
)

data class AreaWebFeedSourceState(
    val url: String,
    val hostLabel: String,
    val sourceKindLabel: String,
    val autoSyncEnabled: Boolean,
    val syncCadenceLabel: String,
    val capabilityLabels: List<String>,
    val lastStatusLabel: String,
    val lastStatusDetail: String,
)

data class AreaWebFeedSyncState(
    val isRunning: Boolean = false,
    val statusLabel: String = "",
    val statusDetail: String = "",
    val sources: List<AreaWebFeedSourceState> = emptyList(),
)

private data class AreaPendingImportState(
    val localId: String,
    val material: AreaImportedMaterialState,
)

private object NoopCaptureRepository : CaptureRepository {
    override fun observeOpen() = kotlinx.coroutines.flow.flowOf(emptyList<com.struperto.androidappdays.data.repository.CaptureItem>())

    override fun observeArchived() = kotlinx.coroutines.flow.flowOf(emptyList<com.struperto.androidappdays.data.repository.CaptureItem>())

    override fun observeWriteCountSince(sinceEpochMillis: Long) = kotlinx.coroutines.flow.flowOf(0)

    override fun observeTouchedAreaIdsSince(sinceEpochMillis: Long) = kotlinx.coroutines.flow.flowOf(emptySet<String>())

    override suspend fun createTextCapture(
        text: String,
        areaId: String?,
    ) = throw UnsupportedOperationException("NoopCaptureRepository does not persist captures.")

    override suspend fun markConverted(id: String) = Unit

    override suspend fun archive(id: String) = Unit

    override suspend fun updateArea(
        id: String,
        areaId: String?,
    ) = Unit

    override suspend fun loadLatestOpen() = null

    override suspend fun loadById(id: String) = null
}

private object NoopAreaWebFeedSourceRepository : AreaWebFeedSourceRepository {
    override fun observeAll() = kotlinx.coroutines.flow.flowOf(emptyList<AreaWebFeedSource>())

    override fun observeByArea(areaId: String) = kotlinx.coroutines.flow.flowOf(emptyList<AreaWebFeedSource>())

    override suspend fun loadAll() = emptyList<AreaWebFeedSource>()

    override suspend fun loadByArea(areaId: String) = emptyList<AreaWebFeedSource>()

    override suspend fun save(
        areaId: String,
        url: String,
        sourceKind: AreaWebFeedSourceKind,
        isAutoSyncEnabled: Boolean,
        syncCadence: AreaWebFeedSyncCadence,
    ) = Unit

    override suspend fun remove(areaId: String, url: String) = Unit

    override suspend fun clearArea(areaId: String) = Unit

    override suspend fun updateSyncResult(
        areaId: String,
        url: String,
        syncedAt: Long,
        statusLabel: String,
        statusDetail: String,
    ) = Unit

    override suspend fun setAutoSyncEnabled(areaId: String, url: String, enabled: Boolean) = Unit

    override suspend fun setSyncCadence(areaId: String, url: String, cadence: AreaWebFeedSyncCadence) = Unit
}

private object NoopWebFeedSyncCoordinator : WebFeedSyncCoordinator {
    override suspend fun syncArea(areaId: String): AreaWebFeedSyncRunResult {
        return AreaWebFeedSyncRunResult(
            newItemCount = 0,
            message = "Feed-Sync ist hier noch nicht verbunden.",
        )
    }

    override suspend fun syncAll(): Int = 0

    override suspend fun ensureScheduled() = Unit
}

private data class AreaStudioProjectedInput(
    val projectedAreas: Map<String, Pair<StartAreaDetailState, AreaAuthoringStudioState>>,
    val bindings: List<com.struperto.androidappdays.data.repository.AreaSourceBinding>,
    val captures: List<com.struperto.androidappdays.data.repository.CaptureItem>,
    val webFeedSources: List<AreaWebFeedSource>,
    val capabilityProfile: com.struperto.androidappdays.domain.CapabilityProfile,
)

private data class AreaStudioSignalInput(
    val input: AreaStudioProjectedInput,
    val calendarSignals: List<com.struperto.androidappdays.data.repository.CalendarSignal>,
    val notificationSignals: List<com.struperto.androidappdays.data.repository.NotificationSignal>,
    val healthObservations: List<com.struperto.androidappdays.domain.DomainObservation>,
)

class AreaStudioViewModel(
    private val areaKernelRepository: AreaKernelRepository,
    private val areaSourceBindingRepository: AreaSourceBindingRepository,
    private val planRepository: PlanRepository,
    private val captureRepository: CaptureRepository = NoopCaptureRepository,
    private val areaWebFeedSourceRepository: AreaWebFeedSourceRepository = NoopAreaWebFeedSourceRepository,
    private val webFeedConnector: WebFeedConnector = LocalWebFeedConnector(),
    private val webFeedSyncCoordinator: WebFeedSyncCoordinator = NoopWebFeedSyncCoordinator,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val calendarSignalRepository: CalendarSignalRepository,
    private val notificationSignalRepository: NotificationSignalRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val zoneId = clock.zone
    private val activeInstances = areaKernelRepository.observeActiveInstances().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
    private val todaySnapshots = areaKernelRepository.observeSnapshots(today).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
    private val todayPlans = planRepository.observeToday(today.toString()).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val baseState = combine(
        activeInstances,
        todaySnapshots,
        todayPlans,
    ) { instances, snapshots, plans ->
        val snapshotMap = snapshots.associateBy(AreaSnapshot::areaId)
        instances
            .sortedBy(AreaInstance::sortOrder)
            .associate { instance ->
                val detailInput = buildStartAreaDetailKernelInput(
                    instance = instance,
                    snapshot = snapshotMap[instance.areaId],
                    todayPlans = plans,
                    logicalDate = today,
                    projectionTime = Instant.now(clock),
                )
                instance.areaId to Pair(
                    projectStartAreaDetailState(detailInput),
                    projectAreaAuthoringStudioState(
                        AreaAuthoringProjectionInput(
                            definition = detailInput.definition,
                            blueprint = detailInput.blueprint,
                            instance = instance,
                            authoringConfig = instance.authoringConfig,
                        ),
                    ),
                )
            }
    }

    private val projectedInput = combine(
        baseState,
        areaSourceBindingRepository.observeAll(),
        captureRepository.observeOpen(),
        areaWebFeedSourceRepository.observeAll(),
        sourceCapabilityRepository.observeProfile(),
    ) { projectedAreas, bindings, captures, webFeedSources, capabilityProfile ->
        AreaStudioProjectedInput(
            projectedAreas = projectedAreas,
            bindings = bindings,
            captures = captures,
            webFeedSources = webFeedSources,
            capabilityProfile = capabilityProfile,
        )
    }

    private val webFeedSyncState = MutableStateFlow<Map<String, AreaWebFeedSyncState>>(emptyMap())
    private val analysisRuntimeState = MutableStateFlow<Map<String, AreaMachineAnalysisRuntime>>(emptyMap())
    private val pendingImportsState = MutableStateFlow<Map<String, List<AreaPendingImportState>>>(emptyMap())
    private val healthObservationsState = sourceCapabilityRepository.observeProfile()
        .map { capabilityProfile ->
            if (capabilityProfile.isUsable(com.struperto.androidappdays.domain.DataSourceKind.HEALTH_CONNECT)) {
                healthConnectRepository.readDailyObservations(today)
            } else {
                emptyList()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
    private val projectedSignals = combine(
        projectedInput,
        calendarSignalRepository.observeToday(today, zoneId),
        notificationSignalRepository.observeToday(today, zoneId),
        healthObservationsState,
    ) { input, calendarSignals, notificationSignals, healthObservations ->
        AreaStudioSignalInput(
            input = input,
            calendarSignals = calendarSignals,
            notificationSignals = notificationSignals,
            healthObservations = healthObservations,
        )
    }

    val state = combine(
        projectedSignals,
        webFeedSyncState,
        analysisRuntimeState,
        pendingImportsState,
    ) { signalInput, webFeedState, analysisState, pendingImports ->
        val input = signalInput.input
        val projectedAreas = input.projectedAreas
        val bindings = input.bindings
        val captures = input.captures
        val webFeedSources = input.webFeedSources
        val capabilityProfile = input.capabilityProfile
        val calendarSignals = signalInput.calendarSignals
        val notificationSignals = signalInput.notificationSignals
        val healthObservations = signalInput.healthObservations
        val bindingsByAreaId = bindings.groupBy { it.areaId }
        val importsByAreaId = captures
            .mapNotNull { capture ->
                parseAreaImportCapture(capture)?.let { parsed ->
                    capture.areaId?.let { areaId -> areaId to parsed }
                }
            }.groupBy(
                keySelector = { it.first },
                valueTransform = { it.second },
            )
        val webFeedSourcesByAreaId = webFeedSources.groupBy(AreaWebFeedSource::areaId)
        AreaStudioUiState(
            areas = projectedAreas.mapValues { (areaId, projected) ->
                val (baseDetail, authoring) = projected
                val areaBindings = bindingsByAreaId[areaId].orEmpty()
                val detail = overlayDetailWithSources(
                    detail = baseDetail,
                    bindings = areaBindings,
                    capabilityProfile = capabilityProfile,
                    calendarSignals = calendarSignals,
                    notificationSignals = notificationSignals,
                    healthObservations = healthObservations,
                    zoneId = zoneId,
                )
                AreaStudioAreaState(
                    detail = detail,
                    authoring = authoring,
                    sourceSetup = buildAreaSourceSetupState(
                        detail = detail,
                        bindings = areaBindings,
                        capabilityProfile = capabilityProfile,
                        calendarSignals = calendarSignals,
                        notificationSignals = notificationSignals,
                        healthObservations = healthObservations,
                        zoneId = zoneId,
                    ).also { sourceSetup ->
                        Unit
                    },
                    importedMaterials = mergeImportedMaterials(
                        actual = importsByAreaId[areaId].orEmpty(),
                        pending = pendingImports[areaId].orEmpty().map(AreaPendingImportState::material),
                    ),
                    webFeedSync = (webFeedState[areaId] ?: AreaWebFeedSyncState()).copy(
                        sources = webFeedSourcesByAreaId[areaId]
                            .orEmpty()
                            .map(AreaWebFeedSource::toAreaState),
                    ),
                ).let { areaState ->
                    val runtime = analysisState[areaId] ?: AreaMachineAnalysisRuntime()
                    areaState.copy(
                        analysis = projectAreaMachineAnalysis(
                            area = detail,
                            sourceSetup = areaState.sourceSetup,
                            importedMaterials = areaState.importedMaterials,
                            webFeedSync = areaState.webFeedSync,
                            runtime = runtime,
                            zoneId = zoneId,
                        ),
                    )
                }
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AreaStudioUiState(),
    )

    fun setTargetScore(
        areaId: String,
        value: Float,
    ) {
        val current = currentInstance(areaId) ?: return
        viewModelScope.launch {
            areaKernelRepository.updateActiveInstance(
                current.copy(
                    targetScore = value.roundToInt().coerceIn(1, 5),
                ),
            )
        }
    }

    fun addImportedLink(
        areaId: String,
        url: String,
    ) {
        val trimmed = url.trim()
        if (trimmed.length < 4) return
        val (title, detail) = buildLinkImportState(trimmed)
        val pendingId = enqueuePendingImport(
            areaId = areaId,
            draft = AreaImportDraft(
                kind = AreaImportKind.Link,
                title = title,
                detail = detail,
                reference = trimmed,
            ),
        )
        viewModelScope.launch {
            runCatching {
                captureRepository.createTextCapture(
                    text = buildAreaImportCaptureText(
                        kind = AreaImportKind.Link,
                        title = title,
                        detail = detail,
                        reference = trimmed,
                    ),
                    areaId = areaId,
                )
            }
            clearPendingImport(areaId, pendingId)
        }
    }

    fun addImportedMaterials(
        areaId: String,
        imports: List<AreaImportDraft>,
    ) {
        val pendingImports = imports.associateWith { draft ->
            enqueuePendingImport(areaId = areaId, draft = draft)
        }
        viewModelScope.launch {
            imports.forEach { item ->
                runCatching {
                    captureRepository.createTextCapture(
                        text = buildAreaImportCaptureText(
                            kind = item.kind,
                            title = item.title,
                            detail = item.detail,
                            reference = item.reference,
                        ),
                        areaId = areaId,
                    )
                }
                clearPendingImport(areaId, pendingImports.getValue(item))
            }
        }
    }

    fun addWebFeedSource(
        areaId: String,
        url: String,
    ) {
        val trimmed = url.trim()
        if (!trimmed.startsWith("http")) return
        val sourceKind = inferWebFeedSourceKind(trimmed)
        viewModelScope.launch {
            areaWebFeedSourceRepository.save(
                areaId = areaId,
                url = trimmed,
                sourceKind = sourceKind,
                isAutoSyncEnabled = true,
                syncCadence = defaultWebFeedSyncCadence(sourceKind),
            )
            webFeedSyncCoordinator.ensureScheduled()
            webFeedSyncState.value = webFeedSyncState.value + (areaId to AreaWebFeedSyncState(
                isRunning = false,
                statusLabel = "Feed gemerkt",
                statusDetail = "Diese Adresse bleibt jetzt als Feed-Quelle im Bereich.",
            ))
        }
    }

    fun removeWebFeedSource(
        areaId: String,
        url: String,
    ) {
        viewModelScope.launch {
            areaWebFeedSourceRepository.remove(areaId = areaId, url = url)
            webFeedSyncCoordinator.ensureScheduled()
        }
    }

    fun setWebFeedAutoSync(
        areaId: String,
        url: String,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            areaWebFeedSourceRepository.setAutoSyncEnabled(
                areaId = areaId,
                url = url,
                enabled = enabled,
            )
            webFeedSyncCoordinator.ensureScheduled()
        }
    }

    fun setWebFeedSyncCadence(
        areaId: String,
        url: String,
        cadence: AreaWebFeedSyncCadence,
    ) {
        viewModelScope.launch {
            areaWebFeedSourceRepository.setSyncCadence(
                areaId = areaId,
                url = url,
                cadence = cadence,
            )
            webFeedSyncCoordinator.ensureScheduled()
        }
    }

    fun syncWebFeed(areaId: String) {
        val currentArea = state.value.areas[areaId] ?: return
        val linkReferences = currentArea.webFeedSync.sources.map(AreaWebFeedSourceState::url) +
            currentArea.importedMaterials
                .filter { it.kind == AreaImportKind.Link }
                .map(AreaImportedMaterialState::reference)
        if (linkReferences.isEmpty()) {
            webFeedSyncState.value = webFeedSyncState.value + (areaId to AreaWebFeedSyncState(
                isRunning = false,
                statusLabel = "Kein Web-Link",
                statusDetail = "Fuege erst einen Link oder eine Feed-URL hinzu.",
            ))
            return
        }
        webFeedSyncState.value = webFeedSyncState.value + (areaId to AreaWebFeedSyncState(
            isRunning = true,
            statusLabel = "Lese Feed",
            statusDetail = "Ich suche nach Feed-Pfaden und hole neue Eintraege.",
        ))
        viewModelScope.launch {
            val result = runCatching {
                webFeedSyncCoordinator.syncArea(areaId)
            }
            result.onSuccess { syncResult ->
                webFeedSyncState.value = webFeedSyncState.value + (areaId to AreaWebFeedSyncState(
                    isRunning = false,
                    statusLabel = if (syncResult.newItemCount == 0) "Nichts neu" else "Feed gelesen",
                    statusDetail = syncResult.message,
                ))
            }.onFailure { error ->
                webFeedSyncState.value = webFeedSyncState.value + (areaId to AreaWebFeedSyncState(
                    isRunning = false,
                    statusLabel = "Feed offen",
                    statusDetail = error.message.orEmpty().ifBlank { "Der Feed konnte gerade nicht gelesen werden." },
                ))
            }
        }
    }

    fun refreshAreaAnalysis(
        areaId: String,
        reason: String,
    ) {
        val existing = analysisRuntimeState.value[areaId]
        if (existing?.isRefreshing == true) return
        viewModelScope.launch {
            analysisRuntimeState.value = analysisRuntimeState.value + (
                areaId to AreaMachineAnalysisRuntime(
                    isRefreshing = true,
                    refreshedAt = existing?.refreshedAt,
                    refreshReason = reason,
                )
            )
            kotlinx.coroutines.delay(420)
            analysisRuntimeState.value = analysisRuntimeState.value + (
                areaId to AreaMachineAnalysisRuntime(
                    isRefreshing = false,
                    refreshedAt = Instant.now(clock),
                    refreshReason = reason,
                )
            )
        }
    }

    fun addImportedImage(
        areaId: String,
        displayName: String,
        uriString: String,
    ) {
        val title = displayName.trim().ifBlank { "Bild" }
        val pendingId = enqueuePendingImport(
            areaId = areaId,
            draft = AreaImportDraft(
                kind = AreaImportKind.Image,
                title = title,
                detail = "Bild fuer diesen Bereich abgelegt.",
                reference = uriString,
            ),
        )
        viewModelScope.launch {
            runCatching {
                captureRepository.createTextCapture(
                    text = buildAreaImportCaptureText(
                        kind = AreaImportKind.Image,
                        title = title,
                        detail = "Bild fuer diesen Bereich abgelegt.",
                        reference = uriString,
                    ),
                    areaId = areaId,
                )
            }
            clearPendingImport(areaId, pendingId)
        }
    }

    fun answerImportQuestion(
        areaId: String,
        answer: String,
    ) {
        val trimmed = answer.trim()
        if (trimmed.isBlank()) return
        val pendingId = enqueuePendingImport(
            areaId = areaId,
            draft = AreaImportDraft(
                kind = AreaImportKind.Text,
                title = "Link-Ziel",
                detail = "Rueckfrage direkt nach dem Link-Import beantwortet.",
                reference = trimmed,
            ),
        )
        viewModelScope.launch {
            runCatching {
                captureRepository.createTextCapture(
                    text = buildAreaImportCaptureText(
                        kind = AreaImportKind.Text,
                        title = "Link-Ziel",
                        detail = "Rueckfrage direkt nach dem Link-Import beantwortet.",
                        reference = trimmed,
                    ),
                    areaId = areaId,
                )
            }
            clearPendingImport(areaId, pendingId)
        }
    }

    fun removeImportedMaterial(importId: String) {
        viewModelScope.launch {
            captureRepository.archive(importId)
        }
    }

    private fun enqueuePendingImport(
        areaId: String,
        draft: AreaImportDraft,
    ): String {
        val localId = "pending-${UUID.randomUUID()}"
        val pendingMaterial = AreaImportedMaterialState(
            id = localId,
            kind = draft.kind,
            title = draft.title,
            detail = draft.detail,
            reference = draft.reference,
            isPending = true,
        )
        pendingImportsState.update { current ->
            current + (areaId to (current[areaId].orEmpty() + AreaPendingImportState(localId, pendingMaterial)))
        }
        return localId
    }

    private fun clearPendingImport(
        areaId: String,
        localId: String,
    ) {
        pendingImportsState.update { current ->
            val remaining = current[areaId]
                .orEmpty()
                .filterNot { it.localId == localId }
            if (remaining.isEmpty()) {
                current - areaId
            } else {
                current + (areaId to remaining)
            }
        }
    }

    private fun mergeImportedMaterials(
        actual: List<AreaImportedMaterialState>,
        pending: List<AreaImportedMaterialState>,
    ): List<AreaImportedMaterialState> {
        if (pending.isEmpty()) return actual
        val actualKeys = actual.mapTo(mutableSetOf()) { item -> importIdentityKey(item) }
        return actual + pending.filterNot { importIdentityKey(it) in actualKeys }
    }

    private fun importIdentityKey(
        item: AreaImportedMaterialState,
    ): String {
        return "${item.kind.name}|${item.title}|${item.reference}"
    }

    fun setAreaIdentity(
        areaId: String,
        title: String,
        summary: String,
        templateId: String,
        iconKey: String,
    ) {
        val current = currentInstance(areaId) ?: return
        viewModelScope.launch {
            areaKernelRepository.updateActiveInstance(
                current.withUpdatedIdentity(
                    title = title,
                    summary = summary,
                    templateId = templateId,
                    iconKey = iconKey,
                ),
            )
        }
    }

    fun setManualScore(
        areaId: String,
        score: Int?,
    ) {
        val currentSnapshot = currentSnapshot(areaId)
        val now = Instant.now(clock)
        viewModelScope.launch {
            if (score == null && currentSnapshot?.manualStateKey == null && currentSnapshot?.manualNote == null) {
                areaKernelRepository.clearSnapshot(
                    areaId = areaId,
                    date = today,
                )
            } else {
                areaKernelRepository.upsertSnapshot(
                    snapshot = AreaSnapshot(
                        areaId = areaId,
                        date = today,
                        manualScore = score,
                        manualStateKey = currentSnapshot?.manualStateKey,
                        manualNote = currentSnapshot?.manualNote,
                        confidence = currentSnapshot?.confidence,
                        freshnessAt = now,
                    ),
                )
            }
        }
    }

    fun setManualState(
        areaId: String,
        stateKey: String?,
    ) {
        val currentSnapshot = currentSnapshot(areaId)
        val now = Instant.now(clock)
        viewModelScope.launch {
            if (stateKey == null && currentSnapshot?.manualScore == null && currentSnapshot?.manualNote == null) {
                areaKernelRepository.clearSnapshot(
                    areaId = areaId,
                    date = today,
                )
            } else {
                areaKernelRepository.upsertSnapshot(
                    snapshot = AreaSnapshot(
                        areaId = areaId,
                        date = today,
                        manualScore = currentSnapshot?.manualScore,
                        manualStateKey = stateKey,
                        manualNote = currentSnapshot?.manualNote,
                        confidence = currentSnapshot?.confidence,
                        freshnessAt = now,
                    ),
                )
            }
        }
    }

    fun setManualNote(
        areaId: String,
        note: String?,
    ) {
        val normalizedNote = note?.trim()?.takeIf(String::isNotBlank)
        val currentSnapshot = currentSnapshot(areaId)
        val now = Instant.now(clock)
        viewModelScope.launch {
            if (normalizedNote == null &&
                currentSnapshot?.manualScore == null &&
                currentSnapshot?.manualStateKey == null
            ) {
                areaKernelRepository.clearSnapshot(
                    areaId = areaId,
                    date = today,
                )
            } else {
                areaKernelRepository.upsertSnapshot(
                    snapshot = AreaSnapshot(
                        areaId = areaId,
                        date = today,
                        manualScore = currentSnapshot?.manualScore,
                        manualStateKey = currentSnapshot?.manualStateKey,
                        manualNote = normalizedNote,
                        confidence = currentSnapshot?.confidence,
                        freshnessAt = now,
                    ),
                )
            }
        }
    }

    fun clearSnapshot(areaId: String) {
        viewModelScope.launch {
            areaKernelRepository.clearSnapshot(
                areaId = areaId,
                date = today,
            )
        }
    }

    fun bindSource(
        areaId: String,
        source: com.struperto.androidappdays.domain.DataSourceKind,
    ) {
        viewModelScope.launch {
            areaSourceBindingRepository.bind(
                areaId = areaId,
                source = source,
            )
        }
    }

    fun unbindSource(
        areaId: String,
        source: com.struperto.androidappdays.domain.DataSourceKind,
    ) {
        viewModelScope.launch {
            areaSourceBindingRepository.unbind(
                areaId = areaId,
                source = source,
            )
        }
    }

    fun setCadence(
        areaId: String,
        cadence: String,
    ) {
        updateProfile(areaId) { it.copy(cadenceKey = cadence) }
    }

    fun setIntensity(
        areaId: String,
        intensity: Float,
    ) {
        updateProfile(areaId) { it.copy(intensity = intensity.roundToInt().coerceIn(1, 5)) }
    }

    fun setSignalBlend(
        areaId: String,
        signalBlend: Float,
    ) {
        updateProfile(areaId) { it.copy(signalBlend = signalBlend.roundToInt().coerceIn(0, 100)) }
    }

    fun toggleTrack(
        areaId: String,
        track: String,
    ) {
        updateProfile(areaId) { current ->
            val nextTracks = if (track in current.selectedTracks) {
                current.selectedTracks - track
            } else {
                current.selectedTracks + track
            }
            current.copy(selectedTracks = nextTracks)
        }
    }

    fun promoteTrack(
        areaId: String,
        track: String,
    ) {
        updateProfile(areaId) { current ->
            val reorderedTracks = linkedSetOf(track).apply {
                current.selectedTracks
                    .filterNot { it == track }
                    .forEach(::add)
            }
            current.copy(selectedTracks = reorderedTracks)
        }
    }

    fun setRemindersEnabled(
        areaId: String,
        enabled: Boolean,
    ) {
        updateProfile(areaId) { it.copy(remindersEnabled = enabled) }
    }

    fun setReviewEnabled(
        areaId: String,
        enabled: Boolean,
    ) {
        updateProfile(areaId) { it.copy(reviewEnabled = enabled) }
    }

    fun setExperimentsEnabled(
        areaId: String,
        enabled: Boolean,
    ) {
        updateProfile(areaId) { it.copy(experimentsEnabled = enabled) }
    }

    fun setLageMode(
        areaId: String,
        value: String,
    ) {
        updateProfile(areaId) {
            it.copy(
                authoringConfig = it.authoringConfig.copy(
                    lageMode = AreaLageMode.fromPersistedValue(value),
                ),
            )
        }
    }

    fun setDirectionMode(
        areaId: String,
        value: String,
    ) {
        updateProfile(areaId) {
            it.copy(
                authoringConfig = it.authoringConfig.copy(
                    directionMode = AreaDirectionMode.fromPersistedValue(value),
                ),
            )
        }
    }

    fun setSourcesMode(
        areaId: String,
        value: String,
    ) {
        updateProfile(areaId) {
            it.copy(
                authoringConfig = it.authoringConfig.copy(
                    sourcesMode = AreaSourcesMode.fromPersistedValue(value),
                ),
            )
        }
    }

    fun setFlowProfile(
        areaId: String,
        value: String,
    ) {
        updateProfile(areaId) {
            it.copy(
                authoringConfig = it.authoringConfig.copy(
                    flowProfile = AreaFlowProfile.fromPersistedValue(value),
                ),
            )
        }
    }

    fun setComplexityLevel(
        areaId: String,
        value: String,
    ) {
        updateProfile(areaId) {
            it.copy(
                authoringConfig = it.authoringConfig.copy(
                    complexityLevel = AreaComplexityLevel.entries.firstOrNull {
                        it.name.equals(value, ignoreCase = true)
                    } ?: AreaComplexityLevel.BASIC,
                ),
            )
        }
    }

    fun setVisibilityLevel(
        areaId: String,
        value: String,
    ) {
        updateProfile(areaId) {
            it.copy(
                authoringConfig = it.authoringConfig.copy(
                    visibilityLevel = AreaVisibilityLevel.fromPersistedValue(value),
                ),
            )
        }
    }

    private fun updateProfile(
        areaId: String,
        transform: (AreaInstance) -> AreaInstance,
    ) {
        val current = currentInstance(areaId) ?: return
        viewModelScope.launch {
            areaKernelRepository.updateActiveInstance(
                transform(current),
            )
        }
    }

    private fun currentInstance(areaId: String): AreaInstance? {
        return activeInstances.value.firstOrNull { it.areaId == areaId }
    }

    private fun currentSnapshot(areaId: String): AreaSnapshot? {
        return todaySnapshots.value.firstOrNull { it.areaId == areaId }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AreaStudioViewModel(
                    areaKernelRepository = appContainer.areaKernelRepository,
                    areaSourceBindingRepository = appContainer.areaSourceBindingRepository,
                    planRepository = appContainer.planRepository,
                    captureRepository = appContainer.captureRepository,
                    areaWebFeedSourceRepository = appContainer.areaWebFeedSourceRepository,
                    webFeedConnector = appContainer.webFeedConnector,
                    webFeedSyncCoordinator = appContainer.webFeedSyncCoordinator,
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

private fun AreaWebFeedSource.toAreaState(): AreaWebFeedSourceState {
    return AreaWebFeedSourceState(
        url = url,
        hostLabel = shortHost(url),
        sourceKindLabel = sourceKind.label,
        autoSyncEnabled = isAutoSyncEnabled,
        syncCadenceLabel = if (isAutoSyncEnabled) syncCadence.label else "Nur manuell",
        capabilityLabels = webFeedCapabilityLabels(sourceKind, isAutoSyncEnabled),
        lastStatusLabel = lastStatusLabel,
        lastStatusDetail = lastStatusDetail,
    )
}

private fun webFeedCapabilityLabels(
    kind: AreaWebFeedSourceKind,
    autoSyncEnabled: Boolean,
): List<String> {
    val base = when (kind) {
        AreaWebFeedSourceKind.Website -> listOf("Artikel lesen", "Artikel finden", "Feed suchen")
        AreaWebFeedSourceKind.Feed -> listOf("Aktuelle Artikel lesen", "Neuigkeiten finden", "Duplikate vermeiden")
    }
    return if (autoSyncEnabled) base + "Automatisch nachladen" else base
}
