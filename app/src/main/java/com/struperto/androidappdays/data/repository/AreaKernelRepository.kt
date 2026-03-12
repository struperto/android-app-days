package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.AreaInstanceEntity
import com.struperto.androidappdays.data.local.AreaKernelDao
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaDefaultConfig
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaSkillKind
import com.struperto.androidappdays.domain.area.TileDisplayMode
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.mapping.toAreaInstance
import com.struperto.androidappdays.domain.area.mapping.toAreaInstanceWithFallbackDefaults
import com.struperto.androidappdays.domain.area.mapping.toAreaSnapshot
import com.struperto.androidappdays.domain.area.mapping.toLifeAreaProfile
import com.struperto.androidappdays.domain.area.defaultAreaProfileConfig
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class AreaKernelPersistenceStore {
    AREA_INSTANCE_RECORDS,
    AREA_SNAPSHOT_RECORDS,
    LEGACY_SINGLE_SETUP_STATE,
}

data class AreaKernelPersistenceBoundary(
    val activeInstanceStores: Set<AreaKernelPersistenceStore>,
    val snapshotStores: Set<AreaKernelPersistenceStore>,
    val bootSetupStores: Set<AreaKernelPersistenceStore>,
)

data class AreaKernelBootstrapState(
    val activeInstanceCount: Int,
    val missingProfileAreaIds: Set<String>,
    val hasLegacySetupRecord: Boolean,
    val persistenceBoundary: AreaKernelPersistenceBoundary,
) {
    val hasActiveInstances: Boolean
        get() = activeInstanceCount > 0

    val hasPersistedProfilesForAllActiveInstances: Boolean
        get() = missingProfileAreaIds.isEmpty()

    val isBootstrapped: Boolean
        get() = hasActiveInstances &&
            hasPersistedProfilesForAllActiveInstances &&
            hasLegacySetupRecord
}

/**
 * Kernel-near persistence facade for active area configuration and day-bound snapshots.
 *
 * This repository exposes [AreaInstance] and [AreaSnapshot] through the current Room-backed
 * Start persistence line while keeping the active Start UI stable.
 */
interface AreaKernelRepository {
    fun observeBootstrapState(): Flow<AreaKernelBootstrapState>

    suspend fun ensureStartBootstrap(): AreaKernelBootstrapState

    fun persistenceBoundary(): AreaKernelPersistenceBoundary

    fun observeActiveInstances(): Flow<List<AreaInstance>>

    suspend fun loadActiveInstances(): List<AreaInstance>

    /**
     * Creates one active Start area instance through the current Room-backed persistence path.
     *
     * Normal Start runtime lifecycle should use this facade instead of calling legacy repositories
     * directly. Boot/setup seeding is handled separately through [ensureStartBootstrap].
     */
    suspend fun createActiveInstance(draft: CreateAreaInstanceDraft): AreaInstance

    suspend fun deleteActiveInstance(areaId: String)

    suspend fun swapActiveInstanceOrder(
        firstAreaId: String,
        secondAreaId: String,
    )

    suspend fun moveActiveInstanceEarlier(areaId: String)

    suspend fun moveActiveInstanceLater(areaId: String)

    fun observeSnapshots(date: LocalDate): Flow<List<AreaSnapshot>>

    /**
     * Persists the currently supported mutable subset of one active instance.
     *
     * Normal in-app Start lifecycle, ordering, and activation state should use the dedicated
     * methods on this facade. Boot/setup seeding stays on [ensureStartBootstrap].
     */
    suspend fun updateActiveInstance(instance: AreaInstance)

    /**
     * Persists one Start snapshot through the current snapshot record store.
     */
    suspend fun upsertSnapshot(snapshot: AreaSnapshot)

    suspend fun clearSnapshot(
        areaId: String,
        date: LocalDate,
    )
}

data class CreateAreaInstanceDraft(
    val title: String,
    val summary: String,
    val templateId: String,
    val iconKey: String,
    val behaviorClass: AreaBehaviorClass,
    val skills: Set<AreaSkillKind> = emptySet(),
    val tileDisplayMode: TileDisplayMode = TileDisplayMode.AMPEL,
    val familyKey: String = "",
)

class RoomBackedAreaKernelRepository(
    private val lifeWheelRepository: LifeWheelRepository,
    private val lifeAreaProfileRepository: LifeAreaProfileRepository,
    private val areaKernelDao: AreaKernelDao? = null,
) : AreaKernelRepository {
    override fun observeBootstrapState(): Flow<AreaKernelBootstrapState> {
        return combine(
            lifeWheelRepository.observeActiveAreas(),
            lifeAreaProfileRepository.observeProfiles(),
            lifeWheelRepository.observeSetupState(),
        ) { areas, profiles, setupState ->
            buildBootstrapState(
                activeAreas = areas,
                persistedProfileAreaIds = profiles.mapTo(linkedSetOf(), LifeAreaProfile::areaId),
                hasLegacySetupRecord = setupState.isLifeWheelConfigured,
            )
        }
    }

    override suspend fun ensureStartBootstrap(): AreaKernelBootstrapState {
        lifeWheelRepository.ensureSeededAreas()
        val activeAreas = lifeWheelRepository.loadActiveAreas()
        ensurePersistedProfiles(activeAreas)
        if (activeAreas.isNotEmpty()) {
            lifeWheelRepository.markSetupConfigured()
        }
        return buildBootstrapState(
            activeAreas = activeAreas,
            persistedProfileAreaIds = loadPersistedProfileAreaIds(),
            hasLegacySetupRecord = lifeWheelRepository.loadSetupState().isLifeWheelConfigured,
        )
    }

    override fun persistenceBoundary(): AreaKernelPersistenceBoundary {
        return ROOM_AREA_KERNEL_PERSISTENCE_BOUNDARY
    }

    override fun observeActiveInstances(): Flow<List<AreaInstance>> {
        val dao = areaKernelDao
        if (dao != null) {
            return dao.observeActiveAreaInstances().map { entities ->
                entities.map(AreaInstanceEntity::toAreaInstance)
            }
        }
        return combine(
            lifeWheelRepository.observeActiveAreas(),
            lifeAreaProfileRepository.observeProfiles(),
        ) { areas, profiles ->
            val profilesByAreaId = profiles.associateBy(LifeAreaProfile::areaId)
            areas.map { area ->
                area.toKernelInstance(profilesByAreaId[area.id])
            }
        }
    }

    override suspend fun loadActiveInstances(): List<AreaInstance> {
        val dao = areaKernelDao
        if (dao != null) {
            return dao.getActiveAreaInstances().map(AreaInstanceEntity::toAreaInstance)
        }
        val profilesByAreaId = lifeAreaProfileRepository
            .observeProfiles()
            .first()
            .associateBy(LifeAreaProfile::areaId)
        return lifeWheelRepository.loadActiveAreas().map { area ->
            area.toKernelInstance(profilesByAreaId[area.id])
        }
    }

    override suspend fun createActiveInstance(draft: CreateAreaInstanceDraft): AreaInstance {
        val areaId = lifeWheelRepository.createArea(
            label = draft.title,
            definition = draft.summary,
            templateId = draft.templateId,
            iconKey = draft.iconKey,
        )
        val createdEntity = areaKernelDao?.getAreaInstance(areaId)
        if (createdEntity != null) {
            val created = createdEntity.toAreaInstance()
            val updated = created.copy(
                authoringConfig = created.authoringConfig.copy(
                    behaviorClass = draft.behaviorClass,
                ),
                tileDisplayMode = draft.tileDisplayMode,
                familyKey = draft.familyKey,
            )
            updateActiveInstance(updated)
            return requireNotNull(areaKernelDao.getAreaInstance(areaId)).toAreaInstance()
        }
        val createdArea = requireNotNull(
            lifeWheelRepository.loadActiveAreas().firstOrNull { it.id == areaId },
        ) {
            "Expected created area $areaId to be active immediately after creation."
        }
        val profile = createdArea.defaultKernelProfile()
        lifeAreaProfileRepository.saveProfile(profile)
        val created = createdArea.toKernelInstance(profile)
        val updated = created.copy(
            authoringConfig = created.authoringConfig.copy(
                behaviorClass = draft.behaviorClass,
            ),
            tileDisplayMode = draft.tileDisplayMode,
            familyKey = draft.familyKey,
        )
        updateActiveInstance(updated)
        return updated
    }

    override suspend fun deleteActiveInstance(areaId: String) {
        lifeWheelRepository.deleteArea(areaId)
    }

    override suspend fun swapActiveInstanceOrder(
        firstAreaId: String,
        secondAreaId: String,
    ) {
        lifeWheelRepository.swapAreaOrder(
            firstId = firstAreaId,
            secondId = secondAreaId,
        )
    }

    override suspend fun moveActiveInstanceEarlier(areaId: String) {
        lifeWheelRepository.moveAreaEarlier(areaId)
    }

    override suspend fun moveActiveInstanceLater(areaId: String) {
        lifeWheelRepository.moveAreaLater(areaId)
    }

    override fun observeSnapshots(date: LocalDate): Flow<List<AreaSnapshot>> {
        return areaKernelDao
            ?.observeAreaSnapshots(date.toString())
            ?.map { entities ->
                entities.map { entity -> entity.toAreaSnapshot() }
            }
            ?: lifeWheelRepository.observeDailyChecks(date.toString()).map { dailyChecks ->
                dailyChecks.map { it.toAreaSnapshot() }
            }
    }

    override suspend fun updateActiveInstance(instance: AreaInstance) {
        val templateId = requireNotNull(instance.templateId) {
            "Current Start persistence requires templateId on AreaInstance."
        }
        val dao = areaKernelDao
        if (dao != null) {
            val current = requireNotNull(dao.getAreaInstance(instance.areaId)) {
                "Expected persisted AreaInstanceEntity for ${instance.areaId}."
            }
            dao.upsertAreaInstance(
                current.copy(
                    definitionId = instance.definitionId,
                    title = instance.title,
                    summary = instance.summary,
                    iconKey = instance.iconKey,
                    targetScore = instance.targetScore,
                    cadenceKey = instance.cadenceKey,
                    selectedTracks = encodeSelectedTracks(instance.selectedTracks),
                    signalBlend = instance.signalBlend,
                    intensity = instance.intensity,
                    remindersEnabled = instance.remindersEnabled,
                    reviewEnabled = instance.reviewEnabled,
                    experimentsEnabled = instance.experimentsEnabled,
                    lageMode = instance.authoringConfig.lageMode.persistedValue,
                    directionMode = instance.authoringConfig.directionMode.persistedValue,
                    sourcesMode = instance.authoringConfig.sourcesMode.persistedValue,
                    flowProfile = instance.authoringConfig.flowProfile.persistedValue,
                    behaviorClass = instance.authoringConfig.behaviorClass.persistedValue,
                    authoringComplexity = instance.authoringConfig.complexityLevel.name,
                    authoringVisibility = instance.authoringConfig.visibilityLevel.persistedValue,
                    tileDisplayMode = instance.tileDisplayMode.persistedValue,
                    familyKey = instance.familyKey,
                    confirmedStepKind = instance.confirmedNextStep?.kind?.name,
                    confirmedStepLabel = instance.confirmedNextStep?.label,
                    confirmedStepDueHint = instance.confirmedNextStep?.dueHint,
                    confirmedStepLinkedPlanItemId = instance.confirmedNextStep?.linkedPlanItemId,
                    confirmedStepLinkedSourceId = instance.confirmedNextStep?.linkedSourceId,
                    confirmedStepUpdatedAt = if (instance.confirmedNextStep != null) {
                        System.currentTimeMillis()
                    } else {
                        null
                    },
                    lastReviewedAt = instance.lastReviewedAt?.toEpochMilli(),
                    templateId = templateId,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            return
        }
        lifeWheelRepository.updateAreaIdentity(
            id = instance.areaId,
            label = instance.title,
            definition = instance.summary,
            templateId = templateId,
            iconKey = instance.iconKey,
        )
        lifeWheelRepository.updateArea(
            id = instance.areaId,
            label = instance.title,
            definition = instance.summary,
            targetScore = instance.targetScore,
        )
        lifeAreaProfileRepository.saveProfile(instance.toLifeAreaProfile())
    }

    override suspend fun upsertSnapshot(snapshot: AreaSnapshot) {
        val dao = areaKernelDao
        if (dao != null) {
            val existing = dao.getAreaSnapshot(
                areaId = snapshot.areaId,
                date = snapshot.date.toString(),
            )
            dao.upsertAreaSnapshot(
                snapshot.toEntity(existingCreatedAt = existing?.createdAt),
            )
        } else {
            val legacyScore = snapshot.manualScore ?: return
            lifeWheelRepository.upsertDailyCheck(
                areaId = snapshot.areaId,
                date = snapshot.date.toString(),
                manualScore = legacyScore,
            )
        }
    }

    override suspend fun clearSnapshot(
        areaId: String,
        date: LocalDate,
    ) {
        val dao = areaKernelDao
        if (dao != null) {
            dao.deleteAreaSnapshot(areaId = areaId, date = date.toString())
        } else {
            lifeWheelRepository.upsertDailyCheck(
                areaId = areaId,
                date = date.toString(),
                manualScore = null,
            )
        }
    }

    private suspend fun ensurePersistedProfiles(activeAreas: List<LifeArea>) {
        val existingProfileAreaIds = loadPersistedProfileAreaIds()
        activeAreas
            .filterNot { it.id in existingProfileAreaIds }
            .forEach { area ->
                lifeAreaProfileRepository.saveProfile(area.defaultKernelProfile())
            }
    }

    private suspend fun loadPersistedProfileAreaIds(): Set<String> {
        return lifeAreaProfileRepository.observeProfiles()
            .first()
            .mapTo(linkedSetOf(), LifeAreaProfile::areaId)
    }
}

private val ROOM_AREA_KERNEL_PERSISTENCE_BOUNDARY = AreaKernelPersistenceBoundary(
    activeInstanceStores = setOf(
        AreaKernelPersistenceStore.AREA_INSTANCE_RECORDS,
    ),
    snapshotStores = setOf(AreaKernelPersistenceStore.AREA_SNAPSHOT_RECORDS),
    bootSetupStores = setOf(
        AreaKernelPersistenceStore.AREA_INSTANCE_RECORDS,
        AreaKernelPersistenceStore.LEGACY_SINGLE_SETUP_STATE,
    ),
)

private fun buildBootstrapState(
    activeAreas: List<LifeArea>,
    persistedProfileAreaIds: Set<String>,
    hasLegacySetupRecord: Boolean,
): AreaKernelBootstrapState {
    return AreaKernelBootstrapState(
        activeInstanceCount = activeAreas.size,
        missingProfileAreaIds = activeAreas
            .map(LifeArea::id)
            .filterNot { it in persistedProfileAreaIds }
            .toCollection(linkedSetOf()),
        hasLegacySetupRecord = hasLegacySetupRecord,
        persistenceBoundary = ROOM_AREA_KERNEL_PERSISTENCE_BOUNDARY,
    )
}

typealias LegacyBackedAreaKernelRepository = RoomBackedAreaKernelRepository

private fun LifeArea.toKernelInstance(profile: LifeAreaProfile?): AreaInstance {
    val definition = startAreaKernelDefinition(id)
    return when {
        definition != null -> toAreaInstance(definition = definition, profile = profile)
        profile != null -> toAreaInstance(profile)
        else -> toAreaInstanceWithFallbackDefaults()
    }
}

private fun LifeArea.defaultKernelProfile(): LifeAreaProfile {
    val definition = startAreaKernelDefinition(id)
    val defaults = definition?.defaultConfig ?: AreaDefaultConfig(
        targetScore = targetScore,
    )
    val profileDefaults = defaultAreaProfileConfig(
        definition = definition,
        templateId = templateId,
    )
    return LifeAreaProfile(
        areaId = id,
        cadence = defaults.cadenceKey,
        intensity = defaults.intensity,
        signalBlend = defaults.signalBlend,
        selectedTracks = defaults.defaultSelectedTracks,
        remindersEnabled = defaults.remindersEnabled,
        reviewEnabled = defaults.reviewEnabled,
        experimentsEnabled = defaults.experimentsEnabled,
        lageMode = profileDefaults.lageMode.persistedValue,
        directionMode = profileDefaults.directionMode.persistedValue,
        sourcesMode = profileDefaults.sourcesMode.persistedValue,
        flowProfile = profileDefaults.flowProfile.persistedValue,
    )
}
