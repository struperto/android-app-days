package com.struperto.androidappdays.domain.service

import com.struperto.androidappdays.data.repository.CalendarSignalRepository
import com.struperto.androidappdays.data.repository.HealthConnectRepository
import com.struperto.androidappdays.data.repository.NotificationSignalRepository
import com.struperto.androidappdays.data.repository.ObservationRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.domain.DataSourceKind
import com.struperto.androidappdays.domain.DomainObservation
import com.struperto.androidappdays.domain.DomainObservationValue
import com.struperto.androidappdays.domain.LifeDomain
import com.struperto.androidappdays.domain.ObservationMetric
import com.struperto.androidappdays.domain.ObservationSource
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.first

interface ObservationSyncService {
    suspend fun syncDay(logicalDate: LocalDate)
}

class LocalObservationSyncService(
    private val clock: Clock,
    private val observationRepository: ObservationRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val calendarSignalRepository: CalendarSignalRepository,
    private val notificationSignalRepository: NotificationSignalRepository,
) : ObservationSyncService {
    override suspend fun syncDay(logicalDate: LocalDate) {
        val profile = sourceCapabilityRepository.loadProfile()
        val observations = buildList {
            if (profile.isUsable(DataSourceKind.HEALTH_CONNECT)) {
                addAll(healthConnectRepository.readDailyObservations(logicalDate))
            }
            if (profile.isUsable(DataSourceKind.CALENDAR)) {
                val calendarSignals = calendarSignalRepository.observeToday(
                    date = logicalDate,
                    zoneId = clock.zone,
                ).first()
                add(
                    DomainObservation(
                        id = "calendar_load_$logicalDate",
                        goalId = null,
                        domain = LifeDomain.FOCUS,
                        metric = ObservationMetric.CALENDAR_LOAD,
                        source = ObservationSource.CALENDAR,
                        startedAt = logicalDate.atStartOfDay(clock.zone).toInstant(),
                        value = DomainObservationValue(
                            numeric = calendarSignals.size.toFloat(),
                            unit = "count",
                        ),
                        logicalDate = logicalDate,
                        sourceRecordId = "calendar_load_$logicalDate",
                        confidence = 0.72f,
                        contextTags = setOf("calendar"),
                    ),
                )
            }
            if (profile.isUsable(DataSourceKind.NOTIFICATIONS)) {
                val notifications = notificationSignalRepository.observeToday(
                    date = logicalDate,
                    zoneId = clock.zone,
                ).first()
                add(
                    DomainObservation(
                        id = "notification_load_$logicalDate",
                        goalId = null,
                        domain = LifeDomain.STRESS,
                        metric = ObservationMetric.NOTIFICATION_LOAD,
                        source = ObservationSource.NOTIFICATION_LISTENER,
                        startedAt = logicalDate.atStartOfDay(clock.zone).toInstant(),
                        value = DomainObservationValue(
                            numeric = notifications.size.toFloat(),
                            unit = "count",
                        ),
                        logicalDate = logicalDate,
                        sourceRecordId = "notification_load_$logicalDate",
                        confidence = 0.66f,
                        contextTags = setOf("notifications"),
                    ),
                )
            }
        }
        observationRepository.upsertAll(observations)
    }
}
