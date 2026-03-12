package com.struperto.androidappdays.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun start_opensSettingsHub() {
        openStart()

        composeRule.onNodeWithTag("start-open-settings").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-root-content").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-domains").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-sources").assertIsDisplayed()
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
    fun settingsHub_opensSources() {
        openStart()
        composeRule.onNodeWithTag("start-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-menu-sources").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-source-health_connect").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-source-health_connect").assertIsDisplayed()
    }

    @Test
    fun start_areaOpensAreaStudio() {
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
    }

    @Test
    fun start_areaWorkbenchOpensFocusedScreen() {
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-entry-snapshot").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-entry-snapshot").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-panel-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-panel-screen").assertIsDisplayed()
    }

    @Test
    fun start_focusAreaShowsPilotPathSemantics() {
        openStart()
        composeRule.onNodeWithTag("start-area-clarity").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-entry-path").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-entry-path").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-panel-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-panel-screen").assertIsDisplayed()
    }

    @Test
    fun start_areaIdentityOpensOwnScreen() {
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
    fun start_areaAuthoringOpensOwnScreen() {
        openStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-edit-authoring").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-edit-authoring").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-authoring-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-authoring-screen").assertIsDisplayed()
    }

    @Test
    fun start_showsGridSurfaceWithoutLegacyActions() {
        openStart()
        composeRule.onNodeWithTag("start-grid").assertIsDisplayed()
        composeRule.onNodeWithTag("start-create-area-button").assertIsDisplayed()
        composeRule.onNodeWithTag("start-area-vitality").assertIsDisplayed()
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
        composeRule.onNodeWithTag("start-create-area-button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-create-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("start-create-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("start-create-title").performTextInput("Podcast Ideen")
        composeRule.onNodeWithTag("start-create-save").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
    }

    @Test
    fun start_createOptionsOpenDedicatedScreen() {
        openStart()
        composeRule.onNodeWithTag("start-create-area-button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-create-options-link").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("start-create-options-link").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-create-options-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("start-create-options-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("start-create-options-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-create-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("start-create-screen").assertIsDisplayed()
    }

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("home-open-settings").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithTag("home-dashboard").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openStart() {
        repeat(3) {
            if (composeRule.onAllNodesWithTag("mode-tab-start").fetchSemanticsNodes().isNotEmpty()) {
                return@repeat
            }
            pressBack()
            composeRule.waitForIdle()
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
