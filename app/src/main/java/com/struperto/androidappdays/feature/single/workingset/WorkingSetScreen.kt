package com.struperto.androidappdays.feature.single.workingset

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
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.data.repository.Vorhaben
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysCard
import com.struperto.androidappdays.feature.single.shared.DaysEmptyStateCard
import com.struperto.androidappdays.feature.single.shared.DaysSectionHeading
import com.struperto.androidappdays.feature.single.shared.SingleFlowScaffold

@Composable
fun WorkingSetScreen(
    state: WorkingSetUiState,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAreaSelected: (String) -> Unit,
    onSave: () -> Unit,
    onOpenPlan: (String) -> Unit,
    onArchive: (String) -> Unit,
    onBack: () -> Unit,
) {
    SingleFlowScaffold(
        title = "Vorhaben",
        onBack = onBack,
        eyebrow = "Später oder größer",
        summary = "Vorhaben bündeln Dinge, die nicht direkt in den heutigen Plan gehören. So bleibt Home leicht und trotzdem verlierst du nichts.",
    ) {
        DaysCard(elevated = true) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.pendingCaptureText != null) {
                    Text(
                        text = "Aus Capture übernommen",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bereich",
                        style = MaterialTheme.typography.labelLarge,
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
                }
                OutlinedTextField(
                    value = state.draftTitle,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titel") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.draftNote,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notiz") },
                    minLines = 3,
                )
                Button(
                    onClick = onSave,
                    enabled = state.draftTitle.isNotBlank() && state.selectedAreaId != null,
                ) {
                    Text("Anlegen")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DaysSectionHeading(
                title = "Aktiv",
                detail = "Aktive Vorhaben können später in den Tagesplan gezogen oder archiviert werden.",
            )
            if (state.items.isEmpty()) {
                DaysEmptyStateCard(
                    title = "Noch kein Vorhaben",
                    detail = "Wenn ein Capture größer wird als eine Tagesaufgabe, landet es hier als länger tragende Einheit.",
                )
            }
            state.items.forEach { item ->
                VorhabenCard(
                    item = item,
                    areaLabel = state.areas.firstOrNull { it.id == item.areaId }?.label.orEmpty(),
                    onOpenPlan = { onOpenPlan(item.id) },
                    onArchive = { onArchive(item.id) },
                )
            }
        }
    }
}

@Composable
private fun VorhabenCard(
    item: Vorhaben,
    areaLabel: String,
    onOpenPlan: () -> Unit,
    onArchive: () -> Unit,
) {
    DaysCard {
        Column(
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
                Button(
                    onClick = onOpenPlan,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("In Plan")
                }
                OutlinedButton(
                    onClick = onArchive,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Archivieren")
                }
            }
        }
    }
}
