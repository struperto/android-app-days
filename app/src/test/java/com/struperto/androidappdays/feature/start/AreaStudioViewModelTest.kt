package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaKernelBootstrapState
import com.struperto.androidappdays.data.repository.AreaKernelPersistenceBoundary
import com.struperto.androidappdays.data.repository.AreaSourceBinding
import com.struperto.androidappdays.data.repository.AreaSourceBindingRepository
import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.data.repository.FakePlanRepository
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
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.defaultAreaAuthoringConfig
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AreaStudioViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(
        Instant.parse("2026-03-11T08:00:00Z"),
        ZoneId.of("Europe/Berlin"),
    )

    @Test
    fun state_readsDetailProjectionFromAreaKernelRepository() = runTest {
        val areaKernelRepository = FakeAreaStudioAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "vitality",
                    title = "Ideenraum",
                    summary = "Gedanken ruhig sammeln.",
                    iconKey = "spark",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "weekly",
                    selectedTracks = linkedSetOf("Energie", "Schlaf"),
                    signalBlend = 75,
                    intensity = 4,
                    remindersEnabled = true,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    authoringConfig = defaultAreaAuthoringConfig(
                        definition = startAreaKernelDefinition("vitality"),
                    ),
                    templateId = "free",
                ),
            ),
            snapshots = listOf(
                AreaSnapshot(
                    areaId = "vitality",
                    date = LocalDate.parse("2026-03-11"),
                    manualScore = 3,
                ),
            ),
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )

        val areaState = viewModel.state.first { it.areas.isNotEmpty() }.areas.getValue("vitality")
        val detail = areaState.detail

        assertEquals("Ideenraum", detail.title)
        assertEquals("3/5", detail.statusLabel)
        assertEquals("Energie", detail.focusTrack)
        assertEquals("Ruhig", detail.profileState.flowLabel)
        assertEquals("Ruhig · Routine · Erinnerung", detail.panelStates.first { it.panel == StartAreaPanel.Options }.summary)
        assertEquals("Seed-Basis", areaState.authoring.basisLabel)
        assertEquals("2", areaState.analysis.snapshotVersion)
        assertEquals("Aktueller Zustand", areaState.analysis.currentState.label)
        assertEquals("Ziel", areaState.analysis.goalState.label)
        assertTrue(areaState.analysis.machinePayload.contains("\"analysisVersion\": \"2\""))
        assertTrue(areaState.analysis.machinePayload.contains("\"currentState\""))
    }

    @Test
    fun panelWrites_goThroughAreaKernelRepository() = runTest {
        val areaKernelRepository = FakeAreaStudioAreaKernelRepository(
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
                    selectedTracks = linkedSetOf("Schlaf", "Energie"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    authoringConfig = defaultAreaAuthoringConfig(
                        definition = startAreaKernelDefinition("vitality"),
                    ),
                    templateId = "ritual",
                ),
            ),
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )
        viewModel.state.first { it.areas.isNotEmpty() }

        viewModel.setTargetScore("vitality", 2f)
        viewModel.setCadence("vitality", "weekly")
        viewModel.toggleTrack("vitality", "Bewegung")
        viewModel.setSignalBlend("vitality", 35f)
        viewModel.setRemindersEnabled("vitality", true)
        viewModel.setLageMode("vitality", "state")
        viewModel.setDirectionMode("vitality", "focus")
        viewModel.setSourcesMode("vitality", "curated")
        viewModel.setFlowProfile("vitality", "active")
        viewModel.setComplexityLevel("vitality", "expert")
        viewModel.setVisibilityLevel("vitality", "expanded")
        viewModel.setManualScore("vitality", 4)
        viewModel.setManualScore("vitality", null)

        val lastInstance = areaKernelRepository.updatedInstances.last()
        assertEquals(2, areaKernelRepository.updatedInstances.first().targetScore)
        assertEquals("weekly", areaKernelRepository.updatedInstances[1].cadenceKey)
        assertTrue("Bewegung" in areaKernelRepository.updatedInstances[2].selectedTracks)
        assertEquals(35, areaKernelRepository.updatedInstances[3].signalBlend)
        assertTrue(areaKernelRepository.updatedInstances[4].remindersEnabled)
        assertEquals("state", areaKernelRepository.updatedInstances[5].authoringConfig.lageMode.persistedValue)
        assertEquals("focus", areaKernelRepository.updatedInstances[6].authoringConfig.directionMode.persistedValue)
        assertEquals("curated", areaKernelRepository.updatedInstances[7].authoringConfig.sourcesMode.persistedValue)
        assertEquals("active", areaKernelRepository.updatedInstances[8].authoringConfig.flowProfile.persistedValue)
        assertEquals("EXPERT", areaKernelRepository.updatedInstances[9].authoringConfig.complexityLevel.name)
        assertEquals("expanded", areaKernelRepository.updatedInstances[10].authoringConfig.visibilityLevel.persistedValue)
        assertEquals("weekly", lastInstance.cadenceKey)
        assertEquals(LocalDate.parse("2026-03-11"), areaKernelRepository.clearedSnapshots.single().second)
    }

    @Test
    fun stateWrites_preserveAndClearStatefulSnapshotsThroughAreaKernelRepository() = runTest {
        val areaKernelRepository = FakeAreaStudioAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "friends",
                    title = "Freundschaft",
                    summary = "Verbindung warm und lebendig halten.",
                    iconKey = "care",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "weekly",
                    selectedTracks = linkedSetOf("Kontakt", "Tiefe"),
                    signalBlend = 50,
                    intensity = 2,
                    remindersEnabled = true,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    authoringConfig = defaultAreaAuthoringConfig(
                        definition = startAreaKernelDefinition("friends"),
                    ),
                    templateId = "person",
                ),
            ),
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )
        viewModel.state.first { it.areas.isNotEmpty() }

        viewModel.setManualState("friends", "warm")
        viewModel.setManualScore("friends", 4)
        viewModel.setManualState("friends", null)
        viewModel.clearSnapshot("friends")

        assertEquals("warm", areaKernelRepository.upsertedSnapshots[0].manualStateKey)
        assertEquals(4, areaKernelRepository.upsertedSnapshots[1].manualScore)
        assertEquals("warm", areaKernelRepository.upsertedSnapshots[1].manualStateKey)
        assertEquals(4, areaKernelRepository.upsertedSnapshots[2].manualScore)
        assertEquals(null, areaKernelRepository.upsertedSnapshots[2].manualStateKey)
        assertEquals("friends", areaKernelRepository.clearedSnapshots.single().first)
    }

    @Test
    fun noteWrites_preserveSnapshotAndRefreshEvidenceTimestamp() = runTest {
        val areaKernelRepository = FakeAreaStudioAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "home",
                    title = "Zuhause",
                    summary = "Raum und Pflege tragbar halten.",
                    iconKey = "home",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "weekly",
                    selectedTracks = linkedSetOf("Pflege", "Ordnung"),
                    signalBlend = 50,
                    intensity = 2,
                    remindersEnabled = true,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    authoringConfig = defaultAreaAuthoringConfig(
                        definition = startAreaKernelDefinition("home"),
                    ),
                    templateId = "place",
                ),
            ),
            snapshots = listOf(
                AreaSnapshot(
                    areaId = "home",
                    date = LocalDate.parse("2026-03-11"),
                    manualScore = 2,
                ),
            ),
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )
        viewModel.state.first { it.areas.isNotEmpty() }

        viewModel.setManualNote("home", "Bad putzen und Waesche sortieren")
        viewModel.setManualNote("home", "")

        assertEquals("Bad putzen und Waesche sortieren", areaKernelRepository.upsertedSnapshots[0].manualNote)
        assertEquals(2, areaKernelRepository.upsertedSnapshots[0].manualScore)
        assertEquals(null, areaKernelRepository.upsertedSnapshots[1].manualNote)
        assertTrue(areaKernelRepository.upsertedSnapshots[0].freshnessAt != null)
    }

    @Test
    fun identityWrites_rebaseTemplateBackedAreasOntoNewAuthoringBasis() = runTest {
        val areaKernelRepository = FakeAreaStudioAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "custom",
                    title = "Eigener Bereich",
                    summary = "Eigenes Thema.",
                    iconKey = "spark",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "adaptive",
                    selectedTracks = linkedSetOf("Kontakt"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    templateId = "free",
                    definitionId = "template:free",
                    authoringConfig = defaultAreaAuthoringConfig(
                        definition = null,
                        templateId = "free",
                    ),
                ),
            ),
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )
        viewModel.state.first { it.areas.isNotEmpty() }

        viewModel.setAreaIdentity(
            areaId = "custom",
            title = "Projektbereich",
            summary = "Arbeit klarer ziehen.",
            templateId = "project",
            iconKey = "briefcase",
        )

        val updated = areaKernelRepository.updatedInstances.single()
        assertEquals("template:project", updated.definitionId)
        assertEquals("project", updated.templateId)
        assertEquals("Projektbereich", updated.title)
        assertEquals("Arbeit klarer ziehen.", updated.summary)
        assertEquals("briefcase", updated.iconKey)
        assertEquals("focus", updated.authoringConfig.directionMode.persistedValue)
        assertEquals("active", updated.authoringConfig.flowProfile.persistedValue)
        assertEquals("BASIC", updated.authoringConfig.complexityLevel.name)
        assertEquals("focused", updated.authoringConfig.visibilityLevel.persistedValue)
    }

    @Test
    fun state_exposesCalendarSourceSetupForBoundArea() = runTest {
        val areaKernelRepository = FakeAreaStudioAreaKernelRepository(
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
                    selectedTracks = linkedSetOf("Termine"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    authoringConfig = defaultAreaAuthoringConfig(
                        definition = startAreaKernelDefinition("home"),
                    ),
                    templateId = "place",
                ),
            ),
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(
                bindings = listOf(
                    AreaSourceBinding(
                        areaId = "home",
                        source = DataSourceKind.CALENDAR,
                    ),
                ),
            ),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(
                enabledSources = setOf(DataSourceKind.CALENDAR),
            ),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(
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
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )

        val areaState = viewModel.state.first { it.areas.isNotEmpty() }.areas.getValue("home")

        assertTrue(areaState.detail.hints.any { it.title == "Kalender aktiv" })
        assertEquals("1 Termin heute", areaState.sourceSetup?.headline)
        assertEquals("Trennen", areaState.sourceSetup?.secondaryActionLabel)
    }

    @Test
    fun bindCalendar_writesThroughAreaSourceBindingRepository() = runTest {
        val bindings = FakeAreaStudioSourceBindingRepository()
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = FakeAreaStudioAreaKernelRepository(),
            areaSourceBindingRepository = bindings,
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )

        viewModel.bindSource("home", DataSourceKind.CALENDAR)
        viewModel.unbindSource("home", DataSourceKind.CALENDAR)

        assertEquals(
            listOf(AreaSourceBinding(areaId = "home", source = DataSourceKind.CALENDAR)),
            bindings.boundHistory,
        )
        assertEquals(
            listOf(AreaSourceBinding(areaId = "home", source = DataSourceKind.CALENDAR)),
            bindings.unboundHistory,
        )
    }

    @Test
    fun state_exposesHealthSourceSetupForBoundArea() = runTest {
        val areaKernelRepository = FakeAreaStudioAreaKernelRepository(
            instances = listOf(
                AreaInstance(
                    areaId = "sleep",
                    title = "Schlaf Blick",
                    summary = "Schlaf und Bewegung sichtbar halten.",
                    iconKey = "heart",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "adaptive",
                    selectedTracks = linkedSetOf("Schlaf"),
                    signalBlend = 60,
                    intensity = 3,
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                    authoringConfig = defaultAreaAuthoringConfig(
                        definition = startAreaKernelDefinition("vitality"),
                    ),
                    templateId = "ritual",
                ),
            ),
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = areaKernelRepository,
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(
                bindings = listOf(
                    AreaSourceBinding(
                        areaId = "sleep",
                        source = DataSourceKind.HEALTH_CONNECT,
                    ),
                ),
            ),
            planRepository = FakePlanRepository(),
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(
                enabledSources = setOf(DataSourceKind.HEALTH_CONNECT),
            ),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(
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

        val areaState = viewModel.state.first { it.areas.isNotEmpty() }.areas.getValue("sleep")

        assertTrue(areaState.detail.hints.any { it.title == "Health Connect aktiv" })
        assertEquals("Schlaf 7.4 h", areaState.sourceSetup?.headline)
        assertEquals("Trennen", areaState.sourceSetup?.secondaryActionLabel)
    }

    @Test
    fun addImportedLink_doesNotTriggerAdditionalHealthReadOnCaptureProjection() = runTest {
        val captureRepository = FakeAreaStudioCaptureRepository()
        val healthRepository = FakeAreaStudioHealthConnectRepository(
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
        )
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = FakeAreaStudioAreaKernelRepository(
                instances = listOf(
                    AreaInstance(
                        areaId = "sleep",
                        title = "Schlaf Blick",
                        summary = "Schlaf und Bewegung sichtbar halten.",
                        iconKey = "heart",
                        targetScore = 3,
                        sortOrder = 0,
                        isActive = true,
                        cadenceKey = "adaptive",
                        selectedTracks = linkedSetOf("Schlaf"),
                        signalBlend = 60,
                        intensity = 3,
                        remindersEnabled = false,
                        reviewEnabled = true,
                        experimentsEnabled = false,
                        authoringConfig = defaultAreaAuthoringConfig(
                            definition = startAreaKernelDefinition("vitality"),
                        ),
                        templateId = "ritual",
                    ),
                ),
            ),
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(
                bindings = listOf(
                    AreaSourceBinding(
                        areaId = "sleep",
                        source = DataSourceKind.HEALTH_CONNECT,
                    ),
                ),
            ),
            planRepository = FakePlanRepository(),
            captureRepository = captureRepository,
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(
                enabledSources = setOf(DataSourceKind.HEALTH_CONNECT),
            ),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = healthRepository,
            clock = clock,
        )

        viewModel.state.first { it.areas.isNotEmpty() }
        val readCountBeforeImport = healthRepository.readCount

        viewModel.addImportedLink("sleep", "https://example.com/post")
        advanceUntilIdle()

        val areaState = viewModel.state.value.areas.getValue("sleep")
        assertTrue(areaState.importedMaterials.any { it.reference == "https://example.com/post" })
        assertEquals(readCountBeforeImport, healthRepository.readCount)
    }

    @Test
    fun answerImportQuestion_createsTextEvidenceImport() = runTest {
        val captureRepository = FakeAreaStudioCaptureRepository()
        val viewModel = AreaStudioViewModel(
            areaKernelRepository = FakeAreaStudioAreaKernelRepository(),
            areaSourceBindingRepository = FakeAreaStudioSourceBindingRepository(),
            planRepository = FakePlanRepository(),
            captureRepository = captureRepository,
            sourceCapabilityRepository = FakeAreaStudioSourceCapabilityRepository(),
            calendarSignalRepository = FakeAreaStudioCalendarSignalRepository(),
            notificationSignalRepository = FakeAreaStudioNotificationSignalRepository(),
            healthConnectRepository = FakeAreaStudioHealthConnectRepository(),
            clock = clock,
        )

        viewModel.answerImportQuestion("home", "Nur wichtige Highlights davon merken")
        advanceUntilIdle()

        assertTrue(captureRepository.items.value.any { item ->
            item.areaId == "home" && item.text.contains("title=Link-Ziel") && item.text.contains("Nur wichtige Highlights davon merken")
        })
    }

}

private class FakeAreaStudioAreaKernelRepository(
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
    val upsertedSnapshots = mutableListOf<AreaSnapshot>()
    val clearedSnapshots = mutableListOf<Pair<String, LocalDate>>()
    val deletedAreaIds = mutableListOf<String>()

    override fun observeBootstrapState(): Flow<AreaKernelBootstrapState> = bootstrapFlow

    override suspend fun ensureStartBootstrap(): AreaKernelBootstrapState = bootstrapFlow.value

    override fun persistenceBoundary(): AreaKernelPersistenceBoundary = bootstrapFlow.value.persistenceBoundary

    override fun observeActiveInstances(): Flow<List<AreaInstance>> = instancesFlow

    override suspend fun loadActiveInstances(): List<AreaInstance> = instancesFlow.value

    override suspend fun createActiveInstance(draft: CreateAreaInstanceDraft): AreaInstance {
        return AreaInstance(
            areaId = "unused",
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
            definitionId = "unused",
            authoringConfig = defaultAreaAuthoringConfig(
                definition = null,
                templateId = draft.templateId,
            ),
            templateId = draft.templateId,
        )
    }

    override suspend fun deleteActiveInstance(areaId: String) {
        deletedAreaIds += areaId
    }

    override suspend fun swapActiveInstanceOrder(firstAreaId: String, secondAreaId: String) = Unit

    override suspend fun moveActiveInstanceEarlier(areaId: String) = Unit

    override suspend fun moveActiveInstanceLater(areaId: String) = Unit

    override fun observeSnapshots(date: LocalDate): Flow<List<AreaSnapshot>> = snapshotsFlow

    override suspend fun updateActiveInstance(instance: AreaInstance) {
        updatedInstances += instance
        instancesFlow.value = instancesFlow.value.map {
            if (it.areaId == instance.areaId) instance else it
        }
    }

    override suspend fun upsertSnapshot(snapshot: AreaSnapshot) {
        upsertedSnapshots += snapshot
        snapshotsFlow.value = snapshotsFlow.value
            .filterNot { it.areaId == snapshot.areaId && it.date == snapshot.date } + snapshot
    }

    override suspend fun clearSnapshot(areaId: String, date: LocalDate) {
        clearedSnapshots += areaId to date
        snapshotsFlow.value = snapshotsFlow.value.filterNot { it.areaId == areaId && it.date == date }
    }
}

private class FakeAreaStudioSourceBindingRepository(
    bindings: List<AreaSourceBinding> = emptyList(),
) : AreaSourceBindingRepository {
    private val bindingsFlow = MutableStateFlow(bindings)
    val boundHistory = mutableListOf<AreaSourceBinding>()
    val unboundHistory = mutableListOf<AreaSourceBinding>()

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
        val binding = AreaSourceBinding(areaId = areaId, source = source)
        unboundHistory += binding
        bindingsFlow.value = bindingsFlow.value.filterNot { it.areaId == areaId && it.source == source }
    }

    override suspend fun clearArea(areaId: String) {
        bindingsFlow.value = bindingsFlow.value.filterNot { it.areaId == areaId }
    }

    override suspend fun clearAll() {
        bindingsFlow.value = emptyList()
    }
}

private class FakeAreaStudioSourceCapabilityRepository(
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

private class FakeAreaStudioCalendarSignalRepository(
    private val signals: List<CalendarSignal> = emptyList(),
) : CalendarSignalRepository {
    override fun observeToday(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<CalendarSignal>> = flowOf(signals)
}

private class FakeAreaStudioNotificationSignalRepository(
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

private class FakeAreaStudioHealthConnectRepository(
    private val observations: List<DomainObservation> = emptyList(),
) : HealthConnectRepository {
    var readCount: Int = 0

    override val requiredPermissions: Set<String> = emptySet()

    override suspend fun availability() = com.struperto.androidappdays.data.repository.HealthConnectAvailability.AVAILABLE

    override suspend fun grantedPermissions(): Set<String> = emptySet()

    override suspend fun readDailyObservations(logicalDate: LocalDate): List<DomainObservation> {
        readCount += 1
        return observations
    }
}

private class FakeAreaStudioCaptureRepository : CaptureRepository {
    val items = MutableStateFlow<List<CaptureItem>>(emptyList())

    override fun observeOpen(): Flow<List<CaptureItem>> = items

    override fun observeArchived(): Flow<List<CaptureItem>> = flowOf(emptyList())

    override fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int> = flowOf(0)

    override fun observeTouchedAreaIdsSince(sinceEpochMillis: Long): Flow<Set<String>> = flowOf(emptySet())

    override suspend fun createTextCapture(text: String, areaId: String?): CaptureItem {
        val item = CaptureItem(
            id = "capture-${items.value.size + 1}",
            text = text,
            areaId = areaId,
            createdAt = 0L,
            updatedAt = 0L,
            status = "open",
        )
        items.value = items.value + item
        return item
    }

    override suspend fun markConverted(id: String) = Unit

    override suspend fun archive(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun updateArea(id: String, areaId: String?) = Unit

    override suspend fun loadLatestOpen(): CaptureItem? = items.value.lastOrNull()

    override suspend fun loadById(id: String): CaptureItem? = items.value.firstOrNull { it.id == id }
}
