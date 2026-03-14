package com.struperto.androidappdays.feature.single.home

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsRuntimeRepositoryTest {
    private val gson = Gson()

    @Test
    fun parsesStolTickerPayloadIntoNewsArticles() {
        val json = """
            {
              "data": {
                "articles": [
                  {
                    "id": 1,
                    "date": "2026-03-13T16:50:08+01:00",
                    "ts": 1773417008,
                    "url": "/artikel/chronik/test-artikel",
                    "breadcrumb": {
                      "department": { "name": "Chronik" },
                      "headline": { "name": "Soziales" }
                    },
                    "title": "Test Artikel",
                    "description": "Kurze Einordnung fuer die Kachel.",
                    "plus": false
                  }
                ]
              }
            }
        """.trimIndent()

        val parsed = parseStolTickerPayload(gson, json)

        assertEquals(1, parsed.size)
        assertEquals("https://www.stol.it/artikel/chronik/test-artikel", parsed.first().articleUrl)
        assertEquals("Test Artikel", parsed.first().title)
        assertEquals("2026-03-13T16:50:08+01:00", parsed.first().publishedLabel)
        assertEquals(1773417008000L, parsed.first().publishedAtMillis)
        assertTrue(parsed.first().summary.contains("Chronik / Soziales"))
        assertTrue(parsed.first().summary.contains("Kurze Einordnung"))
    }

    @Test
    fun parsesPlusFlagIntoStolSummary() {
        val json = """
            {
              "data": {
                "articles": [
                  {
                    "date": "2026-03-13T12:00:00+01:00",
                    "ts": 1773409200,
                    "url": "/artikel/kultur/plus-artikel",
                    "breadcrumb": {
                      "department": { "name": "Kultur" },
                      "headline": { "name": "Buehne" }
                    },
                    "title": "Plus Artikel",
                    "description": "Mehr Kontext.",
                    "plus": true
                  }
                ]
              }
            }
        """.trimIndent()

        val parsed = parseStolTickerPayload(gson, json)

        assertEquals(1, parsed.size)
        assertTrue(parsed.first().summary.contains("Plus"))
    }
}
