package com.struperto.androidappdays.domain.area

import java.time.Instant
import java.time.LocalDate

/**
 * Time-bound state of one area on one day.
 *
 * The snapshot is intentionally separate from [AreaInstance] so that stable configuration,
 * identity, and ordering do not drift into day-specific readings.
 */
data class AreaSnapshot(
    val areaId: String,
    val date: LocalDate,
    val manualScore: Int? = null,
    val manualStateKey: String? = null,
    val manualNote: String? = null,
    val confidence: Float? = null,
    val freshnessAt: Instant? = null,
) {
    val instanceId: String
        get() = areaId

    init {
        require(areaId.isNotBlank()) { "areaId must not be blank." }
        require(manualScore != null || manualStateKey != null || !manualNote.isNullOrBlank()) {
            "AreaSnapshot requires manualScore, manualStateKey, or manualNote."
        }
        require(manualScore == null || manualScore in 1..5) { "manualScore must be between 1 and 5." }
        require(manualStateKey == null || manualStateKey.isNotBlank()) {
            "manualStateKey must not be blank when provided."
        }
        require(manualNote == null || manualNote.isNotBlank()) {
            "manualNote must not be blank when provided."
        }
        require(confidence == null || confidence in 0f..1f) {
            "confidence must be between 0 and 1 when provided."
        }
    }
}
