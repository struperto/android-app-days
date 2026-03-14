package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeAreaProfileDao {
    @Query(
        """
        SELECT * FROM life_area_profiles
        ORDER BY areaId ASC
        """,
    )
    fun observeProfiles(): Flow<List<LifeAreaProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(entity: LifeAreaProfileEntity)
}
