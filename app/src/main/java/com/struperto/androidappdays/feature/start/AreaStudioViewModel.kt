package com.struperto.androidappdays.feature.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaProfile
import com.struperto.androidappdays.data.repository.LifeAreaProfileRepository
import com.struperto.androidappdays.data.repository.LifeWheelRepository
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class AreaStudioAreaState(
    val areaId: String,
    val title: String,
    val summary: String,
    val tracks: List<String>,
    val targetScore: Int,
    val manualScore: Int?,
    val cadence: String,
    val intensity: Int,
    val signalBlend: Int,
    val selectedTracks: Set<String>,
    val remindersEnabled: Boolean,
    val reviewEnabled: Boolean,
    val experimentsEnabled: Boolean,
)

data class AreaStudioUiState(
    val areas: Map<String, AreaStudioAreaState> = emptyMap(),
)

class AreaStudioViewModel(
    private val lifeWheelRepository: LifeWheelRepository,
    private val lifeAreaProfileRepository: LifeAreaProfileRepository,
    private val clock: Clock,
) : ViewModel() {
    private val todayIso = LocalDate.now(clock).toString()

    val state = combine(
        lifeWheelRepository.observeActiveAreas(),
        lifeWheelRepository.observeDailyChecks(todayIso),
        lifeAreaProfileRepository.observeProfiles(),
    ) { activeAreas, dailyChecks, profiles ->
        val areaMap = activeAreas.associateBy(LifeArea::id)
        val checkMap = dailyChecks.associateBy { it.areaId }
        val profileMap = profiles.associateBy(LifeAreaProfile::areaId)
        AreaStudioUiState(
            areas = startAreaBlueprints.associate { blueprint ->
                val area = areaMap[blueprint.id] ?: LifeArea(
                    id = blueprint.id,
                    label = blueprint.label,
                    definition = blueprint.summary,
                    targetScore = 3,
                    sortOrder = startAreaBlueprints.indexOf(blueprint),
                    isActive = true,
                )
                val profile = profileMap[blueprint.id] ?: defaultProfile(blueprint)
                blueprint.id to AreaStudioAreaState(
                    areaId = blueprint.id,
                    title = area.label,
                    summary = blueprint.summary,
                    tracks = blueprint.tracks,
                    targetScore = area.targetScore,
                    manualScore = checkMap[blueprint.id]?.manualScore,
                    cadence = profile.cadence,
                    intensity = profile.intensity,
                    signalBlend = profile.signalBlend,
                    selectedTracks = profile.selectedTracks.ifEmpty { blueprint.tracks.take(2).toSet() },
                    remindersEnabled = profile.remindersEnabled,
                    reviewEnabled = profile.reviewEnabled,
                    experimentsEnabled = profile.experimentsEnabled,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AreaStudioUiState(),
    )

    fun setTargetScore(
        areaId: String,
        value: Float,
    ) {
        val current = state.value.areas[areaId] ?: return
        viewModelScope.launch {
            lifeWheelRepository.updateArea(
                id = areaId,
                label = current.title,
                definition = current.summary,
                targetScore = value.roundToInt().coerceIn(1, 5),
            )
        }
    }

    fun setManualScore(
        areaId: String,
        score: Int?,
    ) {
        viewModelScope.launch {
            lifeWheelRepository.upsertDailyCheck(
                areaId = areaId,
                date = todayIso,
                manualScore = score,
            )
        }
    }

    fun setCadence(
        areaId: String,
        cadence: String,
    ) {
        updateProfile(areaId) { it.copy(cadence = cadence) }
    }

    fun setIntensity(
        areaId: String,
        intensity: Float,
    ) {
        updateProfile(areaId) { it.copy(intensity = intensity.roundToInt().coerceIn(1, 5)) }
    }

    fun setSignalBlend(
        areaId: String,
        signalBlend: Float,
    ) {
        updateProfile(areaId) { it.copy(signalBlend = signalBlend.roundToInt().coerceIn(0, 100)) }
    }

    fun toggleTrack(
        areaId: String,
        track: String,
    ) {
        updateProfile(areaId) { current ->
            val nextTracks = if (track in current.selectedTracks) {
                current.selectedTracks - track
            } else {
                current.selectedTracks + track
            }
            current.copy(selectedTracks = nextTracks)
        }
    }

    fun setRemindersEnabled(
        areaId: String,
        enabled: Boolean,
    ) {
        updateProfile(areaId) { it.copy(remindersEnabled = enabled) }
    }

    fun setReviewEnabled(
        areaId: String,
        enabled: Boolean,
    ) {
        updateProfile(areaId) { it.copy(reviewEnabled = enabled) }
    }

    fun setExperimentsEnabled(
        areaId: String,
        enabled: Boolean,
    ) {
        updateProfile(areaId) { it.copy(experimentsEnabled = enabled) }
    }

    private fun updateProfile(
        areaId: String,
        transform: (LifeAreaProfile) -> LifeAreaProfile,
    ) {
        val blueprint = startAreaBlueprint(areaId) ?: return
        val current = state.value.areas[areaId] ?: return
        viewModelScope.launch {
            lifeAreaProfileRepository.saveProfile(
                transform(
                    LifeAreaProfile(
                        areaId = areaId,
                        cadence = current.cadence,
                        intensity = current.intensity,
                        signalBlend = current.signalBlend,
                        selectedTracks = current.selectedTracks.ifEmpty { blueprint.tracks.take(2).toSet() },
                        remindersEnabled = current.remindersEnabled,
                        reviewEnabled = current.reviewEnabled,
                        experimentsEnabled = current.experimentsEnabled,
                    ),
                ),
            )
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AreaStudioViewModel(
                    lifeWheelRepository = appContainer.lifeWheelRepository,
                    lifeAreaProfileRepository = appContainer.lifeAreaProfileRepository,
                    clock = appContainer.clock,
                )
            }
        }
    }
}

private fun defaultProfile(
    blueprint: StartAreaBlueprint,
): LifeAreaProfile {
    return LifeAreaProfile(
        areaId = blueprint.id,
        cadence = "adaptive",
        intensity = 3,
        signalBlend = 60,
        selectedTracks = blueprint.tracks.take(2).toSet(),
        remindersEnabled = false,
        reviewEnabled = true,
        experimentsEnabled = false,
    )
}
