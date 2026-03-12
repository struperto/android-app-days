package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaProfileConfig
import com.struperto.androidappdays.domain.area.AreaSnapshot
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaKernelRepositoryTest {
    @Test
    fun observeActiveInstances_combinesLegacyAreasProfilesAndKernelDefaults() = runBlocking {
        val lifeWheelRepository = FakeLifeWheelRepository(
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
                LifeArea(
                    id = "custom",
                    label = "Eigener Bereich",
                    definition = "Eigene Beschreibung",
                    targetScore = 2,
                    sortOrder = 1,
                    isActive = true,
                    templateId = "free",
                    iconKey = "spark",
                ),
            ),
        )
        val profileRepository = FakeLifeAreaProfileRepository(
            profiles = listOf(
                LifeAreaProfile(
                    areaId = "vitality",
                    cadence = "daily",
                    intensity = 4,
                    signalBlend = 75,
                    selectedTracks = setOf("Schlaf", "Energie"),
                    remindersEnabled = true,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                ),
            ),
        )

        val repository = LegacyBackedAreaKernelRepository(
            lifeWheelRepository = lifeWheelRepository,
            lifeAreaProfileRepository = profileRepository,
        )

        val instances = repository.observeActiveInstances().first()

        val vitality = instances.first { it.areaId == "vitality" }
        assertEquals("vitality", vitality.definitionId)
        assertEquals("daily", vitality.cadenceKey)
        assertEquals(setOf("Schlaf", "Energie"), vitality.selectedTracks)
        assertTrue(vitality.remindersEnabled)
        assertEquals("score", vitality.profileConfig.lageMode.persistedValue)
        assertEquals("signals", vitality.profileConfig.sourcesMode.persistedValue)
        assertEquals(
            com.struperto.androidappdays.domain.area.AreaComplexityLevel.BASIC,
            vitality.authoringConfig.complexityLevel,
        )

        val custom = instances.first { it.areaId == "custom" }
        assertEquals("template:free", custom.definitionId)
        assertEquals("adaptive", custom.cadenceKey)
        assertTrue(custom.selectedTracks.isEmpty())
        assertEquals("Eigene Beschreibung", custom.summary)
        assertEquals("state", custom.profileConfig.lageMode.persistedValue)
        assertEquals(
            com.struperto.androidappdays.domain.area.AreaVisibilityLevel.Standard,
            custom.authoringConfig.visibilityLevel,
        )
    }

    @Test
    fun loadActiveInstances_usesSeedDefinitionDefaultsWhenProfileIsMissing() = runBlocking {
        val repository = LegacyBackedAreaKernelRepository(
            lifeWheelRepository = FakeLifeWheelRepository(
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
            ),
            lifeAreaProfileRepository = FakeLifeAreaProfileRepository(),
        )

        val instance = repository.loadActiveInstances().single()

        assertEquals("adaptive", instance.cadenceKey)
        assertEquals(setOf("Schlaf", "Energie"), instance.selectedTracks)
        assertTrue(instance.reviewEnabled)
        assertFalse(instance.remindersEnabled)
        assertEquals("signals", instance.profileConfig.sourcesMode.persistedValue)
        assertEquals("stable", instance.profileConfig.flowProfile.persistedValue)
    }

    @Test
    fun startLifecycleOperations_delegateThroughKernelFacade() = runBlocking {
        val lifeWheelRepository = FakeLifeWheelRepository(
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
                LifeArea(
                    id = "clarity",
                    label = "Fokus",
                    definition = "Fokus und Ruhe bewusst halten.",
                    targetScore = 4,
                    sortOrder = 1,
                    isActive = true,
                    templateId = "theme",
                    iconKey = "focus",
                ),
            ),
        )
        val profileRepository = FakeLifeAreaProfileRepository()
        val repository = LegacyBackedAreaKernelRepository(
            lifeWheelRepository = lifeWheelRepository,
            lifeAreaProfileRepository = profileRepository,
        )

        val created = repository.createActiveInstance(
            CreateAreaInstanceDraft(
                title = "Podcast Ideen",
                summary = "Ideen festhalten und pruefen.",
                templateId = "free",
                iconKey = "spark",
                behaviorClass = AreaBehaviorClass.REFLECTION,
            ),
        )
        repository.moveActiveInstanceLater("vitality")
        repository.moveActiveInstanceEarlier("clarity")
        repository.swapActiveInstanceOrder(
            firstAreaId = "vitality",
            secondAreaId = "clarity",
        )
        repository.deleteActiveInstance("clarity")

        assertEquals("Podcast Ideen", lifeWheelRepository.createdAreaLabel)
        assertEquals("Ideen festhalten und pruefen.", lifeWheelRepository.createdAreaDefinition)
        assertEquals("free", lifeWheelRepository.createdAreaTemplateId)
        assertEquals("spark", lifeWheelRepository.createdAreaIconKey)
        assertEquals(lifeWheelRepository.createdAreaId, created.areaId)
        assertEquals("Podcast Ideen", created.title)
        assertEquals("Ideen festhalten und pruefen.", created.summary)
        assertEquals("template:free", created.definitionId)
        assertEquals("adaptive", created.cadenceKey)
        assertTrue(created.selectedTracks.isEmpty())
        assertEquals(created.areaId, profileRepository.savedProfile?.areaId)
        assertTrue(profileRepository.savedProfile?.reviewEnabled == true)
        assertEquals("state", profileRepository.savedProfile?.lageMode)
        assertEquals("curated", profileRepository.savedProfile?.sourcesMode)
        assertEquals("vitality", lifeWheelRepository.movedLaterAreaId)
        assertEquals("clarity", lifeWheelRepository.movedEarlierAreaId)
        assertEquals("vitality" to "clarity", lifeWheelRepository.swappedIds)
        assertEquals("clarity", lifeWheelRepository.deletedAreaId)
    }

    @Test
    fun updateActiveInstance_persistsLegacyAreaAndProfileFields() = runBlocking {
        val lifeWheelRepository = FakeLifeWheelRepository()
        val profileRepository = FakeLifeAreaProfileRepository()
        val repository = LegacyBackedAreaKernelRepository(
            lifeWheelRepository = lifeWheelRepository,
            lifeAreaProfileRepository = profileRepository,
        )
        val instance = AreaInstance(
            areaId = "vitality",
            title = "Meine Vitalitaet",
            summary = "Eigene Beschreibung",
            iconKey = "heart",
            targetScore = 2,
            sortOrder = 0,
            isActive = true,
            cadenceKey = "weekly",
            selectedTracks = setOf("Schlaf"),
            signalBlend = 40,
            intensity = 5,
            remindersEnabled = true,
            reviewEnabled = false,
            experimentsEnabled = true,
            profileConfig = AreaProfileConfig(
                lageMode = com.struperto.androidappdays.domain.area.AreaLageMode.State,
                directionMode = com.struperto.androidappdays.domain.area.AreaDirectionMode.Focus,
                sourcesMode = com.struperto.androidappdays.domain.area.AreaSourcesMode.Curated,
                flowProfile = com.struperto.androidappdays.domain.area.AreaFlowProfile.Active,
            ),
            templateId = "ritual",
        )

        repository.updateActiveInstance(instance)

        assertEquals("Meine Vitalitaet", lifeWheelRepository.updatedIdentityLabel)
        assertEquals("Eigene Beschreibung", lifeWheelRepository.updatedIdentityDefinition)
        assertEquals("ritual", lifeWheelRepository.updatedIdentityTemplateId)
        assertEquals("heart", lifeWheelRepository.updatedIdentityIconKey)
        assertEquals(2, lifeWheelRepository.updatedAreaTargetScore)
        assertEquals("weekly", profileRepository.savedProfile?.cadence)
        assertEquals(setOf("Schlaf"), profileRepository.savedProfile?.selectedTracks)
        assertTrue(profileRepository.savedProfile?.remindersEnabled == true)
        assertEquals("state", profileRepository.savedProfile?.lageMode)
        assertEquals("focus", profileRepository.savedProfile?.directionMode)
        assertEquals("curated", profileRepository.savedProfile?.sourcesMode)
        assertEquals("active", profileRepository.savedProfile?.flowProfile)
    }

    @Test
    fun snapshotOperations_mapToLegacyDailyChecksAndIgnoreStateOnlyValuesOnLegacyFallback() = runBlocking {
        val lifeWheelRepository = FakeLifeWheelRepository(
            dailyChecks = mapOf(
                "2026-03-11" to listOf(
                    LifeAreaDailyCheck(
                        areaId = "vitality",
                        date = "2026-03-11",
                        manualScore = 4,
                    ),
                ),
            ),
        )
        val repository = LegacyBackedAreaKernelRepository(
            lifeWheelRepository = lifeWheelRepository,
            lifeAreaProfileRepository = FakeLifeAreaProfileRepository(),
        )

        val snapshots = repository.observeSnapshots(LocalDate.parse("2026-03-11")).first()
        assertEquals(4, snapshots.single().manualScore)

        repository.upsertSnapshot(
            AreaSnapshot(
                areaId = "vitality",
                date = LocalDate.parse("2026-03-11"),
                manualScore = 3,
            ),
        )
        assertEquals(3, lifeWheelRepository.lastUpsertedManualScore)

        repository.clearSnapshot("vitality", LocalDate.parse("2026-03-11"))
        assertEquals(null, lifeWheelRepository.lastUpsertedManualScore)

        repository.upsertSnapshot(
            AreaSnapshot(
                areaId = "vitality",
                date = LocalDate.parse("2026-03-11"),
                manualStateKey = "calm",
            ),
        )

        assertEquals(null, lifeWheelRepository.lastUpsertedManualScore)
    }

    @Test
    fun ensureStartBootstrap_seedsMissingProfilesAndMarksLegacySetup() = runBlocking {
        val lifeWheelRepository = FakeLifeWheelRepository(
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
                LifeArea(
                    id = "custom",
                    label = "Eigener Bereich",
                    definition = "Eigene Beschreibung",
                    targetScore = 3,
                    sortOrder = 1,
                    isActive = true,
                    templateId = "free",
                    iconKey = "spark",
                ),
            ),
            setupState = SingleSetupState(isLifeWheelConfigured = false),
        )
        val profileRepository = FakeLifeAreaProfileRepository(
            profiles = listOf(
                LifeAreaProfile(
                    areaId = "vitality",
                    cadence = "adaptive",
                    intensity = 3,
                    signalBlend = 60,
                    selectedTracks = setOf("Schlaf", "Energie"),
                    remindersEnabled = false,
                    reviewEnabled = true,
                    experimentsEnabled = false,
                ),
            ),
        )
        val repository = LegacyBackedAreaKernelRepository(
            lifeWheelRepository = lifeWheelRepository,
            lifeAreaProfileRepository = profileRepository,
        )

        val state = repository.ensureStartBootstrap()

        assertTrue(state.isBootstrapped)
        assertTrue(state.missingProfileAreaIds.isEmpty())
        assertTrue(lifeWheelRepository.markSetupConfiguredCalled)
        assertEquals(
            setOf(
                AreaKernelPersistenceStore.AREA_INSTANCE_RECORDS,
            ),
            state.persistenceBoundary.activeInstanceStores,
        )
        assertEquals(
            setOf(
                AreaKernelPersistenceStore.AREA_INSTANCE_RECORDS,
                AreaKernelPersistenceStore.LEGACY_SINGLE_SETUP_STATE,
            ),
            state.persistenceBoundary.bootSetupStores,
        )
        assertEquals("custom", profileRepository.savedProfiles.last().areaId)
        assertTrue(profileRepository.savedProfiles.last().reviewEnabled)
        assertEquals("state", profileRepository.savedProfiles.last().lageMode)
    }
}

private class FakeLifeWheelRepository(
    areas: List<LifeArea> = emptyList(),
    dailyChecks: Map<String, List<LifeAreaDailyCheck>> = emptyMap(),
    setupState: SingleSetupState = SingleSetupState(isLifeWheelConfigured = false),
) : LifeWheelRepository {
    private val areaFlow = MutableStateFlow(areas)
    private val dailyCheckFlows = dailyChecks.mapValues { MutableStateFlow(it.value) }.toMutableMap()
    private val setupStateFlow = MutableStateFlow(setupState)

    var updatedIdentityLabel: String? = null
    var updatedIdentityDefinition: String? = null
    var updatedIdentityTemplateId: String? = null
    var updatedIdentityIconKey: String? = null
    var updatedAreaTargetScore: Int? = null
    var lastUpsertedManualScore: Int? = null
    var createdAreaId: String? = null
    var createdAreaLabel: String? = null
    var createdAreaDefinition: String? = null
    var createdAreaTemplateId: String? = null
    var createdAreaIconKey: String? = null
    var deletedAreaId: String? = null
    var movedEarlierAreaId: String? = null
    var movedLaterAreaId: String? = null
    var swappedIds: Pair<String, String>? = null
    var markSetupConfiguredCalled = false

    override fun observeSetupState(): Flow<SingleSetupState> = setupStateFlow

    override suspend fun loadSetupState(): SingleSetupState = setupStateFlow.value

    override fun observeActiveAreas(): Flow<List<LifeArea>> = areaFlow

    override fun observeDailyChecks(date: String): Flow<List<LifeAreaDailyCheck>> {
        return dailyCheckFlows.getOrPut(date) { MutableStateFlow(emptyList()) }
    }

    override suspend fun loadActiveAreas(): List<LifeArea> = areaFlow.value

    override suspend fun loadAreaInventory(): LifeAreaInventory {
        val (activeAreas, inactiveAreas) = areaFlow.value.partition(LifeArea::isActive)
        return LifeAreaInventory(
            activeAreas = activeAreas.sortedBy(LifeArea::sortOrder),
            inactiveAreas = inactiveAreas.sortedBy(LifeArea::sortOrder),
        )
    }

    override suspend fun ensureSeededAreas() = Unit

    override suspend fun markSetupConfigured() {
        markSetupConfiguredCalled = true
        setupStateFlow.value = SingleSetupState(isLifeWheelConfigured = true)
    }

    override suspend fun completeSetup(areas: List<LifeArea>) = Unit

    override suspend fun createArea(
        label: String,
        definition: String,
        templateId: String,
        iconKey: String,
    ): String {
        createdAreaId = "created-area"
        createdAreaLabel = label
        createdAreaDefinition = definition
        createdAreaTemplateId = templateId
        createdAreaIconKey = iconKey
        areaFlow.value = listOf(
            LifeArea(
                id = createdAreaId ?: error("createdAreaId missing"),
                label = label,
                definition = definition,
                targetScore = 3,
                sortOrder = 0,
                isActive = true,
                templateId = templateId,
                iconKey = iconKey,
            ),
        ) + areaFlow.value.mapIndexed { index, area ->
            area.copy(sortOrder = index + 1)
        }
        return createdAreaId ?: error("createdAreaId missing")
    }

    override suspend fun updateAreaIdentity(
        id: String,
        label: String,
        definition: String,
        templateId: String,
        iconKey: String,
    ) {
        updatedIdentityLabel = label
        updatedIdentityDefinition = definition
        updatedIdentityTemplateId = templateId
        updatedIdentityIconKey = iconKey
    }

    override suspend fun swapAreaOrder(
        firstId: String,
        secondId: String,
    ) {
        swappedIds = firstId to secondId
    }

    override suspend fun moveAreaEarlier(id: String) {
        movedEarlierAreaId = id
    }

    override suspend fun moveAreaLater(id: String) {
        movedLaterAreaId = id
    }

    override suspend fun deleteArea(id: String) {
        deletedAreaId = id
    }

    override suspend fun updateArea(
        id: String,
        label: String,
        definition: String,
        targetScore: Int,
    ) {
        updatedAreaTargetScore = targetScore
    }

    override suspend fun upsertDailyCheck(
        areaId: String,
        date: String,
        manualScore: Int?,
    ) {
        lastUpsertedManualScore = manualScore
    }
}

private class FakeLifeAreaProfileRepository(
    profiles: List<LifeAreaProfile> = emptyList(),
) : LifeAreaProfileRepository {
    private val profileFlow = MutableStateFlow(profiles)

    var savedProfile: LifeAreaProfile? = null
    val savedProfiles = mutableListOf<LifeAreaProfile>()

    override fun observeProfiles(): Flow<List<LifeAreaProfile>> = profileFlow

    override suspend fun saveProfile(profile: LifeAreaProfile) {
        savedProfile = profile
        savedProfiles += profile
        profileFlow.value = profileFlow.value
            .filterNot { it.areaId == profile.areaId } + profile
    }
}
