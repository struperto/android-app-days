# Area Kernel Spec

This file is the canonical specification for the generic area system in `Days`.

Goal:
- define the stable kernel that can later be translated into code, persistence, UI state, and wireframes
- keep `Start` generic instead of area-specific
- separate planning truth from runtime truth

Scope:
- local Android app
- `Start` mode
- current shared area contract: `Lage`, `Richtung`, `Quellen`, `Flow`

Sources used:
- pilot areas: `Fokus`, `Vitalitaet`, `Freundschaft`
- templates under `docs/`
- current repo state in `SingleModels.kt`, `AreaBlueprints.kt`, and `StartStateModel.kt`

## 1. Kernel Objects

### 1.1 `AreaDefinition`

Purpose:
- the stable product and capability description of one area type
- answers what this area is allowed to be inside the generic system

Responsibility:
- define identity, product mode, panel capabilities, and default behavior
- stay stable across user edits and daily state changes
- be the primary candidate for a future typed runtime definition model

Required fields:
- `id`
- `title`
- `shortTitle`
- `iconKey`
- `category`
- `overviewMode`
- `complexityLevel`
- `seededByDefault`
- `userCreatable`
- `lageType`
- `focusType`
- `sourceTypesAllowed`
- `flowCapabilities`
- `defaultConfig`

Optional fields:
- `orderHint`
- `permissionSensitivity`
- `supportsPassiveSignals`
- `supportsImportedSources`
- `reviewRhythm`
- `capabilityNotes`

Does not belong here:
- user overrides like renamed titles or changed icons
- day-specific values like today's score
- UI copy for one specific screen layout
- open planning prose like risks, rollout notes, or task lists

### 1.2 `AreaBlueprint`

Purpose:
- the content blueprint used to seed one area and to feed shared mappers
- answers how this area should sound and what content shape it brings

Responsibility:
- provide default summary, tracks, panel copy seeds, and other area-specific content
- bridge product design and runtime mapping
- remain mostly static, but more content-heavy than `AreaDefinition`

Required fields:
- `areaId`
- `summary`
- `trackLabels`
- `defaultTemplateId`
- `defaultIconKey`
- `panelContentSeeds`

Optional fields:
- `defaultSourceLabels`
- `domainTags`
- `recommendedOrderHint`
- `starterHints`

Does not belong here:
- user-specific target score, cadence, or flow toggles
- current daily values
- screen-local UI state
- implementation-only notes like migration tasks or test plans

### 1.3 `AreaInstance`

Purpose:
- the persisted, user-specific configuration of one actual area in the app
- answers what this specific area currently looks like for this user

Responsibility:
- hold editable identity overrides and configuration
- drive overview ordering, active state, panel settings, and local flow settings
- survive app restarts and area edits

Required fields:
- `areaId`
- `title`
- `summary`
- `iconKey`
- `targetScore`
- `sortOrder`
- `isActive`
- `cadence`
- `selectedTracks`
- `signalBlend`
- `intensity`
- `remindersEnabled`
- `reviewEnabled`
- `experimentsEnabled`

Optional fields:
- `templateId`
- `createdAt`
- `updatedAt`
- future local source availability flags

Does not belong here:
- long descriptive product prose
- derived labels like `2 aktiv` or `Diese Woche Kontakt`
- temporary sheet selection or manage mode state
- raw UI rendering data

### 1.4 `AreaSnapshot`

Purpose:
- the time-bound current state of one area
- answers what is true now or on one given day

Responsibility:
- hold current check or reflection data
- remain separate from stable area configuration
- allow future extensions for local signal summaries

Required fields:
- `areaId`
- `date`
- one current manual state field

Optional fields:
- `manualScore`
- `manualStateKey`
- future derived local signal summary
- future confidence or freshness metadata

Does not belong here:
- persistent configuration like cadence or selected tracks
- permanent identity fields like title or icon
- UI-only wording
- open product planning notes

## 2. Canonical Kernel Axes

These axes are recommended as typed enums or sealed sets in a future code model.

### 2.1 `AreaOverviewMode`
Values:
- `Signal`
- `Plan`
- `Reflection`
- `Hybrid`

Meaning:
- controls how overview and home states should primarily read

Why it is stable:
- all three pilot areas need it
- current repo already hints at this via `StartAreaDrive`

### 2.2 `AreaComplexityLevel`
Values:
- `Basic`
- `Advanced`
- `Expert`

Meaning:
- declares how much of the area is visible or configurable by default

Why it is stable:
- useful in docs-first planning now
- likely useful later for UI exposure and setup flows

### 2.3 `AreaLageType`
Values:
- `Score`
- `State`
- `Checklist`
- `Hybrid`

Meaning:
- describes the kind of current-state reading the area uses

Why it is stable:
- `Fokus` trends score-like
- `Vitalitaet` trends hybrid
- `Freundschaft` trends state-like

### 2.4 `AreaFocusType`
Values:
- `Target`
- `Cadence`
- `NextStep`
- `Hybrid`

Meaning:
- describes how `Richtung` is structured

Why it is stable:
- the pilots all use a hybrid but with different emphasis

### 2.5 `AreaSourceType`
Values:
- `Manual`
- `LocalSignal`
- `Imported`
- `Note`
- `Track`

Meaning:
- describes what kind of source can inform the area

Why it is stable:
- enough to cover current repo shape and likely local-first growth

### 2.6 `AreaFlowCapability`
Values:
- `Reminder`
- `Review`
- `Experiment`
- `Trigger`
- `Action`

Meaning:
- describes what `Flow` may eventually support for an area

Why it is stable:
- current repo already supports reminder/review/experiment toggles
- trigger/action remain explicit future capabilities

### 2.7 Optional additional axes worth keeping

#### `AreaCategory`
Possible values:
- `Foundation`
- `Direction`
- `Relationship`
- `Environment`
- `Growth`
- `Open`

Use:
- lightweight grouping and filtering

#### `PermissionSensitivity`
Possible values:
- `None`
- `Low`
- `Medium`
- `High`

Use:
- future guidance for source and privacy decisions

## 3. Field Separation Rules

### 3.1 Docs-first planning fields
Belong in templates and pilot docs only:
- why the area exists
- detailed risks
- rollout notes
- UX reading notes like `headerRead` or `coreCardRead`
- task sequencing and follow-ups

### 3.2 Future technical model fields
Belong in runtime models:
- identity
- capabilities
- overview mode
- model types for `Lage`, `Richtung`, `Quellen`, `Flow`
- default configuration
- track labels and content seeds

### 3.3 Future UI state fields
Belong only in mapped UI state:
- status labels like `Ziel 4/5`, `Lage offen`, `2 aktiv`
- progress values
- panel summaries
- manage mode selection
- confirmation-sheet visibility
- action card values and option sheets

### 3.4 Future persistence fields
Belong in stored instance or snapshot entities:
- user-edited title and summary
- icon override
- target score
- cadence
- selected tracks
- signal blend
- intensity
- reminders/review/experiments toggles
- daily manual score or state
- sort order
- active/inactive state

## 4. Mapping To Current Repo

### 4.1 Direct reuse candidates

#### `LifeArea` in [SingleModels.kt](/Users/rupertjud/Downloads/android-app-days/app/src/main/java/com/struperto/androidappdays/data/repository/SingleModels.kt)
Current role:
- area identity plus some persisted configuration

Can be reused as:
- the first persistence form of `AreaInstance` identity fields

Gaps:
- it currently mixes stable definition-ish fields with user instance fields
- naming is domain-generic enough, but structure is still seed-era specific

#### `LifeAreaProfile` in [SingleModels.kt](/Users/rupertjud/Downloads/android-app-days/app/src/main/java/com/struperto/androidappdays/data/repository/SingleModels.kt)
Current role:
- per-area direction, source, and flow config

Can be reused as:
- the first persistence form of `AreaInstance` configuration fields

Gaps:
- it is configuration-only, not a full area instance
- it assumes the current 4-panel structure and current flow toggle set

#### `LifeAreaDailyCheck` in [SingleModels.kt](/Users/rupertjud/Downloads/android-app-days/app/src/main/java/com/struperto/androidappdays/data/repository/SingleModels.kt)
Current role:
- daily manual score

Can be reused as:
- the first persistence form of `AreaSnapshot`

Gaps:
- only supports numeric manual score today
- reflective or non-score `Lage` types would need extension later

### 4.2 Rename or reorder candidates

#### `StartAreaBlueprint` in [AreaBlueprints.kt](/Users/rupertjud/Downloads/android-app-days/app/src/main/java/com/struperto/androidappdays/feature/start/AreaBlueprints.kt)
Current role:
- mixes area summary, tracks, drive, defaults, domains, and panel entries

Recommended future role:
- split into `AreaDefinition` plus `AreaBlueprint`

Reason:
- `drive`, defaults, and panel capability concepts feel like kernel fields
- `summary`, `tracks`, `entries`, and content seeds feel like blueprint fields

#### `StartAreaDrive`
Current role:
- current proxy for overview mode

Recommended future role:
- rename to `AreaOverviewMode`

Reason:
- matches product and pilot docs better
- clearer than `Drive`

### 4.3 Replacement candidates

#### `StartStateModel.kt`
Current role:
- rich mapped UI-state layer for overview and detail

Recommended future role:
- keep it as a mapper/UI-state layer, not as kernel truth

Reason:
- it already behaves like projection code
- it should map from future `AreaDefinition` + `AreaBlueprint` + `AreaInstance` + `AreaSnapshot`

#### `LifeAreaDefaults.kt`
Current role:
- seeded default areas

Recommended future role:
- derive from the future canonical `AreaDefinition`/`AreaBlueprint` source of truth

Reason:
- seeded defaults should not stay as a separate truth forever

### 4.4 Candidate migration path
1. Introduce typed kernel docs-first terms in code naming.
2. Split current blueprint data into:
   - stable capability definition
   - content blueprint
3. Keep `LifeArea`, `LifeAreaProfile`, and `LifeAreaDailyCheck` temporarily as persistence carriers.
4. Update mappers to consume the new kernel split.
5. Only later consider persistence schema changes if needed.

## 5. What Should Be Decided Now

These decisions are stable enough to fix now:
- the four kernel objects:
  - `AreaDefinition`
  - `AreaBlueprint`
  - `AreaInstance`
  - `AreaSnapshot`
- `Start` stays on the shared 4-panel contract:
  - `Lage`
  - `Richtung`
  - `Quellen`
  - `Flow`
- `AreaOverviewMode` is a canonical axis
- `AreaLageType`, `AreaFocusType`, `AreaSourceType`, and `AreaFlowCapability` are canonical axes
- planning truth and runtime truth must stay separate
- current Start UI state remains a projection layer, not the kernel

## 6. What May Stay Open For Now

These points should remain open until implementation pressure is real:
- whether `AreaSnapshot` should support non-score `Lage` via one polymorphic field or multiple typed fields
- whether `AreaBlueprint` lives later as Kotlin code, JSON, YAML, or another repo-first format
- whether `AreaCategory` should be tightly typed now or remain loose longer
- how far passive local signals should be normalized before persistence
- whether relationship-style areas need a dedicated non-numeric `Lage` renderer later
- whether current `templateId` survives as a user-facing concept or becomes an internal seed/construction hint only

## 7. Spec Summary

Canonical kernel:
- `AreaDefinition` = stable product capabilities
- `AreaBlueprint` = seeded content and mapper input
- `AreaInstance` = persisted user-specific area configuration
- `AreaSnapshot` = current day-bound state

Canonical separation:
- docs-first planning stays in `docs/`
- technical model carries only stable runtime truth
- UI state is always projected
- persistence holds only instance and snapshot truth
