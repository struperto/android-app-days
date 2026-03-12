package com.struperto.androidappdays.domain.area

enum class AreaLageMode(
    val persistedValue: String,
) {
    Score("score"),
    State("state"),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaLageMode {
            return entries.firstOrNull { it.persistedValue == value } ?: Score
        }
    }
}

enum class AreaDirectionMode(
    val persistedValue: String,
) {
    Balanced("balanced"),
    Focus("focus"),
    Rhythm("rhythm"),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaDirectionMode {
            return entries.firstOrNull { it.persistedValue == value } ?: Balanced
        }
    }
}

enum class AreaSourcesMode(
    val persistedValue: String,
) {
    Balanced("balanced"),
    Signals("signals"),
    Curated("curated"),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaSourcesMode {
            return entries.firstOrNull { it.persistedValue == value } ?: Balanced
        }
    }
}

enum class AreaFlowProfile(
    val persistedValue: String,
) {
    Stable("stable"),
    Supportive("supportive"),
    Active("active"),
    ;

    companion object {
        fun fromPersistedValue(value: String): AreaFlowProfile {
            return entries.firstOrNull { it.persistedValue == value } ?: Stable
        }
    }
}

data class AreaProfileConfig(
    val lageMode: AreaLageMode = AreaLageMode.Score,
    val directionMode: AreaDirectionMode = AreaDirectionMode.Balanced,
    val sourcesMode: AreaSourcesMode = AreaSourcesMode.Balanced,
    val flowProfile: AreaFlowProfile = AreaFlowProfile.Stable,
)

fun defaultAreaProfileConfig(
    definition: AreaDefinition?,
    templateId: String? = null,
): AreaProfileConfig {
    return defaultAreaAuthoringConfig(
        definition = definition,
        templateId = templateId,
    ).profileConfig
}

internal fun defaultAreaProfileConfigInternal(
    definition: AreaDefinition?,
    templateId: String? = null,
): AreaProfileConfig {
    if (definition == null) {
        return when (templateId) {
            "project" -> AreaProfileConfig(
                lageMode = AreaLageMode.Score,
                directionMode = AreaDirectionMode.Focus,
                sourcesMode = AreaSourcesMode.Curated,
                flowProfile = AreaFlowProfile.Active,
            )
            "ritual", "place", "feeling" -> AreaProfileConfig(
                lageMode = AreaLageMode.Score,
                directionMode = AreaDirectionMode.Rhythm,
                sourcesMode = AreaSourcesMode.Signals,
                flowProfile = AreaFlowProfile.Supportive,
            )
            else -> AreaProfileConfig(
                lageMode = AreaLageMode.State,
                directionMode = AreaDirectionMode.Balanced,
                sourcesMode = AreaSourcesMode.Curated,
                flowProfile = AreaFlowProfile.Stable,
            )
        }
    }
    val areaId = canonicalStartAreaId(definition.id)
    return AreaProfileConfig(
        lageMode = when {
            areaId in setOf("friends", "recovery", "discovery", "meaning") -> AreaLageMode.State
            definition.lageType == AreaLageType.STATE -> AreaLageMode.State
            else -> AreaLageMode.Score
        },
        directionMode = when {
            areaId in setOf("clarity", "impact", "home", "learning") -> AreaDirectionMode.Rhythm
            areaId in setOf("friends", "bond", "community", "discovery", "meaning") -> AreaDirectionMode.Focus
            definition.overviewMode == AreaOverviewMode.PLAN -> AreaDirectionMode.Rhythm
            definition.overviewMode == AreaOverviewMode.REFLECTION -> AreaDirectionMode.Focus
            else -> AreaDirectionMode.Balanced
        },
        sourcesMode = when {
            areaId in setOf("vitality", "clarity") -> AreaSourcesMode.Signals
            areaId in setOf("friends", "learning", "discovery", "meaning") -> AreaSourcesMode.Curated
            definition.supportsPassiveSignals -> AreaSourcesMode.Signals
            definition.overviewMode == AreaOverviewMode.REFLECTION -> AreaSourcesMode.Curated
            else -> AreaSourcesMode.Balanced
        },
        flowProfile = when {
            areaId in setOf("clarity", "impact", "home", "learning") -> AreaFlowProfile.Active
            areaId in setOf("friends", "bond", "community", "meaning") -> AreaFlowProfile.Supportive
            definition.overviewMode == AreaOverviewMode.PLAN -> AreaFlowProfile.Active
            definition.overviewMode == AreaOverviewMode.REFLECTION -> AreaFlowProfile.Supportive
            else -> AreaFlowProfile.Stable
        },
    )
}
