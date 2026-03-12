# Area Kernel Migration Plan

This file defines the technical migration path from the current `Start` / `LifeArea` / blueprint model to the generic area kernel specified in `docs/area-kernel-spec.md`.

Goal:
- introduce the kernel in a controlled way before feature code is rebuilt
- keep the current `Start` UI running while the underlying truth is reorganized
- reduce double truth across repository defaults, start blueprints, and UI mappers

Scope:
- local Android app
- current `Start` mode only
- no `Single` or `Multi` migration in the first kernel rollout

## 1. Target Code Structure

The future code shape should separate stable kernel types, persisted runtime data, and UI projections.

### 1.1 Domain layer

Recommended location:
- `app/src/main/java/com/struperto/androidappdays/domain/area/`

Target responsibilities:
- own the stable kernel types
- contain typed enums and value objects
- remain independent from Compose UI and Room entities

Target types:
- `AreaDefinition`
- `AreaBlueprint`
- `AreaInstance`
- `AreaSnapshot`
- typed axes such as `AreaOverviewMode`, `AreaComplexityLevel`, `AreaLageType`, `AreaFocusType`, `AreaSourceType`, `AreaFlowCapability`

Not in this layer:
- Compose icons
- screen-local labels like `Heute 3/5`
- Room entities
- navigation or sheet state

### 1.2 Data layer

Recommended locations:
- `app/src/main/java/com/struperto/androidappdays/data/local/`
- `app/src/main/java/com/struperto/androidappdays/data/repository/`
- optional later: `app/src/main/java/com/struperto/androidappdays/data/area/`

Target responsibilities:
- persist `AreaInstance` and `AreaSnapshot`
- expose repositories that return kernel-ready runtime models
- keep seed/setup logic behind repository boundaries

Target shape:
- Room entities remain the persistence source during transition
- adapters map Room entities to `AreaInstance` and `AreaSnapshot`
- later schema work only happens when the current tables cannot express the kernel cleanly enough

### 1.3 Start feature layer

Recommended locations:
- `app/src/main/java/com/struperto/androidappdays/feature/start/`
- optional later mapper subpackage: `feature/start/mapper/`

Target responsibilities:
- own `Start`-specific UI projection
- consume kernel models instead of raw repository structs
- keep `StartStateModel` and panel projections as UI-only state

Target shape:
- `StartViewModel` and `AreaStudioViewModel` depend on a start-facing projector or mapper
- `StartScreen` and `AreaStudioScreen` stay thin and consume `StartOverviewState` / `StartAreaDetailState`

### 1.4 Mapper / adapter layer

Recommended location:
- `app/src/main/java/com/struperto/androidappdays/domain/area/mapping/`
- or `feature/start/mapper/` for Start-only projections

Target responsibilities:
- adapt old persistence and blueprint sources to the new kernel
- translate kernel models into `StartStateModel`
- allow old and new truth to coexist during migration

Core migration adapters:
- `LifeArea -> AreaInstance` adapter
- `LifeAreaProfile -> AreaInstance` config merge adapter
- `LifeAreaDailyCheck -> AreaSnapshot` adapter
- `StartAreaBlueprint -> AreaDefinition + AreaBlueprint` adapter
- `AreaKernel -> StartStateModel` projector

## 2. Mapping Existing Models To The Kernel

### 2.1 `LifeArea`

Current role:
- persisted active area identity and part of runtime config
- currently mixes seed defaults, editable identity, target, order, activation, template hint, icon hint

Kernel mapping:
- maps mainly to `AreaInstance`
- `id` -> `areaId`
- `label` -> `title`
- `definition` -> `summary`
- `targetScore` -> `targetScore`
- `sortOrder` -> `sortOrder`
- `isActive` -> `isActive`
- `templateId` -> optional `templateId`
- `iconKey` -> `iconKey`

Migration decision:
- continue using during transition
- do not rename in place first
- treat as temporary persistence carrier for part of `AreaInstance`
- later split semantically in code, not necessarily immediately in DB

Classification:
- `weiterverwenden` now
- `nur mappen` into `AreaInstance`
- `spaeter aufspalten` conceptually

### 2.2 `LifeAreaProfile`

Current role:
- persisted area configuration for cadence, intensity, source mix, tracks, reminder/review/experiment toggles

Kernel mapping:
- maps to the remaining configuration fields of `AreaInstance`
- `cadence`
- `intensity`
- `signalBlend`
- `selectedTracks`
- `remindersEnabled`
- `reviewEnabled`
- `experimentsEnabled`

Migration decision:
- continue using as the second persistence source for `AreaInstance`
- merge with `LifeArea` in adapter code, not in DB first

Classification:
- `weiterverwenden` now
- `nur mappen` into `AreaInstance`
- `spaeter entfernen` only if a future unified instance table becomes worth the migration cost

### 2.3 `LifeAreaDailyCheck`

Current role:
- persisted daily manual score for one area and one date

Kernel mapping:
- first persistence form of `AreaSnapshot`
- `areaId`
- `date`
- `manualScore`

Migration decision:
- continue using as the first snapshot backend
- keep numeric score semantics for the first migration wave
- only extend schema when non-score `Lage` becomes real product scope

Classification:
- `weiterverwenden` now
- `umbenennen` only at the kernel adapter level
- `spaeter erweitern`, not yet replace

### 2.4 `StartAreaBlueprint`

Current role:
- area-specific content source for Start
- currently mixes stable product mode, content copy, default config, icon/template hints, domains, and entry metadata

Kernel mapping:
- split into:
  - `AreaDefinition`
  - `AreaBlueprint`

Suggested split:
- `id`, `label`, `drive`, `defaultTargetScore`, `defaultTemplateId`, `defaultIconKey`, `domains` feed `AreaDefinition`
- `summary`, `tracks`, `entries` feed `AreaBlueprint`
- `tier` remains Start-specific and should not move into the generic kernel unless another feature needs it

Migration decision:
- do not delete immediately
- add adapter functions that project `StartAreaBlueprint` into the new kernel types
- only after all `Start` mappers use the kernel output should the old type be reduced or removed

Classification:
- `aufspalten`
- `nur mappen` first
- `spaeter entfernen` as direct truth

### 2.5 `StartAreaDrive`

Current role:
- lightweight enum for how a Start area reads on overview/home

Kernel mapping:
- direct candidate for `AreaOverviewMode`

Migration decision:
- keep existing enum temporarily
- add a typed mapping to the kernel enum
- remove only after all kernel definitions own the canonical value

Classification:
- `umbenennen` conceptually
- `nur mappen` first

### 2.6 `StartAreaTemplate`

Current role:
- create/edit helper template for user-created areas

Kernel mapping:
- not a kernel object
- should later derive from `AreaDefinition` and optional starter blueprint content

Migration decision:
- keep as Start create-flow helper during migration
- later replace with a kernel-backed starter/template source if create flow is generalized

Classification:
- `weiterverwenden`
- `nur mappen`

### 2.7 `StartAreaEntry`

Current role:
- Start-specific panel metadata with panel, title, summary, and Compose icon

Kernel mapping:
- only partially kernel-relevant
- `panel`, `title`, `summary` belong near `AreaBlueprint`
- `ImageVector` icon does not belong in the kernel

Migration decision:
- keep as Start UI metadata until a cleaner presentation layer is extracted
- future split into:
  - content seed metadata
  - UI icon resolution layer

Classification:
- `aufspalten`
- `spaeter neu ordnen`

### 2.8 `LifeAreaDefaults`

Current role:
- repository-facing seed/default accessor
- already delegates to start seed generation

Kernel mapping:
- should later derive from canonical `AreaDefinition + AreaBlueprint`

Migration decision:
- keep as compatibility surface for repository setup
- change its internal source last, not first

Classification:
- `weiterverwenden`
- `spaeter nur mappen`

### 2.9 `StartStateModel`

Current role:
- canonical `Start` UI projection
- maps persistence models and start blueprints into overview, detail, panel, action, and sheet state

Kernel mapping:
- should remain a projection layer
- should later depend on:
  - `AreaDefinition`
  - `AreaBlueprint`
  - `AreaInstance`
  - `AreaSnapshot`

Migration decision:
- preserve its public UI contract for as long as possible
- replace its inputs before reshaping its outputs

Classification:
- `weiterverwenden`
- `nur mappen`
- not part of kernel truth

### 2.10 `LifeWheelRepository`

Current role:
- active area CRUD, seed setup, ordering, deletion, daily checks

Kernel mapping:
- transitional repository behind `AreaInstance` and `AreaSnapshot`
- not the final generic area repository yet

Migration decision:
- keep interface stable initially
- add adapter/projector layer above it before changing repository contracts
- only introduce a dedicated generic area repository after the kernel types are already used internally

Classification:
- `weiterverwenden`
- `spaeter neu ordnen`

## 3. Coexistence Rules During Migration

During migration, old and new truth may coexist, but only under explicit rules.

Allowed temporary coexistence:
- Room entities and repository structs remain the persistence carrier
- `StartAreaBlueprint` remains the content source
- new kernel models exist as adapters over those old sources
- `StartStateModel` continues to feed the current UI

Not allowed:
- new product logic duplicated once in `StartStateModel` and once in kernel adapters
- seed defaults maintained separately in `LifeAreaDefaults` and new kernel definitions
- parallel renaming of persistence tables and UI state in the same phase

Guardrail:
- every new kernel field must have one declared source of truth during the migration phase it appears in

## 4. Recommended Introduction Order

The migration should move from type introduction to projection replacement, not from DB schema to UI first.

### Step 1
- introduce typed kernel models in `domain/area/`
- no repository contract changes yet
- no screen changes yet

### Step 2
- add adapters from current models to kernel models
- keep adapters pure and unit-testable
- do not change Room or navigation yet

### Step 3
- change `StartStateModel` projectors to consume kernel models instead of raw `LifeArea` / `LifeAreaProfile` / `LifeAreaDailyCheck` / `StartAreaBlueprint`
- keep `StartScreen`, `AreaStudioScreen`, and panel UI contracts stable

### Step 4
- centralize seed and blueprint truth around the kernel-backed source
- make `LifeAreaDefaults` a compatibility facade over kernel definitions
- reduce direct use of `StartAreaBlueprint`

### Step 5
- only if needed, evolve persistence to better fit `AreaInstance` / `AreaSnapshot`
- do this after Start is already reading from the kernel adapter layer

## 5. Concrete Migration Phases For This Repo

### Phase 1: Introduce technical kernel types

Add new types without changing behavior:
- `AreaDefinition`
- `AreaBlueprint`
- `AreaInstance`
- `AreaSnapshot`
- typed kernel enums

Recommended package:
- `domain/area/`

Required tests:
- kernel enum and model construction tests if custom helpers exist

Exit criterion:
- kernel compiles and is not yet wired into UI

### Phase 2: Build compatibility adapters

Add pure mapping functions:
- `StartAreaBlueprint -> AreaDefinition`
- `StartAreaBlueprint -> AreaBlueprint`
- `LifeArea + LifeAreaProfile? -> AreaInstance`
- `LifeAreaDailyCheck? -> AreaSnapshot?`

Recommended package:
- `domain/area/mapping/`

Required tests:
- mapper tests for seeded areas and custom areas
- tests for missing profile / missing snapshot defaults

Exit criterion:
- any current Start area can be represented fully as kernel inputs

### Phase 3: Feed Start projectors from kernel inputs

Refactor projector inputs, not screen outputs:
- `mapStartOverviewState(...)` should take kernel-facing inputs or a kernel aggregate
- `mapStartAreaDetailState(...)` should take kernel-facing inputs or a kernel aggregate

Keep unchanged:
- `StartViewModel`
- `AreaStudioViewModel`
- `StartScreen`
- `AreaStudioScreen`

Required tests:
- keep existing Start mapper tests passing
- add parity tests old projector input vs kernel-backed projector input

Exit criterion:
- current Start UI is visually and behaviorally unchanged but no longer depends on raw old truth directly

### Phase 4: Centralize defaults and naming

Move stable definition truth toward the kernel:
- `StartAreaDrive` becomes mapped or replaced by `AreaOverviewMode`
- `LifeAreaDefaults` becomes kernel-derived
- direct reads from `startAreaBlueprints` become limited to adapter or content-seed code

Required tests:
- seed generation tests
- no deleted seed resurrection tests
- no edited seed override tests

Exit criterion:
- one canonical path exists from definition/blueprint source to setup seeds and Start projection

### Phase 5: Optional persistence normalization

Only start if product scope requires it:
- richer snapshot state than numeric score
- explicit source configurations beyond `signalBlend` and `selectedTracks`
- unified instance persistence instead of `LifeArea + LifeAreaProfile`

Possible work:
- Room schema extension
- migration scripts
- repository contract cleanup

Required tests:
- repository migration tests
- destructive/non-destructive data retention tests
- instrumentation sanity checks for Start CRUD and manage flows

Exit criterion:
- persistence shape matches the kernel closely enough to remove compatibility adapters

## 6. Tests Required Before And During Migration

### Before every phase
- `assembleDebug`
- existing Start-related unit tests
- existing repository tests for seed, delete, and cleanup behavior

### Required safety tests by concern

Mapper safety:
- seeded area maps to expected `AreaDefinition` and `AreaBlueprint`
- custom area maps to valid `AreaInstance` even without seed blueprint
- missing profile still yields deterministic `AreaInstance` defaults

Projection safety:
- overview status/progress parity stays intact
- detail panel labels and panel action wiring remain stable

Persistence safety:
- deleted seed areas do not resurrect
- edited seed identity is not overwritten
- delete still cleans related daily checks and profiles

UI safety:
- Start navigation still opens overview and detail
- manage mode, delete confirm, and panel entry flows still work after projector changes

## 7. Risks And Protection Rules

### 7.1 Double truth risk

Risk:
- stable definition data may exist at the same time in:
  - `AreaBlueprints.kt`
  - `LifeAreaDefaults.kt`
  - new kernel definitions

Protection:
- once a field is introduced into `AreaDefinition`, declare that source canonical
- old locations become adapters only

### 7.2 Data-loss risk

Risk:
- merging `LifeArea` and `LifeAreaProfile` too early could break target/config persistence
- extending snapshots too early could invalidate existing daily check data

Protection:
- no Room schema change before adapter-based kernel projection works
- repository tests must pass before any persistence refactor

### 7.3 UI drift risk

Risk:
- Start screens may silently change semantics if kernel projection is not kept parity-safe

Protection:
- phase 3 is projector-only
- screen contracts stay stable until after parity is verified

### 7.4 Rename cascade risk

Risk:
- renaming `StartAreaDrive`, panel enums, and projector inputs all at once creates noisy, fragile diffs

Protection:
- rename conceptually first in docs and adapters
- remove old names only after behavior is stable

### 7.5 Feature creep risk

Risk:
- migration work may accidentally pull in new automations, richer sources, or non-score snapshots

Protection:
- Phase 1 to 4 are structural only
- richer product semantics are postponed to follow-up work

## 8. Decisions That Should Be Fixed Now

- the four kernel objects are the migration target
- `StartStateModel` remains projection, not kernel truth
- `LifeArea` and `LifeAreaProfile` are temporary split carriers of `AreaInstance`
- `LifeAreaDailyCheck` is the first `AreaSnapshot` carrier
- `StartAreaBlueprint` must be split conceptually into `AreaDefinition` and `AreaBlueprint`
- persistence schema changes are not part of the first kernel introduction

## 9. Decisions That May Stay Open For Now

- whether `AreaBlueprint` becomes Kotlin, JSON, YAML, or generated data
- whether `AreaCategory` should be strict in the first implementation
- whether reflective areas need a non-score snapshot field immediately
- whether `templateId` survives as a first-class runtime field long-term
- whether a dedicated generic area repository should replace `LifeWheelRepository` or sit above it
