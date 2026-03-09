package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningEventDao {
    @Query(
        """
        SELECT * FROM learning_events
        ORDER BY createdAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<LearningEventEntity>>

    @Query(
        """
        SELECT COUNT(DISTINCT day) FROM learning_events
        """,
    )
    fun observeDiscoveryDayCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LearningEventEntity)
}
