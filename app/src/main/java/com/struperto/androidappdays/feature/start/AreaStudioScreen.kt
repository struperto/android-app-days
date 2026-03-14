package com.struperto.androidappdays.feature.start

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaAuthoringAxis
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaSourcesMode
import com.struperto.androidappdays.domain.area.AreaSourceSetupStatus
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.feature.single.shared.DaysTopBarAction
import com.struperto.androidappdays.ui.theme.AppTheme

private enum class AreaStudioSurface {
    Overview,
    Inputs,
    TextPreview,
    Identity,
    Authoring,
    ImportQuestion,
    Analysis,
    Diagnostics,
}

private enum class AreaInputsSurface {
    Overview,
    Source,
    LinkEntry,
    FeedEntry,
    Material,
    MaterialCollect,
    MaterialSources,
    MaterialInventory,
    MaterialAutomation,
    Processing,
}

@Composable
fun AreaStudioScreen(
    state: AreaStudioUiState,
    areaId: String,
    onBack: () -> Unit,
    onOpenSourceSettings: () -> Unit,
    onRefreshAnalysis: (String, String) -> Unit,
    onTargetScoreChange: (String, Float) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
    onManualStateChange: (String, String?) -> Unit,
    onClearSnapshot: (String) -> Unit,
    onUpdateIdentity: (String, String, String, String, String) -> Unit,
    onBindSource: (String, DataSourceKind) -> Unit,
    onUnbindSource: (String, DataSourceKind) -> Unit,
    onImportLink: (String, String) -> Unit,
    onAnswerImportQuestion: (String, String) -> Unit,
    onImportMaterials: (String, List<AreaImportDraft>) -> Unit,
    onImportImage: (String, String, String) -> Unit,
    onAddWebFeedSource: (String, String) -> Unit,
    onSyncWebFeed: (String) -> Unit,
    onRemoveWebFeedSource: (String, String) -> Unit,
    onSetWebFeedAutoSync: (String, String, Boolean) -> Unit,
    onSetWebFeedSyncCadence: (String, String, AreaWebFeedSyncCadence) -> Unit,
    onRemoveImportedMaterial: (String) -> Unit,
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
    val areaState = state.areas[areaId]
    val context = LocalContext.current
    if (areaState == null) {
        DaysPageScaffold(
            title = "Bereich wird vorbereitet",
            onBack = onBack,
            modifier = Modifier.testTag("area-studio-screen"),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("area-studio-loading"),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
                ),
                shape = RoundedCornerShape(28.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DaysMetaPill(label = "Wird geladen")
                    Text(
                        text = "Ich richte diesen Bereich gerade ein.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        return
    }
    val area = areaState.detail
    val authoring = areaState.authoring
    val sourceSetup = areaState.sourceSetup
    val importedMaterials = areaState.importedMaterials
    val analysis = areaState.analysis
    var activeSurface by rememberSaveable(area.areaId) { mutableStateOf(AreaStudioSurface.Overview) }
    var activePanel by rememberSaveable(area.areaId) { mutableStateOf<StartAreaPanel?>(null) }
    var importPreviewText by rememberSaveable(area.areaId) { mutableStateOf<String?>(null) }
    var importQuestion by remember(area.areaId) { mutableStateOf<AreaQuickQuestionState?>(null) }
    val analysisAction = DaysTopBarAction(
        icon = Icons.Outlined.AutoAwesome,
        contentDescription = "Bereichsanalyse",
        onClick = { activeSurface = AreaStudioSurface.Analysis },
    )

    LaunchedEffect(
        area.areaId,
        sourceSetup?.status,
        sourceSetup?.headline,
        importedMaterials.size,
        webFeedSyncKey(areaState.webFeedSync),
    ) {
        onRefreshAnalysis(
            area.areaId,
            "background-refresh",
        )
    }
    val documentImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        val drafts = context.resolveDocumentImportDrafts(uri)
        onImportMaterials(area.areaId, drafts)
    }
    val imageImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val label = context.resolveImportDisplayName(uri)
        onImportImage(area.areaId, label, uri.toString())
    }

    val panel = activePanel
    if (panel != null) {
        val panelState = area.panelStates.firstOrNull { it.panel == panel } ?: return
        AreaPanelScreen(
            area = area,
            panelState = panelState,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
            webFeedSync = areaState.webFeedSync,
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
            onOpenInputs = {
                activePanel = null
                activeSurface = AreaStudioSurface.Inputs
            },
        )
        return
    }

    when (activeSurface) {
        AreaStudioSurface.Identity -> {
            AreaIdentityScreen(
                area = area,
                analysis = analysis,
                analysisAction = analysisAction,
                onBack = { activeSurface = AreaStudioSurface.Overview },
                onSave = { title, summary, templateId, iconKey ->
                    onUpdateIdentity(area.areaId, title, summary, templateId, iconKey)
                    activeSurface = AreaStudioSurface.Overview
                },
            )
            return
        }

        AreaStudioSurface.Inputs -> {
            AreaInputsScreen(
                area = area,
                sourceSetup = sourceSetup,
                importedMaterials = importedMaterials,
                webFeedSync = areaState.webFeedSync,
                analysis = analysis,
                analysisAction = analysisAction,
                onBack = { activeSurface = AreaStudioSurface.Overview },
                onOpenSourceSettings = onOpenSourceSettings,
                onBindSource = { sourceKind -> onBindSource(area.areaId, sourceKind) },
                onUnbindSource = { sourceKind -> onUnbindSource(area.areaId, sourceKind) },
                onSaveLink = { normalizedLink ->
                    onImportLink(area.areaId, normalizedLink)
                    importQuestion = buildAreaLinkQuestion(
                        area = area,
                        analysis = analysis,
                        url = normalizedLink,
                    )
                    activeSurface = if (importQuestion != null) {
                        AreaStudioSurface.ImportQuestion
                    } else {
                        AreaStudioSurface.Inputs
                    }
                },
                onAddDocument = { documentImportLauncher.launch(arrayOf("*/*")) },
                onAddImage = {
                    imageImportLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onSaveFeed = { url -> onAddWebFeedSource(area.areaId, url) },
                onSyncWebFeed = { onSyncWebFeed(area.areaId) },
                onRemoveFeed = { url -> onRemoveWebFeedSource(area.areaId, url) },
                onSetFeedAutoSync = { url, enabled -> onSetWebFeedAutoSync(area.areaId, url, enabled) },
                onSetFeedSyncCadence = { url, cadence -> onSetWebFeedSyncCadence(area.areaId, url, cadence) },
                onOpenItem = { item ->
                    when (item.kind) {
                        AreaImportKind.Text -> {
                            importPreviewText = item.reference
                            activeSurface = AreaStudioSurface.TextPreview
                        }
                        AreaImportKind.Link,
                        AreaImportKind.File,
                        AreaImportKind.Image,
                        -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.reference)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(intent) }.onFailure { error ->
                                if (error !is ActivityNotFoundException) throw error
                            }
                        }
                    }
                },
                onRemoveItem = { item -> onRemoveImportedMaterial(item.id) },
            )
            return
        }

        AreaStudioSurface.TextPreview -> {
            AreaTextPreviewScreen(
                text = importPreviewText.orEmpty(),
                onBack = {
                    importPreviewText = null
                    activeSurface = AreaStudioSurface.Inputs
                },
            )
            return
        }

        AreaStudioSurface.Authoring -> {
            AreaAuthoringScreen(
                area = area,
                authoring = authoring,
                analysis = analysis,
                analysisAction = analysisAction,
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

        AreaStudioSurface.ImportQuestion -> {
            val question = importQuestion
            if (question != null) {
                AreaImportQuestionScreen(
                    question = question,
                    onBack = {
                        importQuestion = null
                        activeSurface = AreaStudioSurface.Inputs
                    },
                    onApplyOption = { answer ->
                        onAnswerImportQuestion(area.areaId, answer)
                        importQuestion = null
                        activeSurface = AreaStudioSurface.Inputs
                    },
                )
                return
            }
            activeSurface = AreaStudioSurface.Inputs
        }

        AreaStudioSurface.Analysis -> {
            AreaMachineAnalysisScreen(
                area = area,
                analysis = analysis,
                onBack = { activeSurface = AreaStudioSurface.Overview },
            )
            return
        }

        AreaStudioSurface.Diagnostics -> {
            AreaDiagnosticsScreen(
                area = area,
                sourceSetup = sourceSetup,
                importedMaterials = importedMaterials,
                analysis = analysis,
                analysisAction = analysisAction,
                onBack = { activeSurface = AreaStudioSurface.Overview },
            )
            return
        }

        AreaStudioSurface.Overview -> Unit
    }

    DaysPageScaffold(
        title = area.title,
        onBack = onBack,
        modifier = Modifier.testTag("area-studio-screen"),
        action = analysisAction,
        titleContent = {
            AreaSettingsTopBarTitle(
                areaTitle = area.title,
                family = area.family,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("area-studio-content"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AreaWorkspaceCard(
                area = area,
                authoring = authoring,
                sourceSetup = sourceSetup,
                importedMaterials = importedMaterials,
                webFeedSync = areaState.webFeedSync,
                onEditIdentity = { activeSurface = AreaStudioSurface.Identity },
                onOpenGoal = { activeSurface = AreaStudioSurface.Authoring },
                onOpenAnalysis = { activeSurface = AreaStudioSurface.Analysis },
                onOpenSourceSettings = onOpenSourceSettings,
                onBindSource = { sourceKind -> onBindSource(area.areaId, sourceKind) },
                onUnbindSource = { sourceKind -> onUnbindSource(area.areaId, sourceKind) },
                onOpenInputs = { activeSurface = AreaStudioSurface.Inputs },
                onOpenPanel = { activePanel = it },
            )
        }
    }
}

@Composable
private fun AreaImportQuestionScreen(
    question: AreaQuickQuestionState,
    onBack: () -> Unit,
    onApplyOption: (String) -> Unit,
) {
    var customAnswer by rememberSaveable(question.title) { mutableStateOf("") }
    DaysPageScaffold(
        title = "Rueckfrage",
        onBack = onBack,
        modifier = Modifier.testTag("area-link-question-screen"),
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DaysMetaPill(label = "Analysevorschlag")
                Text(
                    text = question.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                question.options.forEach { option ->
                    OutlinedButton(
                        onClick = { onApplyOption(option.title) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area-link-question-option-${option.id}"),
                        border = BorderStroke(1.dp, AppTheme.colors.outlineSoft),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppTheme.colors.ink,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = option.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = option.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = customAnswer,
                    onValueChange = { customAnswer = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("area-link-question-custom"),
                    label = { Text(question.inputLabel) },
                    placeholder = { Text("Eigene Deutung fuer diesen Link") },
                    minLines = 3,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = { onApplyOption(customAnswer.trim()) },
                        enabled = customAnswer.isNotBlank(),
                    ) {
                        Text("Antwort sichern")
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaQuickImportScreen(
    title: String,
    fieldLabel: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    actionLabel: String,
    enabled: Boolean,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    DaysPageScaffold(
        title = title,
        onBack = onBack,
        modifier = Modifier.testTag("area-quick-import-${title.lowercase()}"),
    ) {
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("area-quick-import-input"),
                    label = { Text(fieldLabel) },
                    placeholder = { Text(placeholder) },
                    minLines = 3,
                )
                Button(
                    onClick = onSubmit,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("area-quick-import-submit"),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun NewsSourceDetailScreen(
    sourceId: String,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    linkDraft: String,
    feedDraft: String,
    onLinkDraftChange: (String) -> Unit,
    onFeedDraftChange: (String) -> Unit,
    onBack: () -> Unit,
    onAddLink: () -> Unit,
    onAddFeed: () -> Unit,
    onAddImage: () -> Unit,
    onOpenItem: (AreaImportedMaterialState) -> Unit,
    onRemoveItem: (AreaImportedMaterialState) -> Unit,
    onRemoveFeed: (String) -> Unit,
) {
    val config = newsSourceDetailConfig(sourceId)
    val importedItems = newsSourceItems(sourceId, importedMaterials)
    val feedItems = if (sourceId == "feeds") webFeedSync.sources else emptyList()
    val value = if (config.usesFeedInput) feedDraft else linkDraft
    val isValid = when {
        config.inputKind == NewsSourceInputKind.None -> true
        else -> value.trim().startsWith("http")
    }

    DaysPageScaffold(
        title = config.title,
        onBack = onBack,
        modifier = Modifier.testTag("area-news-source-${sourceId}-screen"),
        bottomBar = {
            when (config.inputKind) {
                NewsSourceInputKind.Link,
                NewsSourceInputKind.Feed,
                -> Button(
                    onClick = {
                        if (config.usesFeedInput) {
                            onAddFeed()
                        } else {
                            onAddLink()
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("area-news-source-submit"),
                ) {
                    Text(config.actionLabel)
                }

                NewsSourceInputKind.None -> Button(
                    onClick = onAddImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("area-news-source-submit"),
                ) {
                    Text(config.actionLabel)
                }
            }
        },
    ) {
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = config.supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.muted,
                )
                when (config.inputKind) {
                    NewsSourceInputKind.Link,
                    NewsSourceInputKind.Feed,
                    -> OutlinedTextField(
                        value = value,
                        onValueChange = {
                            if (config.usesFeedInput) onFeedDraftChange(it) else onLinkDraftChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area-news-source-input"),
                        label = { Text(config.fieldLabel) },
                        placeholder = { Text(config.placeholder) },
                        minLines = 3,
                    )

                    NewsSourceInputKind.None -> Unit
                }

                if (feedItems.isEmpty() && importedItems.isEmpty()) {
                    Text(
                        text = "Noch nichts hinzugefuegt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }

                feedItems.forEachIndexed { index, source ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
                        )
                    }
                    AreaInputOverviewRow(
                        label = source.hostLabel,
                        value = source.sourceKindLabel,
                        supporting = source.lastStatusLabel.ifBlank { source.syncCadenceLabel },
                        testTag = "area-news-source-feed-${index}",
                        onClick = {},
                        enabled = false,
                    )
                    TextButton(
                        onClick = { onRemoveFeed(source.url) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("area-news-source-feed-remove-${index}"),
                    ) {
                        Text("Entfernen")
                    }
                }

                importedItems.forEachIndexed { index, item ->
                    if (index > 0 || feedItems.isNotEmpty()) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
                        )
                    }
                    AreaInputOverviewRow(
                        label = item.inventoryLabel(),
                        value = if (item.kind == AreaImportKind.Image) "Bild" else "Link",
                        supporting = item.title.ifBlank { item.reference },
                        testTag = "area-news-source-item-${sourceId}-${index}",
                        onClick = { onOpenItem(item) },
                    )
                    TextButton(
                        onClick = { onRemoveItem(item) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("area-news-source-item-remove-${sourceId}-${index}"),
                    ) {
                        Text("Entfernen")
                    }
                }
            }
        }
    }
}

private enum class NewsSourceInputKind {
    Link,
    Feed,
    None,
}

private data class NewsSourceDetailConfig(
    val title: String,
    val fieldLabel: String = "",
    val placeholder: String = "",
    val actionLabel: String,
    val supporting: String,
    val inputKind: NewsSourceInputKind,
) {
    val usesFeedInput: Boolean
        get() = inputKind == NewsSourceInputKind.Feed
}

private fun newsSourceDetailConfig(sourceId: String): NewsSourceDetailConfig {
    return when (sourceId) {
        "web" -> NewsSourceDetailConfig(
            title = "Web",
            fieldLabel = "Link",
            placeholder = "https://...",
            actionLabel = "Link hinzufuegen",
            supporting = "Artikel und Websites direkt in den Bereich holen.",
            inputKind = NewsSourceInputKind.Link,
        )
        "feeds" -> NewsSourceDetailConfig(
            title = "Feeds",
            fieldLabel = "Feed",
            placeholder = "https://.../rss",
            actionLabel = "Feed hinzufuegen",
            supporting = "Mehrere laufende Quellen koennen parallel mitlaufen.",
            inputKind = NewsSourceInputKind.Feed,
        )
        "social-text" -> NewsSourceDetailConfig(
            title = "Social Text",
            fieldLabel = "Post-Link",
            placeholder = "https://x.com/...",
            actionLabel = "Beitrag hinzufuegen",
            supporting = "Ausgewaehlte Text-Posts oder Links merken.",
            inputKind = NewsSourceInputKind.Link,
        )
        "social-image" -> NewsSourceDetailConfig(
            title = "Social Bild",
            fieldLabel = "Bild-Link",
            placeholder = "https://instagram.com/...",
            actionLabel = "Bild hinzufuegen",
            supporting = "Ausgewaehlte Bildposts per Link oder spaeter per Share.",
            inputKind = NewsSourceInputKind.Link,
        )
        "video" -> NewsSourceDetailConfig(
            title = "Video",
            fieldLabel = "Video-Link",
            placeholder = "https://youtube.com/...",
            actionLabel = "Video hinzufuegen",
            supporting = "Einzelne Videos zuerst per Link oder Share, spaeter auch Kanal oder Playlist.",
            inputKind = NewsSourceInputKind.Link,
        )
        "screenshots" -> NewsSourceDetailConfig(
            title = "Screenshots",
            actionLabel = "Bild waehlen",
            supporting = "Screenshots sichern und spaeter im Feed lesen.",
            inputKind = NewsSourceInputKind.None,
        )
        else -> NewsSourceDetailConfig(
            title = "Quelle",
            fieldLabel = "Link",
            placeholder = "https://...",
            actionLabel = "Hinzufuegen",
            supporting = "Quelle fuer diesen Bereich.",
            inputKind = NewsSourceInputKind.Link,
        )
    }
}

private fun newsSourceItems(
    sourceId: String,
    importedMaterials: List<AreaImportedMaterialState>,
): List<AreaImportedMaterialState> {
    return when (sourceId) {
        "web" -> importedMaterials.filter { item ->
            item.kind == AreaImportKind.Link &&
                !item.reference.contains("x.com", ignoreCase = true) &&
                !item.reference.contains("twitter.com", ignoreCase = true) &&
                !item.reference.contains("instagram.com", ignoreCase = true) &&
                !item.reference.contains("youtube.com", ignoreCase = true) &&
                !item.reference.contains("youtu.be", ignoreCase = true)
        }
        "social-text" -> importedMaterials.filter { item ->
            item.kind == AreaImportKind.Link && (
                item.reference.contains("x.com", ignoreCase = true) ||
                    item.reference.contains("twitter.com", ignoreCase = true)
                )
        }
        "social-image" -> importedMaterials.filter { item ->
            item.kind == AreaImportKind.Link && item.reference.contains("instagram.com", ignoreCase = true)
        }
        "video" -> importedMaterials.filter { item ->
            item.kind == AreaImportKind.Link && (
                item.reference.contains("youtube.com", ignoreCase = true) ||
                    item.reference.contains("youtu.be", ignoreCase = true)
                )
        }
        "screenshots" -> importedMaterials.filter { it.kind == AreaImportKind.Image }
        else -> emptyList()
    }
}

@Composable
private fun AreaTextPreviewScreen(
    text: String,
    onBack: () -> Unit,
) {
    DaysPageScaffold(
        title = "Text",
        onBack = onBack,
        modifier = Modifier.testTag("area-text-preview-screen"),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
            ),
            shape = RoundedCornerShape(30.dp),
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun webFeedSyncKey(
    webFeedSync: AreaWebFeedSyncState,
): String {
    return buildString {
        append(webFeedSync.isRunning)
        append('|')
        append(webFeedSync.sources.size)
        append('|')
        append(webFeedSync.statusLabel)
        append('|')
        append(webFeedSync.statusDetail)
        webFeedSync.sources.forEach { source ->
            append('|')
            append(source.url)
            append(':')
            append(source.autoSyncEnabled)
            append(':')
            append(source.syncCadenceLabel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AreaImportsCard(
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    onAddLink: () -> Unit,
    onAddDocument: () -> Unit,
    onAddImage: () -> Unit,
    onAddFeed: () -> Unit,
    onSyncWebFeed: () -> Unit,
    onRemoveFeed: (String) -> Unit,
    onSetFeedAutoSync: (String, Boolean) -> Unit,
    onSetFeedSyncCadence: (String, AreaWebFeedSyncCadence) -> Unit,
    onOpenItem: (AreaImportedMaterialState) -> Unit,
    onRemoveItem: (AreaImportedMaterialState) -> Unit,
) {
    val hasWebLink = importedMaterials.any { item ->
        item.kind == AreaImportKind.Link && item.reference.startsWith("http")
    } || webFeedSync.sources.isNotEmpty()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area-imports-card"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Hinzufuegen in diesem Bereich",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (importedMaterials.isEmpty()) {
                    "Hier landen Links, Bilder, Dateien und spaeter weitere Eingaben. Feeds liefern neue Eintraege nach, Websites koennen lesbar werden und Export-Dateien werden schon beim Import zerlegt."
                } else {
                    "Hier bleibt sichtbar, was zuletzt in den Bereich hineingekommen ist. Feeds bringen neue Eintraege nach, Websites koennen lesbar werden."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (webFeedSync.statusLabel.isNotBlank()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = webFeedSync.statusLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.accent,
                    )
                    Text(
                        text = webFeedSync.statusDetail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onAddLink,
                    modifier = Modifier.testTag("area-import-link"),
                ) {
                    Text("Link")
                }
                OutlinedButton(
                    onClick = onAddDocument,
                    modifier = Modifier.testTag("area-import-document"),
                ) {
                    Text("Datei")
                }
                OutlinedButton(
                    onClick = onAddImage,
                    modifier = Modifier.testTag("area-import-image"),
                ) {
                    Text("Bild")
                }
                OutlinedButton(
                    onClick = onAddFeed,
                    modifier = Modifier.testTag("area-import-feed"),
                ) {
                    Text("Feed")
                }
                if (hasWebLink || webFeedSync.isRunning) {
                    OutlinedButton(
                        onClick = onSyncWebFeed,
                        enabled = !webFeedSync.isRunning,
                        modifier = Modifier.testTag("area-import-sync-feed"),
                    ) {
                        Text(if (webFeedSync.isRunning) "Laeuft..." else "Feed lesen")
                    }
                }
            }
            if (webFeedSync.sources.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Gemerkte Feed-Quellen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    webFeedSync.sources.forEach { source ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.44f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = source.hostLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        DaysMetaPill(label = source.sourceKindLabel)
                                        source.capabilityLabels.take(2).forEach { label ->
                                            DaysMetaPill(label = label)
                                        }
                                    }
                                    Text(
                                        text = source.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.colors.muted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(
                                    onClick = { onRemoveFeed(source.url) },
                                    modifier = Modifier.testTag("area-feed-remove-${source.url.hashCode()}"),
                                ) {
                                    Text("Entfernen")
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = if (source.autoSyncEnabled) "Auto-Sync aktiv" else "Auto-Sync aus",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AppTheme.colors.accent,
                                    )
                                    Text(
                                        text = if (source.autoSyncEnabled) {
                                            "${source.syncCadenceLabel} · ${source.capabilityLabels.joinToString(" · ")}"
                                        } else {
                                            source.capabilityLabels.joinToString(" · ")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (source.lastStatusLabel.isNotBlank()) {
                                        Text(
                                            text = "${source.lastStatusLabel}. ${source.lastStatusDetail}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                androidx.compose.material3.Switch(
                                    checked = source.autoSyncEnabled,
                                    onCheckedChange = { enabled -> onSetFeedAutoSync(source.url, enabled) },
                                    modifier = Modifier.testTag("area-feed-auto-${source.url.hashCode()}"),
                                )
                            }
                            if (source.autoSyncEnabled) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    AreaWebFeedSyncCadence.entries.forEach { cadence ->
                                        val selected = source.syncCadenceLabel == cadence.label
                                        OutlinedButton(
                                            onClick = { onSetFeedSyncCadence(source.url, cadence) },
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (selected) AppTheme.colors.accent else AppTheme.colors.outlineSoft,
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = if (selected) AppTheme.colors.accent else AppTheme.colors.ink,
                                            ),
                                            modifier = Modifier.testTag("area-feed-cadence-${source.url.hashCode()}-${cadence.storageKey}"),
                                        ) {
                                            Text(cadence.label)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (importedMaterials.isEmpty()) {
                Text(
                    text = "Noch kein importiertes Material.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                importedMaterials.take(3).forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = item.kind.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AppTheme.colors.accent,
                                    )
                                    if (item.isPending) {
                                        DaysMetaPill(label = "Wird gesichert")
                                    }
                                }
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Text(
                            text = item.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (item.reference.isNotBlank()) {
                            Text(
                                text = item.reference,
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.muted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                onClick = { onOpenItem(item) },
                                modifier = Modifier.testTag("area-import-open-${item.id}"),
                            ) {
                                Text(if (item.kind == AreaImportKind.Text) "Lesen" else "Oeffnen")
                            }
                            TextButton(
                                onClick = { onRemoveItem(item) },
                                modifier = Modifier.testTag("area-import-remove-${item.id}"),
                            ) {
                                Text("Entfernen")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sourceSetupStatusLabel(
    status: AreaSourceSetupStatus,
): String {
    return when (status) {
        AreaSourceSetupStatus.UNCONFIGURED -> "Offen"
        AreaSourceSetupStatus.PERMISSION_REQUIRED -> "Blockiert"
        AreaSourceSetupStatus.READY -> "Bereit"
        AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> "Heute frei"
    }
}

@Composable
private fun AreaSettingsTopBarTitle(
    areaTitle: String,
    family: StartAreaFamily,
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
            text = "Typ: ${areaTypeLabel(family)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private enum class AreaWorkspaceAction {
    OpenSnapshot,
    OpenInputs,
    OpenPath,
    OpenOptions,
    OpenAnalysis,
    OpenDiagnostics,
    OpenAuthoring,
    OpenSourceSettings,
    ConnectSource,
    DisconnectSource,
}

private data class AreaWorkspaceHero(
    val title: String,
    val detail: String,
)

private data class AreaWorkspaceSignal(
    val label: String,
    val title: String,
    val detail: String,
    val action: AreaWorkspaceAction,
    val testTag: String,
)

private data class AreaWorkspacePalette(
    val container: Color,
    val surface: Color,
    val outline: Color,
    val ink: Color,
    val support: Color,
    val actionInk: Color,
)

private fun newsSelectedSourceLabels(
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<String> {
    val screenshotCount = importedMaterials.count { it.kind == AreaImportKind.Image }
    val xCount = importedMaterials.countImportedForHosts("x.com", "twitter.com")
    val instagramCount = importedMaterials.countImportedForHosts("instagram.com")
    val youtubeCount = importedMaterials.countImportedForHosts("youtube.com", "youtu.be")
    val fazFeedCount = webFeedSync.sources.countFeedSourcesForHosts("faz.net")
    val stolFeedCount = webFeedSync.sources.countFeedSourcesForHosts("stol.it")
    return buildList {
        if (xCount > 0) add("X")
        if (instagramCount > 0) add("Instagram")
        if (youtubeCount > 0) add("YouTube")
        if (fazFeedCount > 0) add("FAZ")
        if (stolFeedCount > 0) add("stol.it")
        if (screenshotCount > 0) add("Screenshots")
    }
}

private fun newsWorkspaceSummary(
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): String {
    val selectedSources = newsSelectedSourceSummary(importedMaterials, webFeedSync)
    return when {
        selectedSources.isEmpty() -> "Noch keine News-Quelle verbunden."
        else -> selectedSources.joinToString(" · ")
    }
}

private fun newsSelectedSourceFamilies(
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<String> {
    val feedCount = webFeedSync.sources.size
    val xCount = importedMaterials.countImportedForHosts("x.com", "twitter.com")
    val instagramCount = importedMaterials.countImportedForHosts("instagram.com")
    val youtubeCount = importedMaterials.countImportedForHosts("youtube.com", "youtu.be")
    val screenshotCount = importedMaterials.count { it.kind == AreaImportKind.Image }
    val webCount = importedMaterials.count { item ->
        item.kind == AreaImportKind.Link &&
            !item.reference.contains("x.com", ignoreCase = true) &&
            !item.reference.contains("twitter.com", ignoreCase = true) &&
            !item.reference.contains("instagram.com", ignoreCase = true) &&
            !item.reference.contains("youtube.com", ignoreCase = true) &&
            !item.reference.contains("youtu.be", ignoreCase = true)
    }
    return buildList {
        if (feedCount > 0) add("Feeds")
        if (webCount > 0) add("Web")
        if (xCount > 0) add("Social Text")
        if (instagramCount > 0) add("Social Bild")
        if (youtubeCount > 0) add("Video")
        if (screenshotCount > 0) add("Screenshots")
    }
}

private fun newsSelectedSourceSummary(
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<String> {
    val feedCount = webFeedSync.sources.size
    val xCount = importedMaterials.countImportedForHosts("x.com", "twitter.com")
    val instagramCount = importedMaterials.countImportedForHosts("instagram.com")
    val youtubeCount = importedMaterials.countImportedForHosts("youtube.com", "youtu.be")
    val screenshotCount = importedMaterials.count { it.kind == AreaImportKind.Image }
    val webCount = importedMaterials.count { item ->
        item.kind == AreaImportKind.Link &&
            !item.reference.contains("x.com", ignoreCase = true) &&
            !item.reference.contains("twitter.com", ignoreCase = true) &&
            !item.reference.contains("instagram.com", ignoreCase = true) &&
            !item.reference.contains("youtube.com", ignoreCase = true) &&
            !item.reference.contains("youtu.be", ignoreCase = true)
    }
    return buildList {
        if (feedCount > 0) add("$feedCount Feeds")
        if (webCount > 0) add("$webCount Web")
        if (xCount > 0) add("$xCount Social Text")
        if (instagramCount > 0) add("$instagramCount Social Bild")
        if (youtubeCount > 0) add("$youtubeCount Video")
        if (screenshotCount > 0) add("$screenshotCount Screenshots")
    }
}

private fun areaTypeLabel(
    family: StartAreaFamily,
): String {
    return family.shortLabel
}

private fun areaWorkspacePalette(
    family: StartAreaFamily,
): AreaWorkspacePalette {
    val base = when (family) {
        StartAreaFamily.Radar -> Color(0xFF1E5EFF)
        StartAreaFamily.Pflicht -> Color(0xFFE4572E)
        StartAreaFamily.Routine -> Color(0xFFD48A00)
        StartAreaFamily.Kontakt -> Color(0xFF12805C)
        StartAreaFamily.Gesundheit -> Color(0xFF307B5A)
        StartAreaFamily.Ort -> Color(0xFF9A5C2C)
        StartAreaFamily.Sammlung -> Color(0xFF6C58B8)
    }
    return AreaWorkspacePalette(
        container = base,
        surface = Color.White.copy(alpha = 0.12f),
        outline = Color.White.copy(alpha = 0.16f),
        ink = Color.White,
        support = Color.White.copy(alpha = 0.82f),
        actionInk = base.copy(alpha = 0.94f),
    )
}

private fun buildAreaWorkspaceSignals(
    area: StartAreaDetailState,
    authoring: AreaAuthoringStudioState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<AreaWorkspaceSignal> {
    if (isNewsMediumArea(area)) {
        return buildNewsWorkspaceSignals(
            area = area,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
            webFeedSync = webFeedSync,
        )
    }
    val sourceLabel = sourceSetup?.let { setup ->
        when (setup.sourceKind) {
            DataSourceKind.CALENDAR -> "Kalender"
            DataSourceKind.NOTIFICATIONS -> "Benachrichtigungen"
            DataSourceKind.HEALTH_CONNECT -> "Health Connect"
            DataSourceKind.MANUAL -> "Manuell"
        }
    } ?: "Noch frei"
    return listOf(
        AreaWorkspaceSignal(
            label = "Aktueller Status",
            title = area.todayOutput.statusLabel,
            detail = when {
                area.profileState.lageMode == AreaLageMode.State ->
                    "Status wird gesetzt."
                area.todayOutput.sourceTruth == com.struperto.androidappdays.domain.area.AreaSourceTruth.missing ->
                    "Noch ohne belastbare Spur."
                else -> "Stand ist gesetzt."
            },
            action = AreaWorkspaceAction.OpenSnapshot,
            testTag = "area-entry-stand",
        ),
        AreaWorkspaceSignal(
            label = "Hinzufuegen",
            title = sourceLabel,
            detail = sourceSetup?.headline ?: "Noch keine klare Spur verbunden.",
            action = AreaWorkspaceAction.OpenInputs,
            testTag = "area-entry-inputs",
        ),
        AreaWorkspaceSignal(
            label = "Sortieren",
            title = area.focusTrack,
            detail = area.profileState.directionLabel,
            action = AreaWorkspaceAction.OpenPath,
            testTag = "area-entry-goal",
        ),
        AreaWorkspaceSignal(
            label = "Im Feed",
            title = area.profileState.flowLabel,
            detail = if (area.flowCount > 0) {
                "${area.flowCount} Extras"
            } else {
                "Noch ruhig"
            },
            action = AreaWorkspaceAction.OpenOptions,
            testTag = "area-entry-flow",
        ),
    )
}

private fun buildNewsWorkspaceSignals(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<AreaWorkspaceSignal> {
    val selectedSources = newsSelectedSourceFamilies(importedMaterials, webFeedSync)
    val sourceSummary = newsSelectedSourceSummary(importedMaterials, webFeedSync)
        .joinToString(" · ")
        .ifBlank { sourceSetup?.headline ?: "Noch nichts" }
    val selectedTracks = area.selectedTracks.ifEmpty { area.tracks }
    val extrasCount = listOf(
        area.reviewEnabled,
        area.remindersEnabled,
        area.experimentsEnabled,
    ).count { it }
    val behaviorDetail = when {
        webFeedSync.isRunning -> "Nachladen laeuft"
        webFeedSync.sources.any { it.autoSyncEnabled } -> "Automatisches Nachladen"
        extrasCount > 0 -> "${extrasCount} Extras"
        else -> "Noch ruhig"
    }
    val statusLabel = when {
        selectedSources.isNotEmpty() || importedMaterials.isNotEmpty() || webFeedSync.sources.isNotEmpty() -> "Bereit"
        else -> area.todayOutput.statusLabel.ifBlank { "Offen" }
    }
    return listOf(
        AreaWorkspaceSignal(
            label = "Aktueller Status",
            title = if (selectedSources.isEmpty()) statusLabel else "${selectedSources.size} Quellenarten",
            detail = when {
                selectedSources.isNotEmpty() -> sourceSummary
                else -> "Noch ohne verbundene News-Quelle."
            },
            action = AreaWorkspaceAction.OpenSnapshot,
            testTag = "area-entry-stand",
        ),
        AreaWorkspaceSignal(
            label = "Hinzufuegen",
            title = "6 Wege",
            detail = "Web · Feeds · Social Text · Social Bild · Video · Screenshots",
            action = AreaWorkspaceAction.OpenInputs,
            testTag = "area-entry-inputs",
        ),
        AreaWorkspaceSignal(
            label = "Sortieren",
            title = if (area.focusTrack.isBlank()) "Noch offen" else "${area.focusTrack} zuerst",
            detail = selectedTracks.joinToString(" · ").ifBlank { "Noch nichts gewaehlt." },
            action = AreaWorkspaceAction.OpenPath,
            testTag = "area-entry-goal",
        ),
        AreaWorkspaceSignal(
            label = "Im Feed",
            title = when {
                webFeedSync.isRunning -> "Laeuft"
                webFeedSync.sources.any { it.autoSyncEnabled } -> "Automatisch"
                else -> area.profileState.flowLabel
            },
            detail = when {
                webFeedSync.isRunning -> "News-Quellen werden gerade aktualisiert."
                webFeedSync.sources.any { it.autoSyncEnabled } -> "Nachladen aktiv · ${newsMediumIntensityLabel(area.intensity)}"
                else -> behaviorDetail
            },
            action = AreaWorkspaceAction.OpenOptions,
            testTag = "area-entry-flow",
        ),
    )
}

@Composable
private fun AreaWorkspaceCard(
    area: StartAreaDetailState,
    authoring: AreaAuthoringStudioState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    onEditIdentity: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onOpenSourceSettings: () -> Unit,
    onBindSource: (DataSourceKind) -> Unit,
    onUnbindSource: (DataSourceKind) -> Unit,
    onOpenInputs: () -> Unit,
    onOpenPanel: (StartAreaPanel) -> Unit,
) {
    val palette = areaWorkspacePalette(area.family)
    val signals = remember(area, authoring, sourceSetup, importedMaterials, webFeedSync) {
        buildAreaWorkspaceSignals(
            area = area,
            authoring = authoring,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
            webFeedSync = webFeedSync,
        )
    }
    fun runAction(action: AreaWorkspaceAction) {
        when (action) {
            AreaWorkspaceAction.OpenSnapshot -> onOpenPanel(StartAreaPanel.Snapshot)
            AreaWorkspaceAction.OpenInputs -> onOpenInputs()
            AreaWorkspaceAction.OpenPath -> onOpenPanel(StartAreaPanel.Path)
            AreaWorkspaceAction.OpenOptions -> onOpenPanel(StartAreaPanel.Options)
            AreaWorkspaceAction.OpenAnalysis -> onOpenAnalysis()
            AreaWorkspaceAction.OpenDiagnostics -> Unit
            AreaWorkspaceAction.OpenAuthoring -> onOpenGoal()
            AreaWorkspaceAction.OpenSourceSettings -> onOpenSourceSettings()
            AreaWorkspaceAction.ConnectSource -> sourceSetup?.let { setup ->
                if (setup.canConnectSource) {
                    onBindSource(setup.sourceKind)
                } else {
                    onOpenSourceSettings()
                }
            }
            AreaWorkspaceAction.DisconnectSource -> sourceSetup?.let { setup ->
                if (setup.canDisconnectSource) {
                    onUnbindSource(setup.sourceKind)
                }
            }
        }
    }
    val statusSignal = if (isNewsMediumArea(area)) {
        signals.firstOrNull { it.action == AreaWorkspaceAction.OpenSnapshot }
    } else {
        null
    }
    val footerSignals = if (isNewsMediumArea(area)) {
        signals.filterNot { it.action == AreaWorkspaceAction.OpenSnapshot }
    } else {
        signals
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area-workspace-card"),
        colors = CardDefaults.cardColors(containerColor = palette.container),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(
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
                                .background(palette.surface),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = startAreaIcon(area.iconKey),
                                contentDescription = null,
                                tint = palette.ink,
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
                                containerColor = Color.White,
                                contentColor = palette.actionInk,
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
                            text = "Bereich",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.support,
                        )
                        Text(
                            text = areaTypeLabel(area.family),
                            style = MaterialTheme.typography.headlineSmall,
                            color = palette.ink,
                        )
                        Text(
                            text = if (isNewsMediumArea(area)) {
                                newsWorkspaceSummary(
                                    importedMaterials = importedMaterials,
                                    webFeedSync = webFeedSync,
                                )
                            } else {
                                area.summary
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.support,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (statusSignal != null) {
                    AreaWorkspaceHeroStatus(
                        signal = statusSignal,
                        palette = palette,
                        onClick = { runAction(statusSignal.action) },
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                footerSignals.forEach { signal ->
                    if (statusSignal != null) {
                        AreaWorkspaceNeutralButtonRow(
                            signal = signal,
                            palette = palette,
                            onClick = { runAction(signal.action) },
                        )
                    } else {
                        AreaWorkspaceSignalRow(
                            signal = signal,
                            palette = palette,
                            onClick = { runAction(signal.action) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaWorkspaceHeroStatus(
    signal: AreaWorkspaceSignal,
    palette: AreaWorkspacePalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(width = 1.dp, color = palette.outline, shape = RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(signal.testTag)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = signal.label,
                style = MaterialTheme.typography.labelLarge,
                color = palette.support,
            )
            Text(
                text = signal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = palette.ink,
            )
            Text(
                text = signal.detail,
                style = MaterialTheme.typography.bodySmall,
                color = palette.support,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = palette.ink,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun AreaWorkspaceNeutralButtonRow(
    signal: AreaWorkspaceSignal,
    palette: AreaWorkspacePalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.82f), shape = RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(signal.testTag)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = signal.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = palette.actionInk,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = palette.actionInk,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun AreaInputsScreen(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    analysis: AreaMachineAnalysisState,
    analysisAction: DaysTopBarAction,
    onBack: () -> Unit,
    onOpenSourceSettings: () -> Unit,
    onBindSource: (DataSourceKind) -> Unit,
    onUnbindSource: (DataSourceKind) -> Unit,
    onSaveLink: (String) -> Unit,
    onAddDocument: () -> Unit,
    onAddImage: () -> Unit,
    onSaveFeed: (String) -> Unit,
    onSyncWebFeed: () -> Unit,
    onRemoveFeed: (String) -> Unit,
    onSetFeedAutoSync: (String, Boolean) -> Unit,
    onSetFeedSyncCadence: (String, AreaWebFeedSyncCadence) -> Unit,
    onOpenItem: (AreaImportedMaterialState) -> Unit,
    onRemoveItem: (AreaImportedMaterialState) -> Unit,
) {
    var activeSurface by rememberSaveable(area.areaId) { mutableStateOf(AreaInputsSurface.Overview) }
    var activeNewsSourceId by rememberSaveable(area.areaId) { mutableStateOf<String?>(null) }
    var linkDraft by rememberSaveable(area.areaId) { mutableStateOf("") }
    var feedDraft by rememberSaveable(area.areaId) { mutableStateOf("") }
    val context = LocalContext.current
    val isNewsMedium = isNewsMediumArea(area)
    val isPersonContact = isPersonContactArea(area)
    val isProjectWork = isProjectWorkArea(area)
    val isPlaceContext = isPlaceContextArea(area)
    val isHealthRitual = isHealthRitualArea(area)
    val isCollectionInbox = isCollectionInboxArea(area)

    if (isNewsMedium && activeNewsSourceId != null) {
        val sourceId = activeNewsSourceId.orEmpty()
        NewsSourceDetailScreen(
            sourceId = sourceId,
            importedMaterials = importedMaterials,
            webFeedSync = webFeedSync,
            linkDraft = linkDraft,
            feedDraft = feedDraft,
            onLinkDraftChange = { linkDraft = it },
            onFeedDraftChange = { feedDraft = it },
            onBack = { activeNewsSourceId = null },
            onAddLink = {
                onSaveLink(linkDraft.trim())
                linkDraft = ""
            },
            onAddFeed = {
                onSaveFeed(feedDraft.trim())
                feedDraft = ""
            },
            onAddImage = onAddImage,
            onOpenItem = onOpenItem,
            onRemoveItem = onRemoveItem,
            onRemoveFeed = onRemoveFeed,
        )
        return
    }

    when (activeSurface) {
        AreaInputsSurface.Source -> {
            DaysPageScaffold(
                title = "Quelle",
                onBack = { activeSurface = AreaInputsSurface.Overview },
                modifier = Modifier.testTag("area-input-source-screen"),
                action = analysisAction,
            ) {
                sourceSetup?.let { setup ->
                    AreaInputSourceCard(
                        sourceSetup = setup,
                        onOpenSourceSettings = onOpenSourceSettings,
                        onBindSource = onBindSource,
                        onUnbindSource = onUnbindSource,
                    )
                } ?: SettingsTileCard(
                    title = "Noch frei",
                    detail = "Du kannst diesen Bereich frei lassen oder spaeter eine Android-Spur anschliessen.",
                )
            }
            return
        }

        AreaInputsSurface.LinkEntry -> {
            AreaQuickImportScreen(
                title = "Link",
                fieldLabel = "Link",
                placeholder = "https://...",
                value = linkDraft,
                onValueChange = { linkDraft = it },
                actionLabel = "Uebernehmen",
                enabled = linkDraft.trim().startsWith("http"),
                onBack = { activeSurface = AreaInputsSurface.Overview },
                onSubmit = {
                    onSaveLink(linkDraft.trim())
                    linkDraft = ""
                    activeSurface = AreaInputsSurface.Overview
                },
            )
            return
        }

        AreaInputsSurface.FeedEntry -> {
            AreaQuickImportScreen(
                title = "Feed",
                fieldLabel = "Feed",
                placeholder = "https://...",
                value = feedDraft,
                onValueChange = { feedDraft = it },
                actionLabel = "Merken",
                enabled = feedDraft.trim().startsWith("http"),
                onBack = { activeSurface = AreaInputsSurface.Overview },
                onSubmit = {
                    onSaveFeed(feedDraft.trim())
                    feedDraft = ""
                    activeSurface = AreaInputsSurface.Overview
                },
            )
            return
        }

        AreaInputsSurface.Material -> {
            DaysPageScaffold(
                title = "Hinzufuegen",
                onBack = { activeSurface = AreaInputsSurface.Overview },
                modifier = Modifier.testTag("area-input-material-screen"),
                action = analysisAction,
            ) {
                AreaInputMaterialHub(
                    importedMaterials = importedMaterials,
                    webFeedSync = webFeedSync,
                    onOpenCollect = { activeSurface = AreaInputsSurface.MaterialCollect },
                    onOpenSources = { activeSurface = AreaInputsSurface.MaterialSources },
                    onOpenInventory = { activeSurface = AreaInputsSurface.MaterialInventory },
                )
            }
            return
        }

        AreaInputsSurface.MaterialCollect -> {
            DaysPageScaffold(
                title = "Direkt",
                onBack = { activeSurface = AreaInputsSurface.Material },
                modifier = Modifier.testTag("area-input-material-collect-screen"),
                action = analysisAction,
            ) {
                AreaInputCollectCard(
                    onAddLink = {
                        linkDraft = ""
                        activeSurface = AreaInputsSurface.LinkEntry
                    },
                    onAddDocument = onAddDocument,
                    onAddImage = onAddImage,
                    onAddFeed = {
                        feedDraft = ""
                        activeSurface = AreaInputsSurface.FeedEntry
                    },
                )
            }
            return
        }

        AreaInputsSurface.MaterialSources -> {
            DaysPageScaffold(
                title = "Laufend",
                onBack = { activeSurface = AreaInputsSurface.Material },
                modifier = Modifier.testTag("area-input-material-sources-screen"),
                action = analysisAction,
            ) {
                AreaInputSourcesCard(
                    webFeedSync = webFeedSync,
                    onRemoveFeed = onRemoveFeed,
                    onSetFeedAutoSync = onSetFeedAutoSync,
                    onSetFeedSyncCadence = onSetFeedSyncCadence,
                )
            }
            return
        }

        AreaInputsSurface.MaterialInventory -> {
            DaysPageScaffold(
                title = "Bestand",
                onBack = { activeSurface = AreaInputsSurface.Material },
                modifier = Modifier.testTag("area-input-material-inventory-screen"),
                action = analysisAction,
            ) {
                AreaInputInventoryCard(
                    importedMaterials = importedMaterials,
                    onOpenItem = onOpenItem,
                    onRemoveItem = onRemoveItem,
                )
            }
            return
        }

        AreaInputsSurface.MaterialAutomation -> {
            DaysPageScaffold(
                title = "Automatik",
                onBack = { activeSurface = AreaInputsSurface.Material },
                modifier = Modifier.testTag("area-input-material-automation-screen"),
                action = analysisAction,
            ) {
                AreaInputAutomationCard(
                    area = area,
                    webFeedSync = webFeedSync,
                    onSyncWebFeed = onSyncWebFeed,
                )
            }
            return
        }

        AreaInputsSurface.Processing -> {
            DaysPageScaffold(
                title = "Verarbeitung",
                onBack = { activeSurface = AreaInputsSurface.Overview },
                modifier = Modifier.testTag("area-input-processing-screen"),
                action = analysisAction,
            ) {
                AreaInputProcessingCard(area = area)
            }
            return
        }

        AreaInputsSurface.Overview -> Unit
    }

    val sourceValue = if (isNewsMedium) {
        "Bereit"
    } else sourceSetup?.let { setup ->
        when (setup.status) {
            AreaSourceSetupStatus.READY -> "Bereit"
            AreaSourceSetupStatus.PERMISSION_REQUIRED -> "Blockiert"
            AreaSourceSetupStatus.NO_RECENT_OR_TODAY_DATA -> "Kein Signal"
            AreaSourceSetupStatus.UNCONFIGURED -> "Noch frei"
        }
    } ?: "Noch frei"
    val onceValue = when (importedMaterials.size) {
        0 -> "Noch leer"
        1 -> "1 Teil"
        else -> "${importedMaterials.size} Teile"
    }
    val screenshotCount = importedMaterials.count { it.kind == AreaImportKind.Image }
    val ongoingValue = when (webFeedSync.sources.size) {
        0 -> "Noch frei"
        1 -> "1 Quelle"
        else -> "${webFeedSync.sources.size} Quellen"
    }
    val inventoryValue = when {
        importedMaterials.isEmpty() && webFeedSync.sources.isEmpty() -> "Noch leer"
        importedMaterials.isEmpty() -> "0 Teile"
        else -> "${importedMaterials.size} im Bestand"
    }
    val xCount = importedMaterials.countImportedForHosts("x.com", "twitter.com")
    val instagramCount = importedMaterials.countImportedForHosts("instagram.com")
    val youtubeCount = importedMaterials.countImportedForHosts("youtube.com", "youtu.be")
    val fazFeedCount = webFeedSync.sources.countFeedSourcesForHosts("faz.net")
    val stolFeedCount = webFeedSync.sources.countFeedSourcesForHosts("stol.it")
    val webCount = importedMaterials.count { item ->
        item.kind == AreaImportKind.Link &&
            !item.reference.contains("x.com", ignoreCase = true) &&
            !item.reference.contains("twitter.com", ignoreCase = true) &&
            !item.reference.contains("instagram.com", ignoreCase = true) &&
            !item.reference.contains("youtube.com", ignoreCase = true) &&
            !item.reference.contains("youtu.be", ignoreCase = true)
    }
    val xInstalled = isAppInstalled(context, "com.twitter.android")
    val instagramInstalled = isAppInstalled(context, "com.instagram.android")
    val youtubeInstalled = isAppInstalled(context, "com.google.android.youtube")

    DaysPageScaffold(
                title = "Hinzufuegen",
                onBack = onBack,
                modifier = Modifier.testTag("area-inputs-screen"),
                action = analysisAction,
    ) {
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when {
                    isNewsMedium -> {
                        AreaInputOverviewRow(
                            label = "Web",
                            value = when {
                                webCount == 1 -> "1 Link"
                                webCount > 1 -> "$webCount Links"
                                else -> "Mit Link"
                            },
                            supporting = "Artikel und Websites direkt hinzufuegen.",
                            testTag = "area-input-overview-web",
                            onClick = {
                                linkDraft = ""
                                activeNewsSourceId = "web"
                            },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Feeds",
                            value = when {
                                webFeedSync.sources.size == 1 -> "1 Quelle"
                                webFeedSync.sources.size > 1 -> "${webFeedSync.sources.size} Quellen"
                                else -> "Noch frei"
                            },
                            supporting = when {
                                fazFeedCount > 0 || stolFeedCount > 0 -> buildList {
                                    if (fazFeedCount > 0) add("FAZ")
                                    if (stolFeedCount > 0) add("stol.it")
                                }.joinToString(" · ")
                                else -> "RSS und laufende News-Quellen."
                            },
                            testTag = "area-input-overview-feeds",
                            onClick = {
                                feedDraft = ""
                                activeNewsSourceId = "feeds"
                            },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Social Text",
                            value = when {
                                xCount == 1 -> "1 Beitrag"
                                xCount > 1 -> "$xCount Beitraege"
                                xInstalled -> "Mit Share"
                                else -> "Mit Link"
                            },
                            supporting = when {
                                xCount > 0 -> "Ausgewaehlte Posts sind schon da."
                                xInstalled -> "Per Share oder Link."
                                else -> "Per x.com-Link."
                            },
                            testTag = "area-input-overview-social-text",
                            onClick = {
                                linkDraft = ""
                                activeNewsSourceId = "social-text"
                            },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Social Bild",
                            value = when {
                                instagramCount == 1 -> "1 Bild"
                                instagramCount > 1 -> "$instagramCount Bilder"
                                instagramInstalled -> "Mit Share"
                                else -> "Mit Link"
                            },
                            supporting = when {
                                instagramCount > 0 -> "Ausgewaehlte Bilder sind schon da."
                                instagramInstalled -> "Per Share, Link oder Screenshot."
                                else -> "Per Link oder Screenshot."
                            },
                            testTag = "area-input-overview-social-image",
                            onClick = {
                                linkDraft = ""
                                activeNewsSourceId = "social-image"
                            },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Video",
                            value = when {
                                youtubeCount == 1 -> "1 Video"
                                youtubeCount > 1 -> "$youtubeCount Videos"
                                youtubeInstalled -> "Mit Share"
                                else -> "Mit Link"
                            },
                            supporting = when {
                                youtubeCount > 0 -> "Ausgewaehlte Videos sind schon da."
                                youtubeInstalled -> "Per Share, Link oder spaeter Kanal/Playlist."
                                else -> "Per YouTube-Link."
                            },
                            testTag = "area-input-overview-video",
                            onClick = {
                                linkDraft = ""
                                activeNewsSourceId = "video"
                            },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Screenshots",
                            value = when (screenshotCount) {
                                0 -> "Noch leer"
                                1 -> "1 Bild"
                                else -> "$screenshotCount Bilder"
                            },
                            supporting = if (screenshotCount == 0) {
                                "Posts oder Artikel sichern."
                            } else {
                                "Bereits gesichert."
                            },
                            testTag = "area-input-overview-screenshots",
                            onClick = { activeNewsSourceId = "screenshots" },
                        )
                    }
                    isPersonContact -> {
                        AreaInputOverviewRow(
                            label = "Messenger",
                            value = sourceValue,
                            supporting = sourceSetup?.headline ?: "Benachrichtigungen und Chat-Spuren fuer diesen Bereich.",
                            testTag = "area-input-overview-source",
                            onClick = { activeSurface = AreaInputsSurface.Source },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Notizen",
                            value = onceValue,
                            supporting = if (importedMaterials.isEmpty()) "Kurze Nachrichten, Namen oder Hinweise festhalten." else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-once",
                            onClick = { activeSurface = AreaInputsSurface.MaterialCollect },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Screenshots",
                            value = when (screenshotCount) {
                                0 -> "Noch leer"
                                1 -> "1 Bild"
                                else -> "$screenshotCount Bilder"
                            },
                            supporting = if (screenshotCount == 0) "Chats oder Personen-Screens spaeter lesbar machen." else "Bereits gesicherte Screenshots",
                            testTag = "area-input-overview-screenshots",
                            onClick = onAddImage,
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Bestand",
                            value = inventoryValue,
                            supporting = if (importedMaterials.isEmpty()) "Noch leer" else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-inventory",
                            onClick = { activeSurface = AreaInputsSurface.MaterialInventory },
                        )
                    }
                    isProjectWork -> {
                        AreaInputOverviewRow(
                            label = "Verbindung",
                            value = sourceValue,
                            supporting = sourceSetup?.headline ?: "Kalender oder andere Android-Spuren fuer dieses Projekt.",
                            testTag = "area-input-overview-source",
                            onClick = { activeSurface = AreaInputsSurface.Source },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Material",
                            value = onceValue,
                            supporting = if (importedMaterials.isEmpty()) "Links, Notizen oder Bilder direkt in den Bereich holen." else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-once",
                            onClick = { activeSurface = AreaInputsSurface.MaterialCollect },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Dateien",
                            value = if (importedMaterials.any { it.kind == AreaImportKind.File }) "Vorhanden" else "Noch leer",
                            supporting = "Dokumente, Exporte oder Unterlagen direkt waehlen.",
                            testTag = "area-input-overview-files",
                            onClick = onAddDocument,
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Screenshots",
                            value = when (screenshotCount) {
                                0 -> "Noch leer"
                                1 -> "1 Bild"
                                else -> "$screenshotCount Bilder"
                            },
                            supporting = if (screenshotCount == 0) "Screens oder Skizzen fuer App-Bau und Projekte sichern." else "Bereits gesicherte Screenshots",
                            testTag = "area-input-overview-screenshots",
                            onClick = onAddImage,
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Bestand",
                            value = inventoryValue,
                            supporting = if (importedMaterials.isEmpty()) "Noch leer" else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-inventory",
                            onClick = { activeSurface = AreaInputsSurface.MaterialInventory },
                        )
                    }
                    isPlaceContext -> {
                        AreaInputOverviewRow(
                            label = "Standort",
                            value = sourceValue,
                            supporting = sourceSetup?.headline ?: "Orte, Wege oder Kontexte spaeter anbinden.",
                            testTag = "area-input-overview-source",
                            onClick = { activeSurface = AreaInputsSurface.Source },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Wege",
                            value = onceValue,
                            supporting = if (importedMaterials.isEmpty()) "Notizen oder Signale fuer Orte und Wege sichern." else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-once",
                            onClick = { activeSurface = AreaInputsSurface.MaterialCollect },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Orte",
                            value = ongoingValue,
                            supporting = if (webFeedSync.sources.isEmpty()) "Gemerkte Orte oder laufende Kontextspuren spaeter anbinden." else "${webFeedSync.sources.size} Quelle${if (webFeedSync.sources.size == 1) "" else "n"} aktiv",
                            testTag = "area-input-overview-ongoing",
                            onClick = { activeSurface = AreaInputsSurface.Source },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Screenshots",
                            value = when (screenshotCount) {
                                0 -> "Noch leer"
                                1 -> "1 Bild"
                                else -> "$screenshotCount Bilder"
                            },
                            supporting = if (screenshotCount == 0) "Karten, Wege oder Ortsideen als Bild sichern." else "Bereits gesicherte Screenshots",
                            testTag = "area-input-overview-screenshots",
                            onClick = onAddImage,
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Bestand",
                            value = inventoryValue,
                            supporting = if (importedMaterials.isEmpty()) "Noch leer" else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-inventory",
                            onClick = { activeSurface = AreaInputsSurface.MaterialInventory },
                        )
                    }
                    isHealthRitual -> {
                        AreaInputOverviewRow(
                            label = "Health",
                            value = sourceValue,
                            supporting = sourceSetup?.headline ?: "Health Connect oder andere Messspuren fuer diesen Bereich.",
                            testTag = "area-input-overview-source",
                            onClick = { activeSurface = AreaInputsSurface.Source },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Notizen",
                            value = onceValue,
                            supporting = if (importedMaterials.isEmpty()) "Kurze Beobachtungen, Zustandsnotizen oder kleine Funde festhalten." else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-once",
                            onClick = { activeSurface = AreaInputsSurface.MaterialCollect },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Screenshots",
                            value = when (screenshotCount) {
                                0 -> "Noch leer"
                                1 -> "1 Bild"
                                else -> "$screenshotCount Bilder"
                            },
                            supporting = if (screenshotCount == 0) "Charts, Schlaf- oder Gesundheitsansichten als Bild sichern." else "Bereits gesicherte Screenshots",
                            testTag = "area-input-overview-screenshots",
                            onClick = onAddImage,
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Bestand",
                            value = inventoryValue,
                            supporting = if (importedMaterials.isEmpty()) "Noch leer" else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-inventory",
                            onClick = { activeSurface = AreaInputsSurface.MaterialInventory },
                        )
                    }
                    isCollectionInbox -> {
                        AreaInputOverviewRow(
                            label = "Links",
                            value = if (importedMaterials.any { it.kind == AreaImportKind.Link }) onceValue else "Noch leer",
                            supporting = "Lose Links direkt in die Inbox sichern.",
                            testTag = "area-input-overview-source",
                            onClick = {
                                linkDraft = ""
                                activeSurface = AreaInputsSurface.LinkEntry
                            },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Notizen",
                            value = onceValue,
                            supporting = if (importedMaterials.isEmpty()) "Kurze Notizen oder Funde sammeln." else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-once",
                            onClick = { activeSurface = AreaInputsSurface.MaterialCollect },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Dateien",
                            value = if (importedMaterials.any { it.kind == AreaImportKind.File }) "Vorhanden" else "Noch leer",
                            supporting = "Dateien oder Exporte direkt in die Inbox holen.",
                            testTag = "area-input-overview-files",
                            onClick = onAddDocument,
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Screenshots",
                            value = when (screenshotCount) {
                                0 -> "Noch leer"
                                1 -> "1 Bild"
                                else -> "$screenshotCount Bilder"
                            },
                            supporting = if (screenshotCount == 0) "Screenshots, Snippets oder lose Ideen als Bild sichern." else "Bereits gesicherte Screenshots",
                            testTag = "area-input-overview-screenshots",
                            onClick = onAddImage,
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Bestand",
                            value = inventoryValue,
                            supporting = if (importedMaterials.isEmpty()) "Noch leer" else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-inventory",
                            onClick = { activeSurface = AreaInputsSurface.MaterialInventory },
                        )
                    }
                    else -> {
                        AreaInputOverviewRow(
                            label = "Quelle",
                            value = sourceValue,
                            supporting = sourceSetup?.headline ?: "Noch frei",
                            testTag = "area-input-overview-source",
                            onClick = { activeSurface = AreaInputsSurface.Source },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Direkt",
                            value = onceValue,
                            supporting = when {
                                importedMaterials.isNotEmpty() -> "${importedMaterials.size} Import${if (importedMaterials.size == 1) "" else "e"} vorhanden"
                                else -> "Links, Dateien, Bilder"
                            },
                            testTag = "area-input-overview-once",
                            onClick = { activeSurface = AreaInputsSurface.MaterialCollect },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Laufend",
                            value = ongoingValue,
                            supporting = if (webFeedSync.sources.isEmpty()) "Feeds und Websites" else "${webFeedSync.sources.count { it.autoSyncEnabled }} mit Auto-Sync",
                            testTag = "area-input-overview-ongoing",
                            onClick = { activeSurface = AreaInputsSurface.MaterialSources },
                        )
                        HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                        AreaInputOverviewRow(
                            label = "Bestand",
                            value = inventoryValue,
                            supporting = if (importedMaterials.isEmpty()) "Noch leer" else buildInventorySummary(importedMaterials),
                            testTag = "area-input-overview-inventory",
                            onClick = { activeSurface = AreaInputsSurface.MaterialInventory },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaInputMaterialHub(
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    onOpenCollect: () -> Unit,
    onOpenSources: () -> Unit,
    onOpenInventory: () -> Unit,
) {
    val sourceCount = webFeedSync.sources.size
    val importCount = importedMaterials.size
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AreaInputOverviewRow(
                label = "Einmalig",
                value = if (importCount > 0 || sourceCount > 0) "Weiter" else "Starten",
                supporting = "Link, Datei oder Bild bewusst hineinholen.",
                testTag = "area-input-material-collect",
                onClick = onOpenCollect,
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
            )
            AreaInputOverviewRow(
                label = "Laufend",
                value = when (sourceCount) {
                    0 -> "Noch frei"
                    1 -> "1 aktiv"
                    else -> "$sourceCount aktiv"
                },
                supporting = if (sourceCount == 0) {
                    "Noch keine gemerkte Quelle verbunden."
                } else {
                    "${webFeedSync.sources.count { it.autoSyncEnabled }} Quelle${if (webFeedSync.sources.count { it.autoSyncEnabled } == 1) "" else "n"} laden bereits automatisch nach."
                },
                testTag = "area-input-material-sources",
                onClick = onOpenSources,
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
            )
            AreaInputOverviewRow(
                label = "Bestand",
                value = when (importCount) {
                    0 -> "Noch leer"
                    1 -> "1 Teil"
                    else -> "$importCount Teile"
                },
                supporting = if (importCount == 0) {
                    "Noch kein importiertes Material im Bereich."
                } else {
                    buildInventorySummary(importedMaterials)
                },
                testTag = "area-input-material-inventory",
                onClick = onOpenInventory,
            )
        }
    }
}

@Composable
private fun AreaInputCollectCard(
    onAddLink: () -> Unit,
    onAddDocument: () -> Unit,
    onAddImage: () -> Unit,
    onAddFeed: () -> Unit,
) {
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AreaInputOverviewRow(
                label = "Link",
                value = "Hinzufuegen",
                supporting = "Einzelne Website, Artikel oder URL in diesen Bereich holen.",
                testTag = "area-import-link",
                onClick = onAddLink,
            )
            HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
            AreaInputOverviewRow(
                label = "Datei",
                value = "Oeffnen",
                supporting = "Dokumente, Exporte oder Notizen aus dem System waehlen.",
                testTag = "area-import-document",
                onClick = onAddDocument,
            )
            HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
            AreaInputOverviewRow(
                label = "Bild",
                value = "Waehlen",
                supporting = "Fotos oder Screenshots in den Bereich holen.",
                testTag = "area-import-image",
                onClick = onAddImage,
            )
            HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
            AreaInputOverviewRow(
                label = "Feed",
                value = "Merken",
                supporting = "Eine Quelle eintragen, die spaeter neue Inhalte nachliefern darf.",
                testTag = "area-import-feed",
                onClick = onAddFeed,
            )
        }
    }
}

@Composable
private fun AreaInputSourcesCard(
    webFeedSync: AreaWebFeedSyncState,
    onRemoveFeed: (String) -> Unit,
    onSetFeedAutoSync: (String, Boolean) -> Unit,
    onSetFeedSyncCadence: (String, AreaWebFeedSyncCadence) -> Unit,
) {
    if (webFeedSync.sources.isEmpty()) {
        SettingsTileCard(
            title = "Noch frei",
            detail = "Noch keine gemerkte Quelle vorhanden.",
        )
        return
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        webFeedSync.sources.forEach { source ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
                ),
                shape = RoundedCornerShape(28.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AreaInputOverviewRow(
                        label = "Quelle",
                        value = source.sourceKindLabel,
                        supporting = source.hostLabel,
                        testTag = "area-feed-source-${source.url.hashCode()}",
                        enabled = false,
                        onClick = {},
                    )
                    HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area-feed-auto-${source.url.hashCode()}")
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Nachladen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (source.autoSyncEnabled) {
                                    "${source.syncCadenceLabel} · ${source.lastStatusLabel.ifBlank { "bereit" }}"
                                } else {
                                    "Noch manuell"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = source.autoSyncEnabled,
                            onCheckedChange = { enabled -> onSetFeedAutoSync(source.url, enabled) },
                        )
                    }
                    if (source.autoSyncEnabled) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AreaWebFeedSyncCadence.entries.forEach { cadence ->
                                val selected = source.syncCadenceLabel == cadence.label
                                OutlinedButton(
                                    onClick = { onSetFeedSyncCadence(source.url, cadence) },
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selected) AppTheme.colors.accent else AppTheme.colors.outlineSoft,
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (selected) AppTheme.colors.accent else AppTheme.colors.ink,
                                    ),
                                    modifier = Modifier.testTag("area-feed-cadence-${source.url.hashCode()}-${cadence.storageKey}"),
                                ) {
                                    Text(cadence.label)
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { onRemoveFeed(source.url) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("area-feed-remove-${source.url.hashCode()}"),
                    ) {
                        Text("Entfernen")
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaInputInventoryCard(
    importedMaterials: List<AreaImportedMaterialState>,
    onOpenItem: (AreaImportedMaterialState) -> Unit,
    onRemoveItem: (AreaImportedMaterialState) -> Unit,
) {
    if (importedMaterials.isEmpty()) {
        SettingsTileCard(
            title = "Noch leer",
            detail = "Noch kein importiertes Material im Bereich.",
        )
        return
    }
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            importedMaterials.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AreaInputOverviewRow(
                        label = item.inventoryLabel(),
                        value = if (item.kind == AreaImportKind.Text) "Lesen" else "Oeffnen",
                        supporting = item.title,
                        testTag = "area-import-open-${item.id}",
                        onClick = { onOpenItem(item) },
                    )
                    if (item.isPending) {
                        DaysMetaPill(label = "Wird gesichert")
                    }
                    if (item.detail.isNotBlank()) {
                        Text(
                            text = item.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp),
                        )
                    }
                    TextButton(
                        onClick = { onRemoveItem(item) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("area-import-remove-${item.id}"),
                    ) {
                        Text("Entfernen")
                    }
                }
            }
        }
    }
}

private fun buildInventorySummary(
    importedMaterials: List<AreaImportedMaterialState>,
): String {
    val counts = linkedMapOf<String, Int>()
    importedMaterials.forEach { item ->
        val label = item.inventoryLabel()
        counts[label] = counts.getOrDefault(label, 0) + 1
    }
    return counts.entries.joinToString(separator = ", ") { (label, count) ->
        "${count}x $label"
    }
}

private fun AreaImportedMaterialState.inventoryLabel(): String {
    if (kind != AreaImportKind.Link) return kind.label
    val normalizedReference = reference.lowercase()
    return when {
        "youtube.com" in normalizedReference || "youtu.be" in normalizedReference -> "YouTube"
        "instagram.com" in normalizedReference -> "Instagram"
        "x.com" in normalizedReference || "twitter.com" in normalizedReference -> "X"
        "tiktok.com" in normalizedReference -> "TikTok"
        "reddit.com" in normalizedReference || "redd.it" in normalizedReference -> "Reddit"
        else -> kind.label
    }
}

private fun List<AreaImportedMaterialState>.countImportedForHosts(vararg hosts: String): Int {
    if (hosts.isEmpty()) return 0
    return count { item ->
        val reference = item.reference.lowercase()
        hosts.any { host -> host in reference }
    }
}

private fun List<AreaWebFeedSourceState>.countFeedSourcesForHosts(vararg hosts: String): Int {
    if (hosts.isEmpty()) return 0
    return count { source ->
        val normalizedUrl = source.url.lowercase()
        val normalizedHost = source.hostLabel.lowercase()
        hosts.any { host -> host in normalizedUrl || host in normalizedHost }
    }
}

private fun newsSourceCountLabel(
    count: Int,
    singular: String,
    plural: String,
): String {
    return when (count) {
        0 -> "Noch nichts"
        1 -> "1 $singular"
        else -> "$count $plural"
    }
}

private fun isAppInstalled(
    context: android.content.Context,
    packageName: String,
): Boolean {
    return context.packageManager.getLaunchIntentForPackage(packageName) != null
}

@Composable
private fun AreaInputAutomationCard(
    area: StartAreaDetailState,
    webFeedSync: AreaWebFeedSyncState,
    onSyncWebFeed: () -> Unit,
) {
    val runtime = area.runtimeContract
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AreaInputOverviewRow(
                label = "Nachladen",
                value = when {
                    webFeedSync.isRunning -> "Laeuft"
                    webFeedSync.sources.any { it.autoSyncEnabled } -> "${webFeedSync.sources.count { it.autoSyncEnabled }} aktiv"
                    else -> "Noch ruhig"
                },
                supporting = webFeedSync.statusDetail.ifBlank { "Neue Inhalte koennen hier bewusst oder automatisch geholt werden." },
                testTag = "area-input-automation-sync",
                onClick = onSyncWebFeed,
            )
            HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
            AreaInputOverviewRow(
                label = "Cluster",
                value = if (runtime.capabilitySet.canClusterLocally) "Lokal" else "Noch offen",
                supporting = "Material gruppieren und Duplikate klein halten.",
                testTag = "area-input-automation-cluster",
                enabled = false,
                onClick = {},
            )
            HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
            AreaInputOverviewRow(
                label = "Kurztext",
                value = if (runtime.capabilitySet.canSummarizeLocally) "Lokal" else "Manuell",
                supporting = "Kurze heutige Klartexte statt langer Deutung.",
                testTag = "area-input-automation-summary",
                enabled = false,
                onClick = {},
            )
            HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f))
            AreaInputOverviewRow(
                label = "Suche",
                value = if (runtime.capabilitySet.canSearchLocally) "Bereit" else "Spaeter",
                supporting = "Lokales Wiederfinden ueber Material und spaetere Cluster.",
                testTag = "area-input-automation-search",
                enabled = false,
                onClick = {},
            )
        }
    }
}

@Composable
private fun AreaInputOverviewRow(
    label: String,
    value: String,
    supporting: String,
    testTag: String,
    enabled: Boolean = true,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag(testTag)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (compact) FontWeight.SemiBold else FontWeight.Normal,
                color = if (compact) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!compact && supporting.isNotBlank()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint = AppTheme.colors.accent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsTileCard(
    title: String,
    detail: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
private fun AreaInputSourceCard(
    sourceSetup: AreaSourceSetupState,
    onOpenSourceSettings: () -> Unit,
    onBindSource: (DataSourceKind) -> Unit,
    onUnbindSource: (DataSourceKind) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Verbundene Spur",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = sourceSetup.headline,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = sourceSetup.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (sourceSetup.canConnectSource) {
                    Button(onClick = { onBindSource(sourceSetup.sourceKind) }) {
                        Text(sourceSetup.primaryActionLabel ?: "Verbinden")
                    }
                } else {
                    Button(onClick = onOpenSourceSettings) {
                        Text(sourceSetup.primaryActionLabel ?: "Android oeffnen")
                    }
                }
                if (sourceSetup.canDisconnectSource) {
                    OutlinedButton(onClick = { onUnbindSource(sourceSetup.sourceKind) }) {
                        Text(sourceSetup.secondaryActionLabel ?: "Trennen")
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaInputProcessingCard(
    area: StartAreaDetailState,
) {
    val runtime = area.runtimeContract
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Klar im Blick",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = runtime.summaryProfile.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            listOf(
                "Duplikate" to if (runtime.capabilitySet.canClusterLocally) "lokal moeglich" else "noch offen",
                "Kurztext" to if (runtime.capabilitySet.canSummarizeLocally) "lokal moeglich" else "manuell",
                "Suche" to if (runtime.capabilitySet.canSearchLocally) "spaeter lokal" else "noch ruhig",
            ).forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.accent,
                    )
                }
            }
            runtime.provenances.forEach { provenance ->
                Text(
                    text = when (provenance.sourceClass) {
                        com.struperto.androidappdays.domain.area.AreaSourceClass.DEVICE_ONLY ->
                            "Geraet: ${provenance.goal}"
                        com.struperto.androidappdays.domain.area.AreaSourceClass.USER_PICKED_PROVIDER ->
                            "Vom Nutzer gewaehlt: ${provenance.goal}"
                        com.struperto.androidappdays.domain.area.AreaSourceClass.CLOUD_ACCOUNT ->
                            "Konto: ${provenance.goal}"
                        com.struperto.androidappdays.domain.area.AreaSourceClass.MANUAL_INPUT ->
                            "Manuell: ${provenance.goal}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AreaWorkspaceSignalRow(
    signal: AreaWorkspaceSignal,
    palette: AreaWorkspacePalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(width = 1.dp, color = palette.outline, shape = RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(signal.testTag)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = signal.label,
                style = MaterialTheme.typography.labelLarge,
                color = palette.support,
            )
            Text(
                text = signal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = palette.ink,
            )
            Text(
                text = signal.detail,
                style = MaterialTheme.typography.bodySmall,
                color = palette.support,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = palette.ink,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun AreaAuthoringScreen(
    area: StartAreaDetailState,
    authoring: AreaAuthoringStudioState,
    analysis: AreaMachineAnalysisState,
    analysisAction: DaysTopBarAction,
    onBack: () -> Unit,
    onLageModeChange: (String, String) -> Unit,
    onDirectionModeChange: (String, String) -> Unit,
    onSourcesModeChange: (String, String) -> Unit,
    onFlowProfileChange: (String, String) -> Unit,
    onComplexityLevelChange: (String, String) -> Unit,
    onVisibilityLevelChange: (String, String) -> Unit,
) {
    DaysPageScaffold(
        title = "Ziel",
        onBack = onBack,
        modifier = Modifier.testTag("area-authoring-screen"),
        action = analysisAction,
    ) {
        AreaAnalysisInlineCard(analysis = analysis)
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
                    text = "Ziel dieses Bereichs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = authoring.basisLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = authoring.basisSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    authoring.previewAxes.forEach { axis ->
                        AreaAuthoringPreviewChip(axis = axis)
                    }
                }
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
private fun AreaAuthoringPreviewChip(
    axis: AreaAuthoringAxisState,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.78f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = "${axis.label}: ${axis.valueLabel}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private enum class AreaDiagnosticTone {
    Good,
    Notice,
    Warning,
}

private data class AreaDiagnosticItem(
    val label: String,
    val title: String,
    val detail: String,
    val tone: AreaDiagnosticTone,
)

private data class AreaDiagnosticStarterStep(
    val title: String,
    val detail: String,
)

@Composable
private fun AreaDiagnosticsSummaryCard(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    onOpenDiagnostics: () -> Unit,
) {
    val items = remember(area, sourceSetup, importedMaterials) {
        buildAreaDiagnostics(
            area = area,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area-diagnostics-card"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Analyse kurz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = items.firstOrNull()?.title ?: "Der Analyse-Stand dieses Bereichs ist aktuell unauffaellig.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items.take(2).forEach { item ->
                AreaDiagnosticRow(item = item)
            }
            TextButton(
                onClick = onOpenDiagnostics,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Analyse oeffnen")
            }
        }
    }
}

@Composable
private fun AreaDiagnosticsScreen(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    analysis: AreaMachineAnalysisState,
    analysisAction: DaysTopBarAction,
    onBack: () -> Unit,
) {
    val items = remember(area, sourceSetup, importedMaterials) {
        buildAreaDiagnostics(
            area = area,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
        )
    }
    val starterSteps = remember(area, sourceSetup, importedMaterials) {
        buildAreaDiagnosticStarterSteps(
            area = area,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
        )
    }
    DaysPageScaffold(
        title = "Analyse",
        onBack = onBack,
        modifier = Modifier.testTag("area-diagnostics-screen"),
        action = analysisAction,
    ) {
        AreaAnalysisInlineCard(analysis = analysis)
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Was jetzt zaehlt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                starterSteps.forEach { step ->
                    CompactDiagnosticStep(step = step)
                }
            }
        }
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Aktueller Analyse-Stand",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.outlineSoft.copy(alpha = 0.55f),
                        )
                    }
                    AreaDiagnosticRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun AreaAnalysisInlineCard(
    analysis: AreaMachineAnalysisState,
) {
    if (!analysis.isRefreshing) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.94f),
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DaysMetaPill(label = "Analyse")
            Text(
                text = "Ich ordne Typ, aktuellen Zustand, Ziel und Hinzufuegen gerade neu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun AreaMachineAnalysisScreen(
    area: StartAreaDetailState,
    analysis: AreaMachineAnalysisState,
    onBack: () -> Unit,
) {
    DaysPageScaffold(
        title = "Analyse",
        onBack = onBack,
        modifier = Modifier.testTag("area-analysis-screen"),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF050505),
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
                    text = "AREA ${area.title.uppercase()}",
                    style = AppTheme.typography.mono,
                    color = Color(0xFF7CFFB2),
                )
                Text(
                    text = "Snapshot v${analysis.snapshotVersion} · ${analysis.lastAnalyzedAtLabel.ifBlank { "Neu" }} · ${analysis.refreshReasonLabel}",
                    style = AppTheme.typography.mono,
                    color = Color(0xFF97A1AB),
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color(0xFF1D252B),
                )
                AreaAnalysisTerminalRow(label = "TYPE", value = analysis.typeLabel)
                AreaAnalysisTerminalRow(label = "ROUTE", value = analysis.routeLabel)
                AreaAnalysisTerminalRow(label = "STATE", value = analysis.statusLabel)
                AreaAnalysisTerminalRow(label = "CONF", value = analysis.confidenceLabel)
                AreaAnalysisTerminalRow(label = "NEXT", value = analysis.nextGoalLabel)
                Text(
                    text = analysis.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD8E0E6),
                )
                AreaAnalysisTerminalSection(section = analysis.currentState)
                AreaAnalysisTerminalSection(section = analysis.goalState)
                AreaAnalysisTerminalSection(section = analysis.inputState)
                AreaAnalysisTerminalSection(section = analysis.analysisState)
                AreaAnalysisTerminalCollection(
                    title = "EVIDENCE",
                    items = analysis.evidenceItems.map { item ->
                        item.label to "${item.stateLabel} · ${item.detail}"
                    },
                )
                AreaAnalysisTerminalCollection(
                    title = "SIGNALS",
                    items = analysis.signalItems.map { item ->
                        item.label to "${item.value} · ${item.detail}"
                    },
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0B1014))
                        .border(1.dp, Color(0xFF1C2A31), RoundedCornerShape(22.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "PAYLOAD",
                        style = AppTheme.typography.mono,
                        color = Color(0xFF7CFFB2),
                    )
                    Text(
                        text = analysis.machinePayload,
                        style = AppTheme.typography.mono,
                        color = Color(0xFFC4CDD5),
                        modifier = Modifier.testTag("area-analysis-machine-payload"),
                    )
                }
            }
        }
    }
}

@Composable
private fun AreaAnalysisTerminalRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.mono,
            color = Color(0xFF7CFFB2),
            modifier = Modifier.weight(0.28f),
        )
        Text(
            text = value,
            style = AppTheme.typography.mono,
            color = Color(0xFFE7EEF4),
            modifier = Modifier.weight(0.72f),
        )
    }
}

@Composable
private fun AreaAnalysisTerminalSection(
    section: AreaAnalysisSectionState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0B1014))
            .border(1.dp, Color(0xFF1C2A31), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = section.label.uppercase(),
            style = AppTheme.typography.mono,
            color = Color(0xFF7CFFB2),
        )
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFF5F7F9),
        )
        if (section.supportingLabel.isNotBlank()) {
            Text(
                text = section.supportingLabel,
                style = AppTheme.typography.mono,
                color = Color(0xFF97A1AB),
            )
        }
        Text(
            text = section.detail,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFD0D8DF),
        )
    }
}

@Composable
private fun AreaAnalysisTerminalCollection(
    title: String,
    items: List<Pair<String, String>>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0B1014))
            .border(1.dp, Color(0xFF1C2A31), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.mono,
            color = Color(0xFF7CFFB2),
        )
        items.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color(0xFF1D252B),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.first,
                    style = AppTheme.typography.mono,
                    color = Color(0xFFE7EEF4),
                )
                Text(
                    text = item.second,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD0D8DF),
                )
            }
        }
    }
}

private fun buildAreaDiagnostics(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<AreaDiagnosticItem> {
    val statusItem = when {
        area.profileState.lageMode == AreaLageMode.State -> AreaDiagnosticItem(
            label = "Aktueller Zustand",
            title = "Aktueller Zustand ist hier manuell fuehrbar",
            detail = "Dieser Bereich lebt eher von gesetzten Zustaenden als von einer dauernden Bewertungsskala.",
            tone = AreaDiagnosticTone.Notice,
        )

        area.todayOutput.sourceTruth == com.struperto.androidappdays.domain.area.AreaSourceTruth.missing -> AreaDiagnosticItem(
            label = "Aktueller Zustand",
            title = "Aktueller Zustand ist noch nicht belastbar",
            detail = "Es fehlt frische Evidenz oder ein klarer Materialpfad, daher zeigt Days nur einen vorlaeufigen Zustand.",
            tone = AreaDiagnosticTone.Warning,
        )

        else -> AreaDiagnosticItem(
            label = "Aktueller Zustand",
            title = "Aktueller Zustand ist technisch lesbar",
            detail = "Days kann fuer diesen Bereich bereits einen aktuellen Zustand aus lokaler Evidenz ableiten.",
            tone = AreaDiagnosticTone.Good,
        )
    }
    val sourceItem = sourceSetup?.let { setup ->
        val sourceName = when (setup.sourceKind) {
            DataSourceKind.CALENDAR -> "Kalender"
            DataSourceKind.NOTIFICATIONS -> "Benachrichtigungen"
            DataSourceKind.HEALTH_CONNECT -> "Health Connect"
            DataSourceKind.MANUAL -> "Manuell"
        }
        AreaDiagnosticItem(
            label = "Hinzufuegen",
            title = "$sourceName: ${sourceSetupStatusLabel(setup.status)}",
            detail = setup.detail,
            tone = if (setup.isWarning) AreaDiagnosticTone.Warning else AreaDiagnosticTone.Good,
        )
    } ?: AreaDiagnosticItem(
        label = "Hinzufuegen",
        title = "Noch keine feste Quelle",
        detail = "Der Bereich kann frei starten, wird aber klarer, sobald eine echte Spur oder Eingabe fest verdrahtet ist.",
        tone = AreaDiagnosticTone.Notice,
    )
    val flowItem = AreaDiagnosticItem(
        label = "Analyse",
        title = if (area.flowCount > 0) "${area.flowCount} Analyse-Routinen aktiv" else "Analyse ist noch ruhig",
        detail = if (area.flowCount > 0) {
            "Aktive Erinnerungen, Reviews oder Experimente greifen bereits in diesen Bereich ein."
        } else {
            "Weitere Flows oder Skills koennen spaeter hier andocken, aktuell bleibt der Bereich bewusst schlank."
        },
        tone = if (area.flowCount > 0) AreaDiagnosticTone.Good else AreaDiagnosticTone.Notice,
    )
    val materialItem = AreaDiagnosticItem(
        label = "Hinzufuegen",
        title = if (importedMaterials.isEmpty()) {
            "Noch kein Zusatzinhalt"
        } else {
            "${importedMaterials.size} Import${if (importedMaterials.size == 1) "" else "e"} vorhanden"
        },
        detail = if (importedMaterials.isEmpty()) {
            "Links, Bilder oder Dateien fehlen noch. Das ist okay, solange die Quelle selbst schon genug Aussagekraft hat."
        } else {
            "Importiertes Material liegt vor und kann spaeter fuer Skills oder weitere Auswertung genutzt werden."
        },
        tone = if (importedMaterials.isEmpty()) AreaDiagnosticTone.Notice else AreaDiagnosticTone.Good,
    )
    return listOf(statusItem, sourceItem, flowItem, materialItem)
}

private fun buildAreaDiagnosticStarterSteps(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<AreaDiagnosticStarterStep> {
    val steps = buildList {
        when {
            sourceSetup == null -> add(
                AreaDiagnosticStarterStep(
                    title = "Hinzufuegen klaeren",
                    detail = "Lege fest, ob der Bereich frei bleibt oder einen klaren Materialpfad bekommen soll.",
                ),
            )

            sourceSetup.status != AreaSourceSetupStatus.READY -> add(
                AreaDiagnosticStarterStep(
                    title = sourceSetup.primaryActionLabel ?: "Quelle verbinden",
                    detail = sourceSetup.detail,
                ),
            )
        }
        if (importedMaterials.isEmpty()) {
            add(
                AreaDiagnosticStarterStep(
                    title = "Erstes Material sammeln",
                    detail = "Ein Link, Bild oder Dokument reicht, damit dieser Bereich schnell belastbarer wird.",
                ),
            )
        }
        if (area.flowCount == 0) {
            add(
                AreaDiagnosticStarterStep(
                    title = "Analyse klein halten",
                    detail = "Starte zuerst nur mit Review oder Hinweisen, bevor weitere Skills andocken.",
                ),
            )
        }
    }
    return steps.ifEmpty {
        listOf(
            AreaDiagnosticStarterStep(
                title = "Analyse wirkt stabil",
                detail = "Aktueller Zustand, Hinzufuegen und Analyse greifen fuer den Moment schluessig ineinander.",
            ),
        )
    }
}

@Composable
private fun AreaDiagnosticRow(
    item: AreaDiagnosticItem,
) {
    val toneColor = when (item.tone) {
        AreaDiagnosticTone.Good -> AppTheme.colors.success
        AreaDiagnosticTone.Notice -> AppTheme.colors.warning
        AreaDiagnosticTone.Warning -> AppTheme.colors.danger
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelLarge,
            color = toneColor,
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = item.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CompactDiagnosticStep(
    step: AreaDiagnosticStarterStep,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = step.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = step.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
private fun AreaPanelScreen(
    area: StartAreaDetailState,
    panelState: StartAreaPanelState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
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
    onOpenInputs: () -> Unit,
) {
    var activeSheetActionId by rememberSaveable(area.areaId, panelState.panel.name) {
        mutableStateOf<StartPanelActionId?>(null)
    }
    val effectiveActions = remember(
        area,
        panelState,
        sourceSetup,
        importedMaterials,
        webFeedSync,
    ) {
        resolvePanelActions(
            area = area,
            panelState = panelState,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
            webFeedSync = webFeedSync,
        )
    }
    val activeAction = effectiveActions.firstOrNull { it.id == activeSheetActionId }

    if (activeAction?.sheet != null) {
        StartPanelActionEditorScreen(
            action = activeAction,
            onBack = { activeSheetActionId = null },
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
        return
    }

    DaysPageScaffold(
        title = panelState.title,
        onBack = onBack,
        modifier = Modifier.testTag("area-panel-screen"),
    ) {
        val onActionClick: (StartPanelActionState) -> Unit = { action ->
            when (action.mode) {
                StartPanelActionMode.Sheet -> activeSheetActionId = action.id
                StartPanelActionMode.Direct -> applyDirectPanelAction(
                    area = area,
                    actionId = action.id,
                    onClearSnapshot = onClearSnapshot,
                )
            }
        }
        if (isNewsMediumArea(area)) {
            NewsMediumPanelPage(
                area = area,
                panelState = panelState,
                sourceSetup = sourceSetup,
                importedMaterials = importedMaterials,
                webFeedSync = webFeedSync,
                actions = effectiveActions,
                onActionClick = onActionClick,
                onOpenInputs = onOpenInputs,
            )
        } else if (isPersonContactArea(area)) {
            PersonContactPanelPage(
                panelState = panelState,
                actions = effectiveActions,
                onActionClick = onActionClick,
                onOpenInputs = onOpenInputs,
            )
        } else if (isProjectWorkArea(area)) {
            ProjectWorkPanelPage(
                panelState = panelState,
                actions = effectiveActions,
                onActionClick = onActionClick,
                onOpenInputs = onOpenInputs,
            )
        } else if (isPlaceContextArea(area)) {
            PlaceContextPanelPage(
                panelState = panelState,
                actions = effectiveActions,
                onActionClick = onActionClick,
                onOpenInputs = onOpenInputs,
            )
        } else if (isHealthRitualArea(area)) {
            HealthRitualPanelPage(
                panelState = panelState,
                actions = effectiveActions,
                onActionClick = onActionClick,
                onOpenInputs = onOpenInputs,
            )
        } else if (isCollectionInboxArea(area)) {
            CollectionInboxPanelPage(
                panelState = panelState,
                actions = effectiveActions,
                onActionClick = onActionClick,
                onOpenInputs = onOpenInputs,
            )
        } else {
            StartPanelPage(
                areaFamily = area.family,
                panelState = panelState,
                sourceSetup = sourceSetup,
                importedMaterials = importedMaterials,
                onActionClick = onActionClick,
            )
        }
    }
}

private fun resolvePanelActions(
    area: StartAreaDetailState,
    panelState: StartAreaPanelState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<StartPanelActionState> {
    return when {
        isNewsMediumArea(area) -> when (panelState.panel) {
            StartAreaPanel.Path -> newsMediumFocusActions(area)
            StartAreaPanel.Options -> newsMediumTaktActions(area, webFeedSync)
            StartAreaPanel.Snapshot -> newsMediumStandActions(area, sourceSetup, importedMaterials, webFeedSync)
            StartAreaPanel.Sources -> panelState.screenState.actions
        }
        isPlaceContextArea(area) -> when (panelState.panel) {
            StartAreaPanel.Path -> placeContextFocusActions(area)
            StartAreaPanel.Options -> placeContextTaktActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Snapshot -> placeContextStandActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Sources -> panelState.screenState.actions
        }
        isHealthRitualArea(area) -> when (panelState.panel) {
            StartAreaPanel.Path -> healthRitualFocusActions(area)
            StartAreaPanel.Options -> healthRitualTaktActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Snapshot -> healthRitualStandActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Sources -> panelState.screenState.actions
        }
        isCollectionInboxArea(area) -> when (panelState.panel) {
            StartAreaPanel.Path -> collectionInboxFocusActions(area)
            StartAreaPanel.Options -> collectionInboxTaktActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Snapshot -> collectionInboxStandActions(area, importedMaterials)
            StartAreaPanel.Sources -> panelState.screenState.actions
        }
        isPersonContactArea(area) -> when (panelState.panel) {
            StartAreaPanel.Path -> personContactFocusActions(area)
            StartAreaPanel.Options -> personContactTaktActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Snapshot -> personContactStandActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Sources -> panelState.screenState.actions
        }
        isProjectWorkArea(area) -> when (panelState.panel) {
            StartAreaPanel.Path -> projectWorkFocusActions(area)
            StartAreaPanel.Options -> projectWorkTaktActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Snapshot -> projectWorkStandActions(area, sourceSetup, importedMaterials)
            StartAreaPanel.Sources -> panelState.screenState.actions
        }
        else -> panelState.screenState.actions
    }
}

private fun newsMediumStandActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
): List<StartPanelActionState> {
    val selectedSources = newsSelectedSourceLabels(importedMaterials, webFeedSync)
    val syncedSource = webFeedSync.sources.firstOrNull { it.lastStatusLabel.isNotBlank() }
    val lastSignalValue = syncedSource?.let { source ->
        "${shortHost(source.url)} · ${source.lastStatusLabel}"
    } ?: sourceSetup?.headline
        ?: importedMaterials.firstOrNull()?.title
        ?: if (webFeedSync.sources.isNotEmpty()) "${webFeedSync.sources.size} Feed${if (webFeedSync.sources.size == 1) "" else "s"}" else "Noch keines"
    val recognizedValue = selectedSources.joinToString(" · ").ifBlank { "Noch nichts" }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.SnapshotMode,
            label = "Quellen",
            valueLabel = recognizedValue,
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotScore,
            label = "Letzter Fund",
            valueLabel = lastSignalValue,
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotState,
            label = "Status",
            valueLabel = if (selectedSources.isNotEmpty()) "Bereit" else area.todayOutput.statusLabel.ifBlank { "Offen" },
            mode = StartPanelActionMode.Direct,
        ),
    )
}

private fun newsMediumFocusActions(
    area: StartAreaDetailState,
): List<StartPanelActionState> {
    val trackOptions = area.tracks.map { track ->
        StartPanelOptionState(
            id = track,
            label = track,
            selected = area.focusTrack == track,
            supportingLabel = newsMediumTrackSupportingLabel(track),
        )
    }
    val selectionOptions = area.tracks.map { track ->
        StartPanelOptionState(
            id = track,
            label = track,
            selected = track in area.selectedTracks,
            supportingLabel = newsMediumTrackSupportingLabel(track),
        )
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.PathFocus,
            label = "Zuerst",
            valueLabel = area.focusTrack,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Zuerst",
                selectionMode = StartPanelSelectionMode.Single,
                options = trackOptions,
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesTracks,
            label = "Mitlesen",
            valueLabel = when (area.selectedTracks.size) {
                0 -> "Noch offen"
                1 -> area.selectedTracks.first()
                else -> "${area.selectedTracks.size} Spuren"
            },
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Mitlesen",
                selectionMode = StartPanelSelectionMode.Multiple,
                options = selectionOptions,
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesMode,
            label = "Sortierung",
            valueLabel = area.profileState.sourcesLabel,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Sortierung",
                selectionMode = StartPanelSelectionMode.Single,
                options = newsMediumSourcesModeOptions(area.profileState.sourcesMode),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.PathCadence,
            label = "Zeitraum",
            valueLabel = cadenceLabel(area.cadence),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Zeitraum",
                selectionMode = StartPanelSelectionMode.Single,
                options = newsMediumCadenceOptions(area.cadence),
            ),
        ),
    )
}

private fun newsMediumTaktActions(
    area: StartAreaDetailState,
    webFeedSync: AreaWebFeedSyncState,
): List<StartPanelActionState> {
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.FlowProfile,
            label = "Stil",
            valueLabel = area.profileState.flowLabel,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Stil",
                selectionMode = StartPanelSelectionMode.Single,
                options = newsMediumFlowProfileOptions(area.profileState.flowProfile),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowIntensity,
            label = "Dichte",
            valueLabel = newsMediumIntensityLabel(area.intensity),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Dichte",
                selectionMode = StartPanelSelectionMode.Single,
                options = newsMediumFlowIntensityOptions(area.intensity),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotMode,
            label = "Nachladen",
            valueLabel = when {
                webFeedSync.isRunning -> "Laeuft"
                webFeedSync.sources.any { it.autoSyncEnabled } -> "Automatisch"
                webFeedSync.sources.isNotEmpty() -> "Manuell"
                else -> "Noch frei"
            },
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowSwitches,
            label = "Wiederkehr",
            valueLabel = buildList {
                if (area.reviewEnabled) add("Rueckblick")
                if (area.remindersEnabled) add("Erinnern")
                if (area.experimentsEnabled) add("Experimente")
            }.joinToString(" · ").ifBlank { "Aus" },
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Wiederkehr",
                selectionMode = StartPanelSelectionMode.Multiple,
                options = newsMediumFlowSwitchOptions(area),
            ),
        ),
    )
}

private fun newsMediumTrackSupportingLabel(track: String): String {
    return when (track) {
        "Quellen" -> "Welche Quellen zuerst ziehen."
        "Themen" -> "Welche Themen nach vorn kommen."
        "Accounts" -> "Welche Accounts oder Personen wichtig sind."
        "Formate" -> "Ob erst Text oder Bild fuehrt."
        else -> ""
    }
}

private fun newsMediumSourcesModeOptions(
    selected: AreaSourcesMode,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaSourcesMode.Balanced.persistedValue,
            label = "Offen",
            selected = selected == AreaSourcesMode.Balanced,
            supportingLabel = "Mehrere Signale bleiben nebeneinander sichtbar.",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Curated.persistedValue,
            label = "Gefuehrt",
            selected = selected == AreaSourcesMode.Curated,
            supportingLabel = "Wenige, bewusst gesetzte Spuren fuehren zuerst.",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Signals.persistedValue,
            label = "Signalnah",
            selected = selected == AreaSourcesMode.Signals,
            supportingLabel = "Frische Signale duerfen staerker nach vorn.",
        ),
    )
}

private fun newsMediumCadenceOptions(
    selected: String,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = "daily",
            label = "Taeglich",
            selected = selected == "daily",
            supportingLabel = "Der Bereich schaut zuerst auf heute.",
        ),
        StartPanelOptionState(
            id = "weekly",
            label = "Woechentlich",
            selected = selected == "weekly",
            supportingLabel = "Der Bereich sammelt eher ueber mehrere Tage.",
        ),
        StartPanelOptionState(
            id = "adaptive",
            label = "Adaptiv",
            selected = selected == "adaptive",
            supportingLabel = "Das Fenster passt sich Material und Lage an.",
        ),
    )
}

private fun newsMediumFlowProfileOptions(
    selected: AreaFlowProfile,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaFlowProfile.Stable.persistedValue,
            label = "Ruhig",
            selected = selected == AreaFlowProfile.Stable,
            supportingLabel = "Nachladen und Rueckkehr bleiben leise.",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Supportive.persistedValue,
            label = "Tragend",
            selected = selected == AreaFlowProfile.Supportive,
            supportingLabel = "Neue Signale bleiben sichtbar, ohne zu draengen.",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Active.persistedValue,
            label = "Aktiv",
            selected = selected == AreaFlowProfile.Active,
            supportingLabel = "Der Bereich meldet sich spuerbarer zurueck.",
        ),
    )
}

private fun newsMediumFlowIntensityOptions(
    selected: Int,
): List<StartPanelOptionState> {
    return listOf(
        1 to "Sehr leicht",
        2 to "Leicht",
        3 to "Mittel",
        4 to "Dicht",
        5 to "Klar",
    ).map { (value, label) ->
        StartPanelOptionState(
            id = value.toString(),
            label = label,
            selected = selected == value,
            supportingLabel = when (value) {
                1 -> "Nur wenig wird gleichzeitig gezeigt."
                2 -> "Mehrere Hinweise bleiben locker gebuendelt."
                3 -> "Ein normaler ruhiger Feed."
                4 -> "Mehr Material bleibt gleichzeitig sichtbar."
                else -> "Der Bereich zieht sichtbarer und dichter nach vorn."
            },
        )
    }
}

private fun newsMediumIntensityLabel(value: Int): String {
    return when (value) {
        1 -> "Sehr leicht"
        2 -> "Leicht"
        3 -> "Mittel"
        4 -> "Dicht"
        else -> "Klar"
    }
}

private fun newsMediumFlowSwitchOptions(
    area: StartAreaDetailState,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = "review",
            label = "Rueckblick",
            selected = area.reviewEnabled,
            supportingLabel = "Der Bereich darf alte Signale kurz wieder hochholen.",
        ),
        StartPanelOptionState(
            id = "reminders",
            label = "Erinnern",
            selected = area.remindersEnabled,
            supportingLabel = "Wichtige offene Punkte duerfen wieder auftauchen.",
        ),
        StartPanelOptionState(
            id = "experiments",
            label = "Experimente",
            selected = area.experimentsEnabled,
            supportingLabel = "Neue Sortierungen koennen vorsichtig mitlaufen.",
        ),
    )
}

private fun personContactStandActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val lastContactValue = sourceSetup?.headline
        ?: importedMaterials.firstOrNull()?.title
        ?: "Noch keiner"
    val openValue = when (importedMaterials.size) {
        0 -> "Noch nichts"
        1 -> "1 Notiz"
        else -> "${importedMaterials.size} Spuren"
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.SnapshotState,
            label = "Status",
            valueLabel = area.todayOutput.statusLabel.ifBlank { "Offen" },
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotScore,
            label = "Letzter Kontakt",
            valueLabel = lastContactValue,
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotMode,
            label = "Offen",
            valueLabel = openValue,
            mode = StartPanelActionMode.Direct,
        ),
    )
}

private fun personContactFocusActions(
    area: StartAreaDetailState,
): List<StartPanelActionState> {
    val trackOptions = area.tracks.map { track ->
        StartPanelOptionState(
            id = track,
            label = track,
            selected = area.focusTrack == track,
            supportingLabel = personContactTrackSupportingLabel(track),
        )
    }
    val selectionOptions = area.tracks.map { track ->
        StartPanelOptionState(
            id = track,
            label = track,
            selected = track in area.selectedTracks,
            supportingLabel = personContactTrackSupportingLabel(track),
        )
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.PathFocus,
            label = "Personen",
            valueLabel = area.focusTrack,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Personen",
                selectionMode = StartPanelSelectionMode.Single,
                options = trackOptions,
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesTracks,
            label = "Auswahl",
            valueLabel = when (area.selectedTracks.size) {
                0 -> "Noch offen"
                1 -> area.selectedTracks.first()
                else -> "${area.selectedTracks.size} aktiv"
            },
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Auswahl",
                selectionMode = StartPanelSelectionMode.Multiple,
                options = selectionOptions,
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesMode,
            label = "Antworten",
            valueLabel = area.profileState.sourcesLabel,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Antworten",
                selectionMode = StartPanelSelectionMode.Single,
                options = personContactSourcesModeOptions(area.profileState.sourcesMode),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.PathCadence,
            label = "Naehe",
            valueLabel = personContactCadenceLabel(area.cadence),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Naehe",
                selectionMode = StartPanelSelectionMode.Single,
                options = personContactCadenceOptions(area.cadence),
            ),
        ),
    )
}

private fun personContactTaktActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val signalState = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Aktiv"
        importedMaterials.isNotEmpty() -> "Manuell"
        else -> "Kein Signal"
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.FlowProfile,
            label = "Rhythmus",
            valueLabel = personContactFlowProfileLabel(area.profileState.flowProfile),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Rhythmus",
                selectionMode = StartPanelSelectionMode.Single,
                options = personContactFlowProfileOptions(area.profileState.flowProfile),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowIntensity,
            label = "Rueckkehr",
            valueLabel = "${area.intensity}/5",
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Rueckkehr",
                selectionMode = StartPanelSelectionMode.Single,
                options = personContactFlowIntensityOptions(area.intensity),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotMode,
            label = "Signale",
            valueLabel = signalState,
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowSwitches,
            label = "Extras",
            valueLabel = flowSwitchSummary(area),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Extras",
                selectionMode = StartPanelSelectionMode.Multiple,
                options = personContactFlowSwitchOptions(area),
            ),
        ),
    )
}

private fun projectWorkStandActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val nextStepValue = area.todayStepLabel.ifBlank {
        sourceSetup?.headline ?: importedMaterials.firstOrNull()?.title ?: "Noch offen"
    }
    val openValue = when {
        importedMaterials.isNotEmpty() -> "${importedMaterials.size} Material"
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Quelle"
        else -> "Leer"
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.SnapshotState,
            label = "Status",
            valueLabel = area.todayOutput.statusLabel.ifBlank { "Offen" },
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotScore,
            label = "Naechster Zug",
            valueLabel = nextStepValue,
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotMode,
            label = "Offen",
            valueLabel = openValue,
            mode = StartPanelActionMode.Direct,
        ),
    )
}

private fun projectWorkFocusActions(
    area: StartAreaDetailState,
): List<StartPanelActionState> {
    val trackOptions = area.tracks.map { track ->
        StartPanelOptionState(
            id = track,
            label = track,
            selected = area.focusTrack == track,
            supportingLabel = projectWorkTrackSupportingLabel(track),
        )
    }
    val selectionOptions = area.tracks.map { track ->
        StartPanelOptionState(
            id = track,
            label = track,
            selected = track in area.selectedTracks,
            supportingLabel = projectWorkTrackSupportingLabel(track),
        )
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.PathFocus,
            label = "Vorne",
            valueLabel = area.focusTrack,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Vorne",
                selectionMode = StartPanelSelectionMode.Single,
                options = trackOptions,
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesTracks,
            label = "Auswahl",
            valueLabel = when (area.selectedTracks.size) {
                0 -> "Noch offen"
                1 -> area.selectedTracks.first()
                else -> "${area.selectedTracks.size} Spuren"
            },
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Auswahl",
                selectionMode = StartPanelSelectionMode.Multiple,
                options = selectionOptions,
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesMode,
            label = "Ordnung",
            valueLabel = projectWorkSourcesModeLabel(area.profileState.sourcesMode),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Ordnung",
                selectionMode = StartPanelSelectionMode.Single,
                options = projectWorkSourcesModeOptions(area.profileState.sourcesMode),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.PathCadence,
            label = "Horizont",
            valueLabel = projectWorkCadenceLabel(area.cadence),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Horizont",
                selectionMode = StartPanelSelectionMode.Single,
                options = projectWorkCadenceOptions(area.cadence),
            ),
        ),
    )
}

private fun projectWorkTaktActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val revisitValue = when {
        area.remindersEnabled -> "Aktiv"
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Bereit"
        importedMaterials.isNotEmpty() -> "Manuell"
        else -> "Aus"
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.FlowProfile,
            label = "Rhythmus",
            valueLabel = projectWorkFlowProfileLabel(area.profileState.flowProfile),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Rhythmus",
                selectionMode = StartPanelSelectionMode.Single,
                options = projectWorkFlowProfileOptions(area.profileState.flowProfile),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowIntensity,
            label = "Zugkraft",
            valueLabel = "${area.intensity}/5",
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Zugkraft",
                selectionMode = StartPanelSelectionMode.Single,
                options = projectWorkFlowIntensityOptions(area.intensity),
            ),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SnapshotMode,
            label = "Wiedervorlage",
            valueLabel = revisitValue,
            mode = StartPanelActionMode.Direct,
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowSwitches,
            label = "Extras",
            valueLabel = flowSwitchSummary(area),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState(
                title = "Extras",
                selectionMode = StartPanelSelectionMode.Multiple,
                options = projectWorkFlowSwitchOptions(area),
            ),
        ),
    )
}

private fun personContactTrackSupportingLabel(track: String): String {
    return when (track) {
        "Kontakt" -> "Wer in diesem Bereich zuerst sichtbar bleibt."
        "Naehe" -> "Wie persoenlich oder nah dieser Bereich gelesen wird."
        "Pflege" -> "Was aktiv getragen oder beantwortet werden soll."
        "Resonanz" -> "Welche Signale ueber Wirkung oder Ton entscheiden."
        else -> ""
    }
}

private fun projectWorkTrackSupportingLabel(track: String): String {
    return when (track) {
        "Fokus" -> "Worauf dieses Projekt zuerst zieht."
        "Schritt" -> "Welcher naechste Zug klar bleiben soll."
        "Termin" -> "Welche Fristen oder Zeitfenster mitlaufen."
        "Fortschritt" -> "Wie sichtbar der aktuelle Stand bleibt."
        else -> ""
    }
}

private fun personContactSourcesModeOptions(
    selected: AreaSourcesMode,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaSourcesMode.Curated.persistedValue,
            label = "Gezielt",
            selected = selected == AreaSourcesMode.Curated,
            supportingLabel = "Wenige Personen und Antworten fuehren klar.",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Balanced.persistedValue,
            label = "Offen",
            selected = selected == AreaSourcesMode.Balanced,
            supportingLabel = "Mehrere Kontaktspuren bleiben nebeneinander sichtbar.",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Signals.persistedValue,
            label = "Signalnah",
            selected = selected == AreaSourcesMode.Signals,
            supportingLabel = "Neue Nachrichten oder Impulse duerfen schneller nach vorn.",
        ),
    )
}

private fun projectWorkSourcesModeOptions(
    selected: AreaSourcesMode,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaSourcesMode.Curated.persistedValue,
            label = "Klar",
            selected = selected == AreaSourcesMode.Curated,
            supportingLabel = "Wenige klare Zuege und Quellen fuehren zuerst.",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Balanced.persistedValue,
            label = "Ausgewogen",
            selected = selected == AreaSourcesMode.Balanced,
            supportingLabel = "Fortschritt, Termine und Material bleiben im Gleichgewicht.",
        ),
        StartPanelOptionState(
            id = AreaSourcesMode.Signals.persistedValue,
            label = "Drucknah",
            selected = selected == AreaSourcesMode.Signals,
            supportingLabel = "Neue Fristen und stoerende Signale duerfen schneller nach vorn.",
        ),
    )
}

private fun personContactCadenceOptions(
    selected: String,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = "daily",
            label = "Heute",
            selected = selected == "daily",
            supportingLabel = "Der Bereich schaut zuerst auf heutige Antworten und Signale.",
        ),
        StartPanelOptionState(
            id = "weekly",
            label = "Diese Woche",
            selected = selected == "weekly",
            supportingLabel = "Auch aeltere offene Kontakte bleiben mit drin.",
        ),
        StartPanelOptionState(
            id = "adaptive",
            label = "Adaptiv",
            selected = selected == "adaptive",
            supportingLabel = "Das Fenster passt sich Naehe und Bewegung an.",
        ),
    )
}

private fun projectWorkCadenceOptions(
    selected: String,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = "daily",
            label = "Heute",
            selected = selected == "daily",
            supportingLabel = "Der Bereich bleibt am heutigen Zug und den naechsten Schritten.",
        ),
        StartPanelOptionState(
            id = "weekly",
            label = "Diese Woche",
            selected = selected == "weekly",
            supportingLabel = "Auch mittlere Fristen und Fortschritt bleiben sichtbar.",
        ),
        StartPanelOptionState(
            id = "adaptive",
            label = "Adaptiv",
            selected = selected == "adaptive",
            supportingLabel = "Das Fenster passt sich Material, Druck und Tempo an.",
        ),
    )
}

private fun personContactFlowProfileOptions(
    selected: AreaFlowProfile,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaFlowProfile.Stable.persistedValue,
            label = "Leise",
            selected = selected == AreaFlowProfile.Stable,
            supportingLabel = "Der Bereich erinnert nur sanft und bleibt zurückhaltend.",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Supportive.persistedValue,
            label = "Nah",
            selected = selected == AreaFlowProfile.Supportive,
            supportingLabel = "Wichtige Personen und offene Antworten bleiben spuerbar.",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Active.persistedValue,
            label = "Aufmerksam",
            selected = selected == AreaFlowProfile.Active,
            supportingLabel = "Der Bereich meldet sich frueher und klarer zurueck.",
        ),
    )
}

private fun projectWorkFlowProfileOptions(
    selected: AreaFlowProfile,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = AreaFlowProfile.Stable.persistedValue,
            label = "Ruhig",
            selected = selected == AreaFlowProfile.Stable,
            supportingLabel = "Nur der klare Kern bleibt sichtbar.",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Supportive.persistedValue,
            label = "Zug",
            selected = selected == AreaFlowProfile.Supportive,
            supportingLabel = "Das Projekt haelt naechste Schritte und Material zusammen.",
        ),
        StartPanelOptionState(
            id = AreaFlowProfile.Active.persistedValue,
            label = "Druck",
            selected = selected == AreaFlowProfile.Active,
            supportingLabel = "Termine, Fortschritt und offene Punkte kommen frueher nach vorn.",
        ),
    )
}

private fun personContactFlowIntensityOptions(
    selected: Int,
): List<StartPanelOptionState> {
    return listOf(
        1 to "Leicht",
        2 to "Sanft",
        3 to "Spuerbar",
        4 to "Klar",
        5 to "Nah",
    ).map { (value, label) ->
        StartPanelOptionState(
            id = value.toString(),
            label = label,
            selected = selected == value,
            supportingLabel = when (value) {
                1 -> "Der Bereich kommt nur selten wieder."
                2 -> "Wichtige Kontakte bleiben locker im Blick."
                3 -> "Ein ruhiger Mittelwert fuer Antworten und Rueckkehr."
                4 -> "Offenes taucht deutlicher wieder auf."
                else -> "Der Bereich bleibt sehr praesent."
            },
        )
    }
}

private fun projectWorkFlowIntensityOptions(
    selected: Int,
): List<StartPanelOptionState> {
    return listOf(
        1 to "Leicht",
        2 to "Normal",
        3 to "Klar",
        4 to "Dicht",
        5 to "Fokus",
    ).map { (value, label) ->
        StartPanelOptionState(
            id = value.toString(),
            label = label,
            selected = selected == value,
            supportingLabel = when (value) {
                1 -> "Nur der naechste Zug bleibt im Blick."
                2 -> "Material und Aufgaben bleiben locker sortiert."
                3 -> "Ein ruhiger Projektfluss fuer Alltag und App-Bau."
                4 -> "Mehr Hinweise und Wiedervorlagen bleiben gleichzeitig sichtbar."
                else -> "Der Bereich zieht sehr klar nach vorn."
            },
        )
    }
}

private fun personContactFlowSwitchOptions(
    area: StartAreaDetailState,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = "review",
            label = "Rueckblick",
            selected = area.reviewEnabled,
            supportingLabel = "Aeltere Kontakte duerfen kurz wieder sichtbar werden.",
        ),
        StartPanelOptionState(
            id = "reminders",
            label = "Erinnern",
            selected = area.remindersEnabled,
            supportingLabel = "Offene Antworten duerfen wieder auftauchen.",
        ),
        StartPanelOptionState(
            id = "experiments",
            label = "Experimente",
            selected = area.experimentsEnabled,
            supportingLabel = "Neue Sortierungen fuer Beziehungen koennen vorsichtig mitlaufen.",
        ),
    )
}

private fun projectWorkFlowSwitchOptions(
    area: StartAreaDetailState,
): List<StartPanelOptionState> {
    return listOf(
        StartPanelOptionState(
            id = "review",
            label = "Rueckblick",
            selected = area.reviewEnabled,
            supportingLabel = "Aelteres Material oder Fortschritt darf kurz wieder auftauchen.",
        ),
        StartPanelOptionState(
            id = "reminders",
            label = "Erinnern",
            selected = area.remindersEnabled,
            supportingLabel = "Naechste Schritte und Fristen duerfen zurueckkommen.",
        ),
        StartPanelOptionState(
            id = "experiments",
            label = "Experimente",
            selected = area.experimentsEnabled,
            supportingLabel = "Neue Projekt-Sortierungen koennen vorsichtig mitlaufen.",
        ),
    )
}

private fun personContactCadenceLabel(selected: String): String {
    return when (selected) {
        "daily" -> "Heute"
        "weekly" -> "Diese Woche"
        else -> "Adaptiv"
    }
}

private fun projectWorkCadenceLabel(selected: String): String {
    return when (selected) {
        "daily" -> "Heute"
        "weekly" -> "Diese Woche"
        else -> "Adaptiv"
    }
}

private fun personContactFlowProfileLabel(selected: AreaFlowProfile): String {
    return when (selected) {
        AreaFlowProfile.Stable -> "Leise"
        AreaFlowProfile.Supportive -> "Tragend"
        AreaFlowProfile.Active -> "Aufmerksam"
    }
}

private fun projectWorkFlowProfileLabel(selected: AreaFlowProfile): String {
    return when (selected) {
        AreaFlowProfile.Stable -> "Ruhig"
        AreaFlowProfile.Supportive -> "Tragend"
        AreaFlowProfile.Active -> "Klar"
    }
}

private fun projectWorkSourcesModeLabel(selected: AreaSourcesMode): String {
    return when (selected) {
        AreaSourcesMode.Curated -> "Klar"
        AreaSourcesMode.Balanced -> "Ausgewogen"
        AreaSourcesMode.Signals -> "Drucknah"
    }
}

private fun placeContextStandActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val lastPlaceValue = sourceSetup?.headline ?: importedMaterials.firstOrNull()?.title ?: "Noch keiner"
    val activeValue = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Verbunden"
        importedMaterials.isNotEmpty() -> "${importedMaterials.size} Spur${if (importedMaterials.size == 1) "" else "en"}"
        else -> "Manuell"
    }
    return listOf(
        StartPanelActionState(StartPanelActionId.SnapshotState, "Status", area.todayOutput.statusLabel.ifBlank { "Offen" }, StartPanelActionMode.Direct),
        StartPanelActionState(StartPanelActionId.SnapshotScore, "Letzter Ort", lastPlaceValue, StartPanelActionMode.Direct),
        StartPanelActionState(StartPanelActionId.SnapshotMode, "Aktiv", activeValue, StartPanelActionMode.Direct),
    )
}

private fun placeContextFocusActions(area: StartAreaDetailState): List<StartPanelActionState> {
    val trackOptions = area.tracks.map { track ->
        StartPanelOptionState(track, track, area.focusTrack == track, placeContextTrackSupportingLabel(track))
    }
    val selectionOptions = area.tracks.map { track ->
        StartPanelOptionState(track, track, track in area.selectedTracks, placeContextTrackSupportingLabel(track))
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.PathFocus,
            label = "Orte",
            valueLabel = area.focusTrack,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Orte", StartPanelSelectionMode.Single, trackOptions),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesTracks,
            label = "Auswahl",
            valueLabel = when (area.selectedTracks.size) {
                0 -> "Noch offen"
                1 -> area.selectedTracks.first()
                else -> "${area.selectedTracks.size} Spuren"
            },
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Auswahl", StartPanelSelectionMode.Multiple, selectionOptions),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesMode,
            label = "Ausloeser",
            valueLabel = placeContextSourcesModeLabel(area.profileState.sourcesMode),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Ausloeser", StartPanelSelectionMode.Single, placeContextSourcesModeOptions(area.profileState.sourcesMode)),
        ),
        StartPanelActionState(
            id = StartPanelActionId.PathCadence,
            label = "Zeitraum",
            valueLabel = placeContextCadenceLabel(area.cadence),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Zeitraum", StartPanelSelectionMode.Single, placeContextCadenceOptions(area.cadence)),
        ),
    )
}

private fun placeContextTaktActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val signalValue = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Aktiv"
        importedMaterials.isNotEmpty() -> "Manuell"
        else -> "Kein Signal"
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.FlowProfile,
            label = "Rhythmus",
            valueLabel = placeContextFlowProfileLabel(area.profileState.flowProfile),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Rhythmus", StartPanelSelectionMode.Single, placeContextFlowProfileOptions(area.profileState.flowProfile)),
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowIntensity,
            label = "Wiederkehr",
            valueLabel = "${area.intensity}/5",
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Wiederkehr", StartPanelSelectionMode.Single, placeContextFlowIntensityOptions(area.intensity)),
        ),
        StartPanelActionState(StartPanelActionId.SnapshotMode, "Ortssignal", signalValue, StartPanelActionMode.Direct),
        StartPanelActionState(
            id = StartPanelActionId.FlowSwitches,
            label = "Extras",
            valueLabel = flowSwitchSummary(area),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Extras", StartPanelSelectionMode.Multiple, placeContextFlowSwitchOptions(area)),
        ),
    )
}

private fun healthRitualStandActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val lastSignalValue = sourceSetup?.headline ?: importedMaterials.firstOrNull()?.title ?: "Noch keines"
    val trendValue = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Messbar"
        importedMaterials.isNotEmpty() -> "${importedMaterials.size} Spur${if (importedMaterials.size == 1) "" else "en"}"
        else -> "Noch offen"
    }
    return listOf(
        StartPanelActionState(StartPanelActionId.SnapshotState, "Status", area.todayOutput.statusLabel.ifBlank { "Offen" }, StartPanelActionMode.Direct),
        StartPanelActionState(StartPanelActionId.SnapshotScore, "Letztes Signal", lastSignalValue, StartPanelActionMode.Direct),
        StartPanelActionState(StartPanelActionId.SnapshotMode, "Trend", trendValue, StartPanelActionMode.Direct),
    )
}

private fun healthRitualFocusActions(area: StartAreaDetailState): List<StartPanelActionState> {
    val trackOptions = area.tracks.map { track ->
        StartPanelOptionState(track, track, area.focusTrack == track, healthRitualTrackSupportingLabel(track))
    }
    val selectionOptions = area.tracks.map { track ->
        StartPanelOptionState(track, track, track in area.selectedTracks, healthRitualTrackSupportingLabel(track))
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.PathFocus,
            label = "Vorne",
            valueLabel = area.focusTrack,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Vorne", StartPanelSelectionMode.Single, trackOptions),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesTracks,
            label = "Auswahl",
            valueLabel = when (area.selectedTracks.size) {
                0 -> "Noch offen"
                1 -> area.selectedTracks.first()
                else -> "${area.selectedTracks.size} Spuren"
            },
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Auswahl", StartPanelSelectionMode.Multiple, selectionOptions),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesMode,
            label = "Deutung",
            valueLabel = healthRitualSourcesModeLabel(area.profileState.sourcesMode),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Deutung", StartPanelSelectionMode.Single, healthRitualSourcesModeOptions(area.profileState.sourcesMode)),
        ),
        StartPanelActionState(
            id = StartPanelActionId.PathCadence,
            label = "Verlauf",
            valueLabel = healthRitualCadenceLabel(area.cadence),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Verlauf", StartPanelSelectionMode.Single, healthRitualCadenceOptions(area.cadence)),
        ),
    )
}

private fun healthRitualTaktActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val signalValue = when {
        sourceSetup?.status == AreaSourceSetupStatus.READY -> "Messbar"
        importedMaterials.isNotEmpty() -> "Manuell"
        else -> "Kein Signal"
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.FlowProfile,
            label = "Rhythmus",
            valueLabel = healthRitualFlowProfileLabel(area.profileState.flowProfile),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Rhythmus", StartPanelSelectionMode.Single, healthRitualFlowProfileOptions(area.profileState.flowProfile)),
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowIntensity,
            label = "Dichte",
            valueLabel = "${area.intensity}/5",
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Dichte", StartPanelSelectionMode.Single, healthRitualFlowIntensityOptions(area.intensity)),
        ),
        StartPanelActionState(StartPanelActionId.SnapshotMode, "Messung", signalValue, StartPanelActionMode.Direct),
        StartPanelActionState(
            id = StartPanelActionId.FlowSwitches,
            label = "Extras",
            valueLabel = flowSwitchSummary(area),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Extras", StartPanelSelectionMode.Multiple, healthRitualFlowSwitchOptions(area)),
        ),
    )
}

private fun collectionInboxStandActions(
    area: StartAreaDetailState,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val lastCatchValue = importedMaterials.firstOrNull()?.title ?: "Noch nichts"
    val openValue = when (importedMaterials.size) {
        0 -> "Leer"
        1 -> "1 Teil"
        else -> "${importedMaterials.size} offen"
    }
    return listOf(
        StartPanelActionState(StartPanelActionId.SnapshotState, "Status", area.todayOutput.statusLabel.ifBlank { "Offen" }, StartPanelActionMode.Direct),
        StartPanelActionState(StartPanelActionId.SnapshotScore, "Letzter Fang", lastCatchValue, StartPanelActionMode.Direct),
        StartPanelActionState(StartPanelActionId.SnapshotMode, "Offen", openValue, StartPanelActionMode.Direct),
    )
}

private fun collectionInboxFocusActions(area: StartAreaDetailState): List<StartPanelActionState> {
    val trackOptions = area.tracks.map { track ->
        StartPanelOptionState(track, track, area.focusTrack == track, collectionInboxTrackSupportingLabel(track))
    }
    val selectionOptions = area.tracks.map { track ->
        StartPanelOptionState(track, track, track in area.selectedTracks, collectionInboxTrackSupportingLabel(track))
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.PathFocus,
            label = "Vorne",
            valueLabel = area.focusTrack,
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Vorne", StartPanelSelectionMode.Single, trackOptions),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesTracks,
            label = "Auswahl",
            valueLabel = when (area.selectedTracks.size) {
                0 -> "Noch offen"
                1 -> area.selectedTracks.first()
                else -> "${area.selectedTracks.size} Spuren"
            },
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Auswahl", StartPanelSelectionMode.Multiple, selectionOptions),
        ),
        StartPanelActionState(
            id = StartPanelActionId.SourcesMode,
            label = "Ordnung",
            valueLabel = collectionInboxSourcesModeLabel(area.profileState.sourcesMode),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Ordnung", StartPanelSelectionMode.Single, collectionInboxSourcesModeOptions(area.profileState.sourcesMode)),
        ),
        StartPanelActionState(
            id = StartPanelActionId.PathCadence,
            label = "Dauer",
            valueLabel = collectionInboxCadenceLabel(area.cadence),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Dauer", StartPanelSelectionMode.Single, collectionInboxCadenceOptions(area.cadence)),
        ),
    )
}

private fun collectionInboxTaktActions(
    area: StartAreaDetailState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): List<StartPanelActionState> {
    val revisitValue = when {
        area.remindersEnabled -> "Aktiv"
        importedMaterials.isNotEmpty() || sourceSetup?.status == AreaSourceSetupStatus.READY -> "Bereit"
        else -> "Aus"
    }
    return listOf(
        StartPanelActionState(
            id = StartPanelActionId.FlowProfile,
            label = "Rhythmus",
            valueLabel = collectionInboxFlowProfileLabel(area.profileState.flowProfile),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Rhythmus", StartPanelSelectionMode.Single, collectionInboxFlowProfileOptions(area.profileState.flowProfile)),
        ),
        StartPanelActionState(
            id = StartPanelActionId.FlowIntensity,
            label = "Rueckholen",
            valueLabel = "${area.intensity}/5",
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Rueckholen", StartPanelSelectionMode.Single, collectionInboxFlowIntensityOptions(area.intensity)),
        ),
        StartPanelActionState(StartPanelActionId.SnapshotMode, "Wiedervorlage", revisitValue, StartPanelActionMode.Direct),
        StartPanelActionState(
            id = StartPanelActionId.FlowSwitches,
            label = "Extras",
            valueLabel = flowSwitchSummary(area),
            mode = StartPanelActionMode.Sheet,
            sheet = StartPanelSheetState("Extras", StartPanelSelectionMode.Multiple, collectionInboxFlowSwitchOptions(area)),
        ),
    )
}

private fun flowSwitchSummary(area: StartAreaDetailState): String {
    val active = buildList {
        if (area.reviewEnabled) add("Rueckblick")
        if (area.remindersEnabled) add("Erinnern")
        if (area.experimentsEnabled) add("Experimente")
    }
    return when (active.size) {
        0 -> "Aus"
        1 -> active.first()
        else -> "${active.size} an"
    }
}

private fun placeContextTrackSupportingLabel(track: String): String = when (track) {
    "Umfeld" -> "Welche Orte oder Umfelder zuerst sichtbar werden."
    "Weg" -> "Welche Wege oder Bewegungen wichtig bleiben."
    "Zeit" -> "Wann ortsbezogene Hinweise auftauchen."
    "Atmosphaere" -> "Wie Stimmung und Umgebung mitgelesen werden."
    else -> ""
}

private fun healthRitualTrackSupportingLabel(track: String): String = when (track) {
    "Ausloeser" -> "Welche Ausloeser oder Momente den Bereich bewegen."
    "Schritt" -> "Welche kleine Praxis oder Handlung zentral bleibt."
    "Rhythmus" -> "Wie die Wiederholung lesbar wird."
    "Wirkung" -> "Welche Wirkung oder Regulation beobachtet wird."
    "Koerper" -> "Welche Koerpersignale zuerst gelesen werden."
    "Stimmung" -> "Welche innere Lage sichtbar bleiben soll."
    "Regulation" -> "Wie der Bereich Ruhe oder Anpassung erkennt."
    else -> ""
}

private fun collectionInboxTrackSupportingLabel(track: String): String = when (track) {
    "Spuren" -> "Was aus der Inbox zuerst Bedeutung bekommt."
    "Notizen" -> "Welche kurzen Gedanken sichtbar bleiben."
    "Links" -> "Welche Verweise oder Quellen priorisiert werden."
    "Impulse" -> "Welche losen Ideen noch einmal auftauchen duerfen."
    else -> ""
}

private fun placeContextSourcesModeOptions(selected: AreaSourcesMode): List<StartPanelOptionState> = listOf(
    StartPanelOptionState(AreaSourcesMode.Curated.persistedValue, "Ort", selected == AreaSourcesMode.Curated, "Ein paar klare Orte oder Wege fuehren zuerst."),
    StartPanelOptionState(AreaSourcesMode.Balanced.persistedValue, "Ausgewogen", selected == AreaSourcesMode.Balanced, "Ort, Weg und Zeit bleiben nebeneinander sichtbar."),
    StartPanelOptionState(AreaSourcesMode.Signals.persistedValue, "Zeitnah", selected == AreaSourcesMode.Signals, "Frische Orts- oder Bewegungssignale duerfen frueher nach vorn."),
)

private fun healthRitualSourcesModeOptions(selected: AreaSourcesMode): List<StartPanelOptionState> = listOf(
    StartPanelOptionState(AreaSourcesMode.Curated.persistedValue, "Klar", selected == AreaSourcesMode.Curated, "Wenige deutliche Signale fuehren zuerst."),
    StartPanelOptionState(AreaSourcesMode.Balanced.persistedValue, "Ausgewogen", selected == AreaSourcesMode.Balanced, "Koerper, Wirkung und Schritte bleiben im Gleichgewicht."),
    StartPanelOptionState(AreaSourcesMode.Signals.persistedValue, "Messnah", selected == AreaSourcesMode.Signals, "Neue Messungen oder Signale duerfen frueher nach vorn."),
)

private fun collectionInboxSourcesModeOptions(selected: AreaSourcesMode): List<StartPanelOptionState> = listOf(
    StartPanelOptionState(AreaSourcesMode.Curated.persistedValue, "Klar", selected == AreaSourcesMode.Curated, "Wenige Spuren werden bewusst zuerst sortiert."),
    StartPanelOptionState(AreaSourcesMode.Balanced.persistedValue, "Offen", selected == AreaSourcesMode.Balanced, "Notizen, Links und Impulse bleiben nebeneinander."),
    StartPanelOptionState(AreaSourcesMode.Signals.persistedValue, "Frisch", selected == AreaSourcesMode.Signals, "Neue Funde duerfen schneller oben landen."),
)

private fun placeContextCadenceOptions(selected: String): List<StartPanelOptionState> = listOf(
    StartPanelOptionState("daily", "Heute", selected == "daily", "Der Bereich schaut zuerst auf die heutige Lage."),
    StartPanelOptionState("weekly", "Diese Woche", selected == "weekly", "Auch Wege und Orte ueber mehrere Tage bleiben sichtbar."),
    StartPanelOptionState("adaptive", "Adaptiv", selected == "adaptive", "Das Fenster passt sich Ort, Zeit und Bewegung an."),
)

private fun healthRitualCadenceOptions(selected: String): List<StartPanelOptionState> = listOf(
    StartPanelOptionState("daily", "Heute", selected == "daily", "Der Bereich schaut auf heutige Signale und Wirkung."),
    StartPanelOptionState("weekly", "Diese Woche", selected == "weekly", "Auch Verlaeufe ueber mehrere Tage bleiben lesbar."),
    StartPanelOptionState("adaptive", "Adaptiv", selected == "adaptive", "Das Fenster passt sich Zustand und Regelmaessigkeit an."),
)

private fun collectionInboxCadenceOptions(selected: String): List<StartPanelOptionState> = listOf(
    StartPanelOptionState("daily", "Heute", selected == "daily", "Neue Funde bleiben zuerst im heutigen Fenster."),
    StartPanelOptionState("weekly", "Diese Woche", selected == "weekly", "Auch aeltere Funde duerfen noch mitlaufen."),
    StartPanelOptionState("adaptive", "Adaptiv", selected == "adaptive", "Das Fenster passt sich Menge und Alter des Materials an."),
)

private fun placeContextFlowProfileOptions(selected: AreaFlowProfile): List<StartPanelOptionState> = listOf(
    StartPanelOptionState(AreaFlowProfile.Stable.persistedValue, "Still", selected == AreaFlowProfile.Stable, "Der Bereich bleibt leise und ortet nur wenig."),
    StartPanelOptionState(AreaFlowProfile.Supportive.persistedValue, "Begleitend", selected == AreaFlowProfile.Supportive, "Orte und Wege bleiben ruhig praesent."),
    StartPanelOptionState(AreaFlowProfile.Active.persistedValue, "Aktiv", selected == AreaFlowProfile.Active, "Ortswechsel und relevante Signale melden sich deutlicher."),
)

private fun healthRitualFlowProfileOptions(selected: AreaFlowProfile): List<StartPanelOptionState> = listOf(
    StartPanelOptionState(AreaFlowProfile.Stable.persistedValue, "Ruhig", selected == AreaFlowProfile.Stable, "Der Bereich beobachtet still."),
    StartPanelOptionState(AreaFlowProfile.Supportive.persistedValue, "Tragend", selected == AreaFlowProfile.Supportive, "Signale und Wirkung bleiben sanft praesent."),
    StartPanelOptionState(AreaFlowProfile.Active.persistedValue, "Klar", selected == AreaFlowProfile.Active, "Wirkung und Messung melden sich frueher zurueck."),
)

private fun collectionInboxFlowProfileOptions(selected: AreaFlowProfile): List<StartPanelOptionState> = listOf(
    StartPanelOptionState(AreaFlowProfile.Stable.persistedValue, "Leise", selected == AreaFlowProfile.Stable, "Die Inbox bleibt ruhig und wenig praesent."),
    StartPanelOptionState(AreaFlowProfile.Supportive.persistedValue, "Sammelnd", selected == AreaFlowProfile.Supportive, "Neue Funde und offene Spuren bleiben sanft sichtbar."),
    StartPanelOptionState(AreaFlowProfile.Active.persistedValue, "Zugreifend", selected == AreaFlowProfile.Active, "Die Inbox zieht frueher wieder nach vorn."),
)

private fun placeContextFlowIntensityOptions(selected: Int): List<StartPanelOptionState> =
    intensityOptions(selected, listOf("Leicht", "Sanft", "Spuerbar", "Klar", "Nah"), listOf(
        "Ortswechsel tauchen nur selten wieder auf.",
        "Wichtige Wege bleiben locker im Blick.",
        "Ein ruhiger Mittelwert fuer Ort und Zeit.",
        "Relevante Wege oder Orte kommen deutlicher zurueck.",
        "Der Bereich bleibt sehr praesent.",
    ))

private fun healthRitualFlowIntensityOptions(selected: Int): List<StartPanelOptionState> =
    intensityOptions(selected, listOf("Leicht", "Sanft", "Mittel", "Klar", "Dicht"), listOf(
        "Nur wenige Signale bleiben sichtbar.",
        "Der Bereich begleitet eher locker.",
        "Ein ruhiger Mittelwert fuer Verlauf und Wirkung.",
        "Mehr Signale bleiben gleichzeitig lesbar.",
        "Der Bereich bleibt dicht und klar praesent.",
    ))

private fun collectionInboxFlowIntensityOptions(selected: Int): List<StartPanelOptionState> =
    intensityOptions(selected, listOf("Leicht", "Locker", "Mittel", "Klar", "Voll"), listOf(
        "Nur wenig aus der Inbox taucht wieder auf.",
        "Neue Funde bleiben locker sichtbar.",
        "Ein ruhiger Mittelwert fuer Sammeln und Leeren.",
        "Mehr offene Funde bleiben im Blick.",
        "Die Inbox zieht stark wieder nach vorn.",
    ))

private fun intensityOptions(
    selected: Int,
    labels: List<String>,
    details: List<String>,
): List<StartPanelOptionState> {
    return labels.mapIndexed { index, label ->
        val value = index + 1
        StartPanelOptionState(
            id = value.toString(),
            label = label,
            selected = selected == value,
            supportingLabel = details[index],
        )
    }
}

private fun placeContextFlowSwitchOptions(area: StartAreaDetailState): List<StartPanelOptionState> =
    commonFlowSwitchOptions(area, "Aeltere Ortsspuren duerfen kurz wieder auftauchen.", "Ortsbezogene Hinweise duerfen wiederkommen.", "Neue Ortslogiken koennen vorsichtig mitlaufen.")

private fun healthRitualFlowSwitchOptions(area: StartAreaDetailState): List<StartPanelOptionState> =
    commonFlowSwitchOptions(area, "Aeltere Signale oder Wirkung duerfen wieder auftauchen.", "Wichtige Rituale oder Hinweise duerfen wiederkommen.", "Neue Deutungen koennen vorsichtig mitlaufen.")

private fun collectionInboxFlowSwitchOptions(area: StartAreaDetailState): List<StartPanelOptionState> =
    commonFlowSwitchOptions(area, "Aeltere Funde duerfen kurz wieder auftauchen.", "Wichtige lose Spuren duerfen wiederkommen.", "Neue Sortierungen koennen vorsichtig mitlaufen.")

private fun commonFlowSwitchOptions(
    area: StartAreaDetailState,
    reviewLabel: String,
    reminderLabel: String,
    experimentLabel: String,
): List<StartPanelOptionState> = listOf(
    StartPanelOptionState("review", "Rueckblick", area.reviewEnabled, reviewLabel),
    StartPanelOptionState("reminders", "Erinnern", area.remindersEnabled, reminderLabel),
    StartPanelOptionState("experiments", "Experimente", area.experimentsEnabled, experimentLabel),
)

private fun placeContextSourcesModeLabel(selected: AreaSourcesMode): String = when (selected) {
    AreaSourcesMode.Curated -> "Ort"
    AreaSourcesMode.Balanced -> "Ausgewogen"
    AreaSourcesMode.Signals -> "Zeitnah"
}

private fun healthRitualSourcesModeLabel(selected: AreaSourcesMode): String = when (selected) {
    AreaSourcesMode.Curated -> "Klar"
    AreaSourcesMode.Balanced -> "Ausgewogen"
    AreaSourcesMode.Signals -> "Messnah"
}

private fun collectionInboxSourcesModeLabel(selected: AreaSourcesMode): String = when (selected) {
    AreaSourcesMode.Curated -> "Klar"
    AreaSourcesMode.Balanced -> "Offen"
    AreaSourcesMode.Signals -> "Frisch"
}

private fun placeContextCadenceLabel(selected: String): String = when (selected) {
    "daily" -> "Heute"
    "weekly" -> "Diese Woche"
    else -> "Adaptiv"
}

private fun healthRitualCadenceLabel(selected: String): String = when (selected) {
    "daily" -> "Heute"
    "weekly" -> "Diese Woche"
    else -> "Adaptiv"
}

private fun collectionInboxCadenceLabel(selected: String): String = when (selected) {
    "daily" -> "Heute"
    "weekly" -> "Diese Woche"
    else -> "Adaptiv"
}

private fun placeContextFlowProfileLabel(selected: AreaFlowProfile): String = when (selected) {
    AreaFlowProfile.Stable -> "Still"
    AreaFlowProfile.Supportive -> "Begleitend"
    AreaFlowProfile.Active -> "Aktiv"
}

private fun healthRitualFlowProfileLabel(selected: AreaFlowProfile): String = when (selected) {
    AreaFlowProfile.Stable -> "Ruhig"
    AreaFlowProfile.Supportive -> "Tragend"
    AreaFlowProfile.Active -> "Klar"
}

private fun collectionInboxFlowProfileLabel(selected: AreaFlowProfile): String = when (selected) {
    AreaFlowProfile.Stable -> "Leise"
    AreaFlowProfile.Supportive -> "Sammelnd"
    AreaFlowProfile.Active -> "Zugreifend"
}

@Composable
private fun StartPanelActionEditorScreen(
    action: StartPanelActionState,
    onBack: () -> Unit,
    onApplyOption: (String) -> Unit,
) {
    val actionSheet = action.sheet ?: return
    DaysPageScaffold(
        title = action.label,
        onBack = onBack,
        modifier = Modifier.testTag("panel-action-editor-screen"),
    ) {
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                actionSheet.options.forEachIndexed { index, option ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
                        )
                    }
                    StartPanelOptionRow(
                        actionId = action.id,
                        option = option,
                        multiple = actionSheet.selectionMode == StartPanelSelectionMode.Multiple,
                        onClick = { onApplyOption(option.id) },
                    )
                }
                if (actionSheet.selectionMode == StartPanelSelectionMode.Multiple) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Fertig")
                    }
                }
            }
        }
    }
}

@Composable
private fun StartPanelPage(
    areaFamily: StartAreaFamily,
    panelState: StartAreaPanelState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    onActionClick: (StartPanelActionState) -> Unit,
) {
    val screenState = panelState.screenState
    val directActions = screenState.actions.filter { it.mode == StartPanelActionMode.Direct }
    val sheetActions = screenState.actions.filter { it.mode == StartPanelActionMode.Sheet }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
            .testTag("start-panel-${panelState.panel.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StartPanelClarityCard(
            actions = sheetActions,
            directActions = directActions,
            accent = startAreaPanelAccentColor(areaFamily, panelState.panel),
            onActionClick = onActionClick,
        )
    }
}

private fun isNewsMediumArea(area: StartAreaDetailState): Boolean {
    return area.templateId == "medium"
}

private fun isPersonContactArea(area: StartAreaDetailState): Boolean {
    return area.templateId == "person"
}

private fun isProjectWorkArea(area: StartAreaDetailState): Boolean {
    return area.templateId == "project"
}

private fun isPlaceContextArea(area: StartAreaDetailState): Boolean {
    return area.templateId == "place"
}

private fun isHealthRitualArea(area: StartAreaDetailState): Boolean {
    return area.templateId == "ritual" || area.templateId == "feeling"
}

private fun isCollectionInboxArea(area: StartAreaDetailState): Boolean {
    return area.templateId == "free"
}

@Composable
private fun NewsMediumPanelPage(
    area: StartAreaDetailState,
    panelState: StartAreaPanelState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
    onOpenInputs: () -> Unit,
) {
    when (panelState.panel) {
        StartAreaPanel.Snapshot -> NewsMediumStandPanel(
            actions = actions,
            importedMaterials = importedMaterials,
            webFeedSync = webFeedSync,
            onOpenInputs = onOpenInputs,
        )

        StartAreaPanel.Path -> NewsMediumFocusPanel(
            actions = actions,
            onActionClick = onActionClick,
        )

        StartAreaPanel.Options -> NewsMediumTaktPanel(
            actions = actions,
            onActionClick = onActionClick,
        )

        StartAreaPanel.Sources -> StartPanelPage(
            areaFamily = area.family,
            panelState = panelState,
            sourceSetup = sourceSetup,
            importedMaterials = importedMaterials,
            onActionClick = onActionClick,
        )
    }
}

@Composable
private fun NewsMediumStandPanel(
    actions: List<StartPanelActionState>,
    importedMaterials: List<AreaImportedMaterialState>,
    webFeedSync: AreaWebFeedSyncState,
    onOpenInputs: () -> Unit,
) {
    val statusAction = actions.firstOrNull { it.id == StartPanelActionId.SnapshotState }
    val lastSignalAction = actions.firstOrNull { it.id == StartPanelActionId.SnapshotScore }
    val webCount = newsSourceItems("web", importedMaterials).size
    val socialTextCount = newsSourceItems("social-text", importedMaterials).size
    val socialImageCount = newsSourceItems("social-image", importedMaterials).size
    val videoCount = newsSourceItems("video", importedMaterials).size
    val screenshotCount = newsSourceItems("screenshots", importedMaterials).size
    val feedCount = webFeedSync.sources.size
    NewsMediumPanelCard(
        onAddClick = onOpenInputs,
        addTag = "news-medium-status-add",
        addDescription = "Quelle hinzufuegen",
    ) {
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Status",
            value = statusAction?.valueLabel ?: "Offen",
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Feeds",
            value = newsSourceCountLabel(feedCount, "Quelle", "Quellen"),
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Web",
            value = newsSourceCountLabel(webCount, "Link", "Links"),
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Social Text",
            value = newsSourceCountLabel(socialTextCount, "Beitrag", "Beitraege"),
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Social Bild",
            value = newsSourceCountLabel(socialImageCount, "Bild", "Bilder"),
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Video",
            value = newsSourceCountLabel(videoCount, "Video", "Videos"),
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Screenshots",
            value = newsSourceCountLabel(screenshotCount, "Bild", "Bilder"),
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Letzter Fund",
            value = lastSignalAction?.valueLabel ?: "Noch keines",
            supporting = "",
        )
    }
}

@Composable
private fun NewsMediumFocusPanel(
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
) {
    val focusAction = actions.firstOrNull { it.id == StartPanelActionId.PathFocus }
    val selectionAction = actions.firstOrNull { it.id == StartPanelActionId.SourcesTracks }
    val modeAction = actions.firstOrNull { it.id == StartPanelActionId.SourcesMode }
    val cadenceAction = actions.firstOrNull { it.id == StartPanelActionId.PathCadence }
    NewsMediumPanelCard {
        NewsMediumPanelRow(
            label = "Zuerst",
            value = focusAction?.valueLabel ?: "Noch offen",
            supporting = "",
            enabled = focusAction != null,
            onClick = { focusAction?.let(onActionClick) },
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Mitlesen",
            value = selectionAction?.valueLabel ?: "Noch offen",
            supporting = "",
            enabled = selectionAction != null,
            onClick = { selectionAction?.let(onActionClick) },
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Sortierung",
            value = modeAction?.valueLabel ?: "Offen",
            supporting = "",
            enabled = modeAction != null,
            onClick = { modeAction?.let(onActionClick) },
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Zeitraum",
            value = cadenceAction?.valueLabel ?: "Adaptiv",
            supporting = "",
            enabled = cadenceAction != null,
            onClick = { cadenceAction?.let(onActionClick) },
        )
    }
}

@Composable
private fun NewsMediumTaktPanel(
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
) {
    val profileAction = actions.firstOrNull { it.id == StartPanelActionId.FlowProfile }
    val intensityAction = actions.firstOrNull { it.id == StartPanelActionId.FlowIntensity }
    val syncAction = actions.firstOrNull { it.id == StartPanelActionId.SnapshotMode }
    val switchesAction = actions.firstOrNull { it.id == StartPanelActionId.FlowSwitches }
    NewsMediumPanelCard {
        NewsMediumPanelRow(
            label = "Stil",
            value = profileAction?.valueLabel ?: "Ruhig",
            supporting = "",
            enabled = profileAction != null,
            onClick = { profileAction?.let(onActionClick) },
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Dichte",
            value = intensityAction?.valueLabel ?: "Mittel",
            supporting = "",
            enabled = intensityAction != null,
            onClick = { intensityAction?.let(onActionClick) },
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Nachladen",
            value = syncAction?.valueLabel ?: "Noch frei",
            supporting = "",
        )
        NewsMediumPanelDivider()
        NewsMediumPanelRow(
            label = "Wiederkehr",
            value = switchesAction?.valueLabel ?: "Aus",
            supporting = "",
            enabled = switchesAction != null,
            onClick = { switchesAction?.let(onActionClick) },
        )
    }
}

@Composable
private fun NewsMediumPanelCard(
    onAddClick: (() -> Unit)? = null,
    addTag: String = "area-panel-add",
    addDescription: String = "Quelle hinzufuegen",
    content: @Composable ColumnScope.() -> Unit,
) {
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (onAddClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    FilledTonalIconButton(
                        onClick = onAddClick,
                        modifier = Modifier.testTag(addTag),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = AppTheme.colors.surfaceMuted.copy(alpha = 0.7f),
                            contentColor = AppTheme.colors.ink,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = addDescription,
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun NewsMediumPanelDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
    )
}

@Composable
private fun NewsMediumPanelRow(
    label: String,
    value: String,
    supporting: String,
    enabled: Boolean = false,
    onClick: () -> Unit = {},
) {
    AreaInputOverviewRow(
        label = label,
        value = value,
        supporting = supporting,
        testTag = "news-medium-row-${label.lowercase().replace(" ", "-")}",
        enabled = enabled,
        compact = true,
        onClick = onClick,
    )
}

@Composable
private fun PersonContactPanelPage(
    panelState: StartAreaPanelState,
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
    onOpenInputs: () -> Unit,
) {
    when (panelState.panel) {
        StartAreaPanel.Snapshot -> TypedPanelCard(
            prefix = "person-contact-row",
            rows = listOf(
                typedRow("status", "Status", actions, StartPanelActionId.SnapshotState, "Aktueller Kontaktstand dieses Bereichs."),
                typedRow("letzter-kontakt", "Letzter Kontakt", actions, StartPanelActionId.SnapshotScore, "Zuletzt sichtbare Person, Nachricht oder Spur."),
                typedRow("offen", "Offen", actions, StartPanelActionId.SnapshotMode, "Was in diesem Bereich noch offen oder ungeklaert bleibt."),
            ),
            onActionClick = onActionClick,
            onAddClick = onOpenInputs,
            addTag = "person-contact-status-add",
        )
        StartAreaPanel.Path -> TypedPanelCard(
            prefix = "person-contact-row",
            rows = listOf(
                typedRow("personen", "Personen", actions, StartPanelActionId.PathFocus, "Welche Person oder Spur zuerst fuehrt."),
                typedRow("auswahl", "Auswahl", actions, StartPanelActionId.SourcesTracks, "Welche Kontaktachsen dieser Bereich aktiv nutzt."),
                typedRow("antworten", "Antworten", actions, StartPanelActionId.SourcesMode, "Wie offen oder gezielt neue Nachrichten einsortiert werden."),
                typedRow("naehe", "Naehe", actions, StartPanelActionId.PathCadence, "Wie weit dieser Bereich zeitlich und persoenlich zurueckschaut."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Options -> TypedPanelCard(
            prefix = "person-contact-row",
            rows = listOf(
                typedRow("rhythmus", "Rhythmus", actions, StartPanelActionId.FlowProfile, "Wie ruhig oder aufmerksam dieser Bereich arbeitet."),
                typedRow("rueckkehr", "Rueckkehr", actions, StartPanelActionId.FlowIntensity, "Wie deutlich offene Kontakte wieder sichtbar werden."),
                typedRow("signale", "Signale", actions, StartPanelActionId.SnapshotMode, "Ob Kontaktspuren nur manuell oder auch systemnah mitlaufen."),
                typedRow("schalter", "Extras", actions, StartPanelActionId.FlowSwitches, "Rueckblick, Erinnern und kleine Experimente."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Sources -> Unit
    }
}

@Composable
private fun ProjectWorkPanelPage(
    panelState: StartAreaPanelState,
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
    onOpenInputs: () -> Unit,
) {
    when (panelState.panel) {
        StartAreaPanel.Snapshot -> TypedPanelCard(
            prefix = "project-work-row",
            rows = listOf(
                typedRow("status", "Status", actions, StartPanelActionId.SnapshotState, "Aktueller Stand dieses Projekts oder App-Bereichs."),
                typedRow("naechster-zug", "Naechster Zug", actions, StartPanelActionId.SnapshotScore, "Welcher Schritt als naechstes ziehen soll."),
                typedRow("offen", "Offen", actions, StartPanelActionId.SnapshotMode, "Was noch frei, lose oder ungeordnet bleibt."),
            ),
            onActionClick = onActionClick,
            onAddClick = onOpenInputs,
            addTag = "project-work-status-add",
        )
        StartAreaPanel.Path -> TypedPanelCard(
            prefix = "project-work-row",
            rows = listOf(
                typedRow("vorne", "Vorne", actions, StartPanelActionId.PathFocus, "Welche Projektachse gerade zuerst fuehrt."),
                typedRow("auswahl", "Auswahl", actions, StartPanelActionId.SourcesTracks, "Welche Achsen dieses Projekt aktiv nutzt."),
                typedRow("ordnung", "Ordnung", actions, StartPanelActionId.SourcesMode, "Wie klar oder drucknah Material und Signale sortiert werden."),
                typedRow("fenster", "Horizont", actions, StartPanelActionId.PathCadence, "Wie weit der Bereich in Zeit, Frist oder Fortschritt schaut."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Options -> TypedPanelCard(
            prefix = "project-work-row",
            rows = listOf(
                typedRow("rhythmus", "Rhythmus", actions, StartPanelActionId.FlowProfile, "Wie ruhig oder druckvoll dieses Projekt arbeitet."),
                typedRow("zugkraft", "Zugkraft", actions, StartPanelActionId.FlowIntensity, "Wie stark der Bereich Aufgaben und Material nach vorn zieht."),
                typedRow("wiedervorlage", "Wiedervorlage", actions, StartPanelActionId.SnapshotMode, "Ob offene Schritte oder Quellen wiederkehren duerfen."),
                typedRow("schalter", "Extras", actions, StartPanelActionId.FlowSwitches, "Rueckblick, Erinnern und kleine Experimente."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Sources -> Unit
    }
}

@Composable
private fun PlaceContextPanelPage(
    panelState: StartAreaPanelState,
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
    onOpenInputs: () -> Unit,
) {
    when (panelState.panel) {
        StartAreaPanel.Snapshot -> TypedPanelCard(
            prefix = "place-context-row",
            rows = listOf(
                typedRow("status", "Status", actions, StartPanelActionId.SnapshotState, "Aktueller Orts- und Kontextstand dieses Bereichs."),
                typedRow("letzter-ort", "Letzter Ort", actions, StartPanelActionId.SnapshotScore, "Zuletzt sichtbarer Ort, Weg oder Kontext."),
                typedRow("aktiv", "Aktiv", actions, StartPanelActionId.SnapshotMode, "Welche Orts- oder Kontextspuren aktuell mitlaufen."),
            ),
            onActionClick = onActionClick,
            onAddClick = onOpenInputs,
            addTag = "place-context-status-add",
        )
        StartAreaPanel.Path -> TypedPanelCard(
            prefix = "place-context-row",
            rows = listOf(
                typedRow("orte", "Orte", actions, StartPanelActionId.PathFocus, "Welche Orte oder Wege zuerst fuehren."),
                typedRow("auswahl", "Auswahl", actions, StartPanelActionId.SourcesTracks, "Welche Ortsachsen der Bereich aktiv nutzt."),
                typedRow("ausloeser", "Ausloeser", actions, StartPanelActionId.SourcesMode, "Wie klar Ort, Zeit oder Bewegung triggern."),
                typedRow("fenster", "Zeitraum", actions, StartPanelActionId.PathCadence, "Wie weit der Bereich zeitlich und situativ schaut."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Options -> TypedPanelCard(
            prefix = "place-context-row",
            rows = listOf(
                typedRow("rhythmus", "Rhythmus", actions, StartPanelActionId.FlowProfile, "Wie ruhig oder aktiv der Ortsbereich arbeitet."),
                typedRow("rueckkehr", "Wiederkehr", actions, StartPanelActionId.FlowIntensity, "Wie deutlich Orte oder Wege wieder auftauchen."),
                typedRow("ortssignal", "Ortssignal", actions, StartPanelActionId.SnapshotMode, "Ob Ortskontext schon verbunden oder nur manuell da ist."),
                typedRow("schalter", "Extras", actions, StartPanelActionId.FlowSwitches, "Rueckblick, Erinnern und kleine Experimente."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Sources -> Unit
    }
}

@Composable
private fun HealthRitualPanelPage(
    panelState: StartAreaPanelState,
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
    onOpenInputs: () -> Unit,
) {
    when (panelState.panel) {
        StartAreaPanel.Snapshot -> TypedPanelCard(
            prefix = "health-ritual-row",
            rows = listOf(
                typedRow("status", "Status", actions, StartPanelActionId.SnapshotState, "Aktueller Zustand dieses Ritual- oder Gesundheitsbereichs."),
                typedRow("letztes-signal", "Letztes Signal", actions, StartPanelActionId.SnapshotScore, "Zuletzt sichtbare Spur aus Messung oder Notiz."),
                typedRow("trend", "Trend", actions, StartPanelActionId.SnapshotMode, "Ob der Bereich schon einen Verlauf lesen kann."),
            ),
            onActionClick = onActionClick,
            onAddClick = onOpenInputs,
            addTag = "health-ritual-status-add",
        )
        StartAreaPanel.Path -> TypedPanelCard(
            prefix = "health-ritual-row",
            rows = listOf(
                typedRow("vorne", "Vorne", actions, StartPanelActionId.PathFocus, "Welche Koerper-, Ritual- oder Wirkungsspur zuerst fuehrt."),
                typedRow("auswahl", "Auswahl", actions, StartPanelActionId.SourcesTracks, "Welche Achsen der Bereich aktiv nutzt."),
                typedRow("deutung", "Deutung", actions, StartPanelActionId.SourcesMode, "Wie klar oder messnah Signale gelesen werden."),
                typedRow("fenster", "Verlauf", actions, StartPanelActionId.PathCadence, "Wie weit der Bereich in Verlauf und Wirkung schaut."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Options -> TypedPanelCard(
            prefix = "health-ritual-row",
            rows = listOf(
                typedRow("rhythmus", "Rhythmus", actions, StartPanelActionId.FlowProfile, "Wie ruhig oder tragend der Bereich arbeitet."),
                typedRow("dichte", "Dichte", actions, StartPanelActionId.FlowIntensity, "Wie viel Verlauf gleichzeitig sichtbar bleibt."),
                typedRow("messung", "Messung", actions, StartPanelActionId.SnapshotMode, "Ob Signale schon systemnah oder nur manuell da sind."),
                typedRow("schalter", "Extras", actions, StartPanelActionId.FlowSwitches, "Rueckblick, Erinnern und kleine Experimente."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Sources -> Unit
    }
}

@Composable
private fun CollectionInboxPanelPage(
    panelState: StartAreaPanelState,
    actions: List<StartPanelActionState>,
    onActionClick: (StartPanelActionState) -> Unit,
    onOpenInputs: () -> Unit,
) {
    when (panelState.panel) {
        StartAreaPanel.Snapshot -> TypedPanelCard(
            prefix = "collection-inbox-row",
            rows = listOf(
                typedRow("status", "Status", actions, StartPanelActionId.SnapshotState, "Aktueller Stand dieser Inbox oder Sammlung."),
                typedRow("letzter-fang", "Letzter Fang", actions, StartPanelActionId.SnapshotScore, "Zuletzt gesicherter Fund oder Impuls."),
                typedRow("offen", "Offen", actions, StartPanelActionId.SnapshotMode, "Was in der Sammlung noch ungeordnet bleibt."),
            ),
            onActionClick = onActionClick,
            onAddClick = onOpenInputs,
            addTag = "collection-inbox-status-add",
        )
        StartAreaPanel.Path -> TypedPanelCard(
            prefix = "collection-inbox-row",
            rows = listOf(
                typedRow("vorne", "Vorne", actions, StartPanelActionId.PathFocus, "Welche Spur aus der Inbox zuerst Bedeutung bekommt."),
                typedRow("auswahl", "Auswahl", actions, StartPanelActionId.SourcesTracks, "Welche Sammelachsen aktiv mitlaufen."),
                typedRow("ordnung", "Ordnung", actions, StartPanelActionId.SourcesMode, "Wie offen oder klar neue Funde einsortiert werden."),
                typedRow("fenster", "Dauer", actions, StartPanelActionId.PathCadence, "Wie lange lose Funde praesent bleiben."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Options -> TypedPanelCard(
            prefix = "collection-inbox-row",
            rows = listOf(
                typedRow("rhythmus", "Rhythmus", actions, StartPanelActionId.FlowProfile, "Wie leise oder zugreifend die Inbox arbeitet."),
                typedRow("leeren", "Rueckholen", actions, StartPanelActionId.FlowIntensity, "Wie stark die Sammlung wieder nach vorn kommt."),
                typedRow("wiedervorlage", "Wiedervorlage", actions, StartPanelActionId.SnapshotMode, "Ob lose Funde spaeter wieder auftauchen duerfen."),
                typedRow("schalter", "Extras", actions, StartPanelActionId.FlowSwitches, "Rueckblick, Erinnern und kleine Experimente."),
            ),
            onActionClick = onActionClick,
        )
        StartAreaPanel.Sources -> Unit
    }
}

private data class TypedPanelRowState(
    val key: String,
    val label: String,
    val value: String,
    val supporting: String,
    val action: StartPanelActionState?,
)

private fun typedRow(
    key: String,
    label: String,
    actions: List<StartPanelActionState>,
    actionId: StartPanelActionId,
    supporting: String,
): TypedPanelRowState {
    val action = actions.firstOrNull { it.id == actionId }
    return TypedPanelRowState(
        key = key,
        label = label,
        value = action?.valueLabel ?: "Noch offen",
        supporting = supporting,
        action = action,
    )
}

@Composable
private fun TypedPanelCard(
    prefix: String,
    rows: List<TypedPanelRowState>,
    onActionClick: (StartPanelActionState) -> Unit,
    onAddClick: (() -> Unit)? = null,
    addTag: String = "area-panel-add",
) {
    NewsMediumPanelCard(
        onAddClick = onAddClick,
        addTag = addTag,
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                NewsMediumPanelDivider()
            }
            AreaInputOverviewRow(
                label = row.label,
                value = row.value,
                supporting = row.supporting,
                testTag = "$prefix-${row.key}",
                enabled = row.action?.mode == StartPanelActionMode.Sheet,
                compact = true,
                onClick = { row.action?.let(onActionClick) },
            )
        }
    }
}

@Composable
private fun StartPanelClarityCard(
    actions: List<StartPanelActionState>,
    directActions: List<StartPanelActionState>,
    accent: Color,
    onActionClick: (StartPanelActionState) -> Unit,
) {
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            actions.forEachIndexed { index, action ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
                    )
                }
                StartPanelEditableRow(
                    action = action,
                    accent = accent,
                    onClick = { onActionClick(action) },
                )
            }
            directActions.forEach { action ->
                HorizontalDivider(
                    thickness = 1.dp,
                    color = AppTheme.colors.outlineSoft.copy(alpha = 0.5f),
                )
                TextButton(
                    onClick = { onActionClick(action) },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(action.label)
                }
            }
        }
    }
}

@Composable
private fun StartPanelEditableRow(
    action: StartPanelActionState,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("panel-action-${action.id.name.lowercase()}")
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = action.valueLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp),
            )
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
            .clickable(onClick = onClick)
            .testTag("panel-option-${actionId.name.lowercase()}-${option.id.toTestTagToken()}")
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (option.selected) AppTheme.colors.accent else MaterialTheme.colorScheme.onSurface,
            )
        }
        if (option.selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = AppTheme.colors.accent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun buildPanelMaterialSummary(
    panelState: StartAreaPanelState,
    screenState: StartPanelScreenState,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): Pair<String, String> {
    val materialSummary = buildLegacyPanelMaterialSummary(
        panel = panelState.panel,
        sourceSetup = sourceSetup,
        importedMaterials = importedMaterials,
    )
    return if (panelState.panel == StartAreaPanel.Path && materialSummary.first == "Noch frei") {
        materialSummary.first to screenState.effectLabel.ifBlank { materialSummary.second }
    } else {
        materialSummary
    }
}

private fun buildLegacyPanelMaterialSummary(
    panel: StartAreaPanel,
    sourceSetup: AreaSourceSetupState?,
    importedMaterials: List<AreaImportedMaterialState>,
): Pair<String, String> {
    val importLabel = when (importedMaterials.size) {
        0 -> "Noch kein Import"
        1 -> importedMaterials.first().title
        else -> "${importedMaterials.size} Importe"
    }
    return when (panel) {
        StartAreaPanel.Snapshot -> when {
            sourceSetup != null -> "Quelle: ${sourceSetup.headline}" to sourceSetup.detail
            else -> importLabel to "Ohne feste Quelle bleibt Lage hier eher gesetzt oder aus manuellem Material gelesen."
        }
        StartAreaPanel.Path -> when {
            importedMaterials.isNotEmpty() -> importLabel to "Material zeigt, worauf der Bereich gerade wirklich zieht."
            sourceSetup != null -> sourceSetup.headline to "Die verbundene Quelle liefert den Stoff fuer Ziel und Analyse."
            else -> "Noch frei" to "Ziel startet hier aus Auftrag, Tracks und Analyse."
        }
        StartAreaPanel.Sources -> when {
            sourceSetup != null && importedMaterials.isNotEmpty() ->
                "${sourceSetup.headline} + ${importedMaterials.size} Importe" to "Verbundene Quelle und bewusst geholtes Material arbeiten hier zusammen."
            sourceSetup != null -> sourceSetup.headline to sourceSetup.detail
            importedMaterials.isNotEmpty() -> importLabel to "Dieses Material ist aktuell fuer den Bereich verfuegbar."
            else -> "Noch frei" to "Noch keine feste Quelle und noch kein bewusst geholtes Material."
        }
        StartAreaPanel.Options -> when {
            importedMaterials.isNotEmpty() -> importLabel to "Flow entscheidet, wie aktiv dieses Material wieder auftaucht."
            sourceSetup != null -> sourceSetup.headline to "Analyse wirkt spaeter auf die verbundene Quelle und ihre Rueckmeldungen."
            else -> "Nur lokal" to "Flow bleibt erst einmal lokal, bis Material oder Quelle andocken."
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
    analysis: AreaMachineAnalysisState,
    analysisAction: DaysTopBarAction,
    onBack: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
) {
    var title by rememberSaveable(area.areaId) { mutableStateOf(area.title) }
    var summary by rememberSaveable(area.areaId) { mutableStateOf(area.summary) }
    var templateId by rememberSaveable(area.areaId) { mutableStateOf(area.templateId) }
    var iconKey by rememberSaveable(area.areaId) { mutableStateOf(area.iconKey) }
    var titleTouched by rememberSaveable(area.areaId) { mutableStateOf(false) }
    var templateTouched by rememberSaveable(area.areaId) { mutableStateOf(false) }
    var iconTouched by rememberSaveable(area.areaId) { mutableStateOf(false) }
    val suggestionInput = summary.ifBlank { title }
    val suggestedDraft = remember(
        area.areaId,
        summary,
        title,
    ) {
        buildPrimaryCreateDraft(suggestionInput)
    }
    val template = startAreaTemplate(templateId)

    LaunchedEffect(summary) {
        if (summary == area.summary) return@LaunchedEffect
        if (!titleTouched) {
            title = suggestedDraft.title
        }
        if (!templateTouched) {
            templateId = suggestedDraft.templateId
        }
        if (!iconTouched) {
            iconKey = suggestedDraft.iconKey
        }
    }

    DaysPageScaffold(
        title = "Name und Auftrag",
        onBack = onBack,
        modifier = Modifier.testTag("area-identity-screen"),
        action = analysisAction,
    ) {
        AreaAnalysisInlineCard(analysis = analysis)
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
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceMuted.copy(alpha = 0.64f)),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DaysMetaPill(label = "Vorschlag aus deinem Auftrag")
                        TextButton(
                            onClick = {
                                title = suggestedDraft.title
                                templateId = suggestedDraft.templateId
                                iconKey = suggestedDraft.iconKey
                            },
                        ) {
                            Text("Anwenden")
                        }
                    }
                    Text(
                        text = suggestedDraft.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = suggestedDraft.meaning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                        onValueChange = {
                            titleTouched = true
                            title = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area-identity-title"),
                        label = { Text("Bereichsname") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Was dieser Bereich fuer dich tun soll") },
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
                            templateTouched = true
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
                        onSelect = {
                            iconTouched = true
                            iconKey = it
                        },
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
