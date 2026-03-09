package com.struperto.androidappdays.feature.single.lifewheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.FingerprintDimension
import com.struperto.androidappdays.data.repository.LearningEventRepository
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeWheelRepository
import com.struperto.androidappdays.data.repository.UserFingerprint
import com.struperto.androidappdays.data.repository.UserFingerprintDraft
import com.struperto.androidappdays.data.repository.UserFingerprintRepository
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LifeWheelAreaCard(
    val id: String,
    val label: String,
    val definition: String,
    val targetScore: Int,
    val manualScore: Int?,
    val confidence: Float,
    val isDirty: Boolean,
)

data class LifeWheelUiState(
    val title: String,
    val todayLabel: String,
    val discoveryDay: Int,
    val discoveryCommitted: Boolean,
    val overviewText: String,
    val dimensions: List<FingerprintDimension>,
    val rolesText: String,
    val responsibilitiesText: String,
    val priorityRulesText: String,
    val weeklyRhythm: String,
    val recurringCommitmentsText: String,
    val goodDayPattern: String,
    val badDayPattern: String,
    val dayStartHour: Int,
    val dayEndHour: Int,
    val morningEnergy: Int,
    val afternoonEnergy: Int,
    val eveningEnergy: Int,
    val focusStrength: Int,
    val disruptionSensitivity: Int,
    val recoveryNeed: Int,
    val activeAreas: List<LifeWheelAreaCard>,
    val didCompleteSetup: Boolean,
) {
    val canCommitDiscovery: Boolean
        get() = priorityRulesText.isNotBlank() && goodDayPattern.isNotBlank()
}

class LifeWheelViewModel(
    private val lifeWheelRepository: LifeWheelRepository,
    private val userFingerprintRepository: UserFingerprintRepository,
    private val learningEventRepository: LearningEventRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val todayIso = today.toString()

    private val rolesText = MutableStateFlow("")
    private val responsibilitiesText = MutableStateFlow("")
    private val priorityRulesText = MutableStateFlow("")
    private val weeklyRhythm = MutableStateFlow("")
    private val recurringCommitmentsText = MutableStateFlow("")
    private val goodDayPattern = MutableStateFlow("")
    private val badDayPattern = MutableStateFlow("")
    private val dayStartHour = MutableStateFlow(6)
    private val dayEndHour = MutableStateFlow(22)
    private val morningEnergy = MutableStateFlow(4)
    private val afternoonEnergy = MutableStateFlow(3)
    private val eveningEnergy = MutableStateFlow(2)
    private val focusStrength = MutableStateFlow(4)
    private val disruptionSensitivity = MutableStateFlow(3)
    private val recoveryNeed = MutableStateFlow(4)
    private val areaEditors = MutableStateFlow<Map<String, AreaEditorState>>(emptyMap())
    private val didCompleteSetup = MutableStateFlow(false)
    private var didSeedDraft = false

    private val fingerprintDraft = combine(
        rolesText,
        responsibilitiesText,
        priorityRulesText,
        weeklyRhythm,
        recurringCommitmentsText,
        goodDayPattern,
        badDayPattern,
        dayStartHour,
        dayEndHour,
        morningEnergy,
        afternoonEnergy,
        eveningEnergy,
        focusStrength,
        disruptionSensitivity,
        recoveryNeed,
    ) { values ->
        DraftUiState(
            rolesText = values[0] as String,
            responsibilitiesText = values[1] as String,
            priorityRulesText = values[2] as String,
            weeklyRhythm = values[3] as String,
            recurringCommitmentsText = values[4] as String,
            goodDayPattern = values[5] as String,
            badDayPattern = values[6] as String,
            dayStartHour = values[7] as Int,
            dayEndHour = values[8] as Int,
            morningEnergy = values[9] as Int,
            afternoonEnergy = values[10] as Int,
            eveningEnergy = values[11] as Int,
            focusStrength = values[12] as Int,
            disruptionSensitivity = values[13] as Int,
            recoveryNeed = values[14] as Int,
        )
    }

    private val fingerprintContent = combine(
        userFingerprintRepository.observe(),
        lifeWheelRepository.observeActiveAreas(),
        lifeWheelRepository.observeDailyChecks(todayIso),
        areaEditors,
        fingerprintDraft,
    ) { fingerprint, activeAreas, dailyChecks, currentEditors, draft ->
        maybeSeedDraft(fingerprint)
        FingerprintContentState(
            fingerprint = fingerprint,
            draft = draft,
            activeAreas = activeAreas,
            dailyChecks = dailyChecks,
            currentEditors = currentEditors,
        )
    }

    val state = combine(
        fingerprintContent,
        didCompleteSetup,
    ) { content, completed ->
        LifeWheelUiState(
            title = "Fingerprint Studio",
            todayLabel = today.format(DateTimeFormatter.ofPattern("EEEE, dd. MMMM", Locale.GERMAN)),
            discoveryDay = content.fingerprint.discoveryDay,
            discoveryCommitted = content.fingerprint.discoveryCommitted,
            overviewText = buildOverviewText(content.fingerprint),
            dimensions = content.fingerprint.dimensions,
            rolesText = content.draft.rolesText,
            responsibilitiesText = content.draft.responsibilitiesText,
            priorityRulesText = content.draft.priorityRulesText,
            weeklyRhythm = content.draft.weeklyRhythm,
            recurringCommitmentsText = content.draft.recurringCommitmentsText,
            goodDayPattern = content.draft.goodDayPattern,
            badDayPattern = content.draft.badDayPattern,
            dayStartHour = content.draft.dayStartHour,
            dayEndHour = content.draft.dayEndHour,
            morningEnergy = content.draft.morningEnergy,
            afternoonEnergy = content.draft.afternoonEnergy,
            eveningEnergy = content.draft.eveningEnergy,
            focusStrength = content.draft.focusStrength,
            disruptionSensitivity = content.draft.disruptionSensitivity,
            recoveryNeed = content.draft.recoveryNeed,
            activeAreas = content.activeAreas.map { area ->
                val editor = content.currentEditors[area.id] ?: AreaEditorState(
                    label = area.label,
                    definition = area.definition,
                    targetScore = area.targetScore,
                )
                LifeWheelAreaCard(
                    id = area.id,
                    label = editor.label,
                    definition = editor.definition,
                    targetScore = editor.targetScore,
                    manualScore = content.dailyChecks.firstOrNull { it.areaId == area.id }?.manualScore,
                    confidence = confidenceForArea(area, content.fingerprint),
                    isDirty = editor.label != area.label ||
                        editor.definition != area.definition ||
                        editor.targetScore != area.targetScore,
                )
            },
            didCompleteSetup = completed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LifeWheelUiState(
            title = "Fingerprint Studio",
            todayLabel = today.toString(),
            discoveryDay = 1,
            discoveryCommitted = false,
            overviewText = "",
            dimensions = emptyList(),
            rolesText = "",
            responsibilitiesText = "",
            priorityRulesText = "",
            weeklyRhythm = "",
            recurringCommitmentsText = "",
            goodDayPattern = "",
            badDayPattern = "",
            dayStartHour = 6,
            dayEndHour = 22,
            morningEnergy = 4,
            afternoonEnergy = 3,
            eveningEnergy = 2,
            focusStrength = 4,
            disruptionSensitivity = 3,
            recoveryNeed = 4,
            activeAreas = emptyList(),
            didCompleteSetup = false,
        ),
    )

    fun onRolesChange(value: String) {
        rolesText.value = value
    }

    fun onResponsibilitiesChange(value: String) {
        responsibilitiesText.value = value
    }

    fun onPriorityRulesChange(value: String) {
        priorityRulesText.value = value
    }

    fun onWeeklyRhythmChange(value: String) {
        weeklyRhythm.value = value
    }

    fun onRecurringCommitmentsChange(value: String) {
        recurringCommitmentsText.value = value
    }

    fun onGoodDayPatternChange(value: String) {
        goodDayPattern.value = value
    }

    fun onBadDayPatternChange(value: String) {
        badDayPattern.value = value
    }

    fun onDayStartHourChange(value: Int) {
        dayStartHour.value = value.coerceIn(5, 10)
    }

    fun onDayEndHourChange(value: Int) {
        dayEndHour.value = value.coerceIn(18, 24)
    }

    fun onMorningEnergyChange(value: Int) {
        morningEnergy.value = value.coerceIn(1, 5)
    }

    fun onAfternoonEnergyChange(value: Int) {
        afternoonEnergy.value = value.coerceIn(1, 5)
    }

    fun onEveningEnergyChange(value: Int) {
        eveningEnergy.value = value.coerceIn(1, 5)
    }

    fun onFocusStrengthChange(value: Int) {
        focusStrength.value = value.coerceIn(1, 5)
    }

    fun onDisruptionSensitivityChange(value: Int) {
        disruptionSensitivity.value = value.coerceIn(1, 5)
    }

    fun onRecoveryNeedChange(value: Int) {
        recoveryNeed.value = value.coerceIn(1, 5)
    }

    fun saveFingerprint() {
        viewModelScope.launch {
            userFingerprintRepository.save(currentDraft())
            learningEventRepository.record(
                type = LearningEventType.FINGERPRINT_SAVED,
                title = "Fingerprint aktualisiert",
                detail = priorityRulesText.value.lineSequence().firstOrNull().orEmpty(),
            )
        }
    }

    fun commitDiscovery() {
        viewModelScope.launch {
            userFingerprintRepository.save(currentDraft())
            userFingerprintRepository.markDiscoveryCommitted()
            learningEventRepository.record(
                type = LearningEventType.DISCOVERY_COMMITTED,
                title = "Discovery committen",
                detail = goodDayPattern.value,
            )
            didCompleteSetup.value = true
        }
    }

    fun acknowledgeCompletion() {
        didCompleteSetup.value = false
    }

    fun onAreaLabelChange(
        id: String,
        value: String,
    ) {
        updateAreaEditor(id) { it.copy(label = value) }
    }

    fun onAreaDefinitionChange(
        id: String,
        value: String,
    ) {
        updateAreaEditor(id) { it.copy(definition = value) }
    }

    fun onAreaTargetScoreChange(
        id: String,
        score: Int,
    ) {
        updateAreaEditor(id) { it.copy(targetScore = score.coerceIn(1, 5)) }
    }

    fun saveArea(id: String) {
        val editor = areaEditors.value[id] ?: return
        viewModelScope.launch {
            lifeWheelRepository.updateArea(
                id = id,
                label = editor.label,
                definition = editor.definition,
                targetScore = editor.targetScore,
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

    private fun maybeSeedDraft(fingerprint: UserFingerprint) {
        if (didSeedDraft) return
        didSeedDraft = true
        rolesText.value = fingerprint.roles.joinToString("\n")
        responsibilitiesText.value = fingerprint.responsibilities.joinToString("\n")
        priorityRulesText.value = fingerprint.priorityRules.joinToString("\n")
        weeklyRhythm.value = fingerprint.weeklyRhythm
        recurringCommitmentsText.value = fingerprint.recurringCommitments.joinToString("\n")
        goodDayPattern.value = fingerprint.goodDayPattern
        badDayPattern.value = fingerprint.badDayPattern
        dayStartHour.value = fingerprint.dayStartHour
        dayEndHour.value = fingerprint.dayEndHour
        morningEnergy.value = fingerprint.morningEnergy
        afternoonEnergy.value = fingerprint.afternoonEnergy
        eveningEnergy.value = fingerprint.eveningEnergy
        focusStrength.value = fingerprint.focusStrength
        disruptionSensitivity.value = fingerprint.disruptionSensitivity
        recoveryNeed.value = fingerprint.recoveryNeed
    }

    private fun currentDraft(): UserFingerprintDraft {
        return UserFingerprintDraft(
            rolesText = rolesText.value,
            responsibilitiesText = responsibilitiesText.value,
            priorityRulesText = priorityRulesText.value,
            weeklyRhythm = weeklyRhythm.value,
            recurringCommitmentsText = recurringCommitmentsText.value,
            goodDayPattern = goodDayPattern.value,
            badDayPattern = badDayPattern.value,
            dayStartHour = dayStartHour.value,
            dayEndHour = dayEndHour.value,
            morningEnergy = morningEnergy.value,
            afternoonEnergy = afternoonEnergy.value,
            eveningEnergy = eveningEnergy.value,
            focusStrength = focusStrength.value,
            disruptionSensitivity = disruptionSensitivity.value,
            recoveryNeed = recoveryNeed.value,
        )
    }

    private fun updateAreaEditor(
        id: String,
        transform: (AreaEditorState) -> AreaEditorState,
    ) {
        val current = areaEditors.value[id] ?: AreaEditorState(
            label = "",
            definition = "",
            targetScore = 3,
        )
        areaEditors.value = areaEditors.value + (id to transform(current))
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LifeWheelViewModel(
                    lifeWheelRepository = appContainer.lifeWheelRepository,
                    userFingerprintRepository = appContainer.userFingerprintRepository,
                    learningEventRepository = appContainer.learningEventRepository,
                    clock = appContainer.clock,
                )
            }
        }
    }
}

private data class DraftUiState(
    val rolesText: String,
    val responsibilitiesText: String,
    val priorityRulesText: String,
    val weeklyRhythm: String,
    val recurringCommitmentsText: String,
    val goodDayPattern: String,
    val badDayPattern: String,
    val dayStartHour: Int,
    val dayEndHour: Int,
    val morningEnergy: Int,
    val afternoonEnergy: Int,
    val eveningEnergy: Int,
    val focusStrength: Int,
    val disruptionSensitivity: Int,
    val recoveryNeed: Int,
)

private data class AreaEditorState(
    val label: String,
    val definition: String,
    val targetScore: Int,
)

private data class FingerprintContentState(
    val fingerprint: UserFingerprint,
    val draft: DraftUiState,
    val activeAreas: List<LifeArea>,
    val dailyChecks: List<com.struperto.androidappdays.data.repository.LifeAreaDailyCheck>,
    val currentEditors: Map<String, AreaEditorState>,
)

private fun buildOverviewText(fingerprint: UserFingerprint): String {
    val lead = fingerprint.priorityRules.firstOrNull()
        ?: fingerprint.lifeAreas.maxByOrNull(LifeArea::targetScore)?.label
        ?: "Klarheit"
    return "Discovery Tag ${fingerprint.discoveryDay}/7 · Leitlinie: $lead"
}

private fun confidenceForArea(
    area: LifeArea,
    fingerprint: UserFingerprint,
): Float {
    val lift = fingerprint.dimensions.firstOrNull()?.confidence ?: 0.52f
    return (0.34f + (area.targetScore / 5f) * 0.28f + lift * 0.3f).coerceIn(0.32f, 0.94f)
}
