package com.struperto.androidappdays.feature.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaSkillBindingRepository
import com.struperto.androidappdays.data.repository.AreaSourceBindingRepository
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
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
import com.struperto.androidappdays.domain.area.TileDisplayMode
import com.struperto.androidappdays.domain.area.withUpdatedIdentity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class AreaStudioAreaState(
    val detail: StartAreaDetailState,
    val authoring: AreaAuthoringStudioState,
    val sourceSetup: AreaSourceSetupState? = null,
)

data class AreaStudioUiState(
    val areas: Map<String, AreaStudioAreaState> = emptyMap(),
)

class AreaStudioViewModel(
    private val areaKernelRepository: AreaKernelRepository,
    private val areaSourceBindingRepository: AreaSourceBindingRepository,
    private val areaSkillBindingRepository: AreaSkillBindingRepository,
    private val planRepository: PlanRepository,
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

    val state = combine(
        baseState,
        areaSourceBindingRepository.observeAll(),
        sourceCapabilityRepository.observeProfile(),
        calendarSignalRepository.observeToday(today, zoneId),
        notificationSignalRepository.observeToday(today, zoneId),
    ) { projectedAreas, bindings, capabilityProfile, calendarSignals, notificationSignals ->
        val healthObservations = if (capabilityProfile.isUsable(com.struperto.androidappdays.domain.DataSourceKind.HEALTH_CONNECT)) {
            healthConnectRepository.readDailyObservations(today)
        } else {
            emptyList()
        }
        val bindingsByAreaId = bindings.groupBy { it.areaId }
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
                    ),
                )
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

    fun setTileDisplayMode(areaId: String, mode: TileDisplayMode) {
        updateProfile(areaId) { it.copy(tileDisplayMode = mode) }
    }

    fun setFamilyKey(areaId: String, familyKey: String) {
        updateProfile(areaId) { it.copy(familyKey = familyKey) }
    }

    fun deleteArea(areaId: String) {
        viewModelScope.launch {
            try {
                areaSkillBindingRepository.clearArea(areaId)
                areaSourceBindingRepository.clearArea(areaId)
                areaKernelRepository.deleteActiveInstance(areaId)
            } catch (_: Exception) {
                // silent — UI stays consistent via observed flow
            }
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
                    areaSkillBindingRepository = appContainer.areaSkillBindingRepository,
                    planRepository = appContainer.planRepository,
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
