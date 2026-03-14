package com.struperto.androidappdays.feature.start

internal data class AreaQuestionOptionState(
    val id: String,
    val title: String,
    val detail: String,
)

internal data class AreaQuickQuestionState(
    val title: String,
    val prompt: String,
    val inputLabel: String,
    val options: List<AreaQuestionOptionState>,
)

internal fun buildAreaLinkQuestion(
    area: StartAreaDetailState,
    analysis: AreaMachineAnalysisState,
    url: String,
): AreaQuickQuestionState {
    val host = url
        .substringAfter("://", url)
        .substringBefore('/')
        .removePrefix("www.")
        .ifBlank { "diese Quelle" }

    val options = when {
        host.contains("youtube", ignoreCase = true) || host.contains("youtu.be", ignoreCase = true) ->
            listOf(
                AreaQuestionOptionState(
                    id = "watch",
                    title = "Neue Videos sammeln",
                    detail = "Days soll neue Inhalte finden und in diesem Bereich ruhig sammeln.",
                ),
                AreaQuestionOptionState(
                    id = "focus",
                    title = "Nur relevante Highlights",
                    detail = "Days soll nur die wichtigsten Treffer fuer diesen Bereich markieren.",
                ),
            )

        host.contains("instagram", ignoreCase = true) ||
            host.contains("x.com", ignoreCase = true) ||
            host.contains("twitter", ignoreCase = true) ->
            listOf(
                AreaQuestionOptionState(
                    id = "monitor",
                    title = "Diese Quelle beobachten",
                    detail = "Days soll neue Posts und Signale dieses Accounts oder Feeds beobachten.",
                ),
                AreaQuestionOptionState(
                    id = "collect",
                    title = "Nur einzelne Funde sichern",
                    detail = "Days soll nur dann sammeln, wenn etwas fuer den Bereich wirklich zaehlt.",
                ),
            )

        host.contains("news", ignoreCase = true) ||
            host.contains("zeit", ignoreCase = true) ||
            host.contains("spiegel", ignoreCase = true) ||
            host.contains("substack", ignoreCase = true) ->
            listOf(
                AreaQuestionOptionState(
                    id = "digest",
                    title = "Wichtige Updates merken",
                    detail = "Days soll Schlagzeilen und neue Eintraege fuer diesen Bereich verdichten.",
                ),
                AreaQuestionOptionState(
                    id = "archive",
                    title = "Als Lesestoff sammeln",
                    detail = "Days soll interessante Artikel eher als ruhigen Bestand ablegen.",
                ),
            )

        else ->
            listOf(
                AreaQuestionOptionState(
                    id = "watch",
                    title = "Diese Quelle im Blick halten",
                    detail = "Days soll die Seite als laufende Spur fuer diesen Bereich behandeln.",
                ),
                AreaQuestionOptionState(
                    id = "store",
                    title = "Nur diesen Link sichern",
                    detail = "Days soll den Link als einmaligen Fund fuer spaeteres Lesen ablegen.",
                ),
            )
    }

    return AreaQuickQuestionState(
        title = "Link kurz einsortieren",
        prompt = "$host wurde zu ${area.title} hinzugefuegt. Fuer ${analysis.typeLabel.lowercase()} brauche ich noch eine kurze Deutung.",
        inputLabel = "Eigene Antwort",
        options = options,
    )
}
