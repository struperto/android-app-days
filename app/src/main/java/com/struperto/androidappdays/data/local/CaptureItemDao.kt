package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureItemDao {
    @Query(
        """
        SELECT * FROM capture_items
        WHERE status = :status
        ORDER BY createdAt DESC
        """,
    )
    fun observeByStatus(status: String): Flow<List<CaptureItemEntity>>

    @Query(
        """
        SELECT * FROM capture_items
        WHERE id = :id
        LIMIT 1
        """,
    )
    suspend fun getById(id: String): CaptureItemEntity?

    @Query(
        """
        SELECT * FROM capture_items
        WHERE status = :status
        ORDER BY createdAt DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestByStatus(status: String): CaptureItemEntity?

    @Query(
        """
        SELECT COUNT(*) FROM capture_items
        WHERE updatedAt >= :sinceEpochMillis
        """,
    )
    fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int>

    @Query(
        """
        SELECT DISTINCT areaId FROM capture_items
        WHERE updatedAt >= :sinceEpochMillis
          AND areaId IS NOT NULL
        """,
    )
    fun observeTouchedAreaIdsSince(sinceEpochMillis: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CaptureItemEntity)

    @Query(
        """
        UPDATE capture_items
        SET status = :status, updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateStatus(
        id: String,
        status: String,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE capture_items
        SET areaId = :areaId, updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateArea(
        id: String,
        areaId: String?,
        updatedAt: Long,
    )
}
