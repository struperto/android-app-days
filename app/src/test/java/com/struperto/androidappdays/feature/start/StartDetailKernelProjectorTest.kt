package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaDailyCheck
import com.struperto.androidappdays.data.repository.LifeAreaProfile
import com.struperto.androidappdays.domain.area.defaultAreaProfileConfig
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartDetailKernelProjectorTest {
    @Test
    fun detailKernelBridge_buildsSeededAreaInputFromLegacyModels() {
        val input = buildStartAreaDetailKernelInput(
            area = LifeArea(
                id = "vitality",
                label = "Vitalitaet",
                definition = "Schlaf, Energie, Bewegung, Essen und Trinken im Takt halten.",
                targetScore = 4,
                sortOrder = 0,
                isActive = true,
                templateId = "ritual",
                iconKey = "heart",
            ),
            dailyCheck = LifeAreaDailyCheck(
                areaId = "vitality",
                date = "2026-03-11",
                manualScore = 4,
            ),
            profile = null,
        )

        assertEquals("vitality", input.definition.id)
        assertEquals("vitality", input.blueprint.areaId)
        assertEquals("vitality", input.instance.areaId)
        assertEquals(4, input.snapshot?.manualScore)
        assertEquals(listOf("Schlaf", "Energie", "Bewegung", "Essen"), input.blueprint.trackLabels)
    }

    @Test
    fun detailProjector_keepsPilotPathActionLabelsForClarity() {
        val detail = projectStartAreaDetailState(
            input = buildStartAreaDetailKernelInput(
                area = LifeArea(
                    id = "clarity",
                    label = "Fokus",
                    definition = "Fokus und Ruhe im Tag absichern.",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    templateId = "theme",
                    iconKey = "focus",
                ),
                dailyCheck = LifeAreaDailyCheck(
                    areaId = "clarity",
                    date = "2026-03-11",
                    manualScore = 3,
                ),
                profile = LifeAreaProfile(
                    areaId = "clarity",
                    cadence = "daily",
                    intensity = 4,
                    signalBlend = 70,
                    selectedTracks = linkedSetOf("Schutz", "Tiefe"),
                    remindersEnabled = true,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                ),
            ),
        )

        val pathPanel = detail.panelStates.first { it.panel == StartAreaPanel.Path }
        assertEquals("Schutzlinie", pathPanel.screenState.actions.first { it.id == StartPanelActionId.PathFocus }.label)
        assertEquals("Rhythmus", pathPanel.screenState.actions.first { it.id == StartPanelActionId.PathMode }.valueLabel)
    }

    @Test
    fun detailProjector_mapsSeededAreaPanelsFromKernelInputs() {
        val detail = projectStartAreaDetailState(
            input = buildStartAreaDetailKernelInput(
                area = LifeArea(
                    id = "impact",
                    label = "Arbeit",
                    definition = "Wichtige Arbeit, Projekte und Entscheidungen klar voranbringen.",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    templateId = "project",
                    iconKey = "briefcase",
                ),
                dailyCheck = LifeAreaDailyCheck(
                    areaId = "impact",
                    date = "2026-03-11",
                    manualScore = 2,
                ),
                profile = LifeAreaProfile(
                    areaId = "impact",
                    cadence = "weekly",
                    intensity = 4,
                    signalBlend = 75,
                    selectedTracks = linkedSetOf("Projekte", "Fokus"),
                    remindersEnabled = true,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                ),
            ),
        )

        assertEquals("Projekte", detail.focusTrack)
        assertEquals("2/5", detail.statusLabel)
        assertEquals(listOf("Zustand", "Auswahl", "Eingang", "Rhythmus"), detail.panelStates.map { it.title })

        val pathPanel = detail.panelStates.first { it.panel == StartAreaPanel.Path }
        assertEquals("Diese Woche Projekte voranbringen", pathPanel.summary)
        assertEquals("Projekte", pathPanel.screenState.core.value)
        assertEquals("Rhythmus", pathPanel.screenState.metrics[1].value)
        assertEquals("Diese Woche Projekte voranbringen", pathPanel.screenState.metrics.last().value)

        val flowPanel = detail.panelStates.first { it.panel == StartAreaPanel.Options }
        assertEquals("Aktiv · Startsignal · Review", flowPanel.summary)
        assertEquals("Aktiv", flowPanel.screenState.headerLabel)
        assertEquals("Aktiv", flowPanel.screenState.metrics[1].value)
        assertEquals(
            "Heute zieht Startsignal und Review den Bereich deutlich nach vorn.",
            flowPanel.screenState.effectLabel,
        )
        assertEquals(
            "legt mehr Aktivierung und sichtbaren Zug hinein",
            flowPanel.screenState.actions.first { it.id == StartPanelActionId.FlowProfile }.supportingLabel,
        )

        val sourcesPanel = detail.panelStates.first { it.panel == StartAreaPanel.Sources }
        assertEquals("Projekte · 75% Signal", sourcesPanel.summary)
        assertEquals(
            "Heute fuehren Signale rund um Projekte die Lesart.",
            sourcesPanel.screenState.effectLabel,
        )
        assertEquals("Signalnah", sourcesPanel.screenState.actions.first { it.id == StartPanelActionId.SourcesMode }.valueLabel)
        assertEquals(
            "liest Quellen staerker ueber Signalanteil und Mix",
            sourcesPanel.screenState.actions.first { it.id == StartPanelActionId.SourcesMode }.supportingLabel,
        )
        assertEquals(
            "laufende Vorhaben bewusst buendeln",
            sourcesPanel.screenState.actions.first { it.id == StartPanelActionId.SourcesTracks }.supportingLabel,
        )
        assertEquals(listOf("Prioritaet", "Tiefenarbeit", "Projekte", "Entscheidungen"), sourcesPanel.screenState.actions
            .first { it.id == StartPanelActionId.SourcesTracks }
            .sheet
            ?.options
            ?.map { it.label })
    }

    @Test
    fun detailKernelBridge_keepsCustomAreaProjectableViaTemplateFallback() {
        val detail = projectStartAreaDetailState(
            input = buildStartAreaDetailKernelInput(
                area = LifeArea(
                    id = "custom-area",
                    label = "Eigenes Thema",
                    definition = "Ein freier Bereich.",
                    targetScore = 3,
                    sortOrder = 0,
                    isActive = true,
                    templateId = "person",
                    iconKey = "chat",
                ),
                dailyCheck = null,
                profile = null,
            ),
        )

        assertEquals("Eigenes Thema", detail.title)
        assertEquals("Leise", detail.statusLabel)
        assertEquals(listOf("Kontakt", "Naehe", "Pflege", "Resonanz"), detail.tracks)
        assertEquals(setOf("Kontakt", "Naehe"), detail.selectedTracks)
        assertTrue(detail.reviewEnabled)
        assertFalse(detail.remindersEnabled)
    }

    @Test
    fun detailKernelBuilder_fromInstanceAndSnapshot_matchesLegacyBridgeForSeededArea() {
        val legacyInput = buildStartAreaDetailKernelInput(
            area = LifeArea(
                id = "vitality",
                label = "Vitalitaet",
                definition = "Schlaf, Energie, Bewegung, Essen und Trinken im Takt halten.",
                targetScore = 4,
                sortOrder = 0,
                isActive = true,
                templateId = "ritual",
                iconKey = "heart",
            ),
            dailyCheck = LifeAreaDailyCheck(
                areaId = "vitality",
                date = "2026-03-11",
                manualScore = 4,
            ),
            profile = LifeAreaProfile(
                areaId = "vitality",
                cadence = "weekly",
                intensity = 4,
                signalBlend = 70,
                selectedTracks = linkedSetOf("Energie", "Schlaf"),
                remindersEnabled = true,
                reviewEnabled = true,
                experimentsEnabled = false,
            ),
        )
        val kernelInput = buildStartAreaDetailKernelInput(
            instance = legacyInput.instance,
            snapshot = legacyInput.snapshot,
        )

        assertEquals(
            projectStartAreaDetailState(legacyInput),
            projectStartAreaDetailState(kernelInput),
        )
    }

    @Test
    fun detailProjector_mapsStateDrivenFriendshipPanelsFromKernelInputs() {
        val detail = projectStartAreaDetailState(
            input = buildStartAreaDetailKernelInput(
                instance = com.struperto.androidappdays.domain.area.AreaInstance(
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
                        definition = startAreaKernelDefinition("friends"),
                    ),
                    templateId = "person",
                ),
                snapshot = com.struperto.androidappdays.domain.area.AreaSnapshot(
                    areaId = "friends",
                    date = java.time.LocalDate.of(2026, 3, 11),
                    manualStateKey = "warm",
                ),
            ),
        )

        assertEquals("Warm", detail.statusLabel)
        assertEquals("warm", detail.manualStateKey)
        assertEquals(StartAreaStatusKind.Stable, detail.statusKind)

        val snapshotPanel = detail.panelStates.first { it.panel == StartAreaPanel.Snapshot }
        assertEquals("Reflexion · Heute Warm", snapshotPanel.summary)
        assertEquals("Reflexion", snapshotPanel.countLabel)
        assertEquals(listOf("Lesart", "Heute", "Zuruecksetzen"), snapshotPanel.screenState.actions.map { it.label })
        assertEquals(
            "Kontakt fuehlt sich lebendig und leicht an.",
            snapshotPanel.screenState.actions.first { it.id == StartPanelActionId.SnapshotState }.supportingLabel,
        )

        val flowPanel = detail.panelStates.first { it.panel == StartAreaPanel.Options }
        assertEquals("Tragend · Impuls · Reflexion", flowPanel.summary)
        assertEquals(
            "stuetzt Rueckkehr und sanften lokalen Zug",
            flowPanel.screenState.actions.first { it.id == StartPanelActionId.FlowProfile }.supportingLabel,
        )

        val sourcesPanel = detail.panelStates.first { it.panel == StartAreaPanel.Sources }
        assertEquals(
            "Kontaktimpulse und letzte Beruehrungspunkte. +1 weitere aktiv",
            sourcesPanel.screenState.actions.first { it.id == StartPanelActionId.SourcesTracks }.supportingLabel,
        )

        val pathPanel = detail.panelStates.first { it.panel == StartAreaPanel.Path }
        assertEquals("Kontakt zuerst", pathPanel.summary)
        assertEquals("Beziehungsfokus", pathPanel.screenState.actions.first { it.id == StartPanelActionId.PathFocus }.label)
        assertEquals(
            "zieht eine Spur klar nach vorn",
            pathPanel.screenState.actions.first { it.id == StartPanelActionId.PathMode }.supportingLabel,
        )
    }

    @Test
    fun detailProjector_mapsRecoveryStateDrivenPanelsFromKernelInputs() {
        val detail = projectStartAreaDetailState(
            input = buildStartAreaDetailKernelInput(
                instance = com.struperto.androidappdays.domain.area.AreaInstance(
                    areaId = "recovery",
                    title = "Erholung",
                    summary = "Pausen, Abschalten und Regeneration bewusst absichern.",
                    iconKey = "lotus",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    cadenceKey = "daily",
                    selectedTracks = linkedSetOf("Pausen", "Reset"),
                    signalBlend = 55,
                    intensity = 2,
                    remindersEnabled = true,
                    reviewEnabled = false,
                    experimentsEnabled = true,
                    profileConfig = defaultAreaProfileConfig(
                        definition = startAreaKernelDefinition("recovery"),
                    ),
                    templateId = "ritual",
                ),
                snapshot = com.struperto.androidappdays.domain.area.AreaSnapshot(
                    areaId = "recovery",
                    date = java.time.LocalDate.of(2026, 3, 11),
                    manualStateKey = "ruhig",
                ),
            ),
        )

        assertEquals("Ruhig", detail.statusLabel)
        assertEquals("ruhig", detail.manualStateKey)

        val snapshotPanel = detail.panelStates.first { it.panel == StartAreaPanel.Snapshot }
        assertEquals("Reflexion · Heute Ruhig", snapshotPanel.summary)
        assertEquals("Erholung", snapshotPanel.screenState.infoLabel)
        assertEquals("Erholungslage", snapshotPanel.screenState.core.title)

        val pathPanel = detail.panelStates.first { it.panel == StartAreaPanel.Path }
        assertEquals("Heute Pausen schuetzen", pathPanel.summary)
        assertEquals("Regenerationsfokus", pathPanel.screenState.actions.first { it.id == StartPanelActionId.PathFocus }.label)

        val flowPanel = detail.panelStates.first { it.panel == StartAreaPanel.Options }
        assertEquals("Stabil · Pause · Wiederholung", flowPanel.summary)
        assertEquals("Stabil", flowPanel.screenState.metrics[1].value)
    }
}
