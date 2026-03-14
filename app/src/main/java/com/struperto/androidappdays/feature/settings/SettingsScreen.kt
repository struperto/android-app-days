package com.struperto.androidappdays.feature.settings

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.feature.single.shared.DaysSectionHeading
import com.struperto.androidappdays.feature.single.shared.DaysTopBarAction
import com.struperto.androidappdays.ui.theme.AppTheme

private const val HealthConnectProviderPackage = "com.google.android.apps.healthdata"

private enum class SettingsTileTone {
    Neutral,
    Accent,
    Warm,
    Signal,
}

private data class SettingsTilePalette(
    val container: Color,
    val border: Color,
)

@Composable
private fun settingsTilePalette(tone: SettingsTileTone): SettingsTilePalette {
    return when (tone) {
        SettingsTileTone.Neutral -> SettingsTilePalette(
            container = AppTheme.colors.surfaceStrong.copy(alpha = 0.97f),
            border = AppTheme.colors.outlineSoft.copy(alpha = 0.72f),
        )
        SettingsTileTone.Accent -> SettingsTilePalette(
            container = AppTheme.colors.accentSoft.copy(alpha = 0.26f),
            border = AppTheme.colors.accentSoft.copy(alpha = 0.62f),
        )
        SettingsTileTone.Warm -> SettingsTilePalette(
            container = AppTheme.colors.surfaceMuted.copy(alpha = 0.92f),
            border = AppTheme.colors.accentSoft.copy(alpha = 0.5f),
        )
        SettingsTileTone.Signal -> SettingsTilePalette(
            container = AppTheme.colors.info.copy(alpha = 0.1f),
            border = AppTheme.colors.info.copy(alpha = 0.22f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTileFrame(
    modifier: Modifier = Modifier,
    tone: SettingsTileTone = SettingsTileTone.Neutral,
    hero: Boolean = false,
    testTag: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = settingsTilePalette(tone)
    val shape = RoundedCornerShape(if (hero) 32.dp else 28.dp)
    val resolvedModifier = if (testTag == null) modifier.fillMaxWidth() else modifier.fillMaxWidth().testTag(testTag)
    val padding = if (hero) 22.dp else 18.dp
    val spacing = if (hero) 16.dp else 14.dp

    if (onClick == null) {
        Card(
            modifier = resolvedModifier,
            colors = CardDefaults.cardColors(
                containerColor = palette.container,
            ),
            border = BorderStroke(1.dp, palette.border),
            shape = shape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = content,
            )
        }
    } else {
        Card(
            onClick = onClick,
            modifier = resolvedModifier,
            colors = CardDefaults.cardColors(
                containerColor = palette.container,
            ),
            border = BorderStroke(1.dp, palette.border),
            shape = shape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onOpenDomains: () -> Unit,
    onOpenFeeds: () -> Unit,
    onOpenSources: () -> Unit,
    onOpenInbox: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingsStatusMatrixCard(state = state)
            SettingsQuickMenuCard(
                testTag = "settings-menu-analysis",
                tone = SettingsTileTone.Accent,
                eyebrow = "Intelligenz",
                title = "Analysen",
                detail = "Routing, Bereichsformat, Android ML und naechste Entwicklungsziele klar im Blick halten.",
                pills = listOf(
                    "${state.analysisItems.size} Felder",
                    "${state.analysisGoals.size} Ziele",
                ),
                onClick = onOpenAnalysis,
            )
            SettingsPrimaryMenuCard(
                activeAreaCount = state.activeAreas.size,
                maxActiveAreas = state.maxActiveAreas,
                directAreaCount = state.directAreaCount,
                waitingAreaCount = state.waitingAreaCount,
                importAreaCount = state.importAreaCount,
                manualAreaCount = state.manualAreaCount,
                activeSourceCount = state.sources.count(::isSourceLive),
                activeFeedAreaCount = state.activeFeedAreaCount,
                activeFeedSourceCount = state.activeFeedSourceCount,
                pendingImportCount = state.pendingImports.size,
                onClick = onOpenDomains,
            )
            SettingsQuickMenuCard(
                testTag = "settings-menu-sources",
                tone = SettingsTileTone.Neutral,
                eyebrow = "Android",
                title = "Android-Zugriffe",
                detail = "Hier legst du nur fest, welche Systemzugriffe Days ueberhaupt nutzen darf.",
                pills = listOf("${state.sources.count(::isSourceLive)} live"),
                onClick = onOpenSources,
            )
        }
    }
}

@Composable
private fun SettingsStatusMatrixCard(
    state: SettingsUiState,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Neutral,
        hero = true,
        testTag = "settings-status-matrix",
    ) {
        Text(
            text = "Systemstatus",
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.ink,
        )
        Text(
            text = "Links steht die Schicht, rechts ihr aktueller Zustand. So bleibt Settings ruhig und lesbar statt zu einem Schalterlager zu werden.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.muted,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.5f))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SettingsStatusMatrixRow(
                label = "Stand",
                value = "${state.activeAreas.size}/${state.maxActiveAreas} Bereiche aktiv",
                detail = if (state.waitingAreaCount > 0) "${state.waitingAreaCount} warten noch auf mehr Klarheit." else "Aktive Bereiche und ihre Grundstruktur sind gesetzt.",
            )
            SettingsStatusMatrixRow(
                label = "Eingang",
                value = "${state.pendingImports.size} offen · ${state.sources.count(::isSourceLive)} live",
                detail = "Geteilte Inhalte, Android-Zugriffe und Web-Eingaenge speisen spaeter die Bereichsfeeds.",
            )
            SettingsStatusMatrixRow(
                label = "Fokus",
                value = "${state.analysisGoals.size} Ziele sichtbar",
                detail = "Analysen und Bereichsziele zeigen, wo der Produktkern gerade schaerfer werden muss.",
            )
            SettingsStatusMatrixRow(
                label = "Takt",
                value = "${state.activeFeedAreaCount} Feedbereiche",
                detail = "Der Takt zeigt, wie viele Bereiche schon regelmaessig Output in Richtung Single liefern.",
            )
        }
    }
}

@Composable
private fun SettingsStatusMatrixRow(
    label: String,
    value: String,
    detail: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppTheme.colors.surface.copy(alpha = 0.78f))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = AppTheme.typography.label,
                color = AppTheme.colors.muted,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.muted,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.ink,
        )
    }
}

@Composable
fun SettingsAnalysisScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
) {
    DaysPageScaffold(
        title = "Analysen",
        onBack = onBack,
        modifier = Modifier.testTag("settings-analysis"),
    ) {
        SettingsTileFrame(
            tone = SettingsTileTone.Accent,
            hero = true,
        ) {
            Text(
                text = "Analyse-Studio",
                style = MaterialTheme.typography.headlineSmall,
                color = AppTheme.colors.ink,
            )
            Text(
                text = "Routing zuerst. Danach Bereichsanalysen, kurze Rueckfragen und spaetere lokale ML-/AI-Bausteine.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusPill(label = "${state.activeAreas.size}/${state.maxActiveAreas} Bereiche")
                StatusPill(label = "${state.waitingAreaCount} warten")
                StatusPill(label = "${state.sources.count(::isSourceLive)} live")
            }
        }
        SettingsTileFrame(
            tone = SettingsTileTone.Signal,
            testTag = "settings-analysis-items",
        ) {
            Text(
                text = "Was jetzt moeglich ist",
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.ink,
            )
            state.analysisItems.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppTheme.colors.outlineSoft.copy(alpha = 0.45f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = AppTheme.colors.ink,
                        )
                        Text(
                            text = item.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.muted,
                        )
                    }
                    StatusPill(label = item.statusLabel)
                }
            }
        }
        SettingsTileFrame(
            tone = SettingsTileTone.Neutral,
            testTag = "settings-analysis-goals",
        ) {
            Text(
                text = "Naechste Ziele",
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.ink,
            )
            state.analysisGoals.forEachIndexed { index, goal ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppTheme.colors.outlineSoft.copy(alpha = 0.45f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = AppTheme.colors.ink,
                        )
                        Text(
                            text = goal.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.muted,
                        )
                    }
                    StatusPill(label = goal.progressLabel)
                }
            }
        }
    }
}

@Composable
fun SettingsDomainsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onOpenDomain: (LifeDomain) -> Unit,
    onOpenArea: (String) -> Unit,
    onOpenFeeds: () -> Unit,
    onOpenSources: () -> Unit,
    onOpenInbox: () -> Unit,
    onDeleteArea: (String) -> Unit,
) {
    DaysPageScaffold(
        title = "Bereiche",
        onBack = onBack,
        modifier = Modifier.testTag("settings-domains"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-domains-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingsTileFrame(
                tone = SettingsTileTone.Neutral,
                hero = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Setup",
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.muted,
                    )
                    StatusPill(label = "${state.activeAreas.size}/${state.maxActiveAreas} aktiv")
                }
                Text(
                    text = "Bereiche klar halten",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppTheme.colors.ink,
                )
                Text(
                    text = "Hier pruefst du Auftrag, Stand und naechsten Schritt jedes Bereichs und loeschst ihn bei Bedarf.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onOpenSources,
                        modifier = Modifier.testTag("settings-domains-open-sources"),
                    ) {
                        Text("Android")
                    }
                    TextButton(
                        onClick = onOpenFeeds,
                        modifier = Modifier.testTag("settings-domains-open-feeds"),
                    ) {
                        Text(if (state.activeFeedSourceCount > 0) "Verbindungen ${state.activeFeedSourceCount}" else "Verbindungen")
                    }
                    TextButton(
                        onClick = onOpenInbox,
                        modifier = Modifier.testTag("settings-domains-open-inbox"),
                    ) {
                        Text(if (state.pendingImports.isNotEmpty()) "Eingang ${state.pendingImports.size}" else "Eingang")
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusPill(label = "${state.directAreaCount} direkt")
                    StatusPill(label = "${state.importAreaCount} mit Import")
                    StatusPill(label = "${state.manualAreaCount} manuell")
                    if (state.waitingAreaCount > 0) {
                        StatusPill(label = "${state.waitingAreaCount} warten")
                    }
                }
            }
            if (state.activeAreas.isEmpty()) {
                SettingsTileFrame(
                    tone = SettingsTileTone.Neutral,
                    testTag = "settings-areas-empty",
                ) {
                    Text(
                        text = "Noch kein aktiver Bereich",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.ink,
                    )
                    Text(
                        text = "Neue Bereiche legst du im Start an. Hier tauchen sie dann zum Verwalten und Loeschen auf.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
            } else {
                state.activeAreas.forEach { item ->
                    SettingsTileFrame(
                        tone = SettingsTileTone.Signal,
                        testTag = "settings-area-card-${item.id}",
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTheme.colors.ink,
                                )
                                Text(
                                    text = item.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.muted,
                                )
                                Text(
                                    text = item.statusDetail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            StatusPill(label = item.statusLabel)
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            StatusPill(label = item.sourceLabel)
                            StatusPill(label = item.materialLabel)
                            StatusPill(label = item.nextStepLabel)
                        }
                        Text(
                            text = item.sourceDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = item.materialDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = item.nextStepDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            TextButton(
                                onClick = { onOpenArea(item.id) },
                                modifier = Modifier.testTag("settings-area-open-${item.id}"),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bereich")
                            }
                            if (item.nextStepLabel == "Quelle verbinden") {
                                TextButton(
                                    onClick = onOpenSources,
                                    modifier = Modifier.testTag("settings-area-sources-${item.id}"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Tune,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Android")
                                }
                            }
                            TextButton(
                                onClick = { onDeleteArea(item.id) },
                                modifier = Modifier.testTag("settings-area-delete-${item.id}"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loeschen")
                            }
                        }
                    }
                }
            }
            DaysSectionHeading(
                title = "Bereichskatalog",
                detail = "Der Katalog zeigt die fachliche Struktur. Aktiv wird ein Bereich erst, wenn du ihn im Start wirklich anlegst.",
            )
            state.catalog.forEach { item ->
                DomainListCard(
                    item = item,
                    goalCount = state.goals.count { it.domain == item.domain },
                    manualCount = state.manualMetrics.count { it.domain == item.domain },
                    activeSourceCount = relevantSourceItems(item.domain, state.sources).count(::isSourceLive),
                    onClick = { onOpenDomain(item.domain) },
                )
            }
        }
    }
}

@Composable
fun SettingsFeedsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenArea: (String) -> Unit,
) {
    DaysPageScaffold(
        title = "Eingaenge",
        onBack = onBack,
        modifier = Modifier.testTag("settings-feeds"),
        action = DaysTopBarAction(
            icon = Icons.Outlined.Sync,
            contentDescription = "Eingaenge aktualisieren",
            onClick = onRefresh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-feeds-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingsTileFrame(
                tone = SettingsTileTone.Accent,
                hero = true,
            ) {
                Text(
                    text = "Digitale Eingaenge",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppTheme.colors.ink,
                )
                Text(
                    text = "Hier siehst du, welche Bereiche schon ueber Websites oder Feeds Material holen und wie ruhig oder aktiv sie nachladen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusPill(label = "${state.activeFeedAreaCount} Bereiche")
                    StatusPill(label = "${state.activeFeedSourceCount} Quellen")
                }
            }
            if (state.feedAreas.isEmpty()) {
                SettingsTileFrame(
                    tone = SettingsTileTone.Neutral,
                    testTag = "settings-feeds-empty",
                ) {
                    Text(
                        text = "Noch kein digitaler Eingang gespeichert",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.ink,
                    )
                    Text(
                        text = "Oeffne einen Bereich, merke dort eine Website oder Feed-URL und hole dann neue Eintraege direkt in seinen Eingang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
            } else {
                state.feedAreas.forEach { item ->
                    SettingsTileFrame(
                        tone = SettingsTileTone.Signal,
                        testTag = "settings-feed-area-${item.areaId}",
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = item.areaTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTheme.colors.ink,
                                )
                                Text(
                                    text = item.goalLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppTheme.colors.ink,
                                )
                                Text(
                                    text = item.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.muted,
                                )
                            }
                            StatusPill(label = "${item.sourceCount} Eingaenge")
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            StatusPill(label = "${item.activeAutoSyncCount} automatisch")
                            item.capabilityLabels.take(3).forEach { label ->
                                StatusPill(label = label)
                            }
                        }
                        item.sources.take(3).forEach { source ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    StatusPill(label = source.hostLabel)
                                    StatusPill(label = source.sourceKindLabel)
                                    StatusPill(label = source.goalLabel)
                                    if (source.autoSyncEnabled) {
                                        StatusPill(label = source.syncCadenceLabel)
                                    }
                                    if (source.lastStatusLabel.isNotBlank()) {
                                        StatusPill(label = source.lastStatusLabel)
                                    }
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    source.capabilityLabels.take(4).forEach { label ->
                                        StatusPill(label = label)
                                    }
                                }
                                Text(
                                    text = "Kann: ${source.capabilityLabels.take(3).joinToString()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.muted,
                                )
                                Text(
                                    text = shortenWebFeedUrl(source.url),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (source.lastStatusLabel.isNotBlank()) {
                                    Text(
                                        text = source.lastStatusDetail.ifBlank { source.lastStatusLabel },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        TextButton(
                            onClick = { onOpenArea(item.areaId) },
                            modifier = Modifier.testTag("settings-feed-open-${item.areaId}"),
                        ) {
                            Text("Bereich ansehen")
                        }
                    }
                }
            }
        }
    }
}

private fun shortenWebFeedUrl(
    url: String,
): String {
    val normalized = url.removePrefix("https://").removePrefix("http://")
    val host = normalized.substringBefore('/')
    val path = normalized.substringAfter('/', "").ifBlank { "/" }
    return if (path.length <= 28) "$host$path" else "$host/${path.takeLast(28)}"
}

@Composable
fun SettingsDomainDetailScreen(
    state: SettingsUiState,
    domain: LifeDomain,
    onBack: () -> Unit,
    onOpenSource: (DataSourceKind) -> Unit,
    onSaveGoal: (String, String, String, String, String) -> Unit,
    onSaveManualMetric: (String?, LifeDomain, ObservationMetric, String, String) -> Unit,
) {
    val catalog = state.catalog.firstOrNull { it.domain == domain }
    val goals = state.goals.filter { it.domain == domain }
    val manualMetrics = state.manualMetrics.filter { it.domain == domain }
    val relevantSources = relevantSourceItems(domain, state.sources)
    val primaryGoal = goals.firstOrNull()
    val primaryManualMetric = manualMetrics.firstOrNull()

    DaysPageScaffold(
        title = catalog?.title ?: domain.name,
        onBack = onBack,
        modifier = Modifier.testTag("settings-domain-detail"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-domain-detail-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DomainHeroCard(
                domain = domain,
                title = catalog?.title ?: domain.name,
                summary = catalog?.summary ?: "Dieser Bereich wird spaeter weiter ausgebaut.",
                statusLabel = catalog?.statusLabel ?: "Offen",
                statusDetail = catalog?.statusDetail ?: "Die Struktur ist da, aber die Tageslage ist noch offen.",
                liveSourceCount = relevantSources.count(::isSourceLive),
            )
            SettingsTileFrame(
                tone = SettingsTileTone.Accent,
                testTag = "settings-domain-stand",
            ) {
                DaysSectionHeading(
                    title = "Stand",
                    detail = "So steht dieser Bereich heute da.",
                )
                OverviewStatRow(
                    label = "Zielrahmen",
                    value = goalSummaryValue(primaryGoal),
                    detail = goalSummaryDetail(primaryGoal),
                )
                WorkbenchDivider()
                OverviewStatRow(
                    label = "Letzter Wert",
                    value = manualSummaryValue(primaryManualMetric),
                    detail = manualSummaryDetail(primaryManualMetric),
                )
                catalog?.statusDetail?.takeIf(String::isNotBlank)?.let { detail ->
                    WorkbenchDivider()
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
            }
            SettingsTileFrame(
                tone = SettingsTileTone.Signal,
                testTag = "settings-domain-inputs",
            ) {
                DaysSectionHeading(
                    title = "Eingang",
                    detail = "Diese Spuren duerfen in den Bereich hineinwirken.",
                )
                Text(
                    text = relevantSourcesFor(domain),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.muted,
                )
                if (relevantSources.isEmpty()) {
                    StatusPill(label = "Noch frei")
                } else {
                    SignalPillRow(
                        sources = relevantSources,
                        onOpenSource = onOpenSource,
                    )
                }
            }
            if (goals.isNotEmpty()) {
                EditorSectionCard(
                    title = "Fokus",
                    detail = "Hier schaerfst du, worauf der Bereich eigentlich zielt.",
                ) {
                    goals.forEachIndexed { index, item ->
                        if (index > 0) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            )
                        }
                        GoalEditorBlock(
                            item = item,
                            showTitle = goals.size > 1,
                            onSave = onSaveGoal,
                        )
                    }
                }
            }
            if (manualMetrics.isNotEmpty()) {
                EditorSectionCard(
                    title = "Takt",
                    detail = "Hier kommt der schnelle Rueckkanal fuer diesen Bereich hinein.",
                ) {
                    manualMetrics.forEachIndexed { index, item ->
                        if (index > 0) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            )
                        }
                        ManualMetricEditorBlock(
                            item = item,
                            showTitle = manualMetrics.size > 1,
                            onSave = onSaveManualMetric,
                        )
                    }
                }
            }
            if (goals.isEmpty() && manualMetrics.isEmpty()) {
                SettingsTileFrame(
                    tone = SettingsTileTone.Neutral,
                ) {
                    DaysSectionHeading(
                        title = "Noch ohne tieferen Ausbau",
                        detail = "Sobald fuer diesen Bereich Fokus oder Takt gepflegt werden, tauchen sie gesammelt hier auf.",
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsInboxScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onOpenStart: () -> Unit,
    onDismissImport: (String) -> Unit,
) {
    DaysPageScaffold(
        title = "Eingang",
        onBack = onBack,
        modifier = Modifier.testTag("settings-inbox"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-inbox-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingsTileFrame(
                tone = if (state.pendingImports.isNotEmpty()) SettingsTileTone.Accent else SettingsTileTone.Neutral,
                hero = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Geteilte Inhalte",
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.muted,
                    )
                    StatusPill(label = "${state.pendingImports.size} offen")
                }
                Text(
                    text = if (state.pendingImports.isNotEmpty()) "Was noch zugeordnet werden will" else "Gerade ist alles einsortiert",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppTheme.colors.ink,
                )
                Text(
                    text = if (state.pendingImports.isNotEmpty()) {
                        "Teile Inhalte an Days und ordne sie dann im Start einem neuen oder bestehenden Bereich zu."
                    } else {
                        "Links, Dateien, Bilder und Texte aus anderen Apps tauchen hier auf, solange sie noch keinem Bereich zugeordnet sind."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
                TextButton(
                    onClick = onOpenStart,
                    modifier = Modifier.testTag("settings-inbox-open-start"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Im Start bearbeiten")
                }
            }
            if (state.pendingImports.isEmpty()) {
                SettingsTileFrame(
                    tone = SettingsTileTone.Neutral,
                    testTag = "settings-inbox-empty",
                ) {
                    Text(
                        text = "Kein offener Eingang",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.ink,
                    )
                    Text(
                        text = "Sobald du Days Inhalte teilst, kannst du sie hier pruefen oder im Start zu einem Bereich machen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
            } else {
                state.pendingImports.forEach { item ->
                    SettingsTileFrame(
                        tone = SettingsTileTone.Warm,
                        testTag = "settings-inbox-item-${item.id}",
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTheme.colors.ink,
                                )
                                Text(
                                    text = item.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.muted,
                                )
                            }
                            StatusPill(label = item.kindLabel)
                        }
                        TextButton(
                            onClick = { onDismissImport(item.id) },
                            modifier = Modifier.testTag("settings-inbox-dismiss-${item.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verwerfen")
                        }
                    }
                }
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
    onOpenSource: (DataSourceKind) -> Unit,
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
        title = "Android-Zugriffe",
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SourceConstellationCard(
                sources = state.sources,
            )
            SettingsTileFrame(
                tone = SettingsTileTone.Signal,
                hero = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Android",
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.muted,
                    )
                    StatusPill(label = "${state.sources.count(::isSourceLive)} live")
                }
                Text(
                    text = "Nur echte Zugriffe zaehlen",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppTheme.colors.ink,
                )
                Text(
                    text = "Verfuegbar. Freigegeben. Live.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
            }
            state.sources.forEach { item ->
                SourceCard(
                    item = item,
                    onOpen = { onOpenSource(item.source) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSourceDetailScreen(
    state: SettingsUiState,
    source: DataSourceKind,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSetSourceEnabled: (DataSourceKind, Boolean) -> Unit,
    onOpenDomain: (LifeDomain) -> Unit,
) {
    val context = LocalContext.current
    val item = state.sources.firstOrNull { it.source == source } ?: return
    val affectedDomains = state.catalog.filter { sourceSupportsDomain(source, it.domain) }
    val liveDomains = affectedDomains.count { catalogItem ->
        sourceSupportsDomain(source, catalogItem.domain) &&
            relevantSourceItems(catalogItem.domain, state.sources)
                .any { it.source == source && isSourceLive(it) }
    }
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

    fun runSourceAction() {
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
    }

    LifecycleResumeEffect(Unit) {
        onRefresh()
        onPauseOrDispose { }
    }

    DaysPageScaffold(
        title = item.label,
        onBack = onBack,
        modifier = Modifier.testTag("settings-source-detail"),
        action = DaysTopBarAction(
            icon = Icons.Outlined.Sync,
            contentDescription = "Aktualisieren",
            onClick = onRefresh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-source-detail-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SourceHeroCard(
                item = item,
                affectedDomainCount = affectedDomains.size,
                liveDomainCount = liveDomains,
            )
            SourceActionDeck(
                item = item,
                onToggle = { onSetSourceEnabled(item.source, !item.enabled) },
                onAction = ::runSourceAction,
            )
            SourceFlowCard(
                item = item,
            )
            SourceCoverageCard(
                source = source,
                items = affectedDomains,
                allSources = state.sources,
                onOpenDomain = onOpenDomain,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPrimaryMenuCard(
    activeAreaCount: Int,
    maxActiveAreas: Int,
    directAreaCount: Int,
    waitingAreaCount: Int,
    importAreaCount: Int,
    manualAreaCount: Int,
    activeSourceCount: Int,
    activeFeedAreaCount: Int,
    activeFeedSourceCount: Int,
    pendingImportCount: Int,
    onClick: () -> Unit,
) {
    val container = Color(0xFFF5864F)
    val border = Color(0xFFFFB28E)
    val shell = Color(0xFFFFC8A8)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-menu-domains"),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, border),
        shape = RoundedCornerShape(36.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 22.dp)
                    .size(182.dp)
                    .clip(CircleShape)
                    .background(shell.copy(alpha = 0.26f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 24.dp)
                    .size(144.dp)
                    .clip(RoundedCornerShape(42.dp))
                    .background(shell.copy(alpha = 0.18f)),
            )
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 18.dp)
                    .size(132.dp),
                tint = Color.White.copy(alpha = 0.18f),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 420.dp)
                    .padding(26.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Einstellungen",
                            style = AppTheme.typography.label,
                            color = AppTheme.colors.ink.copy(alpha = 0.72f),
                        )
                        SettingsPrimaryPill(
                            label = "MUSS",
                            background = shell.copy(alpha = 0.58f),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Bereiche",
                            style = MaterialTheme.typography.headlineLarge,
                            color = AppTheme.colors.ink,
                        )
                        Text(
                            text = "Aktive Bereiche oeffnen, ordnen und sauber halten.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.ink.copy(alpha = 0.76f),
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SettingsPrimaryPill(
                            label = "$activeAreaCount/$maxActiveAreas aktiv",
                            background = Color.White.copy(alpha = 0.24f),
                        )
                        SettingsPrimaryPill(
                            label = "$directAreaCount direkt",
                            background = Color.White.copy(alpha = 0.2f),
                        )
                        SettingsPrimaryPill(
                            label = "$importAreaCount import",
                            background = Color.White.copy(alpha = 0.16f),
                        )
                        SettingsPrimaryPill(
                            label = "$manualAreaCount manuell",
                            background = Color.White.copy(alpha = 0.14f),
                        )
                        if (waitingAreaCount > 0) {
                            SettingsPrimaryPill(
                                label = "$waitingAreaCount warten",
                                background = Color.White.copy(alpha = 0.12f),
                            )
                        }
                        if (pendingImportCount > 0) {
                            SettingsPrimaryPill(
                                label = "$pendingImportCount Eingang",
                                background = Color.White.copy(alpha = 0.1f),
                            )
                        }
                        SettingsPrimaryPill(
                            label = "$activeSourceCount live",
                            background = Color.White.copy(alpha = 0.12f),
                        )
                        if (activeFeedSourceCount > 0) {
                            SettingsPrimaryPill(
                                label = "$activeFeedSourceCount feed",
                                background = Color.White.copy(alpha = 0.12f),
                            )
                        }
                        if (activeFeedAreaCount > 0) {
                            SettingsPrimaryPill(
                                label = "$activeFeedAreaCount web",
                                background = Color.White.copy(alpha = 0.12f),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Bereiche oeffnen",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppTheme.colors.ink,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = AppTheme.colors.ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPrimaryPill(
    label: String,
    background: Color,
) {
    Box(
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.mono,
            color = AppTheme.colors.ink,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsQuickMenuCard(
    testTag: String,
    tone: SettingsTileTone,
    eyebrow: String,
    title: String,
    detail: String,
    pills: List<String>,
    onClick: () -> Unit,
) {
    SettingsTileFrame(
        tone = tone,
        testTag = testTag,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = eyebrow,
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.muted,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTheme.colors.ink,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = AppTheme.colors.ink,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pills.filter(String::isNotBlank).forEach { pill ->
                StatusPill(label = pill)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SourceConstellationCard(
    sources: List<SettingsSourceItem>,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Signal,
    ) {
        DaysSectionHeading(
            title = "Signalnetz",
            detail = "Aktive Pfade stehen visuell vorn.",
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            sources.forEach { item ->
                MiniSignalTile(
                    title = item.label,
                    detail = sourceStatus(item).label,
                    icon = sourceIcon(item.source),
                    tint = sourceTint(item.source),
                    active = isSourceLive(item),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DomainListCard(
    item: SettingsCatalogItem,
    goalCount: Int,
    manualCount: Int,
    activeSourceCount: Int,
    onClick: () -> Unit,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Neutral,
        testTag = "settings-domain-${item.domain.name.lowercase()}",
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconOrb(
                    icon = settingsDomainIcon(item.domain),
                    tint = settingsDomainTint(item.domain),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = AppTheme.colors.ink,
                        )
                        StatusPill(label = item.statusLabel)
                    }
                    Text(text = item.summary, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.muted)
                }
            }
            Text(
                text = item.statusDetail,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusPill(label = "$goalCount Ziele")
                StatusPill(label = "$manualCount Werte")
                StatusPill(label = "$activeSourceCount live")
            }
        }
    }
}

@Composable
private fun DomainHeroCard(
    domain: LifeDomain,
    title: String,
    summary: String,
    statusLabel: String,
    statusDetail: String,
    liveSourceCount: Int,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Neutral,
        hero = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Bereich",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.muted,
                )
                StatusPill(label = statusLabel)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconOrb(
                    icon = settingsDomainIcon(domain),
                    tint = settingsDomainTint(domain),
                    size = 64.dp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppTheme.colors.ink,
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colors.muted,
                    )
                }
            }
            Text(
                text = statusDetail,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusPill(label = "$liveSourceCount Quellen live")
                StatusPill(label = domainCadenceLabel(domain))
            }
        }
    }
}

@Composable
private fun DomainOverviewCard(
    goal: SettingsGoalItem?,
    manualMetric: SettingsManualMetricItem?,
    sources: List<SettingsSourceItem>,
    sourceDetail: String,
    onOpenSource: (DataSourceKind) -> Unit,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Accent,
        testTag = "settings-domain-workbench",
    ) {
        DaysSectionHeading(
            title = "Heute im Blick",
            detail = "Stand zuerst, dann klare naechste Schritte.",
        )
        OverviewStatRow(
            label = "Ziel heute",
            value = goalSummaryValue(goal),
            detail = goalSummaryDetail(goal),
        )
        WorkbenchDivider()
        OverviewStatRow(
            label = "Letzter Wert",
            value = manualSummaryValue(manualMetric),
            detail = manualSummaryDetail(manualMetric),
        )
        WorkbenchDivider()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Signalbild",
                style = AppTheme.typography.label,
                color = AppTheme.colors.muted,
            )
            Text(
                text = sourceDetail,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.muted,
            )
            SignalPillRow(
                sources = sources,
                onOpenSource = onOpenSource,
            )
        }
    }
}

@Composable
private fun OverviewStatRow(
    label: String,
    value: String,
    detail: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.label,
            color = AppTheme.colors.muted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.ink,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.muted,
        )
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    detail: String,
    content: @Composable () -> Unit,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Neutral,
    ) {
        DaysSectionHeading(
            title = title,
            detail = detail,
        )
        content()
    }
}

@Composable
private fun SourceHeroCard(
    item: SettingsSourceItem,
    affectedDomainCount: Int,
    liveDomainCount: Int,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Signal,
        hero = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Android-Eingang",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.muted,
                )
                StatusPill(label = sourceStatus(item).label)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconOrb(
                    icon = sourceIcon(item.source),
                    tint = sourceTint(item.source),
                    size = 64.dp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppTheme.colors.ink,
                    )
                    Text(
                        text = item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
            }
            SourceStageStrip(item = item)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusPill(label = "$affectedDomainCount Bereiche")
                StatusPill(label = "$liveDomainCount live")
                StatusPill(label = if (item.enabled) "Pfad an" else "Pfad aus")
            }
        }
    }
}

@Composable
private fun SourceActionDeck(
    item: SettingsSourceItem,
    onToggle: () -> Unit,
    onAction: () -> Unit,
) {
    val actionLabel = sourceActionLabel(item)
    val toggleTitle = if (item.enabled) "Quelle pausieren" else "Quelle aktivieren"
    val toggleMeta = if (item.enabled) "Der Pfad speist gerade in den Tag ein." else "Der Pfad bleibt sichtbar, liefert aber nichts."

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            ActionTile(
                title = toggleTitle,
                detail = toggleMeta,
            pill = if (item.enabled) "An" else "Aus",
            icon = if (item.enabled) Icons.Outlined.Sync else sourceIcon(item.source),
            tint = sourceTint(item.source),
            onClick = onToggle,
            testTag = "settings-source-detail-toggle",
        )
        actionLabel?.let {
            ActionTile(
                title = "Freigabe oeffnen",
                detail = sourceStatus(item).detail,
                pill = "System",
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                tint = AppTheme.colors.accent,
                onClick = onAction,
                testTag = "settings-source-detail-action",
            )
        }
    }
}

@Composable
private fun SourceFlowCard(
    item: SettingsSourceItem,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Warm,
    ) {
        DaysSectionHeading(
            title = "Verbindungsstand",
            detail = "Von verfuegbar bis live.",
        )
        SourceStageStrip(item = item)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SourceCoverageCard(
    source: DataSourceKind,
    items: List<SettingsCatalogItem>,
    allSources: List<SettingsSourceItem>,
    onOpenDomain: (LifeDomain) -> Unit,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Neutral,
    ) {
        DaysSectionHeading(
            title = "Wirkt auf diese Bereiche",
            detail = "Nur Bereiche mit echter Wirkung bleiben hier sichtbar.",
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items.forEach { item ->
                val sourceIsLive = relevantSourceItems(item.domain, allSources)
                    .any { it.source == source && isSourceLive(it) }
                MiniSignalTile(
                    title = item.title,
                    detail = if (sourceIsLive) "live" else item.statusLabel,
                    icon = settingsDomainIcon(item.domain),
                    tint = settingsDomainTint(item.domain),
                    active = sourceIsLive,
                    onClick = { onOpenDomain(item.domain) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionTile(
    title: String,
    detail: String,
    pill: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
) {
    SettingsTileFrame(
        tone = SettingsTileTone.Warm,
        testTag = testTag,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconOrb(
                icon = icon,
                tint = tint,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.ink,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.muted,
                )
            }
            StatusPill(label = pill)
        }
    }
}

@Composable
private fun SourceStageStrip(
    item: SettingsSourceItem,
) {
    val stages = listOf(
        SourceStage(
            label = "System",
            active = item.available || item.source == DataSourceKind.MANUAL,
        ),
        SourceStage(
            label = "Zugriff",
            active = item.granted || item.source == DataSourceKind.MANUAL,
        ),
        SourceStage(
            label = "Live",
            active = isSourceLive(item),
        ),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        stages.forEach { stage ->
            StageMiniTile(
                modifier = Modifier.weight(1f),
                label = stage.label,
                active = stage.active,
                tint = sourceTint(item.source),
            )
        }
    }
}

private data class SourceStage(
    val label: String,
    val active: Boolean,
)

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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (showTitle) item.title else "Zielbild",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = goalSummaryDetail(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusPill(label = item.priorityLabel)
                StatusPill(label = item.unit)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumericField(
                label = "Minimum",
                value = minimumText,
                onValueChange = { minimumText = it },
                testTag = "settings-goal-${item.domain.name.lowercase()}-min",
                modifier = Modifier.weight(1f),
            )
            if (item.maximumText.isNotBlank()) {
                NumericField(
                    label = "Maximum",
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
                    label = "Von",
                    value = preferredStart,
                    onValueChange = { preferredStart = it },
                    testTag = "settings-goal-${item.domain.name.lowercase()}-start",
                    modifier = Modifier.weight(1f),
                )
                NumericField(
                    label = "Bis",
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
                Text(text = "Ziel sichern", modifier = Modifier.padding(start = 8.dp))
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (showTitle) item.label else "Tageswert",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = manualSummaryDetail(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPill(label = item.unit)
        }
        NumericField(
            label = "Wert heute",
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
                Text(text = "Wert sichern", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceCard(
    item: SettingsSourceItem,
    onOpen: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onAction: () -> Unit,
) {
    val status = sourceStatus(item)
    val actionLabel = sourceActionLabel(item)
    SettingsTileFrame(
        tone = if (isSourceLive(item)) SettingsTileTone.Signal else SettingsTileTone.Neutral,
        testTag = "settings-source-${item.source.name.lowercase()}",
        onClick = onOpen,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconOrb(
                        icon = sourceIcon(item.source),
                        tint = sourceTint(item.source),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusDot(active = item.granted || item.source == DataSourceKind.MANUAL)
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleLarge,
                                color = AppTheme.colors.ink,
                            )
                        }
                        Text(
                            text = item.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = item.enabled,
                    onCheckedChange = onToggle,
                )
            }
            Text(
                text = status.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SourceStageStrip(item = item)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(label = status.label)
                if (actionLabel != null) {
                    TextButton(onClick = onAction) {
                        Text(text = actionLabel)
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
private fun IconOrb(
    icon: ImageVector,
    tint: Color,
    size: androidx.compose.ui.unit.Dp = 56.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 2.6f))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.44f),
        )
    }
}

@Composable
private fun StatusDot(
    active: Boolean,
    tint: Color = AppTheme.colors.success,
) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(
                color = if (active) tint else AppTheme.colors.outlineSoft.copy(alpha = 0.9f),
                shape = RoundedCornerShape(999.dp),
            ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SignalPillRow(
    sources: List<SettingsSourceItem>,
    onOpenSource: ((DataSourceKind) -> Unit)? = null,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sources.forEach { item ->
            SignalPill(
                item = item,
                onClick = onOpenSource?.let { { it(item.source) } },
            )
        }
    }
}

@Composable
private fun SignalPill(
    item: SettingsSourceItem,
    onClick: (() -> Unit)? = null,
) {
    val active = isSourceLive(item)
    Surface(
        modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
        color = if (active) sourceTint(item.source).copy(alpha = 0.14f) else AppTheme.colors.surfaceStrong.copy(alpha = 0.9f),
        border = BorderStroke(
            width = 1.dp,
            color = if (active) sourceTint(item.source).copy(alpha = 0.34f) else AppTheme.colors.outlineSoft.copy(alpha = 0.8f),
        ),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = item.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (active) AppTheme.colors.ink else AppTheme.colors.muted,
        )
    }
}

@Composable
private fun StatusPill(
    label: String,
    containerColor: Color = AppTheme.colors.surfaceMuted.copy(alpha = 0.78f),
    contentColor: Color = AppTheme.colors.ink,
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = AppTheme.typography.label,
            color = contentColor,
        )
    }
}

@Composable
private fun MiniSignalTile(
    title: String,
    detail: String,
    icon: ImageVector,
    tint: Color,
    active: Boolean,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
        color = if (active) tint.copy(alpha = 0.14f) else AppTheme.colors.surfaceStrong.copy(alpha = 0.92f),
        border = BorderStroke(
            width = 1.dp,
            color = if (active) tint.copy(alpha = 0.34f) else AppTheme.colors.outlineSoft.copy(alpha = 0.78f),
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconOrb(
                icon = icon,
                tint = tint,
                size = 34.dp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.colors.ink,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.muted,
                )
            }
        }
    }
}

@Composable
private fun StageMiniTile(
    modifier: Modifier = Modifier,
    label: String,
    active: Boolean,
    tint: Color,
) {
    Surface(
        modifier = modifier,
        color = if (active) tint.copy(alpha = 0.14f) else AppTheme.colors.surfaceStrong.copy(alpha = 0.78f),
        border = BorderStroke(
            width = 1.dp,
            color = if (active) tint.copy(alpha = 0.34f) else AppTheme.colors.outlineSoft.copy(alpha = 0.72f),
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusDot(
                active = active,
                tint = tint,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.ink,
            )
            Text(
                text = if (active) "bereit" else "offen",
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.muted,
            )
        }
    }
}

@Composable
private fun WorkbenchDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
    )
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
private fun settingsDomainTint(domain: LifeDomain): Color {
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

private fun sourceIcon(source: DataSourceKind): ImageVector {
    return when (source) {
        DataSourceKind.HEALTH_CONNECT -> Icons.Outlined.FavoriteBorder
        DataSourceKind.CALENDAR -> Icons.Outlined.Tune
        DataSourceKind.NOTIFICATIONS -> Icons.Outlined.NotificationsActive
        DataSourceKind.MANUAL -> Icons.Outlined.EditNote
    }
}

@Composable
private fun sourceTint(source: DataSourceKind): Color {
    return when (source) {
        DataSourceKind.HEALTH_CONNECT -> AppTheme.colors.success
        DataSourceKind.CALENDAR -> AppTheme.colors.info
        DataSourceKind.NOTIFICATIONS -> AppTheme.colors.warning
        DataSourceKind.MANUAL -> AppTheme.colors.accent
    }
}

private fun relevantSourcesFor(domain: LifeDomain): String {
    return when (domain) {
        LifeDomain.SLEEP -> "Schlaf lebt vor allem von Health Connect. Manuelle Korrekturen bleiben moeglich, wenn Daten fehlen oder abweichen."
        LifeDomain.MOVEMENT -> "Bewegung zieht primaer aus Health Connect. Manuelle Eintraege bleiben der Backup-Weg fuer fehlende Aktivitaetsdaten."
        LifeDomain.HYDRATION -> "Hydration ist im MVP bewusst direkt. Der Bereich lebt von einer schnellen manuellen Rueckmeldung statt von Sensorik."
        LifeDomain.NUTRITION -> "Ernaehrung bleibt vorerst manuell. Wichtig ist hier Klarheit im Eintrag, nicht ein halber Importfluss."
        LifeDomain.FOCUS -> "Fokus liest vor allem Kontext: Kalender, Unterbrechungen und manuelle Korrekturen geben gemeinsam ein brauchbares Bild."
        LifeDomain.STRESS -> "Stress nutzt zuerst Unterbrechungen und manuelle Gegenpruefung. Die Quelle muss eher Druck zeigen als Perfektion vortaeuschen."
        else -> "Dieser Bereich ist strukturell vorbereitet, aber noch nicht voll an passive Quellen angebunden."
    }
}

private fun domainCadenceLabel(domain: LifeDomain): String {
    return when (domain) {
        LifeDomain.SLEEP,
        LifeDomain.MOVEMENT,
        LifeDomain.HYDRATION,
        LifeDomain.NUTRITION,
        LifeDomain.FOCUS,
        LifeDomain.STRESS,
        -> "Taegliche Pflege"
        else -> "Spaeterer Ausbau"
    }
}

private fun goalSummaryValue(goal: SettingsGoalItem?): String {
    if (goal == null) return "Noch offen"
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
    if (goal == null) return "Noch kein Zielbild hinterlegt."
    val start = goal.preferredStartHour
    val end = goal.preferredEndHourExclusive
    val windowLabel = if (start != null && end != null) {
        "${displayHourCompact(start)}-${displayHourCompact(end)} Uhr"
    } else {
        "ohne Zeitfenster"
    }
    return "${goal.priorityLabel} / $windowLabel"
}

private fun manualSummaryValue(item: SettingsManualMetricItem?): String {
    if (item == null) return "Noch leer"
    return item.valueText.takeIf { it.isNotBlank() }?.let { "$it ${item.unit}" } ?: "Noch leer"
}

private fun manualSummaryDetail(item: SettingsManualMetricItem?): String {
    return if (item?.valueText?.isNotBlank() == true) {
        "Heute bereits erfasst."
    } else {
        "Heute liegt noch kein manueller Wert vor."
    }
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

private fun sourceSupportsDomain(
    source: DataSourceKind,
    domain: LifeDomain,
): Boolean {
    return relevantSourceItems(
        domain = domain,
        sources = listOf(
            SettingsSourceItem(
                source = source,
                label = "",
                detail = "",
                enabled = true,
                available = true,
                granted = true,
            ),
        ),
    ).isNotEmpty()
}

private fun isSourceLive(item: SettingsSourceItem): Boolean {
    return item.enabled && item.available && (item.granted || item.source == DataSourceKind.MANUAL)
}

private data class SourceStatusCopy(
    val label: String,
    val detail: String,
)

private fun sourceStatus(item: SettingsSourceItem): SourceStatusCopy {
    if (item.source == DataSourceKind.MANUAL) {
        return if (item.enabled) {
            SourceStatusCopy(
                label = "Direkt aktiv",
                detail = "Manuelle Eintraege koennen sofort genutzt werden.",
            )
        } else {
            SourceStatusCopy(
                label = "Aus",
                detail = "Direkte Eintraege sind vorbereitet, speisen aber aktuell nichts ein.",
            )
        }
    }
    return when {
        !item.available -> SourceStatusCopy(
            label = "Nicht verfuegbar",
            detail = "Das System ist auf diesem Geraet gerade nicht nutzbar.",
        )
        !item.enabled -> SourceStatusCopy(
            label = "Aus",
            detail = "Die Quelle ist vorhanden, aber bewusst vom Tagesbild getrennt.",
        )
        item.granted -> SourceStatusCopy(
            label = "Verbunden",
            detail = "Die Quelle liefert aktiv Signale in den Tag.",
        )
        else -> SourceStatusCopy(
            label = "Freigabe fehlt",
            detail = "Die Quelle ist da, aber die notwendige Berechtigung fehlt noch.",
        )
    }
}

private fun sourceActionLabel(item: SettingsSourceItem): String? {
    return when (item.source) {
        DataSourceKind.MANUAL -> null
        DataSourceKind.HEALTH_CONNECT -> if (item.available) {
            if (item.granted) "Freigabe pruefen" else "Freigabe oeffnen"
        } else {
            null
        }
        DataSourceKind.CALENDAR -> if (item.granted) "Berechtigung pruefen" else "Berechtigung oeffnen"
        DataSourceKind.NOTIFICATIONS -> if (item.granted) "Systemeinstellung" else "Zugriff oeffnen"
    }
}
