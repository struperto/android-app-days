package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.LearningEventDao
import com.struperto.androidappdays.data.local.LearningEventEntity
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LearningEventRepository {
    fun observeRecent(limit: Int = 32): Flow<List<LearningEvent>>
    fun observeDiscoveryDayCount(): Flow<Int>
    suspend fun record(
        type: LearningEventType,
        title: String,
        detail: String,
    )
}

class RoomLearningEventRepository(
    private val dao: LearningEventDao,
    private val clock: Clock,
) : LearningEventRepository {
    override fun observeRecent(limit: Int): Flow<List<LearningEvent>> {
        return dao.observeRecent(limit).map { entities ->
            entities.map(LearningEventEntity::toModel)
        }
    }

    override fun observeDiscoveryDayCount(): Flow<Int> {
        return dao.observeDiscoveryDayCount()
    }

    override suspend fun record(
        type: LearningEventType,
        title: String,
        detail: String,
    ) {
        dao.insert(
            LearningEventEntity(
                id = UUID.randomUUID().toString(),
                type = type.name,
                title = title.trim(),
                detail = detail.trim(),
                createdAt = clock.millis(),
                day = LocalDate.now(clock).toString(),
            ),
        )
    }
}

private fun LearningEventEntity.toModel(): LearningEvent {
    return LearningEvent(
        id = id,
        type = LearningEventType.valueOf(type),
        title = title,
        detail = detail,
        createdAt = createdAt,
        day = day,
    )
}
