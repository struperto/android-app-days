package com.struperto.androidappdays.domain.area

import org.junit.Assert.assertEquals
import org.junit.Test

class AreaProfileConfigTest {
    @Test
    fun defaultProfileConfig_matchesPilotAreaShape() {
        val friends = defaultAreaProfileConfig(
            definition = startAreaKernelDefinition("friends"),
        )
        val vitality = defaultAreaProfileConfig(
            definition = startAreaKernelDefinition("vitality"),
        )
        val discovery = defaultAreaProfileConfig(
            definition = startAreaKernelDefinition("discovery"),
        )
        val meaning = defaultAreaProfileConfig(
            definition = startAreaKernelDefinition("meaning"),
        )

        assertEquals(AreaLageMode.State, friends.lageMode)
        assertEquals(AreaDirectionMode.Focus, friends.directionMode)
        assertEquals(AreaSourcesMode.Curated, friends.sourcesMode)
        assertEquals(AreaFlowProfile.Supportive, friends.flowProfile)

        assertEquals(AreaLageMode.Score, vitality.lageMode)
        assertEquals(AreaDirectionMode.Balanced, vitality.directionMode)
        assertEquals(AreaSourcesMode.Signals, vitality.sourcesMode)
        assertEquals(AreaFlowProfile.Stable, vitality.flowProfile)

        assertEquals(AreaLageMode.State, discovery.lageMode)
        assertEquals(AreaDirectionMode.Focus, discovery.directionMode)
        assertEquals(AreaSourcesMode.Curated, discovery.sourcesMode)
        assertEquals(AreaFlowProfile.Supportive, discovery.flowProfile)

        assertEquals(AreaLageMode.State, meaning.lageMode)
        assertEquals(AreaDirectionMode.Focus, meaning.directionMode)
        assertEquals(AreaSourcesMode.Curated, meaning.sourcesMode)
        assertEquals(AreaFlowProfile.Supportive, meaning.flowProfile)
    }

    @Test
    fun defaultProfileConfig_usesTemplateFallbackForFreeAreas() {
        val project = defaultAreaProfileConfig(
            definition = null,
            templateId = "project",
        )
        val free = defaultAreaProfileConfig(
            definition = null,
            templateId = "free",
        )

        assertEquals(AreaDirectionMode.Focus, project.directionMode)
        assertEquals(AreaFlowProfile.Active, project.flowProfile)

        assertEquals(AreaLageMode.State, free.lageMode)
        assertEquals(AreaSourcesMode.Curated, free.sourcesMode)
        assertEquals(AreaFlowProfile.Stable, free.flowProfile)
    }
}
