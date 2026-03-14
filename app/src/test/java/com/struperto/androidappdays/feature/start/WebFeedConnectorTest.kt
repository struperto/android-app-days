package com.struperto.androidappdays.feature.start

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence

class WebFeedConnectorTest {
    @Test
    fun discoversFeedUrlFromHtml() {
        val html = """
            <html>
              <head>
                <link rel="alternate" type="application/rss+xml" href="/feed.xml" />
              </head>
            </html>
        """.trimIndent()

        val feeds = discoverFeedUrls(
            pageUrl = "https://example.com/blog",
            html = html,
        )

        assertEquals(listOf("https://example.com/feed.xml"), feeds)
    }

    @Test
    fun parsesRssFeedIntoDrafts() {
        val xml = """
            <rss version="2.0">
              <channel>
                <title>Android Feed</title>
                <item>
                  <title>Compose 2026</title>
                  <link>https://example.com/compose-2026</link>
                  <pubDate>Fri, 13 Mar 2026 10:00:00 +0100</pubDate>
                </item>
                <item>
                  <title>Architecture</title>
                  <link>https://example.com/architecture</link>
                  <description>State and flow</description>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val parsed = parseFeedDocument(
            sourceUrl = "https://example.com/feed.xml",
            xml = xml,
        )
        val drafts = parsed.toDrafts(emptySet())

        assertEquals("Android Feed", parsed.title)
        assertEquals(3, drafts.size)
        assertEquals(AreaImportKind.Text, drafts.first().kind)
        assertEquals(AreaImportKind.Link, drafts[1].kind)
        assertTrue(drafts.first().reference.contains("Compose 2026"))
    }

    @Test
    fun parseFeedDraftsSkipKnownLinks() {
        val xml = """
            <feed xmlns="http://www.w3.org/2005/Atom">
              <title>Example Atom</title>
              <entry>
                <title>Known</title>
                <link href="https://example.com/known" />
                <updated>2026-03-13T10:00:00Z</updated>
              </entry>
              <entry>
                <title>Fresh</title>
                <link href="https://example.com/fresh" />
                <updated>2026-03-13T11:00:00Z</updated>
              </entry>
            </feed>
        """.trimIndent()

        val parsed = parseFeedDocument(
            sourceUrl = "https://example.com/atom.xml",
            xml = xml,
        )
        val drafts = parsed.toDrafts(setOf("https://example.com/known"))

        assertEquals(2, drafts.size)
        assertTrue(drafts.any { it.reference == "https://example.com/fresh" })
    }

    @Test
    fun htmlWithoutFeedBecomesReadableDraft() {
        val html = """
            <html>
              <head><title>Example Article</title></head>
              <body>
                <article>
                  <h1>Example Article</h1>
                  <p>This page explains how the area should keep one article directly readable even without an RSS feed.</p>
                  <p>It should also preserve enough body text to be useful inside the area.</p>
                </article>
              </body>
            </html>
        """.trimIndent()

        val parsed = parseReadableWebPage(
            pageUrl = "https://example.com/article",
            html = html,
        )
        val drafts = parsed.toDrafts(emptySet())

        assertEquals("Example Article", parsed.title)
        assertEquals(1, drafts.size)
        assertEquals(AreaImportKind.Text, drafts.first().kind)
        assertEquals("Website direkt gelesen. Kein RSS/Atom-Feed erkannt.", drafts.first().detail)
        assertTrue(drafts.first().reference.contains("Web-Reader: https://example.com/article"))
        assertTrue(drafts.first().reference.contains("without an RSS feed"))
    }

    @Test
    fun htmlWithEmbeddedArticleBodyPrefersStructuredText() {
        val html = """
            <html>
              <head><title>STOL Article</title></head>
              <body>
                <script type="application/ld+json">
                  {"articleBody":"Erster Absatz mit genug Inhalt fuer den Reader.<BR /><BR />Zweiter Absatz mit mehr Kontext und Details.","description":"Kurzfassung"}
                </script>
              </body>
            </html>
        """.trimIndent()

        val parsed = parseReadableWebPage(
            pageUrl = "https://www.stol.it/artikel/test",
            html = html,
        )

        assertTrue(parsed.textPreview.contains("Erster Absatz"))
        assertTrue(parsed.textPreview.contains("Zweiter Absatz"))
        assertTrue(!parsed.textPreview.contains("Kurzfassung"))
    }

    @Test
    fun htmlListingExtractsCurrentArticleCandidates() {
        val html = """
            <html>
              <head><title>Example News</title></head>
              <body>
                <main>
                  <a href="/story-1">Compose reaches stable adaptive layouts</a>
                  <a href="/story-2">WorkManager sync pipeline now retries cleanly</a>
                  <a href="/story-3">Short</a>
                </main>
              </body>
            </html>
        """.trimIndent()

        val parsed = parseReadableWebPage(
            pageUrl = "https://example.com/news",
            html = html,
        )
        val drafts = parsed.toDrafts(emptySet())

        assertTrue(drafts.any { it.kind == AreaImportKind.Text })
        assertTrue(drafts.any { it.reference == "https://example.com/story-1" })
        assertTrue(drafts.any { it.reference == "https://example.com/story-2" })
    }

    @Test
    fun webFeedCapabilityMatrixReportStaysRealistic() {
        val feedDrafts = parseFeedDocument(
            sourceUrl = "https://example.com/feed.xml",
            xml = """
                <rss version="2.0"><channel><title>News</title>
                <item><title>Fresh Story</title><link>https://example.com/fresh-story</link></item>
                </channel></rss>
            """.trimIndent(),
        ).toDrafts(emptySet())
        val htmlFeedDiscovery = discoverFeedUrls(
            pageUrl = "https://example.com/blog",
            html = """<html><head><link rel="alternate" type="application/rss+xml" href="/feed.xml" /></head></html>""",
        )
        val readablePageDrafts = parseReadableWebPage(
            pageUrl = "https://example.com/article",
            html = """
                <html><head><title>Article</title></head><body><p>This article can be read directly inside the area even when the site has no feed.</p></body></html>
            """.trimIndent(),
        ).toDrafts(emptySet())
        val listingDrafts = parseReadableWebPage(
            pageUrl = "https://example.com/news",
            html = """
                <html><body>
                <a href="/story-1">Current article one with enough title text</a>
                <a href="/story-2">Current article two with enough title text</a>
                </body></html>
            """.trimIndent(),
        ).toDrafts(emptySet())
        val knownLinkDedupes = parseFeedDocument(
            sourceUrl = "https://example.com/feed.xml",
            xml = """
                <rss version="2.0"><channel><title>News</title>
                <item><title>Fresh Story</title><link>https://example.com/fresh-story</link></item>
                </channel></rss>
            """.trimIndent(),
        ).toDrafts(setOf("https://example.com/fresh-story")).isEmpty()

        File("tmp/web-feed-capability-report.txt").apply {
            parentFile?.mkdirs()
            writeText(
                buildString {
                    appendLine("Web/Feed capability report")
                    appendLine("1. Ziel: aktuelle Artikel koennen gelesen werden.")
                    appendLine("   Ergebnis: Feed URL liefert ${feedDrafts.count { it.kind == AreaImportKind.Link }} neue Artikel-Links.")
                    appendLine("2. Ziel: eine normale Website kann als Feed-Quelle erkannt werden.")
                    appendLine("   Ergebnis: Website mit Feed liefert ${htmlFeedDiscovery.size} erkannte Feed-Pfade.")
                    appendLine("3. Ziel: ein einzelner Artikel ist auch ohne Feed lesbar.")
                    appendLine("   Ergebnis: Website ohne Feed erzeugt Reader-Text = ${readablePageDrafts.any { it.kind == AreaImportKind.Text }}.")
                    appendLine("4. Ziel: aktuelle Artikel auf einer News-Seite koennen gefunden werden.")
                    appendLine("   Ergebnis: Listing erzeugt ${listingDrafts.count { it.kind == AreaImportKind.Link }} Artikel-Kandidaten.")
                    appendLine("5. Ziel: bekannte Artikel werden beim naechsten Lesen nicht doppelt angelegt.")
                    appendLine("   Ergebnis: Duplikat-Schutz aktiv = $knownLinkDedupes.")
                    appendLine("6. Ziel: Feed-Quellen laden schnell nach, Websites ruhiger.")
                    appendLine("   Ergebnis: Feed = ${defaultWebFeedSyncCadence(AreaWebFeedSourceKind.Feed).label}, Website = ${defaultWebFeedSyncCadence(AreaWebFeedSourceKind.Website).label}.")
                },
            )
        }

        assertTrue(feedDrafts.any { it.reference == "https://example.com/fresh-story" })
        assertEquals(1, htmlFeedDiscovery.size)
        assertTrue(readablePageDrafts.any { it.kind == AreaImportKind.Text })
        assertTrue(listingDrafts.count { it.kind == AreaImportKind.Link } >= 2)
    }

    @Test
    fun sourceKindAndCadenceDefaultMatchUseCase() {
        assertEquals(AreaWebFeedSyncCadence.Hourly, AreaWebFeedSyncCadence.fromStorage("1h"))
        assertEquals(AreaWebFeedSourceKind.Feed, inferWebFeedSourceKind("https://example.com/feed.xml"))
        assertEquals(AreaWebFeedSourceKind.Website, inferWebFeedSourceKind("https://example.com/article"))
        assertEquals(AreaWebFeedSyncCadence.SixHours, defaultWebFeedSyncCadence(AreaWebFeedSourceKind.Feed))
        assertEquals(AreaWebFeedSyncCadence.Daily, defaultWebFeedSyncCadence(AreaWebFeedSourceKind.Website))
    }
}
