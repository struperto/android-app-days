package com.struperto.androidappdays.feature.single.workbench

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.data.repository.TimeBlock
import com.struperto.androidappdays.data.repository.Vorhaben
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.SingleFlowScaffold
import com.struperto.androidappdays.feature.single.shared.TimeBlockChipRow
import java.util.Locale

@Composable
fun WorkbenchScreen(
    state: WorkbenchUiState,
    onPaneSelected: (WorkbenchPane) -> Unit,
    onDraftChange: (String) -> Unit,
    onAreaSelected: (String) -> Unit,
    onTimeBlockSelected: (TimeBlock) -> Unit,
    onSubmit: () -> Unit,
    onImportClipboard: (String?) -> Unit,
    onVoiceTranscript: (String?) -> Unit,
    onShowShareHint: () -> Unit,
    onAssistDraft: () -> Unit,
    onAssistLatest: () -> Unit,
    onSignalSelected: (WorkbenchSignal) -> Unit,
    onClearFeedback: () -> Unit,
    onCaptureToToday: (String) -> Unit,
    onCaptureToLater: (String) -> Unit,
    onCaptureDone: (String) -> Unit,
    onVorhabenToToday: (String) -> Unit,
    onVorhabenDone: (String) -> Unit,
    onTogglePlanDone: (String) -> Unit,
    onRemovePlan: (String) -> Unit,
    onBack: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showAssistPanel by rememberSaveable { mutableStateOf(false) }
    var showSignalPanel by rememberSaveable { mutableStateOf(true) }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val transcript = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        onVoiceTranscript(transcript)
    }

    SingleFlowScaffold(
        title = "Werkbank",
        onBack = onBack,
        eyebrow = "Arbeitsfläche",
        summary = "Die Werkbank sammelt Rohmaterial, Assistenz und Signale an einem Ort. Sie bleibt sekundär zu Home, soll aber als produktive Zwischenstation klar lesbar sein.",
        modifier = Modifier.testTag("workbench-screen"),
    ) {
        WorkbenchHeroCard(
            state = state,
            onImportClipboard = {
                onImportClipboard(clipboardManager.getText()?.text)
                showSignalPanel = true
            },
            onStartVoice = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Sprich deinen Gedanken ein")
                }
                try {
                    voiceLauncher.launch(intent)
                } catch (_: ActivityNotFoundException) {
                    onVoiceTranscript(null)
                }
            },
            onShowShareHint = {
                onShowShareHint()
                showSignalPanel = true
            },
            onToggleAssist = { showAssistPanel = !showAssistPanel },
            onToggleSignals = { showSignalPanel = !showSignalPanel },
        )

        state.toolFeedback?.let { feedback ->
            FeedbackCard(
                text = feedback,
                onDismiss = onClearFeedback,
            )
        }

        PaneSelectorRow(
            activePane = state.activePane,
            onPaneSelected = onPaneSelected,
        )

        WorkbenchComposerCard(
            state = state,
            onDraftChange = onDraftChange,
            onAreaSelected = onAreaSelected,
            onTimeBlockSelected = onTimeBlockSelected,
            onSubmit = onSubmit,
        )

        if (showAssistPanel) {
            AssistPanel(
                state = state,
                onAssistDraft = onAssistDraft,
                onAssistLatest = onAssistLatest,
            )
        }

        if (showSignalPanel) {
            SignalPanel(
                signals = state.signals,
                onSignalSelected = onSignalSelected,
            )
        }

        when (state.activePane) {
            WorkbenchPane.NEU -> {
                WorkbenchSectionTitle(text = "Neu aufgenommen")
                if (state.captures.isEmpty()) {
                    EmptyStateCard(text = "Noch nichts offen. Stimme, Teilen und Zwischenablage landen hier.")
                }
                state.captures.forEach { item ->
                    CaptureWorkbenchCard(
                        item = item,
                        areaLabel = state.areas.firstOrNull { it.id == item.areaId }?.label,
                        onToToday = { onCaptureToToday(item.id) },
                        onToLater = { onCaptureToLater(item.id) },
                        onDone = { onCaptureDone(item.id) },
                    )
                }
            }

            WorkbenchPane.HEUTE -> {
                WorkbenchSectionTitle(text = "Heute im Blick")
                if (state.planItems.isEmpty()) {
                    EmptyStateCard(text = "Noch nichts für heute gesetzt. Ziehe etwas aus Neu oder Später hier hinein.")
                }
                state.planItems.forEach { item ->
                    PlanWorkbenchCard(
                        item = item,
                        areaLabel = state.areas.firstOrNull { it.id == item.areaId }?.label.orEmpty(),
                        onToggleDone = { onTogglePlanDone(item.id) },
                        onRemove = { onRemovePlan(item.id) },
                    )
                }
            }

            WorkbenchPane.SPAETER -> {
                WorkbenchSectionTitle(text = "Später wichtig")
                if (state.vorhaben.isEmpty()) {
                    EmptyStateCard(text = "Noch nichts für später gesammelt.")
                }
                state.vorhaben.forEach { item ->
                    VorhabenWorkbenchCard(
                        item = item,
                        areaLabel = state.areas.firstOrNull { it.id == item.areaId }?.label.orEmpty(),
                        onToToday = { onVorhabenToToday(item.id) },
                        onDone = { onVorhabenDone(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkbenchHeroCard(
    state: WorkbenchUiState,
    onImportClipboard: () -> Unit,
    onStartVoice: () -> Unit,
    onShowShareHint: () -> Unit,
    onToggleAssist: () -> Unit,
    onToggleSignals: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Alles landet hier, bevor es den Tag formt.",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Nutze Text, Stimme, Zwischenablage oder System-Teilen und sortiere dann in Neu, Heute oder Später.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WorkbenchToolRow(
                onImportClipboard = onImportClipboard,
                onStartVoice = onStartVoice,
                onShowShareHint = onShowShareHint,
                onToggleAssist = onToggleAssist,
                onToggleSignals = onToggleSignals,
            )
            state.latestCapturePreview?.let { preview ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Letztes offenes Neu",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkbenchToolRow(
    onImportClipboard: () -> Unit,
    onStartVoice: () -> Unit,
    onShowShareHint: () -> Unit,
    onToggleAssist: () -> Unit,
    onToggleSignals: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ToolChip(
            label = WorkbenchTool.TEXT.label,
            icon = Icons.Outlined.TextFields,
            onClick = {},
        )
        ToolChip(
            label = WorkbenchTool.STIMME.label,
            icon = Icons.Outlined.GraphicEq,
            onClick = onStartVoice,
        )
        ToolChip(
            label = WorkbenchTool.ZWISCHENABLAGE.label,
            icon = Icons.Outlined.ContentPaste,
            onClick = onImportClipboard,
        )
        ToolChip(
            label = WorkbenchTool.TEILEN.label,
            icon = Icons.Outlined.IosShare,
            onClick = onShowShareHint,
        )
        ToolChip(
            label = WorkbenchTool.ASSIST.label,
            icon = Icons.Outlined.AutoAwesome,
            onClick = onToggleAssist,
        )
        ToolChip(
            label = WorkbenchTool.SIGNALE.label,
            icon = Icons.Outlined.NotificationsNone,
            onClick = onToggleSignals,
        )
    }
}

@Composable
private fun ToolChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        colors = FilterChipDefaults.filterChipColors(),
    )
}

@Composable
private fun FeedbackCard(
    text: String,
    onDismiss: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("OK")
            }
        }
    }
}

@Composable
private fun PaneSelectorRow(
    activePane: WorkbenchPane,
    onPaneSelected: (WorkbenchPane) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        WorkbenchPane.entries.forEach { pane ->
            Button(
                onClick = { onPaneSelected(pane) },
                modifier = Modifier.weight(1f),
                colors = if (pane == activePane) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            ) {
                Text(text = pane.label)
            }
        }
    }
}

@Composable
private fun WorkbenchComposerCard(
    state: WorkbenchUiState,
    onDraftChange: (String) -> Unit,
    onAreaSelected: (String) -> Unit,
    onTimeBlockSelected: (TimeBlock) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = when (state.activePane) {
                    WorkbenchPane.NEU -> "Schnell rein, später entscheiden."
                    WorkbenchPane.HEUTE -> "Forme den Tag direkt aus der Werkbank."
                    WorkbenchPane.SPAETER -> "Lege Relevantes ab, ohne Heute zu überladen."
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            ChoiceChipRow(
                items = state.areas.map { area ->
                    ChoiceChipItem(
                        id = area.id,
                        label = area.label,
                    )
                },
                selectedId = state.selectedAreaId,
                onSelect = onAreaSelected,
            )
            if (state.activePane == WorkbenchPane.HEUTE) {
                TimeBlockChipRow(
                    selectedTimeBlock = state.selectedTimeBlock,
                    onSelect = onTimeBlockSelected,
                )
            }
            OutlinedTextField(
                value = state.draftText,
                onValueChange = onDraftChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(state.draftPlaceholder) },
                minLines = 3,
            )
            Button(
                onClick = onSubmit,
                enabled = state.draftText.isNotBlank(),
            ) {
                Text(state.submitLabel)
            }
        }
    }
}

@Composable
private fun AssistPanel(
    state: WorkbenchUiState,
    onAssistDraft: () -> Unit,
    onAssistLatest: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Assist",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Lokal, klein und direkt auf den aktuellen Entwurf oder das letzte Neu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onAssistDraft) {
                    Text("Auf Entwurf")
                }
                Button(onClick = onAssistLatest) {
                    Text("Auf letztes Neu")
                }
            }
            if (state.assistSummary != null || state.assistNextStep != null) {
                state.assistSummary?.let {
                    Text(
                        text = "Kurz: $it",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                state.assistNextStep?.let {
                    Text(
                        text = "Nächster Schritt: $it",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                Text(
                    text = "Noch keine Assist-Ausgabe vorhanden.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SignalPanel(
    signals: List<WorkbenchSignal>,
    onSignalSelected: (WorkbenchSignal) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WorkbenchSectionTitle(text = "Signale")
        if (signals.isEmpty()) {
            EmptyStateCard(text = "Noch keine relevanten Signale. Sobald etwas in Neu, Heute oder Später lebt, taucht es hier auf.")
        }
        signals.forEach { signal ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = signal.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = signal.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    signal.targetPane?.let { target ->
                        Button(onClick = { onSignalSelected(signal) }) {
                            Text("Zu ${target.label}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkbenchSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun EmptyStateCard(text: String) {
    Card {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CaptureWorkbenchCard(
    item: CaptureItem,
    areaLabel: String?,
    onToToday: () -> Unit,
    onToLater: () -> Unit,
    onDone: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (areaLabel != null) {
                Text(
                    text = areaLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onToToday, modifier = Modifier.weight(1f)) {
                    Text("Heute")
                }
                Button(onClick = onToLater, modifier = Modifier.weight(1f)) {
                    Text("Später")
                }
                Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                    Text("Fertig")
                }
            }
        }
    }
}

@Composable
private fun VorhabenWorkbenchCard(
    item: Vorhaben,
    areaLabel: String,
    onToToday: () -> Unit,
    onDone: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = areaLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onToToday, modifier = Modifier.weight(1f)) {
                    Text("Heute")
                }
                Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                    Text("Fertig")
                }
            }
        }
    }
}

@Composable
private fun PlanWorkbenchCard(
    item: PlanItem,
    areaLabel: String,
    onToggleDone: () -> Unit,
    onRemove: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "$areaLabel · ${item.timeBlock.label}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onToggleDone, modifier = Modifier.weight(1f)) {
                    Text(if (item.isDone) "Wieder offen" else "Erledigt")
                }
                Button(onClick = onRemove, modifier = Modifier.weight(1f)) {
                    Text("Entfernen")
                }
            }
        }
    }
}
