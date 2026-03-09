package com.struperto.androidappdays.navigation

import android.net.Uri

sealed class AppDestination(
    val route: String,
    val title: String,
) {
    data object Boot : AppDestination("boot", "Boot")
    data object Start : AppDestination("start", "Start")
    data object Home : AppDestination("single_home", "Single")
    data object Multi : AppDestination("multi_home", "Multi")
    data object Workbench : AppDestination("single_workbench", "Werkbank")
    data object LifeWheel : AppDestination("single_life_wheel", "Fingerprint")
    data object WorkingSet : AppDestination("single_working_set", "Vorhaben")
    data object DaySchedule : AppDestination("single_day_schedule", "Zeitplan")
    data object Plan : AppDestination("single_plan", "Plan")
    data object Capture : AppDestination("single_capture", "Erfassen")
    data object Create : AppDestination("single_create", "Erstellen")
    data object Settings : AppDestination("settings", "Einstellungen")
    data object SettingsDomains : AppDestination("settings/domains", "Domaenen")
    data object SettingsDomainDetail : AppDestination("settings/domain", "Domaene")
    data object SettingsSources : AppDestination("settings/sources", "Quellen")
    data object SettingsResearch : AppDestination("settings/research", "Research")
    data object StartArea : AppDestination("start/area", "Bereich")
}

const val WorkingSetCaptureIdArg = "captureId"
const val PlanCaptureIdArg = "captureId"
const val PlanVorhabenIdArg = "vorhabenId"
const val WorkbenchPaneArg = "pane"
const val SettingsDomainArg = "domain"
const val StartAreaArg = "areaId"
val WorkbenchPattern = "${AppDestination.Workbench.route}?$WorkbenchPaneArg={$WorkbenchPaneArg}"
val WorkingSetPattern = "${AppDestination.WorkingSet.route}?$WorkingSetCaptureIdArg={$WorkingSetCaptureIdArg}"
val PlanPattern = "${AppDestination.Plan.route}?$PlanCaptureIdArg={$PlanCaptureIdArg}&$PlanVorhabenIdArg={$PlanVorhabenIdArg}"
val SettingsDomainPattern = "${AppDestination.SettingsDomainDetail.route}/{$SettingsDomainArg}"
val StartAreaPattern = "${AppDestination.StartArea.route}/{$StartAreaArg}"

fun workbenchRoute(pane: String? = null): String {
    if (pane.isNullOrBlank()) {
        return AppDestination.Workbench.route
    }
    return "${AppDestination.Workbench.route}?$WorkbenchPaneArg=${Uri.encode(pane)}"
}

fun workingSetRoute(captureId: String? = null): String {
    if (captureId.isNullOrBlank()) {
        return AppDestination.WorkingSet.route
    }
    return "${AppDestination.WorkingSet.route}?$WorkingSetCaptureIdArg=${Uri.encode(captureId)}"
}

fun planRoute(
    captureId: String? = null,
    vorhabenId: String? = null,
): String {
    val args = buildList {
        if (!captureId.isNullOrBlank()) {
            add("$PlanCaptureIdArg=${Uri.encode(captureId)}")
        }
        if (!vorhabenId.isNullOrBlank()) {
            add("$PlanVorhabenIdArg=${Uri.encode(vorhabenId)}")
        }
    }
    if (args.isEmpty()) {
        return AppDestination.Plan.route
    }
    return "${AppDestination.Plan.route}?${args.joinToString("&")}"
}

fun settingsDomainRoute(domain: String): String {
    return "${AppDestination.SettingsDomainDetail.route}/${Uri.encode(domain)}"
}

fun startAreaRoute(areaId: String): String {
    return "${AppDestination.StartArea.route}/${Uri.encode(areaId)}"
}
