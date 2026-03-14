package com.struperto.androidappdays.feature.single.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.ui.theme.AppTheme

enum class TileRole {
    Hero, Summary, Action, Row
}

@Composable
fun DaysTile(
    modifier: Modifier = Modifier,
    role: TileRole = TileRole.Summary,
    eyebrow: String? = null,
    headline: String? = null,
    support: String? = null,
    containerColor: Color = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
    onClick: (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    val shape = role.toShape()
    val padding = role.toPadding()
    val spacing = role.toSpacing()

    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            if (!eyebrow.isNullOrBlank()) {
                Text(
                    text = eyebrow.uppercase(),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.muted,
                )
            }
            if (!headline.isNullOrBlank()) {
                Text(
                    text = headline,
                    style = if (role == TileRole.Hero) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            
            content?.invoke(this)

            if (!support.isNullOrBlank()) {
                Text(
                    text = support,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TileRole.toShape(): Shape {
    val radius = when (this) {
        TileRole.Row -> 16.dp
        TileRole.Summary -> 22.dp
        TileRole.Action -> 22.dp
        TileRole.Hero -> 32.dp
    }
    return RoundedCornerShape(radius)
}

@Composable
private fun TileRole.toPadding(): Dp {
    return when (this) {
        TileRole.Row -> 16.dp
        TileRole.Summary -> 20.dp
        TileRole.Action -> 20.dp
        TileRole.Hero -> 24.dp
    }
}

@Composable
private fun TileRole.toSpacing(): Dp {
    return when (this) {
        TileRole.Row -> 10.dp
        TileRole.Summary -> 16.dp
        TileRole.Action -> 12.dp
        TileRole.Hero -> 20.dp
    }
}
