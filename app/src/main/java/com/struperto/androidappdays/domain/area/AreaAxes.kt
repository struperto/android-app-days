package com.struperto.androidappdays.domain.area

/**
 * Describes how an area should primarily read on overview and home projections.
 */
enum class AreaOverviewMode {
    SIGNAL,
    PLAN,
    REFLECTION,
    HYBRID,
}

/**
 * Declares how much of an area is visible or configurable by default.
 */
enum class AreaComplexityLevel {
    BASIC,
    ADVANCED,
    EXPERT,
}

/**
 * Declares the core shape of the current-state reading in `Lage`.
 */
enum class AreaLageType {
    SCORE,
    STATE,
    CHECKLIST,
    HYBRID,
}

/**
 * Declares the core shape of direction-setting in `Richtung`.
 */
enum class AreaFocusType {
    TARGET,
    CADENCE,
    NEXT_STEP,
    HYBRID,
}

/**
 * Declares which kinds of inputs may inform a given area.
 */
enum class AreaSourceType {
    MANUAL,
    LOCAL_SIGNAL,
    IMPORTED,
    NOTE,
    TRACK,
}

/**
 * Declares which local steering capabilities `Flow` may support for an area.
 */
enum class AreaFlowCapability {
    REMINDER,
    REVIEW,
    EXPERIMENT,
    TRIGGER,
    ACTION,
}

/**
 * Lightweight grouping for stable area families. This stays domain-level, not UI-level.
 */
enum class AreaCategory {
    FOUNDATION,
    DIRECTION,
    RELATIONSHIP,
    ENVIRONMENT,
    GROWTH,
    OPEN,
}

/**
 * Indicates whether an area definition is expected to depend on sensitive local signals later on.
 */
enum class AreaPermissionSensitivity {
    NONE,
    LOW,
    HIGH,
}

/**
 * The shared product contract for the four stable Start panels.
 */
enum class AreaPanelKind {
    LAGE,
    RICHTUNG,
    QUELLEN,
    FLOW,
}
