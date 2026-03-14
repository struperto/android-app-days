package com.struperto.androidappdays.feature.single.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.feature.start.inferWebFeedSourceKind
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsRuntimeRepositoryDeviceTest {
    @Test
    fun refreshesFazAndStolIntoRuntimeState() = runBlocking {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp
        val container = app.appContainer
        container.ensureAppBootstrapped()
        val existing = container.areaKernelRepository.loadActiveInstances()
            .firstOrNull { it.title.equals("News", ignoreCase = true) }
        val area = existing ?: container.areaKernelRepository.createActiveInstance(
            CreateAreaInstanceDraft(
                title = "News",
                summary = "News-Testbereich",
                templateId = "medium",
                iconKey = "book",
                behaviorClass = AreaBehaviorClass.REFLECTION,
            ),
        )

        container.areaWebFeedSourceRepository.clearArea(area.areaId)
        listOf(
            "https://www.faz.net/rss/aktuell/" to AreaWebFeedSyncCadence.SixHours,
            "https://www.stol.it/" to AreaWebFeedSyncCadence.Daily,
        ).forEach { (url, cadence) ->
            container.areaWebFeedSourceRepository.save(
                areaId = area.areaId,
                url = url,
                sourceKind = inferWebFeedSourceKind(url),
                isAutoSyncEnabled = true,
                syncCadence = cadence,
            )
        }

        container.newsRuntimeRepository.refreshNow()
        val state = container.newsRuntimeRepository.state.value

        assertEquals(area.areaId, state.areaId)
        assertTrue(state.sourceAnalyses.map { it.url }.contains("https://www.faz.net/rss/aktuell/"))
        assertTrue(state.sourceAnalyses.map { it.url }.contains("https://www.stol.it/"))
        assertFalse(state.sourceAnalyses.any { it.url.contains("heise.de") })
        assertFalse(state.sourceAnalyses.any { it.url.contains("tagesschau.de") })
        assertTrue(state.sourceAnalyses.any { it.url.contains("faz.net") && it.statusLabel.isNotBlank() })
        assertTrue(state.sourceAnalyses.any { it.url.contains("stol.it") && it.statusLabel.isNotBlank() })
        assertTrue("Expected at least one news article after refresh.", state.articles.isNotEmpty())
    }
}
