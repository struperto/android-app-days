package com.struperto.androidappdays

import android.content.Context
import androidx.room.Room
import com.struperto.androidappdays.data.local.SingleDatabase
import com.struperto.androidappdays.data.repository.CalendarSignalRepository
import com.struperto.androidappdays.data.repository.CaptureRepository
import com.struperto.androidappdays.data.repository.DeviceCalendarSignalRepository
import com.struperto.androidappdays.data.repository.GoalRepository
import com.struperto.androidappdays.data.repository.HealthConnectRepository
import com.struperto.androidappdays.data.repository.HourSlotEntryRepository
import com.struperto.androidappdays.data.repository.LearningEventRepository
import com.struperto.androidappdays.data.repository.LifeAreaProfileRepository
import com.struperto.androidappdays.data.repository.LocalSignalRepository
import com.struperto.androidappdays.data.repository.LocalSourceCapabilityRepository
import com.struperto.androidappdays.data.repository.LifeWheelRepository
import com.struperto.androidappdays.data.repository.NotificationSignalRepository
import com.struperto.androidappdays.data.repository.ObservationRepository
import com.struperto.androidappdays.data.repository.PlanRepository
import com.struperto.androidappdays.data.repository.RoomGoalRepository
import com.struperto.androidappdays.data.repository.RoomHourSlotEntryRepository
import com.struperto.androidappdays.data.repository.RoomLifeWheelRepository
import com.struperto.androidappdays.data.repository.RoomCaptureRepository
import com.struperto.androidappdays.data.repository.RoomLearningEventRepository
import com.struperto.androidappdays.data.repository.RoomLifeAreaProfileRepository
import com.struperto.androidappdays.data.repository.RoomNotificationSignalRepository
import com.struperto.androidappdays.data.repository.RoomObservationRepository
import com.struperto.androidappdays.data.repository.RoomPlanRepository
import com.struperto.androidappdays.data.repository.RoomUserFingerprintRepository
import com.struperto.androidappdays.data.repository.RoomVorhabenRepository
import com.struperto.androidappdays.data.repository.SignalRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import com.struperto.androidappdays.data.repository.UserFingerprintRepository
import com.struperto.androidappdays.data.repository.VorhabenRepository
import com.struperto.androidappdays.data.repository.AndroidHealthConnectRepository
import com.struperto.androidappdays.domain.service.EvaluationEngineV0
import com.struperto.androidappdays.domain.service.HomeDomainHintProjector
import com.struperto.androidappdays.domain.service.HypothesisEngineV0
import com.struperto.androidappdays.domain.service.LocalEvaluationEngineV0
import com.struperto.androidappdays.domain.service.LocalHomeDomainHintProjector
import com.struperto.androidappdays.domain.service.LocalHypothesisEngineV0
import com.struperto.androidappdays.domain.service.LocalObservationSyncService
import com.struperto.androidappdays.domain.service.ObservationSyncService
import com.struperto.androidappdays.feature.single.assist.HeuristicLocalAssistGateway
import com.struperto.androidappdays.feature.single.assist.LocalAssistGateway
import com.struperto.androidappdays.feature.single.home.DayModelEngine
import com.struperto.androidappdays.feature.single.home.LocalDayModelEngine
import com.struperto.androidappdays.feature.single.home.LocalSollEngine
import com.struperto.androidappdays.feature.single.home.SollEngine
import com.struperto.androidappdays.testing.MvpPersonaScenarioRunner
import java.time.Clock

class AppContainer(context: Context) {
    val clock: Clock = Clock.systemDefaultZone()

    private val database: SingleDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            SingleDatabase::class.java,
            "single-v1.db",
        ).fallbackToDestructiveMigration().build()
    }

    val lifeWheelRepository: LifeWheelRepository by lazy {
        RoomLifeWheelRepository(
            database = database,
            lifeWheelDao = database.lifeWheelDao(),
            clock = clock,
        )
    }

    val lifeAreaProfileRepository: LifeAreaProfileRepository by lazy {
        RoomLifeAreaProfileRepository(
            dao = database.lifeAreaProfileDao(),
            clock = clock,
        )
    }

    val captureRepository: CaptureRepository by lazy {
        RoomCaptureRepository(
            captureItemDao = database.captureItemDao(),
            clock = clock,
        )
    }

    val calendarSignalRepository: CalendarSignalRepository by lazy {
        DeviceCalendarSignalRepository(
            context = context.applicationContext,
        )
    }

    val vorhabenRepository: VorhabenRepository by lazy {
        RoomVorhabenRepository(
            vorhabenDao = database.vorhabenDao(),
            clock = clock,
        )
    }

    val planRepository: PlanRepository by lazy {
        RoomPlanRepository(
            planItemDao = database.planItemDao(),
            vorhabenDao = database.vorhabenDao(),
            clock = clock,
        )
    }

    val notificationSignalRepository: NotificationSignalRepository by lazy {
        RoomNotificationSignalRepository(
            dao = database.notificationSignalDao(),
            clock = clock,
        )
    }

    val learningEventRepository: LearningEventRepository by lazy {
        RoomLearningEventRepository(
            dao = database.learningEventDao(),
            clock = clock,
        )
    }

    val userFingerprintRepository: UserFingerprintRepository by lazy {
        RoomUserFingerprintRepository(
            dao = database.userFingerprintDao(),
            lifeWheelDao = database.lifeWheelDao(),
            learningEventDao = database.learningEventDao(),
            clock = clock,
        )
    }

    val signalRepository: SignalRepository by lazy {
        LocalSignalRepository(
            calendarSignalRepository = calendarSignalRepository,
            notificationSignalRepository = notificationSignalRepository,
            captureRepository = captureRepository,
            vorhabenRepository = vorhabenRepository,
            planRepository = planRepository,
        )
    }

    val goalRepository: GoalRepository by lazy {
        RoomGoalRepository(
            database = database,
            goalDao = database.domainGoalDao(),
            catalogDao = database.domainCatalogDao(),
            clock = clock,
        )
    }

    val observationRepository: ObservationRepository by lazy {
        RoomObservationRepository(
            dao = database.observationEventDao(),
            clock = clock,
        )
    }

    val hourSlotEntryRepository: HourSlotEntryRepository by lazy {
        RoomHourSlotEntryRepository(
            dao = database.hourSlotEntryDao(),
            clock = clock,
        )
    }

    val healthConnectRepository: HealthConnectRepository by lazy {
        AndroidHealthConnectRepository(
            context = context.applicationContext,
        )
    }

    val sourceCapabilityRepository: SourceCapabilityRepository by lazy {
        LocalSourceCapabilityRepository(
            context = context.applicationContext,
            preferenceDao = database.sourcePreferenceDao(),
            healthConnectRepository = healthConnectRepository,
            clock = clock,
        )
    }

    val observationSyncService: ObservationSyncService by lazy {
        LocalObservationSyncService(
            clock = clock,
            observationRepository = observationRepository,
            sourceCapabilityRepository = sourceCapabilityRepository,
            healthConnectRepository = healthConnectRepository,
            calendarSignalRepository = calendarSignalRepository,
            notificationSignalRepository = notificationSignalRepository,
        )
    }

    val evaluationEngineV0: EvaluationEngineV0 by lazy {
        LocalEvaluationEngineV0()
    }

    val hypothesisEngineV0: HypothesisEngineV0 by lazy {
        LocalHypothesisEngineV0()
    }

    val homeDomainHintProjector: HomeDomainHintProjector by lazy {
        LocalHomeDomainHintProjector()
    }

    val mvpPersonaScenarioRunner: MvpPersonaScenarioRunner by lazy {
        MvpPersonaScenarioRunner(
            clock = clock,
            userFingerprintRepository = userFingerprintRepository,
            goalRepository = goalRepository,
            observationRepository = observationRepository,
            hourSlotEntryRepository = hourSlotEntryRepository,
            sourceCapabilityRepository = sourceCapabilityRepository,
            dayModelEngine = dayModelEngine,
            evaluationEngineV0 = evaluationEngineV0,
            hypothesisEngineV0 = hypothesisEngineV0,
            homeDomainHintProjector = homeDomainHintProjector,
        )
    }

    val localAssistGateway: LocalAssistGateway by lazy {
        HeuristicLocalAssistGateway()
    }

    val dayModelEngine: DayModelEngine by lazy {
        LocalDayModelEngine()
    }

    val sollEngine: SollEngine by lazy {
        LocalSollEngine()
    }

    suspend fun seedDefaults() {
        lifeWheelRepository.ensureSeededAreas()
        goalRepository.ensureSeeded()
        sourceCapabilityRepository.ensureSeeded()
    }
}
