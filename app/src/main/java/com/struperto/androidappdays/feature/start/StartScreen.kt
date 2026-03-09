package com.struperto.androidappdays.feature.start

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.defaultLifeAreas
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.feature.single.shared.DaysTile
import com.struperto.androidappdays.feature.single.shared.TileRole
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.ui.theme.AppTheme

private val StartMuted = Color(0xFF6D675C)
private val StartOutlineSoft = Color(0xFFCBC3B9)

@Composable
fun StartScreen(
    homeState: SingleHomeState,
    onOpenSettings: () -> Unit,
    onOpenStart: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenMulti: () -> Unit,
    onOpenArea: (String) -> Unit,
    onOpenLifeWheel: () -> Unit,
    onOpenSettingsDomains: () -> Unit,
    onOpenSettingsSources: () -> Unit,
    onOpenSettingsResearch: () -> Unit,
) {
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DaysModeTopBar(
                activeDestination = AppDestination.Start,
                onOpenSettings = onOpenSettings,
                onOpenStart = onOpenStart,
                onOpenSingle = onOpenHome,
                onOpenMulti = onOpenMulti,
                settingsTestTag = "start-open-settings",
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AreaPulseCard(
                    homeState = homeState,
                    onOpenArea = onOpenArea,
                )
            }
        }
        StartToolDock(
            onOpenLifeWheel = onOpenLifeWheel,
            onOpenSettingsDomains = onOpenSettingsDomains,
            onOpenSettingsSources = onOpenSettingsSources,
            onOpenSettingsResearch = onOpenSettingsResearch,
        )
    }
}

@Composable
private fun AreaPulseCard(
    homeState: SingleHomeState,
    onOpenArea: (String) -> Unit,
) {
    val areas = effectiveStartAreas(homeState.lifeAreas)

    DaysTile(
        role = TileRole.Hero,
        modifier = Modifier.testTag("start-pulse-card"),
    ) {
        AreaPulseRow(
            areas = areas,
            onOpenArea = onOpenArea,
        )
    }
}

@Composable
private fun AreaPulseRow(
    areas: List<LifeArea>,
    onOpenArea: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        areas.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { area ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenArea(area.id) }
                            .testTag("start-area-${area.id}")
                            .semantics {
                                contentDescription = "${area.label}. Tippen fuer Bereichsdetails."
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.72f))
                                .border(1.dp, StartOutlineSoft.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = startAreaIcon(area.id),
                                    contentDescription = null,
                                    tint = StartMuted,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                        Text(
                            text = area.label,
                            modifier = Modifier.heightIn(min = 34.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Clip,
                        )
                    }
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BoxScope.StartToolDock(
    onOpenLifeWheel: () -> Unit,
    onOpenSettingsDomains: () -> Unit,
    onOpenSettingsSources: () -> Unit,
    onOpenSettingsResearch: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val plusRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "toolbox_plus_rotation",
    )
    val actions = listOf(
        StartDockAction("Fingerprint", Icons.Outlined.TrackChanges, onOpenLifeWheel, "start-toolbox-fingerprint"),
        StartDockAction("Domaenen", Icons.Outlined.Tune, onOpenSettingsDomains, "start-toolbox-domains"),
        StartDockAction("Quellen", Icons.Outlined.Sync, onOpenSettingsSources, "start-toolbox-sources"),
        StartDockAction("Research", Icons.Outlined.AutoAwesome, onOpenSettingsResearch, "start-toolbox-research"),
    )

    Row(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .navigationBarsPadding()
            .padding(start = 20.dp, bottom = 18.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.96f))
            .border(1.dp, StartOutlineSoft.copy(alpha = 0.42f), RoundedCornerShape(999.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .size(56.dp)
                .testTag("start-toolbox-toggle"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = AppTheme.colors.surfaceMuted,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = if (expanded) "Werkzeugkiste schliessen" else "Werkzeugkiste oeffnen",
                modifier = Modifier
                    .size(22.dp)
                    .rotate(plusRotation),
            )
        }
        actions.forEach { action ->
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(animationSpec = tween(220), expandFrom = Alignment.Start),
                exit = fadeOut(animationSpec = tween(120)) + shrinkHorizontally(animationSpec = tween(180), shrinkTowards = Alignment.Start),
            ) {
                FilledTonalIconButton(
                    onClick = {
                        expanded = false
                        action.onClick()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .testTag(action.testTag),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = AppTheme.colors.surfaceMuted,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private data class StartDockAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val testTag: String,
)

private fun effectiveStartAreas(
    areas: List<LifeArea>,
): List<LifeArea> {
    val configuredAreas = areas.associateBy(LifeArea::id)
    return defaultLifeAreas().map { defaultArea ->
        configuredAreas[defaultArea.id] ?: defaultArea
    }.take(16)
}
