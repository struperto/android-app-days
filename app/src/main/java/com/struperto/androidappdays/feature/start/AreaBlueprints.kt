package com.struperto.androidappdays.feature.start

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.domain.area.AreaBlueprint
import com.struperto.androidappdays.domain.area.AreaComplexityLevel
import com.struperto.androidappdays.domain.area.AreaDefinition
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.domain.area.AreaOverviewMode
import com.struperto.androidappdays.domain.area.AreaPanelKind
import com.struperto.androidappdays.domain.area.StartAreaKernelSeed
import com.struperto.androidappdays.domain.area.startAreaKernelBlueprint as kernelBlueprintLookup
import com.struperto.androidappdays.domain.area.startAreaKernelDefinition as kernelDefinitionLookup
import com.struperto.androidappdays.domain.area.startAreaKernelSeed as kernelSeedLookup
import com.struperto.androidappdays.domain.area.startAreaKernelSeeds
import com.struperto.androidappdays.domain.LifeDomain

enum class StartAreaDrive(
    val label: String,
    val cue: String,
) {
    Signal(
        label = "Signale",
        cue = "liest eher Zustand und Tendenzen",
    ),
    Plan(
        label = "Plan",
        cue = "lebt eher von Absicht und Routinen",
    ),
    Reflection(
        label = "Reflexion",
        cue = "lebt eher von Review und Ausrichtung",
    ),
}

enum class StartAreaPanel {
    Snapshot,
    Path,
    Sources,
    Options,
}

enum class StartAreaTier(
    val label: String,
) {
    Core("Jetzt"),
    Secondary("Weiter"),
    Review("Review"),
}

data class StartAreaTemplate(
    val id: String,
    val label: String,
    val summary: String,
    val tracks: List<String>,
    val drive: StartAreaDrive,
    val defaultIconKey: String,
)

data class StartAreaIconOption(
    val id: String,
    val label: String,
)

data class StartAreaEntry(
    val panel: StartAreaPanel,
    val title: String,
    val summary: String,
    val icon: ImageVector,
)

data class StartAreaBlueprint(
    val id: String,
    val label: String,
    val summary: String,
    val tracks: List<String>,
    val drive: StartAreaDrive,
    val tier: StartAreaTier,
    val defaultTargetScore: Int,
    val defaultTemplateId: String,
    val defaultIconKey: String,
    val domains: Set<LifeDomain>,
    val entries: List<StartAreaEntry>,
)

val startAreaTemplates = listOf(
    StartAreaTemplate(
        id = "person",
        label = "Person",
        summary = "Ein Mensch, eine Beziehung oder ein soziales Feld, das du bewusst tragen willst.",
        tracks = listOf("Kontakt", "Naehe", "Pflege", "Resonanz"),
        drive = StartAreaDrive.Reflection,
        defaultIconKey = "care",
    ),
    StartAreaTemplate(
        id = "theme",
        label = "Thema",
        summary = "Ein offenes Thema, das du beobachten, sortieren und ausrichten willst.",
        tracks = listOf("Frage", "Spur", "Material", "Bedeutung"),
        drive = StartAreaDrive.Reflection,
        defaultIconKey = "focus",
    ),
    StartAreaTemplate(
        id = "project",
        label = "Projekt",
        summary = "Ein Vorhaben mit Richtung, naechstem Schritt und sichtbarem Fortschritt.",
        tracks = listOf("Fokus", "Schritt", "Termin", "Fortschritt"),
        drive = StartAreaDrive.Plan,
        defaultIconKey = "briefcase",
    ),
    StartAreaTemplate(
        id = "feeling",
        label = "Gefuehl",
        summary = "Ein innerer Zustand, den du wahrnehmen, benennen und regulieren willst.",
        tracks = listOf("Koerper", "Stimmung", "Ausloeser", "Regulation"),
        drive = StartAreaDrive.Signal,
        defaultIconKey = "spark",
    ),
    StartAreaTemplate(
        id = "ritual",
        label = "Ritual",
        summary = "Eine wiederkehrende Praxis, die dich im Alltag tragen soll.",
        tracks = listOf("Ausloeser", "Schritt", "Rhythmus", "Wirkung"),
        drive = StartAreaDrive.Plan,
        defaultIconKey = "lotus",
    ),
    StartAreaTemplate(
        id = "place",
        label = "Ort",
        summary = "Ein Raum oder Ort, der deinen Alltag praegt und bewusst gestaltet werden soll.",
        tracks = listOf("Umfeld", "Weg", "Zeit", "Atmosphaere"),
        drive = StartAreaDrive.Signal,
        defaultIconKey = "home",
    ),
    StartAreaTemplate(
        id = "medium",
        label = "Medium",
        summary = "Ein Strom aus Inhalten, Links oder Impulsen, den du sammeln und verdichten willst.",
        tracks = listOf("Quellen", "Themen", "Accounts", "Formate"),
        drive = StartAreaDrive.Reflection,
        defaultIconKey = "book",
    ),
    StartAreaTemplate(
        id = "free",
        label = "Freier Bereich",
        summary = "Ein offener Bereich fuer alles, was gerade Bedeutung hat und Form bekommen soll.",
        tracks = listOf("Spuren", "Notizen", "Links", "Impulse"),
        drive = StartAreaDrive.Reflection,
        defaultIconKey = "spark",
    ),
)

val startAreaIconOptions = listOf(
    StartAreaIconOption(id = "heart", label = "Herz"),
    StartAreaIconOption(id = "focus", label = "Fokus"),
    StartAreaIconOption(id = "briefcase", label = "Arbeit"),
    StartAreaIconOption(id = "care", label = "Beziehung"),
    StartAreaIconOption(id = "family", label = "Familie"),
    StartAreaIconOption(id = "chat", label = "Dialog"),
    StartAreaIconOption(id = "groups", label = "Gruppe"),
    StartAreaIconOption(id = "home", label = "Ort"),
    StartAreaIconOption(id = "shield", label = "Sicherheit"),
    StartAreaIconOption(id = "lotus", label = "Ritual"),
    StartAreaIconOption(id = "trend", label = "Weg"),
    StartAreaIconOption(id = "book", label = "Medium"),
    StartAreaIconOption(id = "palette", label = "Kreativ"),
    StartAreaIconOption(id = "spark", label = "Frei"),
    StartAreaIconOption(id = "compass", label = "Sinn"),
    StartAreaIconOption(id = "explore", label = "Neues"),
)

val startAreaBlueprints = startAreaKernelSeeds.map { seed ->
    seed.toLegacyStartAreaBlueprint()
}

fun startAreaBlueprint(areaId: String): StartAreaBlueprint? {
    return kernelSeedLookup(areaId)?.toLegacyStartAreaBlueprint()
}

fun startAreaDefinition(areaId: String): AreaDefinition? {
    return kernelDefinitionLookup(areaId)
}

fun startAreaKernelBlueprint(areaId: String): AreaBlueprint? {
    return kernelBlueprintLookup(areaId)
}

fun startAreaTemplate(templateId: String): StartAreaTemplate {
    return startAreaTemplates.firstOrNull { it.id == templateId } ?: startAreaTemplates.last()
}

fun startSeedAreas(): List<LifeArea> {
    return startAreaKernelSeeds.mapIndexed { index, seed ->
        LifeArea(
            id = seed.definition.id,
            label = seed.definition.title,
            definition = seed.blueprint.summary,
            targetScore = seed.definition.defaultConfig.targetScore,
            sortOrder = index,
            isActive = true,
            templateId = seed.blueprint.defaultTemplateId,
            iconKey = seed.definition.iconKey,
        )
    }
}

fun startAreaDomains(areaId: String): Set<LifeDomain> {
    return startAreaKernelBlueprint(areaId)
        ?.domainTags
        ?.mapNotNull { tag -> LifeDomain.entries.firstOrNull { it.name == tag } }
        ?.toSet()
        .orEmpty()
}

fun startAreaBlueprint(area: LifeArea): StartAreaBlueprint {
    val preset = startAreaBlueprints.firstOrNull { it.id == area.id }
    if (preset != null) {
        return preset.copy(
            label = area.label,
            summary = area.definition,
        )
    }
    val template = startAreaTemplate(area.templateId)
    return StartAreaBlueprint(
        id = area.id,
        label = area.label,
        summary = area.definition.ifBlank { template.summary },
        tracks = template.tracks,
        drive = template.drive,
        tier = StartAreaTier.Secondary,
        defaultTargetScore = area.targetScore,
        defaultTemplateId = area.templateId,
        defaultIconKey = area.iconKey,
        domains = emptySet(),
        entries = listOf(
            StartAreaEntry(StartAreaPanel.Snapshot, "Aktueller Status", "Lage und Resonanz dieses Bereichs lesen.", Icons.Outlined.Insights),
            StartAreaEntry(StartAreaPanel.Path, "Richtung", "Ausrichtung, Rhythmus und naechsten Schritt setzen.", Icons.Outlined.Route),
            StartAreaEntry(StartAreaPanel.Options, "Flow", "Material, Erinnern und Review im Takt halten.", Icons.Outlined.Settings),
        ),
    )
}

fun startAreaBlueprint(instance: AreaInstance): StartAreaBlueprint {
    val preset = startAreaBlueprints.firstOrNull { it.id == instance.definitionId }
    if (preset != null) {
        return preset.copy(
            id = instance.areaId,
            label = instance.title,
            summary = instance.summary,
            defaultTargetScore = instance.targetScore,
            defaultTemplateId = instance.templateId ?: preset.defaultTemplateId,
            defaultIconKey = instance.iconKey,
        )
    }
    val template = startAreaTemplate(instance.templateId ?: "free")
    return StartAreaBlueprint(
        id = instance.areaId,
        label = instance.title,
        summary = instance.summary.ifBlank { template.summary },
        tracks = template.tracks,
        drive = template.drive,
        tier = StartAreaTier.Secondary,
        defaultTargetScore = instance.targetScore,
        defaultTemplateId = instance.templateId ?: template.id,
        defaultIconKey = instance.iconKey,
        domains = emptySet(),
        entries = listOf(
            StartAreaEntry(StartAreaPanel.Snapshot, "Aktueller Status", "Lage und Resonanz dieses Bereichs lesen.", Icons.Outlined.Insights),
            StartAreaEntry(StartAreaPanel.Path, "Richtung", "Ausrichtung, Rhythmus und naechsten Schritt setzen.", Icons.Outlined.Route),
            StartAreaEntry(StartAreaPanel.Options, "Flow", "Material, Erinnern und Review im Takt halten.", Icons.Outlined.Settings),
        ),
    )
}

private fun StartAreaKernelSeed.toLegacyStartAreaBlueprint(): StartAreaBlueprint {
    return StartAreaBlueprint(
        id = definition.id,
        label = definition.title,
        summary = blueprint.summary,
        tracks = blueprint.trackLabels,
        drive = definition.overviewMode.toStartAreaDrive(),
        tier = definition.complexityLevel.toStartAreaTier(),
        defaultTargetScore = definition.defaultConfig.targetScore,
        defaultTemplateId = blueprint.defaultTemplateId,
        defaultIconKey = definition.iconKey,
        domains = blueprint.domainTags
            .mapNotNull { tag -> LifeDomain.entries.firstOrNull { it.name == tag } }
            .toSet(),
        entries = listOf(
            blueprint.panelContentSeeds.getValue(AreaPanelKind.LAGE).toLegacyEntry(
                panel = StartAreaPanel.Snapshot,
                icon = Icons.Outlined.Insights,
            ),
            blueprint.panelContentSeeds.getValue(AreaPanelKind.RICHTUNG).toLegacyEntry(
                panel = StartAreaPanel.Path,
                icon = Icons.Outlined.Route,
            ),
            blueprint.panelContentSeeds.getValue(AreaPanelKind.FLOW).toLegacyEntry(
                panel = StartAreaPanel.Options,
                icon = Icons.Outlined.Settings,
            ),
        ),
    )
}

private fun AreaOverviewMode.toStartAreaDrive(): StartAreaDrive {
    return when (this) {
        AreaOverviewMode.SIGNAL -> StartAreaDrive.Signal
        AreaOverviewMode.PLAN -> StartAreaDrive.Plan
        AreaOverviewMode.REFLECTION,
        AreaOverviewMode.HYBRID,
        -> StartAreaDrive.Reflection
    }
}

private fun AreaComplexityLevel.toStartAreaTier(): StartAreaTier {
    return when (this) {
        AreaComplexityLevel.BASIC -> StartAreaTier.Core
        AreaComplexityLevel.ADVANCED -> StartAreaTier.Secondary
        AreaComplexityLevel.EXPERT -> StartAreaTier.Review
    }
}

private fun com.struperto.androidappdays.domain.area.AreaPanelContentSeed.toLegacyEntry(
    panel: StartAreaPanel,
    icon: ImageVector,
): StartAreaEntry {
    return StartAreaEntry(
        panel = panel,
        title = title,
        summary = summary,
        icon = icon,
    )
}

fun startAreaIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "heart" -> Icons.Outlined.FavoriteBorder
        "focus" -> Icons.Outlined.CenterFocusStrong
        "briefcase" -> Icons.Outlined.WorkOutline
        "care" -> Icons.Outlined.VolunteerActivism
        "family" -> Icons.Outlined.FamilyRestroom
        "chat" -> Icons.Outlined.Forum
        "groups" -> Icons.Outlined.Groups
        "home" -> Icons.Outlined.Home
        "shield" -> Icons.Outlined.Savings
        "lotus" -> Icons.Outlined.SelfImprovement
        "trend" -> Icons.AutoMirrored.Outlined.TrendingUp
        "book" -> Icons.Outlined.AutoStories
        "palette" -> Icons.Outlined.Palette
        "spark" -> Icons.Outlined.AutoAwesome
        "compass" -> Icons.Outlined.Explore
        "explore" -> Icons.Outlined.TravelExplore
        else -> Icons.AutoMirrored.Outlined.DirectionsWalk
    }
}

fun startAreaSourcesIcon(): ImageVector = Icons.Outlined.Sync
