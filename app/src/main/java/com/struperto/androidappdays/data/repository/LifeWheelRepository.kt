package com.struperto.androidappdays.data.repository

import androidx.room.withTransaction
import com.struperto.androidappdays.data.local.AreaInstanceEntity
import com.struperto.androidappdays.data.local.AreaKernelDao
import com.struperto.androidappdays.data.local.AreaSnapshotEntity
import com.struperto.androidappdays.data.local.LifeWheelDao
import com.struperto.androidappdays.data.local.SingleDatabase
import com.struperto.androidappdays.data.local.SingleSetupStateEntity
import com.struperto.androidappdays.domain.area.canonicalStartAreaId
import com.struperto.androidappdays.domain.area.defaultAreaAuthoringConfig
import com.struperto.androidappdays.domain.area.defaultAreaDefinitionId
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LifeWheelRepository {
    fun observeSetupState(): Flow<SingleSetupState>
    suspend fun loadSetupState(): SingleSetupState
    fun observeActiveAreas(): Flow<List<LifeArea>>
    fun observeDailyChecks(date: String): Flow<List<LifeAreaDailyCheck>>
    suspend fun loadActiveAreas(): List<LifeArea>
    suspend fun loadAreaInventory(): LifeAreaInventory
    suspend fun ensureSeededAreas()
    suspend fun markSetupConfigured()
    suspend fun completeSetup(areas: List<LifeArea>)
    suspend fun createArea(
        label: String,
        definition: String,
        templateId: String,
        iconKey: String,
    ): String
    suspend fun updateAreaIdentity(
        id: String,
        label: String,
        definition: String,
        templateId: String,
        iconKey: String,
    )
    suspend fun swapAreaOrder(
        firstId: String,
        secondId: String,
    )
    suspend fun moveAreaEarlier(id: String)
    suspend fun moveAreaLater(id: String)
    suspend fun deleteArea(id: String)
    suspend fun updateArea(
        id: String,
        label: String,
        definition: String,
        targetScore: Int,
    )
    suspend fun upsertDailyCheck(
        areaId: String,
        date: String,
        manualScore: Int?,
    )
}

class RoomLifeWheelRepository(
    private val database: SingleDatabase,
    private val lifeWheelDao: LifeWheelDao,
    private val areaKernelDao: AreaKernelDao,
    private val clock: Clock,
) : LifeWheelRepository {
    override fun observeSetupState(): Flow<SingleSetupState> {
        return lifeWheelDao.observeSetupState().map { entity ->
            SingleSetupState(
                isLifeWheelConfigured = entity?.isLifeWheelConfigured == true,
            )
        }
    }

    override suspend fun loadSetupState(): SingleSetupState {
        return SingleSetupState(
            isLifeWheelConfigured = lifeWheelDao.getSetupState()?.isLifeWheelConfigured == true,
        )
    }

    override fun observeActiveAreas(): Flow<List<LifeArea>> {
        return areaKernelDao.observeActiveAreaInstances().map { entities ->
            entities.map(AreaInstanceEntity::toLifeArea)
        }
    }

    override fun observeDailyChecks(date: String): Flow<List<LifeAreaDailyCheck>> {
        return areaKernelDao.observeAreaSnapshots(date).map { entities ->
            entities.mapNotNull(AreaSnapshotEntity::toLifeAreaDailyCheck)
        }
    }

    override suspend fun loadActiveAreas(): List<LifeArea> {
        return areaKernelDao.getActiveAreaInstances().map(AreaInstanceEntity::toLifeArea)
    }

    override suspend fun loadAreaInventory(): LifeAreaInventory {
        val (activeAreas, inactiveAreas) = areaKernelDao
            .getAllAreaInstances()
            .map(AreaInstanceEntity::toLifeArea)
            .partition(LifeArea::isActive)
        return LifeAreaInventory(
            activeAreas = activeAreas.sortedBy(LifeArea::sortOrder),
            inactiveAreas = inactiveAreas.sortedBy(LifeArea::sortOrder),
        )
    }

    override suspend fun ensureSeededAreas() {
        val currentAreas = areaKernelDao.getAllAreaInstances()
        if (currentAreas.isEmpty()) {
            completeSetup(defaultLifeAreas())
            return
        }
        val existingIds = currentAreas.mapTo(mutableSetOf()) { canonicalStartAreaId(it.areaId) }
        val missingAreas = defaultLifeAreas().filterNot { it.id in existingIds }
        if (missingAreas.isEmpty()) {
            if (!loadSetupState().isLifeWheelConfigured) {
                markSetupConfigured()
            }
            return
        }

        val now = clock.millis()
        database.withTransaction {
            val nextSortOrder = currentAreas
                .filter(AreaInstanceEntity::isActive)
                .maxOfOrNull(AreaInstanceEntity::sortOrder)
                ?.plus(1)
                ?: 0
            areaKernelDao.upsertAreaInstances(
                missingAreas.mapIndexed { index, area ->
                    area.toAreaInstanceEntity(
                        createdAt = now,
                        updatedAt = now,
                        sortOrder = nextSortOrder + index,
                    )
                },
            )
            lifeWheelDao.upsertSetupState(
                SingleSetupStateEntity(
                    isLifeWheelConfigured = true,
                    updatedAt = now,
                ),
            )
        }
    }

    override suspend fun markSetupConfigured() {
        lifeWheelDao.upsertSetupState(
            SingleSetupStateEntity(
                isLifeWheelConfigured = true,
                updatedAt = clock.millis(),
            ),
        )
    }

    override suspend fun completeSetup(areas: List<LifeArea>) {
        val now = clock.millis()
        database.withTransaction {
            areaKernelDao.deleteAllAreaSnapshots()
            areaKernelDao.deleteAllAreaInstances()
            areaKernelDao.upsertAreaInstances(
                areas.mapIndexed { index, area ->
                    area.toAreaInstanceEntity(
                        createdAt = now,
                        updatedAt = now,
                        sortOrder = index,
                        areaId = area.id.ifBlank { UUID.randomUUID().toString() },
                    )
                },
            )
            lifeWheelDao.upsertSetupState(
                SingleSetupStateEntity(
                    isLifeWheelConfigured = true,
                    updatedAt = now,
                ),
            )
        }
    }

    override suspend fun createArea(
        label: String,
        definition: String,
        templateId: String,
        iconKey: String,
    ): String {
        val areaId = UUID.randomUUID().toString()
        val now = clock.millis()
        database.withTransaction {
            areaKernelDao.shiftActiveAreaInstancesForward(updatedAt = now)
            areaKernelDao.upsertAreaInstance(
                LifeArea(
                    id = areaId,
                    label = label.trim(),
                    definition = definition.trim(),
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    templateId = templateId,
                    iconKey = iconKey,
                ).toAreaInstanceEntity(
                    createdAt = now,
                    updatedAt = now,
                    sortOrder = 0,
                    areaId = areaId,
                ),
            )
            lifeWheelDao.upsertSetupState(
                SingleSetupStateEntity(
                    isLifeWheelConfigured = true,
                    updatedAt = now,
                ),
            )
        }
        return areaId
    }

    override suspend fun deleteArea(id: String) {
        val activeAreas = areaKernelDao.getActiveAreaInstances().sortedBy(AreaInstanceEntity::sortOrder)
        if (activeAreas.none { it.areaId == id }) return
        val now = clock.millis()
        database.withTransaction {
            areaKernelDao.deactivateAreaInstance(
                areaId = id,
                updatedAt = now,
            )
            activeAreas
                .filterNot { it.areaId == id }
                .forEachIndexed { index, area ->
                    areaKernelDao.updateAreaSortOrder(
                        areaId = area.areaId,
                        sortOrder = index,
                        updatedAt = now,
                    )
                }
            areaKernelDao.deleteAreaSnapshots(id)
        }
    }

    override suspend fun swapAreaOrder(
        firstId: String,
        secondId: String,
    ) {
        if (firstId == secondId) return
        val activeAreas = areaKernelDao.getActiveAreaInstances().sortedBy(AreaInstanceEntity::sortOrder)
        val first = activeAreas.firstOrNull { it.areaId == firstId } ?: return
        val second = activeAreas.firstOrNull { it.areaId == secondId } ?: return
        val now = clock.millis()
        database.withTransaction {
            areaKernelDao.updateAreaSortOrder(
                areaId = first.areaId,
                sortOrder = second.sortOrder,
                updatedAt = now,
            )
            areaKernelDao.updateAreaSortOrder(
                areaId = second.areaId,
                sortOrder = first.sortOrder,
                updatedAt = now,
            )
        }
    }

    override suspend fun updateAreaIdentity(
        id: String,
        label: String,
        definition: String,
        templateId: String,
        iconKey: String,
    ) {
        updateAreaInstance(id) { current ->
            current.copy(
                title = label.trim(),
                summary = definition.trim(),
                templateId = templateId,
                iconKey = iconKey,
                updatedAt = clock.millis(),
            )
        }
    }

    override suspend fun moveAreaEarlier(id: String) {
        moveAreaBy(id = id, step = -1)
    }

    override suspend fun moveAreaLater(id: String) {
        moveAreaBy(id = id, step = 1)
    }

    override suspend fun updateArea(
        id: String,
        label: String,
        definition: String,
        targetScore: Int,
    ) {
        updateAreaInstance(id) { current ->
            current.copy(
                title = label.trim(),
                summary = definition.trim(),
                targetScore = targetScore.coerceIn(1, 5),
                updatedAt = clock.millis(),
            )
        }
    }

    override suspend fun upsertDailyCheck(
        areaId: String,
        date: String,
        manualScore: Int?,
    ) {
        if (manualScore == null) {
            areaKernelDao.deleteAreaSnapshot(areaId, date)
            return
        }
        val now = clock.millis()
        val existing = areaKernelDao.getAreaSnapshot(areaId, date)
        areaKernelDao.upsertAreaSnapshot(
            AreaSnapshotEntity(
                areaId = areaId,
                date = date,
                manualScore = manualScore.coerceIn(1, 5),
                manualStateKey = null,
                manualNote = null,
                confidence = null,
                freshnessAt = null,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    private suspend fun moveAreaBy(
        id: String,
        step: Int,
    ) {
        if (step == 0) return
        val activeAreas = areaKernelDao.getActiveAreaInstances().sortedBy(AreaInstanceEntity::sortOrder)
        val currentIndex = activeAreas.indexOfFirst { it.areaId == id }
        if (currentIndex == -1) return
        val targetIndex = (currentIndex + step).coerceIn(0, activeAreas.lastIndex)
        if (currentIndex == targetIndex) return
        swapAreaOrder(
            firstId = id,
            secondId = activeAreas[targetIndex].areaId,
        )
    }

    private suspend fun updateAreaInstance(
        areaId: String,
        transform: (AreaInstanceEntity) -> AreaInstanceEntity,
    ) {
        val current = areaKernelDao.getAreaInstance(areaId) ?: return
        areaKernelDao.upsertAreaInstance(transform(current))
    }
}

private fun LifeArea.toAreaInstanceEntity(
    createdAt: Long,
    updatedAt: Long,
    sortOrder: Int,
    areaId: String = id,
): AreaInstanceEntity {
    val definitionId = defaultAreaDefinitionId(
        areaId = areaId,
        templateId = templateId,
    )
    val authoringDefaults = defaultAreaAuthoringConfig(
        definition = startAreaKernelDefinition(areaId),
        templateId = templateId,
    )
    return AreaInstanceEntity(
        areaId = areaId,
        definitionId = definitionId,
        title = label.trim(),
        summary = definition.trim(),
        iconKey = iconKey,
        targetScore = targetScore.coerceIn(1, 5),
        sortOrder = sortOrder,
        isActive = isActive,
        cadenceKey = "adaptive",
        selectedTracks = "",
        signalBlend = 60,
        intensity = 3,
        remindersEnabled = false,
        reviewEnabled = true,
        experimentsEnabled = false,
        lageMode = authoringDefaults.lageMode.persistedValue,
        directionMode = authoringDefaults.directionMode.persistedValue,
        sourcesMode = authoringDefaults.sourcesMode.persistedValue,
        flowProfile = authoringDefaults.flowProfile.persistedValue,
        behaviorClass = authoringDefaults.behaviorClass.persistedValue,
        authoringComplexity = authoringDefaults.complexityLevel.name,
        authoringVisibility = authoringDefaults.visibilityLevel.persistedValue,
        confirmedStepKind = null,
        confirmedStepLabel = null,
        confirmedStepDueHint = null,
        confirmedStepLinkedPlanItemId = null,
        confirmedStepLinkedSourceId = null,
        confirmedStepUpdatedAt = null,
        lastReviewedAt = null,
        templateId = templateId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
