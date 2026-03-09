package com.struperto.androidappdays.data.repository

import androidx.room.withTransaction
import com.struperto.androidappdays.data.local.DomainCatalogDao
import com.struperto.androidappdays.data.local.DomainCatalogEntity
import com.struperto.androidappdays.data.local.DomainGoalDao
import com.struperto.androidappdays.data.local.DomainGoalEntity
import com.struperto.androidappdays.data.local.SingleDatabase
import com.struperto.androidappdays.domain.AdaptationMode
import com.struperto.androidappdays.domain.DomainCatalogEntry
import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.GoalCadence
import com.struperto.androidappdays.domain.GoalPriority
import com.struperto.androidappdays.domain.GoalTarget
import com.struperto.androidappdays.domain.GoalWindow
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.TargetKind
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GoalRepository {
    fun observeGoals(): Flow<List<DomainGoal>>
    fun observeActiveGoals(): Flow<List<DomainGoal>>
    fun observeCatalog(): Flow<List<DomainCatalogEntry>>
    suspend fun loadActiveGoals(): List<DomainGoal>
    suspend fun ensureSeeded()
    suspend fun save(goal: DomainGoal)
}

class RoomGoalRepository(
    private val database: SingleDatabase,
    private val goalDao: DomainGoalDao,
    private val catalogDao: DomainCatalogDao,
    private val clock: Clock,
) : GoalRepository {
    override fun observeGoals(): Flow<List<DomainGoal>> {
        return goalDao.observeAll().map { entities ->
            entities.map(DomainGoalEntity::toModel)
        }
    }

    override fun observeActiveGoals(): Flow<List<DomainGoal>> {
        return goalDao.observeActive().map { entities ->
            entities.map(DomainGoalEntity::toModel)
        }
    }

    override fun observeCatalog(): Flow<List<DomainCatalogEntry>> {
        return catalogDao.observeAll().map { entities ->
            entities.map(DomainCatalogEntity::toModel)
        }
    }

    override suspend fun loadActiveGoals(): List<DomainGoal> {
        return goalDao.getAll()
            .map(DomainGoalEntity::toModel)
            .filter(DomainGoal::isActive)
    }

    override suspend fun ensureSeeded() {
        val now = clock.millis()
        database.withTransaction {
            val existingGoals = goalDao.getAll().associateBy { it.id }
            val missingGoals = defaultDomainGoals()
                .filterNot { goal -> existingGoals.containsKey(goal.id) }
                .mapIndexed { index, goal ->
                    goal.toEntity(
                        createdAt = now + index,
                        updatedAt = now + index,
                    )
                }
            if (missingGoals.isNotEmpty()) {
                goalDao.insertAll(missingGoals)
            }

            val existingCatalog = catalogDao.getAll().associateBy { entity ->
                LifeDomain.valueOf(entity.domain)
            }
            catalogDao.insertAll(
                defaultDomainCatalog().mapIndexed { index, entry ->
                    val existing = existingCatalog[entry.domain]
                    entry.toEntity(
                        sortOrder = index,
                        createdAt = existing?.createdAt ?: now + index,
                        updatedAt = now + index,
                    )
                },
            )
        }
    }

    override suspend fun save(goal: DomainGoal) {
        val existing = goalDao.getAll().firstOrNull { it.id == goal.id }
        goalDao.insert(
            goal.toEntity(
                createdAt = existing?.createdAt ?: clock.millis(),
                updatedAt = clock.millis(),
            ),
        )
    }
}

private fun DomainGoalEntity.toModel(): DomainGoal {
    return DomainGoal(
        id = id,
        domain = LifeDomain.valueOf(domain),
        title = title,
        cadence = GoalCadence.valueOf(cadence),
        target = GoalTarget(
            kind = TargetKind.valueOf(targetKind),
            unit = unit,
            minimum = minimum,
            maximum = maximum,
            exact = exact,
            note = note,
        ),
        adaptationMode = AdaptationMode.valueOf(adaptationMode),
        preferredWindow = if (preferredStartHour != null && preferredEndHourExclusive != null) {
            GoalWindow(
                startLogicalHour = preferredStartHour,
                endLogicalHourExclusive = preferredEndHourExclusive,
            )
        } else {
            null
        },
        priority = GoalPriority.valueOf(priority),
        isActive = isActive,
        rationale = rationale,
    )
}

private fun DomainGoal.toEntity(
    createdAt: Long,
    updatedAt: Long,
): DomainGoalEntity {
    return DomainGoalEntity(
        id = id,
        domain = domain.name,
        title = title,
        cadence = cadence.name,
        targetKind = target.kind.name,
        unit = target.unit,
        minimum = target.minimum,
        maximum = target.maximum,
        exact = target.exact,
        note = target.note,
        adaptationMode = adaptationMode.name,
        preferredStartHour = preferredWindow?.startLogicalHour,
        preferredEndHourExclusive = preferredWindow?.endLogicalHourExclusive,
        priority = priority.name,
        isActive = isActive,
        rationale = rationale,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun DomainCatalogEntity.toModel(): DomainCatalogEntry {
    return DomainCatalogEntry(
        domain = LifeDomain.valueOf(domain),
        title = title,
        summary = summary,
        priority = GoalPriority.valueOf(priority),
        isActive = isActive,
        isImplemented = isImplemented,
    )
}

private fun DomainCatalogEntry.toEntity(
    sortOrder: Int,
    createdAt: Long,
    updatedAt: Long,
): DomainCatalogEntity {
    return DomainCatalogEntity(
        domain = domain.name,
        title = title,
        summary = summary,
        priority = priority.name,
        isActive = isActive,
        isImplemented = isImplemented,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
