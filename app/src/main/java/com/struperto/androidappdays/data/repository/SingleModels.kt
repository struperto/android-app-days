package com.struperto.androidappdays.data.repository

data class CaptureItem(
    val id: String,
    val text: String,
    val areaId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val status: String,
)

data class Vorhaben(
    val id: String,
    val title: String,
    val note: String,
    val areaId: String,
    val sourceCaptureId: String?,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class PlanItem(
    val id: String,
    val vorhabenId: String?,
    val title: String,
    val note: String,
    val areaId: String,
    val timeBlock: TimeBlock,
    val plannedDate: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val isDone: Boolean
        get() = status == PlanItemStatus.DONE
}

object CaptureItemStatus {
    const val OPEN = "open"
    const val CONVERTED = "converted"
    const val ARCHIVED = "archived"
}

object VorhabenStatus {
    const val ACTIVE = "active"
    const val ARCHIVED = "archived"
}

object PlanItemStatus {
    const val OPEN = "open"
    const val DONE = "done"
}

data class LifeArea(
    val id: String,
    val label: String,
    val definition: String,
    val targetScore: Int,
    val sortOrder: Int,
    val isActive: Boolean,
)

data class LifeAreaDailyCheck(
    val areaId: String,
    val date: String,
    val manualScore: Int,
)

data class LifeAreaProfile(
    val areaId: String,
    val cadence: String,
    val intensity: Int,
    val signalBlend: Int,
    val selectedTracks: Set<String>,
    val remindersEnabled: Boolean,
    val reviewEnabled: Boolean,
    val experimentsEnabled: Boolean,
)

data class CalendarSignal(
    val id: Long,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
)

data class NotificationSignal(
    val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val removedAt: Long?,
)

data class SingleSetupState(
    val isLifeWheelConfigured: Boolean,
)

enum class TimeBlock(
    val persistedValue: String,
    val label: String,
    val order: Int,
) {
    MORGEN("morgen", "Morgen", 0),
    MITTAG("mittag", "Mittag", 1),
    NACHMITTAG("nachmittag", "Nachmittag", 2),
    ABEND("abend", "Abend", 3),
    ;

    companion object {
        val all = entries.toList()

        fun fromPersistedValue(value: String): TimeBlock {
            return all.firstOrNull { it.persistedValue == value } ?: MORGEN
        }
    }
}
