package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.PlanItemDao
import com.struperto.androidappdays.data.local.PlanItemEntity
import com.struperto.androidappdays.data.local.VorhabenDao
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PlanRepository {
    fun observeToday(date: String): Flow<List<PlanItem>>
    fun observeRange(
        startDate: String,
        endDate: String,
    ): Flow<List<PlanItem>>
    fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int>
    suspend fun addFromVorhaben(
        vorhabenId: String,
        timeBlock: TimeBlock,
    )
    suspend fun addManual(
        title: String,
        note: String,
        areaId: String,
        timeBlock: TimeBlock,
    )
    suspend fun loadById(id: String): PlanItem?
    suspend fun toggleDone(id: String)
    suspend fun moveToTimeBlock(
        id: String,
        timeBlock: TimeBlock,
    )
    suspend fun removeFromToday(id: String)
}

class RoomPlanRepository(
    private val planItemDao: PlanItemDao,
    private val vorhabenDao: VorhabenDao,
    private val clock: Clock,
) : PlanRepository {
    override fun observeToday(date: String): Flow<List<PlanItem>> {
        return planItemDao.observeForDate(date).map { entities ->
            entities.map(PlanItemEntity::toModel)
        }
    }

    override fun observeRange(
        startDate: String,
        endDate: String,
    ): Flow<List<PlanItem>> {
        return planItemDao.observeForDateRange(
            startDate = startDate,
            endDate = endDate,
        ).map { entities ->
            entities.map(PlanItemEntity::toModel)
        }
    }

    override fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int> {
        return planItemDao.observeWriteCountSince(sinceEpochMillis)
    }

    override suspend fun addFromVorhaben(
        vorhabenId: String,
        timeBlock: TimeBlock,
    ) {
        val vorhaben = vorhabenDao.getById(vorhabenId) ?: return
        val now = clock.millis()
        planItemDao.insert(
            PlanItemEntity(
                id = UUID.randomUUID().toString(),
                vorhabenId = vorhaben.id,
                title = vorhaben.title,
                note = vorhaben.note,
                areaId = vorhaben.areaId,
                timeBlock = timeBlock.persistedValue,
                plannedDate = LocalDate.now(clock).toString(),
                status = PlanItemEntity.STATUS_OPEN,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun addManual(
        title: String,
        note: String,
        areaId: String,
        timeBlock: TimeBlock,
    ) {
        val now = clock.millis()
        planItemDao.insert(
            PlanItemEntity(
                id = UUID.randomUUID().toString(),
                vorhabenId = null,
                title = title.trim(),
                note = note.trim(),
                areaId = areaId,
                timeBlock = timeBlock.persistedValue,
                plannedDate = LocalDate.now(clock).toString(),
                status = PlanItemEntity.STATUS_OPEN,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun loadById(id: String): PlanItem? {
        return planItemDao.getById(id)?.toModel()
    }

    override suspend fun toggleDone(id: String) {
        val current = planItemDao.getById(id) ?: return
        val nextStatus = if (current.status == PlanItemEntity.STATUS_DONE) {
            PlanItemEntity.STATUS_OPEN
        } else {
            PlanItemEntity.STATUS_DONE
        }
        planItemDao.updateStatus(
            id = id,
            status = nextStatus,
            updatedAt = clock.millis(),
        )
    }

    override suspend fun moveToTimeBlock(
        id: String,
        timeBlock: TimeBlock,
    ) {
        planItemDao.updateTimeBlock(
            id = id,
            timeBlock = timeBlock.persistedValue,
            updatedAt = clock.millis(),
        )
    }

    override suspend fun removeFromToday(id: String) {
        planItemDao.deleteById(id)
    }
}

private fun PlanItemEntity.toModel(): PlanItem {
    return PlanItem(
        id = id,
        vorhabenId = vorhabenId,
        title = title,
        note = note,
        areaId = areaId,
        timeBlock = TimeBlock.fromPersistedValue(timeBlock),
        plannedDate = plannedDate,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
