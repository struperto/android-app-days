package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "area_skill_bindings",
    primaryKeys = ["areaId", "skillKind"],
    indices = [
        Index(value = ["areaId"]),
        Index(value = ["skillKind"]),
    ],
)
data class AreaSkillBindingEntity(
    val areaId: String,
    val skillKind: String,
    val configJson: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Dao
interface AreaSkillBindingDao {
    @Query(
        """
        SELECT * FROM area_skill_bindings
        ORDER BY areaId ASC, skillKind ASC
        """,
    )
    fun observeAll(): Flow<List<AreaSkillBindingEntity>>

    @Query(
        """
        SELECT * FROM area_skill_bindings
        WHERE areaId = :areaId
        ORDER BY skillKind ASC
        """,
    )
    fun observeByArea(areaId: String): Flow<List<AreaSkillBindingEntity>>

    @Query(
        """
        SELECT * FROM area_skill_bindings
        ORDER BY areaId ASC, skillKind ASC
        """,
    )
    suspend fun getAll(): List<AreaSkillBindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AreaSkillBindingEntity)

    @Query(
        """
        DELETE FROM area_skill_bindings
        WHERE areaId = :areaId AND skillKind = :skillKind
        """,
    )
    suspend fun delete(
        areaId: String,
        skillKind: String,
    )

    @Query(
        """
        DELETE FROM area_skill_bindings
        WHERE areaId = :areaId
        """,
    )
    suspend fun deleteAllForArea(areaId: String)

    @Query(
        """
        DELETE FROM area_skill_bindings
        """,
    )
    suspend fun deleteAll()
}
