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
    tableName = "area_instances",
    indices = [
        Index(value = ["isActive", "sortOrder"]),
    ],
)
data class AreaInstanceEntity(
    @PrimaryKey val areaId: String,
    val definitionId: String,
    val title: String,
    val summary: String,
    val iconKey: String,
    val targetScore: Int,
    val sortOrder: Int,
    val isActive: Boolean,
    val cadenceKey: String,
    val selectedTracks: String,
    val signalBlend: Int,
    val intensity: Int,
    val remindersEnabled: Boolean,
    val reviewEnabled: Boolean,
    val experimentsEnabled: Boolean,
    val lageMode: String,
    val directionMode: String,
    val sourcesMode: String,
    val flowProfile: String,
    val behaviorClass: String = "",
    val authoringComplexity: String,
    val authoringVisibility: String,
    val confirmedStepKind: String? = null,
    val confirmedStepLabel: String? = null,
    val confirmedStepDueHint: String? = null,
    val confirmedStepLinkedPlanItemId: String? = null,
    val confirmedStepLinkedSourceId: String? = null,
    val confirmedStepUpdatedAt: Long? = null,
    val lastReviewedAt: Long? = null,
    val templateId: String?,
    val tileDisplayMode: String = "ampel",
    val familyKey: String = "",
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "area_snapshots",
    primaryKeys = ["areaId", "date"],
    indices = [
        Index(value = ["date"]),
    ],
)
data class AreaSnapshotEntity(
    val areaId: String,
    val date: String,
    val manualScore: Int?,
    val manualStateKey: String?,
    val manualNote: String? = null,
    val confidence: Float?,
    val freshnessAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Dao
interface AreaKernelDao {
    @Query(
        """
        SELECT * FROM area_instances
        WHERE isActive = 1
        ORDER BY sortOrder ASC
        """,
    )
    fun observeActiveAreaInstances(): Flow<List<AreaInstanceEntity>>

    @Query(
        """
        SELECT * FROM area_instances
        WHERE isActive = 1
        ORDER BY sortOrder ASC
        """,
    )
    suspend fun getActiveAreaInstances(): List<AreaInstanceEntity>

    @Query(
        """
        SELECT * FROM area_instances
        ORDER BY sortOrder ASC
        """,
    )
    suspend fun getAllAreaInstances(): List<AreaInstanceEntity>

    @Query(
        """
        SELECT * FROM area_instances
        WHERE areaId = :areaId
        LIMIT 1
        """,
    )
    suspend fun getAreaInstance(areaId: String): AreaInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAreaInstance(entity: AreaInstanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAreaInstances(entities: List<AreaInstanceEntity>)

    @Query(
        """
        UPDATE area_instances
        SET sortOrder = sortOrder + 1,
            updatedAt = :updatedAt
        WHERE isActive = 1
        """,
    )
    suspend fun shiftActiveAreaInstancesForward(updatedAt: Long)

    @Query(
        """
        UPDATE area_instances
        SET sortOrder = :sortOrder,
            updatedAt = :updatedAt
        WHERE areaId = :areaId
        """,
    )
    suspend fun updateAreaSortOrder(
        areaId: String,
        sortOrder: Int,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE area_instances
        SET isActive = 0,
            updatedAt = :updatedAt
        WHERE areaId = :areaId
        """,
    )
    suspend fun deactivateAreaInstance(
        areaId: String,
        updatedAt: Long,
    )

    @Query("DELETE FROM area_instances")
    suspend fun deleteAllAreaInstances()

    @Query(
        """
        SELECT * FROM area_snapshots
        WHERE date = :date
        ORDER BY areaId ASC
        """,
    )
    fun observeAreaSnapshots(date: String): Flow<List<AreaSnapshotEntity>>

    @Query(
        """
        SELECT * FROM area_snapshots
        WHERE areaId = :areaId AND date = :date
        LIMIT 1
        """,
    )
    suspend fun getAreaSnapshot(
        areaId: String,
        date: String,
    ): AreaSnapshotEntity?

    @Query(
        """
        SELECT * FROM area_snapshots
        ORDER BY date ASC, areaId ASC
        """,
    )
    suspend fun getAllAreaSnapshots(): List<AreaSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAreaSnapshot(entity: AreaSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAreaSnapshots(entities: List<AreaSnapshotEntity>)

    @Query("DELETE FROM area_snapshots")
    suspend fun deleteAllAreaSnapshots()

    @Query(
        """
        DELETE FROM area_snapshots
        WHERE areaId = :areaId AND date = :date
        """,
    )
    suspend fun deleteAreaSnapshot(
        areaId: String,
        date: String,
    )

    @Query(
        """
        DELETE FROM area_snapshots
        WHERE areaId = :areaId
        """,
    )
    suspend fun deleteAreaSnapshots(areaId: String)
}
