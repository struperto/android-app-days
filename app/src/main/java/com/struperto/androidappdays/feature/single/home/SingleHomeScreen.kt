package com.struperto.androidappdays.feature.single.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.struperto.androidappdays.domain.HourSlotStatus
import com.struperto.androidappdays.feature.content.AreaContentItem
import com.struperto.androidappdays.feature.content.AreaContentKind
import com.struperto.androidappdays.feature.content.AreaContentPlatform
import com.struperto.androidappdays.feature.content.AreaContentState
import com.struperto.androidappdays.feature.content.requiresReaderContent
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
    onOpenContentItem: (String) -> Unit,
    onRefreshPassiveSignals: () -> Unit,
    onSetHourSlotStatus: (segmentId: String, logicalHour: Int, windowId: String, status: HourSlotStatus) -> Unit,
    onSaveHourSlotNote: (segmentId: String, logicalHour: Int, windowId: String, note: String) -> Unit,
) {
    LifecycleResumeEffect(Unit) {
        onRefreshPassiveSignals()
        onPauseOrDispose { }
    }

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.dimensions.screenPadding, vertical = 18.dp)
            .testTag("home-dashboard"),
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
            modifier = Modifier.widthIn(max = 980.dp),
            role = TileRole.Hero,
            eyebrow = "Single · Bereichsfeed",
            headline = state.areaFeedTitle.ifBlank { "Heute im Feed" },
            support = if (state.areaFeedStatusDetail.isNotBlank()) {
                state.areaFeedStatusDetail
            } else {
                "Single zeigt die priorisierten Kacheln aus deinen Bereichen als ruhigen Gesamtfeed."
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val primaryStatus = state.areaFeedStatusLabel.ifBlank {
                    if (state.areaFeedItems.isEmpty()) "Noch keine Kacheln" else "${state.areaFeedItems.size} Kacheln"
                }
                DaysMetaPill(label = primaryStatus)
                Text(
                    text = if (state.areaFeedItems.isEmpty()) {
                        "Sobald ein Bereich verwertbare Signale oder Inhalte hat, taucht sein Output hier als eigene Feed-Kachel auf."
                    } else {
                        "Jede Kachel steht fuer einen konkreten Output eines Bereichs und bleibt zuerst lesbar statt interaktiv."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
            }
        }
        if (state.areaFeedItems.isEmpty()) {
            DaysTile(
                modifier = Modifier.widthIn(max = 980.dp),
                role = TileRole.Summary,
                eyebrow = "Feed leer",
                headline = "Noch keine Bereichskachel sichtbar",
                support = "Lege im Start einen Bereich an, fuehre die Analyse zu Ende und verbinde erste Signale. Danach tauchen hier seine Kacheln auf.",
            )
        } else {
            state.areaFeedItems.forEach { item ->
                DaysTile(
                    modifier = Modifier
                        .widthIn(max = 980.dp)
                        .testTag("area-content-card-${item.id.hashCode()}"),
                    role = TileRole.Summary,
                    eyebrow = item.cardEyebrow(),
                    headline = item.title,
                    support = "",
                    containerColor = item.cardColor(),
                    onClick = { onOpenContentItem(item.id) },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            DaysMetaPill(label = item.kindLabel())
                        }
                        item.secondaryMetaText()?.let { meta ->
                            Text(
                                text = meta,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.muted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        item.cardPreviewText()?.let { info ->
                            Text(
                                text = info,
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppTheme.colors.ink,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleContentItemScreen(
    item: AreaContentItem?,
    onBack: () -> Unit,
) {
    if (item == null) {
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
        ) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Zurueck",
                )
            }
            DaysTile(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .testTag("area-content-missing"),
                role = TileRole.Summary,
                eyebrow = "Reader",
                headline = "Dieser Eintrag ist schon verschwunden",
                support = "Er wurde bereits gelesen oder aus dem Bereichsfeed entfernt.",
            )
        }
        return
    }

    val scrollState = rememberScrollState()

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
            .verticalScroll(scrollState)
            .padding(horizontal = AppTheme.dimensions.screenPadding, vertical = 18.dp)
            .testTag("area-content-screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 980.dp),
        ) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Zurueck",
                )
            }
        }
        DaysTile(
            modifier = Modifier.widthIn(max = 980.dp),
            role = TileRole.Hero,
            eyebrow = item.cardEyebrow(),
            headline = item.title,
            support = "",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (item.publishedLabel.isNotBlank()) {
                    DaysMetaPill(label = item.publishedLabel.take(42))
                }
                Text(
                    text = item.detailLeadText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
            }
        }
        DaysTile(
            modifier = Modifier
                .widthIn(max = 980.dp)
                .testTag("area-content-body"),
            role = TileRole.Summary,
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (item.contentState) {
                        AreaContentState.Pending,
                        AreaContentState.Loading -> {
                            Text(
                                text = if (item.requiresReaderContent()) "Volltext wird geladen" else "Inhalt wird vorbereitet",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.ink,
                            )
                            Text(
                                text = item.contentDetail.ifBlank { "Bitte einen Moment warten." },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        AreaContentState.AnalysisNeeded -> {
                            Text(
                                text = if (item.requiresReaderContent()) "Analyse noetig" else "Mehr Kontext noetig",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.ink,
                            )
                            Text(
                                text = item.contentDetail.ifBlank {
                                    "Der Link konnte noch nicht stabil fuer den Reader oder die Vorschau aufbereitet werden."
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = item.contentUrl,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppTheme.colors.muted,
                            )
                        }

                        AreaContentState.Ready -> {
                            Text(
                                text = item.detailHeadline(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.ink,
                            )
                            item.cardPreviewText()?.let { info ->
                                Text(
                                    text = info,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = item.body,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Der Eintrag bleibt vorerst im Feed, auch wenn du bis zum Ende liest.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.muted,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun AreaContentItem.cardEyebrow(): String {
    return when (platform) {
        AreaContentPlatform.Web -> sourceLabel
        else -> {
            val normalizedSourceLabel = sourceLabel.trim()
            if (normalizedSourceLabel.equals(platform.label, ignoreCase = true)) {
                platform.label
            } else {
                "${platform.label} · ${sourceLabel}"
            }
        }
    }
}

private fun AreaContentItem.kindLabel(): String {
    return when (kind) {
        AreaContentKind.Article -> "Artikel"
        AreaContentKind.SocialPost -> "Post"
        AreaContentKind.Video -> "Video"
        AreaContentKind.Link -> "Link"
    }
}

private fun AreaContentItem.secondaryMetaText(): String? {
    val parts = buildList {
        creatorLabel.takeIf(String::isNotBlank)?.let(::add)
        publishedLabel.takeIf(String::isNotBlank)?.let(::add)
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun AreaContentItem.detailLeadText(): String {
    return when {
        requiresReaderContent() && contentState == AreaContentState.Ready -> "Der Volltext ist bereit."
        requiresReaderContent() && contentState in setOf(AreaContentState.Loading, AreaContentState.Pending) -> "Der Artikel wird gerade aufbereitet."
        requiresReaderContent() && contentState == AreaContentState.AnalysisNeeded -> "Der Volltext war noch nicht klar genug lesbar."
        kind == AreaContentKind.Video -> "Dieser Beitrag liegt jetzt als Video im Bereichsfeed."
        kind == AreaContentKind.SocialPost -> "Dieser Beitrag liegt jetzt als Social-Post im Bereichsfeed."
        else -> "Dieser Eintrag liegt jetzt als Artikel im Bereichsfeed."
    }
}

private fun AreaContentItem.detailHeadline(): String {
    return when {
        creatorLabel.isNotBlank() -> "$title · $creatorLabel"
        else -> title
    }
}

private fun AreaContentItem.cardPreviewText(): String? {
    val normalizedSummary = summary.trim()
    val normalizedPublished = publishedLabel.trim()
    return normalizedSummary
        .takeIf { it.isNotBlank() }
        ?.takeIf { it != normalizedPublished }
}

@Composable
private fun AreaContentItem.cardColor(): Color {
    return when (platform) {
        AreaContentPlatform.Web -> AppTheme.colors.surfaceStrong.copy(alpha = 0.94f)
        AreaContentPlatform.YouTube -> Color(0xFFFFF0EE)
        AreaContentPlatform.Instagram -> Color(0xFFFFF3EA)
        AreaContentPlatform.X -> Color(0xFFF2F5F7)
    }
}
