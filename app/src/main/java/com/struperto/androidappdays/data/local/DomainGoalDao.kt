package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DomainGoalDao {
    @Query(
        """
        SELECT * FROM domain_goals
        ORDER BY isActive DESC, createdAt ASC
        """,
    )
    fun observeAll(): Flow<List<DomainGoalEntity>>

    @Query(
        """
        SELECT * FROM domain_goals
        WHERE isActive = 1
        ORDER BY createdAt ASC
        """,
    )
    fun observeActive(): Flow<List<DomainGoalEntity>>

    @Query(
        """
        SELECT * FROM domain_goals
        ORDER BY createdAt ASC
        """,
    )
    suspend fun getAll(): List<DomainGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DomainGoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DomainGoalEntity)
}
