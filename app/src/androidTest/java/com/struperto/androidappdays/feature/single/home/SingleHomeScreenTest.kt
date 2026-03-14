package com.struperto.androidappdays.feature.single.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.struperto.androidappdays.feature.content.AreaContentItem
import com.struperto.androidappdays.feature.content.AreaContentKind
import com.struperto.androidappdays.feature.content.AreaContentPlatform
import com.struperto.androidappdays.feature.content.AreaContentState
import com.struperto.androidappdays.feature.single.model.SingleHomeState
import com.struperto.androidappdays.ui.theme.DaysTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SingleHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun home_showsSingleHeroTile() {
        composeRule.setContent {
            DaysTheme {
                SingleHomeScreen(
                    state = previewState(),
                    onOpenStart = {},
                    onOpenMulti = {},
                    onOpenSettings = {},
                    onOpenContentItem = {},
                    onRefreshPassiveSignals = {},
                    onSetHourSlotStatus = { _, _, _, _ -> },
                    onSaveHourSlotNote = { _, _, _, _ -> },
                )
            }
        }

        composeRule.onNodeWithTag("home-dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("home-open-settings").assertIsDisplayed()
    }

    @Test
    fun home_doesNotRenderLegacyHourlyDashboard() {
        composeRule.setContent {
            DaysTheme {
                SingleHomeScreen(
                    state = previewState(),
                    onOpenStart = {},
                    onOpenMulti = {},
                    onOpenSettings = {},
                    onOpenContentItem = {},
                    onRefreshPassiveSignals = {},
                    onSetHourSlotStatus = { _, _, _, _ -> },
                    onSaveHourSlotNote = { _, _, _, _ -> },
                )
            }
        }

        listOf(
            "home-domain-strip",
            "home-window-vormittag",
            "home-window-mittag",
            "home-window-abend",
            "home-hour-08:00",
            "home-hour-13:00",
            "home-hour-18:00",
        ).forEach { tag ->
            composeRule.onAllNodesWithTag(tag).assertCountEquals(0)
        }
    }
}

private fun previewState(): SingleHomeState {
    return SingleHomeState(
        today = LocalDate.of(2026, 3, 7),
        modeLabel = "Single",
        title = "Single",
        thesis = "Single bleibt als ruhiger Ausfuehrungsmodus sichtbar.",
        fitLabel = "Reduziert",
        fitScore = 0.0f,
        topPriorities = listOf("Ein klarer naechster Zug reicht."),
        risks = emptyList(),
        coachSuggestions = emptyList(),
        dailyDomains = emptyList(),
        segments = emptyList(),
        segmentDetails = emptyMap(),
        segmentHints = emptyMap(),
        feedbackMessage = null,
        lifeAreas = emptyList(),
        dailyChecks = emptyMap(),
        areaDock = null,
        areaFeedAreaId = "arbeit",
        areaFeedTitle = "Arbeit",
        areaFeedStatusLabel = "2 Inhalte",
        areaFeedStatusDetail = "Bereichsfeed ist lokal verfuegbar.",
        areaFeedItems = listOf(
            AreaContentItem(
                id = "content-1",
                areaId = "arbeit",
                title = "Beispielartikel",
                sourceLabel = "Heise",
                sourceUrl = "https://www.heise.de/",
                contentUrl = "https://example.com/article",
                summary = "Kurzfassung",
                body = "Ein lesbarer Beispieltext fuer die Content-Kachel im Single-Screen.",
                publishedLabel = "Heute",
                kind = AreaContentKind.Article,
                platform = AreaContentPlatform.Web,
                contentState = AreaContentState.Ready,
            ),
        ),
    )
}
