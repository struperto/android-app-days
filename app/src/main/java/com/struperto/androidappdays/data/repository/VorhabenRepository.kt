package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.VorhabenDao
import com.struperto.androidappdays.data.local.VorhabenEntity
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface VorhabenRepository {
    fun observeActive(): Flow<List<Vorhaben>>
    fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int>
    fun observeActiveAreaIds(): Flow<Set<String>>
    suspend fun create(
        title: String,
        note: String,
        areaId: String,
    ): Vorhaben
    suspend fun createFromCapture(
        captureId: String,
        title: String,
        note: String,
        areaId: String,
    ): Vorhaben
    suspend fun archive(id: String)
    suspend fun loadById(id: String): Vorhaben?
}

class RoomVorhabenRepository(
    private val vorhabenDao: VorhabenDao,
    private val clock: Clock,
) : VorhabenRepository {
    override fun observeActive(): Flow<List<Vorhaben>> {
        return vorhabenDao.observeByStatus(VorhabenEntity.STATUS_ACTIVE).map { entities ->
            entities.map(VorhabenEntity::toModel)
        }
    }

    override fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int> {
        return vorhabenDao.observeWriteCountSince(sinceEpochMillis)
    }

    override fun observeActiveAreaIds(): Flow<Set<String>> {
        return vorhabenDao.observeActiveAreaIds().map { ids ->
            ids.filter(String::isNotBlank).toSet()
        }
    }

    override suspend fun create(
        title: String,
        note: String,
        areaId: String,
    ): Vorhaben {
        val now = clock.millis()
        val entity = VorhabenEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            note = note.trim(),
            areaId = areaId,
            sourceCaptureId = null,
            status = VorhabenEntity.STATUS_ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        vorhabenDao.insert(entity)
        return entity.toModel()
    }

    override suspend fun createFromCapture(
        captureId: String,
        title: String,
        note: String,
        areaId: String,
    ): Vorhaben {
        val now = clock.millis()
        val entity = VorhabenEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            note = note.trim(),
            areaId = areaId,
            sourceCaptureId = captureId,
            status = VorhabenEntity.STATUS_ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        vorhabenDao.insert(entity)
        return entity.toModel()
    }

    override suspend fun archive(id: String) {
        vorhabenDao.updateStatus(
            id = id,
            status = VorhabenEntity.STATUS_ARCHIVED,
            updatedAt = clock.millis(),
        )
    }

    override suspend fun loadById(id: String): Vorhaben? {
        return vorhabenDao.getById(id)?.toModel()
    }
}

private fun VorhabenEntity.toModel(): Vorhaben {
    return Vorhaben(
        id = id,
        title = title,
        note = note,
        areaId = areaId,
        sourceCaptureId = sourceCaptureId,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
