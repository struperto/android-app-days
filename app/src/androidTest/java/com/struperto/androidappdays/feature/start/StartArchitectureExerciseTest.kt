package com.struperto.androidappdays.feature.start

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.data.repository.AreaSourceBinding
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.area.AreaSnapshot
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartArchitectureExerciseTest {
    @Test
    fun resetAndSeedArchitectureExerciseMatrix() = runBlocking {
        val app = app()
        val container = app.appContainer
        val today = LocalDate.now(container.clock)
        val now = Instant.now(container.clock)

        container.ensureAppBootstrapped()
        container.areaSourceBindingRepository.clearAll()
        container.notificationSignalRepository.clearAll()
        container.lifeWheelRepository.completeSetup(architectureExerciseAreas())

        architectureExerciseBindings().forEach { binding ->
            container.areaSourceBindingRepository.bind(
                areaId = binding.areaId,
                source = binding.source,
            )
        }

        architectureExerciseNotifications(now.toEpochMilli()).forEach { signal ->
            container.notificationSignalRepository.upsert(
                id = signal.id,
                packageName = signal.packageName,
                title = signal.title,
                text = signal.text,
                postedAt = signal.postedAt,
            )
        }

        architectureExerciseSnapshots(today, now).forEach { snapshot ->
            container.areaKernelRepository.upsertSnapshot(snapshot)
        }
    }

    private fun app(): DaysApp {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp
    }
}

private fun architectureExerciseAreas(): List<LifeArea> {
    return listOf(
        exerciseArea(
            id = "lab-screenshot-radar",
            label = "Screenshot Radar",
            definition = "Neue Screenshots lesen und nur Namen, Zahlen oder Aufgaben hervorheben.",
            templateId = "medium",
            iconKey = "palette",
        ),
        exerciseArea(
            id = "lab-photo-recap",
            label = "Foto Rueckblick",
            definition = "Neue Fotos ruhig ordnen und nur auffaellige Inhalte sichtbar machen.",
            templateId = "medium",
            iconKey = "palette",
        ),
        exerciseArea(
            id = "lab-website-watch",
            label = "Website Radar",
            definition = "Einzelne Seiten oder Feeds im Blick halten, ohne jedes Detail aufzurufen.",
            templateId = "medium",
            iconKey = "book",
        ),
        exerciseArea(
            id = "lab-podcast-follow",
            label = "Podcast Folgen",
            definition = "Neue Folgen und Hoerimpulse ruhig sichtbar machen.",
            templateId = "medium",
            iconKey = "book",
        ),
        exerciseArea(
            id = "lab-calendar-today",
            label = "Kalender Heute",
            definition = "Besprechungen, Termine und Druck aus dem Kalender frueh lesbar machen.",
            templateId = "project",
            iconKey = "calendar",
            targetScore = 4,
        ),
        exerciseArea(
            id = "lab-notification-filter",
            label = "Benachrichtigungen",
            definition = "Wichtige Hinweise aus Benachrichtigungen vom Rauschen trennen.",
            templateId = "theme",
            iconKey = "shield",
            targetScore = 4,
        ),
        exerciseArea(
            id = "lab-contact-priority",
            label = "Kontakt Prioritaet",
            definition = "Wichtige Menschen im Alltag nicht uebersehen.",
            templateId = "person",
            iconKey = "chat",
            targetScore = 4,
        ),
        exerciseArea(
            id = "lab-home-routine",
            label = "Zuhause Routinen",
            definition = "Wenn ich zuhause bin, sollen kleine Routinen oder Hinweise ruhig auftauchen.",
            templateId = "place",
            iconKey = "home",
        ),
        exerciseArea(
            id = "lab-sleep-view",
            label = "Schlaf Blick",
            definition = "Schlaf, Erholung und Tagesform lesbar machen, ohne in Zahlen unterzugehen.",
            templateId = "ritual",
            iconKey = "heart",
            targetScore = 4,
        ),
        exerciseArea(
            id = "lab-movement-energy",
            label = "Bewegung & Energie",
            definition = "Bewegung und Energie ueber den Tag hinweg ruhig sichtbar halten.",
            templateId = "ritual",
            iconKey = "trend",
        ),
        exerciseArea(
            id = "lab-work-focus",
            label = "Arbeitsfokus",
            definition = "Wichtige Arbeitslinien, Prioritaeten und Stoerquellen klarer sortieren.",
            templateId = "project",
            iconKey = "briefcase",
            targetScore = 4,
        ),
        exerciseArea(
            id = "lab-admin-fristen",
            label = "Admin & Fristen",
            definition = "Verwaltung und Fristen frueh sichtbar machen, bevor sie kippen.",
            templateId = "project",
            iconKey = "shield",
            targetScore = 4,
        ),
        exerciseArea(
            id = "lab-inbox-ideen",
            label = "Inbox fuer Ideen",
            definition = "Lose Screens, Links und Gedanken erst sammeln und spaeter sauber zuordnen.",
            templateId = "free",
            iconKey = "spark",
        ),
    ).mapIndexed { index, area -> area.copy(sortOrder = index) }
}

private fun architectureExerciseBindings(): List<AreaSourceBinding> {
    return listOf(
        AreaSourceBinding("lab-calendar-today", DataSourceKind.CALENDAR),
        AreaSourceBinding("lab-notification-filter", DataSourceKind.NOTIFICATIONS),
        AreaSourceBinding("lab-contact-priority", DataSourceKind.NOTIFICATIONS),
        AreaSourceBinding("lab-sleep-view", DataSourceKind.HEALTH_CONNECT),
        AreaSourceBinding("lab-movement-energy", DataSourceKind.HEALTH_CONNECT),
        AreaSourceBinding("lab-admin-fristen", DataSourceKind.CALENDAR),
    )
}

private data class ExerciseNotificationSeed(
    val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
)

private fun architectureExerciseNotifications(nowMillis: Long): List<ExerciseNotificationSeed> {
    return listOf(
        ExerciseNotificationSeed(
            id = "lab-notification-1",
            packageName = "com.whatsapp",
            title = "Nora",
            text = "Kannst du die Unterlagen heute noch pruefen?",
            postedAt = nowMillis - 5 * 60_000,
        ),
        ExerciseNotificationSeed(
            id = "lab-notification-2",
            packageName = "com.google.android.gm",
            title = "Frist endet heute",
            text = "Die Rueckmeldung zur Rechnung ist noch offen.",
            postedAt = nowMillis - 18 * 60_000,
        ),
        ExerciseNotificationSeed(
            id = "lab-notification-3",
            packageName = "com.slack",
            title = "Projekt Alpha",
            text = "Build ist fehlgeschlagen.",
            postedAt = nowMillis - 42 * 60_000,
        ),
    )
}

private fun architectureExerciseSnapshots(
    today: LocalDate,
    now: Instant,
): List<AreaSnapshot> {
    return listOf(
        AreaSnapshot(
            areaId = "lab-screenshot-radar",
            date = today,
            manualNote = "Heute nur zwei relevante Screens mit Zahlen und Namen.",
            freshnessAt = now,
        ),
        AreaSnapshot(
            areaId = "lab-work-focus",
            date = today,
            manualScore = 2,
            manualNote = "Zu viele Spruenge zwischen Admin und Produkt.",
            freshnessAt = now,
        ),
        AreaSnapshot(
            areaId = "lab-inbox-ideen",
            date = today,
            manualStateKey = "warm",
            manualNote = "Drei lose Ideen warten noch auf Zuordnung.",
            freshnessAt = now,
        ),
    )
}

private fun exerciseArea(
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
