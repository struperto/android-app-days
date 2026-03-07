package com.struperto.androidappdays.feature.single.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.feature.single.model.SingleMirrorLane
import com.struperto.androidappdays.feature.single.model.SingleQuickAction
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.ui.theme.AppTheme

private enum class SingleSheet {
    Routes,
    Ai,
}

private data class AiQuickAction(
    val title: String,
    val route: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleHomeScreen(
    state: SingleHomeState,
    onOpenAction: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var activeSheet by rememberSaveable { mutableStateOf<SingleSheet?>(null) }
    val surface = AppTheme.colors.surface
    val surfaceStrong = AppTheme.colors.surfaceStrong
    val accentSoft = AppTheme.colors.accentSoft
    val aiActions = remember {
        listOf(
            AiQuickAction(
                title = "Mirror",
                route = AppDestination.Home.route,
                icon = Icons.Outlined.SmartToy,
            ),
            AiQuickAction(
                title = "Plan",
                route = AppDestination.Plan.route,
                icon = Icons.Outlined.TaskAlt,
            ),
            AiQuickAction(
                title = "Capture",
                route = AppDestination.Capture.route,
                icon = Icons.Outlined.PhotoCamera,
            ),
            AiQuickAction(
                title = "Next",
                route = AppDestination.Create.route,
                icon = Icons.Outlined.Timeline,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        surface,
                        surfaceStrong,
                    ),
                ),
            )
            .drawBehind {
                drawCircle(
                    color = accentSoft.copy(alpha = 0.42f),
                    radius = size.minDimension * 0.34f,
                    center = androidx.compose.ui.geometry.Offset(
                        x = size.width * 0.5f,
                        y = size.height * 0.28f,
                    ),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.76f),
                    radius = size.minDimension * 0.27f,
                    center = androidx.compose.ui.geometry.Offset(
                        x = size.width * 0.5f,
                        y = size.height * 0.7f,
                    ),
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = AppTheme.dimensions.screenPadding, vertical = 16.dp)
                .padding(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            HomeTopBar(
                modeLabel = state.modeLabel,
                onOpenSettings = onOpenSettings,
            )
            MirrorDashboardCard(
                stageLabel = state.stageLabel,
                cadenceLabel = state.cadenceLabel,
                lanes = state.mirrorLanes,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }

        BottomDockRow(
            onOpenRoutes = { activeSheet = SingleSheet.Routes },
            onOpenAi = { activeSheet = SingleSheet.Ai },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = AppTheme.dimensions.screenPadding,
                    end = AppTheme.dimensions.screenPadding,
                    bottom = AppTheme.dimensions.screenPadding,
                ),
        )
    }

    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            containerColor = AppTheme.colors.surfaceStrong,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppTheme.colors.outlineSoft),
                )
            },
        ) {
            when (activeSheet) {
                SingleSheet.Routes -> RoutesSheet(
                    actions = state.actions,
                    onOpenAction = { route ->
                        activeSheet = null
                        onOpenAction(route)
                    },
                )
                SingleSheet.Ai -> LocalAiSheet(
                    actions = aiActions,
                    onOpenAction = { route ->
                        activeSheet = null
                        if (route != AppDestination.Home.route) {
                            onOpenAction(route)
                        }
                    },
                )
                null -> Unit
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    modeLabel: String,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacingS),
        ) {
            Text(
                text = "DAYS",
                style = AppTheme.typography.label,
                color = AppTheme.colors.muted,
            )
            ModePickerPill(modeLabel = modeLabel)
        }
        PillIconButton(
            icon = Icons.Outlined.Settings,
            contentDescription = "Einstellungen",
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun ModePickerPill(
    modeLabel: String,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.94f))
                .border(1.dp, AppTheme.colors.outlineSoft.copy(alpha = 0.72f), RoundedCornerShape(999.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = modeLabel,
                style = AppTheme.typography.body,
                color = AppTheme.colors.ink,
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = AppTheme.colors.muted,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Single") },
                onClick = { expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Multi") },
                enabled = false,
                onClick = { expanded = false },
            )
            DropdownMenuItem(
                text = { Text("Coworker") },
                enabled = false,
                onClick = { expanded = false },
            )
        }
    }
}

@Composable
private fun MirrorDashboardCard(
    stageLabel: String,
    cadenceLabel: String,
    lanes: List<SingleMirrorLane>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.86f),
        ),
        shape = RoundedCornerShape(34.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardEdgeLabel(label = "SOLL")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stageLabel,
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.muted,
                    )
                    Text(
                        text = "Dashboard",
                        style = AppTheme.typography.title,
                        color = AppTheme.colors.ink,
                    )
                }
                DashboardEdgeLabel(label = "IST")
            }

            HorizontalDivider(color = AppTheme.colors.outlineSoft.copy(alpha = 0.75f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                lanes.forEach { lane ->
                    MirrorLaneRow(lane = lane)
                }
            }

            HorizontalDivider(color = AppTheme.colors.outlineSoft.copy(alpha = 0.62f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = cadenceLabel,
                    style = AppTheme.typography.mono,
                    color = AppTheme.colors.muted,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiniStatusDot(color = AppTheme.colors.ink)
                    MiniStatusDot(color = AppTheme.colors.accent)
                    MiniStatusDot(color = AppTheme.colors.accentSoft)
                }
            }
        }
    }
}

@Composable
private fun DashboardEdgeLabel(
    label: String,
) {
    Text(
        text = label,
        style = AppTheme.typography.label,
        color = AppTheme.colors.muted,
    )
}

@Composable
private fun MirrorLaneRow(
    lane: SingleMirrorLane,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MirrorSideBar(
            value = lane.target,
            mirrored = true,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    AppTheme.colors.ink.copy(alpha = 0.8f),
                ),
            ),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = lane.label,
            modifier = Modifier.width(72.dp),
            style = AppTheme.typography.label,
            color = AppTheme.colors.ink,
            textAlign = TextAlign.Center,
        )
        MirrorSideBar(
            value = lane.actual,
            mirrored = false,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    AppTheme.colors.accent.copy(alpha = 0.5f),
                    AppTheme.colors.accent,
                ),
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MirrorSideBar(
    value: Float,
    mirrored: Boolean,
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(18.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(AppTheme.colors.surface.copy(alpha = 0.9f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.dp, vertical = 1.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(if (mirrored) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(brush),
            )
        }
    }
}

@Composable
private fun MiniStatusDot(
    color: Color,
) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun BottomDockRow(
    onOpenRoutes: () -> Unit,
    onOpenAi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        OrbitDockButton(
            icon = Icons.Outlined.Add,
            contentDescription = "Kernrouten",
            orbitColor = AppTheme.colors.accentSoft.copy(alpha = 0.85f),
            buttonColor = AppTheme.colors.surfaceStrong,
            iconTint = AppTheme.colors.ink,
            size = 66.dp,
            onClick = onOpenRoutes,
        )
        OrbitDockButton(
            icon = Icons.Outlined.SmartToy,
            contentDescription = "Lokale AI",
            orbitColor = AppTheme.colors.outlineSoft.copy(alpha = 0.95f),
            buttonColor = AppTheme.colors.surfaceStrong,
            iconTint = AppTheme.colors.accent,
            size = 58.dp,
            onClick = onOpenAi,
        )
    }
}

@Composable
private fun OrbitDockButton(
    icon: ImageVector,
    contentDescription: String,
    orbitColor: Color,
    buttonColor: Color,
    iconTint: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .size(size + 10.dp)
            .clip(shape)
            .background(orbitColor.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(buttonColor.copy(alpha = 0.96f))
                .border(1.dp, AppTheme.colors.outlineSoft.copy(alpha = 0.8f), shape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun RoutesSheet(
    actions: List<SingleQuickAction>,
    onOpenAction: (String) -> Unit,
) {
    SheetFrame(
        title = "Kernrouten",
        subtitle = "Single",
    ) {
        actions.chunked(3).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowActions.forEach { action ->
                    SheetTile(
                        label = action.title,
                        icon = iconForRoute(action.route),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenAction(action.route) },
                    )
                }
                repeat(3 - rowActions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LocalAiSheet(
    actions: List<AiQuickAction>,
    onOpenAction: (String) -> Unit,
) {
    SheetFrame(
        title = "Lokale AI",
        subtitle = "on-device shell",
    ) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowActions.forEach { action ->
                    SheetTile(
                        label = action.title,
                        icon = action.icon,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenAction(action.route) },
                    )
                }
                repeat(2 - rowActions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SheetFrame(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = subtitle,
                style = AppTheme.typography.label,
                color = AppTheme.colors.muted,
            )
            Text(
                text = title,
                style = AppTheme.typography.title,
                color = AppTheme.colors.ink,
            )
        }
        content()
    }
}

@Composable
private fun SheetTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 112.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface.copy(alpha = 0.92f),
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(horizontal = 16.dp, vertical = 18.dp)),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceStrong)
                    .border(1.dp, AppTheme.colors.outlineSoft.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.accent,
                )
            }
            Text(
                text = label,
                style = AppTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colors.ink,
            )
        }
    }
}

@Composable
private fun PillIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.94f))
            .border(1.dp, AppTheme.colors.outlineSoft.copy(alpha = 0.72f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppTheme.colors.ink,
        )
    }
}

private fun iconForRoute(route: String): ImageVector {
    return when (route) {
        AppDestination.LifeWheel.route -> Icons.Outlined.FavoriteBorder
        AppDestination.WorkingSet.route -> Icons.Outlined.EditNote
        AppDestination.DaySchedule.route -> Icons.Outlined.Schedule
        AppDestination.Plan.route -> Icons.Outlined.TaskAlt
        AppDestination.Capture.route -> Icons.Outlined.PhotoCamera
        AppDestination.Create.route -> Icons.Outlined.Timeline
        else -> Icons.Outlined.Timeline
    }
}
