package com.struperto.androidappdays.feature.single.assist

interface LocalAssistGateway {
    fun summarize(text: String): String?
    fun suggestNextStep(text: String): String?
}

class HeuristicLocalAssistGateway : LocalAssistGateway {
    override fun summarize(text: String): String? {
        val normalized = text.normalizeWhitespace()
        if (normalized.isBlank()) {
            return null
        }
        val firstSentence = normalized.split(Regex("(?<=[.!?])\\s+")).firstOrNull().orEmpty().trim()
        if (firstSentence.isNotEmpty() && firstSentence.length <= 140) {
            return firstSentence
        }
        if (normalized.length <= 140) {
            return normalized
        }
        return normalized.take(137).trimEnd() + "..."
    }

    override fun suggestNextStep(text: String): String? {
        val normalized = text.normalizeWhitespace().lowercase()
        if (normalized.isBlank()) {
            return null
        }
        return when {
            normalized.contains("anrufen") || normalized.contains("antworten") || normalized.contains("mail") -> {
                "Plane heute einen kurzen Kommunikationsblock und erledige ihn direkt."
            }
            normalized.contains("kaufen") || normalized.contains("bestellen") || normalized.contains("besorgen") -> {
                "Formuliere daraus eine klare Besorgung und nimm sie in den heutigen Plan."
            }
            normalized.contains("?") || normalized.contains("wie ") || normalized.contains("warum ") -> {
                "Ziehe die eigentliche Frage heraus und mache daraus ein einzelnes Vorhaben."
            }
            normalized.length > 120 -> {
                "Brich den Gedanken auf den kleinsten naechsten Schritt herunter und plane nur diesen."
            }
            else -> {
                "Mache daraus ein Vorhaben und gib ihm einen ersten 15-Minuten-Schritt."
            }
        }
    }
}

private fun String.normalizeWhitespace(): String {
    return trim().replace(Regex("\\s+"), " ")
}
