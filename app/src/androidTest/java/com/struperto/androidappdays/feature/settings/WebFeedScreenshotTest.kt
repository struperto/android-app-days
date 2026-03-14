package com.struperto.androidappdays.feature.settings

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.MainActivity
import com.struperto.androidappdays.feature.start.defaultWebFeedSyncCadence
import com.struperto.androidappdays.feature.start.inferWebFeedSourceKind
import com.struperto.androidappdays.feature.start.startSeedAreas
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebFeedScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetState() = runBlocking {
        app().appContainer.areaSourceBindingRepository.clearAll()
        app().appContainer.areaWebFeedSourceRepository.loadAll().forEach { source ->
            app().appContainer.areaWebFeedSourceRepository.remove(source.areaId, source.url)
        }
        app().appContainer.lifeWheelRepository.completeSetup(emptyList())
        app().appContainer.captureRepository.observeOpen().first().forEach { capture ->
            app().appContainer.captureRepository.archive(capture.id)
        }
    }

    @Test
    fun captureSettingsFeedsScreen() {
        seedAreaAndFeed()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("start-open-settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("start-open-settings").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-menu-feeds").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings-menu-feeds").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings-feeds-content").fetchSemanticsNodes().isNotEmpty()
        }

        val bitmap = composeRule.onNodeWithTag("settings-feeds-content").captureToImage().asAndroidBitmap()
        val target = File(
            requireNotNull(composeRule.activity.getExternalFilesDir(null)),
            "web-feed-settings-screen.png",
        )
        FileOutputStream(target).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    private fun seedAreaAndFeed() = runBlocking {
        val area = startSeedAreas().first { it.id == "clarity" }.copy(sortOrder = 0)
        app().appContainer.lifeWheelRepository.completeSetup(listOf(area))
        val url = "https://developer.android.com/feeds/androidx-release-notes.xml"
        val kind = inferWebFeedSourceKind(url)
        app().appContainer.areaWebFeedSourceRepository.save(
            areaId = "clarity",
            url = url,
            sourceKind = kind,
            isAutoSyncEnabled = true,
            syncCadence = defaultWebFeedSyncCadence(kind),
        )
        app().appContainer.areaWebFeedSourceRepository.updateSyncResult(
            areaId = "clarity",
            url = url,
            syncedAt = System.currentTimeMillis(),
            statusLabel = "2 neu",
            statusDetail = "AndroidX Release Notes wurden gelesen.",
        )
    }

    private fun app(): DaysApp {
        return composeRule.activity.application as DaysApp
    }
}
