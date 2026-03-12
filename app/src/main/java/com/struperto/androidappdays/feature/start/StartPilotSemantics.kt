package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.domain.area.AreaBlueprint
import com.struperto.androidappdays.domain.area.AreaDefinition
import com.struperto.androidappdays.domain.area.AreaDirectionSemantics
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaFlowSemantics
import com.struperto.androidappdays.domain.area.AreaFlowToggleSemantics
import com.struperto.androidappdays.domain.area.AreaLageStateOption
import com.struperto.androidappdays.domain.area.AreaLageStateTone
import com.struperto.androidappdays.domain.area.AreaLageType
import com.struperto.androidappdays.domain.area.AreaOverviewMode
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.AreaStatusSemantics
import com.struperto.androidappdays.domain.area.AreaSourceChannel

internal data class StartResolvedStatusPresentation(
    val kind: StartAreaStatusKind,
    val overviewLabel: String,
    val detailLabel: String,
    val overviewProgress: Float,
    val detailProgress: Float,
    val panelSummary: String,
    val countLabel: String,
    val headerLabel: String,
    val infoLabel: String,
    val coreTitle: String,
    val coreValue: String,
    val coreCaption: String,
    val selectionActionId: StartPanelActionId,
    val selectionActionLabel: String,
    val selectionValueLabel: String,
    val selectionSheetTitle: String,
    val selectionOptions: List<StartPanelOptionState>,
    val primaryMetric: StartPanelMetricState,
    val resetValueLabel: String,
)

internal fun startAreaTrackLabels(
    blueprint: AreaBlueprint,
): List<String> {
    val semanticLabels = blueprint.pilotSemantics
        ?.sources
        ?.channels
        ?.map(AreaSourceChannel::label)
        .orEmpty()
    return semanticLabels.ifEmpty { blueprint.trackLabels }
}

internal fun resolveStartStatusPresentation(
    definition: AreaDefinition?,
    blueprint: AreaBlueprint?,
    targetScore: Int,
    snapshot: AreaSnapshot?,
    areaTitle: String,
    cadence: String,
): StartResolvedStatusPresentation {
    val stateOption = resolveStartLageStateOption(
        definition = definition,
        blueprint = blueprint,
        snapshot = snapshot,
    )
    val statusSemantics = startAreaStatusSemantics(blueprint)
    if (usesStateLage(definition = definition, blueprint = blueprint)) {
        return stateStatusPresentation(
            stateOption = stateOption,
            options = blueprint?.pilotSemantics?.lageOptions.orEmpty(),
            cadence = cadence,
            statusSemantics = statusSemantics,
        )
    }
    return scoreStatusPresentation(
        definition = definition,
        blueprint = blueprint,
        targetScore = targetScore,
        manualScore = snapshot?.manualScore,
        cadence = cadence,
    )
}

internal fun startAreaDirectionSemantics(
    blueprint: AreaBlueprint?,
): AreaDirectionSemantics {
    return blueprint?.pilotSemantics?.direction ?: AreaDirectionSemantics()
}

internal fun startAreaSourceChannels(
    blueprint: AreaBlueprint?,
    trackLabels: List<String>,
): List<AreaSourceChannel> {
    return blueprint?.pilotSemantics
        ?.sources
        ?.channels
        ?.takeIf { it.isNotEmpty() }
        ?: trackLabels.map { label ->
            AreaSourceChannel(
                label = label,
                summary = "lokal und manuell im Blick halten",
            )
        }
}

internal fun startAreaFlowSemantics(
    blueprint: AreaBlueprint?,
): AreaFlowSemantics? {
    return blueprint?.pilotSemantics?.flow
}

internal fun startAreaFlowToggles(
    blueprint: AreaBlueprint?,
): List<AreaFlowToggleSemantics> {
    return blueprint?.pilotSemantics
        ?.flow
        ?.toggles
        ?.takeIf { it.isNotEmpty() }
        ?: listOf(
            AreaFlowToggleSemantics(
                id = "reminders",
                label = "Erinnern",
                summary = "kleine Hinweise lokal sichtbar halten",
            ),
            AreaFlowToggleSemantics(
                id = "review",
                label = "Review",
                summary = "den Bereich bewusst kurz durchsehen",
            ),
            AreaFlowToggleSemantics(
                id = "experiments",
                label = "Experimente",
                summary = "eine neue lokale Variante ausprobieren",
            ),
        )
}

internal fun startAreaFlowSummary(
    blueprint: AreaBlueprint?,
    remindersEnabled: Boolean,
    reviewEnabled: Boolean,
    experimentsEnabled: Boolean,
    flowProfile: AreaFlowProfile = AreaFlowProfile.Stable,
): String {
    val toggleLabels = enabledFlowLabels(
        blueprint = blueprint,
        remindersEnabled = remindersEnabled,
        reviewEnabled = reviewEnabled,
        experimentsEnabled = experimentsEnabled,
    )
    val neutral = startAreaFlowSemantics(blueprint)?.neutralLabel ?: "nur lokal"
    val base = toggleLabels.joinToString(" · ").ifBlank { neutral }
    val profileLabel = when (flowProfile) {
        AreaFlowProfile.Stable -> "Stabil"
        AreaFlowProfile.Supportive -> "Tragend"
        AreaFlowProfile.Active -> "Aktiv"
    }
    return "$profileLabel · $base"
}

internal fun startAreaFlowPrimaryLabel(
    blueprint: AreaBlueprint?,
    remindersEnabled: Boolean,
    reviewEnabled: Boolean,
    experimentsEnabled: Boolean,
    flowProfile: AreaFlowProfile = AreaFlowProfile.Stable,
): String {
    val toggleLabels = enabledFlowLabels(
        blueprint = blueprint,
        remindersEnabled = remindersEnabled,
        reviewEnabled = reviewEnabled,
        experimentsEnabled = experimentsEnabled,
    )
    val neutral = startAreaFlowSemantics(blueprint)?.neutralLabel ?: "Lokal"
    val base = when (toggleLabels.size) {
        0 -> neutral
        1 -> toggleLabels.first()
        else -> "${toggleLabels.size} aktiv"
    }
    return when (flowProfile) {
        AreaFlowProfile.Stable -> base
        AreaFlowProfile.Supportive -> base
        AreaFlowProfile.Active -> base
    }
}

internal fun startAreaFlowActionLabel(
    blueprint: AreaBlueprint?,
): String {
    return if (startAreaFlowSemantics(blueprint) != null) "Impulse" else "Schalter"
}

internal fun startAreaStatusSemantics(
    blueprint: AreaBlueprint?,
): AreaStatusSemantics? {
    return blueprint?.pilotSemantics?.status
}

private fun enabledFlowLabels(
    blueprint: AreaBlueprint?,
    remindersEnabled: Boolean,
    reviewEnabled: Boolean,
    experimentsEnabled: Boolean,
): List<String> {
    val togglesById = startAreaFlowToggles(blueprint).associateBy(AreaFlowToggleSemantics::id)
    return buildList {
        if (remindersEnabled) add(togglesById.getValue("reminders").label)
        if (reviewEnabled) add(togglesById.getValue("review").label)
        if (experimentsEnabled) add(togglesById.getValue("experiments").label)
    }
}

private fun resolveStartLageStateOption(
    definition: AreaDefinition?,
    blueprint: AreaBlueprint?,
    snapshot: AreaSnapshot?,
): AreaLageStateOption? {
    if (!usesStateLage(definition = definition, blueprint = blueprint)) {
        return null
    }
    val stateKey = snapshot?.manualStateKey ?: return null
    return blueprint?.pilotSemantics
        ?.lageOptions
        ?.firstOrNull { it.id == stateKey }
}

private fun usesStateLage(
    definition: AreaDefinition?,
    blueprint: AreaBlueprint?,
): Boolean {
    if (definition == null || blueprint == null) {
        return false
    }
    if (definition.lageType == AreaLageType.SCORE) {
        return false
    }
    return blueprint.pilotSemantics?.lageOptions?.isNotEmpty() == true
}

private fun stateStatusPresentation(
    stateOption: AreaLageStateOption?,
    options: List<AreaLageStateOption>,
    cadence: String,
    statusSemantics: AreaStatusSemantics?,
): StartResolvedStatusPresentation {
    val optionLabel = stateOption?.label ?: statusSemantics?.stateEmptyLabel ?: "Reflexion offen"
    val optionSummary = stateOption?.summary
        ?: statusSemantics?.stateResetSummary
        ?: "ohne frischen Lesepunkt"
    return StartResolvedStatusPresentation(
        kind = stateOption?.tone.toStatusKind(),
        overviewLabel = optionLabel,
        detailLabel = optionLabel,
        overviewProgress = stateOption?.progress?.coerceIn(0.18f, 1f) ?: 0.18f,
        detailProgress = stateOption?.progress ?: 0.08f,
        panelSummary = stateOption?.let { "Heute ${it.label}" }
            ?: statusSemantics?.statePanelEmptyLabel
            ?: "Heute offen",
        countLabel = optionLabel,
        headerLabel = optionLabel,
        infoLabel = statusSemantics?.stateInfoLabel ?: "Reflexion",
        coreTitle = statusSemantics?.stateCoreTitle ?: "Aktuelle Lage",
        coreValue = stateOption?.label ?: "offen",
        coreCaption = optionSummary,
        selectionActionId = StartPanelActionId.SnapshotState,
        selectionActionLabel = statusSemantics?.stateActionLabel ?: "Zustand",
        selectionValueLabel = stateOption?.label ?: "offen",
        selectionSheetTitle = statusSemantics?.stateSheetTitle ?: "Zustand",
        selectionOptions = listOf(
            StartPanelOptionState(
                id = "clear",
                label = statusSemantics?.stateResetLabel ?: "Ohne Lesepunkt",
                selected = stateOption == null,
                supportingLabel = statusSemantics?.stateResetSummary ?: "ohne frischen Lesepunkt",
            ),
        ) + startAreaStateOptions(
            options = options,
            selectedOption = stateOption,
        ),
        primaryMetric = StartPanelMetricState(
            statusSemantics?.stateMetricLabel ?: "Lesart",
            statusSemantics?.stateMetricValue ?: "Reflexion",
        ),
        resetValueLabel = if (stateOption == null) "leer" else "setzen",
    )
}

private fun startAreaStateOptions(
    options: List<AreaLageStateOption>,
    selectedOption: AreaLageStateOption?,
): List<StartPanelOptionState> {
    return options.map { option ->
        StartPanelOptionState(
            id = option.id,
            label = option.label,
            selected = option.id == selectedOption?.id,
            supportingLabel = option.summary,
        )
    }
}

private fun scoreStatusPresentation(
    definition: AreaDefinition?,
    blueprint: AreaBlueprint?,
    targetScore: Int,
    manualScore: Int?,
    cadence: String,
): StartResolvedStatusPresentation {
    val pilotPrefix = pilotScorePrefix(definition = definition, blueprint = blueprint)
    val detailPrefix = pilotPrefix ?: "Lage"
    val scoreCoreTitle = startAreaStatusSemantics(blueprint)?.scoreCoreTitle
    return StartResolvedStatusPresentation(
        kind = when {
            manualScore == null -> StartAreaStatusKind.Waiting
            manualScore >= targetScore -> StartAreaStatusKind.Stable
            else -> StartAreaStatusKind.Pull
        },
        overviewLabel = manualScore?.let { score ->
            pilotPrefix?.let { "$it $score/$targetScore" } ?: "$score/$targetScore"
        } ?: pilotPrefix?.let { "$it $targetScore/5" } ?: "Ziel $targetScore/5",
        detailLabel = manualScore?.let { "$detailPrefix $it/5" } ?: "$detailPrefix offen",
        overviewProgress = manualScore?.let { score ->
            (score.toFloat() / targetScore.coerceAtLeast(1).toFloat()).coerceIn(0.18f, 1f)
        } ?: 0.18f,
        detailProgress = manualScore?.let { score ->
            (score.toFloat() / targetScore.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
        } ?: 0.08f,
        panelSummary = manualScore?.let { "Heute $it/5" } ?: "Heute offen",
        countLabel = manualScore?.let { "$detailPrefix $it/5" } ?: "$detailPrefix offen",
        headerLabel = manualScore?.let { "$detailPrefix $it/5" } ?: "$detailPrefix offen",
        infoLabel = "Heute",
        coreTitle = scoreCoreTitle ?: pilotPrefix?.let { "$it-Lage" } ?: "Aktuelle Lage",
        coreValue = manualScore?.let { "$it/5" } ?: "offen",
        coreCaption = manualScore?.let(::scoreOptionSupportingLabel) ?: "ohne frischen Lagewert",
        selectionActionId = StartPanelActionId.SnapshotScore,
        selectionActionLabel = "Lage",
        selectionValueLabel = manualScore?.let { "$it/5" } ?: "offen",
        selectionSheetTitle = if (pilotPrefix != null) "$pilotPrefix setzen" else "Lage",
        selectionOptions = listOf(
            StartPanelOptionState(
                id = "clear",
                label = "Offen",
                selected = manualScore == null,
                supportingLabel = "ohne frischen Lagewert",
            ),
        ) + (1..5).map { score ->
            StartPanelOptionState(
                id = score.toString(),
                label = "$score/5",
                selected = manualScore == score,
                supportingLabel = scoreOptionSupportingLabel(score),
            )
        },
        primaryMetric = StartPanelMetricState("Soll", "$targetScore/5"),
        resetValueLabel = if (manualScore == null) "leer" else "setzen",
    )
}

private fun scoreOptionSupportingLabel(
    score: Int,
): String {
    return when (score) {
        1 -> "deutlich unter Zug"
        2 -> "merklich unter Soll"
        3 -> "teils tragbar"
        4 -> "stabil im Zielbereich"
        else -> "voll auf Kurs"
    }
}

private fun pilotScorePrefix(
    definition: AreaDefinition?,
    blueprint: AreaBlueprint?,
): String? {
    if (definition == null || blueprint?.pilotSemantics == null) {
        return null
    }
    startAreaStatusSemantics(blueprint)?.scorePrefix?.let { return it }
    return when (definition.overviewMode) {
        AreaOverviewMode.SIGNAL -> "Signal"
        AreaOverviewMode.PLAN -> "Richtung"
        AreaOverviewMode.REFLECTION -> "Reflexion"
        AreaOverviewMode.HYBRID -> "Lage"
    }
}

private fun AreaLageStateTone?.toStatusKind(): StartAreaStatusKind {
    return when (this) {
        AreaLageStateTone.LIVE -> StartAreaStatusKind.Live
        AreaLageStateTone.STABLE -> StartAreaStatusKind.Stable
        AreaLageStateTone.PULL -> StartAreaStatusKind.Pull
        AreaLageStateTone.WAITING,
        null,
        -> StartAreaStatusKind.Waiting
    }
}
