package com.struperto.androidappdays.feature.start

import com.google.gson.GsonBuilder
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaSourceSetupStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class AreaAnalysisSectionState(
    val label: String = "",
    val title: String = "",
    val detail: String = "",
    val supportingLabel: String = "",
)

data class AreaAnalysisEvidenceState(
    val label: String = "",
    val stateLabel: String = "",
    val detail: String = "",
)

data class AreaAnalysisSignalState(
    val label: String = "",
    val value: String = "",
    val detail: String = "",
)

data class AreaMachineAnalysisState(
    val snapshotVersion: String = "2",
    val typeLabel: String = "Offen",
    val routeLabel: String = "Manuell",
    val statusLabel: String = "Offen",
    val summary: String = "",
    val confidence: Float = 0f,
    val confidenceLabel: String = "",
    val nextGoalLabel: String = "",
    val nextGoalDetail: String = "",
    val capabilityLabels: List<String> = emptyList(),
    val currentState: AreaAnalysisSectionState = AreaAnalysisSectionState(),
    val goalState: AreaAnalysisSectionState = AreaAnalysisSectionState(),
    val inputState: AreaAnalysisSectionState = AreaAnalysisSectionState(),
    val analysisState: AreaAnalysisSectionState = AreaAnalysisSectionState(),
    val evidenceItems: List<AreaAnalysisEvidenceState> = emptyList(),
    val signalItems: List<AreaAnalysisSignalState> = emptyList(),
    val machinePayload: String = "",
    val isRefreshing: Boolean = false,
    val refreshReasonLabel: String = "",
    val lastAnalyzedAtLabel: String = "",
)

data class AreaMachineAnalysisRuntime(
    val isRefreshing: Boolean = false,
    val refreshedAt: Instant? = null,
    val refreshReason: String = "",
)

private data class AreaMachinePayloadSection(
    val label: String,
    val title: String,
    val detail: String,
    val supporting: String,
)

private data class AreaMachinePayloadEvidence(
    val label: String,
    val state: String,
    val detail: String,
)

private data class AreaMachinePayloadSignal(
    val label: String,
    val value: String,
    val detail: String,
)

private data class AreaMachinePayloadV2(
    val analysisVersion: String,
    val areaId: String,
    val title: String,
    val type: String,
    val family: String,
    val route: String,
    val status: String,
    val confidence: Float,
    val currentState: AreaMachinePayloadSection,
    val goal: AreaMachinePayloadSection,
    val inputs: AreaMachinePayloadSection,
    val analysis: AreaMachinePayloadSection,
    val evidence: List<AreaMachinePayloadEvidence>,
    val signals: List<AreaMachinePayloadSignal>,
    val runtime: Map<String, Boolean>,
    val source: Map<String, String>,
    val materials: Map<String, Int>,
    val refreshedAt: String?,
)

fun projectAreaMachineAnalysis(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    runtime: AreaMachineAnalysisRuntime,
    zoneId: ZoneId,
): AreaMachineAnalysisState {
    val typeLabel = area.family.shortLabel
    val capabilityLabels = buildList {
        if (sourceSetup != null) add(sourceKindLabel(sourceSetup.sourceKind))
        if (area.runtimeContract.capabilitySet.canClusterLocally) add("Cluster")
        if (area.runtimeContract.capabilitySet.canSummarizeLocally) add("Kurztext")
        if (area.runtimeContract.capabilitySet.canSearchLocally) add("Suche")
        if (area.runtimeContract.capabilitySet.canSyncWebFeeds) add("Feeds")
        if (importedMaterials.isNotEmpty()) add("Material")
    }.distinct()

    val baseConfidence = when (sourceSetup?.status) {
        AreaSourceSetupStatus.READY -> 0.9f
        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> 0.74f
        AreaSourceSetupStatus.PERMISSION_REQUIRED -> 0.62f
        AreaSourceSetupStatus.UNCONFIGURED -> 0.55f
        null -> 0.48f
    }
    val materialBoost = importedMaterials.size.coerceAtMost(3) * 0.06f
    val feedBoost = if (webFeedSync.sources.isNotEmpty()) 0.04f else 0f
    val flowBoost = if (area.flowCount > 0) 0.03f else 0f
    val confidence = (baseConfidence + materialBoost + feedBoost + flowBoost).coerceIn(0.18f, 0.98f)

    val routeLabel = when (sourceSetup?.sourceKind) {
        DataSourceKind.CALENDAR -> "Kalender"
        DataSourceKind.NOTIFICATIONS -> "Benachrichtigungen"
        DataSourceKind.HEALTH_CONNECT -> "Health Connect"
        DataSourceKind.MANUAL,
        null,
        -> if (area.family == StartAreaFamily.Routine || area.family == StartAreaFamily.Ort) {
            "Alltagssystem"
        } else {
            "Manuell"
        }
    }

    val statusLabel = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Lesbar"
        sourceSetup?.status == AreaSourceSetupStatus.PERMISSION_REQUIRED -> "Freigabe offen"
        sourceSetup?.status == AreaSourceSetupStatus.UNCONFIGURED -> "Quelle offen"
        importedMaterials.isNotEmpty() -> "Material da"
        else -> "Offen"
    }

    val nextGoalLabel = when {
        sourceSetup?.status == AreaSourceSetupStatus.PERMISSION_REQUIRED -> sourceSetup.primaryActionLabel ?: "Freigeben"
        sourceSetup?.status == AreaSourceSetupStatus.UNCONFIGURED -> sourceSetup.primaryActionLabel ?: "Verbinden"
        importedMaterials.isEmpty() && webFeedSync.sources.isEmpty() -> "1 Eingang holen"
        area.flowCount == 0 -> "1 Routine setzen"
        else -> "Analyse schaerfen"
    }
    val nextGoalDetail = when {
        sourceSetup?.status == AreaSourceSetupStatus.PERMISSION_REQUIRED -> sourceSetup.detail
        sourceSetup?.status == AreaSourceSetupStatus.UNCONFIGURED -> sourceSetup.detail
        importedMaterials.isEmpty() && webFeedSync.sources.isEmpty() ->
            "Ein Link, ein Dokument oder eine echte Spur macht den Bereich sofort belastbarer."
        area.flowCount == 0 ->
            "Mit Review, Erinnerung oder Experiment wird das Verhalten dieses Bereichs klarer."
        else ->
            "Jetzt kann Days diesen Bereich iterativ ueber Material, Zielbild und Kurztexte schaerfen."
    }

    val currentState = buildCurrentStateSection(
        area = area,
        sourceSetup = sourceSetup,
        importedMaterials = importedMaterials,
        statusLabel = statusLabel,
        confidence = confidence,
    )
    val goalState = buildGoalStateSection(
        area = area,
        nextGoalLabel = nextGoalLabel,
    )
    val inputState = buildInputStateSection(
        sourceSetup = sourceSetup,
        importedMaterials = importedMaterials,
        webFeedSync = webFeedSync,
    )
    val analysisState = buildAnalysisStateSection(
        routeLabel = routeLabel,
        nextGoalLabel = nextGoalLabel,
        nextGoalDetail = nextGoalDetail,
        confidence = confidence,
        runtime = runtime,
    )
    val evidenceItems = buildEvidenceItems(
        area = area,
        sourceSetup = sourceSetup,
        importedMaterials = importedMaterials,
        webFeedSync = webFeedSync,
    )
    val signalItems = buildSignalItems(
        area = area,
        confidence = confidence,
        nextGoalLabel = nextGoalLabel,
        sourceSetup = sourceSetup,
        importedMaterials = importedMaterials,
        webFeedSync = webFeedSync,
        runtime = runtime,
    )

    val summary = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY ->
            "$routeLabel liefert bereits belastbare Eingangssignale fuer diesen Bereich."
        sourceSetup?.status == AreaSourceSetupStatus.PERMISSION_REQUIRED ->
            "Routing steht. Fuer echte Eingangssignale fehlt nur noch die Freigabe."
        sourceSetup?.status == AreaSourceSetupStatus.UNCONFIGURED ->
            "Routing steht. Diese Spur ist geplant, aber noch nicht verbunden."
        importedMaterials.isNotEmpty() ->
            "Der Bereich kann schon mit Material arbeiten, auch ohne feste Systemspur."
        else ->
            "Der Bereich startet als Alltagssystem und braucht jetzt eine erste belastbare Spur."
    }

    return AreaMachineAnalysisState(
        typeLabel = typeLabel,
        routeLabel = routeLabel,
        statusLabel = statusLabel,
        summary = summary,
        confidence = confidence,
        confidenceLabel = areaAnalysisConfidenceLabel(confidence),
        nextGoalLabel = nextGoalLabel,
        nextGoalDetail = nextGoalDetail,
        capabilityLabels = capabilityLabels,
        currentState = currentState,
        goalState = goalState,
        inputState = inputState,
        analysisState = analysisState,
        evidenceItems = evidenceItems,
        signalItems = signalItems,
        machinePayload = buildAreaMachinePayload(
            area = area,
            typeLabel = typeLabel,
            routeLabel = routeLabel,
            statusLabel = statusLabel,
            confidence = confidence,
            currentState = currentState,
            goalState = goalState,
            inputState = inputState,
            analysisState = analysisState,
            evidenceItems = evidenceItems,
            signalItems = signalItems,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
            webFeedSync = webFeedSync,
            refreshedAt = runtime.refreshedAt,
            zoneId = zoneId,
        ),
        isRefreshing = runtime.isRefreshing,
        refreshReasonLabel = runtime.refreshReason.ifBlank { "screen_open" },
        lastAnalyzedAtLabel = runtime.refreshedAt?.atZone(zoneId)
            ?.format(DateTimeFormatter.ofPattern("HH:mm"))
            ?.let { "Zuletzt $it" }
            .orEmpty(),
    )
}

private fun buildCurrentStateSection(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    statusLabel: String,
    confidence: Float,
): AreaAnalysisSectionState {
    val title = when {
        area.profileState.lageMode == AreaLageMode.State -> area.todayOutput.statusLabel
        sourceSetup?.status == AreaSourceSetupStatus.READY -> area.todayOutput.statusLabel
        importedMaterials.isNotEmpty() -> "Material lesbar"
        else -> "Noch offen"
    }
    val detail = when {
        area.profileState.lageMode == AreaLageMode.State ->
            "Dieser Bereich lebt derzeit von gesetzten Zustaenden und bewusster Pflege."
        sourceSetup?.status == AreaSourceSetupStatus.READY ->
            area.todayRecommendation
        importedMaterials.isNotEmpty() ->
            "Lokales Material liefert bereits genug Stoff fuer einen ersten belastbaren Zustand."
        else ->
            "Noch keine stabile Spur. Days zeigt deshalb nur einen vorlaeufigen Zustand."
    }
    return AreaAnalysisSectionState(
        label = "Aktueller Zustand",
        title = title,
        detail = detail,
        supportingLabel = "${statusLabel} · ${areaAnalysisConfidenceLabel(confidence)}",
    )
}

private fun buildGoalStateSection(
    area: StartAreaDetailState,
    nextGoalLabel: String,
): AreaAnalysisSectionState {
    val title = area.focusTrack.ifBlank { "Ziel offen" }
    val detail = buildString {
        append("Heute fuehrt ")
        append(title)
        append(". ")
        append(area.profileState.directionLabel)
        append(" · Zielscore ")
        append(area.targetScore)
        append("/5.")
    }
    return AreaAnalysisSectionState(
        label = "Ziel",
        title = title,
        detail = detail,
        supportingLabel = nextGoalLabel,
    )
}

private fun buildInputStateSection(
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): AreaAnalysisSectionState {
    val sourceTitle = sourceSetup?.let { sourceKindLabel(it.sourceKind) } ?: "Noch frei"
    val materialCount = importedMaterials.size
    val feedCount = webFeedSync.sources.size
    val detail = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY ->
            "${sourceSetup.headline} ist verbunden. $materialCount Import${if (materialCount == 1) "" else "e"} und $feedCount Feed${if (feedCount == 1) "" else "s"} erweitern den Eingang."
        sourceSetup != null ->
            "${sourceSetup.detail} $materialCount Import${if (materialCount == 1) "" else "e"} liegen bereits lokal bereit."
        materialCount > 0 || feedCount > 0 ->
            "$materialCount Import${if (materialCount == 1) "" else "e"} und $feedCount Feed${if (feedCount == 1) "" else "s"} geben dem Bereich schon einen ersten Materialpfad."
        else ->
            "Noch kein klarer Materialpfad. Ein Link, Bild, Dokument oder eine Quelle reicht fuer den Start."
    }
    val supporting = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Verbunden"
        materialCount > 0 || feedCount > 0 -> "Lokal bereit"
        else -> "Noch offen"
    }
    return AreaAnalysisSectionState(
        label = "Hinzufuegen",
        title = sourceTitle,
        detail = detail,
        supportingLabel = supporting,
    )
}

private fun buildAnalysisStateSection(
    routeLabel: String,
    nextGoalLabel: String,
    nextGoalDetail: String,
    confidence: Float,
    runtime: AreaMachineAnalysisRuntime,
): AreaAnalysisSectionState {
    val refreshLabel = runtime.refreshReason.ifBlank { "screen_open" }
    return AreaAnalysisSectionState(
        label = "Analyse",
        title = nextGoalLabel,
        detail = nextGoalDetail,
        supportingLabel = "$routeLabel · ${areaAnalysisConfidenceLabel(confidence)} · $refreshLabel",
    )
}

private fun buildEvidenceItems(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<AreaAnalysisEvidenceState> {
    val linkCount = importedMaterials.count { it.kind == AreaImportKind.Link }
    val textCount = importedMaterials.count { it.kind == AreaImportKind.Text }
    val imageCount = importedMaterials.count { it.kind == AreaImportKind.Image }
    val fileCount = importedMaterials.count { it.kind == AreaImportKind.File }
    val socialWebActive = hasSocialOrNewsContext(importedMaterials, webFeedSync)
    return listOf(
        evidenceItem(
            label = "Text",
            active = textCount > 0,
            ready = area.runtimeContract.capabilitySet.canSummarizeLocally,
            activeDetail = "$textCount Textspur${if (textCount == 1) "" else "en"} liegen schon lokal vor.",
            readyDetail = "Text kann spaeter lokal verdichtet und fuer Rueckfragen genutzt werden.",
            openDetail = "Noch keine Textspur im Bereich.",
        ),
        evidenceItem(
            label = "Dateien",
            active = fileCount > 0,
            ready = true,
            activeDetail = "$fileCount Datei${if (fileCount == 1) "" else "en"} stehen fuer spaetere Analyse bereit.",
            readyDetail = "Dateien koennen jederzeit in diesen Bereich geholt werden.",
            openDetail = "Noch keine Datei im Bereich.",
        ),
        evidenceItem(
            label = "Bilder",
            active = imageCount > 0,
            ready = true,
            activeDetail = "$imageCount Bild${if (imageCount == 1) "" else "er"} oder Screenshot${if (imageCount == 1) "" else "s"} liegen vor.",
            readyDetail = "Bilder koennen jederzeit lokal hinzugefuegt werden.",
            openDetail = "Noch kein Bild oder Screenshot im Bereich.",
        ),
        evidenceItem(
            label = "Web",
            active = linkCount > 0 || webFeedSync.sources.isNotEmpty(),
            ready = area.runtimeContract.capabilitySet.canSyncWebFeeds,
            activeDetail = "Links oder Feeds geben diesem Bereich bereits Web-Kontext.",
            readyDetail = "Web-Quellen und Feeds koennen hier spaeter automatisch nachladen.",
            openDetail = "Noch kein Link oder Feed verbunden.",
        ),
        evidenceItem(
            label = "Kalender",
            active = sourceSetup?.sourceKind == DataSourceKind.CALENDAR,
            ready = sourceSetup == null || sourceSetup.sourceKind == DataSourceKind.CALENDAR,
            activeDetail = "Kalender ist die aktive Primarquelle fuer diesen Bereich.",
            readyDetail = "Kalender kann als Primarquelle geschaltet werden.",
            openDetail = "Kalender spielt in diesem Bereich aktuell keine aktive Rolle.",
        ),
        evidenceItem(
            label = "Benachrichtigungen",
            active = sourceSetup?.sourceKind == DataSourceKind.NOTIFICATIONS,
            ready = sourceSetup == null || sourceSetup.sourceKind == DataSourceKind.NOTIFICATIONS,
            activeDetail = "Benachrichtigungen liefern die aktiven Eingangssignale.",
            readyDetail = "Benachrichtigungen koennen spaeter als Primarquelle dienen.",
            openDetail = "Benachrichtigungen sind hier noch nicht aktiv.",
        ),
        evidenceItem(
            label = "Health",
            active = sourceSetup?.sourceKind == DataSourceKind.HEALTH_CONNECT,
            ready = sourceSetup == null || sourceSetup.sourceKind == DataSourceKind.HEALTH_CONNECT,
            activeDetail = "Health Connect liefert die aktiven Eingangssignale.",
            readyDetail = "Health Connect kann spaeter als Primarquelle dienen.",
            openDetail = "Health Connect ist hier noch nicht aktiv.",
        ),
        evidenceItem(
            label = "Kontakte",
            active = area.family == StartAreaFamily.Kontakt,
            ready = true,
            activeDetail = "Dieser Bereich ist semantisch bereits auf Menschen und Beziehungen ausgerichtet.",
            readyDetail = "Kontakte koennen spaeter ueber Imports oder manuelle Signale dazukommen.",
            openDetail = "Kontakte sind hier kein aktiver Schwerpunkt.",
        ),
        evidenceItem(
            label = "Ort",
            active = area.family == StartAreaFamily.Ort,
            ready = true,
            activeDetail = "Dieser Bereich ist bereits als Ort- oder Alltagsraum markiert.",
            readyDetail = "Ortsbezug kann spaeter ueber Material oder Kontext entstehen.",
            openDetail = "Ort ist hier kein aktiver Schwerpunkt.",
        ),
        evidenceItem(
            label = "Social/News",
            active = socialWebActive,
            ready = webFeedSync.sources.isNotEmpty() || linkCount > 0,
            activeDetail = "Die vorhandenen Links oder Feeds deuten bereits auf Social-, Video- oder News-Material.",
            readyDetail = "Sobald Web-Material dazukommt, kann Days Social- und News-Kontext erkennen.",
            openDetail = "Noch kein Social- oder News-Kontext im Bereich.",
        ),
    )
}

private fun buildSignalItems(
    area: StartAreaDetailState,
    confidence: Float,
    nextGoalLabel: String,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    runtime: AreaMachineAnalysisRuntime,
): List<AreaAnalysisSignalState> {
    val dailyUseScore = (
        confidence * 0.45f +
            if (area.flowCount > 0) 0.2f else 0f +
            if (sourceSetup?.status == AreaSourceSetupStatus.READY) 0.2f else 0f +
            if (importedMaterials.isNotEmpty() || webFeedSync.sources.isNotEmpty()) 0.15f else 0f
        ).coerceIn(0f, 1f)
    val dailyUseValue = when {
        dailyUseScore >= 0.82f -> "Stark"
        dailyUseScore >= 0.62f -> "Brauchbar"
        else -> "Offen"
    }
    val learningValue = if (runtime.refreshedAt != null) "Aktiv" else "Neu"
    val automationValue = when {
        area.flowCount > 1 -> "Laeuft"
        area.flowCount == 1 -> "Klein"
        else -> "Noch ruhig"
    }
    return listOf(
        AreaAnalysisSignalState(
            label = "Taegliche Nutzung",
            value = dailyUseValue,
            detail = "North-Star fuer diesen Bereich. Der aktuelle Readiness-Score liegt bei ${(dailyUseScore * 100).roundToInt()}%.",
        ),
        AreaAnalysisSignalState(
            label = "Naechster Schritt",
            value = nextGoalLabel,
            detail = "Days sollte als naechstes genau diesen Schritt absichern, bevor weitere Logik dazukommt.",
        ),
        AreaAnalysisSignalState(
            label = "Lernschleife",
            value = learningValue,
            detail = "Snapshot ${if (runtime.refreshedAt != null) "wurde bereits neu gerechnet" else "startet noch frisch"} und ist versioniert.",
        ),
        AreaAnalysisSignalState(
            label = "Automatik",
            value = automationValue,
            detail = "Reviews, Erinnerungen oder Experimente entscheiden, wie schnell dieser Bereich taeglich greift.",
        ),
    )
}

private fun buildAreaMachinePayload(
    area: StartAreaDetailState,
    typeLabel: String,
    routeLabel: String,
    statusLabel: String,
    confidence: Float,
    currentState: AreaAnalysisSectionState,
    goalState: AreaAnalysisSectionState,
    inputState: AreaAnalysisSectionState,
    analysisState: AreaAnalysisSectionState,
    evidenceItems: List<AreaAnalysisEvidenceState>,
    signalItems: List<AreaAnalysisSignalState>,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    refreshedAt: Instant?,
    zoneId: ZoneId,
): String {
    val payload = AreaMachinePayloadV2(
        analysisVersion = "2",
        areaId = area.areaId,
        title = area.title,
        type = typeLabel,
        family = area.family.label,
        route = routeLabel,
        status = statusLabel,
        confidence = confidence,
        currentState = currentState.toPayloadSection(),
        goal = goalState.toPayloadSection(),
        inputs = inputState.toPayloadSection(),
        analysis = analysisState.toPayloadSection(),
        evidence = evidenceItems.map(AreaAnalysisEvidenceState::toPayloadEvidence),
        signals = signalItems.map(AreaAnalysisSignalState::toPayloadSignal),
        runtime = mapOf(
            "canClusterLocally" to area.runtimeContract.capabilitySet.canClusterLocally,
            "canSummarizeLocally" to area.runtimeContract.capabilitySet.canSummarizeLocally,
            "canSearchLocally" to area.runtimeContract.capabilitySet.canSearchLocally,
            "canSyncWebFeeds" to area.runtimeContract.capabilitySet.canSyncWebFeeds,
        ),
        source = mapOf(
            "kind" to (sourceSetup?.sourceKind?.name ?: "MANUAL"),
            "status" to (sourceSetup?.status?.name ?: "MANUAL"),
            "headline" to (sourceSetup?.headline ?: "Keine feste Quelle"),
        ),
        materials = mapOf(
            "importCount" to importedMaterials.size,
            "linkCount" to importedMaterials.count { it.kind == AreaImportKind.Link },
            "imageCount" to importedMaterials.count { it.kind == AreaImportKind.Image },
            "fileCount" to importedMaterials.count { it.kind == AreaImportKind.File },
            "textCount" to importedMaterials.count { it.kind == AreaImportKind.Text },
            "feedSourceCount" to webFeedSync.sources.size,
            "autoSyncCount" to webFeedSync.sources.count(AreaWebFeedSourceState::autoSyncEnabled),
        ),
        refreshedAt = refreshedAt?.atZone(zoneId)?.toString(),
    )
    return GsonBuilder().setPrettyPrinting().create().toJson(payload)
}

private fun AreaAnalysisSectionState.toPayloadSection(): AreaMachinePayloadSection {
    return AreaMachinePayloadSection(
        label = label,
        title = title,
        detail = detail,
        supporting = supportingLabel,
    )
}

private fun AreaAnalysisEvidenceState.toPayloadEvidence(): AreaMachinePayloadEvidence {
    return AreaMachinePayloadEvidence(
        label = label,
        state = stateLabel,
        detail = detail,
    )
}

private fun AreaAnalysisSignalState.toPayloadSignal(): AreaMachinePayloadSignal {
    return AreaMachinePayloadSignal(
        label = label,
        value = value,
        detail = detail,
    )
}

private fun evidenceItem(
    label: String,
    active: Boolean,
    ready: Boolean,
    activeDetail: String,
    readyDetail: String,
    openDetail: String,
): AreaAnalysisEvidenceState {
    val stateLabel = when {
        active -> "Aktiv"
        ready -> "Bereit"
        else -> "Offen"
    }
    val detail = when {
        active -> activeDetail
        ready -> readyDetail
        else -> openDetail
    }
    return AreaAnalysisEvidenceState(
        label = label,
        stateLabel = stateLabel,
        detail = detail,
    )
}

private fun hasSocialOrNewsContext(
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): Boolean {
    val contextText = buildString {
        importedMaterials.forEach { item ->
            append(item.title)
            append(' ')
            append(item.reference)
            append(' ')
        }
        webFeedSync.sources.forEach { source ->
            append(source.hostLabel)
            append(' ')
            append(source.url)
            append(' ')
        }
    }.lowercase()
    return listOf(
        "youtube",
        "youtu.be",
        "instagram",
        "insta",
        "x.com",
        "twitter",
        "reddit",
        "news",
        "rss",
        "substack",
        "tiktok",
        "linkedin",
    ).any(contextText::contains)
}

private fun areaAnalysisConfidenceLabel(confidence: Float): String {
    return when {
        confidence >= 0.85f -> "Sehr klar"
        confidence >= 0.7f -> "Klar"
        confidence >= 0.55f -> "Brauchbar"
        else -> "Noch offen"
    }
}

private fun sourceKindLabel(sourceKind: DataSourceKind): String {
    return when (sourceKind) {
        DataSourceKind.CALENDAR -> "Kalender"
        DataSourceKind.NOTIFICATIONS -> "Benachrichtigungen"
        DataSourceKind.HEALTH_CONNECT -> "Health Connect"
        DataSourceKind.MANUAL -> "Manuell"
    }
}
