package com.struperto.androidappdays.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaSourceBindingRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceRepository
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureRepository
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
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.service.EvaluationEngineV0
import com.struperto.androidappdays.domain.service.ObservationSyncService
import com.struperto.androidappdays.feature.start.WebFeedSyncCoordinator
import com.struperto.androidappdays.feature.start.parseAreaImportCapture
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val areaKernelRepository: AreaKernelRepository,
    private val areaSourceBindingRepository: AreaSourceBindingRepository,
    private val areaWebFeedSourceRepository: AreaWebFeedSourceRepository,
    private val captureRepository: CaptureRepository,
    private val goalRepository: GoalRepository,
    private val observationRepository: ObservationRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val observationSyncService: ObservationSyncService,
    private val evaluationEngineV0: EvaluationEngineV0,
    private val webFeedSyncCoordinator: WebFeedSyncCoordinator,
    private val healthPermissions: Set<String>,
    private val clock: Clock,
) : ViewModel() {
    private val today: LocalDate
        get() = LocalDate.now(clock)

    init {
        refresh()
    }

    private val areaInputs = combine(
        areaKernelRepository.observeActiveInstances(),
        areaSourceBindingRepository.observeAll(),
        areaWebFeedSourceRepository.observeAll(),
        captureRepository.observeOpen(),
    ) { activeAreas, sourceBindings, webFeedSources, openCaptures ->
        SettingsAreaInputs(
            activeAreas = activeAreas,
            sourceBindings = sourceBindings,
            webFeedSources = webFeedSources,
            openCaptures = openCaptures,
        )
    }

    private val settingsInputs = combine(
        areaInputs,
        goalRepository.observeGoals(),
        goalRepository.observeCatalog(),
        observationRepository.observeDay(today),
        sourceCapabilityRepository.observeProfile(),
    ) { areaInputs, goals, catalog, todayObservations, capabilityProfile ->
        SettingsInput(
            activeAreas = areaInputs.activeAreas,
            sourceBindings = areaInputs.sourceBindings,
            webFeedSources = areaInputs.webFeedSources,
            openCaptures = areaInputs.openCaptures,
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
                activeAreas = input.activeAreas,
                sourceBindings = input.sourceBindings,
                webFeedSources = input.webFeedSources,
                openCaptures = input.openCaptures,
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

    fun deleteArea(areaId: String) {
        viewModelScope.launch {
            areaSourceBindingRepository.clearArea(areaId)
            areaWebFeedSourceRepository.clearArea(areaId)
            areaKernelRepository.deleteActiveInstance(areaId)
            webFeedSyncCoordinator.ensureScheduled()
        }
    }

    fun dismissPendingImport(importId: String) {
        viewModelScope.launch {
            captureRepository.archive(importId)
        }
    }

    fun refreshWebFeeds() {
        viewModelScope.launch {
            webFeedSyncCoordinator.syncAll()
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
                    areaKernelRepository = appContainer.areaKernelRepository,
                    areaSourceBindingRepository = appContainer.areaSourceBindingRepository,
                    areaWebFeedSourceRepository = appContainer.areaWebFeedSourceRepository,
                    captureRepository = appContainer.captureRepository,
                    goalRepository = appContainer.goalRepository,
                    observationRepository = appContainer.observationRepository,
                    sourceCapabilityRepository = appContainer.sourceCapabilityRepository,
                    observationSyncService = appContainer.observationSyncService,
                    evaluationEngineV0 = appContainer.evaluationEngineV0,
                    webFeedSyncCoordinator = appContainer.webFeedSyncCoordinator,
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
    activeAreas: List<AreaInstance>,
    sourceBindings: List<com.struperto.androidappdays.data.repository.AreaSourceBinding>,
    webFeedSources: List<AreaWebFeedSource>,
    openCaptures: List<CaptureItem>,
    healthPermissions: Set<String>,
): SettingsUiState {
    val bindingsByAreaId = sourceBindings.groupBy { it.areaId }
    val webFeedSourcesByAreaId = webFeedSources.groupBy(AreaWebFeedSource::areaId)
    val importsByAreaId = openCaptures
        .mapNotNull { capture ->
            parseAreaImportCapture(capture)?.let { parsed ->
                capture.areaId?.let { areaId -> areaId to parsed }
            }
        }
        .groupBy(
            keySelector = { it.first },
            valueTransform = { it.second },
        )
    val pendingImports = openCaptures
        .mapNotNull { capture ->
            if (capture.areaId != null) {
                null
            } else {
                parseAreaImportCapture(capture)?.let { parsed ->
                    SettingsInboxItem(
                        id = parsed.id,
                        title = parsed.title,
                        detail = parsed.detail,
                        kindLabel = parsed.kind.label,
                    )
                }
            }
        }
        .sortedBy { it.title.lowercase() }
    val directAreaCount = activeAreas.count { area ->
        bindingsByAreaId[area.areaId]
            .orEmpty()
            .mapNotNull { binding -> capabilityProfile.find(binding.source) }
            .firstOrNull()
            ?.let { it.granted && it.enabled && it.available }
            ?: false
    }
    val waitingAreaCount = activeAreas.count { area ->
        bindingsByAreaId[area.areaId]
            .orEmpty()
            .mapNotNull { binding -> capabilityProfile.find(binding.source) }
            .firstOrNull()
            ?.let { !(it.granted && it.enabled && it.available) }
            ?: false
    }
    val importAreaCount = activeAreas.count { area ->
        bindingsByAreaId[area.areaId].isNullOrEmpty() && importsByAreaId[area.areaId].orEmpty().isNotEmpty()
    }
    val manualAreaCount = activeAreas.count { area ->
        bindingsByAreaId[area.areaId].isNullOrEmpty() && importsByAreaId[area.areaId].orEmpty().isEmpty()
    }
    val feedAreas = activeAreas
        .sortedBy(AreaInstance::sortOrder)
        .mapNotNull { area ->
            val sources = webFeedSourcesByAreaId[area.areaId].orEmpty()
            if (sources.isEmpty()) return@mapNotNull null
            SettingsFeedAreaItem(
                areaId = area.areaId,
                areaTitle = area.title,
                sourceCount = sources.size,
                activeAutoSyncCount = sources.count(AreaWebFeedSource::isAutoSyncEnabled),
                summary = area.summary,
                goalLabel = buildWebFeedAreaGoal(sources),
                capabilityLabels = sources
                    .flatMap { webFeedCapabilityLabels(it.sourceKind, it.isAutoSyncEnabled) }
                    .distinct()
                    .take(4),
                sources = sources.map { source ->
                    SettingsFeedSourceItem(
                        url = source.url,
                        hostLabel = source.url.removePrefix("https://").removePrefix("http://").substringBefore('/'),
                        sourceKindLabel = source.sourceKind.label,
                        autoSyncEnabled = source.isAutoSyncEnabled,
                        syncCadenceLabel = if (source.isAutoSyncEnabled) source.syncCadence.label else "Nur manuell",
                        goalLabel = buildWebFeedSourceGoal(source),
                        capabilityLabels = webFeedCapabilityLabels(source.sourceKind, source.isAutoSyncEnabled),
                        lastStatusLabel = source.lastStatusLabel,
                        lastStatusDetail = source.lastStatusDetail,
                    )
                },
            )
        }
    val analysisItems = listOf(
        SettingsAnalysisItem(
            title = "Routing",
            statusLabel = "Live",
            detail = "Prompts werden in Systemnamen, Quelle, Familie und naechsten Schritt uebersetzt.",
        ),
        SettingsAnalysisItem(
            title = "Bereichsanalyse",
            statusLabel = "${activeAreas.size} aktiv",
            detail = "Jeder Bereich kann jetzt vor dem Oeffnen kurz neu fuer Eingang, Routing und Ziele gelesen werden.",
        ),
        SettingsAnalysisItem(
            title = "Android ML",
            statusLabel = "Naechster Schritt",
            detail = "TextClassifier ist bereits nutzbar. OCR, Entity Extraction und lokale Sprachsignale sind die naechsten Bausteine.",
        ),
        SettingsAnalysisItem(
            title = "Lokale AI",
            statusLabel = "Optional",
            detail = "Gemini Nano, lokale Kurztexte und spaetere semantische Suche bleiben moeglich, aber bewusst nachgelagert.",
        ),
    )
    val analysisGoals = listOf(
        SettingsAnalysisGoal(
            title = "Routing schaerfen",
            detail = "Mehrdeutige Alltagsprompts sauber zwischen Alltagssystem, Quelle und Rueckfrage aufteilen.",
            progressLabel = if (waitingAreaCount > 0) "Jetzt" else "Laufend",
        ),
        SettingsAnalysisGoal(
            title = "Bereichsformat stabil halten",
            detail = "Ein kompaktes maschinenlesbares Format pro Bereich fuer Ziele, Training und spaetere Vergleiche halten.",
            progressLabel = "Jetzt",
        ),
        SettingsAnalysisGoal(
            title = "Importe lesen",
            detail = "OCR fuer Screenshots, Dokumente und Belege vorbereiten und spaeter in Bereiche rueckfuehren.",
            progressLabel = "Als naechstes",
        ),
        SettingsAnalysisGoal(
            title = "Lokale Suche",
            detail = "Cluster, Kurztext und spaetere Suche nur dort freischalten, wo Material und Alltagssystem es tragen.",
            progressLabel = "Iterativ",
        ),
    )
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
                    GoalPriority.CORE -> "Kern"
                    GoalPriority.SUPPORT -> "Stuetze"
                    GoalPriority.PLACEHOLDER -> "Spaeter"
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
            val statusCopy = catalogStatusCopy(entry, evaluation?.state)
            SettingsCatalogItem(
                domain = entry.domain,
                title = entry.title,
                summary = entry.summary,
                statusLabel = statusCopy.label,
                statusDetail = statusCopy.detail,
            )
        },
        activeAreas = activeAreas
            .sortedBy(AreaInstance::sortOrder)
            .map { area ->
                val boundSources = bindingsByAreaId[area.areaId].orEmpty()
                val linkedFeeds = webFeedSourcesByAreaId[area.areaId].orEmpty()
                val linkedImports = importsByAreaId[area.areaId].orEmpty()
                val primarySource = boundSources
                    .mapNotNull { binding -> capabilityProfile.find(binding.source) }
                    .firstOrNull()
                val statusLabel = when {
                    primarySource?.granted == true && primarySource.enabled && primarySource.available -> "Direkt"
                    primarySource != null -> "Quelle offen"
                    linkedFeeds.isNotEmpty() -> "Web/Feed"
                    linkedImports.isNotEmpty() -> "Mit Import"
                    else -> "Manuell"
                }
                val statusDetail = when {
                    primarySource?.granted == true && primarySource.enabled && primarySource.available ->
                        "Dieser Bereich bekommt schon echte Signale aus ${primarySource.label.lowercase()}."
                    primarySource != null && primarySource.available && primarySource.enabled ->
                        "Die gedachte Quelle ist da, aber Android-Freigaben fehlen noch."
                    primarySource != null && primarySource.available ->
                        "Die Quelle ist auf diesem Geraet verfuegbar, in Days aber noch nicht aktiv."
                    primarySource != null ->
                        "Die gedachte Quelle ist fuer diesen Bereich noch nicht sauber nutzbar."
                    linkedFeeds.isNotEmpty() ->
                        "Dieser Bereich hat eine echte Web- oder Feed-Verbindung und kann neue Eintraege nachladen."
                    linkedImports.isNotEmpty() ->
                        "Dieser Bereich lebt gerade aus geteiltem Material statt aus einer festen Systemquelle."
                    else ->
                        "Dieser Bereich lebt aktuell nur aus deinem Auftrag und manuellen Eingaben."
                }
                SettingsAreaItem(
                    id = area.areaId,
                    title = area.title,
                    summary = area.summary,
                    statusLabel = statusLabel,
                    statusDetail = statusDetail,
                    sourceLabel = when {
                        primarySource != null -> primarySource.label
                        linkedFeeds.isNotEmpty() -> "Web/Feed"
                        linkedImports.isNotEmpty() -> "Import"
                        else -> "Ohne Quelle"
                    },
                    sourceDetail = when {
                        primarySource?.granted == true && primarySource.enabled && primarySource.available ->
                            "Die Quelle ist bereit und liefert diesem Bereich schon verwertbare Signale."
                        primarySource != null && primarySource.available && primarySource.enabled ->
                            "Die Quelle ist verbunden, aber Android muss noch final freigeben."
                        primarySource != null && primarySource.available ->
                            "Die Quelle ist da, aber in Days aktuell noch ausgeschaltet."
                        primarySource != null ->
                            "Die Quelle passt fachlich, ist auf diesem Geraet aber noch nicht einsatzbereit."
                        linkedFeeds.isNotEmpty() ->
                            "Dieser Bereich hat ${linkedFeeds.size} gemerkte Web- oder Feed-Adresse(n) und kann sie regelmaessig lesen."
                        linkedImports.isNotEmpty() ->
                            "Hier landen Inhalte, die du aus anderen Apps oder ueber Dateiwaehler hereingibst."
                        else ->
                            "Noch keine feste Quelle. Dieser Bereich bleibt bewusst manuell."
                    },
                    materialLabel = when {
                        linkedImports.isEmpty() -> "Noch kein Material"
                        linkedImports.size == 1 -> "1 Material"
                        else -> "${linkedImports.size} Materialien"
                    },
                    materialDetail = when {
                        linkedImports.isEmpty() && primarySource != null ->
                            "Der Bereich ist vorbereitet. Sobald echte Signale oder Importe ankommen, landen sie hier."
                        linkedImports.isEmpty() && linkedFeeds.isNotEmpty() ->
                            linkedFeeds.joinToString(limit = 2, separator = " · ") {
                                it.url.removePrefix("https://").removePrefix("http://").substringBefore('/')
                            }
                        linkedImports.isEmpty() ->
                            "Noch keine geteilten Inhalte oder Dateien in diesem Bereich."
                        else ->
                            linkedImports.joinToString(limit = 2, separator = " · ") { it.title }
                    },
                    nextStepLabel = when {
                        primarySource?.granted == true && primarySource.enabled && primarySource.available -> "Bereich schaerfen"
                        primarySource != null -> "Quelle verbinden"
                        linkedFeeds.isNotEmpty() -> "Feed pflegen"
                        linkedImports.isNotEmpty() -> "Material sichten"
                        else -> "Bedeutung schaerfen"
                    },
                    nextStepDetail = when {
                        primarySource?.granted == true && primarySource.enabled && primarySource.available ->
                            "Im Bereich selbst kannst du nun Ziel, Arbeitsweise und Material fokussieren."
                        primarySource != null ->
                            "Oeffne Quellen oder die Systemeinstellungen, damit dieser Bereich echte Signale bekommt."
                        linkedFeeds.isNotEmpty() ->
                            "Im Bereich kannst du Web-Adressen merken, den Sync ausloesen und neue Eintraege lesen lassen."
                        linkedImports.isNotEmpty() ->
                            "Oeffne den Bereich und entscheide, was von den importierten Inhalten wirklich bleiben soll."
                        else ->
                            "Oeffne den Bereich und bringe Titel, Auftrag und Arbeitsweise in eine klare Form."
                    },
                )
            },
        directAreaCount = directAreaCount,
        waitingAreaCount = waitingAreaCount,
        importAreaCount = importAreaCount,
        manualAreaCount = manualAreaCount,
        assignedImportCount = importsByAreaId.values.sumOf(List<*>::size),
        pendingImports = pendingImports,
        feedAreas = feedAreas,
        activeFeedAreaCount = feedAreas.size,
        activeFeedSourceCount = feedAreas.sumOf(SettingsFeedAreaItem::sourceCount),
        analysisItems = analysisItems,
        analysisGoals = analysisGoals,
        healthPermissions = healthPermissions,
    )
}

private fun buildWebFeedSourceGoal(source: AreaWebFeedSource): String {
    return when (source.sourceKind) {
        AreaWebFeedSourceKind.Website -> "Direkt lesen"
        AreaWebFeedSourceKind.Feed -> "Neu nachladen"
    }
}

private fun buildWebFeedAreaGoal(sources: List<AreaWebFeedSource>): String {
    val hasWebsite = sources.any { it.sourceKind == AreaWebFeedSourceKind.Website }
    val hasFeed = sources.any { it.sourceKind == AreaWebFeedSourceKind.Feed }
    return when {
        hasWebsite && hasFeed -> "Webseiten lesen, Feed finden und aktuelle Artikel nachladen"
        hasFeed -> "Aktuelle Artikel im Blick halten"
        hasWebsite -> "Artikel direkt lesen und neue Artikel finden"
        else -> "Web-Inhalte sammeln"
    }
}

private fun webFeedCapabilityLabels(
    kind: AreaWebFeedSourceKind,
    autoSyncEnabled: Boolean,
): List<String> {
    val base = when (kind) {
        AreaWebFeedSourceKind.Website -> listOf("Artikel lesen", "Artikel finden", "Feed suchen")
        AreaWebFeedSourceKind.Feed -> listOf("Aktuelle Artikel lesen", "Neuigkeiten finden", "Duplikate vermeiden")
    }
    return if (autoSyncEnabled) base + "Automatisch nachladen" else base
}

private data class SettingsAreaInputs(
    val activeAreas: List<AreaInstance>,
    val sourceBindings: List<com.struperto.androidappdays.data.repository.AreaSourceBinding>,
    val webFeedSources: List<AreaWebFeedSource>,
    val openCaptures: List<CaptureItem>,
)

private data class SettingsInput(
    val activeAreas: List<AreaInstance>,
    val sourceBindings: List<com.struperto.androidappdays.data.repository.AreaSourceBinding>,
    val webFeedSources: List<AreaWebFeedSource>,
    val openCaptures: List<CaptureItem>,
    val goals: List<DomainGoal>,
    val catalog: List<DomainCatalogEntry>,
    val todayObservations: List<DomainObservation>,
    val capabilityProfile: CapabilityProfile,
)

private data class SettingsCatalogStatusCopy(
    val label: String,
    val detail: String,
)

private fun catalogStatusCopy(
    entry: DomainCatalogEntry,
    evaluationState: EvaluationState?,
): SettingsCatalogStatusCopy {
    if (!entry.isActive) {
        return SettingsCatalogStatusCopy(
            label = "Spaeter",
            detail = "Der Bereich ist vorbereitet, aber noch nicht Teil deines aktiven Setups.",
        )
    }
    if (evaluationState == null) {
        return SettingsCatalogStatusCopy(
            label = "Bereit",
            detail = "Struktur und Zielbild stehen, heute fehlt nur noch eine klare Lage.",
        )
    }
    return when (evaluationState) {
        EvaluationState.ON_TRACK -> SettingsCatalogStatusCopy(
            label = "Stabil",
            detail = "Ziel und Tageswert laufen aktuell sauber zusammen.",
        )
        EvaluationState.BELOW_TARGET -> SettingsCatalogStatusCopy(
            label = "Braucht Zug",
            detail = "Heute fehlt noch sichtbarer Fortschritt in diesem Bereich.",
        )
        EvaluationState.ABOVE_TARGET -> SettingsCatalogStatusCopy(
            label = "Zu hoch",
            detail = "Der Bereich liegt ueber dem aktuell gesetzten Rahmen.",
        )
        EvaluationState.OUTSIDE_WINDOW -> SettingsCatalogStatusCopy(
            label = "Fenster spaeter",
            detail = "Das Ziel wirkt erst wieder im naechsten Zeitfenster.",
        )
        EvaluationState.UNKNOWN -> SettingsCatalogStatusCopy(
            label = "Offen",
            detail = "Es gibt noch zu wenig Signal fuer eine klare Einschaetzung.",
        )
    }
}

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
