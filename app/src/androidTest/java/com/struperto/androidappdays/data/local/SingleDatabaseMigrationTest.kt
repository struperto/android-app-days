package com.struperto.androidappdays.data.local

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SingleDatabaseMigrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "single-migration-8-12"

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrate8To12_copiesLegacyAreaRowsIntoKernelTablesAndRenamesLegacySeedIds() {
        context.deleteDatabase(databaseName)
        val openHelper = createVersion8OpenHelper()
        val database = openHelper.writableDatabase
        database.createVersion8Schema()
        database.seedLegacyAreaRows()

        MIGRATION_8_9.migrate(database)
        database.version = 9
        MIGRATION_9_10.migrate(database)
        database.version = 10
        database.seedVersion10LegacyAliasRows()
        MIGRATION_10_11.migrate(database)
        database.version = 11
        MIGRATION_11_12.migrate(database)
        database.version = 12
        database.close()
        openHelper.close()

        val migrated = Room.databaseBuilder(
            context,
            SingleDatabase::class.java,
            databaseName,
        ).addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
            .allowMainThreadQueries()
            .build()
        try {
            runBlocking {
                migrated.areaKernelDao().getAreaInstance("vitality").also { vitality ->
                    requireNotNull(vitality)
                    assertEquals("Vitalitaet", vitality.title)
                    assertEquals("Schlaf und Energie tragen.", vitality.summary)
                    assertEquals("daily", vitality.cadenceKey)
                    assertEquals("sleep,energy", vitality.selectedTracks)
                    assertEquals(75, vitality.signalBlend)
                    assertEquals(4, vitality.intensity)
                    assertTrue(vitality.remindersEnabled)
                    assertTrue(vitality.reviewEnabled)
                    assertFalse(vitality.experimentsEnabled)
                    assertEquals("score", vitality.lageMode)
                    assertEquals("balanced", vitality.directionMode)
                    assertEquals("signals", vitality.sourcesMode)
                    assertEquals("stable", vitality.flowProfile)
                    assertEquals("vitality", vitality.definitionId)
                    assertEquals("BASIC", vitality.authoringComplexity)
                    assertEquals("focused", vitality.authoringVisibility)
                    assertEquals("ritual", vitality.templateId)
                }

                migrated.areaKernelDao().getAreaInstance("custom").also { custom ->
                    requireNotNull(custom)
                    assertEquals("Eigener Bereich", custom.title)
                    assertEquals("adaptive", custom.cadenceKey)
                    assertEquals(60, custom.signalBlend)
                    assertEquals(3, custom.intensity)
                    assertFalse(custom.remindersEnabled)
                    assertTrue(custom.reviewEnabled)
                    assertEquals("state", custom.lageMode)
                    assertEquals("balanced", custom.directionMode)
                    assertEquals("curated", custom.sourcesMode)
                    assertEquals("stable", custom.flowProfile)
                    assertEquals("template:free", custom.definitionId)
                    assertEquals("ADVANCED", custom.authoringComplexity)
                    assertEquals("standard", custom.authoringVisibility)
                }

                migrated.areaKernelDao().getAreaInstance("discovery").also { discovery ->
                    requireNotNull(discovery)
                    assertEquals("Neugier alt", discovery.title)
                    assertEquals("Offen fuer neue Wege.", discovery.summary)
                    assertEquals("discovery", discovery.definitionId)
                    assertEquals("state", discovery.lageMode)
                    assertEquals("focus", discovery.directionMode)
                    assertEquals("curated", discovery.sourcesMode)
                    assertEquals("EXPERT", discovery.authoringComplexity)
                    assertEquals("expanded", discovery.authoringVisibility)
                }

                migrated.areaKernelDao().getAreaInstance("meaning").also { meaning ->
                    requireNotNull(meaning)
                    assertEquals("Sinn alt", meaning.title)
                    assertEquals("Mehr Stimmigkeit leben.", meaning.summary)
                    assertEquals("meaning", meaning.definitionId)
                    assertEquals("supportive", meaning.flowProfile)
                    assertEquals("EXPERT", meaning.authoringComplexity)
                    assertEquals("expanded", meaning.authoringVisibility)
                }

                assertEquals(null, migrated.areaKernelDao().getAreaInstance("curiosity"))
                assertEquals(null, migrated.areaKernelDao().getAreaInstance("purpose"))

                migrated.areaKernelDao()
                    .getAreaSnapshot("vitality", "2026-03-11")
                    .also { snapshot ->
                        requireNotNull(snapshot)
                        assertEquals(4, snapshot.manualScore)
                        assertEquals(null, snapshot.manualStateKey)
                        assertEquals(null, snapshot.confidence)
                        assertEquals(null, snapshot.freshnessAt)
                    }

                migrated.areaKernelDao()
                    .getAreaSnapshot("meaning", "2026-03-12")
                    .also { snapshot ->
                        requireNotNull(snapshot)
                        assertEquals(null, snapshot.manualScore)
                        assertEquals("stimmig", snapshot.manualStateKey)
                    }

                val readableDb = migrated.openHelper.readableDatabase
                assertEquals(0, readableDb.countRows("area_instances", "areaId", "curiosity"))
                assertEquals(0, readableDb.countRows("area_instances", "areaId", "purpose"))
                assertEquals(1, readableDb.countRows("capture_items", "areaId", "discovery"))
                assertEquals(1, readableDb.countRows("vorhaben", "areaId", "meaning"))
                assertEquals(1, readableDb.countRows("plan_items", "areaId", "meaning"))
                assertEquals(1, readableDb.countRows("life_areas", "id", "discovery"))
                assertEquals(1, readableDb.countRows("life_area_profiles", "areaId", "meaning"))
            }
        } finally {
            migrated.close()
        }
    }

    private fun createVersion8OpenHelper(): SupportSQLiteOpenHelper {
        return FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(8) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )
    }

    private fun SupportSQLiteDatabase.createVersion8Schema() {
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `capture_items` (
                `id` TEXT NOT NULL,
                `text` TEXT NOT NULL,
                `areaId` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `vorhaben` (
                `id` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `note` TEXT NOT NULL,
                `areaId` TEXT NOT NULL,
                `sourceCaptureId` TEXT,
                `status` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `plan_items` (
                `id` TEXT NOT NULL,
                `vorhabenId` TEXT,
                `title` TEXT NOT NULL,
                `note` TEXT NOT NULL,
                `areaId` TEXT NOT NULL,
                `timeBlock` TEXT NOT NULL,
                `plannedDate` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notification_signals` (
                `id` TEXT NOT NULL,
                `packageName` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `text` TEXT NOT NULL,
                `postedAt` INTEGER NOT NULL,
                `removedAt` INTEGER,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `user_fingerprint` (
                `id` INTEGER NOT NULL,
                `roles` TEXT NOT NULL,
                `responsibilities` TEXT NOT NULL,
                `priorityRules` TEXT NOT NULL,
                `weeklyRhythm` TEXT NOT NULL,
                `recurringCommitments` TEXT NOT NULL,
                `goodDayPattern` TEXT NOT NULL,
                `badDayPattern` TEXT NOT NULL,
                `dayStartHour` INTEGER NOT NULL,
                `dayEndHour` INTEGER NOT NULL,
                `morningEnergy` INTEGER NOT NULL,
                `afternoonEnergy` INTEGER NOT NULL,
                `eveningEnergy` INTEGER NOT NULL,
                `focusStrength` INTEGER NOT NULL,
                `disruptionSensitivity` INTEGER NOT NULL,
                `recoveryNeed` INTEGER NOT NULL,
                `discoveryCommitted` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `learning_events` (
                `id` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `detail` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `day` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `life_areas` (
                `id` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `definition` TEXT NOT NULL,
                `targetScore` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL,
                `templateId` TEXT NOT NULL,
                `iconKey` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `life_area_daily_checks` (
                `areaId` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `manualScore` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`areaId`, `date`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `life_area_profiles` (
                `areaId` TEXT NOT NULL,
                `cadence` TEXT NOT NULL,
                `intensity` INTEGER NOT NULL,
                `signalBlend` INTEGER NOT NULL,
                `selectedTracks` TEXT NOT NULL,
                `remindersEnabled` INTEGER NOT NULL,
                `reviewEnabled` INTEGER NOT NULL,
                `experimentsEnabled` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`areaId`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `single_setup_state` (
                `id` INTEGER NOT NULL,
                `isLifeWheelConfigured` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `domain_goals` (
                `id` TEXT NOT NULL,
                `domain` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `cadence` TEXT NOT NULL,
                `targetKind` TEXT NOT NULL,
                `unit` TEXT NOT NULL,
                `minimum` REAL,
                `maximum` REAL,
                `exact` REAL,
                `note` TEXT NOT NULL,
                `adaptationMode` TEXT NOT NULL,
                `preferredStartHour` INTEGER,
                `preferredEndHourExclusive` INTEGER,
                `priority` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL,
                `rationale` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `observation_events` (
                `id` TEXT NOT NULL,
                `goalId` TEXT,
                `domain` TEXT NOT NULL,
                `metric` TEXT NOT NULL,
                `source` TEXT NOT NULL,
                `startedAt` INTEGER NOT NULL,
                `endedAt` INTEGER,
                `numericValue` REAL,
                `booleanValue` INTEGER,
                `textValue` TEXT,
                `unit` TEXT,
                `logicalDate` TEXT,
                `sourceRecordId` TEXT,
                `confidence` REAL NOT NULL,
                `contextTags` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `domain_catalog` (
                `domain` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `summary` TEXT NOT NULL,
                `priority` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL,
                `isImplemented` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`domain`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `source_preferences` (
                `source` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `isEnabled` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`source`)
            )
            """.trimIndent(),
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `hour_slot_entries` (
                `id` TEXT NOT NULL,
                `logicalDate` TEXT NOT NULL,
                `segmentId` TEXT NOT NULL,
                `logicalHour` INTEGER NOT NULL,
                `windowId` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `note` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
    }

    private fun SupportSQLiteDatabase.seedLegacyAreaRows() {
        execSQL(
            """
            INSERT INTO `life_areas` (
                `id`, `label`, `definition`, `targetScore`, `sortOrder`, `isActive`,
                `templateId`, `iconKey`, `createdAt`, `updatedAt`
            ) VALUES
                ('vitality', 'Vitalitaet', 'Schlaf und Energie tragen.', 4, 0, 1, 'ritual', 'heart', 1000, 1100),
                ('custom', 'Eigener Bereich', 'Eigene Beschreibung', 3, 1, 1, 'free', 'spark', 1200, 1300)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `life_area_profiles` (
                `areaId`, `cadence`, `intensity`, `signalBlend`, `selectedTracks`,
                `remindersEnabled`, `reviewEnabled`, `experimentsEnabled`, `updatedAt`
            ) VALUES
                ('vitality', 'daily', 4, 75, 'sleep,energy', 1, 1, 0, 2100)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `life_area_daily_checks` (
                `areaId`, `date`, `manualScore`, `createdAt`, `updatedAt`
            ) VALUES
                ('vitality', '2026-03-11', 4, 3000, 3100)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `single_setup_state` (`id`, `isLifeWheelConfigured`, `updatedAt`)
            VALUES (0, 1, 4000)
            """.trimIndent(),
        )
    }

    private fun SupportSQLiteDatabase.seedVersion10LegacyAliasRows() {
        execSQL(
            """
            INSERT INTO `area_instances` (
                `areaId`, `title`, `summary`, `iconKey`, `targetScore`, `sortOrder`, `isActive`,
                `cadenceKey`, `selectedTracks`, `signalBlend`, `intensity`, `remindersEnabled`,
                `reviewEnabled`, `experimentsEnabled`, `lageMode`, `directionMode`, `sourcesMode`,
                `flowProfile`, `templateId`, `createdAt`, `updatedAt`
            ) VALUES
                ('curiosity', 'Neugier alt', 'Offen fuer neue Wege.', 'explore', 3, 2, 1, 'weekly', 'Orte,Menschen', 65, 3, 0, 1, 1, 'state', 'focus', 'curated', 'supportive', 'place', 5000, 5100),
                ('purpose', 'Sinn alt', 'Mehr Stimmigkeit leben.', 'compass', 4, 3, 1, 'weekly', 'Werte,Richtung', 55, 2, 1, 1, 0, 'state', 'focus', 'curated', 'supportive', 'theme', 5200, 5300),
                ('meaning', 'Sinn neu', 'Neuere Seed-Kopie.', 'compass', 4, 4, 1, 'adaptive', '', 60, 3, 0, 1, 0, 'score', 'balanced', 'curated', 'stable', 'theme', 5400, 5500)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `area_snapshots` (
                `areaId`, `date`, `manualScore`, `manualStateKey`, `confidence`, `freshnessAt`,
                `createdAt`, `updatedAt`
            ) VALUES
                ('curiosity', '2026-03-12', NULL, 'offen', 0.7, 6000, 6000, 6100),
                ('purpose', '2026-03-12', NULL, 'stimmig', 0.9, 6200, 6200, 6300),
                ('meaning', '2026-03-12', NULL, 'fern', 0.3, 6400, 6400, 6500)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `life_areas` (
                `id`, `label`, `definition`, `targetScore`, `sortOrder`, `isActive`,
                `templateId`, `iconKey`, `createdAt`, `updatedAt`
            ) VALUES
                ('curiosity', 'Neugier alt', 'Offen fuer neue Wege.', 3, 2, 1, 'place', 'explore', 5000, 5100),
                ('purpose', 'Sinn alt', 'Mehr Stimmigkeit leben.', 4, 3, 1, 'theme', 'compass', 5200, 5300),
                ('meaning', 'Sinn neu', 'Neuere Seed-Kopie.', 4, 4, 1, 'theme', 'compass', 5400, 5500)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `life_area_profiles` (
                `areaId`, `cadence`, `intensity`, `signalBlend`, `selectedTracks`,
                `remindersEnabled`, `reviewEnabled`, `experimentsEnabled`, `updatedAt`
            ) VALUES
                ('curiosity', 'weekly', 3, 65, 'Orte,Menschen', 0, 1, 1, 7000),
                ('purpose', 'weekly', 2, 55, 'Werte,Richtung', 1, 1, 0, 7100),
                ('meaning', 'adaptive', 3, 60, '', 0, 1, 0, 7200)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `life_area_daily_checks` (
                `areaId`, `date`, `manualScore`, `createdAt`, `updatedAt`
            ) VALUES
                ('curiosity', '2026-03-12', 4, 7300, 7400),
                ('purpose', '2026-03-12', 5, 7500, 7600),
                ('meaning', '2026-03-12', 2, 7700, 7800)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `capture_items` (
                `id`, `text`, `areaId`, `createdAt`, `updatedAt`, `status`
            ) VALUES
                ('capture-discovery', 'Alt verknuepfter Fund', 'curiosity', 8000, 8100, 'open')
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `vorhaben` (
                `id`, `title`, `note`, `areaId`, `sourceCaptureId`, `status`, `createdAt`, `updatedAt`
            ) VALUES
                ('vorhaben-meaning', 'Sinn pflegen', 'Alter Alias', 'purpose', NULL, 'active', 8200, 8300)
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO `plan_items` (
                `id`, `vorhabenId`, `title`, `note`, `areaId`, `timeBlock`, `plannedDate`,
                `status`, `createdAt`, `updatedAt`
            ) VALUES
                ('plan-meaning', NULL, 'Anker setzen', 'Alter Alias', 'purpose', 'morgen', '2026-03-12', 'open', 8400, 8500)
            """.trimIndent(),
        )
    }

    private fun SupportSQLiteDatabase.countRows(
        table: String,
        column: String,
        value: String,
    ): Int {
        query(
            SimpleSQLiteQuery(
                """
                SELECT COUNT(*)
                FROM `$table`
                WHERE `$column` = ?
                """.trimIndent(),
                arrayOf(value),
            ),
        ).use { cursor ->
            check(cursor.moveToFirst())
            return cursor.getInt(0)
        }
    }
}
