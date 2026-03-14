package com.struperto.androidappdays.feature.start

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

private const val MaxConnectorImportChars = 250_000

internal fun Context.resolveImportDisplayName(uri: Uri): String {
    if (uri.scheme == "content") {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex).orEmpty().ifBlank { uri.lastPathSegment.orEmpty() }
            }
        }
    }
    return uri.lastPathSegment.orEmpty()
        .substringAfterLast('/')
        .ifBlank { "Import" }
}

internal fun Context.resolveDocumentImportDrafts(
    uri: Uri,
    fallbackMimeType: String = "",
): List<AreaImportDraft> {
    val displayName = resolveImportDisplayName(uri)
    val mimeType = contentResolver.getType(uri).orEmpty().ifBlank { fallbackMimeType }
    val textContent = readImportTextIfSupported(uri, displayName, mimeType)
    val connectorDrafts = buildConnectorImportDrafts(
        displayName = displayName,
        mimeType = mimeType,
        textContent = textContent,
    )
    if (connectorDrafts.isNotEmpty()) {
        return connectorDrafts
    }
    return listOf(
        AreaImportDraft(
            kind = AreaImportKind.File,
            title = displayName.trim().ifBlank { "Datei" },
            detail = "Datei fuer diesen Bereich abgelegt.",
            reference = uri.toString(),
        ),
    )
}

private fun Context.readImportTextIfSupported(
    uri: Uri,
    displayName: String,
    mimeType: String,
): String? {
    if (!shouldAttemptTextRead(displayName, mimeType)) return null
    return runCatching {
        contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
            reader?.readTextLimited(MaxConnectorImportChars)
        }
    }.getOrNull()
}

private fun shouldAttemptTextRead(
    displayName: String,
    mimeType: String,
): Boolean {
    val lowerName = displayName.lowercase()
    val lowerMime = mimeType.lowercase()
    return lowerMime.startsWith("text/") ||
        lowerMime.contains("html") ||
        lowerMime.contains("xml") ||
        lowerMime.contains("json") ||
        lowerMime.contains("message/rfc822") ||
        lowerName.endsWith(".html") ||
        lowerName.endsWith(".htm") ||
        lowerName.endsWith(".txt") ||
        lowerName.endsWith(".md") ||
        lowerName.endsWith(".csv") ||
        lowerName.endsWith(".eml") ||
        lowerName.endsWith(".mbox")
}

private fun java.io.Reader.readTextLimited(limit: Int): String {
    val builder = StringBuilder()
    val buffer = CharArray(4_096)
    var remaining = limit
    while (remaining > 0) {
        val read = read(buffer, 0, minOf(buffer.size, remaining))
        if (read <= 0) break
        builder.append(buffer, 0, read)
        remaining -= read
    }
    return builder.toString()
}
