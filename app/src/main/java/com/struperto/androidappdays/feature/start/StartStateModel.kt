package com.struperto.androidappdays.feature.start

import androidx.compose.ui.graphics.vector.ImageVector
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaDailyCheck
import com.struperto.androidappdays.data.repository.LifeAreaProfile
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.domain.area.AreaBlueprint
import com.struperto.androidappdays.domain.area.AreaDefinition
import com.struperto.androidappdays.domain.area.AreaDirectionMode
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaLageType
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaProfileConfig
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.AreaSourceTruth
import com.struperto.androidappdays.domain.area.AreaSourceChannel
import com.struperto.androidappdays.domain.area.AreaSourcesMode
import com.struperto.androidappdays.domain.area.AreaFreshnessBand
import com.struperto.androidappdays.domain.area.AreaSeverity
import com.struperto.androidappdays.domain.area.AreaTodayOutput
import com.struperto.androidappdays.domain.area.AreaTodayOutputInput
import com.struperto.androidappdays.domain.area.mapping.toAreaBlueprint
import com.struperto.androidappdays.domain.area.mapping.toAreaDefinition
import com.struperto.androidappdays.domain.area.mapping.toAreaInstance
import com.struperto.androidappdays.domain.area.mapping.toAreaInstanceWithFallbackDefaults
import com.struperto.androidappdays.domain.area.mapping.toAreaSnapshot
import com.struperto.androidappdays.domain.area.projectAreaTodayOutput
import java.time.Instant

data class StartOverviewState(
    val areas: List<StartAreaOverviewTile> = emptyList(),
)

data class StartOverviewKernelInput(
    val definition: AreaDefinition?,
    val blueprint: AreaBlueprint?,
    val instance: AreaInstance,
    val snapshot: AreaSnapshot?,
    val openPlanTitles: List<String> = emptyList(),
    val dueCount: Int = 0,
    val logicalDate: java.time.LocalDate = snapshot?.date ?: java.time.LocalDate.of(1970, 1, 1),
    val projectionTime: Instant = Instant.EPOCH,
) {
    init {
        require(definition == null || definition.id == instance.definitionId) {
            "AreaDefinition.id must match AreaInstance.definitionId when provided."
        }
        require(blueprint == null || blueprint.areaId == instance.definitionId) {
            "AreaBlueprint.areaId must match AreaInstance.definitionId when provided."
        }
        require(snapshot == null || snapshot.areaId == instance.areaId) {
            "AreaSnapshot.areaId must match AreaInstance.areaId when provided."
        }
    }
}

data class StartAreaOverviewTile(
    val areaId: String,
    val label: String,
    val summary: String,
    val family: StartAreaFamily,
    val todayLabel: String,
    val todayStepLabel: String,
    val templateId: String,
    val iconKey: String,
    val statusKind: StartAreaStatusKind,
    val statusLabel: String,
    val primaryHint: StartAreaHintState,
    val focusLabel: String,
    val profileLabel: String,
    val progress: Float,
    val canMoveEarlier: Boolean,
    val canMoveLater: Boolean,
    val todayOutput: AreaTodayOutput,
)

data class StartAreaDetailKernelInput(
    val definition: AreaDefinition,
    val blueprint: AreaBlueprint,
    val instance: AreaInstance,
    val snapshot: AreaSnapshot?,
    val legacyBlueprint: StartAreaBlueprint,
    val openPlanTitles: List<String> = emptyList(),
    val dueCount: Int = 0,
    val logicalDate: java.time.LocalDate = snapshot?.date ?: java.time.LocalDate.of(1970, 1, 1),
    val projectionTime: Instant = Instant.EPOCH,
) {
    init {
        require(definition.id == instance.definitionId) {
            "AreaDefinition.id must match AreaInstance.definitionId."
        }
        require(blueprint.areaId == instance.definitionId) {
            "AreaBlueprint.areaId must match AreaInstance.definitionId."
        }
        require(legacyBlueprint.id == instance.areaId) {
            "StartAreaBlueprint.id must match AreaInstance.areaId."
        }
        require(snapshot == null || snapshot.areaId == instance.areaId) {
            "AreaSnapshot.areaId must match AreaInstance.areaId when provided."
        }
    }
}

data class StartAreaPanelState(
    val panel: StartAreaPanel,
    val title: String,
    val summary: String,
    val countLabel: String,
    val icon: ImageVector,
    val screenState: StartPanelScreenState,
)

data class StartAreaDetailState(
    val areaId: String,
    val title: String,
    val summary: String,
    val family: StartAreaFamily,
    val templateId: String,
    val iconKey: String,
    val tracks: List<String>,
    val targetScore: Int,
    val manualScore: Int?,
    val manualStateKey: String?,
    val manualNote: String?,
    val cadence: String,
    val intensity: Int,
    val signalBlend: Int,
    val selectedTracks: Set<String>,
    val remindersEnabled: Boolean,
    val reviewEnabled: Boolean,
    val experimentsEnabled: Boolean,
    val profileState: StartAreaProfileState,
    val blueprint: StartAreaBlueprint,
    val focusTrack: String,
    val flowCount: Int,
    val statusKind: StartAreaStatusKind,
    val statusLabel: String,
    val hints: List<StartAreaHintState>,
    val todayLabel: String,
    val todayRecommendation: String,
    val todayStepLabel: String,
    val progress: Float,
    val panelStates: List<StartAreaPanelState>,
    val todayOutput: AreaTodayOutput,
)

data class StartAreaProfileState(
    val lageMode: AreaLageMode,
    val lageLabel: String,
    val directionMode: AreaDirectionMode,
    val directionLabel: String,
    val sourcesMode: AreaSourcesMode,
    val sourcesLabel: String,
    val flowProfile: AreaFlowProfile,
    val flowLabel: String,
    val overviewLabel: String,
)

data class StartPanelScreenState(
    val headerLabel: String,
    val infoLabel: String,
    val core: StartPanelCoreState,
    val metrics: List<StartPanelMetricState>,
    val actions: List<StartPanelActionState>,
    val effectLabel: String = "",
)

data class StartPanelCoreState(
    val title: String,
    val value: String,
    val caption: String,
    val progress: Float,
)

data class StartPanelMetricState(
    val label: String,
    val value: String,
)

data class StartPanelActionState(
    val id: StartPanelActionId,
    val label: String,
    val valueLabel: String,
    val mode: StartPanelActionMode,
    val sheet: StartPanelSheetState? = null,
) {
    val supportingLabel: String
        get() = resolveSelectedActionSupportingLabel(sheet)
}

data class StartPanelSheetState(
    val title: String,
    val selectionMode: StartPanelSelectionMode,
    val options: List<StartPanelOptionState>,
)

data class StartPanelOptionState(
    val id: String,
    val label: String,
    val selected: Boolean,
    val supportingLabel: String = "",
)

private fun resolveSelectedActionSupportingLabel(
    sheet: StartPanelSheetState?,
): String {
    val actionSheet = sheet ?: return ""
    val selectedOptions = actionSheet.options.filter(StartPanelOptionState::selected)
    if (selectedOptions.isEmpty()) {
        return ""
    }
    val supportingLabels = selectedOptions
        .map { option -> option.supportingLabel.ifBlank { option.label } }
        .filter(String::isNotBlank)
    if (supportingLabels.isEmpty()) {
        return ""
    }
    return when (actionSheet.selectionMode) {
        StartPanelSelectionMode.Single -> supportingLabels.first()
        StartPanelSelectionMode.Multiple -> when (supportingLabels.size) {
            1 -> supportingLabels.first()
            else -> "${supportingLabels.first()} +${supportingLabels.size - 1} weitere aktiv"
        }
    }
}

enum class StartPanelActionId {
    SnapshotScore,
    SnapshotState,
    SnapshotMode,
    SnapshotClear,
    PathTarget,
    PathCadence,
    PathFocus,
    PathMode,
    SourcesTracks,
    SourcesBlend,
    SourcesMode,
    FlowIntensity,
    FlowSwitches,
    FlowProfile,
}

enum class StartPanelActionMode {
    Sheet,
    Direct,
}

enum class StartPanelSelectionMode {
    Single,
    Multiple,
}

enum class StartAreaStatusKind {
    Waiting,
    Live,
    Stable,
    Pull,
}

enum class StartAreaFamily(
    val label: String,
    val shortLabel: String,
) {
    Radar(
        label = "Radar",
        shortLabel = "Radar",
    ),
    Pflicht(
        label = "Pflicht",
        shortLabel = "Pflicht",
    ),
    Routine(
        label = "Routine",
        shortLabel = "Routine",
    ),
    Kontakt(
        label = "Kontakt",
        shortLabel = "Kontakt",
    ),
    Gesundheit(
        label = "Gesundheit",
        shortLabel = "Gesundheit",
    ),
    Ort(
        label = "Ort",
        shortLabel = "Ort",
    ),
    Sammlung(
        label = "Sammlung",
        shortLabel = "Sammlung",
    ),
}

enum class StartAreaHintTone {
    Quiet,
    Notice,
    Warning,
}

data class StartAreaHintState(
    val id: String,
    val title: String,
    val detail: String,
    val compactLabel: String,
    val tone: StartAreaHintTone,
)

fun mapStartOverviewState(
    areas: List<LifeArea>,
    dailyChecks: List<LifeAreaDailyCheck>,
): StartOverviewState {
    return projectStartOverviewState(
        inputs = buildStartOverviewKernelInputs(
            areas = areas,
            dailyChecks = dailyChecks,
        ),
    )
}

fun buildStartOverviewKernelInputs(
    areas: List<LifeArea>,
    dailyChecks: List<LifeAreaDailyCheck>,
): List<StartOverviewKernelInput> {
    val checkMap = dailyChecks.associateBy(LifeAreaDailyCheck::areaId)
    return areas.map { area ->
        val definition = startAreaDefinition(area.id)
        val blueprint = startAreaKernelBlueprint(area.id)
        StartOverviewKernelInput(
            definition = definition,
            blueprint = blueprint,
            instance = definition?.let { area.toAreaInstance(definition = it) }
                ?: area.toAreaInstanceWithFallbackDefaults(),
            snapshot = checkMap[area.id]?.toAreaSnapshot(),
        )
    }
}

fun buildStartOverviewKernelInputsFromKernel(
    instances: List<AreaInstance>,
    snapshots: List<AreaSnapshot>,
    todayPlans: List<PlanItem> = emptyList(),
    logicalDate: java.time.LocalDate = java.time.LocalDate.of(1970, 1, 1),
    projectionTime: Instant = Instant.EPOCH,
): List<StartOverviewKernelInput> {
    val snapshotMap = snapshots.associateBy(AreaSnapshot::areaId)
    val plansByAreaId = todayPlans
        .filterNot(PlanItem::isDone)
        .groupBy(PlanItem::areaId)
    return instances.map { instance ->
        val definitionLookupId = instance.definitionId
        val areaPlans = plansByAreaId[instance.areaId].orEmpty()
        StartOverviewKernelInput(
            definition = startAreaDefinition(definitionLookupId),
            blueprint = startAreaKernelBlueprint(definitionLookupId),
            instance = instance,
            snapshot = snapshotMap[instance.areaId],
            openPlanTitles = areaPlans.map(PlanItem::title),
            dueCount = areaPlans.size,
            logicalDate = logicalDate,
            projectionTime = projectionTime,
        )
    }
}

fun projectStartOverviewState(
    inputs: List<StartOverviewKernelInput>,
): StartOverviewState {
    val orderedAreas = inputs.sortedBy { it.instance.sortOrder }
    return StartOverviewState(
        areas = orderedAreas
            .mapIndexed { index, input ->
                val instance = input.instance
                val tracks = input.blueprint?.let(::startAreaTrackLabels)
                    ?: startAreaBlueprint(instance).tracks
                val selectedTracks = instance.selectedTracks.ifEmpty { tracks.take(2).toSet() }
                val focusTrack = startAreaFocusTrack(
                    tracks = tracks,
                    selectedTracks = selectedTracks,
                )
                val profileState = startAreaProfileState(
                    profileConfig = instance.profileConfig,
                    focusTrack = focusTrack,
                )
                val statusPresentation = resolveStartStatusPresentation(
                    definition = input.definition,
                    blueprint = input.blueprint,
                    targetScore = instance.targetScore,
                    snapshot = input.snapshot,
                    areaTitle = instance.title,
                    cadence = instance.cadenceKey,
                )
                val todayOutput = buildStartAreaTodayOutput(
                    definition = input.definition,
                    blueprint = input.blueprint,
                    instance = instance,
                    snapshot = input.snapshot,
                    logicalDate = input.logicalDate,
                    generatedAt = input.projectionTime,
                    openPlanTitles = input.openPlanTitles,
                    dueCount = input.dueCount,
                )
                val family = resolveStartAreaFamily(
                    instance = instance,
                    blueprint = input.blueprint,
                    todayOutput = todayOutput,
                )
                val hints = buildStartAreaHints(
                    instance = instance,
                    todayOutput = todayOutput,
                )
                StartAreaOverviewTile(
                    areaId = instance.areaId,
                    label = instance.title,
                    summary = instance.summary,
                    family = family,
                    todayLabel = todayOutput.statusLabel,
                    todayStepLabel = todayOutput.nextMeaningfulStep.label,
                    templateId = instance.templateId ?: input.blueprint?.defaultTemplateId ?: "free",
                    iconKey = instance.iconKey.ifBlank {
                        input.definition?.iconKey ?: input.blueprint?.defaultIconKey ?: "spark"
                    },
                    statusKind = statusPresentation.kind,
                    statusLabel = statusPresentation.overviewLabel,
                    primaryHint = hints.first(),
                    focusLabel = focusTrack,
                    profileLabel = profileState.overviewLabel,
                    progress = statusPresentation.overviewProgress,
                    canMoveEarlier = index > 0,
                    canMoveLater = index < orderedAreas.lastIndex,
                    todayOutput = todayOutput,
                )
            },
    )
}

fun mapStartAreaDetailState(
    area: LifeArea,
    manualScore: Int?,
    profile: LifeAreaProfile?,
): StartAreaDetailState {
    return projectStartAreaDetailState(
        input = buildStartAreaDetailKernelInput(
            area = area,
            dailyCheck = manualScore?.let {
                LifeAreaDailyCheck(
                    areaId = area.id,
                    date = "1970-01-01",
                    manualScore = it,
                )
            },
            profile = profile,
        ),
    )
}

fun buildStartAreaDetailKernelInput(
    area: LifeArea,
    dailyCheck: LifeAreaDailyCheck?,
    profile: LifeAreaProfile?,
): StartAreaDetailKernelInput {
    val legacyBlueprint = startAreaBlueprint(area)
    val definition = startAreaDefinition(area.id) ?: legacyBlueprint.toAreaDefinition()
    val blueprint = startAreaKernelBlueprint(area.id) ?: legacyBlueprint.toAreaBlueprint()
    val instance = area.toAreaInstance(
        definition = definition,
        profile = profile,
    )
    return StartAreaDetailKernelInput(
        definition = definition,
        blueprint = blueprint,
        instance = instance,
        snapshot = dailyCheck?.toAreaSnapshot(),
        legacyBlueprint = legacyBlueprint,
    )
}

fun buildStartAreaDetailKernelInput(
    instance: AreaInstance,
    snapshot: AreaSnapshot?,
    todayPlans: List<PlanItem> = emptyList(),
    logicalDate: java.time.LocalDate = snapshot?.date ?: java.time.LocalDate.of(1970, 1, 1),
    projectionTime: Instant = Instant.EPOCH,
): StartAreaDetailKernelInput {
    val legacyBlueprint = startAreaBlueprint(instance)
    val definitionLookupId = instance.definitionId
    val definition = startAreaDefinition(definitionLookupId)
        ?: fallbackStartAreaDefinition(
            instance = instance,
            legacyBlueprint = legacyBlueprint,
        )
    val blueprint = startAreaKernelBlueprint(definitionLookupId)
        ?: fallbackStartAreaBlueprint(
            instance = instance,
            legacyBlueprint = legacyBlueprint,
        )
    return StartAreaDetailKernelInput(
        definition = definition,
        blueprint = blueprint,
        instance = instance,
        snapshot = snapshot,
        legacyBlueprint = legacyBlueprint,
        openPlanTitles = todayPlans
            .filter { it.areaId == instance.areaId && !it.isDone }
            .map(PlanItem::title),
        dueCount = todayPlans.count { it.areaId == instance.areaId && !it.isDone },
        logicalDate = logicalDate,
        projectionTime = projectionTime,
    )
}

private fun fallbackStartAreaDefinition(
    instance: AreaInstance,
    legacyBlueprint: StartAreaBlueprint,
): AreaDefinition {
    val fallback = legacyBlueprint.toAreaDefinition()
    return fallback.copy(
        id = instance.definitionId,
        title = instance.title,
        shortTitle = instance.title,
        iconKey = instance.iconKey,
        complexityLevel = instance.authoringConfig.complexityLevel,
        defaultConfig = fallback.defaultConfig.copy(
            targetScore = instance.targetScore,
        ),
    )
}

private fun fallbackStartAreaBlueprint(
    instance: AreaInstance,
    legacyBlueprint: StartAreaBlueprint,
): AreaBlueprint {
    val fallback = legacyBlueprint.toAreaBlueprint()
    return fallback.copy(
        areaId = instance.definitionId,
        summary = instance.summary.ifBlank { fallback.summary },
        defaultTemplateId = instance.templateId ?: fallback.defaultTemplateId,
        defaultIconKey = instance.iconKey,
    )
}

fun projectStartAreaDetailState(
    input: StartAreaDetailKernelInput,
): StartAreaDetailState {
    val instance = input.instance
    val effectiveDefinition = resolveStartProjectionDefinition(
        definition = input.definition,
        profileConfig = instance.profileConfig,
    )
    val tracks = startAreaTrackLabels(input.blueprint)
    val selectedTracks = instance.selectedTracks.ifEmpty { tracks.take(2).toSet() }
    val focusTrack = startAreaFocusTrack(
        tracks = tracks,
        selectedTracks = selectedTracks,
    )
    val flowCount = listOf(
        instance.remindersEnabled,
        instance.reviewEnabled,
        instance.experimentsEnabled,
    ).count { it }
    val statusPresentation = resolveStartStatusPresentation(
        definition = effectiveDefinition,
        blueprint = input.blueprint,
        targetScore = instance.targetScore,
        snapshot = input.snapshot,
        areaTitle = instance.title,
        cadence = instance.cadenceKey,
    )
    val profileState = startAreaProfileState(
        profileConfig = instance.profileConfig,
        focusTrack = focusTrack,
    )
    val todayOutput = buildStartAreaTodayOutput(
        definition = effectiveDefinition,
        blueprint = input.blueprint,
        instance = instance,
        snapshot = input.snapshot,
        logicalDate = input.logicalDate,
        generatedAt = input.projectionTime,
        openPlanTitles = input.openPlanTitles,
        dueCount = input.dueCount,
    )
    val family = resolveStartAreaFamily(
        instance = instance,
        blueprint = input.blueprint,
        todayOutput = todayOutput,
    )
    val hints = buildStartAreaHints(
        instance = instance,
        todayOutput = todayOutput,
    )
    return StartAreaDetailState(
        areaId = instance.areaId,
        title = instance.title,
        summary = instance.summary,
        family = family,
        templateId = instance.templateId ?: input.blueprint.defaultTemplateId,
        iconKey = instance.iconKey.ifBlank { input.definition.iconKey },
        tracks = tracks,
        targetScore = instance.targetScore,
        manualScore = input.snapshot?.manualScore,
        manualStateKey = input.snapshot?.manualStateKey,
        manualNote = input.snapshot?.manualNote,
        cadence = instance.cadenceKey,
        intensity = instance.intensity,
        signalBlend = instance.signalBlend,
        selectedTracks = selectedTracks,
        remindersEnabled = instance.remindersEnabled,
        reviewEnabled = instance.reviewEnabled,
        experimentsEnabled = instance.experimentsEnabled,
        profileState = profileState,
        blueprint = input.legacyBlueprint,
        focusTrack = focusTrack,
        flowCount = flowCount,
        statusKind = statusPresentation.kind,
        statusLabel = todayOutput.statusLabel,
        hints = hints,
        todayLabel = todayOutput.headline,
        todayRecommendation = todayOutput.recommendation,
        todayStepLabel = todayOutput.nextMeaningfulStep.label,
        progress = statusPresentation.detailProgress,
        panelStates = buildStartAreaPanelStates(
            family = family,
            definition = effectiveDefinition,
            legacyBlueprint = input.legacyBlueprint,
            kernelBlueprint = input.blueprint,
            areaTitle = instance.title,
            targetScore = instance.targetScore,
            snapshot = input.snapshot,
            selectedTracks = selectedTracks,
            focusTrack = focusTrack,
            cadence = instance.cadenceKey,
            intensity = instance.intensity,
            signalBlend = instance.signalBlend,
            remindersEnabled = instance.remindersEnabled,
            reviewEnabled = instance.reviewEnabled,
            experimentsEnabled = instance.experimentsEnabled,
            profileState = profileState,
            statusPresentation = statusPresentation,
        ),
        todayOutput = todayOutput,
    )
}

private fun resolveStartProjectionDefinition(
    definition: AreaDefinition,
    profileConfig: AreaProfileConfig,
): AreaDefinition {
    return definition.copy(
        lageType = when (profileConfig.lageMode) {
            AreaLageMode.Score -> AreaLageType.SCORE
            AreaLageMode.State -> AreaLageType.STATE
        },
    )
}

private fun resolveStartAreaFamily(
    instance: AreaInstance,
    blueprint: AreaBlueprint?,
    todayOutput: AreaTodayOutput,
): StartAreaFamily {
    val summaryText = buildString {
        append(instance.title)
        append(' ')
        append(instance.summary)
        blueprint?.summary?.let {
            append(' ')
            append(it)
        }
    }.lowercase()
    val tokens = summaryText
        .split(Regex("""[^\p{L}\p{N}]+"""))
        .filter(String::isNotBlank)

    fun containsAnyWordPrefix(vararg keywords: String): Boolean {
        return keywords.any { keyword ->
            tokens.any { token -> token.startsWith(keyword) }
        }
    }

    fun containsAnyPhrase(vararg phrases: String): Boolean {
        return phrases.any(summaryText::contains)
    }

    return when {
        containsAnyWordPrefix("inbox", "idee", "sammel", "sammlung") -> StartAreaFamily.Sammlung
        instance.templateId == "person" ||
            containsAnyWordPrefix("kontakt", "famil", "freund", "community", "bezieh", "partner") ||
            containsAnyPhrase("nachricht von") -> {
            StartAreaFamily.Kontakt
        }
        containsAnyWordPrefix("schlaf", "sleep", "gesund", "health", "beweg", "energie", "koerper", "erhol") -> {
            StartAreaFamily.Gesundheit
        }
        containsAnyWordPrefix("zuhause", "home", "ort", "orte", "weg", "wege", "standort") ||
            containsAnyPhrase("wenn ich zuhause") -> {
            StartAreaFamily.Ort
        }
        instance.templateId == "project" ||
            containsAnyWordPrefix("kalender", "termin", "besprech", "admin", "frist", "finanz", "arbeit", "fokus") -> {
            StartAreaFamily.Pflicht
        }
        containsAnyWordPrefix("screenshot", "foto", "website", "podcast", "feed", "nachrichten", "lesen") -> {
            StartAreaFamily.Radar
        }
        instance.templateId == "place" -> StartAreaFamily.Ort
        instance.templateId == "ritual" -> StartAreaFamily.Routine
        instance.templateId == "medium" || instance.templateId == "theme" -> StartAreaFamily.Radar
        todayOutput.behaviorClass == com.struperto.androidappdays.domain.area.AreaBehaviorClass.RELATIONSHIP -> {
            StartAreaFamily.Kontakt
        }
        todayOutput.behaviorClass == com.struperto.androidappdays.domain.area.AreaBehaviorClass.PROGRESS ||
            todayOutput.behaviorClass == com.struperto.androidappdays.domain.area.AreaBehaviorClass.PROTECTION -> {
            StartAreaFamily.Pflicht
        }
        todayOutput.behaviorClass == com.struperto.androidappdays.domain.area.AreaBehaviorClass.MAINTENANCE -> {
            StartAreaFamily.Routine
        }
        todayOutput.behaviorClass == com.struperto.androidappdays.domain.area.AreaBehaviorClass.TRACKING -> {
            StartAreaFamily.Radar
        }
        else -> StartAreaFamily.Radar
    }
}

private fun buildStartAreaHints(
    instance: AreaInstance,
    todayOutput: AreaTodayOutput,
): List<StartAreaHintState> {
    val hints = buildList {
        val templateSummary = startAreaTemplate(instance.templateId ?: "free").summary
        val summaryLooksGeneric = instance.summary.length < 52 ||
            instance.summary.equals(templateSummary, ignoreCase = true)

        when (todayOutput.sourceTruth) {
            AreaSourceTruth.missing -> add(
                StartAreaHintState(
                    id = "source-missing",
                    title = "Lokale Quelle fehlt",
                    detail = "Dieser Bereich hat noch keine verlaessliche lokale Spur. Richte zuerst Eingaben oder eine klare Quelle ein.",
                    compactLabel = "Quelle fehlt",
                    tone = StartAreaHintTone.Notice,
                ),
            )

            AreaSourceTruth.manual,
            AreaSourceTruth.manual_plus_local,
            AreaSourceTruth.local_derived -> Unit
        }

        if (todayOutput.isEmptyState) {
            add(
                StartAreaHintState(
                    id = "empty-state",
                    title = "Heute ist noch wenig sichtbar",
                    detail = "Der Bereich ist angelegt, aber fuer heute liegt noch keine brauchbare Spur oder klare Einordnung vor.",
                    compactLabel = "Heute noch leer",
                    tone = StartAreaHintTone.Notice,
                ),
            )
        }

        if (todayOutput.freshnessBand == AreaFreshnessBand.STALE) {
            add(
                StartAreaHintState(
                    id = "stale-signal",
                    title = "Signal ist nicht mehr frisch",
                    detail = "Die letzte brauchbare Spur ist schon aelter. Pruefe, ob dieser Bereich haeufiger gelesen oder aktualisiert werden soll.",
                    compactLabel = "Spur ist alt",
                    tone = StartAreaHintTone.Notice,
                ),
            )
        }

        if (summaryLooksGeneric && instance.templateId in setOf("free", "theme", "medium")) {
            add(
                StartAreaHintState(
                    id = "meaning-open",
                    title = "Zweck noch schaerfen",
                    detail = "Formuliere den Bereich klarer. Dann werden Vorschlaege, Hinweise und spaetere Widgets deutlich treffsicherer.",
                    compactLabel = "Zweck schaerfen",
                    tone = StartAreaHintTone.Notice,
                ),
            )
        }

        if (todayOutput.severity == AreaSeverity.HIGH || todayOutput.severity == AreaSeverity.CRITICAL) {
            add(
                StartAreaHintState(
                    id = "high-severity",
                    title = "Braucht heute Aufmerksamkeit",
                    detail = "Die heutige Lage ist nicht nur sichtbar, sondern merklich angespannt. Halte die Anzeige kompakt, aber den Bereich in Reichweite.",
                    compactLabel = "Heute zieht",
                    tone = StartAreaHintTone.Warning,
                ),
            )
        }
    }

    if (hints.isEmpty()) {
        return listOf(
            StartAreaHintState(
                id = "quiet",
                title = "Laeuft ruhig",
                detail = "Der Bereich ist aktuell ruhig lesbar. Auf Start reicht ein kurzes Signal ohne weitere Eskalation.",
                compactLabel = "Laeuft ruhig",
                tone = StartAreaHintTone.Quiet,
            ),
        )
    }

    return hints.sortedByDescending(StartAreaHintState::tonePriority)
}

fun StartAreaHintState.tonePriority(): Int {
    return when (tone) {
        StartAreaHintTone.Warning -> 3
        StartAreaHintTone.Notice -> 2
        StartAreaHintTone.Quiet -> 1
    }
}

private fun buildStartAreaTodayOutput(
    definition: AreaDefinition?,
    blueprint: AreaBlueprint?,
    instance: AreaInstance,
    snapshot: AreaSnapshot?,
    logicalDate: java.time.LocalDate,
    generatedAt: Instant,
    openPlanTitles: List<String> = emptyList(),
    dueCount: Int = 0,
): AreaTodayOutput {
    return projectAreaTodayOutput(
        AreaTodayOutputInput(
            definition = definition,
            blueprint = blueprint,
            instance = instance,
            snapshot = snapshot,
            generatedAt = generatedAt,
            logicalDate = logicalDate,
            openPlanTitles = openPlanTitles,
            dueCount = dueCount,
        ),
    )
}

fun defaultStartAreaProfile(
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
        lageMode = AreaLageMode.Score.persistedValue,
        directionMode = AreaDirectionMode.Balanced.persistedValue,
        sourcesMode = AreaSourcesMode.Balanced.persistedValue,
        flowProfile = AreaFlowProfile.Stable.persistedValue,
    )
}

fun startAreaStatusKind(
    targetScore: Int,
    actualScore: Int?,
): StartAreaStatusKind {
    return when {
        actualScore == null -> StartAreaStatusKind.Waiting
        actualScore >= targetScore -> StartAreaStatusKind.Stable
        else -> StartAreaStatusKind.Pull
    }
}

fun startAreaOverviewStatusLabel(
    targetScore: Int,
    actualScore: Int?,
): String {
    return actualScore?.let { "$it/$targetScore" } ?: "Ziel $targetScore/5"
}

fun startAreaSnapshotLabel(
    actualScore: Int?,
): String {
    return actualScore?.let { "Lage $it/5" } ?: "Lage offen"
}

fun startAreaOverviewProgress(
    targetScore: Int,
    actualScore: Int?,
): Float {
    return actualScore?.let { score ->
        (score.toFloat() / targetScore.coerceAtLeast(1).toFloat()).coerceIn(0.18f, 1f)
    } ?: 0.18f
}

fun startAreaDetailProgress(
    targetScore: Int,
    actualScore: Int?,
): Float {
    return actualScore?.let { score ->
        (score.toFloat() / targetScore.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    } ?: 0.08f
}

private fun buildStartAreaPanelStates(
    family: StartAreaFamily,
    definition: AreaDefinition,
    legacyBlueprint: StartAreaBlueprint,
    kernelBlueprint: AreaBlueprint,
    areaTitle: String,
    targetScore: Int,
    snapshot: AreaSnapshot?,
    selectedTracks: Set<String>,
    focusTrack: String,
    cadence: String,
    intensity: Int,
    signalBlend: Int,
    remindersEnabled: Boolean,
    reviewEnabled: Boolean,
    experimentsEnabled: Boolean,
    profileState: StartAreaProfileState,
    statusPresentation: StartResolvedStatusPresentation,
): List<StartAreaPanelState> {
    val trackLabels = startAreaTrackLabels(kernelBlueprint)
    val direction = startAreaDirectionSemantics(kernelBlueprint)
    val flowCount = listOf(
        remindersEnabled,
        reviewEnabled,
        experimentsEnabled,
    ).count { it }
    return listOf(
        StartAreaPanelState(
            panel = StartAreaPanel.Snapshot,
            title = startAreaPanelCopy(family, StartAreaPanel.Snapshot).title,
            summary = startAreaSnapshotSummary(
                summary = statusPresentation.panelSummary,
                lageMode = profileState.lageMode,
                lageLabel = profileState.lageLabel,
            ),
            countLabel = profileState.lageLabel,
            icon = legacyBlueprint.entries.first { it.panel == StartAreaPanel.Snapshot }.icon,
            screenState = buildStartPanelScreenState(
                panel = StartAreaPanel.Snapshot,
                definition = definition,
                legacyBlueprint = legacyBlueprint,
                kernelBlueprint = kernelBlueprint,
                trackLabels = trackLabels,
                areaTitle = areaTitle,
                targetScore = targetScore,
                snapshot = snapshot,
                selectedTracks = selectedTracks,
                focusTrack = focusTrack,
                cadence = cadence,
                intensity = intensity,
                signalBlend = signalBlend,
                remindersEnabled = remindersEnabled,
                reviewEnabled = reviewEnabled,
                experimentsEnabled = experimentsEnabled,
                profileState = profileState,
                statusPresentation = statusPresentation,
            ),
        ),
        StartAreaPanelState(
            panel = StartAreaPanel.Path,
            title = startAreaPanelCopy(family, StartAreaPanel.Path).title,
            summary = startAreaNextMoveLabel(
                focusTrack = focusTrack,
                cadence = cadence,
                direction = direction,
                directionMode = profileState.directionMode,
            ),
            countLabel = profileState.directionLabel,
            icon = legacyBlueprint.entries.first { it.panel == StartAreaPanel.Path }.icon,
            screenState = buildStartPanelScreenState(
                panel = StartAreaPanel.Path,
                definition = definition,
                legacyBlueprint = legacyBlueprint,
                kernelBlueprint = kernelBlueprint,
                trackLabels = trackLabels,
                areaTitle = areaTitle,
                targetScore = targetScore,
                snapshot = snapshot,
                selectedTracks = selectedTracks,
                focusTrack = focusTrack,
                cadence = cadence,
                intensity = intensity,
                signalBlend = signalBlend,
                remindersEnabled = remindersEnabled,
                reviewEnabled = reviewEnabled,
                experimentsEnabled = experimentsEnabled,
                profileState = profileState,
                statusPresentation = statusPresentation,
            ),
        ),
        StartAreaPanelState(
            panel = StartAreaPanel.Sources,
            title = startAreaPanelCopy(family, StartAreaPanel.Sources).title,
            summary = startAreaSourcesSummary(
                selectedTracks = selectedTracks,
                trackLabels = trackLabels,
                sourcesMode = profileState.sourcesMode,
                signalBlend = signalBlend,
            ),
            countLabel = profileState.sourcesLabel,
            icon = startAreaSourcesIcon(),
            screenState = buildStartPanelScreenState(
                panel = StartAreaPanel.Sources,
                definition = definition,
                legacyBlueprint = legacyBlueprint,
                kernelBlueprint = kernelBlueprint,
                trackLabels = trackLabels,
                areaTitle = areaTitle,
                targetScore = targetScore,
                snapshot = snapshot,
                selectedTracks = selectedTracks,
                focusTrack = focusTrack,
                cadence = cadence,
                intensity = intensity,
                signalBlend = signalBlend,
                remindersEnabled = remindersEnabled,
                reviewEnabled = reviewEnabled,
                experimentsEnabled = experimentsEnabled,
                profileState = profileState,
                statusPresentation = statusPresentation,
            ),
        ),
        StartAreaPanelState(
            panel = StartAreaPanel.Options,
            title = startAreaPanelCopy(family, StartAreaPanel.Options).title,
            summary = startAreaFlowSummary(
                blueprint = kernelBlueprint,
                remindersEnabled = remindersEnabled,
                reviewEnabled = reviewEnabled,
                experimentsEnabled = experimentsEnabled,
                flowProfile = profileState.flowProfile,
            ),
            countLabel = profileState.flowLabel,
            icon = legacyBlueprint.entries.firstOrNull { it.panel == StartAreaPanel.Options }?.icon
                ?: legacyBlueprint.entries.last().icon,
            screenState = buildStartPanelScreenState(
                panel = StartAreaPanel.Options,
                definition = definition,
                legacyBlueprint = legacyBlueprint,
                kernelBlueprint = kernelBlueprint,
                trackLabels = trackLabels,
                areaTitle = areaTitle,
                targetScore = targetScore,
                snapshot = snapshot,
                selectedTracks = selectedTracks,
                focusTrack = focusTrack,
                cadence = cadence,
                intensity = intensity,
                signalBlend = signalBlend,
                remindersEnabled = remindersEnabled,
                reviewEnabled = reviewEnabled,
                experimentsEnabled = experimentsEnabled,
                profileState = profileState,
                statusPresentation = statusPresentation,
            ),
        ),
    )
}

private fun buildStartPanelScreenState(
    panel: StartAreaPanel,
    definition: AreaDefinition,
    legacyBlueprint: StartAreaBlueprint,
    kernelBlueprint: AreaBlueprint,
    trackLabels: List<String>,
    areaTitle: String,
    targetScore: Int,
    snapshot: AreaSnapshot?,
    selectedTracks: Set<String>,
    focusTrack: String,
    cadence: String,
    intensity: Int,
    signalBlend: Int,
    remindersEnabled: Boolean,
    reviewEnabled: Boolean,
    experimentsEnabled: Boolean,
    profileState: StartAreaProfileState,
    statusPresentation: StartResolvedStatusPresentation,
): StartPanelScreenState {
    val flowCount = listOf(
        remindersEnabled,
        reviewEnabled,
        experimentsEnabled,
    ).count { it }
    return when (panel) {
        StartAreaPanel.Snapshot -> StartPanelScreenState(
            headerLabel = profileState.lageLabel,
            infoLabel = statusPresentation.infoLabel,
            core = StartPanelCoreState(
                title = statusPresentation.coreTitle,
                value = statusPresentation.coreValue,
                caption = statusPresentation.coreCaption,
                progress = statusPresentation.detailProgress.coerceIn(0.08f, 1f),
            ),
            metrics = listOf(
                statusPresentation.primaryMetric,
                StartPanelMetricState("Modus", profileState.lageLabel),
                StartPanelMetricState("Takt", cadenceLabel(cadence)),
            ),
            actions = listOf(
                StartPanelActionState(
                    id = StartPanelActionId.SnapshotMode,
                    label = "Lage-Modus",
                    valueLabel = profileState.lageLabel,
                    mode = StartPanelActionMode.Sheet,
                    sheet = StartPanelSheetState(
                        title = "Lage-Modus",
                        selectionMode = StartPanelSelectionMode.Single,
                        options = areaLageModeOptions(profileState.lageMode),
                    ),
                ),
                StartPanelActionState(
                    id = statusPresentation.selectionActionId,
                    label = statusPresentation.selectionActionLabel,
                    valueLabel = statusPresentation.selectionValueLabel,
                    mode = StartPanelActionMode.Sheet,
                    sheet = StartPanelSheetState(
                        title = statusPresentation.selectionSheetTitle,
                        selectionMode = StartPanelSelectionMode.Single,
                        options = statusPresentation.selectionOptions,
                    ),
                ),
                StartPanelActionState(
                    id = StartPanelActionId.SnapshotClear,
                    label = "Reset",
                    valueLabel = statusPresentation.resetValueLabel,
                    mode = StartPanelActionMode.Direct,
                ),
            ),
            effectLabel = startAreaSnapshotEffectLabel(
                lageMode = profileState.lageMode,
                hasValue = snapshot != null && (
                    snapshot.manualScore != null || snapshot.manualStateKey != null
                ),
            ),
        )

        StartAreaPanel.Path -> {
            val direction = startAreaDirectionSemantics(kernelBlueprint)
            val nextMove = startAreaNextMoveLabel(
                focusTrack = focusTrack,
                cadence = cadence,
                direction = direction,
                directionMode = profileState.directionMode,
            )
            val sourceChannels = startAreaSourceChannels(
                blueprint = kernelBlueprint,
                trackLabels = trackLabels,
            ).associateBy(AreaSourceChannel::label)
            StartPanelScreenState(
                headerLabel = profileState.directionLabel,
                infoLabel = cadenceLabel(cadence),
                core = StartPanelCoreState(
                    title = direction.nextMoveLabel,
                    value = focusTrack,
                    caption = nextMove,
                    progress = (targetScore.toFloat() / 5f).coerceIn(0.2f, 1f),
                ),
                metrics = listOf(
                    StartPanelMetricState(direction.focusActionLabel, focusTrack),
                    StartPanelMetricState("Modus", profileState.directionLabel),
                    StartPanelMetricState(direction.nextMoveLabel, nextMove),
                ),
                actions = listOf(
                    StartPanelActionState(
                        id = StartPanelActionId.PathMode,
                        label = "Richtungsmodus",
                        valueLabel = profileState.directionLabel,
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = "Richtungsmodus",
                            selectionMode = StartPanelSelectionMode.Single,
                            options = areaDirectionModeOptions(profileState.directionMode),
                        ),
                    ),
                    StartPanelActionState(
                        id = StartPanelActionId.PathTarget,
                        label = "Soll",
                        valueLabel = "$targetScore/5",
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = "Soll",
                            selectionMode = StartPanelSelectionMode.Single,
                            options = (1..5).map { score ->
                                StartPanelOptionState(
                                    id = score.toString(),
                                    label = "$score/5",
                                    selected = targetScore == score,
                                    supportingLabel = targetScoreSupportingLabel(score),
                                )
                            },
                        ),
                    ),
                    StartPanelActionState(
                        id = StartPanelActionId.PathCadence,
                        label = "Takt",
                        valueLabel = cadenceLabel(cadence),
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = "Takt",
                            selectionMode = StartPanelSelectionMode.Single,
                            options = listOf(
                                StartPanelOptionState(
                                    id = "daily",
                                    label = "Taeglich",
                                    selected = cadence == "daily",
                                    supportingLabel = "zieht den Bereich taeglich in den Blick",
                                ),
                                StartPanelOptionState(
                                    id = "weekly",
                                    label = "Woechentlich",
                                    selected = cadence == "weekly",
                                    supportingLabel = "liest den Bereich ueber Wochenzug statt Tagesdruck",
                                ),
                                StartPanelOptionState(
                                    id = "adaptive",
                                    label = "Adaptiv",
                                    selected = cadence == "adaptive",
                                    supportingLabel = "passt Takt und Rueckmeldung an die aktuelle Lage an",
                                ),
                            ),
                        ),
                    ),
                    StartPanelActionState(
                        id = StartPanelActionId.PathFocus,
                        label = direction.focusActionLabel,
                        valueLabel = focusTrack,
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = direction.focusActionLabel,
                            selectionMode = StartPanelSelectionMode.Single,
                            options = trackLabels.map { track ->
                                StartPanelOptionState(
                                    id = track,
                                    label = track,
                                    selected = focusTrack == track,
                                    supportingLabel = sourceChannels[track]?.summary.orEmpty(),
                                )
                            },
                        ),
                    ),
                ),
                effectLabel = startAreaDirectionEffectLabel(
                    focusTrack = focusTrack,
                    cadence = cadence,
                    directionMode = profileState.directionMode,
                ),
            )
        }

        StartAreaPanel.Sources -> {
            val sourceSemantics = kernelBlueprint.pilotSemantics?.sources
            val sourceChannels = startAreaSourceChannels(
                blueprint = kernelBlueprint,
                trackLabels = trackLabels,
            )
            val mixLabel = sourceSemantics?.mixLabel ?: "Mix"
            val sourcesEffectLabel = startAreaSourcesEffectLabel(
                selectedTracks = selectedTracks,
                trackLabels = trackLabels,
                sourcesMode = profileState.sourcesMode,
                signalBlend = signalBlend,
            )
            StartPanelScreenState(
                headerLabel = profileState.sourcesLabel,
                infoLabel = "$mixLabel $signalBlend%",
                core = StartPanelCoreState(
                    title = sourceSemantics?.coreTitle ?: "Aktive Quellen",
                    value = when (selectedTracks.size) {
                        1 -> "1 aktiv"
                        else -> "${selectedTracks.size} aktiv"
                    },
                    caption = startAreaSourcesSummary(
                        selectedTracks = selectedTracks,
                        trackLabels = trackLabels,
                        sourcesMode = profileState.sourcesMode,
                        signalBlend = signalBlend,
                    ),
                    progress = (selectedTracks.size.toFloat() / trackLabels.size.coerceAtLeast(1).toFloat()).coerceIn(0.1f, 1f),
                ),
                metrics = listOf(
                    StartPanelMetricState(startAreaDirectionSemantics(kernelBlueprint).focusActionLabel, focusTrack),
                    StartPanelMetricState("Modus", profileState.sourcesLabel),
                    StartPanelMetricState(mixLabel, "$signalBlend%"),
                ),
                actions = listOf(
                    StartPanelActionState(
                        id = StartPanelActionId.SourcesMode,
                        label = "Quellenmodus",
                        valueLabel = profileState.sourcesLabel,
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = "Quellenmodus",
                            selectionMode = StartPanelSelectionMode.Single,
                            options = areaSourcesModeOptions(profileState.sourcesMode),
                        ),
                    ),
                    StartPanelActionState(
                        id = StartPanelActionId.SourcesTracks,
                        label = if (sourceSemantics != null) "Quellen" else "Spuren",
                        valueLabel = "${selectedTracks.size}",
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = sourceSemantics?.coreTitle ?: "Spuren",
                            selectionMode = StartPanelSelectionMode.Multiple,
                            options = sourceChannels.map { channel ->
                                StartPanelOptionState(
                                    id = channel.label,
                                    label = channel.label,
                                    selected = channel.label in selectedTracks,
                                    supportingLabel = channel.summary,
                                )
                            },
                        ),
                    ),
                    StartPanelActionState(
                        id = StartPanelActionId.SourcesBlend,
                        label = mixLabel,
                        valueLabel = "$signalBlend%",
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = mixLabel,
                            selectionMode = StartPanelSelectionMode.Single,
                            options = listOf(0, 25, 50, 75, 100).map { value ->
                                StartPanelOptionState(
                                    id = value.toString(),
                                    label = "$value%",
                                    selected = signalBlend == value,
                                    supportingLabel = sourcesBlendSupportingLabel(value),
                                )
                            },
                        ),
                    ),
                ),
                effectLabel = sourcesEffectLabel,
            )
        }

        StartAreaPanel.Options -> {
            val flowSemantics = startAreaFlowSemantics(kernelBlueprint)
            val intensityLabel = flowSemantics?.intensityLabel ?: "Intensitaet"
            val flowSummary = startAreaFlowSummary(
                blueprint = kernelBlueprint,
                remindersEnabled = remindersEnabled,
                reviewEnabled = reviewEnabled,
                experimentsEnabled = experimentsEnabled,
                flowProfile = profileState.flowProfile,
            )
            val flowEffectLabel = startAreaFlowEffectLabel(
                blueprint = kernelBlueprint,
                intensity = intensity,
                remindersEnabled = remindersEnabled,
                reviewEnabled = reviewEnabled,
                experimentsEnabled = experimentsEnabled,
                flowProfile = profileState.flowProfile,
            )
            StartPanelScreenState(
                headerLabel = profileState.flowLabel,
                infoLabel = "$intensityLabel $intensity/5",
                core = StartPanelCoreState(
                    title = flowSemantics?.coreTitle ?: "Lokaler Flow",
                    value = startAreaFlowPrimaryLabel(
                        blueprint = kernelBlueprint,
                        remindersEnabled = remindersEnabled,
                        reviewEnabled = reviewEnabled,
                        experimentsEnabled = experimentsEnabled,
                        flowProfile = profileState.flowProfile,
                    ),
                    caption = flowSummary,
                    progress = (flowCount.toFloat() / 3f).coerceIn(0.12f, 1f),
                ),
                metrics = listOf(
                    StartPanelMetricState("Aktiv", flowSummary),
                    StartPanelMetricState("Profil", profileState.flowLabel),
                    StartPanelMetricState(intensityLabel, "$intensity/5"),
                ),
                actions = listOf(
                    StartPanelActionState(
                        id = StartPanelActionId.FlowProfile,
                        label = "Flowprofil",
                        valueLabel = profileState.flowLabel,
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = "Flowprofil",
                            selectionMode = StartPanelSelectionMode.Single,
                            options = areaFlowProfileOptions(profileState.flowProfile),
                        ),
                    ),
                    StartPanelActionState(
                        id = StartPanelActionId.FlowIntensity,
                        label = intensityLabel,
                        valueLabel = "$intensity/5",
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = intensityLabel,
                            selectionMode = StartPanelSelectionMode.Single,
                            options = (1..5).map { value ->
                                StartPanelOptionState(
                                    id = value.toString(),
                                    label = "$value/5",
                                    selected = intensity == value,
                                    supportingLabel = flowIntensitySupportingLabel(value),
                                )
                            },
                        ),
                    ),
                    StartPanelActionState(
                        id = StartPanelActionId.FlowSwitches,
                        label = startAreaFlowActionLabel(kernelBlueprint),
                        valueLabel = startAreaFlowPrimaryLabel(
                            blueprint = kernelBlueprint,
                            remindersEnabled = remindersEnabled,
                            reviewEnabled = reviewEnabled,
                            experimentsEnabled = experimentsEnabled,
                            flowProfile = profileState.flowProfile,
                        ),
                        mode = StartPanelActionMode.Sheet,
                        sheet = StartPanelSheetState(
                            title = flowSemantics?.coreTitle ?: "Schalter",
                            selectionMode = StartPanelSelectionMode.Multiple,
                            options = startAreaFlowToggles(kernelBlueprint).map { toggle ->
                                StartPanelOptionState(
                                    id = toggle.id,
                                    label = toggle.label,
                                    selected = when (toggle.id) {
                                        "reminders" -> remindersEnabled
                                        "review" -> reviewEnabled
                                        "experiments" -> experimentsEnabled
                                        else -> false
                                    },
                                    supportingLabel = toggle.summary,
                                )
                            },
                        ),
                    ),
                ),
                effectLabel = flowEffectLabel,
            )
        }
    }
}

fun startAreaFocusTrack(
    blueprint: StartAreaBlueprint,
    selectedTracks: Set<String>,
): String {
    return startAreaFocusTrack(
        tracks = blueprint.tracks,
        selectedTracks = selectedTracks,
    )
}

fun startAreaFocusTrack(
    tracks: List<String>,
    selectedTracks: Set<String>,
): String {
    return selectedTracks.firstOrNull { it in tracks }
        ?: tracks.firstOrNull()
        ?: "Spur"
}

fun cadenceLabel(
    cadence: String,
): String {
    return when (cadence) {
        "daily" -> "Taeglich"
        "weekly" -> "Woechentlich"
        else -> "Adaptiv"
    }
}

private fun startAreaNextMoveLabel(
    focusTrack: String,
    cadence: String,
    direction: com.struperto.androidappdays.domain.area.AreaDirectionSemantics = startAreaDirectionSemantics(null),
    directionMode: AreaDirectionMode = AreaDirectionMode.Balanced,
): String {
    return when (directionMode) {
        AreaDirectionMode.Focus -> "$focusTrack zuerst"
        AreaDirectionMode.Rhythm -> when (cadence) {
            "daily" -> direction.dailyTemplate.format(focusTrack)
            "weekly" -> direction.weeklyTemplate.format(focusTrack)
            else -> direction.adaptiveTemplate.format(focusTrack)
        }
        AreaDirectionMode.Balanced -> when (cadence) {
            "daily" -> direction.dailyTemplate.format(focusTrack)
            "weekly" -> direction.weeklyTemplate.format(focusTrack)
            else -> direction.adaptiveTemplate.format(focusTrack)
        }
    }
}

private fun startAreaSourcesSummary(
    selectedTracks: Set<String>,
    blueprint: StartAreaBlueprint,
): String {
    return startAreaSourcesSummary(
        selectedTracks = selectedTracks,
        trackLabels = blueprint.tracks,
    )
}

private fun startAreaSourcesSummary(
    selectedTracks: Set<String>,
    trackLabels: List<String>,
    sourcesMode: AreaSourcesMode = AreaSourcesMode.Balanced,
    signalBlend: Int = 0,
): String {
    val orderedTracks = trackLabels.filter { it in selectedTracks }
    if (orderedTracks.isEmpty()) {
        return "Keine aktiv"
    }
    return when (sourcesMode) {
        AreaSourcesMode.Signals -> "${orderedTracks.first()} · $signalBlend% Signal"
        AreaSourcesMode.Curated -> orderedTracks.first()
        AreaSourcesMode.Balanced -> {
            val visibleTracks = orderedTracks.take(2)
            val hiddenCount = orderedTracks.size - visibleTracks.size
            buildString {
                append(visibleTracks.joinToString(" · "))
                if (hiddenCount > 0) {
                    append(" +$hiddenCount")
                }
            }
        }
    }
}

private fun startAreaSourcesEffectLabel(
    selectedTracks: Set<String>,
    trackLabels: List<String>,
    sourcesMode: AreaSourcesMode,
    signalBlend: Int,
): String {
    val orderedTracks = trackLabels.filter { it in selectedTracks }
    if (orderedTracks.isEmpty()) {
        return "Heute fehlt noch eine aktive Quelle fuer die Lesart."
    }
    val leadTrack = orderedTracks.first()
    return when (sourcesMode) {
        AreaSourcesMode.Balanced -> when (orderedTracks.size) {
            1 -> "Heute formt $leadTrack die Lesart des Bereichs."
            2 -> "Heute formen ${orderedTracks[0]} und ${orderedTracks[1]} gemeinsam die Lesart."
            else -> "Heute formen ${orderedTracks[0]} und ${orderedTracks[1]} den Bereich, ${orderedTracks.size - 2} weitere laufen mit."
        }

        AreaSourcesMode.Curated -> "Heute fuehrt $leadTrack die Lesart des Bereichs."

        AreaSourcesMode.Signals -> when {
            signalBlend >= 75 -> "Heute fuehren Signale rund um $leadTrack die Lesart."
            signalBlend >= 50 -> "Heute ziehen Signale zu $leadTrack deutlich mit."
            signalBlend > 0 -> "Heute bleibt $leadTrack vorn, Signale mischen leicht mit."
            else -> "Heute fuehrt $leadTrack ohne Signalanteil."
        }
    }
}

private fun startAreaSnapshotSummary(
    summary: String,
    lageMode: AreaLageMode,
    lageLabel: String,
): String {
    return when (lageMode) {
        AreaLageMode.Score -> summary
        AreaLageMode.State -> "$lageLabel · $summary"
    }
}

private fun startAreaSnapshotEffectLabel(
    lageMode: AreaLageMode,
    hasValue: Boolean,
): String {
    return when (lageMode) {
        AreaLageMode.Score -> if (hasValue) {
            "Der Lagewert zeigt heute direkt die Distanz zum Soll."
        } else {
            "Ohne frischen Lagewert bleibt nur das Soll als Orientierung."
        }

        AreaLageMode.State -> if (hasValue) {
            "Der Lesepunkt legt fest, wie der Bereich heute gelesen wird."
        } else {
            "Ohne Lesepunkt bleibt die heutige Einordnung offen."
        }
    }
}

private fun startAreaDirectionEffectLabel(
    focusTrack: String,
    cadence: String,
    directionMode: AreaDirectionMode,
): String {
    return when (directionMode) {
        AreaDirectionMode.Focus -> "Heute startet der Bereich mit $focusTrack."
        AreaDirectionMode.Rhythm -> when (cadence) {
            "daily" -> "Heute fuehrt $focusTrack den ersten Schritt."
            "weekly" -> "Diese Woche fuehrt $focusTrack den Bereich."
            else -> "Heute richtet sich der Bereich zuerst auf $focusTrack aus."
        }

        AreaDirectionMode.Balanced -> when (cadence) {
            "daily" -> "Heute steht $focusTrack zuerst, der Rest folgt im Takt."
            "weekly" -> "Diese Woche fuehrt $focusTrack, ohne den Rest auszublenden."
            else -> "Heute fuehrt $focusTrack, waehrend der Rest mitlaeuft."
        }
    }
}

private fun startAreaProfileState(
    profileConfig: AreaProfileConfig,
    focusTrack: String,
): StartAreaProfileState {
    val lageLabel = when (profileConfig.lageMode) {
        AreaLageMode.Score -> "Status"
        AreaLageMode.State -> "Reflexion"
    }
    val directionLabel = when (profileConfig.directionMode) {
        AreaDirectionMode.Balanced -> "Ausbalanciert"
        AreaDirectionMode.Focus -> "Fokus"
        AreaDirectionMode.Rhythm -> "Rhythmus"
    }
    val sourcesLabel = when (profileConfig.sourcesMode) {
        AreaSourcesMode.Balanced -> "Offen"
        AreaSourcesMode.Signals -> "Signalnah"
        AreaSourcesMode.Curated -> "Kuratiert"
    }
    val flowLabel = when (profileConfig.flowProfile) {
        AreaFlowProfile.Stable -> "Stabil"
        AreaFlowProfile.Supportive -> "Tragend"
        AreaFlowProfile.Active -> "Aktiv"
    }
    return StartAreaProfileState(
        lageMode = profileConfig.lageMode,
        lageLabel = lageLabel,
        directionMode = profileConfig.directionMode,
        directionLabel = directionLabel,
        sourcesMode = profileConfig.sourcesMode,
        sourcesLabel = sourcesLabel,
        flowProfile = profileConfig.flowProfile,
        flowLabel = flowLabel,
        overviewLabel = "$focusTrack · $flowLabel",
    )
}

private fun areaLageModeOptions(
    selected: AreaLageMode,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaLageMode.Score.persistedValue,
            label = "Status",
            selected = selected == AreaLageMode.Score,
            supportingLabel = "liest den Bereich eher ueber Wert und Stabilitaet",
        ),
        StartPanelOptionState(
            id = AreaLageMode.State.persistedValue,
            label = "Reflexion",
            selected = selected == AreaLageMode.State,
            supportingLabel = "liest den Bereich eher ueber Zustand und Lesepunkt",
        ),
    )
}

private fun areaDirectionModeOptions(
    selected: AreaDirectionMode,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaDirectionMode.Balanced.persistedValue,
            label = "Ausbalanciert",
            selected = selected == AreaDirectionMode.Balanced,
            supportingLabel = "haelt Fokus und Takt gemeinsam sichtbar",
        ),
        StartPanelOptionState(
            id = AreaDirectionMode.Focus.persistedValue,
            label = "Fokus",
            selected = selected == AreaDirectionMode.Focus,
            supportingLabel = "zieht eine Spur klar nach vorn",
        ),
        StartPanelOptionState(
            id = AreaDirectionMode.Rhythm.persistedValue,
            label = "Rhythmus",
            selected = selected == AreaDirectionMode.Rhythm,
            supportingLabel = "liest Richtung staerker ueber den Takt",
        ),
    )
}

private fun areaSourcesModeOptions(
    selected: AreaSourcesMode,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaSourcesMode.Balanced.persistedValue,
            label = "Offen",
            selected = selected == AreaSourcesMode.Balanced,
            supportingLabel = "haelt mehrere Quellen gleichmaessig offen",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Curated.persistedValue,
            label = "Kuratiert",
            selected = selected == AreaSourcesMode.Curated,
            supportingLabel = "zieht eine bewusst kuratierte Spur in den Vordergrund",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Signals.persistedValue,
            label = "Signalnah",
            selected = selected == AreaSourcesMode.Signals,
            supportingLabel = "liest Quellen staerker ueber Signalanteil und Mix",
        ),
    )
}

private fun areaFlowProfileOptions(
    selected: AreaFlowProfile,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaFlowProfile.Stable.persistedValue,
            label = "Stabil",
            selected = selected == AreaFlowProfile.Stable,
            supportingLabel = "haelt den Bereich ruhig und dauerhaft tragbar",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Supportive.persistedValue,
            label = "Tragend",
            selected = selected == AreaFlowProfile.Supportive,
            supportingLabel = "stuetzt Rueckkehr und sanften lokalen Zug",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Active.persistedValue,
            label = "Aktiv",
            selected = selected == AreaFlowProfile.Active,
            supportingLabel = "legt mehr Aktivierung und sichtbaren Zug hinein",
        ),
    )
}

private fun targetScoreSupportingLabel(
    score: Int,
): String {
    return when (score) {
        1 -> "reicht als minimales Halteniveau"
        2 -> "haelt den Bereich gerade so offen"
        3 -> "soll spuerbar tragbar sein"
        4 -> "soll klar und verlaesslich stehen"
        else -> "soll heute voll tragen"
    }
}

private fun sourcesBlendSupportingLabel(
    value: Int,
): String {
    return when (value) {
        0 -> "nur kuratierte oder manuelle Spur fuehrt"
        25 -> "Signale geben einen leichten Einschlag"
        50 -> "Signale und kuratierte Spur halten sich die Waage"
        75 -> "Signale ziehen die Lesart deutlich"
        else -> "Signale fuehren den Bereich voll"
    }
}

private fun flowIntensitySupportingLabel(
    value: Int,
): String {
    return when (value) {
        1 -> "haelt Rueckkopplung fast unsichtbar"
        2 -> "setzt nur leichte Rueckkopplung"
        3 -> "haelt den Bereich spuerbar in Bewegung"
        4 -> "zieht den Bereich aktiv nach vorn"
        else -> "legt maximalen Zug in Impulse und Rueckkopplung"
    }
}

private fun startAreaFlowEffectLabel(
    blueprint: AreaBlueprint?,
    intensity: Int,
    remindersEnabled: Boolean,
    reviewEnabled: Boolean,
    experimentsEnabled: Boolean,
    flowProfile: AreaFlowProfile,
): String {
    val toggleLead = flowEffectLeadLabel(
        selectedFlowEffectLabels(
            blueprint = blueprint,
            remindersEnabled = remindersEnabled,
            reviewEnabled = reviewEnabled,
            experimentsEnabled = experimentsEnabled,
        ),
    )
    val intensityCue = when (intensity) {
        1, 2 -> "leicht"
        3 -> "spuerbar"
        else -> "deutlich"
    }
    return when (flowProfile) {
        AreaFlowProfile.Stable -> toggleLead?.let {
            "Heute haelt $it die Rueckkopplung $intensityCue ruhig."
        } ?: "Heute bleibt die Rueckkopplung ruhig und lokal."

        AreaFlowProfile.Supportive -> toggleLead?.let {
            "Heute stuetzt $it den Bereich $intensityCue in der Rueckkehr."
        } ?: "Heute stuetzt der Flow nur still im Hintergrund."

        AreaFlowProfile.Active -> toggleLead?.let {
            "Heute zieht $it den Bereich $intensityCue nach vorn."
        } ?: "Heute ist Zug angelegt, aber noch ohne aktive Impulse."
    }
}

private fun selectedFlowEffectLabels(
    blueprint: AreaBlueprint?,
    remindersEnabled: Boolean,
    reviewEnabled: Boolean,
    experimentsEnabled: Boolean,
): List<String> {
    val togglesById = startAreaFlowToggles(blueprint).associateBy { it.id }
    return buildList {
        if (remindersEnabled) add(togglesById.getValue("reminders").label)
        if (reviewEnabled) add(togglesById.getValue("review").label)
        if (experimentsEnabled) add(togglesById.getValue("experiments").label)
    }
}

private fun flowEffectLeadLabel(
    toggleLabels: List<String>,
): String? {
    return when (toggleLabels.size) {
        0 -> null
        1 -> toggleLabels.first()
        2 -> "${toggleLabels[0]} und ${toggleLabels[1]}"
        else -> "${toggleLabels[0]} und ${toggleLabels.size - 1} weitere Impulse"
    }
}
