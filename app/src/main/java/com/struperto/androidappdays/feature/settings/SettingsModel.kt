package com.struperto.androidappdays.feature.settings

import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric

data class SettingsSourceItem(
    val source: DataSourceKind,
    val label: String,
    val detail: String,
    val enabled: Boolean,
    val available: Boolean,
    val granted: Boolean,
)

data class SettingsGoalItem(
    val id: String,
    val domain: LifeDomain,
    val title: String,
    val priorityLabel: String,
    val unit: String,
    val minimumText: String,
    val maximumText: String,
    val preferredStartHour: Int?,
    val preferredEndHourExclusive: Int?,
)

data class SettingsManualMetricItem(
    val goalId: String?,
    val domain: LifeDomain,
    val label: String,
    val metric: ObservationMetric,
    val unit: String,
    val valueText: String,
)

data class SettingsCatalogItem(
    val domain: LifeDomain,
    val title: String,
    val summary: String,
    val statusLabel: String,
    val statusDetail: String,
)

data class SettingsAreaItem(
    val id: String,
    val title: String,
    val summary: String,
    val statusLabel: String,
    val statusDetail: String,
    val sourceLabel: String,
    val sourceDetail: String,
    val materialLabel: String,
    val materialDetail: String,
    val nextStepLabel: String,
    val nextStepDetail: String,
)

data class SettingsInboxItem(
    val id: String,
    val title: String,
    val detail: String,
    val kindLabel: String,
)

data class SettingsFeedSourceItem(
    val url: String,
    val hostLabel: String,
    val sourceKindLabel: String,
    val autoSyncEnabled: Boolean,
    val syncCadenceLabel: String,
    val goalLabel: String,
    val capabilityLabels: List<String>,
    val lastStatusLabel: String,
    val lastStatusDetail: String,
)

data class SettingsFeedAreaItem(
    val areaId: String,
    val areaTitle: String,
    val sourceCount: Int,
    val activeAutoSyncCount: Int,
    val summary: String,
    val goalLabel: String,
    val capabilityLabels: List<String>,
    val sources: List<SettingsFeedSourceItem>,
)

data class SettingsAnalysisItem(
    val title: String,
    val statusLabel: String,
    val detail: String,
)

data class SettingsAnalysisGoal(
    val title: String,
    val detail: String,
    val progressLabel: String,
)

data class SettingsUiState(
    val title: String = "Einstellungen",
    val sources: List<SettingsSourceItem> = emptyList(),
    val goals: List<SettingsGoalItem> = emptyList(),
    val manualMetrics: List<SettingsManualMetricItem> = emptyList(),
    val catalog: List<SettingsCatalogItem> = emptyList(),
    val activeAreas: List<SettingsAreaItem> = emptyList(),
    val maxActiveAreas: Int = 16,
    val directAreaCount: Int = 0,
    val waitingAreaCount: Int = 0,
    val importAreaCount: Int = 0,
    val manualAreaCount: Int = 0,
    val assignedImportCount: Int = 0,
    val pendingImports: List<SettingsInboxItem> = emptyList(),
    val feedAreas: List<SettingsFeedAreaItem> = emptyList(),
    val activeFeedAreaCount: Int = 0,
    val activeFeedSourceCount: Int = 0,
    val analysisItems: List<SettingsAnalysisItem> = emptyList(),
    val analysisGoals: List<SettingsAnalysisGoal> = emptyList(),
    val healthPermissions: Set<String> = emptySet(),
)
