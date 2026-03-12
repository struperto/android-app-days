# Area Kernel Candidate

This file derives a first technical kernel from the pilot areas `Fokus`, `Vitalitaet`, and `Freundschaft`.

Goal:
- separate fields that look truly generic from fields that are only area-specific content
- reduce the risk of hard-coding seeded areas forever

## 1. Fields that look generic enough for a first technical model

### Identity
- `id`
- `title`
- `shortTitle`
- `iconKey`
- `category`
- `orderHint`
- `seededByDefault`
- `userCreatable`

### Product behavior
- `overviewMode`
- `complexityDefault`
- `hasLage`
- `hasRichtung`
- `hasQuellen`
- `hasFlow`

### Lage
- `lageType`
- `primaryMetricLabel`
- `secondarySignalLabels`
- `emptyStateMeaning`
- `configuredStateMeaning`
- `warningStateMeaning`

### Richtung
- `focusType`
- `defaultTargetShape`
- `defaultCadence`
- `successDefinition`
- `reviewRhythm`

### Quellen
- `sourceTypesAllowed`
- `manualInputAllowed`
- `localSignalsAllowed`
- `importedSourcesAllowed`
- `permissionSensitive`
- `sourceMixMeaning`

### Flow
- `supportsReminders`
- `supportsReviews`
- `supportsExperiments`
- `supportsTriggersLater`
- `supportsActionsLater`
- `localFlowMeaning`
- `inactiveFlowMeaning`

### Delivery and persistence hints
- `mustPersist`
- `canBeDerived`
- `ephemeralOnly`
- `cleanupRules`
- `migrationSensitivity`
- `mustBuildNow`
- `shouldBuildSoon`
- `canDelay`
- `explicitNonGoals`

## 2. Fields that should stay content or planning, not core model fields
- long prose like `whyThisAreaExistsInDays`
- detailed UX-only entries like `headerRead`, `coreCardRead`, `metricRowRead`
- rollout notes like `followUpTasks`
- implementation notes like `featureFlagsNeeded`
- one-off risks that belong in planning, not runtime data

## 3. First technical split

### `AreaDefinition`
Stable product metadata and capabilities.

Suggested shape:
- identity
- area category
- overview mode
- panel capability flags
- generic model types for `Lage`, `Richtung`, `Quellen`, `Flow`
- default configuration values
- permission sensitivity

### `AreaBlueprint`
Area-specific content used by mappers and default setup.

Suggested shape:
- summary
- track labels
- panel copy seeds
- source labels
- default icon and ordering hints

### `AreaInstance`
Persisted user-specific configuration.

Suggested shape:
- area id
- title override
- summary override
- icon override
- target score
- cadence
- selected tracks
- flow toggles
- intensity
- source mix
- active/inactive state
- sort order

### `AreaSnapshot`
Current time-bound state.

Suggested shape:
- area id
- date
- manual score or reflective state
- optional derived signal summary later

## 4. Candidate V1 Kotlin-level model

```kotlin
data class AreaDefinition(
    val id: String,
    val title: String,
    val shortTitle: String,
    val iconKey: String,
    val category: AreaCategory,
    val overviewMode: AreaOverviewMode,
    val complexityDefault: AreaComplexity,
    val seededByDefault: Boolean,
    val userCreatable: Boolean,
    val lage: AreaLageDefinition,
    val richtung: AreaRichtungDefinition,
    val quellen: AreaQuellenDefinition,
    val flow: AreaFlowDefinition,
    val defaultConfig: AreaDefaultConfig,
)
```

```kotlin
data class AreaDefaultConfig(
    val targetScore: Int,
    val cadence: String,
    val selectedTracks: List<String>,
    val intensity: Int,
    val signalBlend: Int,
    val remindersEnabled: Boolean,
    val reviewEnabled: Boolean,
    val experimentsEnabled: Boolean,
)
```

## 5. What the pilots reveal
- `Fokus` proves the kernel must support direction-first areas.
- `Vitalitaet` proves the kernel must support future local signals without requiring them now.
- `Freundschaft` proves the kernel must support reflective areas that should not feel like telemetry.

## 6. Recommendation
Build the first technical kernel around:
- identity
- overview mode
- panel capability definitions
- default config
- track labels

Do not put large descriptive prose or UX-only planning text into the runtime model.
