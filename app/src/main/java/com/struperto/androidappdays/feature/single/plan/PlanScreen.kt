package com.struperto.androidappdays.feature.single.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.data.repository.PlanItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysCard
import com.struperto.androidappdays.feature.single.shared.DaysEmptyStateCard
import com.struperto.androidappdays.feature.single.shared.DaysSectionHeading
import com.struperto.androidappdays.feature.single.shared.SingleFlowScaffold
import com.struperto.androidappdays.feature.single.shared.TimeBlockChipRow

@Composable
fun PlanScreen(
    state: PlanUiState,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAreaSelected: (String) -> Unit,
    onTimeBlockSelected: (com.struperto.androidappdays.data.repository.TimeBlock) -> Unit,
    onSave: () -> Unit,
    onToggleDone: (String) -> Unit,
    onRemove: (String) -> Unit,
    onBack: () -> Unit,
) {
    SingleFlowScaffold(
        title = "Plan",
        onBack = onBack,
        eyebrow = "Heute strukturieren",
        summary = "Lege die wenigen Punkte fest, die den Tag tragen sollen. Bereich und Zeitblock machen aus einer losen Aufgabe eine belastbare Tagesentscheidung.",
        modifier = Modifier.testTag("plan-screen"),
    ) {
        DaysCard(elevated = true) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = state.todayLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (state.pendingSource != null) {
                    Text(
                        text = state.pendingSource.kindLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = state.pendingSource.summary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    OutlinedTextField(
                        value = state.draftTitle,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Neu für heute") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.draftNote,
                        onValueChange = onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notiz") },
                        minLines = 2,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bereich",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (state.areaSelectionEnabled) {
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
                    } else {
                        Text(
                            text = state.areas.firstOrNull { it.id == state.selectedAreaId }?.label.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Zeitblock",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    TimeBlockChipRow(
                        selectedTimeBlock = state.selectedTimeBlock,
                        onSelect = onTimeBlockSelected,
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = state.selectedAreaId != null && (state.pendingSource != null || state.draftTitle.isNotBlank()),
                ) {
                    Text(if (state.pendingSource == null) "Hinzufügen" else "Für heute übernehmen")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DaysSectionHeading(
                title = "Heute",
                detail = "Bestehende Planpunkte bleiben direkt editierbar, damit der Tagesplan lebendig bleibt.",
            )
            if (state.items.isEmpty()) {
                DaysEmptyStateCard(
                    title = "Noch kein Planpunkt",
                    detail = "Sobald du etwas für heute setzt, erscheint es hier als klare Tagesentscheidung.",
                )
            }
            state.items.forEach { item ->
                PlanItemCard(
                    item = item,
                    areaLabel = state.areas.firstOrNull { it.id == item.areaId }?.label.orEmpty(),
                    onToggleDone = { onToggleDone(item.id) },
                    onRemove = { onRemove(item.id) },
                )
            }
        }
    }
}

@Composable
private fun PlanItemCard(
    item: PlanItem,
    areaLabel: String,
    onToggleDone: () -> Unit,
    onRemove: () -> Unit,
) {
    DaysCard {
        Column(
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
                Button(
                    onClick = onToggleDone,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (item.isDone) "Wieder offen" else "Erledigt")
                }
                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Entfernen")
                }
            }
        }
    }
}
