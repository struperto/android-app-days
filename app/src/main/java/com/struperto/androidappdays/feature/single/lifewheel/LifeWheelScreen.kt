package com.struperto.androidappdays.feature.single.lifewheel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysSectionHeading
import com.struperto.androidappdays.feature.single.shared.PulseChipRow
import com.struperto.androidappdays.feature.single.shared.ScoreChipRow
import com.struperto.androidappdays.feature.single.shared.SingleFlowScaffold

@Composable
fun LifeWheelScreen(
    state: LifeWheelUiState,
    onRolesChange: (String) -> Unit,
    onResponsibilitiesChange: (String) -> Unit,
    onPriorityRulesChange: (String) -> Unit,
    onWeeklyRhythmChange: (String) -> Unit,
    onRecurringCommitmentsChange: (String) -> Unit,
    onGoodDayPatternChange: (String) -> Unit,
    onBadDayPatternChange: (String) -> Unit,
    onDayStartHourChange: (Int) -> Unit,
    onDayEndHourChange: (Int) -> Unit,
    onMorningEnergyChange: (Int) -> Unit,
    onAfternoonEnergyChange: (Int) -> Unit,
    onEveningEnergyChange: (Int) -> Unit,
    onFocusStrengthChange: (Int) -> Unit,
    onDisruptionSensitivityChange: (Int) -> Unit,
    onRecoveryNeedChange: (Int) -> Unit,
    onSaveFingerprint: () -> Unit,
    onCommitDiscovery: () -> Unit,
    onAreaLabelChange: (String, String) -> Unit,
    onAreaDefinitionChange: (String, String) -> Unit,
    onAreaTargetScoreChange: (String, Int) -> Unit,
    onSaveArea: (String) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
    onBack: () -> Unit,
) {
    SingleFlowScaffold(
        title = state.title,
        onBack = onBack,
        eyebrow = "Kalibrierung",
        summary = "Das Lebensrad schärft Rollen, Energieverlauf und Prioritäten. Es ist eine Hintergrundkalibrierung und kein täglicher Pflichtscreen.",
        modifier = Modifier.testTag("lifewheel-screen"),
    ) {
        DiscoveryCard(
            state = state,
            onSaveFingerprint = onSaveFingerprint,
            onCommitDiscovery = onCommitDiscovery,
        )
        if (state.dimensions.isNotEmpty()) {
            DimensionCard(state = state)
        }
        FingerprintFieldsCard(
            state = state,
            onRolesChange = onRolesChange,
            onResponsibilitiesChange = onResponsibilitiesChange,
            onPriorityRulesChange = onPriorityRulesChange,
            onWeeklyRhythmChange = onWeeklyRhythmChange,
            onRecurringCommitmentsChange = onRecurringCommitmentsChange,
            onGoodDayPatternChange = onGoodDayPatternChange,
            onBadDayPatternChange = onBadDayPatternChange,
            onDayStartHourChange = onDayStartHourChange,
            onDayEndHourChange = onDayEndHourChange,
            onMorningEnergyChange = onMorningEnergyChange,
            onAfternoonEnergyChange = onAfternoonEnergyChange,
            onEveningEnergyChange = onEveningEnergyChange,
            onFocusStrengthChange = onFocusStrengthChange,
            onDisruptionSensitivityChange = onDisruptionSensitivityChange,
            onRecoveryNeedChange = onRecoveryNeedChange,
        )
        if (state.activeAreas.isNotEmpty()) {
            DaysSectionHeading(
                title = "Aktive Bereiche",
                detail = "Diese Bereiche prägen später Soll, Ist und Drift im Tagesmodell.",
            )
        }
        state.activeAreas.forEach { area ->
            FingerprintAreaCard(
                area = area,
                onAreaLabelChange = onAreaLabelChange,
                onAreaDefinitionChange = onAreaDefinitionChange,
                onAreaTargetScoreChange = onAreaTargetScoreChange,
                onSaveArea = onSaveArea,
                onManualScoreChange = onManualScoreChange,
            )
        }
    }
}

@Composable
private fun DiscoveryCard(
    state: LifeWheelUiState,
    onSaveFingerprint: () -> Unit,
    onCommitDiscovery: () -> Unit,
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
                text = state.todayLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Discovery Tag ${state.discoveryDay}/7",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state.overviewText,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (state.discoveryCommitted) {
                    "Commit gesetzt. Ab jetzt verfeinert die App das Bild täglich weiter."
                } else {
                    "Noch offen. Schärfe Rollen, Prioritäten und Tagesmuster und committe dann die erste Fingerprint-Version."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSaveFingerprint) {
                    Text("Speichern")
                }
                Button(
                    onClick = onCommitDiscovery,
                    enabled = !state.discoveryCommitted && state.canCommitDiscovery,
                ) {
                    Text("Discovery committen")
                }
            }
        }
    }
}

@Composable
private fun DimensionCard(
    state: LifeWheelUiState,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Dimensionen",
                style = MaterialTheme.typography.titleMedium,
            )
            state.dimensions.forEach { dimension ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = dimension.label,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = "${(dimension.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = dimension.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ConfidenceBar(confidence = dimension.confidence)
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBar(
    confidence: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(999.dp),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(confidence.coerceIn(0f, 1f))
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(999.dp),
                )
                .padding(vertical = 4.dp),
        ) { }
    }
}

@Composable
private fun FingerprintFieldsCard(
    state: LifeWheelUiState,
    onRolesChange: (String) -> Unit,
    onResponsibilitiesChange: (String) -> Unit,
    onPriorityRulesChange: (String) -> Unit,
    onWeeklyRhythmChange: (String) -> Unit,
    onRecurringCommitmentsChange: (String) -> Unit,
    onGoodDayPatternChange: (String) -> Unit,
    onBadDayPatternChange: (String) -> Unit,
    onDayStartHourChange: (Int) -> Unit,
    onDayEndHourChange: (Int) -> Unit,
    onMorningEnergyChange: (Int) -> Unit,
    onAfternoonEnergyChange: (Int) -> Unit,
    onEveningEnergyChange: (Int) -> Unit,
    onFocusStrengthChange: (Int) -> Unit,
    onDisruptionSensitivityChange: (Int) -> Unit,
    onRecoveryNeedChange: (Int) -> Unit,
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
                text = "Nutzerbild",
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = state.rolesText,
                onValueChange = onRolesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rollen") },
                minLines = 2,
            )
            OutlinedTextField(
                value = state.responsibilitiesText,
                onValueChange = onResponsibilitiesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Verantwortungen") },
                minLines = 2,
            )
            OutlinedTextField(
                value = state.priorityRulesText,
                onValueChange = onPriorityRulesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Prioritätenregeln") },
                minLines = 3,
            )
            OutlinedTextField(
                value = state.weeklyRhythm,
                onValueChange = onWeeklyRhythmChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Wochen- und Tagesrhythmus") },
                minLines = 2,
            )
            OutlinedTextField(
                value = state.recurringCommitmentsText,
                onValueChange = onRecurringCommitmentsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Wiederkehrende Verpflichtungen") },
                minLines = 2,
            )
            Text(
                text = "Tag sichtbar machen",
                style = MaterialTheme.typography.labelLarge,
            )
            HourSelectionRow(
                label = "Start",
                selectedHour = state.dayStartHour,
                hours = (5..10).toList(),
                onSelect = onDayStartHourChange,
            )
            HourSelectionRow(
                label = "Ende",
                selectedHour = state.dayEndHour,
                hours = (18..24).toList(),
                onSelect = onDayEndHourChange,
            )
            Text(
                text = "Energiekurve",
                style = MaterialTheme.typography.labelLarge,
            )
            EnergyRow("Morgen", state.morningEnergy, onMorningEnergyChange)
            EnergyRow("Mittag", state.afternoonEnergy, onAfternoonEnergyChange)
            EnergyRow("Abend", state.eveningEnergy, onEveningEnergyChange)
            Text(
                text = "Fokusprofil",
                style = MaterialTheme.typography.labelLarge,
            )
            EnergyRow("Fokusdruck", state.focusStrength, onFocusStrengthChange)
            EnergyRow("Stoerprofil", state.disruptionSensitivity, onDisruptionSensitivityChange)
            EnergyRow("Erholungsbedarf", state.recoveryNeed, onRecoveryNeedChange)
            OutlinedTextField(
                value = state.goodDayPattern,
                onValueChange = onGoodDayPatternChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Guter Tag") },
                minLines = 2,
            )
            OutlinedTextField(
                value = state.badDayPattern,
                onValueChange = onBadDayPatternChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Schlechter Tag") },
                minLines = 2,
            )
        }
    }
}

@Composable
private fun HourSelectionRow(
    label: String,
    selectedHour: Int,
    hours: List<Int>,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        ChoiceChipRow(
            items = hours.map { hour ->
                ChoiceChipItem(
                    id = hour.toString(),
                    label = "${hour}:00",
                )
            },
            selectedId = selectedHour.toString(),
            onSelect = { onSelect(it.toInt()) },
        )
    }
}

@Composable
private fun EnergyRow(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        ScoreChipRow(
            selectedScore = value,
            onSelect = onChange,
        )
    }
}

@Composable
private fun FingerprintAreaCard(
    area: LifeWheelAreaCard,
    onAreaLabelChange: (String, String) -> Unit,
    onAreaDefinitionChange: (String, String) -> Unit,
    onAreaTargetScoreChange: (String, Int) -> Unit,
    onSaveArea: (String) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
) {
    var expanded by rememberSaveable(area.id) { mutableStateOf(false) }

    Card {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = area.label,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = area.definition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Gewicht ${area.targetScore}/5 · Confidence ${(area.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Fertig" else "Anpassen")
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Heute, wenn du willst",
                    style = MaterialTheme.typography.labelLarge,
                )
                PulseChipRow(
                    selectedScore = area.manualScore,
                    onSelect = { onManualScoreChange(area.id, it) },
                )
            }
            if (expanded) {
                OutlinedTextField(
                    value = area.label,
                    onValueChange = { onAreaLabelChange(area.id, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Bereich") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = area.definition,
                    onValueChange = { onAreaDefinitionChange(area.id, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Definition") },
                    minLines = 2,
                )
                ScoreChipRow(
                    selectedScore = area.targetScore,
                    onSelect = { onAreaTargetScoreChange(area.id, it) },
                )
                Button(
                    onClick = {
                        onSaveArea(area.id)
                        expanded = false
                    },
                    enabled = area.isDirty,
                ) {
                    Text("Bereich speichern")
                }
            }
        }
    }
}
