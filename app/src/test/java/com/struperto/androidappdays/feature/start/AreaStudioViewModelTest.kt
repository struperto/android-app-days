package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaKernelBootstrapState
import com.struperto.androidappdays.data.repository.AreaKernelPersistenceBoundary
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.data.repository.FakePlanRepository
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
                    title = "Vitalitaet",
                    summary = "Schlaf und Energie tragen.",
                    iconKey = "heart",
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
                    templateId = "ritual",
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
            planRepository = FakePlanRepository(),
            clock = clock,
        )

        val areaState = viewModel.state.first { it.areas.isNotEmpty() }.areas.getValue("vitality")
        val detail = areaState.detail

        assertEquals("Vitalitaet", detail.title)
        assertEquals("3/5", detail.statusLabel)
        assertEquals("Energie", detail.focusTrack)
        assertEquals("Stabil", detail.profileState.flowLabel)
        assertEquals("Stabil · Routine · Erinnerung", detail.panelStates.first { it.panel == StartAreaPanel.Options }.summary)
        assertEquals("Seed-Basis", areaState.authoring.basisLabel)
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
            planRepository = FakePlanRepository(),
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
            planRepository = FakePlanRepository(),
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
            planRepository = FakePlanRepository(),
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
            planRepository = FakePlanRepository(),
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
