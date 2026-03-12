package com.struperto.androidappdays.feature.multi

data class MultiUiState(
    val title: String = "Multi bleibt geparkt",
    val summary: String = "Dieser Modus bleibt nur als sichtbare Huelle fuer spaetere Mehrpersonenarbeit bestehen.",
    val statusLabel: String = "Nur Huelle",
    val detail: String = "Keine Koordination, keine Persona-Logik und keine Fake-Projektion laufen hier weiter.",
)
