package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VorhabenDao {
    @Query(
        """
        SELECT * FROM vorhaben
        WHERE status = :status
        ORDER BY createdAt DESC
        """,
    )
    fun observeByStatus(status: String): Flow<List<VorhabenEntity>>

    @Query(
        """
        SELECT * FROM vorhaben
        WHERE id = :id
        LIMIT 1
        """,
    )
    suspend fun getById(id: String): VorhabenEntity?

    @Query(
        """
        SELECT COUNT(*) FROM vorhaben
        WHERE updatedAt >= :sinceEpochMillis
        """,
    )
    fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int>

    @Query(
        """
        SELECT DISTINCT areaId FROM vorhaben
        WHERE status = :status
        """,
    )
    fun observeActiveAreaIds(status: String = VorhabenEntity.STATUS_ACTIVE): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VorhabenEntity)

    @Query(
        """
        UPDATE vorhaben
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
        UPDATE vorhaben
        SET title = :title,
            note = :note,
            areaId = :areaId,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateContent(
        id: String,
        title: String,
        note: String,
        areaId: String,
        updatedAt: Long,
    )
}
