package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.data.local.AreaWebFeedSourceDao
import com.struperto.androidappdays.data.local.AreaWebFeedSourceEntity
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AreaWebFeedSourceKind(
    val storageKey: String,
    val label: String,
) {
    Website(
        storageKey = "website",
        label = "Website",
    ),
    Feed(
        storageKey = "feed",
        label = "Feed",
    );

    companion object {
        fun fromStorage(value: String): AreaWebFeedSourceKind {
            return entries.firstOrNull { it.storageKey == value } ?: Website
        }
    }
}

enum class AreaWebFeedSyncCadence(
    val storageKey: String,
    val label: String,
    val intervalHours: Long,
) {
    Hourly(
        storageKey = "1h",
        label = "Stuendlich",
        intervalHours = 1,
    ),
    SixHours(
        storageKey = "6h",
        label = "Alle 6h",
        intervalHours = 6,
    ),
    TwelveHours(
        storageKey = "12h",
        label = "Alle 12h",
        intervalHours = 12,
    ),
    Daily(
        storageKey = "24h",
        label = "Taeglich",
        intervalHours = 24,
    );

    companion object {
        fun fromStorage(value: String): AreaWebFeedSyncCadence {
            return entries.firstOrNull { it.storageKey == value } ?: SixHours
        }
    }
}

data class AreaWebFeedSource(
    val areaId: String,
    val url: String,
    val sourceKind: AreaWebFeedSourceKind,
    val isAutoSyncEnabled: Boolean,
    val syncCadence: AreaWebFeedSyncCadence,
    val lastSyncedAt: Long?,
    val lastStatusLabel: String,
    val lastStatusDetail: String,
)

interface AreaWebFeedSourceRepository {
    fun observeAll(): Flow<List<AreaWebFeedSource>>

    fun observeByArea(areaId: String): Flow<List<AreaWebFeedSource>>

    suspend fun loadAll(): List<AreaWebFeedSource>

    suspend fun loadByArea(areaId: String): List<AreaWebFeedSource>

    suspend fun save(
        areaId: String,
        url: String,
        sourceKind: AreaWebFeedSourceKind = AreaWebFeedSourceKind.Website,
        isAutoSyncEnabled: Boolean = true,
        syncCadence: AreaWebFeedSyncCadence = AreaWebFeedSyncCadence.SixHours,
    )

    suspend fun remove(
        areaId: String,
        url: String,
    )

    suspend fun clearArea(areaId: String)

    suspend fun updateSyncResult(
        areaId: String,
        url: String,
        syncedAt: Long,
        statusLabel: String,
        statusDetail: String,
    )

    suspend fun setAutoSyncEnabled(
        areaId: String,
        url: String,
        enabled: Boolean,
    )

    suspend fun setSyncCadence(
        areaId: String,
        url: String,
        cadence: AreaWebFeedSyncCadence,
    )
}

class RoomAreaWebFeedSourceRepository(
    private val dao: AreaWebFeedSourceDao,
    private val clock: Clock,
) : AreaWebFeedSourceRepository {
    override fun observeAll(): Flow<List<AreaWebFeedSource>> {
        return dao.observeAll().map { entities -> entities.map(AreaWebFeedSourceEntity::toDomain) }
    }

    override fun observeByArea(areaId: String): Flow<List<AreaWebFeedSource>> {
        return dao.observeByArea(areaId).map { entities -> entities.map(AreaWebFeedSourceEntity::toDomain) }
    }

    override suspend fun loadAll(): List<AreaWebFeedSource> {
        return dao.getAll().map(AreaWebFeedSourceEntity::toDomain)
    }

    override suspend fun loadByArea(areaId: String): List<AreaWebFeedSource> {
        return dao.getByArea(areaId).map(AreaWebFeedSourceEntity::toDomain)
    }

    override suspend fun save(
        areaId: String,
        url: String,
        sourceKind: AreaWebFeedSourceKind,
        isAutoSyncEnabled: Boolean,
        syncCadence: AreaWebFeedSyncCadence,
    ) {
        val trimmed = url.trim()
        if (!trimmed.startsWith("http")) return
        val now = clock.millis()
        val current = dao.getByArea(areaId).firstOrNull { it.url == trimmed }
        dao.upsert(
            AreaWebFeedSourceEntity(
                areaId = areaId,
                url = trimmed,
                sourceKind = current?.sourceKind ?: sourceKind.storageKey,
                isAutoSyncEnabled = isAutoSyncEnabled,
                syncCadence = current?.syncCadence ?: syncCadence.storageKey,
                createdAt = current?.createdAt ?: now,
                updatedAt = now,
                lastSyncedAt = current?.lastSyncedAt,
                lastStatusLabel = current?.lastStatusLabel.orEmpty(),
                lastStatusDetail = current?.lastStatusDetail.orEmpty(),
            ),
        )
    }

    override suspend fun remove(
        areaId: String,
        url: String,
    ) {
        dao.delete(areaId = areaId, url = url)
    }

    override suspend fun clearArea(areaId: String) {
        dao.deleteAllForArea(areaId)
    }

    override suspend fun updateSyncResult(
        areaId: String,
        url: String,
        syncedAt: Long,
        statusLabel: String,
        statusDetail: String,
    ) {
        val current = dao.getByArea(areaId).firstOrNull { it.url == url } ?: return
        dao.upsert(
            current.copy(
                updatedAt = syncedAt,
                lastSyncedAt = syncedAt,
                lastStatusLabel = statusLabel,
                lastStatusDetail = statusDetail,
            ),
        )
    }

    override suspend fun setAutoSyncEnabled(
        areaId: String,
        url: String,
        enabled: Boolean,
    ) {
        val current = dao.getByArea(areaId).firstOrNull { it.url == url } ?: return
        dao.upsert(
            current.copy(
                isAutoSyncEnabled = enabled,
                updatedAt = clock.millis(),
            ),
        )
    }

    override suspend fun setSyncCadence(
        areaId: String,
        url: String,
        cadence: AreaWebFeedSyncCadence,
    ) {
        val current = dao.getByArea(areaId).firstOrNull { it.url == url } ?: return
        dao.upsert(
            current.copy(
                syncCadence = cadence.storageKey,
                updatedAt = clock.millis(),
            ),
        )
    }
}

private fun AreaWebFeedSourceEntity.toDomain(): AreaWebFeedSource {
    return AreaWebFeedSource(
        areaId = areaId,
        url = url,
        sourceKind = AreaWebFeedSourceKind.fromStorage(sourceKind),
        isAutoSyncEnabled = isAutoSyncEnabled,
        syncCadence = AreaWebFeedSyncCadence.fromStorage(syncCadence),
        lastSyncedAt = lastSyncedAt,
        lastStatusLabel = lastStatusLabel,
        lastStatusDetail = lastStatusDetail,
    )
}
