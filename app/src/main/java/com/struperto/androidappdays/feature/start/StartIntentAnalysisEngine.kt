package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaBehaviorClass

internal enum class StartIntentFamily {
    CALENDAR,
    NOTIFICATIONS,
    HEALTH,
    ROUTINE,
    LOCATION,
    READING,
    WRITING,
    ADMIN,
    SHOPPING,
    FINANCE,
    BOOKMARKS,
    WEB_CONTENT,
    FILES,
    SCREENSHOTS,
    PHOTOS,
    EMAIL,
    APP_CONTENT,
    GENERAL,
}

internal enum class StartIntentOutcome {
    DIRECT_SOURCE,
    MANUAL_AREA,
    BLOCKED,
}

internal data class StartIntentAnalysis(
    val statusLabel: String,
    val headline: String,
    val readinessLabel: String,
    val readinessDetail: String,
    val worksNowTitle: String,
    val worksNowDetail: String,
    val sourceLabel: String,
    val sourceDetail: String,
    val suggestedTitle: String,
    val suggestedSummary: String,
    val suggestedTemplateId: String,
    val suggestedIconKey: String,
    val suggestedBehaviorClass: AreaBehaviorClass,
    val suggestedSourceKind: DataSourceKind?,
    val selectableSources: List<DataSourceKind>,
    val deviceFindings: List<String>,
    val canCreate: Boolean,
    val blockingReason: String? = null,
    val implementationNote: String? = null,
    val aiSummaryLabel: String? = null,
    val aiSummaryDetail: String? = null,
    val confidence: Float = 0f,
    val confidenceLabel: String = "",
    val followUpQuestion: String? = null,
    val followUpOptions: List<StartIntentFollowUpOption> = emptyList(),
    val repairQuestion: String? = null,
    val repairOptions: List<StartIntentRepairOption> = emptyList(),
    val sourceRows: List<StartAnalysisSourceRow> = emptyList(),
    val family: StartIntentFamily,
    val outcome: StartIntentOutcome,
)

internal data class StartAnalysisSourceRow(
    val name: String,
    val hint: String,
    val status: String,
    val actionLabel: String? = null,
)

internal data class StartIntentFollowUpOption(
    val id: String,
    val label: String,
    val supportingLabel: String,
    val draft: CreateAreaDraft,
)

internal data class StartIntentRepairOption(
    val id: String,
    val label: String,
    val supportingLabel: String,
    val replacementInput: String,
)

private data class StartIntentSpec(
    val family: StartIntentFamily,
    val title: String,
    val summary: String,
    val templateId: String,
    val iconKey: String,
    val behaviorClass: AreaBehaviorClass,
    val sourceKind: DataSourceKind? = null,
    val sourceLabel: String,
    val sourceDetail: String,
    val outcome: StartIntentOutcome,
    val headline: String,
    val readinessWhenReady: String,
    val readinessWhenBlocked: String,
    val blockingReason: String? = null,
    val implementationNote: String? = null,
)

internal fun buildPrimaryCreateDraft(
    rawInput: String,
): CreateAreaDraft {
    val spec = interpretStartIntent(rawInput).withResolvedTitle(rawInput)
    return CreateAreaDraft(
        title = spec.title,
        meaning = spec.summary,
        templateId = spec.templateId,
        iconKey = spec.iconKey,
        behaviorClass = spec.behaviorClass,
        sourceKind = spec.sourceKind,
    )
}

internal fun analyzeStartIntent(
    rawInput: String,
    capabilityProfile: CapabilityProfile?,
    browserApps: List<String> = emptyList(),
    installedPackages: Set<String> = emptySet(),
    aiSignals: StartPlatformAiSignals = StartPlatformAiSignals(),
): StartIntentAnalysis {
    val spec = interpretStartIntent(rawInput, aiSignals).withResolvedTitle(rawInput)
    val sourceCapability = spec.sourceKind?.let { capabilityProfile?.find(it) }
    val selectableSources = listOfNotNull(spec.sourceKind)
    val deviceFindings = buildDeviceFindings(
        spec = spec,
        capabilityProfile = capabilityProfile,
        browserApps = browserApps,
    )
    val sourceStateLabel = sourceCapability?.let {
        capabilityStateLabel(it.enabled, it.available, it.granted)
    }
    val sourceDetail = when {
        spec.outcome == StartIntentOutcome.BLOCKED -> spec.sourceDetail
        sourceCapability == null -> spec.sourceDetail
        sourceCapability.enabled && sourceCapability.available && sourceCapability.granted ->
            "${spec.sourceDetail} ${sourceCapability.detail}"
        else -> "${spec.sourceDetail} ${sourceCapability.detail}"
    }
    val statusLabel = when (spec.outcome) {
        StartIntentOutcome.BLOCKED -> "Noch nicht moeglich"
        StartIntentOutcome.MANUAL_AREA -> if ("Import" in spec.sourceLabel) {
            "Mit Import moeglich"
        } else {
            "Als Bereich moeglich"
        }
        StartIntentOutcome.DIRECT_SOURCE -> when {
            sourceCapability == null -> "Direkt moeglich"
            sourceCapability.enabled && sourceCapability.available && sourceCapability.granted -> "Direkt moeglich"
            else -> "Quelle noch offen"
        }
    }
    val readinessLabel = when (spec.outcome) {
        StartIntentOutcome.BLOCKED -> "Jetzt"
        StartIntentOutcome.MANUAL_AREA -> "Jetzt"
        StartIntentOutcome.DIRECT_SOURCE -> if (sourceStateLabel == null) "Jetzt" else "Quelle"
    }
    val readinessDetail = when (spec.outcome) {
        StartIntentOutcome.BLOCKED -> spec.readinessWhenBlocked
        StartIntentOutcome.MANUAL_AREA -> spec.readinessWhenReady
        StartIntentOutcome.DIRECT_SOURCE -> when {
            sourceCapability == null -> spec.readinessWhenReady
            sourceCapability.enabled && sourceCapability.available && sourceCapability.granted -> spec.readinessWhenReady
            else -> "Ich kann den Bereich anlegen. Fuer echte ${spec.sourceLabel.lowercase()}-Signale fehlt noch die Freigabe."
        }
    }
    val worksNowTitle = when (spec.outcome) {
        StartIntentOutcome.BLOCKED -> "Direkt geht das noch nicht."
        StartIntentOutcome.MANUAL_AREA -> "Geht jetzt: Bereich direkt anlegen."
        StartIntentOutcome.DIRECT_SOURCE -> when {
            sourceCapability == null -> "Geht jetzt: Bereich direkt starten."
            sourceCapability.enabled && sourceCapability.available && sourceCapability.granted ->
                "Geht jetzt: Bereich mit ${spec.sourceLabel.lowercase()} verbinden."
            else -> "Geht jetzt: Bereich anlegen, Quelle spaeter freigeben."
        }
    }
    val worksNowDetail = when (spec.outcome) {
        StartIntentOutcome.BLOCKED -> spec.readinessWhenBlocked
        StartIntentOutcome.MANUAL_AREA -> spec.readinessWhenReady
        StartIntentOutcome.DIRECT_SOURCE -> when {
            sourceCapability == null -> spec.readinessWhenReady
            sourceCapability.enabled && sourceCapability.available && sourceCapability.granted -> spec.readinessWhenReady
            else -> "Der Bereich selbst kann sofort entstehen. Fuer laufende ${spec.sourceLabel.lowercase()}-Signale braucht Days danach noch die passende Freigabe."
        }
    }
    val confidence = startIntentConfidence(
        spec = spec,
        sourceCapability = sourceCapability,
        rawInput = rawInput,
    )
    val followUp = buildStartIntentFollowUp(
        spec = spec,
        rawInput = rawInput,
    )
    val repair = buildStartIntentRepairOptions(
        spec = spec,
        rawInput = rawInput,
    )
    val sourceRows = buildStartAnalysisSourceRows(
        spec = spec,
        rawInput = rawInput,
        sourceCapability = sourceCapability,
        browserApps = browserApps,
        installedPackages = installedPackages,
    )
    return StartIntentAnalysis(
        statusLabel = statusLabel,
        headline = spec.headline,
        readinessLabel = readinessLabel,
        readinessDetail = readinessDetail,
        worksNowTitle = worksNowTitle,
        worksNowDetail = worksNowDetail,
        sourceLabel = when {
            sourceStateLabel != null -> "${spec.sourceLabel} · $sourceStateLabel"
            else -> spec.sourceLabel
        },
        sourceDetail = sourceDetail,
        suggestedTitle = spec.title,
        suggestedSummary = spec.summary,
        suggestedTemplateId = spec.templateId,
        suggestedIconKey = spec.iconKey,
        suggestedBehaviorClass = spec.behaviorClass,
        suggestedSourceKind = spec.sourceKind,
        selectableSources = selectableSources,
        deviceFindings = deviceFindings,
        canCreate = spec.outcome != StartIntentOutcome.BLOCKED,
        blockingReason = spec.blockingReason,
        implementationNote = spec.implementationNote,
        aiSummaryLabel = aiSignals.summaryLabel,
        aiSummaryDetail = aiSignals.summaryDetail,
        confidence = confidence,
        confidenceLabel = startIntentConfidenceLabel(confidence),
        followUpQuestion = followUp.first,
        followUpOptions = followUp.second,
        repairQuestion = repair.first,
        repairOptions = repair.second,
        sourceRows = sourceRows,
        family = spec.family,
        outcome = spec.outcome,
    )
}

private fun startIntentConfidence(
    spec: StartIntentSpec,
    sourceCapability: com.struperto.androidappdays.domain.DataSourceCapability?,
    rawInput: String,
): Float {
    val lower = rawInput.lowercase()
    val base = when (spec.outcome) {
        StartIntentOutcome.BLOCKED -> 0.82f
        StartIntentOutcome.MANUAL_AREA -> 0.66f
        StartIntentOutcome.DIRECT_SOURCE -> when {
            sourceCapability == null -> 0.78f
            sourceCapability.enabled && sourceCapability.available && sourceCapability.granted -> 0.92f
            else -> 0.74f
        }
    }
    val ambiguityPenalty = when {
        containsAny(lower, "ruhig", "organisiert", "blick behalten", "nicht uebersehen") -> 0.08f
        containsAny(lower, "morgen", "abends", "abend", "heute") && spec.sourceKind == null -> 0.06f
        else -> 0f
    }
    return (base - ambiguityPenalty).coerceIn(0.24f, 0.98f)
}

private fun startIntentConfidenceLabel(confidence: Float): String {
    return when {
        confidence >= 0.88f -> "Sehr klar"
        confidence >= 0.74f -> "Klar"
        confidence >= 0.58f -> "Brauchbar"
        else -> "Rueckfrage gut"
    }
}

private fun buildStartIntentFollowUp(
    spec: StartIntentSpec,
    rawInput: String,
): Pair<String?, List<StartIntentFollowUpOption>> {
    val lower = rawInput.lowercase()
    val current = CreateAreaDraft(
        title = spec.title,
        meaning = spec.summary,
        templateId = spec.templateId,
        iconKey = spec.iconKey,
        behaviorClass = spec.behaviorClass,
        sourceKind = spec.sourceKind,
    )
    return when {
        spec.outcome == StartIntentOutcome.DIRECT_SOURCE -> {
            val manualFallback = current.copy(sourceKind = null)
            "Soll Days hier sofort eine echte Spur vorbereiten oder erst ruhig manuell starten?" to listOf(
                StartIntentFollowUpOption(
                    id = "direct",
                    label = spec.sourceLabel,
                    supportingLabel = "Direkt mit echter Spur",
                    draft = current,
                ),
                StartIntentFollowUpOption(
                    id = "manual",
                    label = "Manuell",
                    supportingLabel = "Ohne feste Quelle starten",
                    draft = manualFallback,
                ),
            )
        }

        spec.family in setOf(
            StartIntentFamily.ROUTINE,
            StartIntentFamily.LOCATION,
            StartIntentFamily.SHOPPING,
            StartIntentFamily.ADMIN,
        ) && containsAny(lower, "morgen", "heute", "abend", "termine", "zeit") -> {
            "Soll das ein ruhiges Alltagssystem bleiben oder spaeter am Kalender haengen?" to listOf(
                StartIntentFollowUpOption(
                    id = "manual",
                    label = "Alltagssystem",
                    supportingLabel = "Ohne feste Quelle",
                    draft = current,
                ),
                StartIntentFollowUpOption(
                    id = "calendar",
                    label = "Kalender",
                    supportingLabel = "Zeitfenster spaeter mitlesen",
                    draft = current.copy(sourceKind = DataSourceKind.CALENDAR),
                ),
            )
        }

        else -> null to emptyList()
    }
}

private fun buildStartIntentRepairOptions(
    spec: StartIntentSpec,
    rawInput: String,
): Pair<String?, List<StartIntentRepairOption>> {
    if (spec.outcome != StartIntentOutcome.BLOCKED) return null to emptyList()
    val lower = rawInput.lowercase()
    return when (spec.family) {
        StartIntentFamily.BOOKMARKS -> {
            "Direkter Lesezeichenzugriff fehlt noch. Ich kann den Flow aber auf einen gangbaren Signalpfad drehen." to listOf(
                StartIntentRepairOption(
                    id = "bookmark-export",
                    label = "HTML-Export",
                    supportingLabel = "Lesezeichen als Datei importieren",
                    replacementInput = "Importiere meine Chrome-Lesezeichen als HTML-Datei.",
                ),
                StartIntentRepairOption(
                    id = "bookmark-share",
                    label = "Links teilen",
                    supportingLabel = "Einzelne Browser-Links direkt sammeln",
                    replacementInput = "Ich will einzelne Browser-Links hierher teilen.",
                ),
            )
        }

        StartIntentFamily.EMAIL -> {
            "Direktes Postfachlesen fehlt noch. Ich kann stattdessen schon einen klaren Mail-Signalpfad vorbereiten." to listOf(
                StartIntentRepairOption(
                    id = "mail-share",
                    label = "Mails teilen",
                    supportingLabel = "Einzelne Mail-Inhalte weiterleiten",
                    replacementInput = "Ich will dir einzelne Mails weiterleiten und hier sammeln.",
                ),
                StartIntentRepairOption(
                    id = "mail-file",
                    label = "EML-Datei",
                    supportingLabel = "Mail-Dateien direkt importieren",
                    replacementInput = "Lies diese EML-Datei aus meinem Postfach.",
                ),
            )
        }

        StartIntentFamily.APP_CONTENT -> {
            val appLabel = detectIntentTitle(rawInput.trim()).ifBlank {
                if (containsAny(lower, "notion")) "Notion" else "App-Inhalte"
            }
            "Ein echter App-Connector fehlt noch. Ich kann auf einen teilbaren oder manuellen Signalpfad wechseln." to listOf(
                StartIntentRepairOption(
                    id = "app-share",
                    label = "Inhalte teilen",
                    supportingLabel = "Einzelne Inhalte aus $appLabel direkt an Days senden",
                    replacementInput = "Ich will Inhalte aus ${appLabel.lowercase()} hierher teilen.",
                ),
                StartIntentRepairOption(
                    id = "app-manual",
                    label = "Manuell starten",
                    supportingLabel = "Bereich ohne feste App-Quelle anlegen",
                    replacementInput = "Ich will einen ruhigen Bereich fuer dieses Thema ohne feste App-Quelle anlegen.",
                ),
            )
        }

        else -> null to emptyList()
    }
}

internal fun StartIntentAnalysis.suggestedDraft(): CreateAreaDraft {
    return CreateAreaDraft(
        title = suggestedTitle,
        meaning = suggestedSummary,
        templateId = suggestedTemplateId,
        iconKey = suggestedIconKey,
        behaviorClass = suggestedBehaviorClass,
        sourceKind = suggestedSourceKind,
    )
}

internal fun startIntentBatchSamples(): List<String> {
    return listOf(
        "Zeig mir meine Termine fuer heute.",
        "Ordne meine Meetings nach Wichtigkeit.",
        "Hilf mir, freie Fokusfenster im Kalender zu sehen.",
        "Ich will aus dem Kalender meinen Tagesdruck lesen.",
        "Mach einen Bereich fuer anstehende Deadlines aus meinen Terminen.",
        "Zeig mir nur die wichtigen Besprechungen.",
        "Ich moechte meinen Wochenplan aus dem Kalender sehen.",
        "Sag mir, wann mein Tag im Kalender zu voll wird.",
        "Mach einen Bereich fuer Arzttermine und Fristen.",
        "Ich will meine Projekttermine im Blick behalten.",
        "Zeig mir Familien-Termine in einem Bereich.",
        "Hilf mir, Puffer zwischen Meetings zu finden.",
        "Ordne heute alle Calls nach Energieaufwand.",
        "Mach einen Bereich fuer Rechnungsfristen aus dem Kalender.",
        "Ich will sehen, wann ich keine ruhige Arbeitszeit mehr habe.",
        "Zeig mir Reise- und Eventtermine zusammen.",
        "Ich moechte Review-Termine im Kalender sammeln.",
        "Hilf mir, aus dem Kalender eine realistische Tagesroute zu bauen.",
        "Mach meinen Launch-Kalender lesbar.",
        "Zeig mir, ob mein Freitag zu voll wird.",
        "Wenn mir wichtige Leute schreiben, will ich es sofort sehen.",
        "Mach einen Bereich fuer Slack und Signal Benachrichtigungen.",
        "Ich will den Benachrichtigungsdruck aus meinen Apps sehen.",
        "Zeig mir nur Nachrichten, die ich nicht uebersehen sollte.",
        "Mach einen Bereich fuer Familienchat und Freunde.",
        "Hilf mir, WhatsApp-Nachrichten nach Wichtigkeit zu sehen.",
        "Ich will Team-Pings frueh erkennen.",
        "Zeig mir Support- und Kunden-Nachrichten getrennt.",
        "Mach einen Bereich fuer Notfallkontakte aus Benachrichtigungen.",
        "Ich will Mentoring- und Alumni-Pings nicht verpassen.",
        "Zeig mir, wenn ein Kontakt mehrfach schreibt.",
        "Mach einen Bereich fuer Lieferstatus und Paketmeldungen.",
        "Hilf mir, Stoerfeuer aus Notifications zu erkennen.",
        "Ich will nur VIP-Chats sichtbar haben.",
        "Zeig mir Kundenservice-Nachrichten klarer.",
        "Mach einen Bereich fuer Stakeholder-Pings.",
        "Ich will Benachrichtigungen fuer meine Kinder nicht uebersehen.",
        "Zeig mir, wenn im Kernteam Unruhe entsteht.",
        "Hilf mir, Chat-Laerm von echten Signalen zu trennen.",
        "Mach einen Bereich fuer Vereins- und Nachbarschaftspings.",
        "Lies meine Schlafdaten und zeig mir die Nacht.",
        "Ich will Schritte und Bewegung in einem Bereich sehen.",
        "Zeig mir Health-Connect-Daten fuer Erholung.",
        "Mach einen Bereich fuer Wasser und Aktivierung.",
        "Hilf mir, meinen Ruhepuls zu verfolgen.",
        "Ich will meine Regeneration sehen.",
        "Zeig mir Schlafdruck und Schlaffenster.",
        "Mach einen Bereich fuer Mobility und Stretching.",
        "Hilf mir, meine Energie aus Koerpersignalen zu lesen.",
        "Ich will meinen Morgenstart aus Health Connect sehen.",
        "Zeig mir Belastung und Puls zusammen.",
        "Mach einen Bereich fuer Recovery nach dem Sport.",
        "Hilf mir, Wasserbilanz und Schritte zusammen zu sehen.",
        "Ich will Abendschritte getrennt von Tageszielen sehen.",
        "Zeig mir Gesundheitsdaten fuer mein Wochenende.",
        "Ich will eine ruhige Abendroutine pflegen.",
        "Mach einen Bereich fuer Zuhause-Aufraeumen.",
        "Hilf mir, meine Einkaufsroutine zu halten.",
        "Ich will einen Bereich fuer meinen Arbeitsweg.",
        "Zeig mir Besorgungen fuer den Heimweg.",
        "Mach einen Bereich fuer Kueche und Reset.",
        "Hilf mir, meine Wochenplanung manuell zu ordnen.",
        "Ich will meine Lernroutine ruhig tragen.",
        "Zeig mir meine Schreibpraxis ohne Druck.",
        "Mach einen Bereich fuer Sprachpraxis.",
        "Hilf mir, mich an Gartenarbeit zu erinnern.",
        "Ich will ein Thema fuer Buchnotizen anlegen.",
        "Zeig mir meine Fotoauswahl als manuelles Projekt.",
        "Mach einen Bereich fuer meinen Schreibtisch-Reset.",
        "Hilf mir, Notizen und Gedanken ruhig zu sammeln.",
        "Ich will einen Bereich fuer Rezeptideen.",
        "Zeig mir meine Admin-Woche klarer.",
        "Mach einen Bereich fuer Rechnungen und Papierkram.",
        "Hilf mir, Steuerunterlagen zu ordnen.",
        "Ich will meine Shopping-Liste besser pflegen.",
        "Zeig mir Reiseideen in einem Bereich.",
        "Mach einen Bereich fuer meine Research-Spuren.",
        "Hilf mir, offene Artikel spaeter manuell zu lesen.",
        "Ich will Ideen aus Gespraechen sammeln.",
        "Zeig mir, welche Routinen zuhause tragen.",
        "Lies meine Browser-Lesezeichen.",
        "Zeig mir alle Chrome-Bookmarks zu Android APIs.",
        "Analysiere meine Safari-Lesezeichen fuer Entwicklerseiten.",
        "Suche in meinen Browser-Lesezeichen nach Compose.",
        "Lies meine gespeicherten Webseiten direkt.",
        "Pruefe meine offene Leseliste im Browser.",
        "Lies diese Website fuer mich.",
        "Analysiere neue Artikel aus meinem RSS-Feed.",
        "Zeig mir neue Podcast-Folgen direkt aus dem Feed.",
        "Lies meine News-Seiten und fasse sie zusammen.",
        "Scanne meine Screenshots nach To-dos.",
        "Lies neue Screenshots automatisch.",
        "Analysiere meine Fotos nach Rechnungen.",
        "Zeig mir Bilder aus meiner Galerie mit Whiteboards.",
        "Lies meine PDFs und sortiere sie.",
        "Analysiere meine Dokumente nach Fristen.",
        "Lies meine Downloads und sag mir, was wichtig ist.",
        "Scanne meine Excel-Dateien nach offenen Rechnungen.",
        "Lies meine Gmail-Mails.",
        "Analysiere mein Postfach nach dringenden Antworten.",
    )
}

internal fun buildStartIntentAnalysisReport(
    capabilityProfile: CapabilityProfile?,
    browserApps: List<String>,
): String {
    val analyses = startIntentBatchSamples().map { prompt ->
        prompt to analyzeStartIntent(
            rawInput = prompt,
            capabilityProfile = capabilityProfile,
            browserApps = browserApps,
        )
    }
    val outcomes = analyses.groupingBy { it.second.outcome }.eachCount()
    val families = analyses.groupingBy { it.second.family }.eachCount().toList().sortedByDescending { it.second }
    val blocked = analyses.filter { !it.second.canCreate }
    return buildString {
        appendLine("Start intent batch report")
        appendLine("samples=${analyses.size}")
        appendLine(
            "outcomes=direct:${outcomes[StartIntentOutcome.DIRECT_SOURCE] ?: 0}, manual:${outcomes[StartIntentOutcome.MANUAL_AREA] ?: 0}, blocked:${outcomes[StartIntentOutcome.BLOCKED] ?: 0}",
        )
        appendLine("topFamilies=")
        families.take(8).forEach { (family, count) ->
            appendLine("- ${family.name}: $count")
        }
        appendLine("blockedExamples=")
        blocked.take(12).forEach { (prompt, analysis) ->
            appendLine("- $prompt => ${analysis.blockingReason}")
        }
    }
}

private fun interpretStartIntent(
    rawInput: String,
    aiSignals: StartPlatformAiSignals = StartPlatformAiSignals(),
): StartIntentSpec {
    val trimmed = rawInput.trim()
    val lower = trimmed.lowercase()
    val hasReadVerb = containsAny(
        lower,
        "lies",
        "lesen",
        "lese",
        "analys",
        "scan",
        "pruef",
        "prüf",
        "such",
        "find",
        "fass",
        "durchsuch",
    )
    val hasShareVerb = containsAny(
        lower,
        "teilen",
        "teil",
        "weiterleit",
        "weiterleiten",
        "weitergeben",
        "schick",
        "hinein",
        "import",
        "uebergeben",
        "übergeben",
    )
    val titleSeed = detectIntentTitle(trimmed)
    val hasFileConnectorHint = containsAny(
        lower,
        "datei",
        "dateien",
        "html",
        "export",
        "backup",
        "eml",
        "mbox",
        "anhang",
    )

    return when {
        hasFileConnectorHint && containsAny(lower, "lesezeichen", "bookmark") -> StartIntentSpec(
            family = StartIntentFamily.BOOKMARKS,
            title = titleSeed.ifBlank { "Lesezeichen Import" },
            summary = "Dieser Bereich liest Browser-Lesezeichen aus exportierten HTML-Dateien.",
            templateId = "medium",
            iconKey = "book",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Lesezeichen-Datei",
            sourceDetail = "HTML-Exports von Browser-Lesezeichen kann Days jetzt direkt lesen und in Links zerlegen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst Browser-Lesezeichen ueber eine Exportdatei in Days holen.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du eine HTML-Lesezeichen-Datei importieren.",
            readinessWhenBlocked = "",
        )

        hasShareVerb && containsAny(lower, "lesezeichen", "bookmark") -> StartIntentSpec(
            family = StartIntentFamily.WEB_CONTENT,
            title = titleSeed.ifBlank { "Link Sammelpunkt" },
            summary = "Dieser Bereich sammelt geteilte Browser-Links fuer spaetere Sichtung.",
            templateId = "medium",
            iconKey = "book",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Link-Import",
            sourceDetail = "Einzelne Lesezeichen oder Links kannst du schon direkt aus dem Browser teilen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einzelne Browser-Links in einem Bereich sammeln.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du einzelne Browser-Links direkt teilen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "lesezeichen", "bookmark") -> StartIntentSpec(
            family = StartIntentFamily.BOOKMARKS,
            title = titleSeed.ifBlank { "Lesezeichen Blick" },
            summary = "Soll Browser-Lesezeichen lesen und ordnen.",
            templateId = "medium",
            iconKey = "book",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Browser-Lesezeichen",
            sourceDetail = "Gesammelte Browser-Lesezeichen kann Days nicht direkt aus Browser-Apps lesen. Einzelne Links oder HTML-Exports gehen aber schon.",
            outcome = StartIntentOutcome.BLOCKED,
            headline = "Du willst, dass Days Browser-Lesezeichen direkt liest.",
            readinessWhenReady = "",
            readinessWhenBlocked = "Ich kann Browser-Lesezeichen noch nicht direkt aus Browser-Apps lesen. Einzelne Links und HTML-Exports gehen aber schon.",
            blockingReason = "Lesezeichenzugriff ist noch nicht implementiert.",
            implementationNote = "Android bietet keinen generischen Direktzugriff auf Browser-Lesezeichen. Days kann stattdessen Links teilen oder HTML-Exports lesen.",
        )

        (isNewsIntent(lower)) ||
            (hasReadVerb &&
            containsAny(lower, "website", "webseite", "url", "rss", "feed", "podcast", "folge", "artikel", "news", "newsletter", "blog") &&
            !containsAny(lower, "spaeter", "später", "manuell", "sammeln", "ordnen", "merkliste")) ||
            Regex("""https?://\S+""").containsMatchIn(trimmed) ||
            aiSignals.hasUrl -> StartIntentSpec(
            family = StartIntentFamily.WEB_CONTENT,
            title = if (isNewsIntent(lower)) "News" else titleSeed.ifBlank { "Web Blick" },
            summary = if (isSpecificNewsIntent(lower)) {
                "Dieser Bereich holt ausgewaehlte Posts, Bilder und Artikel in einen ruhigen Feed."
            } else if (isNewsIntent(lower)) {
                "Dieser Bereich sammelt News-Quellen fuer einen spaeteren Feed."
            } else {
                "Dieser Bereich sammelt Links und Web-Material fuer spaetere Auswertung."
            },
            templateId = "medium",
            iconKey = "book",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = if (isNewsIntent(lower)) {
                "News-Quellen"
            } else if (containsAny(lower, "rss", "feed", "newsletter", "podcast")) {
                "Feed-Import"
            } else {
                "Link-Import"
            },
            sourceDetail = if (isSpecificNewsIntent(lower)) {
                "Ausgewaehlte Posts, Bilder und Artikel lassen sich jetzt ueber Web, Share und Import vorbereiten."
            } else if (isNewsIntent(lower)) {
                "Welche Quellen hier wichtig sind, wird erst im naechsten Schritt geschaerft."
            } else if (containsAny(lower, "rss", "feed", "newsletter", "podcast")) {
                "Feed-URLs und Webseiten mit Feed lassen sich jetzt in den Bereich holen und dort lesen."
            } else {
                "Links und Webseiten koennen jetzt direkt in den Bereich importiert werden."
            },
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = if (isSpecificNewsIntent(lower)) {
                "Du willst News aus ausgewaehlten Quellen in einem Bereich sammeln."
            } else if (isNewsIntent(lower)) {
                "Du willst einen News-Bereich anlegen."
            } else if (containsAny(lower, "rss", "feed", "newsletter", "podcast")) {
                "Du willst, dass Days neue Eintraege aus dem Web oder einem Feed in einem Bereich liest."
            } else {
                "Du willst, dass Days Material aus dem Web in einem Bereich sammelt."
            },
            readinessWhenReady = if (isSpecificNewsIntent(lower)) {
                "Ich kann den Bereich anlegen. Danach pruefe ich X, Instagram, FAZ, stol.it und weitere News-Wege auf gangbare Pfade."
            } else if (isNewsIntent(lower)) {
                "Ich kann den Bereich anlegen. Danach klaeren wir, welche News-Quellen fuer dich wirklich zaehlen."
            } else if (containsAny(lower, "rss", "feed", "newsletter", "podcast")) {
                "Ich kann den Bereich anlegen. Danach kannst du einen Feed oder eine Website merken und neue Eintraege auch wiederholt lesen."
            } else {
                "Ich kann den Bereich anlegen. Danach kannst du Links direkt in diesen Bereich holen."
            },
            readinessWhenBlocked = "",
        )

        hasReadVerb && containsAny(lower, "screenshot", "screenshots") -> StartIntentSpec(
            family = StartIntentFamily.SCREENSHOTS,
            title = titleSeed.ifBlank { "Screenshot Blick" },
            summary = "Dieser Bereich sammelt Screenshots fuer spaetere Sichtung und Ordnung.",
            templateId = "medium",
            iconKey = "palette",
            behaviorClass = AreaBehaviorClass.TRACKING,
            sourceLabel = "Bild-Import",
            sourceDetail = "Screenshots lassen sich als Bilder direkt in den Bereich holen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst, dass Days mit deinen Screenshots arbeitet.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du Screenshots direkt importieren.",
            readinessWhenBlocked = "",
        )

        hasReadVerb && containsAny(lower, "foto", "fotos", "bild", "bilder", "galerie", "kamera") -> StartIntentSpec(
            family = StartIntentFamily.PHOTOS,
            title = titleSeed.ifBlank { "Foto Blick" },
            summary = "Dieser Bereich sammelt Bilder und Fotos fuer spaetere Sichtung.",
            templateId = "medium",
            iconKey = "palette",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Bild-Import",
            sourceDetail = "Bilder koennen jetzt direkt ueber den Photo Picker in den Bereich kommen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst, dass Days Bilder oder Fotos in einem Bereich sammelt.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du Bilder direkt importieren.",
            readinessWhenBlocked = "",
        )

        hasReadVerb &&
            containsAny(lower, "pdf", "datei", "dateien", "dokument", "dokumente", "download", "excel", "sheet", "csv") &&
            !containsAny(lower, "gmail", "mail", "mails", "email", "emails", "postfach", "inbox", "eml", "mbox") -> StartIntentSpec(
            family = StartIntentFamily.FILES,
            title = titleSeed.ifBlank { "Datei Blick" },
            summary = "Dieser Bereich sammelt Dateien und Dokumente fuer spaetere Sichtung.",
            templateId = "project",
            iconKey = "briefcase",
            behaviorClass = AreaBehaviorClass.PROGRESS,
            sourceLabel = "Datei-Import",
            sourceDetail = "Dateien koennen jetzt gezielt ueber den System-Dateiwaehler in den Bereich kommen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst, dass Days mit lokalen Dateien oder Dokumenten arbeitet.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du Dateien direkt importieren.",
            readinessWhenBlocked = "",
        )

        hasFileConnectorHint && containsAny(lower, "gmail", "mail", "mails", "email", "emails", "postfach", "inbox") -> StartIntentSpec(
            family = StartIntentFamily.EMAIL,
            title = titleSeed.ifBlank { "Mail Import" },
            summary = "Dieser Bereich liest exportierte Mails und EML-Dateien ein.",
            templateId = "person",
            iconKey = "chat",
            behaviorClass = AreaBehaviorClass.PROTECTION,
            sourceLabel = "Mail-Datei",
            sourceDetail = "EML- und Text-Mails kann Days jetzt direkt lesen und Links daraus ziehen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst Mail-Inhalte ueber Export- oder EML-Dateien in Days holen.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du EML- oder Textdateien importieren.",
            readinessWhenBlocked = "",
        )

        hasShareVerb && containsAny(lower, "gmail", "mail", "mails", "email", "emails", "postfach", "inbox") -> StartIntentSpec(
            family = StartIntentFamily.EMAIL,
            title = titleSeed.ifBlank { "Mail Sammelpunkt" },
            summary = "Dieser Bereich sammelt weitergeleitete Mail-Texte und Anhaenge.",
            templateId = "person",
            iconKey = "chat",
            behaviorClass = AreaBehaviorClass.PROTECTION,
            sourceLabel = "Text- und Datei-Import",
            sourceDetail = "Weitergeleitete Mail-Texte und Anhaenge kann Days schon aufnehmen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einzelne Mail-Inhalte in einem Bereich sammeln.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du einzelne Mail-Inhalte direkt teilen.",
            readinessWhenBlocked = "",
        )

        hasReadVerb && containsAny(lower, "gmail", "mail", "mails", "email", "emails", "postfach", "inbox") -> StartIntentSpec(
            family = StartIntentFamily.EMAIL,
            title = titleSeed.ifBlank { "Mail Blick" },
            summary = "Soll Mails oder Postfach direkt lesen.",
            templateId = "person",
            iconKey = "chat",
            behaviorClass = AreaBehaviorClass.PROTECTION,
            sourceLabel = "Mail",
            sourceDetail = "Das Postfach kann Days noch nicht direkt lesen. Geteilte Mail-Texte, Anhaenge und EML-Dateien gehen aber schon.",
            outcome = StartIntentOutcome.BLOCKED,
            headline = "Du willst, dass Days Mails oder dein Postfach direkt liest.",
            readinessWhenReady = "",
            readinessWhenBlocked = "Ich kann Postfaecher noch nicht direkt lesen. Einzelne Inhalte aus Mail-Apps oder EML-Dateien gehen aber schon.",
            blockingReason = "Mailzugriff ist noch nicht implementiert.",
            implementationNote = "Fuer echtes Postfachlesen braucht Days einen Mail-Connector mit API oder IMAP. Geteilte Inhalte und Mail-Dateien kann Days schon lesen.",
        )

        containsAny(
            lower,
            "app-bau",
            "appbau",
            "projekt",
            "job",
            "arbeit",
            "github",
            "repo",
            "ticket",
            "tickets",
            "issue",
            "issues",
            "build",
            "release",
            "entwicklung",
            "entwickeln",
            "code",
            "programm",
        ) -> StartIntentSpec(
            family = StartIntentFamily.APP_CONTENT,
            title = titleSeed.ifBlank { "Projekt Blick" },
            summary = "Dieser Bereich sammelt Projektmaterial, Links und naechste Schritte.",
            templateId = "project",
            iconKey = "briefcase",
            behaviorClass = AreaBehaviorClass.PROGRESS,
            sourceLabel = "Projektquellen",
            sourceDetail = "Links, Dateien, Screenshots und geteilte Inhalte lassen sich fuer Projektarbeit vorbereiten.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer Projektarbeit, App-Bau oder Arbeit anlegen.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach klaeren wir, welche Projektquellen wirklich zaehlen.",
            readinessWhenBlocked = "",
        )

        hasShareVerb && containsAny(lower, "notion", "spotify", "kindle", "bank", "banking", "app", "apps", "bibliothek") -> StartIntentSpec(
            family = StartIntentFamily.APP_CONTENT,
            title = titleSeed.ifBlank { "App Sammelpunkt" },
            summary = "Dieser Bereich sammelt geteilte Inhalte aus anderen Apps.",
            templateId = "theme",
            iconKey = "focus",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Geteilter App-Inhalt",
            sourceDetail = "Einzelne Inhalte aus anderen Apps kann Days schon annehmen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einzelne Inhalte aus anderen Apps in einem Bereich sammeln.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du Inhalte aus anderen Apps direkt teilen.",
            readinessWhenBlocked = "",
        )

        hasReadVerb && containsAny(lower, "notion", "spotify", "kindle", "bank", "banking", "app", "apps", "bibliothek") -> StartIntentSpec(
            family = StartIntentFamily.APP_CONTENT,
            title = titleSeed.ifBlank { "App Blick" },
            summary = "Soll Inhalte aus einer bestimmten App direkt lesen.",
            templateId = "theme",
            iconKey = "focus",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "App-Inhalte",
            sourceDetail = "Direkte App-Integrationen fehlen noch. Geteilte Inhalte aus anderen Apps kann Days aber schon annehmen.",
            outcome = StartIntentOutcome.BLOCKED,
            headline = "Du willst, dass Days Inhalte aus einer anderen App direkt liest.",
            readinessWhenReady = "",
            readinessWhenBlocked = "Ich kann App-Inhalte noch nicht allgemein direkt anbinden. Geteilte Einzelinhalte gehen aber schon.",
            blockingReason = "App-spezifische Integrationen sind noch nicht implementiert.",
            implementationNote = "Noetig sind pro App echte Connectoren. Geteilte Einzelinhalte koennen schon in Days landen.",
        )

        containsAny(lower, "kalender", "termin", "meeting", "besprech", "deadline", "frist", "wochenplan", "event", "call") -> StartIntentSpec(
            family = StartIntentFamily.CALENDAR,
            title = titleSeed.ifBlank { "Kalender Fokus" },
            summary = "Dieser Bereich ordnet Termine, Druck und freie Fenster.",
            templateId = "project",
            iconKey = "briefcase",
            behaviorClass = AreaBehaviorClass.PROGRESS,
            sourceKind = DataSourceKind.CALENDAR,
            sourceLabel = "Kalender",
            sourceDetail = "Days nutzt dafuer Kalenderdaten.",
            outcome = StartIntentOutcome.DIRECT_SOURCE,
            headline = "Du willst, dass Days Termine und Tagesdruck fuer dich lesbar macht.",
            readinessWhenReady = "Ich kann den Bereich anlegen und mit Kalenderdaten verbinden.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "arbeitszeit", "fokusfenster", "freitag", "tagesroute") -> StartIntentSpec(
            family = StartIntentFamily.CALENDAR,
            title = titleSeed.ifBlank { "Kalender Fokus" },
            summary = "Dieser Bereich ordnet Termine, Druck und freie Fenster.",
            templateId = "project",
            iconKey = "briefcase",
            behaviorClass = AreaBehaviorClass.PROGRESS,
            sourceKind = DataSourceKind.CALENDAR,
            sourceLabel = "Kalender",
            sourceDetail = "Days nutzt dafuer Kalenderdaten.",
            outcome = StartIntentOutcome.DIRECT_SOURCE,
            headline = "Du willst, dass Days deinen Tag und freie Zeit aus dem Kalender klaert.",
            readinessWhenReady = "Ich kann den Bereich anlegen und mit Kalenderdaten verbinden.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "nachricht", "benachr", "notification", "chat", "whatsapp", "signal", "slack", "kontakt", "schreibt", "schreiben", "pings", "ping", "vip") -> StartIntentSpec(
            family = StartIntentFamily.NOTIFICATIONS,
            title = titleSeed.ifBlank { "Kontakt Blick" },
            summary = "Dieser Bereich filtert wichtige Signale aus Nachrichten und Benachrichtigungen.",
            templateId = "person",
            iconKey = "chat",
            behaviorClass = AreaBehaviorClass.PROTECTION,
            sourceKind = DataSourceKind.NOTIFICATIONS,
            sourceLabel = "Benachrichtigungen",
            sourceDetail = "Days nutzt dafuer Notification-Signale.",
            outcome = StartIntentOutcome.DIRECT_SOURCE,
            headline = "Du willst, dass Days wichtige Kontakte oder Pings aus Signalen zieht.",
            readinessWhenReady = "Ich kann den Bereich anlegen und mit Benachrichtigungen verbinden.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "lieferstatus", "paket", "kernteam") -> StartIntentSpec(
            family = StartIntentFamily.NOTIFICATIONS,
            title = titleSeed.ifBlank { "Signal Blick" },
            summary = "Dieser Bereich filtert wichtige Signale aus Nachrichten und Benachrichtigungen.",
            templateId = "person",
            iconKey = "chat",
            behaviorClass = AreaBehaviorClass.PROTECTION,
            sourceKind = DataSourceKind.NOTIFICATIONS,
            sourceLabel = "Benachrichtigungen",
            sourceDetail = "Days nutzt dafuer Notification-Signale.",
            outcome = StartIntentOutcome.DIRECT_SOURCE,
            headline = "Du willst, dass Days auffaellige Pings oder Meldungen schnell sichtbar macht.",
            readinessWhenReady = "Ich kann den Bereich anlegen und mit Benachrichtigungen verbinden.",
            readinessWhenBlocked = "",
        )

        containsAny(
            lower,
            "schlaf",
            "sleep",
            "steps",
            "schritte",
            "bewegung",
            "puls",
            "ruhepuls",
            "health",
            "gesund",
            "recovery",
            "regeneration",
            "erholung",
            "wasser",
            "hydration",
            "stretching",
            "mobil",
            "energie",
            "belastung",
            "aktivierung",
            "morgenstart",
        ) -> StartIntentSpec(
            family = StartIntentFamily.HEALTH,
            title = titleSeed.ifBlank { "Gesundheit Blick" },
            summary = "Dieser Bereich liest Koerper-, Energie- oder Erholungssignale.",
            templateId = "ritual",
            iconKey = "heart",
            behaviorClass = AreaBehaviorClass.TRACKING,
            sourceKind = DataSourceKind.HEALTH_CONNECT,
            sourceLabel = "Health Connect",
            sourceDetail = "Days nutzt dafuer Health-Connect-Daten.",
            outcome = StartIntentOutcome.DIRECT_SOURCE,
            headline = "Du willst, dass Days Koerper- oder Erholungssignale fuer dich sichtbar macht.",
            readinessWhenReady = "Ich kann den Bereich anlegen und mit Health Connect verbinden.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "routine", "ritual", "praxis", "abendroutine", "morgenroutine", "gewohn") -> StartIntentSpec(
            family = StartIntentFamily.ROUTINE,
            title = titleSeed.ifBlank { "Ritual Blick" },
            summary = "Dieser Bereich haelt eine wiederkehrende Praxis oder Gewohnheit stabil.",
            templateId = "ritual",
            iconKey = "lotus",
            behaviorClass = AreaBehaviorClass.MAINTENANCE,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier braucht Days zuerst nur deine Eingaben und deinen Rhythmus.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer eine wiederkehrende Praxis oder Gewohnheit.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt als Ritual anlegen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "zuhause", "home", "haushalt", "einkauf", "heimweg", "arbeitsweg", "buero", "küche", "kueche", "garten", "wohnung", "ort", "weg") ||
            aiSignals.hasAddress -> StartIntentSpec(
            family = if (containsAny(lower, "ort", "weg", "zuhause", "home", "buero")) StartIntentFamily.LOCATION else StartIntentFamily.ROUTINE,
            title = titleSeed.ifBlank { "Ort Routine" },
            summary = "Dieser Bereich haelt einen Ort, Weg oder Alltagsrhythmus klar.",
            templateId = "place",
            iconKey = "home",
            behaviorClass = AreaBehaviorClass.MAINTENANCE,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier braucht Days noch keine Systemquelle.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer Ort, Weg oder alltaegliche Pflege.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt als klaren Alltagsbereich anlegen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "fotoauswahl", "whiteboard", "galerie", "bilder") &&
            containsAny(lower, "manuell", "projekt", "auswahl", "samml", "ordnen") -> StartIntentSpec(
            family = StartIntentFamily.READING,
            title = titleSeed.ifBlank { "Foto Projekt" },
            summary = "Dieser Bereich sammelt manuelle Fotoauswahl und Gedanken in einem Ort.",
            templateId = "medium",
            iconKey = "palette",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier startet Days erst einmal als manueller Sammelbereich.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer Bildmaterial, das du selbst kuratierst.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt als manuelles Foto-Projekt anlegen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "foto", "fotos", "bild", "bilder", "galerie", "whiteboard") -> StartIntentSpec(
            family = StartIntentFamily.PHOTOS,
            title = titleSeed.ifBlank { "Foto Blick" },
            summary = "Dieser Bereich sammelt Bilder und Fotos fuer spaetere Sichtung.",
            templateId = "medium",
            iconKey = "palette",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Bild-Import",
            sourceDetail = "Bilder koennen jetzt direkt ueber den Photo Picker in den Bereich kommen.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst, dass Days Bilder oder Fotos in einem Bereich sammelt.",
            readinessWhenReady = "Ich kann den Bereich anlegen. Danach kannst du Bilder direkt importieren.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "lernen", "sprach", "buch", "research", "notiz", "ideen", "leseliste", "lesen", "artikel", "podcast", "rezept", "thema") -> StartIntentSpec(
            family = StartIntentFamily.READING,
            title = titleSeed.ifBlank { "Thema Blick" },
            summary = "Dieser Bereich sammelt Material, Gedanken und Lernspuren.",
            templateId = "medium",
            iconKey = "book",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier kann Days erst einmal ohne feste Quelle starten.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich, der Material oder Lernspuren ruhig sammelt.",
            readinessWhenReady = "Ich kann den Bereich jetzt als offenes Thema anlegen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "schreib", "journal", "tagebuch", "entwurf") -> StartIntentSpec(
            family = StartIntentFamily.WRITING,
            title = titleSeed.ifBlank { "Schreib Praxis" },
            summary = "Dieser Bereich haelt Schreibpraxis und Reflexion zusammen.",
            templateId = "theme",
            iconKey = "focus",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier braucht Days nur deine Eingaben.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer Schreiben, Journaling oder Entwuerfe.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt anlegen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "rechnung", "steuer", "papier", "admin", "organisation", "unterlagen") -> StartIntentSpec(
            family = StartIntentFamily.ADMIN,
            title = titleSeed.ifBlank { "Admin Blick" },
            summary = "Dieser Bereich haelt Papierkram, Fristen und Ordnung zusammen.",
            templateId = "project",
            iconKey = "briefcase",
            behaviorClass = AreaBehaviorClass.PROGRESS,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier kann Days mit einem klaren manuellen Bereich starten.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer Verwaltung, Fristen oder Ordnung.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt anlegen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "shopping", "shop", "kaufen", "besorgung", "einkauf") -> StartIntentSpec(
            family = StartIntentFamily.SHOPPING,
            title = titleSeed.ifBlank { "Einkauf Blick" },
            summary = "Dieser Bereich haelt Besorgungen und Einkaufszuege zusammen.",
            templateId = "project",
            iconKey = "briefcase",
            behaviorClass = AreaBehaviorClass.MAINTENANCE,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier startet Days erst einmal ohne feste Quelle.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer Einkauf oder Besorgungen.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt anlegen.",
            readinessWhenBlocked = "",
        )

        containsAny(lower, "geld", "budget", "ausgaben", "kosten", "finanz", "bank") -> StartIntentSpec(
            family = StartIntentFamily.FINANCE,
            title = titleSeed.ifBlank { "Finanz Blick" },
            summary = "Dieser Bereich haelt Geldthemen, Ausgaben oder Budgets zusammen.",
            templateId = "project",
            iconKey = "shield",
            behaviorClass = AreaBehaviorClass.PROTECTION,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier kann Days erst einmal als manueller Finanzbereich starten.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst einen Bereich fuer Ausgaben, Budget oder Geldthemen.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt anlegen.",
            readinessWhenBlocked = "",
        )

        else -> StartIntentSpec(
            family = StartIntentFamily.GENERAL,
            title = titleSeed.ifBlank { "Neuer Bereich" },
            summary = "Dieser Bereich startet frei und kann spaeter weiter geschaerft werden.",
            templateId = "free",
            iconKey = "spark",
            behaviorClass = AreaBehaviorClass.REFLECTION,
            sourceLabel = "Keine feste Quelle",
            sourceDetail = "Hier braucht Days zuerst nur deinen Auftrag.",
            outcome = StartIntentOutcome.MANUAL_AREA,
            headline = "Du willst zuerst einen freien Bereich fuer ein Thema oder eine Aufgabe.",
            readinessWhenReady = "Ich kann den Bereich jetzt direkt anlegen.",
            readinessWhenBlocked = "",
        )
    }
}

private fun buildDeviceFindings(
    spec: StartIntentSpec,
    capabilityProfile: CapabilityProfile?,
    browserApps: List<String>,
): List<String> {
    val source = spec.sourceKind?.let { capabilityProfile?.find(it) }
    return when (spec.family) {
        StartIntentFamily.BOOKMARKS -> listOf(
            if (browserApps.isEmpty()) {
                "Keine erkennbare Browser-App gefunden."
            } else {
                "Browser gefunden: ${browserApps.joinToString()}"
            },
            "Es gibt hier noch keinen echten Bookmark-Pfad.",
            "Einzelne Links aus Browsern koennen aber schon in Days geteilt werden.",
        )
        StartIntentFamily.SCREENSHOTS -> listOf(
            "Du kannst Screenshots jetzt als Bildmaterial direkt in den Bereich holen.",
            "Die tiefere automatische Auswertung des Bildinhalts folgt noch.",
        )
        StartIntentFamily.PHOTOS -> listOf(
            "Der Photo Picker ist der aktuelle Eingang fuer Bilder in diesen Bereich.",
            "Die tiefere automatische Auswertung des Bildinhalts folgt noch.",
        )
        StartIntentFamily.FILES -> listOf(
            "Dateien koennen jetzt ueber den System-Dateiwaehler importiert werden.",
            "Die tiefere automatische Dokumentauswertung folgt noch.",
        )
        StartIntentFamily.WEB_CONTENT -> listOf(
            "Links lassen sich jetzt direkt im Bereich speichern.",
            "Android kann Links im Text schon lokal erkennen und Days damit schneller auf Web-Material ausrichten.",
        )
        StartIntentFamily.EMAIL -> listOf(
            "Es gibt noch keinen Mail-Connector.",
            "Notification-Signale reichen hier nicht fuer echten Mailzugriff.",
            "Geteilte Mail-Texte oder Anhaenge koennen aber schon in Days landen.",
        )
        StartIntentFamily.APP_CONTENT -> listOf(
            "Es gibt noch keinen allgemeinen App-Connector.",
            "Dafuer braucht jede App einen eigenen sauberen Pfad.",
            "Geteilte Einzelinhalte aus anderen Apps koennen aber schon importiert werden.",
        )
        else -> listOf(
            when {
                source == null && spec.outcome == StartIntentOutcome.MANUAL_AREA ->
                    "Keine spezielle Quelle noetig."
                source == null -> "Noch keine Quelle erkannt."
                else -> "${spec.sourceLabel}: ${capabilityStateLabel(source.enabled, source.available, source.granted)}"
            },
        )
    }
}

private fun buildStartAnalysisSourceRows(
    spec: StartIntentSpec,
    rawInput: String,
    sourceCapability: com.struperto.androidappdays.domain.DataSourceCapability?,
    browserApps: List<String>,
    installedPackages: Set<String>,
): List<StartAnalysisSourceRow> {
    val lower = rawInput.lowercase()

    fun browserHint(): String {
        return if (browserApps.isEmpty()) {
            "Kein Browser sicher erkannt"
        } else {
            browserApps.joinToString()
        }
    }

    fun sourceStatus(): String {
        return when {
            sourceCapability == null && spec.outcome == StartIntentOutcome.MANUAL_AREA -> "Geht jetzt"
            sourceCapability == null && spec.outcome == StartIntentOutcome.BLOCKED -> "Noch nicht"
            sourceCapability == null -> "Moeglich"
            else -> capabilityStateLabel(
                enabled = sourceCapability.enabled,
                available = sourceCapability.available,
                granted = sourceCapability.granted,
            ).replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    return when (spec.family) {
        StartIntentFamily.WEB_CONTENT -> buildList {
            if (isNewsIntent(lower)) {
                add(
                    StartAnalysisSourceRow(
                        name = "Quellen",
                        hint = "Noch offen",
                        status = "Klaeren",
                    ),
                )
                add(
                    StartAnalysisSourceRow(
                        name = "Web",
                        hint = "Artikel oder Websites",
                        status = "Moeglich",
                    ),
                )
                add(
                    StartAnalysisSourceRow(
                        name = "Feeds",
                        hint = "Laufende News-Quellen",
                        status = "Moeglich",
                    ),
                )
                add(
                    StartAnalysisSourceRow(
                        name = "Social Text",
                        hint = "Ausgewaehlte Posts",
                        status = if ("com.twitter.android" in installedPackages) "Mit Share" else "Mit Link",
                    ),
                )
                add(
                    StartAnalysisSourceRow(
                        name = "Social Bild",
                        hint = "Ausgewaehlte Bilder",
                        status = if ("com.instagram.android" in installedPackages) "Mit Share" else "Mit Link",
                    ),
                )
                add(
                    StartAnalysisSourceRow(
                        name = "Video",
                        hint = "Ausgewaehlte Videos",
                        status = if ("com.google.android.youtube" in installedPackages) "Mit Share" else "Mit Link",
                    ),
                )
                add(
                    StartAnalysisSourceRow(
                        name = "Screenshots",
                        hint = "Post oder Artikel sichern",
                        status = "Import",
                    ),
                )
                return@buildList
            }
            add(
                StartAnalysisSourceRow(
                    name = "Browser",
                    hint = browserHint(),
                    status = if (browserApps.isEmpty()) "Pruefen" else "Gefunden",
                    actionLabel = if (browserApps.isEmpty()) null else "testen",
                ),
            )
            add(
                StartAnalysisSourceRow(
                    name = "Web-Link",
                    hint = "Artikel, URL oder Webseite direkt teilen",
                    status = "Geht jetzt",
                    actionLabel = "verbinden",
                ),
            )
            add(
                StartAnalysisSourceRow(
                    name = "Feed / RSS",
                    hint = "Website oder Feed spaeter regelmaessig lesen",
                    status = "Moeglich",
                    actionLabel = "testen",
                ),
            )
            add(
                StartAnalysisSourceRow(
                    name = "Screenshot",
                    hint = "Artikel, Post oder Bild als technischer Kandidat",
                    status = "Kandidat",
                    actionLabel = "vorsehen",
                ),
            )
        }

        StartIntentFamily.BOOKMARKS -> buildList {
            add(
                StartAnalysisSourceRow(
                    name = "Browser",
                    hint = browserHint(),
                    status = if (browserApps.isEmpty()) "Pruefen" else "Gefunden",
                    actionLabel = if (browserApps.isEmpty()) null else "testen",
                ),
            )
            add(
                StartAnalysisSourceRow(
                    name = "Lesezeichen",
                    hint = "Direkter Browser-Zugriff fehlt noch",
                    status = "Noch nicht",
                    actionLabel = "beheben",
                ),
            )
            add(
                StartAnalysisSourceRow(
                    name = "HTML-Export",
                    hint = "Lesezeichen als Datei importieren",
                    status = "Moeglich",
                    actionLabel = "testen",
                ),
            )
            add(
                StartAnalysisSourceRow(
                    name = "Einzelne Links",
                    hint = "Links aus Browsern direkt teilen",
                    status = "Geht jetzt",
                    actionLabel = "verbinden",
                ),
            )
        }

        StartIntentFamily.SCREENSHOTS -> listOf(
            StartAnalysisSourceRow(
                name = "Screenshot",
                hint = "Bildmaterial direkt in den Bereich holen",
                status = "Geht jetzt",
                actionLabel = "testen",
            ),
        )

        StartIntentFamily.PHOTOS -> listOf(
            StartAnalysisSourceRow(
                name = "Bild / Foto",
                hint = "Photo Picker als aktueller Eingang",
                status = "Geht jetzt",
                actionLabel = "verbinden",
            ),
        )

        StartIntentFamily.FILES -> listOf(
            StartAnalysisSourceRow(
                name = "Datei",
                hint = "PDF, Dokument oder Download importieren",
                status = "Geht jetzt",
                actionLabel = "verbinden",
            ),
        )

        StartIntentFamily.EMAIL -> listOf(
            StartAnalysisSourceRow(
                name = "Mail",
                hint = "Direkter Postfachzugriff fehlt noch",
                status = "Noch nicht",
                actionLabel = "beheben",
            ),
            StartAnalysisSourceRow(
                name = "Mail-Text / EML",
                hint = "Geteilte Inhalte oder Dateien importieren",
                status = "Moeglich",
                actionLabel = "testen",
            ),
        )

        StartIntentFamily.APP_CONTENT -> listOf(
            StartAnalysisSourceRow(
                name = "App-Connector",
                hint = "Generischer Direktzugriff fehlt noch",
                status = "Noch nicht",
                actionLabel = "beheben",
            ),
            StartAnalysisSourceRow(
                name = "Geteilter Inhalt",
                hint = "Einzelne Inhalte aus Apps direkt senden",
                status = "Moeglich",
                actionLabel = "testen",
            ),
        )

        else -> listOf(
            StartAnalysisSourceRow(
                name = spec.sourceLabel,
                hint = spec.sourceDetail,
                status = sourceStatus(),
                actionLabel = when {
                    spec.outcome == StartIntentOutcome.BLOCKED -> "beheben"
                    spec.outcome == StartIntentOutcome.DIRECT_SOURCE -> "freigeben"
                    else -> null
                },
            ),
        )
    }
}

private fun isNewsIntent(
    lower: String,
): Boolean {
    return containsAny(
        lower,
        "news",
        "nachrichten",
        "artikel",
        "x-post",
        "x post",
        "twitter",
        "instagram",
        "insta",
        "faz",
        "faz.net",
        "stol",
        "stol.it",
    )
}

private fun isSpecificNewsIntent(
    lower: String,
): Boolean {
    return containsAny(
        lower,
        "x-post",
        "x post",
        "twitter",
        "instagram",
        "insta",
        "faz",
        "faz.net",
        "stol",
        "stol.it",
    )
}

private fun capabilityStateLabel(
    enabled: Boolean,
    available: Boolean,
    granted: Boolean,
): String {
    return when {
        enabled && available && granted -> "bereit"
        enabled && available -> "noch nicht freigegeben"
        available -> "deaktiviert"
        else -> "nicht verfuegbar"
    }
}

private fun detectIntentTitle(rawInput: String): String {
    val trimmed = rawInput.trim()
    if (trimmed.isBlank()) return ""
    val hostMatch = Regex("""https?://(?:www\.)?([^/\s]+)""").find(trimmed)
    if (hostMatch != null) {
        return hostMatch.groupValues[1]
            .substringBefore('.')
            .replaceFirstChar(Char::uppercaseChar)
    }
    val cleaned = trimmed
        .replace(Regex("""https?://\S+"""), "")
        .replace(Regex("""[^\p{L}\p{N}\s]"""), " ")
        .trim()
    val stopWords = setOf(
        "ich",
        "moechte",
        "möchte",
        "will",
        "dass",
        "das",
        "du",
        "meine",
        "meinen",
        "mein",
        "meiner",
        "mir",
        "fuer",
        "für",
        "und",
        "mit",
        "aus",
        "dem",
        "den",
        "der",
        "die",
        "einen",
        "einem",
        "einer",
        "lies",
        "lesen",
        "lese",
        "zeig",
        "zeige",
        "analysiere",
        "analysier",
        "scanne",
        "scan",
        "pruefe",
        "prüfe",
        "mach",
        "hilfe",
        "hilf",
    )
    val words = cleaned
        .split(Regex("""\s+"""))
        .map(String::lowercase)
        .filter { it.length > 2 && it !in stopWords }
        .take(2)
    return words.joinToString(" ").replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}

private fun StartIntentSpec.withResolvedTitle(
    rawInput: String,
): StartIntentSpec {
    return copy(
        title = resolvedIntentTitle(
            rawInput = rawInput,
            family = family,
            fallback = title,
        ),
    )
}

private fun resolvedIntentTitle(
    rawInput: String,
    family: StartIntentFamily,
    fallback: String,
): String {
    val lower = rawInput.lowercase()
    val explicit = when (family) {
        StartIntentFamily.CALENDAR -> when {
            containsAny(lower, "fokusfenster", "freie zeit") -> "Fokusfenster"
            containsAny(lower, "morgen", "morgens") -> "Termine Morgen"
            containsAny(lower, "deadline", "frist") -> "Fristen"
            else -> "Kalender"
        }

        StartIntentFamily.NOTIFICATIONS -> when {
            containsAny(lower, "familie") -> "Familie Inbox"
            containsAny(lower, "wichtig") -> "Wichtige Inbox"
            containsAny(lower, "kontakt", "vip") -> "Kontakt Inbox"
            else -> "Inbox"
        }

        StartIntentFamily.HEALTH -> when {
            containsAny(lower, "nacht", "schlaf", "erholung") -> "Nacht Erholung"
            containsAny(lower, "schritte", "bewegung") -> "Bewegung"
            containsAny(lower, "wasser", "mahlzeiten", "hydration") -> "Energie Alltag"
            else -> "Gesundheit"
        }

        StartIntentFamily.ROUTINE -> if (containsAny(lower, "abend", "abends")) "Abendroutine" else "Routine"
        StartIntentFamily.SHOPPING -> "Einkauf"
        StartIntentFamily.LOCATION -> "Alltag Ort"
        StartIntentFamily.ADMIN -> "Admin"
        else -> fallback
    }
    if (explicit.isNotBlank() && explicit != fallback) return explicit
    return fallback
        .replace(" nicht", "")
        .replace(" ohne", "")
        .replace(" ruhig", "")
        .trim()
        .ifBlank { fallback }
}

private fun containsAny(
    value: String,
    vararg parts: String,
): Boolean {
    return parts.any { part -> part in value }
}
