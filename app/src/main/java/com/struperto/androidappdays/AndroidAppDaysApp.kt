package com.struperto.androidappdays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.struperto.androidappdays.navigation.AppNavHost
import com.struperto.androidappdays.navigation.LaunchRouteBus
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AndroidAppDaysApp() {
    val navController = rememberNavController()
    LaunchedEffect(navController) {
        LaunchRouteBus.pendingRoute.collectLatest { route ->
            if (route.isNullOrBlank()) {
                return@collectLatest
            }
            navController.navigate(route) {
                launchSingleTop = true
            }
            LaunchRouteBus.clear()
        }
    }
    AppNavHost(navController = navController)
}
