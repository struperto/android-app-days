package com.struperto.androidappdays.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.feature.multi.MultiScreen
import com.struperto.androidappdays.feature.multi.MultiViewModel
import com.struperto.androidappdays.feature.settings.SettingsDomainDetailScreen
import com.struperto.androidappdays.feature.settings.SettingsDomainsScreen
import com.struperto.androidappdays.feature.settings.SettingsScreen
import com.struperto.androidappdays.feature.settings.SettingsSourcesScreen
import com.struperto.androidappdays.feature.settings.SettingsViewModel
import com.struperto.androidappdays.feature.single.home.SingleHomeScreen
import com.struperto.androidappdays.feature.single.home.SingleHomeViewModel
import com.struperto.androidappdays.feature.start.AreaStudioScreen
import com.struperto.androidappdays.feature.start.AreaStudioViewModel
import com.struperto.androidappdays.feature.start.StartScreen
import com.struperto.androidappdays.feature.start.StartViewModel

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
        startDestination = AppDestination.Boot.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Boot.route) {
            val bootstrapState by appContainer.appBootstrapCoordinator.state.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                appContainer.ensureAppBootstrapped()
            }
            LaunchedEffect(bootstrapState.isReady) {
                if (!bootstrapState.isReady) return@LaunchedEffect
                navController.navigate(AppDestination.Start.route) {
                    popUpTo(AppDestination.Boot.route) {
                        inclusive = true
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTag = "app-boot-screen" },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = bootstrapState.lastErrorMessage
                            ?.let { "Startinitialisierung fehlgeschlagen: $it" }
                            ?: "Days initialisiert Start und Quellen.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        composable(AppDestination.Start.route) {
            val viewModel: StartViewModel = viewModel(
                factory = StartViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            StartScreen(
                state = state,
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                onOpenStart = { navigateMode(AppDestination.Start.route) },
                onOpenHome = { navigateMode(AppDestination.Home.route) },
                onOpenMulti = { navigateMode(AppDestination.Multi.route) },
                onOpenArea = { areaId: String -> navController.navigate(startAreaRoute(areaId)) },
                onDeleteArea = viewModel::deleteArea,
                onMoveAreaEarlier = viewModel::moveAreaEarlier,
                onMoveAreaLater = viewModel::moveAreaLater,
                onSwapAreas = viewModel::swapAreas,
                onCreateArea = { draft ->
                    viewModel.createArea(
                        title = draft.title,
                        meaning = draft.meaning,
                        templateId = draft.templateId,
                        iconKey = draft.iconKey,
                        behaviorClass = draft.behaviorClass,
                        sourceKind = draft.sourceKind,
                    ) { areaId ->
                        navController.navigate(startAreaRoute(areaId))
                    }
                },
                onUpdateArea = { areaId, draft ->
                    viewModel.updateAreaIdentity(
                        areaId = areaId,
                        title = draft.title,
                        meaning = draft.meaning,
                        templateId = draft.templateId,
                        iconKey = draft.iconKey,
                    )
                },
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
                onOpenSourceSettings = { navController.navigate(AppDestination.SettingsSources.route) },
                onTargetScoreChange = viewModel::setTargetScore,
                onManualScoreChange = viewModel::setManualScore,
                onManualStateChange = viewModel::setManualState,
                onManualNoteChange = viewModel::setManualNote,
                onClearSnapshot = viewModel::clearSnapshot,
                onUpdateIdentity = viewModel::setAreaIdentity,
                onBindSource = viewModel::bindSource,
                onUnbindSource = viewModel::unbindSource,
                onCadenceChange = viewModel::setCadence,
                onIntensityChange = viewModel::setIntensity,
                onSignalBlendChange = viewModel::setSignalBlend,
                onLageModeChange = viewModel::setLageMode,
                onDirectionModeChange = viewModel::setDirectionMode,
                onSourcesModeChange = viewModel::setSourcesMode,
                onFlowProfileChange = viewModel::setFlowProfile,
                onComplexityLevelChange = viewModel::setComplexityLevel,
                onVisibilityLevelChange = viewModel::setVisibilityLevel,
                onToggleTrack = viewModel::toggleTrack,
                onPromoteTrack = viewModel::promoteTrack,
                onRemindersChange = viewModel::setRemindersEnabled,
                onReviewChange = viewModel::setReviewEnabled,
                onExperimentsChange = viewModel::setExperimentsEnabled,
            )
        }

        composable(AppDestination.Home.route) {
            val viewModel: SingleHomeViewModel = viewModel(
                factory = SingleHomeViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            SingleHomeScreen(
                state = state,
                onOpenStart = { navigateMode(AppDestination.Start.route) },
                onOpenMulti = { navigateMode(AppDestination.Multi.route) },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                onRefreshPassiveSignals = viewModel::refreshPassiveSignals,
                onSetHourSlotStatus = viewModel::setHourSlotStatus,
                onSaveHourSlotNote = viewModel::saveHourSlotNote,
            )
        }

        composable(AppDestination.Multi.route) {
            val viewModel: MultiViewModel = viewModel(
                factory = MultiViewModel.factory(appContainer),
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            MultiScreen(
                state = state,
                onOpenStart = { navigateMode(AppDestination.Start.route) },
                onOpenSingle = { navigateMode(AppDestination.Home.route) },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
            )
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
    }
}
