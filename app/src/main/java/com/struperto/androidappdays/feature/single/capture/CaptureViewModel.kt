package com.struperto.androidappdays.feature.single.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.data.repository.LearningEventRepository
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeWheelRepository
import com.struperto.androidappdays.data.repository.defaultLifeAreas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CaptureUiState(
    val draftText: String,
    val selectedAreaId: String?,
    val areas: List<LifeArea>,
    val items: List<CaptureItem>,
)

class CaptureViewModel(
    private val captureRepository: CaptureRepository,
    private val learningEventRepository: LearningEventRepository,
    lifeWheelRepository: LifeWheelRepository,
) : ViewModel() {
    private val draftText = MutableStateFlow("")
    private val selectedAreaId = MutableStateFlow<String?>(null)

    val state = combine(
        draftText,
        selectedAreaId,
        lifeWheelRepository.observeActiveAreas(),
        captureRepository.observeOpen(),
    ) { text, currentAreaId, areas, items ->
        val effectiveAreas = areas.ifEmpty(::defaultLifeAreas)
        CaptureUiState(
            draftText = text,
            selectedAreaId = currentAreaId,
            areas = effectiveAreas,
            items = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CaptureUiState(
            draftText = "",
            selectedAreaId = null,
            areas = emptyList(),
            items = emptyList(),
        ),
    )

    fun onDraftChange(value: String) {
        draftText.value = value
    }

    fun onAreaSelected(areaId: String) {
        selectedAreaId.value = if (selectedAreaId.value == areaId) null else areaId
    }

    fun save() {
        val text = draftText.value.trim()
        if (text.isBlank()) {
            return
        }
        viewModelScope.launch {
            captureRepository.createTextCapture(
                text = text,
                areaId = selectedAreaId.value,
            )
            learningEventRepository.record(
                type = LearningEventType.CAPTURE_SAVED,
                title = "Signal erfasst",
                detail = text,
            )
            draftText.value = ""
            selectedAreaId.value = null
        }
    }

    fun archive(id: String) {
        viewModelScope.launch {
            captureRepository.archive(id)
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CaptureViewModel(
                    captureRepository = appContainer.captureRepository,
                    learningEventRepository = appContainer.learningEventRepository,
                    lifeWheelRepository = appContainer.lifeWheelRepository,
                )
            }
        }
    }
}
