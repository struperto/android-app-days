package com.struperto.androidappdays.feature.single.workingset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.data.repository.LearningEventRepository
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeWheelRepository
import com.struperto.androidappdays.data.repository.Vorhaben
import com.struperto.androidappdays.data.repository.VorhabenRepository
import com.struperto.androidappdays.data.repository.defaultLifeAreas
import com.struperto.androidappdays.navigation.WorkingSetCaptureIdArg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkingSetUiState(
    val draftTitle: String,
    val draftNote: String,
    val selectedAreaId: String?,
    val pendingCaptureText: String?,
    val areas: List<LifeArea>,
    val items: List<Vorhaben>,
)

class WorkingSetViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val captureRepository: CaptureRepository,
    private val learningEventRepository: LearningEventRepository,
    private val vorhabenRepository: VorhabenRepository,
    lifeWheelRepository: LifeWheelRepository,
) : ViewModel() {
    private val title = MutableStateFlow("")
    private val note = MutableStateFlow("")
    private val selectedAreaId = MutableStateFlow<String?>(null)
    private val pendingCaptureId = MutableStateFlow(savedStateHandle.get<String?>(WorkingSetCaptureIdArg))
    private val pendingCaptureText = MutableStateFlow<String?>(null)

    private val draftState = combine(
        title,
        note,
        selectedAreaId,
        pendingCaptureText,
    ) { currentTitle, currentNote, currentAreaId, currentPendingCaptureText ->
        WorkingSetDraftState(
            currentTitle = currentTitle,
            currentNote = currentNote,
            currentAreaId = currentAreaId,
            currentPendingCaptureText = currentPendingCaptureText,
        )
    }

    val state = combine(
        draftState,
        lifeWheelRepository.observeActiveAreas(),
        vorhabenRepository.observeActive(),
    ) { draft, areas, items ->
        val effectiveAreas = areas.ifEmpty(::defaultLifeAreas)
        WorkingSetUiState(
            draftTitle = draft.currentTitle,
            draftNote = draft.currentNote,
            selectedAreaId = draft.currentAreaId,
            pendingCaptureText = draft.currentPendingCaptureText,
            areas = effectiveAreas,
            items = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkingSetUiState(
            draftTitle = "",
            draftNote = "",
            selectedAreaId = null,
            pendingCaptureText = null,
            areas = emptyList(),
            items = emptyList(),
        ),
    )

    init {
        viewModelScope.launch {
            val captureId = pendingCaptureId.value ?: return@launch
            val capture = captureRepository.loadById(captureId) ?: return@launch
            title.value = capture.text.lineSequence().firstOrNull().orEmpty().take(60)
            note.value = capture.text
            selectedAreaId.value = capture.areaId
            pendingCaptureText.value = capture.text
        }
    }

    fun onTitleChange(value: String) {
        title.value = value
    }

    fun onNoteChange(value: String) {
        note.value = value
    }

    fun onAreaSelected(areaId: String) {
        selectedAreaId.value = areaId
    }

    fun save() {
        val trimmedTitle = title.value.trim()
        val trimmedNote = note.value.trim()
        val areaId = selectedAreaId.value
        if (trimmedTitle.isBlank() || areaId.isNullOrBlank()) {
            return
        }
        viewModelScope.launch {
            val captureId = pendingCaptureId.value
            if (captureId.isNullOrBlank()) {
                vorhabenRepository.create(
                    title = trimmedTitle,
                    note = trimmedNote,
                    areaId = areaId,
                )
                learningEventRepository.record(
                    type = LearningEventType.LATER_SAVED,
                    title = "Spaeter-Eintrag erstellt",
                    detail = trimmedTitle,
                )
            } else {
                vorhabenRepository.createFromCapture(
                    captureId = captureId,
                    title = trimmedTitle,
                    note = trimmedNote,
                    areaId = areaId,
                )
                captureRepository.markConverted(captureId)
                learningEventRepository.record(
                    type = LearningEventType.LATER_SAVED,
                    title = "Signal zu Spaeter verdichtet",
                    detail = trimmedTitle,
                )
            }
            clearDraft()
        }
    }

    fun archive(vorhabenId: String) {
        viewModelScope.launch {
            val vorhaben = vorhabenRepository.loadById(vorhabenId)
            vorhabenRepository.archive(vorhabenId)
            learningEventRepository.record(
                type = LearningEventType.LATER_DONE,
                title = "Spaeter-Eintrag abgeschlossen",
                detail = vorhaben?.title.orEmpty(),
            )
        }
    }

    private fun clearDraft() {
        title.value = ""
        note.value = ""
        selectedAreaId.value = null
        pendingCaptureText.value = null
        pendingCaptureId.value = null
        savedStateHandle[WorkingSetCaptureIdArg] = null
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WorkingSetViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    captureRepository = appContainer.captureRepository,
                    learningEventRepository = appContainer.learningEventRepository,
                    vorhabenRepository = appContainer.vorhabenRepository,
                    lifeWheelRepository = appContainer.lifeWheelRepository,
                )
            }
        }
    }
}

private data class WorkingSetDraftState(
    val currentTitle: String,
    val currentNote: String,
    val currentAreaId: String?,
    val currentPendingCaptureText: String?,
)
