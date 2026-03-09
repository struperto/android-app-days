package com.struperto.androidappdays.feature.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.feature.single.shared.MultiChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.PulseChipRow
import com.struperto.androidappdays.ui.theme.AppTheme

@Composable
fun AreaStudioScreen(
    state: AreaStudioUiState,
    areaId: String,
    onBack: () -> Unit,
    onTargetScoreChange: (String, Float) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
    onCadenceChange: (String, String) -> Unit,
    onIntensityChange: (String, Float) -> Unit,
    onSignalBlendChange: (String, Float) -> Unit,
    onToggleTrack: (String, String) -> Unit,
    onRemindersChange: (String, Boolean) -> Unit,
    onReviewChange: (String, Boolean) -> Unit,
    onExperimentsChange: (String, Boolean) -> Unit,
) {
    val area = state.areas[areaId] ?: return

    DaysPageScaffold(
        title = area.title,
        onBack = onBack,
        modifier = Modifier.testTag("area-studio-screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("area-studio-content"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AreaInfoTile(area = area)
            AreaWorkbenchTile(
                area = area,
                onTargetScoreChange = onTargetScoreChange,
                onManualScoreChange = onManualScoreChange,
                onCadenceChange = onCadenceChange,
                onIntensityChange = onIntensityChange,
                onSignalBlendChange = onSignalBlendChange,
                onToggleTrack = onToggleTrack,
                onRemindersChange = onRemindersChange,
                onReviewChange = onReviewChange,
                onExperimentsChange = onExperimentsChange,
            )
        }
    }
}

@Composable
private fun AreaInfoTile(
    area: AreaStudioAreaState,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = startAreaIcon(area.areaId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = area.title,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = area.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DaysMetaPill(label = "Soll ${area.targetScore}/5")
                DaysMetaPill(label = "Heute ${(area.manualScore ?: 0)}/5")
                DaysMetaPill(label = "${area.selectedTracks.size} Tracks")
            }
            LinearProgressIndicator(
                progress = { ((area.manualScore ?: 0).toFloat() / area.targetScore.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp)),
            )
        }
    }
}

@Composable
private fun AreaWorkbenchTile(
    area: AreaStudioAreaState,
    onTargetScoreChange: (String, Float) -> Unit,
    onManualScoreChange: (String, Int?) -> Unit,
    onCadenceChange: (String, String) -> Unit,
    onIntensityChange: (String, Float) -> Unit,
    onSignalBlendChange: (String, Float) -> Unit,
    onToggleTrack: (String, String) -> Unit,
    onRemindersChange: (String, Boolean) -> Unit,
    onReviewChange: (String, Boolean) -> Unit,
    onExperimentsChange: (String, Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area-studio-work-tile"),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ControlSection(title = "Tracks") {
                MultiChoiceChipRow(
                    items = area.tracks.map { ChoiceChipItem(id = it, label = it) },
                    selectedIds = area.selectedTracks,
                    onToggle = { onToggleTrack(area.areaId, it) },
                )
            }
            HorizontalDivider()
            ControlSection(title = "Soll") {
                Slider(
                    value = area.targetScore.toFloat(),
                    onValueChange = { onTargetScoreChange(area.areaId, it) },
                    valueRange = 1f..5f,
                    steps = 3,
                )
                MetricRead(label = "Ziel", value = "${area.targetScore}/5")
            }
            HorizontalDivider()
            ControlSection(title = "Heute") {
                PulseChipRow(
                    selectedScore = area.manualScore,
                    onSelect = { onManualScoreChange(area.areaId, it) },
                )
                LinearProgressIndicator(
                    progress = { ((area.manualScore ?: 0) / 5f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp)),
                )
            }
            HorizontalDivider()
            ControlSection(title = "Rhythmus") {
                ChoiceChipRow(
                    items = listOf(
                        ChoiceChipItem("daily", "Taeglich"),
                        ChoiceChipItem("weekly", "Woechentlich"),
                        ChoiceChipItem("adaptive", "Adaptiv"),
                    ),
                    selectedId = area.cadence,
                    onSelect = { onCadenceChange(area.areaId, it) },
                )
            }
            HorizontalDivider()
            ControlSection(title = "Steuerung") {
                MetricRead(label = "Intensitaet", value = "${area.intensity}/5")
                Slider(
                    value = area.intensity.toFloat(),
                    onValueChange = { onIntensityChange(area.areaId, it) },
                    valueRange = 1f..5f,
                    steps = 3,
                )
                MetricRead(label = "Signalmix", value = "${area.signalBlend}%")
                Slider(
                    value = area.signalBlend.toFloat(),
                    onValueChange = { onSignalBlendChange(area.areaId, it) },
                    valueRange = 0f..100f,
                )
            }
            HorizontalDivider()
            ControlSection(title = "Optionen") {
                SwitchRow(
                    label = "Erinnern",
                    checked = area.remindersEnabled,
                    onCheckedChange = { onRemindersChange(area.areaId, it) },
                )
                SwitchRow(
                    label = "Review",
                    checked = area.reviewEnabled,
                    onCheckedChange = { onReviewChange(area.areaId, it) },
                )
                SwitchRow(
                    label = "Experimente",
                    checked = area.experimentsEnabled,
                    onCheckedChange = { onExperimentsChange(area.areaId, it) },
                )
            }
        }
    }
}

@Composable
private fun ControlSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

@Composable
private fun MetricRead(
    label: String,
    value: String,
) {
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
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
