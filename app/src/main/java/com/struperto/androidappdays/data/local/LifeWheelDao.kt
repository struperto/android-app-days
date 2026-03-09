package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeWheelDao {
    @Query(
        """
        SELECT * FROM life_areas
        WHERE isActive = 1
        ORDER BY sortOrder ASC
        """,
    )
    fun observeActiveAreas(): Flow<List<LifeAreaEntity>>

    @Query(
        """
        SELECT * FROM life_areas
        WHERE isActive = 1
        ORDER BY sortOrder ASC
        """,
    )
    suspend fun getActiveAreas(): List<LifeAreaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAreas(entities: List<LifeAreaEntity>)

    @Query("DELETE FROM life_areas")
    suspend fun deleteAllAreas()

    @Query(
        """
        UPDATE life_areas
        SET label = :label,
            definition = :definition,
            targetScore = :targetScore,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateArea(
        id: String,
        label: String,
        definition: String,
        targetScore: Int,
        updatedAt: Long,
    )

    @Query(
        """
        SELECT * FROM life_area_daily_checks
        WHERE date = :date
        """,
    )
    fun observeDailyChecks(date: String): Flow<List<LifeAreaDailyCheckEntity>>

    @Query(
        """
        SELECT * FROM life_area_daily_checks
        WHERE areaId = :areaId AND date = :date
        LIMIT 1
        """,
    )
    suspend fun getDailyCheck(
        areaId: String,
        date: String,
    ): LifeAreaDailyCheckEntity?

    @Query(
        """
        DELETE FROM life_area_daily_checks
        WHERE areaId = :areaId AND date = :date
        """,
    )
    suspend fun deleteDailyCheck(
        areaId: String,
        date: String,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyCheck(entity: LifeAreaDailyCheckEntity)

    @Query(
        """
        SELECT * FROM single_setup_state
        WHERE id = 0
        LIMIT 1
        """,
    )
    fun observeSetupState(): Flow<SingleSetupStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetupState(entity: SingleSetupStateEntity)
}
