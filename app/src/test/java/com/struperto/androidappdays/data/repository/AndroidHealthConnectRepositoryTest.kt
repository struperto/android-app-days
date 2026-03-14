package com.struperto.androidappdays.data.repository

import android.content.ContextWrapper
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Metadata
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidHealthConnectRepositoryTest {
    @Test
    fun readDailyObservations_returnsEmptyWhenSdkIsUnavailable() = runTest {
        val gateway = FakeHealthConnectGateway(
            sdkStatus = HealthConnectClient.SDK_UNAVAILABLE,
        )
        val repository = AndroidHealthConnectRepository(
            context = ContextWrapper(null),
            gateway = gateway,
        )

        val observations = repository.readDailyObservations(LocalDate.parse("2026-03-12"))

        assertTrue(observations.isEmpty())
        assertEquals(0, gateway.readSleepCalls)
        assertEquals(0, gateway.readStepsCalls)
        assertEquals(0, gateway.readExerciseCalls)
    }

    @Test
    fun readDailyObservations_returnsEmptyWhenPermissionsAreMissing() = runTest {
        val gateway = FakeHealthConnectGateway(
            sdkStatus = HealthConnectClient.SDK_AVAILABLE,
            grantedPermissions = emptySet(),
        )
        val repository = AndroidHealthConnectRepository(
            context = ContextWrapper(null),
            gateway = gateway,
        )

        val observations = repository.readDailyObservations(LocalDate.parse("2026-03-12"))

        assertTrue(observations.isEmpty())
        assertEquals(0, gateway.readSleepCalls)
        assertEquals(0, gateway.readStepsCalls)
        assertEquals(0, gateway.readExerciseCalls)
    }

    @Test
    fun readDailyObservations_projectsSleepStepsAndExerciseForLogicalDay() = runTest {
        val date = LocalDate.parse("2026-03-12")
        val allPermissions = setOf(
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(
                SleepSessionRecord::class,
            ),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(
                StepsRecord::class,
            ),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(
                ExerciseSessionRecord::class,
            ),
        )
        val gateway = FakeHealthConnectGateway(
            sdkStatus = HealthConnectClient.SDK_AVAILABLE,
            grantedPermissions = allPermissions,
            sleepSessions = listOf(
                SleepSessionRecord(
                    startTime = Instant.parse("2026-03-11T22:30:00Z"),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.parse("2026-03-12T05:30:00Z"),
                    endZoneOffset = ZoneOffset.UTC,
                    metadata = Metadata.manualEntry(),
                ),
            ),
            steps = listOf(
                StepsRecord(
                    startTime = Instant.parse("2026-03-12T09:00:00Z"),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.parse("2026-03-12T09:30:00Z"),
                    endZoneOffset = ZoneOffset.UTC,
                    count = 4200,
                    metadata = Metadata.manualEntry(),
                ),
            ),
            exerciseSessions = listOf(
                ExerciseSessionRecord(
                    startTime = Instant.parse("2026-03-12T16:00:00Z"),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.parse("2026-03-12T16:45:00Z"),
                    endZoneOffset = ZoneOffset.UTC,
                    metadata = Metadata.manualEntry(),
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                ),
            ),
        )
        val repository = AndroidHealthConnectRepository(
            context = ContextWrapper(null),
            gateway = gateway,
        )

        val observations = repository.readDailyObservations(date)

        assertEquals(3, observations.size)
        assertEquals(LifeDomain.SLEEP, observations[0].domain)
        assertEquals(ObservationMetric.SLEEP_HOURS, observations[0].metric)
        assertEquals(7.0f, observations[0].value.numeric)
        assertEquals(ObservationMetric.STEPS, observations[1].metric)
        assertEquals(4200f, observations[1].value.numeric)
        assertEquals(ObservationMetric.EXERCISE_MINUTES, observations[2].metric)
        assertEquals(45.0f, observations[2].value.numeric)
        assertEquals(1, gateway.readSleepCalls)
        assertEquals(1, gateway.readStepsCalls)
        assertEquals(1, gateway.readExerciseCalls)
    }
}

private class FakeHealthConnectGateway(
    private val sdkStatus: Int,
    private val grantedPermissions: Set<String> = emptySet(),
    private val sleepSessions: List<SleepSessionRecord> = emptyList(),
    private val steps: List<StepsRecord> = emptyList(),
    private val exerciseSessions: List<ExerciseSessionRecord> = emptyList(),
) : HealthConnectGateway {
    var readSleepCalls: Int = 0
    var readStepsCalls: Int = 0
    var readExerciseCalls: Int = 0

    override suspend fun sdkStatus(): Int = sdkStatus

    override suspend fun grantedPermissions(): Set<String> = grantedPermissions

    override suspend fun readSleepSessions(
        start: Instant,
        end: Instant,
    ): List<SleepSessionRecord> {
        readSleepCalls += 1
        return sleepSessions
    }

    override suspend fun readSteps(
        start: Instant,
        end: Instant,
    ): List<StepsRecord> {
        readStepsCalls += 1
        return steps
    }

    override suspend fun readExerciseSessions(
        start: Instant,
        end: Instant,
    ): List<ExerciseSessionRecord> {
        readExerciseCalls += 1
        return exerciseSessions
    }
}
