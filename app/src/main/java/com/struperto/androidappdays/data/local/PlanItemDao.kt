package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanItemDao {
    @Query(
        """
        SELECT * FROM plan_items
        WHERE plannedDate = :plannedDate
        ORDER BY
            CASE timeBlock
                WHEN 'morgen' THEN 0
                WHEN 'mittag' THEN 1
                WHEN 'nachmittag' THEN 2
                ELSE 3
            END ASC,
            createdAt DESC
        """,
    )
    fun observeForDate(plannedDate: String): Flow<List<PlanItemEntity>>

    @Query(
        """
        SELECT * FROM plan_items
        WHERE plannedDate BETWEEN :startDate AND :endDate
        ORDER BY plannedDate ASC,
            CASE timeBlock
                WHEN 'morgen' THEN 0
                WHEN 'mittag' THEN 1
                WHEN 'nachmittag' THEN 2
                ELSE 3
            END ASC,
            createdAt DESC
        """,
    )
    fun observeForDateRange(
        startDate: String,
        endDate: String,
    ): Flow<List<PlanItemEntity>>

    @Query(
        """
        SELECT * FROM plan_items
        WHERE id = :id
        LIMIT 1
        """,
    )
    suspend fun getById(id: String): PlanItemEntity?

    @Query(
        """
        SELECT COUNT(*) FROM plan_items
        WHERE updatedAt >= :sinceEpochMillis
        """,
    )
    fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int>

    @Query(
        """
        SELECT DISTINCT areaId FROM plan_items
        WHERE plannedDate = :plannedDate
        """,
    )
    fun observePlannedAreaIdsForDate(plannedDate: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlanItemEntity)

    @Query(
        """
        UPDATE plan_items
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
        UPDATE plan_items
        SET timeBlock = :timeBlock, updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateTimeBlock(
        id: String,
        timeBlock: String,
        updatedAt: Long,
    )

    @Query(
        """
        DELETE FROM plan_items
        WHERE id = :id
        """,
    )
    suspend fun deleteById(id: String)
}
