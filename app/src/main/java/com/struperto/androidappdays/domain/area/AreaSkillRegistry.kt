package com.struperto.androidappdays.domain.area

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.ui.graphics.vector.ImageVector

data class SkillDefinition(
    val kind: AreaSkillKind,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val permissionKey: String?,
    val outputType: String,
)

data class ConfigField(
    val key: String,
    val label: String,
    val placeholder: String,
    val type: ConfigFieldType,
)

enum class ConfigFieldType {
    TEXT,
    URL,
    COORDINATES,
}

fun skillDefinition(kind: AreaSkillKind): SkillDefinition {
    return when (kind) {
        AreaSkillKind.HEALTH_TRACKING -> SkillDefinition(
            kind = kind,
            label = "Gesundheit",
            description = "Schlaf, Schritte und Bewegung aus Health Connect lesen.",
            icon = Icons.Outlined.FitnessCenter,
            permissionKey = kind.permissionKey,
            outputType = "health_metrics",
        )
        AreaSkillKind.CALENDAR_WATCH -> SkillDefinition(
            kind = kind,
            label = "Kalender",
            description = "Lokale Termine und Besprechungen als kompakten Tagesstand zeigen.",
            icon = Icons.Outlined.CalendarMonth,
            permissionKey = kind.permissionKey,
            outputType = "calendar_events",
        )
        AreaSkillKind.NOTIFICATION_FILTER -> SkillDefinition(
            kind = kind,
            label = "Benachrichtigungen",
            description = "Wichtige Benachrichtigungen filtern und ruhig sichtbar machen.",
            icon = Icons.Outlined.Notifications,
            permissionKey = kind.permissionKey,
            outputType = "notification_signals",
        )
        AreaSkillKind.MANUAL_LOG -> SkillDefinition(
            kind = kind,
            label = "Manuell",
            description = "Eigene Notizen und Bewertungen erfassen.",
            icon = Icons.Outlined.Pending,
            permissionKey = null,
            outputType = "manual_entries",
        )
        AreaSkillKind.SCREENSHOT_READER -> SkillDefinition(
            kind = kind,
            label = "Screenshots",
            description = "Neue Screenshots lesen und nur auffaellige Inhalte zeigen.",
            icon = Icons.Outlined.PhotoLibrary,
            permissionKey = kind.permissionKey,
            outputType = "media_items",
        )
        AreaSkillKind.PHOTO_STREAM -> SkillDefinition(
            kind = kind,
            label = "Fotos",
            description = "Neue Kamera-Fotos als stilles Archiv oder Tagesstrom zeigen.",
            icon = Icons.Outlined.CameraAlt,
            permissionKey = kind.permissionKey,
            outputType = "media_items",
        )
        AreaSkillKind.CONTACT_WATCH -> SkillDefinition(
            kind = kind,
            label = "Kontakte",
            description = "Kontaktaktivitaet beobachten und Beziehungspflege unterstuetzen.",
            icon = Icons.Outlined.Contacts,
            permissionKey = kind.permissionKey,
            outputType = "contact_signals",
        )
        AreaSkillKind.APP_USAGE -> SkillDefinition(
            kind = kind,
            label = "App-Nutzung",
            description = "Bildschirmzeit und App-Nutzung als Tagesueberblick zeigen.",
            icon = Icons.Outlined.PhoneAndroid,
            permissionKey = kind.permissionKey,
            outputType = "usage_stats",
        )
        AreaSkillKind.WEBSITE_READER -> SkillDefinition(
            kind = kind,
            label = "Website",
            description = "Eine Webseite regelmaessig abrufen und Aenderungen zeigen.",
            icon = Icons.Outlined.Language,
            permissionKey = null,
            outputType = "website_snapshot",
        )
        AreaSkillKind.PODCAST_FOLLOW -> SkillDefinition(
            kind = kind,
            label = "Podcast",
            description = "Einen RSS/Atom-Feed beobachten und neue Folgen melden.",
            icon = Icons.Outlined.Podcasts,
            permissionKey = null,
            outputType = "podcast_episodes",
        )
        AreaSkillKind.LOCATION_CONTEXT -> SkillDefinition(
            kind = kind,
            label = "Standort",
            description = "Naehe zu einem Zielort pruefen und kontextbezogen reagieren.",
            icon = Icons.Outlined.LocationOn,
            permissionKey = kind.permissionKey,
            outputType = "location_state",
        )
        AreaSkillKind.CHECKLIST -> SkillDefinition(
            kind = kind,
            label = "Checkliste",
            description = "Eine lokale Aufgabenliste fuehren und Fortschritt anzeigen.",
            icon = Icons.Outlined.Checklist,
            permissionKey = null,
            outputType = "checklist_items",
        )
    }
}

fun skillsForKeywords(text: String): List<AreaSkillKind> {
    val lower = text.lowercase()
    val matched = mutableListOf<AreaSkillKind>()
    if (containsKeyword(lower, "kalender", "termin", "meeting", "besprech")) {
        matched += AreaSkillKind.CALENDAR_WATCH
    }
    if (containsKeyword(lower, "screenshot", "bildschirmfoto")) {
        matched += AreaSkillKind.SCREENSHOT_READER
    }
    if (containsKeyword(lower, "foto", "kamera", "photo")) {
        matched += AreaSkillKind.PHOTO_STREAM
    }
    if (containsKeyword(lower, "podcast", "feed", "rss")) {
        matched += AreaSkillKind.PODCAST_FOLLOW
    }
    if (containsKeyword(lower, "nachricht", "kontakt", "benachricht", "notification")) {
        matched += AreaSkillKind.NOTIFICATION_FILTER
        matched += AreaSkillKind.CONTACT_WATCH
    }
    if (containsKeyword(lower, "gesund", "schlaf", "health", "schritte", "bewegung")) {
        matched += AreaSkillKind.HEALTH_TRACKING
    }
    if (containsKeyword(lower, "ort", "zuhause", "standort", "location")) {
        matched += AreaSkillKind.LOCATION_CONTEXT
    }
    if (containsKeyword(lower, "aufgabe", "liste", "todo", "checkliste")) {
        matched += AreaSkillKind.CHECKLIST
    }
    if (containsKeyword(lower, "website", "seite", "webseite", "url", "http")) {
        matched += AreaSkillKind.WEBSITE_READER
    }
    if (containsKeyword(lower, "bildschirm", "screen time", "app-nutzung", "nutzung")) {
        matched += AreaSkillKind.APP_USAGE
    }
    return matched.distinct()
}

fun skillRequiresConfig(kind: AreaSkillKind): Boolean {
    return when (kind) {
        AreaSkillKind.WEBSITE_READER,
        AreaSkillKind.PODCAST_FOLLOW,
        AreaSkillKind.LOCATION_CONTEXT,
        -> true
        else -> false
    }
}

fun skillConfigFields(kind: AreaSkillKind): List<ConfigField> {
    return when (kind) {
        AreaSkillKind.WEBSITE_READER -> listOf(
            ConfigField(
                key = "url",
                label = "URL",
                placeholder = "https://...",
                type = ConfigFieldType.URL,
            ),
        )
        AreaSkillKind.PODCAST_FOLLOW -> listOf(
            ConfigField(
                key = "feedUrl",
                label = "Feed-URL",
                placeholder = "https://... /feed.xml",
                type = ConfigFieldType.URL,
            ),
        )
        AreaSkillKind.LOCATION_CONTEXT -> listOf(
            ConfigField(
                key = "latitude",
                label = "Breitengrad",
                placeholder = "52.5200",
                type = ConfigFieldType.TEXT,
            ),
            ConfigField(
                key = "longitude",
                label = "Laengengrad",
                placeholder = "13.4050",
                type = ConfigFieldType.TEXT,
            ),
        )
        else -> emptyList()
    }
}

private fun containsKeyword(text: String, vararg keywords: String): Boolean {
    return keywords.any { it in text }
}
