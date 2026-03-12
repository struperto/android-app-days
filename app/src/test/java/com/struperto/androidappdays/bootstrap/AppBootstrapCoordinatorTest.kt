package com.struperto.androidappdays.bootstrap

import com.struperto.androidappdays.data.repository.AreaKernelBootstrapState
import com.struperto.androidappdays.data.repository.AreaKernelPersistenceBoundary
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.GoalRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DomainCatalogEntry
import com.struperto.androidappdays.domain.DomainGoal
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppBootstrapCoordinatorTest {
    @Test
    fun ensureBootstrapped_runsAreaAndAppSeedingOnceAndExposesReadyState() = runTest {
        val areaRepository = FakeBootstrapAreaKernelRepository()
        val goalRepository = FakeGoalRepository()
        val sourceCapabilityRepository = FakeSourceCapabilityRepository()
        val coordinator = DefaultAppBootstrapCoordinator(
            areaKernelRepository = areaRepository,
            goalRepository = goalRepository,
            sourceCapabilityRepository = sourceCapabilityRepository,
        )

        coordinator.ensureBootstrapped()
        coordinator.ensureBootstrapped()

        val state = coordinator.state.value
        assertTrue(state.isReady)
        assertEquals(1, areaRepository.ensureCalls)
        assertEquals(1, goalRepository.ensureCalls)
        assertEquals(1, sourceCapabilityRepository.ensureCalls)
        assertTrue(state.areaBootstrapState?.isBootstrapped == true)
    }
}

private class FakeBootstrapAreaKernelRepository : AreaKernelRepository {
    private val bootstrapState = AreaKernelBootstrapState(
        activeInstanceCount = 11,
        missingProfileAreaIds = emptySet(),
        hasLegacySetupRecord = true,
        persistenceBoundary = AreaKernelPersistenceBoundary(
            activeInstanceStores = emptySet(),
            snapshotStores = emptySet(),
            bootSetupStores = emptySet(),
        ),
    )

    var ensureCalls = 0

    override fun observeBootstrapState(): Flow<AreaKernelBootstrapState> {
        return MutableStateFlow(bootstrapState)
    }

    override suspend fun ensureStartBootstrap(): AreaKernelBootstrapState {
        ensureCalls += 1
        return bootstrapState
    }

    override fun persistenceBoundary(): AreaKernelPersistenceBoundary {
        return bootstrapState.persistenceBoundary
    }

    override fun observeActiveInstances() = emptyFlow<List<com.struperto.androidappdays.domain.area.AreaInstance>>()

    override suspend fun loadActiveInstances() = emptyList<com.struperto.androidappdays.domain.area.AreaInstance>()

    override suspend fun createActiveInstance(draft: com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft) =
        throw UnsupportedOperationException()

    override suspend fun deleteActiveInstance(areaId: String) = Unit

    override suspend fun swapActiveInstanceOrder(firstAreaId: String, secondAreaId: String) = Unit

    override suspend fun moveActiveInstanceEarlier(areaId: String) = Unit

    override suspend fun moveActiveInstanceLater(areaId: String) = Unit

    override fun observeSnapshots(date: LocalDate) = emptyFlow<List<com.struperto.androidappdays.domain.area.AreaSnapshot>>()

    override suspend fun updateActiveInstance(instance: com.struperto.androidappdays.domain.area.AreaInstance) = Unit

    override suspend fun upsertSnapshot(snapshot: com.struperto.androidappdays.domain.area.AreaSnapshot) = Unit

    override suspend fun clearSnapshot(areaId: String, date: LocalDate) = Unit
}

private class FakeGoalRepository : GoalRepository {
    var ensureCalls = 0

    override fun observeGoals(): Flow<List<DomainGoal>> = emptyFlow()

    override fun observeActiveGoals(): Flow<List<DomainGoal>> = emptyFlow()

    override fun observeCatalog(): Flow<List<DomainCatalogEntry>> = emptyFlow()

    override suspend fun loadActiveGoals(): List<DomainGoal> = emptyList()

    override suspend fun ensureSeeded() {
        ensureCalls += 1
    }

    override suspend fun save(goal: DomainGoal) = Unit
}

private class FakeSourceCapabilityRepository : SourceCapabilityRepository {
    var ensureCalls = 0

    override fun observeProfile(): Flow<CapabilityProfile> = emptyFlow()

    override suspend fun loadProfile(): CapabilityProfile {
        throw UnsupportedOperationException()
    }

    override suspend fun ensureSeeded() {
        ensureCalls += 1
    }

    override suspend fun setEnabled(
        source: com.struperto.androidappdays.domain.DataSourceKind,
        enabled: Boolean,
    ) = Unit
}
