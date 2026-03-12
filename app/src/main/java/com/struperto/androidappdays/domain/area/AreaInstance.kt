package com.struperto.androidappdays.domain.area

import java.time.Instant

/**
 * Persisted, user-specific configuration of one actual area in the app.
 *
 * This object is the long-lived runtime truth for one area instance. It intentionally
 * excludes day-bound state, view-local wording, and screen interaction state.
 */
data class AreaInstance(
    val areaId: String,
    val title: String,
    val summary: String,
    val iconKey: String,
    val targetScore: Int,
    val sortOrder: Int,
    val isActive: Boolean,
    val cadenceKey: String,
    val selectedTracks: Set<String>,
    val signalBlend: Int,
    val intensity: Int,
    val remindersEnabled: Boolean,
    val reviewEnabled: Boolean,
    val experimentsEnabled: Boolean,
    val templateId: String? = null,
    val definitionId: String = defaultAreaDefinitionId(
        areaId = areaId,
        templateId = templateId,
    ),
    val authoringConfig: AreaAuthoringConfig = defaultAreaAuthoringConfig(
        definition = startAreaKernelDefinition(definitionId),
        templateId = templateId,
    ),
    val confirmedNextStep: AreaNextMeaningfulStep? = null,
    val lastReviewedAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    constructor(
        areaId: String,
        title: String,
        summary: String,
        iconKey: String,
        targetScore: Int,
        sortOrder: Int,
        isActive: Boolean,
        cadenceKey: String,
        selectedTracks: Set<String>,
        signalBlend: Int,
        intensity: Int,
        remindersEnabled: Boolean,
        reviewEnabled: Boolean,
        experimentsEnabled: Boolean,
        profileConfig: AreaProfileConfig? = null,
        templateId: String? = null,
        definitionId: String = defaultAreaDefinitionId(
            areaId = areaId,
            templateId = templateId,
        ),
        confirmedNextStep: AreaNextMeaningfulStep? = null,
        lastReviewedAt: Instant? = null,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
    ) : this(
        areaId = areaId,
        title = title,
        summary = summary,
        iconKey = iconKey,
        targetScore = targetScore,
        sortOrder = sortOrder,
        isActive = isActive,
        cadenceKey = cadenceKey,
        selectedTracks = selectedTracks,
        signalBlend = signalBlend,
        intensity = intensity,
        remindersEnabled = remindersEnabled,
        reviewEnabled = reviewEnabled,
        experimentsEnabled = experimentsEnabled,
        templateId = templateId,
        definitionId = definitionId,
        authoringConfig = defaultAreaAuthoringConfig(
            definition = startAreaKernelDefinition(definitionId),
            templateId = templateId,
        ).withProfileConfig(
            profileConfig ?: defaultAreaProfileConfig(
                definition = startAreaKernelDefinition(definitionId),
                templateId = templateId,
            ),
        ),
        confirmedNextStep = confirmedNextStep,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val instanceId: String
        get() = areaId

    val profileConfig: AreaProfileConfig
        get() = authoringConfig.profileConfig

    val behaviorClass: AreaBehaviorClass
        get() = authoringConfig.behaviorClass

    init {
        require(areaId.isNotBlank()) { "areaId must not be blank." }
        require(title.isNotBlank()) { "title must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
        require(iconKey.isNotBlank()) { "iconKey must not be blank." }
        require(targetScore in 1..5) { "targetScore must be between 1 and 5." }
        require(sortOrder >= 0) { "sortOrder must be positive." }
        require(cadenceKey.isNotBlank()) { "cadenceKey must not be blank." }
        require(selectedTracks.none { it.isBlank() }) { "selectedTracks must not contain blank entries." }
        require(signalBlend in 0..100) { "signalBlend must be between 0 and 100." }
        require(intensity in 1..5) { "intensity must be between 1 and 5." }
        require(definitionId.isNotBlank()) { "definitionId must not be blank." }
        require(templateId == null || templateId.isNotBlank()) { "templateId must not be blank when provided." }
    }
}

fun AreaInstance.withUpdatedIdentity(
    title: String,
    summary: String,
    templateId: String,
    iconKey: String,
): AreaInstance {
    val nextDefinitionId = if (definitionId.startsWith("template:")) {
        templateAreaDefinitionId(templateId)
    } else {
        definitionId
    }
    val nextAuthoringConfig = if (nextDefinitionId == definitionId) {
        authoringConfig
    } else {
        authoringConfig.rebasedOntoDefaults(
            previousDefaults = defaultAreaAuthoringConfig(
                definition = startAreaKernelDefinition(definitionId),
                templateId = this.templateId,
            ),
            nextDefaults = defaultAreaAuthoringConfig(
                definition = startAreaKernelDefinition(nextDefinitionId),
                templateId = templateId,
            ),
        )
    }
    return copy(
        title = title.trim(),
        summary = summary.trim(),
        templateId = templateId,
        iconKey = iconKey,
        definitionId = nextDefinitionId,
        authoringConfig = nextAuthoringConfig,
    )
}
