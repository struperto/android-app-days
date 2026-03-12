package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.domain.area.AreaAuthoringConfig
import com.struperto.androidappdays.domain.area.AreaAuthoringAxis
import com.struperto.androidappdays.domain.area.AreaBlueprint
import com.struperto.androidappdays.domain.area.AreaCategory
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaDefaultConfig
import com.struperto.androidappdays.domain.area.AreaDefinition
import com.struperto.androidappdays.domain.area.AreaDirectionMode
import com.struperto.androidappdays.domain.area.AreaFlowCapability
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaFocusType
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaLageType
import com.struperto.androidappdays.domain.area.AreaOverviewMode
import com.struperto.androidappdays.domain.area.AreaPanelContentSeed
import com.struperto.androidappdays.domain.area.AreaPanelKind
import com.struperto.androidappdays.domain.area.AreaSourceType
import com.struperto.androidappdays.domain.area.AreaSourcesMode
import com.struperto.androidappdays.domain.area.AreaVisibilityLevel
import com.struperto.androidappdays.domain.area.defaultAreaAuthoringConfig
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaAuthoringProjectorTest {
    @Test
    fun projector_mapsSeededAreaIntoAuthoringSections() {
        val definition = startAreaKernelDefinition("vitality") ?: error("missing vitality")
        val instance = AreaInstance(
            areaId = "vitality",
            definitionId = "vitality",
            title = "Vitalitaet",
            summary = "Schlaf und Energie tragen.",
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
            authoringConfig = defaultAreaAuthoringConfig(definition = definition),
            templateId = "ritual",
        )

        val state = projectAreaAuthoringStudioState(
            AreaAuthoringProjectionInput(
                definition = definition,
                blueprint = startAreaKernelBlueprint("vitality") ?: error("missing vitality blueprint"),
                instance = instance,
                authoringConfig = instance.authoringConfig,
            ),
        )

        assertEquals("Seed-Basis", state.basisLabel)
        assertEquals("vitality", state.definitionId)
        assertEquals(2, state.sections.size)
        assertEquals(
            listOf("Statusschema", "Richtung", "Quellen", "Flow"),
            state.sections.first().axes.map { it.label },
        )
        assertEquals(2, state.previewAxes.size)
        assertEquals("Status", state.sections.first().axes.first().valueLabel)
    }

    @Test
    fun projector_keepsTemplateBasisVisibleForFreeAreas() {
        val instance = AreaInstance(
            areaId = "custom-area",
            definitionId = "template:free",
            title = "Podcast Ideen",
            summary = "Ideen sammeln und schaerfen.",
            iconKey = "spark",
            targetScore = 3,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "adaptive",
            selectedTracks = setOf("Spuren"),
            signalBlend = 60,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
            authoringConfig = AreaAuthoringConfig(
                lageMode = AreaLageMode.State,
                directionMode = AreaDirectionMode.Focus,
                sourcesMode = AreaSourcesMode.Curated,
                flowProfile = AreaFlowProfile.Supportive,
                complexityLevel = AreaComplexityLevel.EXPERT,
                visibilityLevel = AreaVisibilityLevel.Expanded,
            ),
            templateId = "free",
        )

        val state = projectAreaAuthoringStudioState(
            AreaAuthoringProjectionInput(
                definition = null,
                blueprint = AreaBlueprint(
                    areaId = "custom-area",
                    summary = "Ideen sammeln und schaerfen.",
                    trackLabels = listOf("Spuren", "Notizen", "Links", "Impulse"),
                    defaultTemplateId = "free",
                    defaultIconKey = "spark",
                    panelContentSeeds = mapOf(
                        AreaPanelKind.LAGE to AreaPanelContentSeed("Lage", "Aktuelle Lesart."),
                        AreaPanelKind.RICHTUNG to AreaPanelContentSeed("Richtung", "Naechste Ausrichtung."),
                        AreaPanelKind.QUELLEN to AreaPanelContentSeed("Quellen", "Aktive Quellen."),
                        AreaPanelKind.FLOW to AreaPanelContentSeed("Flow", "Lokaler Flow."),
                    ),
                ),
                instance = instance,
                authoringConfig = instance.authoringConfig,
            ),
        )

        assertEquals("Vorlagen-Basis", state.basisLabel)
        assertTrue(state.summary.contains("Zustand und Lesepunkt"))
        assertEquals("Reflexion", state.sections.first().axes.first().valueLabel)
        assertEquals("Expert", state.sections[1].axes.first().valueLabel)
        assertEquals("Offen", state.sections[1].axes.last().valueLabel)
        assertEquals(6, state.previewAxes.size)
        assertEquals(listOf("Tiefe", "Sichtbarkeit"), state.previewAxes.takeLast(2).map { it.label })
    }

    @Test
    fun projector_respectsDefinitionAuthoringAxesWhenBuildingSections() {
        val definition = AreaDefinition(
            id = "template:light",
            title = "Leichter Bereich",
            shortTitle = "Leicht",
            iconKey = "spark",
            category = AreaCategory.OPEN,
            overviewMode = AreaOverviewMode.REFLECTION,
            complexityLevel = AreaComplexityLevel.BASIC,
            seededByDefault = false,
            userCreatable = true,
            lageType = AreaLageType.STATE,
            focusType = AreaFocusType.HYBRID,
            sourceTypesAllowed = setOf(AreaSourceType.MANUAL),
            flowCapabilities = setOf(AreaFlowCapability.REVIEW),
            defaultConfig = AreaDefaultConfig(targetScore = 3),
            authoringAxes = setOf(
                AreaAuthoringAxis.STATUS_SCHEMA,
                AreaAuthoringAxis.DIRECTION,
                AreaAuthoringAxis.VISIBILITY,
            ),
        )
        val instance = AreaInstance(
            areaId = "light-area",
            definitionId = definition.id,
            title = "Leicht",
            summary = "Kompakt authorierbar.",
            iconKey = "spark",
            targetScore = 3,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "adaptive",
            selectedTracks = setOf("Spur"),
            signalBlend = 60,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
            authoringConfig = AreaAuthoringConfig(
                lageMode = AreaLageMode.State,
                directionMode = AreaDirectionMode.Balanced,
                sourcesMode = AreaSourcesMode.Balanced,
                flowProfile = AreaFlowProfile.Stable,
                complexityLevel = AreaComplexityLevel.BASIC,
                visibilityLevel = AreaVisibilityLevel.Standard,
            ),
            templateId = "free",
        )

        val state = projectAreaAuthoringStudioState(
            AreaAuthoringProjectionInput(
                definition = definition,
                blueprint = AreaBlueprint(
                    areaId = definition.id,
                    summary = "Kompakte Basis.",
                    trackLabels = listOf("Spur"),
                    defaultTemplateId = "free",
                    defaultIconKey = "spark",
                    panelContentSeeds = mapOf(
                        AreaPanelKind.LAGE to AreaPanelContentSeed("Lage", "Status lesen."),
                        AreaPanelKind.RICHTUNG to AreaPanelContentSeed("Richtung", "Naechsten Zug formen."),
                        AreaPanelKind.QUELLEN to AreaPanelContentSeed("Quellen", "Nur leicht halten."),
                        AreaPanelKind.FLOW to AreaPanelContentSeed("Flow", "Rueckkehr sichern."),
                    ),
                ),
                instance = instance,
                authoringConfig = instance.authoringConfig,
            ),
        )

        assertEquals(listOf("Statusschema", "Richtung"), state.sections.first().axes.map { it.label })
        assertEquals(listOf("Sichtbarkeit"), state.sections[1].axes.map { it.label })
        assertEquals(listOf("Statusschema", "Richtung"), state.previewAxes.map { it.label })
        assertTrue(state.summary.contains("Liest"))
        assertTrue(state.summary.contains("richtet"))
    }
}
