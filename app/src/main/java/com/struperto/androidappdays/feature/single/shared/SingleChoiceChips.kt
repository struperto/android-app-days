package com.struperto.androidappdays.feature.single.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.data.repository.TimeBlock

data class ChoiceChipItem(
    val id: String,
    val label: String,
    val testTag: String? = null,
)

@Composable
fun ChoiceChipRow(
    items: List<ChoiceChipItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            FilterChip(
                selected = item.id == selectedId,
                onClick = { onSelect(item.id) },
                modifier = item.testTag?.let { Modifier.testTag(it) } ?: Modifier,
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
fun MultiChoiceChipRow(
    items: List<ChoiceChipItem>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            FilterChip(
                selected = item.id in selectedIds,
                onClick = { onToggle(item.id) },
                modifier = item.testTag?.let { Modifier.testTag(it) } ?: Modifier,
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
fun ScoreChipRow(
    selectedScore: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChoiceChipRow(
        items = (1..5).map { score ->
            ChoiceChipItem(
                id = score.toString(),
                label = score.toString(),
            )
        },
        selectedId = selectedScore?.toString(),
        onSelect = { onSelect(it.toInt()) },
        modifier = modifier,
    )
}

@Composable
fun PulseChipRow(
    selectedScore: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            ChoiceChipItem(id = "2", label = "Zieht"),
            ChoiceChipItem(id = "3", label = "Läuft"),
            ChoiceChipItem(id = "5", label = "Stark"),
        ).forEach { item ->
            FilterChip(
                selected = item.id == selectedScore?.toString(),
                onClick = {
                    val newValue = item.id.toInt()
                    onSelect(if (selectedScore == newValue) null else newValue)
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
fun TimeBlockChipRow(
    selectedTimeBlock: TimeBlock,
    onSelect: (TimeBlock) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChoiceChipRow(
        items = TimeBlock.all.map { block ->
            ChoiceChipItem(
                id = block.persistedValue,
                label = block.label,
            )
        },
        selectedId = selectedTimeBlock.persistedValue,
        onSelect = { value ->
            onSelect(TimeBlock.fromPersistedValue(value))
        },
        modifier = modifier,
    )
}
