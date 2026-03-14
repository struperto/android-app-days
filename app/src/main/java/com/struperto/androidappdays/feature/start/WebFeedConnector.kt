package com.struperto.androidappdays.feature.start

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element

data class WebFeedSyncResult(
    val foundFeedUrls: List<String>,
    val drafts: List<AreaImportDraft>,
    val message: String,
)

interface WebFeedConnector {
    suspend fun sync(
        urls: List<String>,
        knownReferences: Set<String>,
    ): WebFeedSyncResult
}

class LocalWebFeedConnector : WebFeedConnector {
    override suspend fun sync(
        urls: List<String>,
        knownReferences: Set<String>,
    ): WebFeedSyncResult = withContext(Dispatchers.IO) {
        val uniqueUrls = urls
            .map(String::trim)
            .filter { it.startsWith("http") }
            .distinct()
            .take(4)
        if (uniqueUrls.isEmpty()) {
            return@withContext WebFeedSyncResult(
                foundFeedUrls = emptyList(),
                drafts = emptyList(),
                message = "Noch kein Web-Link zum Lesen vorhanden.",
            )
        }

        val discoveredFeedUrls = mutableListOf<String>()
        val drafts = mutableListOf<AreaImportDraft>()
        val diagnostics = mutableListOf<String>()

        uniqueUrls.forEach { inputUrl ->
            val document = runCatching { fetchWebDocument(inputUrl) }.getOrElse { error ->
                diagnostics += "${shortHost(inputUrl)} nicht gelesen: ${error.message.orEmpty().ifBlank { "Fehler" }}"
                return@forEach
            }
            when (document.kind) {
                WebDocumentKind.Feed -> {
                    discoveredFeedUrls += document.url
                    val parsed = parseFeedDocument(document.url, document.body)
                    val newDrafts = parsed.toDrafts(knownReferences + drafts.map(AreaImportDraft::reference))
                    drafts += newDrafts
                    diagnostics += if (newDrafts.isEmpty()) {
                        "${shortHost(document.url)} gelesen, aber nichts Neues gefunden."
                    } else {
                        "${shortHost(document.url)} gelesen: ${newDrafts.count { it.kind == AreaImportKind.Link }} neue Eintraege."
                    }
                }
                WebDocumentKind.Html -> {
                    val discovered = discoverFeedUrls(
                        pageUrl = document.url,
                        html = document.body,
                    )
                    if (discovered.isEmpty()) {
                        val parsedPage = parseReadableWebPage(
                            pageUrl = document.url,
                            html = document.body,
                        )
                        val newDrafts = parsedPage.toDrafts(
                            knownReferences = knownReferences + drafts.map(AreaImportDraft::reference),
                        )
                        drafts += newDrafts
                        diagnostics += if (newDrafts.isEmpty()) {
                            "${shortHost(document.url)} analysiert: kein RSS/Atom-Feed und nichts klar Lesbares."
                        } else {
                            "${shortHost(document.url)} analysiert: kein RSS/Atom-Feed, nur Direktlese- oder Link-Kandidaten."
                        }
                    } else {
                        discoveredFeedUrls += discovered
                        drafts += discovered
                            .filterNot { it in knownReferences || drafts.any { draft -> draft.reference == it } }
                            .map { feedUrl ->
                                AreaImportDraft(
                                    kind = AreaImportKind.Link,
                                    title = "${shortHost(document.url)} Feed",
                                    detail = "RSS/Atom-Feed auf dieser Website gefunden.",
                                    reference = feedUrl,
                                )
                            }
                        diagnostics += "${shortHost(document.url)} analysiert: ${discovered.size} RSS/Atom-Feed-Pfad(e) gefunden."
                    }
                }
                WebDocumentKind.Other -> {
                    diagnostics += "${shortHost(document.url)} war kein lesbarer Feed."
                }
            }
        }

        WebFeedSyncResult(
            foundFeedUrls = discoveredFeedUrls.distinct(),
            drafts = drafts.distinctBy { "${it.kind}:${it.reference}" },
            message = diagnostics.firstOrNull().orEmpty().ifBlank { "Kein lesbarer Feed gefunden." },
        )
    }
}

private data class WebDocument(
    val url: String,
    val kind: WebDocumentKind,
    val body: String,
)

private enum class WebDocumentKind {
    Feed,
    Html,
    Other,
}

internal data class ParsedFeed(
    val title: String,
    val sourceUrl: String,
    val items: List<ParsedFeedItem>,
)

internal data class ParsedFeedItem(
    val title: String,
    val link: String,
    val detail: String,
)

internal data class ParsedWebPage(
    val title: String,
    val sourceUrl: String,
    val textPreview: String,
    val articleLinks: List<ParsedFeedItem>,
)

private suspend fun fetchWebDocument(
    inputUrl: String,
): WebDocument {
    val connection = (URL(inputUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 10_000
        setRequestProperty("User-Agent", "DaysApp/0.1 (+android)")
        instanceFollowRedirects = true
    }
    return connection.inputStream.use { stream ->
        val body = stream.readBytes().toString(Charsets.UTF_8).take(250_000)
        val contentType = connection.contentType.orEmpty().lowercase()
        val resolvedUrl = connection.url.toString()
        val kind = when {
            contentType.contains("xml") || body.trimStart().startsWith("<rss", ignoreCase = true) || body.trimStart().startsWith("<feed", ignoreCase = true) -> WebDocumentKind.Feed
            contentType.contains("html") || body.contains("<html", ignoreCase = true) -> WebDocumentKind.Html
            else -> WebDocumentKind.Other
        }
        WebDocument(
            url = resolvedUrl,
            kind = kind,
            body = body,
        )
    }
}

internal fun discoverFeedUrls(
    pageUrl: String,
    html: String,
): List<String> {
    val relRegex = Regex(
        """<link\b[^>]*rel\s*=\s*["'][^"']*alternate[^"']*["'][^>]*type\s*=\s*["'](?:application|text)/(?:rss\+xml|atom\+xml|xml)["'][^>]*href\s*=\s*["']([^"']+)["'][^>]*>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    val hrefFirstRegex = Regex(
        """<link\b[^>]*href\s*=\s*["']([^"']+)["'][^>]*rel\s*=\s*["'][^"']*alternate[^"']*["'][^>]*type\s*=\s*["'](?:application|text)/(?:rss\+xml|atom\+xml|xml)["'][^>]*>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    return (relRegex.findAll(html).map { it.groupValues[1] } + hrefFirstRegex.findAll(html).map { it.groupValues[1] })
        .map { href -> resolveUrl(pageUrl, href) }
        .filter { it.startsWith("http") }
        .distinct()
        .toList()
}

internal fun parseReadableWebPage(
    pageUrl: String,
    html: String,
): ParsedWebPage {
    val title = Regex("""<title[^>]*>(.*?)</title>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        .find(html)
        ?.groupValues
        ?.getOrNull(1)
        .orEmpty()
        .decodeHtmlEntities()
        .normalizeWhitespace()
        .ifBlank { shortHost(pageUrl) }
    return ParsedWebPage(
        title = title.take(90),
        sourceUrl = pageUrl,
        textPreview = extractReadableText(html).take(20_000),
        articleLinks = extractArticleLinks(pageUrl, html),
    )
}

internal fun parseFeedDocument(
    sourceUrl: String,
    xml: String,
): ParsedFeed {
    val factory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        runCatching { setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true) }
        runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
        runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
    }
    val document = factory.newDocumentBuilder().parse(xml.byteInputStream())
    val root = document.documentElement
    return if (root.localName.equals("feed", ignoreCase = true) || root.tagName.equals("feed", ignoreCase = true)) {
        parseAtomFeed(sourceUrl, root)
    } else {
        parseRssFeed(sourceUrl, root)
    }
}

private fun parseRssFeed(
    sourceUrl: String,
    root: Element,
): ParsedFeed {
    val channel = root.getElementsByTagName("channel").item(0) as? Element ?: root
    val title = channel.childText("title").ifBlank { shortHost(sourceUrl) }
    val items = channel.childElements("item")
        .mapNotNull { item ->
            val link = item.childText("link").trim()
            if (!link.startsWith("http")) return@mapNotNull null
            ParsedFeedItem(
                title = item.childText("title").ifBlank { shortHost(link) },
                link = link,
                detail = item.childText("pubDate").ifBlank {
                    item.childText("description").take(140)
                },
            )
        }
    return ParsedFeed(
        title = title,
        sourceUrl = sourceUrl,
        items = items,
    )
}

private fun parseAtomFeed(
    sourceUrl: String,
    root: Element,
): ParsedFeed {
    val title = root.childText("title").ifBlank { shortHost(sourceUrl) }
    val items = root.childElements("entry")
        .mapNotNull { entry ->
            val link = entry.childLink().trim()
            if (!link.startsWith("http")) return@mapNotNull null
            ParsedFeedItem(
                title = entry.childText("title").ifBlank { shortHost(link) },
                link = link,
                detail = entry.childText("updated").ifBlank {
                    entry.childText("published").ifBlank {
                        entry.childText("summary").take(140)
                    }
                },
            )
        }
    return ParsedFeed(
        title = title,
        sourceUrl = sourceUrl,
        items = items,
    )
}

internal fun ParsedFeed.toDrafts(
    knownReferences: Set<String>,
): List<AreaImportDraft> {
    val newItems = items.filterNot { it.link in knownReferences }
    if (newItems.isEmpty()) return emptyList()
    val summary = AreaImportDraft(
        kind = AreaImportKind.Text,
        title = "$title Feed",
        detail = "${newItems.size} neue Feed-Eintraege gelesen.",
        reference = buildString {
            appendLine("Feed: $title")
            appendLine("Quelle: $sourceUrl")
            appendLine()
            newItems.forEach { item ->
                appendLine("- ${item.title}")
                if (item.detail.isNotBlank()) appendLine("  ${item.detail}")
                appendLine("  ${item.link}")
            }
        }.trim(),
    )
    return listOf(summary) + newItems.map { item ->
        AreaImportDraft(
            kind = AreaImportKind.Link,
            title = item.title.take(90),
            detail = item.detail.ifBlank { "Aus dem Feed gelesen." },
            reference = item.link,
        )
    }
}

internal fun ParsedWebPage.toDrafts(
    knownReferences: Set<String>,
): List<AreaImportDraft> {
    val drafts = mutableListOf<AreaImportDraft>()
    val marker = "Web-Reader: $sourceUrl"
    val alreadyHasReader = knownReferences.any { it.contains(marker) }
    if (!alreadyHasReader && textPreview.isNotBlank()) {
        drafts += AreaImportDraft(
            kind = AreaImportKind.Text,
            title = title,
            detail = "Website direkt gelesen. Kein RSS/Atom-Feed erkannt.",
            reference = buildString {
                appendLine(marker)
                appendLine("Titel: $title")
                appendLine()
                append(textPreview)
            }.trim(),
        )
    }
    drafts += articleLinks
        .filterNot { item -> item.link in knownReferences || drafts.any { it.reference == item.link } }
        .map { item ->
            AreaImportDraft(
                kind = AreaImportKind.Link,
                title = item.title.take(90),
                detail = item.detail.ifBlank { "Artikel auf dieser Website gefunden." },
                reference = item.link,
            )
        }
    return drafts
}

private fun resolveUrl(baseUrl: String, href: String): String {
    return runCatching { URI(baseUrl).resolve(href).toString() }.getOrDefault(href)
}

internal fun shortHost(url: String): String {
    return Regex("""https?://(?:www\.)?([^/\s]+)""", RegexOption.IGNORE_CASE)
        .find(url)
        ?.groupValues
        ?.getOrNull(1)
        ?.substringBefore('.')
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .orEmpty()
        .ifBlank { url }
}

private fun Element.childText(tagName: String): String {
    return childElements(tagName).firstOrNull()?.textContent?.trim().orEmpty()
}

private fun Element.childLink(): String {
    return childElements("link")
        .mapNotNull { link ->
            link.getAttribute("href").takeIf(String::isNotBlank)
                ?: link.textContent?.trim()?.takeIf(String::isNotBlank)
        }
        .firstOrNull()
        .orEmpty()
}

private fun Element.childElements(tagName: String): List<Element> {
    val nodes = childNodes ?: return emptyList()
    return buildList {
        repeat(nodes.length) { index ->
            val node = nodes.item(index) as? Element ?: return@repeat
            if (node.localName.equals(tagName, ignoreCase = true) || node.tagName.equals(tagName, ignoreCase = true)) {
                add(node)
            }
        }
    }
}

private fun extractReadableText(
    html: String,
): String {
    extractEmbeddedArticleText(html)?.let { embedded ->
        if (embedded.isNotBlank()) return embedded
    }
    val cleaned = html
        .replace(Regex("""<script\b[^>]*>.*?</script>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
        .replace(Regex("""<style\b[^>]*>.*?</style>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
        .replace(Regex("""<nav\b[^>]*>.*?</nav>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
        .replace(Regex("""<footer\b[^>]*>.*?</footer>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
        .replace(Regex("""<[^>]+>"""), "\n")
        .decodeHtmlEntities()
    return cleaned
        .lineSequence()
        .map(String::normalizeWhitespace)
        .filter { it.length >= 30 }
        .take(60)
        .joinToString("\n\n")
}

private fun extractEmbeddedArticleText(
    html: String,
): String? {
    val patterns = listOf(
        Regex("""["']articleBody["']\s*:\s*["']((?:\\.|[^"\\])+)["']""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
        Regex("""["']text["']\s*:\s*["']((?:\\.|[^"\\])+)["']""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
    )
    return patterns.asSequence()
        .mapNotNull { regex -> regex.find(html)?.groupValues?.getOrNull(1) }
        .map(String::decodeJsonStringContent)
        .map { text ->
            text
                .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n\n")
                .replace(Regex("""<[^>]+>"""), "\n")
                .decodeHtmlEntities()
                .lineSequence()
                .map(String::normalizeWhitespace)
                .filter(String::isNotBlank)
                .joinToString("\n\n")
        }
        .firstOrNull { it.length >= 80 }
}

private fun extractArticleLinks(
    pageUrl: String,
    html: String,
): List<ParsedFeedItem> {
    val baseHost = hostDomain(pageUrl)
    val articleRegex = Regex(
        """<a\b[^>]*href\s*=\s*["']([^"']+)["'][^>]*>(.*?)</a>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    return articleRegex.findAll(html)
        .mapNotNull { match ->
            val link = resolveUrl(pageUrl, match.groupValues[1].trim())
            val title = match.groupValues[2]
                .replace(Regex("""<[^>]+>"""), " ")
                .decodeHtmlEntities()
                .normalizeWhitespace()
            if (!link.startsWith("http")) return@mapNotNull null
            if (hostDomain(link) != baseHost) return@mapNotNull null
            if (title.length < 16) return@mapNotNull null
            if (title.length > 140) return@mapNotNull null
            if (link == pageUrl) return@mapNotNull null
            ParsedFeedItem(
                title = title,
                link = link,
                detail = "Artikel auf dieser Website gefunden.",
            )
        }
        .distinctBy(ParsedFeedItem::link)
        .take(5)
        .toList()
}

private fun hostDomain(url: String): String {
    return Regex("""https?://(?:www\.)?([^/\s]+)""", RegexOption.IGNORE_CASE)
        .find(url)
        ?.groupValues
        ?.getOrNull(1)
        .orEmpty()
}

private fun String.normalizeWhitespace(): String {
    return replace(Regex("""\s+"""), " ").trim()
}

private fun String.decodeHtmlEntities(): String {
    return replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace("&bdquo;", "„")
        .replace("&ldquo;", "“")
        .replace("&rdquo;", "”")
        .replace("&lsquo;", "‘")
        .replace("&rsquo;", "’")
        .replace("&ndash;", "-")
        .replace("&mdash;", "-")
        .replace("&hellip;", "...")
        .replace("&auml;", "ä")
        .replace("&ouml;", "ö")
        .replace("&uuml;", "ü")
        .replace("&Auml;", "Ä")
        .replace("&Ouml;", "Ö")
        .replace("&Uuml;", "Ü")
        .replace("&szlig;", "ß")
        .replace(Regex("""&#(\d+);""")) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
        }
        .replace(Regex("""&#x([0-9a-fA-F]+);""")) { match ->
            match.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: match.value
        }
}

private fun String.decodeJsonStringContent(): String {
    val builder = StringBuilder(length)
    var index = 0
    while (index < length) {
        val current = this[index]
        if (current != '\\' || index == lastIndex) {
            builder.append(current)
            index += 1
            continue
        }
        when (val escape = this[index + 1]) {
            '\\', '"', '/' -> builder.append(escape)
            'b' -> builder.append('\b')
            'f' -> builder.append('\u000C')
            'n' -> builder.append('\n')
            'r' -> builder.append('\r')
            't' -> builder.append('\t')
            'u' -> {
                val hex = substring(index + 2, (index + 6).coerceAtMost(length))
                val decoded = hex.takeIf { it.length == 4 }?.toIntOrNull(16)?.toChar()
                if (decoded != null) {
                    builder.append(decoded)
                    index += 4
                } else {
                    builder.append("\\u")
                }
            }
            else -> builder.append(escape)
        }
        index += 2
    }
    return builder.toString()
}
