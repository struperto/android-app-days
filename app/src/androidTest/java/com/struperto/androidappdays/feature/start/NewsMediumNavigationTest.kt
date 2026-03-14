package com.struperto.androidappdays.feature.start

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.MainActivity
import com.struperto.androidappdays.data.repository.AreaWebFeedSyncCadence
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsMediumNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetAndSeedLearningArea() = runBlocking {
        val container = app().appContainer
        container.areaSourceBindingRepository.clearAll()
        container.areaWebFeedSourceRepository.loadAll().forEach { source ->
            container.areaWebFeedSourceRepository.remove(source.areaId, source.url)
        }
        container.lifeWheelRepository.completeSetup(
            startSeedAreas()
                .filter { it.id == "learning" }
                .mapIndexed { index, area -> area.copy(sortOrder = index) },
        )
        container.captureRepository.observeOpen().first().forEach { capture ->
            container.captureRepository.archive(capture.id)
        }
    }

    @Test
    fun newsMedium_inputsUseNewsSpecificRows() {
        openLearningArea()
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")
        composeRule.onNodeWithText("Web").assertIsDisplayed()
        composeRule.onNodeWithText("Feeds").assertIsDisplayed()
        composeRule.onNodeWithText("Social Text").assertIsDisplayed()
        composeRule.onNodeWithText("Social Bild").assertIsDisplayed()
        composeRule.onNodeWithText("Video").assertIsDisplayed()
        composeRule.onNodeWithTag("area-input-overview-web").assertIsDisplayed()
        composeRule.onNodeWithTag("area-input-overview-feeds").assertIsDisplayed()
        composeRule.onNodeWithTag("area-input-overview-social-text").assertIsDisplayed()
        composeRule.onNodeWithTag("area-input-overview-social-image").assertIsDisplayed()
        composeRule.onNodeWithTag("area-input-overview-video").assertIsDisplayed()
        composeRule.onNodeWithTag("area-input-overview-screenshots").assertIsDisplayed()
        saveTaggedNode("area-inputs-screen", "news-input-sources.png")
    }

    @Test
    fun newsMedium_xEntryOpensInRealTime() {
        openLearningArea()
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")

        val elapsedMs = measureUntilTagAppears(
            triggerTag = "area-input-overview-social-text",
            targetTag = "area-news-source-social-text-screen",
        )

        composeRule.onNodeWithTag("area-news-source-input").assertIsDisplayed()
        assert(elapsedMs < 1_500L) {
            "X-Eingabe braucht zu lange: ${elapsedMs}ms"
        }
    }

    @Test
    fun newsMedium_fazEntryOpensInRealTime() {
        openLearningArea()
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")

        val elapsedMs = measureUntilTagAppears(
            triggerTag = "area-input-overview-feeds",
            targetTag = "area-news-source-feeds-screen",
        )

        composeRule.onNodeWithTag("area-news-source-input").assertIsDisplayed()
        assert(elapsedMs < 1_500L) {
            "FAZ-Eingabe braucht zu lange: ${elapsedMs}ms"
        }
    }

    @Test
    fun newsMedium_statusListsAddedSourcesAndOpensAddFlow() {
        runBlocking {
            seedAllNewsSources()
        }
        openLearningArea()
        composeRule.onNodeWithTag("area-entry-stand").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("news-medium-row-feeds").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-web").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-social-text").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-social-bild").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-video").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-screenshots").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-status-add").performClick()
        waitForTag("area-inputs-screen")
    }

    @Test
    fun newsMedium_feedDetailShowsMultipleFeeds() {
        runBlocking {
            val container = app().appContainer
            container.areaWebFeedSourceRepository.save(
                areaId = "learning",
                url = "https://www.faz.net/rss/aktuell/",
                syncCadence = AreaWebFeedSyncCadence.SixHours,
            )
            container.areaWebFeedSourceRepository.save(
                areaId = "learning",
                url = "https://www.stol.it/",
                syncCadence = AreaWebFeedSyncCadence.Daily,
            )
        }
        openLearningArea()
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")
        composeRule.onNodeWithTag("area-input-overview-feeds").performClick()
        waitForTag("area-news-source-feeds-screen")
        composeRule.onNodeWithTag("area-news-source-feed-0").assertIsDisplayed()
        composeRule.onNodeWithTag("area-news-source-feed-1").assertIsDisplayed()
    }

    @Test
    fun newsMedium_focusUsesNewsSpecificRows() {
        openLearningArea()
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("news-medium-row-zuerst").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-mitlesen").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-sortierung").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-zeitraum").assertIsDisplayed()
    }

    @Test
    fun newsMedium_taktUsesNewsSpecificRows() {
        openLearningArea()
        composeRule.onNodeWithTag("area-entry-flow").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("news-medium-row-stil").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-dichte").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-nachladen").assertIsDisplayed()
        composeRule.onNodeWithTag("news-medium-row-wiederkehr").assertIsDisplayed()
    }

    private fun openLearningArea() {
        waitForTag("start-area-learning")
        composeRule.onNodeWithTag("start-area-learning").performClick()
        waitForTag("area-studio-screen")
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(tag).assertIsDisplayed()
    }

    private fun measureUntilTagAppears(
        triggerTag: String,
        targetTag: String,
    ): Long {
        val startedAt = SystemClock.elapsedRealtime()
        composeRule.onNodeWithTag(triggerTag).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(targetTag).fetchSemanticsNodes().isNotEmpty()
        }
        return SystemClock.elapsedRealtime() - startedAt
    }

    private fun app(): DaysApp {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp
    }

    private suspend fun seedAllNewsSources() {
        val container = app().appContainer
        container.areaWebFeedSourceRepository.save(
            areaId = "learning",
            url = "https://www.faz.net/rss/aktuell/",
            syncCadence = AreaWebFeedSyncCadence.SixHours,
        )
        container.areaWebFeedSourceRepository.save(
            areaId = "learning",
            url = "https://www.stol.it/",
            syncCadence = AreaWebFeedSyncCadence.Daily,
        )
        container.captureRepository.createTextCapture(
            text = buildAreaImportCaptureText(
                kind = AreaImportKind.Link,
                title = "FAZ Web",
                detail = "Artikel-Link",
                reference = "https://www.faz.net/aktuell/politik/beispiel.html",
            ),
            areaId = "learning",
        )
        container.captureRepository.createTextCapture(
            text = buildAreaImportCaptureText(
                kind = AreaImportKind.Link,
                title = "X Post",
                detail = "Social Text",
                reference = "https://x.com/openai/status/123",
            ),
            areaId = "learning",
        )
        container.captureRepository.createTextCapture(
            text = buildAreaImportCaptureText(
                kind = AreaImportKind.Link,
                title = "Instagram Post",
                detail = "Social Bild",
                reference = "https://www.instagram.com/p/abc123/",
            ),
            areaId = "learning",
        )
        container.captureRepository.createTextCapture(
            text = buildAreaImportCaptureText(
                kind = AreaImportKind.Link,
                title = "YouTube Video",
                detail = "Video",
                reference = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            ),
            areaId = "learning",
        )
        container.captureRepository.createTextCapture(
            text = buildAreaImportCaptureText(
                kind = AreaImportKind.Image,
                title = "Screenshot",
                detail = "Gesicherter Screen",
                reference = "content://android-app-days/screenshots/1",
            ),
            areaId = "learning",
        )
    }

    private fun saveTaggedNode(tag: String, fileName: String) {
        val bitmap = composeRule.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
        val target = File(composeRule.activity.filesDir, fileName)
        FileOutputStream(target).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        saveBitmapToPublicPictures(bitmap, fileName)
    }

    private fun saveBitmapToPublicPictures(bitmap: Bitmap, fileName: String) {
        val resolver = composeRule.activity.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/android-app-days")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
    }
}
