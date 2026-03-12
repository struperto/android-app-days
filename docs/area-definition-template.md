# Area Definition Template

Use this template before any new area is designed or implemented.

Goal:
- describe one area in product and domain terms
- keep `Start` generic instead of hard-wiring special cases
- make later UI, state, persistence, and MVP decisions easier

Scope:
- local Android app
- `Start` mode
- area detail contract: `Lage`, `Richtung`, `Quellen`, `Flow`

## 1. Identity
- `areaId`:
- `title`:
- `shortTitle`:
- `iconKey`:
- `category`:
- `orderHint`:
- `seededByDefault`: yes | no
- `userCreatable`: yes | no
- `complexityDefault`: Basic | Advanced | Expert

## 2. Purpose
- `primaryPurpose`:
- `userProblem`:
- `expectedOutcome`:
- `whyThisAreaExistsInDays`:

## 3. Product Shape
- `overviewMode`: signal | plan | reflection | hybrid
- `coreQuestion`:
- `whatGoodLooksLike`:
- `whatDriftLooksLike`:
- `firstUsefulRead`:
- `firstUsefulAction`:

## 4. Lage Model
- `hasLage`: yes | no
- `lageType`: score | state | checklist | hybrid
- `primaryMetric`:
- `secondarySignals`:
- `emptyStateMeaning`:
- `configuredStateMeaning`:
- `warningStateMeaning`:
- `evaluationLogicNotes`:

## 5. Richtung Model
- `hasRichtung`: yes | no
- `focusType`: target | cadence | next_step | hybrid
- `defaultTargetShape`:
- `defaultCadence`:
- `nextMoveLogic`:
- `successDefinition`:
- `reviewRhythm`:

## 6. Quellen Model
- `hasQuellen`: yes | no
- `sourceTypesAllowed`:
- `manualInputAllowed`: yes | no
- `localSignalsAllowed`: yes | no
- `importedSourcesAllowed`: yes | no
- `permissionSensitive`: yes | no
- `sourceTrustRules`:
- `sourceMixMeaning`:

## 7. Flow Model
- `hasFlow`: yes | no
- `supportsReminders`: yes | no
- `supportsReviews`: yes | no
- `supportsExperiments`: yes | no
- `supportsTriggersLater`: yes | no
- `supportsActionsLater`: yes | no
- `localFlowMeaning`:
- `inactiveFlowMeaning`:

## 8. Persistence
- `mustPersist`:
- `canBeDerived`:
- `ephemeralOnly`:
- `cleanupRules`:
- `migrationSensitivity`:

## 9. State and Logic
- `domainModelsNeeded`:
- `uiStatesNeeded`:
- `mapperOrProjectorNeeded`:
- `validatorsNeeded`:
- `edgeCases`:

## 10. MVP Scope
- `mustBuildNow`:
- `shouldBuildSoon`:
- `canDelay`:
- `explicitNonGoals`:

## 11. Risks
- `confusionRisks`:
- `overloadRisks`:
- `privacyRisks`:
- `automationRisks`:
- `failureStates`:

## 12. Open Decisions
- `productQuestions`:
- `uxQuestions`:
- `technicalQuestions`:
