package com.struperto.androidappdays.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object LaunchRouteBus {
    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute = _pendingRoute.asStateFlow()

    fun open(route: String) {
        _pendingRoute.value = route
    }

    fun clear() {
        _pendingRoute.value = null
    }
}
