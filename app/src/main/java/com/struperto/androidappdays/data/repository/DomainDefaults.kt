package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.domain.AdaptationMode
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainCatalogEntry
import com.struperto.androidappdays.domain.DomainGoal
import com.struperto.androidappdays.domain.GoalCadence
import com.struperto.androidappdays.domain.GoalPriority
import com.struperto.androidappdays.domain.GoalTarget
import com.struperto.androidappdays.domain.GoalWindow
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.TargetKind

fun defaultDomainGoals(): List<DomainGoal> {
    return listOf(
        DomainGoal(
            id = "goal_sleep",
            domain = LifeDomain.SLEEP,
            title = "Schlaf",
            cadence = GoalCadence.DAILY,
            target = GoalTarget(
                kind = TargetKind.MINIMUM,
                unit = "h",
                minimum = 8f,
            ),
            adaptationMode = AdaptationMode.CONTEXTUAL,
            preferredWindow = GoalWindow(
                startLogicalHour = 22,
                endLogicalHourExclusive = 31,
            ),
            priority = GoalPriority.CORE,
            rationale = "Erholung bleibt Grundlast.",
        ),
        DomainGoal(
            id = "goal_movement",
            domain = LifeDomain.MOVEMENT,
            title = "Bewegung",
            cadence = GoalCadence.DAILY,
            target = GoalTarget(
                kind = TargetKind.MINIMUM,
                unit = "steps",
                minimum = 10_000f,
            ),
            adaptationMode = AdaptationMode.CONTEXTUAL,
            priority = GoalPriority.CORE,
            rationale = "Basisbewegung schuetzt den Tag.",
        ),
        DomainGoal(
            id = "goal_hydration",
            domain = LifeDomain.HYDRATION,
            title = "Hydration",
            cadence = GoalCadence.DAILY,
            target = GoalTarget(
                kind = TargetKind.MINIMUM,
                unit = "L",
                minimum = 2.0f,
            ),
            adaptationMode = AdaptationMode.FIXED,
            priority = GoalPriority.CORE,
            rationale = "Wasser ist ein Kernziel.",
        ),
        DomainGoal(
            id = "goal_nutrition",
            domain = LifeDomain.NUTRITION,
            title = "Ernaehrung",
            cadence = GoalCadence.DAILY,
            target = GoalTarget(
                kind = TargetKind.RANGE,
                unit = "g",
                minimum = 120f,
                maximum = 160f,
                note = "Protein",
            ),
            adaptationMode = AdaptationMode.FIXED,
            priority = GoalPriority.SUPPORT,
            rationale = "Protein als klare MVP-Metrik.",
        ),
        DomainGoal(
            id = "goal_focus",
            domain = LifeDomain.FOCUS,
            title = "Fokus",
            cadence = GoalCadence.DAILY,
            target = GoalTarget(
                kind = TargetKind.MINIMUM,
                unit = "min",
                minimum = 120f,
            ),
            adaptationMode = AdaptationMode.CONTEXTUAL,
            preferredWindow = GoalWindow(
                startLogicalHour = 8,
                endLogicalHourExclusive = 11,
            ),
            priority = GoalPriority.SUPPORT,
            rationale = "Zwei ruhige Fokusstunden als Default.",
        ),
        DomainGoal(
            id = "goal_stress",
            domain = LifeDomain.STRESS,
            title = "Stress",
            cadence = GoalCadence.DAILY,
            target = GoalTarget(
                kind = TargetKind.MAXIMUM,
                unit = "count",
                maximum = 8f,
                note = "aktive Notifications",
            ),
            adaptationMode = AdaptationMode.CONTEXTUAL,
            priority = GoalPriority.SUPPORT,
            rationale = "Unterbrechungsdruck soll sichtbar und begrenzbar bleiben.",
        ),
    )
}

fun defaultDomainCatalog(): List<DomainCatalogEntry> {
    return listOf(
        DomainCatalogEntry(LifeDomain.SLEEP, "Schlaf", "Dauer und Rhythmus", GoalPriority.CORE, true, true),
        DomainCatalogEntry(LifeDomain.MOVEMENT, "Bewegung", "Schritte und Aktivitaet", GoalPriority.CORE, true, true),
        DomainCatalogEntry(LifeDomain.HYDRATION, "Hydration", "Wasser ueber den Tag", GoalPriority.CORE, true, true),
        DomainCatalogEntry(LifeDomain.NUTRITION, "Ernaehrung", "Protein und Timing", GoalPriority.SUPPORT, true, true),
        DomainCatalogEntry(LifeDomain.FOCUS, "Fokus", "Geschuetzte Konzentration", GoalPriority.SUPPORT, true, true),
        DomainCatalogEntry(LifeDomain.STRESS, "Stress", "Druck und Unterbrechung", GoalPriority.SUPPORT, true, true),
        DomainCatalogEntry(LifeDomain.RECOVERY, "Recovery", "Erholung und Rueckkehr", GoalPriority.PLACEHOLDER, false, false),
        DomainCatalogEntry(LifeDomain.CAFFEINE, "Koffein", "Stimulanz und Timing", GoalPriority.PLACEHOLDER, false, false),
        DomainCatalogEntry(LifeDomain.MEDICATION, "Medikation", "Pflicht und Einnahme", GoalPriority.PLACEHOLDER, false, false),
        DomainCatalogEntry(LifeDomain.SOCIAL, "Social", "Beziehungen und Kontakt", GoalPriority.PLACEHOLDER, false, false),
        DomainCatalogEntry(LifeDomain.HOUSEHOLD, "Household", "Alltagslast und Pflege", GoalPriority.PLACEHOLDER, false, false),
        DomainCatalogEntry(LifeDomain.SCREEN_TIME, "Screen Time", "Bildschirm und Drift", GoalPriority.PLACEHOLDER, false, false),
    )
}

fun defaultSourcePreferences(): List<DataSourceKind> {
    return listOf(
        DataSourceKind.HEALTH_CONNECT,
        DataSourceKind.CALENDAR,
        DataSourceKind.NOTIFICATIONS,
        DataSourceKind.MANUAL,
    )
}

fun lifeDomainLabel(domain: LifeDomain): String {
    return when (domain) {
        LifeDomain.SLEEP -> "Schlaf"
        LifeDomain.MOVEMENT -> "Bewegung"
        LifeDomain.HYDRATION -> "Hydration"
        LifeDomain.NUTRITION -> "Ernaehrung"
        LifeDomain.FOCUS -> "Fokus"
        LifeDomain.RECOVERY -> "Recovery"
        LifeDomain.STRESS -> "Stress"
        LifeDomain.CAFFEINE -> "Koffein"
        LifeDomain.MEDICATION -> "Medikation"
        LifeDomain.SOCIAL -> "Social"
        LifeDomain.HOUSEHOLD -> "Household"
        LifeDomain.SCREEN_TIME -> "Screen Time"
        LifeDomain.ADMIN -> "Admin"
        LifeDomain.HEALTH -> "Gesundheit"
        LifeDomain.EMOTIONAL_STATE -> "Zustand"
    }
}
