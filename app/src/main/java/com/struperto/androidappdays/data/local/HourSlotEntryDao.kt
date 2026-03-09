package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HourSlotEntryDao {
    @Query(
        """
        SELECT * FROM hour_slot_entries
        WHERE logicalDate = :logicalDate
        ORDER BY logicalHour ASC, updatedAt DESC
        """,
    )
    fun observeForLogicalDate(logicalDate: String): Flow<List<HourSlotEntryEntity>>

    @Query(
        """
        SELECT * FROM hour_slot_entries
        WHERE logicalDate = :logicalDate
        ORDER BY logicalHour ASC, updatedAt DESC
        """,
    )
    suspend fun getForLogicalDate(logicalDate: String): List<HourSlotEntryEntity>

    @Query(
        """
        SELECT * FROM hour_slot_entries
        WHERE id = :id
        LIMIT 1
        """,
    )
    suspend fun getById(id: String): HourSlotEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HourSlotEntryEntity)

    @Query("DELETE FROM hour_slot_entries")
    suspend fun deleteAll()
}
