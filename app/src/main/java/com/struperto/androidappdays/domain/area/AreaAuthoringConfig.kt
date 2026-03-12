package com.struperto.androidappdays.domain.area

enum class AreaVisibilityLevel(
    val persistedValue: String,
) {
    Focused("focused"),
    Standard("standard"),
    Expanded("expanded"),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaVisibilityLevel {
            return entries.firstOrNull { it.persistedValue == value } ?: Standard
        }
    }
}

enum class AreaAuthoringAxis {
    STATUS_SCHEMA,
    DIRECTION,
    SOURCES,
    FLOW,
    COMPLEXITY,
    VISIBILITY,
}

data class AreaAuthoringConfig(
    val behaviorClass: AreaBehaviorClass = AreaBehaviorClass.REFLECTION,
    val lageMode: AreaLageMode = AreaLageMode.Score,
    val directionMode: AreaDirectionMode = AreaDirectionMode.Balanced,
    val sourcesMode: AreaSourcesMode = AreaSourcesMode.Balanced,
    val flowProfile: AreaFlowProfile = AreaFlowProfile.Stable,
    val complexityLevel: AreaComplexityLevel = AreaComplexityLevel.BASIC,
    val visibilityLevel: AreaVisibilityLevel = AreaVisibilityLevel.Standard,
) {
    val profileConfig: AreaProfileConfig
        get() = AreaProfileConfig(
            lageMode = lageMode,
            directionMode = directionMode,
            sourcesMode = sourcesMode,
            flowProfile = flowProfile,
        )
}

fun defaultAreaAuthoringConfig(
    definition: AreaDefinition?,
    templateId: String? = null,
): AreaAuthoringConfig {
    val profileDefaults = defaultAreaProfileConfigInternal(
        definition = definition,
        templateId = templateId,
    )
    val complexityLevel = definition?.complexityLevel ?: defaultAreaComplexityLevel(templateId)
    return AreaAuthoringConfig(
        behaviorClass = definition?.defaultBehaviorClass ?: defaultBehaviorClassForTemplate(templateId),
        lageMode = profileDefaults.lageMode,
        directionMode = profileDefaults.directionMode,
        sourcesMode = profileDefaults.sourcesMode,
        flowProfile = profileDefaults.flowProfile,
        complexityLevel = complexityLevel,
        visibilityLevel = defaultAreaVisibilityLevel(complexityLevel),
    )
}

fun AreaAuthoringConfig.withProfileConfig(profileConfig: AreaProfileConfig): AreaAuthoringConfig {
    return copy(
        lageMode = profileConfig.lageMode,
        directionMode = profileConfig.directionMode,
        sourcesMode = profileConfig.sourcesMode,
        flowProfile = profileConfig.flowProfile,
    )
}

fun AreaAuthoringConfig.rebasedOntoDefaults(
    previousDefaults: AreaAuthoringConfig,
    nextDefaults: AreaAuthoringConfig,
): AreaAuthoringConfig {
    return copy(
        behaviorClass = if (behaviorClass == previousDefaults.behaviorClass) {
            nextDefaults.behaviorClass
        } else {
            behaviorClass
        },
        lageMode = if (lageMode == previousDefaults.lageMode) nextDefaults.lageMode else lageMode,
        directionMode = if (directionMode == previousDefaults.directionMode) {
            nextDefaults.directionMode
        } else {
            directionMode
        },
        sourcesMode = if (sourcesMode == previousDefaults.sourcesMode) {
            nextDefaults.sourcesMode
        } else {
            sourcesMode
        },
        flowProfile = if (flowProfile == previousDefaults.flowProfile) {
            nextDefaults.flowProfile
        } else {
            flowProfile
        },
        complexityLevel = if (complexityLevel == previousDefaults.complexityLevel) {
            nextDefaults.complexityLevel
        } else {
            complexityLevel
        },
        visibilityLevel = if (visibilityLevel == previousDefaults.visibilityLevel) {
            nextDefaults.visibilityLevel
        } else {
            visibilityLevel
        },
    )
}

fun defaultAreaDefinitionId(
    areaId: String,
    templateId: String? = null,
): String {
    return startAreaKernelDefinition(areaId)?.id ?: templateAreaDefinitionId(templateId)
}

fun templateAreaDefinitionId(templateId: String?): String {
    return "template:${templateId ?: "free"}"
}

private fun defaultAreaComplexityLevel(templateId: String?): AreaComplexityLevel {
    return when (templateId) {
        "project", "ritual", "place", "feeling" -> AreaComplexityLevel.BASIC
        "medium" -> AreaComplexityLevel.EXPERT
        else -> AreaComplexityLevel.ADVANCED
    }
}

private fun defaultAreaVisibilityLevel(
    complexityLevel: AreaComplexityLevel,
): AreaVisibilityLevel {
    return when (complexityLevel) {
        AreaComplexityLevel.BASIC -> AreaVisibilityLevel.Focused
        AreaComplexityLevel.ADVANCED -> AreaVisibilityLevel.Standard
        AreaComplexityLevel.EXPERT -> AreaVisibilityLevel.Expanded
    }
}
