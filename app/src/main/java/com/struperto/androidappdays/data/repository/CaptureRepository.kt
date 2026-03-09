package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.CaptureItemDao
import com.struperto.androidappdays.data.local.CaptureItemEntity
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface CaptureRepository {
    fun observeOpen(): Flow<List<CaptureItem>>
    fun observeArchived(): Flow<List<CaptureItem>>
    fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int>
    fun observeTouchedAreaIdsSince(sinceEpochMillis: Long): Flow<Set<String>>
    suspend fun createTextCapture(
        text: String,
        areaId: String?,
    ): CaptureItem
    suspend fun markConverted(id: String)
    suspend fun archive(id: String)
    suspend fun updateArea(
        id: String,
        areaId: String?,
    )
    suspend fun loadLatestOpen(): CaptureItem?
    suspend fun loadById(id: String): CaptureItem?
}

class RoomCaptureRepository(
    private val captureItemDao: CaptureItemDao,
    private val clock: Clock,
) : CaptureRepository {
    override fun observeOpen(): Flow<List<CaptureItem>> {
        return captureItemDao.observeByStatus(CaptureItemEntity.STATUS_OPEN).map { entities ->
            entities.map(CaptureItemEntity::toModel)
        }
    }

    override fun observeArchived(): Flow<List<CaptureItem>> {
        return captureItemDao.observeByStatus(CaptureItemEntity.STATUS_ARCHIVED).map { entities ->
            entities.map(CaptureItemEntity::toModel)
        }
    }

    override fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int> {
        return captureItemDao.observeWriteCountSince(sinceEpochMillis)
    }

    override fun observeTouchedAreaIdsSince(sinceEpochMillis: Long): Flow<Set<String>> {
        return captureItemDao.observeTouchedAreaIdsSince(sinceEpochMillis).map { ids ->
            ids.filter(String::isNotBlank).toSet()
        }
    }

    override suspend fun createTextCapture(
        text: String,
        areaId: String?,
    ): CaptureItem {
        val trimmedText = text.trim()
        val now = clock.millis()
        val entity = CaptureItemEntity(
            id = UUID.randomUUID().toString(),
            text = trimmedText,
            areaId = areaId,
            createdAt = now,
            updatedAt = now,
            status = CaptureItemEntity.STATUS_OPEN,
        )
        captureItemDao.insert(entity)
        return entity.toModel()
    }

    override suspend fun markConverted(id: String) {
        captureItemDao.updateStatus(
            id = id,
            status = CaptureItemEntity.STATUS_CONVERTED,
            updatedAt = clock.millis(),
        )
    }

    override suspend fun archive(id: String) {
        captureItemDao.updateStatus(
            id = id,
            status = CaptureItemEntity.STATUS_ARCHIVED,
            updatedAt = clock.millis(),
        )
    }

    override suspend fun updateArea(
        id: String,
        areaId: String?,
    ) {
        captureItemDao.updateArea(
            id = id,
            areaId = areaId,
            updatedAt = clock.millis(),
        )
    }

    override suspend fun loadLatestOpen(): CaptureItem? {
        return captureItemDao.getLatestByStatus(CaptureItemEntity.STATUS_OPEN)?.toModel()
    }

    override suspend fun loadById(id: String): CaptureItem? {
        return captureItemDao.getById(id)?.toModel()
    }
}

private fun CaptureItemEntity.toModel(): CaptureItem {
    return CaptureItem(
        id = id,
        text = text,
        areaId = areaId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        status = status,
    )
}
