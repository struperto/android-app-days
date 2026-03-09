package com.struperto.androidappdays.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.feature.multi.MultiScreen
import com.struperto.androidappdays.feature.start.AreaStudioScreen
import com.struperto.androidappdays.feature.start.AreaStudioViewModel
import com.struperto.androidappdays.feature.start.StartScreen
import com.struperto.androidappdays.feature.settings.SettingsDomainDetailScreen
import com.struperto.androidappdays.feature.settings.SettingsDomainsScreen
import com.struperto.androidappdays.feature.settings.SettingsResearchScreen
import com.struperto.androidappdays.feature.settings.SettingsScreen
import com.struperto.androidappdays.feature.settings.SettingsSourcesScreen
import com.struperto.androidappdays.feature.settings.SettingsViewModel
import com.struperto.androidappdays.feature.single.capture.CaptureScreen
import com.struperto.androidappdays.feature.single.capture.CaptureViewModel
import com.struperto.androidappdays.feature.single.create.CreateScreen
import com.struperto.androidappdays.feature.single.home.SingleHomeScreen
import com.struperto.androidappdays.feature.single.home.SingleHomeViewModel
import com.struperto.androidappdays.feature.single.lifewheel.LifeWheelScreen
import com.struperto.androidappdays.feature.single.lifewheel.LifeWheelViewModel
import com.struperto.androidappdays.feature.single.plan.PlanScreen
import com.struperto.androidappdays.feature.single.plan.PlanViewModel
import com.struperto.androidappdays.feature.single.schedule.DayScheduleScreen
import com.struperto.androidappdays.feature.single.workbench.WorkbenchScreen
import com.struperto.androidappdays.feature.single.workbench.WorkbenchViewModel
import com.struperto.androidappdays.feature.single.workingset.WorkingSetScreen
import com.struperto.androidappdays.feature.single.workingset.WorkingSetViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val appContainer = (LocalContext.current.applicationContext as DaysApp).appContainer
    fun navigateMode(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }
    NavHost(
        navController = navController,
        startDestination = AppDestination.Start.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Boot.route) {
            LaunchedEffect(Unit) {
                navController.navigate(AppDestination.Start.route) {
                    popUpTo(AppDestination.Boot.route) {
                        inclusive = true
                    }
                }
            }
        }
        composable(AppDestination.Start.route) {
            val homeViewModel: SingleHomeViewModel = viewModel(
                factory = SingleHomeViewModel.factory(appContainer),
            )
            val homeState by homeViewModel.state.collectAsStateWithLifecycle()
            StartScreen(
                homeState = homeState,
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                onOpenStart = { navigateMode(AppDestination.Start.route) },
                onOpenHome = { navigateMode(AppDestination.Home.route) },
                onOpenMulti = { navigateMode(AppDestination.Multi.route) },
                onOpenArea = { areaId -> navController.navigate(startAreaRoute(areaId)) },
                onOpenLifeWheel = { navController.navigate(AppDestination.LifeWheel.route) },
                onOpenSettingsDomains = { navController.navigate(AppDestination.SettingsDomains.route) },
                onOpenSettingsSources = { navController.navigate(AppDestination.SettingsSources.route) },
                onOpenSettingsResearch = { navController.navigate(AppDestination.SettingsResearch.route) },
            )
        }
        composable(
            route = StartAreaPattern,
            arguments = listOf(
                navArgument(StartAreaArg) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val viewModel: AreaStudioViewModel = viewModel(
                factory = AreaStudioViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            val areaId = backStackEntry.arguments?.getString(StartAreaArg).orEmpty()
            AreaStudioScreen(
                state = state,
                areaId = areaId,
                onBack = navController::popBackStack,
                onTargetScoreChange = viewModel::setTargetScore,
                onManualScoreChange = viewModel::setManualScore,
                onCadenceChange = viewModel::setCadence,
                onIntensityChange = viewModel::setIntensity,
                onSignalBlendChange = viewModel::setSignalBlend,
                onToggleTrack = viewModel::toggleTrack,
                onRemindersChange = viewModel::setRemindersEnabled,
                onReviewChange = viewModel::setReviewEnabled,
                onExperimentsChange = viewModel::setExperimentsEnabled,
            )
        }
        composable(AppDestination.Home.route) {
            val homeViewModel: SingleHomeViewModel = viewModel(
                factory = SingleHomeViewModel.factory(appContainer),
            )
            val state by homeViewModel.state.collectAsStateWithLifecycle()
            SingleHomeScreen(
                state = state,
                onOpenStart = { navigateMode(AppDestination.Start.route) },
                onOpenMulti = { navigateMode(AppDestination.Multi.route) },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                onRefreshPassiveSignals = homeViewModel::refreshPassiveSignals,
                onSetHourSlotStatus = homeViewModel::setHourSlotStatus,
                onSaveHourSlotNote = homeViewModel::saveHourSlotNote,
            )
        }
        composable(AppDestination.Multi.route) {
            MultiScreen(
                onOpenStart = { navigateMode(AppDestination.Start.route) },
                onOpenSingle = { navigateMode(AppDestination.Home.route) },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
            )
        }
        composable(
            route = WorkbenchPattern,
            arguments = listOf(
                navArgument(WorkbenchPaneArg) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            val viewModel: WorkbenchViewModel = viewModel(
                factory = WorkbenchViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            WorkbenchScreen(
                state = state,
                onPaneSelected = viewModel::setPane,
                onDraftChange = viewModel::onDraftChange,
                onAreaSelected = viewModel::onAreaSelected,
                onTimeBlockSelected = viewModel::onTimeBlockSelected,
                onSubmit = viewModel::submit,
                onImportClipboard = viewModel::importClipboard,
                onVoiceTranscript = viewModel::ingestVoiceTranscript,
                onShowShareHint = viewModel::showShareHint,
                onAssistDraft = viewModel::generateAssistFromDraft,
                onAssistLatest = viewModel::generateAssistFromLatestCapture,
                onSignalSelected = viewModel::jumpToSignal,
                onClearFeedback = viewModel::clearFeedback,
                onCaptureToToday = viewModel::sendCaptureToToday,
                onCaptureToLater = viewModel::sendCaptureToLater,
                onCaptureDone = viewModel::completeCapture,
                onVorhabenToToday = viewModel::sendVorhabenToToday,
                onVorhabenDone = viewModel::completeVorhaben,
                onTogglePlanDone = viewModel::togglePlanDone,
                onRemovePlan = viewModel::removePlan,
                onBack = navController::popBackStack,
            )
        }
        composable(AppDestination.LifeWheel.route) {
            val viewModel: LifeWheelViewModel = viewModel(
                factory = LifeWheelViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(state.didCompleteSetup) {
                if (state.didCompleteSetup) {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.LifeWheel.route) {
                            inclusive = true
                        }
                    }
                    viewModel.acknowledgeCompletion()
                }
            }
            LifeWheelScreen(
                state = state,
                onRolesChange = viewModel::onRolesChange,
                onResponsibilitiesChange = viewModel::onResponsibilitiesChange,
                onPriorityRulesChange = viewModel::onPriorityRulesChange,
                onWeeklyRhythmChange = viewModel::onWeeklyRhythmChange,
                onRecurringCommitmentsChange = viewModel::onRecurringCommitmentsChange,
                onGoodDayPatternChange = viewModel::onGoodDayPatternChange,
                onBadDayPatternChange = viewModel::onBadDayPatternChange,
                onDayStartHourChange = viewModel::onDayStartHourChange,
                onDayEndHourChange = viewModel::onDayEndHourChange,
                onMorningEnergyChange = viewModel::onMorningEnergyChange,
                onAfternoonEnergyChange = viewModel::onAfternoonEnergyChange,
                onEveningEnergyChange = viewModel::onEveningEnergyChange,
                onFocusStrengthChange = viewModel::onFocusStrengthChange,
                onDisruptionSensitivityChange = viewModel::onDisruptionSensitivityChange,
                onRecoveryNeedChange = viewModel::onRecoveryNeedChange,
                onSaveFingerprint = viewModel::saveFingerprint,
                onCommitDiscovery = viewModel::commitDiscovery,
                onAreaLabelChange = viewModel::onAreaLabelChange,
                onAreaDefinitionChange = viewModel::onAreaDefinitionChange,
                onAreaTargetScoreChange = viewModel::onAreaTargetScoreChange,
                onSaveArea = viewModel::saveArea,
                onManualScoreChange = viewModel::setManualScore,
                onBack = navController::popBackStack,
            )
        }
        composable(
            route = WorkingSetPattern,
            arguments = listOf(
                navArgument(WorkingSetCaptureIdArg) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            val viewModel: WorkingSetViewModel = viewModel(
                factory = WorkingSetViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            WorkingSetScreen(
                state = state,
                onTitleChange = viewModel::onTitleChange,
                onNoteChange = viewModel::onNoteChange,
                onAreaSelected = viewModel::onAreaSelected,
                onSave = viewModel::save,
                onOpenPlan = { vorhabenId ->
                    navController.navigate(planRoute(vorhabenId = vorhabenId))
                },
                onArchive = viewModel::archive,
                onBack = navController::popBackStack,
            )
        }
        composable(AppDestination.DaySchedule.route) {
            DayScheduleScreen(onBack = navController::popBackStack)
        }
        composable(
            route = PlanPattern,
            arguments = listOf(
                navArgument(PlanCaptureIdArg) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(PlanVorhabenIdArg) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            val viewModel: PlanViewModel = viewModel(
                factory = PlanViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            PlanScreen(
                state = state,
                onTitleChange = viewModel::onTitleChange,
                onNoteChange = viewModel::onNoteChange,
                onAreaSelected = viewModel::onAreaSelected,
                onTimeBlockSelected = viewModel::onTimeBlockSelected,
                onSave = viewModel::save,
                onToggleDone = viewModel::toggleDone,
                onRemove = viewModel::remove,
                onBack = navController::popBackStack,
            )
        }
        composable(AppDestination.Capture.route) {
            val viewModel: CaptureViewModel = viewModel(
                factory = CaptureViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            CaptureScreen(
                state = state,
                onDraftChange = viewModel::onDraftChange,
                onAreaSelected = viewModel::onAreaSelected,
                onSave = viewModel::save,
                onDone = viewModel::archive,
                onOpenVorhaben = { captureId ->
                    navController.navigate(workingSetRoute(captureId))
                },
                onOpenPlan = { captureId ->
                    navController.navigate(planRoute(captureId = captureId))
                },
                onBack = navController::popBackStack,
            )
        }
        composable(AppDestination.Create.route) {
            CreateScreen(onBack = navController::popBackStack)
        }
        composable(AppDestination.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            SettingsScreen(
                state = state,
                onBack = navController::popBackStack,
                onRefresh = viewModel::refresh,
                onOpenDomains = { navController.navigate(AppDestination.SettingsDomains.route) },
                onOpenSources = { navController.navigate(AppDestination.SettingsSources.route) },
                onOpenResearch = { navController.navigate(AppDestination.SettingsResearch.route) },
            )
        }
        composable(AppDestination.SettingsDomains.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            SettingsDomainsScreen(
                state = state,
                onBack = navController::popBackStack,
                onOpenDomain = { domain ->
                    navController.navigate(settingsDomainRoute(domain.name))
                },
            )
        }
        composable(
            route = SettingsDomainPattern,
            arguments = listOf(
                navArgument(SettingsDomainArg) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            val domain = backStackEntry.arguments
                ?.getString(SettingsDomainArg)
                ?.let { runCatching { com.struperto.androidappdays.domain.LifeDomain.valueOf(it) }.getOrNull() }
                ?: com.struperto.androidappdays.domain.LifeDomain.FOCUS
            SettingsDomainDetailScreen(
                state = state,
                domain = domain,
                onBack = navController::popBackStack,
                onSaveGoal = viewModel::saveGoal,
                onSaveManualMetric = viewModel::saveManualMetric,
            )
        }
        composable(AppDestination.SettingsSources.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            SettingsSourcesScreen(
                state = state,
                onBack = navController::popBackStack,
                onRefresh = viewModel::refresh,
                onSetSourceEnabled = viewModel::setSourceEnabled,
            )
        }
        composable(AppDestination.SettingsResearch.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            SettingsResearchScreen(
                state = state,
                onBack = navController::popBackStack,
                onLoadPersona = viewModel::loadPersona,
                onRunAllPersonas = viewModel::runAllPersonas,
            )
        }
    }
}
