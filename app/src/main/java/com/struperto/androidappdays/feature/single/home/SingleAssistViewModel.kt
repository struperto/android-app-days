package com.struperto.androidappdays.feature.single.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.feature.single.assist.LocalAssistGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SingleAssistState(
    val hasLatestCapture: Boolean,
    val latestCapturePreview: String?,
    val summary: String?,
    val nextStep: String?,
)

class SingleAssistViewModel(
    private val captureRepository: CaptureRepository,
    private val localAssistGateway: LocalAssistGateway,
) : ViewModel() {
    private val latestCaptureState = MutableStateFlow<CaptureItem?>(null)
    private val resultState = MutableStateFlow(AssistResultState())
    private var latestCaptureId: String? = null

    val state: StateFlow<SingleAssistState> = combine(
        latestCaptureState,
        resultState,
    ) { latestCapture, results ->
        SingleAssistState(
            hasLatestCapture = latestCapture != null,
            latestCapturePreview = latestCapture?.text?.let(::previewText),
            summary = results.summary,
            nextStep = results.nextStep,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SingleAssistState(
            hasLatestCapture = false,
            latestCapturePreview = null,
            summary = null,
            nextStep = null,
        ),
    )

    init {
        viewModelScope.launch {
            captureRepository.observeOpen().collect { items ->
                val latestOpen = items.firstOrNull()
                if (latestOpen?.id != latestCaptureId) {
                    latestCaptureId = latestOpen?.id
                    resultState.value = AssistResultState()
                }
                latestCaptureState.value = latestOpen
            }
        }
    }

    fun summarize() {
        val capture = latestCaptureState.value ?: return
        resultState.update { current ->
            current.copy(summary = localAssistGateway.summarize(capture.text))
        }
    }

    fun suggestNextStep() {
        val capture = latestCaptureState.value ?: return
        resultState.update { current ->
            current.copy(nextStep = localAssistGateway.suggestNextStep(capture.text))
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SingleAssistViewModel(
                    captureRepository = appContainer.captureRepository,
                    localAssistGateway = appContainer.localAssistGateway,
                )
            }
        }
    }
}

private data class AssistResultState(
    val summary: String? = null,
    val nextStep: String? = null,
)

private fun previewText(text: String): String {
    val normalized = text.trim().replace(Regex("\\s+"), " ")
    if (normalized.length <= 160) {
        return normalized
    }
    return normalized.take(157).trimEnd() + "..."
}
