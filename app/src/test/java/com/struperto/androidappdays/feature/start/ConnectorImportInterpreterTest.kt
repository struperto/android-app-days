package com.struperto.androidappdays.feature.start

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectorImportInterpreterTest {
    @Test
    fun bookmarkHtmlBecomesSummaryAndLinks() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
              <DT><A HREF="https://developer.android.com/compose">Compose</A>
              <DT><A HREF="https://developer.android.com/topic/architecture">Architecture</A>
            </DL><p>
        """.trimIndent()

        val drafts = buildConnectorImportDrafts(
            displayName = "chrome-bookmarks.html",
            mimeType = "text/html",
            textContent = html,
        )

        assertEquals(3, drafts.size)
        assertEquals(AreaImportKind.Text, drafts.first().kind)
        assertTrue(drafts.first().reference.contains("2 Lesezeichen"))
        assertEquals(AreaImportKind.Link, drafts[1].kind)
        assertEquals("https://developer.android.com/compose", drafts[1].reference)
    }

    @Test
    fun emlBecomesMailSummaryAndLinks() {
        val eml = """
            From: Dev Rel <devrel@example.com>
            Subject: Compose Links
            Date: Fri, 13 Mar 2026 10:00:00 +0100

            Hallo Rupert,
            hier sind zwei Links:
            https://developer.android.com/compose
            https://developer.android.com/topic/architecture
        """.trimIndent()

        val drafts = buildConnectorImportDrafts(
            displayName = "compose-links.eml",
            mimeType = "message/rfc822",
            textContent = eml,
        )

        assertEquals(3, drafts.size)
        assertEquals(AreaImportKind.Text, drafts.first().kind)
        assertTrue(drafts.first().reference.contains("Compose Links"))
        assertEquals(AreaImportKind.Link, drafts[1].kind)
        assertEquals("https://developer.android.com/compose", drafts[1].reference)
    }

    @Test
    fun plainTextFileBecomesReadableTextImport() {
        val text = """
            Artikel fuer spaeter
            https://developer.android.com/training/sharing/receive
        """.trimIndent()

        val drafts = buildConnectorImportDrafts(
            displayName = "notes.txt",
            mimeType = "text/plain",
            textContent = text,
        )

        assertEquals(2, drafts.size)
        assertEquals(AreaImportKind.Text, drafts.first().kind)
        assertEquals(AreaImportKind.Link, drafts[1].kind)
    }
}
