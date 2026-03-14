package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SourcePreferenceDao {
    @Query(
        """
        SELECT * FROM source_preferences
        ORDER BY source ASC
        """,
    )
    fun observeAll(): Flow<List<SourcePreferenceEntity>>

    @Query(
        """
        SELECT * FROM source_preferences
        ORDER BY source ASC
        """,
    )
    suspend fun getAll(): List<SourcePreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SourcePreferenceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SourcePreferenceEntity)
}
