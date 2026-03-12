package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "checklist_items",
    indices = [
        Index(value = ["areaId", "sortOrder"]),
    ],
)
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val areaId: String,
    val title: String,
    val isCompleted: Boolean,
    val dueDate: String?,
    val cadenceKey: String?,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Dao
interface ChecklistItemDao {
    @Query(
        """
        SELECT * FROM checklist_items
        WHERE areaId = :areaId
        ORDER BY sortOrder ASC
        """,
    )
    fun observeByArea(areaId: String): Flow<List<ChecklistItemEntity>>

    @Query(
        """
        SELECT * FROM checklist_items
        WHERE areaId = :areaId
        ORDER BY sortOrder ASC
        """,
    )
    suspend fun getByArea(areaId: String): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ChecklistItemEntity)

    @Query(
        """
        DELETE FROM checklist_items
        WHERE id = :id
        """,
    )
    suspend fun delete(id: String)

    @Query(
        """
        DELETE FROM checklist_items
        WHERE areaId = :areaId
        """,
    )
    suspend fun deleteAllForArea(areaId: String)

    @Query(
        """
        UPDATE checklist_items
        SET isCompleted = :isCompleted, updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun setCompleted(
        id: String,
        isCompleted: Boolean,
        updatedAt: Long,
    )
}
