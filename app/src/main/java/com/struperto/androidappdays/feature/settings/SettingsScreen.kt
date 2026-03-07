package com.struperto.androidappdays.feature.settings

import androidx.compose.runtime.Composable
import com.struperto.androidappdays.feature.single.shared.SingleSectionItem
import com.struperto.androidappdays.feature.single.shared.SingleSectionScreen

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    SingleSectionScreen(
        title = "Einstellungen",
        eyebrow = "Single / System",
        summary = "Die Einstellungen bleiben in V1 klein. Hier landen nur Dinge, die den Kernfluss unterstuetzen statt ihn zu zerfasern.",
        items = listOf(
            SingleSectionItem(
                title = "Startmodus",
                detail = "Da die App erstmal Single-first bleibt, halten wir die Einstellung bewusst schlank.",
            ),
            SingleSectionItem(
                title = "Rhythmus",
                detail = "Reminder und Tagesstruktur kommen hierhin, wenn sie den Living Mirror wirklich staerken.",
            ),
            SingleSectionItem(
                title = "Debug und Experimente spaeter",
                detail = "Alles Interne oder Lab-artige bleibt aus dem V1-Kern draussen.",
            ),
        ),
        footer = "Der wichtigste Grundsatz: Einstellungen sollen den Alltag klaeren, nicht Produktentscheidungen verstecken.",
        onBack = onBack,
    )
}
