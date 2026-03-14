package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.NotificationSignalDao
import com.struperto.androidappdays.data.local.NotificationSignalEntity
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NotificationSignalRepository {
    fun observeToday(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<NotificationSignal>>

    suspend fun upsert(
        id: String,
        packageName: String,
        title: String,
        text: String,
        postedAt: Long,
    )

    suspend fun markRemoved(
        id: String,
        removedAt: Long,
    )

    suspend fun clearAll()
}

class RoomNotificationSignalRepository(
    private val dao: NotificationSignalDao,
    private val clock: Clock,
) : NotificationSignalRepository {
    override fun observeToday(
        date: LocalDate,
        zoneId: ZoneId,
    ): Flow<List<NotificationSignal>> {
        val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return dao.observeActiveForRange(
            startMillis = startMillis,
            endMillis = endMillis,
        ).map { entities ->
            entities.map(NotificationSignalEntity::toModel)
        }
    }

    override suspend fun upsert(
        id: String,
        packageName: String,
        title: String,
        text: String,
        postedAt: Long,
    ) {
        dao.insert(
            NotificationSignalEntity(
                id = id,
                packageName = packageName,
                title = title,
                text = text,
                postedAt = postedAt,
                removedAt = null,
                updatedAt = clock.millis(),
            ),
        )
    }

    override suspend fun markRemoved(
        id: String,
        removedAt: Long,
    ) {
        dao.markRemoved(
            id = id,
            removedAt = removedAt,
            updatedAt = clock.millis(),
        )
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }
}

private fun NotificationSignalEntity.toModel(): NotificationSignal {
    return NotificationSignal(
        id = id,
        packageName = packageName,
        title = title,
        text = text,
        postedAt = postedAt,
        removedAt = removedAt,
    )
}
