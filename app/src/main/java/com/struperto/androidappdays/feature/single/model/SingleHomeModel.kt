package com.struperto.androidappdays.feature.single.model

data class SingleMetric(
    val label: String,
    val value: String,
)

data class SingleQuickAction(
    val route: String,
    val title: String,
    val detail: String,
)

data class SingleHomeState(
    val stageLabel: String,
    val headline: String,
    val summary: String,
    val mirrorBars: List<Float>,
    val metrics: List<SingleMetric>,
    val nextStepTitle: String,
    val nextStepDetail: String,
    val focusTracks: List<String>,
    val actions: List<SingleQuickAction>,
)

fun previewSingleHomeState(): SingleHomeState {
    return SingleHomeState(
        stageLabel = "Heute",
        headline = "Single als ruhiger Tageskern",
        summary = "Die neue App startet bewusst klein: ein persoenlicher Tagesfluss mit Lebensrad, Working Set, SOLL/IST und klaren Einzelschritten.",
        mirrorBars = listOf(0.18f, 0.24f, 0.31f, 0.28f, 0.42f, 0.57f, 0.63f, 0.49f, 0.54f, 0.68f, 0.51f, 0.36f),
        metrics = listOf(
            SingleMetric(label = "Fokus", value = "3 Vorhaben"),
            SingleMetric(label = "Capture", value = "1 offen"),
            SingleMetric(label = "Rhythmus", value = "08 / 14 / 21"),
        ),
        nextStepTitle = "Naechster produktiver Schritt",
        nextStepDetail = "Vor dem Bauen des echten Datenkerns fixieren wir zuerst die Single-Screens und ihren Flow.",
        focusTracks = listOf(
            "Lebensrad als Balance-Ebene",
            "Working Set als taeglicher Fokus",
            "Zeitplan, Plan, Capture und Create als getrennte Routen",
        ),
        actions = listOf(
            SingleQuickAction(
                route = "single_life_wheel",
                title = "Lebensrad",
                detail = "Bereiche und Balance definieren",
            ),
            SingleQuickAction(
                route = "single_working_set",
                title = "Vorhaben",
                detail = "Aktive Intentionen fuer heute",
            ),
            SingleQuickAction(
                route = "single_day_schedule",
                title = "Zeitplan",
                detail = "SOLL ueber den Tag legen",
            ),
            SingleQuickAction(
                route = "single_plan",
                title = "Plan",
                detail = "1 bis 3 Kernaufgaben",
            ),
            SingleQuickAction(
                route = "single_capture",
                title = "Capture",
                detail = "Gedanken und Signale erfassen",
            ),
            SingleQuickAction(
                route = "single_create",
                title = "Create",
                detail = "Naechste Aufgabe formen",
            ),
        ),
    )
}
