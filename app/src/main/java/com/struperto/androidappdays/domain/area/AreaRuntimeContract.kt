package com.struperto.androidappdays.domain.area

enum class AreaSourceClass {
    DEVICE_ONLY,
    USER_PICKED_PROVIDER,
    CLOUD_ACCOUNT,
    MANUAL_INPUT,
}

enum class AreaExecutionClass {
    ON_DEVICE,
    HYBRID,
    CLOUD,
}

enum class AreaStorageClass {
    DEVICE_ONLY,
    DEVICE_PLUS_CLOUD,
    CLOUD_ONLY,
}

enum class AreaConsentScope {
    ONE_SHOT,
    PERSISTENT,
    ACCOUNT,
    BACKGROUND,
    MANUAL,
}

enum class AreaRuntimeAxis(
    val label: String,
) {
    ZUSTAND("Zustand"),
    EINGANG("Eingang"),
    AUSWAHL("Auswahl"),
    RHYTHMUS("Rhythmus"),
}

data class AreaProvenance(
    val sourceClass: AreaSourceClass,
    val executionClass: AreaExecutionClass,
    val storageClass: AreaStorageClass,
    val provider: String? = null,
    val consentScope: AreaConsentScope = AreaConsentScope.MANUAL,
    val goal: String = "",
) {
    init {
        require(provider == null || provider.isNotBlank()) { "provider must not be blank when provided." }
        require(goal.isNotBlank()) { "goal must not be blank." }
    }
}

data class AreaCapabilitySet(
    val canReadPassiveSignals: Boolean = false,
    val canImportLinks: Boolean = true,
    val canImportFiles: Boolean = true,
    val canImportImages: Boolean = true,
    val canSyncWebFeeds: Boolean = false,
    val canClusterLocally: Boolean = true,
    val canSummarizeLocally: Boolean = true,
    val canShowTodayFeed: Boolean = true,
    val canRenderMixedMediaFeed: Boolean = false,
    val canMarkRead: Boolean = false,
    val canSearchLocally: Boolean = false,
)

data class AreaInputProfile(
    val headline: String = "Material reinholen",
    val summary: String = "Dieser Bereich sammelt zuerst Material und zieht daraus spaeter eine klare Heute-Lesart.",
    val acceptedInputs: List<String> = emptyList(),
    val primaryAxis: AreaRuntimeAxis = AreaRuntimeAxis.EINGANG,
) {
    init {
        require(headline.isNotBlank()) { "headline must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
        require(acceptedInputs.none { it.isBlank() }) { "acceptedInputs must not contain blank labels." }
    }
}

data class AreaSelectionProfile(
    val headline: String = "Heute klar waehlen",
    val summary: String = "Dieser Bereich waehlt aus dem Eingang aus, was heute wirklich vorne steht.",
) {
    init {
        require(headline.isNotBlank()) { "headline must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
    }
}

data class AreaRhythmProfile(
    val headline: String = "Ruhig steuern",
    val summary: String = "Rhythmus regelt, wie oft und wie deutlich sich dieser Bereich meldet.",
) {
    init {
        require(headline.isNotBlank()) { "headline must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
    }
}

data class AreaSummaryProfile(
    val headline: String = "Kurz verdichten",
    val summary: String = "Days darf hier nur sauber bereinigen, Duplikate erkennen, Material gruppieren und kurze lokale Zusammenfassungen bilden.",
) {
    init {
        require(headline.isNotBlank()) { "headline must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
    }
}

data class AreaSingleOutputProfile(
    val headline: String = "Bereichsfeed",
    val summary: String = "Im Single-Modus zeigt der Bereich einen ruhigen Feed aus lokalen Artikeln, Posts, Videos oder Links.",
) {
    init {
        require(headline.isNotBlank()) { "headline must not be blank." }
        require(summary.isNotBlank()) { "summary must not be blank." }
    }
}

data class AreaRuntimeContract(
    val visibleAxes: List<AreaRuntimeAxis> = listOf(
        AreaRuntimeAxis.ZUSTAND,
        AreaRuntimeAxis.EINGANG,
        AreaRuntimeAxis.AUSWAHL,
        AreaRuntimeAxis.RHYTHMUS,
    ),
    val inputProfile: AreaInputProfile = AreaInputProfile(),
    val selectionProfile: AreaSelectionProfile = AreaSelectionProfile(),
    val rhythmProfile: AreaRhythmProfile = AreaRhythmProfile(),
    val summaryProfile: AreaSummaryProfile = AreaSummaryProfile(),
    val singleOutputProfile: AreaSingleOutputProfile = AreaSingleOutputProfile(),
    val capabilitySet: AreaCapabilitySet = AreaCapabilitySet(),
    val provenances: List<AreaProvenance> = listOf(
        AreaProvenance(
            sourceClass = AreaSourceClass.MANUAL_INPUT,
            executionClass = AreaExecutionClass.ON_DEVICE,
            storageClass = AreaStorageClass.DEVICE_ONLY,
            consentScope = AreaConsentScope.MANUAL,
            goal = "Material bewusst sammeln",
        ),
    ),
) {
    init {
        require(visibleAxes.isNotEmpty()) { "visibleAxes must not be empty." }
    }
}

fun buildAreaRuntimeContract(
    definition: AreaDefinition,
    blueprint: AreaBlueprint,
): AreaRuntimeContract {
    val acceptedInputs = buildList {
        add("Links")
        add("Dateien")
        add("Bilder")
        if (definition.supportsPassiveSignals) {
            add("Android-Signale")
        }
        if (definition.supportsImportedSources) {
            add("Feeds")
        }
    }.distinct()
    val provenances = buildList {
        add(
            AreaProvenance(
                sourceClass = AreaSourceClass.MANUAL_INPUT,
                executionClass = AreaExecutionClass.ON_DEVICE,
                storageClass = AreaStorageClass.DEVICE_ONLY,
                consentScope = AreaConsentScope.MANUAL,
                goal = "Material bewusst sammeln",
            ),
        )
        if (definition.supportsPassiveSignals) {
            add(
                AreaProvenance(
                    sourceClass = AreaSourceClass.DEVICE_ONLY,
                    executionClass = AreaExecutionClass.ON_DEVICE,
                    storageClass = AreaStorageClass.DEVICE_ONLY,
                    consentScope = AreaConsentScope.PERSISTENT,
                    goal = "Lokale Signale lesen",
                ),
            )
        }
        if (definition.supportsImportedSources) {
            add(
                AreaProvenance(
                    sourceClass = AreaSourceClass.USER_PICKED_PROVIDER,
                    executionClass = AreaExecutionClass.ON_DEVICE,
                    storageClass = AreaStorageClass.DEVICE_ONLY,
                    consentScope = AreaConsentScope.ONE_SHOT,
                    goal = "Importiertes Material ordnen",
                ),
            )
        }
    }
    return AreaRuntimeContract(
        inputProfile = AreaInputProfile(
            summary = "Hier startet der Bereich mit Eingang. Aus ${acceptedInputs.joinToString(", ")} zieht Days spaeter Auswahl und heutigen Klartext.",
            acceptedInputs = acceptedInputs,
        ),
        selectionProfile = AreaSelectionProfile(
            summary = "Dieser Bereich waehlt aus seinem Eingang nur das aus, was heute wirklich in den Vordergrund gehoert.",
        ),
        rhythmProfile = AreaRhythmProfile(
            summary = "Rhythmus legt fest, wie ruhig oder aktiv ${definition.shortTitle.lowercase()} im Alltag bleiben soll.",
        ),
        summaryProfile = AreaSummaryProfile(
            summary = if (definition.supportsImportedSources) {
                "Days darf hier lokal bereinigen, Duplikate erkennen, Feeds und Imports clustern und kurze heutige Texte bauen."
            } else {
                "Days darf hier lokal bereinigen, Duplikate erkennen, Material clustern und kurze heutige Texte bauen."
            },
        ),
        singleOutputProfile = AreaSingleOutputProfile(
            summary = "Im Single-Modus zeigt ${definition.shortTitle.lowercase()} spaeter einen ruhigen Bereichsfeed statt eines Dashboards.",
        ),
        capabilitySet = AreaCapabilitySet(
            canReadPassiveSignals = definition.supportsPassiveSignals,
            canSyncWebFeeds = definition.supportsImportedSources || blueprint.defaultSourceLabels.any { label ->
                label.contains("feed", ignoreCase = true) || label.contains("web", ignoreCase = true)
            },
            canRenderMixedMediaFeed = definition.supportsImportedSources,
            canMarkRead = definition.supportsImportedSources,
            canSearchLocally = definition.supportsImportedSources,
        ),
        provenances = provenances,
    )
}
