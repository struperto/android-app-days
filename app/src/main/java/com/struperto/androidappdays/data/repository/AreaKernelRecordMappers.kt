package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.AreaInstanceEntity
import com.struperto.androidappdays.data.local.AreaSnapshotEntity
import com.struperto.androidappdays.domain.area.AreaAuthoringConfig
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaDirectionMode
import com.struperto.androidappdays.domain.area.AreaFlowProfile
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaLageMode
import com.struperto.androidappdays.domain.area.AreaNextMeaningfulStep
import com.struperto.androidappdays.domain.area.AreaProfileConfig
import com.struperto.androidappdays.domain.area.AreaSnapshot
import com.struperto.androidappdays.domain.area.AreaStepKind
import com.struperto.androidappdays.domain.area.AreaStepOrigin
import com.struperto.androidappdays.domain.area.AreaStepStatus
import com.struperto.androidappdays.domain.area.AreaSourcesMode
import com.struperto.androidappdays.domain.area.AreaVisibilityLevel
import com.struperto.androidappdays.domain.area.defaultAreaAuthoringConfig
import com.struperto.androidappdays.domain.area.defaultAreaDefinitionId
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition
import java.time.Instant
import java.time.LocalDate

internal fun AreaInstanceEntity.toLifeArea(): LifeArea {
    return LifeArea(
        id = areaId,
        label = title,
        definition = summary,
        targetScore = targetScore.coerceIn(1, 5),
        sortOrder = sortOrder,
        isActive = isActive,
        templateId = templateId ?: "free",
        iconKey = iconKey,
    )
}

internal fun AreaInstanceEntity.toAreaInstance(): AreaInstance {
    val resolvedDefinitionId = definitionId.ifBlank {
        defaultAreaDefinitionId(
            areaId = areaId,
            templateId = templateId,
        )
    }
    val authoringDefaults = defaultAreaAuthoringConfig(
        definition = startAreaKernelDefinition(resolvedDefinitionId),
        templateId = templateId,
    )
    return AreaInstance(
        areaId = areaId,
        title = title,
        summary = summary,
        iconKey = iconKey,
        targetScore = targetScore.coerceIn(1, 5),
        sortOrder = sortOrder,
        isActive = isActive,
        cadenceKey = cadenceKey,
        selectedTracks = decodeSelectedTracks(selectedTracks),
        signalBlend = signalBlend.coerceIn(0, 100),
        intensity = intensity.coerceIn(1, 5),
        remindersEnabled = remindersEnabled,
        reviewEnabled = reviewEnabled,
        experimentsEnabled = experimentsEnabled,
        templateId = templateId,
        definitionId = resolvedDefinitionId,
        authoringConfig = AreaAuthoringConfig(
            behaviorClass = behaviorClass.takeIf(String::isNotBlank)
                ?.let(AreaBehaviorClass::fromPersistedValue)
                ?: authoringDefaults.behaviorClass,
            lageMode = lageMode.takeIf(String::isNotBlank)
                ?.let(AreaLageMode::fromPersistedValue)
                ?: authoringDefaults.lageMode,
            directionMode = directionMode.takeIf(String::isNotBlank)
                ?.let(AreaDirectionMode::fromPersistedValue)
                ?: authoringDefaults.directionMode,
            sourcesMode = sourcesMode.takeIf(String::isNotBlank)
                ?.let(AreaSourcesMode::fromPersistedValue)
                ?: authoringDefaults.sourcesMode,
            flowProfile = flowProfile.takeIf(String::isNotBlank)
                ?.let(AreaFlowProfile::fromPersistedValue)
                ?: authoringDefaults.flowProfile,
            complexityLevel = AreaComplexityLevel.entries.firstOrNull {
                it.name.equals(authoringComplexity, ignoreCase = true)
            } ?: authoringDefaults.complexityLevel,
            visibilityLevel = authoringVisibility.takeIf(String::isNotBlank)
                ?.let(AreaVisibilityLevel::fromPersistedValue)
                ?: authoringDefaults.visibilityLevel,
        ),
        confirmedNextStep = confirmedStepLabel?.takeIf(String::isNotBlank)?.let { label ->
            AreaNextMeaningfulStep(
                kind = confirmedStepKind?.takeIf(String::isNotBlank)
                    ?.let { AreaStepKind.valueOf(it) }
                    ?: AreaStepKind.review,
                label = label,
                status = AreaStepStatus.READY,
                origin = AreaStepOrigin.manual,
                isUserConfirmed = true,
                fallbackLabel = label,
                dueHint = confirmedStepDueHint,
                linkedPlanItemId = confirmedStepLinkedPlanItemId,
                linkedSourceId = confirmedStepLinkedSourceId,
            )
        },
        lastReviewedAt = lastReviewedAt?.let(Instant::ofEpochMilli),
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt),
    )
}

internal fun AreaInstanceEntity.toLifeAreaProfile(): LifeAreaProfile {
    return LifeAreaProfile(
        areaId = areaId,
        cadence = cadenceKey,
        intensity = intensity.coerceIn(1, 5),
        signalBlend = signalBlend.coerceIn(0, 100),
        selectedTracks = decodeSelectedTracks(selectedTracks),
        remindersEnabled = remindersEnabled,
        reviewEnabled = reviewEnabled,
        experimentsEnabled = experimentsEnabled,
        lageMode = lageMode,
        directionMode = directionMode,
        sourcesMode = sourcesMode,
        flowProfile = flowProfile,
    )
}

internal fun AreaInstanceEntity.toAreaProfileConfig(): AreaProfileConfig {
    val resolvedDefinitionId = definitionId.ifBlank {
        defaultAreaDefinitionId(
            areaId = areaId,
            templateId = templateId,
        )
    }
    val defaults = defaultAreaAuthoringConfig(
        definition = startAreaKernelDefinition(resolvedDefinitionId),
        templateId = templateId,
    )
    return AreaProfileConfig(
        lageMode = lageMode.takeIf(String::isNotBlank)
            ?.let(AreaLageMode::fromPersistedValue)
            ?: defaults.lageMode,
        directionMode = directionMode.takeIf(String::isNotBlank)
            ?.let(AreaDirectionMode::fromPersistedValue)
            ?: defaults.directionMode,
        sourcesMode = sourcesMode.takeIf(String::isNotBlank)
            ?.let(AreaSourcesMode::fromPersistedValue)
            ?: defaults.sourcesMode,
        flowProfile = flowProfile.takeIf(String::isNotBlank)
            ?.let(AreaFlowProfile::fromPersistedValue)
            ?: defaults.flowProfile,
    )
}

internal fun AreaSnapshotEntity.toLifeAreaDailyCheck(): LifeAreaDailyCheck? {
    val score = manualScore ?: return null
    return LifeAreaDailyCheck(
        areaId = areaId,
        date = date,
        manualScore = score.coerceIn(1, 5),
    )
}

internal fun AreaSnapshotEntity.toAreaSnapshot(): AreaSnapshot {
    return AreaSnapshot(
        areaId = areaId,
        date = LocalDate.parse(date),
        manualScore = manualScore?.coerceIn(1, 5),
        manualStateKey = manualStateKey,
        manualNote = manualNote,
        confidence = confidence,
        freshnessAt = freshnessAt?.let(Instant::ofEpochMilli),
    )
}

internal fun AreaSnapshot.toEntity(existingCreatedAt: Long? = null): AreaSnapshotEntity {
    val now = freshnessAt?.toEpochMilli() ?: System.currentTimeMillis()
    return AreaSnapshotEntity(
        areaId = instanceId,
        date = date.toString(),
        manualScore = manualScore?.coerceIn(1, 5),
        manualStateKey = manualStateKey,
        manualNote = manualNote,
        confidence = confidence,
        freshnessAt = freshnessAt?.toEpochMilli(),
        createdAt = existingCreatedAt ?: now,
        updatedAt = now,
    )
}

internal fun encodeSelectedTracks(selectedTracks: Set<String>): String {
    return selectedTracks
        .map(String::trim)
        .filter(String::isNotBlank)
        .joinToString(",")
}

internal fun decodeSelectedTracks(selectedTracks: String): Set<String> {
    return selectedTracks.split(",")
        .map(String::trim)
        .filter(String::isNotBlank)
        .toSet()
}
