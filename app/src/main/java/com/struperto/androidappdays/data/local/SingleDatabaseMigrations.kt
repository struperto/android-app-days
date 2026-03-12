package com.struperto.androidappdays.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.struperto.androidappdays.domain.area.legacyStartAreaIdAliases

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `area_instances` (
                `areaId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `summary` TEXT NOT NULL,
                `iconKey` TEXT NOT NULL,
                `targetScore` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL,
                `cadenceKey` TEXT NOT NULL,
                `selectedTracks` TEXT NOT NULL,
                `signalBlend` INTEGER NOT NULL,
                `intensity` INTEGER NOT NULL,
                `remindersEnabled` INTEGER NOT NULL,
                `reviewEnabled` INTEGER NOT NULL,
                `experimentsEnabled` INTEGER NOT NULL,
                `templateId` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`areaId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_area_instances_isActive_sortOrder`
            ON `area_instances` (`isActive`, `sortOrder`)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `area_snapshots` (
                `areaId` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `manualScore` INTEGER,
                `manualStateKey` TEXT,
                `confidence` REAL,
                `freshnessAt` INTEGER,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`areaId`, `date`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_area_snapshots_date`
            ON `area_snapshots` (`date`)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR REPLACE INTO `area_instances` (
                `areaId`,
                `title`,
                `summary`,
                `iconKey`,
                `targetScore`,
                `sortOrder`,
                `isActive`,
                `cadenceKey`,
                `selectedTracks`,
                `signalBlend`,
                `intensity`,
                `remindersEnabled`,
                `reviewEnabled`,
                `experimentsEnabled`,
                `templateId`,
                `createdAt`,
                `updatedAt`
            )
            SELECT
                `life_areas`.`id`,
                `life_areas`.`label`,
                `life_areas`.`definition`,
                `life_areas`.`iconKey`,
                `life_areas`.`targetScore`,
                `life_areas`.`sortOrder`,
                `life_areas`.`isActive`,
                COALESCE(`life_area_profiles`.`cadence`, 'adaptive'),
                COALESCE(`life_area_profiles`.`selectedTracks`, ''),
                COALESCE(`life_area_profiles`.`signalBlend`, 60),
                COALESCE(`life_area_profiles`.`intensity`, 3),
                COALESCE(`life_area_profiles`.`remindersEnabled`, 0),
                COALESCE(`life_area_profiles`.`reviewEnabled`, 1),
                COALESCE(`life_area_profiles`.`experimentsEnabled`, 0),
                `life_areas`.`templateId`,
                `life_areas`.`createdAt`,
                CASE
                    WHEN `life_area_profiles`.`updatedAt` IS NOT NULL
                        AND `life_area_profiles`.`updatedAt` > `life_areas`.`updatedAt`
                    THEN `life_area_profiles`.`updatedAt`
                    ELSE `life_areas`.`updatedAt`
                END
            FROM `life_areas`
            LEFT JOIN `life_area_profiles`
                ON `life_area_profiles`.`areaId` = `life_areas`.`id`
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR REPLACE INTO `area_snapshots` (
                `areaId`,
                `date`,
                `manualScore`,
                `manualStateKey`,
                `confidence`,
                `freshnessAt`,
                `createdAt`,
                `updatedAt`
            )
            SELECT
                `areaId`,
                `date`,
                `manualScore`,
                NULL,
                NULL,
                NULL,
                `createdAt`,
                `updatedAt`
            FROM `life_area_daily_checks`
            """.trimIndent(),
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `lageMode` TEXT NOT NULL DEFAULT 'score'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `directionMode` TEXT NOT NULL DEFAULT 'balanced'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `sourcesMode` TEXT NOT NULL DEFAULT 'balanced'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `flowProfile` TEXT NOT NULL DEFAULT 'stable'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE `area_instances`
            SET
                `lageMode` = CASE
                    WHEN `areaId` IN ('friends', 'recovery', 'discovery', 'meaning', 'curiosity', 'purpose') THEN 'state'
                    WHEN `templateId` IN ('project', 'ritual', 'place', 'feeling') THEN 'score'
                    ELSE 'state'
                END,
                `directionMode` = CASE
                    WHEN `areaId` IN ('vitality', 'recovery') THEN 'balanced'
                    WHEN `areaId` IN ('clarity', 'impact', 'home', 'learning') THEN 'rhythm'
                    WHEN `areaId` IN ('friends', 'bond', 'community', 'joy', 'discovery', 'meaning', 'curiosity', 'purpose') THEN 'focus'
                    WHEN `templateId` = 'project' THEN 'focus'
                    WHEN `templateId` IN ('ritual', 'place', 'feeling') THEN 'rhythm'
                    ELSE 'balanced'
                END,
                `sourcesMode` = CASE
                    WHEN `areaId` IN ('vitality', 'clarity') THEN 'signals'
                    WHEN `areaId` IN ('friends', 'learning', 'discovery', 'meaning', 'curiosity', 'purpose') THEN 'curated'
                    WHEN `templateId` IN ('ritual', 'place', 'feeling') THEN 'signals'
                    WHEN `templateId` = 'project' THEN 'curated'
                    ELSE 'curated'
                END,
                `flowProfile` = CASE
                    WHEN `areaId` IN ('vitality', 'recovery') THEN 'stable'
                    WHEN `areaId` IN ('clarity', 'impact', 'home', 'learning') THEN 'active'
                    WHEN `areaId` IN ('friends', 'bond', 'community', 'joy', 'discovery', 'meaning', 'purpose') THEN 'supportive'
                    WHEN `templateId` = 'project' THEN 'active'
                    WHEN `templateId` IN ('ritual', 'place', 'feeling') THEN 'supportive'
                    ELSE 'stable'
                END
            """.trimIndent(),
        )
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        legacyStartAreaIdAliases.forEach { (legacyId, canonicalId) ->
            mergeAreaKeyTableRows(
                db = db,
                table = "area_instances",
                keyColumn = "areaId",
                copiedColumns = listOf(
                    "title",
                    "summary",
                    "iconKey",
                    "targetScore",
                    "sortOrder",
                    "isActive",
                    "cadenceKey",
                    "selectedTracks",
                    "signalBlend",
                    "intensity",
                    "remindersEnabled",
                    "reviewEnabled",
                    "experimentsEnabled",
                    "lageMode",
                    "directionMode",
                    "sourcesMode",
                    "flowProfile",
                    "templateId",
                    "createdAt",
                    "updatedAt",
                ),
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
            mergeAreaKeyTableRows(
                db = db,
                table = "area_snapshots",
                keyColumn = "areaId",
                copiedColumns = listOf(
                    "date",
                    "manualScore",
                    "manualStateKey",
                    "confidence",
                    "freshnessAt",
                    "createdAt",
                    "updatedAt",
                ),
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
            mergeAreaKeyTableRows(
                db = db,
                table = "life_areas",
                keyColumn = "id",
                copiedColumns = listOf(
                    "label",
                    "definition",
                    "targetScore",
                    "sortOrder",
                    "isActive",
                    "templateId",
                    "iconKey",
                    "createdAt",
                    "updatedAt",
                ),
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
            mergeAreaKeyTableRows(
                db = db,
                table = "life_area_profiles",
                keyColumn = "areaId",
                copiedColumns = listOf(
                    "cadence",
                    "intensity",
                    "signalBlend",
                    "selectedTracks",
                    "remindersEnabled",
                    "reviewEnabled",
                    "experimentsEnabled",
                    "updatedAt",
                ),
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
            mergeAreaKeyTableRows(
                db = db,
                table = "life_area_daily_checks",
                keyColumn = "areaId",
                copiedColumns = listOf(
                    "date",
                    "manualScore",
                    "createdAt",
                    "updatedAt",
                ),
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
            updateAreaReferenceColumn(
                db = db,
                table = "capture_items",
                column = "areaId",
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
            updateAreaReferenceColumn(
                db = db,
                table = "vorhaben",
                column = "areaId",
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
            updateAreaReferenceColumn(
                db = db,
                table = "plan_items",
                column = "areaId",
                legacyId = legacyId,
                canonicalId = canonicalId,
            )
        }
    }
}

private fun mergeAreaKeyTableRows(
    db: SupportSQLiteDatabase,
    table: String,
    keyColumn: String,
    copiedColumns: List<String>,
    legacyId: String,
    canonicalId: String,
) {
    val quotedColumns = copiedColumns.joinToString(", ") { "`$it`" }
    db.execSQL(
        """
        INSERT OR REPLACE INTO `$table` (`$keyColumn`, $quotedColumns)
        SELECT ?, $quotedColumns
        FROM `$table`
        WHERE `$keyColumn` = ?
        """.trimIndent(),
        arrayOf(canonicalId, legacyId),
    )
    db.execSQL(
        """
        DELETE FROM `$table`
        WHERE `$keyColumn` = ?
        """.trimIndent(),
        arrayOf(legacyId),
    )
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `definitionId` TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `authoringComplexity` TEXT NOT NULL DEFAULT 'ADVANCED'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `authoringVisibility` TEXT NOT NULL DEFAULT 'standard'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE `area_instances`
            SET
                `definitionId` = CASE
                    WHEN `areaId` IN (
                        'vitality', 'clarity', 'impact', 'bond', 'family', 'friends',
                        'community', 'home', 'stability', 'recovery', 'growth', 'learning',
                        'creativity', 'joy', 'meaning', 'discovery'
                    ) THEN `areaId`
                    ELSE 'template:' || COALESCE(`templateId`, 'free')
                END,
                `authoringComplexity` = CASE
                    WHEN `areaId` IN (
                        'vitality', 'clarity', 'impact', 'family', 'home', 'stability',
                        'recovery', 'learning'
                    ) THEN 'BASIC'
                    WHEN `areaId` IN (
                        'bond', 'friends', 'community', 'growth', 'creativity'
                    ) THEN 'ADVANCED'
                    WHEN `areaId` IN ('joy', 'meaning', 'discovery') THEN 'EXPERT'
                    WHEN `templateId` = 'medium' THEN 'EXPERT'
                    WHEN `templateId` IN ('project', 'ritual', 'place', 'feeling') THEN 'BASIC'
                    ELSE 'ADVANCED'
                END
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE `area_instances`
            SET `authoringVisibility` = CASE `authoringComplexity`
                WHEN 'BASIC' THEN 'focused'
                WHEN 'EXPERT' THEN 'expanded'
                ELSE 'standard'
            END
            """.trimIndent(),
        )
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `behaviorClass` TEXT NOT NULL DEFAULT 'reflection'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `confirmedStepKind` TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `confirmedStepLabel` TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `confirmedStepDueHint` TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `confirmedStepLinkedPlanItemId` TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `confirmedStepLinkedSourceId` TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `confirmedStepUpdatedAt` INTEGER
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_instances`
            ADD COLUMN `lastReviewedAt` INTEGER
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE `area_snapshots`
            ADD COLUMN `manualNote` TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE `area_instances`
            SET `behaviorClass` = CASE
                WHEN `definitionId` IN ('vitality', 'sleep') THEN 'tracking'
                WHEN `definitionId` IN ('impact', 'learning', 'work') THEN 'progress'
                WHEN `definitionId` IN ('friends', 'friendship', 'bond', 'family', 'community') THEN 'relationship'
                WHEN `definitionId` = 'home' THEN 'maintenance'
                WHEN `definitionId` IN ('clarity', 'focus') THEN 'protection'
                WHEN `definitionId` = 'recovery' THEN 'reflection'
                WHEN `templateId` = 'project' THEN 'progress'
                WHEN `templateId` = 'place' THEN 'maintenance'
                WHEN `templateId` = 'person' THEN 'relationship'
                ELSE 'reflection'
            END
            """.trimIndent(),
        )
    }
}

private fun updateAreaReferenceColumn(
    db: SupportSQLiteDatabase,
    table: String,
    column: String,
    legacyId: String,
    canonicalId: String,
) {
    db.execSQL(
        """
        UPDATE `$table`
        SET `$column` = ?
        WHERE `$column` = ?
        """.trimIndent(),
        arrayOf(canonicalId, legacyId),
    )
}
