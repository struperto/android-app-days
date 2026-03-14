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
    data object SettingsAnalysis : AppDestination("settings/analysis", "Analyse")
    data object SettingsDomains : AppDestination("settings/domains", "Bereiche")
    data object SettingsFeeds : AppDestination("settings/feeds", "Verbindungen")
    data object SettingsInbox : AppDestination("settings/inbox", "Eingang")
    data object SettingsDomainDetail : AppDestination("settings/domain", "Domaene")
    data object SettingsSources : AppDestination("settings/sources", "Android-Zugriffe")
    data object SettingsSourceDetail : AppDestination("settings/source", "Quelle")
    data object StartArea : AppDestination("start/area", "Bereich")
    data object SingleContent : AppDestination("single/content", "Inhalt")
}

const val SettingsDomainArg = "domain"
const val SettingsSourceArg = "source"
const val StartAreaArg = "areaId"
const val SingleContentArg = "contentId"
val SettingsDomainPattern = "${AppDestination.SettingsDomainDetail.route}/{$SettingsDomainArg}"
val SettingsSourcePattern = "${AppDestination.SettingsSourceDetail.route}/{$SettingsSourceArg}"
val StartAreaPattern = "${AppDestination.StartArea.route}/{$StartAreaArg}"
val SingleContentPattern = "${AppDestination.SingleContent.route}/{$SingleContentArg}"

fun settingsDomainRoute(domain: String): String {
    return "${AppDestination.SettingsDomainDetail.route}/${Uri.encode(domain)}"
}

fun startAreaRoute(areaId: String): String {
    return "${AppDestination.StartArea.route}/${Uri.encode(areaId)}"
}

fun settingsSourceRoute(source: String): String {
    return "${AppDestination.SettingsSourceDetail.route}/${Uri.encode(source)}"
}

fun singleContentRoute(contentId: String): String {
    return "${AppDestination.SingleContent.route}/${Uri.encode(contentId)}"
}
