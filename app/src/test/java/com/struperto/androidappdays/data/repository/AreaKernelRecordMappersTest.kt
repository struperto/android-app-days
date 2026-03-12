package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.AreaInstanceEntity
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaDirectionMode
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaSourcesMode
import com.struperto.androidappdays.domain.area.AreaVisibilityLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class AreaKernelRecordMappersTest {
    @Test
    fun areaInstanceEntity_mapsPersistedAuthoringFieldsIntoKernelInstance() {
        val entity = AreaInstanceEntity(
            areaId = "vitality",
            definitionId = "vitality",
            title = "Vitalitaet",
            summary = "Schlaf und Energie tragen.",
            iconKey = "heart",
            targetScore = 4,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "daily",
            selectedTracks = "Schlaf,Energie",
            signalBlend = 75,
            intensity = 4,
            remindersEnabled = true,
            reviewEnabled = true,
            experimentsEnabled = false,
            lageMode = "score",
            directionMode = "focus",
            sourcesMode = "signals",
            flowProfile = "active",
            authoringComplexity = "EXPERT",
            authoringVisibility = "expanded",
            templateId = "ritual",
            createdAt = 10L,
            updatedAt = 20L,
        )

        val instance = entity.toAreaInstance()

        assertEquals("vitality", instance.definitionId)
        assertEquals(AreaLageMode.Score, instance.authoringConfig.lageMode)
        assertEquals(AreaDirectionMode.Focus, instance.authoringConfig.directionMode)
        assertEquals(AreaSourcesMode.Signals, instance.authoringConfig.sourcesMode)
        assertEquals(AreaFlowProfile.Active, instance.authoringConfig.flowProfile)
        assertEquals(AreaComplexityLevel.EXPERT, instance.authoringConfig.complexityLevel)
        assertEquals(AreaVisibilityLevel.Expanded, instance.authoringConfig.visibilityLevel)
    }

    @Test
    fun areaInstanceEntity_fallsBackToDefinitionAndTemplateDefaultsWhenAuthoringFieldsAreMissing() {
        val entity = AreaInstanceEntity(
            areaId = "custom",
            definitionId = "",
            title = "Eigener Bereich",
            summary = "Eigene Beschreibung.",
            iconKey = "spark",
            targetScore = 3,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "adaptive",
            selectedTracks = "",
            signalBlend = 60,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
            lageMode = "",
            directionMode = "",
            sourcesMode = "",
            flowProfile = "",
            authoringComplexity = "",
            authoringVisibility = "",
            templateId = "free",
            createdAt = 10L,
            updatedAt = 20L,
        )

        val instance = entity.toAreaInstance()

        assertEquals("template:free", instance.definitionId)
        assertEquals(AreaLageMode.State, instance.authoringConfig.lageMode)
        assertEquals(AreaDirectionMode.Balanced, instance.authoringConfig.directionMode)
        assertEquals(AreaSourcesMode.Curated, instance.authoringConfig.sourcesMode)
        assertEquals(AreaFlowProfile.Stable, instance.authoringConfig.flowProfile)
        assertEquals(AreaComplexityLevel.ADVANCED, instance.authoringConfig.complexityLevel)
        assertEquals(AreaVisibilityLevel.Standard, instance.authoringConfig.visibilityLevel)
    }
}
