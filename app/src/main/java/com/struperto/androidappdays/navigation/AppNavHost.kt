package com.struperto.androidappdays.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.struperto.androidappdays.feature.settings.SettingsScreen
import com.struperto.androidappdays.feature.single.capture.CaptureScreen
import com.struperto.androidappdays.feature.single.create.CreateScreen
import com.struperto.androidappdays.feature.single.home.SingleHomeScreen
import com.struperto.androidappdays.feature.single.lifewheel.LifeWheelScreen
import com.struperto.androidappdays.feature.single.model.previewSingleHomeState
import com.struperto.androidappdays.feature.single.plan.PlanScreen
import com.struperto.androidappdays.feature.single.schedule.DayScheduleScreen
import com.struperto.androidappdays.feature.single.workingset.WorkingSetScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Home.route) {
            SingleHomeScreen(
                state = previewSingleHomeState(),
                onOpenAction = { route -> navController.navigate(route) },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
            )
        }
        composable(AppDestination.LifeWheel.route) {
            LifeWheelScreen(onBack = navController::popBackStack)
        }
        composable(AppDestination.WorkingSet.route) {
            WorkingSetScreen(onBack = navController::popBackStack)
        }
        composable(AppDestination.DaySchedule.route) {
            DayScheduleScreen(onBack = navController::popBackStack)
        }
        composable(AppDestination.Plan.route) {
            PlanScreen(onBack = navController::popBackStack)
        }
        composable(AppDestination.Capture.route) {
            CaptureScreen(onBack = navController::popBackStack)
        }
        composable(AppDestination.Create.route) {
            CreateScreen(onBack = navController::popBackStack)
        }
        composable(AppDestination.Settings.route) {
            SettingsScreen(onBack = navController::popBackStack)
        }
    }
}
