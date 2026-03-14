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

    @Query(
        """
        SELECT * FROM life_areas
        ORDER BY sortOrder ASC
        """,
    )
    suspend fun getAllAreas(): List<LifeAreaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAreas(entities: List<LifeAreaEntity>)

    @Query(
        """
        UPDATE life_areas
        SET sortOrder = sortOrder + 1
        WHERE isActive = 1
        """,
    )
    suspend fun shiftActiveAreasForward()

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
        UPDATE life_areas
        SET label = :label,
            definition = :definition,
            templateId = :templateId,
            iconKey = :iconKey,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateAreaIdentity(
        id: String,
        label: String,
        definition: String,
        templateId: String,
        iconKey: String,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE life_areas
        SET sortOrder = :sortOrder,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateAreaSortOrder(
        id: String,
        sortOrder: Int,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE life_areas
        SET isActive = 0,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun deactivateArea(
        id: String,
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

    @Query(
        """
        DELETE FROM life_area_daily_checks
        WHERE areaId = :areaId
        """,
    )
    suspend fun deleteDailyChecks(areaId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyCheck(entity: LifeAreaDailyCheckEntity)

    @Query(
        """
        DELETE FROM life_area_profiles
        WHERE areaId = :areaId
        """,
    )
    suspend fun deleteProfile(areaId: String)

    @Query(
        """
        SELECT * FROM single_setup_state
        WHERE id = 0
        LIMIT 1
        """,
    )
    fun observeSetupState(): Flow<SingleSetupStateEntity?>

    @Query(
        """
        SELECT * FROM single_setup_state
        WHERE id = 0
        LIMIT 1
        """,
    )
    suspend fun getSetupState(): SingleSetupStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetupState(entity: SingleSetupStateEntity)
}
