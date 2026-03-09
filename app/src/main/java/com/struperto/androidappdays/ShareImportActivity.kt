package com.struperto.androidappdays

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.struperto.androidappdays.data.repository.LearningEventType
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.LaunchRouteBus
import kotlinx.coroutines.launch

class ShareImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = extractSharedText(intent)
        lifecycleScope.launch {
            if (!sharedText.isNullOrBlank()) {
                val appContainer = (application as DaysApp).appContainer
                appContainer.captureRepository.createTextCapture(
                    text = sharedText,
                    areaId = null,
                )
                appContainer.learningEventRepository.record(
                    type = LearningEventType.CAPTURE_SAVED,
                    title = "Text geteilt",
                    detail = sharedText.take(160),
                )
            }
            LaunchRouteBus.open(AppDestination.Home.route)
            startActivity(
                Intent(this@ShareImportActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
            )
            finish()
        }
    }
}

private fun extractSharedText(intent: Intent?): String? {
    if (intent?.action != Intent.ACTION_SEND) {
        return null
    }
    val type = intent.type ?: return null
    if (!type.startsWith("text/")) {
        return null
    }
    val charSequence = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
    if (!charSequence.isNullOrBlank()) {
        return charSequence.toString().trim()
    }
    val text = intent.getStringExtra(Intent.EXTRA_TEXT)
    return text?.trim().takeUnless { it.isNullOrBlank() }
}
