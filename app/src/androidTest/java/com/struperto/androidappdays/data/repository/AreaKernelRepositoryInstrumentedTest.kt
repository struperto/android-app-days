package com.struperto.androidappdays.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.data.local.SingleDatabase
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaVisibilityLevel
import com.struperto.androidappdays.domain.area.withUpdatedIdentity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AreaKernelRepositoryInstrumentedTest {
    private lateinit var database: SingleDatabase
    private lateinit var lifeWheelRepository: LifeWheelRepository
    private lateinit var lifeAreaProfileRepository: LifeAreaProfileRepository
    private lateinit var areaKernelRepository: AreaKernelRepository

    private val clock = Clock.fixed(
        Instant.parse("2026-03-11T08:00:00Z"),
        ZoneId.of("Europe/Berlin"),
    )

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            SingleDatabase::class.java,
        ).allowMainThreadQueries().build()

        lifeWheelRepository = RoomLifeWheelRepository(
            database = database,
            lifeWheelDao = database.lifeWheelDao(),
            areaKernelDao = database.areaKernelDao(),
            clock = clock,
        )
        lifeAreaProfileRepository = RoomLifeAreaProfileRepository(
            areaKernelDao = database.areaKernelDao(),
            clock = clock,
        )
        areaKernelRepository = LegacyBackedAreaKernelRepository(
            lifeWheelRepository = lifeWheelRepository,
            lifeAreaProfileRepository = lifeAreaProfileRepository,
            areaKernelDao = database.areaKernelDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun kernelRepository_roundTripsActiveInstanceAndSnapshotThroughRoomKernelStorage() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        val original = areaKernelRepository.loadActiveInstances().first { it.areaId == "vitality" }
        areaKernelRepository.updateActiveInstance(
            original.copy(
                title = "Meine Vitalitaet",
                summary = "Schlaf, Essen und Bewegung bewusst halten.",
                targetScore = 2,
                cadenceKey = "weekly",
                selectedTracks = setOf("Schlaf"),
                signalBlend = 35,
                intensity = 5,
                remindersEnabled = true,
                reviewEnabled = false,
                experimentsEnabled = true,
                authoringConfig = original.authoringConfig.copy(
                    complexityLevel = AreaComplexityLevel.EXPERT,
                    visibilityLevel = AreaVisibilityLevel.Expanded,
                ),
            ),
        )
        areaKernelRepository.upsertSnapshot(
            snapshot = com.struperto.androidappdays.domain.area.AreaSnapshot(
                areaId = "vitality",
                date = LocalDate.parse("2026-03-11"),
                manualScore = 4,
            ),
        )

        val vitality = areaKernelRepository.observeActiveInstances().first().first { it.areaId == "vitality" }
        val snapshot = areaKernelRepository.observeSnapshots(LocalDate.parse("2026-03-11")).first()
            .first { it.areaId == "vitality" }

        assertEquals("Meine Vitalitaet", vitality.title)
        assertEquals("Schlaf, Essen und Bewegung bewusst halten.", vitality.summary)
        assertEquals(2, vitality.targetScore)
        assertEquals("vitality", vitality.definitionId)
        assertEquals("weekly", vitality.cadenceKey)
        assertEquals(setOf("Schlaf"), vitality.selectedTracks)
        assertTrue(vitality.remindersEnabled)
        assertEquals("score", vitality.profileConfig.lageMode.persistedValue)
        assertEquals("signals", vitality.profileConfig.sourcesMode.persistedValue)
        assertEquals(AreaComplexityLevel.EXPERT, vitality.authoringConfig.complexityLevel)
        assertEquals(AreaVisibilityLevel.Expanded, vitality.authoringConfig.visibilityLevel)
        assertEquals(4, snapshot.manualScore)
    }

    @Test
    fun kernelRepository_roundTripsStateSnapshotThroughRoomKernelStorage() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()
        areaKernelRepository.upsertSnapshot(
            snapshot = com.struperto.androidappdays.domain.area.AreaSnapshot(
                areaId = "friends",
                date = LocalDate.parse("2026-03-11"),
                manualStateKey = "warm",
                confidence = 0.9f,
            ),
        )

        val snapshot = areaKernelRepository.observeSnapshots(LocalDate.parse("2026-03-11")).first()
            .first { it.areaId == "friends" }

        assertEquals(null, snapshot.manualScore)
        assertEquals("warm", snapshot.manualStateKey)
        assertEquals(0.9f, snapshot.confidence)
    }

    @Test
    fun kernelRepository_reflectsDeleteCleanupForInstancesAndSnapshots() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()
        areaKernelRepository.upsertSnapshot(
            snapshot = com.struperto.androidappdays.domain.area.AreaSnapshot(
                areaId = "vitality",
                date = LocalDate.parse("2026-03-11"),
                manualScore = 3,
            ),
        )

        areaKernelRepository.deleteActiveInstance("vitality")

        assertTrue(areaKernelRepository.observeActiveInstances().first().none { it.areaId == "vitality" })
        assertTrue(
            areaKernelRepository
                .observeSnapshots(LocalDate.parse("2026-03-11"))
                .first()
                .none { it.areaId == "vitality" },
        )
        assertTrue(
            lifeAreaProfileRepository.observeProfiles().first().none { it.areaId == "vitality" },
        )
    }

    @Test
    fun kernelRepository_runsCreateAndReorderLifecycleThroughRoomKernelStorage() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        val created = areaKernelRepository.createActiveInstance(
            CreateAreaInstanceDraft(
                title = "Podcast Ideen",
                summary = "Ideen festhalten und pruefen.",
                templateId = "free",
                iconKey = "spark",
                behaviorClass = AreaBehaviorClass.REFLECTION,
            ),
        )
        areaKernelRepository.moveActiveInstanceLater(created.areaId)
        areaKernelRepository.moveActiveInstanceEarlier(created.areaId)

        val activeBeforeSwap = areaKernelRepository.loadActiveInstances()
        areaKernelRepository.swapActiveInstanceOrder(
            firstAreaId = activeBeforeSwap[0].areaId,
            secondAreaId = activeBeforeSwap[1].areaId,
        )

        val activeAfterSwap = areaKernelRepository.loadActiveInstances()
        assertEquals(created.areaId, activeBeforeSwap.first().areaId)
        assertEquals("template:free", created.definitionId)
        assertEquals("adaptive", created.cadenceKey)
        assertTrue(created.selectedTracks.isEmpty())
        assertEquals("state", created.profileConfig.lageMode.persistedValue)
        assertEquals("curated", created.profileConfig.sourcesMode.persistedValue)
        assertEquals(AreaComplexityLevel.ADVANCED, created.authoringConfig.complexityLevel)
        assertEquals(AreaVisibilityLevel.Standard, created.authoringConfig.visibilityLevel)
        assertEquals(
            created.areaId,
            lifeAreaProfileRepository.observeProfiles().first().first { it.areaId == created.areaId }.areaId,
        )
        assertEquals(activeBeforeSwap[1].areaId, activeAfterSwap.first().areaId)
        assertTrue(activeAfterSwap.any { it.areaId == created.areaId })
    }

    @Test
    fun kernelRepository_rebasesTemplateBackedAuthoringBasisThroughRoomKernelStorage() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        val created = areaKernelRepository.createActiveInstance(
            CreateAreaInstanceDraft(
                title = "Podcast Ideen",
                summary = "Ideen festhalten und pruefen.",
                templateId = "free",
                iconKey = "spark",
                behaviorClass = AreaBehaviorClass.REFLECTION,
            ),
        )
        areaKernelRepository.updateActiveInstance(
            created.withUpdatedIdentity(
                title = "Projektbereich",
                summary = "Arbeit klarer ziehen.",
                templateId = "project",
                iconKey = "briefcase",
            ),
        )

        val reloaded = areaKernelRepository.loadActiveInstances().first { it.areaId == created.areaId }

        assertEquals("template:project", reloaded.definitionId)
        assertEquals("project", reloaded.templateId)
        assertEquals("Projektbereich", reloaded.title)
        assertEquals("Arbeit klarer ziehen.", reloaded.summary)
        assertEquals("briefcase", reloaded.iconKey)
        assertEquals(AreaComplexityLevel.BASIC, reloaded.authoringConfig.complexityLevel)
        assertEquals(AreaVisibilityLevel.Focused, reloaded.authoringConfig.visibilityLevel)
        assertEquals("focus", reloaded.authoringConfig.directionMode.persistedValue)
        assertEquals("active", reloaded.authoringConfig.flowProfile.persistedValue)
    }

    @Test
    fun deletedSeedArea_staysInactiveWhenSeedingRunsAgain() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        areaKernelRepository.deleteActiveInstance("vitality")
        lifeWheelRepository.ensureSeededAreas()

        val activeIds = areaKernelRepository.loadActiveInstances().map { it.areaId }
        assertTrue("vitality" !in activeIds)
    }

    @Test
    fun ensureStartBootstrap_alignsProfilesAndLegacySetupRecord() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()
        val vitality = lifeWheelRepository.loadActiveAreas().first { it.id == "vitality" }
        lifeWheelRepository.deleteArea("clarity")

        val state = areaKernelRepository.ensureStartBootstrap()
        val activeAreas = lifeWheelRepository.loadActiveAreas()
        val activeInstances = areaKernelRepository.loadActiveInstances()
        val profiles = lifeAreaProfileRepository.observeProfiles().first()

        assertTrue(state.isBootstrapped)
        assertEquals(activeAreas.size, state.activeInstanceCount)
        assertTrue(state.missingProfileAreaIds.isEmpty())
        assertTrue(state.hasLegacySetupRecord)
        assertTrue(activeAreas.none { it.id == "clarity" })
        assertTrue(activeInstances.none { it.areaId == "clarity" })
        assertEquals(
            vitality.id,
            profiles.first { it.areaId == vitality.id }.areaId,
        )
        assertTrue(
            profiles.none { it.areaId == "clarity" },
        )
    }
}
