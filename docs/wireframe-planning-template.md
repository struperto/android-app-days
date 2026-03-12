# Wireframe Planning Template

Use this template after the area definition is stable enough to sketch screens.

Goal:
- plan the visible behavior of one area in `Start`
- keep screens text-light, calm, and reusable
- avoid inventing one-off UI patterns per area

Rules:
- preserve the existing `Start` visual language
- prefer clear signals over long explanation copy
- build on the shared panel system: `Lage`, `Richtung`, `Quellen`, `Flow`

## 1. Start Tile
- `primarySignal`:
- `secondarySignal`:
- `statusStyle`:
- `tapAction`:
- `longPressAction`:
- `emptyTileState`:
- `configuredTileState`:
- `warningTileState`:

## 2. Area Home
- `topIdentityContent`:
- `summaryLine`:
- `metricRow`:
- `progressMeaning`:
- `panelEntryOrder`:
- `panelEntrySummaries`:
- `emptyHomeState`:
- `configuredHomeState`:

## 3. Panel: Lage
- `primaryQuestion`:
- `headerRead`:
- `coreCardRead`:
- `metricRowRead`:
- `mainActions`:
- `editControlType`: chips | dropdown | bottom_sheet | slider | mixed
- `emptyState`:
- `configuredState`:
- `warningState`:

## 4. Panel: Richtung
- `primaryQuestion`:
- `headerRead`:
- `coreCardRead`:
- `metricRowRead`:
- `mainActions`:
- `editControlType`:
- `emptyState`:
- `configuredState`:
- `warningState`:

## 5. Panel: Quellen
- `primaryQuestion`:
- `headerRead`:
- `coreCardRead`:
- `metricRowRead`:
- `mainActions`:
- `editControlType`:
- `emptyState`:
- `configuredState`:
- `permissionState`:
- `warningState`:

## 6. Panel: Flow
- `primaryQuestion`:
- `headerRead`:
- `coreCardRead`:
- `metricRowRead`:
- `mainActions`:
- `editControlType`:
- `emptyState`:
- `configuredState`:
- `inactiveFlowState`:
- `warningState`:

## 7. Setup and Edit Flows
- `firstSetupNeeded`: yes | no
- `setupSteps`:
- `decisionPoints`:
- `defaultsShown`:
- `permissionsRequested`:
- `advancedBranches`:

## 8. Manage Flows
- `editEntry`:
- `deleteConfirmationStyle`:
- `reorderBehavior`:
- `swapBehavior`:
- `emptyAfterDeleteBehavior`:

## 9. Empty and Error States
- `noDataYet`:
- `notConfigured`:
- `permissionMissing`:
- `sourceMissing`:
- `invalidConfig`:
- `staleData`:

## 10. Complexity Layers
### Basic
- `visibleFields`:
- `visibleActions`:
- `hiddenAdvancedControls`:

### Advanced
- `expandedFields`:
- `expandedActions`:
- `extraConfig`:

### Expert
- `fullControlAreas`:
- `dangerZones`:
- `diagnosticsVisible`:
