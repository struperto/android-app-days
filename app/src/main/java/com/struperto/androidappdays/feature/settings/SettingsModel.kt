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
)

data class SettingsUiState(
    val title: String = "Einstellungen",
    val sources: List<SettingsSourceItem> = emptyList(),
    val goals: List<SettingsGoalItem> = emptyList(),
    val manualMetrics: List<SettingsManualMetricItem> = emptyList(),
    val catalog: List<SettingsCatalogItem> = emptyList(),
    val healthPermissions: Set<String> = emptySet(),
)
