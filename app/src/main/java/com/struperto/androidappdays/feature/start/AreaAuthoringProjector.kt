package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.domain.area.AreaAuthoringAxis
import com.struperto.androidappdays.domain.area.AreaAuthoringConfig
import com.struperto.androidappdays.domain.area.AreaBlueprint
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaDefinition
import com.struperto.androidappdays.domain.area.AreaDirectionMode
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaSourcesMode
import com.struperto.androidappdays.domain.area.AreaVisibilityLevel

data class AreaAuthoringProjectionInput(
    val definition: AreaDefinition?,
    val blueprint: AreaBlueprint,
    val instance: AreaInstance,
    val authoringConfig: AreaAuthoringConfig,
)

data class AreaAuthoringStudioState(
    val definitionId: String,
    val basisLabel: String,
    val basisSummary: String,
    val characterLabel: String,
    val summary: String,
    val previewAxes: List<AreaAuthoringAxisState>,
    val sections: List<AreaAuthoringSectionState>,
)

data class AreaAuthoringSectionState(
    val title: String,
    val summary: String,
    val axes: List<AreaAuthoringAxisState>,
)

data class AreaAuthoringAxisState(
    val axis: AreaAuthoringAxis,
    val label: String,
    val valueLabel: String,
    val summary: String,
    val options: List<AreaAuthoringOptionState>,
)

data class AreaAuthoringOptionState(
    val id: String,
    val label: String,
    val selected: Boolean,
    val supportingLabel: String = "",
)

fun projectAreaAuthoringStudioState(
    input: AreaAuthoringProjectionInput,
): AreaAuthoringStudioState {
    val authoring = input.authoringConfig
    val definition = input.definition
    val allowedAxes = definition?.authoringAxes ?: AreaAuthoringAxis.entries.toSet()
    val primaryTracks = startAreaTrackLabels(input.blueprint).take(2)
    val template = startAreaTemplate(input.instance.templateId ?: input.blueprint.defaultTemplateId)
    val characterAxes = buildCharacterAxes(
        authoring = authoring,
        primaryTracks = primaryTracks,
        allowedAxes = allowedAxes,
    )
    val depthAxes = buildDepthAxes(
        authoring = authoring,
        allowedAxes = allowedAxes,
    )
    val sections = buildList {
        if (characterAxes.isNotEmpty()) {
            add(
                AreaAuthoringSectionState(
                    title = "Die vier Achsen",
                    summary = "So laeuft der Bereich ueber Zustand, Eingang, Auswahl und Rhythmus.",
                    axes = characterAxes,
                ),
            )
        }
        if (depthAxes.isNotEmpty()) {
            add(
                AreaAuthoringSectionState(
                    title = "Studio-Ebene",
                    summary = "So offen oder kompakt der Bereich im Studio erscheinen soll.",
                    axes = depthAxes,
                ),
            )
        }
    }
    val basisLabel = when {
        definition != null && definition.seededByDefault -> "Seed-Basis"
        input.definitionIdStartsWith("template:") -> "Vorlagen-Basis"
        else -> "Authoring-Basis"
    }
    val basisSummary = when {
        definition != null && definition.seededByDefault -> "${definition.title} bleibt die Typbasis, das Profil ist jetzt direkt formbar."
        else -> "${template.label} liefert die Ausgangsform, das Profil spannt daraus die vier Felder sauber auf."
    }
    val characterLabel = characterAxes
        .map(AreaAuthoringAxisState::valueLabel)
        .joinToString(" · ")
    val summary = buildAuthoringSummary(
        axes = characterAxes,
        authoring = authoring,
        primaryTracks = primaryTracks,
    )
    return AreaAuthoringStudioState(
        definitionId = input.instance.definitionId,
        basisLabel = basisLabel,
        basisSummary = basisSummary,
        characterLabel = characterLabel,
        summary = summary,
        previewAxes = authoringPreviewAxes(
            characterAxes = characterAxes,
            depthAxes = depthAxes,
            visibilityLevel = authoring.visibilityLevel,
        ),
        sections = sections,
    )
}

private fun buildCharacterAxes(
    authoring: AreaAuthoringConfig,
    primaryTracks: List<String>,
    allowedAxes: Set<AreaAuthoringAxis>,
): List<AreaAuthoringAxisState> {
    return buildList {
        if (AreaAuthoringAxis.STATUS_SCHEMA in allowedAxes) {
            add(
                AreaAuthoringAxisState(
                    axis = AreaAuthoringAxis.STATUS_SCHEMA,
                    label = "Zustand",
                    valueLabel = authoringLageLabel(authoring.lageMode),
                    summary = authoringLageSummary(authoring.lageMode),
                    options = areaAuthoringLageOptions(authoring.lageMode),
                ),
            )
        }
        if (AreaAuthoringAxis.SOURCES in allowedAxes) {
            add(
                AreaAuthoringAxisState(
                    axis = AreaAuthoringAxis.SOURCES,
                    label = "Eingang",
                    valueLabel = authoringSourcesLabel(authoring.sourcesMode),
                    summary = authoringSourcesSummary(authoring.sourcesMode),
                    options = areaAuthoringSourcesOptions(authoring.sourcesMode),
                ),
            )
        }
        if (AreaAuthoringAxis.DIRECTION in allowedAxes) {
            add(
                AreaAuthoringAxisState(
                    axis = AreaAuthoringAxis.DIRECTION,
                    label = "Auswahl",
                    valueLabel = authoringDirectionLabel(authoring.directionMode),
                    summary = authoringDirectionSummary(authoring.directionMode, primaryTracks),
                    options = areaAuthoringDirectionOptions(authoring.directionMode),
                ),
            )
        }
        if (AreaAuthoringAxis.FLOW in allowedAxes) {
            add(
                AreaAuthoringAxisState(
                    axis = AreaAuthoringAxis.FLOW,
                    label = "Rhythmus",
                    valueLabel = authoringFlowLabel(authoring.flowProfile),
                    summary = authoringFlowSummary(authoring.flowProfile),
                    options = areaAuthoringFlowOptions(authoring.flowProfile),
                ),
            )
        }
    }
}

private fun buildDepthAxes(
    authoring: AreaAuthoringConfig,
    allowedAxes: Set<AreaAuthoringAxis>,
): List<AreaAuthoringAxisState> {
    return buildList {
        if (AreaAuthoringAxis.COMPLEXITY in allowedAxes) {
            add(
                AreaAuthoringAxisState(
                    axis = AreaAuthoringAxis.COMPLEXITY,
                    label = "Tiefe",
                    valueLabel = authoringComplexityLabel(authoring.complexityLevel),
                    summary = authoringComplexitySummary(authoring.complexityLevel),
                    options = areaAuthoringComplexityOptions(authoring.complexityLevel),
                ),
            )
        }
        if (AreaAuthoringAxis.VISIBILITY in allowedAxes) {
            add(
                AreaAuthoringAxisState(
                    axis = AreaAuthoringAxis.VISIBILITY,
                    label = "Sichtbarkeit",
                    valueLabel = authoringVisibilityLabel(authoring.visibilityLevel),
                    summary = authoringVisibilitySummary(authoring.visibilityLevel),
                    options = areaAuthoringVisibilityOptions(authoring.visibilityLevel),
                ),
            )
        }
    }
}

private fun buildAuthoringSummary(
    axes: List<AreaAuthoringAxisState>,
    authoring: AreaAuthoringConfig,
    primaryTracks: List<String>,
): String {
    val fragments = buildList {
        if (axes.any { it.axis == AreaAuthoringAxis.STATUS_SCHEMA }) {
            add("Zustand ${authoringLageSummary(authoring.lageMode)}")
        }
        if (axes.any { it.axis == AreaAuthoringAxis.SOURCES }) {
            add("Eingang ${authoringSourcesSummary(authoring.sourcesMode)}")
        }
        if (axes.any { it.axis == AreaAuthoringAxis.DIRECTION }) {
            add("Auswahl ${authoringDirectionSummary(authoring.directionMode, primaryTracks)}")
        }
        if (axes.any { it.axis == AreaAuthoringAxis.FLOW }) {
            add("Rhythmus ${authoringFlowSummary(authoring.flowProfile)}")
        }
    }
    return when {
        fragments.isEmpty() -> "Profil und Studio-Ebene bleiben fuer diesen Bereich anpassbar."
        else -> fragments.joinToString(", ").replaceFirstChar { it.uppercase() } + "."
    }
}

private fun authoringPreviewAxes(
    characterAxes: List<AreaAuthoringAxisState>,
    depthAxes: List<AreaAuthoringAxisState>,
    visibilityLevel: AreaVisibilityLevel,
): List<AreaAuthoringAxisState> {
    return when (visibilityLevel) {
        AreaVisibilityLevel.Focused -> characterAxes.take(2).ifEmpty { depthAxes.take(1) }
        AreaVisibilityLevel.Standard -> characterAxes.take(4).ifEmpty { depthAxes.take(2) }
        AreaVisibilityLevel.Expanded -> (characterAxes + depthAxes).take(6)
    }
}

private fun AreaAuthoringProjectionInput.definitionIdStartsWith(prefix: String): Boolean {
    return instance.definitionId.startsWith(prefix)
}

internal fun authoringLageLabel(lageMode: AreaLageMode): String {
    return when (lageMode) {
        AreaLageMode.Score -> "Status"
        AreaLageMode.State -> "Reflexion"
    }
}

internal fun authoringDirectionLabel(directionMode: AreaDirectionMode): String {
    return when (directionMode) {
        AreaDirectionMode.Balanced -> "Ausbalanciert"
        AreaDirectionMode.Focus -> "Fokus"
        AreaDirectionMode.Rhythm -> "Rhythmus"
    }
}

internal fun authoringSourcesLabel(sourcesMode: AreaSourcesMode): String {
    return when (sourcesMode) {
        AreaSourcesMode.Balanced -> "Offen"
        AreaSourcesMode.Signals -> "Signalnah"
        AreaSourcesMode.Curated -> "Kuratiert"
    }
}

internal fun authoringFlowLabel(flowProfile: AreaFlowProfile): String {
    return when (flowProfile) {
        AreaFlowProfile.Stable -> "Stabil"
        AreaFlowProfile.Supportive -> "Tragend"
        AreaFlowProfile.Active -> "Aktiv"
    }
}

internal fun authoringComplexityLabel(complexityLevel: AreaComplexityLevel): String {
    return when (complexityLevel) {
        AreaComplexityLevel.BASIC -> "Basic"
        AreaComplexityLevel.ADVANCED -> "Advanced"
        AreaComplexityLevel.EXPERT -> "Expert"
    }
}

internal fun authoringVisibilityLabel(visibilityLevel: AreaVisibilityLevel): String {
    return when (visibilityLevel) {
        AreaVisibilityLevel.Focused -> "Fokussiert"
        AreaVisibilityLevel.Standard -> "Gefuehrt"
        AreaVisibilityLevel.Expanded -> "Offen"
    }
}

private fun authoringLageSummary(lageMode: AreaLageMode): String {
    return when (lageMode) {
        AreaLageMode.Score -> "liest eher ueber Wert und Zielabstand"
        AreaLageMode.State -> "liest eher ueber Zustand und Lesepunkt"
    }
}

private fun authoringDirectionSummary(
    directionMode: AreaDirectionMode,
    primaryTracks: List<String>,
): String {
    val leadTrack = primaryTracks.firstOrNull() ?: "eine Spur"
    return when (directionMode) {
        AreaDirectionMode.Balanced -> "haelt Fokus und Takt gemeinsam"
        AreaDirectionMode.Focus -> "zieht $leadTrack klar nach vorn"
        AreaDirectionMode.Rhythm -> "liest staerker ueber den Takt"
    }
}

private fun authoringSourcesSummary(sourcesMode: AreaSourcesMode): String {
    return when (sourcesMode) {
        AreaSourcesMode.Balanced -> "halten mehrere Materialspuren offen"
        AreaSourcesMode.Signals -> "ziehen frische Signale staerker nach vorn"
        AreaSourcesMode.Curated -> "folgen bewusst kuratiertem Material"
    }
}

private fun authoringFlowSummary(flowProfile: AreaFlowProfile): String {
    return when (flowProfile) {
        AreaFlowProfile.Stable -> "bleibt ruhig und lokal"
        AreaFlowProfile.Supportive -> "stuetzt Rueckkehr und Halt"
        AreaFlowProfile.Active -> "zieht aktiv ins Naechste"
    }
}

private fun authoringComplexitySummary(complexityLevel: AreaComplexityLevel): String {
    return when (complexityLevel) {
        AreaComplexityLevel.BASIC -> "zeigt nur die Grundachsen und klare Defaults"
        AreaComplexityLevel.ADVANCED -> "zeigt mehr steuerbare Charakterachsen"
        AreaComplexityLevel.EXPERT -> "oeffnet die volle Charaktertiefe fuer diesen Bereich"
    }
}

private fun authoringVisibilitySummary(visibilityLevel: AreaVisibilityLevel): String {
    return when (visibilityLevel) {
        AreaVisibilityLevel.Focused -> "haelt den Bereich im Studio bewusst kompakt"
        AreaVisibilityLevel.Standard -> "zeigt die wichtigsten Charakterachsen mit Guidance"
        AreaVisibilityLevel.Expanded -> "macht mehr Charakter und Optionen direkt sichtbar"
    }
}

internal fun areaAuthoringLageOptions(selected: AreaLageMode): List<AreaAuthoringOptionState> {
    return listOf(
        AreaAuthoringOptionState(
            id = AreaLageMode.Score.persistedValue,
            label = "Wert",
            selected = selected == AreaLageMode.Score,
            supportingLabel = "liest ueber Wert und Zielabstand",
        ),
        AreaAuthoringOptionState(
            id = AreaLageMode.State.persistedValue,
            label = "Zustand",
            selected = selected == AreaLageMode.State,
            supportingLabel = "liest ueber Zustand und Lesepunkt",
        ),
    )
}

internal fun areaAuthoringDirectionOptions(selected: AreaDirectionMode): List<AreaAuthoringOptionState> {
    return AreaDirectionMode.entries.map { mode ->
        AreaAuthoringOptionState(
            id = mode.persistedValue,
            label = authoringDirectionLabel(mode),
            selected = selected == mode,
            supportingLabel = when (mode) {
                AreaDirectionMode.Balanced -> "haelt Fokus und Rhythmus gemeinsam sichtbar"
                AreaDirectionMode.Focus -> "zieht eine Spur klar nach vorn"
                AreaDirectionMode.Rhythm -> "liest Richtung staerker ueber den Takt"
            },
        )
    }
}

internal fun areaAuthoringSourcesOptions(selected: AreaSourcesMode): List<AreaAuthoringOptionState> {
    return AreaSourcesMode.entries.map { mode ->
        AreaAuthoringOptionState(
            id = mode.persistedValue,
            label = authoringSourcesLabel(mode),
            selected = selected == mode,
            supportingLabel = when (mode) {
                AreaSourcesMode.Balanced -> "laesst mehrere Materialspuren zusammenstehen"
                AreaSourcesMode.Signals -> "zieht Signale und Frische staerker nach vorn"
                AreaSourcesMode.Curated -> "betont gezieltes und bewusstes Material"
            },
        )
    }
}

internal fun areaAuthoringFlowOptions(selected: AreaFlowProfile): List<AreaAuthoringOptionState> {
    return AreaFlowProfile.entries.map { profile ->
        AreaAuthoringOptionState(
            id = profile.persistedValue,
            label = authoringFlowLabel(profile),
            selected = selected == profile,
            supportingLabel = when (profile) {
                AreaFlowProfile.Stable -> "bleibt sichtbar ohne viel Zug"
                AreaFlowProfile.Supportive -> "traegt Rueckkehr und Reviews"
                AreaFlowProfile.Active -> "zieht Impulse und Naechstes nach vorn"
            },
        )
    }
}

internal fun areaAuthoringComplexityOptions(selected: AreaComplexityLevel): List<AreaAuthoringOptionState> {
    return AreaComplexityLevel.entries.map { level ->
        AreaAuthoringOptionState(
            id = level.name.lowercase(),
            label = authoringComplexityLabel(level),
            selected = selected == level,
            supportingLabel = authoringComplexitySummary(level),
        )
    }
}

internal fun areaAuthoringVisibilityOptions(selected: AreaVisibilityLevel): List<AreaAuthoringOptionState> {
    return AreaVisibilityLevel.entries.map { level ->
        AreaAuthoringOptionState(
            id = level.persistedValue,
            label = authoringVisibilityLabel(level),
            selected = selected == level,
            supportingLabel = authoringVisibilitySummary(level),
        )
    }
}
