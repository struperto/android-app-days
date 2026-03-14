package com.struperto.androidappdays.data.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

@Database(
    entities = [
        CaptureItemEntity::class,
        VorhabenEntity::class,
        PlanItemEntity::class,
        NotificationSignalEntity::class,
        UserFingerprintEntity::class,
        LearningEventEntity::class,
        AreaInstanceEntity::class,
        AreaSnapshotEntity::class,
        LifeAreaEntity::class,
        LifeAreaDailyCheckEntity::class,
        LifeAreaProfileEntity::class,
        SingleSetupStateEntity::class,
        DomainGoalEntity::class,
        ObservationEventEntity::class,
        DomainCatalogEntity::class,
        SourcePreferenceEntity::class,
        AreaSourceBindingEntity::class,
        HourSlotEntryEntity::class,
        AreaWebFeedSourceEntity::class,
    ],
    version = 16,
    exportSchema = true,
)
abstract class SingleDatabase : RoomDatabase() {
    abstract fun captureItemDao(): CaptureItemDao
    abstract fun vorhabenDao(): VorhabenDao
    abstract fun planItemDao(): PlanItemDao
    abstract fun notificationSignalDao(): NotificationSignalDao
    abstract fun userFingerprintDao(): UserFingerprintDao
    abstract fun learningEventDao(): LearningEventDao
    abstract fun areaKernelDao(): AreaKernelDao
    abstract fun lifeWheelDao(): LifeWheelDao
    abstract fun lifeAreaProfileDao(): LifeAreaProfileDao
    abstract fun domainGoalDao(): DomainGoalDao
    abstract fun observationEventDao(): ObservationEventDao
    abstract fun domainCatalogDao(): DomainCatalogDao
    abstract fun sourcePreferenceDao(): SourcePreferenceDao
    abstract fun areaSourceBindingDao(): AreaSourceBindingDao
    abstract fun hourSlotEntryDao(): HourSlotEntryDao
    abstract fun areaWebFeedSourceDao(): AreaWebFeedSourceDao
}

@Entity(tableName = "capture_items")
data class CaptureItemEntity(
    @PrimaryKey val id: String,
    val text: String,
    val areaId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val status: String,
) {
    companion object {
        const val STATUS_OPEN = "open"
        const val STATUS_CONVERTED = "converted"
        const val STATUS_ARCHIVED = "archived"
    }
}

@Entity(tableName = "vorhaben")
data class VorhabenEntity(
    @PrimaryKey val id: String,
    val title: String,
    val note: String,
    val areaId: String,
    val sourceCaptureId: String?,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_ARCHIVED = "archived"
    }
}

@Entity(tableName = "plan_items")
data class PlanItemEntity(
    @PrimaryKey val id: String,
    val vorhabenId: String?,
    val title: String,
    val note: String,
    val areaId: String,
    val timeBlock: String,
    val plannedDate: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val STATUS_OPEN = "open"
        const val STATUS_DONE = "done"
    }
}

@Entity(tableName = "notification_signals")
data class NotificationSignalEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val removedAt: Long?,
    val updatedAt: Long,
)

@Entity(tableName = "user_fingerprint")
data class UserFingerprintEntity(
    @PrimaryKey val id: Int = 0,
    val roles: String,
    val responsibilities: String,
    val priorityRules: String,
    val weeklyRhythm: String,
    val recurringCommitments: String,
    val goodDayPattern: String,
    val badDayPattern: String,
    val dayStartHour: Int,
    val dayEndHour: Int,
    val morningEnergy: Int,
    val afternoonEnergy: Int,
    val eveningEnergy: Int,
    val focusStrength: Int,
    val disruptionSensitivity: Int,
    val recoveryNeed: Int,
    val discoveryCommitted: Boolean,
    val updatedAt: Long,
)

@Entity(tableName = "learning_events")
data class LearningEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val detail: String,
    val createdAt: Long,
    val day: String,
)

@Entity(tableName = "life_areas")
data class LifeAreaEntity(
    @PrimaryKey val id: String,
    val label: String,
    val definition: String,
    val targetScore: Int,
    val sortOrder: Int,
    val isActive: Boolean,
    val templateId: String,
    val iconKey: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "life_area_daily_checks",
    primaryKeys = ["areaId", "date"],
)
data class LifeAreaDailyCheckEntity(
    val areaId: String,
    val date: String,
    val manualScore: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "life_area_profiles")
data class LifeAreaProfileEntity(
    @PrimaryKey val areaId: String,
    val cadence: String,
    val intensity: Int,
    val signalBlend: Int,
    val selectedTracks: String,
    val remindersEnabled: Boolean,
    val reviewEnabled: Boolean,
    val experimentsEnabled: Boolean,
    val updatedAt: Long,
)

@Entity(tableName = "single_setup_state")
data class SingleSetupStateEntity(
    @PrimaryKey val id: Int = 0,
    val isLifeWheelConfigured: Boolean,
    val updatedAt: Long,
)

@Entity(tableName = "domain_goals")
data class DomainGoalEntity(
    @PrimaryKey val id: String,
    val domain: String,
    val title: String,
    val cadence: String,
    val targetKind: String,
    val unit: String,
    val minimum: Float?,
    val maximum: Float?,
    val exact: Float?,
    val note: String,
    val adaptationMode: String,
    val preferredStartHour: Int?,
    val preferredEndHourExclusive: Int?,
    val priority: String,
    val isActive: Boolean,
    val rationale: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "observation_events")
data class ObservationEventEntity(
    @PrimaryKey val id: String,
    val goalId: String?,
    val domain: String,
    val metric: String,
    val source: String,
    val startedAt: Long,
    val endedAt: Long?,
    val numericValue: Float?,
    val booleanValue: Boolean?,
    val textValue: String?,
    val unit: String?,
    val logicalDate: String?,
    val sourceRecordId: String?,
    val confidence: Float,
    val contextTags: String,
    val updatedAt: Long,
)

@Entity(tableName = "domain_catalog")
data class DomainCatalogEntity(
    @PrimaryKey val domain: String,
    val title: String,
    val summary: String,
    val priority: String,
    val isActive: Boolean,
    val isImplemented: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "source_preferences")
data class SourcePreferenceEntity(
    @PrimaryKey val source: String,
    val label: String,
    val isEnabled: Boolean,
    val updatedAt: Long,
)

@Entity(tableName = "hour_slot_entries")
data class HourSlotEntryEntity(
    @PrimaryKey val id: String,
    val logicalDate: String,
    val segmentId: String,
    val logicalHour: Int,
    val windowId: String,
    val status: String,
    val note: String,
    val updatedAt: Long,
)
