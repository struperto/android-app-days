package com.struperto.androidappdays.feature.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysTile
import com.struperto.androidappdays.feature.single.shared.TileRole
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.ui.theme.AppTheme

@Composable
fun MultiScreen(
    state: MultiUiState,
    onOpenStart: () -> Unit,
    onOpenSingle: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.colors.surface,
                        AppTheme.colors.surfaceStrong,
                    ),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AppTheme.dimensions.screenPadding, vertical = 18.dp)
            .testTag("multi-screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DaysModeTopBar(
            activeDestination = AppDestination.Multi,
            onOpenStart = onOpenStart,
            onOpenSingle = onOpenSingle,
            onOpenMulti = {},
            onOpenSettings = onOpenSettings,
            settingsTestTag = "multi-open-settings",
            showMulti = true,
        )
        DaysTile(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = 980.dp)
                .testTag("multi-hero"),
            role = TileRole.Hero,
            eyebrow = "Multi",
            headline = state.title,
            support = "Multi bleibt absichtlich klein, bis wieder echte Mehrpersonenarbeit verdrahtet wird.",
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DaysMetaPill(label = state.statusLabel)
                Text(
                    text = state.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.detail,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.ink,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
