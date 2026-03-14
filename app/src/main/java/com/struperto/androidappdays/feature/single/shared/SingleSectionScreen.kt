package com.struperto.androidappdays.feature.single.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class SingleSectionItem(
    val title: String,
    val detail: String,
)

@Composable
fun SingleSectionScreen(
    title: String,
    eyebrow: String,
    summary: String,
    items: List<SingleSectionItem>,
    footer: String,
    onBack: () -> Unit,
) {
    SingleFlowScaffold(
        title = title,
        onBack = onBack,
        eyebrow = eyebrow,
        summary = summary,
    ) {
        items.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = item.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        DaysEmptyStateCard(
            title = "Aktueller Stand",
            detail = footer,
        )
    }
}
