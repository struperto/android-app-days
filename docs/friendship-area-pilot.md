# Friendship Pilot Area

This file fills the three master templates for `Freundschaft`.

## 1. Area Definition

### Identity
- `areaId`: `friends`
- `title`: `Freundschaft`
- `shortTitle`: `Freunde`
- `iconKey`: `chat`
- `category`: `relationship`
- `orderHint`: middle of seeded order
- `seededByDefault`: yes
- `userCreatable`: no in first seed set, yes in the generic system
- `complexityDefault`: `Basic`

### Purpose
- `primaryPurpose`: help the user keep friendships warm, present, and alive
- `userProblem`: friendships drift quietly when not revisited intentionally
- `expectedOutcome`: clearer sense of current connection health and next social care impulse
- `whyThisAreaExistsInDays`: `Freundschaft` proves the generic system can also hold softer, reflective areas

### Product Shape
- `overviewMode`: `reflection`
- `coreQuestion`: am I in living contact with the people that matter here
- `whatGoodLooksLike`: contact feels alive, reciprocal, and easy to resume
- `whatDriftLooksLike`: friendships fade through silence or emotional distance
- `firstUsefulRead`: whether the area feels warm, stale, or unattended
- `firstUsefulAction`: choose the next social care impulse

### Lage Model
- `hasLage`: yes
- `lageType`: `state`
- `primaryMetric`: felt connection quality now
- `secondarySignals`: recency of contact, depth, ease, reciprocity
- `emptyStateMeaning`: no reflection yet today
- `configuredStateMeaning`: current friendship feeling has been checked
- `warningStateMeaning`: distance or neglect is present
- `evaluationLogicNotes`: MVP should stay reflective and manual, not pretend to know relationship truth from contact logs

### Richtung Model
- `hasRichtung`: yes
- `focusType`: `hybrid`
- `defaultTargetShape`: one current friendship focus plus a gentle target rhythm
- `defaultCadence`: weekly
- `nextMoveLogic`: define one next social impulse, not a complex CRM task list
- `successDefinition`: one clear contact or care move feels possible
- `reviewRhythm`: weekly reflection fits better than daily forcing

### Quellen Model
- `hasQuellen`: yes
- `sourceTypesAllowed`: manual tracks, remembered people groups, optional local notes
- `manualInputAllowed`: yes
- `localSignalsAllowed`: very limited later
- `importedSourcesAllowed`: no in MVP
- `permissionSensitive`: low in MVP, medium later if communication metadata is ever considered
- `sourceTrustRules`: user reflection must stay primary
- `sourceMixMeaning`: how much the area is driven by chosen relationship tracks vs remembered signals

### Flow Model
- `hasFlow`: yes
- `supportsReminders`: yes
- `supportsReviews`: yes
- `supportsExperiments`: yes
- `supportsTriggersLater`: yes, but probably very light
- `supportsActionsLater`: yes, but only simple local nudges
- `localFlowMeaning`: gentle local prompts to remember, review, or try a social habit
- `inactiveFlowMeaning`: no prompts, only manual reflection

### Persistence
- `mustPersist`: target score, cadence, selected tracks, flow toggles, intensity, daily checks
- `canBeDerived`: tile summary, next move wording, active flow label
- `ephemeralOnly`: open sheets, temporary warnings
- `cleanupRules`: deleting area removes its profile and daily checks
- `migrationSensitivity`: low if the area stays reflection-first

### State and Logic
- `domainModelsNeeded`: identity, reflection snapshot, social source config, local flow config
- `uiStatesNeeded`: tile, home summary, 4 panel states, manage state
- `mapperOrProjectorNeeded`: reflective wording mapper, next social move mapper, flow label mapper
- `validatorsNeeded`: avoid empty source config feeling broken
- `edgeCases`: user has no recent contact, relationships feel emotionally mixed, no social track selected

### MVP Scope
- `mustBuildNow`: manual Lage, cadence, selected tracks, local flow toggles, simple next move wording
- `shouldBuildSoon`: lightweight social review experiments
- `canDelay`: communication-derived hints
- `explicitNonGoals`: contact manager, messenger overlay, relationship scoring engine

### Risks
- `confusionRisks`: users may mistake low score as moral judgment
- `overloadRisks`: too much structure can make friendship feel transactional
- `privacyRisks`: any communication-derived feature later must remain extremely minimal and local
- `automationRisks`: social reminders can feel manipulative if too frequent
- `failureStates`: the area feels cold or mechanical

### Open Decisions
- `productQuestions`: should friendship stay one broad area or support sub-circles later
- `uxQuestions`: how much of `recency` should be visible without making it feel like a tracker
- `technicalQuestions`: whether future contact-derived hints are worth the privacy and tone risk

## 2. Wireframe Planning

### Start Tile
- `primarySignal`: current reflective Lage or target if still open
- `secondarySignal`: subtle care/progress cue
- `statusStyle`: soft, quiet, and less metric-heavy than `Fokus`
- `tapAction`: open `Freundschaft` home
- `longPressAction`: enter manage mode
- `emptyTileState`: neutral with target
- `configuredTileState`: current reflective state visible
- `warningTileState`: distance is visible but not alarming

### Area Home
- `topIdentityContent`: icon, title, one-line social meaning
- `summaryLine`: a warm but minimal relationship read
- `metricRow`: Lage, Soll, Fokus, Flow
- `progressMeaning`: closeness between reflected connection state and target rhythm
- `panelEntryOrder`: Lage, Richtung, Quellen, Flow
- `panelEntrySummaries`: current warmth, next social move, active relationship tracks, active local steering
- `emptyHomeState`: no reflection today, but next move still editable
- `configuredHomeState`: one active social focus and one gentle next move

### Panel: Lage
- `primaryQuestion`: how alive do friendships here feel right now
- `headerRead`: current reflection today
- `coreCardRead`: current felt state
- `metricRowRead`: target and rhythm
- `mainActions`: choose score/state, clear
- `editControlType`: `bottom_sheet`
- `emptyState`: `offen`
- `configuredState`: one simple state or score
- `warningState`: distance without heavy explanation

### Panel: Richtung
- `primaryQuestion`: what social care move matters next
- `headerRead`: active friendship focus and cadence
- `coreCardRead`: next social impulse in one phrase
- `metricRowRead`: focus and next move
- `mainActions`: target, cadence, focus line
- `editControlType`: `bottom_sheet`
- `emptyState`: a default social focus line
- `configuredState`: `Diese Woche Kontakt` or `Tiefe zuerst`
- `warningState`: no social focus chosen

### Panel: Quellen
- `primaryQuestion`: what currently informs this friendship area
- `headerRead`: active source count
- `coreCardRead`: active relationship tracks and mix
- `metricRowRead`: focus and mix
- `mainActions`: choose tracks, set mix
- `editControlType`: `bottom_sheet`
- `emptyState`: manual reflection only
- `configuredState`: selected social tracks visible
- `permissionState`: not relevant in MVP
- `warningState`: stale supporting notes later

### Panel: Flow
- `primaryQuestion`: what gentle local steering is active
- `headerRead`: primary flow or count
- `coreCardRead`: local prompts and intensity
- `metricRowRead`: active steering and intensity
- `mainActions`: intensity, reminders, reviews, experiments
- `editControlType`: `bottom_sheet`
- `emptyState`: `Lokal`
- `configuredState`: one or two active gentle steering modes
- `inactiveFlowState`: fully manual friendship care
- `warningState`: too many prompts would feel artificial

### Setup and Edit Flows
- `firstSetupNeeded`: no
- `setupSteps`: choose social tracks and rhythm
- `decisionPoints`: how reflective vs structured the area should feel
- `defaultsShown`: a soft weekly cadence suggestion
- `permissionsRequested`: none in MVP
- `advancedBranches`: none required early

### Manage Flows
- `editEntry`: area home icon anchor or start manage dock
- `deleteConfirmationStyle`: light confirmation sheet
- `reorderBehavior`: move earlier/later
- `swapBehavior`: long press then tap second tile
- `emptyAfterDeleteBehavior`: area disappears cleanly

### Empty and Error States
- `noDataYet`: no friendship reflection today
- `notConfigured`: defaults only
- `permissionMissing`: not relevant in MVP
- `sourceMissing`: no track chosen
- `invalidConfig`: mostly impossible in MVP
- `staleData`: only relevant if later note-based signals appear

### Complexity Layers
#### Basic
- `visibleFields`: state, rhythm, social focus, flow toggles
- `visibleActions`: set reflection, choose focus, choose tracks
- `hiddenAdvancedControls`: any communication-derived hints

#### Advanced
- `expandedFields`: richer review and source weighting
- `expandedActions`: experiments around contact rhythms
- `extraConfig`: social review patterns

#### Expert
- `fullControlAreas`: probably not needed for first product shape
- `dangerZones`: over-structuring friendship
- `diagnosticsVisible`: minimal even later

## 3. Implementation Mapping

### Domain Models
- `AreaDefinition` fields needed: id, title, icon, summary, overview mode, reflection-first panel semantics
- `AreaInstance` fields needed: target score, cadence, selected tracks, flow toggles
- `LageSnapshot` fields needed: manual reflection score or state, date
- `RichtungConfig` fields needed: friendship focus and cadence
- `QuellenConfig` fields needed: selected social tracks and mix
- `FlowConfig` fields needed: reminders, review, experiments, intensity
- `extraModels`: no extra domain model needed in MVP

### UI State
- `TileUiState`: quiet status, progress, manage affordance
- `AreaHomeUiState`: warm short summary, metric row, panel summaries
- `LagePanelUiState`: reflective state read and actions
- `RichtungPanelUiState`: social focus and next move wording
- `QuellenPanelUiState`: selected social tracks and mix
- `FlowPanelUiState`: gentle steering and intensity
- `ManageUiState`: selected tile, delete confirmation
- `ErrorUiState`: mostly empty in MVP

### Persistence
- `entitiesNeeded`: reuse generic area, daily check, and profile persistence
- `persistedFields`: target, cadence, selected tracks, signal blend, flow toggles, intensity
- `derivedFields`: reflective summary wording, next social move, tile label
- `ephemeralFields`: sheet and manage states
- `seedRules`: use seeded defaults with weekly-leaning cadence
- `deleteCleanupRules`: existing cleanup path
- `migrationNotes`: keep later social hints optional and non-blocking

### Inputs and Dependencies
- `manualInputs`: reflection score, target, cadence, focus, tracks, flow toggles
- `deviceSignals`: not required in MVP
- `permissions`: none in MVP
- `settingsDependencies`: none in MVP
- `defaultValues`: weekly-friendly cadence, moderate intensity, 2 social tracks
- `futureIntegrations`: maybe local notes or contact recency hints later, only if tone survives

### Actions
- `createArea`: generic create flow
- `editAreaIdentity`: existing edit flow
- `updateLage`: manual reflection save
- `updateRichtung`: target, cadence, focus
- `updateQuellen`: selected tracks and mix
- `updateFlow`: toggles and intensity
- `deleteArea`: confirmed delete flow
- `reorderArea`: existing move flow
- `swapArea`: existing swap flow

### Logic
- `mappersNeeded`: reflective summary mapper, next social move mapper, flow label mapper
- `projectorsNeeded`: tile and home projector
- `validatorsNeeded`: handle empty social tracks without feeling broken
- `evaluatorsNeeded`: manual score vs target
- `recommendationLogicNeeded`: gentle next move suggestion later
- `fallbackRules`: default to the first social track if none selected

### Shared Components
- `reuseExistingTile`: yes
- `reuseAreaHomeCard`: yes
- `reusePanelScaffold`: yes
- `reuseBottomSheetPattern`: yes
- `reuseMetricRow`: yes
- `newGenericComponentNeeded`: no for MVP

### Testing
- `mapperTests`: reflective wording, next move wording, flow labels
- `stateTests`: empty/configured/warning reflective states
- `repositoryTests`: generic start repository tests cover persistence
- `panelInteractionTests`: cadence and flow toggles
- `manageFlowTests`: delete confirmation and swap clarity
- `permissionTests`: not needed in MVP
- `manualChecks`: friendship should feel warm, not transactional

### MVP Scope
- `mustBuildNow`: stay on the existing generic start panel and manage architecture
- `shouldBuildSoon`: softer review experiments
- `canDelay`: contact-derived hints
- `doNotBuildInThisStep`: any feature that turns friendship into a CRM or messaging hub

### Delivery Notes
- `implementationOrder`: definition -> wireframe -> mapping -> only then selective UI or mapper changes
- `featureFlagsNeeded`: no
- `rolloutRisks`: friendship can expose where generic fields are too score-heavy
- `followUpTasks`: validate whether relationship areas need a non-score Lage variant later
