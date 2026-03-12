package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.ChecklistItemDao
import com.struperto.androidappdays.data.local.ChecklistItemEntity
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ChecklistItem(
    val id: String,
    val areaId: String,
    val title: String,
    val isCompleted: Boolean,
    val dueDate: String?,
    val cadenceKey: String?,
    val sortOrder: Int,
)

interface ChecklistRepository {
    fun observeByArea(areaId: String): Flow<List<ChecklistItem>>

    suspend fun loadByArea(areaId: String): List<ChecklistItem>

    suspend fun addItem(areaId: String, title: String): ChecklistItem

    suspend fun updateItem(item: ChecklistItem)

    suspend fun toggleCompleted(id: String, isCompleted: Boolean)

    suspend fun deleteItem(id: String)

    suspend fun clearArea(areaId: String)
}

class RoomChecklistRepository(
    private val dao: ChecklistItemDao,
    private val clock: Clock,
) : ChecklistRepository {
    override fun observeByArea(areaId: String): Flow<List<ChecklistItem>> {
        return dao.observeByArea(areaId).map { entities ->
            entities.map(ChecklistItemEntity::toDomain)
        }
    }

    override suspend fun loadByArea(areaId: String): List<ChecklistItem> {
        return dao.getByArea(areaId).map(ChecklistItemEntity::toDomain)
    }

    override suspend fun addItem(areaId: String, title: String): ChecklistItem {
        val existing = dao.getByArea(areaId)
        val sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val now = clock.millis()
        val id = UUID.randomUUID().toString()
        val entity = ChecklistItemEntity(
            id = id,
            areaId = areaId,
            title = title.trim(),
            isCompleted = false,
            dueDate = null,
            cadenceKey = null,
            sortOrder = sortOrder,
            createdAt = now,
            updatedAt = now,
        )
        dao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun updateItem(item: ChecklistItem) {
        val now = clock.millis()
        dao.upsert(
            ChecklistItemEntity(
                id = item.id,
                areaId = item.areaId,
                title = item.title,
                isCompleted = item.isCompleted,
                dueDate = item.dueDate,
                cadenceKey = item.cadenceKey,
                sortOrder = item.sortOrder,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun toggleCompleted(id: String, isCompleted: Boolean) {
        dao.setCompleted(id = id, isCompleted = isCompleted, updatedAt = clock.millis())
    }

    override suspend fun deleteItem(id: String) {
        dao.delete(id)
    }

    override suspend fun clearArea(areaId: String) {
        dao.deleteAllForArea(areaId)
    }
}

private fun ChecklistItemEntity.toDomain(): ChecklistItem {
    return ChecklistItem(
        id = id,
        areaId = areaId,
        title = title,
        isCompleted = isCompleted,
        dueDate = dueDate,
        cadenceKey = cadenceKey,
        sortOrder = sortOrder,
    )
}
