package com.struperto.androidappdays.feature.start

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.ui.theme.AppTheme
import kotlinx.coroutines.delay

enum class StartCreateInputKind(
    val id: String,
    val label: String,
    val fieldLabel: String,
    val placeholder: String,
    val icon: ImageVector,
) {
    Text(
        id = "text",
        label = "Text",
        fieldLabel = "Was soll dieser Bereich fuer dich tun?",
        placeholder = "z.B. Ich will immer sehen, was neue Screenshots zeigen.",
        icon = Icons.AutoMirrored.Outlined.TextSnippet,
    ),
    Link(
        id = "link",
        label = "Link",
        fieldLabel = "Welcher Link oder Feed soll hier eine Rolle spielen?",
        placeholder = "z.B. https://... und ich will nur die wichtigsten neuen Folgen sehen.",
        icon = Icons.Outlined.Link,
    ),
    Screenshot(
        id = "screenshot",
        label = "Screenshot",
        fieldLabel = "Was soll Days aus Screenshots fuer dich herauslesen?",
        placeholder = "z.B. Neue Screenshots lesen und nur das Wichtige hervorheben.",
        icon = Icons.Outlined.PhotoLibrary,
    ),
    App(
        id = "app",
        label = "App",
        fieldLabel = "Welche App oder Quelle soll dieser Bereich spaeter nutzen?",
        placeholder = "z.B. Nachrichten von X lesen und ruhig zusammenfassen.",
        icon = Icons.Outlined.Widgets,
    ),
    Contact(
        id = "contact",
        label = "Kontakt",
        fieldLabel = "Welche Person oder welches Signal soll hier auffallen?",
        placeholder = "z.B. Wenn X schreibt, will ich es sofort sehen.",
        icon = Icons.Outlined.PersonOutline,
    ),
    Location(
        id = "location",
        label = "Ort",
        fieldLabel = "An welchem Ort soll etwas Besonderes passieren?",
        placeholder = "z.B. Wenn ich zuhause bin, will ich an Abendroutine erinnert werden.",
        icon = Icons.Outlined.LocationOn,
    ),
}

data class StartIntentSuggestion(
    val id: String,
    val title: String,
    val summary: String,
    val templateId: String,
    val iconKey: String,
    val behaviorClass: AreaBehaviorClass,
    val sourceKind: DataSourceKind? = null,
    val modeLabel: String,
    val hintLabel: String,
)

data class StartCreateSourceChoice(
    val id: String,
    val title: String,
    val hint: String,
)

@Composable
fun StartCreateScreen(
    inputText: String,
    limitReached: Boolean,
    activeAreaCount: Int,
    maxActiveAreaCount: Int,
    onBack: () -> Unit,
    onInputTextChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    DaysPageScaffold(
        title = "Bereich erstellen",
        onBack = onBack,
        modifier = Modifier.testTag("start-create-screen"),
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = inputText.trim().isNotBlank() && !limitReached,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-create-next"),
            ) {
                Text("Weiter")
            }
        },
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
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .testTag("start-create-input"),
                    placeholder = { Text("Was soll dieser Bereich fuer dich tun?") },
                    minLines = 8,
                    maxLines = 10,
                    singleLine = false,
                )
                if (limitReached) {
                    Text(
                        text = "Aktuell sind $activeAreaCount von $maxActiveAreaCount Bereichen aktiv. Loesche erst einen Bereich in Einstellungen > Bereiche.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.danger,
                    )
                }
            }
        }
    }
}

@Composable
internal fun StartCreateAnalyzeScreen(
    inputText: String,
    capabilityProfile: CapabilityProfile?,
    analysisResult: StartIntentAnalysis?,
    onBack: () -> Unit,
    onAnalysisReady: (StartIntentAnalysis) -> Unit,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val capabilityKey = capabilityProfile
        ?.sources
        ?.joinToString(separator = "|") { source ->
            "${source.source.name}:${source.enabled}:${source.available}:${source.granted}"
        }
        .orEmpty()
    var progress by rememberSaveable(inputText, capabilityKey, analysisResult != null) { mutableStateOf(0.08f) }
    var currentStepLabel by rememberSaveable(inputText, capabilityKey, analysisResult != null) {
        mutableStateOf("Ich lese deine Absicht.")
    }

    LaunchedEffect(inputText, capabilityKey, analysisResult != null) {
        if (analysisResult != null) {
            progress = 1f
            currentStepLabel = "Analyse abgeschlossen."
            return@LaunchedEffect
        }
        progress = 0.08f
        currentStepLabel = "Ich lese deine Absicht."
        delay(180)
        progress = 0.26f
        currentStepLabel = "Ich pruefe moegliche Quellen."
        delay(240)
        progress = 0.48f
        currentStepLabel = "Ich suche gangbare Wege."
        delay(220)
        progress = 0.72f
        currentStepLabel = "Ich ordne den Bereich."
        delay(260)
        progress = 0.9f
        currentStepLabel = "Ich stelle den ersten Bereich bereit."
        delay(200)
        val aiSignals = detectStartPlatformAiSignals(
            context = context,
            rawInput = inputText,
        )
        val analysis = analyzeStartIntent(
            rawInput = inputText,
            capabilityProfile = capabilityProfile,
            browserApps = queryBrowserApps(context),
            installedPackages = queryInstalledPackages(
                context = context,
                packageNames = setOf("com.twitter.android", "com.instagram.android"),
            ),
            aiSignals = aiSignals,
        )
        progress = 1f
        currentStepLabel = "Analyse abgeschlossen."
        delay(180)
        onAnalysisReady(analysis)
    }

    DaysPageScaffold(
        title = "Analyse",
        onBack = onBack,
        modifier = Modifier.testTag("start-analysis-screen"),
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = analysisResult != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-analysis-next"),
            ) {
                Text("Weiter")
            }
        },
    ) {
        if (analysisResult == null) {
            StartAnalysisProgressCard(
                currentStepLabel = currentStepLabel,
                progress = progress,
            )
            StartAnalysisStageCard(
                currentStepLabel = currentStepLabel,
                progress = progress,
            )
        }
        analysisResult?.let { result ->
            StartAnalysisStatusCard(result = result)
        }
    }
}

@Composable
internal fun StartCreateSourcesScreen(
    choices: List<StartCreateSourceChoice>,
    selectedIds: Set<String>,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit,
) {
    DaysPageScaffold(
        title = "Quellen",
        onBack = onBack,
        modifier = Modifier.testTag("start-sources-screen"),
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-sources-next"),
            ) {
                Text("Weiter")
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
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                choices.forEachIndexed { index, choice ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(AppTheme.colors.outlineSoft.copy(alpha = 0.45f)),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(choice.id) }
                            .padding(vertical = 16.dp)
                            .testTag("start-source-choice-${choice.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = choice.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.ink,
                            )
                            if (choice.hint.isNotBlank()) {
                                Text(
                                    text = choice.hint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.muted,
                                )
                            }
                        }
                        Text(
                            text = if (choice.id in selectedIds) "gewaehlt" else "offen",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (choice.id in selectedIds) AppTheme.colors.accent else AppTheme.colors.muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun StartCreateSourceSetupScreen(
    choice: StartCreateSourceChoice,
    value: String,
    isLast: Boolean,
    onBack: () -> Unit,
    onValueChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    DaysPageScaffold(
        title = choice.title,
        onBack = onBack,
        modifier = Modifier.testTag("start-source-setup-screen"),
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-source-setup-next"),
            ) {
                Text(if (isLast) "Bereich anlegen" else "Weiter")
            }
        },
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
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (choice.hint.isNotBlank()) {
                    Text(
                        text = choice.hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .testTag("start-source-setup-input"),
                    placeholder = { Text(sourceSetupPlaceholder(choice.id)) },
                    minLines = 6,
                    maxLines = 8,
                    singleLine = false,
                )
            }
        }
    }
}

@Composable
internal fun StartCreateQuestionScreen(
    draft: CreateAreaDraft,
    analysis: StartIntentAnalysis,
    onBack: () -> Unit,
    onApplySuggestion: (CreateAreaDraft) -> Unit,
    onContinue: () -> Unit,
) {
    DaysPageScaffold(
        title = "Frage klaeren",
        onBack = onBack,
        modifier = Modifier.testTag("start-question-screen"),
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-question-next"),
            ) {
                Text("Vorschau ansehen")
            }
        },
    ) {
        StartFlowHeading(
            title = "Frage klaeren",
            detail = "Eine Entscheidung fehlt noch, damit der Bereich sauber weitergeht.",
        )
        StartAnalysisStatusCard(result = analysis)
        QuestionSelectionCard(
            question = analysis.followUpQuestion.orEmpty(),
            options = analysis.followUpOptions,
            selectedDraft = draft,
            onSelect = { option -> onApplySuggestion(option.draft) },
        )
    }
}

@Composable
internal fun StartCreateBlockerScreen(
    analysis: StartIntentAnalysis,
    onBack: () -> Unit,
    onResolve: (StartIntentRepairOption) -> Unit,
) {
    var selectedRepairId by rememberSaveable(analysis.headline) {
        mutableStateOf(analysis.repairOptions.firstOrNull()?.id)
    }
    val selectedRepair = analysis.repairOptions.firstOrNull { it.id == selectedRepairId }

    DaysPageScaffold(
        title = "Blocker",
        onBack = onBack,
        modifier = Modifier.testTag("start-blocker-screen"),
        bottomBar = {
            Button(
                onClick = { selectedRepair?.let(onResolve) },
                enabled = selectedRepair != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-blocker-solve"),
            ) {
                Text(if (selectedRepair == null) "Noch keine Loesung" else "Loesung finden")
            }
        },
    ) {
        StartFlowHeading(
            title = "Blocker",
            detail = "Gerade fehlt noch ein tragfaehiger Weg. Wenn moeglich, wird direkt ein Reparaturpfad angeboten.",
        )
        StartAnalysisStatusCard(result = analysis)
        analysis.repairQuestion?.let { question ->
            RepairSelectionCard(
                question = question,
                options = analysis.repairOptions,
                selectedId = selectedRepairId,
                onSelect = { selectedRepairId = it },
            )
        }
    }
}

@Composable
internal fun StartCreatePreviewScreen(
    draft: CreateAreaDraft,
    analysis: StartIntentAnalysis,
    activeAreaCount: Int,
    maxActiveAreaCount: Int,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onMeaningChange: (String) -> Unit,
    onSourceKindChange: (DataSourceKind?) -> Unit,
    onApplySuggestion: (CreateAreaDraft) -> Unit,
    onCreate: () -> Unit,
) {
    val template = startAreaTemplate(draft.templateId)
    val summaryPreview = draft.meaning.trim().ifBlank { template.summary }
    val limitReached = activeAreaCount >= maxActiveAreaCount
    val canCreate = draft.title.trim().length >= 2 && !limitReached

    DaysPageScaffold(
        title = "Bereich anlegen",
        onBack = onBack,
        modifier = Modifier.testTag("start-preview-screen"),
        bottomBar = {
            Button(
                onClick = onCreate,
                enabled = canCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-create-save"),
            ) {
                Text("Bereich anlegen")
            }
        },
    ) {
        StartAnalysisDraftCard(
            result = analysis,
            currentDraft = draft,
            summaryPreview = summaryPreview,
            limitReached = limitReached,
            onTitleChange = onTitleChange,
            onMeaningChange = onMeaningChange,
            onSourceKindChange = onSourceKindChange,
            onApplySuggestion = onApplySuggestion,
        )
    }
}

@Composable
private fun QuestionSelectionCard(
    question: String,
    options: List<StartIntentFollowUpOption>,
    selectedDraft: CreateAreaDraft,
    onSelect: (StartIntentFollowUpOption) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DaysMetaPill(label = "Frage")
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ChoiceChipRow(
                items = options.map { option ->
                    ChoiceChipItem(id = option.id, label = option.label)
                },
                selectedId = options.firstOrNull { option ->
                    option.draft.sourceKind == selectedDraft.sourceKind &&
                        option.draft.templateId == selectedDraft.templateId
                }?.id,
                onSelect = { selected ->
                    options.firstOrNull { it.id == selected }?.let(onSelect)
                },
            )
            options.firstOrNull { option ->
                option.draft.sourceKind == selectedDraft.sourceKind &&
                    option.draft.templateId == selectedDraft.templateId
            }?.supportingLabel?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RepairSelectionCard(
    question: String,
    options: List<StartIntentRepairOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DaysMetaPill(label = "Loesungspfad")
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ChoiceChipRow(
                items = options.map { option ->
                    ChoiceChipItem(id = option.id, label = option.label)
                },
                selectedId = selectedId,
                onSelect = onSelect,
            )
            options.firstOrNull { it.id == selectedId }?.supportingLabel?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StartFlowHeading(
    title: String,
    detail: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.ink,
        )
        detail?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.muted,
            )
        }
    }
}

@Composable
private fun StartFlowPromptExamplesCard(
    title: String,
    prompts: List<String>,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            prompts.forEachIndexed { index, prompt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.45f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    DaysMetaPill(label = "${index + 1}")
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun StartAnalysisStageCard(
    currentStepLabel: String,
    progress: Float,
) {
    val stages = listOf(
        "Wunsch lesen",
        "Quellen pruefen",
        "Wege finden",
        "Bereich formen",
    )
    val activeStageCount = when {
        progress < 0.2f -> 1
        progress < 0.45f -> 2
        progress < 0.8f -> 3
        else -> 4
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Schritte",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            stages.forEachIndexed { index, stage ->
                val active = index < activeStageCount
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (active) AppTheme.colors.surfaceMuted.copy(alpha = 0.55f)
                            else AppTheme.colors.surface.copy(alpha = 0.78f),
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun StartAnalysisProgressCard(
    currentStepLabel: String,
    progress: Float,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = currentStepLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-analysis-progress"),
            )
        }
    }
}

@Composable
private fun StartAnalysisDraftCard(
    result: StartIntentAnalysis,
    currentDraft: CreateAreaDraft,
    summaryPreview: String,
    limitReached: Boolean,
    onTitleChange: (String) -> Unit,
    onMeaningChange: (String) -> Unit,
    onSourceKindChange: (DataSourceKind?) -> Unit,
    onApplySuggestion: (CreateAreaDraft) -> Unit,
) {
    val suggestion = result.suggestedDraft()
    val differsFromCurrent = currentDraft.title.trim() != suggestion.title ||
        currentDraft.meaning.trim() != suggestion.meaning ||
        currentDraft.sourceKind != suggestion.sourceKind
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
                        text = "Bereich",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (differsFromCurrent) {
                    TextButton(onClick = { onApplySuggestion(suggestion) }) {
                        Text("Zuruecksetzen")
                    }
                }
            }
            OutlinedTextField(
                value = currentDraft.title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-create-title"),
                placeholder = { Text(result.suggestedTitle) },
                singleLine = true,
            )
            OutlinedTextField(
                value = currentDraft.meaning,
                onValueChange = onMeaningChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-create-meaning"),
                placeholder = { Text(result.suggestedSummary) },
                minLines = 2,
                maxLines = 3,
                singleLine = false,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.28f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Feed",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.muted,
                    )
                    Text(
                        text = summaryPreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.ink,
                    )
                }
                Text(
                    text = result.statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.muted,
                )
            }
            if (result.selectableSources.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Quelle",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChoiceChipRow(
                        items = listOf(ChoiceChipItem(id = "none", label = "Ohne feste Quelle")) +
                            result.selectableSources.map { source ->
                                ChoiceChipItem(id = source.name, label = sourceKindLabel(source))
                            },
                        selectedId = currentDraft.sourceKind?.name ?: "none",
                        onSelect = { selected ->
                            if (selected == "none") {
                                onSourceKindChange(null)
                            } else {
                                onSourceKindChange(DataSourceKind.valueOf(selected))
                            }
                        },
                    )
                }
            }
            if (limitReached) {
                Text(
                    text = "Bereichslimit erreicht. Loesche erst einen Bereich in Einstellungen > Bereiche.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.danger,
                )
            }
        }
    }
}

@Composable
private fun StartAnalysisStatusCard(
    result: StartIntentAnalysis,
) {
    val accent = if (result.canCreate) AppTheme.colors.accent else AppTheme.colors.danger
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("start-analysis-result"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = result.statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                    )
                    Text(
                        text = result.suggestedTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (result.sourceRows.isEmpty()) {
                Text(
                    text = result.readinessDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.muted,
                )
            }
            StartAnalysisSourcesCard(
                rows = result.sourceRows,
                accent = accent,
            )
            if (result.blockingReason != null || result.implementationNote != null) {
                AnalysisMissingCard(
                    blockingReason = result.blockingReason,
                    implementationNote = result.implementationNote,
                )
            }
        }
    }
}

@Composable
private fun StartAnalysisSourcesCard(
    rows: List<StartAnalysisSourceRow>,
    accent: Color,
) {
    if (rows.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(accent.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.16f),
                shape = RoundedCornerShape(22.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Quellen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.ink,
        )
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppTheme.colors.outlineSoft.copy(alpha = 0.5f)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = row.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.ink,
                    )
                    if (row.hint.isNotBlank()) {
                        Text(
                            text = row.hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.muted,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = row.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactInfoPanel(
    label: String,
    title: String,
    detail: String? = null,
    accent: Color = AppTheme.colors.surfaceMuted,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(22.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.muted,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        detail?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnalysisMissingCard(
    blockingReason: String?,
    implementationNote: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppTheme.colors.danger.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Fehlt noch",
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.danger,
        )
        blockingReason?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.danger,
            )
        }
        implementationNote?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun queryBrowserApps(
    context: Context,
): List<String> {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
    val packageManager = context.packageManager
    val infos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    }
    return infos
        .mapNotNull { info -> info.loadLabel(packageManager)?.toString()?.takeIf(String::isNotBlank) }
        .distinct()
        .sorted()
}

private fun queryInstalledPackages(
    context: Context,
    packageNames: Set<String>,
): Set<String> {
    val packageManager = context.packageManager
    return packageNames.filterTo(mutableSetOf()) { packageName ->
        packageManager.getLaunchIntentForPackage(packageName) != null
    }
}

private fun behaviorLabel(
    behaviorClass: AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> "Beobachten"
        AreaBehaviorClass.PROGRESS -> "Voranbringen"
        AreaBehaviorClass.RELATIONSHIP -> "Beziehung"
        AreaBehaviorClass.MAINTENANCE -> "Pflegen"
        AreaBehaviorClass.PROTECTION -> "Absichern"
        AreaBehaviorClass.REFLECTION -> "Einordnen"
    }
}

@Composable
fun StartCreateIdentityOptionsScreen(
    draft: CreateAreaDraft,
    onBack: () -> Unit,
    onTemplateChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onResetToNeutral: () -> Unit,
) {
    val template = startAreaTemplate(draft.templateId)

    DaysPageScaffold(
        title = "Darstellung anpassen",
        onBack = onBack,
        modifier = Modifier.testTag("start-create-options-screen"),
    ) {
        StartCreatePreviewCard(
            title = draft.title,
            summary = draft.meaning.trim().ifBlank { template.summary },
            templateLabel = template.label,
            iconKey = draft.iconKey,
            iconLabel = startAreaIconOptions.firstOrNull { it.id == draft.iconKey }?.label.orEmpty(),
        )
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Grundform",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ChoiceChipRow(
                    items = startAreaTemplates.map { ChoiceChipItem(id = it.id, label = it.label) },
                    selectedId = draft.templateId,
                    onSelect = onTemplateChange,
                )
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ChoiceChipRow(
                    items = startAreaIconOptions.map { ChoiceChipItem(id = it.id, label = it.label) },
                    selectedId = draft.iconKey,
                    onSelect = onIconChange,
                )
                TextButton(
                    onClick = onResetToNeutral,
                    modifier = Modifier.testTag("start-create-reset-neutral"),
                ) {
                    Text("Neutral bleiben")
                }
            }
        }
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start-create-options-done"),
        ) {
            Text("Zurueck")
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: StartIntentSuggestion,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val outlineColor = if (selected) {
        AppTheme.colors.accent
    } else {
        AppTheme.colors.outlineSoft
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.96f))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = outlineColor.copy(alpha = if (selected) 0.42f else 0.18f),
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = startAreaIcon(suggestion.iconKey),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = suggestion.modeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StartCreatePreviewCard(
    title: String,
    summary: String,
    templateLabel: String,
    iconKey: String,
    iconLabel: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.94f),
        ),
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
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = startAreaIcon(iconKey),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title.ifBlank { "Neuer Bereich" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DaysMetaPill(label = templateLabel)
                    if (iconLabel.isNotBlank()) {
                        DaysMetaPill(label = iconLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun StartCreateIdentityLinkCard(
    templateLabel: String,
    iconLabel: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = AppTheme.colors.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .testTag("start-create-options-link"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Darstellung anpassen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DaysMetaPill(label = templateLabel)
            DaysMetaPill(label = iconLabel)
        }
    }
}

fun buildStartIntentSuggestions(
    inputKind: StartCreateInputKind,
    rawInput: String,
): List<StartIntentSuggestion> {
    val normalized = rawInput.trim()
    val inferred = inferPrimaryIntent(inputKind, normalized)
    val baseTitle = inferred.title.ifBlank { fallbackTitleFor(inputKind) }
    val summarySource = normalized.ifBlank { inferred.defaultSummary }
    val primary = StartIntentSuggestion(
        id = "primary-${inputKind.id}",
        title = baseTitle,
        summary = inferred.primarySummary(summarySource),
        templateId = inferred.primaryTemplateId,
        iconKey = inferred.primaryIconKey,
        behaviorClass = inferred.primaryBehavior,
        sourceKind = inferred.primarySourceKind,
        modeLabel = inferred.primaryModeLabel,
        hintLabel = inferred.primaryHint,
    )
    val secondary = StartIntentSuggestion(
        id = "secondary-${inputKind.id}",
        title = baseTitle,
        summary = inferred.secondarySummary(summarySource),
        templateId = inferred.secondaryTemplateId,
        iconKey = inferred.secondaryIconKey,
        behaviorClass = inferred.secondaryBehavior,
        sourceKind = inferred.secondarySourceKind,
        modeLabel = inferred.secondaryModeLabel,
        hintLabel = inferred.secondaryHint,
    )
    val exploration = StartIntentSuggestion(
        id = "explore-${inputKind.id}",
        title = if (normalized.isBlank()) fallbackTitleFor(inputKind) else "$baseTitle Radar",
        summary = "Ein freier Bereich, der zuerst nur sichtbar macht, was hier relevant werden koennte.",
        templateId = "free",
        iconKey = "spark",
        behaviorClass = AreaBehaviorClass.REFLECTION,
        modeLabel = "Offen starten",
        hintLabel = "Gut, wenn du die Aufgabe schon spueren kannst, aber die beste Darstellung noch offen ist.",
    )
    return listOf(primary, secondary, exploration)
}

private data class InferredStartIntent(
    val title: String,
    val defaultSummary: String,
    val primaryTemplateId: String,
    val primaryIconKey: String,
    val primaryBehavior: AreaBehaviorClass,
    val primaryModeLabel: String,
    val primaryHint: String,
    val primarySourceKind: DataSourceKind? = null,
    val secondaryTemplateId: String,
    val secondaryIconKey: String,
    val secondaryBehavior: AreaBehaviorClass,
    val secondaryModeLabel: String,
    val secondaryHint: String,
    val secondarySourceKind: DataSourceKind? = null,
) {
    fun primarySummary(source: String): String {
        return when (primaryBehavior) {
            AreaBehaviorClass.TRACKING -> "Dieser Bereich liest lokal, was hier neu oder auffaellig ist: ${source.take(140)}"
            AreaBehaviorClass.PROGRESS -> "Dieser Bereich soll aus dem Input sichtbare Folgeaktionen oder Routinen formen: ${source.take(140)}"
            AreaBehaviorClass.RELATIONSHIP -> "Dieser Bereich haelt Beziehung, Resonanz oder Kontakt aus dem Input zusammen: ${source.take(140)}"
            AreaBehaviorClass.MAINTENANCE -> "Dieser Bereich soll Pflege und Regelmaessigkeit rund um den Input tragen: ${source.take(140)}"
            AreaBehaviorClass.PROTECTION -> "Dieser Bereich soll Warnungen, Stoerungen oder wichtige Ausnahmen aus dem Input ruhig sichtbar machen: ${source.take(140)}"
            AreaBehaviorClass.REFLECTION -> "Dieser Bereich soll Material aus dem Input ordnen und verdichten: ${source.take(140)}"
        }
    }

    fun secondarySummary(source: String): String {
        return when (secondaryBehavior) {
            AreaBehaviorClass.TRACKING -> "Der Bereich beobachtet zuerst nur leise, was rund um ${source.take(110)} passiert."
            AreaBehaviorClass.PROGRESS -> "Der Bereich macht aus ${source.take(110)} spaeter konkrete naechste Zuege."
            AreaBehaviorClass.RELATIONSHIP -> "Der Bereich verbindet ${source.take(110)} mit Kontakt oder Resonanz."
            AreaBehaviorClass.MAINTENANCE -> "Der Bereich pflegt ${source.take(110)} regelmaessig und ohne Druck."
            AreaBehaviorClass.PROTECTION -> "Der Bereich schuetzt dich vor uebersehenen Signalen aus ${source.take(110)}."
            AreaBehaviorClass.REFLECTION -> "Der Bereich sammelt und sortiert ${source.take(110)} zuerst ruhig."
        }
    }
}

private fun inferPrimaryIntent(
    inputKind: StartCreateInputKind,
    rawInput: String,
): InferredStartIntent {
    val lower = rawInput.lowercase()
    val domainTitle = detectTitle(rawInput)
    return when {
        "kalender" in lower || "termin" in lower || "meeting" in lower || "besprech" in lower -> InferredStartIntent(
            title = domainTitle.ifBlank { "Kalender Heute" },
            defaultSummary = "Lokale Termine und Besprechungen nur als kompakten Tagesstand zeigen.",
            primaryTemplateId = "project",
            primaryIconKey = "calendar",
            primaryBehavior = AreaBehaviorClass.PROGRESS,
            primaryModeLabel = "Termine lesen",
            primaryHint = "Passt, wenn der Bereich lokale Kalendertermine direkt mitnehmen soll.",
            primarySourceKind = DataSourceKind.CALENDAR,
            secondaryTemplateId = "place",
            secondaryIconKey = "home",
            secondaryBehavior = AreaBehaviorClass.MAINTENANCE,
            secondaryModeLabel = "Termine einbetten",
            secondaryHint = "Passt, wenn der Bereich Kalender eher in eine wiederkehrende Tagesroutine einbetten soll.",
            secondarySourceKind = DataSourceKind.CALENDAR,
        )
        "podcast" in lower || "folge" in lower || "feed" in lower -> InferredStartIntent(
            title = domainTitle.ifBlank { "Podcast Radar" },
            defaultSummary = "Neue Folgen und Hoerimpulse ruhig sichtbar machen.",
            primaryTemplateId = "medium",
            primaryIconKey = "book",
            primaryBehavior = AreaBehaviorClass.REFLECTION,
            primaryModeLabel = "Folgen lesen",
            primaryHint = "Passt, wenn der Bereich neue Folgen oder Inhalte erst sammeln und nur das Wesentliche zeigen soll.",
            secondaryTemplateId = "project",
            secondaryIconKey = "trend",
            secondaryBehavior = AreaBehaviorClass.PROGRESS,
            secondaryModeLabel = "Folgen handeln",
            secondaryHint = "Passt, wenn aus neuen Folgen spaeter konkrete Hoer- oder Lernzuege entstehen sollen.",
        )
        "screenshot" in lower || "screen" in lower || "bild" in lower || inputKind == StartCreateInputKind.Screenshot -> InferredStartIntent(
            title = domainTitle.ifBlank { "Screenshot Radar" },
            defaultSummary = "Neue Screenshots lesen und nur auffaellige Inhalte sichtbar machen.",
            primaryTemplateId = "medium",
            primaryIconKey = "palette",
            primaryBehavior = AreaBehaviorClass.TRACKING,
            primaryModeLabel = "Screens lesen",
            primaryHint = "Passt, wenn Screenshots lokal gelesen und auf relevante Inhalte reduziert werden sollen.",
            secondaryTemplateId = "theme",
            secondaryIconKey = "focus",
            secondaryBehavior = AreaBehaviorClass.REFLECTION,
            secondaryModeLabel = "Muster finden",
            secondaryHint = "Passt, wenn Screenshots eher gesammelt, geordnet und spaeter gedeutet werden sollen.",
        )
        "nachricht" in lower || "notification" in lower || "benachr" in lower || "kontakt" in lower || inputKind == StartCreateInputKind.Contact -> InferredStartIntent(
            title = domainTitle.ifBlank { "Kontakt Blick" },
            defaultSummary = "Wichtige Nachrichten und Absender im Blick halten.",
            primaryTemplateId = "person",
            primaryIconKey = "chat",
            primaryBehavior = AreaBehaviorClass.PROTECTION,
            primaryModeLabel = "Wichtiges sehen",
            primaryHint = "Passt, wenn nur die wirklich relevanten Kontakte oder Nachrichten auffallen sollen.",
            primarySourceKind = DataSourceKind.NOTIFICATIONS,
            secondaryTemplateId = "person",
            secondaryIconKey = "care",
            secondaryBehavior = AreaBehaviorClass.RELATIONSHIP,
            secondaryModeLabel = "Kontakt pflegen",
            secondaryHint = "Passt, wenn aus Signalen eher Beziehungspflege und Follow-ups entstehen sollen.",
            secondarySourceKind = DataSourceKind.NOTIFICATIONS,
        )
        "zuhause" in lower || "home" in lower || "ort" in lower || inputKind == StartCreateInputKind.Location -> InferredStartIntent(
            title = domainTitle.ifBlank { "Ort Routine" },
            defaultSummary = "Am richtigen Ort die passende Routine oder Aufmerksamkeit sichtbar machen.",
            primaryTemplateId = "place",
            primaryIconKey = "home",
            primaryBehavior = AreaBehaviorClass.MAINTENANCE,
            primaryModeLabel = "Ort Routine",
            primaryHint = "Passt, wenn ein Bereich an einen Ort gebunden ruhig erinnern oder mitlaufen soll.",
            secondaryTemplateId = "place",
            secondaryIconKey = "shield",
            secondaryBehavior = AreaBehaviorClass.PROTECTION,
            secondaryModeLabel = "Ort Schutz",
            secondaryHint = "Passt, wenn am Ort eher Warnungen, Blocker oder wichtige Kontexte sichtbar werden sollen.",
        )
        "gesund" in lower || "sleep" in lower || "schlaf" in lower || "health" in lower -> InferredStartIntent(
            title = domainTitle.ifBlank { "Gesundheit Blick" },
            defaultSummary = "Koerper- und Gesundheitsdaten ruhig sichtbar halten.",
            primaryTemplateId = "ritual",
            primaryIconKey = "heart",
            primaryBehavior = AreaBehaviorClass.TRACKING,
            primaryModeLabel = "Koerper lesen",
            primaryHint = "Passt, wenn der Bereich Scores, Trends oder koerperliche Signale ruhig anzeigen soll.",
            primarySourceKind = DataSourceKind.HEALTH_CONNECT,
            secondaryTemplateId = "ritual",
            secondaryIconKey = "lotus",
            secondaryBehavior = AreaBehaviorClass.MAINTENANCE,
            secondaryModeLabel = "Gesund bleiben",
            secondaryHint = "Passt, wenn aus Koerpersignalen spaeter kleine Pflegezuege entstehen sollen.",
            secondarySourceKind = DataSourceKind.HEALTH_CONNECT,
        )
        inputKind == StartCreateInputKind.Link -> InferredStartIntent(
            title = domainTitle.ifBlank { "Link Radar" },
            defaultSummary = "Inhalte aus einem Link oder Feed in Ruhe beobachten.",
            primaryTemplateId = "medium",
            primaryIconKey = "book",
            primaryBehavior = AreaBehaviorClass.REFLECTION,
            primaryModeLabel = "Quelle lesen",
            primaryHint = "Passt, wenn eine Quelle zuerst gesammelt, gelesen und verdichtet werden soll.",
            secondaryTemplateId = "project",
            secondaryIconKey = "briefcase",
            secondaryBehavior = AreaBehaviorClass.PROGRESS,
            secondaryModeLabel = "Quelle nutzen",
            secondaryHint = "Passt, wenn aus einer Quelle spaeter klare Schritte oder Routinen entstehen sollen.",
        )
        else -> InferredStartIntent(
            title = domainTitle.ifBlank { fallbackTitleFor(inputKind) },
            defaultSummary = "Einen neuen Bereich ruhig einspielen und spaeter verfeinern.",
            primaryTemplateId = fallbackTemplateFor(inputKind),
            primaryIconKey = fallbackIconFor(inputKind),
            primaryBehavior = fallbackBehaviorFor(inputKind),
            primaryModeLabel = "Ruhig starten",
            primaryHint = "Passt, wenn du die Absicht schon klar spuerst und Days zuerst nur einen brauchbaren Bereich vorschlagen soll.",
            secondaryTemplateId = "theme",
            secondaryIconKey = "focus",
            secondaryBehavior = AreaBehaviorClass.REFLECTION,
            secondaryModeLabel = "Erst ordnen",
            secondaryHint = "Passt, wenn der Bereich noch offen ist und du das Thema zuerst sammeln und sortieren willst.",
        )
    }
}

private fun sourceKindLabel(sourceKind: DataSourceKind): String {
    return when (sourceKind) {
        DataSourceKind.CALENDAR -> "Kalender"
        DataSourceKind.NOTIFICATIONS -> "Benachrichtigungen"
        DataSourceKind.HEALTH_CONNECT -> "Health Connect"
        DataSourceKind.MANUAL -> "Manuell"
    }
}

private fun sourceSetupPlaceholder(sourceId: String): String {
    return when (sourceId) {
        "calendar" -> "z.B. welche Termine, Fenster oder Kalender hier wichtig sind"
        "notes" -> "z.B. welche Notizen oder manuellen Eintraege hier helfen"
        "web" -> "z.B. FAZ, stol.it oder andere Websites"
        "feeds" -> "z.B. welche Feeds oder URLs hier laufen sollen"
        "social_text" -> "z.B. X, Threads oder andere Textquellen"
        "social_image" -> "z.B. Instagram oder andere Bildquellen"
        "video" -> "z.B. YouTube oder andere Videoquellen"
        "screenshots" -> "z.B. wie Screenshots hier landen oder was daran wichtig ist"
        "notifications" -> "z.B. welche News-Apps Hinweise senden duerfen"
        "files" -> "z.B. welche Exporte oder Listen hier wichtig sind"
        "messenger" -> "z.B. welche Kontakte oder Messenger hier relevant sind"
        "places" -> "z.B. welche Orte oder Wege hier wichtig sind"
        "health" -> "z.B. welche Messwerte oder Gesundheitsdaten hier zaehlen"
        "photos" -> "z.B. welche Bilder oder Alben hier relevant sind"
        "mail" -> "z.B. welche Mails, Postfaecher oder Inhalte hier wichtig sind"
        "app_share" -> "z.B. aus welchen Apps Inhalte hier geteilt werden"
        "browser" -> "z.B. welche Browser-Links oder Exporte hier landen sollen"
        else -> "Was soll diese Quelle fuer den Bereich liefern?"
    }
}

internal fun StartIntentAnalysis.toSourceChoices(): List<StartCreateSourceChoice> {
    fun choices(vararg items: Triple<String, String, String>): List<StartCreateSourceChoice> {
        return items.map { (id, title, hint) ->
            StartCreateSourceChoice(id = id, title = title, hint = hint)
        }
    }

    if (family == StartIntentFamily.WEB_CONTENT) {
        return choices(
            Triple("web", "Web", "Artikel oder Websites"),
            Triple("feeds", "Feeds", "Laufende News-Quellen"),
            Triple("social_text", "Social Text", "Ausgewaehlte Posts"),
            Triple("social_image", "Social Bild", "Ausgewaehlte Bilder"),
            Triple("video", "Video", "Ausgewaehlte Videos"),
            Triple("screenshots", "Screenshots", "Post oder Artikel sichern"),
        )
    }

    return when (family) {
        StartIntentFamily.CALENDAR -> choices(
            Triple("calendar", "Kalender", "Termine und freie Fenster"),
            Triple("notes", "Notizen", "Eigene Einordnung"),
            Triple("screenshots", "Screenshots", "Termine oder Plaene sichern"),
        )
        StartIntentFamily.NOTIFICATIONS -> choices(
            Triple("notifications", "Benachrichtigungen", "Wichtige Hinweise"),
            Triple("messenger", "Messenger", "Ausgewaehlte Kontakte"),
            Triple("notes", "Notizen", "Manuelle Ergaenzungen"),
            Triple("screenshots", "Screenshots", "Chats oder Hinweise sichern"),
            Triple("calendar", "Kalender", "Rueckkehr oder Wiedervorlage"),
        )
        StartIntentFamily.HEALTH -> choices(
            Triple("health", "Health", "Koerper- und Erholungssignale"),
            Triple("notes", "Notizen", "Eigene Beobachtungen"),
            Triple("screenshots", "Screenshots", "Messwerte sichern"),
            Triple("calendar", "Kalender", "Praxis oder Routine"),
        )
        StartIntentFamily.ROUTINE -> choices(
            Triple("notes", "Notizen", "Manueller Verlauf"),
            Triple("calendar", "Kalender", "Wiederkehr und Zeiten"),
            Triple("screenshots", "Screenshots", "Zwischenstaende sichern"),
        )
        StartIntentFamily.LOCATION -> choices(
            Triple("places", "Orte", "Zuhause, Arbeit oder Wege"),
            Triple("calendar", "Kalender", "Termine entlang des Tages"),
            Triple("notes", "Notizen", "Ortsbezogene Hinweise"),
            Triple("screenshots", "Screenshots", "Plaene oder Orte sichern"),
        )
        StartIntentFamily.READING -> choices(
            Triple("web", "Web", "Artikel und Websites"),
            Triple("feeds", "Feeds", "Laufende Quellen"),
            Triple("files", "Dateien", "PDFs und Unterlagen"),
            Triple("notes", "Notizen", "Gedanken und Merker"),
            Triple("screenshots", "Screenshots", "Fundstellen sichern"),
        )
        StartIntentFamily.WRITING -> choices(
            Triple("notes", "Notizen", "Texte und Entwuerfe"),
            Triple("files", "Dateien", "Dokumente und Auszuege"),
            Triple("screenshots", "Screenshots", "Referenzen sichern"),
        )
        StartIntentFamily.ADMIN -> choices(
            Triple("files", "Dateien", "Unterlagen und PDFs"),
            Triple("calendar", "Kalender", "Fristen und Termine"),
            Triple("notifications", "Benachrichtigungen", "Erinnerungen"),
            Triple("web", "Web", "Portale und Websites"),
            Triple("notes", "Notizen", "Eigene Einordnung"),
        )
        StartIntentFamily.SHOPPING -> choices(
            Triple("web", "Web", "Shops und Produktseiten"),
            Triple("notifications", "Benachrichtigungen", "Liefer- oder Deal-Hinweise"),
            Triple("notes", "Notizen", "Listen und Merker"),
            Triple("files", "Dateien", "Rechnungen oder Listen"),
        )
        StartIntentFamily.FINANCE -> choices(
            Triple("files", "Dateien", "Kontoauszuege und PDFs"),
            Triple("notifications", "Benachrichtigungen", "Bank- oder Zahlhinweise"),
            Triple("web", "Web", "Portale und Finanzseiten"),
            Triple("notes", "Notizen", "Budget und Einordnung"),
        )
        StartIntentFamily.BOOKMARKS -> choices(
            Triple("browser", "Browser", "Einzelne Links oder Export"),
            Triple("files", "Dateien", "HTML-Export"),
            Triple("web", "Web", "Direkte Links"),
        )
        StartIntentFamily.FILES -> choices(
            Triple("files", "Dateien", "Dokumente und Unterlagen"),
            Triple("notes", "Notizen", "Eigene Einordnung"),
            Triple("screenshots", "Screenshots", "Ausschnitte sichern"),
        )
        StartIntentFamily.SCREENSHOTS -> choices(
            Triple("screenshots", "Screenshots", "Bilder direkt sichern"),
            Triple("notes", "Notizen", "Ergaenzungen"),
        )
        StartIntentFamily.PHOTOS -> choices(
            Triple("photos", "Bilder", "Fotos oder Galeriebilder"),
            Triple("screenshots", "Screenshots", "Ergaenzende Bilder"),
            Triple("notes", "Notizen", "Eigene Einordnung"),
        )
        StartIntentFamily.EMAIL -> choices(
            Triple("mail", "Mail", "Weitergeleitete Inhalte"),
            Triple("files", "Dateien", "EML oder Exporte"),
            Triple("notifications", "Benachrichtigungen", "Wichtige Hinweise"),
        )
        StartIntentFamily.WEB_CONTENT -> choices(
            Triple("web", "Web", "Artikel oder Websites"),
            Triple("feeds", "Feeds", "Laufende Quellen"),
            Triple("screenshots", "Screenshots", "Post oder Artikel sichern"),
        )
        StartIntentFamily.APP_CONTENT -> choices(
            Triple("app_share", "App-Share", "Geteilte Inhalte"),
            Triple("web", "Links", "Direkte Verweise"),
            Triple("notes", "Notizen", "Eigene Einordnung"),
            Triple("screenshots", "Screenshots", "App-Inhalte sichern"),
            Triple("files", "Dateien", "Exporte oder Anhaenge"),
        )
        StartIntentFamily.GENERAL -> choices(
            Triple("notes", "Notizen", "Freie Eingaben"),
            Triple("web", "Web", "Links oder Websites"),
            Triple("files", "Dateien", "Unterlagen"),
            Triple("screenshots", "Screenshots", "Schnelle Sicherung"),
        )
    }
}

private fun detectTitle(rawInput: String): String {
    val trimmed = rawInput.trim()
    if (trimmed.isBlank()) return ""
    val hostMatch = Regex("""https?://(?:www\.)?([^/\s]+)""").find(trimmed)
    if (hostMatch != null) {
        return hostMatch.groupValues[1]
            .substringBefore('.')
            .replaceFirstChar(Char::uppercaseChar)
    }
    val cleaned = trimmed
        .replace(Regex("""https?://\S+"""), "")
        .replace(Regex("""[^\p{L}\p{N}\s]"""), " ")
        .trim()
    val words = cleaned
        .split(Regex("""\s+"""))
        .filter { it.length > 2 }
        .take(3)
    return words.joinToString(" ").replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}

private fun fallbackTitleFor(inputKind: StartCreateInputKind): String {
    return when (inputKind) {
        StartCreateInputKind.Text -> "Neuer Bereich"
        StartCreateInputKind.Link -> "Link Radar"
        StartCreateInputKind.Screenshot -> "Screenshot Radar"
        StartCreateInputKind.App -> "App Bereich"
        StartCreateInputKind.Contact -> "Kontakt Blick"
        StartCreateInputKind.Location -> "Ort Routine"
    }
}

private fun fallbackTemplateFor(inputKind: StartCreateInputKind): String {
    return when (inputKind) {
        StartCreateInputKind.Text -> "free"
        StartCreateInputKind.Link -> "medium"
        StartCreateInputKind.Screenshot -> "medium"
        StartCreateInputKind.App -> "theme"
        StartCreateInputKind.Contact -> "person"
        StartCreateInputKind.Location -> "place"
    }
}

private fun fallbackIconFor(inputKind: StartCreateInputKind): String {
    return when (inputKind) {
        StartCreateInputKind.Text -> "spark"
        StartCreateInputKind.Link -> "book"
        StartCreateInputKind.Screenshot -> "palette"
        StartCreateInputKind.App -> "focus"
        StartCreateInputKind.Contact -> "chat"
        StartCreateInputKind.Location -> "home"
    }
}

private fun fallbackBehaviorFor(inputKind: StartCreateInputKind): AreaBehaviorClass {
    return when (inputKind) {
        StartCreateInputKind.Text -> AreaBehaviorClass.REFLECTION
        StartCreateInputKind.Link -> AreaBehaviorClass.REFLECTION
        StartCreateInputKind.Screenshot -> AreaBehaviorClass.TRACKING
        StartCreateInputKind.App -> AreaBehaviorClass.PROGRESS
        StartCreateInputKind.Contact -> AreaBehaviorClass.PROTECTION
        StartCreateInputKind.Location -> AreaBehaviorClass.MAINTENANCE
    }
}

private fun startCreateExamples(
    inputKind: StartCreateInputKind,
): List<String> {
    return when (inputKind) {
        StartCreateInputKind.Text -> listOf(
            "Ich will immer wissen, ob irgendwo etwas Wichtiges neu ist.",
            "Ich will diesen Bereich nur als ruhiges Radar und nicht als Arbeitsmodus.",
        )
        StartCreateInputKind.Link -> listOf(
            "Ich moechte diese Seite nur auf neue relevante Inhalte pruefen.",
            "Aus diesem Feed will ich spaeter nur Highlights sehen.",
        )
        StartCreateInputKind.Screenshot -> listOf(
            "Neue Screenshots lesen und nur Namen, Zahlen oder To-dos hervorheben.",
            "Screenshots nur als stilles Archiv anzeigen, bis ich sie brauche.",
        )
        StartCreateInputKind.App -> listOf(
            "Wenn in dieser App etwas Wichtiges auftaucht, will ich es direkt sehen.",
            "Diese App soll spaeter nur als Quelle fuer ruhige Hinweise dienen.",
        )
        StartCreateInputKind.Contact -> listOf(
            "Wenn X schreibt, will ich es von allem anderen unterscheiden koennen.",
            "Dieser Bereich soll Kontakt nur sichtbar machen, nicht gleich Handlung fordern.",
        )
        StartCreateInputKind.Location -> listOf(
            "Wenn ich zuhause bin, will ich an eine kleine Routine erinnert werden.",
            "An diesem Ort soll nur auffallen, wenn etwas fehlt oder schief laeuft.",
        )
    }
}
