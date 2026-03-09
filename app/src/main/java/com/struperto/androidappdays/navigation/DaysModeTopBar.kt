package com.struperto.androidappdays.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.ui.theme.AppTheme

@Composable
fun DaysModeTopBar(
    activeDestination: AppDestination,
    onOpenStart: () -> Unit,
    onOpenSingle: () -> Unit,
    onOpenMulti: () -> Unit,
    onOpenSettings: () -> Unit,
    settingsTestTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "DAYS",
                style = AppTheme.typography.label,
                color = AppTheme.colors.muted,
            )
            ModeSwitchHeader(
                activeDestination = activeDestination,
                onOpenStart = onOpenStart,
                onOpenSingle = onOpenSingle,
                onOpenMulti = onOpenMulti,
            )
        }
        FilledTonalIconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(56.dp)
                .testTag(settingsTestTag),
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Einstellungen",
            )
        }
    }
}
