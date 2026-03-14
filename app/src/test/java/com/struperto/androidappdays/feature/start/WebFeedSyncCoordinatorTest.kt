package com.struperto.androidappdays.feature.start

import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureItemStatus
import com.struperto.androidappdays.data.repository.CaptureRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebFeedSyncCoordinatorTest {
    private val clock = Clock.fixed(
        Instant.parse("2026-03-13T09:00:00Z"),
        ZoneId.of("Europe/Berlin"),
    )

    @Test
    fun syncArea_persistsNewDraftsAndDiscoveredFeed() = runTest {
        val captureRepository = FakeCaptureRepository(
            initial = listOf(
                CaptureItem(
                    id = "c1",
                    text = buildAreaImportCaptureText(
                        kind = AreaImportKind.Link,
                        title = "Android",
                        detail = "Link fuer diesen Bereich gespeichert.",
                        reference = "https://developer.android.com/",
                    ),
                    areaId = "focus",
                    createdAt = clock.millis(),
                    updatedAt = clock.millis(),
                    status = CaptureItemStatus.OPEN,
                ),
            ),
        )
        val sourceRepository = FakeAreaWebFeedSourceRepository()
        val scheduler = FakeWebFeedSyncScheduler()
        val coordinator = LocalWebFeedSyncCoordinator(
            appContext = null,
            captureRepository = captureRepository,
            webFeedSourceRepository = sourceRepository,
            webFeedConnector = object : WebFeedConnector {
                override suspend fun sync(urls: List<String>, knownReferences: Set<String>): WebFeedSyncResult {
                    assertTrue("https://developer.android.com/" in urls)
                    return WebFeedSyncResult(
                        foundFeedUrls = listOf("https://developer.android.com/feeds/androidx-release-notes.xml"),
                        drafts = listOf(
                            AreaImportDraft(
                                kind = AreaImportKind.Text,
                                title = "Android Feed",
                                detail = "1 neue Feed-Eintraege gelesen.",
                                reference = "Feed summary",
                            ),
                            AreaImportDraft(
                                kind = AreaImportKind.Link,
                                title = "Release note",
                                detail = "Neu",
                                reference = "https://developer.android.com/jetpack/androidx/releases/activity",
                            ),
                        ),
                        message = "android.com gelesen: 1 neue Eintraege.",
                    )
                }
            },
            clock = clock,
            scheduler = scheduler,
        )

        val result = coordinator.syncArea("focus")

        assertEquals(1, result.newItemCount)
        assertEquals(3, captureRepository.open.value.size)
        val savedSource = sourceRepository.loadByArea("focus").single()
        assertEquals("https://developer.android.com/feeds/androidx-release-notes.xml", savedSource.url)
        assertEquals(AreaWebFeedSourceKind.Feed, savedSource.sourceKind)
        assertEquals(AreaWebFeedSyncCadence.SixHours, savedSource.syncCadence)
        assertEquals("1 neu", savedSource.lastStatusLabel)
        assertTrue(scheduler.lastSources.single().isAutoSyncEnabled)
        assertEquals(AreaWebFeedSyncCadence.SixHours, scheduler.lastSources.single().syncCadence)
    }

    @Test
    fun syncAll_onlyRefreshesDueSources() = runTest {
        val sourceRepository = FakeAreaWebFeedSourceRepository(
            initial = listOf(
                feedSource(
                    areaId = "news",
                    url = "https://stol.it/feed",
                    syncCadence = AreaWebFeedSyncCadence.Hourly,
                    lastSyncedAt = clock.millis() - 2 * 60 * 60 * 1000,
                ),
                feedSource(
                    areaId = "news",
                    url = "https://example.com/daily.xml",
                    syncCadence = AreaWebFeedSyncCadence.Daily,
                    lastSyncedAt = clock.millis() - 30 * 60 * 1000,
                ),
            ),
        )
        val requestedUrls = mutableListOf<List<String>>()
        val coordinator = LocalWebFeedSyncCoordinator(
            appContext = null,
            captureRepository = FakeCaptureRepository(),
            webFeedSourceRepository = sourceRepository,
            webFeedConnector = object : WebFeedConnector {
                override suspend fun sync(urls: List<String>, knownReferences: Set<String>): WebFeedSyncResult {
                    requestedUrls += urls
                    return WebFeedSyncResult(
                        foundFeedUrls = emptyList(),
                        drafts = emptyList(),
                        message = "Keine Neuigkeiten",
                    )
                }
            },
            clock = clock,
            scheduler = FakeWebFeedSyncScheduler(),
        )

        val syncedAreaCount = coordinator.syncAll()

        assertEquals(1, syncedAreaCount)
        assertEquals(listOf(listOf("https://stol.it/feed")), requestedUrls)
    }
}

private class FakeCaptureRepository(
    initial: List<CaptureItem> = emptyList(),
) : CaptureRepository {
    val open = MutableStateFlow(initial)

    override fun observeOpen(): Flow<List<CaptureItem>> = open

    override fun observeArchived(): Flow<List<CaptureItem>> = flowOf(emptyList())

    override fun observeWriteCountSince(sinceEpochMillis: Long): Flow<Int> = flowOf(0)

    override fun observeTouchedAreaIdsSince(sinceEpochMillis: Long): Flow<Set<String>> = flowOf(emptySet())

    override suspend fun createTextCapture(text: String, areaId: String?): CaptureItem {
        val item = CaptureItem(
            id = "cap-${open.value.size + 1}",
            text = text,
            areaId = areaId,
            createdAt = 0L,
            updatedAt = 0L,
            status = CaptureItemStatus.OPEN,
        )
        open.value = open.value + item
        return item
    }

    override suspend fun markConverted(id: String) = Unit

    override suspend fun archive(id: String) = Unit

    override suspend fun updateArea(id: String, areaId: String?) = Unit

    override suspend fun loadLatestOpen(): CaptureItem? = open.value.lastOrNull()

    override suspend fun loadById(id: String): CaptureItem? = open.value.firstOrNull { it.id == id }
}

private class FakeAreaWebFeedSourceRepository : AreaWebFeedSourceRepository {
    private val items = mutableListOf<AreaWebFeedSource>()

    constructor(initial: List<AreaWebFeedSource> = emptyList()) {
        items += initial
    }

    override fun observeAll(): Flow<List<AreaWebFeedSource>> = flowOf(items.toList())

    override fun observeByArea(areaId: String): Flow<List<AreaWebFeedSource>> = flowOf(items.filter { it.areaId == areaId })

    override suspend fun loadAll(): List<AreaWebFeedSource> = items.toList()

    override suspend fun loadByArea(areaId: String): List<AreaWebFeedSource> = items.filter { it.areaId == areaId }

    override suspend fun save(
        areaId: String,
        url: String,
        sourceKind: AreaWebFeedSourceKind,
        isAutoSyncEnabled: Boolean,
        syncCadence: AreaWebFeedSyncCadence,
    ) {
        items.removeAll { it.areaId == areaId && it.url == url }
        items += AreaWebFeedSource(
            areaId = areaId,
            url = url,
            sourceKind = sourceKind,
            isAutoSyncEnabled = isAutoSyncEnabled,
            syncCadence = syncCadence,
            lastSyncedAt = null,
            lastStatusLabel = "",
            lastStatusDetail = "",
        )
    }

    override suspend fun remove(areaId: String, url: String) {
        items.removeAll { it.areaId == areaId && it.url == url }
    }

    override suspend fun clearArea(areaId: String) {
        items.removeAll { it.areaId == areaId }
    }

    override suspend fun updateSyncResult(areaId: String, url: String, syncedAt: Long, statusLabel: String, statusDetail: String) {
        val current = items.first { it.areaId == areaId && it.url == url }
        items.remove(current)
        items += current.copy(
            lastSyncedAt = syncedAt,
            lastStatusLabel = statusLabel,
            lastStatusDetail = statusDetail,
        )
    }

    override suspend fun setAutoSyncEnabled(areaId: String, url: String, enabled: Boolean) {
        val current = items.first { it.areaId == areaId && it.url == url }
        items.remove(current)
        items += current.copy(isAutoSyncEnabled = enabled)
    }

    override suspend fun setSyncCadence(areaId: String, url: String, cadence: AreaWebFeedSyncCadence) {
        val current = items.first { it.areaId == areaId && it.url == url }
        items.remove(current)
        items += current.copy(syncCadence = cadence)
    }
}

private class FakeWebFeedSyncScheduler : WebFeedSyncScheduler {
    var lastSources: List<AreaWebFeedSource> = emptyList()

    override suspend fun refresh(sources: List<AreaWebFeedSource>) {
        lastSources = sources
    }
}

private fun feedSource(
    areaId: String,
    url: String,
    syncCadence: AreaWebFeedSyncCadence,
    lastSyncedAt: Long?,
): AreaWebFeedSource {
    return AreaWebFeedSource(
        areaId = areaId,
        url = url,
        sourceKind = AreaWebFeedSourceKind.Feed,
        isAutoSyncEnabled = true,
        syncCadence = syncCadence,
        lastSyncedAt = lastSyncedAt,
        lastStatusLabel = "",
        lastStatusDetail = "",
    )
}
