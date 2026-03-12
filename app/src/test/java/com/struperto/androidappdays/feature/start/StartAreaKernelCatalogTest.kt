package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.allLifeAreaPresets
import com.struperto.androidappdays.data.repository.defaultLifeAreas
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.area.AreaLageType
import com.struperto.androidappdays.domain.area.AreaOverviewMode
import com.struperto.androidappdays.domain.area.AreaPanelKind
import com.struperto.androidappdays.domain.area.startAreaKernelSeed
import com.struperto.androidappdays.domain.area.startAreaKernelSeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartAreaKernelCatalogTest {
    @Test
    fun defaultLifeAreas_areDerivedFromKernelSeedCatalog() {
        val defaults = defaultLifeAreas()
        val kernelSeeds = startAreaKernelSeeds

        assertEquals(kernelSeeds.size, defaults.size)
        assertEquals(kernelSeeds.first().definition.id, defaults.first().id)
        assertEquals(kernelSeeds.first().definition.title, defaults.first().label)
        assertEquals(kernelSeeds.first().blueprint.summary, defaults.first().definition)
        assertEquals(kernelSeeds.first().definition.defaultConfig.targetScore, defaults.first().targetScore)
    }

    @Test
    fun lifeAreaPresets_areDerivedFromKernelSeedCatalog() {
        val vitalityPreset = allLifeAreaPresets.first { it.id == "vitality" }

        assertEquals("Vitalitaet", vitalityPreset.label)
        assertEquals("ritual", vitalityPreset.templateId)
        assertEquals("heart", vitalityPreset.iconKey)
        assertEquals(4, vitalityPreset.targetScore)
    }

    @Test
    fun legacyStartBlueprints_areDerivedFromCanonicalKernelSeed() {
        val blueprint = startAreaBlueprint("vitality") ?: error("missing vitality blueprint")
        val definition = startAreaDefinition("vitality") ?: error("missing vitality definition")
        val kernelBlueprint = startAreaKernelBlueprint("vitality") ?: error("missing vitality kernel blueprint")

        assertEquals(definition.title, blueprint.label)
        assertEquals(kernelBlueprint.summary, blueprint.summary)
        assertEquals(kernelBlueprint.trackLabels, blueprint.tracks)
        assertEquals(definition.defaultConfig.targetScore, blueprint.defaultTargetScore)
        assertEquals(kernelBlueprint.defaultTemplateId, blueprint.defaultTemplateId)
        assertEquals(definition.iconKey, blueprint.defaultIconKey)
        assertEquals(AreaOverviewMode.SIGNAL, definition.overviewMode)
        assertEquals(setOf(LifeDomain.SLEEP, LifeDomain.MOVEMENT, LifeDomain.HYDRATION, LifeDomain.NUTRITION, LifeDomain.HEALTH), blueprint.domains)
        assertEquals(
            kernelBlueprint.panelContentSeeds.getValue(AreaPanelKind.FLOW).summary,
            blueprint.entries.first { it.panel == StartAreaPanel.Options }.summary,
        )
    }

    @Test
    fun kernelCatalog_keepsExpectedSeedOrderForStartSetup() {
        assertEquals(
            listOf("vitality", "clarity", "impact", "bond"),
            startAreaKernelSeeds.take(4).map { it.definition.id },
        )
        assertTrue(startAreaKernelSeeds.any { it.definition.id == "discovery" })
    }

    @Test
    fun kernelCatalog_resolvesLegacySeedAliasesToCanonicalSeeds() {
        assertEquals("discovery", startAreaKernelSeed("curiosity")?.definition?.id)
        assertEquals("meaning", startAreaKernelSeed("purpose")?.definition?.id)
    }

    @Test
    fun kernelCatalog_rollsSemanticsOutBeyondInitialPilots() {
        val differentiatedIds = setOf(
            "vitality",
            "clarity",
            "friends",
            "impact",
            "recovery",
            "learning",
            "discovery",
            "meaning",
        )

        val differentiatedSeeds = startAreaKernelSeeds.filter { it.definition.id in differentiatedIds }

        assertEquals(differentiatedIds, differentiatedSeeds.map { it.definition.id }.toSet())
        assertTrue(differentiatedSeeds.all { it.blueprint.pilotSemantics != null })
        assertEquals(
            setOf("friends", "recovery", "discovery", "meaning"),
            differentiatedSeeds
                .filter { it.definition.lageType == AreaLageType.STATE }
                .map { it.definition.id }
                .toSet(),
        )
    }
}
