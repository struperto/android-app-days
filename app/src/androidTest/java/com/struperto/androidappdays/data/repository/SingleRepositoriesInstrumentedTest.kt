package com.struperto.androidappdays.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.data.local.AreaInstanceEntity
import com.struperto.androidappdays.data.local.SingleDatabase
import java.time.Clock
import java.time.Instant
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
class SingleRepositoriesInstrumentedTest {
    private lateinit var database: SingleDatabase
    private lateinit var lifeWheelRepository: LifeWheelRepository
    private lateinit var lifeAreaProfileRepository: LifeAreaProfileRepository
    private lateinit var captureRepository: CaptureRepository
    private lateinit var learningEventRepository: LearningEventRepository
    private lateinit var vorhabenRepository: VorhabenRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var userFingerprintRepository: UserFingerprintRepository
    private lateinit var areaSourceBindingRepository: AreaSourceBindingRepository

    private val clock = Clock.fixed(
        Instant.parse("2026-03-07T09:00:00Z"),
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
        captureRepository = RoomCaptureRepository(
            captureItemDao = database.captureItemDao(),
            clock = clock,
        )
        learningEventRepository = RoomLearningEventRepository(
            dao = database.learningEventDao(),
            clock = clock,
        )
        vorhabenRepository = RoomVorhabenRepository(
            vorhabenDao = database.vorhabenDao(),
            clock = clock,
        )
        planRepository = RoomPlanRepository(
            planItemDao = database.planItemDao(),
            vorhabenDao = database.vorhabenDao(),
            clock = clock,
        )
        userFingerprintRepository = RoomUserFingerprintRepository(
            dao = database.userFingerprintDao(),
            lifeWheelDao = database.lifeWheelDao(),
            learningEventDao = database.learningEventDao(),
            clock = clock,
        )
        areaSourceBindingRepository = RoomAreaSourceBindingRepository(
            dao = database.areaSourceBindingDao(),
            clock = clock,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun captureToVorhabenToPlan_flowWorksOffline() = runBlocking {
        lifeWheelRepository.completeSetup(
            listOf(
                LifeArea("health", "Gesundheit", "Energie und Bewegung", 4, 0, true),
                LifeArea("focus", "Fokus", "Bei der Sache bleiben", 5, 1, true),
                LifeArea("work", "Arbeit", "Arbeit sinnvoll bewegen", 4, 2, true),
                LifeArea("relationships", "Beziehungen", "Menschen bewusst pflegen", 4, 3, true),
                LifeArea("recovery", "Erholung", "Runterkommen und auftanken", 3, 4, true),
            ),
        )

        assertTrue(lifeWheelRepository.observeSetupState().first().isLifeWheelConfigured)
        assertEquals(5, lifeWheelRepository.observeActiveAreas().first().size)

        val capture = captureRepository.createTextCapture(
            text = "Rechnung prüfen und Mail beantworten",
            areaId = "work",
        )
        assertEquals(1, captureRepository.observeOpen().first().size)

        val vorhaben = vorhabenRepository.createFromCapture(
            captureId = capture.id,
            title = "Rechnung klären",
            note = "Mail an den Anbieter beantworten",
            areaId = "work",
        )
        captureRepository.markConverted(capture.id)

        assertTrue(captureRepository.observeOpen().first().isEmpty())
        assertEquals(1, vorhabenRepository.observeActive().first().size)

        planRepository.addFromVorhaben(vorhaben.id, TimeBlock.MORGEN)
        val todayItems = planRepository.observeToday("2026-03-07").first()
        assertEquals(1, todayItems.size)
        assertEquals("Rechnung klären", todayItems.first().title)
        assertEquals("work", todayItems.first().areaId)
        assertEquals(TimeBlock.MORGEN, todayItems.first().timeBlock)

        planRepository.toggleDone(todayItems.first().id)
        val doneItems = planRepository.observeToday("2026-03-07").first()
        assertTrue(doneItems.first().isDone)

        lifeWheelRepository.upsertDailyCheck(
            areaId = "work",
            date = "2026-03-07",
            manualScore = 4,
        )
        assertEquals(1, lifeWheelRepository.observeDailyChecks("2026-03-07").first().size)

        lifeWheelRepository.upsertDailyCheck(
            areaId = "work",
            date = "2026-03-07",
            manualScore = null,
        )
        assertTrue(lifeWheelRepository.observeDailyChecks("2026-03-07").first().isEmpty())

        planRepository.removeFromToday(doneItems.first().id)
        assertTrue(planRepository.observeToday("2026-03-07").first().isEmpty())
    }

    @Test
    fun fingerprintAndLearningEvents_persistLocally() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        userFingerprintRepository.save(
            UserFingerprintDraft(
                rolesText = "Produkt\nGruender",
                responsibilitiesText = "Richtung halten",
                priorityRulesText = "Strategieblock",
                weeklyRhythm = "Werktage fokussiert",
                recurringCommitmentsText = "Kalender zuerst lesen",
                goodDayPattern = "Klarer Start",
                badDayPattern = "Zu viele Eingaenge",
                dayStartHour = 6,
                dayEndHour = 22,
                morningEnergy = 4,
                afternoonEnergy = 3,
                eveningEnergy = 2,
                focusStrength = 4,
                disruptionSensitivity = 3,
                recoveryNeed = 4,
            ),
        )
        learningEventRepository.record(
            type = LearningEventType.FINGERPRINT_SAVED,
            title = "Fingerprint aktualisiert",
            detail = "Strategieblock",
        )
        learningEventRepository.record(
            type = LearningEventType.PLAN_SAVED,
            title = "Planpunkt angelegt",
            detail = "Strategieblock",
        )

        val fingerprint = userFingerprintRepository.observe().first()
        assertEquals(listOf("Produkt", "Gruender"), fingerprint.roles)
        assertEquals("Strategieblock", fingerprint.priorityRules.first())
        assertEquals(1, learningEventRepository.observeDiscoveryDayCount().first())
        assertEquals(2, learningEventRepository.observeRecent().first().size)
    }

    @Test
    fun deletedSeedArea_isNotRecreatedByEnsureSeededAreas() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        lifeWheelRepository.deleteArea("vitality")
        assertTrue(lifeWheelRepository.observeActiveAreas().first().none { it.id == "vitality" })

        lifeWheelRepository.ensureSeededAreas()

        assertTrue(lifeWheelRepository.observeActiveAreas().first().none { it.id == "vitality" })
    }

    @Test
    fun areaSourceBinding_roundTripsThroughRoomRepository() = runBlocking {
        areaSourceBindingRepository.bind(
            areaId = "home",
            source = com.struperto.androidappdays.domain.DataSourceKind.CALENDAR,
        )

        val bindings = areaSourceBindingRepository.observeByArea("home").first()

        assertEquals(1, bindings.size)
        assertEquals("home", bindings.single().areaId)
        assertEquals(com.struperto.androidappdays.domain.DataSourceKind.CALENDAR, bindings.single().source)
    }

    @Test
    fun editedSeedArea_isNotOverwrittenByEnsureSeededAreas() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        lifeWheelRepository.updateArea(
            id = "vitality",
            label = "Eigene Vitalitaet",
            definition = "Schlaf und Energie mit eigener Sprache pflegen",
            targetScore = 2,
        )

        lifeWheelRepository.ensureSeededAreas()

        val vitality = lifeWheelRepository.observeActiveAreas().first().first { it.id == "vitality" }
        assertEquals("Eigene Vitalitaet", vitality.label)
        assertEquals("Schlaf und Energie mit eigener Sprache pflegen", vitality.definition)
        assertEquals(2, vitality.targetScore)
    }

    @Test
    fun ensureSeededAreas_marksSetupConfiguredWhenLegacyAreasAlreadyExist() = runBlocking {
        database.areaKernelDao().upsertAreaInstance(
            AreaInstanceEntity(
                areaId = "custom",
                definitionId = "template:free",
                title = "Eigener Bereich",
                summary = "Bestehende Kernel-Instanz",
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
                lageMode = "state",
                directionMode = "balanced",
                sourcesMode = "curated",
                flowProfile = "stable",
                authoringComplexity = "ADVANCED",
                authoringVisibility = "standard",
                templateId = "free",
                createdAt = clock.millis(),
                updatedAt = clock.millis(),
            ),
        )

        lifeWheelRepository.ensureSeededAreas()

        val setupState = lifeWheelRepository.loadSetupState()
        val custom = lifeWheelRepository.observeActiveAreas().first().first { it.id == "custom" }

        assertTrue(setupState.isLifeWheelConfigured)
        assertEquals("Eigener Bereich", custom.label)
        assertEquals("Bestehende Kernel-Instanz", custom.definition)
        assertEquals("free", custom.templateId)
    }

    @Test
    fun deletingArea_cleansProfilesAndDailyChecks() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()
        lifeAreaProfileRepository.saveProfile(
            LifeAreaProfile(
                areaId = "vitality",
                cadence = "adaptive",
                intensity = 3,
                signalBlend = 60,
                selectedTracks = setOf("Schlaf", "Energie"),
                remindersEnabled = true,
                reviewEnabled = true,
                experimentsEnabled = false,
            ),
        )
        lifeWheelRepository.upsertDailyCheck(
            areaId = "vitality",
            date = "2026-03-07",
            manualScore = 4,
        )

        lifeWheelRepository.deleteArea("vitality")

        assertTrue(lifeWheelRepository.observeActiveAreas().first().none { it.id == "vitality" })
        assertTrue(lifeWheelRepository.observeDailyChecks("2026-03-07").first().none { it.areaId == "vitality" })
        assertTrue(lifeAreaProfileRepository.observeProfiles().first().none { it.areaId == "vitality" })
    }

    @Test
    fun movingAreaEarlierAndLater_reordersActiveAreas() = runBlocking {
        lifeWheelRepository.ensureSeededAreas()

        lifeWheelRepository.moveAreaLater("vitality")
        val afterLater = lifeWheelRepository.observeActiveAreas().first().take(3).map { it.id }
        assertEquals(listOf("clarity", "vitality", "impact"), afterLater)

        lifeWheelRepository.moveAreaEarlier("vitality")
        val afterEarlier = lifeWheelRepository.observeActiveAreas().first().take(3).map { it.id }
        assertEquals(listOf("vitality", "clarity", "impact"), afterEarlier)
    }
}
