package com.struperto.androidappdays.feature.single.lifewheel

import androidx.compose.runtime.Composable
import com.struperto.androidappdays.feature.single.shared.SingleSectionItem
import com.struperto.androidappdays.feature.single.shared.SingleSectionScreen

@Composable
fun LifeWheelScreen(onBack: () -> Unit) {
    SingleSectionScreen(
        title = "Lebensrad",
        eyebrow = "Single / Balance",
        summary = "Das Lebensrad bleibt die ruhige Balance-Schicht. Hier definieren wir Bereiche, Bedeutung und spaeter die taegliche Bewertung.",
        items = listOf(
            SingleSectionItem(
                title = "Bereiche",
                detail = "Gesundheit, Fokus, Beziehungen oder eigene Kategorien werden hier verwaltet.",
            ),
            SingleSectionItem(
                title = "Bedeutung",
                detail = "Jeder Bereich soll spaeter einen kurzen Zweck und einen klaren Beobachtungswert bekommen.",
            ),
            SingleSectionItem(
                title = "Trend",
                detail = "Die Detailansicht soll heutige Einordnung und 7-Tage-Richtung nebeneinander zeigen.",
            ),
        ),
        footer = "Im naechsten Schritt bauen wir die echte Bereichsverwaltung und die erste lokale Persistenz dafuer.",
        onBack = onBack,
    )
}
