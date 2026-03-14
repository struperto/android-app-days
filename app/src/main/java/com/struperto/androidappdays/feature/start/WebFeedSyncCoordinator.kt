package com.struperto.androidappdays.feature.start

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.data.repository.AreaWebFeedSource
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceKind
import com.struperto.androidappdays.data.repository.AreaWebFeedSourceRepository
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import com.struperto.androidappdays.data.repository.CaptureRepository
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

data class AreaWebFeedSyncRunResult(
    val newItemCount: Int,
    val message: String,
)

interface WebFeedSyncScheduler {
    suspend fun refresh(sources: List<AreaWebFeedSource>)
}

interface WebFeedSyncCoordinator {
    suspend fun syncArea(areaId: String): AreaWebFeedSyncRunResult

    suspend fun syncAll(): Int

    suspend fun ensureScheduled()
}

class LocalWebFeedSyncCoordinator(
    private val appContext: Context?,
    private val captureRepository: CaptureRepository,
    private val webFeedSourceRepository: AreaWebFeedSourceRepository,
    private val webFeedConnector: WebFeedConnector,
    private val clock: Clock,
    private val scheduler: WebFeedSyncScheduler = appContext?.let(::AndroidWebFeedSyncScheduler) ?: object : WebFeedSyncScheduler {
        override suspend fun refresh(sources: List<AreaWebFeedSource>) = Unit
    },
) : WebFeedSyncCoordinator {
    override suspend fun syncArea(areaId: String): AreaWebFeedSyncRunResult {
        return syncAreaInternal(
            areaId = areaId,
            scheduledSourceUrls = null,
        )
    }

    private suspend fun syncAreaInternal(
        areaId: String,
        scheduledSourceUrls: Set<String>?,
    ): AreaWebFeedSyncRunResult {
        val assignedImports = captureRepository.observeOpen().first().filter { it.areaId == areaId }
        val knownReferences = assignedImports.mapNotNull(::parseAreaImportCapture).map(AreaImportedMaterialState::reference).toSet()
        val storedSources = webFeedSourceRepository.loadByArea(areaId)
        val selectedStoredSources = if (scheduledSourceUrls == null) {
            storedSources
        } else {
            storedSources.filter { it.url in scheduledSourceUrls }
        }
        val importedLinks = if (scheduledSourceUrls == null) {
            assignedImports
                .mapNotNull(::parseAreaImportCapture)
                .filter { it.kind == AreaImportKind.Link && it.reference.startsWith("http") }
                .map(AreaImportedMaterialState::reference)
        } else {
            emptyList()
        }
        val urls = (selectedStoredSources.map { it.url } + importedLinks).distinct()
        if (urls.isEmpty()) {
            return AreaWebFeedSyncRunResult(
                newItemCount = 0,
                message = "Keine faellige Feed-Quelle vorhanden.",
            )
        }
        val result = webFeedConnector.sync(
            urls = urls,
            knownReferences = knownReferences,
        )
        result.drafts.forEach { draft ->
            captureRepository.createTextCapture(
                text = buildAreaImportCaptureText(
                    kind = draft.kind,
                    title = draft.title,
                    detail = draft.detail,
                    reference = draft.reference,
                ),
                areaId = areaId,
            )
        }
        val syncedAt = clock.millis()
        val trackedUrls = (selectedStoredSources.map { it.url } + result.foundFeedUrls).distinct()
        trackedUrls.forEach { url ->
            val kind = if (url in result.foundFeedUrls) {
                AreaWebFeedSourceKind.Feed
            } else {
                storedSources.firstOrNull { it.url == url }?.sourceKind ?: inferWebFeedSourceKind(url)
            }
            webFeedSourceRepository.save(
                areaId = areaId,
                url = url,
                sourceKind = kind,
                isAutoSyncEnabled = storedSources.firstOrNull { it.url == url }?.isAutoSyncEnabled ?: true,
                syncCadence = storedSources.firstOrNull { it.url == url }?.syncCadence ?: defaultWebFeedSyncCadence(kind),
            )
            webFeedSourceRepository.updateSyncResult(
                areaId = areaId,
                url = url,
                syncedAt = syncedAt,
                statusLabel = if (result.drafts.isEmpty()) "Keine Neuigkeiten" else "${result.drafts.count { it.kind == AreaImportKind.Link }} neu",
                statusDetail = result.message,
            )
        }
        ensureScheduled()
        return AreaWebFeedSyncRunResult(
            newItemCount = result.drafts.count { it.kind == AreaImportKind.Link },
            message = result.message,
        )
    }

    override suspend fun syncAll(): Int {
        val nowMillis = clock.millis()
        val dueSourcesByArea = webFeedSourceRepository.loadAll()
            .filter { it.isAutoSyncEnabled }
            .filter { source -> source.isDue(nowMillis) }
            .groupBy(AreaWebFeedSource::areaId)
        dueSourcesByArea.forEach { (areaId, sources) ->
            syncAreaInternal(
                areaId = areaId,
                scheduledSourceUrls = sources.mapTo(linkedSetOf(), AreaWebFeedSource::url),
            )
        }
        return dueSourcesByArea.size
    }

    override suspend fun ensureScheduled() {
        scheduler.refresh(webFeedSourceRepository.loadAll())
    }
}

class AndroidWebFeedSyncScheduler(
    private val appContext: Context,
) : WebFeedSyncScheduler {
    override suspend fun refresh(sources: List<AreaWebFeedSource>) {
        val workManager = WorkManager.getInstance(appContext)
        // News-Feeds laufen fuer diesen Test nur waehrend der App-Prozess lebt.
        workManager.cancelUniqueWork(WebFeedSyncWorker.WorkName)
    }
}

class WebFeedSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val coordinator = (applicationContext as DaysApp).appContainer.webFeedSyncCoordinator
        return runCatching {
            coordinator.syncAll()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        const val WorkName = "web-feed-sync"
    }
}

internal fun inferWebFeedSourceKind(url: String): AreaWebFeedSourceKind {
    val normalized = url.lowercase()
    return if (
        normalized.endsWith(".xml") ||
        "/feed" in normalized ||
        "rss" in normalized ||
        "atom" in normalized
    ) {
        AreaWebFeedSourceKind.Feed
    } else {
        AreaWebFeedSourceKind.Website
    }
}

internal fun defaultWebFeedSyncCadence(kind: AreaWebFeedSourceKind) = when (kind) {
    AreaWebFeedSourceKind.Website -> AreaWebFeedSyncCadence.Daily
    AreaWebFeedSourceKind.Feed -> AreaWebFeedSyncCadence.SixHours
}

private fun AreaWebFeedSource.isDue(nowMillis: Long): Boolean {
    val lastSync = lastSyncedAt ?: return true
    val elapsedMillis = nowMillis - lastSync
    return elapsedMillis >= TimeUnit.HOURS.toMillis(syncCadence.intervalHours)
}
