package com.struperto.androidappdays.feature.single.shared

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable

@Composable
fun SingleFlowScaffold(
    title: String,
    onBack: () -> Unit,
    eyebrow: String? = null,
    summary: String? = null,
    action: DaysTopBarAction? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DaysPageScaffold(
        title = title,
        onBack = onBack,
        action = action,
        modifier = modifier,
    ) {
        if (!summary.isNullOrBlank()) {
            DaysPageIntroCard(
                title = title,
                summary = summary,
                eyebrow = eyebrow,
            )
        }
        content()
    }
}
