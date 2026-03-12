package com.struperto.androidappdays.feature.start

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.defaultBehaviorClassForTemplate
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.ui.theme.AppTheme

private val StartMuted = Color(0xFF6D675C)
private val StartOutlineSoft = Color(0xFFCBC3B9)
private val StartStable = Color(0xFF5A8D64)
private val StartPull = Color(0xFFD97757)
private val StartLive = Color(0xFF6A89A8)
private const val DefaultCreateTemplateId = "free"
private const val DefaultCreateIconKey = "spark"

private enum class StartSurface {
    Overview,
    Create,
    CreateOptions,
}

data class CreateAreaDraft(
    val title: String,
    val meaning: String,
    val templateId: String,
    val iconKey: String,
    val behaviorClass: AreaBehaviorClass,
)

@Composable
fun StartScreen(
    state: StartOverviewState,
    onOpenSettings: () -> Unit,
    onOpenStart: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenMulti: () -> Unit,
    onOpenArea: (String) -> Unit,
    onDeleteArea: (String) -> Unit,
    onMoveAreaEarlier: (String) -> Unit,
    onMoveAreaLater: (String) -> Unit,
    onSwapAreas: (String, String) -> Unit,
    onCreateArea: (CreateAreaDraft) -> Unit,
    onUpdateArea: (String, CreateAreaDraft) -> Unit,
) {
    var activeSurface by rememberSaveable { mutableStateOf(StartSurface.Overview) }
    var createTitle by rememberSaveable { mutableStateOf("") }
    var createMeaning by rememberSaveable { mutableStateOf("") }
    var createTemplateId by rememberSaveable { mutableStateOf(DefaultCreateTemplateId) }
    var createIconKey by rememberSaveable { mutableStateOf(DefaultCreateIconKey) }
    var createBehaviorClass by rememberSaveable {
        mutableStateOf(defaultBehaviorClassForTemplate(DefaultCreateTemplateId))
    }
    val createDraft = CreateAreaDraft(
        title = createTitle,
        meaning = createMeaning,
        templateId = createTemplateId,
        iconKey = createIconKey,
        behaviorClass = createBehaviorClass,
    )
    val areas = startAreaTiles(state)

    fun resetCreateDraft() {
        createTitle = ""
        createMeaning = ""
        createTemplateId = DefaultCreateTemplateId
        createIconKey = DefaultCreateIconKey
        createBehaviorClass = defaultBehaviorClassForTemplate(DefaultCreateTemplateId)
    }

    when (activeSurface) {
        StartSurface.Create -> {
            StartCreateScreen(
                draft = createDraft,
                onBack = {
                    resetCreateDraft()
                    activeSurface = StartSurface.Overview
                },
                onTitleChange = { createTitle = it },
                onMeaningChange = { createMeaning = it },
                onBehaviorClassChange = { createBehaviorClass = it },
                onOpenIdentityOptions = { activeSurface = StartSurface.CreateOptions },
                onCreate = {
                    val templateSummary = startAreaTemplate(createTemplateId).summary
                    onCreateArea(
                        createDraft.copy(
                            title = createTitle.trim(),
                            meaning = createMeaning.trim().ifBlank { templateSummary },
                        ),
                    )
                    resetCreateDraft()
                    activeSurface = StartSurface.Overview
                },
            )
            return
        }

        StartSurface.CreateOptions -> {
            StartCreateIdentityOptionsScreen(
                draft = createDraft,
                onBack = { activeSurface = StartSurface.Create },
                onTemplateChange = { nextTemplateId ->
                    val currentDefault = startAreaTemplate(createTemplateId).defaultIconKey
                    createTemplateId = nextTemplateId
                    createBehaviorClass = defaultBehaviorClassForTemplate(nextTemplateId)
                    if (createIconKey == currentDefault || createIconKey.isBlank()) {
                        createIconKey = startAreaTemplate(nextTemplateId).defaultIconKey
                    }
                },
                onIconChange = { createIconKey = it },
                onResetToNeutral = {
                    createTemplateId = DefaultCreateTemplateId
                    createIconKey = DefaultCreateIconKey
                    createBehaviorClass = defaultBehaviorClassForTemplate(DefaultCreateTemplateId)
                },
            )
            return
        }

        StartSurface.Overview -> Unit
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
            )
            .testTag("start-root"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DaysModeTopBar(
                activeDestination = AppDestination.Start,
                onOpenSettings = onOpenSettings,
                onOpenStart = onOpenStart,
                onOpenSingle = onOpenHome,
                onOpenMulti = onOpenMulti,
                settingsTestTag = "start-open-settings",
                showMulti = true,
            )
            if (areas.isEmpty()) {
                EmptyStartState(
                    onCreate = {
                        resetCreateDraft()
                        activeSurface = StartSurface.Create
                    },
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 92.dp)
                        .widthIn(max = 980.dp)
                        .testTag("start-grid"),
                    contentPadding = PaddingValues(bottom = 112.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(
                        items = areas,
                        key = { it.areaId },
                    ) { area ->
                        StartAreaGridTile(
                            area = area,
                            onOpenArea = { onOpenArea(area.areaId) },
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                resetCreateDraft()
                activeSurface = StartSurface.Create
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 18.dp)
                .testTag("start-create-area-button"),
            containerColor = AppTheme.colors.surfaceStrong,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Bereich",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun EmptyStartState(
    onCreate: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .testTag("start-empty-state"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.97f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Start ist noch leer",
                style = MaterialTheme.typography.headlineSmall,
                color = AppTheme.colors.ink,
            )
            Text(
                text = "Lege den ersten Bereich an. Danach bleibt Start ein ruhiges 3-Spalten-Raster ohne Zusatzkarten.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.muted,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                ExtendedFloatingActionButton(
                    onClick = onCreate,
                    containerColor = AppTheme.colors.surfaceMuted,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Ersten Bereich anlegen",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StartAreaGridTile(
    area: StartAreaTile,
    onOpenArea: () -> Unit,
) {
    val shortStatus = area.summary.ifBlank { area.todayLabel }
    val attentionBadge = when (area.statusKind) {
        StartAreaStatusKind.Stable -> "OK"
        StartAreaStatusKind.Pull -> "Zug"
        StartAreaStatusKind.Live -> "Live"
        StartAreaStatusKind.Waiting -> "Neu"
    }
    val followHint = area.todayStepLabel
        .takeIf { it.isNotBlank() && !it.equals(shortStatus, ignoreCase = true) }
        ?.take(64)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onOpenArea)
            .testTag("start-area-${area.areaId}")
            .semantics {
                contentDescription = "${area.label}. ${area.statusLabel}. ${shortStatus.take(80)}."
            },
        colors = CardDefaults.cardColors(
            containerColor = area.containerColor.copy(alpha = 0.12f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = area.outlineColor.copy(alpha = 0.24f),
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.86f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = startAreaIcon(area.iconKey),
                        contentDescription = null,
                        tint = StartMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DaysMetaPill(label = attentionBadge)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = area.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.ink,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = shortStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                    Text(
                        text = attentionSentence(area),
                        style = MaterialTheme.typography.labelLarge,
                        color = area.outlineColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                if (followHint != null) {
                    Text(
                        text = followHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = AppTheme.colors.ink,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Bearbeiten",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.ink,
                        maxLines = 1,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppTheme.colors.ink,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun attentionSentence(
    area: StartAreaTile,
): String {
    area.todayLabel.takeIf(String::isNotBlank)?.let { return it }
    return when (area.statusLabel.lowercase()) {
        "stabil" -> "Braucht gerade wenig Aufmerksamkeit"
        "zieht" -> "Braucht gerade Aufmerksamkeit"
        "offen" -> "Braucht Konfiguration"
        "wartet" -> "Braucht einen ersten Zug"
        else -> "Status sichtbar halten"
    }
}

private fun startAreaTiles(
    state: StartOverviewState,
): List<StartAreaTile> {
    return state.areas.map { area ->
        StartAreaTile(
            areaId = area.areaId,
            label = area.label,
            summary = area.summary,
            todayLabel = area.todayLabel,
            todayStepLabel = area.todayStepLabel,
            templateId = area.templateId,
            iconKey = area.iconKey,
            outlineColor = startAreaStatusColor(area.statusKind),
            containerColor = startAreaStatusColor(area.statusKind),
            statusKind = area.statusKind,
            statusLabel = area.statusLabel,
            focusLabel = area.focusLabel,
            profileLabel = area.profileLabel,
            progress = area.progress,
            canMoveEarlier = area.canMoveEarlier,
            canMoveLater = area.canMoveLater,
        )
    }
}

private data class StartAreaTile(
    val areaId: String,
    val label: String,
    val summary: String,
    val todayLabel: String,
    val todayStepLabel: String,
    val templateId: String,
    val iconKey: String,
    val outlineColor: Color,
    val containerColor: Color,
    val statusKind: StartAreaStatusKind,
    val statusLabel: String,
    val focusLabel: String,
    val profileLabel: String,
    val progress: Float,
    val canMoveEarlier: Boolean,
    val canMoveLater: Boolean,
)

private fun startAreaStatusColor(
    kind: StartAreaStatusKind,
): Color {
    return when (kind) {
        StartAreaStatusKind.Waiting -> StartOutlineSoft
        StartAreaStatusKind.Live -> StartLive
        StartAreaStatusKind.Stable -> StartStable
        StartAreaStatusKind.Pull -> StartPull
    }
}
