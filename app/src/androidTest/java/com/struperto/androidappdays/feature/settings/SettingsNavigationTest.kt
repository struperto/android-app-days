package com.struperto.androidappdays.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.MainActivity
import com.struperto.androidappdays.feature.start.AreaImportKind
import com.struperto.androidappdays.feature.start.buildAreaImportCaptureText
import com.struperto.androidappdays.feature.start.defaultWebFeedSyncCadence
import com.struperto.androidappdays.feature.start.inferWebFeedSourceKind
import com.struperto.androidappdays.feature.start.startSeedAreas
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetStartState() = runBlocking {
        app().appContainer.areaSourceBindingRepository.clearAll()
        app().appContainer.areaWebFeedSourceRepository.loadAll().forEach { source ->
            app().appContainer.areaWebFeedSourceRepository.remove(source.areaId, source.url)
        }
        app().appContainer.lifeWheelRepository.completeSetup(emptyList())
        app().appContainer.captureRepository.observeOpen().first().forEach { capture ->
            app().appContainer.captureRepository.archive(capture.id)
        }
    }

    @Test
    fun start_opensSettingsHub() {
        openStart()

        composeRule.onNodeWithTag("start-open-settings").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-root-content").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-analysis").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-sources").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-domains").assertIsDisplayed()
        assert(composeRule.onAllNodesWithTag("settings-menu-feeds").fetchSemanticsNodes().isEmpty())
        assert(composeRule.onAllNodesWithTag("settings-menu-inbox").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun settings_analysisMenuOpensAnalysisScreen() {
        openStart()
        composeRule.onNodeWithTag("start-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-menu-analysis").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("settings-menu-analysis").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-analysis").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-analysis").assertIsDisplayed()
    }

    @Test
    fun start_homeCardOpensHome() {
        openStart()
        composeRule.onNodeWithTag("mode-tab-single").performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("home-dashboard").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("home-dashboard").assertIsDisplayed()
    }

    @Test
    fun primaryModeSwitch_showsMultiTab() {
        waitForHome()
        composeRule.onNodeWithTag("mode-tab-multi").assertIsDisplayed()
        openStart()
        composeRule.onNodeWithTag("mode-tab-multi").assertIsDisplayed()
    }

    @Test
    fun start_areaOpensAreaStudio() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
    }

    @Test
    fun start_areaWorkbenchOpensFocusedScreen() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-entry-stand").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-entry-stand").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-panel-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-panel-screen").assertIsDisplayed()
    }

    @Test
    fun start_areaPanelRowOpensOwnEditorScreen() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-entry-stand").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-entry-stand").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("panel-action-snapshotmode").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("panel-action-snapshotmode").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("panel-action-editor-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("panel-action-editor-screen").assertIsDisplayed()
    }

    @Test
    fun start_focusAreaShowsPilotPathSemantics() {
        seedAreas("clarity")
        openStart()
        composeRule.onNodeWithTag("start-area-clarity").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-entry-goal").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-authoring-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-authoring-screen").assertIsDisplayed()
    }

    @Test
    fun start_homeAreaOpensFlowPanel() {
        seedAreas("home")
        openStart()
        composeRule.onNodeWithTag("start-area-home").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-workspace-card").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-workspace-card").assertIsDisplayed()
        composeRule.onAllNodesWithText("Zuhause").onFirst().assertIsDisplayed()
    }

    @Test
    fun start_areaIdentityOpensOwnScreen() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-edit-identity").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-edit-identity").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-identity-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-identity-screen").assertIsDisplayed()
    }

    @Test
    fun start_areaInputsOpenOwnScreen() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-entry-inputs").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-inputs-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-inputs-screen").assertIsDisplayed()
    }

    @Test
    fun start_areaAnalysisIconOpensAnalysisScreen() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("Bereichsanalyse").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-analysis-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-analysis-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("area-analysis-machine-payload").performScrollTo()
        composeRule.onNodeWithTag("area-analysis-machine-payload").assertIsDisplayed()
    }

    @Test
    fun start_areaInputsMaterialOpensOwnScreen() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-entry-inputs").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-input-overview-once").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-input-overview-once").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-input-material-collect-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-input-material-collect-screen").assertIsDisplayed()
    }

    @Test
    fun start_showsEmptySurfaceWithoutLegacyActions() {
        openStart()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-empty-state").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("start-area-news").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("start-create-area-button").fetchSemanticsNodes().isNotEmpty()
        }
        listOf(
            "start-pulse-card",
            "start-create-status",
            "start-area-manage-dock",
            "start-manage-edit",
            "start-manage-delete",
            "start-manage-later",
            "start-action-home",
            "start-action-capture",
            "start-action-plan",
            "start-action-workbench",
            "start-action-fingerprint",
            "start-action-domains",
            "start-action-sources",
            "start-action-research",
            "start-action-multi",
        ).forEach { tag ->
            assert(composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty())
        }
    }

    @Test
    fun start_createAreaCreatesAreaStudioFlow() {
        openStart()
        openCreateFlow()
        composeRule.onNodeWithTag("start-create-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("start-create-input").performTextInput("Ich moechte neue Podcast-Folgen ruhig verfolgen.")
        composeRule.onNodeWithTag("start-create-next").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-analysis-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-analysis-result").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("start-create-save").performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
    }

    @Test
    fun start_createBookmarkIntentShowsBlockedAnalysis() {
        openStart()
        openCreateFlow()
        composeRule.onNodeWithTag("start-create-input").performTextInput("Ich moechte, dass du meine Lesezeichen liest.")
        composeRule.onNodeWithTag("start-create-next").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-blocker-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(
            "Browser-Lesezeichen kann Days nicht direkt",
            substring = true,
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("start-blocker-solve").assertIsDisplayed()
        assert(composeRule.onAllNodesWithTag("start-create-save").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun start_pendingImportCanBecomeNewArea() {
        seedPendingImportLink()
        openStart()

        composeRule.onNodeWithTag("start-pending-imports-card").assertIsDisplayed()
        composeRule.onNodeWithTag("start-pending-import-create-0").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-analysis-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-analysis-result").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("start-create-save").performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
    }

    @Test
    fun settings_inboxShowsAndDismissesPendingImport() {
        seedPendingImportLink()
        openStart()
        composeRule.onNodeWithTag("start-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-menu-domains").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-menu-domains").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-domains-open-inbox").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-domains-open-inbox").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-inbox-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-inbox-content").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-inbox-open-start").assertIsDisplayed()
        composeRule.onNodeWithText("Android Developers").assertIsDisplayed()
        composeRule.onAllNodesWithText("Verwerfen").onFirst().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Android Developers").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun settings_domainsDeleteAreaRemovesCard() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-menu-domains").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-area-card-vitality").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-area-card-vitality").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-area-delete-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-area-card-vitality").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun settings_feedsOpenAreaNavigatesIntoStudio() {
        seedAreas("vitality")
        seedFeedSource("vitality", "https://developer.android.com/feeds/androidx-release-notes.xml")
        openStart()
        composeRule.onNodeWithTag("start-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-menu-domains").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-menu-domains").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-domains-open-feeds").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-domains-open-feeds").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-feeds-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-feed-open-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
    }

    @Test
    fun settings_domainsOpenAreaNavigatesIntoStudio() {
        seedAreas("vitality")
        openStart()
        composeRule.onNodeWithTag("start-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-menu-domains").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-area-open-vitality").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-area-open-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
    }

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("mode-tab-single").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("home-open-settings").fetchSemanticsNodes().isEmpty()) {
            composeRule.onNodeWithTag("mode-tab-single").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("home-open-settings").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithTag("home-dashboard").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun seedAreas(vararg areaIds: String) = runBlocking {
        val seededAreas = startSeedAreas()
            .filter { it.id in areaIds.toSet() }
            .mapIndexed { index, area -> area.copy(sortOrder = index) }
        app().appContainer.lifeWheelRepository.completeSetup(seededAreas)
    }

    private fun seedPendingImportLink() = runBlocking {
        app().appContainer.captureRepository.createTextCapture(
            text = buildAreaImportCaptureText(
                kind = AreaImportKind.Link,
                title = "Android Developers",
                detail = "Link fuer diesen Bereich gespeichert.",
                reference = "https://developer.android.com/compose",
            ),
            areaId = null,
        )
    }

    private fun seedFeedSource(areaId: String, url: String) = runBlocking {
        val kind = inferWebFeedSourceKind(url)
        app().appContainer.areaWebFeedSourceRepository.save(
            areaId = areaId,
            url = url,
            sourceKind = kind,
            isAutoSyncEnabled = true,
            syncCadence = defaultWebFeedSyncCadence(kind),
        )
    }

    private fun openCreateFlow() {
        if (composeRule.onAllNodesWithTag("start-create-area-button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("start-create-area-button").performClick()
        } else {
            composeRule.onNodeWithText("Ersten Bereich entwerfen").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-create-screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openStart() {
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("start-open-settings").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("mode-tab-start").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        repeat(3) {
            if (composeRule.onAllNodesWithTag("start-open-settings").fetchSemanticsNodes().isNotEmpty()) {
                return@repeat
            }
            if (composeRule.onAllNodesWithTag("mode-tab-start").fetchSemanticsNodes().isNotEmpty()) {
                return@repeat
            }
            if (
                composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
            ) {
                pressBack()
                composeRule.waitForIdle()
            }
        }
        if (composeRule.onAllNodesWithTag("start-open-settings").fetchSemanticsNodes().isNotEmpty()) {
            return
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("mode-tab-start").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("mode-tab-start").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-open-settings").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun app(): DaysApp {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp
    }
}
