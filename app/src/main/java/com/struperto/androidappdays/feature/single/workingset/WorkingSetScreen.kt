package com.struperto.androidappdays.feature.single.workingset

import androidx.compose.runtime.Composable
import com.struperto.androidappdays.feature.single.shared.SingleSectionItem
import com.struperto.androidappdays.feature.single.shared.SingleSectionScreen

@Composable
fun WorkingSetScreen(onBack: () -> Unit) {
    SingleSectionScreen(
        title = "Vorhaben",
        eyebrow = "Single / Working Set",
        summary = "Das Working Set ist der aktive Tageskern. Statt einer langen Liste halten wir hier nur die wenigen aktuell wichtigen Vorhaben.",
        items = listOf(
            SingleSectionItem(
                title = "Wizard oder Direktpflege",
                detail = "Spaeter soll dieser Screen sowohl einen ruhigen Setup-Weg als auch schnelles Editieren bieten.",
            ),
            SingleSectionItem(
                title = "Reihenfolge",
                detail = "Die Reihenfolge der Vorhaben soll sichtbar Prioritaet geben und direkt auf Home gespiegelt werden.",
            ),
            SingleSectionItem(
                title = "Kontext",
                detail = "Jedes Vorhaben bekommt Platz fuer Outcome, naechste Aktion und zugehoerige Lebensbereiche.",
            ),
        ),
        footer = "Dieser Screen wird einer der wichtigsten Orte der App. Hier entscheiden wir spaeter sehr bewusst, wie viel Text und wie viel Visualisierung bleiben soll.",
        onBack = onBack,
    )
}
