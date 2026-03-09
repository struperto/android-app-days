package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DomainCatalogDao {
    @Query(
        """
        SELECT * FROM domain_catalog
        ORDER BY sortOrder ASC
        """,
    )
    fun observeAll(): Flow<List<DomainCatalogEntity>>

    @Query(
        """
        SELECT * FROM domain_catalog
        ORDER BY sortOrder ASC
        """,
    )
    suspend fun getAll(): List<DomainCatalogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DomainCatalogEntity>)
}
