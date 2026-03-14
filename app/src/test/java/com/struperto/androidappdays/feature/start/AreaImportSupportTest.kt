package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.CaptureItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AreaImportSupportTest {
    @Test
    fun textImportRoundTripStaysReadable() {
        val item = CaptureItem(
            id = "capture-1",
            text = buildAreaImportCaptureText(
                kind = AreaImportKind.Text,
                title = "Wichtige Mail",
                detail = "Text fuer diesen Bereich gespeichert.",
                reference = "Bitte antworte bis morgen.",
            ),
            areaId = null,
            createdAt = 1L,
            updatedAt = 2L,
            status = "open",
        )

        val parsed = parseAreaImportCapture(item)

        assertNotNull(parsed)
        assertEquals(AreaImportKind.Text, parsed?.kind)
        assertEquals("Wichtige Mail", parsed?.title)
        assertEquals(
            "Ich moechte, dass du diesen Text fuer mich ordnest: Wichtige Mail",
            parsed?.let(::buildImportedMaterialPrompt),
        )
    }
}
