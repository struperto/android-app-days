package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.LearningEventDao
import com.struperto.androidappdays.data.local.LifeAreaEntity
import com.struperto.androidappdays.data.local.LifeWheelDao
import com.struperto.androidappdays.data.local.UserFingerprintDao
import com.struperto.androidappdays.data.local.UserFingerprintEntity
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface UserFingerprintRepository {
    fun observe(): Flow<UserFingerprint>
    suspend fun load(): UserFingerprint
    suspend fun save(draft: UserFingerprintDraft)
    suspend fun markDiscoveryCommitted()
}

class RoomUserFingerprintRepository(
    private val dao: UserFingerprintDao,
    private val lifeWheelDao: LifeWheelDao,
    private val learningEventDao: LearningEventDao,
    private val clock: Clock,
) : UserFingerprintRepository {
    override fun observe(): Flow<UserFingerprint> {
        return combine(
            dao.observe(),
            lifeWheelDao.observeActiveAreas(),
            learningEventDao.observeDiscoveryDayCount(),
        ) { entity, areas, discoveryDayCount ->
            buildFingerprint(
                entity = entity ?: defaultEntity(clock),
                lifeAreas = areas.map(LifeAreaEntity::toModel),
                discoveryDay = discoveryDayCount.coerceIn(1, 7),
            )
        }
    }

    override suspend fun load(): UserFingerprint {
        return buildFingerprint(
            entity = dao.get() ?: defaultEntity(clock),
            lifeAreas = lifeWheelDao.getActiveAreas().map(LifeAreaEntity::toModel),
            discoveryDay = 1,
        )
    }

    override suspend fun save(draft: UserFingerprintDraft) {
        val current = dao.get() ?: defaultEntity(clock)
        dao.upsert(
            current.copy(
                roles = draft.rolesText.trim(),
                responsibilities = draft.responsibilitiesText.trim(),
                priorityRules = draft.priorityRulesText.trim(),
                weeklyRhythm = draft.weeklyRhythm.trim(),
                recurringCommitments = draft.recurringCommitmentsText.trim(),
                goodDayPattern = draft.goodDayPattern.trim(),
                badDayPattern = draft.badDayPattern.trim(),
                dayStartHour = draft.dayStartHour.coerceIn(5, 10),
                dayEndHour = normalizeDayEndHour(
                    startHour = draft.dayStartHour.coerceIn(5, 10),
                    endHour = draft.dayEndHour,
                ),
                morningEnergy = draft.morningEnergy.coerceIn(1, 5),
                afternoonEnergy = draft.afternoonEnergy.coerceIn(1, 5),
                eveningEnergy = draft.eveningEnergy.coerceIn(1, 5),
                focusStrength = draft.focusStrength.coerceIn(1, 5),
                disruptionSensitivity = draft.disruptionSensitivity.coerceIn(1, 5),
                recoveryNeed = draft.recoveryNeed.coerceIn(1, 5),
                updatedAt = clock.millis(),
            ),
        )
    }

    override suspend fun markDiscoveryCommitted() {
        val current = dao.get() ?: defaultEntity(clock)
        dao.upsert(
            current.copy(
                discoveryCommitted = true,
                updatedAt = clock.millis(),
            ),
        )
    }
}

private fun buildFingerprint(
    entity: UserFingerprintEntity,
    lifeAreas: List<LifeArea>,
    discoveryDay: Int,
): UserFingerprint {
    val roles = entity.roles.toDelimitedList().ifEmpty { listOf("Ich") }
    val responsibilities = entity.responsibilities.toDelimitedList()
    val priorityRules = entity.priorityRules.toDelimitedList().ifEmpty {
        listOf("Schuetze das wichtigste Fenster zuerst")
    }
    val recurringCommitments = entity.recurringCommitments.toDelimitedList()
    val dimensions = buildList {
        add(
            FingerprintDimension(
                id = "prioritaeten",
                label = "Prioritaetenlogik",
                summary = priorityRules.firstOrNull() ?: "Noch offen",
                confidence = confidenceForText(priorityRules, discoveryDay, 0.52f),
            ),
        )
        add(
            FingerprintDimension(
                id = "rhythmus",
                label = "Rhythmus",
                summary = "${displayLogicalHour(entity.dayStartHour)} bis ${displayLogicalHour(entity.dayEndHour)} · ${entity.weeklyRhythm.ifBlank { "Werktags fokussiert" }}",
                confidence = confidenceForText(listOf(entity.weeklyRhythm), discoveryDay, 0.58f),
            ),
        )
        add(
            FingerprintDimension(
                id = "fokus",
                label = "Fokus und Stoerung",
                summary = "Fokus ${entity.focusStrength}/5 · Stoerung ${entity.disruptionSensitivity}/5",
                confidence = ((entity.focusStrength + entity.disruptionSensitivity) / 10f).coerceIn(0.45f, 0.92f),
            ),
        )
        add(
            FingerprintDimension(
                id = "energie",
                label = "Energie",
                summary = "Morgen ${entity.morningEnergy}/5 · Mittag ${entity.afternoonEnergy}/5 · Abend ${entity.eveningEnergy}/5",
                confidence = ((entity.morningEnergy + entity.afternoonEnergy + entity.eveningEnergy) / 15f).coerceIn(0.4f, 0.88f),
            ),
        )
        add(
            FingerprintDimension(
                id = "muster",
                label = "Tagesmuster",
                summary = entity.goodDayPattern.ifBlank { "Noch nicht verdichtet" },
                confidence = confidenceForText(listOf(entity.goodDayPattern, entity.badDayPattern), discoveryDay, 0.46f),
            ),
        )
    }
    return UserFingerprint(
        lifeAreas = lifeAreas.ifEmpty(::defaultLifeAreas),
        roles = roles,
        responsibilities = responsibilities,
        priorityRules = priorityRules,
        weeklyRhythm = entity.weeklyRhythm.ifBlank { "Werktage fokussiert, Wochenende weicher" },
        recurringCommitments = recurringCommitments,
        goodDayPattern = entity.goodDayPattern.ifBlank {
            "Klarer Start, geschuetztes Fokusfenster und ruhiger Abschluss."
        },
        badDayPattern = entity.badDayPattern.ifBlank {
            "Zu viele Eingaenge, zerrissener Mittag und offener Abend."
        },
        dayStartHour = entity.dayStartHour.coerceIn(5, 10),
        dayEndHour = normalizeDayEndHour(
            startHour = entity.dayStartHour.coerceIn(5, 10),
            endHour = entity.dayEndHour,
        ),
        morningEnergy = entity.morningEnergy.coerceIn(1, 5),
        afternoonEnergy = entity.afternoonEnergy.coerceIn(1, 5),
        eveningEnergy = entity.eveningEnergy.coerceIn(1, 5),
        focusStrength = entity.focusStrength.coerceIn(1, 5),
        disruptionSensitivity = entity.disruptionSensitivity.coerceIn(1, 5),
        recoveryNeed = entity.recoveryNeed.coerceIn(1, 5),
        discoveryDay = discoveryDay.coerceIn(1, 7),
        discoveryCommitted = entity.discoveryCommitted,
        dimensions = dimensions,
    )
}

private fun defaultEntity(clock: Clock): UserFingerprintEntity {
    return UserFingerprintEntity(
        roles = "Ich",
        responsibilities = "Klar starten, Wichtiges schuetzen",
        priorityRules = "Schuetze das wichtigste Fenster zuerst\nPlane Konflikte sichtbar ein",
        weeklyRhythm = "Werktage fokussiert, Wochenende weicher",
        recurringCommitments = "Kalender zuerst lesen\nMittag nicht ueberziehen",
        goodDayPattern = "Klarer Start, ein echtes Fokusfenster, weniger Reibung am Nachmittag.",
        badDayPattern = "Zu viele offene Schleifen, zersplitterte Aufmerksamkeit, schwerer Abend.",
        dayStartHour = 6,
        dayEndHour = 22,
        morningEnergy = 4,
        afternoonEnergy = 3,
        eveningEnergy = 2,
        focusStrength = 4,
        disruptionSensitivity = 3,
        recoveryNeed = 4,
        discoveryCommitted = false,
        updatedAt = clock.millis(),
    )
}

private fun confidenceForText(
    values: List<String>,
    discoveryDay: Int,
    minimum: Float,
): Float {
    val filled = values.count { it.isNotBlank() }
    val ratio = if (values.isEmpty()) 0f else filled / values.size.toFloat()
    return (minimum + (ratio * 0.24f) + (discoveryDay / 10f)).coerceIn(0.38f, 0.96f)
}

private fun normalizeDayEndHour(
    startHour: Int,
    endHour: Int,
): Int {
    val normalizedEnd = if (endHour < startHour) endHour + 24 else endHour
    return normalizedEnd.coerceIn(startHour + 12, startHour + 24)
}

private fun displayLogicalHour(hour: Int): String {
    return "%02d:00".format(hour.mod(24))
}

private fun String.toDelimitedList(): List<String> {
    return lineSequence()
        .flatMap { line -> line.split(",").asSequence() }
        .map(String::trim)
        .filter(String::isNotBlank)
        .toList()
}

private fun LifeAreaEntity.toModel(): LifeArea {
    return LifeArea(
        id = id,
        label = label,
        definition = definition,
        targetScore = targetScore,
        sortOrder = sortOrder,
        isActive = isActive,
    )
}
