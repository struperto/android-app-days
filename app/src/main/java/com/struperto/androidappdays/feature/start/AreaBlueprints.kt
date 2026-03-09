package com.struperto.androidappdays.feature.start

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

data class StartAreaBlueprint(
    val id: String,
    val label: String,
    val summary: String,
    val tracks: List<String>,
)

val startAreaBlueprints = listOf(
    StartAreaBlueprint("vitality", "Vitalitaet", "Koerper, Schlaf, Bewegung und Energie im Gleichgewicht halten.", listOf("Rhythmus", "Energie", "Bewegung", "Nahrung")),
    StartAreaBlueprint("clarity", "Klarheit", "Aufmerksamkeit, Ruhe und mentale Ordnung schuetzen.", listOf("Fokus", "Ruhe", "Grenzen", "Digital")),
    StartAreaBlueprint("impact", "Wirken", "Arbeit, Projekte und Vorhaben in echten Schritten bewegen.", listOf("Output", "Projekte", "Prioritaeten", "Entscheidungen")),
    StartAreaBlueprint("bond", "Verbundenheit", "Nahe Beziehungen bewusst pflegen und praesent bleiben.", listOf("Naehe", "Zeit", "Gespräch", "Vertrauen")),
    StartAreaBlueprint("family", "Familie", "Familie, Care und gemeinsame Verantwortung tragfaehig halten.", listOf("Praesenz", "Care", "Alltag", "Abstimmung")),
    StartAreaBlueprint("friends", "Freundeskreis", "Freundschaften aktiv, leicht und tragend halten.", listOf("Kontakt", "Tiefe", "Leichtigkeit", "Treffen")),
    StartAreaBlueprint("community", "Gemeinschaft", "Netzwerke, Zugehoerigkeit und Beitrag lebendig halten.", listOf("Netzwerk", "Beitrag", "Zugehoerigkeit", "Resonanz")),
    StartAreaBlueprint("home", "Zuhause", "Raum, Haushalt und Atmosphaere so gestalten, dass Alltag leichter wird.", listOf("Ordnung", "Atmosphaere", "Pflege", "Routinen")),
    StartAreaBlueprint("stability", "Sicherheit", "Geld, Verwaltung und Puffer vorausschauend im Griff halten.", listOf("Finanzen", "Puffer", "Verwaltung", "Planung")),
    StartAreaBlueprint("recovery", "Erholung", "Pausen, Reset und Regeneration bewusst aufbauen.", listOf("Pausen", "Stille", "Reset", "Regeneration")),
    StartAreaBlueprint("growth", "Wachstum", "Die eigene Entwicklung nicht dem Zufall ueberlassen.", listOf("Mut", "Reflexion", "Gewohnheiten", "Naechster Schritt")),
    StartAreaBlueprint("learning", "Lernen", "Wissen vertiefen und Koennen Schritt fuer Schritt ausbauen.", listOf("Lesen", "Ueben", "Verstehen", "Transfer")),
    StartAreaBlueprint("creativity", "Kreativitaet", "Ideen, Ausdruck und Experimente sichtbar machen.", listOf("Ideen", "Skizzen", "Experiment", "Ausdruck")),
    StartAreaBlueprint("joy", "Freude", "Spiel, Genuss und Leichtigkeit im Alltag bewahren.", listOf("Spiel", "Feiern", "Genuss", "Leichtigkeit")),
    StartAreaBlueprint("meaning", "Sinn", "Handeln an Werten, Richtung und Tiefe ausrichten.", listOf("Werte", "Richtung", "Dankbarkeit", "Tiefe")),
    StartAreaBlueprint("discovery", "Entdeckung", "Neues, Horizonte und frische Erfahrungen offen halten.", listOf("Neues", "Orte", "Menschen", "Horizont")),
)

fun startAreaBlueprint(areaId: String): StartAreaBlueprint? {
    return startAreaBlueprints.firstOrNull { it.id == areaId }
}

fun startAreaIcon(areaId: String): ImageVector {
    return when (areaId) {
        "vitality" -> Icons.Outlined.FavoriteBorder
        "clarity" -> Icons.Outlined.CenterFocusStrong
        "impact" -> Icons.Outlined.WorkOutline
        "bond" -> Icons.Outlined.VolunteerActivism
        "family" -> Icons.Outlined.FamilyRestroom
        "friends" -> Icons.Outlined.Forum
        "community" -> Icons.Outlined.Groups
        "home" -> Icons.Outlined.Home
        "stability" -> Icons.Outlined.Savings
        "recovery" -> Icons.Outlined.SelfImprovement
        "growth" -> Icons.AutoMirrored.Outlined.TrendingUp
        "learning" -> Icons.Outlined.AutoStories
        "creativity" -> Icons.Outlined.Palette
        "joy" -> Icons.Outlined.AutoAwesome
        "meaning" -> Icons.Outlined.Explore
        "discovery" -> Icons.Outlined.TravelExplore
        else -> Icons.AutoMirrored.Outlined.DirectionsWalk
    }
}
