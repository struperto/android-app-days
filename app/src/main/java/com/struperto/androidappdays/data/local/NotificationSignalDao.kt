package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationSignalDao {
    @Query(
        """
        SELECT * FROM notification_signals
        WHERE postedAt >= :startMillis AND postedAt < :endMillis
          AND removedAt IS NULL
        ORDER BY postedAt DESC
        """,
    )
    fun observeActiveForRange(
        startMillis: Long,
        endMillis: Long,
    ): Flow<List<NotificationSignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationSignalEntity)

    @Query(
        """
        UPDATE notification_signals
        SET removedAt = :removedAt, updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun markRemoved(
        id: String,
        removedAt: Long,
        updatedAt: Long,
    )
}
