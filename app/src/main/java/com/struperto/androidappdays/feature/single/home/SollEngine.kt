package com.struperto.androidappdays.feature.single.home

import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.NotificationSignal
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.data.repository.Vorhaben
import com.struperto.androidappdays.feature.single.model.HomeTrackWindow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

data class SollProfile(
    val name: String,
    val blockWeights: Map<HomeTrackWindow, Float>,
    val averageTargetScore: Float,
    val completionBias: Float,
    val axes: SollAxes,
)

data class SollAxes(
    val focusPressure: Float,
    val disruptionSensitivity: Float,
    val energyNeed: Float,
)

data class HomeDayTrack(
    val today: LocalDate,
    val profile: SollProfile,
    val blocks: List<HomeDayTrackBlock>,
)

data class HomeDayTrackBlock(
    val window: HomeTrackWindow,
    val targetFill: Float,
    val actualFill: Float,
    val drift: Float,
    val focusHint: String,
    val reasons: List<String>,
    val calendarSignals: List<CalendarSignal>,
    val notificationSignals: List<NotificationSignal>,
    val planItems: List<PlanItem>,
    val captureItems: List<CaptureItem>,
    val laterItems: List<Vorhaben>,
    val isCurrent: Boolean,
)

data class HomeBlockDetail(
    val window: HomeTrackWindow,
    val focusHint: String,
    val targetFill: Float,
    val actualFill: Float,
    val drift: Float,
    val reasons: List<String>,
    val calendarSignals: List<CalendarSignal>,
    val notificationSignals: List<NotificationSignal>,
    val planItems: List<PlanItem>,
    val captureItems: List<CaptureItem>,
    val laterItems: List<Vorhaben>,
)

data class SollEngineInput(
    val today: LocalDate,
    val now: LocalTime,
    val zoneId: ZoneId,
    val activeAreas: List<LifeArea>,
    val calendarSignals: List<CalendarSignal>,
    val notificationSignals: List<NotificationSignal>,
    val openCaptures: List<CaptureItem>,
    val activeVorhaben: List<Vorhaben>,
    val todayPlans: List<PlanItem>,
    val recentPlans: List<PlanItem>,
)

private interface SignalSource {
    fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    )
}

interface SollEngine {
    fun project(input: SollEngineInput): HomeDayTrack
}

class LocalSollEngine : SollEngine {
    private val signalSources: List<SignalSource> = listOf(
        BaselineRhythmSignalSource(),
        LearningPatternSignalSource(),
        CarryoverSignalSource(),
        CalendarSignalSource(),
        NotificationSignalSource(),
        PlanSignalSource(),
        OpenLoopSignalSource(),
    )

    override fun project(input: SollEngineInput): HomeDayTrack {
        val profile = buildSollProfile(
            input = input,
        )
        val currentWindow = HomeTrackWindow.fromLocalTime(input.now)
        val accumulators = HomeTrackWindow.all.associateWith { window ->
            MutableBlockSignal(
                window = window,
                target = 0f,
                actual = emptyActualFloor(
                    window = window,
                    currentWindow = currentWindow,
                ),
                focusHint = defaultFocusHint(
                    window = window,
                    primaryAreaLabel = input.activeAreas.firstOrNull()?.label,
                    axes = profile.axes,
                ),
            )
        }.toMutableMap()

        signalSources.forEach { source ->
            source.apply(
                input = input,
                profile = profile,
                accumulators = accumulators,
            )
        }

        val plansByWindow = input.todayPlans.groupBy { HomeTrackWindow.fromTimeBlock(it.timeBlock) }
        val capturesByWindow = input.openCaptures.groupBy { capture ->
            HomeTrackWindow.fromLocalTime(
                Instant.ofEpochMilli(capture.createdAt).atZone(input.zoneId).toLocalTime(),
            )
        }
        val notificationSignalsByWindow = input.notificationSignals.groupBy { signal ->
            HomeTrackWindow.fromLocalTime(
                Instant.ofEpochMilli(signal.postedAt).atZone(input.zoneId).toLocalTime(),
            )
        }
        val unplannedVorhaben = input.activeVorhaben.filter { vorhaben ->
            input.todayPlans.none { it.vorhabenId == vorhaben.id }
        }
        val calendarSignalsByWindow = HomeTrackWindow.all.associateWith { window ->
            input.calendarSignals.filter { signal ->
                overlapsWindow(
                    signal = signal,
                    window = window,
                    today = input.today,
                    zoneId = input.zoneId,
                )
            }
        }

        return HomeDayTrack(
            today = input.today,
            profile = profile,
            blocks = HomeTrackWindow.all.map { window ->
                val signal = accumulators.getValue(window)
                val target = signal.target.coerceIn(0.14f, 1f)
                val actual = signal.actual.coerceIn(0.02f, 1f)
                val drift = (actual - target + signal.pressure).coerceIn(-1f, 1f)
                HomeDayTrackBlock(
                    window = window,
                    targetFill = target,
                    actualFill = actual,
                    drift = drift,
                    focusHint = signal.focusHint,
                    reasons = signal.reasons.distinct().take(3),
                    calendarSignals = calendarSignalsByWindow[window].orEmpty(),
                    notificationSignals = notificationSignalsByWindow[window].orEmpty(),
                    planItems = plansByWindow[window].orEmpty(),
                    captureItems = capturesByWindow[window].orEmpty(),
                    laterItems = when (window) {
                        HomeTrackWindow.VORMITTAG -> unplannedVorhaben.take(2)
                        HomeTrackWindow.NACHMITTAG -> unplannedVorhaben.drop(1).take(2)
                        HomeTrackWindow.ABEND -> unplannedVorhaben.drop(2).take(2)
                    },
                    isCurrent = window == currentWindow,
                )
            },
        )
    }
}

private class CarryoverSignalSource : SignalSource {
    override fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    ) {
        val yesterdayCarryover = input.recentPlans.filter { plan ->
            !plan.isDone && plan.plannedDate == input.today.minusDays(1).toString()
        }
        if (yesterdayCarryover.isEmpty()) return

        val total = yesterdayCarryover.size
        val currentWindow = HomeTrackWindow.fromLocalTime(input.now)
        val morningSignal = accumulators.getValue(HomeTrackWindow.VORMITTAG)
        morningSignal.target += min(0.18f, total * 0.05f)
        morningSignal.pressure += min(
            0.18f,
            total * (0.04f + (profile.axes.energyNeed * 0.04f)),
        )
        morningSignal.reasons += when {
            profile.axes.energyNeed > 0.62f -> "$total offene Punkte von gestern machen den Einstieg schwerer."
            profile.axes.focusPressure > 0.66f -> "$total offene Punkte von gestern wollen erst wieder auf Spur."
            else -> "$total offene Punkte tragen sich von gestern in heute."
        }
        if (morningSignal.focusHint == defaultFocusHint(HomeTrackWindow.VORMITTAG, input.activeAreas.firstOrNull()?.label, profile.axes)) {
            morningSignal.focusHint = if (profile.axes.focusPressure > 0.66f) {
                "Wieder auf Spur"
            } else {
                "Ordnen"
            }
        }

        if (currentWindow != HomeTrackWindow.VORMITTAG) {
            val currentSignal = accumulators.getValue(currentWindow)
            currentSignal.pressure += min(
                0.12f,
                total * (0.02f + (profile.axes.disruptionSensitivity * 0.03f)),
            )
            currentSignal.reasons += "Gestern Offenes ist bis hier noch spuerbar."
        }

        val byOriginalWindow = yesterdayCarryover.groupBy { HomeTrackWindow.fromTimeBlock(it.timeBlock) }
        byOriginalWindow.forEach { (window, items) ->
            val signal = accumulators.getValue(window)
            signal.reasons += when (window) {
                HomeTrackWindow.VORMITTAG -> "${items.size} davon stammen aus einem fruehen Block."
                HomeTrackWindow.NACHMITTAG -> "${items.size} davon wurden gestern tagsueber nicht geschlossen."
                HomeTrackWindow.ABEND -> "${items.size} davon haengen noch vom Abend nach."
            }
        }
    }
}

private class NotificationSignalSource : SignalSource {
    override fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    ) {
        if (input.notificationSignals.isEmpty()) return
        val currentWindow = HomeTrackWindow.fromLocalTime(input.now)
        val grouped = input.notificationSignals.groupBy { signal ->
            HomeTrackWindow.fromLocalTime(
                Instant.ofEpochMilli(signal.postedAt).atZone(input.zoneId).toLocalTime(),
            )
        }
        HomeTrackWindow.all.forEach { window ->
            val notifications = grouped[window].orEmpty()
            if (notifications.isEmpty()) return@forEach
            val signal = accumulators.getValue(window)
            val amount = notifications.size.toFloat()
            signal.actual += min(0.16f, amount * 0.03f)
            signal.pressure += min(
                0.2f,
                amount * (0.03f + (profile.axes.disruptionSensitivity * 0.04f)),
            )
            if (window == currentWindow && notifications.size >= 2) {
                signal.reasons += "${notifications.size} Benachrichtigungen druecken in diesen Block."
            } else {
                signal.reasons += "${notifications.size} Benachrichtigungen haengen an diesem Block."
            }
            if (profile.axes.disruptionSensitivity > 0.66f && signal.focusHint == defaultFocusHint(window, input.activeAreas.firstOrNull()?.label, profile.axes)) {
                signal.focusHint = "Abschirmen"
            }
        }
    }
}

private class CalendarSignalSource : SignalSource {
    override fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    ) {
        if (input.calendarSignals.isEmpty()) return
        val nowMillis = input.today.atTime(input.now).atZone(input.zoneId).toInstant().toEpochMilli()
        HomeTrackWindow.all.forEach { window ->
            val inWindow = input.calendarSignals.filter { signal ->
                overlapsWindow(
                    signal = signal,
                    window = window,
                    today = input.today,
                    zoneId = input.zoneId,
                )
            }
            if (inWindow.isEmpty()) return@forEach
            val signal = accumulators.getValue(window)
            val intensity = inWindow.sumOf { calendarWeight(it, input.today, input.zoneId).toDouble() }.toFloat()
            signal.target += min(0.26f, intensity * (0.18f + (profile.axes.disruptionSensitivity * 0.06f)))
            if (window == HomeTrackWindow.fromLocalTime(input.now)) {
                signal.actual += min(0.12f, intensity * 0.08f)
            }
            if (inWindow.any { it.startMillis <= nowMillis && it.endMillis > nowMillis }) {
                signal.pressure += min(0.12f, intensity * 0.06f)
                signal.reasons += "Gerade laeuft bereits Kalenderkontext in diesem Block."
            }
            if (signal.focusHint == defaultFocusHint(window, input.activeAreas.firstOrNull()?.label, profile.axes)) {
                signal.focusHint = inWindow.first().title.take(28)
            }
            signal.reasons += when {
                inWindow.any { it.isAllDay } -> "Ein ganztagiger Termin rahmt diesen Block mit."
                inWindow.size == 1 -> "Ein Kalendereintrag formt diesen Block."
                else -> "${inWindow.size} Kalendereintraege halten diesen Block enger."
            }
        }
    }
}

private class BaselineRhythmSignalSource : SignalSource {
    override fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    ) {
        HomeTrackWindow.all.forEach { window ->
            val signal = accumulators.getValue(window)
            val baseTarget = profile.blockWeights.getValue(window)
            signal.target += baseTarget
            signal.focusHint = signal.focusHint.ifBlank {
                defaultFocusHint(window, input.activeAreas.firstOrNull()?.label, profile.axes)
            }
            signal.reasons += baselineReason(
                window = window,
                profile = profile,
            )
        }
    }
}

private class LearningPatternSignalSource : SignalSource {
    override fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    ) {
        if (input.recentPlans.size < 3) return
        val grouped = input.recentPlans.groupBy { HomeTrackWindow.fromTimeBlock(it.timeBlock) }
        val total = input.recentPlans.size.toFloat()
        HomeTrackWindow.all.forEach { window ->
            val history = grouped[window].orEmpty()
            if (history.isEmpty()) return@forEach
            val share = history.size / total
            val completion = history.count { it.isDone }.toFloat() / history.size.toFloat()
            val signal = accumulators.getValue(window)
            signal.target += ((share * 0.12f) + (completion * 0.05f)).coerceAtMost(0.16f)
            if (share >= 0.34f) {
                signal.reasons += "Dein letzter Verlauf landet oft in diesem Block."
            }
            if (completion >= 0.66f) {
                signal.reasons += when (window) {
                    HomeTrackWindow.VORMITTAG -> "Hier klappt Schutz und Fokus meist am besten."
                    HomeTrackWindow.NACHMITTAG -> "Hier werden Dinge meist wirklich bewegt."
                    HomeTrackWindow.ABEND -> "Hier bekommst du den Tag oft sauber geschlossen."
                }
            }
        }
    }
}

private class PlanSignalSource : SignalSource {
    override fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    ) {
        val currentWindow = HomeTrackWindow.fromLocalTime(input.now)
        val grouped = input.todayPlans.groupBy { HomeTrackWindow.fromTimeBlock(it.timeBlock) }
        HomeTrackWindow.all.forEach { window ->
            val blockPlans = grouped[window].orEmpty()
            if (blockPlans.isEmpty()) return@forEach
            val signal = accumulators.getValue(window)
            val doneCount = blockPlans.count { it.isDone }
            val openCount = blockPlans.size - doneCount
            signal.target += min(0.42f, blockPlans.size * 0.16f)
            signal.actual += min(0.56f, doneCount * 0.2f)
            signal.actual += min(0.12f, openCount * 0.04f)
            signal.focusHint = blockPlans.first().title.take(26)
            signal.reasons += "${blockPlans.size} Punkte bereits im Block."
            if (doneCount > 0) {
                signal.reasons += "$doneCount erledigt."
            }
            if (openCount > 0 && window.order < currentWindow.order) {
                signal.pressure += min(
                    0.24f,
                    openCount * (0.06f + (profile.axes.energyNeed * 0.04f)),
                )
                signal.reasons += if (profile.axes.energyNeed > 0.62f) {
                    "Offener Uebertrag zieht in den restlichen Tag."
                } else {
                    "Es gibt noch offenen Uebertrag aus diesem Block."
                }
            }
        }
    }
}

private class OpenLoopSignalSource : SignalSource {
    override fun apply(
        input: SollEngineInput,
        profile: SollProfile,
        accumulators: MutableMap<HomeTrackWindow, MutableBlockSignal>,
    ) {
        val currentWindow = HomeTrackWindow.fromLocalTime(input.now)
        val capturesByWindow = input.openCaptures.groupBy { capture ->
            HomeTrackWindow.fromLocalTime(
                Instant.ofEpochMilli(capture.createdAt).atZone(input.zoneId).toLocalTime(),
            )
        }
        HomeTrackWindow.all.forEach { window ->
            val signal = accumulators.getValue(window)
            val captures = capturesByWindow[window].orEmpty()
            if (captures.isNotEmpty()) {
                signal.actual += min(0.22f, captures.size * 0.09f)
                signal.pressure += if (window == currentWindow) {
                    min(
                        0.18f,
                        captures.size * (0.04f + (profile.axes.disruptionSensitivity * 0.04f)),
                    )
                } else {
                    0f
                }
                signal.reasons += when {
                    profile.axes.disruptionSensitivity > 0.62f -> "${captures.size} neue Impulse brauchen hier Luft."
                    else -> "${captures.size} neue Impulse liegen im Block."
                }
            }
        }

        val plannedVorhabenIds = input.todayPlans.mapNotNull { it.vorhabenId }.toSet()
        val unplanned = input.activeVorhaben.filter { it.id !in plannedVorhabenIds }
        if (unplanned.isNotEmpty()) {
            val currentSignal = accumulators.getValue(currentWindow)
            currentSignal.target += min(0.18f, unplanned.size * 0.05f)
            currentSignal.pressure += min(
                0.16f,
                unplanned.size * (0.03f + (profile.axes.disruptionSensitivity * 0.03f)),
            )
            currentSignal.reasons += when {
                profile.axes.focusPressure > 0.66f -> "${unplanned.size} offene Themen konkurrieren mit Fokus."
                profile.axes.energyNeed > 0.62f -> "${unplanned.size} offene Themen ziehen merklich Energie."
                else -> "${unplanned.size} spaetere Themen ziehen noch an dir."
            }
            if (currentSignal.focusHint == defaultFocusHint(currentWindow, input.activeAreas.firstOrNull()?.label, profile.axes)) {
                currentSignal.focusHint = "Ordnen"
            }
            val eveningSignal = accumulators.getValue(HomeTrackWindow.ABEND)
            if (currentWindow != HomeTrackWindow.ABEND) {
                eveningSignal.target += min(0.14f, unplanned.size * 0.04f)
                eveningSignal.reasons += if (profile.axes.energyNeed > 0.58f) {
                    "Ein Teil davon sollte bewusst spaeter weich landen."
                } else {
                    "Ein Teil davon kann bewusst spaeter landen."
                }
            }
        }
    }
}

private data class MutableBlockSignal(
    val window: HomeTrackWindow,
    var target: Float,
    var actual: Float,
    var pressure: Float = 0f,
    var focusHint: String,
    val reasons: MutableList<String> = mutableListOf(),
)

private fun buildSollProfile(
    input: SollEngineInput,
): SollProfile {
    val today = input.today
    val activeAreas = input.activeAreas
    val recentPlans = input.recentPlans
    val isWeekend = today.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    val baseWeights = if (isWeekend) {
        mapOf(
            HomeTrackWindow.VORMITTAG to 0.28f,
            HomeTrackWindow.NACHMITTAG to 0.34f,
            HomeTrackWindow.ABEND to 0.26f,
        )
    } else {
        mapOf(
            HomeTrackWindow.VORMITTAG to 0.38f,
            HomeTrackWindow.NACHMITTAG to 0.48f,
            HomeTrackWindow.ABEND to 0.32f,
        )
    }
    val groupedHistory = recentPlans.groupBy { HomeTrackWindow.fromTimeBlock(it.timeBlock) }
    val totalHistory = recentPlans.size.toFloat().takeIf { it > 0f } ?: 1f
    val averageTarget = if (activeAreas.isEmpty()) 3.6f else activeAreas.map { it.targetScore }.average().toFloat()
    val completionBias = if (recentPlans.isEmpty()) {
        0.54f
    } else {
        recentPlans.count { it.isDone }.toFloat() / recentPlans.size.toFloat()
    }
    val scale = (0.78f + ((averageTarget / 5f) * 0.18f) + (completionBias * 0.08f)).coerceIn(0.72f, 1.04f)
    val currentPlannedVorhabenIds = input.todayPlans.mapNotNull { it.vorhabenId }.toSet()
    val unplannedLater = input.activeVorhaben.count { it.id !in currentPlannedVorhabenIds }
    val yesterdayCarryover = recentPlans.count { plan ->
        !plan.isDone && plan.plannedDate == today.minusDays(1).toString()
    }
    val openLoopRatio = min(1f, (input.openCaptures.size + unplannedLater + yesterdayCarryover) / 7f)
    val morningHistory = groupedHistory[HomeTrackWindow.VORMITTAG].orEmpty()
    val afternoonHistory = groupedHistory[HomeTrackWindow.NACHMITTAG].orEmpty()
    val eveningHistory = groupedHistory[HomeTrackWindow.ABEND].orEmpty()
    val morningCompletion = completionRatio(morningHistory)
    val afternoonCompletion = completionRatio(afternoonHistory)
    val eveningCompletion = completionRatio(eveningHistory)
    val currentPlanRatio = min(1f, input.todayPlans.count { !it.isDone }.toFloat() / 3f)
    val axes = SollAxes(
        focusPressure = (
            0.24f +
                ((averageTarget / 5f) * 0.26f) +
                (morningCompletion * 0.18f) +
                (afternoonCompletion * 0.08f) +
                if (isWeekend) -0.03f else 0.08f
            ).coerceIn(0.1f, 1f),
        disruptionSensitivity = (
            0.12f +
                (openLoopRatio * 0.42f) +
                ((1f - completionBias) * 0.18f) +
                (currentPlanRatio * 0.14f) +
                (min(1f, yesterdayCarryover / 3f) * 0.12f) +
                ((afternoonHistory.size / totalHistory) * 0.06f)
            ).coerceIn(0.08f, 1f),
        energyNeed = (
            0.16f +
                ((1f - completionBias) * 0.22f) +
                ((eveningHistory.size / totalHistory) * 0.1f) +
                (min(1f, yesterdayCarryover / 3f) * 0.08f) +
                if (isWeekend) 0.14f else 0.04f +
                (((5f - averageTarget) / 5f) * 0.12f) +
                ((1f - eveningCompletion) * 0.1f)
            ).coerceIn(0.1f, 1f),
    )

    val blockWeights = HomeTrackWindow.all.associateWith { window ->
        val base = baseWeights.getValue(window)
        val historyWeight = groupedHistory[window].orEmpty().size / totalHistory
        val completionWeight = groupedHistory[window].orEmpty().let { history ->
            if (history.isEmpty()) 0.52f else history.count { it.isDone }.toFloat() / history.size.toFloat()
        }
        val axisLift = when (window) {
            HomeTrackWindow.VORMITTAG -> (axes.focusPressure * 0.14f) - (axes.disruptionSensitivity * 0.04f) - (axes.energyNeed * 0.02f)
            HomeTrackWindow.NACHMITTAG -> (axes.disruptionSensitivity * 0.1f) + (axes.focusPressure * 0.04f) - (axes.energyNeed * 0.02f)
            HomeTrackWindow.ABEND -> (axes.disruptionSensitivity * 0.02f) - (axes.energyNeed * 0.12f) + if (isWeekend) 0.05f else -0.02f
        }
        ((base * 0.66f) + (historyWeight * 0.22f) + (completionWeight * 0.08f) + axisLift) * scale
    }

    val hasHistory = recentPlans.isNotEmpty()
    return SollProfile(
        name = when {
            axes.energyNeed > 0.72f -> "Schonend"
            axes.focusPressure > 0.7f && axes.disruptionSensitivity < 0.5f -> "Fokus"
            axes.disruptionSensitivity > 0.68f && axes.focusPressure < 0.52f -> "Flex"
            axes.disruptionSensitivity > 0.62f -> "Koordination"
            isWeekend -> "Weekend Soft"
            hasHistory -> "Kreativ"
            else -> "Balanced"
        },
        blockWeights = blockWeights,
        averageTargetScore = averageTarget,
        completionBias = completionBias,
        axes = axes,
    )
}

private fun emptyActualFloor(
    window: HomeTrackWindow,
    currentWindow: HomeTrackWindow,
): Float {
    return when {
        window == currentWindow -> 0.08f
        window.order < currentWindow.order -> 0.05f
        else -> 0.02f
    }
}

private fun defaultFocusHint(
    window: HomeTrackWindow,
    primaryAreaLabel: String?,
    axes: SollAxes,
): String {
    return when (window) {
        HomeTrackWindow.VORMITTAG -> when {
            axes.focusPressure > 0.68f -> primaryAreaLabel ?: "Fokus"
            axes.disruptionSensitivity > 0.64f -> "Sortieren"
            else -> primaryAreaLabel ?: "Fokus"
        }
        HomeTrackWindow.NACHMITTAG -> when {
            axes.disruptionSensitivity > 0.62f -> "Koordination"
            axes.focusPressure > 0.6f -> "Bewegen"
            else -> "Offen halten"
        }
        HomeTrackWindow.ABEND -> when {
            axes.energyNeed > 0.62f -> "Entlasten"
            else -> "Abschluss"
        }
    }
}

private fun baselineReason(
    window: HomeTrackWindow,
    profile: SollProfile,
): String {
    return when (window) {
        HomeTrackWindow.VORMITTAG -> when {
            profile.axes.focusPressure > 0.68f -> "Der Vormittag will eher geschuetzt bleiben."
            profile.axes.disruptionSensitivity > 0.64f -> "Der Vormittag braucht hier eher Luft fuer Eingänge."
            else -> "Der Grundrhythmus gibt diesem Block Richtung."
        }
        HomeTrackWindow.NACHMITTAG -> when {
            profile.axes.disruptionSensitivity > 0.62f -> "Hier darf mehr Abstimmung und Bewegung landen."
            profile.axes.focusPressure > 0.62f -> "Hier soll aus Fokus sichtbare Bewegung werden."
            else -> "Der Tag zieht in diesem Block nach vorne."
        }
        HomeTrackWindow.ABEND -> when {
            profile.axes.energyNeed > 0.62f -> "Der Abend sollte bewusst leichter bleiben."
            profile.name == "Weekend Soft" -> "Der Rhythmus haelt den Abend weich."
            else -> "Der Abend ist eher zum Schliessen als zum Starten da."
        }
    }
}

private fun completionRatio(history: List<PlanItem>): Float {
    if (history.isEmpty()) return 0.52f
    return history.count { it.isDone }.toFloat() / history.size.toFloat()
}

internal fun summarizeDrift(drift: Float): String {
    return when {
        drift > 0.16f -> "zu voll"
        drift < -0.16f -> "unter Soll"
        else -> "nah dran"
    }
}

internal fun preferredTimeBlockForWindow(
    window: HomeTrackWindow,
    now: LocalTime,
): TimeBlock {
    return when (window) {
        HomeTrackWindow.VORMITTAG -> if (now.hour < 11) TimeBlock.MORGEN else TimeBlock.MITTAG
        HomeTrackWindow.NACHMITTAG -> TimeBlock.NACHMITTAG
        HomeTrackWindow.ABEND -> TimeBlock.ABEND
    }
}

internal fun formatFocusHint(raw: String): String {
    return raw.trim()
        .replace(Regex("\\s+"), " ")
        .takeIf { it.isNotBlank() }
        ?.take(28)
        ?: "Fokus"
}

internal fun driftAccent(drift: Float): Float {
    return min(1f, drift.absoluteValue)
}

private fun overlapsWindow(
    signal: CalendarSignal,
    window: HomeTrackWindow,
    today: LocalDate,
    zoneId: ZoneId,
): Boolean {
    if (signal.isAllDay) return true
    val startDateTime = Instant.ofEpochMilli(signal.startMillis).atZone(zoneId)
    val endDateTime = Instant.ofEpochMilli(signal.endMillis).atZone(zoneId)
    val signalStartMinutes = when {
        startDateTime.toLocalDate().isBefore(today) -> 0
        else -> startDateTime.toLocalTime().toSecondOfDay() / 60
    }
    val signalEndMinutes = when {
        endDateTime.toLocalDate().isAfter(today) -> 24 * 60
        else -> max(signalStartMinutes + 1, endDateTime.toLocalTime().toSecondOfDay() / 60)
    }
    val (windowStart, windowEnd) = when (window) {
        HomeTrackWindow.VORMITTAG -> 0 to 13 * 60
        HomeTrackWindow.NACHMITTAG -> 13 * 60 to 18 * 60
        HomeTrackWindow.ABEND -> 18 * 60 to 24 * 60
    }
    return signalStartMinutes < windowEnd && signalEndMinutes > windowStart
}

private fun calendarWeight(
    signal: CalendarSignal,
    today: LocalDate,
    zoneId: ZoneId,
): Float {
    if (signal.isAllDay) return 0.68f
    val startDateTime = Instant.ofEpochMilli(signal.startMillis).atZone(zoneId)
    val endDateTime = Instant.ofEpochMilli(signal.endMillis).atZone(zoneId)
    val startMinutes = when {
        startDateTime.toLocalDate().isBefore(today) -> 0
        else -> startDateTime.toLocalTime().toSecondOfDay() / 60
    }
    val endMinutes = when {
        endDateTime.toLocalDate().isAfter(today) -> 24 * 60
        else -> max(startMinutes + 1, endDateTime.toLocalTime().toSecondOfDay() / 60)
    }
    val durationMinutes = (endMinutes - startMinutes).coerceAtLeast(15)
    return min(1f, durationMinutes / 120f)
}
