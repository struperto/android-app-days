package com.struperto.androidappdays.feature.start

import android.content.Context
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextClassifier
import android.view.textclassifier.TextLinks

internal data class StartPlatformAiSignals(
    val entityTypes: Set<String> = emptySet(),
    val snippets: List<String> = emptyList(),
) {
    val hasUrl: Boolean
        get() = TextClassifier.TYPE_URL in entityTypes

    val hasEmail: Boolean
        get() = TextClassifier.TYPE_EMAIL in entityTypes

    val hasPhone: Boolean
        get() = TextClassifier.TYPE_PHONE in entityTypes

    val hasAddress: Boolean
        get() = TextClassifier.TYPE_ADDRESS in entityTypes

    val hasDateTime: Boolean
        get() = TextClassifier.TYPE_DATE in entityTypes || TextClassifier.TYPE_DATE_TIME in entityTypes

    val summaryLabel: String?
        get() = entityTypes
            .mapNotNull(::entityTypeLabel)
            .distinct()
            .take(3)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ", ")
            ?.let { "Android erkennt lokal: $it" }

    val summaryDetail: String?
        get() = snippets
            .take(2)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = " · ")
}

internal fun detectStartPlatformAiSignals(
    context: Context,
    rawInput: String,
): StartPlatformAiSignals {
    val text = rawInput.trim().take(1_000)
    if (text.isBlank()) return StartPlatformAiSignals()
    val manager = context.getSystemService(TextClassificationManager::class.java) ?: return StartPlatformAiSignals()
    val classifier = manager.textClassifier ?: return StartPlatformAiSignals()
    return runCatching {
        val request = TextLinks.Request.Builder(text)
            .setEntityConfig(
                TextClassifier.EntityConfig.createWithExplicitEntityList(
                    listOf(
                        TextClassifier.TYPE_URL,
                        TextClassifier.TYPE_EMAIL,
                        TextClassifier.TYPE_PHONE,
                        TextClassifier.TYPE_ADDRESS,
                        TextClassifier.TYPE_DATE,
                        TextClassifier.TYPE_DATE_TIME,
                        TextClassifier.TYPE_FLIGHT_NUMBER,
                    ),
                ),
            )
            .build()
        val links = classifier.generateLinks(request)
        val entityTypes = linkedSetOf<String>()
        val snippets = mutableListOf<String>()
        links.links.forEach { link ->
            repeat(link.entityCount) { index ->
                entityTypes += link.getEntity(index)
            }
            val snippet = text.substring(link.start, link.end).trim()
            if (snippet.isNotEmpty()) {
                snippets += snippet
            }
        }
        StartPlatformAiSignals(
            entityTypes = entityTypes,
            snippets = snippets.distinct(),
        )
    }.getOrDefault(StartPlatformAiSignals())
}

private fun entityTypeLabel(
    entityType: String,
): String? {
    return when (entityType) {
        TextClassifier.TYPE_URL -> "Link"
        TextClassifier.TYPE_EMAIL -> "Mail"
        TextClassifier.TYPE_PHONE -> "Telefon"
        TextClassifier.TYPE_ADDRESS -> "Ort"
        TextClassifier.TYPE_DATE -> "Datum"
        TextClassifier.TYPE_DATE_TIME -> "Zeit"
        TextClassifier.TYPE_FLIGHT_NUMBER -> "Flug"
        else -> null
    }
}
