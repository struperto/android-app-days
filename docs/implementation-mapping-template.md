# Implementation Mapping Template

Use this template after product and wireframe planning are stable.

Goal:
- translate one area into code, state, persistence, and tests
- keep implementation generic enough for later user-creatable areas
- make MVP scope explicit before writing app logic

Context:
- local Android app
- `Start` overview and area detail flow
- shared panel architecture for `Lage`, `Richtung`, `Quellen`, `Flow`

## 1. Domain Models
- `AreaDefinition` fields needed:
- `AreaInstance` fields needed:
- `LageSnapshot` fields needed:
- `RichtungConfig` fields needed:
- `QuellenConfig` fields needed:
- `FlowConfig` fields needed:
- `extraModels`:

## 2. UI State
- `TileUiState`:
- `AreaHomeUiState`:
- `LagePanelUiState`:
- `RichtungPanelUiState`:
- `QuellenPanelUiState`:
- `FlowPanelUiState`:
- `ManageUiState`:
- `ErrorUiState`:

## 3. Persistence
- `entitiesNeeded`:
- `persistedFields`:
- `derivedFields`:
- `ephemeralFields`:
- `seedRules`:
- `deleteCleanupRules`:
- `migrationNotes`:

## 4. Inputs and Dependencies
- `manualInputs`:
- `deviceSignals`:
- `permissions`:
- `settingsDependencies`:
- `defaultValues`:
- `futureIntegrations`:

## 5. Actions
- `createArea`:
- `editAreaIdentity`:
- `updateLage`:
- `updateRichtung`:
- `updateQuellen`:
- `updateFlow`:
- `deleteArea`:
- `reorderArea`:
- `swapArea`:

## 6. Logic
- `mappersNeeded`:
- `projectorsNeeded`:
- `validatorsNeeded`:
- `evaluatorsNeeded`:
- `recommendationLogicNeeded`:
- `fallbackRules`:

## 7. Shared Components
- `reuseExistingTile`: yes | no
- `reuseAreaHomeCard`: yes | no
- `reusePanelScaffold`: yes | no
- `reuseBottomSheetPattern`: yes | no
- `reuseMetricRow`: yes | no
- `newGenericComponentNeeded`:

## 8. Testing
- `mapperTests`:
- `stateTests`:
- `repositoryTests`:
- `panelInteractionTests`:
- `manageFlowTests`:
- `permissionTests`:
- `manualChecks`:

## 9. MVP Scope
- `mustBuildNow`:
- `shouldBuildSoon`:
- `canDelay`:
- `doNotBuildInThisStep`:

## 10. Delivery Notes
- `implementationOrder`:
- `featureFlagsNeeded`: yes | no
- `rolloutRisks`:
- `followUpTasks`:
