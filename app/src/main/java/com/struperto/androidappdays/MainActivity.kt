package com.struperto.androidappdays

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DebugHomeWindowBus
import com.struperto.androidappdays.navigation.LaunchRouteBus
import com.struperto.androidappdays.testing.MvpTestPersonas
import com.struperto.androidappdays.ui.theme.DaysTheme
import java.time.LocalDate
import kotlinx.coroutines.launch

private const val ExtraDebugPersonaId = "debug_persona_id"
private const val ExtraDebugWindowId = "debug_window_id"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DaysTheme {
                AndroidAppDaysApp()
            }
        }
        handleDebugIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDebugIntent(intent)
    }

    private fun handleDebugIntent(intent: Intent?) {
        val personaId = intent?.getStringExtra(ExtraDebugPersonaId)
        val windowId = intent?.getStringExtra(ExtraDebugWindowId)
        if (personaId.isNullOrBlank() && windowId.isNullOrBlank()) {
            return
        }

        lifecycleScope.launch {
            val app = application as DaysApp
            if (!windowId.isNullOrBlank()) {
                DebugHomeWindowBus.show(windowId)
            }
            if (!personaId.isNullOrBlank()) {
                val persona = MvpTestPersonas.all.firstOrNull { it.id == personaId } ?: return@launch
                app.appContainer.mvpPersonaScenarioRunner.playPersona(
                    persona = persona,
                    baseDate = LocalDate.of(2026, 3, 8),
                )
            }
            LaunchRouteBus.open(AppDestination.Home.route)
        }
    }
}
