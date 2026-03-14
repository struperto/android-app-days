package com.struperto.androidappdays.feature.start

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.data.repository.LifeArea
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartExplorationSeedTest {
    @Test
    fun seedExplorationAreas() = runBlocking {
        app().appContainer.lifeWheelRepository.completeSetup(explorationAreas())
    }

    private fun app(): DaysApp {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp
    }
}

private fun explorationAreas(): List<LifeArea> {
    return listOf(
        lifeArea(
            id = "explore-screenshot-radar",
            label = "Screenshot Radar",
            definition = "Neue Screenshots lesen und nur Namen, Zahlen oder To-dos hervorheben.",
            templateId = "medium",
            iconKey = "palette",
            targetScore = 4,
        ),
        lifeArea(
            id = "explore-photo-recap",
            label = "Foto Rueckblick",
            definition = "Neueste Fotos ruhig ordnen und nur auffaellige Inhalte sichtbar machen.",
            templateId = "medium",
            iconKey = "palette",
        ),
        lifeArea(
            id = "explore-link-watch",
            label = "Link Radar",
            definition = "Wichtige Seiten oder Feeds im Blick halten, ohne gleich alles lesen zu muessen.",
            templateId = "medium",
            iconKey = "book",
        ),
        lifeArea(
            id = "explore-podcast-watch",
            label = "Podcast Folgen",
            definition = "Neue Folgen und Hoerimpulse ruhig sammeln, statt im Player zu verlieren.",
            templateId = "medium",
            iconKey = "book",
        ),
        lifeArea(
            id = "explore-calendar-focus",
            label = "Kalender Heute",
            definition = "Besprechungen, Blöcke und Druck aus dem Kalender frueh spuerbar machen.",
            templateId = "project",
            iconKey = "briefcase",
            targetScore = 4,
        ),
        lifeArea(
            id = "explore-notification-filter",
            label = "Benachrichtigungen",
            definition = "Wichtige Nachrichten von Rauschen trennen und nur Relevantes nach vorn holen.",
            templateId = "theme",
            iconKey = "shield",
            targetScore = 4,
        ),
        lifeArea(
            id = "explore-contact-priority",
            label = "Kontakt Prioritaet",
            definition = "Bestimmte Menschen oder Gruppen im Alltag nicht uebersehen.",
            templateId = "person",
            iconKey = "chat",
            targetScore = 4,
        ),
        lifeArea(
            id = "explore-home-routine",
            label = "Zuhause Routinen",
            definition = "Wenn ich zuhause bin, sollen kleine Routinen oder Hinweise ruhig auftauchen.",
            templateId = "place",
            iconKey = "home",
        ),
        lifeArea(
            id = "explore-place-trigger",
            label = "Orte & Wege",
            definition = "An bestimmten Orten oder unterwegs nur passende Signale und Aufgaben sehen.",
            templateId = "place",
            iconKey = "explore",
        ),
        lifeArea(
            id = "explore-sleep-view",
            label = "Schlaf Blick",
            definition = "Schlaf, Erholung und Tagesform lesbar machen, ohne in Zahlen unterzugehen.",
            templateId = "ritual",
            iconKey = "heart",
            targetScore = 4,
        ),
        lifeArea(
            id = "explore-movement-energy",
            label = "Bewegung & Energie",
            definition = "Bewegung, Aktivierung und Energie im Verlauf des Tages sichtbar halten.",
            templateId = "ritual",
            iconKey = "trend",
        ),
        lifeArea(
            id = "explore-work-focus",
            label = "Arbeitsfokus",
            definition = "Wichtige Arbeitslinien, Prioritaeten und Stoerquellen einfacher sortieren.",
            templateId = "project",
            iconKey = "briefcase",
            targetScore = 4,
        ),
        lifeArea(
            id = "explore-learning-feed",
            label = "Lernen Feed",
            definition = "Lernmaterial, Artikel und Quellen sammeln, damit sie nicht zerfasern.",
            templateId = "medium",
            iconKey = "focus",
        ),
        lifeArea(
            id = "explore-news-watch",
            label = "Nachrichten Lage",
            definition = "Nur wichtige Entwicklungen sehen, nicht den gesamten News-Strom.",
            templateId = "theme",
            iconKey = "chat",
        ),
        lifeArea(
            id = "explore-admin-deadlines",
            label = "Admin & Fristen",
            definition = "Verwaltung, Fristen und Pflichtthemen frueh und ohne Panik sichtbar machen.",
            templateId = "project",
            iconKey = "shield",
        ),
        lifeArea(
            id = "explore-money-buffer",
            label = "Finanz Radar",
            definition = "Konten, Kosten und Puffer ruhig beobachten statt erst im Problemfall.",
            templateId = "project",
            iconKey = "shield",
        ),
        lifeArea(
            id = "explore-shopping-loop",
            label = "Einkauf Signal",
            definition = "Einkauf, Nachkauf und Alltagsbedarf orts- oder kontextbezogen sichtbar machen.",
            templateId = "place",
            iconKey = "home",
        ),
        lifeArea(
            id = "explore-capture-inbox",
            label = "Inbox fuer Ideen",
            definition = "Lose Screens, Links und Gedanken erst sammeln und spaeter sauber zuordnen.",
            templateId = "free",
            iconKey = "spark",
        ),
    ).mapIndexed { index, area ->
        area.copy(sortOrder = index)
    }
}

private fun lifeArea(
    id: String,
    label: String,
    definition: String,
    templateId: String,
    iconKey: String,
    targetScore: Int = 3,
): LifeArea {
    return LifeArea(
        id = id,
        label = label,
        definition = definition,
        targetScore = targetScore,
        sortOrder = 0,
        isActive = true,
        templateId = templateId,
        iconKey = iconKey,
    )
}
