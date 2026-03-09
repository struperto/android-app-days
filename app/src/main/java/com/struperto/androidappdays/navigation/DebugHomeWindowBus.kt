package com.struperto.androidappdays.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DebugHomeWindowBus {
    private val _windowId = MutableStateFlow<String?>(null)
    val windowId: StateFlow<String?> = _windowId

    fun show(windowId: String?) {
        _windowId.value = windowId
    }

    fun clear() {
        _windowId.value = null
    }
}
