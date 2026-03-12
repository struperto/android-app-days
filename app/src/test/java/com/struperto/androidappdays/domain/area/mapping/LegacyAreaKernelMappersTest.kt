package com.struperto.androidappdays.domain.area.mapping

import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaDailyCheck
import com.struperto.androidappdays.data.repository.LifeAreaProfile
import com.struperto.androidappdays.domain.area.AreaCategory
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaFlowCapability
import com.struperto.androidappdays.domain.area.AreaOverviewMode
import com.struperto.androidappdays.domain.area.AreaPanelKind
import com.struperto.androidappdays.domain.area.AreaPermissionSensitivity
import com.struperto.androidappdays.domain.area.AreaSourceType
import com.struperto.androidappdays.feature.start.startAreaBlueprints
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAreaKernelMappersTest {
    @Test
    fun startAreaBlueprint_mapsToKernelDefinitionWithStableAxes() {
        val blueprint = startAreaBlueprints.first { it.id == "vitality" }

        val definition = blueprint.toAreaDefinition()

        assertEquals("vitality", definition.id)
        assertEquals(AreaCategory.FOUNDATION, definition.category)
        assertEquals(AreaOverviewMode.SIGNAL, definition.overviewMode)
        assertEquals(AreaComplexityLevel.BASIC, definition.complexityLevel)
        assertTrue(definition.sourceTypesAllowed.contains(AreaSourceType.LOCAL_SIGNAL))
        assertTrue(definition.flowCapabilities.contains(AreaFlowCapability.REVIEW))
        assertTrue(definition.authoringAxes.contains(com.struperto.androidappdays.domain.area.AreaAuthoringAxis.STATUS_SCHEMA))
        assertTrue(definition.authoringAxes.contains(com.struperto.androidappdays.domain.area.AreaAuthoringAxis.SOURCES))
        assertEquals(4, definition.defaultConfig.targetScore)
        assertEquals(setOf("Schlaf", "Energie"), definition.defaultConfig.defaultSelectedTracks)
        assertEquals(AreaPermissionSensitivity.HIGH, definition.permissionSensitivity)
    }

    @Test
    fun startAreaBlueprint_mapsToKernelBlueprintAndDerivesMissingSourcesPanel() {
        val legacy = startAreaBlueprints.first { it.id == "friends" }

        val blueprint = legacy.toAreaBlueprint()

        assertEquals("friends", blueprint.areaId)
        assertEquals(4, blueprint.panelContentSeeds.size)
        assertEquals("Status", blueprint.panelContentSeeds.getValue(AreaPanelKind.LAGE).title)
        assertEquals("Ausrichten", blueprint.panelContentSeeds.getValue(AreaPanelKind.RICHTUNG).title)
        assertEquals("Automatik", blueprint.panelContentSeeds.getValue(AreaPanelKind.FLOW).title)
        assertEquals("Quellen", blueprint.panelContentSeeds.getValue(AreaPanelKind.QUELLEN).title)
        assertTrue(blueprint.panelContentSeeds.getValue(AreaPanelKind.QUELLEN).summary.contains("Spuren"))
        assertTrue(blueprint.defaultSourceLabels.contains("Kontakt"))
        assertTrue(blueprint.domainTags.contains("SOCIAL"))
    }

    @Test
    fun lifeAreaAndProfile_mapToKernelInstance() {
        val area = LifeArea(
            id = "clarity",
            label = "Fokus",
            definition = "Fokus, Ruhe und Bildschirmdruck bewusst im Griff halten.",
            targetScore = 4,
            sortOrder = 2,
            isActive = true,
            templateId = "theme",
            iconKey = "focus",
        )
        val profile = LifeAreaProfile(
            areaId = "clarity",
            cadence = "daily",
            intensity = 4,
            signalBlend = 70,
            selectedTracks = setOf("Fokus", "Ruhe"),
            remindersEnabled = true,
            reviewEnabled = true,
            experimentsEnabled = false,
        )

        val instance = area.toAreaInstance(profile)

        assertEquals("clarity", instance.areaId)
        assertEquals("clarity", instance.definitionId)
        assertEquals("Fokus", instance.title)
        assertEquals("daily", instance.cadenceKey)
        assertEquals(70, instance.signalBlend)
        assertTrue(instance.remindersEnabled)
        assertFalse(instance.experimentsEnabled)
    }

    @Test
    fun lifeAreaCanMapToKernelInstanceWithDefinitionDefaultsWhenProfileIsMissing() {
        val legacyBlueprint = startAreaBlueprints.first { it.id == "friends" }
        val area = LifeArea(
            id = "friends",
            label = "Freundschaft",
            definition = "Freunde im Kontakt und im echten Leben lebendig halten.",
            targetScore = 3,
            sortOrder = 5,
            isActive = true,
            templateId = "person",
            iconKey = "chat",
        )

        val instance = area.toAreaInstance(
            definition = legacyBlueprint.toAreaDefinition(),
            profile = null,
        )

        assertEquals("adaptive", instance.cadenceKey)
        assertEquals("friends", instance.definitionId)
        assertEquals(setOf("Kontakt", "Tiefe"), instance.selectedTracks)
        assertEquals(60, instance.signalBlend)
        assertTrue(instance.reviewEnabled)
        assertFalse(instance.remindersEnabled)
    }

    @Test
    fun lifeAreaDailyCheck_mapsToKernelSnapshot() {
        val dailyCheck = LifeAreaDailyCheck(
            areaId = "clarity",
            date = "2026-03-11",
            manualScore = 4,
        )

        val snapshot = dailyCheck.toAreaSnapshot()

        assertEquals("clarity", snapshot.areaId)
        assertEquals(LocalDate.of(2026, 3, 11), snapshot.date)
        assertEquals(4, snapshot.manualScore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun lifeAreaAndProfileRequireMatchingIds() {
        val area = LifeArea(
            id = "clarity",
            label = "Fokus",
            definition = "Fokus halten.",
            targetScore = 4,
            sortOrder = 0,
            isActive = true,
        )
        val profile = LifeAreaProfile(
            areaId = "other",
            cadence = "adaptive",
            intensity = 3,
            signalBlend = 60,
            selectedTracks = emptySet(),
            remindersEnabled = false,
            reviewEnabled = true,
            experimentsEnabled = false,
        )

        area.toAreaInstance(profile)
    }
}
