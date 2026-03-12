package com.struperto.androidappdays.domain.area

/**
 * Content seed for one of the four stable Start panels.
 *
 * This stays content-oriented on purpose. It may later be mapped to feature-specific icons,
 * sheet labels, or richer Start projections without pushing those concerns into the kernel.
 */
data class AreaPanelContentSeed(
    val title: String,
    val summary: String,
    val prompt: String? = null,
) {
    init {
        require(title.isNotBlank()) { "title must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
        require(prompt == null || prompt.isNotBlank()) { "prompt must not be blank when provided." }
    }
}

/**
 * Area-specific content blueprint used to seed and map one area type.
 *
 * This is heavier on wording and track structure than [AreaDefinition], but still excludes
 * user-specific runtime configuration and transient screen state.
 */
data class AreaBlueprint(
    val areaId: String,
    val summary: String,
    val trackLabels: List<String>,
    val defaultTemplateId: String,
    val defaultIconKey: String,
    val panelContentSeeds: Map<AreaPanelKind, AreaPanelContentSeed>,
    val pilotSemantics: AreaPilotSemantics? = null,
    val defaultSourceLabels: List<String> = emptyList(),
    val domainTags: Set<String> = emptySet(),
    val recommendedOrderHint: Int? = null,
    val starterHints: List<String> = emptyList(),
) {
    init {
        require(areaId.isNotBlank()) { "areaId must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
        require(trackLabels.none { it.isBlank() }) { "trackLabels must not contain blank entries." }
        require(defaultTemplateId.isNotBlank()) { "defaultTemplateId must not be blank." }
        require(defaultIconKey.isNotBlank()) { "defaultIconKey must not be blank." }
        require(panelContentSeeds.keys.containsAll(AreaPanelKind.entries)) {
            "panelContentSeeds must define Lage, Richtung, Quellen, and Flow."
        }
        require(defaultSourceLabels.none { it.isBlank() }) {
            "defaultSourceLabels must not contain blank entries."
        }
        require(domainTags.none { it.isBlank() }) { "domainTags must not contain blank entries." }
        require(recommendedOrderHint == null || recommendedOrderHint >= 0) {
            "recommendedOrderHint must be positive when provided."
        }
        require(starterHints.none { it.isBlank() }) { "starterHints must not contain blank entries." }
    }
}
