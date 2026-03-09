package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.ObservationEventDao
import com.struperto.androidappdays.data.local.ObservationEventEntity
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ObservationRepository {
    fun observeDay(logicalDate: LocalDate): Flow<List<DomainObservation>>
    fun observeRange(
        startLogicalDate: LocalDate,
        endLogicalDate: LocalDate,
    ): Flow<List<DomainObservation>>

    suspend fun loadRange(
        startLogicalDate: LocalDate,
        endLogicalDate: LocalDate,
    ): List<DomainObservation>

    suspend fun upsert(observation: DomainObservation)
    suspend fun upsertAll(observations: List<DomainObservation>)
    suspend fun clearAll()
    suspend fun saveManualNumeric(
        logicalDate: LocalDate,
        domain: LifeDomain,
        metric: ObservationMetric,
        value: Float?,
        unit: String,
        goalId: String? = null,
    )
}

class RoomObservationRepository(
    private val dao: ObservationEventDao,
    private val clock: Clock,
) : ObservationRepository {
    override fun observeDay(logicalDate: LocalDate): Flow<List<DomainObservation>> {
        return dao.observeForLogicalDate(logicalDate.toString()).map { entities ->
            entities.map(ObservationEventEntity::toModel)
        }
    }

    override fun observeRange(
        startLogicalDate: LocalDate,
        endLogicalDate: LocalDate,
    ): Flow<List<DomainObservation>> {
        return dao.observeRange(
            startLogicalDate = startLogicalDate.toString(),
            endLogicalDate = endLogicalDate.toString(),
        ).map { entities ->
            entities.map(ObservationEventEntity::toModel)
        }
    }

    override suspend fun loadRange(
        startLogicalDate: LocalDate,
        endLogicalDate: LocalDate,
    ): List<DomainObservation> {
        return dao.getRange(
            startLogicalDate = startLogicalDate.toString(),
            endLogicalDate = endLogicalDate.toString(),
        ).map(ObservationEventEntity::toModel)
    }

    override suspend fun upsert(observation: DomainObservation) {
        dao.insert(
            observation.toEntity(
                updatedAt = clock.millis(),
            ),
        )
    }

    override suspend fun upsertAll(observations: List<DomainObservation>) {
        dao.insertAll(
            observations.map { observation ->
                observation.toEntity(
                    updatedAt = clock.millis(),
                )
            },
        )
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }

    override suspend fun saveManualNumeric(
        logicalDate: LocalDate,
        domain: LifeDomain,
        metric: ObservationMetric,
        value: Float?,
        unit: String,
        goalId: String?,
    ) {
        if (value == null) {
            return
        }
        val instant = logicalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        upsert(
            DomainObservation(
                id = "manual_${logicalDate}_$metric",
                goalId = goalId,
                domain = domain,
                metric = metric,
                source = ObservationSource.USER_INPUT,
                startedAt = instant,
                value = DomainObservationValue(
                    numeric = value,
                    unit = unit,
                ),
                logicalDate = logicalDate,
                sourceRecordId = "manual_${logicalDate}_$metric",
                confidence = 1f,
                contextTags = setOf("manual"),
            ),
        )
    }
}

private fun ObservationEventEntity.toModel(): DomainObservation {
    return DomainObservation(
        id = id,
        goalId = goalId,
        domain = LifeDomain.valueOf(domain),
        metric = ObservationMetric.valueOf(metric),
        source = ObservationSource.valueOf(source),
        startedAt = Instant.ofEpochMilli(startedAt),
        endedAt = endedAt?.let(Instant::ofEpochMilli),
        value = DomainObservationValue(
            numeric = numericValue,
            boolean = booleanValue,
            text = textValue,
            unit = unit,
        ),
        logicalDate = logicalDate?.let(LocalDate::parse),
        sourceRecordId = sourceRecordId,
        confidence = confidence,
        contextTags = contextTags.split('|').filter(String::isNotBlank).toSet(),
    )
}

private fun DomainObservation.toEntity(
    updatedAt: Long,
): ObservationEventEntity {
    return ObservationEventEntity(
        id = id.ifBlank { UUID.randomUUID().toString() },
        goalId = goalId,
        domain = domain.name,
        metric = metric.name,
        source = source.name,
        startedAt = startedAt.toEpochMilli(),
        endedAt = endedAt?.toEpochMilli(),
        numericValue = value.numeric,
        booleanValue = value.boolean,
        textValue = value.text,
        unit = value.unit,
        logicalDate = logicalDate?.toString(),
        sourceRecordId = sourceRecordId,
        confidence = confidence.coerceIn(0f, 1f),
        contextTags = contextTags.joinToString("|"),
        updatedAt = updatedAt,
    )
}
