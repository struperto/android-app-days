package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.AreaSkillBindingDao
import com.struperto.androidappdays.data.local.AreaSkillBindingEntity
import com.struperto.androidappdays.domain.area.AreaSkillKind
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AreaSkillBinding(
    val areaId: String,
    val skillKind: AreaSkillKind,
    val configJson: String = "",
    val isActive: Boolean = true,
)

interface AreaSkillBindingRepository {
    fun observeAll(): Flow<List<AreaSkillBinding>>

    fun observeByArea(areaId: String): Flow<List<AreaSkillBinding>>

    suspend fun loadAll(): List<AreaSkillBinding>

    suspend fun bind(
        areaId: String,
        skillKind: AreaSkillKind,
        configJson: String = "",
    )

    suspend fun unbind(
        areaId: String,
        skillKind: AreaSkillKind,
    )

    suspend fun updateConfig(
        areaId: String,
        skillKind: AreaSkillKind,
        configJson: String,
    )

    suspend fun clearArea(areaId: String)
}

class RoomAreaSkillBindingRepository(
    private val dao: AreaSkillBindingDao,
    private val sourceBindingRepository: AreaSourceBindingRepository,
    private val clock: Clock,
) : AreaSkillBindingRepository {
    override fun observeAll(): Flow<List<AreaSkillBinding>> {
        return dao.observeAll().map { entities ->
            entities.mapNotNull(AreaSkillBindingEntity::toDomain)
        }
    }

    override fun observeByArea(areaId: String): Flow<List<AreaSkillBinding>> {
        return dao.observeByArea(areaId).map { entities ->
            entities.mapNotNull(AreaSkillBindingEntity::toDomain)
        }
    }

    override suspend fun loadAll(): List<AreaSkillBinding> {
        return dao.getAll().mapNotNull(AreaSkillBindingEntity::toDomain)
    }

    override suspend fun bind(
        areaId: String,
        skillKind: AreaSkillKind,
        configJson: String,
    ) {
        val now = clock.millis()
        dao.upsert(
            AreaSkillBindingEntity(
                areaId = areaId,
                skillKind = skillKind.persistedValue,
                configJson = configJson,
                isActive = true,
                createdAt = now,
                updatedAt = now,
            ),
        )
        skillKind.sourceKind?.let { sourceKind ->
            sourceBindingRepository.bind(areaId = areaId, source = sourceKind)
        }
    }

    override suspend fun unbind(
        areaId: String,
        skillKind: AreaSkillKind,
    ) {
        dao.delete(areaId = areaId, skillKind = skillKind.persistedValue)
        skillKind.sourceKind?.let { sourceKind ->
            sourceBindingRepository.unbind(areaId = areaId, source = sourceKind)
        }
    }

    override suspend fun updateConfig(
        areaId: String,
        skillKind: AreaSkillKind,
        configJson: String,
    ) {
        val now = clock.millis()
        dao.upsert(
            AreaSkillBindingEntity(
                areaId = areaId,
                skillKind = skillKind.persistedValue,
                configJson = configJson,
                isActive = true,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun clearArea(areaId: String) {
        dao.deleteAllForArea(areaId)
    }
}

private fun AreaSkillBindingEntity.toDomain(): AreaSkillBinding? {
    val kind = AreaSkillKind.fromPersistedValue(skillKind) ?: return null
    return AreaSkillBinding(
        areaId = areaId,
        skillKind = kind,
        configJson = configJson,
        isActive = isActive,
    )
}
