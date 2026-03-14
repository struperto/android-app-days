package com.struperto.androidappdays.domain.area

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaAuthoringConfigTest {
    @Test
    fun defaultAuthoringConfig_reusesProfileAxesAndAddsComplexityVisibility() {
        val vitality = defaultAreaAuthoringConfig(
            definition = startAreaKernelDefinition("vitality"),
            templateId = "ritual",
        )
        val discovery = defaultAreaAuthoringConfig(
            definition = startAreaKernelDefinition("discovery"),
            templateId = "place",
        )

        assertEquals(AreaLageMode.Score, vitality.lageMode)
        assertEquals(AreaSourcesMode.Signals, vitality.sourcesMode)
        assertEquals(AreaComplexityLevel.BASIC, vitality.complexityLevel)
        assertEquals(AreaVisibilityLevel.Focused, vitality.visibilityLevel)

        assertEquals(AreaLageMode.State, discovery.lageMode)
        assertEquals(AreaFlowProfile.Supportive, discovery.flowProfile)
        assertEquals(AreaComplexityLevel.EXPERT, discovery.complexityLevel)
        assertEquals(AreaVisibilityLevel.Expanded, discovery.visibilityLevel)
    }

    @Test
    fun templateAuthoringDefaults_andDefinitionIds_stayStableForFreeAreas() {
        val project = defaultAreaAuthoringConfig(
            definition = null,
            templateId = "project",
        )
        val free = defaultAreaAuthoringConfig(
            definition = null,
            templateId = "free",
        )

        assertEquals(AreaDirectionMode.Focus, project.directionMode)
        assertEquals(AreaComplexityLevel.BASIC, project.complexityLevel)
        assertEquals(AreaVisibilityLevel.Focused, project.visibilityLevel)

        assertEquals(AreaLageMode.State, free.lageMode)
        assertEquals(AreaComplexityLevel.ADVANCED, free.complexityLevel)
        assertEquals(AreaVisibilityLevel.Standard, free.visibilityLevel)
        assertEquals("vitality", defaultAreaDefinitionId(areaId = "vitality", templateId = "ritual"))
        assertEquals("template:free", defaultAreaDefinitionId(areaId = "custom", templateId = "free"))
    }

    @Test
    fun seededDefinitions_exposeExplicitAuthoringAxes() {
        val definition = requireNotNull(startAreaKernelDefinition("meaning"))

        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.STATUS_SCHEMA))
        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.DIRECTION))
        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.SOURCES))
        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.FLOW))
        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.COMPLEXITY))
        assertTrue(definition.authoringAxes.contains(AreaAuthoringAxis.VISIBILITY))
    }

    @Test
    fun areaInstance_derivesLegacyProfileCompatibilityFromAuthoringConfig() {
        val instance = AreaInstance(
            areaId = "custom",
            title = "Eigener Bereich",
            summary = "Eigene Semantik.",
            iconKey = "spark",
            targetScore = 3,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "adaptive",
            selectedTracks = setOf("Kontakt"),
            signalBlend = 55,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
            templateId = "free",
            definitionId = "template:free",
            authoringConfig = AreaAuthoringConfig(
                lageMode = AreaLageMode.State,
                directionMode = AreaDirectionMode.Focus,
                sourcesMode = AreaSourcesMode.Curated,
                flowProfile = AreaFlowProfile.Active,
                complexityLevel = AreaComplexityLevel.EXPERT,
                visibilityLevel = AreaVisibilityLevel.Expanded,
            ),
        )

        assertEquals("template:free", instance.definitionId)
        assertEquals(AreaLageMode.State, instance.profileConfig.lageMode)
        assertEquals(AreaDirectionMode.Focus, instance.profileConfig.directionMode)
        assertEquals(AreaSourcesMode.Curated, instance.profileConfig.sourcesMode)
        assertEquals(AreaFlowProfile.Active, instance.profileConfig.flowProfile)
        assertEquals(AreaComplexityLevel.EXPERT, instance.authoringConfig.complexityLevel)
        assertEquals(AreaVisibilityLevel.Expanded, instance.authoringConfig.visibilityLevel)
    }

    @Test
    fun templateBackedInstance_rebasesDefinitionAndUntouchedDefaultsWhenTemplateChanges() {
        val instance = AreaInstance(
            areaId = "custom",
            title = "Eigener Bereich",
            summary = "Eigene Semantik.",
            iconKey = "spark",
            targetScore = 3,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "adaptive",
            selectedTracks = setOf("Kontakt"),
            signalBlend = 55,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
            templateId = "free",
            definitionId = "template:free",
            authoringConfig = defaultAreaAuthoringConfig(
                definition = null,
                templateId = "free",
            ),
        )

        val rebased = instance.withUpdatedIdentity(
            title = "Projektbereich",
            summary = "Mehr Zug fuer Arbeit.",
            templateId = "project",
            iconKey = "briefcase",
        )

        assertEquals("template:project", rebased.definitionId)
        assertEquals("Projektbereich", rebased.title)
        assertEquals("Mehr Zug fuer Arbeit.", rebased.summary)
        assertEquals("briefcase", rebased.iconKey)
        assertEquals(AreaDirectionMode.Focus, rebased.authoringConfig.directionMode)
        assertEquals(AreaFlowProfile.Active, rebased.authoringConfig.flowProfile)
        assertEquals(AreaComplexityLevel.BASIC, rebased.authoringConfig.complexityLevel)
        assertEquals(AreaVisibilityLevel.Focused, rebased.authoringConfig.visibilityLevel)
    }
}
