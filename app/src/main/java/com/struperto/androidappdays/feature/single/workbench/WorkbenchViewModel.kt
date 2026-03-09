package com.struperto.androidappdays.feature.single.workbench

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeWheelRepository
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.PlanRepository
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.data.repository.Vorhaben
import com.struperto.androidappdays.data.repository.VorhabenRepository
import com.struperto.androidappdays.data.repository.defaultLifeAreas
import com.struperto.androidappdays.feature.single.assist.LocalAssistGateway
import com.struperto.androidappdays.navigation.WorkbenchPaneArg
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WorkbenchPane(
    val navValue: String,
    val label: String,
) {
    NEU("neu", "Neu"),
    HEUTE("heute", "Heute"),
    SPAETER("spaeter", "Später"),
    ;

    companion object {
        fun fromNavValue(value: String?): WorkbenchPane {
            return entries.firstOrNull { it.navValue == value } ?: NEU
        }
    }
}

enum class WorkbenchTool(
    val label: String,
) {
    TEXT("Text"),
    STIMME("Stimme"),
    ZWISCHENABLAGE("Zwischenablage"),
    TEILEN("Teilen"),
    ASSIST("Assist"),
    SIGNALE("Signale"),
}

data class WorkbenchSignal(
    val title: String,
    val detail: String,
    val targetPane: WorkbenchPane?,
)

data class WorkbenchUiState(
    val activePane: WorkbenchPane,
    val draftText: String,
    val selectedAreaId: String?,
    val selectedTimeBlock: TimeBlock,
    val areas: List<LifeArea>,
    val captures: List<CaptureItem>,
    val vorhaben: List<Vorhaben>,
    val planItems: List<PlanItem>,
    val latestCapturePreview: String?,
    val assistSummary: String?,
    val assistNextStep: String?,
    val signals: List<WorkbenchSignal>,
    val toolFeedback: String?,
) {
    val draftPlaceholder: String
        get() = when (activePane) {
            WorkbenchPane.NEU -> "Etwas festhalten"
            WorkbenchPane.HEUTE -> "Was gehört heute noch rein?"
            WorkbenchPane.SPAETER -> "Was bleibt wichtig, aber nicht jetzt?"
        }

    val submitLabel: String
        get() = when (activePane) {
            WorkbenchPane.NEU -> "In Neu ablegen"
            WorkbenchPane.HEUTE -> "In Heute legen"
            WorkbenchPane.SPAETER -> "Für später merken"
        }
}

class WorkbenchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val captureRepository: CaptureRepository,
    private val vorhabenRepository: VorhabenRepository,
    private val planRepository: PlanRepository,
    private val lifeWheelRepository: LifeWheelRepository,
    private val localAssistGateway: LocalAssistGateway,
    private val clock: Clock,
) : ViewModel() {
    private val todayIso = LocalDate.now(clock).toString()
    private val activePane = MutableStateFlow(
        WorkbenchPane.fromNavValue(savedStateHandle.get<String?>(WorkbenchPaneArg)),
    )
    private val draftText = MutableStateFlow("")
    private val selectedAreaId = MutableStateFlow<String?>(null)
    private val selectedTimeBlock = MutableStateFlow(currentTimeBlock(LocalTime.now(clock)))
    private val toolFeedback = MutableStateFlow<String?>(null)
    private val assistState = MutableStateFlow(WorkbenchAssistState())

    private val baseInputState = combine(
        activePane,
        draftText,
        selectedAreaId,
        selectedTimeBlock,
    ) { pane, draft, currentAreaId, timeBlock ->
        WorkbenchInputState(
            pane = pane,
            draft = draft,
            selectedAreaId = currentAreaId,
            timeBlock = timeBlock,
            toolFeedback = null,
            assist = WorkbenchAssistState(),
        )
    }

    private val inputState = combine(
        baseInputState,
        toolFeedback,
        assistState,
    ) { base, feedback, assist ->
        base.copy(
            toolFeedback = feedback,
            assist = assist,
        )
    }

    private val contentState = combine(
        lifeWheelRepository.observeActiveAreas(),
        captureRepository.observeOpen(),
        vorhabenRepository.observeActive(),
        planRepository.observeToday(todayIso),
    ) { areas, captures, vorhaben, planItems ->
        val effectiveAreas = areas.ifEmpty(::defaultLifeAreas)
        WorkbenchContentState(
            areas = effectiveAreas,
            captures = captures,
            vorhaben = vorhaben,
            planItems = planItems,
            latestCapturePreview = captures.firstOrNull()?.text?.let(::previewText),
            signals = buildSignals(
                captures = captures,
                vorhaben = vorhaben,
                planItems = planItems,
                currentTimeBlock = currentTimeBlock(LocalTime.now(clock)),
            ),
        )
    }

    val state = combine(inputState, contentState) { input, content ->
        WorkbenchUiState(
            activePane = input.pane,
            draftText = input.draft,
            selectedAreaId = input.selectedAreaId ?: content.areas.firstOrNull()?.id,
            selectedTimeBlock = input.timeBlock,
            areas = content.areas,
            captures = content.captures,
            vorhaben = content.vorhaben,
            planItems = content.planItems,
            latestCapturePreview = content.latestCapturePreview,
            assistSummary = input.assist.summary,
            assistNextStep = input.assist.nextStep,
            signals = content.signals,
            toolFeedback = input.toolFeedback,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkbenchUiState(
            activePane = WorkbenchPane.fromNavValue(savedStateHandle.get<String?>(WorkbenchPaneArg)),
            draftText = "",
            selectedAreaId = defaultLifeAreas().firstOrNull()?.id,
            selectedTimeBlock = currentTimeBlock(LocalTime.now(clock)),
            areas = defaultLifeAreas(),
            captures = emptyList(),
            vorhaben = emptyList(),
            planItems = emptyList(),
            latestCapturePreview = null,
            assistSummary = null,
            assistNextStep = null,
            signals = emptyList(),
            toolFeedback = null,
        ),
    )

    fun setPane(pane: WorkbenchPane) {
        activePane.value = pane
        savedStateHandle[WorkbenchPaneArg] = pane.navValue
        clearFeedback()
    }

    fun onDraftChange(value: String) {
        draftText.value = value
    }

    fun onAreaSelected(areaId: String) {
        selectedAreaId.value = if (selectedAreaId.value == areaId) null else areaId
    }

    fun onTimeBlockSelected(timeBlock: TimeBlock) {
        selectedTimeBlock.value = timeBlock
    }

    fun importClipboard(text: String?) {
        val normalized = text?.trim()
        if (normalized.isNullOrBlank()) {
            toolFeedback.value = "Die Zwischenablage ist leer."
            return
        }
        activePane.value = WorkbenchPane.NEU
        draftText.value = normalized
        toolFeedback.value = "Text aus der Zwischenablage liegt in Neu bereit."
    }

    fun ingestVoiceTranscript(text: String?) {
        val normalized = text?.trim()
        if (normalized.isNullOrBlank()) {
            toolFeedback.value = "Keine Sprache erkannt."
            return
        }
        activePane.value = WorkbenchPane.NEU
        draftText.value = normalized
        toolFeedback.value = "Sprachtext liegt in Neu bereit."
    }

    fun showShareHint() {
        toolFeedback.value = "System-Teilen ist aktiv. Geteilter Text landet direkt in Neu."
    }

    fun clearFeedback() {
        toolFeedback.value = null
    }

    fun generateAssistFromDraft() {
        val source = draftText.value.trim()
        if (source.isBlank()) {
            toolFeedback.value = "Für Assist fehlt gerade Text im Eingabefeld."
            return
        }
        assistState.value = WorkbenchAssistState(
            summary = localAssistGateway.summarize(source),
            nextStep = localAssistGateway.suggestNextStep(source),
        )
        toolFeedback.value = "Assist hat den aktuellen Entwurf gelesen."
    }

    fun generateAssistFromLatestCapture() {
        viewModelScope.launch {
            val latest = captureRepository.loadLatestOpen()
            if (latest == null) {
                toolFeedback.value = "Es gibt noch kein offenes Neu-Element für Assist."
                return@launch
            }
            assistState.value = WorkbenchAssistState(
                summary = localAssistGateway.summarize(latest.text),
                nextStep = localAssistGateway.suggestNextStep(latest.text),
            )
            toolFeedback.value = "Assist arbeitet auf dem letzten offenen Neu-Eintrag."
        }
    }

    fun jumpToSignal(signal: WorkbenchSignal) {
        signal.targetPane?.let(::setPane)
    }

    fun submit() {
        val draft = draftText.value.trim()
        if (draft.isBlank()) {
            return
        }
        viewModelScope.launch {
            val areaId = resolveAreaId()
            when (activePane.value) {
                WorkbenchPane.NEU -> {
                    captureRepository.createTextCapture(
                        text = draft,
                        areaId = areaId,
                    )
                    toolFeedback.value = "Neu aufgenommen."
                }
                WorkbenchPane.HEUTE -> {
                    planRepository.addManual(
                        title = titleFromText(draft),
                        note = noteFromText(draft),
                        areaId = areaId,
                        timeBlock = selectedTimeBlock.value,
                    )
                    toolFeedback.value = "Direkt in Heute gelegt."
                }
                WorkbenchPane.SPAETER -> {
                    vorhabenRepository.create(
                        title = titleFromText(draft),
                        note = noteFromText(draft),
                        areaId = areaId,
                    )
                    toolFeedback.value = "Für später vorgemerkt."
                }
            }
            draftText.value = ""
        }
    }

    fun sendCaptureToToday(captureId: String) {
        viewModelScope.launch {
            val capture = captureRepository.loadById(captureId) ?: return@launch
            planRepository.addManual(
                title = titleFromText(capture.text),
                note = noteFromText(capture.text),
                areaId = capture.areaId ?: resolveAreaId(),
                timeBlock = selectedTimeBlock.value,
            )
            captureRepository.markConverted(captureId)
            setPane(WorkbenchPane.HEUTE)
            toolFeedback.value = "Neu wurde in Heute übernommen."
        }
    }

    fun sendCaptureToLater(captureId: String) {
        viewModelScope.launch {
            val capture = captureRepository.loadById(captureId) ?: return@launch
            vorhabenRepository.createFromCapture(
                captureId = captureId,
                title = titleFromText(capture.text),
                note = noteFromText(capture.text),
                areaId = capture.areaId ?: resolveAreaId(),
            )
            captureRepository.markConverted(captureId)
            setPane(WorkbenchPane.SPAETER)
            toolFeedback.value = "Neu wurde in Später überführt."
        }
    }

    fun completeCapture(captureId: String) {
        viewModelScope.launch {
            captureRepository.archive(captureId)
            toolFeedback.value = "Neu-Eintrag abgeschlossen."
        }
    }

    fun sendVorhabenToToday(vorhabenId: String) {
        viewModelScope.launch {
            planRepository.addFromVorhaben(
                vorhabenId = vorhabenId,
                timeBlock = selectedTimeBlock.value,
            )
            setPane(WorkbenchPane.HEUTE)
            toolFeedback.value = "Später wurde nach Heute gezogen."
        }
    }

    fun completeVorhaben(vorhabenId: String) {
        viewModelScope.launch {
            vorhabenRepository.archive(vorhabenId)
            toolFeedback.value = "Später-Eintrag abgeschlossen."
        }
    }

    fun togglePlanDone(planId: String) {
        viewModelScope.launch {
            planRepository.toggleDone(planId)
            toolFeedback.value = "Heute wurde aktualisiert."
        }
    }

    fun removePlan(planId: String) {
        viewModelScope.launch {
            planRepository.removeFromToday(planId)
            toolFeedback.value = "Heute-Eintrag entfernt."
        }
    }

    private suspend fun resolveAreaId(): String {
        val current = selectedAreaId.value
        if (!current.isNullOrBlank()) {
            return current
        }
        return lifeWheelRepository.loadActiveAreas()
            .ifEmpty(::defaultLifeAreas)
            .first()
            .id
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WorkbenchViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    captureRepository = appContainer.captureRepository,
                    vorhabenRepository = appContainer.vorhabenRepository,
                    planRepository = appContainer.planRepository,
                    lifeWheelRepository = appContainer.lifeWheelRepository,
                    localAssistGateway = appContainer.localAssistGateway,
                    clock = appContainer.clock,
                )
            }
        }
    }
}

private data class WorkbenchInputState(
    val pane: WorkbenchPane,
    val draft: String,
    val selectedAreaId: String?,
    val timeBlock: TimeBlock,
    val toolFeedback: String?,
    val assist: WorkbenchAssistState,
)

private data class WorkbenchContentState(
    val areas: List<LifeArea>,
    val captures: List<CaptureItem>,
    val vorhaben: List<Vorhaben>,
    val planItems: List<PlanItem>,
    val latestCapturePreview: String?,
    val signals: List<WorkbenchSignal>,
)

private data class WorkbenchAssistState(
    val summary: String? = null,
    val nextStep: String? = null,
)

private fun buildSignals(
    captures: List<CaptureItem>,
    vorhaben: List<Vorhaben>,
    planItems: List<PlanItem>,
    currentTimeBlock: TimeBlock,
): List<WorkbenchSignal> {
    val signals = mutableListOf<WorkbenchSignal>()
    if (captures.isNotEmpty()) {
        signals += WorkbenchSignal(
            title = "${captures.size} neu offen",
            detail = "Sortiere, was heute oder später wichtig ist.",
            targetPane = WorkbenchPane.NEU,
        )
    }
    if (vorhaben.isNotEmpty()) {
        signals += WorkbenchSignal(
            title = "${vorhaben.size} später aktiv",
            detail = "Mindestens ein Eintrag kann wahrscheinlich in Heute gezogen werden.",
            targetPane = WorkbenchPane.SPAETER,
        )
    }
    if (planItems.none { it.timeBlock == currentTimeBlock }) {
        signals += WorkbenchSignal(
            title = "${currentTimeBlock.label} noch leer",
            detail = "Setze einen kleinen Fokus für den aktuellen Zeitblock.",
            targetPane = WorkbenchPane.HEUTE,
        )
    } else {
        val openInCurrent = planItems.count { !it.isDone && it.timeBlock == currentTimeBlock }
        if (openInCurrent > 0) {
            signals += WorkbenchSignal(
                title = "$openInCurrent offen in ${currentTimeBlock.label}",
                detail = "Schließe einen dieser Punkte bewusst ab.",
                targetPane = WorkbenchPane.HEUTE,
            )
        }
    }
    return signals.take(3)
}

private fun currentTimeBlock(now: LocalTime): TimeBlock {
    return when {
        now.hour < 11 -> TimeBlock.MORGEN
        now.hour < 14 -> TimeBlock.MITTAG
        now.hour < 18 -> TimeBlock.NACHMITTAG
        else -> TimeBlock.ABEND
    }
}

private fun titleFromText(text: String): String {
    return text.lineSequence()
        .firstOrNull()
        .orEmpty()
        .trim()
        .take(60)
}

private fun noteFromText(text: String): String {
    val trimmed = text.trim()
    val title = titleFromText(trimmed)
    return if (trimmed == title) "" else trimmed
}

private fun previewText(text: String): String {
    val normalized = text.trim().replace(Regex("\\s+"), " ")
    if (normalized.length <= 120) {
        return normalized
    }
    return normalized.take(117).trimEnd() + "..."
}
