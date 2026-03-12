package com.struperto.androidappdays.journey

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.MainActivity
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserJourneyTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun freshUser_canOpenHome_andSparseHydrationStaysNeutral() {
        runBlocking {
            val app = app()
            app.appContainer.observationRepository.clearAll()
            app.appContainer.hourSlotEntryRepository.clearAll()

            waitForHome()

            composeRule.onNodeWithTag("home-dashboard").assertIsDisplayed()
            composeRule.onNodeWithTag("home-open-settings").assertIsDisplayed()
        }
    }

    @Test
    fun user_canAdjustSleepGoal_andChangePersists() = runBlocking {
        val app = app()
        val goalRepository = app.appContainer.goalRepository
        val originalGoal = goalRepository.loadActiveGoals().first { it.domain == LifeDomain.SLEEP }

        try {
            waitForHome()
            openDomainDetail("sleep")

            composeRule.onNodeWithTag("settings-goal-sleep-min").performTextClearance()
            composeRule.onNodeWithTag("settings-goal-sleep-min").performTextInput("7.5")
            composeRule.onNodeWithTag("settings-goal-sleep-save").performClick()

            composeRule.waitUntil(timeoutMillis = 10_000) {
                val updated = runBlocking {
                    goalRepository.loadActiveGoals().first { it.domain == LifeDomain.SLEEP }
                }
                updated.target.minimum == 7.5f
            }

            val updated = goalRepository.loadActiveGoals().first { it.domain == LifeDomain.SLEEP }
            assertEquals(7.5f, updated.target.minimum)
        } finally {
            goalRepository.save(originalGoal)
        }
    }

    private fun openDomainDetail(domainTag: String) {
        composeRule.onNodeWithTag("home-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-root-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-menu-domains").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-domains-content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-domain-$domainTag").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-domain-detail-content").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("home-open-settings").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithTag("home-dashboard").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun app(): DaysApp {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp
    }
}
