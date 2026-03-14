package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaKernelBootstrapState
import com.struperto.androidappdays.data.repository.AreaKernelPersistenceBoundary
import com.struperto.androidappdays.data.repository.AreaSourceBinding
import com.struperto.androidappdays.data.repository.AreaSourceBindingRepository
import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.data.repository.FakePlanRepository
import com.struperto.androidappdays.data.repository.HealthConnectAvailability
import com.struperto.androidappdays.data.repository.HealthConnectRepository
import com.struperto.androidappdays.data.repository.NotificationSignal
import com.struperto.androidappdays.data.repository.NotificationSignalRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceCapability
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.defaultAreaProfileConfig
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition
import com.struperto.androidappdays.testing.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(
        Instant.parse("2026-03-11T08:00:00Z"),
        ZoneId.of("Europe/Berlin"),
    )

    @Test
    fun state_readsOverviewFromAreaKernelRepository() = runTest {
        val areaKernelRepository = FakeStartAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "vitality",
                    title = "Ideenraum",
                    summary = "Gedanken ruhig sammeln.",
                    iconKey = "spark",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "adaptive",
                    selectedTracks = setOf("Schlaf", "Energie"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    profileConfig = defaultAreaProfileConfig(
                        definition = startAreaKernelDefinition("vitality"),
                    ),
                    templateId = "free",
                ),
            ),
            snapshots = listOf(
                AreaSnapshot(
                    areaId = "vitality",
                    date = LocalDate.parse("2026-03-11"),
                    manualScore = 4,
                ),
            ),
        )
        val viewModel = StartViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeSourceCapabilityRepository(),
            calendarSignalRepository = FakeCalendarSignalRepository(),
            notificationSignalRepository = FakeNotificationSignalRepository(),
            healthConnectRepository = FakeHealthConnectRepository(),
            clock = clock,
        )

        val tile = viewModel.state.first { it.areas.isNotEmpty() }.areas.single()

        assertEquals("Ideenraum", tile.label)
        assertEquals("Signal 4/4", tile.statusLabel)
        assertEquals("spark", tile.iconKey)
    }

    @Test
    fun updateAreaIdentity_writesThroughAreaKernelRepository() = runTest {
        val areaKernelRepository = FakeStartAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "vitality",
                    title = "Vitalitaet",
                    summary = "Schlaf und Energie tragen.",
                    iconKey = "heart",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "adaptive",
                    selectedTracks = setOf("Schlaf", "Energie"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    profileConfig = defaultAreaProfileConfig(
                        definition = startAreaKernelDefinition("vitality"),
                    ),
                    templateId = "ritual",
                ),
            ),
        )
        val viewModel = StartViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeSourceCapabilityRepository(),
            calendarSignalRepository = FakeCalendarSignalRepository(),
            notificationSignalRepository = FakeNotificationSignalRepository(),
            healthConnectRepository = FakeHealthConnectRepository(),
            clock = clock,
        )
        viewModel.state.first { it.areas.isNotEmpty() }

        viewModel.updateAreaIdentity(
            areaId = "vitality",
            title = "Meine Vitalitaet",
            meaning = "Eigene Beschreibung",
            templateId = "ritual",
            iconKey = "heart",
        )

        val updated = areaKernelRepository.updatedInstances.single()
        assertEquals("Meine Vitalitaet", updated.title)
        assertEquals("Eigene Beschreibung", updated.summary)
        assertEquals("ritual", updated.templateId)
    }

    @Test
    fun createDeleteAndReorderUseAreaKernelLifecycleFacade() = runTest {
        val areaKernelRepository = FakeStartAreaKernelRepository()
        val bindingRepository = FakeAreaSourceBindingRepository()
        val viewModel = StartViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = bindingRepository,
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeSourceCapabilityRepository(),
            calendarSignalRepository = FakeCalendarSignalRepository(),
            notificationSignalRepository = FakeNotificationSignalRepository(),
            healthConnectRepository = FakeHealthConnectRepository(),
            clock = clock,
        )

        viewModel.createArea(
            title = "Neu",
            meaning = "Neu beschrieben",
            templateId = "free",
            iconKey = "spark",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceKind = DataSourceKind.CALENDAR,
        ) { }
        viewModel.deleteArea("vitality")
        viewModel.moveAreaEarlier("vitality")
        viewModel.moveAreaLater("vitality")
        viewModel.swapAreas("first", "second")

        assertEquals("Neu", areaKernelRepository.createdDrafts.single().title)
        assertEquals("vitality", areaKernelRepository.deletedAreaIds.single())
        assertEquals("vitality", areaKernelRepository.movedEarlierAreaIds.single())
        assertEquals("vitality", areaKernelRepository.movedLaterAreaIds.single())
        assertEquals("first" to "second", areaKernelRepository.swappedAreaIds.single())
        assertEquals(
            listOf(AreaSourceBinding(areaId = "new-area", source = DataSourceKind.CALENDAR)),
            bindingRepository.boundHistory,
        )
        assertEquals(listOf("vitality"), bindingRepository.clearedAreaIds)
    }

    @Test
    fun state_overlaysCalendarSignalForBoundAreas() = runTest {
        val areaKernelRepository = FakeStartAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "home",
                    title = "Kalender Heute",
                    summary = "Besprechungen und Termine im Blick.",
                    iconKey = "calendar",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "adaptive",
                    selectedTracks = setOf("Termine"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    profileConfig = defaultAreaProfileConfig(
                        definition = startAreaKernelDefinition("home"),
                    ),
                    templateId = "place",
                ),
            ),
        )
        val viewModel = StartViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaSourceBindingRepository(
                bindings = listOf(
                    AreaSourceBinding(
                        areaId = "home",
                        source = DataSourceKind.CALENDAR,
                    ),
                ),
            ),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeSourceCapabilityRepository(
                enabledSources = setOf(DataSourceKind.CALENDAR),
            ),
            calendarSignalRepository = FakeCalendarSignalRepository(
                signals = listOf(
                    CalendarSignal(
                        id = 1L,
                        title = "Review",
                        startMillis = Instant.parse("2026-03-11T10:00:00Z").toEpochMilli(),
                        endMillis = Instant.parse("2026-03-11T11:00:00Z").toEpochMilli(),
                        isAllDay = false,
                    ),
                ),
            ),
            notificationSignalRepository = FakeNotificationSignalRepository(),
            healthConnectRepository = FakeHealthConnectRepository(),
            clock = clock,
        )

        val tile = viewModel.state.first { it.areas.isNotEmpty() }.areas.single()

        assertEquals("Kalender aktiv", tile.primaryHint.title)
        assertEquals("1 Termin heute", tile.todayLabel)
        assertEquals(StartAreaStatusKind.Live, tile.statusKind)
        assertEquals("Kalender Heute: 11:00 Review", tile.todayOutput.headline)
        assertEquals("11:00 · Review", tile.todayOutput.nextMeaningfulStep.label)
        assertTrue(tile.todayOutput.evidenceSummary.contains("Review"))
    }

    @Test
    fun state_showsCalendarSetupWhenBindingIsMissing() = runTest {
        val areaKernelRepository = FakeStartAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "home",
                    title = "Kalender Heute",
                    summary = "Besprechungen und Termine lesen.",
                    iconKey = "calendar",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "adaptive",
                    selectedTracks = setOf("Termine"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    profileConfig = defaultAreaProfileConfig(
                        definition = startAreaKernelDefinition("home"),
                    ),
                    templateId = "place",
                ),
            ),
        )
        val viewModel = StartViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeSourceCapabilityRepository(
                enabledSources = setOf(DataSourceKind.CALENDAR),
            ),
            calendarSignalRepository = FakeCalendarSignalRepository(),
            notificationSignalRepository = FakeNotificationSignalRepository(),
            healthConnectRepository = FakeHealthConnectRepository(),
            clock = clock,
        )

        val tile = viewModel.state.first { it.areas.isNotEmpty() }.areas.single()

        assertEquals(StartAreaStatusKind.Waiting, tile.statusKind)
        assertEquals("Einrichtung offen", tile.statusLabel)
        assertEquals("Kalender Heute: Kalender verbinden", tile.todayOutput.headline)
        assertEquals("Kalender fuer diesen Bereich verbinden", tile.todayOutput.nextMeaningfulStep.label)
    }

    @Test
    fun state_overlaysHealthSignalsForBoundAreas() = runTest {
        val areaKernelRepository = FakeStartAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "sleep",
                    title = "Schlaf Blick",
                    summary = "Schlaf und Bewegung ruhig sichtbar machen.",
                    iconKey = "heart",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "adaptive",
                    selectedTracks = setOf("Schlaf"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    profileConfig = defaultAreaProfileConfig(
                        definition = startAreaKernelDefinition("vitality"),
                    ),
                    templateId = "ritual",
                ),
            ),
        )
        val viewModel = StartViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaSourceBindingRepository(
                bindings = listOf(
                    AreaSourceBinding(
                        areaId = "sleep",
                        source = DataSourceKind.HEALTH_CONNECT,
                    ),
                ),
            ),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeSourceCapabilityRepository(
                enabledSources = setOf(DataSourceKind.HEALTH_CONNECT),
            ),
            calendarSignalRepository = FakeCalendarSignalRepository(),
            notificationSignalRepository = FakeNotificationSignalRepository(),
            healthConnectRepository = FakeHealthConnectRepository(
                observations = listOf(
                    DomainObservation(
                        id = "sleep_1",
                        goalId = null,
                        domain = LifeDomain.SLEEP,
                        metric = ObservationMetric.SLEEP_HOURS,
                        source = ObservationSource.WEARABLE,
                        startedAt = Instant.parse("2026-03-10T23:00:00Z"),
                        value = DomainObservationValue(numeric = 7.4f, unit = "h"),
                        logicalDate = LocalDate.parse("2026-03-11"),
                        sourceRecordId = "sleep_1",
                        confidence = 0.9f,
                        contextTags = setOf("health_connect"),
                    ),
                ),
            ),
            clock = clock,
        )

        val tile = viewModel.state.first { it.areas.isNotEmpty() }.areas.single()

        assertEquals("Health Connect aktiv", tile.primaryHint.title)
        assertEquals("Schlaf 7.4 h", tile.todayLabel)
        assertEquals(StartAreaStatusKind.Live, tile.statusKind)
    }
}

private class FakeHealthConnectRepository(
    private val observations: List<DomainObservation> = emptyList(),
) : HealthConnectRepository {
    override val requiredPermissions: Set<String> = emptySet()

    override suspend fun availability(): HealthConnectAvailability = HealthConnectAvailability.AVAILABLE

    override suspend fun grantedPermissions(): Set<String> = emptySet()

    override suspend fun readDailyObservations(logicalDate: LocalDate): List<DomainObservation> = observations
}

private class FakeAreaSourceBindingRepository(
    bindings: List<AreaSourceBinding> = emptyList(),
) : AreaSourceBindingRepository {
    private val bindingsFlow = MutableStateFlow(bindings)
    val boundHistory = mutableListOf<AreaSourceBinding>()
    val clearedAreaIds = mutableListOf<String>()

    override fun observeAll(): Flow<List<AreaSourceBinding>> = bindingsFlow

    override fun observeByArea(areaId: String): Flow<List<AreaSourceBinding>> {
        return flowOf(bindingsFlow.value.filter { it.areaId == areaId })
    }

    override suspend fun loadAll(): List<AreaSourceBinding> = bindingsFlow.value

    override suspend fun bind(areaId: String, source: DataSourceKind) {
        val binding = AreaSourceBinding(areaId = areaId, source = source)
        boundHistory += binding
        bindingsFlow.value = bindingsFlow.value
            .filterNot { it.areaId == areaId && it.source == source } + binding
    }

    override suspend fun unbind(areaId: String, source: DataSourceKind) {
        bindingsFlow.value = bindingsFlow.value.filterNot { it.areaId == areaId && it.source == source }
    }

    override suspend fun clearArea(areaId: String) {
        clearedAreaIds += areaId
        bindingsFlow.value = bindingsFlow.value.filterNot { it.areaId == areaId }
    }

    override suspend fun clearAll() {
        bindingsFlow.value = emptyList()
    }
}

private class FakeSourceCapabilityRepository(
    enabledSources: Set<DataSourceKind> = emptySet(),
) : SourceCapabilityRepository {
    private val profile = CapabilityProfile(
        sources = DataSourceKind.entries.map { source ->
            DataSourceCapability(
                source = source,
                label = source.name,
                enabled = source in enabledSources || source == DataSourceKind.MANUAL,
                available = true,
                granted = source in enabledSources || source == DataSourceKind.MANUAL,
                detail = "",
            )
        },
    )

    override fun observeProfile(): Flow<CapabilityProfile> = flowOf(profile)

    override suspend fun loadProfile(): CapabilityProfile = profile

    override suspend fun ensureSeeded() = Unit

    override suspend fun setEnabled(source: DataSourceKind, enabled: Boolean) = Unit
}

private class FakeCalendarSignalRepository(
    private val signals: List<CalendarSignal> = emptyList(),
) : CalendarSignalRepository {
    override fun observeToday(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<CalendarSignal>> = flowOf(signals)
}

private class FakeNotificationSignalRepository(
    private val signals: List<NotificationSignal> = emptyList(),
) : NotificationSignalRepository {
    override fun observeToday(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<NotificationSignal>> = flowOf(signals)

    override suspend fun upsert(
        id: String,
        packageName: String,
        title: String,
        text: String,
        postedAt: Long,
    ) = Unit

    override suspend fun markRemoved(
        id: String,
        removedAt: Long,
    ) = Unit

    override suspend fun clearAll() = Unit
}

private class FakeStartAreaKernelRepository(
    instances: List<AreaInstance> = emptyList(),
    snapshots: List<AreaSnapshot> = emptyList(),
) : AreaKernelRepository {
    private val instancesFlow = MutableStateFlow(instances)
    private val snapshotsFlow = MutableStateFlow(snapshots)
    private val bootstrapFlow = MutableStateFlow(
        AreaKernelBootstrapState(
            activeInstanceCount = instances.size,
            missingProfileAreaIds = emptySet(),
            hasLegacySetupRecord = true,
            persistenceBoundary = AreaKernelPersistenceBoundary(
                activeInstanceStores = emptySet(),
                snapshotStores = emptySet(),
                bootSetupStores = emptySet(),
            ),
        ),
    )

    val updatedInstances = mutableListOf<AreaInstance>()
    val createdDrafts = mutableListOf<CreateAreaInstanceDraft>()
    val deletedAreaIds = mutableListOf<String>()
    val movedEarlierAreaIds = mutableListOf<String>()
    val movedLaterAreaIds = mutableListOf<String>()
    val swappedAreaIds = mutableListOf<Pair<String, String>>()

    override fun observeBootstrapState(): Flow<AreaKernelBootstrapState> = bootstrapFlow

    override suspend fun ensureStartBootstrap(): AreaKernelBootstrapState = bootstrapFlow.value

    override fun persistenceBoundary(): AreaKernelPersistenceBoundary = bootstrapFlow.value.persistenceBoundary

    override fun observeActiveInstances(): Flow<List<AreaInstance>> = instancesFlow

    override suspend fun loadActiveInstances(): List<AreaInstance> = instancesFlow.value

    override suspend fun createActiveInstance(draft: CreateAreaInstanceDraft): AreaInstance {
        createdDrafts += draft
        return AreaInstance(
            areaId = "new-area",
            title = draft.title,
            summary = draft.summary,
            iconKey = draft.iconKey,
            targetScore = 3,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "adaptive",
            selectedTracks = emptySet<String>(),
            signalBlend = 60,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
            profileConfig = defaultAreaProfileConfig(
                definition = null,
                templateId = draft.templateId,
            ),
            templateId = draft.templateId,
        )
    }

    override suspend fun deleteActiveInstance(areaId: String) {
        deletedAreaIds += areaId
    }

    override suspend fun swapActiveInstanceOrder(firstAreaId: String, secondAreaId: String) {
        swappedAreaIds += firstAreaId to secondAreaId
    }

    override suspend fun moveActiveInstanceEarlier(areaId: String) {
        movedEarlierAreaIds += areaId
    }

    override suspend fun moveActiveInstanceLater(areaId: String) {
        movedLaterAreaIds += areaId
    }

    override fun observeSnapshots(date: LocalDate): Flow<List<AreaSnapshot>> = snapshotsFlow

    override suspend fun updateActiveInstance(instance: AreaInstance) {
        updatedInstances += instance
        instancesFlow.value = instancesFlow.value.map {
            if (it.areaId == instance.areaId) instance else it
        }
    }

    override suspend fun upsertSnapshot(snapshot: AreaSnapshot) {
        snapshotsFlow.value = snapshotsFlow.value
            .filterNot { it.areaId == snapshot.areaId && it.date == snapshot.date } + snapshot
    }

    override suspend fun clearSnapshot(areaId: String, date: LocalDate) {
        snapshotsFlow.value = snapshotsFlow.value.filterNot { it.areaId == areaId && it.date == date }
    }
}
