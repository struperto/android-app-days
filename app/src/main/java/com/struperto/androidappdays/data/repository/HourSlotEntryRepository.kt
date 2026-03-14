package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.HourSlotEntryDao
import com.struperto.androidappdays.data.local.HourSlotEntryEntity
import com.struperto.androidappdays.domain.HourSlotEntry
import com.struperto.androidappdays.domain.HourSlotStatus
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HourSlotEntryRepository {
    fun observeDay(logicalDate: LocalDate): Flow<List<HourSlotEntry>>
    suspend fun loadDay(logicalDate: LocalDate): List<HourSlotEntry>
    suspend fun saveStatus(
        logicalDate: LocalDate,
        segmentId: String,
        logicalHour: Int,
        windowId: String,
        status: HourSlotStatus,
    )

    suspend fun saveNote(
        logicalDate: LocalDate,
        segmentId: String,
        logicalHour: Int,
        windowId: String,
        note: String,
    )

    suspend fun clearAll()
}

class RoomHourSlotEntryRepository(
    private val dao: HourSlotEntryDao,
    private val clock: Clock,
) : HourSlotEntryRepository {
    override fun observeDay(logicalDate: LocalDate): Flow<List<HourSlotEntry>> {
        return dao.observeForLogicalDate(logicalDate.toString()).map { entities ->
            entities.map(HourSlotEntryEntity::toModel)
        }
    }

    override suspend fun loadDay(logicalDate: LocalDate): List<HourSlotEntry> {
        return dao.getForLogicalDate(logicalDate.toString()).map(HourSlotEntryEntity::toModel)
    }

    override suspend fun saveStatus(
        logicalDate: LocalDate,
        segmentId: String,
        logicalHour: Int,
        windowId: String,
        status: HourSlotStatus,
    ) {
        val current = dao.getById(entryId(logicalDate, segmentId))
        dao.insert(
            HourSlotEntryEntity(
                id = entryId(logicalDate, segmentId),
                logicalDate = logicalDate.toString(),
                segmentId = segmentId,
                logicalHour = logicalHour,
                windowId = windowId,
                status = status.name,
                note = current?.note.orEmpty(),
                updatedAt = clock.millis(),
            ),
        )
    }

    override suspend fun saveNote(
        logicalDate: LocalDate,
        segmentId: String,
        logicalHour: Int,
        windowId: String,
        note: String,
    ) {
        val current = dao.getById(entryId(logicalDate, segmentId))
        dao.insert(
            HourSlotEntryEntity(
                id = entryId(logicalDate, segmentId),
                logicalDate = logicalDate.toString(),
                segmentId = segmentId,
                logicalHour = logicalHour,
                windowId = windowId,
                status = current?.status ?: HourSlotStatus.UNKNOWN.name,
                note = note.trim(),
                updatedAt = clock.millis(),
            ),
        )
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }
}

private fun HourSlotEntryEntity.toModel(): HourSlotEntry {
    return HourSlotEntry(
        id = id,
        logicalDate = LocalDate.parse(logicalDate),
        segmentId = segmentId,
        logicalHour = logicalHour,
        windowId = windowId,
        status = HourSlotStatus.valueOf(status),
        note = note,
    )
}

private fun entryId(
    logicalDate: LocalDate,
    segmentId: String,
): String {
    return "${logicalDate}_$segmentId"
}
