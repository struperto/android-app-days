package com.struperto.androidappdays.feature.start

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.struperto.androidappdays.ui.theme.AppTheme

data class StartAreaUiVisuals(
    val background: List<Color>,
    val chrome: Color,
    val outline: Color,
    val signal: Color,
    val iconTint: Color,
)

data class StartAreaPanelCopy(
    val title: String,
    val actionLabel: String,
)

@Composable
fun startAreaUiVisuals(
    family: StartAreaFamily,
    hintTone: StartAreaHintTone,
): StartAreaUiVisuals {
    val familyColor = when (family) {
        StartAreaFamily.Radar -> AppTheme.colors.info
        StartAreaFamily.Pflicht -> AppTheme.colors.accent
        StartAreaFamily.Routine -> AppTheme.colors.warning
        StartAreaFamily.Kontakt -> AppTheme.colors.success
        StartAreaFamily.Gesundheit -> Color(0xFF64876B)
        StartAreaFamily.Ort -> Color(0xFF9F7357)
        StartAreaFamily.Sammlung -> Color(0xFF7E6E97)
    }
    val signal = when (hintTone) {
        StartAreaHintTone.Warning -> AppTheme.colors.danger
        StartAreaHintTone.Notice -> AppTheme.colors.warning
        StartAreaHintTone.Quiet -> familyColor
    }
    return StartAreaUiVisuals(
        background = listOf(
            familyColor.copy(alpha = 0.16f),
            familyColor.copy(alpha = 0.06f),
            AppTheme.colors.surfaceStrong.copy(alpha = 0.96f),
        ),
        chrome = familyColor.copy(alpha = 0.14f),
        outline = signal,
        signal = signal,
        iconTint = AppTheme.colors.ink,
    )
}

fun startAreaPanelCopy(
    _family: StartAreaFamily,
    panel: StartAreaPanel,
): StartAreaPanelCopy {
    return when (panel) {
        StartAreaPanel.Snapshot -> StartAreaPanelCopy("Aktueller Status", "Status oeffnen")
        StartAreaPanel.Path -> StartAreaPanelCopy("Sortieren", "Sortieren oeffnen")
        StartAreaPanel.Sources -> StartAreaPanelCopy("Hinzufuegen", "Hinzufuegen oeffnen")
        StartAreaPanel.Options -> StartAreaPanelCopy("Im Feed", "Im Feed oeffnen")
    }
}

fun startAreaTileActionLabel(
    family: StartAreaFamily,
    primaryHint: StartAreaHintState,
): String {
    return when (primaryHint.id) {
        "source-missing" -> "Einrichten"
        "calendar-not-connected" -> "Verbinden"
        "calendar-permission" -> "Freigeben"
        "notifications-not-connected" -> "Verbinden"
        "notifications-permission" -> "Freigeben"
        "health-not-connected" -> "Verbinden"
        "health-permission" -> "Freigeben"
        "meaning-open" -> "Schaerfen"
        else -> when (family) {
            StartAreaFamily.Radar -> "Bearbeiten"
            StartAreaFamily.Pflicht -> "Pruefen"
            StartAreaFamily.Routine -> "Pflegen"
            StartAreaFamily.Kontakt -> "Pflegen"
            StartAreaFamily.Gesundheit -> "Einsehen"
            StartAreaFamily.Ort -> "Ordnen"
            StartAreaFamily.Sammlung -> "Sortieren"
        }
    }
}

fun startAreaTileStatusLine(
    family: StartAreaFamily,
    hint: StartAreaHintState,
    todayLabel: String,
    summary: String,
    statusLabel: String,
): String {
    val normalizedToday = todayLabel.substringAfter(": ").trim()
    val normalizedStatus = statusLabel.trim()
    val primary = when {
        normalizedToday.isNotBlank() && !normalizedToday.equals(hint.compactLabel, ignoreCase = true) -> normalizedToday
        else -> hint.compactLabel
    }
    val preferredStatus = when {
        normalizedStatus.isBlank() -> primary
        normalizedStatus.equals(primary, ignoreCase = true) -> normalizedStatus
        normalizedStatus.equals("Quelle fehlt", ignoreCase = true) &&
            !hint.compactLabel.equals("Quelle fehlt", ignoreCase = true) -> hint.compactLabel
        else -> normalizedStatus
    }
    if (
        hint.id.startsWith("calendar-") ||
        hint.id.startsWith("notifications-") ||
        hint.id.startsWith("health-")
    ) {
        return preferredStatus
    }
    return when (family) {
        StartAreaFamily.Radar -> preferredStatus
        StartAreaFamily.Pflicht -> preferredStatus
        StartAreaFamily.Routine -> summary.ifBlank { preferredStatus }
        StartAreaFamily.Kontakt -> summary.ifBlank { preferredStatus }
        StartAreaFamily.Gesundheit -> summary.ifBlank { preferredStatus }
        StartAreaFamily.Ort -> summary.ifBlank { preferredStatus }
        StartAreaFamily.Sammlung -> summary.ifBlank { preferredStatus }
    }
}

fun startAreaDetailSectionTitle(family: StartAreaFamily): String {
    return when (family) {
        StartAreaFamily.Radar -> "Heute im Blick"
        StartAreaFamily.Pflicht -> "Heute im Stand"
        StartAreaFamily.Routine -> "Heute in der Routine"
        StartAreaFamily.Kontakt -> "Heute im Kontakt"
        StartAreaFamily.Gesundheit -> "Heute in deiner Lage"
        StartAreaFamily.Ort -> "Heute am Ort"
        StartAreaFamily.Sammlung -> "Heute im Eingang"
    }
}

fun startAreaManualNoteLabel(family: StartAreaFamily): String {
    return when (family) {
        StartAreaFamily.Radar -> "Heute-Notiz"
        StartAreaFamily.Pflicht -> "Standnotiz"
        StartAreaFamily.Routine -> "Pflegenotiz"
        StartAreaFamily.Kontakt -> "Kontaktnotiz"
        StartAreaFamily.Gesundheit -> "Koerpernotiz"
        StartAreaFamily.Ort -> "Ortshinweis"
        StartAreaFamily.Sammlung -> "Sammelnotiz"
    }
}

fun startAreaManualNotePlaceholder(family: StartAreaFamily): String {
    return when (family) {
        StartAreaFamily.Radar -> "Kurz notieren, was heute neu, wichtig oder lesenswert ist"
        StartAreaFamily.Pflicht -> "Kurz festhalten, was offen, faellig oder blockiert ist"
        StartAreaFamily.Routine -> "Kurz notieren, was erhalten oder wiederholt werden soll"
        StartAreaFamily.Kontakt -> "Ton, letzter Impuls oder naechstes Follow-up"
        StartAreaFamily.Gesundheit -> "Kurz notieren, was dein Koerper oder deine Tagesform zeigt"
        StartAreaFamily.Ort -> "Kurz notieren, was an diesem Ort heute wichtig ist"
        StartAreaFamily.Sammlung -> "Kurz notieren, was gesammelt und spaeter sortiert werden soll"
    }
}

fun startAreaPanelAccentColor(
    family: StartAreaFamily,
    panel: StartAreaPanel,
): Color {
    val base = when (family) {
        StartAreaFamily.Radar -> Color(0xFF5C7FA8)
        StartAreaFamily.Pflicht -> Color(0xFFE07A5F)
        StartAreaFamily.Routine -> Color(0xFFC8933F)
        StartAreaFamily.Kontakt -> Color(0xFF4F8A5B)
        StartAreaFamily.Gesundheit -> Color(0xFF64876B)
        StartAreaFamily.Ort -> Color(0xFF9F7357)
        StartAreaFamily.Sammlung -> Color(0xFF7E6E97)
    }
    return when (panel) {
        StartAreaPanel.Snapshot -> base
        StartAreaPanel.Path -> base.copy(alpha = 0.92f)
        StartAreaPanel.Sources -> base.copy(alpha = 0.8f)
        StartAreaPanel.Options -> base.copy(alpha = 0.68f)
    }
}
