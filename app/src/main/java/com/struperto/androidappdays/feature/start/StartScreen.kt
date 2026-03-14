package com.struperto.androidappdays.feature.start

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.defaultBehaviorClassForTemplate
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val DefaultCreateTemplateId = "free"
private const val DefaultCreateIconKey = "spark"

private enum class StartSurface {
    Overview,
    CreateCapture,
    CreateAnalyze,
    CreateSources,
    CreateSourceSetup,
    CreateQuestion,
    CreateBlocker,
    CreatePreview,
}

data class CreateAreaDraft(
    val title: String,
    val meaning: String,
    val templateId: String,
    val iconKey: String,
    val behaviorClass: AreaBehaviorClass,
    val sourceKind: DataSourceKind? = null,
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
    onCreateArea: (CreateAreaDraft, (String) -> Unit) -> Unit,
    onUpdateArea: (String, CreateAreaDraft) -> Unit,
    onAttachPendingImport: (String, String) -> Unit,
    onDismissPendingImport: (String) -> Unit,
) {
    var activeSurface by rememberSaveable { mutableStateOf(StartSurface.Overview) }
    var createInputText by rememberSaveable { mutableStateOf("") }
    var createTitle by rememberSaveable { mutableStateOf("") }
    var createMeaning by rememberSaveable { mutableStateOf("") }
    var createTemplateId by rememberSaveable { mutableStateOf(DefaultCreateTemplateId) }
    var createIconKey by rememberSaveable { mutableStateOf(DefaultCreateIconKey) }
    var createBehaviorClass by rememberSaveable {
        mutableStateOf(defaultBehaviorClassForTemplate(DefaultCreateTemplateId))
    }
    var createSourceKind by rememberSaveable { mutableStateOf<DataSourceKind?>(null) }
    var createTitleEdited by rememberSaveable { mutableStateOf(false) }
    var createMeaningEdited by rememberSaveable { mutableStateOf(false) }
    var createSourceEdited by rememberSaveable { mutableStateOf(false) }
    var lastSuggestedTitle by rememberSaveable { mutableStateOf("") }
    var lastSuggestedMeaning by rememberSaveable { mutableStateOf("") }
    var lastSuggestedSourceName by rememberSaveable { mutableStateOf("") }
    var pendingImportToAttach by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingImportToAssign by rememberSaveable { mutableStateOf<String?>(null) }
    var createAnalysis by remember { mutableStateOf<StartIntentAnalysis?>(null) }
    var createSelectedSourceIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    var createSourceSetupNotes by rememberSaveable { mutableStateOf(mapOf<String, String>()) }
    var createSourceSetupIndex by rememberSaveable { mutableStateOf(0) }
    val createDraft = CreateAreaDraft(
        title = createTitle,
        meaning = createMeaning,
        templateId = createTemplateId,
        iconKey = createIconKey,
        behaviorClass = createBehaviorClass,
        sourceKind = createSourceKind,
    )
    val areas = startAreaTiles(state)
    val pendingImports = state.pendingImports
    val limitReached = state.activeAreaCount >= state.maxActiveAreaCount

    fun resetCreateDraft() {
        createInputText = ""
        createTitle = ""
        createMeaning = ""
        createTemplateId = DefaultCreateTemplateId
        createIconKey = DefaultCreateIconKey
        createBehaviorClass = defaultBehaviorClassForTemplate(DefaultCreateTemplateId)
        createSourceKind = null
        createTitleEdited = false
        createMeaningEdited = false
        createSourceEdited = false
        lastSuggestedTitle = ""
        lastSuggestedMeaning = ""
        lastSuggestedSourceName = ""
        createAnalysis = null
        pendingImportToAttach = null
        createSelectedSourceIds = emptyList()
        createSourceSetupNotes = emptyMap()
        createSourceSetupIndex = 0
    }

    fun applySuggestedDraft(
        suggestion: CreateAreaDraft,
        force: Boolean,
    ) {
        val previousSuggestedTitle = lastSuggestedTitle
        val previousSuggestedMeaning = lastSuggestedMeaning
        val previousSuggestedSourceName = lastSuggestedSourceName

        if (force || !createTitleEdited || createTitle.isBlank() || createTitle == previousSuggestedTitle) {
            createTitle = suggestion.title
        }
        if (force || !createMeaningEdited || createMeaning.isBlank() || createMeaning == previousSuggestedMeaning) {
            createMeaning = suggestion.meaning
        }
        createTemplateId = suggestion.templateId
        createIconKey = suggestion.iconKey
        createBehaviorClass = suggestion.behaviorClass
        if (force || !createSourceEdited || createSourceKind?.name == previousSuggestedSourceName) {
            createSourceKind = suggestion.sourceKind
        }

        lastSuggestedTitle = suggestion.title
        lastSuggestedMeaning = suggestion.meaning
        lastSuggestedSourceName = suggestion.sourceKind?.name.orEmpty()
    }

    fun beginCreateFromImport(item: StartPendingImportState) {
        resetCreateDraft()
        createInputText = item.suggestedInput
        pendingImportToAttach = item.id
        applySuggestedDraft(
            suggestion = buildPrimaryCreateDraft(item.suggestedInput),
            force = true,
        )
        activeSurface = StartSurface.CreateAnalyze
    }

    when (activeSurface) {
        StartSurface.CreateCapture -> {
            StartCreateScreen(
                inputText = createInputText,
                limitReached = limitReached,
                activeAreaCount = state.activeAreaCount,
                maxActiveAreaCount = state.maxActiveAreaCount,
                onBack = {
                    resetCreateDraft()
                    activeSurface = StartSurface.Overview
                },
                onInputTextChange = {
                    createInputText = it
                    createTitleEdited = false
                    createMeaningEdited = false
                    createSourceEdited = false
                },
                onContinue = {
                    applySuggestedDraft(
                        suggestion = buildPrimaryCreateDraft(createInputText),
                        force = true,
                    )
                    createAnalysis = null
                    activeSurface = StartSurface.CreateAnalyze
                },
            )
            return
        }

        StartSurface.CreateAnalyze -> {
            StartCreateAnalyzeScreen(
                inputText = createInputText,
                capabilityProfile = state.capabilityProfile,
                analysisResult = createAnalysis,
                onBack = { activeSurface = StartSurface.CreateCapture },
                onAnalysisReady = { analysis ->
                    createAnalysis = analysis
                    applySuggestedDraft(
                        suggestion = analysis.suggestedDraft(),
                        force = false,
                    )
                },
                onContinue = {
                    createAnalysis?.let { analysis ->
                        activeSurface = when {
                            !analysis.canCreate -> StartSurface.CreateBlocker
                            analysis.toSourceChoices().isNotEmpty() -> StartSurface.CreateSources
                            analysis.followUpQuestion != null && analysis.followUpOptions.isNotEmpty() -> StartSurface.CreateQuestion
                            else -> StartSurface.CreatePreview
                        }
                    }
                },
            )
            return
        }

        StartSurface.CreateSources -> {
            val analysis = createAnalysis ?: run {
                activeSurface = StartSurface.CreateAnalyze
                return
            }
            val choices = analysis.toSourceChoices()
            StartCreateSourcesScreen(
                choices = choices,
                selectedIds = createSelectedSourceIds.toSet(),
                onBack = { activeSurface = StartSurface.CreateAnalyze },
                onToggle = { choiceId ->
                    createSelectedSourceIds = if (choiceId in createSelectedSourceIds) {
                        createSelectedSourceIds.filterNot { it == choiceId }
                    } else {
                        createSelectedSourceIds + choiceId
                    }
                },
                onContinue = {
                    createSourceSetupIndex = 0
                    activeSurface = if (createSelectedSourceIds.isEmpty()) {
                        StartSurface.CreatePreview
                    } else {
                        StartSurface.CreateSourceSetup
                    }
                },
            )
            return
        }

        StartSurface.CreateSourceSetup -> {
            val analysis = createAnalysis ?: run {
                activeSurface = StartSurface.CreateAnalyze
                return
            }
            val choicesById = analysis.toSourceChoices().associateBy { it.id }
            val orderedSelectedChoices = createSelectedSourceIds.mapNotNull { choicesById[it] }
            val currentChoice = orderedSelectedChoices.getOrNull(createSourceSetupIndex) ?: run {
                activeSurface = StartSurface.CreatePreview
                return
            }
            StartCreateSourceSetupScreen(
                choice = currentChoice,
                value = createSourceSetupNotes[currentChoice.id].orEmpty(),
                isLast = createSourceSetupIndex >= orderedSelectedChoices.lastIndex,
                onBack = {
                    activeSurface = if (createSourceSetupIndex == 0) {
                        StartSurface.CreateSources
                    } else {
                        createSourceSetupIndex -= 1
                        StartSurface.CreateSourceSetup
                    }
                },
                onValueChange = { next ->
                    createSourceSetupNotes = createSourceSetupNotes + (currentChoice.id to next)
                },
                onContinue = {
                    if (createSourceSetupIndex >= orderedSelectedChoices.lastIndex) {
                        activeSurface = StartSurface.CreatePreview
                    } else {
                        createSourceSetupIndex += 1
                    }
                },
            )
            return
        }

        StartSurface.CreateQuestion -> {
            val analysis = createAnalysis ?: run {
                activeSurface = StartSurface.CreateAnalyze
                return
            }
            StartCreateQuestionScreen(
                draft = createDraft,
                analysis = analysis,
                onBack = { activeSurface = StartSurface.CreateAnalyze },
                onApplySuggestion = { suggestion ->
                    applySuggestedDraft(suggestion, force = true)
                },
                onContinue = { activeSurface = StartSurface.CreatePreview },
            )
            return
        }

        StartSurface.CreateBlocker -> {
            val analysis = createAnalysis ?: run {
                activeSurface = StartSurface.CreateAnalyze
                return
            }
            StartCreateBlockerScreen(
                analysis = analysis,
                onBack = { activeSurface = StartSurface.CreateAnalyze },
                onResolve = { repair ->
                    createInputText = repair.replacementInput
                    createTitleEdited = false
                    createMeaningEdited = false
                    createSourceEdited = false
                    activeSurface = StartSurface.CreateAnalyze
                },
            )
            return
        }

        StartSurface.CreatePreview -> {
            val analysis = createAnalysis ?: run {
                activeSurface = StartSurface.CreateAnalyze
                return
            }
            StartCreatePreviewScreen(
                draft = createDraft,
                analysis = analysis,
                activeAreaCount = state.activeAreaCount,
                maxActiveAreaCount = state.maxActiveAreaCount,
                onBack = {
                    activeSurface = if (createSelectedSourceIds.isNotEmpty()) {
                        StartSurface.CreateSourceSetup
                    } else if (analysis.toSourceChoices().isNotEmpty()) {
                        StartSurface.CreateSources
                    } else if (analysis.followUpQuestion != null && analysis.followUpOptions.isNotEmpty()) {
                        StartSurface.CreateQuestion
                    } else {
                        StartSurface.CreateAnalyze
                    }
                },
                onTitleChange = {
                    createTitleEdited = true
                    createTitle = it
                },
                onMeaningChange = {
                    createMeaningEdited = true
                    createMeaning = it
                },
                onSourceKindChange = {
                    createSourceEdited = true
                    createSourceKind = it
                },
                onApplySuggestion = { suggestion -> applySuggestedDraft(suggestion, force = true) },
                onCreate = {
                    val templateSummary = startAreaTemplate(createTemplateId).summary
                    val importId = pendingImportToAttach
                    onCreateArea(
                        createDraft.copy(
                            title = createTitle.trim(),
                            meaning = createMeaning.trim().ifBlank { templateSummary },
                        ),
                    ) { areaId ->
                        importId?.let { onAttachPendingImport(it, areaId) }
                    }
                    resetCreateDraft()
                    activeSurface = StartSurface.Overview
                },
            )
            return
        }

        StartSurface.Overview -> Unit
    }

    val importTarget = pendingImports.firstOrNull { it.id == pendingImportToAssign }

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
            if (limitReached) {
                AreaLimitNotice(
                    activeAreaCount = state.activeAreaCount,
                    maxActiveAreaCount = state.maxActiveAreaCount,
                )
            }
            if (pendingImports.isNotEmpty()) {
                PendingImportsCard(
                    imports = pendingImports,
                    hasAreas = areas.isNotEmpty(),
                    limitReached = limitReached,
                    onCreateAreaFromImport = ::beginCreateFromImport,
                    onAssignImport = { importId -> pendingImportToAssign = importId },
                    onDismissImport = onDismissPendingImport,
                )
            }
            if (areas.isEmpty()) {
                EmptyStartState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .widthIn(max = 760.dp),
                    limitReached = limitReached,
                    activeAreaCount = state.activeAreaCount,
                    maxActiveAreaCount = state.maxActiveAreaCount,
                    onCreate = {
                        if (limitReached) return@EmptyStartState
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

        if (importTarget != null) {
            AlertDialog(
                onDismissRequest = { pendingImportToAssign = null },
                title = { Text("Wohin soll das Material?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = importTarget.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        areas.forEach { area ->
                            TextButton(
                                onClick = {
                                    onAttachPendingImport(importTarget.id, area.areaId)
                                    pendingImportToAssign = null
                                    onOpenArea(area.areaId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("start-pending-import-target-${area.areaId}"),
                            ) {
                                Text(area.label)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { pendingImportToAssign = null }) {
                        Text("Abbrechen")
                    }
                },
            )
        }

        if (areas.isNotEmpty() && !limitReached) {
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
}

@Composable
private fun AreaLimitNotice(
    activeAreaCount: Int,
    maxActiveAreaCount: Int,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceMuted.copy(alpha = 0.94f),
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Bereichslimit erreicht",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$activeAreaCount von $maxActiveAreaCount Bereichen sind aktiv. Loeschen geht in Einstellungen > Bereiche.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PendingImportsCard(
    imports: List<StartPendingImportState>,
    hasAreas: Boolean,
    limitReached: Boolean,
    onCreateAreaFromImport: (StartPendingImportState) -> Unit,
    onAssignImport: (String) -> Unit,
    onDismissImport: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .testTag("start-pending-imports-card"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DaysMetaPill(label = "Neu geteilt")
            imports.take(3).forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.42f))
                        .padding(14.dp)
                        .testTag("start-pending-import-$index"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = item.kind.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            onClick = { onCreateAreaFromImport(item) },
                            enabled = !limitReached,
                            modifier = Modifier.testTag("start-pending-import-create-$index"),
                        ) {
                            Text("Neuer Bereich")
                        }
                        if (hasAreas) {
                            TextButton(
                                onClick = { onAssignImport(item.id) },
                                modifier = Modifier.testTag("start-pending-import-assign-$index"),
                            ) {
                                Text("Zu Bereich")
                            }
                        }
                        TextButton(
                            onClick = { onDismissImport(item.id) },
                            modifier = Modifier.testTag("start-pending-import-dismiss-$index"),
                        ) {
                            Text("Spaeter")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStartState(
    modifier: Modifier = Modifier,
    limitReached: Boolean,
    activeAreaCount: Int,
    maxActiveAreaCount: Int,
    onCreate: () -> Unit,
) {
    val showcases = remember { startEmptyShowcases() }
    var activeIndex by rememberSaveable { mutableStateOf(0) }
    var autoPlay by rememberSaveable { mutableStateOf(true) }
    val activeShowcase = showcases[activeIndex]
    val containerColor by animateColorAsState(
        targetValue = activeShowcase.containerColor,
        animationSpec = tween(durationMillis = 700),
        label = "start-empty-container",
    )
    val accentColor by animateColorAsState(
        targetValue = activeShowcase.accentColor,
        animationSpec = tween(durationMillis = 700),
        label = "start-empty-accent",
    )

    LaunchedEffect(autoPlay, activeIndex, showcases.size) {
        if (!autoPlay) return@LaunchedEffect
        delay(2_000)
        activeIndex = (activeIndex + 1) % showcases.size
    }

    Card(
        modifier = modifier.testTag("start-empty-state"),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.05f),
                            activeShowcase.haloColor.copy(alpha = 0.2f),
                        ),
                        start = Offset.Zero,
                        end = Offset(840f, 1280f),
                    ),
                )
                .padding(24.dp),
        ) {
            EmptyStartBackdrop(
                modifier = Modifier.fillMaxSize(),
                accentColor = accentColor,
                haloColor = activeShowcase.haloColor,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DaysMetaPill(label = "Noch kein Bereich")
                    IconButton(onClick = { autoPlay = !autoPlay }) {
                        Icon(
                            imageVector = if (autoPlay) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                            contentDescription = if (autoPlay) "Rotation pausieren" else "Rotation starten",
                            tint = accentColor,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = activeShowcase,
                        transitionSpec = { startEmptyContentTransition() },
                        label = "start-empty-showcase",
                    ) { showcase ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(showcase.accentColor.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = showcase.accentColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Text(
                                    text = showcase.label.uppercase(),
                                    style = AppTheme.typography.label,
                                    color = showcase.accentColor,
                                )
                            }
                            Text(
                                text = showcase.statement,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    showcases.forEachIndexed { index, _ ->
                        val barColor by animateColorAsState(
                            targetValue = if (index == activeIndex) accentColor else Color.White.copy(alpha = 0.18f),
                            animationSpec = tween(durationMillis = 250),
                            label = "start-empty-bar-$index",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(barColor)
                                .clickable {
                                    autoPlay = false
                                    activeIndex = index
                                },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(enabled = !limitReached) {
                            autoPlay = false
                            onCreate()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (limitReached) {
                            "Bereiche voll ($activeAreaCount/$maxActiveAreaCount)"
                        } else {
                            "Ersten Bereich entwerfen"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = accentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStartBackdrop(
    modifier: Modifier = Modifier,
    accentColor: Color,
    haloColor: Color,
) {
    val transition = rememberInfiniteTransition(label = "start-empty-visual")
    val orbitRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "start-empty-orbit",
    )
    val innerRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "start-empty-inner-orbit",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "start-empty-pulse",
    )

    Canvas(modifier = modifier) {
        val anchor = Offset(size.width * 0.8f, size.height * 0.22f)
        val outerRadius = size.minDimension * 0.28f
        val innerRadius = outerRadius * 0.64f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    haloColor.copy(alpha = 0.44f * pulse),
                    accentColor.copy(alpha = 0.18f),
                    Color.Transparent,
                ),
                center = anchor,
                radius = outerRadius * 1.4f,
            ),
            radius = outerRadius * 1.4f,
            center = anchor,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = outerRadius,
            center = anchor,
            style = Stroke(width = 3.dp.toPx()),
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.22f),
            radius = innerRadius,
            center = anchor,
            style = Stroke(width = 2.dp.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 14.dp.toPx(),
            center = anchor,
        )
        drawCircle(
            color = accentColor,
            radius = 7.dp.toPx(),
            center = anchor,
        )

        drawStartOrbiter(anchor, outerRadius, orbitRotation, 9.dp.toPx(), accentColor.copy(alpha = 0.94f))
        drawStartOrbiter(anchor, innerRadius, innerRotation + 76f, 6.dp.toPx(), Color.White.copy(alpha = 0.76f))
        drawStartOrbiter(anchor, outerRadius * 1.12f, innerRotation + 214f, 5.dp.toPx(), haloColor.copy(alpha = 0.88f))

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.22f),
                    Color.Transparent,
                ),
            ),
            topLeft = Offset(0f, size.height * 0.6f),
            size = Size(size.width * 0.58f, size.height * 0.16f),
            cornerRadius = CornerRadius(36.dp.toPx()),
        )
        drawLine(
            color = lerp(accentColor, Color.White, 0.38f).copy(alpha = 0.3f),
            start = Offset(size.width * 0.14f, size.height * 0.78f),
            end = Offset(size.width * 0.52f, size.height * 0.56f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStartOrbiter(
    center: Offset,
    orbitRadius: Float,
    angleDegrees: Float,
    dotRadius: Float,
    color: Color,
) {
    val radians = Math.toRadians(angleDegrees.toDouble())
    val point = Offset(
        x = center.x + (cos(radians) * orbitRadius).toFloat(),
        y = center.y + (sin(radians) * orbitRadius).toFloat(),
    )
    drawCircle(
        color = color,
        radius = dotRadius,
        center = point,
    )
}

private fun startEmptyContentTransition(): ContentTransform {
    return (fadeIn(animationSpec = tween(420)) + slideInVertically(
        animationSpec = tween(420),
        initialOffsetY = { it / 4 },
    )).togetherWith(
        fadeOut(animationSpec = tween(280)) + slideOutVertically(
            animationSpec = tween(280),
            targetOffsetY = { -it / 6 },
        ),
    )
}

private data class StartEmptyShowcase(
    val label: String,
    val statement: String,
    val containerColor: Color,
    val accentColor: Color,
    val haloColor: Color,
)

private fun startEmptyShowcases(): List<StartEmptyShowcase> {
    return listOf(
        StartEmptyShowcase(
            label = "Aus Signalen wird Fokus",
            statement = "Days zieht Termine, Energie, Notizen und offene Zuege in einen Bereich, der dir heute den naechsten wirksamen Schritt zeigt.",
            containerColor = Color(0xFF101A2B),
            accentColor = Color(0xFF82B8FF),
            haloColor = Color(0xFF5BE1C5),
        ),
        StartEmptyShowcase(
            label = "Kalender mit Richtung",
            statement = "Days macht aus einem vollen Tag kein Rauschen, sondern eine klare Richtung mit Prioritaet, Material und passendem Takt.",
            containerColor = Color(0xFF1B1827),
            accentColor = Color(0xFFFFB067),
            haloColor = Color(0xFFE9698D),
        ),
        StartEmptyShowcase(
            label = "Fruehe Kipppunkte",
            statement = "Days verbindet Quellen frueh genug, damit du erkennst, was kippt, und aus Reaktion wieder Gestaltung wird.",
            containerColor = Color(0xFF11211D),
            accentColor = Color(0xFF7AE7A2),
            haloColor = Color(0xFFC4F06B),
        ),
        StartEmptyShowcase(
            label = "Fortschritt mit Zugkraft",
            statement = "Days buendelt Aufgaben, Routinen und Impulse so, dass aus losem Aufwand ein Bereich mit stabilem Fortschritt wird.",
            containerColor = Color(0xFF25141B),
            accentColor = Color(0xFFFF8B94),
            haloColor = Color(0xFFFFD36E),
        ),
        StartEmptyShowcase(
            label = "Material wird Hebel",
            statement = "Days verheiratet Material, Lage, Richtung und Flow zu einer Arbeitsflaeche, die aus Daten konkrete Hebel fuer heute macht.",
            containerColor = Color(0xFF171C11),
            accentColor = Color(0xFFFFD86B),
            haloColor = Color(0xFF99D8FF),
        ),
    )
}

private data class StartWorkbenchFlowStep(
    val number: String,
    val title: String,
    val detail: String,
    val badge: String,
)

@Composable
private fun StartWorkbenchFlowCard(
    title: String,
    detail: String,
    steps: List<StartWorkbenchFlowStep>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
        steps.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.surface.copy(alpha = 0.86f))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.accentSoft.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = step.number,
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.ink,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = AppTheme.colors.ink,
                    )
                    Text(
                        text = step.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
                DaysMetaPill(label = step.badge)
            }
        }
    }
}

@Composable
private fun StartReferenceStripCard(
    items: List<StartWorkbenchFlowStep>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.42f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Referenzbereiche",
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.ink,
        )
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DaysMetaPill(label = item.badge)
            }
        }
    }
}

private fun startEmptyFlowSteps(): List<StartWorkbenchFlowStep> {
    return listOf(
        StartWorkbenchFlowStep(
            number = "1",
            title = "Bereich beschreiben",
            detail = "Ein freier Satz reicht. Days leitet daraus Ziel, moegliche Quelle und erste Arbeitsflaeche ab.",
            badge = "Freitext",
        ),
        StartWorkbenchFlowStep(
            number = "2",
            title = "Analyse und Blocker",
            detail = "Pflichtanalyse prueft Signalpfade, offene Fragen und zeigt bei Bedarf sofort einen Loesung-finden-Pfad.",
            badge = "Pflicht",
        ),
        StartWorkbenchFlowStep(
            number = "3",
            title = "Vorschau fuer Single",
            detail = "Vor dem Speichern siehst du vier Beispielkacheln fuer den spaeteren Bereichsfeed und waehlst deinen Favoriten.",
            badge = "4 Kacheln",
        ),
    )
}

private fun startReferenceAreaSteps(): List<StartWorkbenchFlowStep> {
    return listOf(
        StartWorkbenchFlowStep(
            number = "A",
            title = "News",
            detail = "Posts, Links und Quellen werden zu einer ruhigen Leseliste statt zu einem lauten Strom.",
            badge = "Radar",
        ),
        StartWorkbenchFlowStep(
            number = "B",
            title = "Freunde",
            detail = "Unbeantwortete Nachrichten, Signale und kleine Rueckwege werden als klarer Kachelstrom sichtbar.",
            badge = "Kontakt",
        ),
        StartWorkbenchFlowStep(
            number = "C",
            title = "App-Bau",
            detail = "Mails, Notizen, Issues und Fundstuecke landen in einem Bereich, der heute den naechsten Zug zeigt.",
            badge = "Studio",
        ),
    )
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
        family = area.family,
        hint = area.primaryHint,
        todayLabel = area.todayLabel,
        summary = area.summary,
        statusLabel = area.statusLabel,
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
            statusLabel = area.statusLabel,
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
    val statusLabel: String,
)
