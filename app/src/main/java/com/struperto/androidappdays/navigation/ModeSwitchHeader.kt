package com.struperto.androidappdays.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.ui.theme.AppTheme

@Composable
fun ModeSwitchHeader(
    activeDestination: AppDestination,
    onOpenStart: () -> Unit,
    onOpenSingle: () -> Unit,
    onOpenMulti: () -> Unit,
    showMulti: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModeSwitchLabel(
            label = AppDestination.Start.title,
            isActive = activeDestination == AppDestination.Start,
            onClick = onOpenStart,
            testTag = "mode-tab-start",
        )
        ModeSwitchLabel(
            label = AppDestination.Home.title,
            isActive = activeDestination == AppDestination.Home,
            onClick = onOpenSingle,
            testTag = "mode-tab-single",
        )
        if (showMulti) {
            ModeSwitchLabel(
                label = AppDestination.Multi.title,
                isActive = activeDestination == AppDestination.Multi,
                onClick = onOpenMulti,
                testTag = "mode-tab-multi",
            )
        }
    }
}

@Composable
private fun ModeSwitchLabel(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    Text(
        text = label,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag(testTag),
        style = MaterialTheme.typography.headlineMedium,
        color = if (isActive) {
            MaterialTheme.colorScheme.onBackground
        } else {
            AppTheme.colors.outlineSoft
        },
    )
}
