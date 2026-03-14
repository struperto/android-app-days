package com.struperto.androidappdays.testing

import com.struperto.androidappdays.data.repository.UserFingerprintDraft
import com.struperto.androidappdays.data.repository.defaultDomainGoals
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.GoalWindow
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.LocalDate
import java.time.ZoneId

data class PersonaGoalOverride(
    val goalId: String,
    val minimum: Float? = null,
    val maximum: Float? = null,
    val preferredWindow: GoalWindow? = null,
)

data class PersonaObservationSample(
    val dayOffset: Long,
    val domain: LifeDomain,
    val metric: ObservationMetric,
    val value: Float,
    val unit: String,
    val source: ObservationSource = ObservationSource.USER_INPUT,
    val confidence: Float = 0.82f,
    val contextTags: Set<String> = emptySet(),
)

data class MvpTestPersona(
    val id: String,
    val name: String,
    val archetype: String,
    val summary: String,
    val testFocus: List<String>,
    val sourcePreset: Set<DataSourceKind>,
    val fingerprintDraft: UserFingerprintDraft,
    val goalOverrides: List<PersonaGoalOverride> = emptyList(),
    val observationSamples: List<PersonaObservationSample> = emptyList(),
) {
    fun buildGoals(): List<DomainGoal> {
        return defaultDomainGoals().map { goal ->
            val override = goalOverrides.firstOrNull { it.goalId == goal.id } ?: return@map goal
            goal.copy(
                target = goal.target.copy(
                    minimum = override.minimum ?: goal.target.minimum,
                    maximum = override.maximum ?: goal.target.maximum,
                ),
                preferredWindow = override.preferredWindow ?: goal.preferredWindow,
            )
        }
    }

    fun buildObservations(baseDate: LocalDate): List<DomainObservation> {
        return observationSamples.mapIndexed { index, sample ->
            val logicalDate = baseDate.plusDays(sample.dayOffset)
            DomainObservation(
                id = "${id}_${sample.metric.name.lowercase()}_${logicalDate}_$index",
                goalId = null,
                domain = sample.domain,
                metric = sample.metric,
                source = sample.source,
                startedAt = logicalDate.atTime(8, 0).atZone(ZoneId.systemDefault()).toInstant(),
                value = DomainObservationValue(
                    numeric = sample.value,
                    unit = sample.unit,
                ),
                logicalDate = logicalDate,
                sourceRecordId = "${id}_${sample.metric.name.lowercase()}_${logicalDate}",
                confidence = sample.confidence,
                contextTags = sample.contextTags,
            )
        }
    }
}

object MvpTestPersonas {
    val all: List<MvpTestPersona> = listOf(
        earlyAthlete(),
        busyParent(),
        nightOwlCreative(),
        deskPm(),
        recoveryWeek(),
        nutritionOptimizer(),
        freelanceSwitcher(),
        shiftWorker(),
        minimalManualTracker(),
        overloadedLead(),
    )
}

private fun earlyAthlete(): MvpTestPersona {
    return MvpTestPersona(
        id = "early-athlete",
        name = "Lena",
        archetype = "Early Athlete",
        summary = "Stabiler Morgenrhythmus, gute Watch-Daten, Bewegung als starke Kernroutine.",
        testFocus = listOf(
            "Prueft, ob gute Tage ruhig und nicht ueberdramatisch dargestellt werden.",
            "Prueft, ob Schlaf und Bewegung aus Health Connect sauber als on track erscheinen.",
        ),
        sourcePreset = setOf(
            DataSourceKind.HEALTH_CONNECT,
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Frueh starten, frueh abschliessen",
            goodDayPattern = "Training, Fokusblock, ruhiger Abend",
            badDayPattern = "Zu spaete Termine",
            dayStartHour = 6,
            dayEndHour = 22,
            morningEnergy = 5,
            afternoonEnergy = 4,
            eveningEnergy = 2,
            focusStrength = 4,
            disruptionSensitivity = 2,
            recoveryNeed = 3,
        ),
        goalOverrides = listOf(
            PersonaGoalOverride(goalId = "goal_movement", minimum = 12_000f),
            PersonaGoalOverride(goalId = "goal_focus", minimum = 90f),
        ),
        observationSamples = listOf(
            sample(-2, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 8.2f, "h", ObservationSource.WEARABLE),
            sample(-2, LifeDomain.MOVEMENT, ObservationMetric.STEPS, 13_400f, "steps", ObservationSource.WEARABLE),
            sample(-1, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 7.9f, "h", ObservationSource.WEARABLE),
            sample(-1, LifeDomain.MOVEMENT, ObservationMetric.STEPS, 12_180f, "steps", ObservationSource.WEARABLE),
            sample(0, LifeDomain.HYDRATION, ObservationMetric.HYDRATION_LITERS, 1.8f, "L"),
            sample(0, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 95f, "min"),
        ),
    )
}

private fun busyParent(): MvpTestPersona {
    return MvpTestPersona(
        id = "busy-parent",
        name = "Mara",
        archetype = "Busy Parent",
        summary = "Starke Unterbrechungen, wenig Schlaf, lueckenhafte Daten, hohe Notification-Last.",
        testFocus = listOf(
            "Prueft sparse data und viele unknowns ohne Bestrafung.",
            "Prueft, ob Sleep-vs-Notification-Hypothesen weich formuliert bleiben.",
        ),
        sourcePreset = setOf(
            DataSourceKind.CALENDAR,
            DataSourceKind.NOTIFICATIONS,
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Familienrhythmus und fixe Slots",
            goodDayPattern = "Kinder, Arbeit, ruhiger Abend",
            badDayPattern = "Unruhige Nacht, viele Wechsel",
            dayStartHour = 6,
            dayEndHour = 23,
            morningEnergy = 2,
            afternoonEnergy = 3,
            eveningEnergy = 2,
            focusStrength = 2,
            disruptionSensitivity = 5,
            recoveryNeed = 5,
        ),
        observationSamples = listOf(
            sample(-2, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 5.8f, "h"),
            sample(-2, LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 12f, "count", contextTags = setOf("notifications")),
            sample(-1, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 6.1f, "h"),
            sample(-1, LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 11f, "count", contextTags = setOf("notifications")),
            sample(0, LifeDomain.FOCUS, ObservationMetric.CALENDAR_LOAD, 5f, "count", ObservationSource.CALENDAR),
            sample(0, LifeDomain.HYDRATION, ObservationMetric.HYDRATION_LITERS, 1.2f, "L"),
        ),
    )
}

private fun nightOwlCreative(): MvpTestPersona {
    return MvpTestPersona(
        id = "night-owl-creative",
        name = "Jonas",
        archetype = "Night Owl Creative",
        summary = "Spaete kreative Phasen, Schlafziel wird formal gerissen, Fokus verschiebt sich in den Abend.",
        testFocus = listOf(
            "Prueft, wie streng das System mit untypischen Zeitfenstern umgeht.",
            "Prueft Zielanpassung ueber Fokusfenster statt pauschaler Bewertung.",
        ),
        sourcePreset = setOf(
            DataSourceKind.HEALTH_CONNECT,
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Spaeter Start, kreativer Abend",
            goodDayPattern = "Ruhiger Vormittag, langer Kreativabend",
            badDayPattern = "Fruehe Calls zerstoeren den Tag",
            dayStartHour = 8,
            dayEndHour = 2,
            morningEnergy = 1,
            afternoonEnergy = 3,
            eveningEnergy = 5,
            focusStrength = 5,
            disruptionSensitivity = 3,
            recoveryNeed = 4,
        ),
        goalOverrides = listOf(
            PersonaGoalOverride(
                goalId = "goal_focus",
                minimum = 150f,
                preferredWindow = GoalWindow(19, 24),
            ),
        ),
        observationSamples = listOf(
            sample(-2, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 7.1f, "h", ObservationSource.WEARABLE),
            sample(-1, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 6.9f, "h", ObservationSource.WEARABLE),
            sample(0, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 170f, "min"),
            sample(0, LifeDomain.NUTRITION, ObservationMetric.PROTEIN_GRAMS, 112f, "g"),
        ),
    )
}

private fun deskPm(): MvpTestPersona {
    return MvpTestPersona(
        id = "desk-pm",
        name = "Alex",
        archetype = "Desk PM",
        summary = "Meeting-lastiger Produktalltag, Bewegung sinkt, Fokus kippt bei dichtem Kalender.",
        testFocus = listOf(
            "Prueft Fokus-vs-Kalender-Muster.",
            "Prueft neutralen Umgang mit wenig Bewegung an dichten Tagen.",
        ),
        sourcePreset = setOf(
            DataSourceKind.CALENDAR,
            DataSourceKind.MANUAL,
            DataSourceKind.HEALTH_CONNECT,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Werktags meetinglastig",
            goodDayPattern = "Wenige Calls, ein klarer Fokusblock",
            badDayPattern = "Back-to-back Meetings",
            morningEnergy = 3,
            afternoonEnergy = 3,
            eveningEnergy = 2,
            focusStrength = 3,
            disruptionSensitivity = 4,
            recoveryNeed = 3,
        ),
        observationSamples = listOf(
            sample(-2, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 70f, "min"),
            sample(-2, LifeDomain.FOCUS, ObservationMetric.CALENDAR_LOAD, 6f, "count", ObservationSource.CALENDAR),
            sample(-1, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 65f, "min"),
            sample(-1, LifeDomain.FOCUS, ObservationMetric.CALENDAR_LOAD, 5f, "count", ObservationSource.CALENDAR),
            sample(0, LifeDomain.MOVEMENT, ObservationMetric.STEPS, 4_800f, "steps", ObservationSource.WEARABLE),
        ),
    )
}

private fun recoveryWeek(): MvpTestPersona {
    return MvpTestPersona(
        id = "recovery-week",
        name = "Sara",
        archetype = "Recovery Week",
        summary = "Nach Krankheit oder Ueberlastung. Kernziele bleiben, Stretch-Ziele sollen zuruecktreten.",
        testFocus = listOf(
            "Prueft, ob Core-Ziele visuell staerker wirken als Stretch-Verhalten.",
            "Prueft, ob der Tag trotz wenig Leistung nicht als Scheitern erscheint.",
        ),
        sourcePreset = setOf(
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Erholung vor Leistung",
            goodDayPattern = "Schlaf, Wasser, kurze Wege",
            badDayPattern = "Zu frueher Leistungsdruck",
            morningEnergy = 2,
            afternoonEnergy = 2,
            eveningEnergy = 2,
            focusStrength = 2,
            disruptionSensitivity = 4,
            recoveryNeed = 5,
        ),
        goalOverrides = listOf(
            PersonaGoalOverride(goalId = "goal_movement", minimum = 6_500f),
            PersonaGoalOverride(goalId = "goal_focus", minimum = 45f),
        ),
        observationSamples = listOf(
            sample(0, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 8.7f, "h"),
            sample(0, LifeDomain.HYDRATION, ObservationMetric.HYDRATION_LITERS, 2.3f, "L"),
            sample(0, LifeDomain.MOVEMENT, ObservationMetric.STEPS, 5_900f, "steps"),
        ),
    )
}

private fun nutritionOptimizer(): MvpTestPersona {
    return MvpTestPersona(
        id = "nutrition-optimizer",
        name = "Tim",
        archetype = "Nutrition Optimizer",
        summary = "Ernaehrung und Hydration werden sehr bewusst erfasst, andere Bereiche nur leicht.",
        testFocus = listOf(
            "Prueft, ob manuelle Werte im MVP wirklich tragend sein koennen.",
            "Prueft Protein-Range gegen echte Eintraege.",
        ),
        sourcePreset = setOf(
            DataSourceKind.MANUAL,
            DataSourceKind.HEALTH_CONNECT,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Meal prep und Training",
            goodDayPattern = "Protein sauber ueber den Tag verteilt",
            badDayPattern = "Abendessen kippt in Snacks",
            morningEnergy = 4,
            afternoonEnergy = 4,
            eveningEnergy = 3,
            focusStrength = 3,
            disruptionSensitivity = 2,
            recoveryNeed = 3,
        ),
        observationSamples = listOf(
            sample(-1, LifeDomain.NUTRITION, ObservationMetric.PROTEIN_GRAMS, 132f, "g"),
            sample(-1, LifeDomain.HYDRATION, ObservationMetric.HYDRATION_LITERS, 2.2f, "L"),
            sample(0, LifeDomain.NUTRITION, ObservationMetric.PROTEIN_GRAMS, 98f, "g"),
            sample(0, LifeDomain.HYDRATION, ObservationMetric.HYDRATION_LITERS, 1.5f, "L"),
        ),
    )
}

private fun freelanceSwitcher(): MvpTestPersona {
    return MvpTestPersona(
        id = "freelance-switcher",
        name = "Nico",
        archetype = "Freelance Switcher",
        summary = "Projektwechsel, offene Zeitslots, viele Kontextspruenge, schwankender Fokus.",
        testFocus = listOf(
            "Prueft, wie das Dashboard mit fluider Tagesstruktur umgeht.",
            "Prueft, ob Hypothesen noch vorsichtig genug bleiben.",
        ),
        sourcePreset = setOf(
            DataSourceKind.CALENDAR,
            DataSourceKind.NOTIFICATIONS,
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Unregelmaessig, projektgetrieben",
            goodDayPattern = "Ein Kunde pro Halbtag",
            badDayPattern = "Viele kleine Reaktionen",
            morningEnergy = 3,
            afternoonEnergy = 4,
            eveningEnergy = 3,
            focusStrength = 3,
            disruptionSensitivity = 4,
            recoveryNeed = 3,
        ),
        observationSamples = listOf(
            sample(-2, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 55f, "min"),
            sample(-2, LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 14f, "count"),
            sample(-1, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 80f, "min"),
            sample(-1, LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 13f, "count"),
            sample(0, LifeDomain.FOCUS, ObservationMetric.CALENDAR_LOAD, 2f, "count", ObservationSource.CALENDAR),
            sample(0, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 72f, "min"),
        ),
    )
}

private fun shiftWorker(): MvpTestPersona {
    return MvpTestPersona(
        id = "shift-worker",
        name = "Lea",
        archetype = "Shift Worker",
        summary = "Atypische Arbeitszeiten, Schlaf und Essen wandern. Wichtig fuer die Grenzen des jetzigen Modells.",
        testFocus = listOf(
            "Prueft, wo das aktuelle 24h-Modell fuer Sonderrhythmen noch zu starr ist.",
            "Prueft, ob der Test klar zeigt, welche Annahmen spaeter geoeffnet werden muessen.",
        ),
        sourcePreset = setOf(
            DataSourceKind.HEALTH_CONNECT,
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Schichtplan wechselt",
            goodDayPattern = "Schlaf vor dem Dienst schuetzen",
            badDayPattern = "Frueher Wechsel nach Spaetschicht",
            dayStartHour = 10,
            dayEndHour = 4,
            morningEnergy = 2,
            afternoonEnergy = 3,
            eveningEnergy = 4,
            focusStrength = 2,
            disruptionSensitivity = 4,
            recoveryNeed = 5,
        ),
        goalOverrides = listOf(
            PersonaGoalOverride(
                goalId = "goal_sleep",
                preferredWindow = GoalWindow(1, 10),
            ),
        ),
        observationSamples = listOf(
            sample(-1, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 7.4f, "h", ObservationSource.WEARABLE),
            sample(0, LifeDomain.HYDRATION, ObservationMetric.HYDRATION_LITERS, 1.7f, "L"),
            sample(0, LifeDomain.NUTRITION, ObservationMetric.PROTEIN_GRAMS, 118f, "g"),
        ),
    )
}

private fun minimalManualTracker(): MvpTestPersona {
    return MvpTestPersona(
        id = "minimal-manual",
        name = "Ben",
        archetype = "Minimal Manual Tracker",
        summary = "Keine Integrationen, nur kurze manuelle Werte. Das ist der zentrale sparse-data-Test.",
        testFocus = listOf(
            "Prueft, ob die App auch ohne jede Systemquelle nutzbar bleibt.",
            "Prueft, ob unknown neutral bleibt und manuelle Eintraege genug Wert erzeugen.",
        ),
        sourcePreset = setOf(
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Einfach und leichtgewichtig",
            goodDayPattern = "Kurz reflektieren, nicht viel tippen",
            badDayPattern = "Zu viel Pflegeaufwand",
            morningEnergy = 3,
            afternoonEnergy = 3,
            eveningEnergy = 3,
            focusStrength = 3,
            disruptionSensitivity = 3,
            recoveryNeed = 3,
        ),
        observationSamples = listOf(
            sample(0, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 7.0f, "h"),
            sample(0, LifeDomain.HYDRATION, ObservationMetric.HYDRATION_LITERS, 1.9f, "L"),
        ),
    )
}

private fun overloadedLead(): MvpTestPersona {
    return MvpTestPersona(
        id = "overloaded-lead",
        name = "Paul",
        archetype = "Overloaded Lead",
        summary = "Fuehrungsrolle, hoher Kalenderdruck, viele Benachrichtigungen, Schlaf und Bewegung fallen mit.",
        testFocus = listOf(
            "Prueft das Zusammenspiel aus Kernzielen und hoher Arbeitslast.",
            "Prueft, ob Sleep-, Focus- und Movement-Muster sichtbar werden, ohne zu moralisieren.",
        ),
        sourcePreset = setOf(
            DataSourceKind.HEALTH_CONNECT,
            DataSourceKind.CALENDAR,
            DataSourceKind.NOTIFICATIONS,
            DataSourceKind.MANUAL,
        ),
        fingerprintDraft = defaultFingerprintDraft(
            weeklyRhythm = "Viel Koordination, wenig Leere",
            goodDayPattern = "Wenige Meetings, klar delegiert",
            badDayPattern = "Nachrichten bis spaet abends",
            morningEnergy = 3,
            afternoonEnergy = 2,
            eveningEnergy = 2,
            focusStrength = 2,
            disruptionSensitivity = 5,
            recoveryNeed = 4,
        ),
        observationSamples = listOf(
            sample(-2, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 6.2f, "h", ObservationSource.WEARABLE),
            sample(-2, LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 16f, "count"),
            sample(-2, LifeDomain.FOCUS, ObservationMetric.CALENDAR_LOAD, 7f, "count", ObservationSource.CALENDAR),
            sample(-2, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 50f, "min"),
            sample(-1, LifeDomain.SLEEP, ObservationMetric.SLEEP_HOURS, 6.0f, "h", ObservationSource.WEARABLE),
            sample(-1, LifeDomain.STRESS, ObservationMetric.NOTIFICATION_LOAD, 15f, "count"),
            sample(-1, LifeDomain.FOCUS, ObservationMetric.CALENDAR_LOAD, 6f, "count", ObservationSource.CALENDAR),
            sample(-1, LifeDomain.FOCUS, ObservationMetric.FOCUS_MINUTES, 45f, "min"),
            sample(0, LifeDomain.MOVEMENT, ObservationMetric.STEPS, 3_900f, "steps", ObservationSource.WEARABLE),
        ),
    )
}

private fun defaultFingerprintDraft(
    weeklyRhythm: String,
    goodDayPattern: String,
    badDayPattern: String,
    dayStartHour: Int = 6,
    dayEndHour: Int = 22,
    morningEnergy: Int = 3,
    afternoonEnergy: Int = 3,
    eveningEnergy: Int = 3,
    focusStrength: Int = 3,
    disruptionSensitivity: Int = 3,
    recoveryNeed: Int = 3,
): UserFingerprintDraft {
    return UserFingerprintDraft(
        rolesText = "Selbststeuerung, Alltag",
        responsibilitiesText = "Gesund bleiben, Arbeit tragen",
        priorityRulesText = "Core vor Stretch",
        weeklyRhythm = weeklyRhythm,
        recurringCommitmentsText = "Morgenstart, Tagesabschluss",
        goodDayPattern = goodDayPattern,
        badDayPattern = badDayPattern,
        dayStartHour = dayStartHour,
        dayEndHour = dayEndHour,
        morningEnergy = morningEnergy,
        afternoonEnergy = afternoonEnergy,
        eveningEnergy = eveningEnergy,
        focusStrength = focusStrength,
        disruptionSensitivity = disruptionSensitivity,
        recoveryNeed = recoveryNeed,
    )
}

private fun sample(
    dayOffset: Long,
    domain: LifeDomain,
    metric: ObservationMetric,
    value: Float,
    unit: String,
    source: ObservationSource = ObservationSource.USER_INPUT,
    confidence: Float = 0.82f,
    contextTags: Set<String> = emptySet(),
): PersonaObservationSample {
    return PersonaObservationSample(
        dayOffset = dayOffset,
        domain = domain,
        metric = metric,
        value = value,
        unit = unit,
        source = source,
        confidence = confidence,
        contextTags = contextTags,
    )
}
