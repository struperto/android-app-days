package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaDailyCheck
import com.struperto.androidappdays.domain.area.AreaBlueprint
import com.struperto.androidappdays.domain.area.AreaDefaultConfig
import com.struperto.androidappdays.domain.area.AreaDefinition
import com.struperto.androidappdays.domain.area.AreaFlowCapability
import com.struperto.androidappdays.domain.area.AreaFocusType
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaLageType
import com.struperto.androidappdays.domain.area.AreaOverviewMode
import com.struperto.androidappdays.domain.area.AreaPanelContentSeed
import com.struperto.androidappdays.domain.area.AreaPanelKind
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.AreaSourceType
import com.struperto.androidappdays.domain.area.defaultAreaProfileConfig
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartOverviewKernelProjectorTest {
    @Test
    fun kernelProjector_projectsOverviewFromKernelInputs() {
        val input = StartOverviewKernelInput(
            definition = AreaDefinition(
                id = "clarity",
                title = "Fokus",
                shortTitle = "Fokus",
                iconKey = "focus",
                category = com.struperto.androidappdays.domain.area.AreaCategory.DIRECTION,
                overviewMode = AreaOverviewMode.PLAN,
                complexityLevel = com.struperto.androidappdays.domain.area.AreaComplexityLevel.BASIC,
                seededByDefault = true,
                userCreatable = false,
                lageType = AreaLageType.SCORE,
                focusType = AreaFocusType.HYBRID,
                sourceTypesAllowed = setOf(AreaSourceType.MANUAL, AreaSourceType.TRACK),
                flowCapabilities = setOf(AreaFlowCapability.REVIEW),
                defaultConfig = AreaDefaultConfig(targetScore = 4),
            ),
            blueprint = AreaBlueprint(
                areaId = "clarity",
                summary = "Fokus halten.",
                trackLabels = listOf("Fokus", "Ruhe"),
                defaultTemplateId = "theme",
                defaultIconKey = "focus",
                panelContentSeeds = mapOf(
                    AreaPanelKind.LAGE to AreaPanelContentSeed("Lage", "Aktuellen Zustand lesen."),
                    AreaPanelKind.RICHTUNG to AreaPanelContentSeed("Richtung", "Naechsten Zug setzen."),
                    AreaPanelKind.QUELLEN to AreaPanelContentSeed("Quellen", "Spuren gewichten."),
                    AreaPanelKind.FLOW to AreaPanelContentSeed("Flow", "Lokale Steuerung setzen."),
                ),
            ),
            instance = AreaInstance(
                areaId = "clarity",
                title = "Fokus",
                summary = "Fokus halten.",
                iconKey = "focus",
                targetScore = 4,
                sortOrder = 1,
                isActive = true,
                cadenceKey = "adaptive",
                selectedTracks = setOf("Fokus"),
                signalBlend = 60,
                intensity = 3,
                remindersEnabled = false,
                reviewEnabled = true,
                experimentsEnabled = false,
                profileConfig = defaultAreaProfileConfig(
                    definition = startAreaDefinition("clarity"),
                ),
                templateId = "theme",
            ),
            snapshot = AreaSnapshot(
                areaId = "clarity",
                date = LocalDate.of(2026, 3, 11),
                manualScore = 3,
            ),
        )

        val state = projectStartOverviewState(listOf(input))

        assertEquals(1, state.areas.size)
        assertEquals("Fokus", state.areas.single().label)
        assertEquals("Fokus halten.", state.areas.single().summary)
        assertEquals("3/4", state.areas.single().statusLabel)
        assertEquals("Fokus", state.areas.single().focusLabel)
        assertEquals("Fokus · Aktiv", state.areas.single().profileLabel)
        assertEquals(StartAreaStatusKind.Pull, state.areas.single().statusKind)
    }

    @Test
    fun kernelProjector_projectsStateDrivenFriendshipOverview() {
        val state = projectStartOverviewState(
            listOf(
                StartOverviewKernelInput(
                    definition = startAreaDefinition("friends"),
                    blueprint = startAreaKernelBlueprint("friends"),
                    instance = AreaInstance(
                        areaId = "friends",
                        title = "Freundschaft",
                        summary = "Verbindung warm und lebendig halten.",
                        iconKey = "care",
                        targetScore = 3,
                        sortOrder = 0,
                        isActive = true,
                        cadenceKey = "weekly",
                        selectedTracks = linkedSetOf("Kontakt", "Tiefe"),
                        signalBlend = 50,
                        intensity = 2,
                        remindersEnabled = true,
                        reviewEnabled = true,
                        experimentsEnabled = false,
                        profileConfig = defaultAreaProfileConfig(
                            definition = startAreaDefinition("friends"),
                        ),
                        templateId = "person",
                    ),
                    snapshot = AreaSnapshot(
                        areaId = "friends",
                        date = LocalDate.of(2026, 3, 11),
                        manualStateKey = "warm",
                    ),
                ),
            ),
        )

        assertEquals("Warm", state.areas.single().statusLabel)
        assertEquals("Kontakt", state.areas.single().focusLabel)
        assertEquals("Kontakt · Tragend", state.areas.single().profileLabel)
        assertEquals(StartAreaStatusKind.Stable, state.areas.single().statusKind)
        assertEquals(0.9f, state.areas.single().progress)
    }

    @Test
    fun kernelProjector_projectsLearningOverviewWithProgressSemantics() {
        val state = projectStartOverviewState(
            listOf(
                StartOverviewKernelInput(
                    definition = startAreaDefinition("learning"),
                    blueprint = startAreaKernelBlueprint("learning"),
                    instance = AreaInstance(
                        areaId = "learning",
                        title = "Lernen",
                        summary = "Wissen und Koennen im Alltag wirklich aufbauen.",
                        iconKey = "book",
                        targetScore = 3,
                        sortOrder = 0,
                        isActive = true,
                        cadenceKey = "weekly",
                        selectedTracks = linkedSetOf("Ueben", "Anwenden"),
                        signalBlend = 70,
                        intensity = 4,
                        remindersEnabled = true,
                        reviewEnabled = true,
                        experimentsEnabled = true,
                        profileConfig = defaultAreaProfileConfig(
                            definition = startAreaDefinition("learning"),
                        ),
                        templateId = "medium",
                    ),
                    snapshot = AreaSnapshot(
                        areaId = "learning",
                        date = LocalDate.of(2026, 3, 11),
                        manualScore = 2,
                    ),
                ),
            ),
        )

        assertEquals("Fortschritt 2/3", state.areas.single().statusLabel)
        assertEquals(StartAreaStatusKind.Pull, state.areas.single().statusKind)
        assertEquals(2f / 3f, state.areas.single().progress)
    }

    @Test
    fun kernelProjector_uses_plan_context_for_progress_and_maintenance_outputs() {
        val progressState = projectStartOverviewState(
            listOf(
                StartOverviewKernelInput(
                    definition = startAreaDefinition("impact"),
                    blueprint = startAreaKernelBlueprint("impact"),
                    instance = AreaInstance(
                        areaId = "impact",
                        title = "Arbeit",
                        summary = "Wichtige Arbeit voranbringen.",
                        iconKey = "briefcase",
                        targetScore = 4,
                        sortOrder = 0,
                        isActive = true,
                        cadenceKey = "weekly",
                        selectedTracks = linkedSetOf("Prioritaet", "Projekte"),
                        signalBlend = 70,
                        intensity = 3,
                        remindersEnabled = false,
                        reviewEnabled = true,
                        experimentsEnabled = false,
                        profileConfig = defaultAreaProfileConfig(
                            definition = startAreaDefinition("impact"),
                        ),
                        templateId = "project",
                    ),
                    snapshot = null,
                    openPlanTitles = listOf("Ticket X schliessen"),
                    dueCount = 1,
                    logicalDate = LocalDate.of(2026, 3, 12),
                    projectionTime = Instant.parse("2026-03-12T08:00:00Z"),
                ),
                StartOverviewKernelInput(
                    definition = startAreaDefinition("home"),
                    blueprint = startAreaKernelBlueprint("home"),
                    instance = AreaInstance(
                        areaId = "home",
                        title = "Zuhause",
                        summary = "Raum und Ordnung tragbar halten.",
                        iconKey = "home",
                        targetScore = 3,
                        sortOrder = 1,
                        isActive = true,
                        cadenceKey = "weekly",
                        selectedTracks = linkedSetOf("Pflege", "Ordnung"),
                        signalBlend = 60,
                        intensity = 2,
                        remindersEnabled = true,
                        reviewEnabled = true,
                        experimentsEnabled = false,
                        profileConfig = defaultAreaProfileConfig(
                            definition = startAreaDefinition("home"),
                        ),
                        templateId = "place",
                    ),
                    snapshot = null,
                    openPlanTitles = listOf("Bad putzen"),
                    dueCount = 1,
                    logicalDate = LocalDate.of(2026, 3, 12),
                    projectionTime = Instant.parse("2026-03-12T08:00:00Z"),
                ),
            ),
        )

        assertEquals("Ticket X schliessen", progressState.areas[0].todayStepLabel)
        assertEquals("Bad putzen", progressState.areas[1].todayStepLabel)
    }

    @Test
    fun legacyBridge_andKernelProjector_preserveOverviewParityForSeededAreas() {
        val areas = listOf(
            LifeArea(
                id = "impact",
                label = "Arbeit",
                definition = "Wichtige Arbeit voranbringen",
                targetScore = 4,
                sortOrder = 0,
                isActive = true,
                templateId = "project",
                iconKey = "briefcase",
            ),
        )
        val dailyChecks = listOf(
            LifeAreaDailyCheck(
                areaId = "impact",
                date = "2026-03-11",
                manualScore = 3,
            ),
        )

        val legacyState = mapStartOverviewState(
            areas = areas,
            dailyChecks = dailyChecks,
        )
        val kernelState = projectStartOverviewState(
            inputs = buildStartOverviewKernelInputs(
                areas = areas,
                dailyChecks = dailyChecks,
            ),
        )

        assertEquals(legacyState, kernelState)
    }

    @Test
    fun legacyBridge_keepsCustomAreaProjectableWithoutSeedDefinition() {
        val inputs = buildStartOverviewKernelInputs(
            areas = listOf(
                LifeArea(
                    id = "custom-area",
                    label = "Eigenes Thema",
                    definition = "Ein freier Bereich.",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    templateId = "free",
                    iconKey = "spark",
                ),
            ),
            dailyChecks = emptyList(),
        )

        val input = inputs.single()
        assertEquals(null, input.definition)
        assertEquals(null, input.blueprint)

        val state = projectStartOverviewState(inputs)
        assertEquals("Eigenes Thema", state.areas.single().label)
        assertEquals("Ziel 3/5", state.areas.single().statusLabel)
        assertTrue(state.areas.single().progress > 0f)
    }

    @Test
    fun kernelInputBuilder_fromInstancesAndSnapshots_matchesLegacyBridgeForSeededArea() {
        val legacyInputs = buildStartOverviewKernelInputs(
            areas = listOf(
                LifeArea(
                    id = "vitality",
                    label = "Vitalitaet",
                    definition = "Schlaf und Energie tragen.",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    templateId = "ritual",
                    iconKey = "heart",
                ),
            ),
            dailyChecks = listOf(
                LifeAreaDailyCheck(
                    areaId = "vitality",
                    date = "2026-03-11",
                    manualScore = 4,
                ),
            ),
        )
        val kernelInputs = buildStartOverviewKernelInputsFromKernel(
            instances = listOf(
                AreaInstance(
                    areaId = "vitality",
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
                    profileConfig = defaultAreaProfileConfig(
                        definition = startAreaKernelDefinition("vitality"),
                    ),
                    templateId = "ritual",
                ),
            ),
            snapshots = listOf(
                AreaSnapshot(
                    areaId = "vitality",
                    date = LocalDate.of(2026, 3, 11),
                    manualScore = 4,
                ),
            ),
        )

        assertEquals(projectStartOverviewState(legacyInputs), projectStartOverviewState(kernelInputs))
    }
}
