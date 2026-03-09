package com.struperto.androidappdays.feature.single.home

import com.struperto.androidappdays.data.repository.CoachSuggestion
import com.struperto.androidappdays.data.repository.DateContext
import com.struperto.androidappdays.data.repository.DayModelInput
import com.struperto.androidappdays.data.repository.DayRisk
import com.struperto.androidappdays.data.repository.LearningEvent
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.SignalEnvelope
import com.struperto.androidappdays.data.repository.SignalKind
import com.struperto.androidappdays.data.repository.SollDayLayer
import com.struperto.androidappdays.data.repository.SollDayLayerType
import com.struperto.androidappdays.data.repository.SollDayModel
import com.struperto.androidappdays.data.repository.SollDaySegment
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.data.repository.UserFingerprint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

interface DayModelEngine {
    fun project(input: DayModelInput): SollDayModel
}

class LocalDayModelEngine : DayModelEngine {
    override fun project(input: DayModelInput): SollDayModel {
        val fingerprint = input.userFingerprint
        val context = input.dateContext
        val effectiveEndHour = effectiveDayEndHour(fingerprint)
        val currentHour = currentLogicalHour(
            nowHour = context.now.hour,
            dayStartHour = fingerprint.dayStartHour,
        )
        val assignedPlans = assignPlansToHours(
            fingerprint = fingerprint,
            date = context.date,
            plans = input.plans,
        )
        val topPriorities = deriveTopPriorities(
            fingerprint = fingerprint,
            plans = input.plans,
        )
        val segments = (fingerprint.dayStartHour until effectiveEndHour).map { hour ->
            val segmentId = segmentId(context.date, hour)
            val linkedSignals = input.signals.filter { overlapsHour(it, hour, context.date, context.zoneId) }
            val linkedPlans = assignedPlans[hour].orEmpty()
            val baseline = baselineIntensity(
                hour = hour,
                fingerprint = fingerprint,
                date = context.date,
            )
            val commitment = commitmentIntensity(linkedSignals, linkedPlans)
            val focusShield = focusIntensity(
                hour = hour,
                fingerprint = fingerprint,
                topPriorities = topPriorities,
                linkedPlans = linkedPlans,
            )
            val flex = flexIntensity(
                hour = hour,
                fingerprint = fingerprint,
                linkedSignals = linkedSignals,
            )
            val pressure = pressureIntensity(
                fingerprint = fingerprint,
                linkedSignals = linkedSignals,
            )
            val actual = actualIntensity(
                context = context,
                linkedSignals = linkedSignals,
                linkedPlans = linkedPlans,
            )
            val target = (baseline + commitment + focusShield + flex).coerceIn(0.12f, 1f)
            val drift = (actual - target).coerceIn(-1f, 1f)
            val reasons = buildReasons(
                hour = hour,
                fingerprint = fingerprint,
                linkedSignals = linkedSignals,
                linkedPlans = linkedPlans,
                pressure = pressure,
                target = target,
            )
            val suggestion = buildCoachSuggestion(
                segmentId = segmentId,
                hour = hour,
                drift = drift,
                pressure = pressure,
                linkedPlans = linkedPlans,
                linkedSignals = linkedSignals,
            )
            val layers = listOf(
                SollDayLayer(SollDayLayerType.BASELINE, "Basis-Soll", baseline),
                SollDayLayer(SollDayLayerType.COMMITMENT, "Verpflichtung", commitment),
                SollDayLayer(SollDayLayerType.PROTECTED_FOCUS, "Schutz", focusShield),
                SollDayLayer(SollDayLayerType.FLEX, "Flex", flex),
                SollDayLayer(SollDayLayerType.INCOMING_PRESSURE, "Druck", pressure),
                SollDayLayer(SollDayLayerType.ACTUAL, "Ist", actual),
                SollDayLayer(SollDayLayerType.DRIFT, "Drift", drift.absoluteValue.coerceIn(0f, 1f)),
            ).filter { it.intensity > 0.02f }
            SollDaySegment(
                id = segmentId,
                startHour = hour,
                endHour = hour + 1,
                label = displayHourLabel(hour),
                targetLoad = target,
                actualLoad = actual,
                drift = drift,
                primaryFocus = primaryFocusLabel(
                    linkedPlans = linkedPlans,
                    linkedSignals = linkedSignals,
                    hour = hour,
                ),
                layers = layers,
                linkedSignals = linkedSignals,
                linkedPlanItems = linkedPlans,
                reasons = reasons,
                learningHint = learningHint(
                    hour = hour,
                    learningEvents = input.recentBehavior.learningEvents,
                    linkedPlans = linkedPlans,
                    pressure = pressure,
                ),
                coachSuggestion = suggestion,
                isCurrent = currentHour == hour,
            )
        }
        val risks = deriveRisks(segments)
        val coachSuggestions = segments.mapNotNull(SollDaySegment::coachSuggestion)
            .sortedByDescending(CoachSuggestion::intensity)
            .take(3)
        val fitScore = computeFitScore(
            segments = segments,
            priorities = topPriorities,
            plans = input.plans,
        )
        val fitLabel = when {
            fitScore >= 0.74f -> "auf Spur"
            fitScore >= 0.54f -> "anfällig"
            else -> "unter Druck"
        }
        val thesis = buildThesis(
            fingerprint = fingerprint,
            fitLabel = fitLabel,
            topPriorities = topPriorities,
            risks = risks,
        )
        return SollDayModel(
            date = context.date,
            fingerprint = fingerprint,
            thesis = thesis,
            fitScore = fitScore,
            fitLabel = fitLabel,
            topPriorities = topPriorities,
            risks = risks,
            coachSuggestions = coachSuggestions,
            segments = segments,
        )
    }
}

private fun assignPlansToHours(
    fingerprint: UserFingerprint,
    date: LocalDate,
    plans: List<PlanItem>,
): Map<Int, List<PlanItem>> {
    val effectiveEndHour = effectiveDayEndHour(fingerprint)
    val result = mutableMapOf<Int, MutableList<PlanItem>>()
    TimeBlock.all.forEach { block ->
        val range = hourRangeForTimeBlock(
            timeBlock = block,
            startHour = fingerprint.dayStartHour,
            endHour = effectiveEndHour,
        )
        val items = plans.filter { it.timeBlock == block }
        items.forEachIndexed { index, item ->
            val bucket = if (range.isEmpty()) {
                fingerprint.dayStartHour
            } else {
                range.elementAt(index % range.count())
            }
            result.getOrPut(bucket) { mutableListOf() } += item
        }
    }
    return result
}

private fun deriveTopPriorities(
    fingerprint: UserFingerprint,
    plans: List<PlanItem>,
): List<String> {
    val openPlans = plans.filterNot(PlanItem::isDone)
    if (openPlans.isNotEmpty()) {
        return openPlans.take(3).map(PlanItem::title)
    }
    val areaPriorities = fingerprint.lifeAreas
        .sortedByDescending(LifeArea::targetScore)
        .take(2)
        .map { it.label }
    return (fingerprint.priorityRules.take(2) + areaPriorities).distinct().take(3)
}

private fun baselineIntensity(
    hour: Int,
    fingerprint: UserFingerprint,
    date: LocalDate,
): Float {
    val weekend = date.dayOfWeek.value >= 6
    val energy = when {
        hour < 12 -> fingerprint.morningEnergy
        hour < 17 -> fingerprint.afternoonEnergy
        else -> fingerprint.eveningEnergy
    } / 5f
    val rhythm = when {
        hour in 7..10 -> 0.18f + (fingerprint.focusStrength * 0.06f)
        hour in 11..13 -> 0.16f + (fingerprint.afternoonEnergy * 0.03f)
        hour in 14..17 -> 0.2f + (fingerprint.afternoonEnergy * 0.05f)
        else -> 0.12f + (fingerprint.eveningEnergy * 0.04f) - (fingerprint.recoveryNeed * 0.03f)
    }
    val weekendLift = if (weekend && hour >= 18) -0.08f else if (weekend) -0.02f else 0f
    return (0.12f + (energy * 0.22f) + rhythm + weekendLift).coerceIn(0.08f, 0.8f)
}

private fun commitmentIntensity(
    linkedSignals: List<SignalEnvelope>,
    linkedPlans: List<PlanItem>,
): Float {
    val signalCommitment = linkedSignals
        .filter { it.kind == SignalKind.CALENDAR || it.kind == SignalKind.PLAN }
        .sumOf { it.intensity.toDouble() }
        .toFloat()
    val planCommitment = linkedPlans.size * 0.12f
    return (signalCommitment * 0.26f + planCommitment).coerceIn(0f, 0.46f)
}

private fun focusIntensity(
    hour: Int,
    fingerprint: UserFingerprint,
    topPriorities: List<String>,
    linkedPlans: List<PlanItem>,
): Float {
    val morningFocus = if (hour in 7..11) fingerprint.focusStrength * 0.05f else 0f
    val protectedPriority = when {
        linkedPlans.any { plan -> topPriorities.any { priority -> plan.title.contains(priority, ignoreCase = true) } } -> 0.2f
        hour in 8..10 && topPriorities.isNotEmpty() -> 0.16f
        else -> 0.08f
    }
    return (morningFocus + protectedPriority).coerceIn(0f, 0.42f)
}

private fun flexIntensity(
    hour: Int,
    fingerprint: UserFingerprint,
    linkedSignals: List<SignalEnvelope>,
): Float {
    val laterSignals = linkedSignals.count { it.kind == SignalKind.LATER }
    val captureSignals = linkedSignals.count { it.kind == SignalKind.CAPTURE }
    val coordinationLift = if (hour in 13..17) fingerprint.disruptionSensitivity * 0.03f else 0f
    return (laterSignals * 0.08f + captureSignals * 0.05f + coordinationLift).coerceIn(0f, 0.32f)
}

private fun pressureIntensity(
    fingerprint: UserFingerprint,
    linkedSignals: List<SignalEnvelope>,
): Float {
    val incoming = linkedSignals.count { it.kind == SignalKind.NOTIFICATION }
    val openSignals = linkedSignals.count { it.kind == SignalKind.CAPTURE }
    val sensitivity = fingerprint.disruptionSensitivity * 0.05f
    return (incoming * 0.1f + openSignals * 0.06f + sensitivity).coerceIn(0f, 0.5f)
}

private fun actualIntensity(
    context: DateContext,
    linkedSignals: List<SignalEnvelope>,
    linkedPlans: List<PlanItem>,
): Float {
    val donePlans = linkedPlans.count(PlanItem::isDone) * 0.18f
    val openPlans = linkedPlans.count { !it.isDone } * 0.07f
    val currentCommitments = linkedSignals
        .filter { signal ->
            signal.kind == SignalKind.CALENDAR &&
                signal.startMillis <= context.date.atTime(context.now).atZone(context.zoneId).toInstant().toEpochMilli() &&
                (signal.endMillis ?: Long.MAX_VALUE) > context.date.atTime(context.now).atZone(context.zoneId).toInstant().toEpochMilli()
        }
        .sumOf { it.intensity.toDouble() }
        .toFloat() * 0.18f
    val incoming = linkedSignals.count { it.kind == SignalKind.NOTIFICATION } * 0.04f
    return (0.05f + donePlans + openPlans + currentCommitments + incoming).coerceIn(0.04f, 1f)
}

private fun buildReasons(
    hour: Int,
    fingerprint: UserFingerprint,
    linkedSignals: List<SignalEnvelope>,
    linkedPlans: List<PlanItem>,
    pressure: Float,
    target: Float,
): List<String> {
    val reasons = mutableListOf<String>()
    if (linkedPlans.isNotEmpty()) {
        reasons += "${linkedPlans.size} Planpunkte liegen hier."
    }
    val calendarCount = linkedSignals.count { it.kind == SignalKind.CALENDAR }
    if (calendarCount > 0) {
        reasons += if (calendarCount == 1) {
            "Ein fester Termin rahmt diese Stunde."
        } else {
            "$calendarCount feste Termine verdichten diese Zone."
        }
    }
    val notificationCount = linkedSignals.count { it.kind == SignalKind.NOTIFICATION }
    if (notificationCount > 0) {
        reasons += "$notificationCount eingehende Stoerungen muessen hier mitgedacht werden."
    }
    if (hour in 7..11 && fingerprint.focusStrength >= 4) {
        reasons += "Der Fingerprint will dieses Fenster eher schuetzen."
    }
    if (pressure > 0.24f) {
        reasons += "Der eingehende Druck ist hier klar sichtbar."
    }
    if (target < 0.32f && hour >= 19) {
        reasons += "Der Abend soll bewusst leichter bleiben."
    }
    return reasons.distinct().take(4)
}

private fun buildCoachSuggestion(
    segmentId: String,
    hour: Int,
    drift: Float,
    pressure: Float,
    linkedPlans: List<PlanItem>,
    linkedSignals: List<SignalEnvelope>,
): CoachSuggestion? {
    return when {
        pressure > 0.32f && linkedSignals.any { it.kind == SignalKind.NOTIFICATION } -> {
            CoachSuggestion(
                title = "Stoerquellen abfangen",
                detail = "Schirme diese Stunde kurz ab und entscheide bewusst, was jetzt wirklich rein darf.",
                segmentId = segmentId,
                intensity = (pressure + drift.absoluteValue).coerceIn(0.2f, 1f),
            )
        }
        drift < -0.2f && linkedPlans.isNotEmpty() -> {
            CoachSuggestion(
                title = "Soll aktivieren",
                detail = "Der Slot ist unter Soll. Ziehe den wichtigsten Planpunkt bewusst in diese Stunde.",
                segmentId = segmentId,
                intensity = drift.absoluteValue.coerceIn(0.2f, 1f),
            )
        }
        drift > 0.22f -> {
            CoachSuggestion(
                title = "Last senken",
                detail = "Diese Stunde ist voller als vorgesehen. Schiebe einen Punkt oder lasse bewusst etwas spaeter landen.",
                segmentId = segmentId,
                intensity = drift.coerceIn(0.2f, 1f),
            )
        }
        hour >= 18 && linkedPlans.any { !it.isDone } -> {
            CoachSuggestion(
                title = "Tag sauber schliessen",
                detail = "Offene Punkte am Abend als Abschluss oder bewusstes Spaeter markieren.",
                segmentId = segmentId,
                intensity = 0.42f,
            )
        }
        else -> null
    }
}

private fun learningHint(
    hour: Int,
    learningEvents: List<LearningEvent>,
    linkedPlans: List<PlanItem>,
    pressure: Float,
): String {
    val recentMoves = learningEvents.count { it.type == LearningEventType.PLAN_MOVED || it.type == LearningEventType.QUICK_ADD_NOW }
    return when {
        pressure > 0.32f -> "Wenn du diese Stunde jetzt abschirmst, lernt das Modell deinen echten Stoerschutz."
        linkedPlans.any { !it.isDone } && hour in 7..11 -> "Ein bewusst geschuetzter Start schaerft dein Fokusprofil."
        recentMoves >= 3 -> "Mehrere Umbauten zuletzt: jede Verschiebung kalibriert deinen Tagesrhythmus."
        else -> "Wenn du diesen Slot justierst, verdichtet sich dein Fingerprint fuer kuenftige Tage."
    }
}

private fun deriveRisks(segments: List<SollDaySegment>): List<DayRisk> {
    return segments.mapNotNull { segment ->
        when {
            segment.layers.any { it.type == SollDayLayerType.INCOMING_PRESSURE && it.intensity > 0.32f } -> {
                DayRisk(
                    title = "Stoerdruck",
                    detail = "${segment.label} steht unter viel eingehendem Druck.",
                    severity = segment.layers.first { it.type == SollDayLayerType.INCOMING_PRESSURE }.intensity,
                    segmentId = segment.id,
                )
            }
            segment.drift > 0.24f -> {
                DayRisk(
                    title = "Ueberladung",
                    detail = "${segment.label} ist voller als dein Soll fuer heute.",
                    severity = segment.drift.absoluteValue,
                    segmentId = segment.id,
                )
            }
            segment.drift < -0.22f && segment.linkedPlanItems.isNotEmpty() -> {
                DayRisk(
                    title = "Soll fehlt im Takt",
                    detail = "${segment.label} traegt zu wenig vom geplanten Schwerpunkt.",
                    severity = segment.drift.absoluteValue,
                    segmentId = segment.id,
                )
            }
            else -> null
        }
    }.sortedByDescending(DayRisk::severity)
        .take(3)
}

private fun computeFitScore(
    segments: List<SollDaySegment>,
    priorities: List<String>,
    plans: List<PlanItem>,
): Float {
    val driftPenalty = segments.map(SollDaySegment::drift).average().toFloat().absoluteValue * 0.45f
    val openPriorityPenalty = priorities.count { priority ->
        plans.any { !it.isDone && it.title.contains(priority, ignoreCase = true) }
    } * 0.08f
    val protectionBonus = segments.count { segment ->
        segment.layers.any { it.type == SollDayLayerType.PROTECTED_FOCUS && it.intensity > 0.2f }
    } * 0.02f
    return (0.82f - driftPenalty - openPriorityPenalty + protectionBonus).coerceIn(0.08f, 0.98f)
}

private fun buildThesis(
    fingerprint: UserFingerprint,
    fitLabel: String,
    topPriorities: List<String>,
    risks: List<DayRisk>,
): String {
    val leadPriority = topPriorities.firstOrNull() ?: fingerprint.lifeAreas.maxByOrNull { it.targetScore }?.label.orEmpty()
    val risk = risks.firstOrNull()?.title?.lowercase().orEmpty()
    return when {
        risk.isNotBlank() -> "$leadPriority soll heute geschuetzt werden, obwohl $risk bereits sichtbar ist."
        fitLabel == "auf Spur" -> "$leadPriority gibt dem Tag Richtung und das Soll ist bereits stimmig angelegt."
        else -> "$leadPriority braucht heute ein bewussteres Geruest, damit der Tag nicht zerfaellt."
    }
}

private fun primaryFocusLabel(
    linkedPlans: List<PlanItem>,
    linkedSignals: List<SignalEnvelope>,
    hour: Int,
): String {
    return linkedPlans.firstOrNull()?.title?.let(::compactFocusLabel)
        ?: linkedSignals.firstOrNull()?.let(::signalFocusLabel)
        ?: when {
            hour in 6..8 -> "Klar starten"
            hour in 9..11 -> "Fokus halten"
            hour in 12..14 -> "Mittag halten"
            hour in 15..17 -> "Im Fluss bleiben"
            hour in 18..21 -> "Tag schliessen"
            else -> "Runterfahren"
        }
}

private fun compactFocusLabel(
    raw: String,
): String {
    return raw
        .replace(Regex("\\s+"), " ")
        .trim()
        .removePrefix("-")
        .removePrefix("•")
        .take(30)
        .ifBlank { "Diese Stunde" }
}

private fun signalFocusLabel(
    signal: SignalEnvelope,
): String {
    return when (signal.kind) {
        SignalKind.CALENDAR -> compactFocusLabel(signal.title).ifBlank { "Termin" }
        SignalKind.NOTIFICATION -> "Eingaenge klaeren"
        SignalKind.CAPTURE -> "Gedanken sichern"
        SignalKind.PLAN -> compactFocusLabel(signal.title)
        SignalKind.LATER -> "Spaeter sortieren"
    }
}

private fun overlapsHour(
    signal: SignalEnvelope,
    hour: Int,
    date: LocalDate,
    zoneId: ZoneId,
): Boolean {
    val start = logicalHourToEpochMillis(
        date = date,
        hour = hour,
        zoneId = zoneId,
    )
    val end = logicalHourToEpochMillis(
        date = date,
        hour = hour + 1,
        zoneId = zoneId,
    )
    val signalEnd = signal.endMillis ?: (signal.startMillis + 3_600_000L)
    return signal.startMillis < end && signalEnd > start
}

private fun hourRangeForTimeBlock(
    timeBlock: TimeBlock,
    startHour: Int,
    endHour: Int,
): IntRange {
    val range = when (timeBlock) {
        TimeBlock.MORGEN -> max(startHour, 6)..min(endHour - 1, 9)
        TimeBlock.MITTAG -> max(startHour, 10)..min(endHour - 1, 12)
        TimeBlock.NACHMITTAG -> max(startHour, 13)..min(endHour - 1, 17)
        TimeBlock.ABEND -> max(startHour, 18)..(endHour - 1)
    }
    return if (range.first > range.last) IntRange.EMPTY else range
}

private fun segmentId(
    date: LocalDate,
    hour: Int,
): String = "${date}_$hour"

private fun effectiveDayEndHour(
    fingerprint: UserFingerprint,
): Int {
    return max(
        fingerprint.dayEndHour,
        fingerprint.dayStartHour + 24,
    )
}

private fun currentLogicalHour(
    nowHour: Int,
    dayStartHour: Int,
): Int {
    return if (nowHour < dayStartHour) nowHour + 24 else nowHour
}

private fun displayHourLabel(
    hour: Int,
): String {
    return "%02d:00".format(normalizeHour(hour))
}

private fun logicalHourToEpochMillis(
    date: LocalDate,
    hour: Int,
    zoneId: ZoneId,
): Long {
    val dayOffset = hour.floorDiv(24)
    return date
        .plusDays(dayOffset.toLong())
        .atTime(normalizeHour(hour), 0)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}

private fun normalizeHour(
    hour: Int,
): Int {
    return hour.mod(24)
}
