package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaKernelBootstrapState
import com.struperto.androidappdays.data.repository.AreaKernelPersistenceBoundary
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.data.repository.FakePlanRepository
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
            planRepository = FakePlanRepository(),
            clock = clock,
        )

        val tile = viewModel.state.first { it.areas.isNotEmpty() }.areas.single()

        assertEquals("Vitalitaet", tile.label)
        assertEquals("Signal 4/4", tile.statusLabel)
        assertEquals("heart", tile.iconKey)
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
            planRepository = FakePlanRepository(),
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
        val viewModel = StartViewModel(
            areaKernelRepository = areaKernelRepository,
            planRepository = FakePlanRepository(),
            clock = clock,
        )

        viewModel.createArea(
            title = "Neu",
            meaning = "Neu beschrieben",
            templateId = "free",
            iconKey = "spark",
            behaviorClass = AreaBehaviorClass.REFLECTION,
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
    }
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
