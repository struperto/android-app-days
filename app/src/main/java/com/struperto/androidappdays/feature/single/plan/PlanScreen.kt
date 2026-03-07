package com.struperto.androidappdays.feature.single.plan

import androidx.compose.runtime.Composable
import com.struperto.androidappdays.feature.single.shared.SingleSectionItem
import com.struperto.androidappdays.feature.single.shared.SingleSectionScreen

@Composable
fun PlanScreen(onBack: () -> Unit) {
    SingleSectionScreen(
        title = "Plan",
        eyebrow = "Single / Kernaufgaben",
        summary = "Plan bleibt eine enge Route fuer 1 bis 3 echte Aufgaben. Kein Projektboard, kein Backlog-Ersatz, sondern ein taeglicher Fokusfilter.",
        items = listOf(
            SingleSectionItem(
                title = "Hart begrenzt",
                detail = "Die enge Begrenzung schafft Fokus und macht das Home verstaendlich.",
            ),
            SingleSectionItem(
                title = "Sichtbarer Erfolg",
                detail = "Der Statuswechsel von geplant zu erledigt soll spaeter sofort in den Tagesfluss zurueckspiegeln.",
            ),
            SingleSectionItem(
                title = "Konkrete Sprache",
                detail = "Der Screen soll Nutzer zu klar benannten Vorhaben zwingen statt vager Notizen.",
            ),
        ),
        footer = "Plan ist absichtlich klein. Wenn wir hier zu viel einbauen, verliert Single sofort wieder seine Ruhe.",
        onBack = onBack,
    )
}
