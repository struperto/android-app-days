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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.feature.single.shared.ChoiceChipItem
import com.struperto.androidappdays.feature.single.shared.ChoiceChipRow
import com.struperto.androidappdays.feature.single.shared.DaysMetaPill
import com.struperto.androidappdays.feature.single.shared.DaysPageScaffold
import com.struperto.androidappdays.ui.theme.AppTheme

@Composable
fun StartCreateScreen(
    draft: CreateAreaDraft,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onMeaningChange: (String) -> Unit,
    onBehaviorClassChange: (AreaBehaviorClass) -> Unit,
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
        title = "Bereich anlegen",
        onBack = onBack,
        modifier = Modifier.testTag("start-create-screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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
                        label = { Text("Titel") },
                        placeholder = { Text("z.B. Podcast Ideen") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = draft.meaning,
                        onValueChange = onMeaningChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start-create-meaning"),
                        label = { Text("Bedeutung") },
                        placeholder = { Text(template.summary) },
                        minLines = 2,
                        maxLines = 3,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Nutzungsart",
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
                Text("Bereich starten")
            }
        }
    }
}

private fun behaviorLabel(
    behaviorClass: AreaBehaviorClass,
): String {
    return when (behaviorClass) {
        AreaBehaviorClass.TRACKING -> "Tracking"
        AreaBehaviorClass.PROGRESS -> "Progress"
        AreaBehaviorClass.RELATIONSHIP -> "Beziehung"
        AreaBehaviorClass.MAINTENANCE -> "Pflege"
        AreaBehaviorClass.PROTECTION -> "Schutz"
        AreaBehaviorClass.REFLECTION -> "Reflexion"
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
        title = "Vorlage und Icon",
        onBack = onBack,
        modifier = Modifier.testTag("start-create-options-screen"),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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
                        text = "Vorlage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChoiceChipRow(
                        items = startAreaTemplates.map { ChoiceChipItem(id = it.id, label = it.label) },
                        selectedId = draft.templateId,
                        onSelect = onTemplateChange,
                    )
                    Text(
                        text = template.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        template.tracks.take(4).forEach { track ->
                            DaysMetaPill(label = track)
                        }
                    }
                    HorizontalDivider()
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
                        Text("Neutral starten")
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
                        text = "Vorlage und Icon optional",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Unten ruhig halten, bei Bedarf hier verfeinern.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
