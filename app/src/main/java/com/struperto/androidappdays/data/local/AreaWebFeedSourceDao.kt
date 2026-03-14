package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "area_web_feed_sources",
    primaryKeys = ["areaId", "url"],
    indices = [
        Index(value = ["areaId"]),
    ],
)
data class AreaWebFeedSourceEntity(
    val areaId: String,
    val url: String,
    val sourceKind: String,
    val isAutoSyncEnabled: Boolean,
    val syncCadence: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long?,
    val lastStatusLabel: String,
    val lastStatusDetail: String,
)

@Dao
interface AreaWebFeedSourceDao {
    @Query(
        """
        SELECT * FROM area_web_feed_sources
        ORDER BY areaId ASC, createdAt ASC, url ASC
        """,
    )
    fun observeAll(): Flow<List<AreaWebFeedSourceEntity>>

    @Query(
        """
        SELECT * FROM area_web_feed_sources
        WHERE areaId = :areaId
        ORDER BY createdAt ASC, url ASC
        """,
    )
    fun observeByArea(areaId: String): Flow<List<AreaWebFeedSourceEntity>>

    @Query(
        """
        SELECT * FROM area_web_feed_sources
        ORDER BY areaId ASC, createdAt ASC, url ASC
        """,
    )
    suspend fun getAll(): List<AreaWebFeedSourceEntity>

    @Query(
        """
        SELECT * FROM area_web_feed_sources
        WHERE areaId = :areaId
        ORDER BY createdAt ASC, url ASC
        """,
    )
    suspend fun getByArea(areaId: String): List<AreaWebFeedSourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AreaWebFeedSourceEntity)

    @Query(
        """
        DELETE FROM area_web_feed_sources
        WHERE areaId = :areaId AND url = :url
        """,
    )
    suspend fun delete(
        areaId: String,
        url: String,
    )

    @Query(
        """
        DELETE FROM area_web_feed_sources
        WHERE areaId = :areaId
        """,
    )
    suspend fun deleteAllForArea(areaId: String)
}
