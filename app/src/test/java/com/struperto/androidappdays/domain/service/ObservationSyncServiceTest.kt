package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.data.repository.CalendarSignal
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
import com.struperto.androidappdays.data.repository.HealthConnectAvailability
import com.struperto.androidappdays.data.repository.HealthConnectRepository
import com.struperto.androidappdays.data.repository.NotificationSignal
import com.struperto.androidappdays.data.repository.NotificationSignalRepository
import com.struperto.androidappdays.data.repository.ObservationRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.domain.CapabilityProfile
import com.struperto.androidappdays.domain.DataSourceCapability
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationSyncServiceTest {
    private val clock: Clock = Clock.fixed(
        Instant.parse("2026-03-08T08:00:00Z"),
        ZoneOffset.UTC,
    )

    @Test
    fun syncDay_collectsOnlyUsablePassiveSources() = runBlocking {
        val logicalDate = LocalDate.of(2026, 3, 8)
        val observationRepository = FakeObservationRepository()
        val healthRepository = FakeHealthConnectRepository(
            observations = listOf(
                testObservation(
                    id = "sleep",
                    domain = LifeDomain.SLEEP,
                    metric = ObservationMetric.SLEEP_HOURS,
                    value = 7.8f,
                    logicalDate = logicalDate,
                ),
                testObservation(
                    id = "steps",
                    domain = LifeDomain.MOVEMENT,
                    metric = ObservationMetric.STEPS,
                    value = 9420f,
                    logicalDate = logicalDate,
                ),
            ),
        )
        val calendarRepository = FakeCalendarSignalRepository(
            signals = listOf(
                CalendarSignal(1, "Standup", 1L, 2L, false),
                CalendarSignal(2, "Review", 3L, 4L, false),
                CalendarSignal(3, "1:1", 5L, 6L, false),
            ),
        )
        val notificationRepository = FakeNotificationSignalRepository(
            signals = listOf(
                NotificationSignal("n1", "pkg", "A", "alpha", 1L, null),
                NotificationSignal("n2", "pkg", "B", "beta", 2L, null),
            ),
        )
        val service = LocalObservationSyncService(
            clock = clock,
            observationRepository = observationRepository,
            sourceCapabilityRepository = FakeSourceCapabilityRepository(
                profile = capabilityProfile(
                    healthConnect = true,
                    calendar = true,
                    notifications = true,
                ),
            ),
            healthConnectRepository = healthRepository,
            calendarSignalRepository = calendarRepository,
            notificationSignalRepository = notificationRepository,
        )

        service.syncDay(logicalDate)

        assertEquals(1, healthRepository.readCount)
        assertEquals(1, calendarRepository.observeCount)
        assertEquals(1, notificationRepository.observeCount)
        assertEquals(4, observationRepository.saved.size)
        assertTrue(observationRepository.saved.any { it.metric == ObservationMetric.SLEEP_HOURS })
        assertTrue(observationRepository.saved.any { it.metric == ObservationMetric.STEPS })

        val calendarLoad = observationRepository.saved.first { it.metric == ObservationMetric.CALENDAR_LOAD }
        assertEquals(LifeDomain.FOCUS, calendarLoad.domain)
        assertEquals(3f, calendarLoad.value.numeric)

        val notificationLoad = observationRepository.saved.first { it.metric == ObservationMetric.NOTIFICATION_LOAD }
        assertEquals(LifeDomain.STRESS, notificationLoad.domain)
        assertEquals(2f, notificationLoad.value.numeric)
    }

    @Test
    fun syncDay_skipsUnavailableSourcesWithoutGeneratingFalseSignal() = runBlocking {
        val logicalDate = LocalDate.of(2026, 3, 8)
        val observationRepository = FakeObservationRepository()
        val healthRepository = FakeHealthConnectRepository(
            observations = listOf(
                testObservation(
                    id = "sleep",
                    domain = LifeDomain.SLEEP,
                    metric = ObservationMetric.SLEEP_HOURS,
                    value = 8f,
                    logicalDate = logicalDate,
                ),
            ),
        )
        val calendarRepository = FakeCalendarSignalRepository(
            signals = listOf(CalendarSignal(1, "Standup", 1L, 2L, false)),
        )
        val notificationRepository = FakeNotificationSignalRepository(
            signals = listOf(NotificationSignal("n1", "pkg", "A", "alpha", 1L, null)),
        )
        val service = LocalObservationSyncService(
            clock = clock,
            observationRepository = observationRepository,
            sourceCapabilityRepository = FakeSourceCapabilityRepository(
                profile = capabilityProfile(
                    healthConnect = false,
                    calendar = false,
                    notifications = false,
                ),
            ),
            healthConnectRepository = healthRepository,
            calendarSignalRepository = calendarRepository,
            notificationSignalRepository = notificationRepository,
        )

        service.syncDay(logicalDate)

        assertEquals(0, healthRepository.readCount)
        assertEquals(0, calendarRepository.observeCount)
        assertEquals(0, notificationRepository.observeCount)
        assertTrue(observationRepository.saved.isEmpty())
        assertFalse(observationRepository.upsertAllCalls.isEmpty())
        assertTrue(observationRepository.upsertAllCalls.single().isEmpty())
    }
}

private fun capabilityProfile(
    healthConnect: Boolean,
    calendar: Boolean,
    notifications: Boolean,
): CapabilityProfile {
    return CapabilityProfile(
        sources = listOf(
            sourceCapability(DataSourceKind.HEALTH_CONNECT, healthConnect),
            sourceCapability(DataSourceKind.CALENDAR, calendar),
            sourceCapability(DataSourceKind.NOTIFICATIONS, notifications),
            sourceCapability(DataSourceKind.MANUAL, true),
        ),
    )
}

private fun sourceCapability(
    source: DataSourceKind,
    usable: Boolean,
): DataSourceCapability {
    return DataSourceCapability(
        source = source,
        label = source.name,
        enabled = usable,
        available = usable,
        granted = usable,
        detail = "test",
    )
}

private fun testObservation(
    id: String,
    domain: LifeDomain,
    metric: ObservationMetric,
    value: Float,
    logicalDate: LocalDate,
): DomainObservation {
    return DomainObservation(
        id = id,
        goalId = null,
        domain = domain,
        metric = metric,
        source = ObservationSource.WEARABLE,
        startedAt = logicalDate.atStartOfDay().toInstant(ZoneOffset.UTC),
        value = DomainObservationValue(
            numeric = value,
            unit = "test",
        ),
        logicalDate = logicalDate,
        sourceRecordId = id,
        confidence = 0.9f,
        contextTags = setOf("test"),
    )
}

private class FakeObservationRepository : ObservationRepository {
    val saved = mutableListOf<DomainObservation>()
    val upsertAllCalls = mutableListOf<List<DomainObservation>>()

    override fun observeDay(logicalDate: LocalDate): Flow<List<DomainObservation>> = emptyFlow()

    override fun observeRange(
        startLogicalDate: LocalDate,
        endLogicalDate: LocalDate,
    ): Flow<List<DomainObservation>> = emptyFlow()

    override suspend fun loadRange(
        startLogicalDate: LocalDate,
        endLogicalDate: LocalDate,
    ): List<DomainObservation> = emptyList()

    override suspend fun upsert(observation: DomainObservation) {
        saved.removeAll { it.id == observation.id }
        saved += observation
    }

    override suspend fun upsertAll(observations: List<DomainObservation>) {
        upsertAllCalls += observations
        observations.forEach { observation ->
            saved.removeAll { it.id == observation.id }
            saved += observation
        }
    }

    override suspend fun clearAll() {
        saved.clear()
        upsertAllCalls.clear()
    }

    override suspend fun saveManualNumeric(
        logicalDate: LocalDate,
        domain: LifeDomain,
        metric: ObservationMetric,
        value: Float?,
        unit: String,
        goalId: String?,
    ) = Unit
}

private class FakeSourceCapabilityRepository(
    private val profile: CapabilityProfile,
) : SourceCapabilityRepository {
    override fun observeProfile(): Flow<CapabilityProfile> = flowOf(profile)

    override suspend fun loadProfile(): CapabilityProfile = profile

    override suspend fun ensureSeeded() = Unit

    override suspend fun setEnabled(
        source: DataSourceKind,
        enabled: Boolean,
    ) = Unit
}

private class FakeHealthConnectRepository(
    private val observations: List<DomainObservation>,
) : HealthConnectRepository {
    var readCount: Int = 0

    override val requiredPermissions: Set<String> = emptySet()

    override suspend fun availability(): HealthConnectAvailability = HealthConnectAvailability.AVAILABLE

    override suspend fun grantedPermissions(): Set<String> = emptySet()

    override suspend fun readDailyObservations(logicalDate: LocalDate): List<DomainObservation> {
        readCount += 1
        return observations
    }
}

private class FakeCalendarSignalRepository(
    private val signals: List<CalendarSignal>,
) : CalendarSignalRepository {
    var observeCount: Int = 0

    override fun observeToday(
        date: LocalDate,
        zoneId: java.time.ZoneId,
    ): Flow<List<CalendarSignal>> {
        observeCount += 1
        return flowOf(signals)
    }
}

private class FakeNotificationSignalRepository(
    private val signals: List<NotificationSignal>,
) : NotificationSignalRepository {
    var observeCount: Int = 0

    override fun observeToday(
        date: LocalDate,
        zoneId: java.time.ZoneId,
    ): Flow<List<NotificationSignal>> {
        observeCount += 1
        return flowOf(signals)
    }

    override suspend fun upsert(
        id: String,
        packageName: String,
        title: String,
        text: String,
        postedAt: Long,
    ) = Unit

    override suspend fun markRemoved(
        id: String,
        removedAt: Long,
    ) = Unit

    override suspend fun clearAll() = Unit
}
