package com.struperto.androidappdays.domain.area

/**
 * Stable default behavior for one area type.
 *
 * This config belongs to the kernel because it describes what a seeded or newly created
 * instance should start with before any user-specific override exists.
 */
data class AreaDefaultConfig(
    val targetScore: Int,
    val cadenceKey: String = "adaptive",
    val intensity: Int = 3,
    val signalBlend: Int = 60,
    val defaultSelectedTracks: Set<String> = emptySet(),
    val remindersEnabled: Boolean = false,
    val reviewEnabled: Boolean = true,
    val experimentsEnabled: Boolean = false,
) {
    init {
        require(targetScore in 1..5) { "targetScore must be between 1 and 5." }
        require(cadenceKey.isNotBlank()) { "cadenceKey must not be blank." }
        require(intensity in 1..5) { "intensity must be between 1 and 5." }
        require(signalBlend in 0..100) { "signalBlend must be between 0 and 100." }
    }
}

/**
 * Stable product and capability description of one area type.
 *
 * This object is the future kernel truth for what an area is allowed to be inside the
 * generic area system. It intentionally excludes user overrides, transient UI state,
 * and time-bound snapshot data.
 */
data class AreaDefinition(
    val id: String,
    val title: String,
    val shortTitle: String,
    val iconKey: String,
    val defaultBehaviorClass: AreaBehaviorClass = AreaBehaviorClass.REFLECTION,
    val category: AreaCategory,
    val overviewMode: AreaOverviewMode,
    val complexityLevel: AreaComplexityLevel,
    val seededByDefault: Boolean,
    val userCreatable: Boolean,
    val lageType: AreaLageType,
    val focusType: AreaFocusType,
    val sourceTypesAllowed: Set<AreaSourceType>,
    val flowCapabilities: Set<AreaFlowCapability>,
    val defaultConfig: AreaDefaultConfig,
    val orderHint: Int? = null,
    val permissionSensitivity: AreaPermissionSensitivity? = null,
    val supportsPassiveSignals: Boolean = false,
    val supportsImportedSources: Boolean = false,
    val reviewRhythmKey: String? = null,
    val capabilityNotes: String? = null,
    val authoringAxes: Set<AreaAuthoringAxis> = AreaAuthoringAxis.entries.toSet(),
) {
    init {
        require(id.isNotBlank()) { "id must not be blank." }
        require(title.isNotBlank()) { "title must not be blank." }
        require(shortTitle.isNotBlank()) { "shortTitle must not be blank." }
        require(iconKey.isNotBlank()) { "iconKey must not be blank." }
        require(orderHint == null || orderHint >= 0) { "orderHint must be positive when provided." }
        require(reviewRhythmKey == null || reviewRhythmKey.isNotBlank()) {
            "reviewRhythmKey must not be blank when provided."
        }
        require(authoringAxes.isNotEmpty()) { "authoringAxes must not be empty." }
    }
}
