package com.struperto.androidappdays.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        waitForStart()

        composeRule.onNodeWithTag("start-open-settings").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-root-content").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-domains").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-sources").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-menu-research").assertIsDisplayed()
    }

    @Test
    fun start_homeCardOpensHome() {
        waitForStart()
        composeRule.onNodeWithTag("mode-tab-single").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("home-dashboard").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("home-dashboard").assertIsDisplayed()
    }

    @Test
    fun start_modeTabOpensMulti() {
        waitForStart()
        composeRule.onNodeWithTag("mode-tab-multi").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("multi-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("multi-screen").assertIsDisplayed()
    }

    @Test
    fun settingsHub_opensSources() {
        waitForStart()
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
        waitForStart()
        composeRule.onNodeWithTag("start-area-vitality").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("area-studio-screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("area-studio-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("area-studio-work-tile").assertIsDisplayed()
    }

    @Test
    fun start_showsOnlyPulseSurface() {
        waitForStart()
        composeRule.onNodeWithTag("start-pulse-card").assertIsDisplayed()
        composeRule.onNodeWithTag("start-toolbox-toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("start-area-vitality").assertIsDisplayed()
        assert(composeRule.onAllNodesWithTag("start-area-discovery").fetchSemanticsNodes().isNotEmpty())
        listOf(
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
    fun start_toolboxExpandsDockActions() {
        waitForStart()
        composeRule.onNodeWithTag("start-toolbox-toggle").performClick()
        composeRule.onNodeWithTag("start-toolbox-fingerprint").assertIsDisplayed()
        composeRule.onNodeWithTag("start-toolbox-domains").assertIsDisplayed()
        composeRule.onNodeWithTag("start-toolbox-sources").assertIsDisplayed()
        composeRule.onNodeWithTag("start-toolbox-research").assertIsDisplayed()
    }

    private fun waitForStart() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-open-settings").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
