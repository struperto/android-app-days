package com.struperto.androidappdays.feature.single.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.struperto.androidappdays.domain.EvaluationState
import com.struperto.androidappdays.domain.HourSlotStatus
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.feature.single.model.HomeDomainHint
import com.struperto.androidappdays.feature.single.model.HomeDomainStripItem
import com.struperto.androidappdays.feature.single.model.HomeTimelineSegment
import com.struperto.androidappdays.feature.single.model.HomeTrackWindow
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysTile
import com.struperto.androidappdays.feature.single.shared.TileRole
import com.struperto.androidappdays.feature.single.shared.daysDomainIcon
import com.struperto.androidappdays.feature.single.shared.daysDomainShortLabel
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.navigation.DebugHomeWindowBus
import com.struperto.androidappdays.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleHomeScreen(
    state: SingleHomeState,
    onOpenStart: () -> Unit,
    onOpenMulti: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefreshPassiveSignals: () -> Unit,
    onSetHourSlotStatus: (segmentId: String, logicalHour: Int, windowId: String, status: HourSlotStatus) -> Unit,
    onSaveHourSlotNote: (segmentId: String, logicalHour: Int, windowId: String, note: String) -> Unit,
) {
    val sections = remember(state.segments, state.segmentHints) {
        buildHourlySections(
            segments = state.segments,
            segmentHints = state.segmentHints,
        )
    }
    val debugWindowId by DebugHomeWindowBus.windowId.collectAsStateWithLifecycle()
    var selectedWindowId by rememberSaveable { mutableStateOf<String?>(null) }
    val drafts = remember { mutableStateMapOf<String, String>() }

    var selectedRowForOverlay by remember { mutableStateOf<HourlyDashboardRow?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sections) {
        val preferredWindowId = sections
            .firstOrNull { section -> section.rows.any(HourlyDashboardRow::isCurrent) }
            ?.window
            ?.id
            ?: sections.firstOrNull()?.window?.id
        if (selectedWindowId == null || sections.none { it.window.id == selectedWindowId }) {
            selectedWindowId = preferredWindowId
        }
    }

    LaunchedEffect(debugWindowId, sections) {
        val overrideWindowId = debugWindowId ?: return@LaunchedEffect
        if (sections.any { it.window.id == overrideWindowId }) {
            selectedWindowId = overrideWindowId
            DebugHomeWindowBus.clear()
        }
    }

    LaunchedEffect(sections) {
        val currentIds = sections.flatMap { section -> section.rows }.map(HourlyDashboardRow::id).toSet()
        drafts.keys.toList().filterNot(currentIds::contains).forEach(drafts::remove)
        sections
            .flatMap { section -> section.rows }
            .forEach { row ->
                if (drafts[row.id] != row.slotNote) {
                    drafts[row.id] = row.slotNote
                }
            }
    }

    LifecycleResumeEffect(Unit) {
        onRefreshPassiveSignals()
        onPauseOrDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.colors.surface,
                        AppTheme.colors.surfaceStrong,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = AppTheme.dimensions.screenPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DaysModeTopBar(
                activeDestination = AppDestination.Home,
                onOpenStart = onOpenStart,
                onOpenSingle = {},
                onOpenMulti = onOpenMulti,
                onOpenSettings = onOpenSettings,
                settingsTestTag = "home-open-settings",
            )
            SingleTileStack(
                state = state,
                sections = sections,
                activeWindow = sections.firstOrNull { it.window.id == selectedWindowId } ?: sections.firstOrNull(),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 980.dp),
            )
        }

        if (selectedRowForOverlay != null) {
            val row = selectedRowForOverlay!!
            ModalBottomSheet(
                onDismissRequest = { selectedRowForOverlay = null },
                sheetState = sheetState,
                containerColor = AppTheme.colors.surface,
                dragHandle = null,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimensions.screenPadding)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = row.hourLabel,
                                style = MaterialTheme.typography.titleLarge,
                                color = AppTheme.colors.ink,
                            )
                            Text(
                                text = row.focusLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = AppTheme.colors.muted,
                            )
                        }
                        SlotStatusBadge(
                            hourLabel = row.hourLabel,
                            status = row.slotStatus,
                        )
                    }

                    SlotStatusActionRow(
                        row = row,
                        onSetStatus = { segmentId, logicalHour, windowId, status ->
                            onSetHourSlotStatus(segmentId, logicalHour, windowId, status)
                        },
                    )

                    CompactInputField(
                        value = drafts[row.id].orEmpty(),
                        placeholder = row.focusLabel,
                        onValueChange = {
                            drafts[row.id] = it
                            onSaveHourSlotNote(row.id, row.logicalHour, row.windowId, it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleTileStack(
    state: SingleHomeState,
    sections: List<HourlyDashboardSection>,
    activeWindow: HourlyDashboardSection?,
    modifier: Modifier = Modifier,
) {
    val currentRow = sections
        .flatMap(HourlyDashboardSection::rows)
        .sortedBy(HourlyDashboardRow::logicalHour)
        .firstOrNull(HourlyDashboardRow::isCurrent)
        ?: activeWindow?.rows?.firstOrNull()
        ?: sections.firstOrNull()?.rows?.firstOrNull()
    val currentHints = currentRow?.let { state.segmentHints[it.id].orEmpty() }.orEmpty()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home-dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DaysTile(
            role = TileRole.Hero,
        ) {
            FocusNowTileBody(
                dateLabel = state.title,
                currentRow = currentRow,
                hints = currentHints,
            )
        }
    }
}

@Composable
private fun FocusNowTileBody(
    dateLabel: String,
    currentRow: HourlyDashboardRow?,
    hints: List<HomeDomainHint>,
) {
    val focus = currentRow?.let { readableFocusLabel(it, hints) }.orEmpty()
    val hour = currentRow?.hourLabel ?: "--:--"
    val status = visualStatus(currentRow?.slotStatus ?: HourSlotStatus.UNKNOWN, hints)
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = hour,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.ink,
                )
                if (focus.isNotBlank()) {
                    Text(
                        text = focus,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FocusMetaIcons(
                currentRow = currentRow,
                status = status,
            )
        }
        HintGlyphRow(
            row = currentRow,
            hints = hints,
        )
        LoadMeterRow(
            target = currentRow?.target ?: 0f,
            actual = currentRow?.actual ?: 0f,
        )
    }
}

@Composable
private fun FocusMetaIcons(
    currentRow: HourlyDashboardRow?,
    status: HourSlotStatus,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        currentRow?.windowId?.let { windowId ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surface.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center,
            ) {
                WindowGlyph(
                    window = when (windowId) {
                        HomeTrackWindow.VORMITTAG.id -> HomeTrackWindow.VORMITTAG
                        HomeTrackWindow.NACHMITTAG.id -> HomeTrackWindow.NACHMITTAG
                        else -> HomeTrackWindow.ABEND
                    },
                    selected = true,
                )
            }
        }
        if (status != HourSlotStatus.UNKNOWN) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(slotStatusContainer(status)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = slotStatusIcon(status),
                    contentDescription = null,
                    tint = slotStatusColor(status),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun readableFocusLabel(
    row: HourlyDashboardRow,
    hints: List<HomeDomainHint>,
): String {
    val genericLabels = setOf(
        "Fokus",
        "Bewegen",
        "Abschluss",
        "Klar starten",
        "Fokus halten",
        "Mittag halten",
        "Im Fluss bleiben",
        "Tag schliessen",
        "Runterfahren",
    )
    if (row.focusLabel !in genericLabels) {
        return row.focusLabel
    }
    val domains = hints.map(HomeDomainHint::domain).distinct()
    return when {
        LifeDomain.NUTRITION in domains && LifeDomain.HYDRATION in domains -> "Essen und Wasser"
        LifeDomain.SLEEP in domains -> "Schlaf schuetzen"
        LifeDomain.FOCUS in domains -> "Fokus halten"
        LifeDomain.MOVEMENT in domains -> "In Bewegung bleiben"
        LifeDomain.HYDRATION in domains -> "Wasser auffuellen"
        LifeDomain.NUTRITION in domains -> "Essen planen"
        row.logicalHour in 6..8 -> "Klar starten"
        row.logicalHour in 9..11 -> "Fokus halten"
        row.logicalHour in 12..14 -> "Mittag halten"
        row.logicalHour in 15..17 -> "Im Fluss bleiben"
        row.logicalHour in 18..21 -> "Tag schliessen"
        else -> "Runterfahren"
    }
}

@Composable
private fun LoadMeterRow(
    target: Float,
    actual: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoadMeter(
            icon = Icons.Outlined.TrackChanges,
            value = target,
            color = AppTheme.colors.ink,
            contentDescription = "Soll ${loadBandLabel(target)}",
            modifier = Modifier.weight(1f),
        )
        LoadMeter(
            icon = Icons.Outlined.EditNote,
            value = actual,
            color = AppTheme.colors.accent,
            contentDescription = "Ist ${loadBandLabel(actual)}",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LoadMeter(
    icon: ImageVector,
    value: Float,
    color: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp),
            )
        }
        MetricBar(
            value = value,
            color = color,
            trackColor = AppTheme.colors.outlineSoft.copy(alpha = 0.22f),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HintGlyphRow(
    row: HourlyDashboardRow?,
    hints: List<HomeDomainHint>,
) {
    val visibleHints = hints.distinctBy(HomeDomainHint::domain).take(3)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (visibleHints.isEmpty()) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surface.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = row?.let { focusIcon(it, emptyList()) } ?: Icons.Outlined.CenterFocusStrong,
                    contentDescription = null,
                    tint = AppTheme.colors.ink,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            visibleHints.forEach { hint ->
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(domainStateColor(hint.state).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = daysDomainIcon(hint.domain),
                        contentDescription = null,
                        tint = domainStateColor(hint.state),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeBandTileBody(
    rows: List<HourlyDashboardRow>,
    segmentHints: Map<String, List<HomeDomainHint>>,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { row ->
            TimeBandHourMarker(
                row = row,
                hints = segmentHints[row.id].orEmpty(),
            )
        }
    }
}

@Composable
private fun TimeBandHourMarker(
    row: HourlyDashboardRow,
    hints: List<HomeDomainHint>,
) {
    val status = visualStatus(row.slotStatus, hints)
    val visibleHints = hints.distinctBy(HomeDomainHint::domain).take(2)
    val containerColor = when {
        row.isCurrent -> AppTheme.colors.accentSoft.copy(alpha = 0.32f)
        status != HourSlotStatus.UNKNOWN -> slotStatusColor(status).copy(alpha = 0.08f)
        else -> AppTheme.colors.surface.copy(alpha = 0.56f)
    }
    val borderColor = when {
        row.isCurrent -> AppTheme.colors.accent
        status != HourSlotStatus.UNKNOWN -> slotStatusColor(status).copy(alpha = 0.4f)
        else -> AppTheme.colors.outlineSoft.copy(alpha = 0.28f)
    }

    Column(
        modifier = Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .semantics {
                contentDescription = "${row.hourLabel} ${slotStatusLabel(status)}"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = row.hourLabel,
            style = if (row.isCurrent) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            },
            color = if (row.isCurrent) AppTheme.colors.ink else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (visibleHints.isEmpty()) {
                Icon(
                    imageVector = focusIcon(row, emptyList()),
                    contentDescription = null,
                    tint = AppTheme.colors.ink,
                    modifier = Modifier.size(16.dp),
                )
            } else {
                visibleHints.forEach { hint ->
                    Icon(
                        imageVector = daysDomainIcon(hint.domain),
                        contentDescription = null,
                        tint = domainStateColor(hint.state),
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppTheme.colors.outlineSoft.copy(alpha = 0.16f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(slotStatusColor(status)),
            )
        }
    }
}

@Composable
private fun WindowBandsTileBody(
    sections: List<HourlyDashboardSection>,
    activeWindowId: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        sections.forEach { section ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WindowGlyph(
                    window = section.window,
                    selected = section.window.id == activeWindowId,
                )
                Text(
                    text = section.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.ink,
                )
                SectionStatePulse(section = section)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DomainOrbitTileBody(
    items: List<HomeDomainStripItem>,
) {
    val trackColor = AppTheme.colors.outlineSoft.copy(alpha = 0.26f)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items.take(6).forEach { item ->
            val stateColor = domainStateColor(item.state)
            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val strokeWidth = size.minDimension * 0.12f
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = strokeWidth),
                        )
                        drawArc(
                            color = stateColor,
                            startAngle = -90f,
                            sweepAngle = 360f * item.confidence.coerceIn(0f, 1f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }
                    Icon(
                        imageVector = daysDomainIcon(item.domain),
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = daysDomainShortLabel(item.domain),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DriftTileBody(
    fitScore: Float,
    hintCount: Int,
    sections: List<HourlyDashboardSection>,
) {
    val issueColor = if (hintCount > 0) AppTheme.colors.danger else AppTheme.colors.accent
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MetricRow(
            label = "Fit",
            value = fitScore,
            color = AppTheme.colors.accent,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            icon = Icons.Outlined.TrackChanges,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MiniMetricTile(
                icon = Icons.Outlined.ErrorOutline,
                label = "Hinweise",
                value = hintCount.toString(),
                color = issueColor,
                modifier = Modifier.weight(1f),
            )
            MiniMetricTile(
                icon = Icons.Outlined.Schedule,
                label = "Fenster",
                value = sections.size.toString(),
                color = AppTheme.colors.info,
                modifier = Modifier.weight(1f),
            )
            MiniMetricTile(
                icon = Icons.Outlined.CheckCircle,
                label = "Spur",
                value = sections.sumOf { it.onTrackCount }.toString(),
                color = AppTheme.colors.accent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MiniMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.ink,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NextMoveTileBody(
    priorities: List<String>,
    thesis: String,
    coachText: String?,
) {
    val chips = buildList {
        addAll(priorities.take(3))
        if (!coachText.isNullOrBlank()) add(coachText)
    }.distinct().take(4)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (thesis.isNotBlank()) {
            Text(
                text = thesis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chips.forEach { chip ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppTheme.colors.accentSoft.copy(alpha = 0.34f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppTheme.colors.accent,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun HourRibbonTileBody(
    rows: List<HourlyDashboardRow>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.take(5).forEach { row ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (row.isCurrent) AppTheme.colors.accentSoft.copy(alpha = 0.36f) else AppTheme.colors.surfaceStrong,
                    )
                    .border(
                        1.dp,
                        if (row.isCurrent) AppTheme.colors.accentSoft else AppTheme.colors.outlineSoft.copy(alpha = 0.3f),
                        RoundedCornerShape(18.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = row.hourLabel,
                    style = AppTheme.typography.mono,
                    color = AppTheme.colors.ink,
                )
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppTheme.colors.outlineSoft.copy(alpha = 0.24f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((26.dp * row.actual.coerceIn(0f, 1f)))
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(999.dp))
                            .background(slotStatusColor(row.slotStatus)),
                    )
                }
                Icon(
                    imageVector = focusIcon(row, emptyList()),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SleepToFocusTileBody(
    domains: List<HomeDomainStripItem>,
    currentRow: HourlyDashboardRow?,
) {
    val sleep = domains.firstOrNull { it.domain == LifeDomain.SLEEP }
    val focus = domains.firstOrNull { it.domain == LifeDomain.FOCUS }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiniIconGauge(
            icon = Icons.Outlined.Hotel,
            label = "Schlaf",
            item = sleep,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .weight(0.4f)
                .height(2.dp)
                .background(AppTheme.colors.outlineSoft.copy(alpha = 0.5f)),
        )
        MiniIconGauge(
            icon = Icons.Outlined.CenterFocusStrong,
            label = currentRow?.focusLabel ?: "Fokus",
            item = focus,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MiniIconGauge(
    icon: ImageVector,
    label: String,
    item: HomeDomainStripItem?,
    modifier: Modifier = Modifier,
) {
    val color = item?.let { domainStateColor(it.state) } ?: AppTheme.colors.outlineSoft
    val value = item?.confidence ?: 0f
    val trackColor = AppTheme.colors.outlineSoft.copy(alpha = 0.24f)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val strokeWidth = size.minDimension * 0.12f
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokeWidth),
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * value.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class HourlyDashboardSection(
    val window: HomeTrackWindow,
    val label: String,
    val rows: List<HourlyDashboardRow>,
    val issueCount: Int,
    val onTrackCount: Int,
    val unknownCount: Int,
)

private data class HourlyDashboardRow(
    val id: String,
    val logicalHour: Int,
    val windowId: String,
    val hourLabel: String,
    val focusLabel: String,
    val target: Float,
    val actual: Float,
    val isCurrent: Boolean,
    val slotStatus: HourSlotStatus,
    val slotNote: String,
)

private fun buildHourlySections(
    segments: List<HomeTimelineSegment>,
    segmentHints: Map<String, List<HomeDomainHint>>,
): List<HourlyDashboardSection> {
    val sortedSegments = segments.sortedBy(::segmentStartHour)
    return HomeTrackWindow.all.mapNotNull { window ->
        val rows = sortedSegments
            .filter { it.window == window }
            .map { segment ->
                HourlyDashboardRow(
                    id = segment.id,
                    logicalHour = segmentStartHour(segment),
                    windowId = window.id,
                    hourLabel = segment.label,
                    focusLabel = segment.primaryFocus,
                    target = segment.targetLoad.coerceIn(0f, 1f),
                    actual = segment.actualLoad.coerceIn(0f, 1f),
                    isCurrent = segment.isCurrent,
                    slotStatus = segment.slotStatus,
                    slotNote = segment.slotNote,
                )
            }
        if (rows.isEmpty()) {
            null
        } else {
            val rowStates = rows.map { row ->
                when {
                    row.slotStatus == HourSlotStatus.OPEN || row.slotStatus == HourSlotStatus.REDUCED -> EvaluationState.BELOW_TARGET
                    row.slotStatus == HourSlotStatus.ON_TRACK -> EvaluationState.ON_TRACK
                    else -> EvaluationState.UNKNOWN
                }
            }
            HourlyDashboardSection(
                window = window,
                label = window.label,
                rows = rows,
                issueCount = rows.sumOf { row ->
                    segmentHints[row.id].orEmpty().count(::isAlertHint)
                }.coerceAtMost(3),
                onTrackCount = rowStates.count { it == EvaluationState.ON_TRACK },
                unknownCount = rowStates.count { it == EvaluationState.UNKNOWN },
            )
        }
    }
}

private fun segmentStartHour(
    segment: HomeTimelineSegment,
): Int {
    return segment.id.substringAfterLast('_').toIntOrNull()
        ?: segment.label.substringBefore(':').toIntOrNull()
        ?: Int.MAX_VALUE
}

@Composable
private fun HourlyDashboardCard(
    title: String,
    fitLabel: String,
    fitScore: Float,
    thesis: String,
    topPriorities: List<String>,
    dailyDomains: List<HomeDomainStripItem>,
    sections: List<HourlyDashboardSection>,
    activeWindowId: String?,
    onWindowSelected: (String) -> Unit,
    segmentHints: Map<String, List<HomeDomainHint>>,
    onRowSelected: (HourlyDashboardRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeSection = sections.firstOrNull { it.window.id == activeWindowId } ?: sections.firstOrNull()

    DaysTile(
        role = TileRole.Summary,
        modifier = modifier.testTag("home-dashboard"),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val isWide = maxWidth >= 760.dp
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HomeDashboardHeader(
                            title = title,
                            fitLabel = fitLabel,
                            fitScore = fitScore,
                            thesis = thesis,
                            topPriorities = topPriorities,
                        )
                        WindowSwitchRow(
                            sections = sections,
                            activeWindowId = activeSection?.window?.id,
                            onWindowSelected = onWindowSelected,
                        )
                        DomainStrip(
                            items = dailyDomains,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        HourlySectionList(
                            rows = activeSection?.rows.orEmpty(),
                            segmentHints = segmentHints,
                            onRowSelected = onRowSelected,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HomeDashboardHeader(
                        title = title,
                        fitLabel = fitLabel,
                        fitScore = fitScore,
                        thesis = thesis,
                        topPriorities = topPriorities,
                    )

                    WindowSwitchRow(
                        sections = sections,
                        activeWindowId = activeSection?.window?.id,
                        onWindowSelected = onWindowSelected,
                    )

                    DomainStrip(
                        items = dailyDomains,
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        HourlySectionList(
                            rows = activeSection?.rows.orEmpty(),
                            segmentHints = segmentHints,
                            onRowSelected = onRowSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlySectionList(
    rows: List<HourlyDashboardRow>,
    segmentHints: Map<String, List<HomeDomainHint>>,
    onRowSelected: (HourlyDashboardRow) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        rows.forEachIndexed { index, row ->
            HourlyInputTile(
                row = row,
                hints = segmentHints[row.id].orEmpty(),
                onClick = { onRowSelected(row) },
            )
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = AppTheme.colors.outlineSoft.copy(alpha = 0.45f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeDashboardHeader(
    title: String,
    fitLabel: String,
    fitScore: Float,
    thesis: String,
    topPriorities: List<String>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    text = "Single",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.muted,
                )
                Text(
                    text = title,
                    style = AppTheme.typography.title,
                    color = AppTheme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FitSummary(
                label = fitLabel,
                score = fitScore,
            )
        }
        if (thesis.isNotBlank()) {
            Text(
                text = thesis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (topPriorities.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                topPriorities.take(3).forEach { priority ->
                    DaysMetaPill(
                        label = priority,
                        modifier = Modifier.widthIn(max = 220.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FitDots(
    score: Float,
) {
    val activeCount = when {
        score >= 0.74f -> 3
        score >= 0.54f -> 2
        else -> 1
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == 1) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < activeCount) AppTheme.colors.accent else AppTheme.colors.outlineSoft.copy(alpha = 0.35f),
                    ),
            )
        }
    }
}

@Composable
private fun FitSummary(
    label: String,
    score: Float,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DaysMetaPill(label = label)
        FitDots(score = score)
    }
}

@Composable
private fun DomainStrip(
    items: List<HomeDomainStripItem>,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home-domain-strip"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .testTag("home-domain-${item.domain.name.lowercase()}-${item.state.name.lowercase()}")
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (item.state == EvaluationState.UNKNOWN) {
                            AppTheme.colors.surface.copy(alpha = 0.8f)
                        } else {
                            domainStateColor(item.state).copy(alpha = 0.12f)
                        },
                    )
                    .border(1.dp, domainStateColor(item.state).copy(alpha = 0.24f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = daysDomainIcon(item.domain),
                    contentDescription = null,
                    tint = domainStateColor(item.state),
                    modifier = Modifier
                        .size(16.dp)
                        .testTag("home-domain-${item.domain.name.lowercase()}"),
                )
                Text(
                    text = daysDomainShortLabel(item.domain),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                ConfidenceBar(
                    value = item.confidence,
                    color = domainStateColor(item.state),
                )
            }
        }
    }
}

@Composable
private fun WindowSwitchRow(
    sections: List<HourlyDashboardSection>,
    activeWindowId: String?,
    onWindowSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppTheme.colors.surface.copy(alpha = 0.75f))
            .border(1.dp, AppTheme.colors.outlineSoft.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        sections.forEach { section ->
            DayPartTile(
                section = section,
                selected = section.window.id == activeWindowId,
                onClick = { onWindowSelected(section.window.id) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayPartTile(
    section: HourlyDashboardSection,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) AppTheme.colors.accentSoft.copy(alpha = 0.46f) else Color.Transparent,
            )
            .border(
                1.dp,
                if (selected) AppTheme.colors.accentSoft else Color.Transparent,
                RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("home-window-${section.window.id}"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WindowGlyph(
            window = section.window,
            selected = selected,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = section.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) AppTheme.colors.ink else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${section.rows.size}h",
                    style = AppTheme.typography.mono,
                    color = AppTheme.colors.muted,
                    maxLines = 1,
                )
                SectionStatePulse(section = section)
            }
        }
    }
}

@Composable
private fun SectionStatePulse(
    section: HourlyDashboardSection,
) {
    val total = section.rows.size.coerceAtLeast(1).toFloat()
    val alertWeight = (section.issueCount / total).coerceIn(0f, 1f)
    val onTrackWeight = (section.onTrackCount / total).coerceIn(0f, 1f)
    val unknownWeight = (section.unknownCount / total).coerceIn(0f, 1f)
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiniSectionBar(
            active = onTrackWeight > 0.05f,
            color = AppTheme.colors.accent,
        )
        MiniSectionBar(
            active = alertWeight > 0.05f,
            color = Color(0xFFB85C38),
        )
        MiniSectionBar(
            active = unknownWeight > 0.05f,
            color = AppTheme.colors.outlineSoft.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun MiniSectionBar(
    active: Boolean,
    color: Color,
) {
    Box(
        modifier = Modifier
            .width(12.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = if (active) 1f else 0.18f)),
    )
}

@Composable
private fun WindowGlyph(
    window: HomeTrackWindow,
    selected: Boolean,
) {
    val barWidths = when (window.id) {
        "vormittag" -> listOf(18.dp, 28.dp, 38.dp)
        "mittag" -> listOf(28.dp, 38.dp, 28.dp)
        else -> listOf(38.dp, 28.dp, 18.dp)
    }
    val activeColor = if (selected) AppTheme.colors.accent else AppTheme.colors.ink
    val passiveColor = if (selected) AppTheme.colors.accentSoft else AppTheme.colors.outlineSoft.copy(alpha = 0.55f)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        barWidths.forEachIndexed { index, width ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (index == 1) activeColor else passiveColor),
            )
        }
    }
}

@Composable
private fun HourlyInputTile(
    row: HourlyDashboardRow,
    hints: List<HomeDomainHint>,
    onClick: () -> Unit,
) {
    val visualStatus = visualStatus(row.slotStatus, hints)
    val visibleHints = hints.take(3)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (row.isCurrent) {
                    AppTheme.colors.accentSoft.copy(alpha = 0.14f)
                } else if (visualStatus != HourSlotStatus.UNKNOWN) {
                    slotStatusColor(visualStatus).copy(alpha = 0.04f)
                } else {
                    Color.Transparent
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .testTag("home-hour-${row.hourLabel}"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = row.hourLabel,
                            style = AppTheme.typography.mono,
                            color = AppTheme.colors.ink,
                        )
                    }
                    if (row.isCurrent) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                            Text(
                                text = "jetzt",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = focusIcon(row, visibleHints),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = row.focusLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SlotStatusBadge(
                    hourLabel = row.hourLabel,
                    status = row.slotStatus,
                )
                HourHintRow(
                    hourLabel = row.hourLabel,
                    hints = visibleHints,
                )
            }
        }

        MetricRow(
            label = "Soll",
            value = row.target,
            color = AppTheme.colors.ink,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            icon = Icons.Outlined.CheckCircle,
        )
        MetricRow(
            label = "Ist",
            value = row.actual,
            color = AppTheme.colors.accent,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            icon = Icons.Outlined.EditNote,
        )
    }
}

@Composable
private fun SlotStatusBadge(
    hourLabel: String,
    status: HourSlotStatus,
    showUnknown: Boolean = true,
) {
    if (status == HourSlotStatus.UNKNOWN && !showUnknown) {
        return
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(slotStatusContainer(status))
            .border(1.dp, slotStatusColor(status).copy(alpha = 0.24f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("home-status-badge-${hourLabel}-${status.name.lowercase()}"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = slotStatusIcon(status),
            contentDescription = null,
            tint = slotStatusColor(status),
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = slotStatusLabel(status),
            style = MaterialTheme.typography.labelMedium,
            color = slotStatusColor(status),
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotStatusActionRow(
    row: HourlyDashboardRow,
    onSetStatus: (segmentId: String, logicalHour: Int, windowId: String, status: HourSlotStatus) -> Unit,
) {
    val items = statusActionItems()
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEachIndexed { index, item ->
            val selected = row.slotStatus == item.status
            SegmentedButton(
                selected = selected,
                onClick = {
                    onSetStatus(
                        row.id,
                        row.logicalHour,
                        row.windowId,
                        if (selected) HourSlotStatus.UNKNOWN else item.status,
                    )
                },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = slotStatusContainer(item.status),
                    activeContentColor = slotStatusColor(item.status),
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    activeBorderColor = slotStatusColor(item.status).copy(alpha = 0.42f),
                    inactiveBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                ),
                modifier = Modifier.testTag("home-status-chip-${row.hourLabel}-${item.status.name.lowercase()}"),
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

private data class SlotActionItem(
    val status: HourSlotStatus,
    val label: String,
    val icon: ImageVector,
)

private fun statusActionItems(): List<SlotActionItem> {
    return listOf(
        SlotActionItem(HourSlotStatus.ON_TRACK, "Spur", Icons.Outlined.CheckCircle),
        SlotActionItem(HourSlotStatus.REDUCED, "Reduziert", Icons.Outlined.RemoveCircleOutline),
        SlotActionItem(HourSlotStatus.OPEN, "Offen", Icons.Outlined.ErrorOutline),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HourHintRow(
    hourLabel: String,
    hints: List<HomeDomainHint>,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        hints.forEach { hint ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(domainStateColor(hint.state).copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("home-hint-${hourLabel}-${hint.domain.name.lowercase()}"),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                    Icon(
                        imageVector = daysDomainIcon(hint.domain),
                        contentDescription = null,
                        tint = domainStateColor(hint.state),
                        modifier = Modifier.size(12.dp),
                    )
                Text(
                    text = daysDomainShortLabel(hint.domain),
                    style = MaterialTheme.typography.labelSmall,
                    color = domainStateColor(hint.state),
                )
            }
        }
    }
}

@Composable
private fun ConfidenceBar(
    value: Float,
    color: Color,
) {
    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        modifier = Modifier
            .width(24.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp)),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        drawStopIndicator = {},
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HourHintLegend(
    hints: List<HomeDomainHint>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        hints.forEach { hint ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(domainStateColor(hint.state)),
                )
                Text(
                    text = daysDomainShortLabel(hint.domain),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: Float,
    color: Color,
    trackColor: Color,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(12.dp),
            )
        }
        Column(
            modifier = Modifier.width(34.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
            Text(
                text = "${(value.coerceIn(0f, 1f) * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MetricBar(
            value = value,
            color = color,
            trackColor = trackColor,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun loadBandLabel(value: Float): String {
    return when {
        value < 0.12f -> "leer"
        value < 0.32f -> "offen"
        value < 0.56f -> "aktiv"
        value < 0.76f -> "dicht"
        else -> "voll"
    }
}

private fun visualStatus(
    slotStatus: HourSlotStatus,
    hints: List<HomeDomainHint>,
): HourSlotStatus {
    if (slotStatus != HourSlotStatus.UNKNOWN) {
        return slotStatus
    }
    return when {
        hints.any(::isAlertHint) -> HourSlotStatus.OPEN
        hints.any { it.state == EvaluationState.ON_TRACK } -> HourSlotStatus.ON_TRACK
        else -> HourSlotStatus.UNKNOWN
    }
}

private fun slotStatusLabel(
    status: HourSlotStatus,
): String {
    return when (status) {
        HourSlotStatus.ON_TRACK -> "auf Spur"
        HourSlotStatus.REDUCED -> "reduziert"
        HourSlotStatus.OPEN -> "offen"
        HourSlotStatus.UNKNOWN -> "ohne Signal"
    }
}

@Composable
private fun slotStatusColor(status: HourSlotStatus): Color {
    return when (status) {
        HourSlotStatus.ON_TRACK -> AppTheme.colors.accent
        HourSlotStatus.REDUCED -> Color(0xFFC8933F)
        HourSlotStatus.OPEN -> Color(0xFFB85C38)
        HourSlotStatus.UNKNOWN -> AppTheme.colors.muted
    }
}

@Composable
private fun slotStatusContainer(status: HourSlotStatus): Color {
    return when (status) {
        HourSlotStatus.UNKNOWN -> MaterialTheme.colorScheme.surface
        else -> slotStatusColor(status).copy(alpha = 0.14f)
    }
}

private fun slotStatusIcon(status: HourSlotStatus): ImageVector {
    return when (status) {
        HourSlotStatus.ON_TRACK -> Icons.Outlined.CheckCircle
        HourSlotStatus.REDUCED -> Icons.Outlined.RemoveCircleOutline
        HourSlotStatus.OPEN -> Icons.Outlined.ErrorOutline
        HourSlotStatus.UNKNOWN -> Icons.Outlined.MoreHoriz
    }
}

private fun isAlertHint(
    hint: HomeDomainHint,
): Boolean {
    return hint.state == EvaluationState.BELOW_TARGET ||
        hint.state == EvaluationState.ABOVE_TARGET ||
        hint.state == EvaluationState.OUTSIDE_WINDOW
}

@Composable
private fun domainStateColor(state: EvaluationState): Color {
    return when (state) {
        EvaluationState.ON_TRACK -> AppTheme.colors.accent
        EvaluationState.BELOW_TARGET -> Color(0xFFB85C38)
        EvaluationState.ABOVE_TARGET -> Color(0xFF5C7FA8)
        EvaluationState.OUTSIDE_WINDOW -> AppTheme.colors.muted
        EvaluationState.UNKNOWN -> AppTheme.colors.muted
    }
}

@Composable
private fun MetricBar(
    value: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp)),
        color = color,
        trackColor = trackColor,
        drawStopIndicator = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag("home-note-field"),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = null,
            )
        },
        placeholder = {
            Text(
                text = placeholder.ifBlank { "Kurze Notiz" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        textStyle = MaterialTheme.typography.bodySmall,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

private fun focusIcon(
    row: HourlyDashboardRow,
    hints: List<HomeDomainHint>,
): ImageVector {
    return hints.firstOrNull()?.let { daysDomainIcon(it.domain) }
        ?: when {
            row.focusLabel.contains("sleep", ignoreCase = true) || row.focusLabel.contains("schlaf", ignoreCase = true) ->
                Icons.Outlined.Hotel
            row.focusLabel.contains("move", ignoreCase = true) || row.focusLabel.contains("walk", ignoreCase = true) ->
                Icons.AutoMirrored.Outlined.DirectionsWalk
            row.focusLabel.contains("water", ignoreCase = true) || row.focusLabel.contains("hyd", ignoreCase = true) ->
                Icons.Outlined.WaterDrop
            row.focusLabel.contains("food", ignoreCase = true) || row.focusLabel.contains("meal", ignoreCase = true) ->
                Icons.Outlined.Restaurant
            else -> Icons.Outlined.CenterFocusStrong
        }
}
