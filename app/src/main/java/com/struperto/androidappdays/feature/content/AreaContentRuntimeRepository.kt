package com.struperto.androidappdays.feature.content

import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.CaptureItem
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.domain.area.AreaInstance
import com.struperto.androidappdays.feature.start.AreaImportKind
import com.struperto.androidappdays.feature.start.AreaImportedMaterialState
import com.struperto.androidappdays.feature.start.inferWebFeedSourceKind
import com.struperto.androidappdays.feature.start.parseAreaImportCapture
import com.struperto.androidappdays.feature.start.parseReadableWebPage
import com.struperto.androidappdays.feature.start.shortHost
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class AreaContentItem(
    val id: String,
    val areaId: String,
    val title: String,
    val sourceLabel: String,
    val sourceUrl: String,
    val contentUrl: String,
    val summary: String,
    val body: String = "",
    val publishedLabel: String,
    val publishedAtMillis: Long? = null,
    val kind: AreaContentKind = AreaContentKind.Article,
    val platform: AreaContentPlatform = AreaContentPlatform.Web,
    val creatorLabel: String = "",
    val contentState: AreaContentState = AreaContentState.Pending,
    val contentDetail: String = "",
)

enum class AreaContentKind {
    Article,
    SocialPost,
    Video,
    Link,
}

enum class AreaContentPlatform(
    val label: String,
) {
    Web("Web"),
    X("X"),
    Instagram("Instagram"),
    YouTube("YouTube"),
}

enum class AreaContentState {
    Pending,
    Loading,
    Ready,
    AnalysisNeeded,
}

data class AreaContentFeedState(
    val areaId: String,
    val areaTitle: String,
    val statusLabel: String,
    val statusDetail: String,
    val items: List<AreaContentItem>,
)

data class AreaContentRuntimeState(
    val feeds: Map<String, AreaContentFeedState> = emptyMap(),
)

interface AreaContentRuntimeRepository {
    val state: StateFlow<AreaContentRuntimeState>

    fun start(scope: CoroutineScope)

    suspend fun refreshNow()

    suspend fun ensureItemContent(itemId: String)

    suspend fun markItemRead(itemId: String)

    fun itemById(itemId: String): AreaContentItem?
}

class LocalAreaContentRuntimeRepository(
    private val areaKernelRepository: AreaKernelRepository,
    private val captureRepository: CaptureRepository,
    private val clock: Clock,
) : AreaContentRuntimeRepository {
    override val state: StateFlow<AreaContentRuntimeState>
        get() = mutableState.asStateFlow()

    private val mutableState = MutableStateFlow(AreaContentRuntimeState())
    private val rebuildMutex = Mutex()
    private val dismissedItemIds = linkedSetOf<String>()
    private var watcherJob: Job? = null

    override fun start(scope: CoroutineScope) {
        if (watcherJob?.isActive == true) return
        watcherJob = scope.launch {
            combine(
                areaKernelRepository.observeActiveInstances(),
                captureRepository.observeOpen(),
            ) { areas, captures ->
                rebuildFrom(
                    areaInstances = areas,
                    captures = captures,
                )
            }.collect { nextState ->
                rebuildMutex.withLock {
                    val previousItems = mutableState.value.feeds.values
                        .flatMap(AreaContentFeedState::items)
                        .associateBy(AreaContentItem::id)
                    mutableState.value = nextState.mergeContent(previousItems)
                }
            }
        }
    }

    override suspend fun refreshNow() {
        val areas = areaKernelRepository.loadActiveInstances()
        val captures = captureRepository.observeOpen().first()
        val nextState = rebuildFrom(
            areaInstances = areas,
            captures = captures,
        )
        rebuildMutex.withLock {
            val previousItems = mutableState.value.feeds.values
                .flatMap(AreaContentFeedState::items)
                .associateBy(AreaContentItem::id)
            mutableState.value = nextState.mergeContent(previousItems)
        }
    }

    override suspend fun ensureItemContent(itemId: String) {
        val item = rebuildMutex.withLock<AreaContentItem?> {
            val current = itemById(itemId) ?: return@withLock null
            if (!current.requiresReaderContent()) return@withLock null
            if (current.contentState == AreaContentState.Ready || current.contentState == AreaContentState.Loading) {
                return@withLock null
            }
            mutableState.value = mutableState.value.updateItem(
                itemId = itemId,
                transform = { existing ->
                    existing.copy(
                        contentState = AreaContentState.Loading,
                        contentDetail = "Volltext wird geladen.",
                    )
                },
            )
            current
        }
        if (item == null) return
        val loaded = loadReadableContent(item)
        rebuildMutex.withLock {
            if (itemById(itemId) == null) return@withLock
            mutableState.value = mutableState.value.updateItem(
                itemId = itemId,
                transform = { loaded },
            )
        }
    }

    override suspend fun markItemRead(itemId: String) {
        rebuildMutex.withLock {
            dismissedItemIds += itemId
            mutableState.value = mutableState.value.removeItem(itemId)
        }
    }

    override fun itemById(itemId: String): AreaContentItem? {
        return mutableState.value.feeds.values
            .asSequence()
            .flatMap { it.items.asSequence() }
            .firstOrNull { it.id == itemId }
    }

    private fun rebuildFrom(
        areaInstances: List<AreaInstance>,
        captures: List<CaptureItem>,
    ): AreaContentRuntimeState {
        val capturesByAreaId = captures
            .asSequence()
            .mapNotNull { capture ->
                val areaId = capture.areaId ?: return@mapNotNull null
                val imported = parseAreaImportCapture(capture) ?: return@mapNotNull null
                areaId to (capture to imported)
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second },
            )
        val feeds = areaInstances
            .sortedBy(AreaInstance::sortOrder)
            .mapNotNull { area ->
                val items = capturesByAreaId[area.areaId]
                    .orEmpty()
                    .filterNot { (capture, _) -> capture.id in dismissedItemIds }
                    .mapNotNull { (capture, imported) -> capture.toAreaContentItem(imported) }
                    .sortedWith(
                        compareByDescending<AreaContentItem> { it.publishedAtMillis ?: Long.MIN_VALUE }
                            .thenByDescending { captures.firstOrNull { capture -> capture.id == it.id }?.createdAt ?: Long.MIN_VALUE }
                            .thenBy { it.title.lowercase() },
                    )
                if (items.isEmpty()) return@mapNotNull null
                area.areaId to AreaContentFeedState(
                    areaId = area.areaId,
                    areaTitle = area.title,
                    statusLabel = "${items.size} Inhalt${if (items.size == 1) "" else "e"}",
                    statusDetail = "Lokale Links, Posts oder Videos dieses Bereichs erscheinen hier als eigener Feed.",
                    items = items,
                )
            }
            .toMap(linkedMapOf())
        return AreaContentRuntimeState(feeds = feeds)
    }

    private fun CaptureItem.toAreaContentItem(
        imported: AreaImportedMaterialState,
    ): AreaContentItem? {
        if (imported.kind != AreaImportKind.Link) return null
        val reference = imported.reference.trim()
        if (!reference.startsWith("http")) return null
        if (imported.shouldStayInfrastructureLink()) return null
        val areaId = areaId ?: return null
        val platform = detectPlatform(reference)
        val creatorLabel = resolveCreatorLabel(reference, platform)
        val kind = when (platform) {
            AreaContentPlatform.YouTube -> AreaContentKind.Video
            AreaContentPlatform.X,
            AreaContentPlatform.Instagram -> AreaContentKind.SocialPost
            AreaContentPlatform.Web -> AreaContentKind.Article
        }
        val publishedAt = parsePublishedAtMillis(imported.detail).takeIf { it != createdAt } ?: createdAt
        return AreaContentItem(
            id = id,
            areaId = areaId,
            title = imported.title.trim().ifBlank { defaultTitleFor(reference, platform, creatorLabel) },
            sourceLabel = sourceLabelFor(reference, platform),
            sourceUrl = reference,
            contentUrl = reference,
            summary = buildSummary(imported, platform, creatorLabel),
            body = if (kind.requiresReaderContent()) "" else buildInlineBody(imported, platform, creatorLabel),
            publishedLabel = resolvePublishedLabel(imported.detail, createdAt, clock.zone),
            publishedAtMillis = publishedAt,
            kind = kind,
            platform = platform,
            creatorLabel = creatorLabel,
            contentState = if (kind.requiresReaderContent()) AreaContentState.Pending else AreaContentState.Ready,
            contentDetail = if (kind.requiresReaderContent()) "" else "Lokal erkannter Medien-Link.",
        )
    }

    private suspend fun loadReadableContent(
        item: AreaContentItem,
    ): AreaContentItem {
        val document = runCatching { fetchAreaContentDocument(item.contentUrl) }.getOrNull()
            ?: return item.copy(
                contentState = AreaContentState.AnalysisNeeded,
                contentDetail = "Der Inhalt konnte nicht geladen werden.",
            )
        if (document.kind != AreaContentDocumentKind.Html) {
            return item.copy(
                contentState = AreaContentState.AnalysisNeeded,
                contentDetail = "Der Link liefert keinen lesbaren HTML-Text.",
            )
        }
        val parsed = parseReadableWebPage(item.contentUrl, document.body)
        val body = parsed.textPreview.trim()
        if (body.length < MinimumReadableBodyLength) {
            return item.copy(
                body = body,
                contentState = AreaContentState.AnalysisNeeded,
                contentDetail = "Der Volltext ist auf dieser Seite noch zu duenn oder zu unruhig fuer den Reader.",
            )
        }
        return item.copy(
            body = body,
            contentState = AreaContentState.Ready,
            contentDetail = "Volltext geladen.",
        )
    }
}

private data class AreaContentDocument(
    val kind: AreaContentDocumentKind,
    val body: String,
)

private enum class AreaContentDocumentKind {
    Html,
    Other,
}

private suspend fun fetchAreaContentDocument(
    inputUrl: String,
): AreaContentDocument = withContext(Dispatchers.IO) {
    val connection = (URL(inputUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 10_000
        setRequestProperty("User-Agent", "DaysApp/0.1 (+android)")
        instanceFollowRedirects = true
    }
    connection.inputStream.use { stream ->
        val body = stream.readBytes().toString(Charsets.UTF_8).take(300_000)
        val contentType = connection.contentType.orEmpty().lowercase()
        val kind = when {
            contentType.contains("html") || body.contains("<html", ignoreCase = true) -> AreaContentDocumentKind.Html
            else -> AreaContentDocumentKind.Other
        }
        AreaContentDocument(
            kind = kind,
            body = body,
        )
    }
}

private fun AreaImportedMaterialState.shouldStayInfrastructureLink(): Boolean {
    val detailText = detail.lowercase()
    return (detailText.contains("rss/atom-feed") || detailText.contains("feed-pfad")) &&
        inferWebFeedSourceKind(reference).storageKey == "feed"
}

private fun detectPlatform(url: String): AreaContentPlatform {
    val normalized = url.lowercase()
    return when {
        "youtube.com" in normalized || "youtu.be" in normalized -> AreaContentPlatform.YouTube
        "instagram.com" in normalized -> AreaContentPlatform.Instagram
        "x.com" in normalized || "twitter.com" in normalized -> AreaContentPlatform.X
        else -> AreaContentPlatform.Web
    }
}

private fun resolveCreatorLabel(
    url: String,
    platform: AreaContentPlatform,
): String {
    val pathSegments = runCatching {
        URI(url).path.orEmpty().split('/').filter(String::isNotBlank)
    }.getOrDefault(emptyList())
    return when (platform) {
        AreaContentPlatform.X -> pathSegments.firstOrNull()
            ?.takeIf { it.lowercase() !in setOf("home", "explore", "i", "search", "notifications") }
            ?.let { "@$it" }
            .orEmpty()
        AreaContentPlatform.Instagram -> pathSegments.firstOrNull()
            ?.takeIf { it.lowercase() !in setOf("p", "reel", "reels", "tv", "stories", "explore") }
            ?.let { "@$it" }
            .orEmpty()
        AreaContentPlatform.YouTube -> pathSegments.firstOrNull()
            ?.takeIf { it.startsWith("@") }
            .orEmpty()
        AreaContentPlatform.Web -> ""
    }
}

private fun sourceLabelFor(
    reference: String,
    platform: AreaContentPlatform,
): String {
    return when (platform) {
        AreaContentPlatform.YouTube -> "YouTube"
        AreaContentPlatform.Instagram -> "Instagram"
        AreaContentPlatform.X -> "X"
        AreaContentPlatform.Web -> shortHost(reference)
    }
}

private fun defaultTitleFor(
    reference: String,
    platform: AreaContentPlatform,
    creatorLabel: String,
): String {
    return when (platform) {
        AreaContentPlatform.YouTube -> "Video${creatorLabel.takeIf(String::isNotBlank)?.let { " von $it" }.orEmpty()}"
        AreaContentPlatform.X -> "Post${creatorLabel.takeIf(String::isNotBlank)?.let { " von $it" }.orEmpty()}"
        AreaContentPlatform.Instagram -> "Insta-Post${creatorLabel.takeIf(String::isNotBlank)?.let { " von $it" }.orEmpty()}"
        AreaContentPlatform.Web -> shortHost(reference)
    }
}

private fun buildSummary(
    imported: AreaImportedMaterialState,
    platform: AreaContentPlatform,
    creatorLabel: String,
): String {
    val normalizedDetail = imported.detail.trim()
    if (normalizedDetail.isNotBlank() && !normalizedDetail.equals("Link fuer diesen Bereich gespeichert.", ignoreCase = true)) {
        return normalizedDetail
    }
    return when (platform) {
        AreaContentPlatform.YouTube -> "Lokal erkanntes Video${creatorLabel.takeIf(String::isNotBlank)?.let { " von $it" }.orEmpty()}."
        AreaContentPlatform.X -> "Lokal erkannter X-Post${creatorLabel.takeIf(String::isNotBlank)?.let { " von $it" }.orEmpty()}."
        AreaContentPlatform.Instagram -> "Lokal erkannter Instagram-Post${creatorLabel.takeIf(String::isNotBlank)?.let { " von $it" }.orEmpty()}."
        AreaContentPlatform.Web -> "Lokal erkannter Web-Link fuer diesen Bereich."
    }
}

private fun buildInlineBody(
    imported: AreaImportedMaterialState,
    platform: AreaContentPlatform,
    creatorLabel: String,
): String {
    return buildString {
        append(
            when (platform) {
                AreaContentPlatform.YouTube -> "Days hat diesen Link lokal als Video erkannt."
                AreaContentPlatform.X -> "Days hat diesen Link lokal als X-Post erkannt."
                AreaContentPlatform.Instagram -> "Days hat diesen Link lokal als Instagram-Post erkannt."
                AreaContentPlatform.Web -> "Days hat diesen Link lokal als Web-Eintrag erkannt."
            },
        )
        if (creatorLabel.isNotBlank()) {
            append("\n\nKonto: ")
            append(creatorLabel)
        }
        imported.detail.trim()
            .takeIf { it.isNotBlank() && !it.equals("Link fuer diesen Bereich gespeichert.", ignoreCase = true) }
            ?.let { detail ->
                append("\n\nKontext: ")
                append(detail)
            }
        append("\n\nQuelle: ")
        append(imported.reference)
    }
}

private fun resolvePublishedLabel(
    detail: String,
    createdAtMillis: Long,
    zoneId: ZoneId,
): String {
    val normalized = detail.trim()
    if (normalized.isNotBlank() && !normalized.equals("Link fuer diesen Bereich gespeichert.", ignoreCase = true)) {
        return normalized
    }
    return Instant.ofEpochMilli(createdAtMillis)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"))
}

private fun parsePublishedAtMillis(value: String): Long? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    return runCatching { OffsetDateTime.parse(trimmed).toInstant().toEpochMilli() }.getOrNull()
        ?: runCatching {
            ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        }.getOrNull()
}

private fun AreaContentKind.requiresReaderContent(): Boolean {
    return this == AreaContentKind.Article
}

fun AreaContentItem.requiresReaderContent(): Boolean {
    return kind.requiresReaderContent()
}

private fun AreaContentRuntimeState.mergeContent(
    previousItems: Map<String, AreaContentItem>,
): AreaContentRuntimeState {
    return copy(
        feeds = feeds.mapValues { (_, feed) ->
            feed.copy(
                items = feed.items.map { item ->
                    item.mergeContent(previousItems[item.id])
                },
            )
        },
    )
}

private fun AreaContentItem.mergeContent(
    previous: AreaContentItem?,
): AreaContentItem {
    if (previous == null) return this
    return copy(
        body = previous.body,
        contentState = previous.contentState,
        contentDetail = previous.contentDetail,
    )
}

private fun AreaContentRuntimeState.updateItem(
    itemId: String,
    transform: (AreaContentItem) -> AreaContentItem,
): AreaContentRuntimeState {
    return copy(
        feeds = feeds.mapValues { (_, feed) ->
            feed.copy(
                items = feed.items.map { item ->
                    if (item.id == itemId) transform(item) else item
                },
            )
        },
    )
}

private fun AreaContentRuntimeState.removeItem(
    itemId: String,
): AreaContentRuntimeState {
    return copy(
        feeds = feeds.mapValues { (_, feed) ->
            feed.copy(
                items = feed.items.filterNot { it.id == itemId },
                statusLabel = "${feed.items.count { it.id != itemId }} Inhalt${if (feed.items.count { it.id != itemId } == 1) "" else "e"}",
            )
        }.filterValues { it.items.isNotEmpty() },
    )
}

private const val MinimumReadableBodyLength = 140
