package com.struperto.androidappdays.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class HomeFocusItem(
    val title: String,
    val detail: String,
    val icon: ImageVector,
)

private val foundationItems = listOf(
    HomeFocusItem(
        title = "Life Wheel",
        detail = "Daily balance view and area management remain part of the core product.",
        icon = Icons.Outlined.FavoriteBorder,
    ),
    HomeFocusItem(
        title = "Working Set",
        detail = "A focused edit flow will define the active intentions for the day.",
        icon = Icons.Outlined.EditNote,
    ),
    HomeFocusItem(
        title = "Mirror Timeline",
        detail = "Single mode will compare SOLL and IST without pulling in the old multi-mode platform surface.",
        icon = Icons.Outlined.Timeline,
    ),
    HomeFocusItem(
        title = "Daily Actions",
        detail = "Plan, capture, create, and schedule stay as dedicated routes instead of being mixed into home.",
        icon = Icons.Outlined.TaskAlt,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Android App Days") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HeroCard()
            }
            items(foundationItems) { item ->
                FocusCard(item = item)
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Single-first foundation",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "This repository starts with a calm, focused Android base. The old prototype stays as reference; the new app starts small and intentional.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Immediate next step: build the real Single mode flow on top of this clean base.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FocusCard(item: HomeFocusItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = item.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
