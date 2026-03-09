package com.struperto.androidappdays.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.ui.theme.DaysTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsResearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun researchScreen_showsPersonaLabAndCallsActions() {
        var loadedPersonaId: String? = null
        var runAllCount = 0

        composeRule.setContent {
            DaysTheme {
                SettingsResearchScreen(
                    state = previewResearchState(),
                    onBack = {},
                    onLoadPersona = { loadedPersonaId = it },
                    onRunAllPersonas = { runAllCount += 1 },
                )
            }
        }

        composeRule.onNodeWithTag("research-persona-lab").assertIsDisplayed()
        composeRule.onNodeWithTag("research-run-all").performClick()
        composeRule.onNodeWithTag("research-load-persona-early-athlete").performClick()

        assertEquals(1, runAllCount)
        assertEquals("early-athlete", loadedPersonaId)
    }
}

private fun previewResearchState(): SettingsUiState {
    return SettingsUiState(
        personaLab = SettingsPersonaLabState(
            activePersonaId = "busy-parent",
            statusTitle = "Persona Lab",
            statusDetail = "Noch kein Persona-Lauf gestartet.",
            personas = listOf(
                SettingsPersonaItem(
                    id = "early-athlete",
                    name = "Lena",
                    archetype = "Early Athlete",
                    summary = "Ruhiger Morgen, Bewegung frueh.",
                ),
                SettingsPersonaItem(
                    id = "busy-parent",
                    name = "Mara",
                    archetype = "Busy Parent",
                    summary = "Viele neutrale Stunden, wenig moralische Haerte.",
                ),
            ),
        ),
        hypotheses = listOf(
            SettingsHypothesisItem(
                id = "h1",
                domain = LifeDomain.SLEEP,
                label = "Schlaf",
                confidenceLabel = "0.68",
                detail = "Moegliches Muster zwischen spaeter Aktivitaet und muedem Start.",
            ),
        ),
    )
}
