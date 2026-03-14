package com.struperto.androidappdays.domain.area

import com.struperto.androidappdays.domain.LifeDomain

/**
 * Canonical kernel-near seed bundle for one Start area.
 *
 * The stable Start seed truth now lives here as kernel models. Legacy Start metadata can be
 * derived from this catalog while Room and UI contracts remain unchanged.
 */
data class StartAreaKernelSeed(
    val definition: AreaDefinition,
    val blueprint: AreaBlueprint,
)

private val startPassiveSignalDomains = setOf(
    LifeDomain.SLEEP,
    LifeDomain.MOVEMENT,
    LifeDomain.HYDRATION,
    LifeDomain.NUTRITION,
    LifeDomain.HEALTH,
    LifeDomain.RECOVERY,
    LifeDomain.STRESS,
    LifeDomain.SCREEN_TIME,
    LifeDomain.EMOTIONAL_STATE,
)

private val startHighSensitivityDomains = setOf(
    LifeDomain.SLEEP,
    LifeDomain.HEALTH,
    LifeDomain.MEDICATION,
    LifeDomain.EMOTIONAL_STATE,
)

private val startFoundationDomains = setOf(
    LifeDomain.SLEEP,
    LifeDomain.MOVEMENT,
    LifeDomain.HYDRATION,
    LifeDomain.NUTRITION,
    LifeDomain.HEALTH,
    LifeDomain.RECOVERY,
    LifeDomain.STRESS,
)

private val startDirectionDomains = setOf(
    LifeDomain.FOCUS,
    LifeDomain.ADMIN,
    LifeDomain.SCREEN_TIME,
)

val startAreaKernelSeeds = listOf(
    startSeed(
        id = "vitality",
        title = "Vitalitaet",
        summary = "Schlaf, Energie, Bewegung, Essen und Trinken im Takt halten.",
        tracks = listOf("Schlaf", "Energie", "Bewegung", "Essen"),
        overviewMode = AreaOverviewMode.SIGNAL,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 4,
        defaultTemplateId = "ritual",
        defaultIconKey = "heart",
        domains = setOf(LifeDomain.SLEEP, LifeDomain.MOVEMENT, LifeDomain.HYDRATION, LifeDomain.NUTRITION, LifeDomain.HEALTH),
        lageTitle = "Status",
        lageSummary = "Schlaf, Energie und Belastung lesen.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Zielbild, Rhythmus und Basis setzen.",
        flowTitle = "Automatik",
        flowSummary = "Signale, Reviews und Erinnern steuern.",
        pilotSemantics = scoreAreaSemantics(
            scorePrefix = "Signal",
            scoreCoreTitle = "Signal-Lage",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Care-Fokus",
                dailyTemplate = "Heute %s staerken",
                weeklyTemplate = "Diese Woche %s staerken",
                adaptiveTemplate = "%s zuerst",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Koerpersignale",
                mixLabel = "Signalanteil",
                channels = listOf(
                    AreaSourceChannel("Schlaf", "Nacht und Erholung bewusst lesen.", AreaSourceType.LOCAL_SIGNAL),
                    AreaSourceChannel("Energie", "Eigenes Koerpergefuehl und Kraftniveau.", AreaSourceType.MANUAL),
                    AreaSourceChannel("Bewegung", "Aktivierung und Koerpertempo wahrnehmen.", AreaSourceType.LOCAL_SIGNAL),
                    AreaSourceChannel("Essen", "Essen, Trinken und Grundversorgung einbeziehen.", AreaSourceType.MANUAL),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Routinefluss",
                intensityLabel = "Takt",
                neutralLabel = "Nur lokal",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Routine", "kleine Basispraxis sichtbar halten"),
                    AreaFlowToggleSemantics("review", "Erinnerung", "kurzen Tagescheck bewusst setzen"),
                    AreaFlowToggleSemantics("experiments", "Wiederholung", "gleiche Pflegebewegung wieder aufnehmen"),
                ),
            ),
        ),
    ),
    startSeed(
        id = "clarity",
        title = "Fokus",
        summary = "Fokus, Ruhe und Bildschirmdruck bewusst im Griff halten.",
        tracks = listOf("Fokus", "Ruhe", "Grenzen", "Bildschirm"),
        overviewMode = AreaOverviewMode.PLAN,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 4,
        defaultTemplateId = "theme",
        defaultIconKey = "focus",
        domains = setOf(LifeDomain.FOCUS, LifeDomain.STRESS, LifeDomain.SCREEN_TIME, LifeDomain.RECOVERY),
        lageTitle = "Status",
        lageSummary = "Fokus, Ruhe und Druck lesen.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Grenzen, Fokusfenster und Takt setzen.",
        flowTitle = "Automatik",
        flowSummary = "Signalmix und Reviews justieren.",
        pilotSemantics = scoreAreaSemantics(
            scorePrefix = "Richtung",
            scoreCoreTitle = "Richtungslage",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Schutzlinie",
                dailyTemplate = "Heute %s schuetzen",
                weeklyTemplate = "Diese Woche %s schuetzen",
                adaptiveTemplate = "%s fokussieren",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Fokusquellen",
                mixLabel = "Schutzgrad",
                channels = listOf(
                    AreaSourceChannel("Fokus", "die aktive Tiefenlinie fuer den Tag", AreaSourceType.MANUAL),
                    AreaSourceChannel("Ruhe", "mentale Klarheit und Reset-Raum", AreaSourceType.MANUAL),
                    AreaSourceChannel("Grenzen", "Stoerquellen und Schutzsignale", AreaSourceType.LOCAL_SIGNAL),
                    AreaSourceChannel("Bildschirm", "Screen-Pull und Druck im Blick halten", AreaSourceType.LOCAL_SIGNAL),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Schutzmodus",
                intensityLabel = "Schutz",
                neutralLabel = "Nur lokal",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Schutz", "Fokusfenster aktiv absichern"),
                    AreaFlowToggleSemantics("review", "Review", "Rueckkehr in den Fokus kurz pruefen"),
                    AreaFlowToggleSemantics("experiments", "Naechster Zug", "den klaren Rueckweg vorbereiten"),
                ),
            ),
        ),
    ),
    startSeed(
        id = "impact",
        title = "Arbeit",
        summary = "Wichtige Arbeit, Projekte und Entscheidungen klar voranbringen.",
        tracks = listOf("Prioritaet", "Tiefenarbeit", "Projekte", "Entscheidungen"),
        overviewMode = AreaOverviewMode.PLAN,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 4,
        defaultTemplateId = "project",
        defaultIconKey = "briefcase",
        domains = setOf(LifeDomain.FOCUS, LifeDomain.ADMIN, LifeDomain.SCREEN_TIME),
        lageTitle = "Status",
        lageSummary = "Was heute schon zieht und wo es haengt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Prioritaet, Tiefenarbeit und Takt setzen.",
        flowTitle = "Automatik",
        flowSummary = "Aktivierung, Reviews und Impulse steuern.",
        pilotSemantics = scoreAreaSemantics(
            scorePrefix = "Prioritaet",
            scoreCoreTitle = "Arbeitslage",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Arbeitsfokus",
                nextMoveLabel = "Naechster Zug",
                dailyTemplate = "Heute %s voranbringen",
                weeklyTemplate = "Diese Woche %s voranbringen",
                adaptiveTemplate = "%s zuerst ziehen",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Arbeitsquellen",
                mixLabel = "Zugkraft",
                channels = listOf(
                    AreaSourceChannel("Prioritaet", "die eine Linie, die heute wirklich traegt", AreaSourceType.MANUAL),
                    AreaSourceChannel("Tiefenarbeit", "Zeitblock fuer ungestoerte Arbeit", AreaSourceType.LOCAL_SIGNAL),
                    AreaSourceChannel("Projekte", "laufende Vorhaben bewusst buendeln", AreaSourceType.MANUAL),
                    AreaSourceChannel("Entscheidungen", "offene Entscheidungen nicht verdriften lassen", AreaSourceType.NOTE),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Arbeitsfluss",
                intensityLabel = "Zug",
                neutralLabel = "Nur sichtbar",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Startsignal", "den ersten Arbeitszug frueh sichtbar machen"),
                    AreaFlowToggleSemantics("review", "Review", "Arbeitslage kurz nachziehen und schaerfen"),
                    AreaFlowToggleSemantics("experiments", "Absicherung", "Stoerquellen aktiv abfedern"),
                ),
            ),
        ),
    ),
    startSeed(
        id = "bond",
        title = "Partnerschaft",
        summary = "Partnerschaft mit Zeit, Naehe und Vertrauen bewusst pflegen.",
        tracks = listOf("Naehe", "Zeit", "Gespraech", "Vertrauen"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.ADVANCED,
        defaultTargetScore = 4,
        defaultTemplateId = "person",
        defaultIconKey = "care",
        domains = setOf(LifeDomain.SOCIAL, LifeDomain.EMOTIONAL_STATE),
        lageTitle = "Status",
        lageSummary = "Naehe, Spannung und Resonanz lesen.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Zeit, Naehe und gemeinsame Spur setzen.",
        flowTitle = "Automatik",
        flowSummary = "Erinnern, Review und kleine Impulse steuern.",
    ),
    startSeed(
        id = "family",
        title = "Familie",
        summary = "Care, Alltag und gemeinsame Verantwortung gut tragbar halten.",
        tracks = listOf("Care", "Praesenz", "Alltag", "Abstimmung"),
        overviewMode = AreaOverviewMode.PLAN,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 4,
        defaultTemplateId = "person",
        defaultIconKey = "family",
        domains = setOf(LifeDomain.SOCIAL, LifeDomain.HOUSEHOLD, LifeDomain.EMOTIONAL_STATE),
        lageTitle = "Status",
        lageSummary = "Wie tragfaehig und abgestimmt es heute wirkt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Alltagsfokus, Takt und Verantwortung setzen.",
        flowTitle = "Automatik",
        flowSummary = "Reviews, Erinnern und leichte Absicherung.",
    ),
    startSeed(
        id = "friends",
        title = "Freundschaft",
        summary = "Freunde im Kontakt und im echten Leben lebendig halten.",
        tracks = listOf("Kontakt", "Tiefe", "Leichtigkeit", "Treffen"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.ADVANCED,
        defaultTargetScore = 3,
        defaultTemplateId = "person",
        defaultIconKey = "chat",
        domains = setOf(LifeDomain.SOCIAL, LifeDomain.EMOTIONAL_STATE),
        lageTitle = "Status",
        lageSummary = "Kontakt, Naehe und Resonanz lesen.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Naechste Begegnungen und Raum dafuer setzen.",
        flowTitle = "Automatik",
        flowSummary = "Erinnern, Review und kleine Impulse steuern.",
        lageType = AreaLageType.STATE,
        defaultCadenceKey = "weekly",
        pilotSemantics = stateAreaSemantics(
            lageOptions = listOf(
                AreaLageStateOption("warm", "Warm", "Kontakt fuehlt sich lebendig und leicht an.", AreaLageStateTone.STABLE, 0.9f),
                AreaLageStateOption("offen", "Offen", "Verbindung ist da, aber ohne frischen Impuls.", AreaLageStateTone.LIVE, 0.62f),
                AreaLageStateOption("fern", "Fern", "Es braucht wieder einen bewussten Kontakt.", AreaLageStateTone.PULL, 0.36f),
                AreaLageStateOption("still", "Still", "Freundschaft ist gerade zu weit weggerutscht.", AreaLageStateTone.PULL, 0.18f),
            ),
            stateCoreTitle = "Beziehungslage",
            stateInfoLabel = "Reflexion",
            stateMetricValue = "Reflexion",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Beziehungsfokus",
                dailyTemplate = "Heute %s pflegen",
                weeklyTemplate = "Diese Woche %s pflegen",
                adaptiveTemplate = "%s aufnehmen",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Kontaktspuren",
                mixLabel = "Naehe",
                channels = listOf(
                    AreaSourceChannel("Kontakt", "Kontaktimpulse und letzte Beruehrungspunkte.", AreaSourceType.MANUAL),
                    AreaSourceChannel("Tiefe", "echte Gespraeche und Resonanz.", AreaSourceType.NOTE),
                    AreaSourceChannel("Leichtigkeit", "leichte spontane Momente erinnern.", AreaSourceType.NOTE),
                    AreaSourceChannel("Treffen", "offene oder konkrete Begegnungen im Blick halten.", AreaSourceType.MANUAL),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Sanfter Flow",
                intensityLabel = "Sanftheit",
                neutralLabel = "Nur offen halten",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Impuls", "einen kleinen Kontaktimpuls freundlich setzen"),
                    AreaFlowToggleSemantics("review", "Reflexion", "die Beziehung kurz und bewusst anschauen"),
                    AreaFlowToggleSemantics("experiments", "Follow-up", "nach einem Kontakt locker dranbleiben"),
                ),
            ),
        ),
    ),
    startSeed(
        id = "community",
        title = "Netzwerk",
        summary = "Menschen, Gruppen und Zugehoerigkeit im Leben aktiv halten.",
        tracks = listOf("Gruppen", "Beitrag", "Zugehoerigkeit", "Resonanz"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.ADVANCED,
        defaultTargetScore = 3,
        defaultTemplateId = "person",
        defaultIconKey = "groups",
        domains = setOf(LifeDomain.SOCIAL),
        lageTitle = "Status",
        lageSummary = "Wie verbunden und lebendig es gerade wirkt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Wofuer du Raum geben willst.",
        flowTitle = "Automatik",
        flowSummary = "Reviews und leichte Impulse steuern.",
    ),
    startSeed(
        id = "home",
        title = "Zuhause",
        summary = "Raum, Ordnung und Atmosphaere so gestalten, dass Alltag leichter wird.",
        tracks = listOf("Ordnung", "Pflege", "Atmosphaere", "Routinen"),
        overviewMode = AreaOverviewMode.PLAN,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 3,
        defaultTemplateId = "place",
        defaultIconKey = "home",
        domains = setOf(LifeDomain.HOUSEHOLD),
        lageTitle = "Status",
        lageSummary = "Wie tragend und ruhig es heute wirkt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Routinen und Alltagsschwerpunkte setzen.",
        flowTitle = "Optionen",
        flowSummary = "Erinnern, Mix und Review.",
    ),
    startSeed(
        id = "stability",
        title = "Sicherheit",
        summary = "Finanzen, Verwaltung und Puffer vorausschauend stabil halten.",
        tracks = listOf("Finanzen", "Puffer", "Verwaltung", "Vorsorge"),
        overviewMode = AreaOverviewMode.PLAN,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 3,
        defaultTemplateId = "theme",
        defaultIconKey = "shield",
        domains = setOf(LifeDomain.ADMIN),
        lageTitle = "Status",
        lageSummary = "Wie sicher und vorbereitet es gerade wirkt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Zielniveau, Vorsorge und Takt definieren.",
        flowTitle = "Automatik",
        flowSummary = "Reviews, Erinnern und Sicherungslogik steuern.",
    ),
    startSeed(
        id = "recovery",
        title = "Erholung",
        summary = "Pausen, Abschalten und Regeneration bewusst absichern.",
        tracks = listOf("Pausen", "Stille", "Abschalten", "Reset"),
        overviewMode = AreaOverviewMode.SIGNAL,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 4,
        defaultTemplateId = "ritual",
        defaultIconKey = "lotus",
        domains = setOf(LifeDomain.RECOVERY, LifeDomain.SLEEP, LifeDomain.EMOTIONAL_STATE),
        lageTitle = "Status",
        lageSummary = "Spannung, Leere und Reset lesen.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Pausen, Schutz und Wiederholung setzen.",
        flowTitle = "Automatik",
        flowSummary = "Signale und ruhige Prompts steuern.",
        lageType = AreaLageType.STATE,
        defaultCadenceKey = "daily",
        pilotSemantics = stateAreaSemantics(
            lageOptions = listOf(
                AreaLageStateOption("getragen", "Getragen", "Regeneration ist spuerbar und der Koerper kommt mit.", AreaLageStateTone.STABLE, 0.9f),
                AreaLageStateOption("ruhig", "Ruhig", "Erholung ist da, aber noch fragil.", AreaLageStateTone.LIVE, 0.64f),
                AreaLageStateOption("leer", "Leer", "Es fehlt gerade an echter Regeneration.", AreaLageStateTone.PULL, 0.34f),
                AreaLageStateOption("ueberreizt", "Ueberreizt", "Nervensystem und Kopf brauchen spuerbar Schutz.", AreaLageStateTone.PULL, 0.16f),
            ),
            stateCoreTitle = "Erholungslage",
            stateInfoLabel = "Erholung",
            stateMetricValue = "Regeneration",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Regenerationsfokus",
                nextMoveLabel = "Naechster Ruhezug",
                dailyTemplate = "Heute %s schuetzen",
                weeklyTemplate = "Diese Woche %s vertiefen",
                adaptiveTemplate = "%s sanft sichern",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Regenerationsquellen",
                mixLabel = "Regeneration",
                channels = listOf(
                    AreaSourceChannel("Pausen", "echte Pausenfenster wieder oeffnen", AreaSourceType.MANUAL),
                    AreaSourceChannel("Stille", "Reizarmut und Ruhe bewusst suchen", AreaSourceType.LOCAL_SIGNAL),
                    AreaSourceChannel("Abschalten", "mentales Abschalten aktiv erlauben", AreaSourceType.MANUAL),
                    AreaSourceChannel("Reset", "kleine Rueckkehrpunkte im Tag spuerbar machen", AreaSourceType.LOCAL_SIGNAL),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Sanfter Flow",
                intensityLabel = "Sanftheit",
                neutralLabel = "Nur Raum",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Pause", "kleine Pausen rechtzeitig erinnern"),
                    AreaFlowToggleSemantics("review", "Rueckkehr", "Erholungslage kurz nachspueren"),
                    AreaFlowToggleSemantics("experiments", "Wiederholung", "eine ruhige Regeneration wiederholen"),
                ),
            ),
        ),
    ),
    startSeed(
        id = "growth",
        title = "Entwicklung",
        summary = "Eigene Entwicklung mit Gewohnheiten und naechsten Schritten voranbringen.",
        tracks = listOf("Mut", "Reflexion", "Gewohnheiten", "Naechster Schritt"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.ADVANCED,
        defaultTargetScore = 3,
        defaultTemplateId = "theme",
        defaultIconKey = "trend",
        domains = setOf(LifeDomain.EMOTIONAL_STATE, LifeDomain.FOCUS),
        lageTitle = "Status",
        lageSummary = "Wie getragen und lebendig Entwicklung gerade ist.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Gewohnheiten und naechsten Schritt setzen.",
        flowTitle = "Automatik",
        flowSummary = "Experimente und Review pflegen.",
    ),
    startSeed(
        id = "learning",
        title = "Lernen",
        summary = "Wissen und Koennen im Alltag wirklich aufbauen.",
        tracks = listOf("Lesen", "Ueben", "Verstehen", "Anwenden"),
        overviewMode = AreaOverviewMode.PLAN,
        complexityLevel = AreaComplexityLevel.BASIC,
        defaultTargetScore = 3,
        defaultTemplateId = "medium",
        defaultIconKey = "book",
        domains = setOf(LifeDomain.FOCUS),
        lageTitle = "Status",
        lageSummary = "Wieviel heute schon sitzt und was offen ist.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Lernspur und Takt setzen.",
        flowTitle = "Automatik",
        flowSummary = "Reviews und Impulse steuern.",
        pilotSemantics = scoreAreaSemantics(
            scorePrefix = "Fortschritt",
            scoreCoreTitle = "Lernstand",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Lernfokus",
                nextMoveLabel = "Naechster Lernzug",
                dailyTemplate = "Heute %s vertiefen",
                weeklyTemplate = "Diese Woche %s aufbauen",
                adaptiveTemplate = "%s zuerst ueben",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Lernquellen",
                mixLabel = "Lernmix",
                channels = listOf(
                    AreaSourceChannel("Lesen", "Input sauber aufnehmen und markieren", AreaSourceType.NOTE),
                    AreaSourceChannel("Ueben", "Wissen aktiv durch Wiederholung festigen", AreaSourceType.MANUAL),
                    AreaSourceChannel("Verstehen", "offene Fragen und Begriffe klaeren", AreaSourceType.NOTE),
                    AreaSourceChannel("Anwenden", "Transfer in echte Anwendung bringen", AreaSourceType.MANUAL),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Lernfluss",
                intensityLabel = "Rhythmus",
                neutralLabel = "Nur sichtbar",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Lernslot", "kleines Lernfenster lokal sichtbar halten"),
                    AreaFlowToggleSemantics("review", "Review", "Gelerntes kurz rueckholen und festigen"),
                    AreaFlowToggleSemantics("experiments", "Transfer", "das Gelernte sofort in Anwendung ziehen"),
                ),
            ),
        ),
    ),
    startSeed(
        id = "creativity",
        title = "Kreativitaet",
        summary = "Ideen und Ausdruck nicht nur sammeln, sondern machen.",
        tracks = listOf("Ideen", "Skizzen", "Versuch", "Ausdruck"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.ADVANCED,
        defaultTargetScore = 3,
        defaultTemplateId = "free",
        defaultIconKey = "palette",
        domains = setOf(LifeDomain.EMOTIONAL_STATE, LifeDomain.FOCUS),
        lageTitle = "Status",
        lageSummary = "Wie lebendig und offen es heute wirkt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Fokus und naechsten Versuch setzen.",
        flowTitle = "Automatik",
        flowSummary = "Experimente und Review rahmen.",
    ),
    startSeed(
        id = "joy",
        title = "Freude",
        summary = "Spiel, Genuss und leichte Momente im Alltag wirklich zulassen.",
        tracks = listOf("Spiel", "Genuss", "Leichtigkeit", "Erleben"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.EXPERT,
        defaultTargetScore = 3,
        defaultTemplateId = "feeling",
        defaultIconKey = "spark",
        domains = setOf(LifeDomain.EMOTIONAL_STATE, LifeDomain.SOCIAL),
        lageTitle = "Status",
        lageSummary = "Wie lebendig und leicht es heute wirkt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Mehr Raum fuer kleine Freude setzen.",
        flowTitle = "Automatik",
        flowSummary = "Erinnern und Mikro-Impulse steuern.",
    ),
    startSeed(
        id = "meaning",
        title = "Sinn",
        summary = "Was dir wichtig ist im Alltag wirklich tragen.",
        tracks = listOf("Werte", "Richtung", "Dankbarkeit", "Tiefe"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.EXPERT,
        defaultTargetScore = 4,
        defaultTemplateId = "theme",
        defaultIconKey = "compass",
        domains = setOf(LifeDomain.EMOTIONAL_STATE),
        lageTitle = "Status",
        lageSummary = "Wie stimmig und getragen es sich heute anfuehlt.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Werte, Richtung und Prioritaet schaerfen.",
        flowTitle = "Automatik",
        flowSummary = "Review, Impulse und kleine Experimente.",
        lageType = AreaLageType.STATE,
        defaultCadenceKey = "weekly",
        pilotSemantics = stateAreaSemantics(
            lageOptions = listOf(
                AreaLageStateOption("stimmig", "Stimmig", "Alltag und Werte greifen gut ineinander.", AreaLageStateTone.STABLE, 0.9f),
                AreaLageStateOption("wach", "Wach", "Sinn ist spuerbar, aber noch nicht klar getragen.", AreaLageStateTone.LIVE, 0.66f),
                AreaLageStateOption("fern", "Fern", "Ausrichtung ist da, aber gerade zu weit weg.", AreaLageStateTone.PULL, 0.34f),
                AreaLageStateOption("leer", "Leer", "Es fehlt an Verbindung zu dem, was wirklich wichtig ist.", AreaLageStateTone.PULL, 0.18f),
            ),
            stateCoreTitle = "Ausrichtung",
            stateInfoLabel = "Sinn",
            stateMetricValue = "Ausrichtung",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Wertefokus",
                nextMoveLabel = "Naechste Ausrichtung",
                dailyTemplate = "Heute %s ehren",
                weeklyTemplate = "Diese Woche %s vertiefen",
                adaptiveTemplate = "%s bewusst tragen",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Sinnquellen",
                mixLabel = "Stimmigkeit",
                channels = listOf(
                    AreaSourceChannel("Werte", "Werte im Alltag sichtbar machen", AreaSourceType.NOTE),
                    AreaSourceChannel("Richtung", "grobe Richtung aktiv erinnern", AreaSourceType.MANUAL),
                    AreaSourceChannel("Dankbarkeit", "kleine Resonanzpunkte bewusst festhalten", AreaSourceType.NOTE),
                    AreaSourceChannel("Tiefe", "Momente mit echter Tiefe nicht uebergehen", AreaSourceType.MANUAL),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Stiller Flow",
                intensityLabel = "Tiefe",
                neutralLabel = "Nur offen",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Anker", "einen stillen Anker im Tag sichtbar halten"),
                    AreaFlowToggleSemantics("review", "Reflexion", "Sinnspur kurz und bewusst ansehen"),
                    AreaFlowToggleSemantics("experiments", "Naechster Schritt", "eine kleine stimmige Bewegung ausprobieren"),
                ),
            ),
        ),
    ),
    startSeed(
        id = "discovery",
        title = "Neues",
        summary = "Neue Orte, Menschen und Erfahrungen bewusst im Leben halten.",
        tracks = listOf("Orte", "Menschen", "Erfahrungen", "Horizont"),
        overviewMode = AreaOverviewMode.REFLECTION,
        complexityLevel = AreaComplexityLevel.EXPERT,
        defaultTargetScore = 3,
        defaultTemplateId = "place",
        defaultIconKey = "explore",
        domains = setOf(LifeDomain.EMOTIONAL_STATE, LifeDomain.SOCIAL),
        lageTitle = "Status",
        lageSummary = "Wie offen und neugierig du gerade bist.",
        richtungTitle = "Ausrichten",
        richtungSummary = "Naechste Oeffnung und Tempo setzen.",
        flowTitle = "Automatik",
        flowSummary = "Impulse, Review und Signalmix steuern.",
        lageType = AreaLageType.STATE,
        defaultCadenceKey = "weekly",
        pilotSemantics = stateAreaSemantics(
            lageOptions = listOf(
                AreaLageStateOption("neugierig", "Neugierig", "Neue Eindruecke ziehen und machen Lust auf mehr.", AreaLageStateTone.STABLE, 0.9f),
                AreaLageStateOption("offen", "Offen", "Neues ist moeglich, braucht aber einen Impuls.", AreaLageStateTone.LIVE, 0.64f),
                AreaLageStateOption("voll", "Voll", "Zu viel Input blockiert echte Entdeckung.", AreaLageStateTone.PULL, 0.32f),
                AreaLageStateOption("zu", "Zu", "Neugier ist gerade verschlossen und braucht Oeffnung.", AreaLageStateTone.PULL, 0.16f),
            ),
            stateCoreTitle = "Offenheit",
            stateInfoLabel = "Entdeckung",
            stateMetricValue = "Offenheit",
            direction = AreaDirectionSemantics(
                focusActionLabel = "Entdeckungsfokus",
                nextMoveLabel = "Naechster Impuls",
                dailyTemplate = "Heute %s entdecken",
                weeklyTemplate = "Diese Woche %s oeffnen",
                adaptiveTemplate = "%s neugierig halten",
            ),
            sources = AreaSourceSemantics(
                coreTitle = "Inputquellen",
                mixLabel = "Horizont",
                channels = listOf(
                    AreaSourceChannel("Orte", "neue Orte und Wege bewusst sehen", AreaSourceType.LOCAL_SIGNAL),
                    AreaSourceChannel("Menschen", "neue Menschen und Perspektiven aufnehmen", AreaSourceType.MANUAL),
                    AreaSourceChannel("Erfahrungen", "ungewohnte Erfahrungen konkret suchen", AreaSourceType.NOTE),
                    AreaSourceChannel("Horizont", "den eigenen Horizont bewusst weiten", AreaSourceType.NOTE),
                ),
            ),
            flow = AreaFlowSemantics(
                coreTitle = "Impulsfluss",
                intensityLabel = "Impuls",
                neutralLabel = "Nur offen",
                toggles = listOf(
                    AreaFlowToggleSemantics("reminders", "Hinweis", "kleinen Entdeckungsimpuls lokal setzen"),
                    AreaFlowToggleSemantics("review", "Rueckblick", "Neues kurz nachhallen lassen"),
                    AreaFlowToggleSemantics("experiments", "Naechster Versuch", "einen neuen Versuch locker anstossen"),
                ),
            ),
        ),
    ),
)

private val startAreaKernelSeedMap = startAreaKernelSeeds.associateBy { it.definition.id }

/**
 * Returns the canonical seeded kernel definition for one Start area when it exists.
 */
fun startAreaKernelDefinition(areaId: String): AreaDefinition? {
    return startAreaKernelSeedMap[canonicalStartAreaId(areaId)]?.definition
}

/**
 * Returns the canonical seeded kernel blueprint for one Start area when it exists.
 */
fun startAreaKernelBlueprint(areaId: String): AreaBlueprint? {
    return startAreaKernelSeedMap[canonicalStartAreaId(areaId)]?.blueprint
}

/**
 * Returns the canonical seeded kernel bundle for one Start area when it exists.
 */
fun startAreaKernelSeed(areaId: String): StartAreaKernelSeed? {
    return startAreaKernelSeedMap[canonicalStartAreaId(areaId)]
}

private fun startSeed(
    id: String,
    title: String,
    summary: String,
    tracks: List<String>,
    overviewMode: AreaOverviewMode,
    complexityLevel: AreaComplexityLevel,
    defaultTargetScore: Int,
    defaultTemplateId: String,
    defaultIconKey: String,
    domains: Set<LifeDomain>,
    lageTitle: String,
    lageSummary: String,
    richtungTitle: String,
    richtungSummary: String,
    flowTitle: String,
    flowSummary: String,
    lageType: AreaLageType = AreaLageType.SCORE,
    focusType: AreaFocusType = AreaFocusType.HYBRID,
    defaultCadenceKey: String = "adaptive",
    pilotSemantics: AreaPilotSemantics? = null,
): StartAreaKernelSeed {
    val domainTags = domains.mapTo(linkedSetOf(), LifeDomain::name)
    val sourceTypesAllowed = startAreaSourceTypes(overviewMode, domains)
    val normalizedLageTitle = when (lageTitle) {
        "Status" -> "Lage"
        else -> lageTitle
    }
    val normalizedRichtungTitle = when (richtungTitle) {
        "Ausrichten" -> "Richtung"
        else -> richtungTitle
    }
    val normalizedFlowTitle = when (flowTitle) {
        "Automatik",
        "Optionen",
        -> "Flow"
        else -> flowTitle
    }
    val flowCapabilities = setOf(
        AreaFlowCapability.REMINDER,
        AreaFlowCapability.REVIEW,
        AreaFlowCapability.EXPERIMENT,
    )
    return StartAreaKernelSeed(
        definition = AreaDefinition(
            id = id,
            title = title,
            shortTitle = title,
            iconKey = defaultIconKey,
            defaultBehaviorClass = seedBehaviorClass(
                definitionId = id,
                templateId = defaultTemplateId,
            ),
            category = startAreaCategory(domains),
            overviewMode = overviewMode,
            complexityLevel = complexityLevel,
            seededByDefault = true,
            userCreatable = false,
            lageType = lageType,
            focusType = focusType,
            sourceTypesAllowed = sourceTypesAllowed,
            flowCapabilities = flowCapabilities,
            defaultConfig = AreaDefaultConfig(
                targetScore = defaultTargetScore,
                cadenceKey = defaultCadenceKey,
                defaultSelectedTracks = tracks.take(2).toSet(),
            ),
            permissionSensitivity = startAreaPermissionSensitivity(domains),
            supportsPassiveSignals = supportsPassiveSignals(domains),
            supportsImportedSources = false,
            authoringAxes = startAuthoringAxes(
                sourceTypesAllowed = sourceTypesAllowed,
                flowCapabilities = flowCapabilities,
            ),
        ),
        blueprint = AreaBlueprint(
            areaId = id,
            summary = summary,
            trackLabels = tracks,
                defaultTemplateId = defaultTemplateId,
                defaultIconKey = defaultIconKey,
                panelContentSeeds = mapOf(
                    AreaPanelKind.LAGE to AreaPanelContentSeed(
                    title = normalizedLageTitle,
                        summary = lageSummary,
                    ),
                    AreaPanelKind.RICHTUNG to AreaPanelContentSeed(
                    title = normalizedRichtungTitle,
                        summary = richtungSummary,
                    ),
                    AreaPanelKind.QUELLEN to derivedSourcesSeed(
                        overviewMode = overviewMode,
                        tracks = tracks,
                    ),
                    AreaPanelKind.FLOW to AreaPanelContentSeed(
                    title = normalizedFlowTitle,
                        summary = flowSummary,
                    ),
                ),
            pilotSemantics = pilotSemantics,
            defaultSourceLabels = tracks.take(2),
            domainTags = domainTags,
        ),
    )
}

private fun seedBehaviorClass(
    definitionId: String,
    templateId: String,
): AreaBehaviorClass {
    return when (canonicalStartAreaId(definitionId)) {
        "vitality", "sleep" -> AreaBehaviorClass.TRACKING
        "impact", "learning", "work" -> AreaBehaviorClass.PROGRESS
        "friends", "friendship" -> AreaBehaviorClass.RELATIONSHIP
        "home" -> AreaBehaviorClass.MAINTENANCE
        "clarity", "focus" -> AreaBehaviorClass.PROTECTION
        "recovery" -> AreaBehaviorClass.REFLECTION
        "bond", "family", "community" -> AreaBehaviorClass.RELATIONSHIP
        else -> defaultBehaviorClassForTemplate(templateId)
    }
}

private fun startAuthoringAxes(
    sourceTypesAllowed: Set<AreaSourceType>,
    flowCapabilities: Set<AreaFlowCapability>,
): Set<AreaAuthoringAxis> {
    return buildSet {
        add(AreaAuthoringAxis.STATUS_SCHEMA)
        add(AreaAuthoringAxis.DIRECTION)
        add(AreaAuthoringAxis.COMPLEXITY)
        add(AreaAuthoringAxis.VISIBILITY)
        if (sourceTypesAllowed.isNotEmpty()) {
            add(AreaAuthoringAxis.SOURCES)
        }
        if (flowCapabilities.isNotEmpty()) {
            add(AreaAuthoringAxis.FLOW)
        }
    }
}

private fun derivedSourcesSeed(
    overviewMode: AreaOverviewMode,
    tracks: List<String>,
): AreaPanelContentSeed {
    val leadTracks = tracks.take(2)
    val prompt = when {
        leadTracks.isEmpty() -> "Signale und Spuren ordnen."
        leadTracks.size == 1 -> "${leadTracks.first()} als Spur nutzen."
        else -> "${leadTracks[0]} und ${leadTracks[1]} als Spuren nutzen."
    }
    val summary = when (overviewMode) {
        AreaOverviewMode.SIGNAL -> "Signale und Spuren fuer diesen Bereich gewichten."
        AreaOverviewMode.PLAN -> "Spuren und Inputs fuer die naechsten Zuege setzen."
        AreaOverviewMode.REFLECTION,
        AreaOverviewMode.HYBRID,
        -> "Spuren, Hinweise und Notizen zusammenziehen."
    }
    return AreaPanelContentSeed(
        title = "Quellen",
        summary = summary,
        prompt = prompt,
    )
}

private fun startAreaSourceTypes(
    overviewMode: AreaOverviewMode,
    domains: Set<LifeDomain>,
): Set<AreaSourceType> {
    return buildSet {
        add(AreaSourceType.MANUAL)
        add(AreaSourceType.TRACK)
        if (overviewMode == AreaOverviewMode.REFLECTION || overviewMode == AreaOverviewMode.HYBRID) {
            add(AreaSourceType.NOTE)
        }
        if (supportsPassiveSignals(domains)) {
            add(AreaSourceType.LOCAL_SIGNAL)
        }
    }
}

private fun startAreaCategory(domains: Set<LifeDomain>): AreaCategory {
    return when {
        domains.any { it == LifeDomain.SOCIAL } -> AreaCategory.RELATIONSHIP
        domains.any { it == LifeDomain.HOUSEHOLD } -> AreaCategory.ENVIRONMENT
        domains.any { it in startFoundationDomains } -> AreaCategory.FOUNDATION
        domains.any { it in startDirectionDomains } -> AreaCategory.DIRECTION
        else -> AreaCategory.OPEN
    }
}

private fun startAreaPermissionSensitivity(domains: Set<LifeDomain>): AreaPermissionSensitivity {
    return when {
        domains.any { it in startHighSensitivityDomains } -> AreaPermissionSensitivity.HIGH
        supportsPassiveSignals(domains) -> AreaPermissionSensitivity.LOW
        else -> AreaPermissionSensitivity.NONE
    }
}

private fun supportsPassiveSignals(domains: Set<LifeDomain>): Boolean {
    return domains.any { it in startPassiveSignalDomains }
}
