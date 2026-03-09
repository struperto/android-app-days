package com.struperto.androidappdays.feature.single.capture

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
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysCard
import com.struperto.androidappdays.feature.single.shared.DaysEmptyStateCard
import com.struperto.androidappdays.feature.single.shared.DaysSectionHeading
import com.struperto.androidappdays.feature.single.shared.SingleFlowScaffold

@Composable
fun CaptureScreen(
    state: CaptureUiState,
    onDraftChange: (String) -> Unit,
    onAreaSelected: (String) -> Unit,
    onSave: () -> Unit,
    onDone: (String) -> Unit,
    onOpenVorhaben: (String) -> Unit,
    onOpenPlan: (String) -> Unit,
    onBack: () -> Unit,
) {
    SingleFlowScaffold(
        title = "Erfassen",
        onBack = onBack,
        eyebrow = "Input sammeln",
        summary = "Halte rohe Gedanken schnell fest. Bereiche sind optional, damit Erfassen leicht bleibt und erst danach sortiert werden muss.",
        modifier = Modifier.testTag("capture-screen"),
    ) {
        DaysCard(elevated = true) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Halte Gedanken schnell fest und gib ihnen auf Wunsch schon einen Bereich mit.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bereich (optional)",
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
                    value = state.draftText,
                    onValueChange = onDraftChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Gedanke") },
                    minLines = 3,
                )
                Button(
                    onClick = onSave,
                    enabled = state.draftText.isNotBlank(),
                ) {
                    Text("Speichern")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DaysSectionHeading(
                title = "Offen",
                detail = "Aus offenen Captures werden später konkrete Vorhaben oder Tagespunkte.",
            )
            if (state.items.isEmpty()) {
                DaysEmptyStateCard(
                    title = "Nichts offen",
                    detail = "Neue Captures landen hier, bis du sie in ein Vorhaben, einen Tagesplan oder in den Abschluss überführst.",
                )
            }
            state.items.forEach { item ->
                CaptureItemCard(
                    item = item,
                    areaLabel = state.areas.firstOrNull { it.id == item.areaId }?.label,
                    onDone = { onDone(item.id) },
                    onOpenVorhaben = { onOpenVorhaben(item.id) },
                    onOpenPlan = { onOpenPlan(item.id) },
                )
            }
        }
    }
}

@Composable
private fun CaptureItemCard(
    item: CaptureItem,
    areaLabel: String?,
    onDone: () -> Unit,
    onOpenVorhaben: () -> Unit,
    onOpenPlan: () -> Unit,
) {
    DaysCard {
        Column(
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
                Button(
                    onClick = onOpenVorhaben,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Zu Vorhaben")
                }
                Button(
                    onClick = onOpenPlan,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Für heute")
                }
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Fertig")
                }
            }
        }
    }
}
