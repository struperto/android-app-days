package com.struperto.androidappdays.data.repository

data class LifeAreaPreset(
    val id: String,
    val label: String,
    val definition: String,
    val targetScore: Int,
)

val allLifeAreaPresets = listOf(
    LifeAreaPreset("vitality", "Vitalitaet", "Koerper, Schlaf, Bewegung und Energie im Gleichgewicht halten.", 4),
    LifeAreaPreset("clarity", "Klarheit", "Aufmerksamkeit, Ruhe und mentale Ordnung schuetzen.", 4),
    LifeAreaPreset("impact", "Wirken", "Arbeit, Projekte und Vorhaben in echten Schritten bewegen.", 4),
    LifeAreaPreset("bond", "Verbundenheit", "Nahe Beziehungen bewusst pflegen und praesent bleiben.", 4),
    LifeAreaPreset("family", "Familie", "Familie, Care und gemeinsame Verantwortung tragfaehig halten.", 4),
    LifeAreaPreset("friends", "Freundeskreis", "Freundschaften aktiv, leicht und tragend halten.", 3),
    LifeAreaPreset("community", "Gemeinschaft", "Netzwerke, Zugehoerigkeit und Beitrag lebendig halten.", 3),
    LifeAreaPreset("home", "Zuhause", "Raum, Haushalt und Atmosphaere so gestalten, dass Alltag leichter wird.", 3),
    LifeAreaPreset("stability", "Sicherheit", "Geld, Verwaltung und Puffer vorausschauend im Griff halten.", 3),
    LifeAreaPreset("recovery", "Erholung", "Pausen, Reset und Regeneration bewusst aufbauen.", 4),
    LifeAreaPreset("growth", "Wachstum", "Die eigene Entwicklung nicht dem Zufall ueberlassen.", 3),
    LifeAreaPreset("learning", "Lernen", "Wissen vertiefen und Koennen Schritt fuer Schritt ausbauen.", 3),
    LifeAreaPreset("creativity", "Kreativitaet", "Ideen, Ausdruck und Experimente sichtbar machen.", 3),
    LifeAreaPreset("joy", "Freude", "Spiel, Genuss und Leichtigkeit im Alltag bewahren.", 3),
    LifeAreaPreset("meaning", "Sinn", "Handeln an Werten, Richtung und Tiefe ausrichten.", 4),
    LifeAreaPreset("discovery", "Entdeckung", "Neues, Horizonte und frische Erfahrungen offen halten.", 3),
)

fun defaultLifeAreas(): List<LifeArea> {
    return allLifeAreaPresets.mapIndexed { index, preset ->
        LifeArea(
            id = preset.id,
            label = preset.label,
            definition = preset.definition,
            targetScore = preset.targetScore,
            sortOrder = index,
            isActive = true,
        )
    }
}
