package com.struperto.androidappdays.domain.area

import java.time.Instant
import java.time.LocalDate

data class AreaTodayOutputInput(
    val definition: AreaDefinition?,
    val blueprint: AreaBlueprint?,
    val instance: AreaInstance,
    val snapshot: AreaSnapshot?,
    val generatedAt: Instant,
    val logicalDate: LocalDate = snapshot?.date ?: LocalDate.now(),
    val openPlanTitles: List<String> = emptyList(),
    val dueCount: Int = 0,
)

fun projectAreaEvidenceProfile(
    input: AreaTodayOutputInput,
): AreaEvidenceProfile {
    val snapshot = input.snapshot
    val behaviorClass = input.instance.behaviorClass
    val supportingKinds = linkedSetOf<AreaEvidenceKind>()
    var count = 0
    var primaryKind: AreaEvidenceKind? = null
    var latestEvidenceAt: Instant? = null

    fun add(kind: AreaEvidenceKind) {
        if (primaryKind == null) {
            primaryKind = kind
        } else {
            supportingKinds += kind
        }
        count += 1
        if (latestEvidenceAt == null) {
            latestEvidenceAt = snapshot?.freshnessAt ?: input.instance.updatedAt ?: input.instance.createdAt
        }
    }

    if (snapshot?.manualScore != null) add(AreaEvidenceKind.MANUAL_SCORE)
    if (snapshot?.manualStateKey != null) add(AreaEvidenceKind.MANUAL_STATE)
    if (!snapshot?.manualNote.isNullOrBlank()) add(AreaEvidenceKind.MANUAL_NOTE)
    when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> {
            if (input.dueCount > 0) add(AreaEvidenceKind.SIGNAL)
        }
        AreaBehaviorClass.PROGRESS -> {
            if (input.openPlanTitles.isNotEmpty()) add(AreaEvidenceKind.PLAN)
            if (input.dueCount > 0) add(AreaEvidenceKind.SIGNAL)
        }
        AreaBehaviorClass.RELATIONSHIP -> {
            // Relationship areas only become evidential through an explicit human anchor.
        }
        AreaBehaviorClass.MAINTENANCE -> {
            if (input.openPlanTitles.isNotEmpty()) add(AreaEvidenceKind.PLAN)
            if (input.dueCount > 0) add(AreaEvidenceKind.SIGNAL)
            if ((input.openPlanTitles.isNotEmpty() || input.dueCount > 0) && input.reviewEnabledOrAdaptiveRhythm()) {
                add(AreaEvidenceKind.RHYTHM)
            }
        }
        AreaBehaviorClass.PROTECTION -> {
            if (input.dueCount > 0) add(AreaEvidenceKind.SIGNAL)
        }
        AreaBehaviorClass.REFLECTION -> {
            // Reflection stays manual until a deliberate reflective anchor exists.
        }
    }

    val hasManualAnchor = snapshot?.manualScore != null ||
        snapshot?.manualStateKey != null ||
        !snapshot?.manualNote.isNullOrBlank()
    val hasFreshEvidence = count > 0 &&
        freshnessBandFor(input.generatedAt, latestEvidenceAt) == AreaFreshnessBand.FRESH
    return AreaEvidenceProfile(
        evidenceCount = count,
        primaryEvidenceKind = primaryKind,
        hasFreshEvidence = hasFreshEvidence,
        hasManualAnchor = hasManualAnchor,
        latestEvidenceAt = latestEvidenceAt,
        supportingEvidenceKinds = supportingKinds,
        stalenessReason = when (freshnessBandFor(input.generatedAt, latestEvidenceAt)) {
            AreaFreshnessBand.STALE -> "Letzte relevante Evidenz ist nicht mehr frisch."
            AreaFreshnessBand.UNKNOWN -> "Noch keine belastbare Evidenz vorhanden."
            else -> null
        },
    )
}

private fun AreaTodayOutputInput.reviewEnabledOrAdaptiveRhythm(): Boolean {
    return instance.reviewEnabled || instance.cadenceKey.equals("adaptive", ignoreCase = true)
}

fun projectAreaTodayOutput(
    input: AreaTodayOutputInput,
): AreaTodayOutput {
    val behaviorClass = input.instance.behaviorClass
    val emptyState = defaultEmptyStateContract(behaviorClass)
    val evidence = projectAreaEvidenceProfile(input)
    val freshnessBand = freshnessBandFor(input.generatedAt, evidence.latestEvidenceAt ?: input.snapshot?.freshnessAt)
    val sourceTruth = resolveSourceTruth(evidence)
    val manualPriority = input.snapshot?.manualScore != null ||
        input.snapshot?.manualStateKey != null ||
        !input.snapshot?.manualNote.isNullOrBlank()
    val confirmedStepPriority = input.instance.confirmedNextStep?.isUserConfirmed == true
    val hasFreshLocalEvidence = evidence.hasFreshEvidence && !manualPriority
    val hasOlderLocalEvidence = evidence.evidenceCount > 0 && !manualPriority
    val baseOutput = when {
        manualPriority -> manualDrivenOutput(input, evidence, sourceTruth, freshnessBand)
        confirmedStepPriority -> confirmedStepOutput(input, evidence, sourceTruth, freshnessBand)
        hasFreshLocalEvidence -> localEvidenceOutput(input, evidence, sourceTruth, freshnessBand, fresh = true)
        hasOlderLocalEvidence -> localEvidenceOutput(input, evidence, sourceTruth, freshnessBand, fresh = false)
        else -> emptyStateOutput(input, evidence, sourceTruth, freshnessBand, emptyState)
    }
    val nextStep = ensureDistinctRecommendation(baseOutput.recommendation, baseOutput.nextMeaningfulStep)
    return baseOutput.copy(nextMeaningfulStep = nextStep)
}

private fun manualDrivenOutput(
    input: AreaTodayOutputInput,
    evidence: AreaEvidenceProfile,
    sourceTruth: AreaSourceTruth,
    freshnessBand: AreaFreshnessBand,
): AreaTodayOutput {
    val snapshot = requireNotNull(input.snapshot)
    val scoreLabel = snapshot.manualScore?.let { "$it/5" }
    val stateLabel = snapshot.manualStateKey?.replaceFirstChar(Char::uppercaseChar)
    val noteLabel = snapshot.manualNote
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.let { manualNoteStatusLabel(input.instance.behaviorClass) }
    val leadingLabel = stateLabel ?: scoreLabel ?: noteLabel ?: "Gesehen"
    val recommendation = manualRecommendation(
        behaviorClass = input.instance.behaviorClass,
        snapshot = snapshot,
    )
    val step = input.instance.confirmedNextStep ?: projectedStepFor(input, fallbackStatus = AreaStepStatus.READY)
    return AreaTodayOutput(
        instanceId = input.instance.instanceId,
        date = input.snapshot.date,
        generatedAt = input.generatedAt,
        behaviorClass = input.instance.behaviorClass,
        headline = "${input.instance.title}: $leadingLabel",
        statusLabel = leadingLabel,
        recommendation = recommendation,
        nextMeaningfulStep = step.copy(
            origin = if (step.isUserConfirmed) AreaStepOrigin.manual else step.origin,
        ),
        evidenceSummary = evidenceSummary(input, evidence, sourceTruth),
        sourceTruth = sourceTruth,
        confidence = if (sourceTruth == AreaSourceTruth.manual_plus_local) 0.96f else 0.92f,
        freshnessAt = evidence.latestEvidenceAt ?: snapshot.freshnessAt,
        freshnessBand = freshnessBand,
        severity = severityFor(input, emptyState = false, evidence = evidence),
        singleDockKind = dockKindFor(input.instance.behaviorClass),
        isEmptyState = false,
        usabilitySignal = usabilitySignalFor(evidence, emptyState = false),
    )
}

private fun confirmedStepOutput(
    input: AreaTodayOutputInput,
    evidence: AreaEvidenceProfile,
    sourceTruth: AreaSourceTruth,
    freshnessBand: AreaFreshnessBand,
): AreaTodayOutput {
    val step = requireNotNull(input.instance.confirmedNextStep)
    return AreaTodayOutput(
        instanceId = input.instance.instanceId,
        date = input.snapshot?.date ?: input.logicalDate,
        generatedAt = input.generatedAt,
        behaviorClass = input.instance.behaviorClass,
        headline = "${input.instance.title}: ${step.label}",
        statusLabel = step.status.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar),
        recommendation = recommendationForConfirmedStep(input.instance.behaviorClass),
        nextMeaningfulStep = step,
        evidenceSummary = evidenceSummary(input, evidence, sourceTruth),
        sourceTruth = sourceTruth,
        confidence = 0.86f,
        freshnessAt = evidence.latestEvidenceAt,
        freshnessBand = freshnessBand,
        severity = severityFor(input, emptyState = false, evidence = evidence),
        singleDockKind = dockKindFor(input.instance.behaviorClass),
        isEmptyState = false,
        usabilitySignal = usabilitySignalFor(evidence, emptyState = false),
    )
}

private fun localEvidenceOutput(
    input: AreaTodayOutputInput,
    evidence: AreaEvidenceProfile,
    sourceTruth: AreaSourceTruth,
    freshnessBand: AreaFreshnessBand,
    fresh: Boolean,
): AreaTodayOutput {
    val step = projectedStepFor(
        input = input,
        fallbackStatus = if (fresh) AreaStepStatus.READY else AreaStepStatus.STALE,
    )
    val label = when (input.instance.behaviorClass) {
        AreaBehaviorClass.TRACKING -> if (fresh) "Trend sichtbar" else "Trend aelter"
        AreaBehaviorClass.PROGRESS -> if (fresh) "Zug aktiv" else "Zug offen"
        AreaBehaviorClass.RELATIONSHIP -> if (fresh) "Impuls vorhanden" else "Impuls verblasst"
        AreaBehaviorClass.MAINTENANCE -> if (fresh) "Pflegepunkt sichtbar" else "Pflegepunkt aelter"
        AreaBehaviorClass.PROTECTION -> if (fresh) "Schutzsignal aktiv" else "Schutzsignal aelter"
        AreaBehaviorClass.REFLECTION -> if (fresh) "Lesepunkt offen" else "Lesepunkt aelter"
    }
    val recommendation = localEvidenceRecommendation(
        behaviorClass = input.instance.behaviorClass,
        fresh = fresh,
    )
    return AreaTodayOutput(
        instanceId = input.instance.instanceId,
        date = input.snapshot?.date ?: input.logicalDate,
        generatedAt = input.generatedAt,
        behaviorClass = input.instance.behaviorClass,
        headline = "${input.instance.title}: $label",
        statusLabel = label,
        recommendation = recommendation,
        nextMeaningfulStep = step,
        evidenceSummary = evidenceSummary(input, evidence, sourceTruth),
        sourceTruth = sourceTruth,
        confidence = if (fresh) 0.74f else 0.52f,
        freshnessAt = evidence.latestEvidenceAt,
        freshnessBand = freshnessBand,
        severity = severityFor(input, emptyState = false, evidence = evidence),
        singleDockKind = dockKindFor(input.instance.behaviorClass),
        isEmptyState = false,
        usabilitySignal = usabilitySignalFor(evidence, emptyState = false),
    )
}

private fun emptyStateOutput(
    input: AreaTodayOutputInput,
    evidence: AreaEvidenceProfile,
    sourceTruth: AreaSourceTruth,
    freshnessBand: AreaFreshnessBand,
    contract: AreaEmptyStateContract,
): AreaTodayOutput {
    return AreaTodayOutput(
        instanceId = input.instance.instanceId,
        date = input.snapshot?.date ?: input.logicalDate,
        generatedAt = input.generatedAt,
        behaviorClass = input.instance.behaviorClass,
        headline = contract.headline,
        statusLabel = contract.statusLabel,
        recommendation = contract.recommendation,
        nextMeaningfulStep = AreaNextMeaningfulStep(
            kind = contract.fallbackStepKind,
            label = contract.fallbackStepLabel,
            status = AreaStepStatus.EMPTY,
            origin = AreaStepOrigin.projected_empty_state,
            isUserConfirmed = false,
            fallbackLabel = contract.fallbackStepLabel,
        ),
        evidenceSummary = evidenceSummary(input, evidence, sourceTruth),
        sourceTruth = sourceTruth,
        confidence = 0.18f,
        freshnessAt = evidence.latestEvidenceAt,
        freshnessBand = freshnessBand,
        severity = contract.fallbackSeverity,
        singleDockKind = contract.fallbackDockKind,
        isEmptyState = true,
        usabilitySignal = AreaUsabilitySignal.EMPTY,
    )
}

private fun projectedStepFor(
    input: AreaTodayOutputInput,
    fallbackStatus: AreaStepStatus,
): AreaNextMeaningfulStep {
    val openPlan = input.openPlanTitles.firstOrNull()
    val fallback = defaultEmptyStateContract(input.instance.behaviorClass)
    return when (input.instance.behaviorClass) {
        AreaBehaviorClass.TRACKING -> AreaNextMeaningfulStep(
            kind = AreaStepKind.observe,
            label = if (fallbackStatus == AreaStepStatus.STALE) {
                "Zustand noch einmal pruefen"
            } else {
                "Heutigen Zustand erneut erfassen"
            },
            status = fallbackStatus,
            origin = AreaStepOrigin.projected_from_signal,
            isUserConfirmed = false,
            fallbackLabel = fallback.fallbackStepLabel,
        )
        AreaBehaviorClass.PROGRESS -> AreaNextMeaningfulStep(
            kind = AreaStepKind.do_step,
            label = openPlan ?: "Naechsten Fortschrittsschritt festlegen",
            status = if (openPlan == null) AreaStepStatus.EMPTY else fallbackStatus,
            origin = if (openPlan == null) AreaStepOrigin.projected_empty_state else AreaStepOrigin.projected_from_plan,
            isUserConfirmed = false,
            fallbackLabel = fallback.fallbackStepLabel,
        )
        AreaBehaviorClass.RELATIONSHIP -> AreaNextMeaningfulStep(
            kind = AreaStepKind.contact,
            label = "Einen kleinen Kontaktimpuls setzen",
            status = fallbackStatus,
            origin = AreaStepOrigin.projected_from_signal,
            isUserConfirmed = false,
            fallbackLabel = fallback.fallbackStepLabel,
        )
        AreaBehaviorClass.MAINTENANCE -> AreaNextMeaningfulStep(
            kind = AreaStepKind.maintain,
            label = openPlan ?: "Offenen Pflegepunkt verankern",
            status = if (openPlan == null) AreaStepStatus.EMPTY else fallbackStatus,
            origin = if (openPlan == null) AreaStepOrigin.projected_empty_state else AreaStepOrigin.projected_from_plan,
            isUserConfirmed = false,
            fallbackLabel = fallback.fallbackStepLabel,
        )
        AreaBehaviorClass.PROTECTION -> AreaNextMeaningfulStep(
            kind = AreaStepKind.protect,
            label = "Einen konkreten Schutzzug setzen",
            status = fallbackStatus,
            origin = AreaStepOrigin.projected_from_signal,
            isUserConfirmed = false,
            fallbackLabel = fallback.fallbackStepLabel,
        )
        AreaBehaviorClass.REFLECTION -> AreaNextMeaningfulStep(
            kind = AreaStepKind.reflect,
            label = "Kurzen Lesepunkt setzen",
            status = fallbackStatus,
            origin = AreaStepOrigin.projected_from_signal,
            isUserConfirmed = false,
            fallbackLabel = fallback.fallbackStepLabel,
        )
    }
}

private fun resolveSourceTruth(
    evidence: AreaEvidenceProfile,
): AreaSourceTruth {
    return when {
        evidence.hasManualAnchor && evidence.evidenceCount > 1 -> AreaSourceTruth.manual_plus_local
        evidence.hasManualAnchor -> AreaSourceTruth.manual
        evidence.evidenceCount > 0 -> AreaSourceTruth.local_derived
        else -> AreaSourceTruth.missing
    }
}

private fun severityFor(
    input: AreaTodayOutputInput,
    emptyState: Boolean,
    evidence: AreaEvidenceProfile,
): AreaSeverity {
    if (emptyState) {
        return defaultEmptyStateContract(input.instance.behaviorClass).fallbackSeverity
    }
    return when (input.instance.behaviorClass) {
        AreaBehaviorClass.TRACKING -> when {
            input.snapshot?.manualScore != null && input.snapshot.manualScore <= 2 -> AreaSeverity.MEDIUM
            evidence.hasFreshEvidence -> AreaSeverity.LOW
            else -> AreaSeverity.NEUTRAL
        }
        AreaBehaviorClass.PROGRESS -> when {
            input.openPlanTitles.isNotEmpty() -> AreaSeverity.MEDIUM
            else -> AreaSeverity.LOW
        }
        AreaBehaviorClass.RELATIONSHIP -> AreaSeverity.LOW
        AreaBehaviorClass.MAINTENANCE -> if (input.dueCount > 0 || input.openPlanTitles.isNotEmpty()) {
            AreaSeverity.MEDIUM
        } else {
            AreaSeverity.LOW
        }
        AreaBehaviorClass.PROTECTION -> when {
            input.snapshot?.manualScore != null && input.snapshot.manualScore <= 2 -> AreaSeverity.HIGH
            input.snapshot?.manualStateKey?.contains("fragil", ignoreCase = true) == true -> AreaSeverity.HIGH
            else -> AreaSeverity.MEDIUM
        }
        AreaBehaviorClass.REFLECTION -> AreaSeverity.NEUTRAL
    }
}

private fun evidenceSummary(
    input: AreaTodayOutputInput,
    evidence: AreaEvidenceProfile,
    sourceTruth: AreaSourceTruth,
): String {
    val noteExcerpt = input.snapshot?.manualNote
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.let(::truncateAreaNote)
    return when (sourceTruth) {
        AreaSourceTruth.manual -> noteExcerpt
            ?.let { "Manueller Anker: \"$it\"" }
            ?: "Manueller Anker traegt die heutige Lesart."
        AreaSourceTruth.manual_plus_local -> noteExcerpt
            ?.let { "Manueller Anker \"$it\" wird lokal gestuetzt." }
            ?: "Manueller Anker wird von lokaler Evidenz gestuetzt."
        AreaSourceTruth.local_derived -> {
            val lead = input.openPlanTitles.firstOrNull() ?: input.instance.selectedTracks.firstOrNull() ?: "Lokale Spuren"
            "$lead traegt die heutige Lesart lokal."
        }
        AreaSourceTruth.missing -> evidence.stalenessReason ?: "Noch keine belastbare Evidenz vorhanden."
    }
}

private fun recommendationForConfirmedStep(
    behaviorClass: AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> "Der naechste Beobachtungsschritt ist bereits klar."
        AreaBehaviorClass.PROGRESS -> "Der Bereich hat schon einen bestaetigten Zug."
        AreaBehaviorClass.RELATIONSHIP -> "Der naechste soziale Impuls ist bereits festgelegt."
        AreaBehaviorClass.MAINTENANCE -> "Der Erhaltungszug ist bereits benoetigt und benannt."
        AreaBehaviorClass.PROTECTION -> "Der Schutzschritt ist bereits konkret festgelegt."
        AreaBehaviorClass.REFLECTION -> "Der naechste Lesepunkt ist bereits bewusst gesetzt."
    }
}

private fun manualRecommendation(
    behaviorClass: AreaBehaviorClass,
    snapshot: AreaSnapshot,
): String {
    val hasNote = !snapshot.manualNote.isNullOrBlank()
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> if (hasNote) {
            "Der heutige Zustand ist notiert und laesst sich zusammen mit der Beobachtung sauber lesen."
        } else {
            "Der heutige Zustand ist gesetzt und laesst sich jetzt sauber lesen."
        }
        AreaBehaviorClass.PROGRESS -> if (hasNote) {
            "Der Fortschrittsstand ist beschrieben und kann in einen klaren Arbeitszug uebersetzt werden."
        } else {
            "Der Bereich hat einen benannten Stand und kann in einen klaren Zug uebersetzt werden."
        }
        AreaBehaviorClass.RELATIONSHIP -> if (hasNote) {
            "Die Beziehung hat einen benannten Ton und braucht eher einen passenden Impuls als mehr KPI."
        } else {
            "Die Beziehungslage ist benannt und braucht nur einen kleinen naechsten Impuls."
        }
        AreaBehaviorClass.MAINTENANCE -> if (hasNote) {
            "Der Pflegekontext ist beschrieben und laesst sich jetzt in einen tragbaren Erhaltungszug uebersetzen."
        } else {
            "Der Pflegezustand ist sichtbar und kann jetzt konkret gehalten werden."
        }
        AreaBehaviorClass.PROTECTION -> if (hasNote) {
            "Die Schutzlage ist beschrieben und braucht heute einen klaren Rueckkehr- oder Schutzpfad."
        } else {
            "Die Schutzlage ist benannt und kann jetzt gezielt abgesichert werden."
        }
        AreaBehaviorClass.REFLECTION -> if (hasNote) {
            "Der Lesepunkt ist beschrieben und darf ruhig weitergefuehrt werden, ohne gleich zur Aufgabe zu werden."
        } else {
            "Der Lesepunkt ist gesetzt und kann ruhig weitergefuehrt werden."
        }
    }
}

private fun localEvidenceRecommendation(
    behaviorClass: AreaBehaviorClass,
    fresh: Boolean,
): String {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> if (fresh) {
            "Die letzten Signale deuten auf einen lesbaren Trend hin."
        } else {
            "Der Trend ist noch sichtbar, braucht aber bald eine frische Beobachtung."
        }
        AreaBehaviorClass.PROGRESS -> if (fresh) {
            "Es gibt bereits konkrete Bewegung; wichtig ist jetzt der naechste Zug."
        } else {
            "Es ist noch Fortschritt erkennbar, aber ohne neuen Zug beginnt er zu kippen."
        }
        AreaBehaviorClass.RELATIONSHIP -> if (fresh) {
            "Es gibt einen frischen menschlichen Anker; die Beziehung wirkt ansprechbar."
        } else {
            "Der letzte Impuls traegt noch, aber die Beziehung sollte bald wieder bewusst beruehrt werden."
        }
        AreaBehaviorClass.MAINTENANCE -> if (fresh) {
            "Offene oder faellige Pflegepunkte sind klar; der Bereich braucht heute Haltung statt Deutung."
        } else {
            "Es gibt noch offene Pflegepunkte, aber der Anker wird alt und sollte erneuert werden."
        }
        AreaBehaviorClass.PROTECTION -> if (fresh) {
            "Warnzeichen sind sichtbar; Schutz sollte heute frueh greifen."
        } else {
            "Die Schutzlage ist noch lesbar, aber der Rueckweg sollte neu gesetzt werden."
        }
        AreaBehaviorClass.REFLECTION -> if (fresh) {
            "Es gibt einen sanften Lesepunkt; mehr Druck waere hier falsch."
        } else {
            "Der Lesepunkt ist noch da, verliert aber ohne frische Rueckkehr an Klarheit."
        }
    }
}

private fun manualNoteStatusLabel(
    behaviorClass: AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> "Notiz gesetzt"
        AreaBehaviorClass.PROGRESS -> "Stand notiert"
        AreaBehaviorClass.RELATIONSHIP -> "Impuls notiert"
        AreaBehaviorClass.MAINTENANCE -> "Pflege notiert"
        AreaBehaviorClass.PROTECTION -> "Schutz notiert"
        AreaBehaviorClass.REFLECTION -> "Lesepunkt notiert"
    }
}

private fun truncateAreaNote(
    note: String,
): String {
    return if (note.length <= 72) {
        note
    } else {
        note.take(69).trimEnd() + "..."
    }
}

private fun dockKindFor(
    behaviorClass: AreaBehaviorClass,
): AreaTodayDockKind {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> AreaTodayDockKind.STATUS
        AreaBehaviorClass.PROGRESS -> AreaTodayDockKind.ACTION
        AreaBehaviorClass.RELATIONSHIP -> AreaTodayDockKind.CARE
        AreaBehaviorClass.MAINTENANCE -> AreaTodayDockKind.ACTION
        AreaBehaviorClass.PROTECTION -> AreaTodayDockKind.SHIELD
        AreaBehaviorClass.REFLECTION -> AreaTodayDockKind.REFLECTION
    }
}

private fun usabilitySignalFor(
    evidence: AreaEvidenceProfile,
    emptyState: Boolean,
): AreaUsabilitySignal {
    return when {
        emptyState -> AreaUsabilitySignal.EMPTY
        evidence.hasManualAnchor && evidence.hasFreshEvidence -> AreaUsabilitySignal.STRONG
        evidence.evidenceCount >= 2 -> AreaUsabilitySignal.USEFUL
        else -> AreaUsabilitySignal.WEAK
    }
}

private fun ensureDistinctRecommendation(
    recommendation: String,
    step: AreaNextMeaningfulStep,
): AreaNextMeaningfulStep {
    return if (recommendation.equals(step.label, ignoreCase = true)) {
        step.copy(label = step.fallbackLabel)
    } else {
        step
    }
}
