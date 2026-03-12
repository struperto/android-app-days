package com.struperto.androidappdays.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.GoalRepository
import com.struperto.androidappdays.data.repository.ObservationRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.data.repository.lifeDomainLabel
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainCatalogEntry
import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.GoalPriority
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.service.EvaluationEngineV0
import com.struperto.androidappdays.domain.service.ObservationSyncService
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val goalRepository: GoalRepository,
    private val observationRepository: ObservationRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val observationSyncService: ObservationSyncService,
    private val evaluationEngineV0: EvaluationEngineV0,
    private val healthPermissions: Set<String>,
    private val clock: Clock,
) : ViewModel() {
    private val today: LocalDate
        get() = LocalDate.now(clock)

    init {
        refresh()
    }

    private val settingsInputs = combine(
        goalRepository.observeGoals(),
        goalRepository.observeCatalog(),
        observationRepository.observeDay(today),
        sourceCapabilityRepository.observeProfile(),
    ) { goals, catalog, todayObservations, capabilityProfile ->
        SettingsInput(
            goals = goals,
            catalog = catalog,
            todayObservations = todayObservations,
            capabilityProfile = capabilityProfile,
        )
    }

    val state = settingsInputs
        .combine(sourceCapabilityRepository.observeProfile()) { input, _ ->
            val evaluations = evaluationEngineV0.evaluate(
                goals = input.goals.filter(DomainGoal::isActive),
                observations = input.todayObservations,
                capabilityProfile = input.capabilityProfile,
                logicalDate = today,
            )
            buildSettingsUiState(
                goals = input.goals,
                catalog = input.catalog,
                todayObservations = input.todayObservations,
                capabilityProfile = input.capabilityProfile,
                evaluations = evaluations,
                healthPermissions = healthPermissions,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(
                healthPermissions = healthPermissions,
            ),
        )

    fun refresh() {
        viewModelScope.launch {
            observationSyncService.syncDay(today)
        }
    }

    fun setSourceEnabled(
        source: DataSourceKind,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            sourceCapabilityRepository.setEnabled(source, enabled)
            observationSyncService.syncDay(today)
        }
    }

    fun saveGoal(
        goalId: String,
        minimumText: String,
        maximumText: String,
        preferredStartHour: String,
        preferredEndHour: String,
    ) {
        viewModelScope.launch {
            val current = goalRepository.loadActiveGoals().firstOrNull { it.id == goalId } ?: return@launch
            goalRepository.save(
                current.copy(
                    target = current.target.copy(
                        minimum = minimumText.toFloatOrNullLoose() ?: current.target.minimum,
                        maximum = maximumText.toFloatOrNullLoose().takeIf { current.target.maximum != null }
                            ?: current.target.maximum,
                    ),
                    preferredWindow = current.preferredWindow?.copy(
                        startLogicalHour = preferredStartHour.toIntOrNull() ?: current.preferredWindow.startLogicalHour,
                        endLogicalHourExclusive = preferredEndHour.toIntOrNull() ?: current.preferredWindow.endLogicalHourExclusive,
                    ) ?: current.preferredWindow,
                ),
            )
        }
    }

    fun saveManualMetric(
        goalId: String?,
        domain: LifeDomain,
        metric: ObservationMetric,
        valueText: String,
        unit: String,
    ) {
        viewModelScope.launch {
            observationRepository.saveManualNumeric(
                logicalDate = today,
                domain = domain,
                metric = metric,
                value = valueText.toFloatOrNullLoose(),
                unit = unit,
                goalId = goalId,
            )
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    goalRepository = appContainer.goalRepository,
                    observationRepository = appContainer.observationRepository,
                    sourceCapabilityRepository = appContainer.sourceCapabilityRepository,
                    observationSyncService = appContainer.observationSyncService,
                    evaluationEngineV0 = appContainer.evaluationEngineV0,
                    healthPermissions = appContainer.healthConnectRepository.requiredPermissions,
                    clock = appContainer.clock,
                )
            }
        }
    }
}

private fun buildSettingsUiState(
    goals: List<DomainGoal>,
    catalog: List<DomainCatalogEntry>,
    todayObservations: List<DomainObservation>,
    capabilityProfile: CapabilityProfile,
    evaluations: List<com.struperto.androidappdays.domain.DomainEvaluation>,
    healthPermissions: Set<String>,
): SettingsUiState {
    return SettingsUiState(
        sources = capabilityProfile.sources.map { source ->
            SettingsSourceItem(
                source = source.source,
                label = source.label,
                detail = source.detail,
                enabled = source.enabled,
                available = source.available,
                granted = source.granted,
            )
        },
        goals = goals.filter(DomainGoal::isActive).map { goal ->
            SettingsGoalItem(
                id = goal.id,
                domain = goal.domain,
                title = goal.title,
                priorityLabel = when (goal.priority) {
                    GoalPriority.CORE -> "Core"
                    GoalPriority.SUPPORT -> "Support"
                    GoalPriority.PLACEHOLDER -> "Geplant"
                },
                unit = goal.target.unit,
                minimumText = goal.target.minimum?.displayValue().orEmpty(),
                maximumText = goal.target.maximum?.displayValue().orEmpty(),
                preferredStartHour = goal.preferredWindow?.startLogicalHour,
                preferredEndHourExclusive = goal.preferredWindow?.endLogicalHourExclusive,
            )
        },
        manualMetrics = goals.filter(DomainGoal::isActive).map { goal ->
            SettingsManualMetricItem(
                goalId = goal.id,
                domain = goal.domain,
                label = lifeDomainLabel(goal.domain),
                metric = metricFor(goal.domain),
                unit = goal.target.unit,
                valueText = todayObservations
                    .filter { it.domain == goal.domain && it.metric == metricFor(goal.domain) }
                    .maxByOrNull { it.startedAt }
                    ?.value
                    ?.numeric
                    ?.displayValue()
                    .orEmpty(),
            )
        },
        catalog = catalog.map { entry ->
            val evaluation = evaluations.firstOrNull { it.domain == entry.domain }
            SettingsCatalogItem(
                domain = entry.domain,
                title = entry.title,
                summary = entry.summary,
                statusLabel = when {
                    !entry.isActive -> "geplant"
                    evaluation == null -> "aktiv"
                    else -> when (evaluation.state) {
                        com.struperto.androidappdays.domain.EvaluationState.ON_TRACK -> "stabil"
                        com.struperto.androidappdays.domain.EvaluationState.BELOW_TARGET -> "zieht"
                        com.struperto.androidappdays.domain.EvaluationState.ABOVE_TARGET -> "ueber Ziel"
                        com.struperto.androidappdays.domain.EvaluationState.OUTSIDE_WINDOW -> "ausserhalb"
                        com.struperto.androidappdays.domain.EvaluationState.UNKNOWN -> "offen"
                    }
                },
            )
        },
        healthPermissions = healthPermissions,
    )
}

private data class SettingsInput(
    val goals: List<DomainGoal>,
    val catalog: List<DomainCatalogEntry>,
    val todayObservations: List<DomainObservation>,
    val capabilityProfile: CapabilityProfile,
)

private fun metricFor(domain: LifeDomain): ObservationMetric {
    return when (domain) {
        LifeDomain.SLEEP -> ObservationMetric.SLEEP_HOURS
        LifeDomain.MOVEMENT -> ObservationMetric.STEPS
        LifeDomain.HYDRATION -> ObservationMetric.HYDRATION_LITERS
        LifeDomain.NUTRITION -> ObservationMetric.PROTEIN_GRAMS
        LifeDomain.FOCUS -> ObservationMetric.FOCUS_MINUTES
        LifeDomain.STRESS -> ObservationMetric.NOTIFICATION_LOAD
        else -> ObservationMetric.FOCUS_MINUTES
    }
}

private fun Float.displayValue(): String {
    val rounded = toInt()
    return if (this == rounded.toFloat()) rounded.toString() else String.format("%.1f", this)
}

private fun String.toFloatOrNullLoose(): Float? {
    return replace(',', '.').trim().takeIf(String::isNotEmpty)?.toFloatOrNull()
}
