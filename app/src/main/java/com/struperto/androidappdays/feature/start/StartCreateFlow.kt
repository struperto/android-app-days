package com.struperto.androidappdays.feature.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.domain.area.AreaSkillKind
import com.struperto.androidappdays.domain.area.TileDisplayMode
import com.struperto.androidappdays.domain.area.skillDefinition
import com.struperto.androidappdays.domain.area.skillsForKeywords
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.ui.theme.AppTheme

enum class StartCreateInputKind(
    val id: String,
    val label: String,
    val fieldLabel: String,
    val placeholder: String,
    val icon: ImageVector,
) {
    Text(
        id = "text",
        label = "Text",
        fieldLabel = "Was soll dieser Bereich fuer dich tun?",
        placeholder = "z.B. Ich will immer sehen, was neue Screenshots zeigen.",
        icon = Icons.AutoMirrored.Outlined.TextSnippet,
    ),
    Link(
        id = "link",
        label = "Link",
        fieldLabel = "Welcher Link oder Feed soll hier eine Rolle spielen?",
        placeholder = "z.B. https://... und ich will nur die wichtigsten neuen Folgen sehen.",
        icon = Icons.Outlined.Link,
    ),
    Screenshot(
        id = "screenshot",
        label = "Screenshot",
        fieldLabel = "Was soll Days aus Screenshots fuer dich herauslesen?",
        placeholder = "z.B. Neue Screenshots lesen und nur das Wichtige hervorheben.",
        icon = Icons.Outlined.PhotoLibrary,
    ),
    App(
        id = "app",
        label = "App",
        fieldLabel = "Welche App oder Quelle soll dieser Bereich spaeter nutzen?",
        placeholder = "z.B. Nachrichten von X lesen und ruhig zusammenfassen.",
        icon = Icons.Outlined.Widgets,
    ),
    Contact(
        id = "contact",
        label = "Kontakt",
        fieldLabel = "Welche Person oder welches Signal soll hier auffallen?",
        placeholder = "z.B. Wenn X schreibt, will ich es sofort sehen.",
        icon = Icons.Outlined.PersonOutline,
    ),
    Location(
        id = "location",
        label = "Ort",
        fieldLabel = "An welchem Ort soll etwas Besonderes passieren?",
        placeholder = "z.B. Wenn ich zuhause bin, will ich an Abendroutine erinnert werden.",
        icon = Icons.Outlined.LocationOn,
    ),
}

data class StartIntentSuggestion(
    val id: String,
    val title: String,
    val summary: String,
    val templateId: String,
    val iconKey: String,
    val behaviorClass: AreaBehaviorClass,
    val sourceKind: DataSourceKind? = null,
    val skills: Set<AreaSkillKind> = emptySet(),
    val modeLabel: String,
    val hintLabel: String,
)

@Composable
fun StartCreateScreen(
    inputKind: StartCreateInputKind,
    inputText: String,
    suggestions: List<StartIntentSuggestion>,
    selectedSuggestionId: String?,
    onBack: () -> Unit,
    onInputKindChange: (StartCreateInputKind) -> Unit,
    onInputTextChange: (String) -> Unit,
    onSuggestionSelect: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val selectedSuggestion = suggestions.firstOrNull { it.id == selectedSuggestionId } ?: suggestions.firstOrNull()

    DaysPageScaffold(
        title = "Neuer Bereich",
        onBack = onBack,
        modifier = Modifier.testTag("start-create-screen"),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
            ),
            shape = RoundedCornerShape(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Eingabe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChoiceChipRow(
                        items = StartCreateInputKind.entries.map { kind ->
                            ChoiceChipItem(id = kind.id, label = kind.label)
                        },
                        selectedId = inputKind.id,
                        onSelect = { selected ->
                            onInputKindChange(
                                StartCreateInputKind.entries.first { it.id == selected },
                            )
                        },
                    )
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start-create-input"),
                    label = { Text(inputKind.fieldLabel) },
                    placeholder = { Text(inputKind.placeholder) },
                    minLines = 4,
                    maxLines = 6,
                    singleLine = false,
                )
            }
        }
        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Vorschlaege",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                suggestions.forEach { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        selected = suggestion.id == selectedSuggestion?.id,
                        onClick = { onSuggestionSelect(suggestion.id) },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onContinue,
            enabled = inputText.trim().isNotBlank() || selectedSuggestion != null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start-create-next"),
        ) {
            Text("Weiter")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartCreateConfirmScreen(
    draft: CreateAreaDraft,
    inputKind: StartCreateInputKind,
    inputText: String,
    selectedSuggestion: StartIntentSuggestion?,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onMeaningChange: (String) -> Unit,
    onBehaviorClassChange: (AreaBehaviorClass) -> Unit,
    onSourceKindChange: (DataSourceKind?) -> Unit,
    onSkillToggle: (AreaSkillKind) -> Unit,
    onOpenIdentityOptions: () -> Unit,
    onCreate: () -> Unit,
) {
    val template = startAreaTemplate(draft.templateId)
    val iconLabel = startAreaIconOptions
        .firstOrNull { it.id == draft.iconKey }
        ?.label
        .orEmpty()
    val canCreate = draft.title.trim().length >= 2
    val summaryPreview = draft.meaning.trim().ifBlank { template.summary }

    DaysPageScaffold(
        title = "Bereich pruefen",
        onBack = onBack,
        modifier = Modifier.testTag("start-confirm-screen"),
    ) {
        StartCreatePreviewCard(
            title = draft.title,
            summary = summaryPreview,
            templateLabel = template.label,
            iconKey = draft.iconKey,
            iconLabel = iconLabel,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.98f),
            ),
            shape = RoundedCornerShape(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start-create-title"),
                    label = { Text("Name") },
                    placeholder = { Text("z.B. Podcast Ideen") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draft.meaning,
                    onValueChange = onMeaningChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start-create-meaning"),
                    label = { Text("Kurzbeschreibung") },
                    placeholder = { Text(template.summary) },
                    minLines = 3,
                    maxLines = 5,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Wie soll Days damit umgehen?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChoiceChipRow(
                        items = AreaBehaviorClass.entries.map { behavior ->
                            ChoiceChipItem(
                                id = behavior.persistedValue,
                                label = behaviorLabel(behavior),
                            )
                        },
                        selectedId = draft.behaviorClass.persistedValue,
                        onSelect = { selected ->
                            onBehaviorClassChange(AreaBehaviorClass.fromPersistedValue(selected))
                        },
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Skills",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AreaSkillKind.entries.forEach { skill ->
                            val isSelected = skill in draft.selectedSkills
                            val def = skillDefinition(skill)
                            ChoiceChipRow(
                                items = listOf(
                                    ChoiceChipItem(
                                        id = skill.persistedValue,
                                        label = if (def.permissionKey != null && !isSelected) {
                                            "${def.label} \uD83D\uDD12"
                                        } else {
                                            def.label
                                        },
                                    ),
                                ),
                                selectedId = if (isSelected) skill.persistedValue else "",
                                onSelect = { onSkillToggle(skill) },
                            )
                        }
                    }
                    if (draft.selectedSkills.isEmpty()) {
                        Text(
                            text = "Noch kein Skill gewaehlt. Der Bereich startet ohne Datenquelle.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        StartCreateIdentityLinkCard(
            templateLabel = template.label,
            iconLabel = iconLabel,
            onClick = onOpenIdentityOptions,
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onCreate,
            enabled = canCreate,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start-create-save"),
        ) {
            Text("Erstellen")
        }
    }
}

private fun behaviorLabel(
    behaviorClass: AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> "Beobachten"
        AreaBehaviorClass.PROGRESS -> "Voranbringen"
        AreaBehaviorClass.RELATIONSHIP -> "Beziehung"
        AreaBehaviorClass.MAINTENANCE -> "Pflegen"
        AreaBehaviorClass.PROTECTION -> "Absichern"
        AreaBehaviorClass.REFLECTION -> "Einordnen"
    }
}

@Composable
fun StartCreateIdentityOptionsScreen(
    draft: CreateAreaDraft,
    onBack: () -> Unit,
    onTemplateChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onResetToNeutral: () -> Unit,
) {
    val template = startAreaTemplate(draft.templateId)

    DaysPageScaffold(
        title = "Darstellung anpassen",
        onBack = onBack,
        modifier = Modifier.testTag("start-create-options-screen"),
    ) {
        StartCreatePreviewCard(
            title = draft.title,
            summary = draft.meaning.trim().ifBlank { template.summary },
            templateLabel = template.label,
            iconKey = draft.iconKey,
            iconLabel = startAreaIconOptions.firstOrNull { it.id == draft.iconKey }?.label.orEmpty(),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
            ),
            shape = RoundedCornerShape(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Grundform",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ChoiceChipRow(
                    items = startAreaTemplates.map { ChoiceChipItem(id = it.id, label = it.label) },
                    selectedId = draft.templateId,
                    onSelect = onTemplateChange,
                )
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ChoiceChipRow(
                    items = startAreaIconOptions.map { ChoiceChipItem(id = it.id, label = it.label) },
                    selectedId = draft.iconKey,
                    onSelect = onIconChange,
                )
                TextButton(
                    onClick = onResetToNeutral,
                    modifier = Modifier.testTag("start-create-reset-neutral"),
                ) {
                    Text("Neutral bleiben")
                }
            }
        }
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start-create-options-done"),
        ) {
            Text("Zurueck")
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: StartIntentSuggestion,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val outlineColor = if (selected) {
        AppTheme.colors.accent
    } else {
        AppTheme.colors.outlineSoft
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.96f))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = outlineColor.copy(alpha = if (selected) 0.42f else 0.18f),
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = startAreaIcon(suggestion.iconKey),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = suggestion.modeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StartCreatePreviewCard(
    title: String,
    summary: String,
    templateLabel: String,
    iconKey: String,
    iconLabel: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong.copy(alpha = 0.94f),
        ),
        shape = RoundedCornerShape(30.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = startAreaIcon(iconKey),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title.ifBlank { "Neuer Bereich" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DaysMetaPill(label = templateLabel)
                    if (iconLabel.isNotBlank()) {
                        DaysMetaPill(label = iconLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun StartCreateIdentityLinkCard(
    templateLabel: String,
    iconLabel: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = AppTheme.colors.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .testTag("start-create-options-link"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.surfaceMuted.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Darstellung anpassen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DaysMetaPill(label = templateLabel)
            DaysMetaPill(label = iconLabel)
        }
    }
}

fun buildStartIntentSuggestions(
    inputKind: StartCreateInputKind,
    rawInput: String,
): List<StartIntentSuggestion> {
    val normalized = rawInput.trim()
    val inferred = inferPrimaryIntent(inputKind, normalized)
    val baseTitle = inferred.title.ifBlank { fallbackTitleFor(inputKind) }
    val summarySource = normalized.ifBlank { inferred.defaultSummary }
    val keywordSkills = skillsForKeywords(normalized).toSet()
    val primary = StartIntentSuggestion(
        id = "primary-${inputKind.id}",
        title = baseTitle,
        summary = inferred.primarySummary(summarySource),
        templateId = inferred.primaryTemplateId,
        iconKey = inferred.primaryIconKey,
        behaviorClass = inferred.primaryBehavior,
        sourceKind = inferred.primarySourceKind,
        skills = inferred.primarySkills.ifEmpty { keywordSkills },
        modeLabel = inferred.primaryModeLabel,
        hintLabel = inferred.primaryHint,
    )
    val secondary = StartIntentSuggestion(
        id = "secondary-${inputKind.id}",
        title = baseTitle,
        summary = inferred.secondarySummary(summarySource),
        templateId = inferred.secondaryTemplateId,
        iconKey = inferred.secondaryIconKey,
        behaviorClass = inferred.secondaryBehavior,
        sourceKind = inferred.secondarySourceKind,
        skills = inferred.secondarySkills.ifEmpty { keywordSkills },
        modeLabel = inferred.secondaryModeLabel,
        hintLabel = inferred.secondaryHint,
    )
    val exploration = StartIntentSuggestion(
        id = "explore-${inputKind.id}",
        title = if (normalized.isBlank()) fallbackTitleFor(inputKind) else "$baseTitle Radar",
        summary = "Ein freier Bereich, der zuerst nur sichtbar macht, was hier relevant werden koennte.",
        templateId = "free",
        iconKey = "spark",
        behaviorClass = AreaBehaviorClass.REFLECTION,
        modeLabel = "Offen starten",
        hintLabel = "Gut, wenn du die Aufgabe schon spueren kannst, aber die beste Darstellung noch offen ist.",
    )
    return listOf(primary, secondary, exploration)
}

private data class InferredStartIntent(
    val title: String,
    val defaultSummary: String,
    val primaryTemplateId: String,
    val primaryIconKey: String,
    val primaryBehavior: AreaBehaviorClass,
    val primaryModeLabel: String,
    val primaryHint: String,
    val primarySourceKind: DataSourceKind? = null,
    val primarySkills: Set<AreaSkillKind> = emptySet(),
    val secondaryTemplateId: String,
    val secondaryIconKey: String,
    val secondaryBehavior: AreaBehaviorClass,
    val secondaryModeLabel: String,
    val secondaryHint: String,
    val secondarySourceKind: DataSourceKind? = null,
    val secondarySkills: Set<AreaSkillKind> = emptySet(),
) {
    fun primarySummary(source: String): String {
        return when (primaryBehavior) {
            AreaBehaviorClass.TRACKING -> "Dieser Bereich liest lokal, was hier neu oder auffaellig ist: ${source.take(140)}"
            AreaBehaviorClass.PROGRESS -> "Dieser Bereich soll aus dem Input sichtbare Folgeaktionen oder Routinen formen: ${source.take(140)}"
            AreaBehaviorClass.RELATIONSHIP -> "Dieser Bereich haelt Beziehung, Resonanz oder Kontakt aus dem Input zusammen: ${source.take(140)}"
            AreaBehaviorClass.MAINTENANCE -> "Dieser Bereich soll Pflege und Regelmaessigkeit rund um den Input tragen: ${source.take(140)}"
            AreaBehaviorClass.PROTECTION -> "Dieser Bereich soll Warnungen, Stoerungen oder wichtige Ausnahmen aus dem Input ruhig sichtbar machen: ${source.take(140)}"
            AreaBehaviorClass.REFLECTION -> "Dieser Bereich soll Material aus dem Input ordnen und verdichten: ${source.take(140)}"
        }
    }

    fun secondarySummary(source: String): String {
        return when (secondaryBehavior) {
            AreaBehaviorClass.TRACKING -> "Der Bereich beobachtet zuerst nur leise, was rund um ${source.take(110)} passiert."
            AreaBehaviorClass.PROGRESS -> "Der Bereich macht aus ${source.take(110)} spaeter konkrete naechste Zuege."
            AreaBehaviorClass.RELATIONSHIP -> "Der Bereich verbindet ${source.take(110)} mit Kontakt oder Resonanz."
            AreaBehaviorClass.MAINTENANCE -> "Der Bereich pflegt ${source.take(110)} regelmaessig und ohne Druck."
            AreaBehaviorClass.PROTECTION -> "Der Bereich schuetzt dich vor uebersehenen Signalen aus ${source.take(110)}."
            AreaBehaviorClass.REFLECTION -> "Der Bereich sammelt und sortiert ${source.take(110)} zuerst ruhig."
        }
    }
}

private fun inferPrimaryIntent(
    inputKind: StartCreateInputKind,
    rawInput: String,
): InferredStartIntent {
    val lower = rawInput.lowercase()
    val domainTitle = detectTitle(rawInput)
    return when {
        "kalender" in lower || "termin" in lower || "meeting" in lower || "besprech" in lower -> InferredStartIntent(
            title = domainTitle.ifBlank { "Kalender Heute" },
            defaultSummary = "Lokale Termine und Besprechungen nur als kompakten Tagesstand zeigen.",
            primaryTemplateId = "project",
            primaryIconKey = "calendar",
            primaryBehavior = AreaBehaviorClass.PROGRESS,
            primaryModeLabel = "Termine lesen",
            primaryHint = "Passt, wenn der Bereich lokale Kalendertermine direkt mitnehmen soll.",
            primarySourceKind = DataSourceKind.CALENDAR,
            primarySkills = setOf(AreaSkillKind.CALENDAR_WATCH),
            secondaryTemplateId = "place",
            secondaryIconKey = "home",
            secondaryBehavior = AreaBehaviorClass.MAINTENANCE,
            secondaryModeLabel = "Termine einbetten",
            secondaryHint = "Passt, wenn der Bereich Kalender eher in eine wiederkehrende Tagesroutine einbetten soll.",
            secondarySourceKind = DataSourceKind.CALENDAR,
            secondarySkills = setOf(AreaSkillKind.CALENDAR_WATCH),
        )
        "podcast" in lower || "folge" in lower || "feed" in lower -> InferredStartIntent(
            title = domainTitle.ifBlank { "Podcast Radar" },
            defaultSummary = "Neue Folgen und Hoerimpulse ruhig sichtbar machen.",
            primaryTemplateId = "medium",
            primaryIconKey = "book",
            primaryBehavior = AreaBehaviorClass.REFLECTION,
            primaryModeLabel = "Folgen lesen",
            primaryHint = "Passt, wenn der Bereich neue Folgen oder Inhalte erst sammeln und nur das Wesentliche zeigen soll.",
            primarySkills = setOf(AreaSkillKind.PODCAST_FOLLOW),
            secondaryTemplateId = "project",
            secondaryIconKey = "trend",
            secondaryBehavior = AreaBehaviorClass.PROGRESS,
            secondaryModeLabel = "Folgen handeln",
            secondaryHint = "Passt, wenn aus neuen Folgen spaeter konkrete Hoer- oder Lernzuege entstehen sollen.",
            secondarySkills = setOf(AreaSkillKind.PODCAST_FOLLOW),
        )
        "screenshot" in lower || "screen" in lower || "bild" in lower || inputKind == StartCreateInputKind.Screenshot -> InferredStartIntent(
            title = domainTitle.ifBlank { "Screenshot Radar" },
            defaultSummary = "Neue Screenshots lesen und nur auffaellige Inhalte sichtbar machen.",
            primaryTemplateId = "medium",
            primaryIconKey = "palette",
            primaryBehavior = AreaBehaviorClass.TRACKING,
            primaryModeLabel = "Screens lesen",
            primaryHint = "Passt, wenn Screenshots lokal gelesen und auf relevante Inhalte reduziert werden sollen.",
            primarySkills = setOf(AreaSkillKind.SCREENSHOT_READER),
            secondaryTemplateId = "theme",
            secondaryIconKey = "focus",
            secondaryBehavior = AreaBehaviorClass.REFLECTION,
            secondaryModeLabel = "Muster finden",
            secondaryHint = "Passt, wenn Screenshots eher gesammelt, geordnet und spaeter gedeutet werden sollen.",
            secondarySkills = setOf(AreaSkillKind.SCREENSHOT_READER),
        )
        "nachricht" in lower || "notification" in lower || "benachr" in lower || "kontakt" in lower || inputKind == StartCreateInputKind.Contact -> InferredStartIntent(
            title = domainTitle.ifBlank { "Kontakt Blick" },
            defaultSummary = "Wichtige Nachrichten und Absender im Blick halten.",
            primaryTemplateId = "person",
            primaryIconKey = "chat",
            primaryBehavior = AreaBehaviorClass.PROTECTION,
            primaryModeLabel = "Wichtiges sehen",
            primaryHint = "Passt, wenn nur die wirklich relevanten Kontakte oder Nachrichten auffallen sollen.",
            primarySourceKind = DataSourceKind.NOTIFICATIONS,
            primarySkills = setOf(AreaSkillKind.NOTIFICATION_FILTER, AreaSkillKind.CONTACT_WATCH),
            secondaryTemplateId = "person",
            secondaryIconKey = "care",
            secondaryBehavior = AreaBehaviorClass.RELATIONSHIP,
            secondaryModeLabel = "Kontakt pflegen",
            secondaryHint = "Passt, wenn aus Signalen eher Beziehungspflege und Follow-ups entstehen sollen.",
            secondarySourceKind = DataSourceKind.NOTIFICATIONS,
            secondarySkills = setOf(AreaSkillKind.NOTIFICATION_FILTER, AreaSkillKind.CONTACT_WATCH),
        )
        "zuhause" in lower || "home" in lower || "ort" in lower || inputKind == StartCreateInputKind.Location -> InferredStartIntent(
            title = domainTitle.ifBlank { "Ort Routine" },
            defaultSummary = "Am richtigen Ort die passende Routine oder Aufmerksamkeit sichtbar machen.",
            primaryTemplateId = "place",
            primaryIconKey = "home",
            primaryBehavior = AreaBehaviorClass.MAINTENANCE,
            primaryModeLabel = "Ort Routine",
            primaryHint = "Passt, wenn ein Bereich an einen Ort gebunden ruhig erinnern oder mitlaufen soll.",
            primarySkills = setOf(AreaSkillKind.LOCATION_CONTEXT),
            secondaryTemplateId = "place",
            secondaryIconKey = "shield",
            secondaryBehavior = AreaBehaviorClass.PROTECTION,
            secondaryModeLabel = "Ort Schutz",
            secondaryHint = "Passt, wenn am Ort eher Warnungen, Blocker oder wichtige Kontexte sichtbar werden sollen.",
            secondarySkills = setOf(AreaSkillKind.LOCATION_CONTEXT),
        )
        "gesund" in lower || "sleep" in lower || "schlaf" in lower || "health" in lower -> InferredStartIntent(
            title = domainTitle.ifBlank { "Gesundheit Blick" },
            defaultSummary = "Koerper- und Gesundheitsdaten ruhig sichtbar halten.",
            primaryTemplateId = "ritual",
            primaryIconKey = "heart",
            primaryBehavior = AreaBehaviorClass.TRACKING,
            primaryModeLabel = "Koerper lesen",
            primaryHint = "Passt, wenn der Bereich Scores, Trends oder koerperliche Signale ruhig anzeigen soll.",
            primarySourceKind = DataSourceKind.HEALTH_CONNECT,
            primarySkills = setOf(AreaSkillKind.HEALTH_TRACKING),
            secondaryTemplateId = "ritual",
            secondaryIconKey = "lotus",
            secondaryBehavior = AreaBehaviorClass.MAINTENANCE,
            secondaryModeLabel = "Gesund bleiben",
            secondaryHint = "Passt, wenn aus Koerpersignalen spaeter kleine Pflegezuege entstehen sollen.",
            secondarySourceKind = DataSourceKind.HEALTH_CONNECT,
            secondarySkills = setOf(AreaSkillKind.HEALTH_TRACKING),
        )
        inputKind == StartCreateInputKind.Link -> InferredStartIntent(
            title = domainTitle.ifBlank { "Link Radar" },
            defaultSummary = "Inhalte aus einem Link oder Feed in Ruhe beobachten.",
            primaryTemplateId = "medium",
            primaryIconKey = "book",
            primaryBehavior = AreaBehaviorClass.REFLECTION,
            primaryModeLabel = "Quelle lesen",
            primaryHint = "Passt, wenn eine Quelle zuerst gesammelt, gelesen und verdichtet werden soll.",
            primarySkills = setOf(AreaSkillKind.WEBSITE_READER),
            secondaryTemplateId = "project",
            secondaryIconKey = "briefcase",
            secondaryBehavior = AreaBehaviorClass.PROGRESS,
            secondaryModeLabel = "Quelle nutzen",
            secondaryHint = "Passt, wenn aus einer Quelle spaeter klare Schritte oder Routinen entstehen sollen.",
            secondarySkills = setOf(AreaSkillKind.WEBSITE_READER),
        )
        else -> InferredStartIntent(
            title = domainTitle.ifBlank { fallbackTitleFor(inputKind) },
            defaultSummary = "Einen neuen Bereich ruhig einspielen und spaeter verfeinern.",
            primaryTemplateId = fallbackTemplateFor(inputKind),
            primaryIconKey = fallbackIconFor(inputKind),
            primaryBehavior = fallbackBehaviorFor(inputKind),
            primaryModeLabel = "Ruhig starten",
            primaryHint = "Passt, wenn du die Absicht schon klar spuerst und Days zuerst nur einen brauchbaren Bereich vorschlagen soll.",
            secondaryTemplateId = "theme",
            secondaryIconKey = "focus",
            secondaryBehavior = AreaBehaviorClass.REFLECTION,
            secondaryModeLabel = "Erst ordnen",
            secondaryHint = "Passt, wenn der Bereich noch offen ist und du das Thema zuerst sammeln und sortieren willst.",
        )
    }
}

private fun sourceKindLabel(sourceKind: DataSourceKind): String {
    return when (sourceKind) {
        DataSourceKind.CALENDAR -> "Kalender"
        DataSourceKind.NOTIFICATIONS -> "Benachrichtigungen"
        DataSourceKind.HEALTH_CONNECT -> "Health Connect"
        DataSourceKind.MANUAL -> "Manuell"
    }
}

private fun detectTitle(rawInput: String): String {
    val trimmed = rawInput.trim()
    if (trimmed.isBlank()) return ""
    val hostMatch = Regex("""https?://(?:www\.)?([^/\s]+)""").find(trimmed)
    if (hostMatch != null) {
        return hostMatch.groupValues[1]
            .substringBefore('.')
            .replaceFirstChar(Char::uppercaseChar)
    }
    val cleaned = trimmed
        .replace(Regex("""https?://\S+"""), "")
        .replace(Regex("""[^\p{L}\p{N}\s]"""), " ")
        .trim()
    val words = cleaned
        .split(Regex("""\s+"""))
        .filter { it.length > 2 }
        .take(3)
    return words.joinToString(" ").replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}

private fun fallbackTitleFor(inputKind: StartCreateInputKind): String {
    return when (inputKind) {
        StartCreateInputKind.Text -> "Neuer Bereich"
        StartCreateInputKind.Link -> "Link Radar"
        StartCreateInputKind.Screenshot -> "Screenshot Radar"
        StartCreateInputKind.App -> "App Bereich"
        StartCreateInputKind.Contact -> "Kontakt Blick"
        StartCreateInputKind.Location -> "Ort Routine"
    }
}

private fun fallbackTemplateFor(inputKind: StartCreateInputKind): String {
    return when (inputKind) {
        StartCreateInputKind.Text -> "free"
        StartCreateInputKind.Link -> "medium"
        StartCreateInputKind.Screenshot -> "medium"
        StartCreateInputKind.App -> "theme"
        StartCreateInputKind.Contact -> "person"
        StartCreateInputKind.Location -> "place"
    }
}

private fun fallbackIconFor(inputKind: StartCreateInputKind): String {
    return when (inputKind) {
        StartCreateInputKind.Text -> "spark"
        StartCreateInputKind.Link -> "book"
        StartCreateInputKind.Screenshot -> "palette"
        StartCreateInputKind.App -> "focus"
        StartCreateInputKind.Contact -> "chat"
        StartCreateInputKind.Location -> "home"
    }
}

private fun fallbackBehaviorFor(inputKind: StartCreateInputKind): AreaBehaviorClass {
    return when (inputKind) {
        StartCreateInputKind.Text -> AreaBehaviorClass.REFLECTION
        StartCreateInputKind.Link -> AreaBehaviorClass.REFLECTION
        StartCreateInputKind.Screenshot -> AreaBehaviorClass.TRACKING
        StartCreateInputKind.App -> AreaBehaviorClass.PROGRESS
        StartCreateInputKind.Contact -> AreaBehaviorClass.PROTECTION
        StartCreateInputKind.Location -> AreaBehaviorClass.MAINTENANCE
    }
}

private fun startCreateExamples(
    inputKind: StartCreateInputKind,
): List<String> {
    return when (inputKind) {
        StartCreateInputKind.Text -> listOf(
            "Ich will immer wissen, ob irgendwo etwas Wichtiges neu ist.",
            "Ich will diesen Bereich nur als ruhiges Radar und nicht als Arbeitsmodus.",
        )
        StartCreateInputKind.Link -> listOf(
            "Ich moechte diese Seite nur auf neue relevante Inhalte pruefen.",
            "Aus diesem Feed will ich spaeter nur Highlights sehen.",
        )
        StartCreateInputKind.Screenshot -> listOf(
            "Neue Screenshots lesen und nur Namen, Zahlen oder To-dos hervorheben.",
            "Screenshots nur als stilles Archiv anzeigen, bis ich sie brauche.",
        )
        StartCreateInputKind.App -> listOf(
            "Wenn in dieser App etwas Wichtiges auftaucht, will ich es direkt sehen.",
            "Diese App soll spaeter nur als Quelle fuer ruhige Hinweise dienen.",
        )
        StartCreateInputKind.Contact -> listOf(
            "Wenn X schreibt, will ich es von allem anderen unterscheiden koennen.",
            "Dieser Bereich soll Kontakt nur sichtbar machen, nicht gleich Handlung fordern.",
        )
        StartCreateInputKind.Location -> listOf(
            "Wenn ich zuhause bin, will ich an eine kleine Routine erinnert werden.",
            "An diesem Ort soll nur auffallen, wenn etwas fehlt oder schief laeuft.",
        )
    }
}
