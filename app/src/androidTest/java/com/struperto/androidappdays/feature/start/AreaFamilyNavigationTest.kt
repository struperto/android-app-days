package com.struperto.androidappdays.feature.start

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.struperto.androidappdays.DaysApp
import com.struperto.androidappdays.MainActivity
import com.struperto.androidappdays.data.repository.AreaSourceBinding
import com.struperto.androidappdays.data.repository.LifeArea
import com.struperto.androidappdays.domain.DataSourceKind
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AreaFamilyNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetAndSeedFamilyAreas() = runBlocking {
        val container = app().appContainer
        container.areaSourceBindingRepository.clearAll()
        container.areaWebFeedSourceRepository.loadAll().forEach { source ->
            container.areaWebFeedSourceRepository.remove(source.areaId, source.url)
        }
        container.lifeWheelRepository.completeSetup(familyAreas())
        familyBindings().forEach { binding ->
            container.areaSourceBindingRepository.bind(
                areaId = binding.areaId,
                source = binding.source,
            )
        }
        container.captureRepository.observeOpen().first().forEach { capture ->
            container.captureRepository.archive(capture.id)
        }
    }

    @Test
    fun personContact_inputsUseCustomRows() {
        openArea("contact-priority")

        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")
        composeRule.onNodeWithText("Messenger").assertIsDisplayed()
        composeRule.onNodeWithText("Notizen").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshots").assertIsDisplayed()
        composeRule.onNodeWithText("Bestand").assertIsDisplayed()
    }

    @Test
    fun personContact_focusUsesCustomRows() {
        openArea("contact-priority")
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("person-contact-row-personen").assertIsDisplayed()
        composeRule.onNodeWithTag("person-contact-row-auswahl").assertIsDisplayed()
        composeRule.onNodeWithTag("person-contact-row-antworten").assertIsDisplayed()
        composeRule.onNodeWithTag("person-contact-row-naehe").assertIsDisplayed()
    }

    @Test
    fun personContact_taktUsesCustomRows() {
        openArea("contact-priority")
        composeRule.onNodeWithTag("area-entry-flow").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("person-contact-row-rhythmus").assertIsDisplayed()
        composeRule.onNodeWithTag("person-contact-row-rueckkehr").assertIsDisplayed()
        composeRule.onNodeWithTag("person-contact-row-signale").assertIsDisplayed()
        composeRule.onNodeWithTag("person-contact-row-schalter").assertIsDisplayed()
        saveTaggedNode("area-panel-screen", "person-takt-panel.png")
    }

    @Test
    fun projectWork_inputsUseCustomRows() {
        openArea("app-bau")

        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")
        composeRule.onNodeWithText("Verbindung").assertIsDisplayed()
        composeRule.onNodeWithText("Material").assertIsDisplayed()
        composeRule.onNodeWithText("Dateien").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshots").assertIsDisplayed()
        composeRule.onNodeWithText("Bestand").assertIsDisplayed()
    }

    @Test
    fun projectWork_focusUsesCustomRows() {
        openArea("app-bau")
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("project-work-row-vorne").assertIsDisplayed()
        composeRule.onNodeWithTag("project-work-row-auswahl").assertIsDisplayed()
        composeRule.onNodeWithTag("project-work-row-ordnung").assertIsDisplayed()
        composeRule.onNodeWithTag("project-work-row-fenster").assertIsDisplayed()
        saveTaggedNode("area-panel-screen", "project-focus-panel.png")
    }

    @Test
    fun projectWork_taktUsesCustomRows() {
        openArea("app-bau")
        composeRule.onNodeWithTag("area-entry-flow").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("project-work-row-rhythmus").assertIsDisplayed()
        composeRule.onNodeWithTag("project-work-row-zugkraft").assertIsDisplayed()
        composeRule.onNodeWithTag("project-work-row-wiedervorlage").assertIsDisplayed()
        composeRule.onNodeWithTag("project-work-row-schalter").assertIsDisplayed()
    }

    @Test
    fun placeContext_inputsUseCustomRows() {
        openArea("wege")
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")
        composeRule.onNodeWithText("Standort").assertIsDisplayed()
        composeRule.onNodeWithText("Wege").assertIsDisplayed()
        composeRule.onNodeWithText("Orte").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshots").assertIsDisplayed()
        composeRule.onNodeWithText("Bestand").assertIsDisplayed()
    }

    @Test
    fun placeContext_focusUsesCustomRows() {
        openArea("wege")
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("place-context-row-orte").assertIsDisplayed()
        composeRule.onNodeWithTag("place-context-row-auswahl").assertIsDisplayed()
        composeRule.onNodeWithTag("place-context-row-ausloeser").assertIsDisplayed()
        composeRule.onNodeWithTag("place-context-row-fenster").assertIsDisplayed()
        saveTaggedNode("area-panel-screen", "place-focus-panel.png")
    }

    @Test
    fun placeContext_taktUsesCustomRows() {
        openArea("wege")
        composeRule.onNodeWithTag("area-entry-flow").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("place-context-row-rhythmus").assertIsDisplayed()
        composeRule.onNodeWithTag("place-context-row-rueckkehr").assertIsDisplayed()
        composeRule.onNodeWithTag("place-context-row-ortssignal").assertIsDisplayed()
        composeRule.onNodeWithTag("place-context-row-schalter").assertIsDisplayed()
    }

    @Test
    fun placeContext_editorShowsAusloeserOptions() {
        openArea("wege")
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("place-context-row-ausloeser").performClick()
        waitForTag("panel-action-editor-screen")
        composeRule.onNodeWithText("Ort").assertIsDisplayed()
        composeRule.onNodeWithText("Ausgewogen").assertIsDisplayed()
        composeRule.onNodeWithText("Zeitnah").assertIsDisplayed()
    }

    @Test
    fun healthRitual_inputsUseCustomRows() {
        openArea("gesundheit")
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")
        composeRule.onNodeWithText("Health").assertIsDisplayed()
        composeRule.onNodeWithText("Notizen").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshots").assertIsDisplayed()
        composeRule.onNodeWithText("Bestand").assertIsDisplayed()
    }

    @Test
    fun healthRitual_focusUsesCustomRows() {
        openArea("gesundheit")
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("health-ritual-row-vorne").assertIsDisplayed()
        composeRule.onNodeWithTag("health-ritual-row-auswahl").assertIsDisplayed()
        composeRule.onNodeWithTag("health-ritual-row-deutung").assertIsDisplayed()
        composeRule.onNodeWithTag("health-ritual-row-fenster").assertIsDisplayed()
    }

    @Test
    fun healthRitual_taktUsesCustomRows() {
        openArea("gesundheit")
        composeRule.onNodeWithTag("area-entry-flow").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("health-ritual-row-rhythmus").assertIsDisplayed()
        composeRule.onNodeWithTag("health-ritual-row-dichte").assertIsDisplayed()
        composeRule.onNodeWithTag("health-ritual-row-messung").assertIsDisplayed()
        composeRule.onNodeWithTag("health-ritual-row-schalter").assertIsDisplayed()
        saveTaggedNode("area-panel-screen", "health-takt-panel.png")
    }

    @Test
    fun healthRitual_editorShowsRhythmusOptions() {
        openArea("gesundheit")
        composeRule.onNodeWithTag("area-entry-flow").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("health-ritual-row-rhythmus").performClick()
        waitForTag("panel-action-editor-screen")
        composeRule.onNodeWithText("Ruhig").assertIsDisplayed()
        composeRule.onNodeWithText("Tragend").assertIsDisplayed()
        composeRule.onNodeWithText("Klar").assertIsDisplayed()
    }

    @Test
    fun collectionInbox_inputsUseCustomRows() {
        openArea("inbox")
        composeRule.onNodeWithTag("area-entry-inputs").performClick()
        waitForTag("area-inputs-screen")
        composeRule.onNodeWithText("Links").assertIsDisplayed()
        composeRule.onNodeWithText("Notizen").assertIsDisplayed()
        composeRule.onNodeWithText("Dateien").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshots").assertIsDisplayed()
        composeRule.onNodeWithText("Bestand").assertIsDisplayed()
    }

    @Test
    fun collectionInbox_focusUsesCustomRows() {
        openArea("inbox")
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("collection-inbox-row-vorne").assertIsDisplayed()
        composeRule.onNodeWithTag("collection-inbox-row-auswahl").assertIsDisplayed()
        composeRule.onNodeWithTag("collection-inbox-row-ordnung").assertIsDisplayed()
        composeRule.onNodeWithTag("collection-inbox-row-fenster").assertIsDisplayed()
    }

    @Test
    fun collectionInbox_taktUsesCustomRows() {
        openArea("inbox")
        composeRule.onNodeWithTag("area-entry-flow").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("collection-inbox-row-rhythmus").assertIsDisplayed()
        composeRule.onNodeWithTag("collection-inbox-row-leeren").assertIsDisplayed()
        composeRule.onNodeWithTag("collection-inbox-row-wiedervorlage").assertIsDisplayed()
        composeRule.onNodeWithTag("collection-inbox-row-schalter").assertIsDisplayed()
    }

    @Test
    fun collectionInbox_editorShowsOrdnungOptions() {
        openArea("inbox")
        composeRule.onNodeWithTag("area-entry-goal").performClick()
        waitForTag("area-panel-screen")
        composeRule.onNodeWithTag("collection-inbox-row-ordnung").performClick()
        waitForTag("panel-action-editor-screen")
        composeRule.onNodeWithText("Klar").assertIsDisplayed()
        composeRule.onNodeWithText("Offen").assertIsDisplayed()
        composeRule.onNodeWithText("Frisch").assertIsDisplayed()
        saveTaggedNode("panel-action-editor-screen", "inbox-ordnung-editor.png")
    }

    private fun openArea(areaId: String) {
        waitForTag("start-area-$areaId")
        composeRule.onNodeWithTag("start-area-$areaId").performClick()
        waitForTag("area-studio-screen")
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(tag).assertIsDisplayed()
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

    private fun app(): DaysApp {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as DaysApp
    }
}

private fun familyAreas(): List<LifeArea> {
    return listOf(
        familyArea(
            id = "learning",
            label = "News",
            definition = "Links und News ruhig sammeln und nur das Wesentliche lesen.",
            templateId = "medium",
            iconKey = "book",
        ),
        familyArea(
            id = "contact-priority",
            label = "Kontakt Prioritaet",
            definition = "Wichtige Menschen und offene Antworten nicht uebersehen.",
            templateId = "person",
            iconKey = "chat",
            targetScore = 4,
        ),
        familyArea(
            id = "app-bau",
            label = "App-Bau",
            definition = "Material, naechste Schritte und Fortschritt fuer den App-Bau zusammenhalten.",
            templateId = "project",
            iconKey = "briefcase",
            targetScore = 4,
        ),
        familyArea(
            id = "wege",
            label = "Orte & Wege",
            definition = "Orte, Wege und Kontexte ruhig begleiten, damit sie im Alltag brauchbar werden.",
            templateId = "place",
            iconKey = "home",
        ),
        familyArea(
            id = "gesundheit",
            label = "Gesundheit",
            definition = "Signale, Wirkung und kleine Rituale lesbar halten, ohne in Zahlen zu kippen.",
            templateId = "ritual",
            iconKey = "heart",
            targetScore = 4,
        ),
        familyArea(
            id = "inbox",
            label = "Inbox",
            definition = "Lose Links, Notizen und Ideen erst sammeln und spaeter sauber sortieren.",
            templateId = "free",
            iconKey = "spark",
        ),
    ).mapIndexed { index, area -> area.copy(sortOrder = index) }
}

private fun familyBindings(): List<AreaSourceBinding> {
    return listOf(
        AreaSourceBinding("contact-priority", DataSourceKind.NOTIFICATIONS),
        AreaSourceBinding("app-bau", DataSourceKind.CALENDAR),
        AreaSourceBinding("wege", DataSourceKind.MANUAL),
        AreaSourceBinding("gesundheit", DataSourceKind.HEALTH_CONNECT),
    )
}

private fun familyArea(
    id: String,
    label: String,
    definition: String,
    templateId: String,
    iconKey: String,
    targetScore: Int = 3,
): LifeArea {
    return LifeArea(
        id = id,
        label = label,
        definition = definition,
        targetScore = targetScore,
        sortOrder = 0,
        isActive = true,
        templateId = templateId,
        iconKey = iconKey,
    )
}
