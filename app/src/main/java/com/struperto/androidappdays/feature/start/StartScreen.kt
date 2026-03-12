package com.struperto.androidappdays.feature.start

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.AutoAwesome
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
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaSkillKind
import com.struperto.androidappdays.domain.area.TileDisplayMode
import com.struperto.androidappdays.domain.area.defaultBehaviorClassForTemplate
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.ui.theme.AppTheme

private const val DefaultCreateTemplateId = "free"
private const val DefaultCreateIconKey = "spark"

private enum class StartSurface {
    Overview,
    CreateCapture,
    CreateConfirm,
    CreateOptions,
}

data class CreateAreaDraft(
    val title: String,
    val meaning: String,
    val templateId: String,
    val iconKey: String,
    val behaviorClass: AreaBehaviorClass,
    val sourceKind: DataSourceKind? = null,
    val selectedSkills: Set<AreaSkillKind> = emptySet(),
    val tileDisplayMode: TileDisplayMode = TileDisplayMode.AMPEL,
    val familyKey: String = "",
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
    var createInputKind by rememberSaveable { mutableStateOf(StartCreateInputKind.Text) }
    var createInputText by rememberSaveable { mutableStateOf("") }
    var selectedSuggestionId by rememberSaveable { mutableStateOf<String?>(null) }
    var createTitle by rememberSaveable { mutableStateOf("") }
    var createMeaning by rememberSaveable { mutableStateOf("") }
    var createTemplateId by rememberSaveable { mutableStateOf(DefaultCreateTemplateId) }
    var createIconKey by rememberSaveable { mutableStateOf(DefaultCreateIconKey) }
    var createBehaviorClass by rememberSaveable {
        mutableStateOf(defaultBehaviorClassForTemplate(DefaultCreateTemplateId))
    }
    var createSourceKind by rememberSaveable { mutableStateOf<DataSourceKind?>(null) }
    var createSelectedSkills by rememberSaveable { mutableStateOf(emptySet<AreaSkillKind>()) }
    val createDraft = CreateAreaDraft(
        title = createTitle,
        meaning = createMeaning,
        templateId = createTemplateId,
        iconKey = createIconKey,
        behaviorClass = createBehaviorClass,
        sourceKind = createSourceKind,
        selectedSkills = createSelectedSkills,
    )
    val areas = startAreaTiles(state)
    val suggestions = buildStartIntentSuggestions(
        inputKind = createInputKind,
        rawInput = createInputText,
    )

    fun resetCreateDraft() {
        createInputKind = StartCreateInputKind.Text
        createInputText = ""
        selectedSuggestionId = null
        createTitle = ""
        createMeaning = ""
        createTemplateId = DefaultCreateTemplateId
        createIconKey = DefaultCreateIconKey
        createBehaviorClass = defaultBehaviorClassForTemplate(DefaultCreateTemplateId)
        createSourceKind = null
        createSelectedSkills = emptySet()
    }

    fun applySuggestion(suggestion: StartIntentSuggestion) {
        createTitle = suggestion.title
        createMeaning = suggestion.summary
        createTemplateId = suggestion.templateId
        createIconKey = suggestion.iconKey
        createBehaviorClass = suggestion.behaviorClass
        createSourceKind = suggestion.sourceKind
        createSelectedSkills = suggestion.skills
        selectedSuggestionId = suggestion.id
    }

    when (activeSurface) {
        StartSurface.CreateCapture -> {
            StartCreateScreen(
                inputKind = createInputKind,
                inputText = createInputText,
                suggestions = suggestions,
                selectedSuggestionId = selectedSuggestionId,
                onBack = {
                    resetCreateDraft()
                    activeSurface = StartSurface.Overview
                },
                onInputKindChange = {
                    createInputKind = it
                    selectedSuggestionId = null
                },
                onInputTextChange = { createInputText = it },
                onSuggestionSelect = { selectedSuggestionId = it },
                onContinue = {
                    val suggestion = suggestions.firstOrNull { it.id == selectedSuggestionId }
                        ?: suggestions.firstOrNull()
                    if (suggestion != null) {
                        applySuggestion(suggestion)
                        activeSurface = StartSurface.CreateConfirm
                    }
                },
            )
            return
        }

        StartSurface.CreateConfirm -> {
            StartCreateConfirmScreen(
                draft = createDraft,
                inputKind = createInputKind,
                inputText = createInputText,
                selectedSuggestion = suggestions.firstOrNull { it.id == selectedSuggestionId }
                    ?: suggestions.firstOrNull(),
                onBack = { activeSurface = StartSurface.CreateCapture },
                onTitleChange = { createTitle = it },
                onMeaningChange = { createMeaning = it },
                onBehaviorClassChange = { createBehaviorClass = it },
                onSourceKindChange = { createSourceKind = it },
                onSkillToggle = { skill ->
                    createSelectedSkills = if (skill in createSelectedSkills) {
                        createSelectedSkills - skill
                    } else {
                        createSelectedSkills + skill
                    }
                },
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
                onBack = { activeSurface = StartSurface.CreateConfirm },
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
                        activeSurface = StartSurface.CreateCapture
                    },
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 172.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 88.dp)
                        .widthIn(max = 980.dp)
                        .testTag("start-grid"),
                    contentPadding = PaddingValues(bottom = 120.dp),
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
                activeSurface = StartSurface.CreateCapture
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
    val transition = rememberInfiniteTransition(label = "start-empty-visual")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "start-empty-drift",
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .testTag("start-empty-state"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = (drift * 10).dp, end = ((1f - drift) * 10).dp)
                    .size(84.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(AppTheme.colors.accentSoft.copy(alpha = 0.42f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = (18 + (1f - drift) * 14).dp, start = (drift * 8).dp)
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.info.copy(alpha = 0.14f)),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    AppTheme.colors.accentSoft.copy(alpha = 0.56f),
                                    AppTheme.colors.surfaceMuted.copy(alpha = 0.78f),
                                    AppTheme.colors.surfaceStrong,
                                ),
                            ),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(20.dp)
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = AppTheme.colors.ink,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Noch kein Bereich angelegt.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AppTheme.colors.ink,
                        )
                        Text(
                            text = "Erstelle deinen ersten Bereich. Beschreibe kurz, was du im Blick behalten willst — Days schlaegt passende Einstellungen vor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.muted,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Text", "Link", "Bild", "App", "Kontakt", "Ort").forEach { label ->
                        DaysMetaPill(label = label)
                    }
                }
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
                            text = "Bereich erstellen",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
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
    val visual = startAreaUiVisuals(
        family = area.family,
        hintTone = area.primaryHint.tone,
    )
    val shortStatus = startAreaTileStatusLine(
        hint = area.primaryHint,
        todayLabel = area.todayLabel,
        summary = area.summary,
    )
    val actionLabel = startAreaTileActionLabel(
        family = area.family,
        primaryHint = area.primaryHint,
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onOpenArea)
            .testTag("start-area-${area.areaId}")
            .semantics {
                contentDescription = "${area.label}. ${area.family.shortLabel}. ${area.primaryHint.compactLabel}. ${shortStatus.take(80)}."
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = visual.outline.copy(alpha = 0.24f),
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = visual.background,
                    ),
                )
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
                        .background(visual.chrome),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = startAreaIcon(area.iconKey),
                        contentDescription = null,
                        tint = visual.iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = area.family.shortLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = visual.signal,
                    maxLines = 1,
                )
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
                        tint = visual.iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.ink,
                        maxLines = 1,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = visual.iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
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
            family = area.family,
            todayLabel = area.todayLabel,
            todayStepLabel = area.todayStepLabel,
            templateId = area.templateId,
            iconKey = area.iconKey,
            primaryHint = area.primaryHint,
        )
    }
}

private data class StartAreaTile(
    val areaId: String,
    val label: String,
    val summary: String,
    val family: StartAreaFamily,
    val todayLabel: String,
    val todayStepLabel: String,
    val templateId: String,
    val iconKey: String,
    val primaryHint: StartAreaHintState,
)
