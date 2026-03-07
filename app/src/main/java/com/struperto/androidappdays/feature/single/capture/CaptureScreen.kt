package com.struperto.androidappdays.feature.single.capture

import androidx.compose.runtime.Composable
import com.struperto.androidappdays.feature.single.shared.SingleSectionItem
import com.struperto.androidappdays.feature.single.shared.SingleSectionScreen

@Composable
fun CaptureScreen(onBack: () -> Unit) {
    SingleSectionScreen(
        title = "Capture",
        eyebrow = "Single / Input",
        summary = "Capture ist der ruhige Eingang fuer Gedanken, Beobachtungen und kleine Signale aus dem Tag. Fuer V1 starten wir bewusst textnah.",
        items = listOf(
            SingleSectionItem(
                title = "Kurz und direkt",
                detail = "Spaeter soll das Erfassen mit sehr wenig Reibung funktionieren und den Tagesfluss nicht unterbrechen.",
            ),
            SingleSectionItem(
                title = "Signal fuer Home",
                detail = "Ein Capture ist nicht nur ein Eintrag, sondern ein Baustein fuer den Mirror und die naechsten Aktionen.",
            ),
            SingleSectionItem(
                title = "Erweiterbar",
                detail = "Medien, Sprache oder Import koennen spaeter kommen, aber nicht in die erste Struktur hineinregieren.",
            ),
        ),
        footer = "Capture sollte in V1 wahrscheinlich textzentriert bleiben. Alles Weitere haengen wir nur an, wenn der Kern sitzt.",
        onBack = onBack,
    )
}
