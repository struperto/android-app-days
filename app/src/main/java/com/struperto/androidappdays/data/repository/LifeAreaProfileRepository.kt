package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.LifeAreaProfileDao
import com.struperto.androidappdays.data.local.LifeAreaProfileEntity
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LifeAreaProfileRepository {
    fun observeProfiles(): Flow<List<LifeAreaProfile>>
    suspend fun saveProfile(profile: LifeAreaProfile)
}

class RoomLifeAreaProfileRepository(
    private val dao: LifeAreaProfileDao,
    private val clock: Clock,
) : LifeAreaProfileRepository {
    override fun observeProfiles(): Flow<List<LifeAreaProfile>> {
        return dao.observeProfiles().map { entities ->
            entities.map(LifeAreaProfileEntity::toModel)
        }
    }

    override suspend fun saveProfile(profile: LifeAreaProfile) {
        dao.upsertProfile(
            LifeAreaProfileEntity(
                areaId = profile.areaId,
                cadence = profile.cadence,
                intensity = profile.intensity.coerceIn(1, 5),
                signalBlend = profile.signalBlend.coerceIn(0, 100),
                selectedTracks = profile.selectedTracks.joinToString(","),
                remindersEnabled = profile.remindersEnabled,
                reviewEnabled = profile.reviewEnabled,
                experimentsEnabled = profile.experimentsEnabled,
                updatedAt = clock.millis(),
            ),
        )
    }
}

private fun LifeAreaProfileEntity.toModel(): LifeAreaProfile {
    return LifeAreaProfile(
        areaId = areaId,
        cadence = cadence,
        intensity = intensity.coerceIn(1, 5),
        signalBlend = signalBlend.coerceIn(0, 100),
        selectedTracks = selectedTracks.split(",")
            .map(String::trim)
            .filter(String::isNotBlank)
            .toSet(),
        remindersEnabled = remindersEnabled,
        reviewEnabled = reviewEnabled,
        experimentsEnabled = experimentsEnabled,
    )
}
