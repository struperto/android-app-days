package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.AreaKernelDao
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LifeAreaProfileRepository {
    fun observeProfiles(): Flow<List<LifeAreaProfile>>
    suspend fun saveProfile(profile: LifeAreaProfile)
}

class RoomLifeAreaProfileRepository(
    private val areaKernelDao: AreaKernelDao,
    private val clock: Clock,
) : LifeAreaProfileRepository {
    override fun observeProfiles(): Flow<List<LifeAreaProfile>> {
        return areaKernelDao.observeActiveAreaInstances().map { entities ->
            entities.map { entity -> entity.toLifeAreaProfile() }
        }
    }

    override suspend fun saveProfile(profile: LifeAreaProfile) {
        val current = areaKernelDao.getAreaInstance(profile.areaId) ?: return
        areaKernelDao.upsertAreaInstance(
            current.copy(
                cadenceKey = profile.cadence,
                intensity = profile.intensity.coerceIn(1, 5),
                signalBlend = profile.signalBlend.coerceIn(0, 100),
                selectedTracks = encodeSelectedTracks(profile.selectedTracks),
                remindersEnabled = profile.remindersEnabled,
                reviewEnabled = profile.reviewEnabled,
                experimentsEnabled = profile.experimentsEnabled,
                lageMode = profile.lageMode,
                directionMode = profile.directionMode,
                sourcesMode = profile.sourcesMode,
                flowProfile = profile.flowProfile,
                updatedAt = clock.millis(),
            ),
        )
    }
}
