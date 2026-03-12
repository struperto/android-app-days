package com.struperto.androidappdays.domain.area.mapping

import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.data.repository.LifeAreaDailyCheck
import com.struperto.androidappdays.data.repository.LifeAreaProfile
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.area.AreaBlueprint
import com.struperto.androidappdays.domain.area.AreaCategory
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaDefaultConfig
import com.struperto.androidappdays.domain.area.AreaDefinition
import com.struperto.androidappdays.domain.area.defaultBehaviorClassForTemplate
import com.struperto.androidappdays.domain.area.AreaFlowCapability
import com.struperto.androidappdays.domain.area.AreaFocusType
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaLageType
import com.struperto.androidappdays.domain.area.AreaOverviewMode
import com.struperto.androidappdays.domain.area.AreaPanelContentSeed
import com.struperto.androidappdays.domain.area.AreaPanelKind
import com.struperto.androidappdays.domain.area.AreaProfileConfig
import com.struperto.androidappdays.domain.area.AreaPermissionSensitivity
import com.struperto.androidappdays.domain.area.defaultAreaDefinitionId
import com.struperto.androidappdays.domain.area.defaultAreaProfileConfig
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.AreaSourceType
import com.struperto.androidappdays.feature.start.StartAreaBlueprint
import com.struperto.androidappdays.feature.start.StartAreaDrive
import com.struperto.androidappdays.feature.start.StartAreaPanel
import com.struperto.androidappdays.feature.start.StartAreaTier
import java.time.LocalDate

/**
 * Maps the current Start blueprint model into the stable kernel definition type.
 *
 * This adapter is intentionally transitional. It derives kernel axes from the existing
 * Start-only blueprint shape without changing the active Start implementation.
 */
fun StartAreaBlueprint.toAreaDefinition(): AreaDefinition {
    return AreaDefinition(
        id = id,
        title = label,
        shortTitle = label,
        iconKey = defaultIconKey,
        defaultBehaviorClass = defaultBehaviorClassForTemplate(defaultTemplateId),
        category = domains.toAreaCategory(),
        overviewMode = drive.toAreaOverviewMode(),
        complexityLevel = tier.toAreaComplexityLevel(),
        seededByDefault = true,
        userCreatable = false,
        lageType = AreaLageType.SCORE,
        focusType = AreaFocusType.HYBRID,
        sourceTypesAllowed = buildAreaSourceTypes(
            drive = drive,
            domains = domains,
        ),
        flowCapabilities = setOf(
            AreaFlowCapability.REMINDER,
            AreaFlowCapability.REVIEW,
            AreaFlowCapability.EXPERIMENT,
        ),
        defaultConfig = AreaDefaultConfig(
            targetScore = defaultTargetScore,
            defaultSelectedTracks = tracks.take(2).toSet(),
        ),
        orderHint = null,
        permissionSensitivity = domains.toAreaPermissionSensitivity(),
        supportsPassiveSignals = domains.supportsPassiveSignals(),
        supportsImportedSources = false,
        reviewRhythmKey = null,
        capabilityNotes = "Transition-mapped from StartAreaBlueprint.",
    )
}

/**
 * Maps the current Start blueprint model into the content-oriented kernel blueprint type.
 *
 * Current `StartAreaBlueprint` does not contain an explicit `Quellen` entry. The mapper therefore
 * derives a small fallback seed from the drive and track list instead of inventing UI state.
 */
fun StartAreaBlueprint.toAreaBlueprint(): AreaBlueprint {
    val entryMap = entries.associateBy { it.panel }
    return AreaBlueprint(
        areaId = id,
        summary = summary,
        trackLabels = tracks,
        defaultTemplateId = defaultTemplateId,
        defaultIconKey = defaultIconKey,
        panelContentSeeds = mapOf(
            AreaPanelKind.LAGE to entryMap.getValue(StartAreaPanel.Snapshot).toAreaPanelContentSeed(),
            AreaPanelKind.RICHTUNG to entryMap.getValue(StartAreaPanel.Path).toAreaPanelContentSeed(),
            AreaPanelKind.QUELLEN to derivedSourcesSeed(),
            AreaPanelKind.FLOW to entryMap.getValue(StartAreaPanel.Options).toAreaPanelContentSeed(),
        ),
        defaultSourceLabels = tracks.take(2),
        domainTags = domains.mapTo(linkedSetOf(), LifeDomain::name),
        recommendedOrderHint = null,
        starterHints = emptyList(),
    )
}

/**
 * Maps the currently active persistence pair into one kernel instance.
 *
 * This overload is the direct bridge for the current runtime path where both legacy persistence
 * models are present.
 */
fun LifeArea.toAreaInstance(profile: LifeAreaProfile): AreaInstance {
    require(profile.areaId == id) { "LifeAreaProfile.areaId must match LifeArea.id." }
    val definition = com.struperto.androidappdays.domain.area.startAreaKernelDefinition(id)
    return AreaInstance(
        areaId = id,
        title = label,
        summary = definition?.let { definitionFromLegacy(it) } ?: this.definition,
        iconKey = iconKey,
        targetScore = targetScore,
        sortOrder = sortOrder,
        isActive = isActive,
        cadenceKey = profile.cadence,
        selectedTracks = profile.selectedTracks,
        signalBlend = profile.signalBlend,
        intensity = profile.intensity,
        remindersEnabled = profile.remindersEnabled,
        reviewEnabled = profile.reviewEnabled,
        experimentsEnabled = profile.experimentsEnabled,
        profileConfig = profile.toAreaProfileConfig(
            definition = definition,
            templateId = templateId,
        ),
        templateId = templateId,
        definitionId = defaultAreaDefinitionId(
            areaId = id,
            templateId = templateId,
        ),
    )
}

/**
 * Maps the current persistence pair into one kernel instance while allowing a missing profile.
 *
 * The fallback uses the kernel definition defaults so that migration code can stay deterministic
 * without depending on the current Start UI projection layer.
 */
fun LifeArea.toAreaInstance(
    definition: AreaDefinition,
    profile: LifeAreaProfile? = null,
): AreaInstance {
    require(definition.id == id) { "AreaDefinition.id must match LifeArea.id." }
    if (profile != null) {
        return toAreaInstance(profile)
    }
    return AreaInstance(
        areaId = id,
        title = label,
        summary = definitionFromLegacy(definition),
        iconKey = iconKey,
        targetScore = targetScore,
        sortOrder = sortOrder,
        isActive = isActive,
        cadenceKey = definition.defaultConfig.cadenceKey,
        selectedTracks = definition.defaultConfig.defaultSelectedTracks,
        signalBlend = definition.defaultConfig.signalBlend,
        intensity = definition.defaultConfig.intensity,
        remindersEnabled = definition.defaultConfig.remindersEnabled,
        reviewEnabled = definition.defaultConfig.reviewEnabled,
        experimentsEnabled = definition.defaultConfig.experimentsEnabled,
        profileConfig = defaultAreaProfileConfig(definition = definition),
        templateId = templateId,
        definitionId = definition.id,
    )
}

/**
 * Fallback mapping for legacy areas that do not yet have a canonical kernel definition.
 *
 * This exists only so the Start overview can project custom or currently undefined areas
 * through the kernel without changing repository or UI contracts.
 */
fun LifeArea.toAreaInstanceWithFallbackDefaults(): AreaInstance {
    val defaults = AreaDefaultConfig(targetScore = targetScore)
    return AreaInstance(
        areaId = id,
        title = label,
        summary = definition,
        iconKey = iconKey,
        targetScore = targetScore,
        sortOrder = sortOrder,
        isActive = isActive,
        cadenceKey = defaults.cadenceKey,
        selectedTracks = defaults.defaultSelectedTracks,
        signalBlend = defaults.signalBlend,
        intensity = defaults.intensity,
        remindersEnabled = defaults.remindersEnabled,
        reviewEnabled = defaults.reviewEnabled,
        experimentsEnabled = defaults.experimentsEnabled,
        profileConfig = defaultAreaProfileConfig(
            definition = null,
            templateId = templateId,
        ),
        templateId = templateId,
        definitionId = defaultAreaDefinitionId(
            areaId = id,
            templateId = templateId,
        ),
    )
}

/**
 * Maps the current legacy daily check model into the kernel snapshot type.
 */
fun LifeAreaDailyCheck.toAreaSnapshot(): AreaSnapshot {
    return AreaSnapshot(
        areaId = areaId,
        date = LocalDate.parse(date),
        manualScore = manualScore,
    )
}

/**
 * Maps the kernel instance back into the current legacy profile store shape.
 *
 * This reverse adapter is intentionally limited to the persistence fields already supported by
 * the current Room schema. Ordering and activation still stay on the legacy area model itself.
 */
fun AreaInstance.toLifeAreaProfile(): LifeAreaProfile {
    return LifeAreaProfile(
        areaId = areaId,
        cadence = cadenceKey,
        intensity = intensity,
        signalBlend = signalBlend,
        selectedTracks = selectedTracks,
        remindersEnabled = remindersEnabled,
        reviewEnabled = reviewEnabled,
        experimentsEnabled = experimentsEnabled,
        lageMode = profileConfig.lageMode.persistedValue,
        directionMode = profileConfig.directionMode.persistedValue,
        sourcesMode = profileConfig.sourcesMode.persistedValue,
        flowProfile = profileConfig.flowProfile.persistedValue,
    )
}

private fun LifeAreaProfile.toAreaProfileConfig(
    definition: AreaDefinition?,
    templateId: String?,
): AreaProfileConfig {
    return if (
        lageMode.isBlank() &&
        directionMode.isBlank() &&
        sourcesMode.isBlank() &&
        flowProfile.isBlank()
    ) {
        defaultAreaProfileConfig(
            definition = definition,
            templateId = templateId,
        )
    } else {
        AreaProfileConfig(
            lageMode = com.struperto.androidappdays.domain.area.AreaLageMode.fromPersistedValue(lageMode),
            directionMode = com.struperto.androidappdays.domain.area.AreaDirectionMode.fromPersistedValue(directionMode),
            sourcesMode = com.struperto.androidappdays.domain.area.AreaSourcesMode.fromPersistedValue(sourcesMode),
            flowProfile = com.struperto.androidappdays.domain.area.AreaFlowProfile.fromPersistedValue(flowProfile),
        )
    }
}

/**
 * Maps the kernel snapshot back into the current score-based daily check model.
 *
 * The legacy persistence layer cannot store state-only snapshots yet, so a manual score is
 * required for this reverse mapping.
 */
fun AreaSnapshot.toLifeAreaDailyCheck(): LifeAreaDailyCheck {
    return LifeAreaDailyCheck(
        areaId = areaId,
        date = date.toString(),
        manualScore = requireNotNull(manualScore) {
            "LifeAreaDailyCheck requires a score-based AreaSnapshot."
        },
    )
}

private fun StartAreaDrive.toAreaOverviewMode(): AreaOverviewMode {
    return when (this) {
        StartAreaDrive.Signal -> AreaOverviewMode.SIGNAL
        StartAreaDrive.Plan -> AreaOverviewMode.PLAN
        StartAreaDrive.Reflection -> AreaOverviewMode.REFLECTION
    }
}

private fun StartAreaTier.toAreaComplexityLevel(): AreaComplexityLevel {
    return when (this) {
        StartAreaTier.Core -> AreaComplexityLevel.BASIC
        StartAreaTier.Secondary -> AreaComplexityLevel.ADVANCED
        StartAreaTier.Review -> AreaComplexityLevel.EXPERT
    }
}

private fun buildAreaSourceTypes(
    drive: StartAreaDrive,
    domains: Set<LifeDomain>,
): Set<AreaSourceType> {
    return buildSet {
        add(AreaSourceType.MANUAL)
        add(AreaSourceType.TRACK)
        if (drive == StartAreaDrive.Reflection) {
            add(AreaSourceType.NOTE)
        }
        if (domains.supportsPassiveSignals()) {
            add(AreaSourceType.LOCAL_SIGNAL)
        }
    }
}

private fun Set<LifeDomain>.toAreaCategory(): AreaCategory {
    return when {
        any { it == LifeDomain.SOCIAL } -> AreaCategory.RELATIONSHIP
        any { it == LifeDomain.HOUSEHOLD } -> AreaCategory.ENVIRONMENT
        any { it in foundationDomains } -> AreaCategory.FOUNDATION
        any { it in growthDomains } -> AreaCategory.GROWTH
        any { it in directionDomains } -> AreaCategory.DIRECTION
        else -> AreaCategory.OPEN
    }
}

private fun Set<LifeDomain>.toAreaPermissionSensitivity(): AreaPermissionSensitivity {
    return when {
        any { it in highSensitivityDomains } -> AreaPermissionSensitivity.HIGH
        supportsPassiveSignals() -> AreaPermissionSensitivity.LOW
        else -> AreaPermissionSensitivity.NONE
    }
}

private fun Set<LifeDomain>.supportsPassiveSignals(): Boolean {
    return any { it in passiveSignalDomains }
}

private fun StartAreaBlueprint.derivedSourcesSeed(): AreaPanelContentSeed {
    val leadTracks = tracks.take(2)
    val sourceLine = when {
        leadTracks.isEmpty() -> "Signale und Spuren ordnen."
        leadTracks.size == 1 -> "${leadTracks.first()} als Spur nutzen."
        else -> "${leadTracks[0]} und ${leadTracks[1]} als Spuren nutzen."
    }
    val summary = when (drive) {
        StartAreaDrive.Signal -> "Signale und Spuren fuer diesen Bereich gewichten."
        StartAreaDrive.Plan -> "Spuren und Inputs fuer die naechsten Zuege setzen."
        StartAreaDrive.Reflection -> "Spuren, Hinweise und Notizen zusammenziehen."
    }
    return AreaPanelContentSeed(
        title = "Quellen",
        summary = summary,
        prompt = sourceLine,
    )
}

private fun com.struperto.androidappdays.feature.start.StartAreaEntry.toAreaPanelContentSeed(): AreaPanelContentSeed {
    return AreaPanelContentSeed(
        title = title,
        summary = summary,
    )
}

private fun LifeArea.definitionFromLegacy(definition: AreaDefinition): String {
    return if (definition.title == label && this.definition.isBlank()) {
        definition.capabilityNotes ?: label
    } else {
        this.definition
    }
}

private val passiveSignalDomains = setOf(
    LifeDomain.SLEEP,
    LifeDomain.MOVEMENT,
    LifeDomain.HYDRATION,
    LifeDomain.NUTRITION,
    LifeDomain.HEALTH,
    LifeDomain.RECOVERY,
    LifeDomain.STRESS,
    LifeDomain.SCREEN_TIME,
    LifeDomain.EMOTIONAL_STATE,
)

private val highSensitivityDomains = setOf(
    LifeDomain.SLEEP,
    LifeDomain.HEALTH,
    LifeDomain.MEDICATION,
    LifeDomain.EMOTIONAL_STATE,
)

private val foundationDomains = setOf(
    LifeDomain.SLEEP,
    LifeDomain.MOVEMENT,
    LifeDomain.HYDRATION,
    LifeDomain.NUTRITION,
    LifeDomain.HEALTH,
    LifeDomain.RECOVERY,
    LifeDomain.STRESS,
)

private val directionDomains = setOf(
    LifeDomain.FOCUS,
    LifeDomain.ADMIN,
    LifeDomain.SCREEN_TIME,
)

private val growthDomains = emptySet<LifeDomain>()
