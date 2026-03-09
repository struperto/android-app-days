package com.struperto.androidappdays.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.struperto.androidappdays.data.local.SourcePreferenceDao
import com.struperto.androidappdays.data.local.SourcePreferenceEntity
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceCapability
import com.struperto.androidappdays.domain.DataSourceKind
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SourceCapabilityRepository {
    fun observeProfile(): Flow<CapabilityProfile>
    suspend fun loadProfile(): CapabilityProfile
    suspend fun ensureSeeded()
    suspend fun setEnabled(
        source: DataSourceKind,
        enabled: Boolean,
    )
}

class LocalSourceCapabilityRepository(
    private val context: Context,
    private val preferenceDao: SourcePreferenceDao,
    private val healthConnectRepository: HealthConnectRepository,
    private val clock: Clock,
) : SourceCapabilityRepository {
    override fun observeProfile(): Flow<CapabilityProfile> {
        return preferenceDao.observeAll().map { entities ->
            buildProfile(entities)
        }
    }

    override suspend fun loadProfile(): CapabilityProfile {
        return buildProfile(preferenceDao.getAll())
    }

    override suspend fun ensureSeeded() {
        if (preferenceDao.getAll().isNotEmpty()) return
        val now = clock.millis()
        preferenceDao.insertAll(
            defaultSourcePreferences().mapIndexed { index, source ->
                SourcePreferenceEntity(
                    source = source.name,
                    label = sourceLabel(source),
                    isEnabled = true,
                    updatedAt = now + index,
                )
            },
        )
    }

    override suspend fun setEnabled(
        source: DataSourceKind,
        enabled: Boolean,
    ) {
        preferenceDao.insert(
            SourcePreferenceEntity(
                source = source.name,
                label = sourceLabel(source),
                isEnabled = enabled,
                updatedAt = clock.millis(),
            ),
        )
    }

    private suspend fun buildProfile(entities: List<SourcePreferenceEntity>): CapabilityProfile {
        val prefMap = entities.associateBy { DataSourceKind.valueOf(it.source) }
        val healthAvailability = healthConnectRepository.availability()
        val healthPermissions = healthConnectRepository.grantedPermissions()
        return CapabilityProfile(
            sources = DataSourceKind.entries.map { source ->
                val isEnabled = prefMap[source]?.isEnabled ?: true
                when (source) {
                    DataSourceKind.HEALTH_CONNECT -> {
                        DataSourceCapability(
                            source = source,
                            label = sourceLabel(source),
                            enabled = isEnabled,
                            available = healthAvailability == HealthConnectAvailability.AVAILABLE,
                            granted = healthPermissions.containsAll(healthConnectRepository.requiredPermissions),
                            detail = when (healthAvailability) {
                                HealthConnectAvailability.AVAILABLE -> {
                                    if (healthPermissions.containsAll(healthConnectRepository.requiredPermissions)) {
                                        "Schlaf und Bewegung koennen importiert werden."
                                    } else {
                                        "Noch ohne Freigabe fuer Schlaf und Bewegung."
                                    }
                                }
                                HealthConnectAvailability.UPDATE_REQUIRED -> "Health Connect braucht ein Update."
                                HealthConnectAvailability.UNAVAILABLE -> "Health Connect ist nicht verfuegbar."
                            },
                        )
                    }
                    DataSourceKind.CALENDAR -> {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CALENDAR,
                        ) == PackageManager.PERMISSION_GRANTED
                        DataSourceCapability(
                            source = source,
                            label = sourceLabel(source),
                            enabled = isEnabled,
                            available = true,
                            granted = granted,
                            detail = if (granted) {
                                "Kalenderdruck kann als Kontextsignal genutzt werden."
                            } else {
                                "Kalender ist optional und aktuell nicht freigegeben."
                            },
                        )
                    }
                    DataSourceKind.NOTIFICATIONS -> {
                        val granted = NotificationManagerCompat.getEnabledListenerPackages(context)
                            .contains(context.packageName)
                        DataSourceCapability(
                            source = source,
                            label = sourceLabel(source),
                            enabled = isEnabled,
                            available = true,
                            granted = granted,
                            detail = if (granted) {
                                "Stoerdruck kann aus dem Notification Listener kommen."
                            } else {
                                "Notification Listener ist noch nicht aktiv."
                            },
                        )
                    }
                    DataSourceKind.MANUAL -> {
                        DataSourceCapability(
                            source = source,
                            label = sourceLabel(source),
                            enabled = isEnabled,
                            available = true,
                            granted = true,
                            detail = "Manuelle Eintraege ergaenzen sparse data.",
                        )
                    }
                }
            },
        )
    }
}

private fun sourceLabel(source: DataSourceKind): String {
    return when (source) {
        DataSourceKind.HEALTH_CONNECT -> "Health Connect"
        DataSourceKind.CALENDAR -> "Kalender"
        DataSourceKind.NOTIFICATIONS -> "Benachrichtigungen"
        DataSourceKind.MANUAL -> "Manuell"
    }
}
