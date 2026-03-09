package com.struperto.androidappdays.data.repository

import androidx.room.withTransaction
import com.struperto.androidappdays.data.local.LifeAreaDailyCheckEntity
import com.struperto.androidappdays.data.local.LifeAreaEntity
import com.struperto.androidappdays.data.local.LifeWheelDao
import com.struperto.androidappdays.data.local.SingleDatabase
import com.struperto.androidappdays.data.local.SingleSetupStateEntity
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LifeWheelRepository {
    fun observeSetupState(): Flow<SingleSetupState>
    fun observeActiveAreas(): Flow<List<LifeArea>>
    fun observeDailyChecks(date: String): Flow<List<LifeAreaDailyCheck>>
    suspend fun loadActiveAreas(): List<LifeArea>
    suspend fun ensureSeededAreas()
    suspend fun completeSetup(areas: List<LifeArea>)
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
    private val clock: Clock,
) : LifeWheelRepository {
    override fun observeSetupState(): Flow<SingleSetupState> {
        return lifeWheelDao.observeSetupState().map { entity ->
            SingleSetupState(
                isLifeWheelConfigured = entity?.isLifeWheelConfigured == true,
            )
        }
    }

    override fun observeActiveAreas(): Flow<List<LifeArea>> {
        return lifeWheelDao.observeActiveAreas().map { entities ->
            entities.map(LifeAreaEntity::toModel)
        }
    }

    override fun observeDailyChecks(date: String): Flow<List<LifeAreaDailyCheck>> {
        return lifeWheelDao.observeDailyChecks(date).map { entities ->
            entities.map(LifeAreaDailyCheckEntity::toModel)
        }
    }

    override suspend fun loadActiveAreas(): List<LifeArea> {
        return lifeWheelDao.getActiveAreas().map(LifeAreaEntity::toModel)
    }

    override suspend fun ensureSeededAreas() {
        val currentAreas = lifeWheelDao.getActiveAreas()
        if (currentAreas.isEmpty()) {
            completeSetup(defaultLifeAreas())
            return
        }
        val existingIds = currentAreas.mapTo(mutableSetOf()) { it.id }
        val missingAreas = defaultLifeAreas().filterNot { it.id in existingIds }
        if (missingAreas.isEmpty()) {
            return
        }
        val now = clock.millis()
        database.withTransaction {
            lifeWheelDao.insertAreas(
                missingAreas.mapIndexed { index, area ->
                    LifeAreaEntity(
                        id = area.id,
                        label = area.label.trim(),
                        definition = area.definition.trim(),
                        targetScore = area.targetScore.coerceIn(1, 5),
                        sortOrder = currentAreas.size + index,
                        isActive = true,
                        createdAt = now,
                        updatedAt = now,
                    )
                },
            )
        }
    }

    override suspend fun completeSetup(areas: List<LifeArea>) {
        val now = clock.millis()
        database.withTransaction {
            lifeWheelDao.deleteAllAreas()
            lifeWheelDao.insertAreas(
                areas.mapIndexed { index, area ->
                    LifeAreaEntity(
                        id = area.id.ifBlank { UUID.randomUUID().toString() },
                        label = area.label.trim(),
                        definition = area.definition.trim(),
                        targetScore = area.targetScore.coerceIn(1, 5),
                        sortOrder = index,
                        isActive = true,
                        createdAt = now,
                        updatedAt = now,
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

    override suspend fun updateArea(
        id: String,
        label: String,
        definition: String,
        targetScore: Int,
    ) {
        lifeWheelDao.updateArea(
            id = id,
            label = label.trim(),
            definition = definition.trim(),
            targetScore = targetScore.coerceIn(1, 5),
            updatedAt = clock.millis(),
        )
    }

    override suspend fun upsertDailyCheck(
        areaId: String,
        date: String,
        manualScore: Int?,
    ) {
        if (manualScore == null) {
            lifeWheelDao.deleteDailyCheck(areaId = areaId, date = date)
            return
        }
        val now = clock.millis()
        lifeWheelDao.upsertDailyCheck(
            LifeAreaDailyCheckEntity(
                areaId = areaId,
                date = date,
                manualScore = manualScore.coerceIn(1, 5),
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}

private fun LifeAreaEntity.toModel(): LifeArea {
    return LifeArea(
        id = id,
        label = label,
        definition = definition,
        targetScore = targetScore,
        sortOrder = sortOrder,
        isActive = isActive,
    )
}

private fun LifeAreaDailyCheckEntity.toModel(): LifeAreaDailyCheck {
    return LifeAreaDailyCheck(
        areaId = areaId,
        date = date,
        manualScore = manualScore,
    )
}
