package com.struperto.androidappdays.feature.single.schedule

import androidx.compose.runtime.Composable
import com.struperto.androidappdays.feature.single.shared.SingleSectionItem
import com.struperto.androidappdays.feature.single.shared.SingleSectionScreen

@Composable
fun DayScheduleScreen(onBack: () -> Unit) {
    SingleSectionScreen(
        title = "Zeitplan",
        eyebrow = "Single / SOLL",
        summary = "Der Zeitplan legt die SOLL-Form fuer den Tag fest. Er ist keine klassische Kalenderansicht, sondern die ruhige Gegenkurve zum IST.",
        items = listOf(
            SingleSectionItem(
                title = "24h Profil",
                detail = "Wir halten die Struktur absichtlich einfach: ein Profil fuer den Tag statt komplexer Wochenlogik.",
            ),
            SingleSectionItem(
                title = "Vergleichbar",
                detail = "Der Home-Mirror soll spaeter direkt zeigen, wo IST und SOLL auseinanderlaufen.",
            ),
            SingleSectionItem(
                title = "Leicht aenderbar",
                detail = "Die Eingabe muss schnell gehen, sonst wird sie im Alltag nicht gepflegt.",
            ),
        ),
        footer = "Hier sollten wir nach deinem Feedback entscheiden, ob der Screen grafischer, reduzierter oder textaermer werden soll.",
        onBack = onBack,
    )
}
