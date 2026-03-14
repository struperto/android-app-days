package com.struperto.androidappdays.feature.start

private const val MaxBookmarkLinks = 20
private const val MaxMailLinks = 8
private val UrlRegex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
private val BookmarkAnchorRegex = Regex(
    pattern = """<a\b[^>]*href\s*=\s*["']([^"']+)["'][^>]*>(.*?)</a>""",
    options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
)

internal fun buildConnectorImportDrafts(
    displayName: String,
    mimeType: String,
    textContent: String?,
): List<AreaImportDraft> {
    val cleanedName = displayName.trim().ifBlank { "Import" }
    val cleanedText = textContent
        ?.replace("\u0000", "")
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: return emptyList()

    return when {
        isBookmarkDocument(cleanedName, mimeType, cleanedText) ->
            parseBookmarkDocument(cleanedName, cleanedText)
        isMailDocument(cleanedName, mimeType, cleanedText) ->
            parseMailDocument(cleanedName, cleanedText)
        isTextDocument(cleanedName, mimeType) ->
            parseTextDocument(cleanedName, cleanedText)
        else -> emptyList()
    }
}

private fun isBookmarkDocument(
    displayName: String,
    mimeType: String,
    textContent: String,
): Boolean {
    val lowerName = displayName.lowercase()
    val lowerMime = mimeType.lowercase()
    val lowerText = textContent.lowercase()
    return (lowerName.endsWith(".html") || lowerName.endsWith(".htm") || lowerMime.contains("html")) &&
        (lowerText.contains("bookmark") || lowerText.contains("<dt><a") || lowerText.contains("netscape-bookmark-file-format"))
}

private fun isMailDocument(
    displayName: String,
    mimeType: String,
    textContent: String,
): Boolean {
    val lowerName = displayName.lowercase()
    val lowerMime = mimeType.lowercase()
    val normalized = textContent.replace("\r\n", "\n")
    return lowerName.endsWith(".eml") ||
        lowerName.endsWith(".mbox") ||
        lowerMime.contains("message/rfc822") ||
        Regex("""(?m)^(from|subject|date):\s+.+$""", RegexOption.IGNORE_CASE).containsMatchIn(normalized)
}

private fun isTextDocument(
    displayName: String,
    mimeType: String,
): Boolean {
    val lowerName = displayName.lowercase()
    val lowerMime = mimeType.lowercase()
    return lowerMime.startsWith("text/") ||
        lowerName.endsWith(".txt") ||
        lowerName.endsWith(".md") ||
        lowerName.endsWith(".csv")
}

private fun parseBookmarkDocument(
    displayName: String,
    textContent: String,
): List<AreaImportDraft> {
    val links = BookmarkAnchorRegex.findAll(textContent)
        .mapNotNull { match ->
            val href = match.groupValues.getOrNull(1).orEmpty().trim()
            if (!href.startsWith("http")) return@mapNotNull null
            val rawTitle = match.groupValues.getOrNull(2).orEmpty()
            val title = decodeHtmlEntities(rawTitle)
                .replace(Regex("""\s+"""), " ")
                .trim()
                .ifBlank { href }
            AreaImportDraft(
                kind = AreaImportKind.Link,
                title = title.take(80),
                detail = "Aus ${displayName.trim()} gelesen.",
                reference = href,
            )
        }
        .distinctBy(AreaImportDraft::reference)
        .take(MaxBookmarkLinks)
        .toList()

    if (links.isEmpty()) return emptyList()

    val summaryText = buildString {
        appendLine("${links.size} Lesezeichen aus ${displayName.trim()} gelesen.")
        appendLine()
        links.take(10).forEach { link ->
            appendLine("- ${link.title}: ${link.reference}")
        }
    }.trim()

    return listOf(
        AreaImportDraft(
            kind = AreaImportKind.Text,
            title = displayName.substringBeforeLast('.').ifBlank { "Lesezeichen Import" },
            detail = "${links.size} Lesezeichen aus Datei gelesen.",
            reference = summaryText,
        ),
    ) + links
}

private fun parseMailDocument(
    displayName: String,
    textContent: String,
): List<AreaImportDraft> {
    val normalized = decodeQuotedPrintable(textContent.replace("\r\n", "\n"))
    val sections = normalized.split("\n\n", limit = 2)
    val headers = parseMailHeaders(sections.firstOrNull().orEmpty())
    val body = sections.getOrNull(1).orEmpty().trim()
    val subject = headers["subject"].orEmpty().ifBlank {
        displayName.substringBeforeLast('.').ifBlank { "Mail" }
    }
    val from = headers["from"].orEmpty()
    val date = headers["date"].orEmpty()
    val previewBody = body
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .take(12)
        .joinToString("\n")
        .take(4000)

    val summaryText = buildString {
        appendLine("Betreff: $subject")
        if (from.isNotBlank()) appendLine("Von: $from")
        if (date.isNotBlank()) appendLine("Datum: $date")
        if (previewBody.isNotBlank()) {
            appendLine()
            appendLine(previewBody)
        }
    }.trim()

    val summaryDraft = AreaImportDraft(
        kind = AreaImportKind.Text,
        title = subject.take(80),
        detail = "Mail aus Datei gelesen.",
        reference = summaryText,
    )

    val linkDrafts = UrlRegex.findAll(body)
        .map { it.value.trimEnd('.', ',', ';', ')') }
        .distinct()
        .take(MaxMailLinks)
        .map { url ->
            val (title, _) = buildLinkImportState(url)
            AreaImportDraft(
                kind = AreaImportKind.Link,
                title = title,
                detail = "Link aus Mail extrahiert.",
                reference = url,
            )
        }
        .toList()

    return listOf(summaryDraft) + linkDrafts
}

private fun parseTextDocument(
    displayName: String,
    textContent: String,
): List<AreaImportDraft> {
    val preview = textContent.take(4000)
    val title = displayName.substringBeforeLast('.').ifBlank { "Text Import" }
    val drafts = mutableListOf(
        AreaImportDraft(
            kind = AreaImportKind.Text,
            title = title.take(80),
            detail = "Text aus Datei gelesen.",
            reference = preview,
        ),
    )
    UrlRegex.findAll(textContent)
        .map { it.value.trimEnd('.', ',', ';', ')') }
        .distinct()
        .take(8)
        .forEach { url ->
            val (linkTitle, _) = buildLinkImportState(url)
            drafts += AreaImportDraft(
                kind = AreaImportKind.Link,
                title = linkTitle,
                detail = "Link aus Textdatei extrahiert.",
                reference = url,
            )
        }
    return drafts
}

private fun parseMailHeaders(headerBlock: String): Map<String, String> {
    val unfolded = headerBlock
        .lineSequence()
        .fold(mutableListOf<String>()) { lines, rawLine ->
            if (rawLine.startsWith(" ") || rawLine.startsWith("\t")) {
                if (lines.isNotEmpty()) {
                    lines[lines.lastIndex] = lines.last() + " " + rawLine.trim()
                }
            } else {
                lines += rawLine.trimEnd()
            }
            lines
        }
    return unfolded.mapNotNull { line ->
        val index = line.indexOf(':')
        if (index <= 0) return@mapNotNull null
        line.substring(0, index).trim().lowercase() to line.substring(index + 1).trim()
    }.toMap()
}

private fun decodeQuotedPrintable(raw: String): String {
    val withoutSoftBreaks = raw.replace(Regex("""=\n"""), "")
    return Regex("""=([0-9A-Fa-f]{2})""").replace(withoutSoftBreaks) { match ->
        match.groupValues[1].toInt(16).toChar().toString()
    }
}

private fun decodeHtmlEntities(raw: String): String {
    return raw
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
}
