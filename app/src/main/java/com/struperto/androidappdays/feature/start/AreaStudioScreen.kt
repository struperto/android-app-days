package com.struperto.androidappdays.feature.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.domain.area.AreaAuthoringAxis
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysPageIntroCard
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.ui.theme.AppTheme

private enum class AreaStudioSurface {
    Overview,
    Identity,
    Authoring,
}

@Composable
fun AreaStudioScreen(
    state: AreaStudioUiState,
    areaId: String,
    onBack: () -> Unit,
    onTargetScoreChange: (String, Float) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
    onManualStateChange: (String, String?) -> Unit,
    onManualNoteChange: (String, String?) -> Unit,
    onClearSnapshot: (String) -> Unit,
    onUpdateIdentity: (String, String, String, String, String) -> Unit,
    onCadenceChange: (String, String) -> Unit,
    onIntensityChange: (String, Float) -> Unit,
    onSignalBlendChange: (String, Float) -> Unit,
    onLageModeChange: (String, String) -> Unit,
    onDirectionModeChange: (String, String) -> Unit,
    onSourcesModeChange: (String, String) -> Unit,
    onFlowProfileChange: (String, String) -> Unit,
    onComplexityLevelChange: (String, String) -> Unit,
    onVisibilityLevelChange: (String, String) -> Unit,
    onToggleTrack: (String, String) -> Unit,
    onPromoteTrack: (String, String) -> Unit,
    onRemindersChange: (String, Boolean) -> Unit,
    onReviewChange: (String, Boolean) -> Unit,
    onExperimentsChange: (String, Boolean) -> Unit,
) {
    val areaState = state.areas[areaId] ?: return
    val area = areaState.detail
    val authoring = areaState.authoring
    var activeSurface by rememberSaveable(area.areaId) { mutableStateOf(AreaStudioSurface.Overview) }
    var activePanel by rememberSaveable(area.areaId) { mutableStateOf<StartAreaPanel?>(null) }

    val panel = activePanel
    if (panel != null) {
        val panelState = area.panelStates.firstOrNull { it.panel == panel } ?: return
        AreaPanelScreen(
            area = area,
            panelState = panelState,
            onBack = { activePanel = null },
            onTargetScoreChange = onTargetScoreChange,
            onManualScoreChange = onManualScoreChange,
            onManualStateChange = onManualStateChange,
            onClearSnapshot = onClearSnapshot,
            onCadenceChange = onCadenceChange,
            onIntensityChange = onIntensityChange,
            onSignalBlendChange = onSignalBlendChange,
            onLageModeChange = onLageModeChange,
            onDirectionModeChange = onDirectionModeChange,
            onSourcesModeChange = onSourcesModeChange,
            onFlowProfileChange = onFlowProfileChange,
            onToggleTrack = onToggleTrack,
            onPromoteTrack = onPromoteTrack,
            onRemindersChange = onRemindersChange,
            onReviewChange = onReviewChange,
            onExperimentsChange = onExperimentsChange,
        )
        return
    }

    when (activeSurface) {
        AreaStudioSurface.Identity -> {
            AreaIdentityScreen(
                area = area,
                onBack = { activeSurface = AreaStudioSurface.Overview },
                onSave = { title, summary, templateId, iconKey ->
                    onUpdateIdentity(area.areaId, title, summary, templateId, iconKey)
                    activeSurface = AreaStudioSurface.Overview
                },
            )
            return
        }

        AreaStudioSurface.Authoring -> {
            AreaAuthoringScreen(
                area = area,
                authoring = authoring,
                onBack = { activeSurface = AreaStudioSurface.Overview },
                onLageModeChange = onLageModeChange,
                onDirectionModeChange = onDirectionModeChange,
                onSourcesModeChange = onSourcesModeChange,
                onFlowProfileChange = onFlowProfileChange,
                onComplexityLevelChange = onComplexityLevelChange,
                onVisibilityLevelChange = onVisibilityLevelChange,
            )
            return
        }

        AreaStudioSurface.Overview -> Unit
    }

    DaysPageScaffold(
        title = area.title,
        onBack = onBack,
        modifier = Modifier.testTag("area-studio-screen"),
        titleContent = {
            AreaSettingsTopBarTitle(areaTitle = area.title)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("area-studio-content"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AreaHomeHubCard(
                area = area,
                onEditIdentity = { activeSurface = AreaStudioSurface.Identity },
                onOpenPanel = { activePanel = it },
            )
            AreaAuthoringEntryCard(
                authoring = authoring,
                onOpenAuthoring = { activeSurface = AreaStudioSurface.Authoring },
            )
            AreaTodayOutputCard(
                area = area,
                onManualNoteChange = onManualNoteChange,
            )
        }
    }
}

@Composable
private fun AreaTodayOutputCard(
    area: StartAreaDetailState,
    onManualNoteChange: (String, String?) -> Unit,
) {
    var manualNoteDraft by rememberSaveable(area.areaId, area.manualNote) {
        mutableStateOf(area.manualNote.orEmpty())
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.97f),
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = area.todayLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = area.todayRecommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = area.todayStepLabel,
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colors.accent,
            )
            Text(
                text = area.todayOutput.evidenceSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = manualNoteDraft,
                onValueChange = { next ->
                    manualNoteDraft = next
                    onManualNoteChange(area.areaId, next)
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = manualNoteLabel(area.todayOutput.behaviorClass))
                },
                placeholder = {
                    Text(text = manualNotePlaceholder(area.todayOutput.behaviorClass))
                },
                minLines = 2,
                maxLines = 4,
                singleLine = false,
            )
        }
    }
}

private fun manualNoteLabel(
    behaviorClass: com.struperto.androidappdays.domain.area.AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.TRACKING -> "Beobachtungsnotiz"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.PROGRESS -> "Standnotiz"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.RELATIONSHIP -> "Kontakt- oder Tonnotiz"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.MAINTENANCE -> "Pflegenotiz"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.PROTECTION -> "Warn- oder Schutznotiz"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.REFLECTION -> "Reflexionsnotiz"
    }
}

private fun manualNotePlaceholder(
    behaviorClass: com.struperto.androidappdays.domain.area.AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.TRACKING -> "Kurz notieren, was heute auffaellt"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.PROGRESS -> "Kurz festhalten, was steht oder blockiert"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.RELATIONSHIP -> "Ton, letzter Impuls oder naechstes Follow-up"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.MAINTENANCE -> "Offener Pflegepunkt, Faelligkeit oder Erhaltungszug"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.PROTECTION -> "Trigger, Warnzeichen oder Rueckweg"
        com.struperto.androidappdays.domain.area.AreaBehaviorClass.REFLECTION -> "Ein kurzer Lesepunkt fuer heute"
    }
}

@Composable
private fun AreaSettingsTopBarTitle(
    areaTitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area-settings-title"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = areaTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Bereich",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AreaAuthoringEntryCard(
    authoring: AreaAuthoringStudioState,
    onOpenAuthoring: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.97f),
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Bereichscharakter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = authoring.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalIconButton(
                    onClick = onOpenAuthoring,
                    modifier = Modifier.testTag("area-edit-authoring"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = AppTheme.colors.accentSoft.copy(alpha = 0.24f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Bereichscharakter oeffnen",
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DaysMetaPill(label = authoring.basisLabel)
                authoring.previewAxes.firstOrNull()?.let { axis ->
                    DaysMetaPill(label = "${axis.label} ${axis.valueLabel}")
                }
            }
        }
    }
}

@Composable
private fun AreaHomeHubCard(
    area: StartAreaDetailState,
    onEditIdentity: () -> Unit,
    onOpenPanel: (StartAreaPanel) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(62.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = startAreaIcon(area.iconKey),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onEditIdentity,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .testTag("area-edit-identity"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = AppTheme.colors.surfaceStrong,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Bereich bearbeiten",
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = area.title,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "${area.statusLabel} · Soll ${area.targetScore}/5",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = area.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            AreaHomeMetricGrid(
                metrics = listOf(
                    StartPanelMetricState("Lage", area.statusLabel),
                    StartPanelMetricState("Richtung", area.profileState.directionLabel),
                    StartPanelMetricState("Quellen", area.profileState.sourcesLabel),
                    StartPanelMetricState("Flow", area.profileState.flowLabel),
                ),
            )
            LinearProgressIndicator(
                progress = { area.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp)),
            )
            HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.12f))
            AreaPanelEntryGrid(
                panelStates = area.panelStates,
                onOpenPanel = onOpenPanel,
            )
        }
    }
}

@Composable
private fun AreaAuthoringScreen(
    area: StartAreaDetailState,
    authoring: AreaAuthoringStudioState,
    onBack: () -> Unit,
    onLageModeChange: (String, String) -> Unit,
    onDirectionModeChange: (String, String) -> Unit,
    onSourcesModeChange: (String, String) -> Unit,
    onFlowProfileChange: (String, String) -> Unit,
    onComplexityLevelChange: (String, String) -> Unit,
    onVisibilityLevelChange: (String, String) -> Unit,
) {
    DaysPageScaffold(
        title = "Bereichscharakter",
        onBack = onBack,
        modifier = Modifier.testTag("area-authoring-screen"),
    ) {
        DaysPageIntroCard(
            title = area.title,
            summary = authoring.summary,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f)),
            shape = RoundedCornerShape(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Typbasis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${authoring.basisLabel} · ${authoring.definitionId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = authoring.basisSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        authoring.sections.forEach { section ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f)),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = section.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    section.axes.forEach { axis ->
                        AreaAuthoringAxisCard(
                            axis = axis,
                            onSelect = { optionId ->
                                when (axis.axis) {
                                    AreaAuthoringAxis.STATUS_SCHEMA -> onLageModeChange(area.areaId, optionId)
                                    AreaAuthoringAxis.DIRECTION -> onDirectionModeChange(area.areaId, optionId)
                                    AreaAuthoringAxis.SOURCES -> onSourcesModeChange(area.areaId, optionId)
                                    AreaAuthoringAxis.FLOW -> onFlowProfileChange(area.areaId, optionId)
                                    AreaAuthoringAxis.COMPLEXITY -> onComplexityLevelChange(area.areaId, optionId)
                                    AreaAuthoringAxis.VISIBILITY -> onVisibilityLevelChange(area.areaId, optionId)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaAuthoringAxisCard(
    axis: AreaAuthoringAxisState,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = axis.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = axis.valueLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = axis.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ChoiceChipRow(
            items = axis.options.map { option ->
                ChoiceChipItem(
                    id = option.id,
                    label = option.label,
                    testTag = "area-authoring-${axis.axis.name.lowercase()}-${option.id.toTestTagToken()}",
                )
            },
            selectedId = axis.options.firstOrNull { it.selected }?.id,
            onSelect = onSelect,
        )
        val selectedOption = axis.options.firstOrNull { it.selected }
        if (!selectedOption?.supportingLabel.isNullOrBlank()) {
            Text(
                text = selectedOption?.supportingLabel.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AreaHomeMetricGrid(
    metrics: List<StartPanelMetricState>,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val columns = if (maxWidth < 440.dp) 2 else 4
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            metrics.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowItems.forEach { metric ->
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.surface.copy(alpha = 0.72f),
                            ),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = metric.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = metric.value,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaPanelEntryGrid(
    panelStates: List<StartAreaPanelState>,
    onOpenPanel: (StartAreaPanel) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area-studio-work-tile"),
    ) {
        val columns = if (maxWidth < 420.dp) 1 else 2
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            panelStates.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { panelState ->
                        AreaPanelEntryCard(
                            panelState = panelState,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenPanel(panelState.panel) },
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaPanelEntryCard(
    panelState: StartAreaPanelState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = 108.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(AppTheme.colors.surface.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = AppTheme.colors.outline.copy(alpha = 0.16f),
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("area-entry-${panelState.panel.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = panelState.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = panelState.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = panelState.countLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = panelState.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (panelState.screenState.effectLabel.isNotBlank()) {
                    Text(
                        text = panelState.screenState.effectLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(16.dp),
            )
        }
        LinearProgressIndicator(
            progress = { panelState.screenState.core.progress.coerceIn(0.08f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp)),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
            trackColor = AppTheme.colors.outlineSoft.copy(alpha = 0.14f),
        )
    }
}

private fun StartAreaPanelState.iconTintStatus(): StartAreaStatusKind {
    return when (panel) {
        StartAreaPanel.Snapshot -> StartAreaStatusKind.Live
        StartAreaPanel.Path -> StartAreaStatusKind.Stable
        StartAreaPanel.Sources -> StartAreaStatusKind.Waiting
        StartAreaPanel.Options -> StartAreaStatusKind.Pull
    }
}

private data class StartPanelActionVisual(
    val icon: ImageVector,
    val accent: Color,
)

@Composable
private fun startPanelActionVisual(
    actionId: StartPanelActionId,
): StartPanelActionVisual {
    return when (actionId) {
        StartPanelActionId.SnapshotScore,
        StartPanelActionId.SnapshotState,
        StartPanelActionId.SnapshotMode,
        StartPanelActionId.SourcesBlend,
        StartPanelActionId.SourcesMode,
        StartPanelActionId.FlowSwitches,
        StartPanelActionId.FlowProfile -> StartPanelActionVisual(
            icon = Icons.Outlined.Tune,
            accent = AppTheme.colors.info,
        )

        StartPanelActionId.PathTarget -> StartPanelActionVisual(
            icon = Icons.Outlined.TrackChanges,
            accent = AppTheme.colors.success,
        )

        StartPanelActionId.PathCadence -> StartPanelActionVisual(
            icon = Icons.Outlined.Sync,
            accent = AppTheme.colors.info,
        )

        StartPanelActionId.PathFocus,
        StartPanelActionId.PathMode,
        StartPanelActionId.SourcesTracks -> StartPanelActionVisual(
            icon = Icons.Outlined.AutoAwesome,
            accent = AppTheme.colors.accent,
        )

        StartPanelActionId.FlowIntensity -> StartPanelActionVisual(
            icon = Icons.Outlined.AutoAwesome,
            accent = AppTheme.colors.warning,
        )

        StartPanelActionId.SnapshotClear -> StartPanelActionVisual(
            icon = Icons.Outlined.Refresh,
            accent = AppTheme.colors.danger,
        )
    }
}

@Composable
private fun StartPanelActionToolbar(
    directActions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        directActions.forEach { action ->
            val visual = startPanelActionVisual(action.id)
            val contentColor = if (action.id == StartPanelActionId.SnapshotClear) {
                AppTheme.colors.danger
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            FilledTonalIconButton(
                onClick = { onActionClick(action) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = AppTheme.colors.surfaceMuted.copy(alpha = 0.72f),
                    contentColor = contentColor,
                ),
                modifier = Modifier.testTag("panel-direct-${action.id.name.lowercase()}"),
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = "${action.label} ${action.valueLabel}",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AreaPanelScreen(
    area: StartAreaDetailState,
    panelState: StartAreaPanelState,
    onBack: () -> Unit,
    onTargetScoreChange: (String, Float) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
    onManualStateChange: (String, String?) -> Unit,
    onClearSnapshot: (String) -> Unit,
    onCadenceChange: (String, String) -> Unit,
    onIntensityChange: (String, Float) -> Unit,
    onSignalBlendChange: (String, Float) -> Unit,
    onLageModeChange: (String, String) -> Unit,
    onDirectionModeChange: (String, String) -> Unit,
    onSourcesModeChange: (String, String) -> Unit,
    onFlowProfileChange: (String, String) -> Unit,
    onToggleTrack: (String, String) -> Unit,
    onPromoteTrack: (String, String) -> Unit,
    onRemindersChange: (String, Boolean) -> Unit,
    onReviewChange: (String, Boolean) -> Unit,
    onExperimentsChange: (String, Boolean) -> Unit,
) {
    var activeSheetActionId by rememberSaveable(area.areaId, panelState.panel.name) {
        mutableStateOf<StartPanelActionId?>(null)
    }
    val activeAction = panelState.screenState.actions.firstOrNull { it.id == activeSheetActionId }

    if (activeAction?.sheet != null) {
        StartPanelOptionSheet(
            action = activeAction,
            onDismiss = { activeSheetActionId = null },
            onApplyOption = { optionId ->
                applyPanelOption(
                    area = area,
                    actionId = activeAction.id,
                    optionId = optionId,
                    onTargetScoreChange = onTargetScoreChange,
                    onManualScoreChange = onManualScoreChange,
                    onManualStateChange = onManualStateChange,
                    onCadenceChange = onCadenceChange,
                    onIntensityChange = onIntensityChange,
                    onSignalBlendChange = onSignalBlendChange,
                    onLageModeChange = onLageModeChange,
                    onDirectionModeChange = onDirectionModeChange,
                    onSourcesModeChange = onSourcesModeChange,
                    onFlowProfileChange = onFlowProfileChange,
                    onToggleTrack = onToggleTrack,
                    onPromoteTrack = onPromoteTrack,
                    onRemindersChange = onRemindersChange,
                    onReviewChange = onReviewChange,
                    onExperimentsChange = onExperimentsChange,
                )
                if (activeAction.sheet.selectionMode == StartPanelSelectionMode.Single) {
                    activeSheetActionId = null
                }
            },
        )
    }

    DaysPageScaffold(
        title = panelState.title,
        onBack = onBack,
        modifier = Modifier.testTag("area-panel-screen"),
    ) {
        StartPanelPage(
            area = area,
            panelState = panelState,
            onActionClick = { action ->
                when (action.mode) {
                    StartPanelActionMode.Sheet -> activeSheetActionId = action.id
                    StartPanelActionMode.Direct -> applyDirectPanelAction(
                        area = area,
                        actionId = action.id,
                        onClearSnapshot = onClearSnapshot,
                    )
                }
            },
        )
    }
}

@Composable
private fun StartPanelPage(
    area: StartAreaDetailState,
    panelState: StartAreaPanelState,
    onActionClick: (StartPanelActionState) -> Unit,
) {
    val screenState = panelState.screenState
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
            .testTag("start-panel-${panelState.panel.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StartPanelCoreCard(
            area = area,
            panelState = panelState,
            screenState = screenState,
        )
        StartPanelActionZone(
            actions = screenState.actions,
            onActionClick = onActionClick,
        )
        StartPanelMetricSection(metrics = screenState.metrics)
    }
}

@Composable
private fun StartPanelCoreCard(
    area: StartAreaDetailState,
    panelState: StartAreaPanelState,
    screenState: StartPanelScreenState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.95f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = panelState.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = area.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOf(screenState.headerLabel, screenState.infoLabel)
                            .filter(String::isNotBlank)
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = panelState.countLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = screenState.core.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = screenState.core.value,
                    style = MaterialTheme.typography.headlineMedium,
                )
                screenState.core.caption
                    .takeIf(String::isNotBlank)
                    ?.let { caption ->
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
            }
            if (screenState.effectLabel.isNotBlank()) {
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.12f))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Wirkung heute",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = screenState.effectLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { screenState.core.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            )
        }
    }
}

@Composable
private fun StartPanelActionZone(
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
) {
    val directActions = actions.filter { it.mode == StartPanelActionMode.Direct }
    val sheetActions = actions.filter { it.mode == StartPanelActionMode.Sheet }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Einstellungen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Passe die wirksamen Hebel dieses Bereichs an.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (directActions.isNotEmpty()) {
                    StartPanelActionToolbar(
                        directActions = directActions,
                        onActionClick = onActionClick,
                    )
                }
            }
            if (sheetActions.isNotEmpty()) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val columns = if (maxWidth < 360.dp) 1 else 2
                    val rows = sheetActions.chunked(columns)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowItems.forEach { action ->
                                    StartPanelActionCard(
                                        action = action,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onActionClick(action) },
                                    )
                                }
                                repeat(columns - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StartPanelActionCard(
    action: StartPanelActionState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val visual = startPanelActionVisual(action.id)
    Column(
        modifier = modifier
            .heightIn(min = 108.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(AppTheme.colors.surface.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = AppTheme.colors.outline.copy(alpha = 0.14f),
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .testTag("panel-action-${action.id.name.lowercase()}")
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                tint = if (action.id == StartPanelActionId.SnapshotClear) {
                    AppTheme.colors.danger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = action.valueLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (action.supportingLabel.isNotBlank()) {
                Text(
                    text = action.supportingLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StartPanelMetricSection(
    metrics: List<StartPanelMetricState>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Aktuell",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StartPanelMetricRow(metrics = metrics)
    }
}

@Composable
private fun StartPanelMetricRow(
    metrics: List<StartPanelMetricState>,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val columns = if (maxWidth < 360.dp) 1 else metrics.size.coerceAtMost(2)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            metrics.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowItems.forEach { metric ->
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.9f),
                            ),
                            shape = RoundedCornerShape(22.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = metric.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = metric.value,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartPanelOptionSheet(
    action: StartPanelActionState,
    onDismiss: () -> Unit,
    onApplyOption: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val actionSheet = action.sheet ?: return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.surfaceStrong,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val visual = startPanelActionVisual(action.id)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = actionSheet.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (action.supportingLabel.isNotBlank()) {
                        Text(
                            text = action.supportingLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            actionSheet.options.forEach { option ->
                StartPanelOptionRow(
                    actionId = action.id,
                    option = option,
                    multiple = actionSheet.selectionMode == StartPanelSelectionMode.Multiple,
                    onClick = { onApplyOption(option.id) },
                )
            }
            if (actionSheet.selectionMode == StartPanelSelectionMode.Multiple) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text("Fertig")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StartPanelOptionRow(
    actionId: StartPanelActionId,
    option: StartPanelOptionState,
    multiple: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (option.selected) {
                    AppTheme.colors.surfaceStrong.copy(alpha = 0.98f)
                } else {
                    AppTheme.colors.surface.copy(alpha = 0.68f)
                },
            )
            .border(
                width = 1.dp,
                color = if (option.selected) {
                    AppTheme.colors.outline.copy(alpha = 0.24f)
                } else {
                    AppTheme.colors.outline.copy(alpha = 0.12f)
                },
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .testTag("panel-option-${actionId.name.lowercase()}-${option.id.toTestTagToken()}")
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (option.selected) {
                        AppTheme.colors.surfaceMuted.copy(alpha = 0.98f)
                    } else {
                        AppTheme.colors.surfaceMuted.copy(alpha = 0.9f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (option.selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (option.supportingLabel.isNotBlank()) {
                Text(
                    text = option.supportingLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (multiple && option.selected) {
            Text(
                text = "aktiv",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun String.toTestTagToken(): String {
    return lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "value" }
}

@Composable
private fun AreaIdentityScreen(
    area: StartAreaDetailState,
    onBack: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
) {
    var title by rememberSaveable(area.areaId) { mutableStateOf(area.title) }
    var summary by rememberSaveable(area.areaId) { mutableStateOf(area.summary) }
    var templateId by rememberSaveable(area.areaId) { mutableStateOf(area.templateId) }
    var iconKey by rememberSaveable(area.areaId) { mutableStateOf(area.iconKey) }
    val template = startAreaTemplate(templateId)

    DaysPageScaffold(
        title = "Bereich bearbeiten",
        onBack = onBack,
        modifier = Modifier.testTag("area-identity-screen"),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.94f)),
                shape = RoundedCornerShape(30.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = startAreaIcon(iconKey),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = title.ifBlank { "Bereichstitel" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = summary.ifBlank { template.summary },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f)),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area-identity-title"),
                        label = { Text("Titel") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Bedeutung") },
                        minLines = 2,
                        maxLines = 3,
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f)),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Vorlage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChoiceChipRow(
                        items = startAreaTemplates.map { ChoiceChipItem(it.id, it.label) },
                        selectedId = templateId,
                        onSelect = { next ->
                            val currentDefault = startAreaTemplate(templateId).defaultIconKey
                            templateId = next
                            if (iconKey == currentDefault) {
                                iconKey = startAreaTemplate(next).defaultIconKey
                            }
                        },
                    )
                    Text(
                        text = template.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider()
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChoiceChipRow(
                        items = areaIdentityIconChoices,
                        selectedId = iconKey,
                        onSelect = { iconKey = it },
                    )
                }
            }
            Button(
                onClick = { onSave(title.trim(), summary.trim(), templateId, iconKey) },
                enabled = title.trim().length >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("area-identity-save"),
            ) {
                Text("Bereich sichern")
            }
        }
    }
}

private fun applyDirectPanelAction(
    area: StartAreaDetailState,
    actionId: StartPanelActionId,
    onClearSnapshot: (String) -> Unit,
) {
    when (actionId) {
        StartPanelActionId.SnapshotClear -> onClearSnapshot(area.areaId)
        else -> Unit
    }
}

private fun applyPanelOption(
    area: StartAreaDetailState,
    actionId: StartPanelActionId,
    optionId: String,
    onTargetScoreChange: (String, Float) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
    onManualStateChange: (String, String?) -> Unit,
    onCadenceChange: (String, String) -> Unit,
    onIntensityChange: (String, Float) -> Unit,
    onSignalBlendChange: (String, Float) -> Unit,
    onLageModeChange: (String, String) -> Unit,
    onDirectionModeChange: (String, String) -> Unit,
    onSourcesModeChange: (String, String) -> Unit,
    onFlowProfileChange: (String, String) -> Unit,
    onToggleTrack: (String, String) -> Unit,
    onPromoteTrack: (String, String) -> Unit,
    onRemindersChange: (String, Boolean) -> Unit,
    onReviewChange: (String, Boolean) -> Unit,
    onExperimentsChange: (String, Boolean) -> Unit,
) {
    when (actionId) {
        StartPanelActionId.SnapshotScore -> {
            onManualScoreChange(area.areaId, optionId.toIntOrNull())
        }

        StartPanelActionId.SnapshotState -> {
            onManualStateChange(area.areaId, optionId.takeUnless { it == "clear" })
        }

        StartPanelActionId.SnapshotMode -> onLageModeChange(area.areaId, optionId)
        StartPanelActionId.PathTarget -> onTargetScoreChange(area.areaId, optionId.toFloat())
        StartPanelActionId.PathCadence -> onCadenceChange(area.areaId, optionId)
        StartPanelActionId.PathFocus -> onPromoteTrack(area.areaId, optionId)
        StartPanelActionId.PathMode -> onDirectionModeChange(area.areaId, optionId)
        StartPanelActionId.SourcesTracks -> onToggleTrack(area.areaId, optionId)
        StartPanelActionId.SourcesBlend -> onSignalBlendChange(area.areaId, optionId.toFloat())
        StartPanelActionId.SourcesMode -> onSourcesModeChange(area.areaId, optionId)
        StartPanelActionId.FlowIntensity -> onIntensityChange(area.areaId, optionId.toFloat())
        StartPanelActionId.FlowProfile -> onFlowProfileChange(area.areaId, optionId)
        StartPanelActionId.FlowSwitches -> when (optionId) {
            "reminders" -> onRemindersChange(area.areaId, !area.remindersEnabled)
            "review" -> onReviewChange(area.areaId, !area.reviewEnabled)
            "experiments" -> onExperimentsChange(area.areaId, !area.experimentsEnabled)
            else -> Unit
        }

        StartPanelActionId.SnapshotClear -> Unit
    }
}

private val areaIdentityIconChoices = startAreaIconOptions.map { ChoiceChipItem(id = it.id, label = it.label) }
