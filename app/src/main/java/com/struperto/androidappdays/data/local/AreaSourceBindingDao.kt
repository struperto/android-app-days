package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "area_source_bindings",
    primaryKeys = ["areaId", "source"],
    indices = [
        Index(value = ["areaId"]),
        Index(value = ["source"]),
    ],
)
data class AreaSourceBindingEntity(
    val areaId: String,
    val source: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Dao
interface AreaSourceBindingDao {
    @Query(
        """
        SELECT * FROM area_source_bindings
        ORDER BY areaId ASC, source ASC
        """,
    )
    fun observeAll(): Flow<List<AreaSourceBindingEntity>>

    @Query(
        """
        SELECT * FROM area_source_bindings
        WHERE areaId = :areaId
        ORDER BY source ASC
        """,
    )
    fun observeByArea(areaId: String): Flow<List<AreaSourceBindingEntity>>

    @Query(
        """
        SELECT * FROM area_source_bindings
        ORDER BY areaId ASC, source ASC
        """,
    )
    suspend fun getAll(): List<AreaSourceBindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AreaSourceBindingEntity)

    @Query(
        """
        DELETE FROM area_source_bindings
        WHERE areaId = :areaId AND source = :source
        """,
    )
    suspend fun delete(
        areaId: String,
        source: String,
    )

    @Query(
        """
        DELETE FROM area_source_bindings
        WHERE areaId = :areaId
        """,
    )
    suspend fun deleteAllForArea(areaId: String)

    @Query(
        """
        DELETE FROM area_source_bindings
        """,
    )
    suspend fun deleteAll()
}
