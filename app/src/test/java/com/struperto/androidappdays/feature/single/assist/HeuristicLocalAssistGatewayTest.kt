package com.struperto.androidappdays.feature.single.assist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicLocalAssistGatewayTest {
    private val gateway = HeuristicLocalAssistGateway()

    @Test
    fun summarize_prefersFirstSentence() {
        val result = gateway.summarize(
            "Morgen den Vermieter anrufen. Danach die offenen Unterlagen sortieren.",
        )

        assertEquals("Morgen den Vermieter anrufen.", result)
    }

    @Test
    fun suggestNextStep_detectsCommunicationIntent() {
        val result = gateway.suggestNextStep("Max noch wegen des Angebots anrufen")

        assertTrue(result.orEmpty().contains("Kommunikationsblock"))
    }
}
