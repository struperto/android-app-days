package com.struperto.androidappdays.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

data class WebsiteSnapshot(
    val url: String,
    val title: String?,
    val excerpt: String?,
    val fetchedAt: Instant,
    val statusCode: Int,
)

interface WebsiteReaderRepository {
    suspend fun fetch(url: String): WebsiteSnapshot
}

class HttpWebsiteReaderRepository : WebsiteReaderRepository {
    override suspend fun fetch(url: String): WebsiteSnapshot = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Days-App/1.0")
            val statusCode = connection.responseCode
            val body = if (statusCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText().take(10_000) }
            } else {
                null
            }
            val title = body?.let { extractHtmlTitle(it) }
            val excerpt = body?.let { extractExcerpt(it) }
            WebsiteSnapshot(
                url = url,
                title = title,
                excerpt = excerpt,
                fetchedAt = Instant.now(),
                statusCode = statusCode,
            )
        } finally {
            connection.disconnect()
        }
    }
}

private fun extractHtmlTitle(html: String): String? {
    val match = Regex("<title[^>]*>(.*?)</title>", RegexOption.IGNORE_CASE).find(html)
    return match?.groupValues?.get(1)?.trim()?.take(200)
}

private fun extractExcerpt(html: String): String? {
    val metaMatch = Regex(
        """<meta[^>]*name=["']description["'][^>]*content=["']([^"']+)["']""",
        RegexOption.IGNORE_CASE,
    ).find(html)
    return metaMatch?.groupValues?.get(1)?.trim()?.take(300)
}
