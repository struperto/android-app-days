package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaDailyCheck
import com.struperto.androidappdays.data.repository.LifeAreaProfile
import com.struperto.androidappdays.domain.LifeDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartStateModelTest {
    @Test
    fun overviewState_marksAreasWithoutDailyCheckAsWaiting() {
        val state = mapStartOverviewState(
            areas = listOf(
                LifeArea(
                    id = "vitality",
                    label = "Vitalitaet",
                    definition = "Energie halten",
                    targetScore = 4,
                    sortOrder = 0,
                    isActive = true,
                    templateId = "ritual",
                    iconKey = "heart",
                ),
            ),
            dailyChecks = emptyList(),
        )

        val tile = state.areas.single()
        assertEquals(StartAreaStatusKind.Waiting, tile.statusKind)
        assertEquals("Signal 4/5", tile.statusLabel)
        assertEquals("Schlaf", tile.focusLabel)
        assertEquals("Schlaf · Stabil", tile.profileLabel)
        assertEquals(0.18f, tile.progress)
        assertEquals("Energie halten", tile.summary)
        assertEquals("ritual", tile.templateId)
    }

    @Test
    fun overviewState_usesCentralStatusAndProgressMapping() {
        val state = mapStartOverviewState(
            areas = listOf(
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
            ),
            dailyChecks = listOf(
                LifeAreaDailyCheck(
                    areaId = "impact",
                    date = "2026-03-11",
                    manualScore = 3,
                ),
            ),
        )

        val tile = state.areas.single()
        assertEquals(StartAreaStatusKind.Pull, tile.statusKind)
        assertEquals("Prioritaet 3/4", tile.statusLabel)
        assertEquals(0.75f, tile.progress)
    }

    @Test
    fun detailState_buildsPanelSummariesFromCentralBlueprint() {
        val detail = mapStartAreaDetailState(
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
            manualScore = 4,
            profile = null,
        )

        assertEquals(StartAreaStatusKind.Stable, detail.statusKind)
        assertEquals("4/5", detail.statusLabel)
        assertEquals(listOf("Tagesform", "Heute", "Signale", "Rhythmus"), detail.panelStates.map { it.title })
        assertEquals("Status", detail.panelStates.first { it.panel == StartAreaPanel.Snapshot }.countLabel)
        assertEquals("Ausbalanciert", detail.panelStates.first { it.panel == StartAreaPanel.Path }.countLabel)
        assertEquals("Signalnah", detail.panelStates.first { it.panel == StartAreaPanel.Sources }.countLabel)
        assertEquals("Stabil", detail.panelStates.first { it.panel == StartAreaPanel.Options }.countLabel)
        assertEquals("Status", detail.profileState.lageLabel)
        assertEquals("Ausbalanciert", detail.profileState.directionLabel)
        assertEquals("Signalnah", detail.profileState.sourcesLabel)
        assertEquals("Stabil", detail.profileState.flowLabel)
        assertTrue(detail.blueprint.domains.contains(LifeDomain.SLEEP))
        assertEquals(setOf(LifeDomain.SLEEP, LifeDomain.MOVEMENT, LifeDomain.HYDRATION, LifeDomain.NUTRITION, LifeDomain.HEALTH), startAreaDomains("vitality"))
    }

    @Test
    fun detailState_buildsUnifiedPanelActionsAndFocusTrack() {
        val detail = mapStartAreaDetailState(
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
            manualScore = 2,
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
        )

        assertEquals("Projekte", detail.focusTrack)
        assertEquals("Rhythmus", detail.profileState.directionLabel)
        assertEquals("Aktiv", detail.profileState.flowLabel)
        val pathPanel = detail.panelStates.first { it.panel == StartAreaPanel.Path }
        assertEquals("Projekte", pathPanel.screenState.core.value)
        assertEquals(listOf("Richtungsmodus", "Soll", "Takt", "Arbeitsfokus"), pathPanel.screenState.actions.map { it.label })
        assertEquals("Woechentlich", pathPanel.screenState.actions.first { it.id == StartPanelActionId.PathCadence }.valueLabel)
        assertEquals("Diese Woche Projekte voranbringen", pathPanel.summary)
        assertEquals("Diese Woche Projekte voranbringen", pathPanel.screenState.core.caption)
        assertEquals("Rhythmus", pathPanel.screenState.metrics[1].value)
        assertEquals("Diese Woche Projekte voranbringen", pathPanel.screenState.metrics.last().value)

        val flowPanel = detail.panelStates.first { it.panel == StartAreaPanel.Options }
        assertEquals("Aktiv", flowPanel.screenState.headerLabel)
        assertEquals(listOf("Flowprofil", "Zug", "Impulse"), flowPanel.screenState.actions.map { it.label })
        assertEquals(
            "Heute zieht Startsignal und Review den Bereich deutlich nach vorn.",
            flowPanel.screenState.effectLabel,
        )
        assertEquals(listOf("Startsignal", "Review", "Absicherung"), flowPanel.screenState.actions
            .first { it.id == StartPanelActionId.FlowSwitches }
            .sheet
            ?.options
            ?.map { it.label })
        assertEquals("Aktiv · Startsignal · Review", flowPanel.summary)
        assertEquals("2 aktiv", flowPanel.screenState.core.value)
        assertEquals("Aktiv · Startsignal · Review", flowPanel.screenState.metrics.first().value)
        assertEquals("Aktiv", flowPanel.screenState.metrics[1].value)

        val sourcesPanel = detail.panelStates.first { it.panel == StartAreaPanel.Sources }
        assertEquals("Projekte · 75% Signal", sourcesPanel.summary)
        assertEquals(
            "Heute fuehren Signale rund um Projekte die Lesart.",
            sourcesPanel.screenState.effectLabel,
        )
        assertEquals("Signalnah", sourcesPanel.screenState.actions.first { it.id == StartPanelActionId.SourcesMode }.valueLabel)
    }

    @Test
    fun overviewState_marksMoveBoundariesForManageDock() {
        val state = mapStartOverviewState(
            areas = listOf(
                LifeArea("first", "Erster", "A", 3, 0, true, "ritual", "heart"),
                LifeArea("second", "Zweiter", "B", 3, 1, true, "ritual", "heart"),
                LifeArea("third", "Dritter", "C", 3, 2, true, "ritual", "heart"),
            ),
            dailyChecks = emptyList(),
        )

        assertEquals(false, state.areas[0].canMoveEarlier)
        assertEquals(true, state.areas[0].canMoveLater)
        assertEquals(true, state.areas[1].canMoveEarlier)
        assertEquals(true, state.areas[1].canMoveLater)
        assertEquals(true, state.areas[2].canMoveEarlier)
        assertEquals(false, state.areas[2].canMoveLater)
    }
}
