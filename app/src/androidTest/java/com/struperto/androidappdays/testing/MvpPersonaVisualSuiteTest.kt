package com.struperto.androidappdays.testing

import android.app.Instrumentation
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.MainActivity
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MvpPersonaVisualSuiteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureAllPersonasAcrossDayWindows() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as DaysApp
        val baseDate = LocalDate.of(2026, 3, 8)
        val screenshotDir = "/data/local/tmp/persona-visual-suite"

        runShellCommand(
            instrumentation = instrumentation,
            command = "sh -c 'rm -rf $screenshotDir && mkdir -p $screenshotDir'",
        )

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("home-dashboard").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("mode-tab-single").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("mode-tab-single").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("mode-tab-single").performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("home-dashboard").assertIsDisplayed()

        MvpTestPersonas.all.forEach { persona ->
            val result = app.appContainer.mvpPersonaScenarioRunner.playPersona(
                persona = persona,
                baseDate = baseDate,
            )
            assertTrue(
                "${persona.id} should produce at least one non-unknown evaluation",
                result.evaluations.any { evaluation ->
                    evaluation.state != com.struperto.androidappdays.domain.EvaluationState.UNKNOWN
                },
            )

            composeRule.waitForIdle()
            captureScreen(screenshotDir, persona.id, "vormittag", instrumentation)

            composeRule.onNodeWithTag("home-window-mittag").performClick()
            composeRule.waitForIdle()
            captureScreen(screenshotDir, persona.id, "mittag", instrumentation)

            composeRule.onNodeWithTag("home-window-abend").performClick()
            composeRule.waitForIdle()
            captureScreen(screenshotDir, persona.id, "abend", instrumentation)

            composeRule.onNodeWithTag("home-window-vormittag").performClick()
            composeRule.waitForIdle()

            Log.i(
                "PersonaVisualSuite",
                buildString {
                    append(persona.id)
                    append(" -> ")
                    append(
                        result.evaluations.joinToString(", ") { evaluation ->
                            "${evaluation.domain.name}:${evaluation.state.name}"
                        },
                    )
                    append(" | hypotheses=")
                    append(result.hypotheses.size)
                },
            )
        }
    }
}

private fun captureScreen(
    screenshotDir: String,
    personaId: String,
    windowId: String,
    instrumentation: Instrumentation,
) {
    runShellCommand(
        instrumentation = instrumentation,
        command = "sh -c 'screencap -p $screenshotDir/${personaId}_${windowId}.png'",
    )
}

private fun runShellCommand(
    instrumentation: Instrumentation,
    command: String,
) {
    val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
    ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { stream ->
        while (stream.read() != -1) {
            // Drain shell output to ensure the command completes before continuing.
        }
    }
}
