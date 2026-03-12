package com.struperto.androidappdays.domain.area

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaTodayOutputProjectorTest {
    private val logicalDate = LocalDate.of(2026, 3, 12)
    private val referenceTime = Instant.parse("2026-03-12T08:00:00Z")

    @Test
    fun manual_truth_beats_confirmed_step_and_local_evidence() {
        val output = projectAreaTodayOutput(
            seedInput(
                definitionId = "impact",
                confirmedStep = AreaNextMeaningfulStep(
                    kind = AreaStepKind.do_step,
                    label = "Ticket X abschliessen",
                    status = AreaStepStatus.READY,
                    origin = AreaStepOrigin.manual,
                    isUserConfirmed = true,
                    fallbackLabel = "Ticket X abschliessen",
                ),
                snapshot = AreaSnapshot(
                    areaId = "impact",
                    date = logicalDate,
                    manualScore = 2,
                    manualNote = "Blocker sitzt noch im Review.",
                    freshnessAt = referenceTime,
                ),
                openPlanTitles = listOf("Ticket X abschliessen"),
                dueCount = 1,
            ),
        )

        assertEquals("2/5", output.statusLabel)
        assertEquals("Ticket X abschliessen", output.nextMeaningfulStep.label)
        assertEquals(AreaSourceTruth.manual_plus_local, output.sourceTruth)
        assertFalse(output.isEmptyState)
    }

    @Test
    fun relationship_seed_without_anchor_stays_empty_and_keeps_recommendation_distinct() {
        val output = projectAreaTodayOutput(
            seedInput(definitionId = "friends"),
        )

        assertTrue(output.isEmptyState)
        assertEquals(AreaBehaviorClass.RELATIONSHIP, output.behaviorClass)
        assertEquals("Leise", output.statusLabel)
        assertEquals(AreaStepKind.contact, output.nextMeaningfulStep.kind)
        assertNotEquals(output.recommendation, output.nextMeaningfulStep.label)
    }

    @Test
    fun maintenance_seed_needs_real_anchor_not_just_rhythm() {
        val output = projectAreaTodayOutput(
            seedInput(
                definitionId = "home",
                cadenceKey = "weekly",
                reviewEnabled = true,
            ),
        )

        assertTrue(output.isEmptyState)
        assertEquals("Offen", output.statusLabel)
        assertEquals(AreaStepKind.maintain, output.nextMeaningfulStep.kind)
    }

    @Test
    fun maintenance_seed_uses_plan_anchor_for_erhaltungszug() {
        val output = projectAreaTodayOutput(
            seedInput(
                definitionId = "home",
                openPlanTitles = listOf("Bad putzen"),
                dueCount = 1,
                updatedAt = referenceTime,
            ),
        )

        assertFalse(output.isEmptyState)
        assertEquals(AreaSourceTruth.local_derived, output.sourceTruth)
        assertEquals("Bad putzen", output.nextMeaningfulStep.label)
        assertEquals("Pflegepunkt sichtbar", output.statusLabel)
        assertNotEquals(output.recommendation, output.nextMeaningfulStep.label)
    }

    @Test
    fun reflection_manual_note_becomes_real_signal_without_collapsing_into_step() {
        val output = projectAreaTodayOutput(
            seedInput(
                definitionId = "recovery",
                snapshot = AreaSnapshot(
                    areaId = "recovery",
                    date = logicalDate,
                    manualNote = "Heute mehr Stille als Output.",
                    freshnessAt = referenceTime,
                ),
            ),
        )

        assertFalse(output.isEmptyState)
        assertEquals("Lesepunkt notiert", output.statusLabel)
        assertTrue(output.evidenceSummary.contains("Heute mehr Stille als Output."))
        assertNotEquals(output.recommendation, output.nextMeaningfulStep.label)
    }

    @Test
    fun six_core_seeds_expose_expected_empty_contracts() {
        val expectations = listOf(
            SeedExpectation("vitality", AreaBehaviorClass.TRACKING, AreaStepKind.observe, AreaSeverity.LOW),
            SeedExpectation("impact", AreaBehaviorClass.PROGRESS, AreaStepKind.do_step, AreaSeverity.MEDIUM),
            SeedExpectation("friends", AreaBehaviorClass.RELATIONSHIP, AreaStepKind.contact, AreaSeverity.LOW),
            SeedExpectation("home", AreaBehaviorClass.MAINTENANCE, AreaStepKind.maintain, AreaSeverity.LOW),
            SeedExpectation("clarity", AreaBehaviorClass.PROTECTION, AreaStepKind.protect, AreaSeverity.MEDIUM),
            SeedExpectation("recovery", AreaBehaviorClass.REFLECTION, AreaStepKind.reflect, AreaSeverity.NEUTRAL),
        )

        expectations.forEach { expectation ->
            val output = projectAreaTodayOutput(seedInput(definitionId = expectation.definitionId))
            assertTrue("Expected ${expectation.definitionId} to start in empty state", output.isEmptyState)
            assertEquals(expectation.behaviorClass, output.behaviorClass)
            assertEquals(expectation.stepKind, output.nextMeaningfulStep.kind)
            assertEquals(expectation.severity, output.severity)
        }
    }

    private fun seedInput(
        definitionId: String,
        snapshot: AreaSnapshot? = null,
        confirmedStep: AreaNextMeaningfulStep? = null,
        openPlanTitles: List<String> = emptyList(),
        dueCount: Int = 0,
        cadenceKey: String = "weekly",
        reviewEnabled: Boolean = true,
        updatedAt: Instant? = null,
    ): AreaTodayOutputInput {
        val definition = requireNotNull(startAreaKernelDefinition(definitionId))
        val blueprint = requireNotNull(startAreaKernelBlueprint(definitionId))
        val instance = AreaInstance(
            areaId = definitionId,
            title = definition.title,
            summary = blueprint.summary,
            iconKey = definition.iconKey,
            targetScore = definition.defaultConfig.targetScore,
            sortOrder = 0,
            isActive = true,
            cadenceKey = cadenceKey,
            selectedTracks = blueprint.trackLabels.take(2).toSet(),
            signalBlend = 60,
            intensity = 3,
            remindersEnabled = false,
            reviewEnabled = reviewEnabled,
            experimentsEnabled = false,
            profileConfig = defaultAreaProfileConfig(definition = definition),
            templateId = blueprint.defaultTemplateId,
            definitionId = definitionId,
            confirmedNextStep = confirmedStep,
            updatedAt = updatedAt,
        )
        return AreaTodayOutputInput(
            definition = definition,
            blueprint = blueprint,
            instance = instance,
            snapshot = snapshot,
            generatedAt = referenceTime,
            logicalDate = logicalDate,
            openPlanTitles = openPlanTitles,
            dueCount = dueCount,
        )
    }

    private data class SeedExpectation(
        val definitionId: String,
        val behaviorClass: AreaBehaviorClass,
        val stepKind: AreaStepKind,
        val severity: AreaSeverity,
    )
}
