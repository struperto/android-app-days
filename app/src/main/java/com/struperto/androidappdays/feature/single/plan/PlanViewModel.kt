package com.struperto.androidappdays.feature.single.plan

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
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.PlanRepository
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.data.repository.VorhabenRepository
import com.struperto.androidappdays.data.repository.defaultLifeAreas
import com.struperto.androidappdays.navigation.PlanCaptureIdArg
import com.struperto.androidappdays.navigation.PlanVorhabenIdArg
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingPlanSource(
    val kindLabel: String,
    val summary: String,
)

data class PlanUiState(
    val todayLabel: String,
    val draftTitle: String,
    val draftNote: String,
    val selectedAreaId: String?,
    val selectedTimeBlock: TimeBlock,
    val areas: List<LifeArea>,
    val pendingSource: PendingPlanSource?,
    val areaSelectionEnabled: Boolean,
    val items: List<PlanItem>,
)

class PlanViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val captureRepository: CaptureRepository,
    private val learningEventRepository: LearningEventRepository,
    private val vorhabenRepository: VorhabenRepository,
    private val planRepository: PlanRepository,
    lifeWheelRepository: LifeWheelRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val todayIso = today.toString()
    private val title = MutableStateFlow("")
    private val note = MutableStateFlow("")
    private val selectedAreaId = MutableStateFlow<String?>(null)
    private val selectedTimeBlock = MutableStateFlow(currentTimeBlock(LocalTime.now(clock)))
    private val pendingSource = MutableStateFlow<PendingPlanSource?>(null)
    private val pendingCaptureId = MutableStateFlow(savedStateHandle.get<String?>(PlanCaptureIdArg))
    private val pendingVorhabenId = MutableStateFlow(savedStateHandle.get<String?>(PlanVorhabenIdArg))

    private val draftState = combine(
        title,
        note,
        selectedAreaId,
        selectedTimeBlock,
        pendingSource,
    ) { currentTitle, currentNote, currentAreaId, timeBlock, source ->
        PlanDraftState(
            currentTitle = currentTitle,
            currentNote = currentNote,
            currentAreaId = currentAreaId,
            timeBlock = timeBlock,
            source = source,
            hasPendingVorhaben = source?.kindLabel == "Aus Vorhaben",
        )
    }

    val state = combine(
        draftState,
        lifeWheelRepository.observeActiveAreas(),
        planRepository.observeToday(todayIso),
    ) { draft, areas, items ->
        val effectiveAreas = areas.ifEmpty(::defaultLifeAreas)
        PlanUiState(
            todayLabel = today.format(
                DateTimeFormatter.ofPattern("EEEE, dd. MMMM", Locale.GERMAN),
            ),
            draftTitle = draft.currentTitle,
            draftNote = draft.currentNote,
            selectedAreaId = draft.currentAreaId,
            selectedTimeBlock = draft.timeBlock,
            areas = effectiveAreas,
            pendingSource = draft.source,
            areaSelectionEnabled = !draft.hasPendingVorhaben,
            items = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlanUiState(
            todayLabel = today.toString(),
            draftTitle = "",
            draftNote = "",
            selectedAreaId = null,
            selectedTimeBlock = currentTimeBlock(LocalTime.now(clock)),
            areas = emptyList(),
            pendingSource = null,
            areaSelectionEnabled = true,
            items = emptyList(),
        ),
    )

    init {
        viewModelScope.launch {
            val captureId = pendingCaptureId.value
            if (!captureId.isNullOrBlank()) {
                val capture = captureRepository.loadById(captureId) ?: return@launch
                title.value = titleFromCapture(capture.text)
                note.value = noteFromCapture(capture.text)
                selectedAreaId.value = capture.areaId
                pendingSource.value = PendingPlanSource(
                    kindLabel = "Aus Erfassen",
                    summary = capture.text,
                )
                return@launch
            }

            val vorhabenId = pendingVorhabenId.value
            if (!vorhabenId.isNullOrBlank()) {
                val vorhaben = vorhabenRepository.loadById(vorhabenId) ?: return@launch
                selectedAreaId.value = vorhaben.areaId
                pendingSource.value = PendingPlanSource(
                    kindLabel = "Aus Vorhaben",
                    summary = vorhaben.title,
                )
            }
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

    fun onTimeBlockSelected(timeBlock: TimeBlock) {
        selectedTimeBlock.value = timeBlock
    }

    fun save() {
        val areaId = selectedAreaId.value
        if (areaId.isNullOrBlank()) {
            return
        }
        val currentPendingCaptureId = pendingCaptureId.value
        val currentPendingVorhabenId = pendingVorhabenId.value
        if (!currentPendingCaptureId.isNullOrBlank()) {
            saveFromCapture(
                captureId = currentPendingCaptureId,
                areaId = areaId,
            )
            return
        }
        if (!currentPendingVorhabenId.isNullOrBlank()) {
            saveFromVorhaben(currentPendingVorhabenId)
            return
        }
        addManual()
    }

    private fun addManual() {
        val trimmedTitle = title.value.trim()
        val trimmedNote = note.value.trim()
        val areaId = selectedAreaId.value
        if (trimmedTitle.isBlank() || areaId.isNullOrBlank()) {
            return
        }
        viewModelScope.launch {
            planRepository.addManual(
                title = trimmedTitle,
                note = trimmedNote,
                areaId = areaId,
                timeBlock = selectedTimeBlock.value,
            )
            learningEventRepository.record(
                type = LearningEventType.PLAN_SAVED,
                title = "Planpunkt angelegt",
                detail = trimmedTitle,
            )
            title.value = ""
            note.value = ""
        }
    }

    private fun saveFromCapture(
        captureId: String,
        areaId: String,
    ) {
        viewModelScope.launch {
            planRepository.addManual(
                title = title.value.trim(),
                note = note.value.trim(),
                areaId = areaId,
                timeBlock = selectedTimeBlock.value,
            )
            captureRepository.markConverted(captureId)
            learningEventRepository.record(
                type = LearningEventType.PLAN_SAVED,
                title = "Signal in Plan uebernommen",
                detail = title.value.trim(),
            )
            clearPendingSource()
        }
    }

    private fun saveFromVorhaben(vorhabenId: String) {
        viewModelScope.launch {
            val vorhaben = vorhabenRepository.loadById(vorhabenId)
            planRepository.addFromVorhaben(
                vorhabenId = vorhabenId,
                timeBlock = selectedTimeBlock.value,
            )
            learningEventRepository.record(
                type = LearningEventType.PLAN_SAVED,
                title = "Spaeter-Eintrag eingeplant",
                detail = vorhaben?.title.orEmpty(),
            )
            clearPendingSource()
        }
    }

    fun toggleDone(id: String) {
        viewModelScope.launch {
            val plan = planRepository.loadById(id)
            planRepository.toggleDone(id)
            learningEventRepository.record(
                type = LearningEventType.PLAN_TOGGLED,
                title = "Planstatus angepasst",
                detail = plan?.title.orEmpty(),
            )
        }
    }

    fun remove(id: String) {
        viewModelScope.launch {
            val plan = planRepository.loadById(id)
            planRepository.removeFromToday(id)
            learningEventRepository.record(
                type = LearningEventType.PLAN_REMOVED,
                title = "Planpunkt entfernt",
                detail = plan?.title.orEmpty(),
            )
        }
    }

    private fun clearPendingSource() {
        title.value = ""
        note.value = ""
        pendingSource.value = null
        pendingCaptureId.value = null
        pendingVorhabenId.value = null
        savedStateHandle[PlanCaptureIdArg] = null
        savedStateHandle[PlanVorhabenIdArg] = null
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlanViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    captureRepository = appContainer.captureRepository,
                    learningEventRepository = appContainer.learningEventRepository,
                    vorhabenRepository = appContainer.vorhabenRepository,
                    planRepository = appContainer.planRepository,
                    lifeWheelRepository = appContainer.lifeWheelRepository,
                    clock = appContainer.clock,
                )
            }
        }
    }
}

private fun currentTimeBlock(now: LocalTime): TimeBlock {
    return when {
        now.hour < 11 -> TimeBlock.MORGEN
        now.hour < 14 -> TimeBlock.MITTAG
        now.hour < 18 -> TimeBlock.NACHMITTAG
        else -> TimeBlock.ABEND
    }
}

private fun titleFromCapture(text: String): String {
    return text.lineSequence()
        .firstOrNull()
        .orEmpty()
        .trim()
        .take(60)
}

private fun noteFromCapture(text: String): String {
    val trimmed = text.trim()
    val title = titleFromCapture(trimmed)
    return if (trimmed == title) "" else trimmed
}

private data class PlanDraftState(
    val currentTitle: String,
    val currentNote: String,
    val currentAreaId: String?,
    val timeBlock: TimeBlock,
    val source: PendingPlanSource?,
    val hasPendingVorhaben: Boolean,
)
