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
    data object Settings : AppDestination("settings", "Einstellungen")
    data object SettingsDomains : AppDestination("settings/domains", "Domaenen")
    data object SettingsDomainDetail : AppDestination("settings/domain", "Domaene")
    data object SettingsSources : AppDestination("settings/sources", "Quellen")
    data object StartArea : AppDestination("start/area", "Bereich")
}

const val SettingsDomainArg = "domain"
const val StartAreaArg = "areaId"
val SettingsDomainPattern = "${AppDestination.SettingsDomainDetail.route}/{$SettingsDomainArg}"
val StartAreaPattern = "${AppDestination.StartArea.route}/{$StartAreaArg}"

fun settingsDomainRoute(domain: String): String {
    return "${AppDestination.SettingsDomainDetail.route}/${Uri.encode(domain)}"
}

fun startAreaRoute(areaId: String): String {
    return "${AppDestination.StartArea.route}/${Uri.encode(areaId)}"
}
