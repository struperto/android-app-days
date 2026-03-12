package com.struperto.androidappdays.domain.area

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaKernelModelTest {
    @Test
    fun areaDefinition_keepsStableCapabilityAxes() {
        val definition = AreaDefinition(
            id = "clarity",
            title = "Fokus",
            shortTitle = "Fokus",
            iconKey = "focus",
            category = AreaCategory.DIRECTION,
            overviewMode = AreaOverviewMode.PLAN,
            complexityLevel = AreaComplexityLevel.BASIC,
            seededByDefault = true,
            userCreatable = false,
            lageType = AreaLageType.SCORE,
            focusType = AreaFocusType.HYBRID,
            sourceTypesAllowed = setOf(AreaSourceType.MANUAL, AreaSourceType.TRACK),
            flowCapabilities = setOf(AreaFlowCapability.REVIEW, AreaFlowCapability.REMINDER),
            defaultConfig = AreaDefaultConfig(
                targetScore = 4,
                defaultSelectedTracks = setOf("Fokus", "Ruhe"),
            ),
            supportsPassiveSignals = true,
        )

        assertEquals(AreaOverviewMode.PLAN, definition.overviewMode)
        assertTrue(definition.sourceTypesAllowed.contains(AreaSourceType.TRACK))
        assertTrue(definition.flowCapabilities.contains(AreaFlowCapability.REVIEW))
        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.STATUS_SCHEMA))
        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.VISIBILITY))
        assertEquals(4, definition.defaultConfig.targetScore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun areaBlueprint_requiresAllFourPanelSeeds() {
        AreaBlueprint(
            areaId = "friends",
            summary = "Freunde lebendig halten.",
            trackLabels = listOf("Kontakt", "Tiefe"),
            defaultTemplateId = "person",
            defaultIconKey = "chat",
            panelContentSeeds = mapOf(
                AreaPanelKind.LAGE to AreaPanelContentSeed(
                    title = "Lage",
                    summary = "Wie lebendig es sich anfuehlt.",
                ),
            ),
        )
    }

    @Test
    fun areaInstance_holdsUserSpecificConfigurationWithoutUiState() {
        val instance = AreaInstance(
            areaId = "vitality",
            title = "Vitalitaet",
            summary = "Schlaf, Energie und Bewegung im Takt halten.",
            iconKey = "heart",
            targetScore = 4,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "adaptive",
            selectedTracks = setOf("Schlaf", "Energie"),
            signalBlend = 60,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
            profileConfig = defaultAreaProfileConfig(
                definition = startAreaKernelDefinition("vitality"),
            ),
            templateId = "ritual",
        )

        assertEquals("vitality", instance.areaId)
        assertEquals("vitality", instance.definitionId)
        assertEquals(2, instance.selectedTracks.size)
        assertTrue(instance.reviewEnabled)
    }

    @Test
    fun areaSnapshot_supportsNumericAndStateBasedCurrentReadings() {
        val scoreSnapshot = AreaSnapshot(
            areaId = "clarity",
            date = LocalDate.of(2026, 3, 11),
            manualScore = 4,
        )
        val stateSnapshot = AreaSnapshot(
            areaId = "friends",
            date = LocalDate.of(2026, 3, 11),
            manualStateKey = "warm",
            confidence = 0.8f,
        )

        assertEquals(4, scoreSnapshot.manualScore)
        assertEquals("warm", stateSnapshot.manualStateKey)
        assertEquals(0.8f, stateSnapshot.confidence)
    }

    @Test(expected = IllegalArgumentException::class)
    fun areaSnapshot_requiresAtLeastOneManualStateField() {
        AreaSnapshot(
            areaId = "friends",
            date = LocalDate.of(2026, 3, 11),
        )
    }
}
