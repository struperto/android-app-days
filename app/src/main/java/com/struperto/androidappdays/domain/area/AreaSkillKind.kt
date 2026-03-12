package com.struperto.androidappdays.domain.area

import com.struperto.androidappdays.domain.DataSourceKind

enum class AreaSkillKind(
    val persistedValue: String,
    val label: String,
    val permissionKey: String?,
    val sourceKind: DataSourceKind?,
) {
    HEALTH_TRACKING(
        persistedValue = "health_tracking",
        label = "Gesundheit",
        permissionKey = "health_connect",
        sourceKind = DataSourceKind.HEALTH_CONNECT,
    ),
    CALENDAR_WATCH(
        persistedValue = "calendar_watch",
        label = "Kalender",
        permissionKey = "read_calendar",
        sourceKind = DataSourceKind.CALENDAR,
    ),
    NOTIFICATION_FILTER(
        persistedValue = "notification_filter",
        label = "Benachrichtigungen",
        permissionKey = "notification_listener",
        sourceKind = DataSourceKind.NOTIFICATIONS,
    ),
    MANUAL_LOG(
        persistedValue = "manual_log",
        label = "Manuell",
        permissionKey = null,
        sourceKind = DataSourceKind.MANUAL,
    ),
    SCREENSHOT_READER(
        persistedValue = "screenshot_reader",
        label = "Screenshots",
        permissionKey = "read_media_images",
        sourceKind = null,
    ),
    PHOTO_STREAM(
        persistedValue = "photo_stream",
        label = "Fotos",
        permissionKey = "read_media_images",
        sourceKind = null,
    ),
    CONTACT_WATCH(
        persistedValue = "contact_watch",
        label = "Kontakte",
        permissionKey = "read_contacts",
        sourceKind = null,
    ),
    APP_USAGE(
        persistedValue = "app_usage",
        label = "App-Nutzung",
        permissionKey = "package_usage_stats",
        sourceKind = null,
    ),
    WEBSITE_READER(
        persistedValue = "website_reader",
        label = "Website",
        permissionKey = null,
        sourceKind = null,
    ),
    PODCAST_FOLLOW(
        persistedValue = "podcast_follow",
        label = "Podcast",
        permissionKey = null,
        sourceKind = null,
    ),
    LOCATION_CONTEXT(
        persistedValue = "location_context",
        label = "Standort",
        permissionKey = "access_fine_location",
        sourceKind = null,
    ),
    CHECKLIST(
        persistedValue = "checklist",
        label = "Checkliste",
        permissionKey = null,
        sourceKind = null,
    ),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaSkillKind? {
            return entries.firstOrNull { it.persistedValue == value }
        }
    }
}
