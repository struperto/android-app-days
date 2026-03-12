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
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.data.repository.HealthConnectRepository
import com.struperto.androidappdays.data.repository.NotificationSignalRepository
import com.struperto.androidappdays.data.repository.PlanRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaSkillKind
import com.struperto.androidappdays.domain.area.TileDisplayMode
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StartViewModel(
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

    val state = combine(
        baseOverviewState,
        areaSourceBindingRepository.observeAll(),
        sourceCapabilityRepository.observeProfile(),
        calendarSignalRepository.observeToday(today, zoneId),
        notificationSignalRepository.observeToday(today, zoneId),
    ) { baseState, bindings, capabilityProfile, calendarSignals, notificationSignals ->
        val healthObservations = if (capabilityProfile.isUsable(DataSourceKind.HEALTH_CONNECT)) {
            healthConnectRepository.readDailyObservations(today)
        } else {
            emptyList()
        }
        overlayOverviewWithSources(
            state = baseState,
            bindings = bindings,
            capabilityProfile = capabilityProfile,
            calendarSignals = calendarSignals,
            notificationSignals = notificationSignals,
            healthObservations = healthObservations,
            zoneId = zoneId,
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
        skills: Set<AreaSkillKind> = emptySet(),
        tileDisplayMode: TileDisplayMode = TileDisplayMode.AMPEL,
        familyKey: String = "",
        onCreated: (String) -> Unit,
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return
        val trimmedMeaning = meaning.trim()

        viewModelScope.launch {
            val areaId = areaKernelRepository.createActiveInstance(
                CreateAreaInstanceDraft(
                    title = trimmedTitle,
                    summary = trimmedMeaning,
                    templateId = templateId,
                    iconKey = iconKey,
                    behaviorClass = behaviorClass,
                    skills = skills,
                    tileDisplayMode = tileDisplayMode,
                    familyKey = familyKey,
                ),
            )
            sourceKind?.let { source ->
                areaSourceBindingRepository.bind(
                    areaId = areaId.areaId,
                    source = source,
                )
            }
            for (skill in skills) {
                areaSkillBindingRepository.bind(
                    areaId = areaId.areaId,
                    skillKind = skill,
                )
            }
            onCreated(areaId.areaId)
        }
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

    fun updateAreaIdentity(
        areaId: String,
        title: String,
        meaning: String,
        templateId: String,
        iconKey: String,
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return
        val current = activeInstances.value.firstOrNull { it.areaId == areaId } ?: return
        viewModelScope.launch {
            areaKernelRepository.updateActiveInstance(
                current.copy(
                    title = trimmedTitle,
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
            try {
                areaKernelRepository.swapActiveInstanceOrder(
                    firstAreaId = firstAreaId,
                    secondAreaId = secondAreaId,
                )
            } catch (_: Exception) {
                // silent — UI stays consistent via observed flow
            }
        }
    }

    fun moveAreaEarlier(areaId: String) {
        viewModelScope.launch {
            try {
                areaKernelRepository.moveActiveInstanceEarlier(areaId)
            } catch (_: Exception) {
                // silent — UI stays consistent via observed flow
            }
        }
    }

    fun moveAreaLater(areaId: String) {
        viewModelScope.launch {
            try {
                areaKernelRepository.moveActiveInstanceLater(areaId)
            } catch (_: Exception) {
                // silent — UI stays consistent via observed flow
            }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StartViewModel(
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
