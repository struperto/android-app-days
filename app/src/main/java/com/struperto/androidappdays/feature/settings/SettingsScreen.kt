package com.struperto.androidappdays.feature.settings

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.health.connect.client.PermissionController
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.feature.single.shared.DaysTopBarAction
import com.struperto.androidappdays.ui.theme.AppTheme

private const val HealthConnectProviderPackage = "com.google.android.apps.healthdata"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenDomains: () -> Unit,
    onOpenSources: () -> Unit,
) {
    LifecycleResumeEffect(Unit) {
        onRefresh()
        onPauseOrDispose { }
    }

    DaysPageScaffold(
        title = state.title,
        onBack = onBack,
        modifier = Modifier.testTag("settings-root"),
        action = DaysTopBarAction(
            icon = Icons.Outlined.Sync,
            contentDescription = "Aktualisieren",
            onClick = onRefresh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-root-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            IntroCard(
                sourceCount = state.sources.size,
                goalCount = state.goals.size,
            )
            SectionHeader(
                title = "Struktur",
                detail = "Einstellungen bleiben schlank. Domaenen und Quellen liegen hier klar getrennt vom Produktfluss.",
            )
            SettingsMenuCard(
                title = "Domaenen",
                detail = "Alle Lebensbereiche als strukturierte Unterseiten. Dort liegen Ziele, manuelle Werte und Domaintests.",
                pill = "${state.catalog.size} Bereiche",
                testTag = "settings-menu-domains",
                onClick = onOpenDomains,
            )
            SettingsMenuCard(
                title = "Quellen",
                detail = "Passive Signale, Berechtigungen und technische Datenpfade getrennt vom Fachsetup.",
                pill = "${state.sources.count { it.enabled }} aktiv",
                testTag = "settings-menu-sources",
                onClick = onOpenSources,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDomainsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onOpenDomain: (LifeDomain) -> Unit,
) {
    DaysPageScaffold(
        title = "Domaenen",
        onBack = onBack,
        modifier = Modifier.testTag("settings-domains"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-domains-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionHeader(
                title = "Lebensbereiche",
                detail = "Hier werden Ziele, manuelle Eingaben und spaetere Experimente pro Domaene gebuendelt.",
            )
            state.catalog.forEach { item ->
                SettingsMenuCard(
                    title = item.title,
                    detail = item.summary,
                    pill = item.statusLabel,
                    testTag = "settings-domain-${item.domain.name.lowercase()}",
                    onClick = { onOpenDomain(item.domain) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDomainDetailScreen(
    state: SettingsUiState,
    domain: LifeDomain,
    onBack: () -> Unit,
    onSaveGoal: (String, String, String, String, String) -> Unit,
    onSaveManualMetric: (String?, LifeDomain, ObservationMetric, String, String) -> Unit,
) {
    val catalog = state.catalog.firstOrNull { it.domain == domain }
    val goals = state.goals.filter { it.domain == domain }
    val manualMetrics = state.manualMetrics.filter { it.domain == domain }

    DaysPageScaffold(
        title = catalog?.title ?: domain.name,
        onBack = onBack,
        modifier = Modifier.testTag("settings-domain-detail"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-domain-detail-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DomainHeroTile(
                domain = domain,
                title = catalog?.title ?: domain.name,
                summary = catalog?.summary ?: "Hier wird diese Domaene spaeter fachlich ausgebaut.",
            )
            DomainWorkbenchTile(
                domain = domain,
                goals = goals,
                manualMetrics = manualMetrics,
                sources = state.sources,
                sourceDetail = relevantSourcesFor(domain),
                onSaveGoal = onSaveGoal,
                onSaveManualMetric = onSaveManualMetric,
            )
        }
    }
}

@Composable
private fun DomainHeroTile(
    domain: LifeDomain,
    title: String,
    summary: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Pulse",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.muted,
                )
                StatusPill(label = "Bearbeiten")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(settingsDomainTint(domain).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = settingsDomainIcon(domain),
                        contentDescription = null,
                        tint = settingsDomainTint(domain),
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DomainWorkbenchTile(
    domain: LifeDomain,
    goals: List<SettingsGoalItem>,
    manualMetrics: List<SettingsManualMetricItem>,
    sources: List<SettingsSourceItem>,
    sourceDetail: String,
    onSaveGoal: (String, String, String, String, String) -> Unit,
    onSaveManualMetric: (String?, LifeDomain, ObservationMetric, String, String) -> Unit,
) {
    val primaryGoal = goals.firstOrNull()
    val primaryManualMetric = manualMetrics.firstOrNull()
    val relevantSources = relevantSourceItems(domain, sources)

    Card(
        modifier = Modifier.testTag("settings-domain-workbench"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Heute",
                style = AppTheme.typography.label,
                color = AppTheme.colors.muted,
            )

            WorkbenchValueRow(
                label = "Soll",
                value = goalSummaryValue(primaryGoal),
                detail = goalSummaryDetail(primaryGoal),
            )
            WorkbenchDivider()
            WorkbenchValueRow(
                label = "Ist",
                value = manualSummaryValue(primaryManualMetric),
                detail = manualSummaryDetail(primaryManualMetric),
            )
            WorkbenchDivider()
            SectionText(label = "Signale")
            SourceSummaryBlock(
                domain = domain,
                detail = sourceDetail,
                sources = relevantSources,
            )

            if (goals.isEmpty() && manualMetrics.isEmpty()) {
                WorkbenchDivider()
                Text(
                    text = "Diese Domaene ist vorbereitet. Sobald Ziel oder Tageswert auftauchen, landen sie gesammelt hier.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (goals.isNotEmpty()) {
                WorkbenchDivider()
                SectionText(label = "Ziel")
                goals.forEachIndexed { index, item ->
                    if (index > 0) {
                        WorkbenchDivider()
                    }
                    GoalEditorBlock(
                        item = item,
                        showTitle = goals.size > 1,
                        onSave = onSaveGoal,
                    )
                }
            }

            if (manualMetrics.isNotEmpty()) {
                WorkbenchDivider()
                SectionText(label = "Eintrag")
                manualMetrics.forEachIndexed { index, item ->
                    if (index > 0) {
                        WorkbenchDivider()
                    }
                    ManualMetricEditorBlock(
                        item = item,
                        showTitle = manualMetrics.size > 1,
                        onSave = onSaveManualMetric,
                    )
                }
            }

        }
    }
}

@Composable
private fun WorkbenchValueRow(
    label: String,
    value: String,
    detail: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.label,
            color = AppTheme.colors.muted,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionText(label: String) {
    Text(
        text = label,
        style = AppTheme.typography.label,
        color = AppTheme.colors.muted,
    )
}

@Composable
private fun WorkbenchDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
    )
}

@Composable
private fun GoalEditorBlock(
    item: SettingsGoalItem,
    showTitle: Boolean,
    onSave: (String, String, String, String, String) -> Unit,
) {
    var minimumText by rememberSaveable(item.id) { mutableStateOf(item.minimumText) }
    var maximumText by rememberSaveable(item.id) { mutableStateOf(item.maximumText) }
    var preferredStart by rememberSaveable(item.id) { mutableStateOf(item.preferredStartHour?.toString().orEmpty()) }
    var preferredEnd by rememberSaveable(item.id) { mutableStateOf(item.preferredEndHourExclusive?.toString().orEmpty()) }

    LaunchedEffect(item.minimumText, item.maximumText, item.preferredStartHour, item.preferredEndHourExclusive) {
        minimumText = item.minimumText
        maximumText = item.maximumText
        preferredStart = item.preferredStartHour?.toString().orEmpty()
        preferredEnd = item.preferredEndHourExclusive?.toString().orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-goal-${item.domain.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (showTitle) item.title else "Zielwert",
                style = MaterialTheme.typography.titleMedium,
            )
            StatusPill(label = item.unit)
        }
        Text(
            text = goalSummaryDetail(item),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumericField(
                label = "Min",
                value = minimumText,
                onValueChange = { minimumText = it },
                testTag = "settings-goal-${item.domain.name.lowercase()}-min",
                modifier = Modifier.weight(1f),
            )
            if (item.maximumText.isNotBlank()) {
                NumericField(
                    label = "Max",
                    value = maximumText,
                    onValueChange = { maximumText = it },
                    testTag = "settings-goal-${item.domain.name.lowercase()}-max",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (item.preferredStartHour != null && item.preferredEndHourExclusive != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumericField(
                    label = "Start",
                    value = preferredStart,
                    onValueChange = { preferredStart = it },
                    testTag = "settings-goal-${item.domain.name.lowercase()}-start",
                    modifier = Modifier.weight(1f),
                )
                NumericField(
                    label = "Ende",
                    value = preferredEnd,
                    onValueChange = { preferredEnd = it },
                    testTag = "settings-goal-${item.domain.name.lowercase()}-end",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = {
                    onSave(
                        item.id,
                        minimumText,
                        maximumText,
                        preferredStart,
                        preferredEnd,
                    )
                },
                modifier = Modifier.testTag("settings-goal-${item.domain.name.lowercase()}-save"),
            ) {
                Icon(imageVector = Icons.Outlined.Save, contentDescription = null)
                Text(text = "Speichern", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSourcesScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSetSourceEnabled: (DataSourceKind, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        onRefresh()
    }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(
            providerPackageName = HealthConnectProviderPackage,
        ),
    ) {
        onRefresh()
    }

    LifecycleResumeEffect(Unit) {
        onRefresh()
        onPauseOrDispose { }
    }

    DaysPageScaffold(
        title = "Quellen",
        onBack = onBack,
        modifier = Modifier.testTag("settings-sources"),
        action = DaysTopBarAction(
            icon = Icons.Outlined.Sync,
            contentDescription = "Aktualisieren",
            onClick = onRefresh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-sources-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionHeader(
                title = "Passive Signale",
                detail = "Nur verfuegbare Systeme speisen das Ist. Alles andere bleibt neutral und erzeugt kein falsches Rot.",
            )
            state.sources.forEach { item ->
                SourceCard(
                    item = item,
                    onToggle = { enabled -> onSetSourceEnabled(item.source, enabled) },
                    onAction = {
                        when (item.source) {
                            DataSourceKind.HEALTH_CONNECT -> {
                                if (item.available) {
                                    healthPermissionLauncher.launch(state.healthPermissions)
                                }
                            }
                            DataSourceKind.CALENDAR -> {
                                calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                            }
                            DataSourceKind.NOTIFICATIONS -> {
                                context.startActivity(
                                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    },
                                )
                            }
                            DataSourceKind.MANUAL -> Unit
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun IntroCard(
    sourceCount: Int,
    goalCount: Int,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Kalibrierung",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Home bleibt still. Hier justierst du Quellen, Ziele und erste Muster.",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusPill(label = "$sourceCount Quellen")
                StatusPill(label = "$goalCount Ziele")
            }
        }
    }
}

@Composable
private fun SettingsMenuCard(
    title: String,
    detail: String,
    pill: String,
    testTag: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            StatusPill(label = pill)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    detail: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusDot(granted: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

@Composable
private fun SourceCard(
    item: SettingsSourceItem,
    onToggle: (Boolean) -> Unit,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.testTag("settings-source-${item.source.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = when (item.source) {
                            DataSourceKind.HEALTH_CONNECT -> Icons.Outlined.FavoriteBorder
                            DataSourceKind.CALENDAR -> Icons.Outlined.Tune
                            DataSourceKind.NOTIFICATIONS -> Icons.Outlined.NotificationsActive
                            DataSourceKind.MANUAL -> Icons.Outlined.EditNote
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusDot(granted = item.granted)
                            Text(text = item.label, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            text = item.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = item.enabled,
                    onCheckedChange = onToggle,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    label = when {
                        item.granted -> "bereit"
                        item.available -> "teilweise"
                        else -> "nicht verfuegbar"
                    },
                )
                if (item.source != DataSourceKind.MANUAL) {
                    TextButton(onClick = onAction) {
                        Text(
                            text = if (item.granted) "neu lesen" else "freigeben",
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualMetricEditorBlock(
    item: SettingsManualMetricItem,
    showTitle: Boolean,
    onSave: (String?, LifeDomain, ObservationMetric, String, String) -> Unit,
) {
    var valueText by rememberSaveable(item.domain, item.metric) { mutableStateOf(item.valueText) }

    LaunchedEffect(item.valueText) {
        valueText = item.valueText
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-manual-${item.domain.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (showTitle) item.label else "Tageswert",
                style = MaterialTheme.typography.titleMedium,
            )
            StatusPill(label = item.unit)
        }
        Text(
            text = manualSummaryDetail(item),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NumericField(
            label = "Ist heute",
            value = valueText,
            onValueChange = { valueText = it },
            testTag = "settings-manual-${item.domain.name.lowercase()}-value",
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = {
                    onSave(
                        item.goalId,
                        item.domain,
                        item.metric,
                        valueText,
                        item.unit,
                    )
                },
                modifier = Modifier.testTag("settings-manual-${item.domain.name.lowercase()}-save"),
            ) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                Text(text = "Uebernehmen", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun SourceSummaryBlock(
    domain: LifeDomain,
    detail: String,
    sources: List<SettingsSourceItem>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SignalPillRow(sources = sources)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SignalPillRow(
    sources: List<SettingsSourceItem>,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sources.forEach { item ->
            SignalPill(item = item)
        }
    }
}

@Composable
private fun SignalPill(item: SettingsSourceItem) {
    val active = item.enabled && item.available && (item.granted || item.source == DataSourceKind.MANUAL)
    Surface(
        color = if (active) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = item.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (active) MaterialTheme.colorScheme.onSecondaryContainer else AppTheme.colors.muted,
        )
    }
}

@Composable
private fun StatusPill(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 1.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun NumericField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String? = null,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = if (testTag == null) modifier else modifier.testTag(testTag),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
        ),
    )
}

private fun settingsDomainIcon(domain: LifeDomain): ImageVector {
    return when (domain) {
        LifeDomain.SLEEP -> Icons.Outlined.Hotel
        LifeDomain.MOVEMENT -> Icons.AutoMirrored.Outlined.DirectionsWalk
        LifeDomain.HYDRATION -> Icons.Outlined.WaterDrop
        LifeDomain.NUTRITION -> Icons.Outlined.Restaurant
        LifeDomain.FOCUS -> Icons.Outlined.CenterFocusStrong
        LifeDomain.RECOVERY -> Icons.Outlined.FavoriteBorder
        LifeDomain.STRESS -> Icons.Outlined.NotificationsActive
        else -> Icons.Outlined.EditNote
    }
}

@Composable
private fun settingsDomainTint(domain: LifeDomain): androidx.compose.ui.graphics.Color {
    return when (domain) {
        LifeDomain.SLEEP -> AppTheme.colors.info
        LifeDomain.MOVEMENT -> AppTheme.colors.success
        LifeDomain.HYDRATION -> AppTheme.colors.info
        LifeDomain.NUTRITION -> AppTheme.colors.warning
        LifeDomain.FOCUS -> AppTheme.colors.accent
        LifeDomain.RECOVERY -> AppTheme.colors.success
        LifeDomain.STRESS -> AppTheme.colors.danger
        else -> AppTheme.colors.muted
    }
}

private fun relevantSourcesFor(domain: LifeDomain): String {
    return when (domain) {
        LifeDomain.SLEEP -> "Primaer ueber Health Connect oder Wearable, optional manuelle Korrektur."
        LifeDomain.MOVEMENT -> "Primaer ueber Schritte, Workouts und Health Connect, spaeter adaptiv mit Tageskontext."
        LifeDomain.HYDRATION -> "Im MVP hauptsaechlich manuell, spaeter mit Triggern und Tagesfenstern."
        LifeDomain.NUTRITION -> "Im MVP manuell. Spaeter Import, Mahlzeitenlogik und Timing-Muster."
        LifeDomain.FOCUS -> "Kalender, Notifications und aktive Korrekturen sind hier wichtiger als reine Sensorik."
        LifeDomain.STRESS -> "Notifications sind hier das erste passive Signal, manuelle Korrektur bleibt moeglich."
        else -> "Dieser Bereich ist strukturell vorbereitet, aber im MVP noch nicht voll an passive Quellen angebunden."
    }
}

private fun goalSummaryValue(goal: SettingsGoalItem?): String {
    if (goal == null) return "offen"
    val minimum = goal.minimumText.takeIf { it.isNotBlank() }
    val maximum = goal.maximumText.takeIf { it.isNotBlank() }
    val value = when {
        minimum != null && maximum != null -> "$minimum-$maximum ${goal.unit}"
        minimum != null -> "$minimum ${goal.unit}"
        maximum != null -> "$maximum ${goal.unit}"
        else -> goal.unit
    }
    return value.trim()
}

private fun goalSummaryDetail(goal: SettingsGoalItem?): String {
    if (goal == null) return "noch kein Soll"
    val start = goal.preferredStartHour
    val end = goal.preferredEndHourExclusive
    return if (start != null && end != null) {
        "${displayHourCompact(start)}-${displayHourCompact(end)}"
    } else {
        "ohne Zeitfenster"
    }
}

private fun manualSummaryValue(item: SettingsManualMetricItem?): String {
    if (item == null) return "leer"
    return item.valueText.takeIf { it.isNotBlank() }?.let { "$it ${item.unit}" } ?: "leer"
}

private fun manualSummaryDetail(item: SettingsManualMetricItem?): String {
    return if (item?.valueText?.isNotBlank() == true) "heute erfasst" else "heute leer"
}

private fun displayHourCompact(hour: Int): String = "%02d".format(hour.mod(24))

private fun relevantSourceItems(
    domain: LifeDomain,
    sources: List<SettingsSourceItem>,
): List<SettingsSourceItem> {
    return sources.filter { item ->
        when (domain) {
            LifeDomain.SLEEP,
            LifeDomain.MOVEMENT,
            -> item.source in setOf(DataSourceKind.HEALTH_CONNECT, DataSourceKind.MANUAL)
            LifeDomain.HYDRATION,
            LifeDomain.NUTRITION,
            -> item.source == DataSourceKind.MANUAL
            LifeDomain.FOCUS,
            -> item.source in setOf(DataSourceKind.CALENDAR, DataSourceKind.NOTIFICATIONS, DataSourceKind.MANUAL)
            LifeDomain.STRESS,
            -> item.source in setOf(DataSourceKind.NOTIFICATIONS, DataSourceKind.MANUAL)
            else -> item.source == DataSourceKind.MANUAL
        }
    }
}
