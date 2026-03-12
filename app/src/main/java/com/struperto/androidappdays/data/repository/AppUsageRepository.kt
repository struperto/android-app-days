package com.struperto.androidappdays.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class AppUsageSummary(
    val packageName: String,
    val appLabel: String,
    val foregroundMinutes: Int,
    val lastUsed: Instant?,
)

interface AppUsageRepository {
    suspend fun loadDailyUsage(date: LocalDate, zoneId: ZoneId): List<AppUsageSummary>
}

class DeviceAppUsageRepository(
    private val context: Context,
) : AppUsageRepository {
    override suspend fun loadDailyUsage(date: LocalDate, zoneId: ZoneId): List<AppUsageSummary> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            endOfDay,
        ) ?: return emptyList()
        val pm = context.packageManager
        return stats
            .filter { it.totalTimeInForeground > 60_000 }
            .sortedByDescending { it.totalTimeInForeground }
            .map { stat ->
                val appLabel = try {
                    pm.getApplicationLabel(
                        pm.getApplicationInfo(stat.packageName, 0),
                    ).toString()
                } catch (_: PackageManager.NameNotFoundException) {
                    stat.packageName
                }
                AppUsageSummary(
                    packageName = stat.packageName,
                    appLabel = appLabel,
                    foregroundMinutes = (stat.totalTimeInForeground / 60_000).toInt(),
                    lastUsed = Instant.ofEpochMilli(stat.lastTimeUsed).takeIf { stat.lastTimeUsed > 0 },
                )
            }
    }
}
