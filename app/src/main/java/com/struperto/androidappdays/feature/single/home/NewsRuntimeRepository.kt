package com.struperto.androidappdays.feature.single.home

import android.util.Log
import com.google.gson.Gson
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.data.repository.CreateAreaInstanceDraft
import com.struperto.androidappdays.domain.area.AreaBehaviorClass
import com.struperto.androidappdays.feature.start.discoverFeedUrls
import com.struperto.androidappdays.feature.start.inferWebFeedSourceKind
import com.struperto.androidappdays.feature.start.parseFeedDocument
import com.struperto.androidappdays.feature.start.parseReadableWebPage
import com.struperto.androidappdays.feature.start.shortHost
import java.net.HttpURLConnection
import java.net.URL
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class NewsArticle(
    val id: String,
    val title: String,
    val sourceLabel: String,
    val sourceUrl: String,
    val articleUrl: String,
    val summary: String,
    val body: String = "",
    val publishedLabel: String,
    val publishedAtMillis: Long? = null,
    val contentState: NewsArticleContentState = NewsArticleContentState.Pending,
    val contentDetail: String = "",
)

data class NewsSourceAnalysis(
    val url: String,
    val hostLabel: String,
    val statusLabel: String,
    val statusDetail: String,
)

enum class NewsArticleContentState {
    Pending,
    Loading,
    Ready,
    AnalysisNeeded,
}

data class NewsRuntimeState(
    val areaId: String? = null,
    val areaTitle: String = "News",
    val statusLabel: String = "News werden vorbereitet",
    val statusDetail: String = "Ich richte den Testbereich ein.",
    val sourceAnalyses: List<NewsSourceAnalysis> = emptyList(),
    val articles: List<NewsArticle> = emptyList(),
    val lastUpdatedAtMillis: Long? = null,
)

interface NewsRuntimeRepository {
    val state: StateFlow<NewsRuntimeState>

    fun start(scope: CoroutineScope)

    suspend fun refreshNow()

    suspend fun ensureArticleContent(articleId: String)

    suspend fun markArticleRead(articleId: String)

    fun articleById(articleId: String): NewsArticle?
}

class LocalNewsRuntimeRepository(
    private val areaKernelRepository: AreaKernelRepository,
    private val areaWebFeedSourceRepository: AreaWebFeedSourceRepository,
    private val clock: Clock,
) : NewsRuntimeRepository {
    private val gson = Gson()
    private val logTag = "NewsRuntime"

    override val state: StateFlow<NewsRuntimeState>
        get() = mutableState.asStateFlow()

    private val mutableState = MutableStateFlow(NewsRuntimeState())
    private val refreshMutex = Mutex()
    private val dismissedArticleUrls = linkedSetOf<String>()
    private var pollingJob: Job? = null

    override fun start(scope: CoroutineScope) {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launchNewsPolling()
    }

    override suspend fun refreshNow() {
        refresh(force = true)
    }

    override suspend fun ensureArticleContent(articleId: String) {
        val article = refreshMutex.withLock<NewsArticle?> {
            val current = mutableState.value.articles.firstOrNull { it.id == articleId } ?: return@withLock null
            if (current.contentState == NewsArticleContentState.Ready || current.contentState == NewsArticleContentState.Loading) {
                return@withLock null
            }
            mutableState.value = mutableState.value.copy(
                articles = mutableState.value.articles.map { item ->
                    if (item.id == articleId) {
                        item.copy(
                            contentState = NewsArticleContentState.Loading,
                            contentDetail = "Volltext wird geladen.",
                        )
                    } else {
                        item
                    }
                },
            )
            current
        }
        if (article == null) return
        val loadedArticle = loadArticleContent(article)
        refreshMutex.withLock {
            if (mutableState.value.articles.none { it.id == articleId }) return@withLock
            mutableState.value = mutableState.value.copy(
                articles = mutableState.value.articles.map { item ->
                    if (item.id == articleId) loadedArticle else item
                },
            )
        }
    }

    override suspend fun markArticleRead(articleId: String) {
        refreshMutex.withLock {
            val current = mutableState.value.articles.firstOrNull { it.id == articleId } ?: return
            dismissedArticleUrls += current.articleUrl
            mutableState.value = mutableState.value.copy(
                articles = mutableState.value.articles.filterNot { it.id == articleId },
                statusLabel = "Artikel gelesen",
                statusDetail = "${current.title} verschwindet jetzt aus der Liste.",
            )
        }
    }

    override fun articleById(articleId: String): NewsArticle? {
        return mutableState.value.articles.firstOrNull { it.id == articleId }
    }

    private fun CoroutineScope.launchNewsPolling(): Job {
        return launch {
            try {
                ensureNewsAreaAndSources()
                refresh(force = true)
                while (isActive) {
                    delay(TimeUnit.HOURS.toMillis(1))
                    refresh(force = false)
                }
            } catch (error: Throwable) {
                Log.e(logTag, "News polling crashed", error)
                mutableState.value = mutableState.value.copy(
                    statusLabel = "News-Fehler",
                    statusDetail = error.message.orEmpty().ifBlank { "Der News-Refresh ist abgebrochen." },
                )
            }
        }
    }

    private suspend fun refresh(force: Boolean) {
        refreshMutex.withLock {
            val areaId = ensureNewsAreaAndSources()
            val allSources = areaWebFeedSourceRepository.loadByArea(areaId)
            val enabledSources = allSources.filter(AreaWebFeedSource::isAutoSyncEnabled)
            Log.d(logTag, "refresh(force=$force) area=$areaId all=${allSources.size} enabled=${enabledSources.size}")
            if (enabledSources.isEmpty()) {
                mutableState.value = mutableState.value.copy(
                    areaId = areaId,
                    statusLabel = "Keine aktive Quelle",
                    statusDetail = "Im Bereich News ist gerade keine Quelle aktiv.",
                    sourceAnalyses = allSources.map { source ->
                        NewsSourceAnalysis(
                            url = source.url,
                            hostLabel = shortHost(source.url),
                            statusLabel = "Deaktiviert",
                            statusDetail = "Diese Quelle ist im News-Test gerade ausgeschaltet.",
                        )
                    },
                    articles = emptyList(),
                )
                return
            }

            val dueSources = if (force) {
                enabledSources
            } else {
                enabledSources.filter { source -> source.isDue(clock.millis()) }
            }
            Log.d(logTag, "due sources=${dueSources.map { it.url }}")
            if (dueSources.isEmpty() && mutableState.value.articles.isNotEmpty()) {
                return
            }

            val analyses = mutableListOf<NewsSourceAnalysis>()
            val articles = linkedMapOf<String, NewsArticle>()
            dueSources.forEach { source ->
                val result = readSource(source)
                Log.d(
                    logTag,
                    "source ${source.url} -> ${result.analysis.statusLabel} (${result.articles.size} article(s))",
                )
                analyses += result.analysis
                result.articles.forEach { article ->
                    if (article.articleUrl in dismissedArticleUrls) return@forEach
                    articles.putIfAbsent(article.articleUrl, article)
                }
                areaWebFeedSourceRepository.updateSyncResult(
                    areaId = areaId,
                    url = source.url,
                    syncedAt = clock.millis(),
                    statusLabel = result.analysis.statusLabel,
                    statusDetail = result.analysis.statusDetail,
                )
            }

            val previousAnalyses = mutableState.value.sourceAnalyses.associateBy(NewsSourceAnalysis::url)
            val mergedAnalyses = enabledSources.map { source ->
                analyses.firstOrNull { it.url == source.url }
                    ?: previousAnalyses[source.url]
                    ?: NewsSourceAnalysis(
                        url = source.url,
                        hostLabel = shortHost(source.url),
                        statusLabel = "Wartet",
                        statusDetail = "Diese Quelle wird spaeter erneut geprueft.",
                    )
            }
            val dueSourceUrls = dueSources.mapTo(linkedSetOf(), AreaWebFeedSource::url)
            val previousArticles = mutableState.value.articles
                .filterNot { it.articleUrl in dismissedArticleUrls }
                .associateBy(NewsArticle::articleUrl)
            val mergedArticles = buildList {
                previousArticles.values
                    .filterNot { it.sourceUrl in dueSourceUrls }
                    .forEach(::add)
                articles.values.forEach { article ->
                    val previous = previousArticles[article.articleUrl]
                    add(article.mergeContent(previous))
                }
            }.sortedWith(
                compareByDescending<NewsArticle> { it.publishedAtMillis ?: Long.MIN_VALUE }
                    .thenBy { it.sourceLabel.lowercase() }
                    .thenBy { it.title.lowercase() },
            )

            mutableState.value = NewsRuntimeState(
                areaId = areaId,
                areaTitle = "News",
                statusLabel = when {
                    mergedArticles.isNotEmpty() -> "${mergedArticles.size} neue Artikel"
                    mergedAnalyses.any { it.statusLabel == "Kein Feed" } -> "Analyse noetig"
                    else -> "Keine lesbaren Artikel"
                },
                statusDetail = when {
                    mergedArticles.isNotEmpty() -> "Nur ungelesene Artikel bleiben als Kacheln sichtbar."
                    mergedAnalyses.any { it.statusLabel == "Kein Feed" } -> "Mindestens eine Quelle liefert noch keinen RSS/Atom-Feed."
                    else -> "Die aktiven Quellen wurden gelesen, aber kein belastbarer Volltext blieb uebrig."
                },
                sourceAnalyses = mergedAnalyses,
                articles = mergedArticles,
                lastUpdatedAtMillis = clock.millis(),
            )
            Log.d(
                logTag,
                "refresh completed analyses=${mergedAnalyses.size} articles=${mergedArticles.size}",
            )
        }
    }

    private suspend fun ensureNewsAreaAndSources(): String {
        val existing = areaKernelRepository.loadActiveInstances()
            .firstOrNull { it.title.equals("News", ignoreCase = true) }
        val current = existing ?: areaKernelRepository.createActiveInstance(
                CreateAreaInstanceDraft(
                    title = "News",
                    summary = "Stuendlich laufender Testbereich fuer RSS/Atom-News mit Leseansicht in Single.",
                    templateId = "medium",
                    iconKey = "book",
                    behaviorClass = AreaBehaviorClass.REFLECTION,
                ),
            )
        val areaId = current.areaId
        val sources = areaWebFeedSourceRepository.loadByArea(areaId)
        val createdNow = existing == null
        if (createdNow && sources.isEmpty()) {
            DefaultNewsSources.forEach { url ->
                areaWebFeedSourceRepository.save(
                    areaId = areaId,
                    url = url,
                    sourceKind = inferWebFeedSourceKind(url),
                    isAutoSyncEnabled = true,
                    syncCadence = AreaWebFeedSyncCadence.Hourly,
                )
            }
            Log.d(logTag, "seeded default news sources for fresh News area")
        }
        return areaId
    }

    private suspend fun readSource(source: AreaWebFeedSource): SourceReadResult {
        if (source.url.isStolSourceUrl()) {
            return readStolSource(source)
        }
        val resolvedUrl = runtimeFeedUrl(source.url)
        val document = runCatching { fetchNewsDocument(resolvedUrl) }.getOrElse { error ->
            return SourceReadResult(
                analysis = NewsSourceAnalysis(
                    url = source.url,
                    hostLabel = shortHost(source.url),
                    statusLabel = "Fehler",
                    statusDetail = error.message.orEmpty().ifBlank { "Die Quelle konnte nicht gelesen werden." },
                ),
                articles = emptyList(),
            )
        }
        return when (document.kind) {
            NewsDocumentKind.Feed -> readFeedSource(
                source = source,
                feedUrl = document.url,
                xml = document.body,
            )
            NewsDocumentKind.Html -> readHtmlSource(
                source = source,
                pageUrl = document.url,
                html = document.body,
            )
            NewsDocumentKind.Other -> SourceReadResult(
                analysis = NewsSourceAnalysis(
                    url = source.url,
                    hostLabel = shortHost(source.url),
                    statusLabel = "Analyse noetig",
                    statusDetail = "Die Quelle liefert weder HTML noch einen erkennbaren Feed.",
                ),
                articles = emptyList(),
            )
        }
    }

    private suspend fun readStolSource(
        source: AreaWebFeedSource,
    ): SourceReadResult {
        val payload = runCatching { fetchNewsText(StolTickerApiUrl) }.getOrElse { error ->
            return SourceReadResult(
                analysis = NewsSourceAnalysis(
                    url = source.url,
                    hostLabel = "Stol",
                    statusLabel = "Fehler",
                    statusDetail = error.message.orEmpty().ifBlank { "Der STOL-Newsticker konnte nicht gelesen werden." },
                ),
                articles = emptyList(),
            )
        }
        val parsedArticles = runCatching { parseStolTickerPayload(gson, payload.body) }.getOrElse { error ->
            return SourceReadResult(
                analysis = NewsSourceAnalysis(
                    url = source.url,
                    hostLabel = "Stol",
                    statusLabel = "Analyse noetig",
                    statusDetail = error.message.orEmpty().ifBlank { "Der STOL-Newsticker liess sich nicht lesen." },
                ),
                articles = emptyList(),
            )
        }
        val readableArticles = parsedArticles.map { item ->
            buildArticleStub(
                sourceUrl = source.url,
                sourceLabel = "Stol",
                articleUrl = item.articleUrl,
                title = item.title,
                publishedLabel = item.publishedLabel,
                summary = item.summary,
                publishedAtMillis = item.publishedAtMillis,
            )
        }
        return SourceReadResult(
            analysis = NewsSourceAnalysis(
                url = source.url,
                hostLabel = "Stol",
                statusLabel = when {
                    readableArticles.isNotEmpty() -> "${readableArticles.size} Artikel"
                    else -> "Kein Artikel"
                },
                statusDetail = when {
                    readableArticles.isNotEmpty() -> "Der STOL-Newsticker liefert Artikel fuer Single. Volltext wird erst beim Oeffnen geladen."
                    else -> "Der STOL-Newsticker lieferte im Testfenster keine Artikel."
                },
            ),
            articles = readableArticles,
        )
    }

    private suspend fun readHtmlSource(
        source: AreaWebFeedSource,
        pageUrl: String,
        html: String,
    ): SourceReadResult {
        val feedUrls = discoverFeedUrls(pageUrl, html)
        if (feedUrls.isEmpty()) {
            return SourceReadResult(
                analysis = NewsSourceAnalysis(
                    url = source.url,
                    hostLabel = shortHost(source.url),
                    statusLabel = "Kein Feed",
                    statusDetail = "Kein RSS/Atom-Feed gefunden. Diese Quelle bleibt zur Analyse markiert.",
                ),
                articles = emptyList(),
            )
        }

        val readableArticles = mutableListOf<NewsArticle>()
        feedUrls.take(2).forEach { feedUrl ->
            val feedDocument = runCatching { fetchNewsDocument(feedUrl) }.getOrNull() ?: return@forEach
            if (feedDocument.kind != NewsDocumentKind.Feed) return@forEach
            val parsedFeed = runCatching { parseFeedDocument(feedUrl, feedDocument.body) }.getOrNull() ?: return@forEach
            parsedFeed.items.forEach { item ->
                readableArticles += buildArticleStub(
                    sourceUrl = source.url,
                    sourceLabel = shortHost(source.url),
                    articleUrl = item.link,
                    title = item.title,
                    publishedLabel = item.detail,
                )
            }
        }

        return SourceReadResult(
            analysis = NewsSourceAnalysis(
                url = source.url,
                hostLabel = shortHost(source.url),
                statusLabel = when {
                    readableArticles.isNotEmpty() -> "${readableArticles.size} Artikel"
                    else -> "Kein Artikel"
                },
                statusDetail = when {
                    readableArticles.isNotEmpty() -> "Feed gefunden. Volltext wird erst beim Oeffnen des Artikels geladen."
                    else -> "Feed gefunden, aber noch keine lesbaren Artikel im Testfenster."
                },
            ),
            articles = readableArticles,
        )
    }

    private suspend fun readFeedSource(
        source: AreaWebFeedSource,
        feedUrl: String,
        xml: String,
    ): SourceReadResult {
        val parsedFeed = runCatching { parseFeedDocument(feedUrl, xml) }.getOrElse { error ->
            return SourceReadResult(
                analysis = NewsSourceAnalysis(
                    url = source.url,
                    hostLabel = shortHost(source.url),
                    statusLabel = "Analyse noetig",
                    statusDetail = error.message.orEmpty().ifBlank { "Der Feed liess sich nicht parsen." },
                ),
                articles = emptyList(),
            )
        }
        val readableArticles = parsedFeed.items.map { item ->
            buildArticleStub(
                sourceUrl = source.url,
                sourceLabel = shortHost(source.url),
                articleUrl = item.link,
                title = item.title,
                publishedLabel = item.detail,
            )
        }
        return SourceReadResult(
            analysis = NewsSourceAnalysis(
                url = source.url,
                hostLabel = shortHost(source.url),
                statusLabel = when {
                    readableArticles.isNotEmpty() -> "${readableArticles.size} Artikel"
                    else -> "Feed leer"
                },
                statusDetail = when {
                    readableArticles.isNotEmpty() -> "Der Feed liefert Artikel fuer Single. Volltext wird erst beim Oeffnen geladen."
                    else -> "Der Feed enthielt im Testfenster keinen passenden Artikel."
                },
            ),
            articles = readableArticles,
        )
    }

    private fun buildArticleStub(
        sourceUrl: String,
        sourceLabel: String,
        articleUrl: String,
        title: String,
        publishedLabel: String,
        summary: String = publishedLabel.ifBlank { "Artikel aus $sourceLabel" },
        publishedAtMillis: Long? = parsePublishedAtMillis(publishedLabel),
    ): NewsArticle {
        return NewsArticle(
            id = articleUrl,
            title = title.ifBlank { shortHost(articleUrl) },
            sourceLabel = sourceLabel,
            sourceUrl = sourceUrl,
            articleUrl = articleUrl,
            summary = summary,
            publishedLabel = publishedLabel,
            publishedAtMillis = publishedAtMillis,
        )
    }

    private suspend fun loadArticleContent(article: NewsArticle): NewsArticle {
        val document = runCatching { fetchNewsDocument(article.articleUrl) }.getOrNull()
            ?: return article.copy(
                contentState = NewsArticleContentState.AnalysisNeeded,
                contentDetail = "Der Artikel konnte nicht geladen werden.",
            )
        if (document.kind != NewsDocumentKind.Html) {
            return article.copy(
                contentState = NewsArticleContentState.AnalysisNeeded,
                contentDetail = "Der Artikel liefert keinen lesbaren HTML-Text.",
            )
        }
        val parsed = parseReadableWebPage(article.articleUrl, document.body)
        val body = parsed.textPreview.trim()
        if (body.length < MinimumReadableBodyLength) {
            return article.copy(
                body = body,
                contentState = NewsArticleContentState.AnalysisNeeded,
                contentDetail = "Der Volltext ist auf dieser Seite noch zu duenn oder zu unruhig fuer den Reader.",
            )
        }
        return article.copy(
            summary = article.summary.ifBlank { "Artikel aus ${article.sourceLabel}" },
            body = body,
            contentState = NewsArticleContentState.Ready,
            contentDetail = "Volltext geladen.",
        )
    }
}

private data class SourceReadResult(
    val analysis: NewsSourceAnalysis,
    val articles: List<NewsArticle>,
)

internal data class StolTickerArticle(
    val articleUrl: String,
    val title: String,
    val summary: String,
    val publishedLabel: String,
    val publishedAtMillis: Long?,
)

private data class NewsDocument(
    val url: String,
    val kind: NewsDocumentKind,
    val body: String,
)

private enum class NewsDocumentKind {
    Feed,
    Html,
    Other,
}

private suspend fun fetchNewsDocument(
    inputUrl: String,
): NewsDocument = withContext(Dispatchers.IO) {
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
        val resolvedUrl = connection.url.toString()
        val kind = when {
            contentType.contains("xml") || body.trimStart().startsWith("<rss", ignoreCase = true) || body.trimStart().startsWith("<feed", ignoreCase = true) -> NewsDocumentKind.Feed
            contentType.contains("html") || body.contains("<html", ignoreCase = true) -> NewsDocumentKind.Html
            else -> NewsDocumentKind.Other
        }
        NewsDocument(
            url = resolvedUrl,
            kind = kind,
            body = body,
        )
    }
}

private suspend fun fetchNewsText(
    inputUrl: String,
): NewsTextDocument = withContext(Dispatchers.IO) {
    val connection = (URL(inputUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 10_000
        setRequestProperty("User-Agent", "DaysApp/0.1 (+android)")
        instanceFollowRedirects = true
    }
    connection.inputStream.use { stream ->
        NewsTextDocument(
            url = connection.url.toString(),
            body = stream.readBytes().toString(Charsets.UTF_8).take(300_000),
        )
    }
}

internal fun parseStolTickerPayload(
    gson: Gson,
    json: String,
): List<StolTickerArticle> {
    val response = gson.fromJson(json, StolTickerResponse::class.java)
    return response.data?.articles.orEmpty()
        .mapNotNull { article ->
            val path = article.url?.trim().orEmpty()
            if (path.isBlank()) return@mapNotNull null
            val articleUrl = resolveStolArticleUrl(path)
            val title = article.title?.trim().orEmpty().ifBlank { shortHost(articleUrl) }
            StolTickerArticle(
                articleUrl = articleUrl,
                title = title,
                summary = buildStolSummary(article),
                publishedLabel = article.date?.trim().orEmpty(),
                publishedAtMillis = article.ts?.let { it * 1_000L } ?: parsePublishedAtMillis(article.date.orEmpty()),
            )
        }
}

private fun AreaWebFeedSource.isDue(nowMillis: Long): Boolean {
    val lastSync = lastSyncedAt ?: return true
    return nowMillis - lastSync >= TimeUnit.HOURS.toMillis(syncCadence.intervalHours)
}

private fun String.isStolSourceUrl(): Boolean {
    return contains("stol.it", ignoreCase = true)
}

private val DefaultNewsSources = listOf(
    "https://www.stol.it/",
    "https://www.heise.de/rss/heise-atom.xml",
    "https://www.tagesschau.de/infoservices/alle-meldungen-100~rss2.xml",
)

private const val MinimumReadableBodyLength = 140
private const val StolTickerApiUrl = "https://www.stol.it/api/rest/v1/newsticker"

private fun NewsArticle.mergeContent(previous: NewsArticle?): NewsArticle {
    if (previous == null) return this
    return copy(
        body = previous.body,
        contentState = previous.contentState,
        contentDetail = previous.contentDetail,
    )
}

private fun parsePublishedAtMillis(value: String): Long? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    return runCatching { OffsetDateTime.parse(trimmed).toInstant().toEpochMilli() }.getOrNull()
        ?: runCatching { ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }.getOrNull()
}

private fun runtimeFeedUrl(sourceUrl: String): String {
    return when (sourceUrl) {
        "https://www.heise.de/" -> "https://www.heise.de/rss/heise-atom.xml"
        "https://www.tagesschau.de/" -> "https://www.tagesschau.de/infoservices/alle-meldungen-100~rss2.xml"
        else -> sourceUrl
    }
}

private fun resolveStolArticleUrl(path: String): String {
    return when {
        path.startsWith("http") -> path
        path.startsWith("/") -> "https://www.stol.it$path"
        else -> "https://www.stol.it/$path"
    }
}

private fun buildStolSummary(
    article: StolTickerPayloadArticle,
): String {
    val prefixes = buildList {
        article.breadcrumb?.department?.name?.trim()?.takeIf(String::isNotBlank)?.let(::add)
        article.breadcrumb?.headline?.name?.trim()?.takeIf(String::isNotBlank)?.let(::add)
        if (article.plus == true) add("Plus")
    }
    val teaser = article.description
        ?.replace(Regex("""\s+"""), " ")
        ?.trim()
        .orEmpty()
    return buildString {
        if (prefixes.isNotEmpty()) append(prefixes.joinToString(" / "))
        if (teaser.isNotBlank()) {
            if (isNotEmpty()) append("\n")
            append(teaser)
        }
    }.ifBlank { "Artikel aus Stol" }
}

private data class NewsTextDocument(
    val url: String,
    val body: String,
)

private data class StolTickerResponse(
    val data: StolTickerData? = null,
)

private data class StolTickerData(
    val articles: List<StolTickerPayloadArticle>? = null,
)

private data class StolTickerPayloadArticle(
    val url: String? = null,
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val ts: Long? = null,
    val plus: Boolean? = null,
    val breadcrumb: StolTickerBreadcrumb? = null,
)

private data class StolTickerBreadcrumb(
    val department: StolTickerTag? = null,
    val headline: StolTickerTag? = null,
)

private data class StolTickerTag(
    val name: String? = null,
)
