package com.struperto.androidappdays.feature.single.home

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
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.struperto.androidappdays.domain.HourSlotStatus
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysTile
import com.struperto.androidappdays.feature.single.shared.TileRole
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.ui.theme.AppTheme

@Composable
fun SingleHomeScreen(
    state: SingleHomeState,
    onOpenStart: () -> Unit,
    onOpenMulti: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefreshPassiveSignals: () -> Unit,
    onSetHourSlotStatus: (segmentId: String, logicalHour: Int, windowId: String, status: HourSlotStatus) -> Unit,
    onSaveHourSlotNote: (segmentId: String, logicalHour: Int, windowId: String, note: String) -> Unit,
) {
    LifecycleResumeEffect(Unit) {
        onRefreshPassiveSignals()
        onPauseOrDispose { }
    }

    val primaryHint = state.topPriorities.firstOrNull()
        ?: state.areaDock?.nextMeaningfulStep?.label
        ?: "Operative Ausfuehrung bleibt bewusst klein."
    val summary = state.areaDock?.recommendation
        ?.takeIf { it.isNotBlank() && !it.equals(primaryHint, ignoreCase = true) }
        ?: state.thesis.ifBlank {
        "Single bleibt sichtbar, aber reduziert. Der Modus zeigt nur noch den aktuellen Ausfuehrungskontext."
    }
    val secondaryHint = state.areaDock?.headline
        ?.takeIf {
            it.isNotBlank() &&
                !it.equals(primaryHint, ignoreCase = true) &&
                !it.equals(summary, ignoreCase = true)
        }
        ?: "Mehr operative Tiefe wird erst wieder freigelegt, wenn der Produktfokus das braucht."

    Column(
        modifier = Modifier
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
            .padding(horizontal = AppTheme.dimensions.screenPadding, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DaysModeTopBar(
            activeDestination = AppDestination.Home,
            onOpenStart = onOpenStart,
            onOpenSingle = {},
            onOpenMulti = onOpenMulti,
            onOpenSettings = onOpenSettings,
            settingsTestTag = "home-open-settings",
            showMulti = true,
        )
        DaysTile(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = 980.dp)
                .testTag("home-dashboard"),
            role = TileRole.Hero,
            eyebrow = "Single",
            headline = state.title,
            support = "Single bleibt als Ausfuehrungsmodus sichtbar, aber ohne alte Stunden-, Lab- und Variantenoberflaechen.",
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DaysMetaPill(label = state.fitLabel.ifBlank { "Reduziert" })
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = primaryHint,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = secondaryHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.muted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
