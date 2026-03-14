package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceCapability
import com.struperto.androidappdays.domain.DataSourceKind
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartIntentAnalysisEngineTest {
    private val fullyUsableProfile = CapabilityProfile(
        sources = DataSourceKind.entries.map { source ->
            DataSourceCapability(
                source = source,
                label = source.name,
                enabled = true,
                available = true,
                granted = true,
                detail = "bereit",
            )
        },
    )

    @Test
    fun bookmarksStayBlockedUntilRealIntegrationExists() {
        val analysis = analyzeStartIntent(
            rawInput = "Ich moechte, dass du meine Lesezeichen liest.",
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome"),
        )

        assertFalse(analysis.canCreate)
        assertEquals(StartIntentFamily.BOOKMARKS, analysis.family)
        assertEquals(StartIntentOutcome.BLOCKED, analysis.outcome)
        assertTrue(analysis.blockingReason?.contains("Lesezeichen") == true)
    }

    @Test
    fun calendarIntentUsesCalendarSource() {
        val draft = buildPrimaryCreateDraft("Zeig mir meine Termine fuer heute.")
        val analysis = analyzeStartIntent(
            rawInput = "Zeig mir meine Termine fuer heute.",
            capabilityProfile = fullyUsableProfile,
        )

        assertEquals(DataSourceKind.CALENDAR, draft.sourceKind)
        assertEquals("project", draft.templateId)
        assertTrue(analysis.canCreate)
        assertEquals(StartIntentFamily.CALENDAR, analysis.family)
        assertEquals(StartIntentOutcome.DIRECT_SOURCE, analysis.outcome)
    }

    @Test
    fun routineIntentStaysManualAndCreatable() {
        val draft = buildPrimaryCreateDraft("Ich will eine ruhige Abendroutine pflegen.")
        val analysis = analyzeStartIntent(
            rawInput = "Ich will eine ruhige Abendroutine pflegen.",
            capabilityProfile = fullyUsableProfile,
        )

        assertEquals(null, draft.sourceKind)
        assertEquals("ritual", draft.templateId)
        assertTrue(analysis.canCreate)
        assertEquals(StartIntentFamily.ROUTINE, analysis.family)
        assertEquals(StartIntentOutcome.MANUAL_AREA, analysis.outcome)
    }

    @Test
    fun formerlyGenericSignalsNowMapToConcreteFamilies() {
        val friday = analyzeStartIntent(
            rawInput = "Zeig mir, ob mein Freitag zu voll wird.",
            capabilityProfile = fullyUsableProfile,
        )
        val parcels = analyzeStartIntent(
            rawInput = "Mach einen Bereich fuer Lieferstatus und Paketmeldungen.",
            capabilityProfile = fullyUsableProfile,
        )
        val gallery = analyzeStartIntent(
            rawInput = "Zeig mir Bilder aus meiner Galerie mit Whiteboards.",
            capabilityProfile = fullyUsableProfile,
        )

        assertEquals(StartIntentFamily.CALENDAR, friday.family)
        assertEquals(StartIntentFamily.NOTIFICATIONS, parcels.family)
        assertEquals(StartIntentFamily.PHOTOS, gallery.family)
        assertEquals(StartIntentOutcome.MANUAL_AREA, gallery.outcome)
        assertTrue(gallery.sourceLabel.contains("Import"))
    }

    @Test
    fun webAndFileIntentsBecomeImportable() {
        val web = analyzeStartIntent(
            rawInput = "Lies diese Website fuer mich.",
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome"),
        )
        val files = analyzeStartIntent(
            rawInput = "Lies meine PDFs und sortiere sie.",
            capabilityProfile = fullyUsableProfile,
        )

        assertTrue(web.canCreate)
        assertEquals(StartIntentOutcome.MANUAL_AREA, web.outcome)
        assertTrue(web.sourceLabel.contains("Import"))
        assertTrue(files.canCreate)
        assertEquals(StartIntentOutcome.MANUAL_AREA, files.outcome)
        assertTrue(files.sourceLabel.contains("Import"))
    }

    @Test
    fun newsIntentStaysGeneralAndLetsSourcesBeChosenLater() {
        val analysis = analyzeStartIntent(
            rawInput = "Ich will News ruhig lesen und meine ausgewaehlten X-Posts, Insta-Bilder, FAZ und stol.it sehen.",
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome"),
            installedPackages = setOf("com.twitter.android"),
        )

        assertTrue(analysis.canCreate)
        assertEquals(StartIntentFamily.WEB_CONTENT, analysis.family)
        assertEquals("News", analysis.suggestedTitle)
        assertEquals(
            listOf("Quellen", "Web", "Feeds", "Social Text", "Social Bild", "Video", "Screenshots"),
            analysis.sourceRows.map { it.name },
        )
        assertEquals("Mit Share", analysis.sourceRows.first { it.name == "Social Text" }.status)
        assertEquals("Mit Link", analysis.sourceRows.first { it.name == "Social Bild" }.status)
        assertEquals("Mit Link", analysis.sourceRows.first { it.name == "Video" }.status)
    }

    @Test
    fun plainNewsStillMapsToNewsSources() {
        val analysis = analyzeStartIntent(
            rawInput = "News",
            capabilityProfile = fullyUsableProfile,
        )

        assertEquals(StartIntentFamily.WEB_CONTENT, analysis.family)
        assertEquals("News", analysis.suggestedTitle)
        assertEquals(listOf("Quellen", "Web", "Feeds", "Social Text", "Social Bild", "Video", "Screenshots"), analysis.sourceRows.map { it.name })
        assertEquals("Klaeren", analysis.sourceRows.first { it.name == "Quellen" }.status)
    }

    @Test
    fun newsSourceChoicesStayGeneral() {
        val analysis = analyzeStartIntent(
            rawInput = "News",
            capabilityProfile = fullyUsableProfile,
        )

        assertEquals(
            listOf("Web", "Feeds", "Social Text", "Social Bild", "Video", "Screenshots"),
            analysis.toSourceChoices().map { it.title },
        )
    }

    @Test
    fun contactAndFinanceIntentsExposeRelevantSourceChoices() {
        val contact = analyzeStartIntent(
            rawInput = "Wenn mir wichtige Leute schreiben, will ich es sofort sehen.",
            capabilityProfile = fullyUsableProfile,
        )
        val finance = analyzeStartIntent(
            rawInput = "Ich will meine Ausgaben und Finanzthemen ruhig ordnen.",
            capabilityProfile = fullyUsableProfile,
        )

        assertEquals(
            listOf("Benachrichtigungen", "Messenger", "Notizen", "Screenshots", "Kalender"),
            contact.toSourceChoices().map { it.title },
        )
        assertEquals(
            listOf("Dateien", "Benachrichtigungen", "Web", "Notizen"),
            finance.toSourceChoices().map { it.title },
        )
    }

    @Test
    fun projectAndRoutineIntentsExposeRelevantSourceChoices() {
        val project = analyzeStartIntent(
            rawInput = "Ich will Material und naechste Schritte fuer den App-Bau ordnen.",
            capabilityProfile = fullyUsableProfile,
        )
        val routine = analyzeStartIntent(
            rawInput = "Ich will eine ruhige Abendroutine pflegen.",
            capabilityProfile = fullyUsableProfile,
        )

        assertEquals(
            listOf("App-Share", "Links", "Notizen", "Screenshots", "Dateien"),
            project.toSourceChoices().map { it.title },
        )
        assertEquals(
            listOf("Notizen", "Kalender", "Screenshots"),
            routine.toSourceChoices().map { it.title },
        )
    }

    @Test
    fun sharedMailAndAppContentBecomeImportable() {
        val mail = analyzeStartIntent(
            rawInput = "Ich will dir einzelne Mails weiterleiten und hier sammeln.",
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome"),
        )
        val appContent = analyzeStartIntent(
            rawInput = "Ich will Inhalte aus Notion hierher teilen.",
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome"),
        )

        assertTrue(mail.canCreate)
        assertEquals(StartIntentOutcome.MANUAL_AREA, mail.outcome)
        assertTrue(mail.sourceDetail.contains("Mail"))
        assertTrue(appContent.canCreate)
        assertEquals(StartIntentOutcome.MANUAL_AREA, appContent.outcome)
        assertTrue(appContent.sourceDetail.contains("Apps"))
    }

    @Test
    fun bookmarkAndMailExportPromptsBecomeConnectorImports() {
        val bookmarks = analyzeStartIntent(
            rawInput = "Importiere meine Chrome-Lesezeichen als HTML-Datei.",
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome"),
        )
        val mail = analyzeStartIntent(
            rawInput = "Lies diese EML-Datei aus meinem Postfach.",
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome"),
        )

        assertTrue(bookmarks.canCreate)
        assertEquals(StartIntentFamily.BOOKMARKS, bookmarks.family)
        assertEquals(StartIntentOutcome.MANUAL_AREA, bookmarks.outcome)
        assertTrue(bookmarks.sourceLabel.contains("Datei"))
        assertTrue(mail.canCreate)
        assertEquals(StartIntentFamily.EMAIL, mail.family)
        assertEquals(StartIntentOutcome.MANUAL_AREA, mail.outcome)
        assertTrue(mail.sourceLabel.contains("Datei"))
    }

    @Test
    fun tenDiverseAreaPromptsStayCriticallyClassified() {
        val prompts = listOf(
            "Zeig mir meine Termine fuer heute." to Pair(StartIntentFamily.CALENDAR, StartIntentOutcome.DIRECT_SOURCE),
            "Wenn mir wichtige Leute schreiben, will ich es sofort sehen." to Pair(StartIntentFamily.NOTIFICATIONS, StartIntentOutcome.DIRECT_SOURCE),
            "Lies meine Schlafdaten und zeig mir die Nacht." to Pair(StartIntentFamily.HEALTH, StartIntentOutcome.DIRECT_SOURCE),
            "Ich will eine ruhige Abendroutine pflegen." to Pair(StartIntentFamily.ROUTINE, StartIntentOutcome.MANUAL_AREA),
            "Lies diese Website fuer mich." to Pair(StartIntentFamily.WEB_CONTENT, StartIntentOutcome.MANUAL_AREA),
            "Scanne meine Screenshots nach To-dos." to Pair(StartIntentFamily.SCREENSHOTS, StartIntentOutcome.MANUAL_AREA),
            "Lies meine PDFs und sortiere sie." to Pair(StartIntentFamily.FILES, StartIntentOutcome.MANUAL_AREA),
            "Ich will dir einzelne Mails weiterleiten und hier sammeln." to Pair(StartIntentFamily.EMAIL, StartIntentOutcome.MANUAL_AREA),
            "Lies meine Browser-Lesezeichen." to Pair(StartIntentFamily.BOOKMARKS, StartIntentOutcome.BLOCKED),
            "Analysiere mein Postfach nach dringenden Antworten." to Pair(StartIntentFamily.EMAIL, StartIntentOutcome.BLOCKED),
        )

        val analyses = prompts.map { (prompt, expected) ->
            prompt to analyzeStartIntent(
                rawInput = prompt,
                capabilityProfile = fullyUsableProfile,
                browserApps = listOf("Chrome"),
            ).also { analysis ->
                assertEquals(prompt, expected.first, analysis.family)
                assertEquals(prompt, expected.second, analysis.outcome)
            }
        }

        File("tmp/start-intent-10-case-report.txt").apply {
            parentFile?.mkdirs()
            writeText(
                buildString {
                    appendLine("Start intent 10-case report")
                    analyses.forEach { (prompt, analysis) ->
                        appendLine("- $prompt")
                        appendLine("  family=${analysis.family.name} outcome=${analysis.outcome.name} status=${analysis.statusLabel}")
                        appendLine("  source=${analysis.sourceLabel}")
                        appendLine("  canCreate=${analysis.canCreate}")
                    }
                },
            )
        }

        assertEquals(3, analyses.count { it.second.outcome == StartIntentOutcome.DIRECT_SOURCE })
        assertEquals(5, analyses.count { it.second.outcome == StartIntentOutcome.MANUAL_AREA })
        assertEquals(2, analyses.count { it.second.outcome == StartIntentOutcome.BLOCKED })
        assertTrue(analyses.none { it.second.family == StartIntentFamily.GENERAL })
    }

    @Test
    fun hundredSampleBatchKeepsOutcomeMixStable() {
        val report = buildStartIntentAnalysisReport(
            capabilityProfile = fullyUsableProfile,
            browserApps = listOf("Chrome", "Firefox"),
        )
        val analyses = startIntentBatchSamples().map { prompt ->
            analyzeStartIntent(
                rawInput = prompt,
                capabilityProfile = fullyUsableProfile,
                browserApps = listOf("Chrome", "Firefox"),
            )
        }
        File("tmp/start-intent-analysis-report.txt").apply {
            parentFile?.mkdirs()
            writeText(report)
        }

        assertEquals(100, analyses.size)
        assertTrue(analyses.count { it.outcome == StartIntentOutcome.DIRECT_SOURCE } >= 50)
        assertTrue(analyses.count { it.outcome == StartIntentOutcome.MANUAL_AREA } >= 30)
        assertTrue(analyses.count { it.outcome == StartIntentOutcome.BLOCKED } >= 5)
        assertTrue(analyses.none { it.family == StartIntentFamily.GENERAL })
        assertTrue(report.contains("samples=100"))
        assertTrue(report.contains("blocked:"))
    }
}
