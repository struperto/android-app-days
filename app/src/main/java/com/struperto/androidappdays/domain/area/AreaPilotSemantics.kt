package com.struperto.androidappdays.domain.area

data class AreaLageStateOption(
    val id: String,
    val label: String,
    val summary: String,
    val tone: AreaLageStateTone,
    val progress: Float,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank." }
        require(label.isNotBlank()) { "label must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
        require(progress in 0f..1f) { "progress must be between 0 and 1." }
    }
}

enum class AreaLageStateTone {
    WAITING,
    LIVE,
    STABLE,
    PULL,
}

data class AreaStatusSemantics(
    val scorePrefix: String? = null,
    val scoreCoreTitle: String? = null,
    val stateEmptyLabel: String = "Reflexion offen",
    val statePanelEmptyLabel: String = "Heute offen",
    val stateInfoLabel: String = "Reflexion",
    val stateCoreTitle: String = "Aktuelle Lage",
    val stateActionLabel: String = "Zustand",
    val stateSheetTitle: String = "Zustand",
    val stateResetLabel: String = "Ohne Lesepunkt",
    val stateResetSummary: String = "ohne frischen Lesepunkt",
    val stateMetricLabel: String = "Lesart",
    val stateMetricValue: String = "Reflexion",
) {
    init {
        require(scorePrefix == null || scorePrefix.isNotBlank()) { "scorePrefix must not be blank." }
        require(scoreCoreTitle == null || scoreCoreTitle.isNotBlank()) { "scoreCoreTitle must not be blank." }
        require(stateEmptyLabel.isNotBlank()) { "stateEmptyLabel must not be blank." }
        require(statePanelEmptyLabel.isNotBlank()) { "statePanelEmptyLabel must not be blank." }
        require(stateInfoLabel.isNotBlank()) { "stateInfoLabel must not be blank." }
        require(stateCoreTitle.isNotBlank()) { "stateCoreTitle must not be blank." }
        require(stateActionLabel.isNotBlank()) { "stateActionLabel must not be blank." }
        require(stateSheetTitle.isNotBlank()) { "stateSheetTitle must not be blank." }
        require(stateResetLabel.isNotBlank()) { "stateResetLabel must not be blank." }
        require(stateResetSummary.isNotBlank()) { "stateResetSummary must not be blank." }
        require(stateMetricLabel.isNotBlank()) { "stateMetricLabel must not be blank." }
        require(stateMetricValue.isNotBlank()) { "stateMetricValue must not be blank." }
    }
}

data class AreaDirectionSemantics(
    val focusActionLabel: String = "Fokus",
    val nextMoveLabel: String = "Naechster Zug",
    val dailyTemplate: String = "Heute %s",
    val weeklyTemplate: String = "Diese Woche %s",
    val adaptiveTemplate: String = "%s zuerst",
) {
    init {
        require(focusActionLabel.isNotBlank()) { "focusActionLabel must not be blank." }
        require(nextMoveLabel.isNotBlank()) { "nextMoveLabel must not be blank." }
        require("%s" in dailyTemplate) { "dailyTemplate must contain %s." }
        require("%s" in weeklyTemplate) { "weeklyTemplate must contain %s." }
        require("%s" in adaptiveTemplate) { "adaptiveTemplate must contain %s." }
    }
}

data class AreaSourceChannel(
    val label: String,
    val summary: String,
    val sourceType: AreaSourceType = AreaSourceType.MANUAL,
) {
    init {
        require(label.isNotBlank()) { "label must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
    }
}

data class AreaSourceSemantics(
    val coreTitle: String = "Aktive Quellen",
    val mixLabel: String = "Mix",
    val channels: List<AreaSourceChannel>,
) {
    init {
        require(coreTitle.isNotBlank()) { "coreTitle must not be blank." }
        require(mixLabel.isNotBlank()) { "mixLabel must not be blank." }
        require(channels.none { it.label.isBlank() }) { "channels must not contain blank labels." }
    }
}

data class AreaFlowToggleSemantics(
    val id: String,
    val label: String,
    val summary: String,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank." }
        require(label.isNotBlank()) { "label must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
    }
}

data class AreaFlowSemantics(
    val coreTitle: String = "Lokaler Flow",
    val intensityLabel: String = "Intensitaet",
    val neutralLabel: String = "Lokal",
    val toggles: List<AreaFlowToggleSemantics>,
) {
    init {
        require(coreTitle.isNotBlank()) { "coreTitle must not be blank." }
        require(intensityLabel.isNotBlank()) { "intensityLabel must not be blank." }
        require(neutralLabel.isNotBlank()) { "neutralLabel must not be blank." }
        require(toggles.none { it.id.isBlank() || it.label.isBlank() }) {
            "toggles must not contain blank ids or labels."
        }
    }
}

data class AreaPilotSemantics(
    val status: AreaStatusSemantics? = null,
    val lageOptions: List<AreaLageStateOption> = emptyList(),
    val direction: AreaDirectionSemantics = AreaDirectionSemantics(),
    val sources: AreaSourceSemantics? = null,
    val flow: AreaFlowSemantics? = null,
)

fun scoreAreaSemantics(
    scorePrefix: String,
    scoreCoreTitle: String = "$scorePrefix-Lage",
    direction: AreaDirectionSemantics = AreaDirectionSemantics(),
    sources: AreaSourceSemantics? = null,
    flow: AreaFlowSemantics? = null,
): AreaPilotSemantics {
    return AreaPilotSemantics(
        status = AreaStatusSemantics(
            scorePrefix = scorePrefix,
            scoreCoreTitle = scoreCoreTitle,
        ),
        direction = direction,
        sources = sources,
        flow = flow,
    )
}

fun stateAreaSemantics(
    lageOptions: List<AreaLageStateOption>,
    stateCoreTitle: String,
    stateInfoLabel: String = "Reflexion",
    stateMetricLabel: String = "Lesart",
    stateMetricValue: String = stateInfoLabel,
    stateActionLabel: String = "Zustand",
    stateSheetTitle: String = stateActionLabel,
    stateEmptyLabel: String = "$stateInfoLabel offen",
    statePanelEmptyLabel: String = "Heute offen",
    stateResetLabel: String = "Ohne Lesepunkt",
    stateResetSummary: String = "ohne frischen Lesepunkt",
    direction: AreaDirectionSemantics = AreaDirectionSemantics(),
    sources: AreaSourceSemantics? = null,
    flow: AreaFlowSemantics? = null,
): AreaPilotSemantics {
    return AreaPilotSemantics(
        status = AreaStatusSemantics(
            stateEmptyLabel = stateEmptyLabel,
            statePanelEmptyLabel = statePanelEmptyLabel,
            stateInfoLabel = stateInfoLabel,
            stateCoreTitle = stateCoreTitle,
            stateActionLabel = stateActionLabel,
            stateSheetTitle = stateSheetTitle,
            stateResetLabel = stateResetLabel,
            stateResetSummary = stateResetSummary,
            stateMetricLabel = stateMetricLabel,
            stateMetricValue = stateMetricValue,
        ),
        lageOptions = lageOptions,
        direction = direction,
        sources = sources,
        flow = flow,
    )
}
