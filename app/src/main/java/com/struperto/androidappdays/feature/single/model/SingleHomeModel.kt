package com.struperto.androidappdays.feature.single.model

data class SingleMetric(
    val label: String,
    val value: String,
)

data class SingleMirrorLane(
    val label: String,
    val target: Float,
    val actual: Float,
)

data class SingleQuickAction(
    val route: String,
    val title: String,
)

data class SingleHomeState(
    val modeLabel: String,
    val stageLabel: String,
    val cadenceLabel: String,
    val mirrorTitle: String,
    val metrics: List<SingleMetric>,
    val mirrorLanes: List<SingleMirrorLane>,
    val actions: List<SingleQuickAction>,
)

fun previewSingleHomeState(): SingleHomeState {
    return SingleHomeState(
        modeLabel = "Single",
        stageLabel = "Heute",
        cadenceLabel = "08 / 14 / 21",
        mirrorTitle = "Tagesabgleich",
        metrics = listOf(
            SingleMetric(label = "Fokus", value = "3 Vorhaben"),
            SingleMetric(label = "Capture", value = "1 offen"),
            SingleMetric(label = "Plan", value = "2 Slots"),
            SingleMetric(label = "Flow", value = "68%"),
        ),
        mirrorLanes = listOf(
            SingleMirrorLane(label = "Körper", target = 0.62f, actual = 0.46f),
            SingleMirrorLane(label = "Fokus", target = 0.86f, actual = 0.74f),
            SingleMirrorLane(label = "Arbeit", target = 0.72f, actual = 0.58f),
            SingleMirrorLane(label = "Ordnung", target = 0.54f, actual = 0.69f),
            SingleMirrorLane(label = "Bezug", target = 0.48f, actual = 0.42f),
            SingleMirrorLane(label = "Lernen", target = 0.66f, actual = 0.78f),
        ),
        actions = listOf(
            SingleQuickAction(
                route = "single_life_wheel",
                title = "Lebensrad",
            ),
            SingleQuickAction(
                route = "single_working_set",
                title = "Vorhaben",
            ),
            SingleQuickAction(
                route = "single_day_schedule",
                title = "Zeitplan",
            ),
            SingleQuickAction(
                route = "single_plan",
                title = "Plan",
            ),
            SingleQuickAction(
                route = "single_capture",
                title = "Capture",
            ),
            SingleQuickAction(
                route = "single_create",
                title = "Create",
            ),
        ),
    )
}
