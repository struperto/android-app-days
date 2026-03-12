package com.struperto.androidappdays

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.struperto.androidappdays.navigation.AppNavHost

@Composable
fun AndroidAppDaysApp() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}
