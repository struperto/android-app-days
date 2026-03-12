# Focus Pilot Area

This file fills the three master templates for `Fokus`.

## 1. Area Definition

### Identity
- `areaId`: `clarity`
- `title`: `Fokus`
- `shortTitle`: `Fokus`
- `iconKey`: `focus`
- `category`: `direction`
- `orderHint`: high in seeded order
- `seededByDefault`: yes
- `userCreatable`: no in first seed set, yes in the generic system
- `complexityDefault`: `Basic`

### Purpose
- `primaryPurpose`: help the user protect attention and stay with the right thing
- `userProblem`: attention drifts, context switches, and screens steal depth
- `expectedOutcome`: clearer next move and less fragmented work or thinking
- `whyThisAreaExistsInDays`: `Fokus` is a core local steering area for daily correction

### Product Shape
- `overviewMode`: `plan`
- `coreQuestion`: am I with the right thing right now
- `whatGoodLooksLike`: one meaningful focus line feels protected and active
- `whatDriftLooksLike`: scattered attention, too many switches, or no clear next move
- `firstUsefulRead`: current focus and whether drift is open or under control
- `firstUsefulAction`: set or change the next focus line

### Lage Model
- `hasLage`: yes
- `lageType`: `score`
- `primaryMetric`: subjective focus quality now
- `secondarySignals`: interruption pressure, screen pull, mental noise
- `emptyStateMeaning`: focus was not checked yet today
- `configuredStateMeaning`: current focus quality is known
- `warningStateMeaning`: low score means drift, fragmentation, or no protected block
- `evaluationLogicNotes`: keep MVP manual-first; later device or notification pressure may influence hints

### Richtung Model
- `hasRichtung`: yes
- `focusType`: `hybrid`
- `defaultTargetShape`: target score plus one active focus line
- `defaultCadence`: adaptive
- `nextMoveLogic`: pick one focus line first, then protect it for the next useful block
- `successDefinition`: user can name the current focus and return to it quickly
- `reviewRhythm`: daily lightweight review, weekly pattern check later

### Quellen Model
- `hasQuellen`: yes
- `sourceTypesAllowed`: manual notes, selected focus tracks, local notification pressure later
- `manualInputAllowed`: yes
- `localSignalsAllowed`: yes later
- `importedSourcesAllowed`: no in MVP
- `permissionSensitive`: low in MVP, medium later if notification access is used
- `sourceTrustRules`: manual user choice wins over passive hints
- `sourceMixMeaning`: how much focus guidance comes from user selection vs passive signals

### Flow Model
- `hasFlow`: yes
- `supportsReminders`: yes
- `supportsReviews`: yes
- `supportsExperiments`: yes
- `supportsTriggersLater`: yes
- `supportsActionsLater`: yes
- `localFlowMeaning`: lightweight local steering around reminders, review, and experiments
- `inactiveFlowMeaning`: no active steering, only manual read and edit

### Persistence
- `mustPersist`: title, icon, summary, target score, cadence, selected tracks, flow toggles, manual daily check
- `canBeDerived`: next move copy, flow labels, tile progress
- `ephemeralOnly`: sheet open state, transient warnings
- `cleanupRules`: deleting area removes profile and daily checks
- `migrationSensitivity`: low if signals stay optional, higher once passive signals are stored

### State and Logic
- `domainModelsNeeded`: area identity, focus snapshot, selected focus tracks, local flow settings
- `uiStatesNeeded`: tile state, home summary state, 4 panel states, manage state
- `mapperOrProjectorNeeded`: central mapper for current focus line, next move, drift wording
- `validatorsNeeded`: at least one selected focus track if sources are configured
- `edgeCases`: no focus line selected, all flow toggles off, user changes focus often in one day

### MVP Scope
- `mustBuildNow`: manual Lage, target score, cadence, selected tracks, local flow toggles
- `shouldBuildSoon`: passive drift hints from local device pressure
- `canDelay`: advanced triggers and automated corrections
- `explicitNonGoals`: deep productivity system, cross-app blocking, complex automation

### Risks
- `confusionRisks`: focus can sound moralizing if overexplained
- `overloadRisks`: too many control points can make the user less focused
- `privacyRisks`: notification pressure later needs careful local-only framing
- `automationRisks`: too many reminders would undermine calm product language
- `failureStates`: user does not know what the current focus line should be

### Open Decisions
- `productQuestions`: should `Fokus` bias toward one task or one theme
- `uxQuestions`: how explicit should interruption pressure be shown later
- `technicalQuestions`: if passive signals come later, are they persisted raw or only projected

## 2. Wireframe Planning

### Start Tile
- `primarySignal`: current Lage label or target if still open
- `secondarySignal`: small progress cue
- `statusStyle`: calm but sharper than reflective areas
- `tapAction`: open `Fokus` home
- `longPressAction`: enter manage mode
- `emptyTileState`: neutral tile with target shown
- `configuredTileState`: current Lage is visible
- `warningTileState`: subtle drift state, not alarming red

### Area Home
- `topIdentityContent`: icon, title, short meaning
- `summaryLine`: one-line read of focus now
- `metricRow`: Lage, Soll, Fokus, Flow
- `progressMeaning`: closeness of current focus quality to target
- `panelEntryOrder`: Lage, Richtung, Quellen, Flow
- `panelEntrySummaries`: today focus, next move, active sources, active local steering
- `emptyHomeState`: no Lage yet, but focus direction is still editable
- `configuredHomeState`: one clear focus line and calm supporting settings

### Panel: Lage
- `primaryQuestion`: how focused am I right now
- `headerRead`: focus now plus day context
- `coreCardRead`: current focus quality
- `metricRowRead`: target and cadence
- `mainActions`: set score, clear score
- `editControlType`: `bottom_sheet`
- `emptyState`: `offen`
- `configuredState`: one score from 1 to 5
- `warningState`: low score without extra prose

### Panel: Richtung
- `primaryQuestion`: what should I stay with next
- `headerRead`: active focus line and cadence
- `coreCardRead`: next move phrased as one clear focus step
- `metricRowRead`: current focus line and next move
- `mainActions`: update target, cadence, focus line
- `editControlType`: `bottom_sheet`
- `emptyState`: use first recommended focus line
- `configuredState`: `Heute X`, `Diese Woche X`, or `X zuerst`
- `warningState`: missing focus line should feel incomplete, not broken

### Panel: Quellen
- `primaryQuestion`: what currently feeds focus guidance
- `headerRead`: number of active sources
- `coreCardRead`: active focus tracks and source mix
- `metricRowRead`: focus line and source mix
- `mainActions`: choose tracks, set mix
- `editControlType`: `bottom_sheet`
- `emptyState`: no source active yet
- `configuredState`: 1 to 3 focus tracks active
- `permissionState`: later passive signals unavailable
- `warningState`: passive sources noisy or switched off

### Panel: Flow
- `primaryQuestion`: what local steering is active for focus
- `headerRead`: primary active flow or count
- `coreCardRead`: active steering plus intensity
- `metricRowRead`: active steering labels and intensity
- `mainActions`: set intensity, toggle reminders/review/experiments
- `editControlType`: `bottom_sheet`
- `emptyState`: `Lokal`
- `configuredState`: one or two active steering modes
- `inactiveFlowState`: manual use only
- `warningState`: too many active steering modes later may need trimming

### Setup and Edit Flows
- `firstSetupNeeded`: no
- `setupSteps`: optional choose focus tracks, target, cadence
- `decisionPoints`: whether focus is guided manually only
- `defaultsShown`: a default focus track list
- `permissionsRequested`: none in MVP
- `advancedBranches`: passive signals later

### Manage Flows
- `editEntry`: area home icon anchor or start manage dock
- `deleteConfirmationStyle`: light confirmation sheet
- `reorderBehavior`: move earlier/later
- `swapBehavior`: long press then tap second tile
- `emptyAfterDeleteBehavior`: area disappears, order closes cleanly

### Empty and Error States
- `noDataYet`: no Lage today
- `notConfigured`: default focus line still active
- `permissionMissing`: only relevant later
- `sourceMissing`: no active focus track
- `invalidConfig`: impossible only if target or cadence corrupted
- `staleData`: passive hints older than useful window later

### Complexity Layers
#### Basic
- `visibleFields`: Lage, one focus line, source tracks, flow toggles
- `visibleActions`: set score, set cadence, choose focus line
- `hiddenAdvancedControls`: passive-signal rules

#### Advanced
- `expandedFields`: source mix and richer drift hints
- `expandedActions`: experiments and signal weighting
- `extraConfig`: passive signal permissions later

#### Expert
- `fullControlAreas`: triggers and condition rules
- `dangerZones`: over-automation and nagging
- `diagnosticsVisible`: source trust and recommendation reasons later

## 3. Implementation Mapping

### Domain Models
- `AreaDefinition` fields needed: id, title, icon, summary, overview mode, seeded flag, panel capabilities
- `AreaInstance` fields needed: target score, cadence, selected tracks, flow toggles
- `LageSnapshot` fields needed: manual score, date
- `RichtungConfig` fields needed: focus line, cadence, target score
- `QuellenConfig` fields needed: selected tracks, signal mix
- `FlowConfig` fields needed: reminders, review, experiments, intensity
- `extraModels`: optional passive focus signal later

### UI State
- `TileUiState`: label, status, progress, manage affordance
- `AreaHomeUiState`: title, summary, metric row, panel entry summaries
- `LagePanelUiState`: score read and score actions
- `RichtungPanelUiState`: current focus and next move read
- `QuellenPanelUiState`: active tracks and mix
- `FlowPanelUiState`: active steering and intensity
- `ManageUiState`: selected for swap, delete confirmation open
- `ErrorUiState`: invalid profile or missing source later

### Persistence
- `entitiesNeeded`: reuse area identity, daily check, profile
- `persistedFields`: target score, cadence, selected tracks, flow toggles, intensity
- `derivedFields`: next move wording, tile status, active flow label
- `ephemeralFields`: selected tile in manage mode, sheet state
- `seedRules`: seeded area with default focus tracks
- `deleteCleanupRules`: reuse existing area/profile/daily cleanup
- `migrationNotes`: keep passive focus hints separate if introduced later

### Inputs and Dependencies
- `manualInputs`: score, target, cadence, chosen focus line, flow toggles
- `deviceSignals`: optional notification pressure later
- `permissions`: none in MVP
- `settingsDependencies`: later only if passive signals are enabled
- `defaultValues`: adaptive cadence, moderate intensity, 2 default tracks
- `futureIntegrations`: local notifications or interruption signal source

### Actions
- `createArea`: possible through generic start create flow
- `editAreaIdentity`: existing area edit flow
- `updateLage`: save manual score
- `updateRichtung`: save target, cadence, focus line
- `updateQuellen`: save tracks and mix
- `updateFlow`: save toggles and intensity
- `deleteArea`: existing delete flow with confirmation
- `reorderArea`: existing move earlier/later
- `swapArea`: existing swap flow

### Logic
- `mappersNeeded`: focus summary mapper, next move wording mapper, flow label mapper
- `projectorsNeeded`: tile and home projector
- `validatorsNeeded`: at least one valid focus track if focus is configured
- `evaluatorsNeeded`: manual score vs target comparison
- `recommendationLogicNeeded`: later optional next focus suggestion
- `fallbackRules`: use first available track if none is selected

### Shared Components
- `reuseExistingTile`: yes
- `reuseAreaHomeCard`: yes
- `reusePanelScaffold`: yes
- `reuseBottomSheetPattern`: yes
- `reuseMetricRow`: yes
- `newGenericComponentNeeded`: no for MVP

### Testing
- `mapperTests`: next move wording, flow label, source summary
- `stateTests`: empty vs configured vs warning panel states
- `repositoryTests`: none beyond existing generic area persistence
- `panelInteractionTests`: cadence, focus line, and flow toggles
- `manageFlowTests`: delete confirmation and swap clarity
- `permissionTests`: later only
- `manualChecks`: focus panel readability on phone height

### MVP Scope
- `mustBuildNow`: stay within the existing generic start state and panel architecture
- `shouldBuildSoon`: clearer passive hints
- `canDelay`: advanced trigger logic
- `doNotBuildInThisStep`: cross-app blocking or deep productivity automation

### Delivery Notes
- `implementationOrder`: definition -> wireframe -> mapping -> mapper polish -> panel polish
- `featureFlagsNeeded`: no
- `rolloutRisks`: overfitting `Fokus` can distort the generic system
- `followUpTasks`: validate if one focus line is enough for first ship
