package com.struperto.androidappdays.navigation

sealed class AppDestination(
    val route: String,
    val title: String,
) {
    data object Home : AppDestination("single_home", "Single")
    data object LifeWheel : AppDestination("single_life_wheel", "Lebensrad")
    data object WorkingSet : AppDestination("single_working_set", "Vorhaben")
    data object DaySchedule : AppDestination("single_day_schedule", "Zeitplan")
    data object Plan : AppDestination("single_plan", "Plan")
    data object Capture : AppDestination("single_capture", "Capture")
    data object Create : AppDestination("single_create", "Create")
    data object Settings : AppDestination("settings", "Einstellungen")
}
