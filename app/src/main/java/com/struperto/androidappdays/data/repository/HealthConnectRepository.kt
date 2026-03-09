package com.struperto.androidappdays.data.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val HealthConnectProviderPackage = "com.google.android.apps.healthdata"

enum class HealthConnectAvailability {
    AVAILABLE,
    UPDATE_REQUIRED,
    UNAVAILABLE,
}

interface HealthConnectRepository {
    val requiredPermissions: Set<String>

    suspend fun availability(): HealthConnectAvailability
    suspend fun grantedPermissions(): Set<String>
    suspend fun readDailyObservations(logicalDate: LocalDate): List<DomainObservation>
}

class AndroidHealthConnectRepository(
    private val context: Context,
) : HealthConnectRepository {
    override val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    private suspend fun clientOrNull(): HealthConnectClient? {
        return if (availability() == HealthConnectAvailability.AVAILABLE) {
            HealthConnectClient.getOrCreate(
                context = context,
                providerPackageName = HealthConnectProviderPackage,
            )
        } else {
            null
        }
    }

    override suspend fun availability(): HealthConnectAvailability {
        return when (
            HealthConnectClient.getSdkStatus(
                context = context,
                providerPackageName = HealthConnectProviderPackage,
            )
        ) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.UNAVAILABLE
        }
    }

    override suspend fun grantedPermissions(): Set<String> {
        val client = clientOrNull() ?: return emptySet()
        return runCatching {
            client.permissionController.getGrantedPermissions()
        }.getOrElse { emptySet() }
    }

    override suspend fun readDailyObservations(logicalDate: LocalDate): List<DomainObservation> {
        val client = clientOrNull() ?: return emptyList()
        val granted = grantedPermissions()
        if (!granted.containsAll(requiredPermissions)) {
            return emptyList()
        }

        val zoneId = ZoneId.systemDefault()
        val logicalStart = logicalDate.atTime(6, 0).atZone(zoneId).toInstant()
        val logicalEnd = logicalDate.plusDays(1).atTime(6, 0).atZone(zoneId).toInstant()
        val sleepWindowStart = logicalDate.minusDays(1).atTime(18, 0).atZone(zoneId).toInstant()
        val sleepWindowEnd = logicalDate.atTime(12, 0).atZone(zoneId).toInstant()

        val sleepRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(sleepWindowStart, sleepWindowEnd),
                ),
            ).records
        }.getOrElse { emptyList() }
        val sleepHours = sleepRecords
            .filter { it.endTime.atZone(zoneId).toLocalDate() == logicalDate }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
            .toFloat() / 60f

        val stepRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(logicalStart, logicalEnd),
                ),
            ).records
        }.getOrElse { emptyList() }
        val steps = stepRecords.sumOf { it.count }

        val exerciseRecords = runCatching {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(logicalStart, logicalEnd),
                ),
            ).records
        }.getOrElse { emptyList() }
        val exerciseMinutes = exerciseRecords.sumOf {
            Duration.between(it.startTime, it.endTime).toMinutes()
        }.toFloat()

        return buildList {
            if (sleepHours > 0f) {
                add(
                    dailyObservation(
                        id = "health_sleep_$logicalDate",
                        domain = LifeDomain.SLEEP,
                        metric = ObservationMetric.SLEEP_HOURS,
                        value = sleepHours,
                        unit = "h",
                        logicalDate = logicalDate,
                        startedAt = sleepWindowStart,
                    ),
                )
            }
            if (steps > 0) {
                add(
                    dailyObservation(
                        id = "health_steps_$logicalDate",
                        domain = LifeDomain.MOVEMENT,
                        metric = ObservationMetric.STEPS,
                        value = steps.toFloat(),
                        unit = "steps",
                        logicalDate = logicalDate,
                        startedAt = logicalStart,
                    ),
                )
            }
            if (exerciseMinutes > 0f) {
                add(
                    dailyObservation(
                        id = "health_exercise_$logicalDate",
                        domain = LifeDomain.MOVEMENT,
                        metric = ObservationMetric.EXERCISE_MINUTES,
                        value = exerciseMinutes,
                        unit = "min",
                        logicalDate = logicalDate,
                        startedAt = logicalStart,
                    ),
                )
            }
        }
    }
}

private fun dailyObservation(
    id: String,
    domain: LifeDomain,
    metric: ObservationMetric,
    value: Float,
    unit: String,
    logicalDate: LocalDate,
    startedAt: Instant,
): DomainObservation {
    return DomainObservation(
        id = id,
        goalId = null,
        domain = domain,
        metric = metric,
        source = ObservationSource.WEARABLE,
        startedAt = startedAt,
        value = DomainObservationValue(
            numeric = value,
            unit = unit,
        ),
        logicalDate = logicalDate,
        sourceRecordId = id,
        confidence = 0.9f,
        contextTags = setOf("health_connect"),
    )
}
