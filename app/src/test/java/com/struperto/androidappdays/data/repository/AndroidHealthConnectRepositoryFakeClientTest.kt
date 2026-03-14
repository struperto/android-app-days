package com.struperto.androidappdays.data.repository

import android.content.ContextWrapper
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.testing.FakeHealthConnectClient
import androidx.health.connect.client.testing.FakePermissionController
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidHealthConnectRepositoryFakeClientTest {
    @Test
    fun officialFakeClient_projectsDailyObservations() = runTest {
        val permissionController = FakePermissionController().apply {
            grantPermissions(
                setOf(
                    HealthPermission.getReadPermission(SleepSessionRecord::class),
                    HealthPermission.getReadPermission(StepsRecord::class),
                    HealthPermission.getReadPermission(ExerciseSessionRecord::class),
                ),
            )
        }
        val client = FakeHealthConnectClient(
            "com.struperto.androidappdays.test",
            java.time.Clock.systemUTC(),
            permissionController,
        )
        client.insertRecords(
            listOf<Record>(
                SleepSessionRecord(
                    startTime = Instant.parse("2026-03-11T22:00:00Z"),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.parse("2026-03-12T05:00:00Z"),
                    endZoneOffset = ZoneOffset.UTC,
                    metadata = Metadata.manualEntry(),
                ),
                StepsRecord(
                    startTime = Instant.parse("2026-03-12T08:00:00Z"),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.parse("2026-03-12T09:00:00Z"),
                    endZoneOffset = ZoneOffset.UTC,
                    count = 3600,
                    metadata = Metadata.manualEntry(),
                ),
                ExerciseSessionRecord(
                    startTime = Instant.parse("2026-03-12T16:00:00Z"),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.parse("2026-03-12T16:30:00Z"),
                    endZoneOffset = ZoneOffset.UTC,
                    metadata = Metadata.manualEntry(),
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                ),
            ),
        )

        val repository = AndroidHealthConnectRepository(
            context = ContextWrapper(null),
            gateway = FakeClientGateway(client),
        )

        val observations = repository.readDailyObservations(LocalDate.parse("2026-03-12"))

        assertEquals(3, observations.size)
        assertEquals(7.0f, observations.first { it.metric.name == "SLEEP_HOURS" }.value.numeric)
        assertEquals(3600f, observations.first { it.metric.name == "STEPS" }.value.numeric)
        assertEquals(30.0f, observations.first { it.metric.name == "EXERCISE_MINUTES" }.value.numeric)
    }

    @Test
    fun officialFakeClient_respectsMissingPermissions() = runTest {
        val client = FakeHealthConnectClient(
            "com.struperto.androidappdays.test",
            java.time.Clock.systemUTC(),
            FakePermissionController(false),
        )
        client.insertRecords(
            listOf(
                StepsRecord(
                    startTime = Instant.parse("2026-03-12T08:00:00Z"),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.parse("2026-03-12T09:00:00Z"),
                    endZoneOffset = ZoneOffset.UTC,
                    count = 3600,
                    metadata = Metadata.manualEntry(),
                ),
            ),
        )
        val repository = AndroidHealthConnectRepository(
            context = ContextWrapper(null),
            gateway = FakeClientGateway(client),
        )

        val observations = repository.readDailyObservations(LocalDate.parse("2026-03-12"))

        assertTrue(observations.isEmpty())
    }
}

private class FakeClientGateway(
    private val client: HealthConnectClient,
) : HealthConnectGateway {
    override suspend fun sdkStatus(): Int = HealthConnectClient.SDK_AVAILABLE

    override suspend fun grantedPermissions(): Set<String> {
        return client.permissionController.getGrantedPermissions()
    }

    override suspend fun readSleepSessions(
        start: Instant,
        end: Instant,
    ): List<SleepSessionRecord> {
        return client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            ),
        ).records
    }

    override suspend fun readSteps(
        start: Instant,
        end: Instant,
    ): List<StepsRecord> {
        return client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            ),
        ).records
    }

    override suspend fun readExerciseSessions(
        start: Instant,
        end: Instant,
    ): List<ExerciseSessionRecord> {
        return client.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            ),
        ).records
    }
}
