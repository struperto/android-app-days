package com.struperto.androidappdays.domain.area

enum class TileDisplayMode(
    val persistedValue: String,
    val label: String,
) {
    AMPEL("ampel", "Ampel"),
    SCORE("score", "Score"),
    SHORT_TEXT("short_text", "Kurztext"),
    LATEST_NAME("latest_name", "Letzter Name"),
    COUNT("count", "Anzahl"),
    TREND("trend", "Trend"),
    ;

    companion object {
        fun fromPersistedValue(value: String): TileDisplayMode {
            return entries.firstOrNull { it.persistedValue == value } ?: AMPEL
        }
    }
}
