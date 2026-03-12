package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.AreaSourceBindingDao
import com.struperto.androidappdays.data.local.AreaSourceBindingEntity
import com.struperto.androidappdays.domain.DataSourceKind
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AreaSourceBinding(
    val areaId: String,
    val source: DataSourceKind,
)

interface AreaSourceBindingRepository {
    fun observeAll(): Flow<List<AreaSourceBinding>>

    fun observeByArea(areaId: String): Flow<List<AreaSourceBinding>>

    suspend fun loadAll(): List<AreaSourceBinding>

    suspend fun bind(
        areaId: String,
        source: DataSourceKind,
    )

    suspend fun unbind(
        areaId: String,
        source: DataSourceKind,
    )

    suspend fun clearArea(areaId: String)

    suspend fun clearAll()
}

class RoomAreaSourceBindingRepository(
    private val dao: AreaSourceBindingDao,
    private val clock: Clock,
) : AreaSourceBindingRepository {
    override fun observeAll(): Flow<List<AreaSourceBinding>> {
        return dao.observeAll().map { entities ->
            entities.map(AreaSourceBindingEntity::toDomain)
        }
    }

    override fun observeByArea(areaId: String): Flow<List<AreaSourceBinding>> {
        return dao.observeByArea(areaId).map { entities ->
            entities.map(AreaSourceBindingEntity::toDomain)
        }
    }

    override suspend fun loadAll(): List<AreaSourceBinding> {
        return dao.getAll().map(AreaSourceBindingEntity::toDomain)
    }

    override suspend fun bind(
        areaId: String,
        source: DataSourceKind,
    ) {
        val now = clock.millis()
        dao.upsert(
            AreaSourceBindingEntity(
                areaId = areaId,
                source = source.name,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun unbind(
        areaId: String,
        source: DataSourceKind,
    ) {
        dao.delete(
            areaId = areaId,
            source = source.name,
        )
    }

    override suspend fun clearArea(areaId: String) {
        dao.deleteAllForArea(areaId)
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }
}

private fun AreaSourceBindingEntity.toDomain(): AreaSourceBinding {
    return AreaSourceBinding(
        areaId = areaId,
        source = DataSourceKind.valueOf(source),
    )
}
