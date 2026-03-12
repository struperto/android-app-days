# Vitalitaet Pilot Area

This file fills the three master templates for `Vitalitaet`.

## 1. Area Definition

### Identity
- `areaId`: `vitality`
- `title`: `Vitalitaet`
- `shortTitle`: `Vitalitaet`
- `iconKey`: `heart`
- `category`: `foundation`
- `orderHint`: first seeded area
- `seededByDefault`: yes
- `userCreatable`: no in first seed set, yes in the generic system
- `complexityDefault`: `Basic`

### Purpose
- `primaryPurpose`: keep body energy and recovery conditions in a usable rhythm
- `userProblem`: energy drops, basic care drifts, and the user notices too late
- `expectedOutcome`: better awareness of sleep, energy, movement, food, and hydration as one local care area
- `whyThisAreaExistsInDays`: `Vitalitaet` anchors the calm local-first life-area model with a more signal-like area

### Product Shape
- `overviewMode`: `signal`
- `coreQuestion`: does my body state feel supportive enough for the day
- `whatGoodLooksLike`: body support feels steady and recoverable
- `whatDriftLooksLike`: low energy, poor rhythm, or neglected basics
- `firstUsefulRead`: current Lage and one bodily focus line
- `firstUsefulAction`: notice what basic care needs attention next

### Lage Model
- `hasLage`: yes
- `lageType`: `hybrid`
- `primaryMetric`: subjective vitality score now
- `secondarySignals`: sleep, movement, energy, hydration, nutrition
- `emptyStateMeaning`: no vitality read yet today
- `configuredStateMeaning`: current body state is captured
- `warningStateMeaning`: body support feels weak or drifting
- `evaluationLogicNotes`: keep MVP manual-first; later passive signals may enrich but should not replace self-read

### Richtung Model
- `hasRichtung`: yes
- `focusType`: `hybrid`
- `defaultTargetShape`: target score plus one current care focus
- `defaultCadence`: adaptive
- `nextMoveLogic`: choose the most useful body support action first
- `successDefinition`: the next care move is clear and small enough to do
- `reviewRhythm`: daily light read, weekly pattern review later

### Quellen Model
- `hasQuellen`: yes
- `sourceTypesAllowed`: manual tracks, local health or sensor signals later, lightweight notes
- `manualInputAllowed`: yes
- `localSignalsAllowed`: yes later
- `importedSourcesAllowed`: no in MVP
- `permissionSensitive`: medium later if health sources are connected
- `sourceTrustRules`: explicit user input wins; passive sources only support
- `sourceMixMeaning`: how much vitality read is manually chosen vs passively sensed

### Flow Model
- `hasFlow`: yes
- `supportsReminders`: yes
- `supportsReviews`: yes
- `supportsExperiments`: yes
- `supportsTriggersLater`: yes
- `supportsActionsLater`: yes
- `localFlowMeaning`: gentle local care steering, not medical intervention
- `inactiveFlowMeaning`: read-only manual area without nudges

### Persistence
- `mustPersist`: target score, cadence, selected tracks, flow toggles, intensity, daily checks
- `canBeDerived`: vitality summary, next move copy, tile progress
- `ephemeralOnly`: sheet state, active manage selection
- `cleanupRules`: deleting area removes attached profile and daily checks
- `migrationSensitivity`: medium if later health sources are persisted or normalized

### State and Logic
- `domainModelsNeeded`: area identity, vitality snapshot, vitality source config, local flow config
- `uiStatesNeeded`: tile, area home, all 4 panels, permission or source state later
- `mapperOrProjectorNeeded`: next care move mapper, source summary mapper, flow label mapper
- `validatorsNeeded`: source config must handle zero passive inputs cleanly
- `edgeCases`: passive health sources missing, user score and passive hint diverge, no care track selected

### MVP Scope
- `mustBuildNow`: manual Lage, target score, cadence, care tracks, local flow toggles
- `shouldBuildSoon`: optional passive source integration
- `canDelay`: richer health diagnostics or condition rules
- `explicitNonGoals`: medical dashboard, quantified-self wall, diagnosis

### Risks
- `confusionRisks`: vitality can become too broad if all body topics are shown equally
- `overloadRisks`: too many health-like controls would break calm product language
- `privacyRisks`: health-related sources need very explicit local-first framing
- `automationRisks`: reminders can become nagging or moralizing
- `failureStates`: user feels judged instead of supported

### Open Decisions
- `productQuestions`: should vitality stay broad or split later into sleep and body support
- `uxQuestions`: how visible should passive health sources be when present
- `technicalQuestions`: whether future health data is stored raw, summarized, or projected only

## 2. Wireframe Planning

### Start Tile
- `primarySignal`: current Lage or target if still open
- `secondarySignal`: subtle energy progress cue
- `statusStyle`: soft but clear, more bodily than task-like
- `tapAction`: open `Vitalitaet` home
- `longPressAction`: enter manage mode
- `emptyTileState`: neutral tile with `Ziel`
- `configuredTileState`: current vitality state visible
- `warningTileState`: quiet drift state, no medical alarm framing

### Area Home
- `topIdentityContent`: icon, title, short bodily meaning
- `summaryLine`: body support sentence in one line
- `metricRow`: Lage, Soll, Fokus, Flow
- `progressMeaning`: closeness between vitality state and target
- `panelEntryOrder`: Lage, Richtung, Quellen, Flow
- `panelEntrySummaries`: current state, next care move, active care sources, active local steering
- `emptyHomeState`: no vitality read yet but direction and sources still editable
- `configuredHomeState`: one care focus is visible and the area feels calm

### Panel: Lage
- `primaryQuestion`: how supportive does my body state feel right now
- `headerRead`: current Lage today
- `coreCardRead`: vitality score
- `metricRowRead`: target and cadence
- `mainActions`: choose score, clear score
- `editControlType`: `bottom_sheet`
- `emptyState`: `offen`
- `configuredState`: 1 to 5 score
- `warningState`: low support without long copy

### Panel: Richtung
- `primaryQuestion`: what body-support action matters next
- `headerRead`: current care focus and cadence
- `coreCardRead`: next care move as one short phrase
- `metricRowRead`: focus line and next move
- `mainActions`: target, cadence, care focus
- `editControlType`: `bottom_sheet`
- `emptyState`: first recommended care track
- `configuredState`: `Heute Schlaf`, `Diese Woche Bewegung`, or `Energie zuerst`
- `warningState`: no clear care focus selected

### Panel: Quellen
- `primaryQuestion`: what currently informs vitality
- `headerRead`: active source count
- `coreCardRead`: active care tracks and source mix
- `metricRowRead`: care focus and mix
- `mainActions`: choose tracks, set mix
- `editControlType`: `bottom_sheet`
- `emptyState`: manual only
- `configuredState`: selected care tracks are visible
- `permissionState`: health or sensor permission missing later
- `warningState`: passive source is stale or absent

### Panel: Flow
- `primaryQuestion`: what local care steering is active
- `headerRead`: main active steering or count
- `coreCardRead`: local flow plus intensity
- `metricRowRead`: active steering and intensity
- `mainActions`: set intensity, toggle reminders/review/experiments
- `editControlType`: `bottom_sheet`
- `emptyState`: `Lokal`
- `configuredState`: one or two active care steering modes
- `inactiveFlowState`: no nudges, only manual care
- `warningState`: too much nudging could feel pushy

### Setup and Edit Flows
- `firstSetupNeeded`: no
- `setupSteps`: choose care tracks, optional target, cadence
- `decisionPoints`: whether passive health sources are connected later
- `defaultsShown`: a useful body-care starter set
- `permissionsRequested`: none in MVP
- `advancedBranches`: passive local signals later

### Manage Flows
- `editEntry`: area home edit anchor or start manage dock
- `deleteConfirmationStyle`: light confirmation sheet
- `reorderBehavior`: move earlier/later
- `swapBehavior`: long press then tap second tile
- `emptyAfterDeleteBehavior`: tile disappears cleanly

### Empty and Error States
- `noDataYet`: no vitality read yet today
- `notConfigured`: only defaults are active
- `permissionMissing`: health source unavailable later
- `sourceMissing`: no active track selected
- `invalidConfig`: impossible only if profile fields break
- `staleData`: passive source older than useful rhythm later

### Complexity Layers
#### Basic
- `visibleFields`: score, one care focus, care tracks, flow toggles
- `visibleActions`: set score, set focus, choose tracks
- `hiddenAdvancedControls`: passive source rules

#### Advanced
- `expandedFields`: source mix and passive source confidence
- `expandedActions`: experiments around rhythm or recovery
- `extraConfig`: permissions and signal weighting later

#### Expert
- `fullControlAreas`: triggers by low-energy pattern later
- `dangerZones`: too much health-style complexity
- `diagnosticsVisible`: raw signal details later if ever needed

## 3. Implementation Mapping

### Domain Models
- `AreaDefinition` fields needed: id, title, icon, summary, overview mode, panel capabilities, permission sensitivity
- `AreaInstance` fields needed: target score, cadence, selected care tracks, flow toggles
- `LageSnapshot` fields needed: manual vitality score, date
- `RichtungConfig` fields needed: next care focus, cadence, target
- `QuellenConfig` fields needed: selected care tracks, source mix, optional signal availability later
- `FlowConfig` fields needed: reminders, review, experiments, intensity
- `extraModels`: optional local vitality signal summary later

### UI State
- `TileUiState`: status label, progress, care icon, manage cues
- `AreaHomeUiState`: summary, metric row, panel summaries
- `LagePanelUiState`: vitality score and score actions
- `RichtungPanelUiState`: current care focus and next move
- `QuellenPanelUiState`: active tracks and mix
- `FlowPanelUiState`: active steering and intensity
- `ManageUiState`: selected for swap, pending delete confirmation
- `ErrorUiState`: permission missing or passive source stale later

### Persistence
- `entitiesNeeded`: reuse generic area, daily check, and profile entities
- `persistedFields`: target score, cadence, selected tracks, signal blend, flow toggles, intensity
- `derivedFields`: summary copy, next move wording, tile status, flow labels
- `ephemeralFields`: manage mode selection and sheet state
- `seedRules`: vitality gets seed defaults and starter tracks
- `deleteCleanupRules`: reuse existing cleanup
- `migrationNotes`: keep future passive source storage isolated from current area profile

### Inputs and Dependencies
- `manualInputs`: vitality score, target, cadence, focus, tracks, flow toggles
- `deviceSignals`: optional health or body-related local signals later
- `permissions`: none in MVP, health-related later
- `settingsDependencies`: source permissions later
- `defaultValues`: adaptive cadence, intensity 3, two default care tracks
- `futureIntegrations`: Health Connect or local body-related signals later

### Actions
- `createArea`: generic start create flow
- `editAreaIdentity`: existing edit flow
- `updateLage`: manual score save
- `updateRichtung`: target, cadence, focus
- `updateQuellen`: selected tracks and mix
- `updateFlow`: toggles and intensity
- `deleteArea`: confirmed delete flow
- `reorderArea`: existing move flow
- `swapArea`: existing swap flow

### Logic
- `mappersNeeded`: next care move wording, source summary, flow label
- `projectorsNeeded`: tile and home state projector
- `validatorsNeeded`: avoid broken state if no track is selected
- `evaluatorsNeeded`: manual score vs target
- `recommendationLogicNeeded`: optional care-focus recommendation later
- `fallbackRules`: first available care track if focus is missing

### Shared Components
- `reuseExistingTile`: yes
- `reuseAreaHomeCard`: yes
- `reusePanelScaffold`: yes
- `reuseBottomSheetPattern`: yes
- `reuseMetricRow`: yes
- `newGenericComponentNeeded`: no for MVP

### Testing
- `mapperTests`: next care move wording, source summary, flow label
- `stateTests`: empty/configured/warning vitality states
- `repositoryTests`: existing generic persistence tests are enough for MVP
- `panelInteractionTests`: score, focus, and flow toggles
- `manageFlowTests`: delete confirmation and swap hinting
- `permissionTests`: later only
- `manualChecks`: calm vitality feeling on phone screen

### MVP Scope
- `mustBuildNow`: use the current shared start architecture without custom vitality-only flows
- `shouldBuildSoon`: optional passive source integration
- `canDelay`: richer diagnostics and advanced triggers
- `doNotBuildInThisStep`: any medicalized dashboard behavior

### Delivery Notes
- `implementationOrder`: definition -> wireframe -> mapping -> passive-source decision later
- `featureFlagsNeeded`: no
- `rolloutRisks`: vitality can easily over-pull the generic model toward telemetry
- `followUpTasks`: decide if health source support is part of V1 or a later local plugin
