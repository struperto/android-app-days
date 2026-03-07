package com.struperto.androidappdays.feature.single.create

import androidx.compose.runtime.Composable
import com.struperto.androidappdays.feature.single.shared.SingleSectionItem
import com.struperto.androidappdays.feature.single.shared.SingleSectionScreen

@Composable
fun CreateScreen(onBack: () -> Unit) {
    SingleSectionScreen(
        title = "Create",
        eyebrow = "Single / Naechste Aufgabe",
        summary = "Create ist die Bruecke vom diffusen Input zur klaren Aktion. Hier entsteht aus einem Gedanken eine konkrete naechste Aufgabe.",
        items = listOf(
            SingleSectionItem(
                title = "Ein Output",
                detail = "Die Route sollte vor allem genau einen sauberen Task erzeugen koennen.",
            ),
            SingleSectionItem(
                title = "Naechster Schritt statt Grossplanung",
                detail = "Create soll nicht in umfangreiche Planung kippen, sondern Bewegung erzeugen.",
            ),
            SingleSectionItem(
                title = "Rueckkopplung",
                detail = "Die erzeugte Aufgabe muss spaeter direkt in Plan und Home wieder auftauchen.",
            ),
        ),
        footer = "Wenn du an diesem Screen etwas grundsaetzlich anders willst, ist das ein guter Punkt fuer visuelle und sprachliche Schaerfung.",
        onBack = onBack,
    )
}
