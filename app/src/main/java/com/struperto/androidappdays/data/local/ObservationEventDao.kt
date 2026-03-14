package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationEventDao {
    @Query(
        """
        SELECT * FROM observation_events
        WHERE logicalDate = :logicalDate
        ORDER BY startedAt ASC
        """,
    )
    fun observeForLogicalDate(logicalDate: String): Flow<List<ObservationEventEntity>>

    @Query(
        """
        SELECT * FROM observation_events
        WHERE logicalDate BETWEEN :startLogicalDate AND :endLogicalDate
        ORDER BY startedAt ASC
        """,
    )
    fun observeRange(
        startLogicalDate: String,
        endLogicalDate: String,
    ): Flow<List<ObservationEventEntity>>

    @Query(
        """
        SELECT * FROM observation_events
        WHERE logicalDate BETWEEN :startLogicalDate AND :endLogicalDate
        ORDER BY startedAt ASC
        """,
    )
    suspend fun getRange(
        startLogicalDate: String,
        endLogicalDate: String,
    ): List<ObservationEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ObservationEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ObservationEventEntity>)

    @Query("DELETE FROM observation_events")
    suspend fun deleteAll()
}
