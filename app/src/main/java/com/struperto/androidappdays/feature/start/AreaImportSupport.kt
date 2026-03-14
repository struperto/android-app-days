package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.CaptureItem

private const val AreaImportPrefix = "[days-import]"

enum class AreaImportKind(
    val label: String,
) {
    Link("Link"),
    File("Datei"),
    Image("Bild"),
    Text("Text"),
}

data class AreaImportedMaterialState(
    val id: String,
    val kind: AreaImportKind,
    val title: String,
    val detail: String,
    val reference: String,
    val isPending: Boolean = false,
)

data class AreaImportDraft(
    val kind: AreaImportKind,
    val title: String,
    val detail: String,
    val reference: String,
)

fun buildAreaImportCaptureText(
    kind: AreaImportKind,
    title: String,
    detail: String,
    reference: String,
): String {
    return listOf(
        AreaImportPrefix,
        "kind=${kind.name}",
        "title=${title.trim()}",
        "detail=${detail.trim()}",
        "reference=${reference.trim()}",
    ).joinToString(separator = "\n")
}

fun parseAreaImportCapture(
    item: CaptureItem,
): AreaImportedMaterialState? {
    if (!item.text.startsWith(AreaImportPrefix)) return null
    val values = item.text
        .lineSequence()
        .drop(1)
        .mapNotNull { line ->
            val separatorIndex = line.indexOf('=')
            if (separatorIndex <= 0) return@mapNotNull null
            line.substring(0, separatorIndex) to line.substring(separatorIndex + 1)
        }
        .toMap()
    val kind = values["kind"]?.let { raw ->
        runCatching { AreaImportKind.valueOf(raw) }.getOrNull()
    } ?: return null
    return AreaImportedMaterialState(
        id = item.id,
        kind = kind,
        title = values["title"].orEmpty().ifBlank { kind.label },
        detail = values["detail"].orEmpty(),
        reference = values["reference"].orEmpty(),
    )
}

fun buildLinkImportState(
    url: String,
): Pair<String, String> {
    val host = Regex("""https?://(?:www\.)?([^/\s]+)""", RegexOption.IGNORE_CASE)
        .find(url)
        ?.groupValues
        ?.getOrNull(1)
        ?.substringBefore('.')
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .orEmpty()
    val title = host.ifBlank { "Web Link" }
    val detail = "Link fuer diesen Bereich gespeichert."
    return title to detail
}

fun buildTextImportState(
    text: String,
    subject: String = "",
): Pair<String, String> {
    val cleanedSubject = subject.trim()
    val cleanedText = text.trim()
    val title = cleanedSubject.ifBlank {
        cleanedText
            .lineSequence()
            .map(String::trim)
            .firstOrNull(String::isNotBlank)
            ?.take(42)
            .orEmpty()
    }.ifBlank { "Text" }
    return title to "Text fuer diesen Bereich gespeichert."
}

fun buildImportedMaterialPrompt(
    item: AreaImportedMaterialState,
): String {
    return when (item.kind) {
        AreaImportKind.Link -> "Ich moechte, dass du diese Website fuer mich im Blick behaeltst: ${item.reference}"
        AreaImportKind.File -> "Ich moechte, dass du diese Datei fuer mich ordnest: ${item.title}"
        AreaImportKind.Image -> "Ich moechte, dass du dieses Bild fuer mich sichtest: ${item.title}"
        AreaImportKind.Text -> "Ich moechte, dass du diesen Text fuer mich ordnest: ${item.title}"
    }
}
